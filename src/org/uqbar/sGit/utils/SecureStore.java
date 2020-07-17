package org.uqbar.sGit.utils;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;

public class SecureStore {

	private static SecureStore instance;
	private final Map<String, ISecurePreferences> nodes = new HashMap<>();

	private SecureStore() {
		this.createNode("credentials");
	}

	public static SecureStore getInstance() {
		if (instance == null) {

			instance = new SecureStore();
		}
		return instance;
	}

	public void createNode(String node) {
		this.nodes.put(node, SecurePreferencesFactory.getDefault().node(node));
	}

	public void secure(String node, String key, String value) {
		try {
			nodes.get(node).put(key, value, true);
		}

		catch (StorageException e) {
			e.printStackTrace();
		}
	}

	public String getOrDefault(String node, String key, String defaultValue) {
		try {
			return nodes.get(node).get(key, "");
		}

		catch (StorageException e) {
			return defaultValue;
		}
	}

}
