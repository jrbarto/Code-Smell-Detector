package com.codesmell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.codesmell.gh.objects.Commit;
import com.codesmell.gh.objects.GHFile;
import com.codesmell.gh.objects.PullRequest;

/**
 * Dispatcher class to execute full repository review or pull request review.
 *
 */
public class Runner {
    private final String REVIEW_BODY = "The automated 'Code Hound' tool found issues "
            + "with this pull request.";
    private final String API_URL = "https://api.github.com";
    private String repoPath;
    private Map<String, String> scripts;
    private boolean fullReview;
    private String groovyExe;
    private GHRestClient restClient;

    public Runner(String json) {
        String jsonContent = readFile(json);

        try {
            JSONObject jsonObj = new JSONObject(jsonContent);
            String repoPath = jsonObj.getString("repoPath");
            String authHeader = jsonObj.getString("authHeader");
            boolean fullReview = jsonObj.getBoolean("fullReview");
            JSONArray scripts = jsonObj.getJSONArray("scripts");

            if (jsonObj.has("groovyExe")) {
                String groovyExe = jsonObj.getString("groovyExe");
                this.groovyExe = groovyExe;
            }

            this.repoPath = repoPath;
            this.fullReview = fullReview;
            this.restClient = new GHRestClient(API_URL, authHeader);
            this.scripts = new HashMap<String, String>();

            for (int i = 0; i < scripts.length(); i++) {
                JSONObject script = scripts.getJSONObject(i);
                String scriptPath = script.getString("path");
                String comment = "[Code Hound Automated Comment]\n" + script.getString("comment");
                this.scripts.put(scriptPath, comment);
            }

        }
        catch (JSONException ex) {
            System.out.println("[Error] Failed to parse json content : " + jsonContent);
            System.out.println(ex.getMessage());
            System.exit(1);
        }
    }

    public void execute() throws JSONException, IOException {
        if (fullReview) {
            /* Execute review on all java files in the repository */
            executeRepoReview();
        }
        else {
            /* Execute review on java files in the pull request */
            executePullReview();
        }
    }

    /**
     * Review files associated with the latest pull request.
     *
     * @throws IOException
     * @throws JSONException
     */
    public void executePullReview()
            throws IOException, JSONException
    {
        PullRequest latestPullRequest = restClient.getLatestPullRequest(repoPath);
        Commit latestCommit = null;

        if (latestPullRequest != null) {
            latestCommit = latestPullRequest.getLatestCommit();
        }
        else {
            System.out.println("[OK] No current pull requests for the " + repoPath + "repository.");
            System.exit(0); // Gracefully exit the process
        }

        ArrayList<GHFile> pullRequestFiles = restClient.getPullRequestFiles(repoPath, latestPullRequest);
        JSONArray draftComments = new JSONArray(); // Each comment associated with a position in the diff
        ArrayList<String> generalComments = new ArrayList<>(); // Comments for issues not found in diff

        System.out.println("[Action] Checking all files of commit '" + latestCommit.getSha() + "'...");
        System.out.println();
        for (GHFile ghFile : pullRequestFiles) {
            File sourceFile = ghFile.getTempFile();
            FileParser parser = new FileParser(scripts, sourceFile, groovyExe);
            Map<List<String>, String> scriptOutput = parser.runGroovyScripts(ghFile.getPath());

            for (Entry<List<String>, String> entry : scriptOutput.entrySet()) {
                List<String> lines = entry.getKey();
                String comment = entry.getValue();
                /* Find positions (line numbers) referenced in procOutput */
                if (lines.size() > 0) {
                    System.out.println("[Ok] Found issues with file '" + ghFile.getPath()
                      + "' at line numbers: " + lines.toString());
                    System.out.println();
                }

                /* For each position in procOutput, find position in diff */
                for (String lineNum : lines) {
                    try {
                        int line = Integer.parseInt(lineNum.trim());
                        int diffPos = ghFile.getDiffPosition(line);

                        if (diffPos > 0) {
                            /* The line number lies within the bounds of the diff, include it in the review */
                            System.out.println("[Action] Posting comment on file '" + ghFile.getPath() + "' at "
                              + "line " + diffPos + " in the file diff.");
                            System.out.println();
                            JSONObject draftComment = new JSONObject();
                            draftComment.put("path", ghFile.getPath());
                            draftComment.put("position", diffPos);
                            draftComment.put("body", comment);
                            draftComments.put(draftComment);
                        }
                        else {
                            /* Position not found in diff, post general comment instead */
                            String generalComment = "Comment: " + comment + "\nFile Path: "
                                    + ghFile.getPath() + "Line Number: " + line;
                            generalComments.add(generalComment);
                        }
                    }
                    catch (NumberFormatException ex) {
                        System.out.println("[Error] " + lineNum + " is not a properly formatted line number.");
                        System.out.println("[Solution] Groovy process output must consist of only integers separated "
                                + "by newlines or commas.");
                        throw ex;
                    }
                }
            }
        }

        if (draftComments.length() > 0) {
            /* Post review with draft comments */
            restClient.postReview(repoPath, latestPullRequest.getNumber(),
                    REVIEW_BODY, latestCommit.getSha(), draftComments);
        }
        if (generalComments.size() > 0) {
            /* Post general comment reviews */
            String reviewBody = generalComments.toString();
            restClient.postReview(repoPath, latestPullRequest.getNumber(),
                    reviewBody, latestCommit.getSha(), null);
        }
    }


    /**
     * Review all files in the repository.
     *
     * @throws IOException
     * @throws JSONException
     */
    public void executeRepoReview() throws JSONException, IOException {
        ArrayList<GHFile> repoFiles= restClient.getRepoFiles(repoPath);

        String lineViolations = "";
        System.out.println("[Action] Reviewing repository '" + repoPath + "...");
        System.out.println();
        for (GHFile ghFile : repoFiles) {
            File sourceFile = ghFile.getTempFile();
            FileParser parser = new FileParser(scripts, sourceFile, groovyExe);
            Map<List<String>, String> scriptOutput = parser.runGroovyScripts(ghFile.getPath());

            for (Entry<List<String>, String> entry : scriptOutput.entrySet()) {
                /* Find positions (line numbers) referenced in procOutput */
                List<String> lines = entry.getKey();
                String comment = entry.getValue();

                if (lines.size() > 0) {
                    System.out.println("[Ok] Found issues with file '" + ghFile.getPath()
                      + "' at line numbers: " + lines.toString());
                    System.out.println();

                    lineViolations += "Comment: " + comment + "\nFile Path: " + ghFile.getPath()
                            + " Line Number: " + lines.toString() + "\n";
                }
            }


        }

        if (lineViolations.length() != 0) {
            String title = "[Code Hound Automated Comment]\n Found issues with this repository.";
            String body = "The following lines are in violation:\n " + lineViolations;
            restClient.createIssue(repoPath, title, body);
        }
    }

    /**
     * Clean up resources and process streams.
     */
    public void cleanUp() {
        restClient.closeClient();
    }

    /**
     * Read the contents of a file
     * @param filePath
     * @return
     */
    private String readFile(String filePath) {
        String text = "";

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            StringBuilder builder = new StringBuilder();

            String currLine = reader.readLine();
            while (currLine != null) {
                builder.append(currLine);
                currLine = reader.readLine();
            }

            text = builder.toString();
        }
        catch (IOException ex) {
            System.out.println("[Error] Failed to read JSON file. " + filePath);
            System.exit(1);
        }

        return text;
    }
}
