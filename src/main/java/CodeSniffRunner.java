package main.java;

import java.io.File;

public class CodeSniffRunner {

	/**
	 * pass in arguments of groovyFile and source file for now
	 * e.g. 'java CodeSniffRunner groovyFile sourceFile'
	 */
	public static void main(String[] args) {
		String groovyFile = args[0];
		String sourceFile = args[1];
		/*
		 * temporarily hard coded REST client setup
		 * just a test user to prove the concept of the REST client
		 */
		GHRestClient restClient = new GHRestClient("https://api.github.com", "snifftestuser", "password123");

		FileParser parser = new FileParser(groovyFile, sourceFile);
		parser.runGroovyCommand();
	}
}
