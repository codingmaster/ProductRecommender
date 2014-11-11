package de.hpi.semrecsys.persistence;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import de.hpi.semrecsys.ProductCategory;
import de.hpi.semrecsys.model.Category;
import de.hpi.semrecsys.model.Product;

/**
 * Database functionality for product category {@link de.hpi.semrecsys.ProductCategory}
 */
public class ProductCategoryDAO extends AbstractDAO {

	private static ProductCategoryDAO productCategoryManager;
	private static CategoryDAO categoryDAO = CategoryDAO.getDefault();

	public static ProductCategoryDAO getDefault() {
		if (productCategoryManager == null) {
			productCategoryManager = new ProductCategoryDAO();
		}
		return productCategoryManager;
	}

	private ProductCategoryDAO() {
	}

	@Override
	protected Class<?> getType() {
		return ProductCategory.class;
	}

	public Set<Category> findCategoriesForProduct(Product product) {
		return findCategoriesForProduct(product.getProductId());
	}

    /**
     * returns all categories for given product
     * @param productId
     * @return set of categories
     */
	public Set<Category> findCategoriesForProduct(int productId) {
		Set<Category> result = new HashSet<Category>();
		List<ProductCategory> productCategories = findByProductId(productId);
		for (ProductCategory productCategory : productCategories) {
			Category category = categoryDAO.getCategoryById(productCategory.getId().getCategoryId());
			if (category != null) {
				result.add(category);
			}
		}
		return result;

	}

    /**
     * find productcategory by id
     * @param productId
     * @return
     */
	public List<ProductCategory> findByProductId(int productId) {
		Session session = getSession();
		Criteria criteria = session.createCriteria(getType());
		criteria.add(Restrictions.eq("id.productId", productId));
		@SuppressWarnings("unchecked")
		List<ProductCategory> result = criteria.list();
		return result;
	}


    /**
     * returns category with its cardinality
     * @return categoryId, categoryCount
     */
	public Map<Integer, Integer> getCategoryFrquencyMap() {
		Map<Integer, Integer> categoryFrequencyMap = new HashMap<Integer, Integer>();
		Session session = getSession();
		Criteria criteria = session.createCriteria(getType());
		criteria.setProjection(Projections.projectionList().add(Projections.groupProperty("id.categoryId"))
				.add(Projections.count("id.categoryId")));
		@SuppressWarnings("unchecked")
		List<Object[]> result = criteria.list();
		for (Object[] obj : result) {
			categoryFrequencyMap.put((Integer) obj[0], (Integer) obj[1]);
		}

		return categoryFrequencyMap;
	}

	public long getProductCount() {
		Session session = getSession();
		String hql = "select count( distinct id.productId) from " + getType().getSimpleName();
		Long size = ((Long) session.createQuery(hql).uniqueResult()).longValue();
		return size;

	}

}
