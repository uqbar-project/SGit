package org.uqbar.sGit.exceptions;

@SuppressWarnings("serial")
public class NotAuthorizedException extends SgitException {

	public NotAuthorizedException() {
		super(Messages.NotAuthorizedExceptionMessage);
	}

}
