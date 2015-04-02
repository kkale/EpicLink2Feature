package com.rally.packlink;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.request.UpdateRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.response.UpdateResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;

public class EpicLin2Feature {

	private RallyRestApi rallyAPI;

	public static void main(String[] args) {
		String url = args[0];
		String apiKey = args[1];
		String projectName = args[2];
		String projectOid = null;

		EpicLin2Feature app = new EpicLin2Feature();

		try {
			app.rallyAPI = new RallyRestApi(new URI(url), apiKey);
			projectOid = app.resolveProjectOid(projectName);
			if (projectOid == null) {
				System.out.println("Could not find the project " + projectName);
				return;
			}
			Map<String, String> featureOIDMap = app
					.getFeatureOIDMap(projectOid);
			app.linkStoriesToFeatures(projectOid, featureOIDMap);
		} catch (URISyntaxException e) {
			System.out.println("Please check the Rally URL and API Key. "
					+ e.getMessage());
			return;
		} catch (IOException e) {
			System.out
					.println("Please make sure that you have provided the correct workspace name.");
			return;
		}

	}

	private void linkStoriesToFeatures(String project,
			Map<String, String> featureOIDMap) throws IOException {
		QueryRequest storyReq = new QueryRequest("hierarchicalrequirement");
		storyReq.setQueryFilter(new QueryFilter("c_EpicLink", "!=", "\"\""));
		QueryResponse queryResponse = rallyAPI.query(storyReq);
		if (queryResponse.wasSuccessful()) {
			String epicLink, storyRef, storyName;
			System.out.println(String.format("\nTotal results: %d",
					queryResponse.getTotalResultCount()));
			UpdateRequest updateRequest = null;
			for (JsonElement result : queryResponse.getResults()) {
				JsonObject story = result.getAsJsonObject();
				storyRef = story.get("_ref").getAsString();
				epicLink = story.get("c_EpicLink").getAsString();
				storyName = story.get("Name").getAsString();
				String featureRef = featureOIDMap.get(epicLink);
				if (featureRef == null) {
					System.out.println("Could not find the feature " + epicLink);
				} else {
					System.out.println("Connectiong the story " + storyName + " to feature " + epicLink + "(" + featureRef + ")");
				}
				JsonObject updator =  new JsonObject();
				updator.addProperty("PortfolioItem", featureRef);
	            updateRequest = new UpdateRequest(storyRef, updator);
	            UpdateResponse updateResponse = rallyAPI.update(updateRequest);
	            if (updateResponse.getObject() != null ) {
					System.out.println("Done...");
	            }  else {
            		System.out.println("Errors: ");
	            	for (String error: updateResponse.getErrors()) {
	            		System.out.println(error);
	            	}
	            }
				
			}
		} else {
			System.out.println("Errors: ");
			for (String error : queryResponse.getErrors()) {
				System.out.println(error);
			}
		}
	}

	private Map<String, String> getFeatureOIDMap(String projectOid)
			throws IOException {
		Map<String, String> featureOIDMap = new HashMap<String, String>();
		QueryRequest featureReq = new QueryRequest("PortfolioItem/Feature");
		featureReq.setQueryFilter(new QueryFilter("Project.ObjectID", "=",
				projectOid));
		QueryResponse queryResponse = rallyAPI.query(featureReq);
		if (queryResponse.wasSuccessful()) {
			String featureRef, name;
			System.out.println(String.format("\nTotal results: %d",
					queryResponse.getTotalResultCount()));
			for (JsonElement result : queryResponse.getResults()) {
				JsonObject feature = result.getAsJsonObject();
				name = feature.get("Name").getAsString();
				featureRef = feature.get("ObjectID").getAsString();
				featureOIDMap.put(name, featureRef);
			}
			
		} else {
			System.out.println("Errors: ");
			for (String error : queryResponse.getErrors()) {
				System.out.println(error);
			}
		}
		return featureOIDMap;
	}

	private String resolveProjectOid(String projectName) throws IOException {
		QueryRequest projectRequest = new QueryRequest("project");
		projectRequest.setFetch(new Fetch("ObjectID", "Name"));
		projectRequest
				.setQueryFilter(new QueryFilter("Name", "=", projectName));
		QueryResponse queryResponse = rallyAPI.query(projectRequest);
		String projectOID = null;
		if (queryResponse.wasSuccessful()) {
			System.out.println(String.format("\nTotal results: %d",
					queryResponse.getTotalResultCount()));
			for (JsonElement result : queryResponse.getResults()) {
				JsonObject project = result.getAsJsonObject();
				projectOID = project.get("ObjectID").getAsString();
			}
		}

		return projectOID;
	}

}
