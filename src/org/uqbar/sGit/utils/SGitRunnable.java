package org.uqbar.sGit.utils;

import org.eclipse.jgit.api.errors.GitAPIException;

@FunctionalInterface
public interface SGitRunnable {

	public void run() throws GitAPIException;

}
