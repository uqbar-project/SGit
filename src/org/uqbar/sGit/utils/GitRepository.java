package org.uqbar.sGit.utils;

import static org.uqbar.sGit.utils.GitStatus.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.AbortedByHookException;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.RefNotAdvertisedException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.uqbar.sGit.exceptions.MergeConflictsException;
import org.uqbar.sGit.exceptions.NotAuthorizedException;

public class GitRepository {

	private Git git;
	private GitCredentials credentials;
	
	public GitRepository() {
		this(new GitCredentials());
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
	
	public GitRepository(GitCredentials credentials) {
		this.credentials = credentials;
	}

	public GitRepository(String directoryPath, GitCredentials credentials) {
		this(directoryPath);
		this.credentials = credentials;
	}
	
	public GitRepository(String workspacePath, String projectName, GitCredentials credentials) {
		this(workspacePath + "/git/" + projectName, credentials);
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
			status.getConflicting().stream().forEach(file -> unstagedChanges.add(new GitFile(MODIFIED, file)));
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
	 * @throws GitAPIException 
	 * @throws NoFilepatternException 
	 */
	public void addFileToStaging(String filePath) throws NoFilepatternException, GitAPIException {
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
	}

	/**
	 * Performs the GIT reset operation.
	 * 
	 * @param filePath the path of the file to be removed from index.
	 * @throws GitAPIException 
	 * @throws CheckoutConflictException 
	 */
	public void removeFileFromStaging(String filePath) throws CheckoutConflictException, GitAPIException {
			git.reset()
			.setRef(Constants.HEAD)
			.addPath(this.getStagedFile(filePath).getFilePath())
			.call();
	}
	
	/**
	 * Returns a array of all branches on this repository. 
	 */
	public String[] getBranches(String remote) throws InvalidRemoteException, TransportException, GitAPIException {
		LsRemoteCommand lsRemoteRepository = Git.lsRemoteRepository();
		lsRemoteRepository.setRemote(remote).setHeads(true);
		this.setCredentialsProvider(lsRemoteRepository);
		return lsRemoteRepository.call().stream().map(this::getReferenceName).toArray(String[]::new);
	}
	
	/**
	 * Returns the origin of the repository. 
	 */
	public String getOrigin() {
		return git.getRepository().getConfig().getString("remote", "origin", "url");
	}

	/**
	 * Performs the GIT commit operation.
	 * @throws GitAPIException 
	 * @throws AbortedByHookException 
	 * @throws WrongRepositoryStateException 
	 * @throws ConcurrentRefUpdateException 
	 * @throws UnmergedPathsException 
	 * @throws NoMessageException 
	 * @throws NoHeadException 
	 */
	public void commit(String message, String author, String authorEmail, String committer, String committerEmail) throws NoHeadException, NoMessageException, UnmergedPathsException, ConcurrentRefUpdateException, WrongRepositoryStateException, AbortedByHookException, GitAPIException {
		CommitCommand commit = git.commit();
		commit.setMessage(message);
		commit.setAuthor(author, authorEmail);
		commit.setCommitter(committer, committerEmail);
		commit.call();
	}
	
	/**
	 * Performs the GIT commit operation.
	 * @throws GitAPIException 
	 * @throws AbortedByHookException 
	 * @throws WrongRepositoryStateException 
	 * @throws ConcurrentRefUpdateException 
	 * @throws UnmergedPathsException 
	 * @throws NoMessageException 
	 * @throws NoHeadException 
	 */
	public void commit(String message, String author, String authorEmail) throws NoHeadException, NoMessageException, UnmergedPathsException, ConcurrentRefUpdateException, WrongRepositoryStateException, AbortedByHookException, GitAPIException {
		this.commit(message, author, authorEmail, author, authorEmail);
	}

	/**
	 * Performs the GIT push operation.
	 * @throws NoHeadException 
	 * @throws RefNotAdvertisedException 
	 * @throws RefNotFoundException 
	 * @throws CanceledException 
	 * @throws DetachedHeadException 
	 * @throws InvalidConfigurationException 
	 * @throws WrongRepositoryStateException 
	 * @throws GitAPIException 
	 * @throws TransportException 
	 * @throws InvalidRemoteException 
	 */
	public void push() throws WrongRepositoryStateException, InvalidConfigurationException, DetachedHeadException, InvalidRemoteException, CanceledException, RefNotFoundException, RefNotAdvertisedException, NoHeadException, TransportException, GitAPIException, MergeConflictsException {
		PullCommand pull = git.pull();
		this.setCredentialsProvider(pull);
		pull.call();
		
		RepositoryState repositoryState = git.getRepository().getRepositoryState();
		if (repositoryState == RepositoryState.MERGING) {
			throw new MergeConflictsException();
		}

		PushCommand push = git.push();
		this.setCredentialsProvider(push);
		push.call();

	}

	/**
	 * Performs the GIT pull operation.
	 * @throws GitAPIException 
	 * @throws TransportException 
	 * @throws NoHeadException 
	 * @throws RefNotAdvertisedException 
	 * @throws RefNotFoundException 
	 * @throws CanceledException 
	 * @throws InvalidRemoteException 
	 * @throws DetachedHeadException 
	 * @throws InvalidConfigurationException 
	 * @throws WrongRepositoryStateException 
	 */
	public void pull() throws WrongRepositoryStateException, InvalidConfigurationException, DetachedHeadException, InvalidRemoteException, CanceledException, RefNotFoundException, RefNotAdvertisedException, NoHeadException, TransportException, GitAPIException, MergeConflictsException {
		PullCommand pull = git.pull();
		this.setCredentialsProvider(pull);
		pull.call();
		
		RepositoryState repositoryState = git.getRepository().getRepositoryState();
		if (repositoryState == RepositoryState.MERGING) {
			throw new MergeConflictsException();
		}
		
	}
	
	/**
	 * Performs the GIT pull operation.
	 * 
	 * @param remote the remote name of project repository.
	 * @param branch the branch name of repository to clone.
	 * @throws GitAPIException 
	 * @throws TransportException 
	 * @throws NoHeadException 
	 * @throws RefNotAdvertisedException 
	 * @throws RefNotFoundException 
	 * @throws CanceledException 
	 * @throws InvalidRemoteException 
	 * @throws DetachedHeadException 
	 * @throws InvalidConfigurationException 
	 * @throws WrongRepositoryStateException 
	 */
	public void pull(String remote, String branch) throws WrongRepositoryStateException, InvalidConfigurationException, DetachedHeadException, InvalidRemoteException, CanceledException, RefNotFoundException, RefNotAdvertisedException, NoHeadException, TransportException, GitAPIException {
		PullCommand pull = git.pull();
		this.setCredentialsProvider(pull);
		pull.setRemote(remote);
		pull.setRemoteBranchName(branch);
		pull.call();
	}
	
	/**
	 * Performs the GIT clone operation.
	 * 
	 * @param directory the directory to clone the remote repository.
	 * @param remote the remote name of project repository.
	 * @param branch the branch name of repository to clone.
	 * @throws GitAPIException 
	 * @throws TransportException 
	 * @throws InvalidRemoteException 
	 */
	public void cloneRepository(String directory, String remote, String branch) throws InvalidRemoteException, TransportException, GitAPIException {
		CloneCommand clone = Git.cloneRepository();
		this.setCredentialsProvider(clone);
		clone.setDirectory(new File(directory + "/" + this.getRepositoryName(remote)));
		clone.setURI(remote);
		clone.setBranch(branch);
		clone.setCloneAllBranches(true);
		clone.setCloneSubmodules(true);
		clone.call();
	}
	
	/**
	 * Returns the necessary credentials to be able to exchange data with the remote
	 * repository.
	 */
	public GitCredentials getCredentials() {
		return this.credentials;
	}
	
	@SuppressWarnings("rawtypes")
	private void setCredentialsProvider(TransportCommand command) {
		if (!this.credentials.isEmpty()) {
			final String username = this.getCredentials().getUsername();
			final String password = this.getCredentials().getPassword();
			command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
		}
		
		else {
			CrendentialsDialog dialog = new CrendentialsDialog(null);
			dialog.open();
			GitCredentials credentials = dialog.getCredentials();
			
			if(!credentials.isEmpty()){
				command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(credentials.getUsername(), credentials.getPassword()));
			}
			
			else {
				throw new NotAuthorizedException();					
			}
		}
	}

}