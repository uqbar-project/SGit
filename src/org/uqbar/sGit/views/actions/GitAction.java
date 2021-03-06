package org.uqbar.sGit.views.actions;

import static org.uqbar.sGit.utils.git.GitStatus.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jface.action.Action;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.uqbar.sGit.exceptions.MergeConflictsException;
import org.uqbar.sGit.exceptions.NotAuthorizedException;
import org.uqbar.sGit.exceptions.SgitException;
import org.uqbar.sGit.utils.git.GitFile;
import org.uqbar.sGit.utils.git.SecureStoredUserPasswordCredentials;
import org.uqbar.sGit.utils.git.UserPasswordCredentials;
import org.uqbar.sGit.views.CrendentialsDialog;
import org.uqbar.sGit.views.View;

public abstract class GitAction extends Action {

	protected final View view;
	protected Git git;
	protected Consumer<SgitException> exceptionHandler;

	public GitAction(View view) {
		this.view = view;
	}

	public List<GitFile> getStagedFiles() throws NoWorkTreeException, GitAPIException {
		ArrayList<GitFile> stagedChanges = new ArrayList<GitFile>();
		Status status = this.git.status().call();
		status.getAdded().stream().forEach(file -> stagedChanges.add(new GitFile(ADDED, file)));
		status.getChanged().stream().forEach(file -> stagedChanges.add(new GitFile(CHANGED, file)));
		status.getRemoved().stream().forEach(file -> stagedChanges.add(new GitFile(REMOVED, file)));
		return stagedChanges;
	}

	public List<GitFile> getUnstagedFiles() throws NoWorkTreeException, GitAPIException {
		ArrayList<GitFile> unstagedChanges = new ArrayList<GitFile>();
		Status status = this.git.status().call();
		status.getConflicting().stream().forEach(file -> unstagedChanges.add(new GitFile(MODIFIED, file)));
		status.getMissing().stream().forEach(file -> unstagedChanges.add(new GitFile(MISSING, file)));
		status.getModified().stream().forEach(file -> unstagedChanges.add(new GitFile(MODIFIED, file)));
		status.getUntracked().stream().forEach(file -> unstagedChanges.add(new GitFile(UNTRACKED, file)));
		return unstagedChanges;
	}

	protected GitFile getStagedFile(String filePath) throws NoWorkTreeException, GitAPIException {
		return this.getStagedFiles().stream().filter(file -> file.getFilePath().equals(filePath)).findFirst().get();
	}

	protected GitFile getUnstagedFile(String filePath) throws NoWorkTreeException, GitAPIException {
		return this.getUnstagedFiles().stream().filter(file -> file.getFilePath().equals(filePath)).findFirst().get();
	}

	@SuppressWarnings("rawtypes")
	private void setCredentialsProvider(UserPasswordCredentials credentials, TransportCommand command) {
		if (!credentials.isEmpty()) {
			String user = credentials.getUser();
			String password = credentials.getPassword();
			command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(user, password));
		}
	}

	@SuppressWarnings("rawtypes")
	private void setCredentialsProvider(UserPasswordCredentials credentials, List<TransportCommand> commandlist) {
		for (TransportCommand command : commandlist) {
			this.setCredentialsProvider(credentials, command);
		}
	}

	@SuppressWarnings("rawtypes")
	protected void setCredentialsProvider(TransportCommand... commands) {
		UserPasswordCredentials credentials;
		List<TransportCommand> commandlist = Arrays.asList(commands);
		credentials = new SecureStoredUserPasswordCredentials();

		if (!credentials.isEmpty()) {
			this.setCredentialsProvider(credentials, commandlist);
		}

		else {
			CrendentialsDialog dialog = new CrendentialsDialog(null);
			dialog.open();
			credentials = dialog.getCredentials();

			if (!credentials.isEmpty()) {
				this.setCredentialsProvider(credentials, commandlist);
			}

			else {
				throw new NotAuthorizedException();
			}
		}
	}

	protected void validateMerginState(Repository repository) throws MergeConflictsException {
		RepositoryState repositoryState = repository.getRepositoryState();

		if (repositoryState == RepositoryState.MERGING) {
			throw new MergeConflictsException();
		}
	}

	public void setExceptionHandler(Consumer<SgitException> handler) {
		this.exceptionHandler = handler;
	}

	@Override
	public abstract void run();

}