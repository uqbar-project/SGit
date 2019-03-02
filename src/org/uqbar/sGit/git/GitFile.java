package org.uqbar.sGit.git;

public class GitFile {

	final private GitStatus status;
	final private String filePath;

	public GitFile(GitStatus status, String filePath) {
		this.status = status;
		this.filePath = filePath;
	}

	public GitStatus getStatus() {
		return status;
	}

	public String getFilePath() {
		return filePath;
	}

}
