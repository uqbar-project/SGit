package org.uqbar.sGit.utils.git;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.uqbar.sGit.exceptions.NotAuthorizedException;
import org.uqbar.sGit.views.CrendentialsDialog;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class GitRepositoryUtils {

	public GitRepositoryUtils() {
		
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

	@SuppressWarnings("rawtypes")
	private void setCredentialsProvider(TransportCommand...commands) {
		UserPasswordCredentials credentials;
		List<TransportCommand> commandlist = Arrays.asList(commands);
		credentials = new SecureStoredUserPasswordCredentials();
		
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
