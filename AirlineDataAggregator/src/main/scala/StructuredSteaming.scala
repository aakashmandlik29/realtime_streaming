
import java.util
import com.paulgoldbaum.influxdbclient._
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt


object StructuredSteaming {

  def main(args: Array[String]): Unit ={

    //  Kafka Connection Parameters
    val r = scala.util.Random
    val groupId = s"stream-checker-v${r.nextInt.toString}"
    val kafkaParams: Map[String, Object] = Map[String, Object](
      "bootstrap.servers" -> "127.0.0.1:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> groupId,
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )
    val topics = Array("airlines")

    //  InfluxDb connection
    val influxdb = InfluxDB.connect("127.0.0.1", 8086)
    println("Influx databases : "+ Await.result(influxdb.showDatabases(), 5 seconds))
    val inf_database = influxdb.selectDatabase("airlines")

    val conf = new SparkConf().setMaster("local[2]").setAppName("Spark Streaming ")
    //    Start streaming spark context and have batch duration of 60 seconds.
    val ssc = new StreamingContext(conf, Seconds(60))
    //  Kafka connection via Spark streaming
    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )

    //  We get a bunch of metadata from Kafka like partitions, timestamps, etc. Only interested in message payload
    val messages: DStream[String] = stream.map(record => record.value)

    //  Accumulate batch of messages into RDD
    messages.foreachRDD { rdd =>
      // Transform RDD into DataFrame
      val spark = SparkSession.builder.config(rdd.sparkContext.getConf).getOrCreate()
      import spark.implicits._
      val rawDF = rdd.toDF("record")
      rawDF.createOrReplaceTempView("raw_view")
      //  Create dataframe using Temporary view and deserialize the data.
      val raw_df = spark.sql(
        "select split(record,',')[0] as Carrier, " +
          "split(record,',')[1] as Date_of_Journey," +
          "split(record,',')[2] as Source, " +
          "split(record,',')[3] as Destination, " +
          "split(record,',')[4] as Route," +
          "split(record,',')[5] as Dep_Time, " +
          "split(record,',')[6] as Arrival_Time, " +
          "split(record,',')[7] as Duration," +
          "split(record,',')[8] as Total_Stops, " +
          "split(record,',')[9] as Additional_Info, " +
          "split(record,',')[10] as Price " +
          "from raw_view")
      //      Cache the dataframe since can work fast if in-memory.
      raw_df.cache()
      //      Transform and do aggregation on each the metrices.
      var carrier_wise_cnt_df = raw_df.filter("Carrier is not null and Carrier !=''").groupBy("Carrier").count()
      val source_wise_cnt_df = raw_df.filter("Source is not null and Source !=''").groupBy("Source").count()
      val destination_wise_cnt_df = raw_df.filter("Destination is not null and Destination !=''").groupBy("Destination").count()
      val stops_wise_cnt_df = raw_df.filter("Total_Stops is not null and Total_Stops !=''").groupBy("Total_Stops").count()
      val route_wise_cnt_df = raw_df.filter("Route is not null and Route !=''").groupBy("Route").count()

      var df_arr= new util.ArrayList[(DataFrame, String)]()
      df_arr.add((carrier_wise_cnt_df, "carrier"))
      df_arr.add((source_wise_cnt_df,"source"))
      df_arr.add((destination_wise_cnt_df,"destination"))
      df_arr.add((stops_wise_cnt_df,"stops"))
      df_arr.add((route_wise_cnt_df,"route"))

      val df_it = df_arr.iterator
      //      For each dataframe send the data to individual Influx measurement(anologous to table).
      while (df_it.hasNext){
        var influxData = new ListBuffer[Point]()
        var influxData_brcast = spark.sparkContext.broadcast(influxData)
        val df_this = df_it.next()
        val df= df_this._1
        val name= spark.sparkContext.broadcast(df_this._2)
        //        Iterating over dataframe to send the data to Influx Db
        df.foreach(row => {
          val point = Point(name.value)
            .addTag(name.value, row.getString(0))
            .addField("count", row.getLong(1))
          influxData_brcast.value += point
          print("")
        }
        )
        val influxDataList = influxData_brcast.value.toList
        println("Records to write : "+ influxDataList.size)
        inf_database.bulkWrite(influxDataList)
        influxData_brcast.destroy()
      }
    }
    ssc.start()
    ssc.awaitTermination()
  }
}
