package org.uqbar.sGit.importWizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.uqbar.sGit.importWizards.pages.LocalDestinationPage;
import org.uqbar.sGit.importWizards.pages.SGitWizardPage;
import org.uqbar.sGit.importWizards.pages.SourceGitRepositoryPage;

public class ImportProjectFromGitRepositoryWizard extends Wizard implements IImportWizard {

	public ImportProjectFromGitRepositoryWizard() {
		this.setWindowTitle("Import a project from any Git repository");
		this.setNeedsProgressMonitor(true);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {

	}

	@Override
	public void addPages() {
		this.addPage(new SourceGitRepositoryPage("Source GIT Repository"));
		this.addPage(new LocalDestinationPage("Local Destination"));
	}

	@Override
	public boolean performFinish() {

		for (IWizardPage page : this.getPages()) {
			((SGitWizardPage) page).close();
		}

		return true;
	}

}
