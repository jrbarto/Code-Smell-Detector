package com.codesmell;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.Arrays;

/**
 * Class to execute processes on the machine
 *
 */
public class ProcessHelper {
	ProcessBuilder pb;
	OutputStream out = System.out; // used to flush stdout

	public ProcessHelper(File workDir) {
		pb = new ProcessBuilder().directory(workDir);
	}

	public String runCommandWithOutput(String message, String[] command) {
		Process process = null;
		pb.command(command);
		StringBuilder output = new StringBuilder();

		if (!message.isEmpty()) {
			System.out.println(message);
		}

		try {
			process = pb.start();

			/* Stream reader of input stream connected to stdout of the process */
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			/* Capture all process output */
			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + System.lineSeparator());
			}

			process.waitFor(); // wait for process to complete

		}
		catch (IOException ex) {
			System.out.println("[Error] Failed to execute command " + Arrays.toString(command));
			ex.printStackTrace();
		}
		catch (InterruptedException ex) {
			System.out.println("[Error] Thread was interrupted while waiting for process to finish.");
			ex.printStackTrace();
		}
		finally {
			try {
				out.flush(); // flush out any remaining output
				process.destroy();
			}
			catch (IOException ex) {
				System.out.println("[Error] Failed to flush standard output stream");
				ex.printStackTrace();
			}
		}

		return output.toString();
	}

	public int runCommand(String message, String[] command) {
		Process process = null;
		pb.command(command);

		if (!message.isEmpty()) {
			System.out.println(message);
		}

		try {
			File outputFile = File.createTempFile("output", ".tmp");
			outputFile.deleteOnExit();
			pb.redirectOutput(outputFile);
			pb.redirectErrorStream();
			process = pb.start();
			process.waitFor(); // wait for process to complete

			Scanner input = new Scanner(outputFile);
			while(input.hasNextLine()) {
				System.out.println(input.nextLine());
			}
		}
		catch (IOException ex) {
			System.out.println("[Error] Failed to execute command " + Arrays.toString(command));
			ex.printStackTrace();
		}
		catch (InterruptedException ex) {
			System.out.println("[Error] Thread was interrupted while waiting for process to finish.");
			ex.printStackTrace();
		}
		finally {
			try {
				out.flush(); // flush out any remaining output
				process.destroy();
			}
			catch (IOException ex) {
				System.out.println("[Error] Failed to flush standard output stream");
				ex.printStackTrace();
			}
		}

		return process.exitValue();
	}

}
