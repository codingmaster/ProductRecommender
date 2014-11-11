package de.hpi.semrecsys.model;

import java.util.ArrayList;
import java.util.List;

import de.hpi.semrecsys.spotlight.SpotlightResponse.ResponseResource;


/**
 * mapping between {@link Attribute} and an {@link Entity}
 */
public class AttributeEntity implements Comparable<AttributeEntity>{

	private static final Double MAX_WEIGHT = 5.0;
	Attribute attribute;
	Entity entity;
	List<ResponseResource> responseResources = new ArrayList<ResponseResource>();
	Double weight = 0.0;
	Integer count = 0;

	@Override
	public String toString() {
		return toSimpleString();
		// return getProduct() + "\t" + getEntity() + "\t" + getWeight() + "\n";
	}

	public String toSimpleString() {

		return getAttribute().getAttributeCode() + "\t" + getEntity().toSimpleString() + "\n";
	}

	public Integer getWeight() {
		if (weight < 1.0) {
			weight = 1.0;
		} else if (weight > MAX_WEIGHT) {
			weight = MAX_WEIGHT;
		}
		return weight.intValue();
	}

	public AttributeEntity() {
	}

	public AttributeEntity(Attribute attribute, Entity entity) {
		this.attribute = attribute;
		this.entity = entity;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public Entity getEntity() {
		return entity;
	}

	public String getValue() {
		return attribute.getValue();
	}

	public List<ResponseResource> getResponseResources() {
		return responseResources;
	}

	public String getResponseResourcesString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		int idx = 0;
		for (ResponseResource resource : getResponseResources()) {
			if (idx > 0) {
				builder.append(", ");
			}
			builder.append(resource.getOriginString() + "(" + resource.getOffset() + ")");
			idx++;
		}
		builder.append("]");
		return builder.toString();
	}

	public void addResponseResources(List<ResponseResource> responseList) {
		responseResources.addAll(responseList);
		for (ResponseResource resource : responseList) {
			if (resource.getSimilarity() != null) {
				weight += resource.getSimilarity();
			}
		}

	}

	public void addResponseResource(ResponseResource resource) {
		responseResources.add(resource);
		if (resource.getSimilarity() != null) {
			weight += resource.getSimilarity();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attribute == null) ? 0 : attribute.hashCode());
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
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
		AttributeEntity other = (AttributeEntity) obj;
		if (attribute == null) {
			if (other.attribute != null)
				return false;
		} else if (!attribute.equals(other.attribute))
			return false;
		if (entity == null) {
			if (other.entity != null)
				return false;
		} else if (!entity.equals(other.entity))
			return false;
		return true;
	}

    @Override
    public int compareTo(AttributeEntity o) {
        return responseResources.size() - o.responseResources.size();
    }
}
