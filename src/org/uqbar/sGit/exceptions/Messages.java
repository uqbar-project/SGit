package org.uqbar.sGit.exceptions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.uqbar.sGit.exceptions.messages"; //$NON-NLS-1$
	public static String MergeConflictsExceptionMessage;
	public static String NoConnectionWithRemoteExceptionMessage;
	public static String NotAuthorizedExceptionMessage;
	public static String FailedToReadProjectDescriptionFileExceptiond_0;
	public static String FailedToReadProjectDescriptionFileExceptiond_1;
	public static String ProjectAlreadyExistsException_0;
	public static String ProjectAlreadyExistsException_1;
	public static String RefreshProjectTroubleExceptionMessage;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
