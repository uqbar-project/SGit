package org.uqbar.sGit.utils;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class Clipboard {

	public static String getContent() {

		java.awt.datatransfer.Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

		String content = ""; //$NON-NLS-1$

		if (systemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
			try {
				content = (String) systemClipboard.getData(DataFlavor.stringFlavor);
			}

			catch (UnsupportedFlavorException | IOException e) {
				// Do nothing.
			}
		}
		return content;
	}

}
