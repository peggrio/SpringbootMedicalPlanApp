# SpringbootMedicalPlanApp
## what is Oauth2.0
![image](./assets/oauth2.0.png)

## Overall architecture
![image](./assets/architecture.png)

## Pre-requisites
Java
Maven
OAuth 2.0 client (Refer Google APIs for more details)

## Connect to Redis server
1. Check Redis Installation: In the Terminal window, type the following command and press Enter:
```cd /opt/homebrew/bin/```(this is where your server installed)
2. type in ```redis-server```

After starting the server, kick off the CLI:
1. ```redis-cli```
2. query for all the data: ```KEYS *```

## Kafka installation
1. Install Kafka```brew install kafka```
2. Apache Kafka depends on Zookeeper for cluster management. Hence, prior to starting Kafka, Zookeeper has to be started.
3. Start Zookeeper
```/opt/homebrew/bin/zookeeper-server-start /opt/homebrew/etc/zookeeper/zoo.cfg```
4. Start Kafka
```/opt/homebrew/opt/kafka/bin/kafka-server-start /opt/homebrew/etc/zookeeper/zoo.cfg```
5. Create topic(in `/opt/homebrew/opt/kafka/bin`)
```kafka-topics --create --topic info7255 --bootstrap-server localhost:9092```
6. Delete topic(in `/opt/homebrew/opt/kafka/bin`)
```kafka-topics --bootstrap-server localhost:9092 --delete --topic info7255```
7. List all messages remain in Kafka(in `/opt/homebrew/opt/kafka/bin`)
```./kafka-console-consumer --bootstrap-server localhost:9092 --topic info7255 --from-beginning --max-messages 100```

## start ElasticSearch and Kibana
Follow this [DOCUMENT](https://www.elastic.co/guide/en/elasticsearch/reference/current/run-elasticsearch-locally.html) to install docker and run. If you have docker running already, please refer `application.properties` for retrieving the username and password.

## Future improvement
Avoid storing "（quotation mark）in ETag

## To Start
1. start redis server
2. configure elastic search server and start(on docker)
3. start Kafka server
4. create Kafka Topic
5. start this Springboot app on port 8082
6. test on postman and Kibana portal

## Helpers
1. if you find any problem in starting zookeeper, reference blogs [Zookeeper: address already in use](https://stackoverflow.com/questions/48542763/kafka-zookeeper-java-net-bindexception-address-already-in-use)
2. the installation path and configure files location in MAC please refer to this [article](https://www.conduktor.io/kafka/how-to-install-apache-kafka-on-mac-with-homebrew/)
3. use `brew services list` to check if your services is running or not


## Kafka monitor/visualization
run this cmd: `docker run -it -p 9090:8080 -e DYNAMIC_CONFIG_ENABLED=true provectuslabs/kafka-ui`, open`http://localhost:9090/` to checkout.

## Port
zookeeper -8080
