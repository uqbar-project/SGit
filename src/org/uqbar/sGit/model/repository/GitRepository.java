package org.uqbar.sGit.model.repository;

import static org.uqbar.sGit.model.file.GitStatus.*;
import static org.uqbar.sGit.model.repository.credentials.GitCredentials.NO_CREDENTIALS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.uqbar.sGit.model.file.GitFile;
import org.uqbar.sGit.model.repository.credentials.GitCredentials;

public class GitRepository {

	private Git git;
	private GitCredentials credentials;
	
	public static GitRepository getRepository(String workspacePath, String projectName, GitCredentials credentials) {
		return new GitRepository(workspacePath + "/git/" + projectName, credentials);
	}
	
	public static GitRepository getRepository(String workspacePath, String projectName) {
		return new GitRepository(workspacePath + "/git/" + projectName);
	}

	public GitRepository() {
		this(NO_CREDENTIALS);
	}
	
	public GitRepository(String directoryPath) {
		try {
			git = Git.wrap(new FileRepositoryBuilder().setGitDir(new File(directoryPath + "/.git")).build());
		}

		catch (IOException e) {
			// TODO: Needs a Validation.
			e.printStackTrace();
		}
	}

	public GitRepository(String directoryPath, GitCredentials credentials) {
		this(directoryPath);
		this.credentials = credentials;
	}
	
	public GitRepository(String directoryPath, String username, String password) {
		this(directoryPath, new GitCredentials(username, password));
	}
	
	public GitRepository(GitCredentials credentials) {
		this.credentials = credentials;
	}
	
	public GitRepository(String username, String password) {
		this(new GitCredentials(username, password));
	}
	
	private void runSGitAction(SGitRunnable action) {
		try {
			action.run();
		}

		catch (Exception e) {
			// TODO: Needs a Validation.
			e.printStackTrace();
		}
	}
	
	/**
	 *	Returns a GIT staged files list.
	 */
	public List<GitFile> getStagedFiles() {
		ArrayList<GitFile> stagedChanges = new ArrayList<GitFile>();

		try {
			Status status = git.status().call();
			status.getAdded().stream().forEach(file -> stagedChanges.add(new GitFile(ADDED, file)));
			status.getChanged().stream().forEach(file -> stagedChanges.add(new GitFile(CHANGED, file)));
			status.getRemoved().stream().forEach(file -> stagedChanges.add(new GitFile(REMOVED, file)));
		}

		catch (NoWorkTreeException | GitAPIException e) {
			// TODO: Needs a Validation.
			e.printStackTrace();
		}

		return stagedChanges;
	}

	/**
	 *	Returns a GIT unstaged files list.
	 */
	public List<GitFile> getUnstagedFiles() {
		ArrayList<GitFile> unstagedChanges = new ArrayList<GitFile>();

		try {
			Status status = git.status().call();
			status.getMissing().stream().forEach(file -> unstagedChanges.add(new GitFile(MISSING, file)));
			status.getModified().stream().forEach(file -> unstagedChanges.add(new GitFile(MODIFIED, file)));
			status.getUntracked().stream().forEach(file -> unstagedChanges.add(new GitFile(UNTRACKED, file)));
		}

		catch (NoWorkTreeException | GitAPIException e) {
			// TODO: Needs a Validation.
			e.printStackTrace();
		}

		return unstagedChanges;
	}

	/**
	 *	Returns the last Commit. 
	 */
	private RevCommit getLastCommit() {
		RevCommit commit = null;
		try {
			final RevWalk revWalk = new RevWalk(git.getRepository());
			final ObjectId branchID = git.getRepository().getRefDatabase().getRef("HEAD").getObjectId();
			commit = revWalk.parseCommit(branchID);
			revWalk.close();
		}

		catch (IOException e) {
			// TODO: Needs a Validation.
			e.printStackTrace();
		}
		return commit;
	}
	
	/**
	 * returns the set of authors identification for this repository.
	 */
	public Set<PersonIdent> getAuthors() {
		Repository repository = git.getRepository();
		String treeName = null;
		Iterable<RevCommit> commits = null;
		HashSet<PersonIdent> authors = new HashSet<PersonIdent>();
		
		try {
			treeName = repository.getFullBranch();
			commits = git.log().add(repository.resolve(treeName)).call();
			for (RevCommit commit : commits) {
				authors.add(commit.getAuthorIdent());
			}
			
		} 
		
		catch (RevisionSyntaxException | GitAPIException | IOException e) {
			// TODO: Needs a Validation.
			e.printStackTrace();
		}
		
		return authors;
				
	}

	/**
	 *	Returns the last Author identification. 
	 */
	public PersonIdent getLastAuthor() {
		return this.getLastCommit().getAuthorIdent();
	}
	
	/**
	 *	Returns the last committer identification. 
	 */
	public PersonIdent getLastCommitter() {
		return this.getLastCommit().getCommitterIdent();
	}
	
	/**
	 * Get reference name.
	 * 
	 * @param reference
	 */
	private String getReferenceName(Ref reference) {
		return Arrays.asList(reference.getName().split("/")).stream().reduce((f, s) -> s).get();
	}
	
	/**
	 * Returns the repository name.
	 * 
	 * @param remote the remote name of project repository.
	 */
	public String getRepositoryName(String remote) {
		return Arrays.asList(remote.split("/")).stream().reduce((f, s) -> s).get().split("\\.")[0];
	}
	
	/**
	 * Returns the current branch name for this repository.
	 */
	public String getCurrentBranch() {
		String branch = "master";
		try {
			branch = git.getRepository().getBranch();
		}

		catch (IOException e) {
			// TODO: Needs a Validation.
			e.printStackTrace();
		}

		return branch;
	}
	
	/**
	 * Returns a staged GIT file with a file path and status.
	 * 
	 * @param filePath the path of the file to be getter.
	 */
	private GitFile getStagedFile(String filePath) {
		return this.getStagedFiles().stream().filter(file -> file.getFilePath().equals(filePath)).findFirst().get();
	}

	/**
	 * Returns a unstaged GIT file with a file path and status.
	 * 
	 * @param filePath the path of the file to be getter.
	 */
	private GitFile getUnstagedFile(String filePath) {
		return this.getUnstagedFiles().stream().filter(file -> file.getFilePath().equals(filePath)).findFirst().get();
	}
	
	/**
	 * Performs the GIT add operation.
	 * 
	 * @param filePath the path of the file to be added from index.
	 */
	public void addFileToStaging(String filePath) {
		this.runSGitAction(() -> {
			switch (this.getUnstagedFile(filePath).getStatus()) {

			case MISSING:
				git.rm()
				.addFilepattern(this.getUnstagedFile(filePath).getFilePath())
				.setCached(true)
				.call();
				break;

			default:
				git.add()
				.addFilepattern(this.getUnstagedFile(filePath).getFilePath())
				.call();
				break;
			}
		});
	}

	/**
	 * Performs the GIT reset operation.
	 * 
	 * @param filePath the path of the file to be removed from index.
	 */
	public void removeFileFromStaging(String filePath) {
		this.runSGitAction(() -> {
			git.reset()
			.setRef(Constants.HEAD)
			.addPath(this.getStagedFile(filePath).getFilePath())
			.call();
		});
	}
	
	/**
	 * Returns a array of all branches on this repository. 
	 */
	public String[] getBranches(String remote) throws InvalidRemoteException, TransportException, GitAPIException {
		return Git.lsRemoteRepository()
				.setRemote(remote).setHeads(true)
				.setCredentialsProvider(this.getCredentialProvider())
				.call()
				.stream()
				.map(this::getReferenceName)
				.toArray(String[]::new);
	}
	
	/**
	 * Returns the origin of the repository. 
	 */
	public String getOrigin() {
		return git.getRepository().getConfig().getString("remote", "origin", "url");
	}

	/**
	 * Performs the GIT commit operation.
	 */
	public void commit(String message, String author, String authorEmail, String committer, String committerEmail) {
		this.runSGitAction(() -> {
			git.commit()
			.setMessage(message)
			.setAuthor(author, authorEmail)
			.setCommitter(committer, committerEmail)
			.call();
		});
	}

	/**
	 * Performs the GIT push operation.
	 */
	public void push() {
		this.runSGitAction(() -> {
			git.push()
			.setCredentialsProvider(this.getCredentialProvider())
			.call();
		});
	}
	
	/**
	 * Performs the GIT pull operation.
	 */
	public void pull() {
		this.runSGitAction(() -> {
			git.pull()
			.setCredentialsProvider(this.getCredentialProvider())
			.call();
		});
	}
	
	/**
	 * Performs the GIT pull operation.
	 * 
	 * @param remote the remote name of project repository.
	 * @param branch the branch name of repository to clone.
	 */
	public void pull(String remote, String branch) {
		this.runSGitAction(() -> {
			git.pull()
			.setRemote(remote)
			.setRemoteBranchName(branch)
			.setCredentialsProvider(this.getCredentialProvider())
			.call();
		});
	}
	
	/**
	 * Performs the GIT clone operation.
	 * 
	 * @param directory the directory to clone the remote repository.
	 * @param remote the remote name of project repository.
	 * @param branch the branch name of repository to clone.
	 */
	public void cloneRepository(String directory, String remote, String branch) {
		this.runSGitAction(() -> {
		Git.cloneRepository()
			.setDirectory(new File(directory + "/" + this.getRepositoryName(remote)))
			.setURI(remote)
			.setCredentialsProvider(this.getCredentialProvider())
			.setBranch(branch)
			.setCloneAllBranches(true)
			.setCloneSubmodules(true)
			.call();
		});
	}
	
	/**
	 * Returns the necessary credentials to be able to exchange data with the remote
	 * repository.
	 */
	public GitCredentials getCredentials() {
		return this.credentials;
	}

	/**
	 * Returns a JGIT credential provider.
	 */
	public UsernamePasswordCredentialsProvider getCredentialProvider() {
		final String username = this.credentials.getUsername();
		final String password = this.getCredentials().getPassword();
		return new UsernamePasswordCredentialsProvider(username, password);
	}

}
