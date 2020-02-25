package org.uqbar.sGit.exceptions;

@SuppressWarnings("serial")
public class ProjectAlreadyExistsException extends SgitException {

	public ProjectAlreadyExistsException(String directoryPath, String repositoryName) {
		super(Messages.ProjectAlreadyExistsException_0 + " " + '"' + directoryPath + "/git/" + repositoryName + '"' + ": "
				+ Messages.ProjectAlreadyExistsException_1);
	}

}
