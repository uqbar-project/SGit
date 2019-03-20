package org.uqbar.sGit.model.repository.credentials;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;

/**
 * @author Lucas Alan Silvestri
 *
 */
public class SecureStoredCredentialsManager {

	private static SecureStoredCredentialsManager instance = new SecureStoredCredentialsManager();
	final ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
	final ISecurePreferences node = preferences.node("credentials");

	public static SecureStoredCredentialsManager getInstance() {
		if (instance == null) {

			instance = new SecureStoredCredentialsManager();
		}
		return instance;
	}

	private SecureStoredCredentialsManager() {

	}

	public void secureCrendetials(String user, String password) {
		try {
			node.put("user", user, true);
			node.put("password", password, true);
		}

		catch (StorageException e1) {
			// TODO: Needs a Validation.
			e1.printStackTrace();
		}
	}

	public GitCredentials retrieveCrendentials() {
		String username = "";
		String password = "";

		try {
			username = node.get("user", "");
			password = node.get("password", "");
		}

		catch (StorageException e) {
			// TODO: Needs a Validation.
			e.printStackTrace();
		}

		return new GitCredentials(username, password);
	}

}
