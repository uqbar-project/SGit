package org.uqbar.sGit.exceptions;

@SuppressWarnings("serial")
public class NoConnectionWithRemoteException extends SgitException {

	public NoConnectionWithRemoteException() {
		super(Messages.NoConnectionWithRemoteExceptionMessage);
	}

}
