package com.codesmell;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Helper class to build the proper CLI command for executing Groovy scripts.
 *
 */
public class FileParser {
    Map<String, String> groovyScripts;
    File sourceFile;
    String groovyExe;
    boolean isWindows;

    public FileParser(Map<String, String> groovyScripts, File sourceFile, String groovyExe) {
        String os = System.getProperty("os.name");
        isWindows = os.toLowerCase().contains("windows");

        this.groovyScripts = groovyScripts;
        this.sourceFile = sourceFile;
        this.groovyExe = groovyExe;


        if (!sourceFile.exists() || !sourceFile.isFile()) {
            System.out.println("[Error] Source file " + sourceFile.getAbsolutePath() + " doesn't exist.");
            throw new RuntimeException("Missing source file " + sourceFile.getAbsolutePath());
        }
    }

    /**
     * Run the groovy process on a specific file from github on the command line.
     *
     * @param filePath The path to the Github file.
     * @return The output from the groovy process.
     */
    public Map<List<String>, String> runGroovyScripts(String filePath) {
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

        String classpath = System.getProperty("java.class.path");

        ExecutorService executor = Executors.newCachedThreadPool();
        Map<Future<String>, String> results = new HashMap<Future<String>, String>();
        Map<List<String>, String> linesComments = new HashMap<List<String>, String>();

        for (Entry<String, String> entry : groovyScripts.entrySet()) {
            String scriptPath = entry.getKey();
            String comment = entry.getValue();
            File groovyFile = new File(scriptPath);

            if (!groovyFile.exists() || !groovyFile.isFile()) {
                System.out.println("[Error] Groovy file " + groovyFile.getAbsolutePath() + " doesn't exist.");
                throw new RuntimeException("Missing groovy file " + groovyFile.getAbsolutePath());
            }

            String message = "[Action] Checking file " + filePath + " with groovy script: " + groovyFile.getName() + "...";

            String[] args = {groovyExe, "-cp" , classpath, groovyFile.getAbsolutePath(), sourceFile.getAbsolutePath()};

            GroovyThread thread = new GroovyThread(args, message);
            // execute a new process with the given commmand line arguments
            Future<String> output = executor.submit(thread);
            results.put(output, comment);
        }

        for (Entry<Future<String>, String> result : results.entrySet()) {
            Future<String> futureOutput = result.getKey();

            try {
                String comment = result.getValue();
                String output = futureOutput.get(30, TimeUnit.SECONDS);

                /* Find positions (line numbers) referenced in procOutput */
                List<String> lines = new ArrayList<>(Arrays.asList(output.split(",|\n")));
                lines.removeAll(Arrays.asList("", null)); // Remove all empty and null line entries

                if (lines.size() > 0) {
                    linesComments.put(lines, comment);
                }
            }
            catch (Exception ex) {
                futureOutput.cancel(true);
            }
        }

        executor.shutdown();
        return linesComments;
    }
}
