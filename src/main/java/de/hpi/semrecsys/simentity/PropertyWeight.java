package de.hpi.semrecsys.simentity;

public class PropertyWeight {

	String propertyType;
	String propertyValue;
	Integer propertyTypeWeight;
	Integer propertyValueWeight;
	static Integer NUMBER_OF_TRIPLES;

	public static void setNUMBER_OF_TRIPLES(Integer nUMBER_OF_TRIPLES) {
		NUMBER_OF_TRIPLES = nUMBER_OF_TRIPLES;
	}

	public static Integer getNUMBER_OF_TRIPLES() {
		return NUMBER_OF_TRIPLES;
	}

	public String getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(String propertyType) {
		this.propertyType = propertyType;
	}

	public String getPropertyValue() {
		return propertyValue;
	}

	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}

	public double getPropertyTypeWeight() {
		return ((double) propertyTypeWeight);
	}

	public void setPropertyTypeWeight(Integer propertyTypeWeight) {
		this.propertyTypeWeight = propertyTypeWeight;
	}

	public double getPropertyValueWeight() {
		return ((double) propertyValueWeight);
	}

	public void setPropertyValueWeight(Integer propertyValueWeight) {
		this.propertyValueWeight = propertyValueWeight;
	}

	public Double getCalculatedWeight() {
		Double weight = 0.0;
		if (propertyTypeWeight != null && propertyValueWeight != null) {
			weight = calculateIDF((double) propertyValueWeight) * calculateIDF((double) propertyTypeWeight);
		}
		return weight;
	}

	private Double calculateIDF(Double value) {
		Double result = Math.log(NUMBER_OF_TRIPLES / value);
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((propertyType == null) ? 0 : propertyType.hashCode());
		result = prime * result + ((propertyValue == null) ? 0 : propertyValue.hashCode());
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
		PropertyWeight other = (PropertyWeight) obj;
		if (propertyType == null) {
			if (other.propertyType != null)
				return false;
		} else if (!propertyType.equals(other.propertyType))
			return false;
		if (propertyValue == null) {
			if (other.propertyValue != null)
				return false;
		} else if (!propertyValue.equals(other.propertyValue))
			return false;
		return true;
	}

	@Override
	public String toString() {

		return propertyType + ": " + getPropertyTypeWeight() + "; " + propertyValue + ": " + getPropertyValueWeight()
				+ " = " + getCalculatedWeight();
	}

}
