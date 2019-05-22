# Overview

This module is a Apache Kafka based streaming application.
It was developed to demo Cloud Native Data capabilities based
on Spring, GemFire/Apache Geode, Greenplum and Messaging Driven Architecture.

## GemFire/Apache Geode Setup 

	create region --name=beaconProducts --type=PARTITION
	create region --name=customerFavorites --type=PARTITION
	create region --name=customerPromotions --type=PARTITION
	create region --name=alerts --type=PARTITION



## Testing

	POST http://localhost:6060/processBeaconRequest
	
	{"customerId":1003,"deviceId":"6a468e3e-631f-44e6-8620-cc83330ed994","uuid":"6a468e3e-631f-44e6-8620-cc83330ed994","major":23,"minor":1,"signalPower":0}
	
	http://localhost:8080/favorites/1003
	
	
	http://localhost:6060/loadProductsCache

or  pivotmarketstreams.apps.pcfone.io/loadProductsCache

## SCDF

	app import --uri http://bit.ly/Darwin-SR3-stream-applications-rabbit-maven
	
	
	
	app register --name kafka --type source --uri file:///Projects/solutions/nyla/integration/dev/nyla-integration/messaging/apacheKafka/nyla-kafka-spring-cloud-stream-source/target/nyla-kafka-spring-cloud-stream-source-0.0.1-SNAPSHOT.jar
	
	
	app register --name orders --type processor --uri file:///Projects/Pivotal/demo/cloudNativeDataDemo/dev/CloudNativeDataDemo/supermarket/pivot-market-stream-processor-orders-0.0.1-SNAPSHOT.jar




	stream create --definition "kafka --boot-strap-servers-config=localhost:9092 --group-id=scdfPivotMarket | pivotmart-process  --jdbcUrl='jdbc:postgresql://localhost:5432/template1' --jdbcUsername=gpadmin --jdbcPassword=$PASSWD --kafkaBootStrapServers=localhost:9092 --kafkaGroupId=scdf| log" --name pivotMarket-stream
	
	
		
		
	
	stream deploy --name pivotMarket-stream --properties  app.kafka.spring.cloud.stream.defaultBinder=rabbit1



   **Remove **
   

   
   
	stream destroy --name pivotMarket-stream
	
	app unregister --name pivotmart-process --type processor
    
  
	   
	   app unregister --name kafka --type source
	   
 ** Debugging **
 
 	http://localhost:9393/management/configprops
 
   
### On PCF ONE

	cf dataflow-shell scdf
	
	app import --uri http://bit.ly/Celsius-SR3-stream-applications-kafka-10-maven
	
	
	app register --name kafka --type source --uri http://s3.amazonaws.com/cloud-native-data/apps/nyla-kafka-spring-cloud-stream-source-0.0.1-SNAPSHOT.jar
	
	app register --name pivotmart-process --type processor --uri https://s3.amazonaws.com/cloud-native-data/apps/pivot-market-stream-0.0.4-SNAPSHOT.jar
	
	app register --name pivotmart-process-orders --type processor --uri https://s3.amazonaws.com/cloud-native-data/apps/pivot-market-stream-processor-orders-0.0.1-SNAPSHOT.jar
	
	stream deploy --name pivotMarket-stream --properties  app.kafka.spring.cloud.stream.defaultBinder=rabbit
	

See http://docs.spring.io/spring-cloud-dataflow-server-cloudfoundry/docs/1.7.2.RELEASE/reference/htmlsingle/#configuration-ups


cf create-user-provided-service kafkacups -p '{”brokers":"HOST:PORT","zkNodes":"HOST:PORT"}
	
	
	stream destroy --name test
	
	stream create --name test --definition "http --port=7374  | pivotmart-process --jdbcUrl='jdbc:postgresql://localhost:5432/template1' --jdbcUsername=gpadmin --jdbcPassword=$PASSWD --kafkaBootStrapServers=localhost:9092 --kafkaGroupId=scdf | log" 
	
 stream deploy test --properties "app.http.spring.cloud.stream.kafka.binder.brokers=ec2-35-174-17-30.compute-1.amazonaws.com:9092,app.http.spring.cloud.stream.kafka.binder.zkNodes=ec2-35-174-17-30.compute-1.amazonaws.com:2181,app.log.spring.cloud.stream.kafka.binder.brokers=ec2-35-174-17-30.compute-1.amazonaws.com:9092,app.log.spring.cloud.stream.kafka.binder.zkNodes=ec2-35-174-17-30.compute-1.amazonaws.com:2181,app.pivotmart-process.spring.cloud.stream.kafka.binder.brokers=ec2-35-174-17-30.compute-1.amazonaws.com:9092,app.pivotmart-process.spring.cloud.stream.kafka.binder.zkNodes=ec2-35-174-17-30.compute-1.amazonaws.com:9092"


:9092

	--

	cf bind-service umqjatp-pivotMarket-stream-pivotmart-process-v5  pcc-dev-plan
	cf restage umqjatp-pivotMarket-stream-log-v1
	
	cf bind-service pivotMarketStreams pcc-dev-plan
	   	
	app unregister --name kafka --type source
	app unregister --name pivotmart-process --type processor
   
# Greenplum Association Rules

	set search_path to pivotalmarkets;	 
	SELECT * FROM madlib.assoc_rules( .25,            -- Support
	                                  .5,             -- Confidence
	                                  'itemid',     -- Transaction id col
	                                  'productid',      -- Product col
	                                  'pivotalmarkets.order_items',    -- Input data
	                                  'pivotalmarkets',           -- Output schema
	                                  TRUE            -- Verbose output
	                                )
	                                
	set search_path to pivotalmarkets;
	select  *  from  madlib.assoc_rules  (.10,   -- Support
	.03,   -- Confidence
	  'orderid', -- Transaction id column
	   'productname', -- item column  
	   'pivotalmarkets.order_items',  -- Input table
	    'pivotalmarkets',  -- Output schema
	     TRUE, --verbose
	      2); --  max_itemset_size
	   
	select * from pivotalmarkets.assoc_rules
	 
	 psql -d retail
	 
## Rabbit MQ

	http://localhost:15672/#/
	
	
# Testing

	bin/kafka-console-producer.sh --broker-list localhost:9092 --topic inbound
	"0","Nyla","Nyla","777-777-7777","1,2"