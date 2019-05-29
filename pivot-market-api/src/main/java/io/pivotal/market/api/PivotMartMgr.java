package io.pivotal.market.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;

import org.apache.geode.cache.Region;
import org.springframework.beans.factory.annotation.Autowired;
import io.pivotal.gemfire.domain.Beacon;
import io.pivotal.gemfire.domain.BeaconRequest;
import io.pivotal.gemfire.domain.CustomerFavorites;
import io.pivotal.gemfire.domain.CustomerIdentifier;
import io.pivotal.gemfire.domain.OrderDTO;
import io.pivotal.gemfire.domain.Product;
import io.pivotal.gemfire.domain.ProductAssociate;
import io.pivotal.gemfire.domain.Promotion;
import io.pivotal.market.api.dao.PivotMartDAO;
import nyla.solutions.core.io.csv.CsvReader;
import nyla.solutions.core.patterns.workthread.ExecutorBoss;
import nyla.solutions.core.patterns.workthread.MemorizedQueue;
import nyla.solutions.core.util.Debugger;
import nyla.solutions.core.util.Organizer;
import nyla.solutions.core.util.Text;

public class PivotMartMgr implements PivotalMartFacadeService 
{

	@Autowired
	PivotMartDAO dao;
	
	@Resource
	Region<String,Set<Product>> beaconProductsRegion;
	
	@Resource
	Region<String,Set<CustomerFavorites>> customerFavoritesRegion;
	
	@Resource
	Region<String,Set<Promotion>> customerPromotionsRegion;
	
	@Resource
	Region<Integer,Product> productsRegion;
	
	
	@Resource
	Region<String,String> customerLocationRegion;
	
	@Resource
	Region<String,Set<Promotion>> beaconPromotionsRegion;
	
	@Resource
	Region<Integer, Set<ProductAssociate>> productAssociationsRegion;
	
	ExecutorBoss boss = new ExecutorBoss(1);
	
	
	
	
	/**
	 * @param dao the dao to set
	 */
	public void setDao(PivotMartDAO dao)
	{
		this.dao = dao;
	}

	/* (non-Javadoc)
	 * @see io.pivotal.market.api.PivotalMartFacadeService#processBeaconRequest(io.pivotal.gemfire.domain.BeaconRequest)
	 */	
	@Override
	public void processBeaconRequest(BeaconRequest br)
	{
		try
		{
			
			this.loadProductsCache();
			
			System.out.println("processBeaconRequest:"+br);
			
			Beacon beacon = new Beacon();
			beacon.setUuid(br.getUuid());
			beacon.setMajor(br.getMajor());
			beacon.setMinor(br.getMinor());
			
			//this.customerLocationRegion.put(br.get,beacon.getKey());

			cacheCustomerFavorites(br.getCustomerId());
			
			Set<Product> products = dao.selectProductsByBeacon(beacon);
			
			if(products == null || products.isEmpty())
				return;
			
			beaconProductsRegion.put(beacon.getKey(), products);
			
			Set<Promotion> promotions = new HashSet<>();
			for (Product product : products)
			{
				System.out.println(" Looking for promotion for product:"+product);
				Set<Promotion> set = dao.selectPromotionsByProduct(product);
				if(set == null || set.isEmpty())
					continue;
				
				System.out.println("found promotions:"+set);
				
				promotions.addAll(set);
				
			}
			if(promotions.isEmpty())
				return;
			
			//Add associations
			this.constructProductAssociations(products);
			
			//add promotions
			customerPromotionsRegion.put(br.getCustomerId().getKey(),promotions);
			this.beaconPromotionsRegion.put(beacon.getKey(),promotions);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			throw e;
		}
	}//------------------------------------------------

	private void cacheCustomerFavorites(CustomerIdentifier customerIdentifier)
	{
		Set<CustomerFavorites> cf = dao.selectCustomerFavorites(customerIdentifier);
		this.customerFavoritesRegion.put(customerIdentifier.getKey(), cf);
	}//------------------------------------------------
	
	/* (non-Javadoc)
	 * @see io.pivotal.market.api.PivotalMartFacadeService#getProduct(int)
	 */
	@Override
	public Product getProduct(int productId)
	{
		return this.dao.selectProduct(productId);
	}//------------------------------------------------
	/**
	 * Load all products from database into cache
	 */
	@Override
	public Boolean loadProductsCache()
	{
		java.util.List<Integer> ids = this.dao.selectProductIds();
		
		int batchSize = 100;
		java.util.List<Collection<Integer>> pages= Organizer.toPages(ids, batchSize);
		
		MemorizedQueue queue = new MemorizedQueue();
		
		for (Collection<Integer> collection : pages)
		{
			queue.add(() -> {
				try
				{
					Map<Integer,Product> batch = new HashMap<>(batchSize);
					
					for (int productId : collection)
					{
						batch.put(Integer.valueOf(productId),this.dao.selectProduct(productId));
						
						
						if(batch.size() > batchSize)
						{
							this.productsRegion.putAll(batch);
							batch.clear();
						}
					}//end for
					
					if(!batch.isEmpty())
					{
						this.productsRegion.putAll(batch);
						batch.clear();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			});
			
		}
		
		this.boss.startWorking(queue,true);
		
		return true;
		
	}//------------------------------------------------
	/* (non-Javadoc)
	 * @see io.pivotal.market.api.PivotalMartFacadeService#processOrder(io.pivotal.gemfire.domain.OrderDTO)
	 */
	@Override
	public int processOrder(OrderDTO order)
	{
		Debugger.println(this,"process Order %s",order);
		
		//insert into order_times
		Collection<Product> products = dao.insertOrder(order);
		
		constructProductAssociations(products);
		
		if(products == null || products.isEmpty())
			return 0;
		
		
		//calculate
		
		this.dao.updateCustomerFavorites();
		
		//populate region
		this.cacheCustomerFavorites(order.getCustomerIdentifier());
		
		return products.size();
	}

	private void constructProductAssociations(Collection<Product> products)
	{

		if(products != null)
		{
			for (Product product : products)
			{				
				Set<ProductAssociate> productAssociation = dao.selectProductAssociates(product);
				
				if(productAssociation == null || productAssociation.isEmpty())
					continue;
				
				this.productAssociationsRegion.put(product.getProductId(),productAssociation);
			}
		}
	}//------------------------------------------------
	
	/* (non-Javadoc)
	 * @see io.pivotal.market.api.PivotalMartFacadeService#processOrderCSV(java.lang.String)
	 */
	@Override
	public OrderDTO processOrderCSV(String csv)
	{
		Debugger.println(this,"processing csv:"+csv);
		
		if(csv  == null || csv.length() == 0)
		{
			Debugger.println(this,"CSV is null. Returning null");
			return null;
		}
		
		
		try
		{
			List<String> cells = CsvReader.parse(csv);
			int length  = cells.size();
			
			int i=0;
			String key = length > i ? cells.get(i) : null;
			i++;
			
			String firstName = length > i ? cells.get(i) : null;
			i++;
			
			String lastName  = length > i ? cells.get(i) : null;
			i++;

			String mobileNumber = length > i ? cells.get(i) : null;
			i++;
			
			String text = length > i ? cells.get(i) : null;
			
			if(text == null || text.length() ==0)
			{
				Debugger.printInfo(this,"No product ids provided:"+csv);
				throw new IllegalArgumentException("No product ids provided:"+csv);
			}
			Integer[] productIds = Text.splitRE(text, ",",Integer.class);
			
			if(productIds == null || productIds.length ==0 )
			{
				throw new IllegalArgumentException("Product ids are null or empty:"+csv);
			}
			
			CustomerIdentifier customerIdentifier = new CustomerIdentifier(key, firstName, lastName, mobileNumber);
			OrderDTO orderDTO = new OrderDTO(customerIdentifier, productIds);
			
			this.processOrder(orderDTO);
			
			return orderDTO;
		}
		catch (RuntimeException e)
		{
			Debugger.printError(e);
			
			throw e;
		}
		
	}
}
