package org.uqbar.sGit.views;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.uqbar.sGit.model.repository.GitRepository;
import org.uqbar.sGit.model.repository.credentials.GitCredentials;
import org.uqbar.sGit.model.repository.credentials.SecureStoredCredentialsManager;

public abstract class SGitView extends ViewPart {

	protected GitRepository gitRepository;
	protected Composite container;

	private String getWorkspacePath() {
		return Platform.getLocation().toString();
	}

	protected void updateProject() {
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			System.out.println(project);
		}
	}

	protected void updateView() {
		final ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		selectionService.addSelectionListener(new UISelectionListener(this));
	}

	protected void update() {
		this.updateView();
		this.updateProject();
	}

	protected void onUpdateProjectOnStructureSelection(IProject project) {
		gitRepository = new GitRepository(this.getWorkspacePath() + "/" + project.getName());
		this.onProjectReferenceUpdate();
	}

	protected void onUpdateProjectOnEditorSelection(IProject project) {
		GitCredentials credentials = SecureStoredCredentialsManager.getInstance().retrieveCrendentials();

		if (!credentials.isEmpty()) {
			gitRepository = GitRepository.getRepository(this.getWorkspacePath(), project.getName(), credentials);
		}

		else {
			gitRepository = GitRepository.getRepository(this.getWorkspacePath(), project.getName());
		}
		
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
