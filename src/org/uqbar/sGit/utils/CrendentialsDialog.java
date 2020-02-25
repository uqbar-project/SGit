package org.uqbar.sGit.utils;

import static org.eclipse.swt.SWT.*;
import static org.eclipse.swt.layout.GridData.*;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.uqbar.sGit.importWizards.pages.Messages;
import org.uqbar.sGit.utils.git.UserPasswordCredentials;
import org.eclipse.jface.dialogs.Dialog;

public class CrendentialsDialog extends Dialog {
	private Text usernameText;
	private Text passwordText;
	private String user = "";
	private String password = "";
	private Button secureCheckbox;

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
		usernameLabel.setText(Messages.USER);

		usernameText = new Text(container, BORDER | SINGLE);
		usernameText.setLayoutData(new GridData(FILL_HORIZONTAL));
		usernameText.setText(""); //$NON-NLS-1$

		final Label passwordLabel = new Label(container, PUSH);
		final GridData passwordGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		passwordGridData.horizontalSpan = 1;
		passwordLabel.setLayoutData(passwordGridData);
		passwordLabel.setText(Messages.PASSWORD);

		passwordText = new Text(container, BORDER | SINGLE | PASSWORD);
		passwordText.setLayoutData(new GridData(FILL_HORIZONTAL));
		passwordText.setText(""); //$NON-NLS-1$

		secureCheckbox = new Button(container, CHECK);
		final GridData secureCheckboxGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		secureCheckboxGridData.horizontalSpan = 2;
		secureCheckbox.setLayoutData(secureCheckboxGridData);
		secureCheckbox.setText(Messages.STORE_IN_SECURE_STORE);

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
		return new Point(600, 400);
	}
	
	public Boolean isSecureStoreEnable() {
		return secureCheckbox.getSelection();
	}

	@Override
	protected void okPressed() {
		user = usernameText.getText();
		password = passwordText.getText();
		if (this.isSecureStoreEnable()) {
			SecureStore secureStore = SecureStore.getInstance();
			secureStore.secure("credentials", "user", user);
			secureStore.secure("credentials", "password", password);
		}
		super.okPressed();
	}
	
	public UserPasswordCredentials getCredentials(){
		return new UserPasswordCredentials(user, password);
	}

}