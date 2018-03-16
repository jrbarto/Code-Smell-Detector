package com.codesmell.gh.objects;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A file that exists in a Github repository.
 *
 */
public class GHFile {
    private String path; // The relative path from the root of the repository
    private String contents; // The raw file contents
    private ArrayList<String> diffLines; // The diff of this file split into a list of lines

    /* Constructor for pull request files (a diff exists) */
    public GHFile(String path, String contents, String diff) {
        this.path = path;
        this.contents = contents;
        /* split diff into a list of lines */
        this.diffLines = new ArrayList<>(Arrays.asList(diff.split("\n")));
        this.diffLines.removeIf(s -> s.startsWith("-")); // remove all deleted lines from diff
    }

    /* Constructor for repo files (no diff) */
    public GHFile(String path, String contents) {
        this.path = path;
        this.contents = contents;
    }

    /**
     * Create a temporary file on the server with contents from the file in Github.
     *
     * @return A temporary file with the Github file contents.
     * @throws IOException
     */
    public File getTempFile() throws IOException {
        String property = "java.io.tmpdir";

        String tempDir = System.getProperty(property);

        File dir = new File(tempDir);

        File tempFile = File.createTempFile(path, ".tmp", dir);
        tempFile.deleteOnExit();
        FileWriter fileWriter = new FileWriter(tempFile, true);
        BufferedWriter bw = new BufferedWriter(fileWriter);
        bw.write(contents);
        bw.close();

        return tempFile;
    }

    /**
     * Calculate the position in the diff of the Github file from the line number of the full file.
     *
     * @param fileLine
     * @return
     */
    public int getDiffPosition(int fileLine) {
        int index = 0;

        while (index < diffLines.size() - 1) {
            String head = diffLines.get(index); // contains relative line positions
            String[] positions = (head.substring(head.indexOf("+") + 1, head.lastIndexOf("@@"))).split(",");
            int startLine = Integer.parseInt(positions[0].trim()); // beginning line of this change
            int changeLength = startLine; // line numbers in this change

            /* Single line changes do not include a length */
            if (positions.length > 1) {
                changeLength = Integer.parseInt(positions[1].trim());
            }

            /* Return position in div if it is within this range */
            if (fileLine >= startLine && fileLine <= (startLine + changeLength)) {
                return fileLine - startLine + 1;
            }

            index += changeLength;
        }

        return -1;
    }

    /**
     * Accessor for the path in the repository.
     *
     * @return Path to the file.
     */
    public String getPath() {
        return path;
    }
}