package org.uqbar.sGit.utils.git;

/**
 * Implements a credentials object containing user and password.
 */
public class UserPasswordCredentials {

	final private String user;
	final private String password;

	/**
	 * Creates a new empty Credentials.
	 */
	public UserPasswordCredentials() {
		this.user = "";
		this.password = "";
	}

	/**
	 * Creates a new Credentials.
	 * 
	 * @param username the Git repository user credential.
	 * @param password the Git repository password credential.
	 */
	public UserPasswordCredentials(final String user, final String password) {
		this.user = user;
		this.password = password;
	}

	/**
	 * Returns the user credential.
	 */
	public String getUser() {
		return user;
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
		return this.getUser().isEmpty() && this.getPassword().isEmpty();
	}

}
