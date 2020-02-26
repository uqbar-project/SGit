package org.uqbar.sGit.views.actions;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.uqbar.sGit.exceptions.StageFileActionFailedException;
import org.uqbar.sGit.utils.FileLocator;
import org.uqbar.sGit.views.SGitView;

public class StageFileAction extends GitAction {

	private String filePath = "";

	public StageFileAction(SGitView view) {
		super(view);
		this.setImageDescriptor(FileLocator.getImageDescriptor("add", this));
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

				switch (this.getUnstagedFile(filePath).getStatus()) {

				case MISSING:
					this.git.rm().addFilepattern(this.getUnstagedFile(filePath).getFilePath()).setCached(true).call();
					break;

				default:
					this.git.add().addFilepattern(this.getUnstagedFile(filePath).getFilePath()).call();
					break;
				}
			}

			catch (Exception exception) {
				this.exceptionHandler.accept(new StageFileActionFailedException(exception.getMessage()));
			}

		}
	}

}