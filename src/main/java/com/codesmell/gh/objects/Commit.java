package com.codesmell.gh.objects;

import java.util.Date;

public class Commit {
	private String sha; // The unique hash for this commit
	private Date date; // The date that the code was committed to the repo

	public Commit (String sha, Date date) {
		this.sha = sha;
		this.date = date;
	}

	public String getSha() {
		return sha;
	}

	public Date getDate() {
		return date;
	}
}
