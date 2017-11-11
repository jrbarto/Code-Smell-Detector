package com.codesmell;

import java.io.File;

public class FileParser {
	File groovyDir;
	File groovyFile;
	File sourceFile;

	public FileParser(String groovyArg, String sourceArg) {
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
		String message = "Running groovy file " + groovyFile.getName() + " on source file " + sourceFile.getName();
		String groovyCommand = "groovy"; // must be on system path, but will change in future updates
		String[] args = {groovyCommand, groovyFile.getAbsolutePath(), sourceFile.getAbsolutePath()};

		// execute a new process with the given commmand line arguments
		procHelper.runCommand(message, args);
	}


}
