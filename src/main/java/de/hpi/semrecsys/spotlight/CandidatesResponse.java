package de.hpi.semrecsys.spotlight;

import java.util.ArrayList;
import java.util.List;

import de.hpi.semrecsys.spotlight.SpotlightConnector.EndpointType;
import de.hpi.semrecsys.utils.JsonUtils;

public class CandidatesResponse extends SpotlightResponse {

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getJsonString());
		String result = "Response [" + "\n\t text = " + getText() + ", ";
		builder.append(result);
		builder.append("]\n\t surfaceForms = [");
		for (SurfaceForm surfaceForm : getSurfaceForms()) {
			CandidatesSurfaceForm candidatesSurfaceForm = (CandidatesSurfaceForm) surfaceForm;
			builder.append(candidatesSurfaceForm + "\n");
		}
		builder.append("]\n]");

		return builder.toString();
	}

	@Override
	public EndpointType getType() {
		return EndpointType.CANDIDATES;
	}

	public static class CandidatesResponseResource extends ResponseResource {
		String label;
		Double priorScore;
		Double finalScore;
		Double contextualScore;
		Double percentageOfSecondRank;

		public Double getPercentageOfSecondRank() {
			return percentageOfSecondRank;
		}

		public void setPercentageOfSecondRank(String percentageOfSecondRank) {
			this.percentageOfSecondRank = Double.valueOf(percentageOfSecondRank);
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public Double getPriorScore() {
			return priorScore;
		}

		public void setPriorScore(String priorScore) {
			this.priorScore = JsonUtils.getDoubleValue(priorScore);
		}

		public Double getFinalScore() {
			return finalScore;
		}

		public void setFinalScore(String finalScore) {
			this.finalScore = JsonUtils.getDoubleValue(finalScore);
		}

		public Double getContextualScore() {
			return contextualScore;
		}

		public void setContextualScore(String contextualScore) {
			this.contextualScore = JsonUtils.getDoubleValue(contextualScore);
		}

		@Override
		public String toString() {
			return "\n\t\t {" + " URI = "
					+ getURI()
					// + ", \n\t Support = " + getSupport()
					+ ", \n\t Types = "
					+ getTypes()
					// + ", \n\t Offset = " + getOffset()
					+ ", \n\t PercentageOfSecondRank = " + getPercentageOfSecondRank() + ", \n\t Label = " + getLabel()
					+ ", \n\t PriorScore = " + getPriorScore() + ", \n\t FinalScore = " + getFinalScore()
					+ ", \n\t ContextualScore = " + getContextualScore() + "}";
		}

		public String toAnalyseString() {
			String result = getLabel() + "\t" + getURI() + "\t" + getPriorScore() + "\t" + getFinalScore() + "\t"
					+ getContextualScore();
			return result;
		}

	}

	public static class CandidatesSurfaceForm extends SurfaceForm {
		List<ResponseResource> resources = new ArrayList<ResponseResource>();

		public List<ResponseResource> getResources() {
			return resources;
		}

		public void setResources(List<ResponseResource> resources) {
			this.resources = resources;
		}

		@Override
		public String toString() {
			String result = "\n\t\t [\n\t\t Name = " + getName()
			// + ", \n\t\t Offset = " + getOffset()
					+ ", \n\t\t Resources = [";
			for (ResponseResource resource : getResources()) {
				result += resource + " , \n";
			}
			result += "]";
			return result;
		}
	}

}
