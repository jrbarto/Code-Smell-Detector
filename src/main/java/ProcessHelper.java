package main.java;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;


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

	public int runCommand(String message, String[] command) {
		Process process = null;
		pb.command(command);

		if (!message.isEmpty()) {
			System.out.println(message);
		}

		try {
			process = pb.start();
			process.getOutputStream().close(); // close stdin

		}
		catch (IOException ex) {
			System.out.println("[Error] Failed to execute command " + command.toString());
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
