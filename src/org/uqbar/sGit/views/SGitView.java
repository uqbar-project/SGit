package org.uqbar.sGit.views;

import static org.eclipse.swt.SWT.BORDER;
import static org.eclipse.swt.SWT.DROP_DOWN;
import static org.eclipse.swt.SWT.HORIZONTAL;
import static org.eclipse.swt.SWT.H_SCROLL;
import static org.eclipse.swt.SWT.MULTI;
import static org.eclipse.swt.SWT.NULL;
import static org.eclipse.swt.SWT.PUSH;
import static org.eclipse.swt.SWT.RIGHT_TO_LEFT;
import static org.eclipse.swt.SWT.SEPARATOR_FILL;
import static org.eclipse.swt.SWT.VERTICAL;
import static org.eclipse.swt.SWT.V_SCROLL;
import static org.eclipse.swt.SWT.WRAP;
import static org.eclipse.swt.layout.GridData.FILL_BOTH;
import static org.eclipse.swt.layout.GridData.FILL_HORIZONTAL;
import static org.eclipse.swt.layout.GridData.HORIZONTAL_ALIGN_FILL;
import static org.uqbar.sGit.views.Messages.AUTHOR;
import static org.uqbar.sGit.views.Messages.COMMIT_ACTION;
import static org.uqbar.sGit.views.Messages.COMMIT_AND_PUSH_ACTION;
import static org.uqbar.sGit.views.Messages.COMMIT_MESSAGE;
import static org.uqbar.sGit.views.Messages.PULL_ACTION;
import static org.uqbar.sGit.views.Messages.PUSH_ACTION;
import static org.uqbar.sGit.views.Messages.STAGED_CHANGES;
import static org.uqbar.sGit.views.Messages.UNSTAGED_CHANGES;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.uqbar.sGit.utils.FileLocator;
import org.uqbar.sGit.utils.git.GitFile;
import org.uqbar.sGit.views.Dialogs.NewAuthorDialog;

public class SGitView extends View implements ModifyListener {

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
		addItem.setImage(FileLocator.getImage("add", this)); //$NON-NLS-1$
		addItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (unstagedFiles.getSelectionIndex() >= 0) {
					Arrays.asList(unstagedFiles.getSelection()).stream().forEach(item -> that.stage(item.getText()));
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
		addAllItem.setImage(FileLocator.getImage("add_all", this)); //$NON-NLS-1$
		addAllItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				that.stageAll();
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
		removeItem.setImage(FileLocator.getImage("unstage", this)); //$NON-NLS-1$
		removeItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (stagedFiles.getSelectionIndex() >= 0) {
					Arrays.asList(stagedFiles.getSelection()).stream().forEach(item -> that.unstage(item.getText()));
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
		removeAllItem.setImage(FileLocator.getImage("unstage_all", this)); //$NON-NLS-1$
		removeAllItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				that.unstageAll();
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
		
		authorCombo.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				that.enableCommitIfCanMakeACommit();
				
			}
		});

		Composite commiterButtonsComposite = new Composite(commiterContainer, RIGHT_TO_LEFT);
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
		return !this.authorCombo.getText().isEmpty() && this.stagedFiles.getItemCount() > 0 && this.commitMessageTexbox.getText().length() > 0;
	}

	private void enableCommitIfCanMakeACommit() {
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
	protected void onFocus() {
		this.onUpdate();
	}

	@Override
	protected void onUpdate() {
		if (this.isActive) {
			this.updateView();
			this.enableStagingArea();
			this.enableCommitterArea();
			this.enablePullButton();
			this.enablePushButton();
		}

		else {
			this.disableStagingArea();
			this.disableCommitterArea();
			this.disablePullButton();
			this.disablePushButton();
		}
	}

	@Override
	public void modifyText(ModifyEvent e) {
		this.enableCommitIfCanMakeACommit();
	}

}
