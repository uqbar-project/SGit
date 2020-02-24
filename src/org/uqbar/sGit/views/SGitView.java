package org.uqbar.sGit.views;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.uqbar.sGit.utils.GitRepository;
import org.uqbar.sGit.utils.ImageLocator;
import org.uqbar.sGit.utils.WorkspaceHelper;

public abstract class SGitView extends ViewPart implements ISelectionListener {

	protected GitRepository gitRepository;
	protected Composite container;
	protected Dialogs view;
	protected WorkspaceHelper workspace;

	protected abstract void onFocus();

	protected abstract void onViewInit();

	protected abstract void onUpdate();

	@Override
	public void createPartControl(Composite parent) {
		this.container = parent;
		this.view = new Dialogs();
		this.workspace = new WorkspaceHelper();
		this.onViewInit();
		this.workspace.getSelectionService().addSelectionListener(this);
		this.makeActions();
		this.container.pack();
	}

	@Override
	public void setFocus() {
		if (this.container != null) {
			this.container.setFocus();
			this.onFocus();
		}
	}

	private void makeActions() {
		IActionBars bars = getViewSite().getActionBars();

		Action update = new Action() {
			public void run() {
				if (container != null && gitRepository != null) {
					onUpdate();
				}
			}
		};

		update.setText(Messages.UPDATE_MESSAGE);
		update.setToolTipText(Messages.UPDATE_TOOLTIP);
		update.setImageDescriptor(ImageLocator.getImageDescriptor("refresh", this));
		bars.getToolBarManager().add(update);
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IProject project;

		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			// TODO: Should get project from selection element, but i have no
			// idea about make this possible :(
		}

		else {

			if (this.workspace.getActiveEditor() != null) {
				project = (IProject) this.workspace.getActiveEditorInput().getAdapter(IProject.class);

				if (project == null) {
					IResource resource = (IResource) this.workspace.getActiveEditorInput().getAdapter(IResource.class);

					if (resource != null) {
						project = resource.getProject();
						this.gitRepository = new GitRepository(this.workspace.getCurrentWorkspacePath(),
								project.getName());
						this.onUpdate();
					}

				}
			}
		}

	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Class adapter) {
		return super.getAdapter(adapter);
	}
}
