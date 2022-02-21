# Airline Trends in real time
	**Author : Aakash Mandlik**


# Problem statement

Building a real time dashboard to see the trends of airline data in real time. The dashboard should be refreshed in a few seconds and the graphs and charts should be refreshed in accordance with the real time data. 

# Prerequisites 
Docker basics : Docker is a set of platform as a service products that use OS-level virtualization to deliver software in packages called containers
Kafka : A messaging queue which keeps the data enabling the producers and consumers to work independently. The async way of using the kafka makes it powerful with respect to the traditional producer consumer way. 
Apache spark : Apache Spark is an open-source unified analytics engine for large-scale data processing. We will be familiarized with spark standalone clusters. Also knowledge of spark RDD and dataframes will be a plus. 
InfluxDB : InfluxDB is an open-source time series database used for storing time series data mainly used for monitoring and real time trends. 
Grafana or Chronograf : Both are dashboarding tools. Their usage depends on our use case. Grafana is mainly used when we have multiple data sources like InfluxDB or Redshift or BigQuery to get the data from and use the data in a single dashboard. While Chronograf can only be used when the data source is influxDb. 

Let’s look at the source data we used for our analysis.  

# Source data

The data we are using here is airline data. Attached is the example snap of the data: 



What does the data say : 
Each row here is giving information of a single flight. 
What does the first row of data say :
It's an indigo flight. 
Date of journey of 24th March 2019. 
It's a direct(non-stop) flight from Bangalore to New Delhi. 
Flight duration is 2 hours and 50 minutes. 
Price is 3897 INR. 

Now as we have seen our source data, let's look at what requirements or problems we can solve using this data in real time.

# Requirement or KPIs
As the data is flowing in Kafka in real time, we want to create a dashboard with following basic trends. In last 15 minutes
Most flying carriers(Indigo, Jet Airways etc)?
Airports with maximum flight departures. 
Airports with maximum flight arrivals.
Route with maximum flight flying. 
Trends on the basis of individual entity: 
Sparkline showing Indigo flights for the last 15 minutes. . 
Sparkline showing New Delhi departures for the last 15 minutes. 
Sparkline showing Bangalore arrivals for the last 15 minutes. 

As we have concrete requirements in place, let's look at how we can fulfill these requirements. We will go at high level about what frameworks we can use to achieve our goal. 

# High level Design

We already went through what each of these frameworks are individually. 
We will now go through how these can come together to give a wonderful real time user experience. 

## Steps :
Producing data to Kafka topic
Reading the data from Kafka topic in microbatches using Apache spark streaming. 
After doing aggregations, we need to write the data to influxdb(airlines database). 
Create a data source for influxDb on chronograf. 
Create dashboard and underlying panels to see real time trends. 

As we have discussed our high level, let's go into the low level approach. We will drill down into each step we pointed at a high level. Stay excited. 

# Low level approach
Setup : 
Docker : 
Download docker desktop. For CLI use pip install docker. 
Kafka :
Run docker image for setting up kafka and zookeeper. 
Image : docker run -p 2181:2181 -p 9092:9092 -e ADVERTISED_HOST=127.0.0.1  -e NUM_PARTITIONS=10 johnnypark/kafka-zookeeper
After #1 , the kafka broker will be available on localhost:9092. 
Download kafka binaries for producing kafka messages(using Kafka CLI) : https://kafka.apache.org/downloads
Spark : 
Setup a spark standalone cluster using docker image. 
After #1, the spark master will be available on spark://localhost:7077. 
Snap : 

Download IntelliJ or other IDE for spark development. 
Create a scala maven project in IntelliJ to start with. 

Influxdb , Chronograf and Grafana:
Setup influxDb , chronograf and grafana using docker image. 
After #1, 
InfluxDb will be accessible on http://localhost:8086. 
Chronograf will be accessible on http://localhost:3004/.
Grafana will be accessible on http://localhost:3003/. 
Create a database called airlines on InfluxDb.
On Chronograf, there is no need to create a data source for the above influxdb database. 

## Next steps :
Step 1 : Producing data to Kafka topic
  Download source data for analysis and break the bigger file into 10 smaller files: https://www.kaggle.com/nikhilmittal/flight-fare-prediction-mh/download
  Creating kafka_producer.sh bash script to continuously write the data to kafka topic: 

  #!/bin/bash
  echo "Kafka producer!"

  exitfn () {
      trap SIGINT        # Restore signal handling for SIGINT
      echo 'Aarghh!!'    # Growl at user,
      kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic airlines
      exit                     #   then exit script.
  }

  trap "echo 'exit called';kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic airlines; exit" ERR EXIT

  while true
  do
          kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic airlines < airlines_1.csv
          echo "Sleep for 15 secs"
          sleep 15
          kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic airlines < airlines_2.csv
          echo "Sleep for 15 secs"
          sleep 15
          kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic airlines < airlines_3.csv
          echo "Sleep for 15 secs"
          sleep 15
          kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic airlines < airlines_4.csv
          echo "Sleep for 15 secs"
          sleep 15
          kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic airlines < airlines_5.csv
          echo "Sleep for 15 secs"
          sleep 15
          kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic airlines < airlines_6.csv
          echo "Sleep for 15 secs"
          sleep 15
          kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic airlines < airlines_7.csv
          echo "Sleep for 15 secs"
          sleep 15
          kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic airlines < airlines_8.csv
          echo "Sleep for 15 secs"
          sleep 15
          kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic airlines < airlines_9.csv
          echo "Sleep for 15 secs"
          sleep 15

  done


Step 2 : Creating spark project to read from kafka, aggregate and write to InfluxDb. 
  Create a scala maven project and add dependencies in pom.xml . 

  <?xml version="1.0" encoding="UTF-8"?>
  <project xmlns="http://maven.apache.org/POM/4.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
     <modelVersion>4.0.0</modelVersion>

     <groupId>org.example</groupId>
     <artifactId>AirlineDataAggregator</artifactId>
     <version>1.0-SNAPSHOT</version>
     <dependencies>
         <!-- https://mvnrepository.com/artifact/org.apache.spark/spark-sql -->
         <dependency>
             <groupId>org.apache.spark</groupId>
             <artifactId>spark-sql_2.12</artifactId>
             <version>3.2.0</version>
         </dependency>
         <!-- https://mvnrepository.com/artifact/org.apache.spark/spark-core -->
         <dependency>
             <groupId>org.apache.spark</groupId>
             <artifactId>spark-core_2.12</artifactId>
             <version>3.2.0</version>
         </dependency>
         <!-- https://mvnrepository.com/artifact/org.apache.spark/spark-streaming -->
         <dependency>
             <groupId>org.apache.spark</groupId>
             <artifactId>spark-streaming_2.12</artifactId>
             <version>3.2.0</version>
         </dependency>
         <dependency>
             <groupId>org.apache.spark</groupId>
             <artifactId>spark-streaming-kafka-0-10_2.12</artifactId>
             <version>3.2.0</version>
         </dependency>
         <dependency>
             <groupId>com.paulgoldbaum</groupId>
             <artifactId>scala-influxdb-client_2.12</artifactId>
             <version>0.6.1</version>
         </dependency>
     </dependencies>

  </project>



  Writing spark code to read the data from kafka and aggregate it for each microBatch on different matrices and write to Influxdb. Added comments for understanding. 

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

     val conf = new SparkConf().setMaster("spark://localhost:7077").setAppName("Spark Streaming ")
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
       //      For each dataframe send the data to individual Influx measurement(analogous to table).
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


  After the changes are done ,we can run the spark application to read the data from         Kafka, aggregate it and write to InfluxDb. 

Step 3 : Creating dashboard on chronograf using influxDb data. 

  Now as the data is being sent to InfluxDb every 60 seconds ,we can create a dashboard to see the real time trends. 
  Following is the dashboard which shows carrier wise flights from airlines dataset. Here are the stats visible from the dashboard: 
  At 16:16 Jet airways had 1180 flights in the last one minute. Which dropped to 1060 in the next minute. 
  At 16:16 indigo had 666 flights which dropped 535 in the next minute. 
  Carrier wise comparison can also be made. For example JetAirways is the most booked carrier , while Vistara is the least booked carrier. 



The same way a panel can be created for each of the other matrices like sources, destinations, stops etc. 



# Conclusion

The data is changing at great speed and being able to transform and capitalize using data is the biggest thing every organization requires now. The data drives the business and using the data in real time and making decisions is icing on the cake. In this lecture we focused more on how we can achieve a minimal viable product for our use case. Eventually for a production use case you would need bigger clusters and real time monitoring of services running. This lecture has answered the initial what, why and hows of real time streaming. Now I wish you luck to continue on exploring the beautiful path of data streaming. 


