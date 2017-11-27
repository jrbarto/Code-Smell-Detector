package com.codesmell.gh.objects;

public class Commit {
	private String sha;

	public Commit (String sha) {
		this.sha = sha;
	}

	public String getSha() {
		return sha;
	}
}
