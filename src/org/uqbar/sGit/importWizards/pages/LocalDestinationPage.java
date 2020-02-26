package org.uqbar.sGit.importWizards.pages;

import static org.eclipse.swt.SWT.*;
import static org.eclipse.swt.layout.GridData.*;
import static org.uqbar.sGit.importWizards.pages.Messages.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.uqbar.sGit.exceptions.FailedToReadProjectDescriptionFileException;
import org.uqbar.sGit.exceptions.NoConnectionWithRemoteException;
import org.uqbar.sGit.exceptions.NotAuthorizedException;
import org.uqbar.sGit.exceptions.ProjectAlreadyExistsException;
import org.uqbar.sGit.exceptions.SgitException;
import org.uqbar.sGit.utils.SecureStore;
import org.uqbar.sGit.utils.git.GitRepositoryUtils;
import org.uqbar.sGit.utils.git.SecureStoredUserPasswordCredentials;
import org.uqbar.sGit.utils.git.UserPasswordCredentials;
import org.uqbar.sGit.views.Dialogs;

public class LocalDestinationPage extends SGitWizardPage {

	private GitRepositoryUtils gitRepository;
	private Dialogs view = new Dialogs();
	private String repositoryName = "project"; //$NON-NLS-1$
	private Composite destinationComposite;
	private Composite branchesComposite;
	private Button defaultDirectoryCheckbox;
	private Text directory;
	private Button browse;
	private Combo branches;

	public LocalDestinationPage(String pageName) {
		super(pageName, LOCAL_DESTINATION_PAGE_TITLE);
	}

	public List<String> getBranchesItems() {
		return Arrays.asList(branches.getItems());
	}

	public String getSelectedBranchName() {
		return branches.getText();
	}

	public String getDirectory() {
		return this.directory.getText();
	}

	public Boolean isDirectoryEmpty() {
		return this.getDirectory().isEmpty();
	}

	public Boolean isBranchesEmpty() {
		return this.getBranchesItems().isEmpty();
	}

	public String getUsername() {
		return this.getPreviousPageAs(SourceGitRepositoryPage.class).getUsername();
	}

	public String getPassword() {
		return this.getPreviousPageAs(SourceGitRepositoryPage.class).getPassword();
	}

	public String getUri() {
		return this.getPreviousPageAs(SourceGitRepositoryPage.class).getUri();
	}

	public Boolean hasAuthentication() {
		return this.getPreviousPageAs(SourceGitRepositoryPage.class).hasAuthentication();
	}

	public Boolean isSecureStoreEnable() {
		return this.getPreviousPageAs(SourceGitRepositoryPage.class).isSecureStoreEnable();
	}

	protected void setDirectoryWithDefaultPath() {
		directory.setText(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString());
	}

	public String getRepositoryName() {
		return repositoryName;
	}

	private void throwCustomException(Exception exception) throws Exception {
		if (exception.getMessage().endsWith(": cannot open git-upload-pack")) { //$NON-NLS-1$
			throw new NoConnectionWithRemoteException();
		}
		if (exception.getMessage().startsWith("Destination path") && exception.getMessage().endsWith("already exists and is not an empty directory")) { //$NON-NLS-1$ //$NON-NLS-2$
			throw new ProjectAlreadyExistsException(this.getDirectory(), this.getRepositoryName());
		}
		if (exception.getMessage().startsWith("Failed to read project description file from location ")) { //$NON-NLS-1$
			throw new FailedToReadProjectDescriptionFileException(this.getDirectory(), this.getRepositoryName());
		}
		if (exception.getMessage().endsWith(": not authorized")) { //$NON-NLS-1$
			throw new NotAuthorizedException();
		}
		throw new SgitException(exception.getMessage());
	}

	private void updateDescriptionWithReposityName() {
		this.setDescription(LOCAL_DESTINATION_PAGE_TITLE + this.getRepositoryName());
	}

	private void fetchBranchesFromRemote() {
		try {
			branches.setItems(gitRepository.getBranches(this.getUri(), new UserPasswordCredentials(this.getUsername(), this.getPassword())));
			branches.select(branches.indexOf(this.getBranchesItems().stream().filter(branch -> branch.equals("master")).findFirst().get())); //$NON-NLS-1$
		}

		catch (Exception error) {
			try {
				this.throwCustomException(error);
			}

			catch (Exception customError) {
				this.addMessageError(customError.getMessage());
				this.setAllCompositeEnabled(false);
				this.setPageComplete(false);
			}
		}

	}

	protected Boolean directoryHasAValidPath() {
		return this.directoryHasAValidPath(this.getDirectory());
	}

	protected Boolean directoryHasAValidPath(String directory) {
		File path = new File(directory);
		return path != null && path.isDirectory();
	}

	protected void setCompositeEnabled(Composite composite, boolean enabled) {
		Arrays.asList(composite.getChildren()).stream().forEach(children -> children.setEnabled(enabled));
	}

	protected void setAllCompositeEnabled(boolean enabled) {
		this.setCompositeEnabled(destinationComposite, enabled);
		this.setCompositeEnabled(branchesComposite, enabled);
	}

	private void createDestinationComposite(Composite parent) {
		final LocalDestinationPage that = this;
		destinationComposite = new Composite(parent, NULL);
		final GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		destinationComposite.setLayout(layout);
		destinationComposite.setLayoutData(new GridData(FILL_HORIZONTAL));

		final Label destinationLabel = new Label(destinationComposite, PUSH);
		final GridData destinationGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		destinationGridData.horizontalSpan = 3;
		destinationLabel.setLayoutData(destinationGridData);
		destinationLabel.setText(DESTINATION);

		defaultDirectoryCheckbox = new Button(destinationComposite, CHECK);
		final GridData defaultDirectoryCheckboxGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		defaultDirectoryCheckboxGridData.horizontalSpan = 3;
		defaultDirectoryCheckbox.setLayoutData(defaultDirectoryCheckboxGridData);
		defaultDirectoryCheckbox.setText(USE_DEFAULT_LOCATION);
		defaultDirectoryCheckbox.setSelection(true);

		final Label directoryLabel = new Label(destinationComposite, PUSH);
		final GridData directoryGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		directoryGridData.horizontalSpan = 1;
		directoryLabel.setLayoutData(directoryGridData);
		directoryLabel.setText(DIRECTORY);

		directory = new Text(destinationComposite, BORDER | SINGLE);
		directory.setLayoutData(new GridData(FILL_HORIZONTAL));
		directory.setText(""); //$NON-NLS-1$
		directory.addModifyListener(this);

		browse = new Button(destinationComposite, PUSH);
		final GridData browseGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		browseGridData.horizontalSpan = 1;
		browse.setLayoutData(browseGridData);
		browse.setText(BROWSE);

		defaultDirectoryCheckbox.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (defaultDirectoryCheckbox.getSelection()) {
					directory.setEnabled(false);
					browse.setEnabled(false);
				}

				else {
					directory.setEnabled(true);
					browse.setEnabled(true);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});

		browse.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(that.getShell(), NULL);
				String path = dialog.open();
				
				if (path != null) {
					directory.setText(path);
				}
				
				that.updatePageState();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});

		destinationComposite.pack();
	}

	private void createBranchesComposite(Composite parent) {
		branchesComposite = new Composite(parent, NULL);
		final GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		branchesComposite.setLayout(layout);
		branchesComposite.setLayoutData(new GridData(FILL_HORIZONTAL));

		final Label branchesLabel = new Label(branchesComposite, PUSH);
		final GridData branchesGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		branchesGridData.horizontalSpan = 3;
		branchesLabel.setLayoutData(branchesGridData);
		branchesLabel.setText(BRANCHES);

		final Label initialBranchLabel = new Label(branchesComposite, PUSH);
		final GridData initialBranchGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		initialBranchGridData.horizontalSpan = 1;
		initialBranchLabel.setLayoutData(initialBranchGridData);
		initialBranchLabel.setText(INITIAL_BRANCH);

		branches = new Combo(branchesComposite, DROP_DOWN | READ_ONLY);
		final GridData branchsGridData = new GridData(FILL_HORIZONTAL);
		branchsGridData.horizontalSpan = 2;
		branches.setLayoutData(branchsGridData);
		branches.setItems(new String[] {});
		branches.addModifyListener(this);

		branchesComposite.pack();
	}

	@Override
	protected void generateMessageErrors() {
		if (!(this.directoryHasAValidPath())) {
			this.addMessageError(ENTER_VALID_DIRECTORY + this.getRepositoryName());
		}
	}

	private void secureCrendentials(String user, String password) {
		SecureStore secureStore = SecureStore.getInstance();
		secureStore.secure("credentials", "user", user); //$NON-NLS-1$ //$NON-NLS-2$
		secureStore.secure("credentials", "password", password); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected void onPageInit() {
		this.createDestinationComposite(container);
		this.createBranchesComposite(container);
	}

	private void showCloneErrorDialog(String message) {
		view.showErrorDialog(CloneProjectErrorTitle, message);
	}

	private void showRemoveClonedRepositoryQuestionDialog(String message) {
		boolean keep = view.showQuestionDialog(ImportProjectErrorTitle, message + "\n \n" + ImportProjectErrorMessage); //$NON-NLS-1$
		
		if (!keep) {
			java.nio.file.Path path = Paths.get(this.getDirectory() + "/git/" + repositoryName); //$NON-NLS-1$

			try {
				Files.walk(path).sorted(Comparator.reverseOrder()).map(java.nio.file.Path::toFile).forEach(File::delete);
			}

			catch (IOException e) {
				view.showErrorDialog(DeleteDirectoryErrorMessage + path.toUri().toString(), ErrorMessage_5);
			}
		}
	}

	private Boolean cloneProject() {
		Boolean cloned = true;
		
		try {
			UserPasswordCredentials credentials = this.isSecureStoreEnable() ? new SecureStoredUserPasswordCredentials(): new UserPasswordCredentials(this.getUsername(), this.getPassword());
			gitRepository.cloneRepository(this.getDirectory() + "/git", this.getUri(), this.getSelectedBranchName(), credentials); //$NON-NLS-1$
		}

		catch (FileNotFoundException e) {
			// Do nothing. Throwing .project file not found is not necessary.
			// Clone will be done successfully anyway.
			// .project file not found validation will be performed on open project step.
		}

		catch (Exception error) {
			try {
				this.throwCustomException(error);
			}

			catch (Exception e) {
				cloned = false;
				this.showCloneErrorDialog(e.getMessage());
			}
		}
		return cloned;
	}
	
	private void importProject() {
		try {
			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			final Path path = new Path(this.getDirectory() + "/git/" + repositoryName + "/.project"); //$NON-NLS-1$ //$NON-NLS-2$
			final IProjectDescription description = workspace.loadProjectDescription(path);
			final IProject project = workspace.getRoot().getProject(description.getName());
			final IProgressMonitor monitor = new NullProgressMonitor();
			project.create(description, monitor);
			project.open(monitor);
		}

		catch (CoreException error) {
			try {
				this.throwCustomException(error);
			}

			catch (Exception customError) {
				this.showRemoveClonedRepositoryQuestionDialog(customError.getMessage());
			}
		}
	}

	@Override
	protected void onPageShow() {
		gitRepository = new GitRepositoryUtils();
		repositoryName = gitRepository.getRepositoryName(this.getUri());
		this.updateDescriptionWithReposityName();
		this.setAllCompositeEnabled(true);
		this.setDirectoryWithDefaultPath();
		defaultDirectoryCheckbox.setSelection(true);
		directory.setEnabled(false);
		browse.setEnabled(false);
		branches.removeAll();
		this.setPageComplete(true);
		this.fetchBranchesFromRemote();
	}

	@Override
	protected void onPageFinish() {
		if (this.isSecureStoreEnable()) {
			this.secureCrendentials(this.getUsername(), this.getPassword());
		}

		if (this.cloneProject()) {
			this.importProject();
		}
	}

}
