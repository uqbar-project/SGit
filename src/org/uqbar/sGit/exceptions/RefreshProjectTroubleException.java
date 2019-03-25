package org.uqbar.sGit.exceptions;

@SuppressWarnings("serial")
public class RefreshProjectTroubleException extends SgitException {

	public RefreshProjectTroubleException() {
		super("a problem has occurred when refreshing the current project.");
	}

}
