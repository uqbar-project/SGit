package org.uqbar.sGit.exceptions;

@SuppressWarnings("serial")
public class NotAuthorizedException extends SgitException {

	public NotAuthorizedException() {
		super("Nombre de usuario y/o contraseña invalidas, acceso al repositorio remoto no autorizado.");
	}

}
