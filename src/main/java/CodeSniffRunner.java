package main.java;

import java.io.File;

public class CodeSniffRunner {

	/**
	 * pass in arguments of groovyFile and source file for now
	 * e.g. 'java CodeSniffRunner groovyFile sourceFile'
	 */
	public static void main(String[] args) {
		String groovyFile = args[1];
		String sourceFile = args[2];
		File f = new File(".", "src/main/groovy");
		/*
		 * temporarily hard coded REST client setup
		 * just a test user to prove the concept of the REST client
		 */
		GHRestClient restClient = new GHRestClient("snifftestuser", "password123", "https://api.github.com");

		FileParser parser = new FileParser(groovyFile, sourceFile);
		parser.runGroovyCommand();
	}
}
