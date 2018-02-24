package com.codesmell;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import com.codesmell.gh.objects.PullRequest;
import com.codesmell.gh.objects.Commit;
import com.codesmell.gh.objects.GHFile;

public class CodeSniffRunner {

	/**
	 * pass in arguments of groovyFile and source file for now
	 * e.g. 'java CodeSniffRunner groovyFile sourceFile'
	 */
	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("[Error] Missing groovy file, owner, or repo arguments.\n"
					+ "[Solution] Please run in the format 'CodeSniffer.jar /path/to/groovyfile owner repo comment'");
			System.exit(1);
		}

		String groovyFile = args[0];
		String owner = args[1];
		String repoName = args[2];
		String comment = args[3];

		/*
		 * temporarily hard coded REST client setup
		 * just a test user to prove the concept of the REST client
		 */
		GHRestClient restClient = new GHRestClient("https://api.github.com", "snifftestuser", "password123");

		try {
			PullRequest latestPullRequest = restClient.getLatestPullRequest(owner, repoName);
			Commit latestCommit = latestPullRequest.getLatestCommit();
			if (latestPullRequest == null) {
				System.out.println("[OK] No current pull requests for the " + owner + "/" + repoName + "repository.");
			}

			ArrayList<GHFile> pullRequestFiles = restClient.getPullRequestFiles(owner, repoName, latestPullRequest);

			System.out.println("[Action] Checking all files of commit '" + latestCommit.getSha() + "'...");
			for (GHFile ghFile : pullRequestFiles) {
				File sourceFile = ghFile.getTempFile();
				FileParser parser = new FileParser(groovyFile, sourceFile);
				String procOutput = parser.runGroovyCommand();

				//Find positions (line numbers) referenced in procOutput
				List<String> lines = new ArrayList<>(Arrays.asList(procOutput.split(",|\n")));
				lines.removeAll(Arrays.asList("", null));
				if (lines.size() > 0) {
					System.out.println("[Ok] Found issues with file '" + ghFile.getPath()
					+ "' at line numbers: " + lines.toString());
				}

				//For each position in procOutput, find position in diff
				for (String lineNum : lines) {
					try {
						int line = Integer.parseInt(lineNum.trim());
						int diffPos = ghFile.getDiffPosition(line);

						restClient.postReviewComment(owner, repoName, latestPullRequest.getNumber(),
								comment, latestCommit.getSha(), ghFile.getPath(), diffPos);
					}
					catch (NumberFormatException ex) {
						System.out.println("[Error] " + lineNum + " is not a properly formatted line number.");
						System.out.println("[Solution] Groovy process output must consist of only integers separated "
								+ "by newlines or commas.");
						throw ex;
					}
				}

				//If position not in diff, post general comment, otherwise post in diff position
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
