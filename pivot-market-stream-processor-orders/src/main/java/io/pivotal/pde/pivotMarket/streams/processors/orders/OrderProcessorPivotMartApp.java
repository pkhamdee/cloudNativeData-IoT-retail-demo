package io.pivotal.pde.pivotMarket.streams.processors.orders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.messaging.handler.annotation.SendTo;

import io.pivotal.gemfire.domain.OrderDTO;
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
	
	@EnableBinding(Processor.class)
	public static class OrderProcessor {

		@Autowired
		PivotalMartFacadeService service;
		
		/**
		 * 
		 * @param csv the CSV input
		 * @return the order details
		 */
		@StreamListener(Processor.INPUT)
		@SendTo(Processor.OUTPUT)
		public OrderDTO process(String csv) {

			System.out.println("stream PROCESSING:"+csv);
			
			try
			{
						System.out.println("ARGUMENTS:"+System.getProperty("sun.java.command"));
						System.out.println(" csv:"+csv);
						OrderDTO order = service.processOrderCSV(csv);
						
						System.out.println("ORDER processed:"+order);
						
						return order;
			}
			catch(Exception e)
			{
						System.err.println("CANNOT process csv:"+csv);
						e.printStackTrace();
						throw e;
			}
		}
	}
}