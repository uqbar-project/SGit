package org.uqbar.sGit.views.actions;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.uqbar.sGit.exceptions.PushActionFailedException;
import org.uqbar.sGit.utils.FileLocator;
import org.uqbar.sGit.views.SGitView;

public class PushAction extends GitAction {

	public PushAction(SGitView view) {
		super(view);
		this.setImageDescriptor(FileLocator.getImageDescriptor("push", this));
	}

	@Override
	public void run() {
		if (view.isAlreadyInitialized() && this.view.getProject() != null) {

			try {
				String uri = this.view.getProject().getLocation().toOSString();
				this.git = Git.wrap(new FileRepositoryBuilder().setGitDir(new File(uri + "/.git")).build());
				PushCommand push = this.git.push();
				this.setCredentialsProvider(push);
			}

			catch (Exception exception) {
				this.exceptionHandler.accept(new PushActionFailedException(exception.getMessage()));
			}

		}
	}

}