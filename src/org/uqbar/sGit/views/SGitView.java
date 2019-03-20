package org.uqbar.sGit.views;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.uqbar.sGit.model.repository.GitRepository;
import org.uqbar.sGit.model.repository.credentials.GitCredentials;
import org.uqbar.sGit.model.repository.credentials.SecureStoredCredentialsManager;

public abstract class SGitView extends ViewPart {

	protected GitRepository gitRepository;
	protected Composite container;
	protected Action refreshProject;

	private String getWorkspacePath() {
		return Platform.getLocation().toString();
	}

	protected void refreshProject() {
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			try {
				project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			} 
			
			catch (CoreException e) {
				this.showErrorDialog("a problem has occurred when refreshing the project.");
			}
		}
	}

	protected void updateView() {
		final ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getSelectionService();
		selectionService.addSelectionListener(new UISelectionListener(this));
	}

	protected void update() {
		this.updateView();
		this.refreshProject();
	}

	protected void onUpdateProjectOnStructureSelection(IProject project) {
		gitRepository = new GitRepository(this.getWorkspacePath() + "/" + project.getName());
		this.onProjectReferenceUpdate();
	}

	protected void onUpdateProjectOnEditorSelection(IProject project) {
		GitCredentials credentials = SecureStoredCredentialsManager.getInstance().retrieveCrendentials();
		gitRepository = GitRepository.getRepository(this.getWorkspacePath(), project.getName(), credentials);
		this.onProjectReferenceUpdate();
	}

	protected abstract void onFocus();

	protected abstract void onViewInit();

	protected abstract void onProjectReferenceUpdate();

	@Override
	public void createPartControl(Composite parent) {
		container = parent;
		this.onViewInit();
		container.pack();
		this.updateView();
		makeActions();
		hookContextMenu();
		contributeToActionBars();

	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				SGitView.this.fillContextMenu(manager);
			}
		});
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(refreshProject);
		manager.add(new Separator());
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(refreshProject);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refreshProject);
	}

	private void makeActions() {
		SGitView that = this;
		refreshProject = new Action() {
			public void run() {
				that.refreshProject();
			}
		};
		refreshProject.setText("Refresh");
		refreshProject.setToolTipText("Refresh current project workspace");
		refreshProject.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
	}
	
	protected void showInformationDialog(String message) {
		MessageDialog.openInformation(this.container.getShell(), "Git", message);
	}
	
	protected void showErrorDialog(String message) {
		MessageDialog.openError(this.container.getShell(), "Git", message);
	}
	
	protected void showQuestionDialog(String message) {
		MessageDialog.openQuestion(this.container.getShell(), "Git", message);
	}
	
	protected void showWarningDialog(String message) {
		MessageDialog.openWarning(this.container.getShell(), "Git", message);
	}
	
	protected void showConfirmDialog(String message) {
		MessageDialog.openConfirm(this.container.getShell(), "Git", message);
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
