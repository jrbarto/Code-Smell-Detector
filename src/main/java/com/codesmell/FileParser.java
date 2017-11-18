package com.codesmell;

import java.io.File;

public class FileParser {
	File groovyDir;
	File groovyFile;
	File sourceFile;
	boolean isWindows;

	public FileParser(String groovyArg, String sourceArg) {
		String os = System.getProperty("os.name");
		isWindows = os.toLowerCase().contains("windows");

		groovyFile = new File(groovyArg);
		sourceFile = new File(sourceArg);

		if (!groovyFile.exists() || !groovyFile.isFile()) {
			System.out.println("[Error] Groovy file " + groovyFile.getAbsolutePath() + " doesn't exist.");
			throw new RuntimeException("Missing groovy file " + groovyFile.getAbsolutePath());
		}
		if (!sourceFile.exists() || !sourceFile.isFile()) {
			System.out.println("[Error] Source file " + groovyFile.getAbsolutePath() + " doesn't exist.");
			throw new RuntimeException("Missing source file " + groovyFile.getAbsolutePath());
		}
	}

	public void runGroovyCommand() {
		ProcessHelper procHelper = new ProcessHelper(groovyDir);
		String groovyCommand; // must be on system path

		if (isWindows) {
			groovyCommand = "groovy.bat";
		}
		else {
			groovyCommand = "groovy";
		}

		String message = "Running groovy file " + groovyFile.getName() + " on source file " + sourceFile.getName();
		String classpath = System.getProperty("java.class.path");
		String[] args = {groovyCommand, "-cp", classpath, groovyFile.getAbsolutePath(), sourceFile.getAbsolutePath()};

		// execute a new process with the given commmand line arguments
		procHelper.runCommand(message, args);
	}

}
