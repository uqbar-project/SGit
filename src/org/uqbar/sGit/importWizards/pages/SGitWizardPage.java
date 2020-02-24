package org.uqbar.sGit.importWizards.pages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jgit.util.StringUtils;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public abstract class SGitWizardPage extends WizardPage implements ModifyListener, IPageChangedListener {

	protected Composite container;
	private final List<String> errors;

	public SGitWizardPage(String pageName) {
		super(pageName);
		this.setTitle(pageName);
		this.setDescription(""); //$NON-NLS-1$
		this.errors = new ArrayList<String>();
	}

	public SGitWizardPage(String pageName, String pageDescription) {
		super(pageName);
		this.setTitle(pageName);
		this.setDescription(pageDescription);
		this.errors = new ArrayList<String>();
	}

	public <T> T getPreviousPageAs(Class<T> clazz) {
		return clazz.cast(this.getPreviousPage());
	}

	protected abstract void generateMessageErrors();

	private void showMessageErrors() {
		this.setErrorMessage(errors.isEmpty() ? null : StringUtils.join(errors, "\n ")); //$NON-NLS-1$
		errors.clear();
	}

	protected void addMessageError(String message) {
		errors.add(message);
	}

	public Boolean hasErrors() {
		return !(this.getErrorMessage() == null);
	}

	protected void updatePageState() {
		this.generateMessageErrors();
		this.showMessageErrors();
		this.getWizard().getContainer().updateButtons();
	}

	protected void addPageChangedListener(IPageChangedListener listener) {
		((WizardDialog) this.getWizard().getContainer()).addPageChangedListener(this);
	}

	protected abstract void onPageInit();

	protected abstract void onPageShow();

	protected abstract void onPageFinish();

	public void close() {
		this.onPageFinish();
	}

	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, NONE);
		container.setLayout(new GridLayout());
		this.onPageInit();
		container.pack();
		this.addPageChangedListener(this);
		this.setControl(container);
	}

	@Override
	public void modifyText(ModifyEvent e) {
		this.updatePageState();
	}

	@Override
	public boolean isPageComplete() {
		return super.isPageComplete() && !this.hasErrors();
	}

	@Override
	public void pageChanged(PageChangedEvent event) {
		if (event.getSelectedPage().equals(this)) {
			this.onPageShow();
		}
		this.updatePageState();
	}

}
