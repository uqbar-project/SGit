package org.uqbar.sGit.views;

import static org.eclipse.swt.SWT.*;
import static org.eclipse.swt.layout.GridData.*;
import static org.uqbar.sGit.views.Messages.*;

import java.net.URL;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.uqbar.sGit.exceptions.MergeConflictsException;
import org.uqbar.sGit.exceptions.NoConnectionWithRemoteException;
import org.uqbar.sGit.exceptions.NotAuthorizedException;
import org.uqbar.sGit.exceptions.SgitException;
import org.uqbar.sGit.utils.GitFile;

public class GitView extends SGitView implements ModifyListener {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.uqbar.sGit.views.GitView"; //$NON-NLS-1$

	private Label unstagingFilesLabel;
	private Label stagingFilesLabel;
	private Table unstagedFiles;
	private Table stagedFiles;
	private ToolItem addItem;
	private ToolItem addAllItem;
	private ToolItem removeItem;
	private ToolItem removeAllItem;
	private Text commitMessageTexbox;
	private Combo authorCombo;
	private String authorName;
	private String authorEmail;
	private Button commit;
	private Button pull;
	private Button push;
	private Button commitAndPush;
	
	private void validateErrorMessage(String message) {
		if (message.contains("not authorized")) {
			view.showErrorDialog("Git", new NotAuthorizedException().getMessage());
		}

		else if (message.contains("cannot open git-receive-pack")) {
			view.showErrorDialog("Git", new NoConnectionWithRemoteException().getMessage());
		}
		
		else if (message.contains("Existen conflictos entre el repositorio local y el remoto")) {
			view.showWarningDialog("Git", new MergeConflictsException().getMessage());
		}
		
		else {
			view.showErrorDialog("Error", new SgitException(message).getMessage());
		}
	}

	private Image getImage(String name) {
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		URL url = FileLocator.find(bundle, new Path("icons/" + name + ".png"), null);
		ImageDescriptor imageDesc = ImageDescriptor.createFromURL(url);
		Image image = imageDesc.createImage();
		return image;
	}

	private void showOnTable(GitFile file, Table table) {
		TableItem item = new TableItem(table, 0);
		item.setImage(this.getImage(file.getStatusImageName()));
		item.setText(file.getFilePath());
	}

	/**
	 * add a Git file wrapper to the non staging area.
	 */
	private void showAsUnstaged(GitFile file) {
		this.showOnTable(file, unstagedFiles);
	}

	/**
	 * add a Git file wrapper to the staging area.
	 */
	private void showAsStaged(GitFile file) {
		this.showOnTable(file, stagedFiles);
	}

	/**
	 * Updates staging state.
	 */
	private void updateStagingState() {
		unstagedFiles.removeAll();
		stagedFiles.removeAll();
		gitRepository.getUnstagedFiles().stream().forEach(this::showAsUnstaged);
		gitRepository.getStagedFiles().stream().forEach(this::showAsStaged);
		unstagingFilesLabel.setText(UNSTAGED_CHANGES + unstagedFiles.getItemCount());
		stagingFilesLabel.setText(STAGED_CHANGES + stagedFiles.getItemCount());
	}

	/**
	 * Updates committer box state.
	 */
	private void updateCommitDetailsState() {

		Set<String> authorsName = gitRepository.getAuthors().stream().map(a -> a.getName()).collect(Collectors.toSet());
		authorCombo.setItems(authorsName.toArray(new String[0]));
		authorCombo.select(authorCombo.indexOf(gitRepository.getLastAuthor().getName()));

		if (authorCombo.getItemCount() > 0) {
			PersonIdent author = gitRepository.getAuthors().stream().filter(a -> a.getName().equals(authorCombo.getText())).findFirst().get();
			authorName = author.getName();
			authorEmail = author.getEmailAddress();
		}
		commitMessageTexbox.setText("");

	}

	/**
	 * Perform git add to staging Action.
	 */
	private void add(String filePath) {
		try {
			gitRepository.addFileToStaging(filePath);
		} 
		
		catch (Exception e) {
			this.validateErrorMessage(e.getMessage());
		}
	}

	/**
	 * Perform git add all to staging Action.
	 */
	private void addAll() {
		Arrays.asList(unstagedFiles.getItems()).stream().forEach(item -> this.add(item.getText()));
	}

	/**
	 * Perform git remove from staging Action.
	 */
	private void remove(String filePath) {
		try {
			gitRepository.removeFileFromStaging(filePath);
		} 
		
		catch (Exception e) {
			this.validateErrorMessage(e.getMessage());
		}
	}

	/**
	 * Perform git remove all from staging Action.
	 */
	private void removeAll() {
		Arrays.asList(stagedFiles.getItems()).stream().forEach(item -> this.remove(item.getText()));
	}

	/**
	 * Perform a Git commit Action.
	 * 
	 * @param message: The commit message.
	 * @param author: The author name.
	 * @param email: the author email.
	 * @param committer name.
	 * @param committerEmail the committer email.
	 */
	private void commit(String message, String author, String email) {
		try {
			gitRepository.commit(message, author, email);
			this.update();
		} 
		
		catch (Exception e) {
			this.validateErrorMessage(e.getMessage());
		}
	}
	
	/**
	 * Perform a Git push Action.
	 */
	private void push() {
		try {
			gitRepository.push();
			this.updateStagingState();
		}

		catch (Exception e) {
			this.validateErrorMessage(e.getMessage());
		}
	}

	/**
	 * Perform a Git pull Action.
	 */
	private void pull() {
		try {
			gitRepository.pull();
			this.updateStagingState();
		} 
		
		catch (Exception e) {
			this.validateErrorMessage(e.getMessage());
		}
	}

	public void onViewInit() {
		final GitView that = this;
		final SashForm gitStagingSashForm = new SashForm(container, HORIZONTAL);
		final SashForm addRemoveSashForm = new SashForm(gitStagingSashForm, VERTICAL);

		final Composite unstagingComposite = new Composite(addRemoveSashForm, NULL);
		final GridLayout unstagingCompositeLayout = new GridLayout();
		unstagingComposite.setLayout(unstagingCompositeLayout);
		unstagingComposite.setLayoutData(new GridData(FILL_HORIZONTAL));

		final Composite unstagingToolbarComposite = new Composite(unstagingComposite, NULL);
		final GridLayout unstagingToolbarCompositeCompositeLayout = new GridLayout();
		unstagingToolbarCompositeCompositeLayout.numColumns = 2;
		unstagingToolbarComposite.setLayout(unstagingToolbarCompositeCompositeLayout);
		unstagingToolbarComposite.setLayoutData(new GridData(FILL_HORIZONTAL));

		unstagingFilesLabel = new Label(unstagingToolbarComposite, PUSH);
		unstagingFilesLabel.setLayoutData(new GridData(HORIZONTAL_ALIGN_FILL));
		unstagingFilesLabel.setText(UNSTAGED_CHANGES + 0);

		final ToolBar unstagingActionToolBar = new ToolBar(unstagingToolbarComposite, BORDER);
		unstagingActionToolBar.setLayoutData(new GridData(HORIZONTAL_ALIGN_FILL | SEPARATOR_FILL));

		addItem = new ToolItem(unstagingActionToolBar, PUSH);
		addItem.setImage(this.getImage("add"));
		addItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (unstagedFiles.getSelectionIndex() >= 0) {
					Arrays.asList(unstagedFiles.getSelection()).stream().forEach(item -> that.add(item.getText()));
					that.updateStagingState();
					that.enableCommitIfCanMakeACommit();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});

		addAllItem = new ToolItem(unstagingActionToolBar, PUSH);
		addAllItem.setImage(this.getImage("add_all"));
		addAllItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				that.addAll();
				that.updateStagingState();
				that.enableCommitIfCanMakeACommit();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});

		unstagedFiles = new Table(unstagingComposite, PUSH | MULTI | BORDER | H_SCROLL | V_SCROLL | WRAP);
		unstagedFiles.setLayoutData(new GridData(FILL_BOTH));

		Composite stagingComposite = new Composite(addRemoveSashForm, NULL);
		final GridLayout stagingCompositeLayout = new GridLayout();
		stagingComposite.setLayout(stagingCompositeLayout);
		stagingComposite.setLayoutData(new GridData(FILL_HORIZONTAL));

		Composite stagingToolbarComposite = new Composite(stagingComposite, NULL);
		final GridLayout stagingToolbarCompositeCompositeLayout = new GridLayout();
		stagingToolbarCompositeCompositeLayout.numColumns = 2;
		stagingToolbarComposite.setLayout(stagingToolbarCompositeCompositeLayout);
		stagingToolbarComposite.setLayoutData(new GridData(FILL_HORIZONTAL));

		stagingFilesLabel = new Label(stagingToolbarComposite, PUSH);
		stagingFilesLabel.setLayoutData(new GridData(FILL_HORIZONTAL));
		stagingFilesLabel.setText(STAGED_CHANGES + 0);

		ToolBar stagingActionToolBar = new ToolBar(stagingToolbarComposite, BORDER);
		stagingActionToolBar.setLayoutData(new GridData(HORIZONTAL_ALIGN_FILL | SEPARATOR_FILL));

		removeItem = new ToolItem(stagingActionToolBar, PUSH);
		removeItem.setImage(this.getImage("unstage"));
		removeItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (stagedFiles.getSelectionIndex() >= 0) {
					Arrays.asList(stagedFiles.getSelection()).stream().forEach(item -> that.remove(item.getText()));
					that.updateStagingState();
					that.enableCommitIfCanMakeACommit();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});

		removeAllItem = new ToolItem(stagingActionToolBar, PUSH);
		removeAllItem.setImage(this.getImage("unstage_all"));
		removeAllItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				that.removeAll();
				that.updateStagingState();
				that.enableCommitIfCanMakeACommit();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});

		stagedFiles = new Table(stagingComposite, PUSH | MULTI | BORDER | H_SCROLL | V_SCROLL | WRAP);
		stagedFiles.setLayoutData(new GridData(FILL_BOTH));

		// Committer Section.

		final Composite commiterContainer = new Composite(gitStagingSashForm, BORDER | VERTICAL);
		final GridLayout commiterlayout = new GridLayout();
		commiterContainer.setLayout(commiterlayout);

		final Label commitLabel = new Label(commiterContainer, NULL);
		commitLabel.setText(COMMIT_MESSAGE);
		commitLabel.setLayoutData(new GridData(FILL_HORIZONTAL));

		commitMessageTexbox = new Text(commiterContainer, BORDER | H_SCROLL | V_SCROLL | WRAP);
		commitMessageTexbox.setLayoutData(new GridData(FILL_BOTH));
		commitMessageTexbox.addModifyListener(this);

		final Label authorLabel = new Label(commiterContainer, NULL);
		authorLabel.setText(AUTHOR);
		authorLabel.setLayoutData(new GridData(FILL_HORIZONTAL));

		authorCombo = new Combo(commiterContainer, DROP_DOWN);
		final GridData authorComboComboGridData = new GridData(FILL_HORIZONTAL);
		authorComboComboGridData.horizontalSpan = 2;
		authorCombo.setLayoutData(authorComboComboGridData);
		authorCombo.setItems(new String[] {});

		Composite commiterButtonsComposite = new Composite(commiterContainer, RIGHT_TO_LEFT);
		final GridLayout commiterButtonsCompositeLayout = new GridLayout();
		commiterButtonsCompositeLayout.numColumns = 4;
		commiterButtonsComposite.setLayout(commiterButtonsCompositeLayout);
		commiterButtonsComposite.setLayoutData(new GridData(FILL_HORIZONTAL));

		commitAndPush = new Button(commiterButtonsComposite, PUSH);
		commitAndPush.setLayoutData(new GridData(HORIZONTAL_ALIGN_FILL));
		commitAndPush.setText(COMMIT_AND_PUSH_ACTION);
		commitAndPush.setImage(this.getImage("commitandpush"));
		commitAndPush.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				that.commit(commitMessageTexbox.getText(), authorName, authorEmail);
				that.push();
				that.update();
				that.view.refreshWorkspace();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});

		push = new Button(commiterButtonsComposite, PUSH);
		push.setLayoutData(new GridData(HORIZONTAL_ALIGN_FILL));
		push.setText(PUSH_ACTION);
		push.setImage(this.getImage("push"));
		push.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				that.push();
				that.update();
				that.view.refreshWorkspace();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});

		pull = new Button(commiterButtonsComposite, PUSH);
		pull.setLayoutData(new GridData(HORIZONTAL_ALIGN_FILL));
		pull.setText(PULL_ACTION);
		pull.setImage(this.getImage("pull"));
		pull.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				that.pull();
				that.update();
				that.view.refreshWorkspace();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});

		commit = new Button(commiterButtonsComposite, PUSH);
		commit.setLayoutData(new GridData(HORIZONTAL_ALIGN_FILL));
		commit.setImage(this.getImage("commit"));
		commit.setText(COMMIT_ACTION);
		commit.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				that.commit(commitMessageTexbox.getText(), authorName, authorEmail);
				that.update();
				that.view.refreshWorkspace();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});
		
		this.disableStagingArea();
		this.disableCommitterArea();
		this.disableCommitButton();
		this.disablePullButton();
		this.disablePushButton();
		this.disableCommitAndPushButton();
	}
	
	protected void update(){
		this.updateStagingState();
		this.updateCommitDetailsState();
	}

	@Override
	protected void onFocus() {
		if (gitRepository != null) {
			this.updateStagingState();
		}
	}

	@Override
	protected void onUpdate() {
		if(this.isRepositoryAlreadyInitializated()) {
			this.update();
			this.enableStagingArea();
			this.enableCommitterArea();
			this.enablePullButton();
			this.enablePushButton();
		}
	}
	
	private boolean isRepositoryAlreadyInitializated(){
		return this.gitRepository != null;
	}

	private void disableCommitButton() {
		this.commit.setEnabled(false);
	}

	private void enableCommitButton() {
		this.commit.setEnabled(true);
	}
	
	private void disableCommitAndPushButton() {
		this.commitAndPush.setEnabled(false);
	}

	private void enableCommitAndPushButton() {
		this.commitAndPush.setEnabled(true);
	}
	
	private void disablePullButton() {
		this.pull.setEnabled(false);
	}

	private void enablePullButton() {
		this.pull.setEnabled(true);
	}
	
	private void disablePushButton() {
		this.push.setEnabled(false);
	}

	private void enablePushButton() {
		this.push.setEnabled(true);
	}
	
	private void disableStagingArea() {
		this.addItem.setEnabled(false);
		this.addAllItem.setEnabled(false);
		this.removeItem.setEnabled(false);
		this.removeAllItem.setEnabled(false);
	}

	private void enableStagingArea() {
		this.addItem.setEnabled(true);
		this.addAllItem.setEnabled(true);
		this.removeItem.setEnabled(true);
		this.removeAllItem.setEnabled(true);
	}
	
	private void disableCommitterArea() {
		this.commitMessageTexbox.setEnabled(false);
		this.authorCombo.setEnabled(false);
	}

	private void enableCommitterArea() {
		this.commitMessageTexbox.setEnabled(true);
		this.authorCombo.setEnabled(true);
	}

	private boolean canMakeACommit() {
		return this.isRepositoryAlreadyInitializated() && this.stagedFiles.getItemCount() > 0 && this.commitMessageTexbox.getText().length() > 0;
	}
	
	private void enableCommitIfCanMakeACommit(){
		if (this.canMakeACommit()) {
			this.enableCommitButton();
			this.enableCommitAndPushButton();
		}
		
		else {
			this.disableCommitButton();
			this.disableCommitAndPushButton();
		}
	}
	
	@Override
	public void modifyText(ModifyEvent e) {
		this.enableCommitIfCanMakeACommit();
	}

}
