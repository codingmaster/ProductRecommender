package de.hpi.semrecsys.main;

import de.hpi.semrecsys.config.SemRecSysConfigurator;
import de.hpi.semrecsys.config.SemRecSysConfigurator.Customer;
import de.hpi.semrecsys.model.Attribute.AttributeType;
import de.hpi.semrecsys.model.Product;
import de.hpi.semrecsys.output.RecommendationResult;
import de.hpi.semrecsys.output.RecommendationResultsHolder;
import de.hpi.semrecsys.persistence.ProductDAO;
import de.hpi.semrecsys.populator.Populator;
import de.hpi.semrecsys.populator.Populator.PopulationOption;
import de.hpi.semrecsys.strategy.RecommendationStrategy;
import de.hpi.semrecsys.strategy.RecommendationStrategyImpl;
import de.hpi.semrecsys.utils.CollectionUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Main class for recommendation generation
 * @author Michael Wolowyk
 *
 */
public class RecommenderMain {

	static Customer customer = Customer.melovely;
	static SemRecSysConfigurator configurator = SemRecSysConfigurator.getDefaultConfigurator(customer);
	static RecommendationStrategy recommendationStrategy = new RecommendationStrategyImpl(configurator);

	static ProductDAO productManager = ProductDAO.getDefault();
	static Recommender recommender = new Recommender(configurator);
	private static Populator populator = new Populator(configurator);
    static Logger log = Logger.getLogger("");

	static AttributeType[] attributeTypes = { AttributeType.unstruct, AttributeType.img };
	static String type = "generated";

	static int[] naturideenSelectedProducts = { 110, 36, 78, 3232, 1868, 1358, 1563, 1797, 2512, 1117 };
	static int[] melovelySelectedProducts = { 1815, 920, 3132, 3516, 3031, 3122, 3446 };

	public static void main(String[] args) {

        Integer productid1 = 54;
        Integer productid2 = 2467;
        calculateSimilarity(productid1, productid2);


//        execute(3469, 3471);
//		int[] selectedProducts;
//		if (customer.equals(Customer.naturideen) || customer.equals(Customer.naturideen2)) {
//			selectedProducts = naturideenSelectedProducts;
//		} else {
//			selectedProducts = melovelySelectedProducts;
//		}
//
//		for (int productId : selectedProducts) {
//			executeComplete(productId);
//		}
	}

    private static void calculateSimilarity(Integer productid1, Integer productid2) {
        Product product1 = productManager.findById(productid1);
        Product product2 = productManager.findById(productid2);
        RecommendationResultsHolder resultsHolder = recommendationStrategy.getSimilarityBetweenProducts(product1, product2);
        for(RecommendationResult result : resultsHolder.getRecommendationResults()){
            System.out.println(result.recommendedProduct() + " score: " + result.getRelativeScore());
            System.out.println(CollectionUtils.collectionToString(result.getCommonEntities()));
        }
    }

    private static void executeComplete(int productId) {
		AttributeType[] structAttributeTypes = { AttributeType.unstruct, AttributeType.struct, AttributeType.cat,
				AttributeType.split, AttributeType.img };
		attributeTypes = structAttributeTypes;
		// type = "generated";
		populator.populateMeta(true, configurator.getMetaGraphName(), attributeTypes);
		execute(productId);
	}

	/**
	 * Generates recommendations for structured attributes for productId
	 * @param productId
	 */
	public static void executeStruct(int productId) {
		AttributeType[] structAttributeTypes = { AttributeType.struct, AttributeType.cat, AttributeType.split,
				AttributeType.img };
		attributeTypes = structAttributeTypes;
		type = "struct";
		populator.populateMeta(true, configurator.getMetaGraphName(), attributeTypes);
		execute(productId);
	}
	/**
	 * Generates recommendations for unstructured attributes for productId
	 * @param productId
	 */
	public static void executeUnstruct(int productId) {
		type = "unstruct";
		AttributeType[] unstructAttributeTypes = { AttributeType.unstruct, AttributeType.img };
		attributeTypes = unstructAttributeTypes;
		populator.populateMeta(true, configurator.getMetaGraphName(), attributeTypes);
		execute(productId);
	}

	/**
	 * generate random recommendations for productId
	 */
	public static void executeRand() {
		while (true) {
			recommender.recommendGenerated(productManager.getRandom(), type);
		}

	}

	/**
	 * generates recommendations for all products in the database
	 */
	public static void executeAll() {
        log.info("Extracting all products from the database");
        Map<Integer, Product> products = productManager.getAllProducts();
        log.info(products.size() + " products were extracted from the database");


		PopulationOption[] options = { PopulationOption.meta };
		// ,PopulationOption.attribute_sim, PopulationOption.entity_sim };
		PopulatorMain.execute(true, options);


		recommender.recommendGenerated(products, type);
	}

	/**
	 * generates recommendations for the given productId
	 * 
	 * @param productId
	 */
	public static void execute(Integer productId) {
        long start = System.currentTimeMillis();
        Product product1 = productManager.findById(productId);
        recommender.recommendGenerated(product1, type);
        long end = System.currentTimeMillis();
        System.out.println("Time : " + String.valueOf(end - start) + " ms");
	}

    public static void execute(Integer fromInt, Integer toInt) {
        log.info("Extracting products from the database from " + fromInt + " to " + toInt);
        Map<Integer, Product> products = productManager.getProducts(fromInt, toInt);
        log.info(products.size() + " products were extracted from the database");

        PopulationOption[] options = { PopulationOption.meta };
        // ,PopulationOption.attribute_sim, PopulationOption.entity_sim };
        PopulatorMain.execute(true, options);

        recommender.recommendGenerated(products, type);
    }
}
