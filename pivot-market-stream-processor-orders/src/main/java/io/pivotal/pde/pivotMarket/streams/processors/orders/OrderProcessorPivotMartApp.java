package io.pivotal.pde.pivotMarket.streams.processors.orders;
import java.util.Arrays;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.kafka.streams.annotations.KafkaStreamsProcessor;
import org.springframework.messaging.handler.annotation.SendTo;

import io.pivotal.market.api.PivotalMartFacadeService;
import nyla.solutions.core.util.Config;


@SpringBootApplication
public class OrderProcessorPivotMartApp {
	 
	/**
	 * Add argument to Configurations and Run the spring boot app 
	 * @param args the input args include --LOCATORS=host[port] --SECURITY_USERNAME=u --SECURITY_PASSWORD=p
	 */
	public static void main(String[] args) {
		Config.loadArgs(args);
		SpringApplication.run(OrderProcessorPivotMartApp.class, args);
	}//------------------------------------------------
	
	@EnableBinding(KafkaStreamsProcessor.class)
	public static class OrderProcessor {

		@Autowired
		PivotalMartFacadeService service;
		
		@StreamListener("input")
		@SendTo("output")
		public KStream<Object, Object> process(KStream<Object, String> input) {

			input.foreach((k,v) -> System.out.println("v:"+v));
			
			//return input;
			
			return input.flatMapValues(
				(csv) ->
				{
					try
					{
						System.out.println("ARGUMENTS:"+System.getProperty("sun.java.command"));
						System.out.println(" csv:"+csv);
						return Arrays.asList(service.processOrderCSV(csv));
					}
					catch(Exception e)
					{
						System.err.println("CANNOT process csv:"+csv);
						e.printStackTrace();
						return Arrays.asList();
					}					
					
				}).map((k,v) -> new KeyValue<>(k, v));
			
			
			/**
			return input
					.flatMapValues(value -> Arrays.asList(value.toLowerCase().split("\\W+")))
					.map((key, value) -> new KeyValue<>(value, value))
					.groupByKey(Serialized.with(Serdes.String(), Serdes.String()))
					.windowedBy(TimeWindows.of(30000))
					.count(Materialized.as("WordCounts-1"))
					.toStream()
					.map((key, value) -> new KeyValue<>("HELLO","WORLD"));
					*/
		}
	}
}