package de.hpi.semrecsys.spotlight;

public class SpotlightRequest {
	String text;
	String disambiguator = "Default";
	String spotter = "Default";
	String policy = "whitelist";
	String support = "0";
	String confidence = "0.404";

	public SpotlightRequest(String text) {
		super();
		this.text = text;
	}

	public SpotlightRequest(String text, Double confidence) {
		this.text = text;
		this.confidence = String.valueOf(confidence);
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setDisambiguator(String disambiguator) {
		this.disambiguator = disambiguator;
	}

	public void setSupport(Integer support) {
		this.support = String.valueOf(support);
	}

	public void setConfidence(Double confidence) {
		this.confidence = String.valueOf(confidence);
	}

	public String getText() {
		return text;
	}

	public String getDisambiguator() {
		return disambiguator;
	}

	public String getSupport() {
		return support;
	}

	public String getConfidence() {
		return confidence;
	}

	public String getSpotter() {
		return spotter;
	}

	public String getPolicy() {
		return policy;
	}

}
