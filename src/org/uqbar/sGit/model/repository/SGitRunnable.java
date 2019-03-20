package org.uqbar.sGit.model.repository;

import org.eclipse.jgit.api.errors.GitAPIException;

@FunctionalInterface
public interface SGitRunnable {

	public void run() throws GitAPIException;

}
