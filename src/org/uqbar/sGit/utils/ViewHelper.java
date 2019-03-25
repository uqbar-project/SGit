package org.uqbar.sGit.utils;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.uqbar.sGit.exceptions.RefreshProjectTroubleException;

public class ViewHelper {

	public void showInformationDialog(String title, String message) {
		MessageDialog.openInformation(null, title, message);
	}

	public void showErrorDialog(String title, String message) {
		MessageDialog.openError(null, title, message);
	}

	public boolean showQuestionDialog(String title, String message) {
		return MessageDialog.openQuestion(null, title, message);
	}

	public void showWarningDialog(String title, String message) {
		MessageDialog.openWarning(null, title, message);
	}

	
	public void showConfirmDialog(String title, String message) {
		MessageDialog.openConfirm(null, title, message);
	}
	
	public List<IProject> getProjects() {
		return Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().getProjects());
	}

	public void refreshProject(IProject project) {
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		}

		catch (Exception e) {
			this.showErrorDialog("Error Interno", new RefreshProjectTroubleException().getMessage());
		}
	}

	public void refreshWorkspace() {
		this.getProjects().stream().forEach(this::refreshProject);
	}

}
