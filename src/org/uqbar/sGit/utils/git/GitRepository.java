package org.uqbar.sGit.utils.git;

import static org.uqbar.sGit.utils.git.GitStatus.ADDED;
import static org.uqbar.sGit.utils.git.GitStatus.CHANGED;
import static org.uqbar.sGit.utils.git.GitStatus.MISSING;
import static org.uqbar.sGit.utils.git.GitStatus.MODIFIED;
import static org.uqbar.sGit.utils.git.GitStatus.REMOVED;
import static org.uqbar.sGit.utils.git.GitStatus.UNTRACKED;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

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
import org.uqbar.sGit.utils.CrendentialsDialog;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class GitRepository {

	private Git git;
	
	public GitRepository() {
		
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
	
	public GitRepository(String workspacePath, String projectName) {
		this(workspacePath + "/git/" + projectName);
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
	 * @throws IOException 
	 */
	private RevCommit getLastCommit() throws IOException {
		final RevWalk revWalk = new RevWalk(git.getRepository());
		final ObjectId branchID = git.getRepository().getRefDatabase().getRef("HEAD").getObjectId();
		final RevCommit commit = revWalk.parseCommit(branchID);
		revWalk.close();
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

			if(treeName != null || commits != null){
				commits = git.log().add(repository.resolve(treeName)).call();
				for (RevCommit commit : commits) {
					authors.add(commit.getAuthorIdent());
				}
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
	 * @throws IOException 
	 */
	public PersonIdent getLastAuthor() throws IOException {
		return this.getLastCommit().getAuthorIdent();
	}
	
	/**
	 *	Returns the last committer identification. 
	 * @throws IOException 
	 */
	public PersonIdent getLastCommitter() throws IOException {
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
	 * Returns a array of all branches on this repository. 
	 */
	public String[] getBranches(String remote, UserPasswordCredentials credentials) throws InvalidRemoteException, TransportException, GitAPIException {
		LsRemoteCommand lsRemoteRepository = Git.lsRemoteRepository();
		lsRemoteRepository.setRemote(remote).setHeads(true);
		lsRemoteRepository.setCredentialsProvider(new UsernamePasswordCredentialsProvider(credentials.getUser(), credentials.getPassword()));
		return lsRemoteRepository.call().stream().map(this::getReferenceName).toArray(String[]::new);
	}
	
	/**
	 * Performs the GIT clone operation.
	 * 
	 * @param directory the directory to clone the remote repository.
	 * @param uri the remote name of project repository.
	 * @param branch the branch name of repository to clone.
	 * @throws GitAPIException 
	 * @throws TransportException 
	 * @throws InvalidRemoteException 
	 * @throws IOException 
	 */
	public void cloneRepository(String directory, String uri, String branch, UserPasswordCredentials credentials) throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		CloneCommand clone = Git.cloneRepository();
		clone.setCredentialsProvider(new UsernamePasswordCredentialsProvider(credentials.getUser(), credentials.getPassword()));
		clone.setDirectory(new File(directory + "/" + this.getRepositoryName(uri)));
		clone.setURI(uri);
		clone.setRemote("origin");
		clone.setCloneAllBranches(true);
		clone.setCloneSubmodules(true);
		clone.setBranch("refs/heads/" + branch);
		clone.call();

		String filepath = directory + "/" + this.getRepositoryName(uri) + "/.project";

		Document doc;
		
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(filepath));
			XPath xpath = XPathFactory.newInstance().newXPath();
			NodeList nodes = (NodeList) xpath.evaluate("//projectDescription/name", doc, XPathConstants.NODESET);
			for (int idx = 0; idx < nodes.getLength(); idx++) {
				nodes.item(idx).setTextContent(this.getRepositoryName(uri));
			}
			Transformer xformer = TransformerFactory.newInstance().newTransformer();
			xformer.transform(new DOMSource(doc), new StreamResult(new File(filepath)));
		}

		catch (SAXException | ParserConfigurationException | XPathExpressionException | TransformerFactoryConfigurationError | TransformerException e) {
			e.printStackTrace();
		}

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
	 * @throws IOException 
	 */
	public void pull() throws WrongRepositoryStateException, InvalidConfigurationException, DetachedHeadException, InvalidRemoteException, CanceledException, RefNotFoundException, RefNotAdvertisedException, NoHeadException, TransportException, GitAPIException, MergeConflictsException, IOException {
		PullCommand pull = git.pull();
		
		
		
		Repository repository = git.getRepository();
		this.setCredentialsProvider(pull);
		pull.call();
		
		RepositoryState repositoryState = repository.getRepositoryState();
		if (repositoryState == RepositoryState.MERGING) {
			throw new MergeConflictsException();
		}
		
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
		PushCommand push = git.push();

		this.setCredentialsProvider(pull, push);

		pull.call();
		
		RepositoryState repositoryState = git.getRepository().getRepositoryState();
		if (repositoryState == RepositoryState.MERGING) {
			throw new MergeConflictsException();
		}
		
		push.call();

	}
	
	public void commitAndPush(String message, String author, String authorEmail, String committer, String committerEmail) throws NoHeadException, NoMessageException, UnmergedPathsException, ConcurrentRefUpdateException, WrongRepositoryStateException, AbortedByHookException, GitAPIException {
		CommitCommand commit = git.commit();
		PullCommand pull = git.pull();
		PushCommand push = git.push();

		commit.setMessage(message);
		commit.setAuthor(author, authorEmail);
		commit.setCommitter(committer, committerEmail);
		
		this.setCredentialsProvider(pull, push);
		
		commit.call();
		pull.call();
		
		RepositoryState repositoryState = git.getRepository().getRepositoryState();
		if (repositoryState == RepositoryState.MERGING) {
			throw new MergeConflictsException();
		}

		push.call();
	}
	
	public void commitAndPush(String message, String author, String authorEmail) throws NoHeadException, NoMessageException, UnmergedPathsException, ConcurrentRefUpdateException, WrongRepositoryStateException, AbortedByHookException, GitAPIException {
		this.commitAndPush(message, author, authorEmail, author, authorEmail);
	}

	@SuppressWarnings("rawtypes")
	private void setCredentialsProvider(TransportCommand...commands) {
		UserPasswordCredentials credentials;
		List<TransportCommand> commandlist = Arrays.asList(commands);
		credentials = new SecureStoredUserPasswordCredentials();
//				SecureStoredUserPasswordCredentials.getInstance().retrieve();
		
		if (!credentials.isEmpty()) {
			for (TransportCommand command : commandlist) {
				command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(credentials.getUser(), credentials.getPassword()));
			}
		}
		
		else {
			CrendentialsDialog dialog = new CrendentialsDialog(null);
			dialog.open();
			credentials = dialog.getCredentials();
			
			if(!credentials.isEmpty()){
				for (TransportCommand command : commandlist) {
					command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(credentials.getUser(), credentials.getPassword()));
				}
			}
			
			else {
				throw new NotAuthorizedException();					
			}
		}
	}

}
