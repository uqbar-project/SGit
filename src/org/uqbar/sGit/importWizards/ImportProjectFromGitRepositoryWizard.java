package org.uqbar.sGit.importWizards;

import static org.uqbar.sGit.importWizards.Messages.*;

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
		this.setWindowTitle(IMPORT_GIT_PROJECT_WIZARD_TITLE);
		this.setNeedsProgressMonitor(true);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {

	}

	@Override
	public void addPages() {
		this.addPage(new SourceGitRepositoryPage(Messages.SOURCE_TITLE));
		this.addPage(new LocalDestinationPage(Messages.LOCAL_DESTINATION_TITLE));
	}

	@Override
	public boolean performFinish() {

		for (IWizardPage page : this.getPages()) {
			((SGitWizardPage) page).close();
		}

		return true;
	}

}
