package com.codesmell;

import java.io.File;
import java.io.IOException;

import org.codehaus.jettison.json.JSONException;

public class CodeSniffRunner {

	/**
	 * pass in arguments of groovyFile and source file for now
	 * e.g. 'java CodeSniffRunner groovyFile sourceFile'
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("[Error] Missing groovy file and source file arguments.\n"
					+ "[Solution] Please run in the format 'CodeSniffer.jar /path/to/groovyfile /path/to/sourcefile'");
			System.exit(1);
		}

		String groovyFile = args[0];
		String sourceFile = args[1];
		/*
		 * temporarily hard coded REST client setup
		 * just a test user to prove the concept of the REST client
		 */
		GHRestClient restClient = new GHRestClient("https://api.github.com", "snifftestuser", "password123");

		try {
			int latestRequestNum = restClient.getLatestPullRequest("jrbarto", "Sample-Repo");
			System.out.println("LATEST REQUEST IS : " + latestRequestNum);
		}
		catch (IOException ex) {
			ex.printStackTrace();
			System.exit(1);
		}
		catch (JSONException ex) {
			ex.printStackTrace();
			System.exit(1);
		}

		FileParser parser = new FileParser(groovyFile, sourceFile);
		parser.runGroovyCommand();
	}
}
