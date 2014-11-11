package de.hpi.semrecsys.spotlight;

import org.apache.commons.httpclient.methods.PostMethod;

import de.hpi.semrecsys.webservice.WebServiceHelper;

public class SpotlightConnector extends WebServiceHelper {

	private static final String DEFAULT_DATATYPE = "application/json;charset=utf-8";

	public SpotlightConnector(String spotlightUrl) {
		super(spotlightUrl, DEFAULT_DATATYPE);
	}

	public SpotlightResponse getSpotlightResponse(SpotlightRequest spotlightRequest, EndpointType endpointType) {
		setEndpointType(endpointType);
		PostMethod post = getPostMethod(spotlightRequest);
		String responseJson = null;
		responseJson = readResponse(post);
		SpotlightResponse response = ResponseFactory.createResponse(responseJson, endpointType);
		return response;
	}

	private PostMethod getPostMethod(SpotlightRequest spotlightRequest) {
		PostMethod method = new PostMethod(getEndpointUrl());
		method.setRequestHeader("ContentType", "application/x-www-form-urlencoded; charset=UTF-8");
		method.setRequestHeader("Accept", getDatatype());
		method.getParams().setContentCharset(getCHARSET());
		method.setRequestHeader("Accept-Charset", getCHARSET());
		method.addParameter("disambiguator", spotlightRequest.getDisambiguator());
		method.addParameter("spotter", spotlightRequest.getSpotter());
		method.addParameter("policy", spotlightRequest.getPolicy());

		method.addParameter("confidence", spotlightRequest.getConfidence());
		method.addParameter("support", spotlightRequest.getSupport());
		method.addParameter("text", spotlightRequest.getText());
		return method;
	}

	public enum EndpointType {
		SPOT("spot/"), ANNOTATE("annotate/"),
		// DISAMBIGUATE("disambiguate/"),
		CANDIDATES("candidates/");

		String type;

		EndpointType(String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}
	}

}
