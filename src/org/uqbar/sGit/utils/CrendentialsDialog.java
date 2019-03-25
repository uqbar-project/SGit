package org.uqbar.sGit.utils;

import static org.eclipse.swt.SWT.*;
import static org.eclipse.swt.layout.GridData.*;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.uqbar.sGit.importWizards.pages.Messages;

import org.eclipse.jface.dialogs.Dialog;

public class CrendentialsDialog extends Dialog {
	private Text usernameText;
	private Text passwordText;
	private String username = "";
	private String password = "";
	private Button secureCheckbox;
	private boolean okPressed = false;

	public CrendentialsDialog(Shell parent) {
		super(parent);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		container.setLayoutData(new GridData(FILL_HORIZONTAL));

		final Label authenticationLabel = new Label(container, HORIZONTAL_ALIGN_FILL);
		final GridData authenticationGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		authenticationGridData.horizontalSpan = 2;
		authenticationLabel.setLayoutData(authenticationGridData);
		authenticationLabel.setText("Autenticarse");

		final Label usernameLabel = new Label(container, PUSH);
		final GridData usernameGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		usernameGridData.horizontalSpan = 1;
		usernameLabel.setLayoutData(usernameGridData);
		usernameLabel.setText("Usuario:");

		usernameText = new Text(container, BORDER | SINGLE);
		usernameText.setLayoutData(new GridData(FILL_HORIZONTAL));
		usernameText.setText(""); //$NON-NLS-1$
		// username.addModifyListener(this);

		final Label passwordLabel = new Label(container, PUSH);
		final GridData passwordGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		passwordGridData.horizontalSpan = 1;
		passwordLabel.setLayoutData(passwordGridData);
		passwordLabel.setText(Messages.PASSWORD);

		passwordText = new Text(container, BORDER | SINGLE | PASSWORD);
		passwordText.setLayoutData(new GridData(FILL_HORIZONTAL));
		passwordText.setText(""); //$NON-NLS-1$
		// password.addModifyListener(this);

		secureCheckbox = new Button(container, CHECK);
		final GridData secureCheckboxGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		secureCheckboxGridData.horizontalSpan = 2;
		secureCheckbox.setLayoutData(secureCheckboxGridData);
		secureCheckbox.setText("¿Desea recordar su nombre de usuario y contraseña?");
		secureCheckbox.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// that.updatePageState();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// this.widgetSelected(e);
			}

		});

		container.pack();

		return container;
	}

	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Ingrese un nombre de usuario y contraseña.");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(400, 200);
	}

	@Override
	protected void okPressed() {
		username = usernameText.getText();
		password = passwordText.getText();
		super.okPressed();
		okPressed = true;
	}
	
	public boolean isOkPressed(){
		return okPressed;
	}

	public GitCredentials getCredentials(){
		return new GitCredentials(username, password);
	}

}