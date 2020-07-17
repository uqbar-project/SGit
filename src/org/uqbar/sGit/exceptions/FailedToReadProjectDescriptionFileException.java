package org.uqbar.sGit.exceptions;

@SuppressWarnings("serial")
public class FailedToReadProjectDescriptionFileException extends SgitException {

	public FailedToReadProjectDescriptionFileException(String directoryPath, String repositoryName) {
		super(Messages.FailedToReadProjectDescriptionFileExceptiond_0 + " " + '"' + directoryPath + "/git/" + repositoryName
				+ '"' + ". " + Messages.FailedToReadProjectDescriptionFileExceptiond_1);
	}

}
