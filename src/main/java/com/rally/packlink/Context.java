package com.rally.packlink;

class Context {
	
	String url = "https://rally1.rallydev.com";
	String apiKey;
	String project;
	String workspace;		
	
	public Context(String url, String apiKey, String project) {
		if (url != null ) {
			this.url = url;
		}
		this.apiKey = apiKey;
		this.project = project;
	}

	public String getUrl() {
		return url;
	}
	
	public String getProject() {
		return project;
	}
	public String getWorkspace() {
		return workspace;
	}
	public String getApiKey() {
		return apiKey;
	}

}
