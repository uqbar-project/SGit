package org.uqbar.sGit.model.repository.credentials;

/**
 * The GitCredentials class is the abstraction of the necessary credentials to
 * be able to perform operation with a remote GIT repository.
 * 
 * @author Lucas Alan Silvestri
 *
 */
public class GitCredentials {

	final private static String NO_USERNAME = "";
	final private static String NO_PASSWORD = "";
	final public static GitCredentials NO_CREDENTIALS = new GitCredentials(NO_USERNAME, NO_PASSWORD);
	final private String username;
	final private String password;

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
	 * Creates a new Empty Credentials.
	 */
	public GitCredentials() {
		this.username = NO_USERNAME;
		this.password = NO_PASSWORD;
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
		return this.getUsername() == NO_USERNAME && this.getPassword() == NO_PASSWORD;
	}

}
