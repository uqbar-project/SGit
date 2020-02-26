package org.uqbar.sGit.views.actions;

import java.io.File;
import java.util.function.Consumer;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.uqbar.sGit.exceptions.SgitException;
import org.uqbar.sGit.utils.FileLocator;
import org.uqbar.sGit.utils.git.GitFile;
import org.uqbar.sGit.views.SGitView;

public class UnstagingFileConsumerAction extends GitAction {

	private Consumer<GitFile> consumer;

	public UnstagingFileConsumerAction(SGitView view) {
		super(view);
		this.setImageDescriptor(FileLocator.getImageDescriptor("refresh", this));
	}

	public void setGitFileConsumer(Consumer<GitFile> consumer) {
		this.consumer = consumer;
	}

	public Consumer<GitFile> getConsumer() {
		return this.consumer;
	}

	@Override
	public void run() {
		if (view.isAlreadyInitialized() && this.consumer != null && this.view.getProject() != null) {

			try {
				String uri = this.view.getProject().getLocation().toOSString();
				this.git = Git.wrap(new FileRepositoryBuilder().setGitDir(new File(uri + "/.git")).build());
				this.getUnstagedFiles().stream().forEach(this.consumer::accept);
			}

			catch (Exception exception) {
				this.exceptionHandler.accept(new SgitException(exception.getMessage()));
			}

		}
	}

}