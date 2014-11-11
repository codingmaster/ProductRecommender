package de.hpi.semrecsys.spotlight;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import de.hpi.semrecsys.spotlight.AnnotateResponse.AnnotateResponseResource;
import de.hpi.semrecsys.spotlight.SpotlightConnector.EndpointType;
import de.hpi.semrecsys.utils.JsonUtils;

public class AnnotateResponseFactory extends ResponseFactory {

	public static SpotlightResponse createResponse(JSONObject jsonObject) {
		AnnotateResponse response = new AnnotateResponse();
		EndpointType type = response.getType();
		response.setText(JsonUtils.getStringFromJsonObject(jsonObject, "@text"));
		response.setConfidence(JsonUtils.getStringFromJsonObject(jsonObject, "@confidence"));
		response.setSupport(JsonUtils.getStringFromJsonObject(jsonObject, "@support"));
		response.setTypes(JsonUtils.getStringFromJsonObject(jsonObject, "@types"));
		response.setSparql(JsonUtils.getStringFromJsonObject(jsonObject, "@sparql"));
		response.setPolicy(JsonUtils.getStringFromJsonObject(jsonObject, "@policy"));

		List<AnnotateResponseResource> resources = getResources(jsonObject, type);
		response.getResources().addAll(resources);
		return response;
	}

	private static List<AnnotateResponseResource> getResources(JSONObject jsonObject, EndpointType endpointType) {
		JSONArray jsonArray = new JSONArray();
		List<AnnotateResponseResource> resources = new ArrayList<AnnotateResponseResource>();
		if (jsonObject.keySet().contains("Resources")) {
			jsonArray = JsonUtils.getJsonArrayFromJsonObject(jsonObject, "Resources");

			for (int i = 0; i < jsonArray.length(); i++) {
				AnnotateResponseResource resource = createSpotlightResponseResource((JSONObject) jsonArray.get(i));
				resources.add(resource);
			}
		}

		return resources;
	}

	private static AnnotateResponseResource createSpotlightResponseResource(JSONObject jsonObject) {
		AnnotateResponseResource result = new AnnotateResponseResource();
		result.setURI(JsonUtils.getStringFromJsonObject(jsonObject, "@URI"));
		result.setSupport(JsonUtils.getStringFromJsonObject(jsonObject, "@support"));
		result.setTypes(JsonUtils.getStringFromJsonObject(jsonObject, "@types"));
		result.setOriginString(JsonUtils.getStringFromJsonObject(jsonObject, "@surfaceForm"));

		result.setOffset(JsonUtils.getStringFromJsonObject(jsonObject, "@offset"));
		result.setSimilarity(JsonUtils.getStringFromJsonObject(jsonObject, "@similarityScore"));
		result.setPercentageOfSecondRank(JsonUtils.getStringFromJsonObject(jsonObject, "@percentageOfSecondRank"));
		return result;
	}

}
