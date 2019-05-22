package io.pivotal.market.api;

import org.springframework.stereotype.Service;
import io.pivotal.gemfire.domain.BeaconRequest;
import io.pivotal.gemfire.domain.OrderDTO;
import io.pivotal.gemfire.domain.Product;

@Service
public interface PivotalMartFacadeService
{
	/**
	 * 
	 * @param br the beacon request
	 */
	void processBeaconRequest(BeaconRequest br);
	
	Product getProduct(int productId);
	
	Boolean loadProductsCache();
	
	
	int processOrder(OrderDTO order);

	
	OrderDTO processOrderCSV(String csv);

}