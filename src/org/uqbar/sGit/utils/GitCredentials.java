package org.uqbar.sGit.utils;

/**
 * The GitCredentials class is the abstraction of the necessary credentials to
 * be able to perform operation with a remote GIT repository.
 * 
 * @author Lucas Alan Silvestri
 *
 */
public class GitCredentials {

	final private String username;
	final private String password;
	
	/**
	 * Creates a new empty Credentials.
	 */
	public GitCredentials() {
		this.username = "";
		this.password = "";
	}

	/**
	 * Creates a new Credentials.
	 * 
	 * @param username the Git repository username credential.
	 * @param password the Git repository password credential.
	 */
	public GitCredentials(final String username, final String password) {
		this.username = username;
		this.password = password;
	}

	/**
	 * Returns the username credential.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Returns the password credential.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Returns if the credentials are empty.
	 */
	public boolean isEmpty() {
		return username == "" && password == "";
	}

}
