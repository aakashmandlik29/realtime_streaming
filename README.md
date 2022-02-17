# realtime_streaming

Building a real time dashboard to see the trends of airline data in real time. The dashboard should be refreshed in a few seconds and the graphs and charts should be refreshed in accordance with the real time data. 

**Prerequisites 

Docker basics : Docker is a set of platform as a service products that use OS-level virtualization to deliver software in packages called containers
Kafka : A messaging queue which keeps the data enabling the producers and consumers to work independently. The async way of using the kafka makes it powerful with respect to the traditional producer consumer way. 
Apache spark : Apache Spark is an open-source unified analytics engine for large-scale data processing. We will be familiarized with spark standalone clusters. Also knowledge of spark RDD and dataframes will be a plus. 
InfluxDB : InfluxDB is an open-source time series database used for storing time series data mainly used for monitoring and real time trends. 
Grafana or Chronograf : Both are dashboarding tools. Their usage depends on our use case. Grafana is mainly used when we have multiple data sources like InfluxDB or Redshift or BigQuery to get the data from and use the data in a single dashboard. While Chronograf can only be used when the data source is influxDb. 

Let’s look at the source data we used for our analysis.  


**Source data 

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

**Requirement or KPIs 

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

**High level Design

We already went through what each of these frameworks are individually. 
We will now go through how these can come together to give a wonderful real time user experience. 

Steps : 
Producing data to Kafka topic
Reading the data from Kafka topic in microbatches using Apache spark streaming. 
After doing aggregations, we need to write the data to influxdb(airlines database). 
Create a data source for influxDb on chronograf. 
Create dashboard and underlying panels to see real time trends. 

As we have discussed our high level, let's go into the low level approach. We will drill down into each step we pointed at a high level. Stay excited. 

**Low level approach

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

**Next steps : 

Step 1 : Producing data to Kafka topic
Download source data for analysis and break the bigger file into 10 smaller files: https://www.kaggle.com/nikhilmittal/flight-fare-prediction-mh/download
Creating kafka_producer.sh bash script to continuously write the data to kafka topic: 

Step 2 : Creating spark project to read from kafka, aggregate and write to InfluxDb. 
Create a scala maven project and add dependencies in pom.xml . 
Writing spark code to read the data from kafka and aggregate it for each microBatch on different matrices and write to Influxdb. Added comments for understanding. 
After the changes are done ,we can run the spark application to read the data from Kafka, aggregate it and write to InfluxDb. 

Step 3 : Creating dashboard on chronograf using influxDb data. 

Now as the data is being sent to InfluxDb every 60 seconds ,we can create a dashboard to see the real time trends. 
Following is the dashboard which shows carrier wise flights from airlines dataset. Here are the stats visible from the dashboard: 
At 16:16 Jet airways had 1180 flights in the last one minute. Which dropped to 1060 in the next minute. 
At 16:16 indigo had 666 flights which dropped 535 in the next minute. 
Carrier wise comparison can also be made. For example JetAirways is the most booked carrier , while Vistara is the least booked carrier. 
The same way a panel can be created for each of the other matrices like sources, destinations, stops etc. 

Conclusion

The data is changing at great speed and being able to transform and capitalize using data is the biggest thing every organization requires now. The data drives the business and using the data in real time and making decisions is icing on the cake. In this lecture we focused more on how we can achieve a minimal viable product for our use case. Eventually for a production use case you would need bigger clusters and real time monitoring of services running. This lecture has answered the initial what, why and hows of real time streaming. Now I wish you luck to continue on exploring the beautiful path of data streaming. 

Project completion document : https://docs.google.com/document/d/1BF76NmA8K9qIp_YwLICzAzBcDcvIQKQijCSuOyc8Mbg/edit#

