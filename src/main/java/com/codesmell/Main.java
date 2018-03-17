package com.codesmell;

import java.io.IOException;
import org.codehaus.jettison.json.JSONException;

/**
 * Main class to initialize execution of the CodeHound application.
 *
 */
public class Main {

    public static void main(String[] args) {
        if (args.length < 5) {
            System.out.println("[Error] Missing groovy file, owner, or repo arguments.\n"
                    + "[Solution] Please run in the format "
                    + "'CodeSniffer.jar /path/to/groovyfile repoPath username:password comment true/false'");
            System.exit(1);
        }
        String groovyHome = System.getenv("JAVA_HOME");
        System.out.println("GROOVY HOME IS " + groovyHome);

        String groovyFile = args[0];
        String repoPath = args[1];    // Full path to the repo 'Org/RepoName'
        String authHeader = args[2]; // Authorization header... Will be Base64 encoded if not already
        String comment = args[3];
        boolean fullReview = Boolean.valueOf(args[4]); // Whether or not to execute the review on the full repo
        Runner sniffRunner = new Runner(groovyFile, repoPath, authHeader, comment);

        try {
            long startTime = System.currentTimeMillis();

            if (fullReview) {
                /* Execute review on all java files in the repository */
                sniffRunner.executeRepoReview();
            }
            else {
                /* Execute review on java files in the pull request */
                sniffRunner.executePullReview();
            }

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            int seconds =  (int) (totalTime / 1000) % 60;
            int minutes = (int) (totalTime / (1000 * 60)) % 60;
            int hours = (int) (totalTime / (1000 * 60 * 60)) % 60;
            System.out.format("The automated code review tool finished in %02d:%02d:%02d seconds.\n", hours, minutes, seconds);
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
            sniffRunner.cleanUp();
        }
    }
}
