package com.codesmell;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.util.EntityUtils;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.codesmell.gh.objects.PullRequest;
import com.codesmell.gh.objects.Commit;
import com.codesmell.gh.objects.GHFile;

/**
 * Basic authentication REST client for GitHub to execute http commands.
 *
 */
public class GHRestClient {
    CloseableHttpClient client; // client used to execute http requests
    String serverUrl; // url of the github server
    String authHeader; // Base64 encoded authorization header

    public GHRestClient(String serverUrl, String authHeader) {
        this.serverUrl = serverUrl;

        // Determine if authHeader is Base64 encoded before using it
        if (!Base64.isBase64(authHeader)) {
            byte[] credentials = Base64.encodeBase64((authHeader).getBytes(StandardCharsets.UTF_8));
            this.authHeader = new String(credentials, StandardCharsets.UTF_8);
        }
        else {
            this.authHeader = authHeader;
        }

        HttpClientBuilder builder = HttpClientBuilder.create();
        client = builder.build();

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

    /**
     * Construct and return the latest pull request on the repository.
     *
     * @param repoPath
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public PullRequest getLatestPullRequest(String repoPath) throws IOException, JSONException {
        /* Get all pull requests on the repo */
        int requestNumber = 0;
        PullRequest pullRequest = null;
        String url = serverUrl + "/repos/" + repoPath + "/pulls";
        HttpGet request = new HttpGet(url);
        CloseableHttpResponse response = doGetRequest(request);
        JSONArray pullRequests = parseArrayResponse(response);

        /* Take the latest pull request and get its commits */
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
            System.out.println("[OK] There are no pull requests for the " + repoPath + " repository.");
            System.exit(0);
        }

        return pullRequest;
    }

    /**
     * Get all files (raw files and diffs) associated with this pull request.
     *
     * @param repoPath
     * @param pullRequest
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public ArrayList<GHFile> getPullRequestFiles(String repoPath, PullRequest pullRequest)
            throws JSONException, IOException
    {
        ArrayList<GHFile> files = new ArrayList<>();
        String url = serverUrl + "/repos/" + repoPath + "/pulls/" + pullRequest.getNumber() + "/files";
        HttpGet request = new HttpGet(url);
        CloseableHttpResponse response = doGetRequest(request);

        JSONArray filesJson = parseArrayResponse(response);

        for (int i = 0; i < filesJson.length(); i++) {
            JSONObject fileJson = filesJson.getJSONObject(i);
            String path = fileJson.getString("filename");

            /* Only review Java files */
            if (path.endsWith(".java")) {
                String contentsUrl = fileJson.getString("contents_url");
                files.add(buildGHFile(fileJson, path, contentsUrl));
            }
        }

        return files;
    }

    /**
     * Get all files (only the raw file) of a repository.
     *
     * @param repoPath
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public ArrayList<GHFile> getRepoFiles(String repoPath)
            throws JSONException, IOException
    {
        ArrayList<GHFile> files = new ArrayList<>();
        String url = serverUrl + "/repos/" + repoPath + "/contents";
        HttpGet request = new HttpGet(url);
        CloseableHttpResponse response = doGetRequest(request);

        JSONArray filesJson = parseArrayResponse(response);

        /* Recurse into all directories, starting with the root */
        files = recurseDirectory(filesJson, url, files);

        return files;
    }

    /**
     * Delve into each directory to extract each Java file.
     *
     * @param directory
     * @param url
     * @param files
     * @return
     * @throws JSONException
     * @throws ParseException
     * @throws IOException
     */
    private ArrayList<GHFile> recurseDirectory(JSONArray directory, String url, ArrayList<GHFile> files)
            throws JSONException, ParseException, IOException
    {
        for (int i = 0; i < directory.length(); i++) {
            JSONObject fileJson = directory.getJSONObject(i);
            String type = fileJson.getString("type");

            if (type.equals("file")) {
                String path = fileJson.getString("path");

                if (path.endsWith(".java")) {
                    String contentsUrl = fileJson.getString("download_url");
                    files.add(buildGHFile(fileJson, path, contentsUrl));
                }
            }
            else if (type.equals("dir")) {
                String dirPath = fileJson.getString("path");
                String pathUrl = url + "/" + dirPath;
                HttpGet request = new HttpGet(pathUrl);
                CloseableHttpResponse response = doGetRequest(request);

                JSONArray filesJson = parseArrayResponse(response);
                recurseDirectory(filesJson, url, files);
            }
        }

        return files;
    }

    /**
     * Generate a GHFile object from JSON.
     *
     * @param fileJson
     * @param path
     * @param contentsUrl
     * @return
     * @throws ParseException
     * @throws IOException
     * @throws JSONException
     */
    private GHFile buildGHFile(JSONObject fileJson, String path, String contentsUrl)
            throws ParseException, IOException, JSONException
    {
        HttpGet request = new HttpGet(contentsUrl);
        request.addHeader("Accept", "application/vnd.github.VERSION.raw");
        CloseableHttpResponse response = doGetRequest(request);
        String contents = EntityUtils.toString(response.getEntity());
        GHFile ghFile = null;

        /* Pull request files have patches (diffs), repository files do not */
        if (fileJson.has("patch")) {
            String diff = fileJson.getString("patch");
            ghFile = new GHFile(path, contents, diff);
        }
        else {
            ghFile = new GHFile(path, contents);
        }

        return ghFile;
    }

    /**
     * Post a review to request a specific change or make a general comment on a pull request.
     *
     * @param repoPath
     * @param pullNumber
     * @param body
     * @param commitId
     * @param draftComments
     * @throws IOException
     */
    public void postReview(
            String repoPath,
            int pullNumber,
            String body,
            String commitId,
            JSONArray draftComments)
    throws IOException {
        String url = serverUrl + "/repos/" + repoPath + "/pulls/"
                + Integer.toString(pullNumber) + "/reviews";
        HttpPost reviewRequest = new HttpPost(url);
        reviewRequest.setHeader("Content-Type", "application/json");
        reviewRequest.setHeader("Accept", "application/json");

        String jsonString = null;
        CloseableHttpResponse response = null;

        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("event", "REQUEST_CHANGES");
            jsonObj.put("body", body);
            jsonObj.put("commit_id", commitId);

            /* Post comments at positions in the diff if violations found in diff */
            if (draftComments != null && draftComments.length() > 0) {
                jsonObj.put("comments", draftComments);
            }

            jsonString = jsonObj.toString();

            response = doPostRequest(reviewRequest, jsonString);
        }
        catch (JSONException ex) {
            System.out.println("[Error] Failed to create JSON body with parameters: body: " + body + "commit_id: "
                    + commitId + "comments:" + draftComments.toString());
            throw new RuntimeException("Failed to create JSON body.");
        }
        finally {
            response.close();
        }

    }

    /**
     * Post an issue to the repository.
     *
     * @param repoPath
     * @param title
     * @param body
     * @throws IOException
     */
    public void createIssue(String repoPath, String title, String body) throws IOException {
        String url = serverUrl + "/repos/" + repoPath + "/issues";
        HttpPost issueRequest = new HttpPost(url);
        issueRequest.setHeader("Content-Type", "application/json");
        issueRequest.setHeader("Accept", "application/json");

        String jsonString = null;
        CloseableHttpResponse response = null;

        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("title", title);
            jsonObj.put("body", body);

            jsonString = jsonObj.toString();

            response = doPostRequest(issueRequest, jsonString);
        }
        catch (JSONException ex) {
            System.out.println("[Error] Failed to create JSON body with parameters: title: " + title + "body: " + body);
            throw new RuntimeException("Failed to create JSON body.");
        }
        finally {
            response.close();
        }
    }

    /**
     * Release any resources and close the process stream.
     */
    public void closeClient() {
        try {
            client.close();
        }
        catch (IOException ex) {
            System.out.println("[Warning] Failed to close httpclient stream.");
        }
    }

    /**
     * Extract a JSON array out of an HTTP response body.
     *
     * @param response
     * @return
     * @throws IOException
     * @throws JSONException
     */
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
        finally {
            response.close();
        }

        return jsonArr;
    }

    /**
     * Extract a JSON object from an HTTP response body.
     * @param response
     * @return
     * @throws IOException
     * @throws JSONException
     */
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
        finally {
            response.close();
        }

        return jsonObj;
    }

    /**
     * Execute an HTTP GET request.
     *
     * @param request
     * @return
     */
    private CloseableHttpResponse doGetRequest(HttpGet request) {
        CloseableHttpResponse response = doRequest(request);
        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();

        if (statusCode != 200) {
            System.out.println("[Error] GET request failed with status "
                    + statusCode + " " + statusLine.getReasonPhrase());
            throw new RuntimeException("HTTP GET request failed with status " + statusCode);
        }

        return response;
    }

    /**
     * Execute an HTTP POST request
     *
     * @param request
     * @param jsonString
     * @return
     */
    private CloseableHttpResponse doPostRequest(HttpPost request, String jsonString) {
        if (!"".equals(jsonString) && jsonString != null) {
            StringEntity requestBody = null;
            try {
                requestBody = new StringEntity(jsonString);
            }
            catch (UnsupportedEncodingException ex) {
                System.out.println("[Error] Unsupported characters in http request body: " + jsonString);
                throw new RuntimeException(ex); // throw new unchecked exception
            }

            request.setEntity(requestBody);
        }

        CloseableHttpResponse response = doRequest(request);
        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();

        if (statusCode != 200 && statusCode != 201) {
            System.out.println("[Error] POST request failed with status "
                    + statusCode + " " + statusLine.getReasonPhrase());
            throw new RuntimeException("HTTP POST request failed with status " + statusCode);
        }

        return response;
    }

    /**
     * Execute a generic HTTP request.
     *
     * @param request
     * @return
     */
    private CloseableHttpResponse doRequest(HttpUriRequest request) {
        CloseableHttpResponse response = null;
        request.setHeader("Authorization", "Basic " + authHeader);

        try {
            response = client.execute(request);
        } catch (ClientProtocolException ex) {
            System.out.println("[Error] Invalid protocol or endpoint not found.");
            ex.printStackTrace(System.out);
        } catch (IOException ex) {
            System.out.println("[Error] Http connection closed.");
            ex.printStackTrace(System.out);
        }

        return response;
    }
}