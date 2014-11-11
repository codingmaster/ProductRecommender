package de.hpi.semrecsys.model;

import java.util.ArrayList;
import java.util.List;

import de.hpi.semrecsys.CategoryTable;
import de.hpi.semrecsys.persistence.CategoryDAO;


/**
 * Product category class created from {@link CategoryTable}
 * @author Michael Wolowyk
 *
 */
public class Category extends CategoryTable {

	CategoryDAO manager = CategoryDAO.getDefault();
	private Category parent;
	List<Category> children = new ArrayList<Category>();
	private Integer categoryFrequency;
	private Double categoryProbability;

	public Category(CategoryTable categoryTable) {
		super(categoryTable.getCategoryId(), categoryTable.getParentId(), categoryTable.getChildrenCount(),
				categoryTable.getLevel(), categoryTable.getValue(), categoryTable.getPath());

	}

	public void addChild(Category child) {
		children.add(child);
		child.setParent(this);
	}

	public List<Category> getChildren() {
		return children;
	}

	public void setParent(Category parent) {
		this.parent = parent;
	}

	public Category getParent() {
		return parent;
	}

	public String hierarchyToString() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < this.getLevel(); i++) {
			builder.append("\t");
		}
		builder.append(toString() + "\n");
		for (Category child : getChildren()) {
			builder.append(child.hierarchyToString());
		}

		return builder.toString();
	}

	public Integer getCategoryFrequency() {
		return categoryFrequency;
	}

	public void setCategoryFrequency(Integer categoryFrequency) {
		this.categoryFrequency = categoryFrequency;
	}

	public Double getCategoryProbability() {
		return categoryProbability;
	}

	public void setCategoryProbability(Double categoryProbability) {
		this.categoryProbability = categoryProbability;
	}

	public Object getValue(CategoryAttribute attribute) {
		Object result = null;
		switch (attribute) {
		case level:
			result = getLevel();
			break;
		case probability:
			result = getCategoryProbability();
			break;
		case frequency:
			result = getCategoryFrequency();
			break;
		default:
			break;
		}
		return result;
	}

	public enum CategoryAttribute {
		level, probability, frequency
	}
}
