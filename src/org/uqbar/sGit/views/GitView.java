package org.uqbar.sGit.views;

import static org.eclipse.swt.SWT.*;
import static org.eclipse.swt.layout.GridData.*;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.uqbar.sGit.git.GitFile;

public class GitView extends SGitView {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.uqbar.sGit.views.GitView";

	private Label unstagingFilesLabel;
	private Label stagingFilesLabel;
	private List unstagedFilesList;
	private List stagedFilesList;
	private Text commitMessageTexbox;
	private Combo authorCombo;
	private Combo originCombo;
	private String authorName;
	private String authorEmail;
	private String committerName;
	private String committerEmail;

	/**
	 * add a Git file wrapper to the non staging area.
	 */
	private void addUnstagedGitFile(GitFile file) {
		unstagedFilesList.add(file.getStatus() + " - " + file.getFilePath());
	}

	/**
	 * add a Git file wrapper to the staging area.
	 */
	private void addStagedGitFile(GitFile file) {
		stagedFilesList.add(file.getStatus() + " - " + file.getFilePath());
	}
	
	/**
	 * Updates staging state.
	 */
	private void updateStagingState() {
		unstagedFilesList.removeAll();
		stagedFilesList.removeAll();
		gitRepository.getUnstagedFiles().stream().forEach(this::addUnstagedGitFile);
		gitRepository.getStagedFiles().stream().forEach(this::addStagedGitFile);
		unstagingFilesLabel.setText("Unstaged changes: " + unstagedFilesList.getItemCount());
		stagingFilesLabel.setText("Staged changes: " + stagedFilesList.getItemCount());
	}
	
	/**
	 * Updates committer box state.
	 */
	private void updateCommitDetailsState() {
		
		Set<String> authorsName = gitRepository.getAuthors().stream().map(a -> a.getName()).collect(Collectors.toSet());
		authorCombo.setItems(authorsName.toArray(new String[0]));
		authorCombo.select(authorCombo.indexOf(gitRepository.getLastAuthor().getName()));
		
		originCombo.setItems(new String[] {gitRepository.getCurrentBranch()});
		originCombo.select(0);
		
		if (authorCombo.getItemCount() > 0) {
			PersonIdent author = gitRepository.getAuthors().stream()
					.filter(a -> a.getName().equals(authorCombo.getText())).findFirst().get();

			authorName = author.getName();
			authorEmail = author.getEmailAddress();
			committerName = author.getName();
			committerEmail = author.getEmailAddress();
		}
		commitMessageTexbox.setText("");

	}

	/**
	 * Perform git add to staging Action.
	 */
	private void add(String filePath) {
		gitRepository.addFileToStaging(filePath.split(" - ")[1]);
		this.updateStagingState();
	}

	/**
	 * Perform git add all to staging Action.
	 */
	private void addAll() {
		Arrays.asList(unstagedFilesList.getItems()).stream().forEach(this::add);
	}
	
	/**
	 * Perform git remove from staging Action.
	 */
	private void remove(String filePath) {
		gitRepository.removeFileFromStaging(filePath.split(" - ")[1]);
		this.updateStagingState();
	}

	/**
	 * Perform git remove all from staging Action.
	 */
	private void removeAll() {
		Arrays.asList(stagedFilesList.getItems()).stream().forEach(this::remove);
	}

	/**
	 * Perform a Git commit Action.
	 * 
	 * @param message: The committer message.
	 * @param author: The committer author.
	 * @param email: the committer.
	 * @param committer.
	 * @param committerEmail.
	 */
	private void commit(String message, String author, String email, String committer, String committerEmail) {
		gitRepository.commit(message, author, email, committer, committerEmail);
	}

	/**
	 * Perform a Git push Action.
	 */
	private void push() {
		gitRepository.push();
	}
	
	/**
	 * Perform a Git pull Action.
	 */
	private void pull() {
		gitRepository.pull();
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
		unstagingFilesLabel.setText("Unstaged changes: " + 0);

		final ToolBar unstagingActionToolBar = new ToolBar(unstagingToolbarComposite, BORDER);
		unstagingActionToolBar.setLayoutData(new GridData(HORIZONTAL_ALIGN_FILL | SEPARATOR_FILL));

		final ToolItem addItem = new ToolItem(unstagingActionToolBar, PUSH);
		addItem.setText("+");
		addItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (unstagedFilesList.getSelectionIndex() >= 0) {
					Arrays.asList(unstagedFilesList.getSelection()).stream().forEach(that::add);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});

		final ToolItem addAllItem = new ToolItem(unstagingActionToolBar, PUSH);
		addAllItem.setText("++");
		addAllItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				that.addAll();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});

		unstagedFilesList = new List(unstagingComposite, PUSH | MULTI | BORDER | H_SCROLL | V_SCROLL | WRAP);
		unstagedFilesList.setLayoutData(new GridData(FILL_BOTH));

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
		stagingFilesLabel.setText("Staged changes: " + 0);

		ToolBar stagingActionToolBar = new ToolBar(stagingToolbarComposite, BORDER);
		stagingActionToolBar.setLayoutData(new GridData(HORIZONTAL_ALIGN_FILL | SEPARATOR_FILL));

		ToolItem removeItem = new ToolItem(stagingActionToolBar, PUSH);
		removeItem.setText("-");
		removeItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (stagedFilesList.getSelectionIndex() >= 0) {
					Arrays.asList(stagedFilesList.getSelection()).stream().forEach(that::remove);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});

		ToolItem removeAllItem = new ToolItem(stagingActionToolBar, PUSH);
		removeAllItem.setText("--");
		removeAllItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				that.removeAll();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});

		stagedFilesList = new List(stagingComposite, PUSH | MULTI | BORDER | H_SCROLL | V_SCROLL | WRAP);
		stagedFilesList.setLayoutData(new GridData(FILL_BOTH));

		
		// Committer Section.
		
		final Composite commiterContainer = new Composite(gitStagingSashForm, BORDER | VERTICAL);
		final GridLayout commiterlayout = new GridLayout();
		commiterContainer.setLayout(commiterlayout);
		
		final Label originLabel = new Label(commiterContainer, PUSH);
		final GridData originLabelGridData = new GridData(HORIZONTAL_ALIGN_FILL);
		originLabel.setLayoutData(originLabelGridData);
		originLabel.setText("Origin: ");
		
		originCombo = new Combo(commiterContainer, DROP_DOWN);
		final GridData originsGridData = new GridData(FILL_HORIZONTAL);
		originCombo.setLayoutData(originsGridData);
		originCombo.setItems(new String[] { });

		final Label commitLabel = new Label(commiterContainer, NULL);
		commitLabel.setText("Commit Message:");
		commitLabel.setLayoutData(new GridData(FILL_HORIZONTAL));
		
		commitMessageTexbox = new Text(commiterContainer, BORDER | H_SCROLL | V_SCROLL | WRAP);
		commitMessageTexbox.setLayoutData(new GridData(FILL_BOTH));
		
		final Label authorLabel = new Label(commiterContainer, NULL);
		authorLabel.setText("Author: ");
		authorLabel.setLayoutData(new GridData(FILL_HORIZONTAL));
		
		authorCombo = new Combo(commiterContainer, DROP_DOWN);
		final GridData authorComboComboGridData = new GridData(FILL_HORIZONTAL);
		authorComboComboGridData.horizontalSpan = 2;
		authorCombo.setLayoutData(authorComboComboGridData);
		authorCombo.setItems(new String[] { });
		
		Composite commiterButtonsComposite = new Composite(commiterContainer, RIGHT_TO_LEFT);
		final GridLayout commiterButtonsCompositeLayout = new GridLayout();
		commiterButtonsCompositeLayout.numColumns = 4;
		commiterButtonsComposite.setLayout(commiterButtonsCompositeLayout);
		commiterButtonsComposite.setLayoutData(new GridData(FILL_HORIZONTAL));

		final Button commitAndPushButton = new Button(commiterButtonsComposite, PUSH);
		commitAndPushButton.setLayoutData(new GridData(HORIZONTAL_ALIGN_FILL));
		commitAndPushButton.setText("Commit and Push");
		commitAndPushButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				that.commit(commitMessageTexbox.getText(), authorName, authorEmail, committerName, committerEmail);
				that.push();
				that.updateStagingState();
				that.updateCommitDetailsState();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});
		
		final Button push = new Button(commiterButtonsComposite, PUSH);
		push.setLayoutData(new GridData(HORIZONTAL_ALIGN_FILL));
		push.setText("Push");
		push.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				that.push();
				that.updateStagingState();
				that.updateCommitDetailsState();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});
		
		final Button pull = new Button(commiterButtonsComposite, PUSH);
		pull.setLayoutData(new GridData(HORIZONTAL_ALIGN_FILL));
		pull.setText("Pull");
		pull.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				that.pull();
				that.updateStagingState();
				that.updateCommitDetailsState();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});
		
		final Button commit = new Button(commiterButtonsComposite, PUSH);
		commit.setLayoutData(new GridData(HORIZONTAL_ALIGN_FILL));
		commit.setText("Commit");
		commit.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				that.commit(commitMessageTexbox.getText(), authorName, authorEmail, committerName, committerEmail);
				that.updateStagingState();
				that.updateCommitDetailsState();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});

		
	}

	@Override
	protected void onFocus() {
		if (gitRepository != null) {
			this.updateStagingState();
		}
	}

	@Override
	protected void onProjectReferenceUpdate() {
		this.updateStagingState();
		this.updateCommitDetailsState();
	}


}
