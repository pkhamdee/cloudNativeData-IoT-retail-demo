package io.pivotal.market.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import java.util.Arrays;
import org.apache.geode.cache.Region;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;

import io.pivotal.gemfire.domain.CustomerIdentifier;
import io.pivotal.gemfire.domain.OrderDTO;
import io.pivotal.market.api.dao.PivotMartDAO;
import nyla.solutions.core.patterns.workthread.ExecutorBoss;

public class PivotMartMgrTest
{
	static PivotMartMgr service = null;

	private Gson gson = new Gson();
	
	@SuppressWarnings("unchecked")

	@BeforeClass
	public static void setup()
	{
		service = new PivotMartMgr();
		service.beaconProductsRegion = mock(Region.class);
		service.customerFavoritesRegion = mock(Region.class);
		service.customerPromotionsRegion = mock(Region.class);
	
		
		OrderDTO order = new OrderDTO();
		order.setCustomerIdentifier(new CustomerIdentifier());
		order.getCustomerIdentifier().setFirstName("nyla");
		order.getCustomerIdentifier().setLastName("nyla");
		
		Integer[] productIds = {1};
		
		order.setProductIds(productIds);
		
		service.dao = mock(PivotMartDAO.class);
		service.boss = new ExecutorBoss(1);
	}
	@Test
	public void testProcessCSV()
	{
		
		
		String csv ="\"0\",\"Nyla\",\"Nyla\",\"77-777\",\"1,2\"";
		
		OrderDTO order = service.processOrderCSV(csv);
		
		assertTrue(order.getProductIds() != null);
		
		Integer [] expected = {1,2};
		
		
		assertEquals(Arrays.asList(expected), Arrays.asList(order.getProductIds()));
		
	}

}
