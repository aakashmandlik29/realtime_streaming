import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.{Duration, Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent

object SparkStreamingExample {
  def main(args:Array[String]): Unit ={
    println("Hello, world")
    val conf = new SparkConf().setMaster("local[2]").setAppName("Spark Streaming Example")
    val ssc = new StreamingContext(conf, Seconds(60))
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "127.0.0.1:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "airlines",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean),
      "internal.leave.group.on.close" -> (false: java.lang.Boolean)
    )

    val topics = Array("airlines")
    val dstream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )
    dstream.map(record => (record.key, record.value))
    dstream.foreachRDD { rdd =>
      rdd.foreach { record =>
        println(record.value()) // executed at the worker
      }
    }

//    dstream.countByValueAndWindow(new Duration(60000),new Duration(60000),1)
    ssc.start()
    ssc.awaitTermination()
  }
}
