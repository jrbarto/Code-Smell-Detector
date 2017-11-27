package com.codesmell;

import org.apache.http.client.HttpClient;
import org.apache.http.StatusLine;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.auth.UsernamePasswordCredentials;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.auth.AuthScope;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import com.codesmell.gh.objects.PullRequest;
import com.codesmell.gh.objects.Commit;
import com.codesmell.gh.objects.GHFile;

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
			System.out.println("substring is " + serverUrl.substring(0, 7));
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

	public PullRequest getLatestPullRequest(String owner, String repoName) throws IOException, JSONException {
		int requestNumber = 0;
		PullRequest pullRequest = null;
		String url = serverUrl + "/repos/" + owner + "/" + repoName + "/pulls";
		HttpGet request = new HttpGet(url);
		CloseableHttpResponse response = doGetRequest(request);

		JSONArray pullRequests = parseArrayResponse(response);
		if (pullRequests.length() > 0) {
			JSONObject latestPullRequest = pullRequests.getJSONObject(0);
			requestNumber = latestPullRequest.getInt("number");
			pullRequest = new PullRequest(requestNumber);
			url += "/" + requestNumber + "/commits";
			request = new HttpGet(url);
			response = doGetRequest(request);
			JSONArray commits = parseArrayResponse(response);

			for (int i = 0; i < commits.length(); i++) {
				JSONObject commitJson = commits.getJSONObject(i);
				Commit newCommit = new Commit(commitJson.getString("sha"));
				pullRequest.addCommit(newCommit);
			}
		}
		else {
			System.out.println("[OK] There are no pull requests for the " + owner + "/" + repoName + " repository.");
			System.exit(0);
		}

		return pullRequest;
	}

	public ArrayList<GHFile> getCommitFiles(String owner, String repoName, Commit commit) throws JSONException, IOException {
		ArrayList<GHFile> files = new ArrayList<>();
		String url = serverUrl + "/repos/" + owner + "/" + repoName + "/commits/" + commit.getSha();
		HttpGet request = new HttpGet(url);
		CloseableHttpResponse response = doGetRequest(request);

		JSONObject commitJson = parseObjectResponse(response);
		JSONArray filesJson = commitJson.getJSONArray("files");

		for (int i = 0; i < filesJson.length(); i++) {
			JSONObject fileJson = filesJson.getJSONObject(i);
			url = fileJson.getString("contents_url");
			request = new HttpGet(url);
			request.addHeader("Accept", "application/vnd.github.VERSION.raw");
			response = doGetRequest(request);
			String path = fileJson.getString("filename");
			String patchHeader = fileJson.getString("patch");
			String contents = EntityUtils.toString(response.getEntity());
			GHFile ghFile = new GHFile(path, contents, patchHeader);
			files.add(ghFile);
		}

		return files;
	}

	private JSONArray parseArrayResponse(CloseableHttpResponse response) throws IOException, JSONException {
		String json = "";
		JSONArray jsonArr;

		try {
			json = EntityUtils.toString(response.getEntity());
			jsonArr = new JSONArray(json);
		}
		catch (IOException ex) {
			System.out.println("[Error] Failed to parse entity response.");
			throw ex;
		}
		catch (JSONException ex) {
			System.out.println("[Error] Unable to convert string to JSON:\n " + json);
			throw ex;
		}

		return jsonArr;
	}

	private JSONObject parseObjectResponse(CloseableHttpResponse response) throws IOException, JSONException {
		String json = "";
		JSONObject jsonObj;

		try {
			json = EntityUtils.toString(response.getEntity());
			jsonObj = new JSONObject(json);
		}
		catch (IOException ex) {
			System.out.println("[Error] Failed to parse entity response.");
			throw ex;
		}
		catch (JSONException ex) {
			System.out.println("[Error] Unable to convert string to JSON:\n " + json);
			throw ex;
		}

		return jsonObj;
	}

	private CloseableHttpResponse doGetRequest(HttpGet request) {
		CloseableHttpResponse response = doRequest(request);
		StatusLine statusLine = response.getStatusLine();
		int statusCode = statusLine.getStatusCode();

		//unfinished... handle errors
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