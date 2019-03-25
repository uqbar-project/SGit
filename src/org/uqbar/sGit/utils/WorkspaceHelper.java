package org.uqbar.sGit.utils;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class WorkspaceHelper {

	public String getCurrentWorkspacePath() {
		return Platform.getLocation().toString();
	}

	public IWorkbench getActiveWorkbench() {
		return PlatformUI.getWorkbench();
	}

	public IWorkbenchWindow getActiveWorkbenchWindow() {
		return this.getActiveWorkbench().getActiveWorkbenchWindow();
	}

	public IWorkbenchPage getActivePage() {
		return this.getActiveWorkbenchWindow().getActivePage();
	}

	public IEditorPart getActiveEditor() {
		return this.getActivePage().getActiveEditor();
	}

	public IEditorInput getActiveEditorInput() {
		return this.getActiveEditor().getEditorInput();
	}

	public ISelectionService getSelectionService() {
		return this.getActiveWorkbenchWindow().getSelectionService();
	}

}
