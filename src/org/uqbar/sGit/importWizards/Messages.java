package org.uqbar.sGit.importWizards;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.uqbar.sGit.importWizards.messages"; //$NON-NLS-1$
	public static String IMPORT_GIT_PROJECT_WIZARD_TITLE;
	public static String LOCAL_DESTINATION_TITLE;
	public static String SOURCE_TITLE;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
