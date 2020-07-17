package org.uqbar.sGit.views;

import static org.eclipse.swt.SWT.*;
import static org.eclipse.swt.layout.GridData.*;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.uqbar.sGit.exceptions.RefreshProjectTroubleException;

public class Dialogs {

	public void showInformationDialog(String title, String message) {
		MessageDialog.openInformation(null, title, message);
	}

	public void showErrorDialog(String title, String message) {
		MessageDialog.openError(null, title, message);
	}

	public boolean showQuestionDialog(String title, String message) {
		return MessageDialog.openQuestion(null, title, message);
	}

	public void showWarningDialog(String title, String message) {
		MessageDialog.openWarning(null, title, message);
	}

	
	public void showConfirmDialog(String title, String message) {
		MessageDialog.openConfirm(null, title, message);
	}
	
	public List<IProject> getProjects() {
		return Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().getProjects());
	}

	public void refreshProject(IProject project) {
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		}

		catch (Exception e) {
			this.showErrorDialog(Messages.Dialogs_INTERNAL_ERROR, new RefreshProjectTroubleException().getMessage());
		}
	}

	public void refreshWorkspace() {
		this.getProjects().stream().forEach(this::refreshProject);
	}
	
	public static class NewAuthorDialog extends Dialog {
		private Text nameText;
		private Text emailText;
		private String name = ""; //$NON-NLS-1$
		private String email = ""; //$NON-NLS-1$

		public NewAuthorDialog(Shell parent, String name) {
			super(parent);
			this.name = name;
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite container = (Composite) super.createDialogArea(parent);
			final GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			container.setLayout(layout);
			container.setLayoutData(new GridData(FILL_HORIZONTAL));

			final Label newAuthorLabel = new Label(container, HORIZONTAL_ALIGN_FILL);
			final GridData newAuthorGridData = new GridData(HORIZONTAL_ALIGN_FILL);
			newAuthorGridData.horizontalSpan = 2;
			newAuthorLabel.setLayoutData(newAuthorGridData);
			newAuthorLabel.setText(Messages.Dialogs_LOAD_NEW_USER_IDENTIFICATION);

			final Label nameLabel = new Label(container, PUSH);
			final GridData nameGridData = new GridData(HORIZONTAL_ALIGN_FILL);
			nameGridData.horizontalSpan = 1;
			nameLabel.setLayoutData(nameGridData);
			nameLabel.setText(Messages.Dialogs_USERNAME);

			nameText = new Text(container, BORDER | SINGLE);
			nameText.setLayoutData(new GridData(FILL_HORIZONTAL));
			nameText.setText(this.name); //$NON-NLS-1$

			final Label emailLabel = new Label(container, PUSH);
			final GridData emailGridData = new GridData(HORIZONTAL_ALIGN_FILL);
			emailGridData.horizontalSpan = 1;
			emailLabel.setLayoutData(emailGridData);
			emailLabel.setText(Messages.Dialogs_EMAIL);

			emailText = new Text(container, BORDER | SINGLE);
			emailText.setLayoutData(new GridData(FILL_HORIZONTAL));
			emailText.setText(""); //$NON-NLS-1$

			container.pack();

			return container;
		}

		// overriding this methods allows you to set the
		// title of the custom dialog
		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(Messages.Dialogs_FIRST_USER_COMMIT);
		}

		@Override
		protected void okPressed() {
			name = nameText.getText();
			email = emailText.getText();
			super.okPressed();
		}

		public String getName() {
			return name;
		}

		public String getEmail() {
			return email;
		}

	}

}
