package org.uqbar.sGit.exceptions;

@SuppressWarnings("serial")
public class NoConnectionWithRemoteException extends SgitException {

	public NoConnectionWithRemoteException() {
		super("No se pudo establecer la conexión con el repositorio remoto, revise si su conexión a internet esta habilitada.");
	}

}
