package com.codesmell;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private String groovyFile;
    private String repoPath;
    private String comment;
    private GHRestClient restClient;

    public Runner(String groovyFile, String repoPath, String authHeader, String comment) {
        this.groovyFile = groovyFile;
        this.repoPath = repoPath;
        this.comment = "[Code Hound Automated Comment]\n" + comment;
        this.restClient = new GHRestClient(API_URL, authHeader);
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
            FileParser parser = new FileParser(groovyFile, sourceFile);
            String procOutput = parser.runGroovyCommand();

            /* Find positions (line numbers) referenced in procOutput */
            List<String> lines = new ArrayList<>(Arrays.asList(procOutput.split(",|\n")));
            lines.removeAll(Arrays.asList("", null)); // Remove all empty and null line entries

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
                        String generalComment = "File Path: " + ghFile.getPath() + "Line Number: " + line;
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

        if (draftComments.length() > 0) {
            /* Post review with draft comments */
            restClient.postReview(repoPath, latestPullRequest.getNumber(),
                    REVIEW_BODY, latestCommit.getSha(), draftComments);
        }
        if (generalComments.size() > 0) {
            /* Post general comment reviews */
            String reviewBody = comment + "\nIssue found at the following positions:\n" + generalComments.toString();
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
        System.out.println("[Action] Checking all files in repository '" + repoPath + "'...");
        System.out.println();
        for (GHFile ghFile : repoFiles) {
            File sourceFile = ghFile.getTempFile();
            FileParser parser = new FileParser(groovyFile, sourceFile);
            String procOutput = parser.runGroovyCommand();

            /* Find positions (line numbers) referenced in procOutput */
            List<String> lines = new ArrayList<>(Arrays.asList(procOutput.split(",|\n")));
            lines.removeAll(Arrays.asList("", null)); // Remove all empty and null line entries

            if (lines.size() > 0) {
                System.out.println("[Ok] Found issues with file '" + ghFile.getPath()
                  + "' at line numbers: " + lines.toString());
                System.out.println();

                lineViolations += "File Path: " + ghFile.getPath()
                        + " Line Number: " + lines.toString() + "\n";
            }
        }

        if (lineViolations.length() != 0) {
            String title = "[Code Hound Automated Comment]\n Found issues with this repository.";
            String body = comment + "\nThe following lines are in violation:\n " + lineViolations;
            restClient.createIssue(repoPath, title, body);
        }
    }

    /**
     * Clean up resources and process streams.
     */
    public void cleanUp() {
        restClient.closeClient();
    }
}
