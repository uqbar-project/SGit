package org.uqbar.sGit.views.actions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.uqbar.sGit.views.actions.messages"; //$NON-NLS-1$
	public static String REFRESH_ACTION_MESSAGE;
	public static String REFRESH_ACTION_TOOLTIP;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {

	}

}
