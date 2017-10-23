package main.java;

import org.apache.http.client.HttpClient;
import org.apache.http.StatusLine;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.auth.UsernamePasswordCredentials;
import java.io.IOException;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;


/**
 * Basic authentication REST client for GitHub to execute http commands
 *
 */
public class GHRestClient {
	CloseableHttpClient client; // client used to execute http requests
	String serverUrl; // url of the github server

	public GHRestClient(String serverUrl, String username, String password) {
		this.serverUrl = serverUrl;

		// configure http client
		CredentialsProvider provider = new BasicCredentialsProvider();
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
		provider.setCredentials(AuthScope.ANY, credentials);
		HttpClientBuilder builder = HttpClientBuilder.create();
		client = builder.setDefaultCredentialsProvider(provider).build();

		// check for malformed URL
		if (!serverUrl.isEmpty() && serverUrl != null) {
			if (!serverUrl.substring(0, 7).equalsIgnoreCase("http://")
					&& !serverUrl.substring(0, 8).equalsIgnoreCase("https://"))
			{
				System.out.println("[Error] An HTTP protocol (http:// or https://) must be prepended to server URL: "
						+ serverUrl);
				throw new RuntimeException("Missing HTTP protocol in URL: " + serverUrl);
			}
		}
	}

	/*
	 * to do: methods to pull down code, add comments, create webhooks to repos
	 */

	private CloseableHttpResponse doGetRequest(HttpGet request) {
		CloseableHttpResponse response = doRequest(request);
		StatusLine statusLine = response.getStatusLine();
		int statusCode = statusLine.getStatusCode();

		//unfinished
		return response;
	}

	private CloseableHttpResponse doRequest(HttpUriRequest request) {
		CloseableHttpResponse response = null;

		try {
			response = client.execute(request);
		} catch (ClientProtocolException ex) {
			System.out.println("[Error] Invalid protocol or endpoint not found.");
			ex.printStackTrace();
		} catch (IOException ex) {
			System.out.println("[Error] Http connection closed.");
			ex.printStackTrace();
		}

		return response;
	}



}