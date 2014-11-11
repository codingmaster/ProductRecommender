package de.hpi.semrecsys.spotlight;

import java.util.List;

import de.hpi.semrecsys.spotlight.SpotlightConnector.EndpointType;
import de.hpi.semrecsys.utils.JsonUtils;

public class AnnotateResponse extends SpotlightResponse {
	Double confidence;
	Double support;
	List<String> types;
	String sparql;
	String policy;

	public Double getConfidence() {
		return confidence;
	}

	public void setConfidence(String confidence) {
		this.confidence = JsonUtils.getDoubleValue(confidence);
	}

	public Double getSupport() {
		return support;
	}

	public void setSupport(String support) {
		this.support = JsonUtils.getDoubleValue(support);
	}

	public List<String> getTypes() {
		return types;
	}

	public void setTypes(String types) {
		this.types = JsonUtils.getStringList(types, ",");
	}

	public String getSparql() {
		return sparql;
	}

	public void setSparql(String sparql) {
		this.sparql = sparql;
	}

	public String getPolicy() {
		return policy;
	}

	public void setPolicy(String policy) {
		this.policy = policy;
	}

	@Override
	public EndpointType getType() {
		return EndpointType.ANNOTATE;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getJsonString());
		String result = "Response [" + "\n\t text = " + getText() + ", " + "\n\t confidence = " + getConfidence()
				+ ", " + "\n\t support = " + getSupport() + ", " + "\n\t types = " + getTypes() + ", "
		// + "\n\t sparql = " + getSparql() + ", "
		// + "\n\t policy = " + getPolicy() + ", "
		;
		builder.append(result);
		builder.append("\n\t resources = [");
		for (ResponseResource resource : getResources()) {
			builder.append(((AnnotateResponseResource) resource).toString() + " \n");
		}
		// builder.append("]\n\t surfaceForms = [");
		// for (SurfaceForm surfaceForm : getSurfaceForms()) {
		// builder.append(surfaceForm + "\n");
		// }
		builder.append("]\n]");

		return builder.toString();
	}

	public static class AnnotateResponseResource extends ResponseResource {
		Double percentageOfSecondRank;

		public Double getPercentageOfSecondRank() {
			return percentageOfSecondRank;
		}

		public void setPercentageOfSecondRank(String percentageOfSecondRank) {
			this.percentageOfSecondRank = Double.valueOf(percentageOfSecondRank);
		}

	}

}
