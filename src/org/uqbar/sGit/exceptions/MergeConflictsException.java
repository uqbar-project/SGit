package org.uqbar.sGit.exceptions;

@SuppressWarnings("serial")
public class MergeConflictsException extends SgitException {

	public MergeConflictsException() {
		super("Existen conflictos entre el repositorio local y el remoto, resuelva los conflictos y realice nuevamente un commit.");
	}

}
