package org.uqbar.sGit.utils;

import static org.eclipse.swt.SWT.*;
import static org.eclipse.swt.layout.GridData.*;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.dialogs.Dialog;

public class NewAuthorDialog extends Dialog {
	private Text nameText;
	private Text emailText;
	private String name = "";
	private String email = "";

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
		newAuthorLabel.setText("Cargar nuevo autor");

		final Label nameLabel = new Label(container, PUSH);
		final GridData nameGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		nameGridData.horizontalSpan = 1;
		nameLabel.setLayoutData(nameGridData);
		nameLabel.setText("Nombre completo: ");

		nameText = new Text(container, BORDER | SINGLE);
		nameText.setLayoutData(new GridData(FILL_HORIZONTAL));
		nameText.setText(this.name); //$NON-NLS-1$

		final Label emailLabel = new Label(container, PUSH);
		final GridData emailGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		emailGridData.horizontalSpan = 1;
		emailLabel.setLayoutData(emailGridData);
		emailLabel.setText("Email: ");

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
		newShell.setText("Realizar commit por primera vez con author.");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(400, 200);
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