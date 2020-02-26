package org.uqbar.sGit.views.actions;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.uqbar.sGit.utils.FileLocator;
import org.uqbar.sGit.views.SGitView;

public class PullAction extends GitAction {

	public PullAction(SGitView view) {
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

			catch (Exception e) {
				// need a way to show errors.
			}

		}
	}

}