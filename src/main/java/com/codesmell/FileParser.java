package com.codesmell;

import java.io.File;
import java.util.List;

/**
 * Helper class to build the proper CLI command for executing Groovy scripts.
 *
 */
public class FileParser {
    private static String GROOVY_HOME = "/bin/groovy";
    File groovyFile;
    File sourceFile;
    boolean isWindows;

    public FileParser(File groovyFile, File sourceFile) {
        String os = System.getProperty("os.name");
        isWindows = os.toLowerCase().contains("windows");

        this.groovyFile = groovyFile;
        this.sourceFile = sourceFile;


        if (!groovyFile.exists() || !groovyFile.isFile()) {
            System.out.println("[Error] Groovy file " + groovyFile.getAbsolutePath() + " doesn't exist.");
            throw new RuntimeException("Missing groovy file " + groovyFile.getAbsolutePath());
        }
        if (!sourceFile.exists() || !sourceFile.isFile()) {
            System.out.println("[Error] Source file " + groovyFile.getAbsolutePath() + " doesn't exist.");
            throw new RuntimeException("Missing source file " + groovyFile.getAbsolutePath());
        }
    }

    /**
     * Run the groovy process on a specific file from github on the command line.
     *
     * @param filePath The path to the Github file.
     * @return The output from the groovy process.
     */
    public String runGroovyCommand(String filePath) {
        ProcessHelper procHelper = new ProcessHelper();
        String groovyCommand; // must be on system path

        if (isWindows) {
            groovyCommand = "groovy.bat";
        }
        else {
            String groovyHome = System.getenv("GROOVY_HOME");

            if (groovyHome == null || groovyHome.isEmpty()) {
              groovyCommand = GROOVY_HOME + "/bin/groovy";
            }
            else {
              groovyCommand = groovyHome + "/bin/groovy";
            }
        }

        String message = "[Action] Checking file " + filePath + "...";
        String classpath = System.getProperty("java.class.path");
        String[] args = {groovyCommand, "-cp" , classpath, groovyFile.getAbsolutePath(), sourceFile.getAbsolutePath()};

        // execute a new process with the given commmand line arguments
        String procOutput = procHelper.runCommandWithOutput(message, args);
        return procOutput;
    }
}
