package de.hpi.semrecsys.spotlight;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import de.hpi.semrecsys.spotlight.SpotResponse.SpotSurfaceForm;
import de.hpi.semrecsys.spotlight.SpotlightResponse.SurfaceForm;
import de.hpi.semrecsys.utils.JsonUtils;

public class SpotResponseFactory extends ResponseFactory {

	/*
	 * {"annotation": {"@text":
	 * "text=President Obama called Wednesday on Congress to extend a tax break for students ..."
	 * , "surfaceForm": [ {"@name":"text","@offset":"0"},
	 * {"@name":"President Obama","@offset":"5"} ] } }
	 */
	public static SpotlightResponse createResponse(JSONObject jsonObject) {
		SpotlightResponse response = new SpotResponse();
		jsonObject = JsonUtils.getJSONObjectFromJsonObject(jsonObject, "annotation");
		response.setText(JsonUtils.getStringFromJsonObject(jsonObject, "@text"));
		List<SurfaceForm> resources = getSurfaceForms(jsonObject);
		response.getSurfaceForms().addAll(resources);
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

	private static SpotSurfaceForm createSpotlightSurfaceForm(JSONObject jsonObject) {
		SpotSurfaceForm result = new SpotSurfaceForm();
		result.setName(JsonUtils.getStringFromJsonObject(jsonObject, "@name"));
		result.setOffset(JsonUtils.getStringFromJsonObject(jsonObject, "@offset"));
		return result;
	}

}
