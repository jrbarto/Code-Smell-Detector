package com.codesmell;

import java.io.IOException;
import org.codehaus.jettison.json.JSONException;

public class Main {

	public static void main(String[] args) {
		if (args.length < 7) {
			System.out.println("[Error] Missing groovy file, owner, or repo arguments.\n"
					+ "[Solution] Please run in the format "
					+ "'CodeSniffer.jar /path/to/groovyfile owner repo username password comment true/false'");
			System.exit(1);
		}

		String groovyFile = args[0];
		String owner = args[1];
		String repoName = args[2];
		String username = args[3];
		String password = args[4];
		String comment = args[5];
		boolean fullReview = Boolean.valueOf(args[6]); // Whether or not to execute the review on the full repo
		Runner sniffRunner = new Runner(groovyFile, owner, repoName, username, password, comment);

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
