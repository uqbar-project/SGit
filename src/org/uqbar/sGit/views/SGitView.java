package org.uqbar.sGit.views;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.uqbar.sGit.git.GitRepository;

public abstract class SGitView extends ViewPart {

	protected GitRepository gitRepository;
	protected Composite container;

	protected abstract void onFocus();

	protected abstract void onViewInit();

	protected abstract void onProjectReferenceUpdate();

	private void updateProjectReference() {
		final IWorkbench workbrench = PlatformUI.getWorkbench();
		final IWorkbenchWindow window = workbrench.getActiveWorkbenchWindow();
		final ISelectionService selectionService = window.getSelectionService();
		final String workspacePath = Platform.getLocation().toString();
		final SGitView that = this;

		selectionService.addSelectionListener(new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart sourcePart, ISelection selection) {
				if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
					if (((IStructuredSelection) selection).getFirstElement() instanceof IProject) {
						final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
						final IProject project = (IProject) structuredSelection.getFirstElement();
						gitRepository = new GitRepository(workspacePath + "/" + project.getName());
						that.onProjectReferenceUpdate();
					}
				}

				else {
					IWorkbenchPage activePage = window.getActivePage();
					IEditorPart activeEditor = activePage.getActiveEditor();

					if (activeEditor != null) {
						IEditorInput input = activeEditor.getEditorInput();
						IProject project = (IProject) input.getAdapter(IProject.class);
						if (project == null) {
							IResource resource = (IResource) input.getAdapter(IResource.class);
							if (resource != null) {
								project = resource.getProject();
								ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
								ISecurePreferences node = preferences.node("credentials");
								String user = "";
								String password = "";
								try {
									user = node.get("user", "n/a");
									password = node.get("password", "n/a");
								} 
								catch (StorageException e) {
									
								}
								
								if (user != "n/a" && password != "n/a") {
									gitRepository = new GitRepository(workspacePath + "/" + project.getName(), user, password);
								}
								else {
									gitRepository = new GitRepository(workspacePath + "/" + project.getName());
								}
								that.onProjectReferenceUpdate();
							}
						}
					}
				}
			}
		});
	}

	@Override
	public void createPartControl(Composite parent) {
		container = parent;
		this.onViewInit();
		container.pack();
		this.updateProjectReference();
	}

	@Override
	public void setFocus() {
		if (container != null) {
			container.setFocus();
			this.onFocus();
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Class adapter) {
		return super.getAdapter(adapter);
	}

}
