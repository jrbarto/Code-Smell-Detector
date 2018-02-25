package com.codesmell;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.codesmell.gh.objects.PullRequest;
import com.codesmell.gh.objects.Commit;
import com.codesmell.gh.objects.GHFile;

public class CodeSniffRunner {
	private static final String REVIEW_BODY = "The automated 'Code Smell Detector' tool found issues "
			+ "with this pull request.";

	public static void main(String[] args) {
		if (args.length < 4) {
			System.out.println("[Error] Missing groovy file, owner, or repo arguments.\n"
					+ "[Solution] Please run in the format 'CodeSniffer.jar /path/to/groovyfile owner repo comment'");
			System.exit(1);
		}

		String groovyFile = args[0];
		String owner = args[1];
		String repoName = args[2];
		String comment = "[Code Smell Detector Automated Comment]\n" + args[3];

		/*
		 * temporarily hard coded REST client setup
		 * just a test user to prove the concept of the REST client
		 */
		GHRestClient restClient = new GHRestClient("https://api.github.com", "snifftestuser", "password123");

		try {
			PullRequest latestPullRequest = restClient.getLatestPullRequest(owner, repoName);
			Commit latestCommit = null;

			if (latestPullRequest != null) {
				latestCommit = latestPullRequest.getLatestCommit();
			}
			else {
				System.out.println("[OK] No current pull requests for the " + owner + "/" + repoName + "repository.");
				System.exit(0); // Gracefully exit the process
			}

			ArrayList<GHFile> pullRequestFiles = restClient.getPullRequestFiles(owner, repoName, latestPullRequest);
			JSONArray draftComments = new JSONArray(); // Array of draft review comments to post

			System.out.println("[Action] Checking all files of commit '" + latestCommit.getSha() + "'...");
			for (GHFile ghFile : pullRequestFiles) {
				File sourceFile = ghFile.getTempFile();
				FileParser parser = new FileParser(groovyFile, sourceFile);
				String procOutput = parser.runGroovyCommand();

				/* Find positions (line numbers) referenced in procOutput */
				List<String> lines = new ArrayList<>(Arrays.asList(procOutput.split(",|\n")));
				lines.removeAll(Arrays.asList("", null));

				if (lines.size() > 0) {
					System.out.println("[Ok] Found issues with file '" + ghFile.getPath()
					+ "' at line numbers: " + lines.toString());
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
							JSONObject draftComment = new JSONObject();
							draftComment.put("path", ghFile.getPath());
							draftComment.put("position", diffPos);
							draftComment.put("body", comment);
							draftComments.put(draftComment);
						}
						else {
							/* Position not found in diff, post general comment instead */

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
				restClient.postReview(owner, repoName, latestPullRequest.getNumber(),
						REVIEW_BODY, latestCommit.getSha(), draftComments);
			}

		}
		catch (IOException ex) {
			ex.printStackTrace();
			System.exit(1);
		}
		catch (JSONException ex) {
			ex.printStackTrace();
			System.exit(1);
		}
		finally {
			restClient.cleanUp();
		}
	}
}
