package org.uqbar.sGit.utils;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;

/**
 * @author Lucas Alan Silvestri
 *
 */
public class SecureStoredCredentials {

	private static SecureStoredCredentials instance = new SecureStoredCredentials();
	final ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
	final ISecurePreferences node = preferences.node("credentials");

	public static SecureStoredCredentials getInstance() {
		if (instance == null) {

			instance = new SecureStoredCredentials();
		}
		return instance;
	}

	private SecureStoredCredentials() {

	}

	public void secure(String user, String password) {
		try {
			node.put("user", user, true);
			node.put("password", password, true);
		}

		catch (StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public GitCredentials retrieve() {
		try {
			String username = node.get("user", "");
			String password = node.get("password", "");
			return new GitCredentials(username, password);
		}

		catch (StorageException e) {
			return new GitCredentials();
		}
	}

}
