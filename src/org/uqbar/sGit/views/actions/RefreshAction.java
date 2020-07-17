package org.uqbar.sGit.views.actions;

import org.eclipse.jface.action.Action;
import org.uqbar.sGit.utils.FileLocator;
import org.uqbar.sGit.views.View;

public class RefreshAction extends Action {

	private final View view;

	public RefreshAction(View view) {
		this.view = view;
		this.setText(Messages.REFRESH_ACTION_MESSAGE);
		this.setToolTipText(Messages.REFRESH_ACTION_TOOLTIP);
		this.setImageDescriptor(FileLocator.getImageDescriptor("refresh", this));
	}

	@Override
	public void run() {
		if (view.isAlreadyInitialized()) {
			view.refresh();
		}
	}

}