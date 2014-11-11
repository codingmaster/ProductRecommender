package de.hpi.semrecsys.spotlight;

import java.util.ArrayList;
import java.util.List;

import de.hpi.semrecsys.spotlight.SpotlightConnector.EndpointType;
import de.hpi.semrecsys.utils.JsonUtils;

public abstract class SpotlightResponse {

	String jsonString;

	String text;
	EndpointType type;

	List<ResponseResource> resources = new ArrayList<ResponseResource>();
	List<SurfaceForm> surfaceForms = new ArrayList<SurfaceForm>();

	public SpotlightResponse() {
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		if (text != null) {
			text = text.replace("text=", "");
		}
		this.text = text;
	}

	public List<ResponseResource> getResources() {
		return resources;
	}

	public void setResources(List<ResponseResource> resources) {
		this.resources = resources;
	}

	public List<SurfaceForm> getSurfaceForms() {
		return surfaceForms;
	}

	public void setSurfaceForms(List<SurfaceForm> surfaceForms) {
		this.surfaceForms = surfaceForms;
	}

	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}

	public String getJsonString() {
		return jsonString;
	}

	public abstract EndpointType getType();

	public static class SurfaceForm {
		String name;
		double offset;

		public SurfaceForm() {
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public double getOffset() {
			return offset;
		}

		public void setOffset(String offset) {
			this.offset = JsonUtils.getDoubleValue(offset);
		}

	}

	/*
	 * {"@uri":"Sheffield_Wednesday_F.C.", "@finalScore":"0.059119582176208496",
	 * "@types":"DBpedia:SoccerClub, DBpedia:SportsTeam, ...",
	 * "@support":"1850", "@percentageOfSecondRank":"0.7888147494308684",
	 * "@contextualScore":"0.059119582176208496",
	 * "@priorScore":"2.6514837072202454E-5",
	 * "@label":"Sheffield Wednesday F.C."}
	 */

	public static class ResponseResource {
		String URI;
		Double support;
		List<String> types;
		String originString;
		Double similarity;
		Integer offset = 0;
		boolean isMeta = false;

		public ResponseResource() {
		}

		public String getURI() {
			return URI;
		}

		public void setURI(String uRI) {
			URI = uRI;
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

		public String getOriginString() {
			return originString;
		}

		public void setOriginString(String originString) {
			this.originString = originString;
		}

		public Integer getOffset() {

			return offset;
		}

		public void setOffset(String offset) {
			this.offset = JsonUtils.getIntValue(offset);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((URI == null) ? 0 : URI.hashCode());
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
			ResponseResource other = (ResponseResource) obj;
			if (URI == null) {
				if (other.URI != null)
					return false;
			} else if (!URI.equals(other.URI))
				return false;
			return true;
		}

		public Double getSimilarity() {
			return similarity;
		}

		public void setMeta(boolean isMeta) {
			this.isMeta = isMeta;
		}

		public boolean isMeta() {
			return isMeta;
		}

		public void setSimilarity(String similarityScore) {
			this.similarity = JsonUtils.getDoubleValue(similarityScore);
		}

		@Override
		public String toString() {
			return getOriginString() + "(" + getOffset() + ")" + "\t" + getURI() + "\t" + getSimilarity();
		}

	}

}