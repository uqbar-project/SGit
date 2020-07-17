package org.uqbar.sGit.utils.git;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitUrlChecker {

	public static boolean isValidGitUrl(String url) {
		String regexp = "((git|ssh|http(s)?)|(git@[\\w\\.]+))(:(//)?)([\\w\\.@\\:/\\-~]+)(\\.git)(/)?";
		Pattern p = Pattern.compile(regexp);
		Matcher m = p.matcher(url);
		return m.find();
	}

}
