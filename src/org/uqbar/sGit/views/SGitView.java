package org.uqbar.sGit.views;

import static org.eclipse.swt.SWT.*;
import static org.eclipse.swt.layout.GridData.*;
import static org.uqbar.sGit.views.Messages.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.uqbar.sGit.utils.FileLocator;
import org.uqbar.sGit.utils.git.GitFile;
import org.uqbar.sGit.views.Dialogs.NewAuthorDialog;

public class SGitView extends View {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.uqbar.sGit.views.GitView"; //$NON-NLS-1$

	private Label tipLabel;
	private Label projectLabel;
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
	private Button commit;
	private Button pull;
	private Button push;
	private Button commitAndPush;

	private void showOnTable(GitFile file, Table table) {
		TableItem item = new TableItem(table, 0);
		item.setImage(FileLocator.getImage(file.getStatusImageName(), this));
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

	private Set<PersonIdent> getAuthors() {
		Set<PersonIdent> authors = new HashSet<PersonIdent>();
		this.personIdentConsumerAction.setPersonIndentConsumer(authors::add);
		this.personIdentConsumerAction.run();
		return authors;
	}

	/**
	 * Updates staging state.
	 */
	private void updateStagingState() {
		unstagedFiles.removeAll();
		stagedFiles.removeAll();
		this.unstaginFileConsumerAction.setGitFileConsumer(this::showAsUnstaged);
		this.unstaginFileConsumerAction.run();
		this.staginFileConsumerAction.setGitFileConsumer(this::showAsStaged);
		this.staginFileConsumerAction.run();
		unstagingFilesLabel.setText(UNSTAGED_CHANGES + unstagedFiles.getItemCount());
		stagingFilesLabel.setText(STAGED_CHANGES + stagedFiles.getItemCount());
	}

	/**
	 * Updates committer box state.
	 */
	private void updateCommitDetailsState() {
		if (this.isActive) {
			Set<String> authorsName = this.getAuthors().stream().map(PersonIdent::getName).collect(Collectors.toSet());
			authorCombo.setItems(authorsName.toArray(new String[0]));
		}

		else {
			authorCombo.removeAll();
			commitMessageTexbox.setText(""); //$NON-NLS-1$
		}

	}

	/**
	 * Perform git add to staging Action.
	 */
	private void stage(String filePath) {
		this.stageFileAction.setFilePath(filePath);
		this.stageFileAction.run();
	}

	/**
	 * Perform git add all to staging Action.
	 */
	private void stageAll() {
		Arrays.asList(unstagedFiles.getItems()).stream().forEach(item -> this.stage(item.getText()));
	}

	/**
	 * Perform git remove from staging Action.
	 */
	private void unstage(String filePath) {
		this.unstageFileAction.setFilePath(filePath);
		this.unstageFileAction.run();
	}

	/**
	 * Perform git remove all from staging Action.
	 */
	private void unstageAll() {
		Arrays.asList(stagedFiles.getItems()).stream().forEach(item -> this.unstage(item.getText()));
	}

	/**
	 * Perform a Git commit Action.
	 */
	private void commit(String message, String author, String email) {
		this.commitAction.setExceptionHandler((exception) -> view.showErrorDialog("Commit Action", exception.getMessage())); //$NON-NLS-1$
		this.commitAction.setCommitDetails(message, author, email, author, email);
		this.commitAction.run();
	}

	/**
	 * Perform a Git push Action.
	 */
	private void push() {
		this.pushAction.setExceptionHandler((exception) -> view.showErrorDialog("Push Action", exception.getMessage())); //$NON-NLS-1$
		this.pushAction.run();
	}

	/**
	 * Perform a Git pull Action.
	 */
	private void pull() {
		this.pullAction.setExceptionHandler((exception) -> view.showErrorDialog("Pull Action", exception.getMessage())); //$NON-NLS-1$
		this.pullAction.run();
	}

	public void onViewInit() {
		final SGitView that = this;
		
		final SashForm gitStagingSashForm = new SashForm(container, HORIZONTAL);
		final SashForm addRemoveSashForm = new SashForm(gitStagingSashForm, VERTICAL);

		final Composite unstagingComposite = new Composite(addRemoveSashForm, PUSH);
		final GridLayout unstagingCompositeLayout = new GridLayout();
		unstagingComposite.setLayout(unstagingCompositeLayout);
		unstagingComposite.setLayoutData(new GridData(FILL_HORIZONTAL));
		
		final Composite unstagingToolbarComposite = new Composite(unstagingComposite, PUSH);
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
		addItem.setImage(FileLocator.getImage("add", this)); //$NON-NLS-1$
		addItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (unstagedFiles.getSelectionIndex() >= 0) {
					Arrays.asList(unstagedFiles.getSelection()).stream().forEach(item -> that.stage(item.getText()));
					that.updateStagingState();
					that.updateComponentsState();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});

		addAllItem = new ToolItem(unstagingActionToolBar, PUSH);
		addAllItem.setImage(FileLocator.getImage("add_all", this)); //$NON-NLS-1$
		addAllItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				that.stageAll();
				that.updateStagingState();
				that.updateComponentsState();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});

		unstagedFiles = new Table(unstagingComposite, PUSH | MULTI | BORDER | H_SCROLL | V_SCROLL | WRAP);
		unstagedFiles.setLayoutData(new GridData(FILL_BOTH));

		Composite stagingComposite = new Composite(addRemoveSashForm, PUSH);
		final GridLayout stagingCompositeLayout = new GridLayout();
		stagingComposite.setLayout(stagingCompositeLayout);
		stagingComposite.setLayoutData(new GridData(FILL_HORIZONTAL));
		
		Composite stagingToolbarComposite = new Composite(stagingComposite, PUSH);
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
		removeItem.setImage(FileLocator.getImage("unstage", this)); //$NON-NLS-1$
		removeItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (stagedFiles.getSelectionIndex() >= 0) {
					Arrays.asList(stagedFiles.getSelection()).stream().forEach(item -> that.unstage(item.getText()));
					that.updateStagingState();
					that.updateComponentsState();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});

		removeAllItem = new ToolItem(stagingActionToolBar, PUSH);
		removeAllItem.setImage(FileLocator.getImage("unstage_all", this)); //$NON-NLS-1$
		removeAllItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				that.unstageAll();
				that.updateStagingState();
				that.updateComponentsState();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});

		stagedFiles = new Table(stagingComposite, PUSH | MULTI | BORDER | H_SCROLL | V_SCROLL | WRAP);
		stagedFiles.setLayoutData(new GridData(FILL_BOTH));

		// Committer Section.

		final Composite commiterContainer = new Composite(gitStagingSashForm, VERTICAL);
		final GridLayout commiterlayout = new GridLayout();
		commiterContainer.setLayout(commiterlayout);
		
		projectLabel = new Label(commiterContainer, PUSH);
		projectLabel.setLayoutData(new GridData(FILL_HORIZONTAL));
		projectLabel.setText("");

		
		final Label commitLabel = new Label(commiterContainer, PUSH);
		commitLabel.setText(COMMIT_MESSAGE);
		commitLabel.setLayoutData(new GridData(FILL_HORIZONTAL));

		commitMessageTexbox = new Text(commiterContainer, BORDER | H_SCROLL | V_SCROLL | WRAP);
		commitMessageTexbox.setLayoutData(new GridData(FILL_BOTH));
		commitMessageTexbox.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				that.updateComponentsState();
			}
			
		});

		final Label authorLabel = new Label(commiterContainer, PUSH);
		authorLabel.setText(AUTHOR);
		authorLabel.setLayoutData(new GridData(FILL_HORIZONTAL));

		authorCombo = new Combo(commiterContainer, DROP_DOWN);
		final GridData authorComboComboGridData = new GridData(FILL_HORIZONTAL);
		authorComboComboGridData.horizontalSpan = 2;
		authorCombo.setLayoutData(authorComboComboGridData);
		authorCombo.setItems(new String[] {});
		
		authorCombo.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				that.updateComponentsState();
			}
			
		});
		
		tipLabel = new Label(commiterContainer, PUSH | WRAP);
		tipLabel.setText("");
		tipLabel.setLayoutData(new GridData(FILL_HORIZONTAL));

		final Composite commiterButtonsComposite = new Composite(commiterContainer, RIGHT_TO_LEFT | WRAP);
		final GridLayout commiterButtonsCompositeLayout = new GridLayout();
		commiterButtonsCompositeLayout.numColumns = 4;
		commiterButtonsComposite.setLayout(commiterButtonsCompositeLayout);
		commiterButtonsComposite.setLayoutData(new GridData(FILL_HORIZONTAL));

		commitAndPush = new Button(commiterButtonsComposite, PUSH);
		commitAndPush.setLayoutData(new GridData(HORIZONTAL_ALIGN_FILL));
		commitAndPush.setText(COMMIT_AND_PUSH_ACTION);
		commitAndPush.setImage(FileLocator.getImage("commitandpush", this)); //$NON-NLS-1$
		commitAndPush.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				PersonIdent author = that.getAuthors().stream().filter(a -> a.getName().equals(authorCombo.getText())).findFirst().orElse(null);

				if (author != null) {
					that.commit(commitMessageTexbox.getText(), author.getName(), author.getEmailAddress());
					that.pull();
					that.push();
				}

				else {
					NewAuthorDialog dialog = new NewAuthorDialog(null, authorCombo.getText());
					dialog.open();
					if (dialog.getName() != "" && dialog.getEmail() != "") { //$NON-NLS-1$ //$NON-NLS-2$
						that.commit(commitMessageTexbox.getText(), dialog.getName(), dialog.getEmail());
						that.pull();
						that.push();
					}
				}
				
				commitMessageTexbox.setText(""); //$NON-NLS-1$
				that.updateView();
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
		push.setImage(FileLocator.getImage("push", this)); //$NON-NLS-1$
		push.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				that.pull();
				that.push();
				that.updateView();
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
		pull.setImage(FileLocator.getImage("pull", this)); //$NON-NLS-1$
		pull.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				that.pull();
				that.updateView();
				that.view.refreshWorkspace();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});

		commit = new Button(commiterButtonsComposite, PUSH);
		commit.setLayoutData(new GridData(HORIZONTAL_ALIGN_FILL));
		commit.setImage(FileLocator.getImage("commit", this)); //$NON-NLS-1$
		commit.setText(COMMIT_ACTION);
		commit.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				PersonIdent author = that.getAuthors().stream().filter(a -> a.getName().equals(authorCombo.getText())).findFirst().orElse(null);

				if (author != null) {
					that.commit(commitMessageTexbox.getText(), author.getName(), author.getEmailAddress());
				}

				else {
					NewAuthorDialog dialog = new NewAuthorDialog(null, authorCombo.getText());
					dialog.open();
					if (dialog.getName() != "" && dialog.getEmail() != "") { //$NON-NLS-1$ //$NON-NLS-2$
						that.commit(commitMessageTexbox.getText(), dialog.getName(), dialog.getEmail());
					}
				}
				commitMessageTexbox.setText(""); //$NON-NLS-1$
				that.updateView();
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

	protected void updateView() {
		this.updateStagingState();
		this.updateCommitDetailsState();
		
		if (this.isActive && this.getProject() != null) {
			try {
				String uri = this.getProject().getLocation().toOSString();
				Git git = Git.wrap(new FileRepositoryBuilder().setGitDir(new File(uri + "/.git")).build()); //$NON-NLS-1
			
				if (git.getRepository().getBranch() != null) {
					projectLabel.setText(PROJECT_LABEL + " " + this.getProject().getName() + " [" + git.getRepository().getBranch() + "]"); //$NON-NLS-2 //$NON-NLS-3 //$NON-NLS-4
					this.enableStagingArea();
					this.enablePullButton();
					this.enablePushButton();
					this.updateComponentsState();
				}
				
				else {
					this.clear();
					projectLabel.setText(NOT_A_GIT_REPOSITORY_FOUND_LABEL + " " + this.getProject().getName());
					this.disableAll();
				}
				
			}

			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		else {
			this.clear();
			this.disableAll();
		}
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
	
	private boolean hasAddedChanges() {
		return stagedFiles.getItemCount() > 0;
	}
	
	private void enableCommitMessageTextboxtIfHasAddedChanges() {
		if (this.hasAddedChanges()) {
			this.commitMessageTexbox.setEnabled(true);
			tipLabel.setText(TIP_LABEL_CAN_WRITE_A_COMMIT_MESSAGE);
		}

		else {
			try {
				this.commitMessageTexbox.setEnabled(false);
				String uri = this.getProject().getLocation().toOSString();
				
				Git git = Git.wrap(new FileRepositoryBuilder().setGitDir(new File(uri + "/.git")).build()); // $NON-NLS-1
				if (git.getRepository().getBranch() != null) {
					tipLabel.setText(TIP_LABEL_NEED_ADD_CHANGES);
				}
				
				else {
					tipLabel.setText(TIP_LABEL_NOT_A_GIT_REPOSITORY);
				}
			} 
			
			catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
	
	private boolean hasCommitMessageWrited() {
		return commitMessageTexbox.getText().length() > 0;
	}
	
	private boolean canSelectAAuthor() {
		return this.hasAddedChanges() && this.hasCommitMessageWrited();
	}
	
	private void enableAuthorSelectionComboIfCanSelectAAuthor() {
		if (this.canSelectAAuthor()) {
			authorCombo.setEnabled(true);
			tipLabel.setText(TIP_LABEL_CAN_SELECT_AUTHOR);
		}

		else {
			authorCombo.setEnabled(false);
		}
	}

	private boolean hasAAuthorSelected() {
		return !authorCombo.getText().isEmpty();
	}

	private boolean canMakeACommit() {
		return this.hasAddedChanges() && this.hasCommitMessageWrited() && this.hasAAuthorSelected();
	}

	private void enableCommitIfCanMakeACommit() {
		if (this.canMakeACommit()) {
			tipLabel.setText(TIP_LABEL_CAN_COMMIT);
			this.enableCommitButton();
			this.enableCommitAndPushButton();
		}

		else {
			this.disableCommitButton();
			this.disableCommitAndPushButton();
		}
	}
	
	private void updateComponentsState() {
		this.enableCommitMessageTextboxtIfHasAddedChanges();
		this.enableAuthorSelectionComboIfCanSelectAAuthor();
		this.enableCommitIfCanMakeACommit();
		this.view.refreshWorkspace();
	}
	
	private void disableAll() {
		this.disableStagingArea();
		this.disableCommitterArea();
		this.disablePullButton();
		this.disablePushButton();
	}
	
	private void clear() {
		projectLabel.setText(""); //$NON-NLS-1
		tipLabel.setText(""); //$NON-NLS-1
		unstagedFiles.removeAll();
		stagedFiles.removeAll();
		unstagingFilesLabel.setText(UNSTAGED_CHANGES + 0);
		stagingFilesLabel.setText(STAGED_CHANGES + 0);
		commitMessageTexbox.setText(""); //$NON-NLS-1
		authorCombo.clearSelection();
	}

	@Override
	protected void onFocus() {
		this.onUpdate();
	}

	@Override
	protected void onUpdate() {
		this.updateView();
	}

}
