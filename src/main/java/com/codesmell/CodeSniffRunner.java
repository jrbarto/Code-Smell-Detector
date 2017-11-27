package com.codesmell;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
		if (args.length < 2) {
			System.out.println("[Error] Missing groovy file, owner, or repo arguments.\n"
					+ "[Solution] Please run in the format 'CodeSniffer.jar /path/to/groovyfile owner repo'");
			System.exit(1);
		}

		String groovyFile = args[0];
		String owner = args[1];
		String repoName = args[2];

		/*
		 * temporarily hard coded REST client setup
		 * just a test user to prove the concept of the REST client
		 */
		GHRestClient restClient = new GHRestClient("https://api.github.com", "snifftestuser", "password123");

		try {
			PullRequest latestPullRequest = restClient.getLatestPullRequest(owner, repoName);
			if (latestPullRequest == null) {
				System.out.println("[OK] No current pull requests for the " + owner + "/" + repoName + "repository.");
			}

			for (Commit commit : latestPullRequest.getCommits()) {
				ArrayList<GHFile> commitFiles = restClient.getCommitFiles(owner, repoName, commit);

				for (GHFile ghFile : commitFiles) {
					File sourceFile = ghFile.getTempFile();
					FileParser parser = new FileParser(groovyFile, sourceFile);
					parser.runGroovyCommand();
				}
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
	}
}
