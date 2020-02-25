package org.uqbar.sGit.views;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
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
		System.out.println("cleaned");
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

	private void onActiveEditorItemSelection() {
		IProject project = ((IResource) this.workspace.getActiveEditorInput().getAdapter(IResource.class)).getProject();

		if (project != null) {
			this.gitRepository = new GitRepository(this.workspace.getCurrentWorkspacePath(), project.getName());
			this.refresh.run();
		}
	}

//	private void onNotActiveSelections() {
//		this.clean();
//	}

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
				this.onActiveEditorItemSelection();
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
