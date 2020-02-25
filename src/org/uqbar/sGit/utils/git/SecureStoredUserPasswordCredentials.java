package org.uqbar.sGit.utils.git;

import org.uqbar.sGit.utils.SecureStore;

public class SecureStoredUserPasswordCredentials extends UserPasswordCredentials {

	private final static String DEFAULT_VALUE = "";
	private final static SecureStore secureStore = SecureStore.getInstance();

	public SecureStoredUserPasswordCredentials() {
		super(secureStore.getOrDefault("credentials", "user", DEFAULT_VALUE),
				secureStore.getOrDefault("credentials", "password", DEFAULT_VALUE));
	}

}
