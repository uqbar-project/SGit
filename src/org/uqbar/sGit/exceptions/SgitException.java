package org.uqbar.sGit.exceptions;

@SuppressWarnings("serial")
public class SgitException extends RuntimeException {

	public SgitException() {
		super();
	}

	public SgitException(String s) {
		super(s);
	}

	public SgitException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public SgitException(Throwable throwable) {
		super(throwable);
	}

}
