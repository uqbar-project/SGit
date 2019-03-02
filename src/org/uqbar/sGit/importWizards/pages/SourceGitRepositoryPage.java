package org.uqbar.sGit.importWizards.pages;

import static org.eclipse.swt.SWT.*;
import static org.eclipse.swt.layout.GridData.*;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class SourceGitRepositoryPage extends SGitWizardPage {

	private Text uri;
	private Text user;
	private Text password;
	private Button secureCheckbox;

	public SourceGitRepositoryPage(String pageName) {
		super(pageName, "Enter the location of the source repository.");
	}

	public String getUri() {
		return uri.getText();
	}

	public String getUsername() {
		return user.getText();
	}

	public String getPassword() {
		return password.getText();
	}

	public Boolean isSecureStoreEnable() {
		return secureCheckbox.getSelection();
	}

	public Boolean hasAuthentication() {
		return !this.getUsername().isEmpty() && !this.getPassword().isEmpty();
	}

	public Boolean hasValidGitURIFormat(String path) {
		return path.startsWith("https://") && path.endsWith(".git");
	}
	
	private void createLocationComposite(Composite parent) {
		final Composite container = new Composite(parent, NULL);
		final GridLayout location = new GridLayout();
		location.numColumns = 2;
		container.setLayout(location);
		container.setLayoutData(new GridData(FILL_HORIZONTAL));

		final Label locationLabel = new Label(container, HORIZONTAL_ALIGN_FILL);
		final GridData locationGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		locationGridData.horizontalSpan = 2;
		locationLabel.setLayoutData(locationGridData);
		locationLabel.setText("Location");

		final Label uriLabel = new Label(container, PUSH);
		final GridData uriGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		uriGridData.horizontalSpan = 1;
		uriLabel.setLayoutData(uriGridData);
		uriLabel.setText("URI: ");

		uri = new Text(container, BORDER | SINGLE);
		uri.setLayoutData(new GridData(FILL_HORIZONTAL));
		uri.setText("");
		uri.addModifyListener(this);

		container.pack();
	}

	private void createAuthenticationComposite(Composite parent) {
		final SourceGitRepositoryPage that = this;
		final Composite container = new Composite(parent, NULL);
		final GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		container.setLayoutData(new GridData(FILL_HORIZONTAL));

		final Label authenticationLabel = new Label(container, HORIZONTAL_ALIGN_FILL);
		final GridData authenticationGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		authenticationGridData.horizontalSpan = 2;
		authenticationLabel.setLayoutData(authenticationGridData);
		authenticationLabel.setText("Authentication");

		final Label usernameLabel = new Label(container, PUSH);
		final GridData usernameGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		usernameGridData.horizontalSpan = 1;
		usernameLabel.setLayoutData(usernameGridData);
		usernameLabel.setText("User: ");

		user = new Text(container, BORDER | SINGLE);
		user.setLayoutData(new GridData(FILL_HORIZONTAL));
		user.setText("");
		user.addModifyListener(this);

		final Label passwordLabel = new Label(container, PUSH);
		final GridData passwordGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		passwordGridData.horizontalSpan = 1;
		passwordLabel.setLayoutData(passwordGridData);
		passwordLabel.setText("Password: ");

		password = new Text(container, BORDER | SINGLE | PASSWORD);
		password.setLayoutData(new GridData(FILL_HORIZONTAL));
		password.setText("");
		password.addModifyListener(this);

		secureCheckbox = new Button(container, CHECK);
		final GridData secureCheckboxGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		secureCheckboxGridData.horizontalSpan = 2;
		secureCheckbox.setLayoutData(secureCheckboxGridData);
		secureCheckbox.setText("Store in Secure Store");
		secureCheckbox.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				that.updatePageState();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});

		container.pack();
	}

	@Override
	protected void generateMessageErrors() {
		if (!this.getUri().isEmpty() && !this.hasValidGitURIFormat(this.getUri())) {
			this.addMessageError("Please, enter a valid Git repository URI.");
		}

		if (!this.getUsername().isEmpty() && this.getPassword().isEmpty()) {
			this.addMessageError("Please, enter a password for Git repository authentication.");
		}

		if (this.getUsername().isEmpty() && !this.getPassword().isEmpty()) {
			this.addMessageError("Please, enter a username for Git repository authentication.");
		}

		if (this.isSecureStoreEnable() && !(!this.getUsername().isEmpty() || !this.getPassword().isEmpty())) {
			this.addMessageError("Please, enter username and password for secure store Git repository authentication.");
		}
	}

	@Override
	public boolean canFlipToNextPage() {
		return super.canFlipToNextPage() && !this.getUri().isEmpty();
	}

	@Override
	protected void onPageInit() {
		this.createLocationComposite(container);
		this.createAuthenticationComposite(container);
	}

	@Override
	protected void onPageShow() {
		if (hasValidGitURIFormat(this.getClipboardContentText())) {
			uri.setText(this.getClipboardContentText());
		}
	}

	@Override
	protected void onPageFinish() {

	}

}
