package org.uqbar.sGit.importWizards.pages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.uqbar.sGit.importWizards.pages.messages"; //$NON-NLS-1$
	public static String AUTHENTICATION;
	public static String BRANCHES;
	public static String BROWSE;
	public static String DESTINATION;
	public static String DIRECTORY;
	public static String ENTER_VALID_DIRECTORY;
	public static String ERROR_ENTER_PASSWORD;
	public static String ERROR_ENTER_USERNAME;
	public static String ERROR_ENTER_USERNAME_AND_PASSWORD;
	public static String ERROR_ENTER_VALID_GIT_URI;
	public static String INITIAL_BRANCH;
	public static String LOCAL_DESTINATION_PAGE_TITLE;
	public static String LOCATION;
	public static String PASSWORD;
	public static String SOURCE_TITLE;
	public static String STORE_IN_SECURE_STORE;
	public static String USE_DEFAULT_LOCATION;
	public static String USER;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
