package com.codesmell.gh.objects;

import java.util.ArrayList;
import java.util.List;

public class PullRequest {
	private int number;
	private List<Commit> commits;
	private Commit latestCommit;

	public PullRequest(int number) {
		this.number = number;
		commits = new ArrayList<>();
	}

	public void setCommits(List<Commit> commits) {
		this.commits = commits;
	}

	public void addCommit(Commit commit) {
		this.commits.add(commit);
	}

	public List<Commit> getCommits() {
		return commits;
	}

	public int getNumber() {
		return number;
	}

	public void setLatestCommit(Commit latestCommit) {
		this.latestCommit = latestCommit;
	}

	public Commit getLatestCommit() {
		return latestCommit;
	}
}
