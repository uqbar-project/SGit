package org.uqbar.sGit.utils;

import static org.uqbar.sGit.utils.GitStatus.*;

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
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class GitRepository {

	private Git git;
	private GitCredentials credentials;

	public GitRepository() {
		this.credentials = new GitCredentials("", "");
	}

	public GitRepository(String directoryPath) {
		try {
			git = Git.wrap(new FileRepositoryBuilder().setGitDir(new File(directoryPath + "/.git")).build());
		}

		catch (IOException e) {
			e.printStackTrace();
		}
		this.credentials = new GitCredentials("", "");
	}
	
	public GitRepository(String username, String password) {
		this.credentials = new GitCredentials(username, password);
	}
	
	public GitRepository(String directoryPath, String username, String password) {
		try {
			git = Git.wrap(new FileRepositoryBuilder().setGitDir(new File(directoryPath + "/.git")).build());
		}

		catch (IOException e) {
			e.printStackTrace();
		}
		this.credentials = new GitCredentials(username, password);
	}
	
	
	private void runSGitAction(SGitRunnable action) {
		try {
			action.run();
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<GitFile> getStagedFiles() {
		ArrayList<GitFile> stagedChanges = new ArrayList<GitFile>();

		try {
			Status status = git.status().call();
			status.getAdded().stream().forEach(file -> stagedChanges.add(new GitFile(ADDED, file)));
			status.getChanged().stream().forEach(file -> stagedChanges.add(new GitFile(CHANGED, file)));
			status.getRemoved().stream().forEach(file -> stagedChanges.add(new GitFile(REMOVED, file)));
		}

		catch (NoWorkTreeException | GitAPIException e) {
			e.printStackTrace();
		}

		return stagedChanges;
	}

	public List<GitFile> getUnstagedFiles() {
		ArrayList<GitFile> unstagedChanges = new ArrayList<GitFile>();

		try {
			Status status = git.status().call();
			status.getMissing().stream().forEach(file -> unstagedChanges.add(new GitFile(MISSING, file)));
			status.getModified().stream().forEach(file -> unstagedChanges.add(new GitFile(MODIFIED, file)));
			status.getUntracked().stream().forEach(file -> unstagedChanges.add(new GitFile(UNTRACKED, file)));
		}

		catch (NoWorkTreeException | GitAPIException e) {
			e.printStackTrace();
		}

		return unstagedChanges;
	}

	private RevCommit getLastCommit() {
		RevCommit commit = null;
		try {
			final RevWalk revWalk = new RevWalk(git.getRepository());
			final ObjectId branchID = git.getRepository().getRefDatabase().getRef("HEAD").getObjectId();
			commit = revWalk.parseCommit(branchID);
			revWalk.close();
		}

		catch (IOException e) {
			e.printStackTrace();
		}
		return commit;
	}
	
	public Set<PersonIdent> getAuthors() {
		Repository repository = git.getRepository();
		String treeName = "refs/heads/master"; // tag or branch
		Iterable<RevCommit> commits = null;
		HashSet<PersonIdent> authors = new HashSet<PersonIdent>();
		
		try {
			commits = git.log().add(repository.resolve(treeName)).call();
			for (RevCommit commit : commits) {
				authors.add(commit.getAuthorIdent());
			}
			
		} 
		
		catch (RevisionSyntaxException | GitAPIException | IOException e) {
			e.printStackTrace();
		}
		
		return authors;
				
	}

	public PersonIdent getLastAuthor() {
		return this.getLastCommit().getAuthorIdent();
	}
	
	public PersonIdent getLastCommitter() {
		return this.getLastCommit().getCommitterIdent();
	}
	
	private String getReferenceName(Ref reference) {
		return Arrays.asList(reference.getName().split("/")).stream().reduce((f, s) -> s).get();
	}
	
	public String getRepositoryName(String remote) {
		return Arrays.asList(remote.split("/")).stream().reduce((f, s) -> s).get().split("\\.")[0];
	}
	
	private GitFile getStagedFile(String filePath) {
		return this.getStagedFiles().stream().filter(file -> file.getFilePath().equals(filePath)).findFirst().get();
	}

	private GitFile getUnstagedFile(String filePath) {
		return this.getUnstagedFiles().stream().filter(file -> file.getFilePath().equals(filePath)).findFirst().get();
	}
	
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

	public void removeFileFromStaging(String filePath) {
		this.runSGitAction(() -> {
			git.reset()
			.setRef(Constants.HEAD)
			.addPath(this.getStagedFile(filePath).getFilePath())
			.call();
		});
	}
	
	public String[] getBranches(String remote) throws InvalidRemoteException, TransportException, GitAPIException {
		return Git.lsRemoteRepository()
				.setRemote(remote).setHeads(true)
				.setCredentialsProvider(this.credentials.getCredentialProvider())
				.call()
				.stream()
				.map(this::getReferenceName)
				.toArray(String[]::new);
	}
	
	public String getCurrentBranch() {
		String branch = "master";
		try {
			branch = git.getRepository().getBranch();
		}

		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return branch;
	}

	public String getOrigin() {
		return git.getRepository().getConfig().getString("remote", "origin", "url");
	}

	public void commit(String message, String author, String authorEmail, String committer, String committerEmail) {
		this.runSGitAction(() -> {
			git.commit()
			.setMessage(message)
			.setAuthor(author, authorEmail)
			.setCommitter(committer, committerEmail)
			.call();
		});
	}

	public void push() {
		this.runSGitAction(() -> {
			git.push()
			.setCredentialsProvider(this.credentials.getCredentialProvider())
			.call();
		});
	}
	
	public void pull() {
		this.runSGitAction(() -> {
			git.pull()
			.setCredentialsProvider(this.credentials.getCredentialProvider())
			.call();
		});
	}
	
	public void pull(String remote, String branch) {
		this.runSGitAction(() -> {
			git.pull()
			.setRemote(remote)
			.setRemoteBranchName(branch)
			.setCredentialsProvider(this.credentials.getCredentialProvider())
			.call();
		});
	}
	
	
	public void cloneRepository(String directory, String remote, String branch) {
		this.runSGitAction(() -> {
		Git.cloneRepository()
			.setDirectory(new File(directory + "/" + this.getRepositoryName(remote)))
			.setURI(remote)
			.setCredentialsProvider(this.credentials.getCredentialProvider())
			.setBranch(branch)
			.setCloneAllBranches(true)
			.setCloneSubmodules(true)
			.call();
		});
	}


}
