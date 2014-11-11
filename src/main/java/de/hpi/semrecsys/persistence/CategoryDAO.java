package de.hpi.semrecsys.persistence;

import java.util.HashMap;
import java.util.Map;

import de.hpi.semrecsys.CategoryTable;
import de.hpi.semrecsys.model.Category;

/**
 * Database functionality for category {@link de.hpi.semrecsys.CategoryTable}
 */
public class CategoryDAO extends AbstractDAO {

	private static CategoryDAO categoryDAO;
	private static Map<Integer, Category> categories = new HashMap<Integer, Category>();

    /**
     * returns CategoryDAO singleton or creates a new one if it doesn't exists
     * @return CategoryDAO singleton
     */
	public static CategoryDAO getDefault() {
		if (categoryDAO == null) {
			categoryDAO = new CategoryDAO();
			categories = categoryDAO.findAllCategories();
		}
		return categoryDAO;
	}

	private CategoryDAO() {
	}

	@Override
	protected Class<CategoryTable> getType() {
		return CategoryTable.class;
	}

	public static Map<Integer, Category> getCategories() {
		return categories;
	}

	public Category getCategoryById(Integer categoryId) {
		return categories.get(categoryId);

	}

    /**
     * returns all categories
     * @return map category id, category
     */
	private Map<Integer, Category> findAllCategories() {
		ProductCategoryDAO productCategoryManager = ProductCategoryDAO.getDefault();
		Map<Integer, Integer> categoryFrequencyMap = productCategoryManager.getCategoryFrquencyMap();
		long productCount = productCategoryManager.getProductCount();
		for (Object obj : findAll()) {
			Category category = createCategory(categoryFrequencyMap, productCount, obj);
			categories.put(category.getCategoryId(), category);
		}

		return categories;
	}


	private Category createCategory(Map<Integer, Integer> categoryFrequencyMap, long productCount, Object obj) {
		Category category = new Category((CategoryTable) obj);
		int parentId = category.getParentId();
		CategoryTable parentTable = (CategoryTable) findById(parentId);

		if (parentTable != null) {
			Category parent = new Category(parentTable);
			category.setParent(parent);
		}
		Integer categoryFrequency = categoryFrequencyMap.get(category.getCategoryId());
		if (categoryFrequency != null) {
			Double categoryProbability = (double) categoryFrequency / (double) productCount;
			category.setCategoryFrequency(categoryFrequency);
			category.setCategoryProbability(categoryProbability);
		}
		return category;
	}


}
