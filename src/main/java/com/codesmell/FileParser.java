package com.codesmell;

import java.io.File;
import java.util.List;

/**
 * Helper class to build the proper CLI command for executing Groovy scripts.
 *
 */
public class FileParser {
    File groovyScript;
    File sourceFile;
    String groovyExe;
    boolean isWindows;

    public FileParser(File groovyScript, File sourceFile, String groovyExe) {
        String os = System.getProperty("os.name");
        isWindows = os.toLowerCase().contains("windows");

        this.groovyScript = groovyScript;
        this.sourceFile = sourceFile;
        this.groovyExe = groovyExe;


        if (!groovyScript.exists() || !groovyScript.isFile()) {
            System.out.println("[Error] Groovy file " + groovyScript.getAbsolutePath() + " doesn't exist.");
            throw new RuntimeException("Missing groovy file " + groovyScript.getAbsolutePath());
        }
        if (!sourceFile.exists() || !sourceFile.isFile()) {
            System.out.println("[Error] Source file " + groovyScript.getAbsolutePath() + " doesn't exist.");
            throw new RuntimeException("Missing source file " + groovyScript.getAbsolutePath());
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

        if (isWindows) {
            if (groovyExe == null || groovyExe.isEmpty()) {
                groovyExe = "groovy.bat";
            }
        }
        else {
            if (groovyExe == null || groovyExe.isEmpty()) {
              groovyExe = "groovy";
            }
        }

        String message = "[Action] Checking file " + filePath + "...";
        String classpath = System.getProperty("java.class.path");
        String[] args = {groovyExe, "-cp" , classpath, groovyScript.getAbsolutePath(), sourceFile.getAbsolutePath()};

        // execute a new process with the given commmand line arguments
        String procOutput = procHelper.runCommandWithOutput(message, args);
        return procOutput;
    }
}
