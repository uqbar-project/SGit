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
import org.uqbar.sGit.utils.Clipboard;

public class SourceGitRepositoryPage extends SGitWizardPage {

	private Text uri;
	private Text user;
	private Text password;
	private Button secureCheckbox;

	public SourceGitRepositoryPage(String pageName) {
		super(pageName, Messages.SOURCE_TITLE);
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
		return path.startsWith("https://") && path.endsWith(".git"); //$NON-NLS-1$ //$NON-NLS-2$
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
		locationLabel.setText(Messages.LOCATION);

		final Label uriLabel = new Label(container, PUSH);
		final GridData uriGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		uriGridData.horizontalSpan = 1;
		uriLabel.setLayoutData(uriGridData);
		uriLabel.setText("URI: "); //$NON-NLS-1$

		uri = new Text(container, BORDER | SINGLE);
		uri.setLayoutData(new GridData(FILL_HORIZONTAL));
		uri.setText(""); //$NON-NLS-1$
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
		authenticationLabel.setText(Messages.AUTHENTICATION);

		final Label usernameLabel = new Label(container, PUSH);
		final GridData usernameGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		usernameGridData.horizontalSpan = 1;
		usernameLabel.setLayoutData(usernameGridData);
		usernameLabel.setText(Messages.USER);

		user = new Text(container, BORDER | SINGLE);
		user.setLayoutData(new GridData(FILL_HORIZONTAL));
		user.setText(""); //$NON-NLS-1$
		user.addModifyListener(this);

		final Label passwordLabel = new Label(container, PUSH);
		final GridData passwordGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		passwordGridData.horizontalSpan = 1;
		passwordLabel.setLayoutData(passwordGridData);
		passwordLabel.setText(Messages.PASSWORD);

		password = new Text(container, BORDER | SINGLE | PASSWORD);
		password.setLayoutData(new GridData(FILL_HORIZONTAL));
		password.setText(""); //$NON-NLS-1$
		password.addModifyListener(this);

		secureCheckbox = new Button(container, CHECK);
		final GridData secureCheckboxGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		secureCheckboxGridData.horizontalSpan = 2;
		secureCheckbox.setLayoutData(secureCheckboxGridData);
		secureCheckbox.setText(Messages.STORE_IN_SECURE_STORE);
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
			this.addMessageError(Messages.ERROR_ENTER_VALID_GIT_URI);
		}

		if (!this.getUsername().isEmpty() && this.getPassword().isEmpty()) {
			this.addMessageError(Messages.ERROR_ENTER_PASSWORD);
		}

		if (this.getUsername().isEmpty() && !this.getPassword().isEmpty()) {
			this.addMessageError(Messages.ERROR_ENTER_USERNAME);
		}

		if (this.isSecureStoreEnable() && !(!this.getUsername().isEmpty() || !this.getPassword().isEmpty())) {
			this.addMessageError(Messages.ERROR_ENTER_USERNAME_AND_PASSWORD);
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
		String clipboardContent = Clipboard.getContent();
		uri.setText(hasValidGitURIFormat(clipboardContent) ? clipboardContent : "");
	}

	@Override
	protected void onPageFinish() {

	}

}
