package org.uqbar.sGit.views.actions;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.uqbar.sGit.exceptions.MergeConflictsException;
import org.uqbar.sGit.exceptions.PullActionFailedException;
import org.uqbar.sGit.utils.FileLocator;
import org.uqbar.sGit.views.View;

public class PullAction extends GitAction {

	public PullAction(View view) {
		super(view);
		this.setImageDescriptor(FileLocator.getImageDescriptor("pull", this));
	}

	@Override
	public void run() {
		if (view.isAlreadyInitialized() && this.view.getProject() != null) {

			try {
				String uri = this.view.getProject().getLocation().toOSString();
				this.git = Git.wrap(new FileRepositoryBuilder().setGitDir(new File(uri + "/.git")).build());
				PullCommand pull = this.git.pull();
				Repository repository = this.git.getRepository();
				this.setCredentialsProvider(pull);
				pull.call();
				this.validateMerginState(repository);
			}

			catch (MergeConflictsException exception) {
				this.exceptionHandler.accept(exception);
			}

			catch (Exception exception) {
				this.exceptionHandler.accept(new PullActionFailedException(exception.getMessage()));
			}

		}
	}

}