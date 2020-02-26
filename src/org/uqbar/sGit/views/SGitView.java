package org.uqbar.sGit.views;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
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
import org.uqbar.sGit.views.actions.StageFileAction;
import org.uqbar.sGit.views.actions.CommitAction;
import org.uqbar.sGit.views.actions.PersonIdentConsumerAction;
import org.uqbar.sGit.views.actions.PullAction;
import org.uqbar.sGit.views.actions.PushAction;
import org.uqbar.sGit.views.actions.RefreshAction;
import org.uqbar.sGit.views.actions.UnstageFileAction;
import org.uqbar.sGit.views.actions.StagingFileConsumerAction;
import org.uqbar.sGit.views.actions.UnstagingFileConsumerAction;

public abstract class SGitView extends ViewPart implements ISelectionListener {

	private IActionBars actionBar;
	private IProject project;
	protected Boolean isActive = false;
	protected Composite container;
	protected Dialogs view;
	protected WorkspaceHelper workspace;
	protected CommitAction commitAction;
	protected PullAction pullAction;
	protected PushAction pushAction;
	protected PersonIdentConsumerAction personIdentConsumerAction;
	protected RefreshAction refreshAction;
	protected StageFileAction stageFileAction;
	protected StagingFileConsumerAction staginFileConsumerAction;
	protected UnstageFileAction unstageFileAction;
	protected UnstagingFileConsumerAction unstaginFileConsumerAction;

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

	public IProject getProject() {
		return this.project;
	}

	private void makeActions() {
		this.stageFileAction = new StageFileAction(this);
		this.commitAction = new CommitAction(this);
		this.personIdentConsumerAction = new PersonIdentConsumerAction(this);
		this.pullAction = new PullAction(this);
		this.pushAction = new PushAction(this);
		this.refreshAction = new RefreshAction(this);
		this.unstageFileAction = new UnstageFileAction(this);
		this.staginFileConsumerAction = new StagingFileConsumerAction(this);
		this.unstaginFileConsumerAction = new UnstagingFileConsumerAction(this);

		this.actionBar.getToolBarManager().add(this.refreshAction);
	}

	private Boolean workspaceHaveAnActiveEditor() {
		return this.workspace.getActiveEditor() != null;
	}

	private void onProjectTreeItemSelection(IStructuredSelection selection) {
		this.project = (IProject) Platform.getAdapterManager().getAdapter(selection.getFirstElement(), IProject.class);

		if (this.project != null) {
			this.isActive = true;
			this.refreshAction.run();
		}

		else {
			this.isActive = false;
		}
	}

	private void onActiveEditorItemSelection(IWorkbenchPage activePage) {
		IEditorPart activeEditor = activePage.getActiveEditor();

		if (activeEditor != null) {
			IEditorInput input = activeEditor.getEditorInput();

			this.project = (IProject) input.getAdapter(IProject.class);

			if (project == null) {
				IResource resource = (IResource) input.getAdapter(IResource.class);

				if (resource != null) {
					this.project = resource.getProject();
				}

			}

		}

		if (this.project != null) {
			this.isActive = true;
			this.refreshAction.run();
		}

		else {
			this.isActive = false;
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
			this.onProjectTreeItemSelection((IStructuredSelection) selection);
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
