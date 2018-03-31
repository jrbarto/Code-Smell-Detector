package com.codesmell;

import java.io.IOException;
import org.codehaus.jettison.json.JSONException;

/**
 * Main class to initialize execution of the CodeHound application.
 *
 */
public class Main {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("[Error] Missing json file path argument.\n"
                    + "[Solution] Please run in the format "
                    + "'CodeSniffer.jar /path/to/json'");
            System.exit(1);
        }

        String jsonPath = args[0];
        Runner sniffRunner = new Runner(jsonPath);

        try {
            long startTime = System.currentTimeMillis();

            sniffRunner.execute();

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            int seconds =  (int) (totalTime / 1000) % 60;
            int minutes = (int) (totalTime / (1000 * 60)) % 60;
            int hours = (int) (totalTime / (1000 * 60 * 60)) % 60;
            System.out.format("[Done] The automated code review tool finished in %02d:%02d:%02d seconds.\n",
                    hours, minutes, seconds);
            System.out.println();
        }
        catch (IOException ex) {
            ex.printStackTrace(System.out);
            System.exit(1);
        }
        catch (JSONException ex) {
            ex.printStackTrace(System.out);
            System.exit(1);
        }
        finally {
            sniffRunner.cleanUp();
        }
    }
}
