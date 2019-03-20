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
import org.uqbar.sGit.model.file.GitFile;

public class GitView extends SGitView {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.uqbar.sGit.views.GitView"; //$NON-NLS-1$

	private Label unstagingFilesLabel;
	private Label stagingFilesLabel;
	private Table unstagedFiles;
	private Table stagedFiles;
	private Text commitMessageTexbox;
	private Combo authorCombo;
	private String authorName;
	private String authorEmail;
	private String committerName;
	private String committerEmail;

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
		gitRepository.addFileToStaging(filePath);
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
		gitRepository.removeFileFromStaging(filePath);
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
	 * @param message: The committer message.
	 * @param author: The committer author.
	 * @param email: the committer.
	 * @param committer.
	 * @param committerEmail.
	 */
	private void commit(String message, String author, String email, String committer, String committerEmail) {
		gitRepository.commit(message, author, email, committer, committerEmail);
		this.updateStagingState();
	}

	/**
	 * Perform a Git push Action.
	 */
	private void push() {
		gitRepository.push();
		this.updateStagingState();
	}

	/**
	 * Perform a Git pull Action.
	 */
	private void pull() {
		gitRepository.pull();
		this.updateStagingState();
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

		final ToolItem addItem = new ToolItem(unstagingActionToolBar, PUSH);
		addItem.setImage(this.getImage("add"));
		addItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (unstagedFiles.getSelectionIndex() >= 0) {
					Arrays.asList(unstagedFiles.getSelection()).stream().forEach(item -> that.add(item.getText()));
					that.updateStagingState();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});

		final ToolItem addAllItem = new ToolItem(unstagingActionToolBar, PUSH);
		addAllItem.setImage(this.getImage("add_all"));
		addAllItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				that.addAll();
				that.updateStagingState();
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

		ToolItem removeItem = new ToolItem(stagingActionToolBar, PUSH);
		removeItem.setImage(this.getImage("unstage"));
		removeItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (stagedFiles.getSelectionIndex() >= 0) {
					Arrays.asList(stagedFiles.getSelection()).stream().forEach(item -> that.remove(item.getText()));
					that.updateStagingState();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});

		ToolItem removeAllItem = new ToolItem(stagingActionToolBar, PUSH);
		removeAllItem.setImage(this.getImage("unstage_all"));
		removeAllItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				that.removeAll();
				that.updateStagingState();
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

		final Button commitAndPushButton = new Button(commiterButtonsComposite, PUSH);
		commitAndPushButton.setLayoutData(new GridData(HORIZONTAL_ALIGN_FILL));
		commitAndPushButton.setText(COMMIT_AND_PUSH_ACTION);
		commitAndPushButton.setImage(this.getImage("commitandpush"));
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
		push.setText(PUSH_ACTION);
		push.setImage(this.getImage("push"));
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
		pull.setText(PULL_ACTION);
		pull.setImage(this.getImage("pull"));
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
		commit.setImage(this.getImage("commit"));
		commit.setText(COMMIT_ACTION);
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
