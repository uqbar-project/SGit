package org.uqbar.sGit.views;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

public class UISelectionListener implements ISelectionListener {

	private final SGitView view;

	public UISelectionListener(final SGitView view) {
		this.view = view;
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IProject project;

		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			// TODO: Should get project from selection element, but i have no idea about  make this possible :( .
			
			// final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			// final Object element = structuredSelection.getFirstElement();
			// project = (IProject) structuredSelection.getFirstElement();
			// this.view.onUpdateProjectOnStructureSelection(project);
		}

		else {
			IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

			if (editor != null) {
				project = (IProject) editor.getEditorInput().getAdapter(IProject.class);

				if (project == null) {
					IResource resource = (IResource) editor.getEditorInput().getAdapter(IResource.class);

					if (resource != null) {
						project = resource.getProject();
						this.view.onUpdateProjectOnEditorSelection(project);
					}

				}
			}

		}

	}

}
