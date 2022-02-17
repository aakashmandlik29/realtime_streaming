#!/bin/bash  
# Bash script  
echo "Kafka producer!"

exitfn () {
    trap SIGINT              # Restore signal handling for SIGINT
    echo 'Aarghh!!'    # Growl at user,
    kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic airlines    
    exit                     #   then exit script.
}

trap "echo 'exit called';kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic airlines; exit" ERR EXIT 

while true 
do 	
	kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic airlines < airlines_1.csv
	echo "Sleep for 1 mins"
	sleep 20
        kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic airlines < airlines_2.csv
        echo "Sleep for 1 mins"
        sleep 20
        kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic airlines < airlines_3.csv
        echo "Sleep for 1 mins"
        sleep 20
        kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic airlines < airlines_4.csv
        echo "Sleep for 1 mins"
        sleep 20
        kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic airlines < airlines_5.csv
        echo "Sleep for 1 mins"
        sleep 20
        kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic airlines < airlines_6.csv
        echo "Sleep for 1 mins"
        sleep 20
        kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic airlines < airlines_7.csv
        echo "Sleep for 1 mins"
        sleep 20
        kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic airlines < airlines_8.csv
        echo "Sleep for 1 mins"
        sleep 20
        kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic airlines < airlines_9.csv
        echo "Sleep for 1 mins"
        sleep 20

done

