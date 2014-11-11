package de.hpi.semrecsys.spotlight;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import de.hpi.semrecsys.spotlight.CandidatesResponse.CandidatesResponseResource;
import de.hpi.semrecsys.spotlight.CandidatesResponse.CandidatesSurfaceForm;
import de.hpi.semrecsys.spotlight.SpotlightResponse.ResponseResource;
import de.hpi.semrecsys.spotlight.SpotlightResponse.SurfaceForm;
import de.hpi.semrecsys.utils.JsonUtils;

public class CandidatesResponseFactory extends ResponseFactory {
	/*
	 * CANDIDATES {"@text":
	 * "text=President Obama called Wednesday on Congress to extend a tax break for students include"
	 * , "surfaceForm": [ {"@name":"economic stimulus","@offset":"109"},
	 * {"@name":"Wednesday","@offset":"28", "resource": [
	 * {"@uri":"Sheffield_Wednesday_F.C.", "@finalScore":"0.059119582176208496",
	 * "@types":"DBpedia:SoccerClub, DBpedia:SportsTeam, ...",
	 * "@support":"1850", "@percentageOfSecondRank":"0.7888147494308684",
	 * "@contextualScore":"0.059119582176208496",
	 * "@priorScore":"2.6514837072202454E-5",
	 * "@label":"Sheffield Wednesday F.C."} ] } ] }
	 */

	public static SpotlightResponse createResponse(JSONObject jsonObject) {
		CandidatesResponse response = new CandidatesResponse();
		jsonObject = JsonUtils.getJSONObjectFromJsonObject(jsonObject, "annotation");
		response.setText(JsonUtils.getStringFromJsonObject(jsonObject, "@text"));

		response.getSurfaceForms().addAll(getSurfaceForms(jsonObject));

		response.getResources().addAll(getResources(jsonObject));
		return response;
	}

	private static List<SurfaceForm> getSurfaceForms(JSONObject jsonObject) {
		JSONArray jsonArray = JsonUtils.getJsonArrayFromJsonObject(jsonObject, "surfaceForm");
		List<SurfaceForm> surfaceForms = new ArrayList<SurfaceForm>();
		for (int i = 0; i < jsonArray.length(); i++) {
			SurfaceForm resource = createSpotlightSurfaceForm((JSONObject) jsonArray.get(i));
			surfaceForms.add(resource);
		}
		return surfaceForms;
	}

	// private static List<SpotlightSurfaceForm> getSurfaceForms(
	// JSONObject jsonObject, EndpointType endpointType) {
	// JSONArray jsonArray = DatatypeConverter.getJsonArrayFromJsonObject(
	// jsonObject, "surfaceForm");
	// List<SpotlightSurfaceForm> surfaceForms = new
	// ArrayList<SpotlightSurfaceForm>();
	// for (int i = 0; i < jsonArray.length(); i++) {
	// SpotlightSurfaceForm resource = createSpotlightSurfaceForm(
	// (JSONObject) jsonArray.get(i), endpointType);
	// surfaceForms.add(resource);
	// }
	// return surfaceForms;
	// }

	private static CandidatesSurfaceForm createSpotlightSurfaceForm(JSONObject jsonObject) {
		CandidatesSurfaceForm result = new CandidatesSurfaceForm();
		result.setName(JsonUtils.getStringFromJsonObject(jsonObject, "@name"));
		result.setOffset(JsonUtils.getStringFromJsonObject(jsonObject, "@offset"));
		List<ResponseResource> resources = getResources(jsonObject);
		result.getResources().addAll(resources);
		return result;
	}

	private static List<ResponseResource> getResources(JSONObject jsonObject) {
		List<ResponseResource> resources = new ArrayList<ResponseResource>();
		if (jsonObject.has("resource")) {
			Object obj = jsonObject.get("resource");
			if (obj.getClass().equals(JSONObject.class)) {
				JSONObject resourceObj = (JSONObject) obj;
				ResponseResource resource = createSpotlightResponseResource(resourceObj);
				resources.add(resource);
			} else if (obj.getClass().equals(JSONArray.class)) {
				JSONArray resourceArray = (JSONArray) obj;
				for (int j = 0; j < resourceArray.length(); j++) {
					JSONObject resourceObj = resourceArray.getJSONObject(j);
					ResponseResource resource = createSpotlightResponseResource(resourceObj);
					resources.add(resource);
				}
			}

		}

		return resources;
	}

	private static ResponseResource createSpotlightResponseResource(JSONObject jsonObject) {
		CandidatesResponseResource result = new CandidatesResponseResource();
		result.setURI(JsonUtils.getStringFromJsonObject(jsonObject, "@URI"));
		result.setSupport(JsonUtils.getStringFromJsonObject(jsonObject, "@support"));
		result.setTypes(JsonUtils.getStringFromJsonObject(jsonObject, "@types"));
		result.setFinalScore(JsonUtils.getStringFromJsonObject(jsonObject, "@finalScore"));
		result.setContextualScore(JsonUtils.getStringFromJsonObject(jsonObject, "@contextualScore"));
		result.setPriorScore(JsonUtils.getStringFromJsonObject(jsonObject, "@priorScore"));
		result.setLabel(JsonUtils.getStringFromJsonObject(jsonObject, "@label"));
		result.setPercentageOfSecondRank(JsonUtils.getStringFromJsonObject(jsonObject, "@percentageOfSecondRank"));
		return result;
	}

}
