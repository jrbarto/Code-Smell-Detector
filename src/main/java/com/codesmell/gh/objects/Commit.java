package com.codesmell.gh.objects;

import java.util.Date;

/**
 * Represents a commit on a Github repository.
 *
 */
public class Commit {
    private String sha; // The unique hash for this commit

    public Commit (String sha) {
        this.sha = sha;
    }

    public String getSha() {
        return sha;
    }
}