package org.uqbar.sGit.views;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.uqbar.sGit.views.messages"; //$NON-NLS-1$
	public static String UNSTAGED_CHANGES;
	public static String UPDATE_MESSAGE;
	public static String UPDATE_TOOLTIP;
	public static String STAGED_CHANGES;
	public static String COMMIT_MESSAGE;
	public static String AUTHOR;
	public static String PUSH_ACTION;
	public static String PULL_ACTION;
	public static String COMMIT_ACTION;
	public static String COMMIT_AND_PUSH_ACTION;
	public static String Dialogs_EMAIL;
	public static String Dialogs_FIRST_USER_COMMIT;
	public static String Dialogs_INTERNAL_ERROR;
	public static String Dialogs_LOAD_NEW_USER_IDENTIFICATION;
	public static String Dialogs_USERNAME;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
