package de.hpi.semrecsys.spotlight;

import org.json.JSONObject;

import de.hpi.semrecsys.spotlight.SpotlightConnector.EndpointType;

/*
 *
 * ANNOTATIONS
 * 
 * {
 "@text": "text=President Obama called Wednesday on Congress to extend a tax break for students included in last year's economic stimulus package, arguing that the policy provides more generous assistance",
 "@confidence": "0.2",
 "@support": "20",
 "@types": "",
 "@sparql": "",
 "@policy": "whitelist",
 "Resources":   [
 {
 "@URI": "http://dbpedia.org/resource/Presidency_of_Barack_Obama",
 "@support": "134",
 "@types": "DBpedia:OfficeHolder,DBpedia:Person,Schema:Person,Freebase:/book/book_subject,Freebase:/book,Freebase:/book/periodical_subject,Freebase:/media_common/quotation_subject,Freebase:/media_common,DBpedia:TopicalConcept",
 "@surfaceForm": "President Obama",
 "@offset": "5",
 "@similarityScore": "0.17918290197849274",
 "@percentageOfSecondRank": "-1.0"
 },

 ...

 ]
 }

 */

public class ResponseFactory {

	public static SpotlightResponse createResponse(String jsonString, EndpointType type) {
		SpotlightResponse response = null;
		if (jsonString == null) {
			response = new SpotlightResponse() {

				@Override
				public EndpointType getType() {
					// TODO Auto-generated method stub
					return null;
				}
			};
		}
		JSONObject jsonObject = new JSONObject(jsonString);
		response = createResponse(jsonObject, type);
		return response;
	}

	public static SpotlightResponse createResponse(JSONObject jsonObject, EndpointType type) {

		SpotlightResponse response = createEmptyResponse();

		if (jsonObject != null && jsonObject.toString().length() > 2) {
			switch (type) {
			case ANNOTATE:
				response = AnnotateResponseFactory.createResponse(jsonObject);
				break;
			case CANDIDATES:
				response = CandidatesResponseFactory.createResponse(jsonObject);
				break;
			case SPOT:
				response = SpotResponseFactory.createResponse(jsonObject);
				break;
			default:
				break;
			}
			response.setJsonString(jsonObject.toString());
		}

		return response;
	}

	private static SpotlightResponse createEmptyResponse() {
		SpotlightResponse response = new SpotlightResponse() {

			@Override
			public EndpointType getType() {
				return null;
			}
		};
		return response;
	}

}
