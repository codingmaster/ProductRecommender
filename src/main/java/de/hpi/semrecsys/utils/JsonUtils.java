package de.hpi.semrecsys.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {
	public static Double getDoubleValue(String value) {
		Double result = null;
		if (value != null && !value.isEmpty()) {
			result = Double.valueOf(value);
		}
		return result;
	}

	public static Integer getIntValue(String value) {
		Integer result = null;
		if (value != null && !value.isEmpty()) {
			result = Integer.valueOf(value);
		}
		return result;
	}

	public static List<String> getStringList(String value, String separator) {
		List<String> arrayList = new ArrayList<String>();
		if (value != null) {
			String[] array = value.replace("\"", "").split(separator);
			arrayList = new ArrayList<String>(Arrays.asList(array));
		}
		return arrayList;
	}

	public static String getStringFromJsonObject(JSONObject jsonObject, String key) {
		String result = "";
		Object tempObj = getObjectFromJson(jsonObject, key);
		if (tempObj != null) {
			result = (String) tempObj;
		}
		return result;
	}

	public static Object getObjectFromJson(JSONObject jsonObject, String key) {
		Object result = null;
		try {
			result = jsonObject.get(key);
		} catch (JSONException ex) {

			key = key.toLowerCase();
			try {
				result = jsonObject.get(key);
			} catch (JSONException ex1) {
				System.err.println("WARN: " + ex1.getMessage());
			}
		}
		return result;
	}

	public static JSONObject getJSONObjectFromJsonObject(JSONObject jsonObject, String key) {
		JSONObject result = new JSONObject();
		Object tempObj = getObjectFromJson(jsonObject, key);
		if (tempObj != null) {
			result = (JSONObject) tempObj;
		}
		return result;
	}

	public static JSONArray getJsonArrayFromJsonObject(JSONObject jsonObject, String key) {
		JSONArray result = new JSONArray();
		Object tempObj = getObjectFromJson(jsonObject, key);
		if (tempObj != null) {
			result = (JSONArray) tempObj;
		}
		return result;
	}
}
