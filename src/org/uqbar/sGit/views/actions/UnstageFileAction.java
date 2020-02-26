package org.uqbar.sGit.views.actions;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.uqbar.sGit.exceptions.UnstageFileActionFailedException;
import org.uqbar.sGit.utils.FileLocator;
import org.uqbar.sGit.views.SGitView;

public class UnstageFileAction extends GitAction {

	private String filePath = "";

	public UnstageFileAction(SGitView view) {
		super(view);
		this.setImageDescriptor(FileLocator.getImageDescriptor("unstage", this));
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Boolean hasFilePath() {
		return !this.filePath.isEmpty();
	}

	@Override
	public void run() {
		if (view.isAlreadyInitialized() && this.hasFilePath() && this.view.getProject() != null) {

			try {
				String uri = this.view.getProject().getLocation().toOSString();
				this.git = Git.wrap(new FileRepositoryBuilder().setGitDir(new File(uri + "/.git")).build());
				this.git.reset().setRef(Constants.HEAD).addPath(this.getStagedFile(filePath).getFilePath()).call();
			}

			catch (Exception exception) {
				this.exceptionHandler.accept(new UnstageFileActionFailedException(exception.getMessage()));
			}

		}
	}

}