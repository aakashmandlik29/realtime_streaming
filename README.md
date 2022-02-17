# realtime_streaming

Problem statement 

Building a real time dashboard to see the trends of airline data in real time. The dashboard should be refreshed in a few seconds and the graphs and charts should be refreshed in accordance with the real time data. 
Prerequisites 
Docker basics : Docker is a set of platform as a service products that use OS-level virtualization to deliver software in packages called containers
Kafka : A messaging queue which keeps the data enabling the producers and consumers to work independently. The async way of using the kafka makes it powerful with respect to the traditional producer consumer way. 
Apache spark : Apache Spark is an open-source unified analytics engine for large-scale data processing. We will be familiarized with spark standalone clusters. Also knowledge of spark RDD and dataframes will be a plus. 
InfluxDB : InfluxDB is an open-source time series database used for storing time series data mainly used for monitoring and real time trends. 
Grafana or Chronograf : Both are dashboarding tools. Their usage depends on our use case. Grafana is mainly used when we have multiple data sources like InfluxDB or Redshift or BigQuery to get the data from and use the data in a single dashboard. While Chronograf can only be used when the data source is influxDb. 

Let’s look at the source data we used for our analysis.  

