package org.uqbar.sGit.utils;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.FrameworkUtil;

public class ImageLocator {

	private static URL getFileURL(String name, Object obj) {
		return FileLocator.find(FrameworkUtil.getBundle(obj.getClass()), new Path("icons/" + name + ".png"), null);
	}

	public static Image getImage(String name, Object obj) {
		return ImageLocator.getImageDescriptor(name, obj).createImage();
	}

	public static ImageDescriptor getImageDescriptor(String name, Object obj) {
		return ImageDescriptor.createFromURL(getFileURL(name, obj));
	}

}