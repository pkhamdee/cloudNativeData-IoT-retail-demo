
	app import --uri http://bit.ly/Celsius-SR3-stream-applications-kafka-10-maven

	app register --name orders --type processor --uri https://s3.amazonaws.com/cloud-native-data/apps/pivot-market-stream-processor-orders-0.0.1-SNAPSHOT.jar
	
	stream create --name pivotMartOrders --definition "http --port=7374  | orders --jdbcUrl='jdbc:postgresql://18.213.48.32:6432/retail' --jdbcUsername=retail | log"
	
	
	
	 stream deploy pivotMartOrders --properties "app.http.spring.cloud.stream.kafka.binder.brokers=ec2-35-174-17-30.compute-1.amazonaws.com:9092,app.http.spring.cloud.stream.kafka.binder.zkNodes=ec2-35-174-17-30.compute-1.amazonaws.com:2181,app.log.spring.cloud.stream.kafka.binder.brokers=ec2-35-174-17-30.compute-1.amazonaws.com:9092,app.log.spring.cloud.stream.kafka.binder.zkNodes=ec2-35-174-17-30.compute-1.amazonaws.com:2181,app. orders.spring.cloud.stream.kafka.streams.binder.brokers=ec2-35-174-17-30.compute-1.amazonaws.com:9092,app. orders.spring.cloud.stream.kafka.streams.binder.zkNodes=ec2-35-174-17-30.compute-1.amazonaws.com:2181"
	 
	 
app.http.spring.cloud.stream.defaultBinder=kakfa1,app.order.spring.cloud.stream.defaultBinder=kakfa
	 

SPRING_APPLICATION_JSON: {
 "spring.cloud.dataflow.stream.app.label":"orders",
 "spring.cloud.stream.metrics.properties":"spring.application.name,spring.application.index,spring.cloud.application.*,spring.cloud.dataflow.*"
 ,
 "spring.cloud.stream.bindings.applicationMetrics.destination":"metrics"
 ,
 "spring.cloud.dataflow.stream.name":"pivotMartOrders",
 "jdbcUsername":"retail",
 "spring.metrics.export.triggers.application.includes":"integration**",
 "spring.cloud.stream.metrics.key":"pivotMartOrders.orders.${spring.cloud.application.guid}",
 "spring.cloud.stream.bindings.input.group":"pivotMartOrders",
 "spring.cloud.stream.bindings.output.producer.requiredGroups":"pivotMartOrders",
 "jdbcUrl":"jdbc:postgresql://18.213.48.32:6432/retail",
 "spring.cloud.stream.bindings.output.destination":"pivotMartOrders.orders",
 "spring.cloud.dataflow.stream.app.type":"processor",
 "spring.cloud.stream.bindings.input.destination":"pivotMartOrders.http"}

	 
	 
## PCF 

//cf create-service  p-redis shared-vm redis
 
	cf create-service  p-dataflow standard scdf  -c '{ "analytics-data-service": { "name": "redis", "plan": "shared-vm" }, "maven.remote-repositories.repo1.url": "https://repo.spring.io/libs-snapshot" }'

	cf dataflow-shell scdf
	

cf set-env LoiRSIh-pivotMartOrders-orders-v6 spring.cloud.dataflow.applicationProperties.stream.spring.cloud.stream.kafka.binder.brokers=ec2-35-174-17-30.compute-1.amazonaws.com 9092

spring.cloud.dataflow.applicationProperties.stream.spring.cloud.stream.kafka.binder.zkNodes=ec2-35-174-17-30.compute-1.amazonaws.coms:2181
      
## Issues/Work around

When deploying in SCDF

Remove spring.cloud.stream.bindings.applicationMetrics.destination from  SPRING_APPLICATION_JSON.


# Troubleshooting

## View service logfs


	cf install-plugin -r CF-Community "Service Instance Logging"
	cf service-logs scdf
	cf target -o group-pde-dev -s cloud-demo

# Testing

	bin/kafka-console-producer.sh --broker-list localhost:9092 --topic orders-in
	"0","Nyla","Nyla","777-777-7777","1,2"
	
	
	
	-----------------------
	
	
	  kafka:
    image: wurstmeister/kafka:1.1.0
    expose:
      - "9092"
    environment:
      - KAFKA_ADVERTISED_PORT=9092
      - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
      - KAFKA_ADVERTISED_HOST_NAME=kafka
  zookeeper:
    image: wurstmeister/zookeeper
    expose:
      - "2181"
      
      