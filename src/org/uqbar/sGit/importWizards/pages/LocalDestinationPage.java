package org.uqbar.sGit.importWizards.pages;

import static org.eclipse.swt.SWT.*;
import static org.eclipse.swt.layout.GridData.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jgit.api.errors.GitAPIException;
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
import org.uqbar.sGit.model.repository.GitRepository;
import org.uqbar.sGit.model.repository.credentials.SecureStoredCredentialsManager;

public class LocalDestinationPage extends SGitWizardPage {

	private GitRepository gitRepository;
	private String repositoryName = "project"; //$NON-NLS-1$
	private Composite destinationComposite;
	private Composite branchesComposite;
	private Button defaultDirectoryCheckbox;
	private Text directory;
	private Button browse;
	private Combo branches;
	
	public LocalDestinationPage(String pageName) {
		super(pageName, Messages.LOCAL_DESTINATION_PAGE_TITLE);
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

	private void updateDescriptionWithReposityName() {
		this.setDescription(Messages.LOCAL_DESTINATION_PAGE_TITLE + this.getRepositoryName());
	}

	private void fetchBranchesFromRemote() {
		try {
			branches.setItems(gitRepository.getBranches(this.getUri()));
			branches.select(branches.indexOf(
					this.getBranchesItems()
					.stream()
					.filter(branch -> branch.equals("master")).findFirst().get()));
		}

		catch (Exception e) {
			this.addMessageError(e.getMessage());
			this.setAllCompositeEnabled(false);
			this.setPageComplete(false);
		}

	}

	protected Boolean directoryHasAValidPath() {
		File path = new File(directory.getText());
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
		destinationLabel.setText(Messages.DESTINATION);

		defaultDirectoryCheckbox = new Button(destinationComposite, CHECK);
		final GridData defaultDirectoryCheckboxGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		defaultDirectoryCheckboxGridData.horizontalSpan = 3;
		defaultDirectoryCheckbox.setLayoutData(defaultDirectoryCheckboxGridData);
		defaultDirectoryCheckbox.setText(Messages.USE_DEFAULT_LOCATION);
		defaultDirectoryCheckbox.setSelection(true);

		final Label directoryLabel = new Label(destinationComposite, PUSH);
		final GridData directoryGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		directoryGridData.horizontalSpan = 1;
		directoryLabel.setLayoutData(directoryGridData);
		directoryLabel.setText(Messages.DIRECTORY);

		directory = new Text(destinationComposite, BORDER | SINGLE);
		directory.setLayoutData(new GridData(FILL_HORIZONTAL));
		directory.setText("");
		directory.addModifyListener(this);

		browse = new Button(destinationComposite, PUSH);
		final GridData browseGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		browseGridData.horizontalSpan = 1;
		browse.setLayoutData(browseGridData);
		browse.setText(Messages.BROWSE);

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
		branchesLabel.setText(Messages.BRANCHES);

		final Label initialBranchLabel = new Label(branchesComposite, PUSH);
		final GridData initialBranchGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		initialBranchGridData.horizontalSpan = 1;
		initialBranchLabel.setLayoutData(initialBranchGridData);
		initialBranchLabel.setText(Messages.INITIAL_BRANCH);

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
			this.addMessageError(Messages.ENTER_VALID_DIRECTORY + this.getRepositoryName());
		}
	}
	
	private void secureCrendentials(String user, String password) {
		SecureStoredCredentialsManager.getInstance().secureCrendetials(user, password);
	}

	@Override
	protected void onPageInit() {
		this.createDestinationComposite(container);
		this.createBranchesComposite(container);
	}

	@Override
	protected void onPageShow() {
		gitRepository = new GitRepository(this.getUsername(), this.getPassword());
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

	private void cloneRepository() {
			try {
				gitRepository.cloneRepository(this.getDirectory() + "/git", this.getUri(), this.getSelectedBranchName());
			} 
			
			catch (GitAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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

		catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onPageFinish() {
		if (this.isSecureStoreEnable()) {
			this.secureCrendentials(this.getUsername(), this.getPassword());
		}
		this.cloneRepository();
		this.importProject();
	}

}
