package com.codesmell.gh.objects;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GHFile {
	String path;
	String contents;
	String patchHeader;

	public GHFile(String path, String contents, String patchHeader) {
		this.path = path;
		this.contents = contents;
		this.patchHeader = patchHeader;
	}

	public File getTempFile() throws IOException {
		String property = "java.io.tmpdir";

		String tempDir = System.getProperty(property);

		File dir = new File(tempDir);

		File tempFile = File.createTempFile(path, ".tmp", dir);
		tempFile.deleteOnExit();
		FileWriter fileWriter = new FileWriter(tempFile, true);
		System.out.println(tempFile.getName());
		BufferedWriter bw = new BufferedWriter(fileWriter);
		bw.write(contents);
		bw.close();

		return tempFile;
	}
}
