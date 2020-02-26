package org.uqbar.sGit.views.actions;

import java.io.File;
import java.util.function.Consumer;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.uqbar.sGit.utils.FileLocator;
import org.uqbar.sGit.views.SGitView;

public class PersonIdentConsumerAction extends GitAction {

	private Consumer<PersonIdent> consumer;

	public PersonIdentConsumerAction(SGitView view) {
		super(view);
		this.setImageDescriptor(FileLocator.getImageDescriptor("refresh", this));
	}

	public void setPersonIndentConsumer(Consumer<PersonIdent> consumer) {
		this.consumer = consumer;
	}

	public Consumer<PersonIdent> getConsumer() {
		return this.consumer;
	}

	@Override
	public void run() {
		if (view.isAlreadyInitialized() && this.consumer != null && this.view.getProject() != null) {

			try {
				String uri = this.view.getProject().getLocation().toOSString();
				this.git = Git.wrap(new FileRepositoryBuilder().setGitDir(new File(uri + "/.git")).build());
				Repository repository = git.getRepository();
				String treeName = null;
				Iterable<RevCommit> commits = null;

				treeName = repository.getFullBranch();

				if (treeName != null || commits != null) {
					commits = git.log().add(repository.resolve(treeName)).call();
					for (RevCommit commit : commits) {
						this.consumer.accept(commit.getAuthorIdent());
					}
				}

			}

			catch (Exception e) {
				// need a way to show errors.
			}

		}
	}

}