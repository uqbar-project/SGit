package org.uqbar.sGit.model.file;

/**
 * 
 * @author Lucas Alan Silvestri
 *
 */
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

	public String getStatusImageName() {

		switch (this.status) {

		case ADDED:
			return "staged_added";

		case CHANGED:
			return "dirty";

		case REMOVED:
			return "staged_removed";

		case MISSING:
			return "untracked";

		case MODIFIED:
			return "staged";

		case UNTRACKED:
			return "untracked";

		}
		return "untracked";
	}

}