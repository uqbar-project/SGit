package org.uqbar.sGit.views;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.uqbar.sGit.utils.WorkspaceHelper;
import org.uqbar.sGit.utils.git.GitRepository;
import org.uqbar.sGit.views.actions.RefreshAction;

public abstract class SGitView extends ViewPart implements ISelectionListener {

	protected GitRepository gitRepository;
	protected Composite container;
	protected Dialogs view;
	protected WorkspaceHelper workspace;
	private IActionBars actionBar;
	private RefreshAction refresh;

	public boolean isAlreadyInitialized() {
		return this.container != null;
	}

	@Override
	public void dispose() {
		this.workspace.getSelectionService().removeSelectionListener(this);
		super.dispose();
	}

	public void clean() {
		this.onViewClean();
	}

	public void refresh() {
		this.onUpdate();
	}

	@Override
	public void setFocus() {

	}

	private void makeActions() {
		this.refresh = new RefreshAction(this);
		this.actionBar.getToolBarManager().add(this.refresh);
	}

	private Boolean workspaceHaveAnActiveEditor() {
		return this.workspace.getActiveEditor() != null;
	}

	private void onProjectTreeItemSelection() {
		// TODO: Should get project from selection element, but i have no
		// idea about make this possible :(
	}

	private void onActiveEditorItemSelection(IWorkbenchPage activePage) {
		IEditorPart activeEditor = activePage.getActiveEditor();

		if (activeEditor != null) {
			IEditorInput input = activeEditor.getEditorInput();

			IProject project = (IProject) input.getAdapter(IProject.class);
			if (project == null) {
				IResource resource = (IResource) input.getAdapter(IResource.class);
				if (resource != null) {
					project = resource.getProject();
					String path = project.getLocation().toOSString();
					this.gitRepository = new GitRepository(path);
					this.refresh.run();
				}
			}
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		this.container = parent;
		this.view = new Dialogs();
		this.workspace = new WorkspaceHelper();
		this.onViewInit();
		this.workspace.getSelectionService().addSelectionListener(this);
		this.actionBar = this.getViewSite().getActionBars();
		this.makeActions();
		this.container.pack();
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {

		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			this.onProjectTreeItemSelection();
		}

		else {

			if (this.workspaceHaveAnActiveEditor()) {
				this.onActiveEditorItemSelection(part.getSite().getWorkbenchWindow().getActivePage());
			}

		}

	}

	// UI hooks.

	protected abstract void onViewInit();

	protected abstract void onUpdate();

	protected abstract void onFocus();

	protected abstract void onViewClean();

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Class adapter) {
		return super.getAdapter(adapter);
	}

}
