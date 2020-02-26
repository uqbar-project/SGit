package org.uqbar.sGit.views.actions;

import java.io.File;

import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.uqbar.sGit.exceptions.CommitActionFailedException;
import org.uqbar.sGit.utils.FileLocator;
import org.uqbar.sGit.views.SGitView;

public class CommitAction extends GitAction {

	private String message = "";
	private String author = "";
	private String authorEmail = "";
	private String committer = "";
	private String committerEmail = "";

	public CommitAction(SGitView view) {
		super(view);
		this.setImageDescriptor(FileLocator.getImageDescriptor("commit", this));
	}

	public void setCommitDetails(String message, String author, String authorEmail, String committer,
			String committerEmail) {
		this.message = message;
		this.author = author;
		this.authorEmail = authorEmail;
		this.committer = committer;
		this.committerEmail = committerEmail;
	}

	public Boolean hasCommitDetails() {
		return !this.message.isEmpty() && !this.author.isEmpty() && !this.authorEmail.isEmpty()
				&& !this.committer.isEmpty() && !this.committerEmail.isEmpty();
	}

	@Override
	public void run() {
		if (view.isAlreadyInitialized() && this.hasCommitDetails() && this.view.getProject() != null) {

			try {
				String uri = this.view.getProject().getLocation().toOSString();
				this.git = Git.wrap(new FileRepositoryBuilder().setGitDir(new File(uri + "/.git")).build());
				CommitCommand commit = this.git.commit();
				commit.setMessage(message);
				commit.setAuthor(author, authorEmail);
				commit.setCommitter(committer, committerEmail);
				commit.call();
			}

			catch (Exception exception) {
				this.exceptionHandler.accept(new CommitActionFailedException(exception.getMessage()));
			}

		}
	}

}