package de.hpi.semrecsys.model;

import de.hpi.semrecsys.ProductTable;

/**
 * Representation of attribute with its type and value
 * @author Michael Wolowyk
 *
 */
public class Attribute {

	String attributeCode;
	String value;
	Long optionId = -1L;
	Double weight;
	Integer count = 0;
	int productId;
	AttributeType type;

	public Attribute(String attributeCode, String value, Long optionId) {
		super();
		this.attributeCode = attributeCode;
		this.value = value;
		this.optionId = optionId;
	}

	public Attribute(ProductTable productLine) {
		init(productLine);
	}

	public Attribute(String attributeCode) {
		this.attributeCode = attributeCode;
	}

	private void init(ProductTable productLine) {
		this.attributeCode = productLine.getId().getAttributeCode();
		this.value = productLine.getValue();
		this.type = AttributeType.valueOf(productLine.getType());
		long optionId = productLine.getId().getOptionId();
		this.optionId = optionId;
		this.productId = productLine.getId().getEntityId();
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public void addCount(Integer count) {
		this.count += count;

	}

	public Integer getCount() {
		return count;
	}

	public Double getWeight() {
		return weight;
	}

	public String getAttributeCode() {
		return attributeCode;
	}

	public String getValue() {
		return value;
	}

	public Long getOptionId() {
		return optionId;
	}

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public AttributeType getType() {
		return type;
	}

	public void setType(AttributeType type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributeCode == null) ? 0 : attributeCode.hashCode());
		result = prime * result + ((optionId == null) ? 0 : optionId.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Attribute other = (Attribute) obj;
		if (attributeCode == null) {
			if (other.attributeCode != null)
				return false;
		} else if (!attributeCode.equals(other.attributeCode))
			return false;
		if (optionId == null) {
			if (other.optionId != null)
				return false;
		} else if (!optionId.equals(other.optionId))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "<b>" + attributeCode + "</b> : " + value;
	}

	public enum AttributeType {
		struct, unstruct, split, cat, img;
	}
}
