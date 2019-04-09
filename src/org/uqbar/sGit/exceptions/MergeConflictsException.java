package org.uqbar.sGit.exceptions;

@SuppressWarnings("serial")
public class MergeConflictsException extends SgitException {

	public MergeConflictsException() {
		super(Messages.MergeConflictsExceptionMessage);
	}

}
