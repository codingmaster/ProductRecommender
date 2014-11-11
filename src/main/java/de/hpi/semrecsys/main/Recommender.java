package de.hpi.semrecsys.main;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.hp.gagawa.java.elements.Html;

import de.hpi.semrecsys.GeneratedRecommendation;
import de.hpi.semrecsys.RandomRecommendation;
import de.hpi.semrecsys.RecommendationId;
import de.hpi.semrecsys.config.RecommenderProperties;
import de.hpi.semrecsys.config.SemRecSysConfigurator;
import de.hpi.semrecsys.model.Product;
import de.hpi.semrecsys.output.HTMLOutputCreator;
import de.hpi.semrecsys.output.RecommendationResult;
import de.hpi.semrecsys.output.RecommendationResultsHolder;
import de.hpi.semrecsys.persistence.GeneratedRecommendationDAO;
import de.hpi.semrecsys.persistence.ProductDAO;
import de.hpi.semrecsys.persistence.RandomRecommendationDAO;
import de.hpi.semrecsys.strategy.RecommendationStrategy;
import de.hpi.semrecsys.strategy.RecommendationStrategyImpl;
import de.hpi.semrecsys.utils.FileUtils;
import java.util.logging.Logger;

/**
 * generates recommendations for the given product
 * @author Michael Wolowyk
 *
 */
public class Recommender {

	SemRecSysConfigurator configurator;

	RecommendationStrategy recommendationStrategy;

	ProductDAO productManager = ProductDAO.getDefault();
    Logger log = Logger.getLogger("");

	public Recommender(SemRecSysConfigurator configurator) {
		this.configurator = configurator;
		this.recommendationStrategy = new RecommendationStrategyImpl(configurator);
	}

	/**
	 * generates recommendations for product1 of type with special attribute weights 
	 * @param product1
	 * @param attributeWeights custom attributeWeights
	 * @param type one of generated, existing, random
	 */
	public void recommendGenerated(Product product1, Map<String, Double> attributeWeights, String type) {
		configurator.getJsonProperties().setAttributesByType(attributeWeights);
		recommendGenerated(product1, type);
	}


    public void recommendGenerated(Map<Integer, Product> products, String type){
        for(Integer productId : products.keySet()){
            Product product = products.get(productId);
            try {
                recommendGenerated(product, type);
            }
            catch(Exception ex){
                log.warning("Exception occurred: " + ex.getLocalizedMessage());
            }
        }

    }

	/**
	 * generates recommendations for product1 of type and saves them to database and to HTML output file
	 * @param product1
	 * @param type
	 */
	public void recommendGenerated(Product product1, String type) {
		if (product1 != null && product1.getTitle() != null && !product1.getImgPathes().isEmpty()) {

			long start = System.currentTimeMillis();
			RecommendationResultsHolder recommendationHolder = recommendationStrategy.getRecommendationResults(
					product1, type);
			saveRecommendationsToDatabase(recommendationHolder.getRecommendationResults());
			File outFile = printRecommendationsToHTML(recommendationHolder);
			long fin = System.currentTimeMillis();

			System.out.println("Execution time: " + String.valueOf(fin - start) + " ms");
			System.out.println("Output is written to " + outFile.getAbsolutePath());
		} else {
			System.out.println("Product " + product1 + " is empty");
		}
	}

	/**
	 * creates random recommendations for product1 and saves them to the database 
	 * @param product1
	 */
	public void recommendRandom(Product product1) {
		System.out.println("\nRandom Recommendations: ");
		for (int j = 0; j < RecommenderProperties.NUMBER_OF_RESULTS; j++) {
			Product random = productManager.getRandom();
			RecommendationId recommendationId = new RecommendationId(product1.getProductId(), j, random.getProductId());
			RandomRecommendation randomRecommendation = new RandomRecommendation(recommendationId);
			RandomRecommendationDAO.getDefault().attachDirty(randomRecommendation);
			System.out.println(random);
		}
	}

	private File printRecommendationsToHTML(RecommendationResultsHolder recommendationResults) {
		Html html = createRecommendationsHTML(recommendationResults);

		Product product1 = recommendationResults.getBaseProduct();
		String htmlToString = html.write();
		String customer = configurator.getJsonProperties().getCustomer();
		String outPath = new File(SemRecSysConfigurator.getPropertiesDirPath()).getParentFile().getAbsolutePath()
				+ "/../out_" + customer + "/" + product1.getProductId() + "_generated.html";

		try {
			FileUtils.writeTextToFile(htmlToString, outPath, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new File(outPath);
	}

	private void saveRecommendationsToDatabase(List<RecommendationResult> recommendationResults) {
        int idx = 1;
		for (RecommendationResult recommendationResult : recommendationResults) {
            recommendationResult.getId().setPosition(idx);
			GeneratedRecommendation recommendation = recommendationResult.toRecommendation();
			GeneratedRecommendationDAO.getDefault().attachDirty(recommendation);
            idx++;
		}
	}



	private Html createRecommendationsHTML(RecommendationResultsHolder recommendationResults) {
		HTMLOutputCreator htmlCreator = new HTMLOutputCreator(configurator,
				recommendationResults.getBaseProduct());
        int idx = 1;

		for (RecommendationResult recommendationResult : recommendationResults.getRecommendationResults()) {
            recommendationResult.getId().setPosition(idx);
			htmlCreator.addRecommendationEntry(recommendationResult);
            idx++;
		}
		Html html = htmlCreator.getHtml();
		return html;
	}
}
