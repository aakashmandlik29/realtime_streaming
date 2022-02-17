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


