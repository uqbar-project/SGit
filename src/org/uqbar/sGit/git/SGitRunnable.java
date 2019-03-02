package org.uqbar.sGit.git;

import org.eclipse.jgit.api.errors.GitAPIException;

@FunctionalInterface
public interface SGitRunnable {

	public void run() throws GitAPIException;

}
