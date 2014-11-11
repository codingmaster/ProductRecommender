package de.hpi.semrecsys.similarity.category;

import de.hpi.semrecsys.persistence.ProductCategoryDAO;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.hpi.semrecsys.model.Product;
import de.hpi.semrecsys.similarity.AbstractSimilarityCalculator;

public abstract class CategorySimilarityCalculator extends AbstractSimilarityCalculator {

	protected ProductCategoryDAO productCategoryManager = ProductCategoryDAO.getDefault();
	protected Logger logger = Logger.getLogger(getClass());

	public void setLevel(Level level) {
		logger.setLevel(level);
	}

	public abstract Double calculateSimilarity(Product product1, Product product2);

}
