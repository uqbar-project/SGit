package org.uqbar.sGit.utils;

import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class GitCredentials {

	final private String username;
	final private String password;
	final private UsernamePasswordCredentialsProvider credentialProvider;

	public GitCredentials(String username, String password) {
		this.username = username;
		this.password = password;
		this.credentialProvider = new UsernamePasswordCredentialsProvider(username, password);
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public UsernamePasswordCredentialsProvider getCredentialProvider() {
		return credentialProvider;
	}

}
