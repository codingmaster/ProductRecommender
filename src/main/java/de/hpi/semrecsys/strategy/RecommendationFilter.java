package de.hpi.semrecsys.strategy;

import java.util.ArrayList;
import java.util.List;

import de.hpi.semrecsys.model.Attribute;
import de.hpi.semrecsys.utils.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.hpi.semrecsys.config.RecommenderProperties;
import de.hpi.semrecsys.config.SemRecSysConfigurator;
import de.hpi.semrecsys.model.AttributeEntity;
import de.hpi.semrecsys.model.Product;
import de.hpi.semrecsys.output.RecommendationResult;
import de.hpi.semrecsys.output.RecommendationResultsHolder;
import de.hpi.semrecsys.similarity.AttributeEntityMapping;
import de.hpi.semrecsys.similarity.category.ProductSimilarityCalculator;

public class RecommendationFilter {

    public static final String GENERATED_TYPE = "generated";
    public static final String DESIGNER_TYPE = "designer";
    Logger log = Logger.getLogger(getClass());
	ProductSimilarityCalculator similarityCalculator;
	boolean calculateIntraListSimilarity = true;

	public RecommendationFilter(SemRecSysConfigurator configurator) {
		this.similarityCalculator = new ProductSimilarityCalculator(configurator);
	}

	public void setLogLevel(Level level) {
		log.setLevel(level);

	}

    /**
     * Filter irrelevant recommendations
     *
     * Irrelevant recommendations:
     *
     * 1. recommendations is null, recommendation product is null or empty
     *
     * 2. recommendation product has invalid image
     *
     * 3. recommendation product is too similar to the base product or to the previous product
     *
     * @param preselectedRecommendations - unfiltered recommendations
     * @return relevant recommendation
     */
	public RecommendationResultsHolder filterPreselectedRecommendations(
			RecommendationResultsHolder preselectedRecommendations) {
		RecommendationResultsHolder filteredResults = new RecommendationResultsHolder(
				preselectedRecommendations.getBaseProduct());
		Product lastProduct = null;
		for (RecommendationResult recommendationResult : preselectedRecommendations.getRecommendationResults()) {
			Product recommendationProduct = recommendationResult.recommendedProduct();
            recommendationResult = updateRecommendationType(recommendationResult);
			if (isRelevantRecommendation(lastProduct, recommendationResult)) {
				filteredResults.add(recommendationResult);
                Product productToRemove = getProductToRemove(lastProduct, recommendationResult);
                if(productToRemove != null){
                    filteredResults.remove(productToRemove);
                }

				lastProduct = recommendationProduct;
			} else {
				log.warn("Product " + recommendationProduct + " wasn't added to the result list");
			}
		}
		return filteredResults;
	}

    private RecommendationResult updateRecommendationType(RecommendationResult recommendation) {
        String type = updatedRecommendationType(recommendation);
        recommendation.setType(type);
        return recommendation;
    }

    private String updatedRecommendationType(RecommendationResult recommendation) {
        Product baseProduct = recommendation.getBaseProduct();

        Product recommendedProduct = recommendation.recommendedProduct();
        String man1 = getManufacturer(baseProduct);
        String man2 = getManufacturer(recommendedProduct);
        if(man1 == null || man2 == null){
            return GENERATED_TYPE;
        }
        return (man1.equalsIgnoreCase(man2)) ? DESIGNER_TYPE : GENERATED_TYPE;
    }

    private String getManufacturer(Product product) {
        List<Attribute> manufacturers = product.getAttributes().get("manufacturer");
        if(manufacturers==null || manufacturers.isEmpty()){
            return null;
        }

        return manufacturers.get(0).getValue();
    }


    private boolean isRelevantRecommendation(Product lastProduct, RecommendationResult recommendationResult) {

		Product recommendationProduct = recommendationResult.recommendedProduct();
		Product baseProduct = recommendationResult.getBaseProduct();
        Double recommendationResultRelativeScore = recommendationResult.getRelativeScore();
        if(recommendationResult == null ||
                recommendationProduct == null ||
                recommendationProduct.getTitle() == null){
            log.warn("Recommendation is null");
            return false;
        }
        if (recommendationProduct.getImgPathes().isEmpty()) {
            for (String imgPath : recommendationProduct.getImgPathes()) {
                if (!RecommendationValidator.isValidUrl(imgPath)) {
                    log.warn(recommendationProduct + " has invalid image path");
                    return false;
                }
            }
            log.warn(recommendationProduct + " has invalid image path");
            return false;
        }

        if(recommendationResultRelativeScore < RecommenderProperties.MIN_PRODUCT_SIMILARITY
                || recommendationResultRelativeScore > RecommenderProperties.MAX_PRODUCT_SIMILARITY){
            log.warn("Recommendation score " + recommendationResultRelativeScore + " is not in range");
            return false;
        }

        Double intraListSim = calculateQuickSimilarity(recommendationProduct, baseProduct);
        log.debug("sim (" + StringUtils.doubleToString(intraListSim) + ") with " +  recommendationProduct);
        if(Math.abs(intraListSim) > RecommenderProperties.MAX_PRODUCT_FILTER_SIMILARITY){
            log.warn("Too similar with base sim (" + StringUtils.doubleToString(intraListSim) + ")");
            return false;
        }
        return true;
	}

    private Product getProductToRemove(Product lastProduct, RecommendationResult recommendationResult) {
        Double intraListSim;
        Product productToRemove = null;
        Product baseProduct = recommendationResult.getBaseProduct();
        String baseColor = getColor(baseProduct);
        Product recommendationProduct = recommendationResult.recommendedProduct();
        intraListSim = calculateQuickSimilarity(recommendationProduct, lastProduct);
        log.debug("sim (" + StringUtils.doubleToString(intraListSim) + ") with " +  lastProduct);
        if(Math.abs(intraListSim) > RecommenderProperties.MAX_PRODUCT_FILTER_SIMILARITY){
            String lastProductColor = getColor(lastProduct);
            String recommendationProductColor = getColor(recommendationProduct);
            log.warn("Too similar with last sim (" + StringUtils.doubleToString(intraListSim) + ")"  + " last " + lastProduct );
            if( baseColor.equalsIgnoreCase(recommendationProductColor)){
                log.warn("Remove: " + lastProduct);
                return lastProduct;
            }
            else{
                log.warn("Remove: " + recommendationProduct);
                return recommendationProduct;
            }
        }
        return null;
    }

    private String getColor(Product lastProduct) {
        if(lastProduct != null && lastProduct.getAttributes().get("color_primary") != null) {
            return lastProduct.getAttributes().get("color_primary").get(0).getValue();
        }
        return "";
    }

    public Double calculateQuickSimilarity(Product product1, Product product2) {
        if(product1 == null || product2 == null){
            return 0.0;
        }
        String desc1 = product1.getAttributes().get("description").get(0).getValue();
        String desc2 = product2.getAttributes().get("description").get(0).getValue();
        long desc1size = desc1.length();
        long desc2size = desc2.length();
        double levenstein = StringUtils.levensteinDistance(desc1, desc2);
        double dice = ((desc1size - levenstein)  + (desc2size - levenstein)) / (double)(desc1size + desc2size);
        return dice;
	}

    private Double getEntitySim(Product product1, Product product2) {
        similarityCalculator.fillProductWithAttributeEntityMapping(product1);
        AttributeEntityMapping attributeEntityHolder1 = product1.getAttributeEntityMapping();
        AttributeEntityMapping attributeEntityHolder2 = product2.getAttributeEntityMapping();

        List<AttributeEntity> commonAttributeEntities = new ArrayList<AttributeEntity>();
        List<AttributeEntity> attributeEntities1 = attributeEntityHolder1.getAttributeEntities();
        List<AttributeEntity> attributeEntities2 = attributeEntityHolder2.getAttributeEntities();

        for (AttributeEntity attributeEntity1 : attributeEntities1) {
            if (attributeEntities2.contains(attributeEntity1)) {
                commonAttributeEntities.add(attributeEntity1);
            }
        }

        log.debug("attributeEntities1: " + attributeEntities1);
        log.debug("attributeEntities2: " + attributeEntities2);
        log.debug("commonAttributeEntities: " + commonAttributeEntities);

        int attributeEntities1Size = attributeEntities1.size();
        int attributeEntities2Size = attributeEntities2.size();
        int commonEntitiesSize = commonAttributeEntities.size();

        log.debug("ae1: " + attributeEntities1Size);
        log.debug("ae2: " + attributeEntities2Size);
        log.debug("common: " + commonEntitiesSize);
        Double dice = (double) (2 * (double) commonEntitiesSize / (double) (attributeEntities1Size + attributeEntities2Size));
        return dice;
    }

}
