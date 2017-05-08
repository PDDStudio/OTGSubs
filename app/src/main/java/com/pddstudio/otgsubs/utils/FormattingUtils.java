package com.pddstudio.otgsubs.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by pddstudio on 08/05/2017.
 */

public class FormattingUtils {

	private FormattingUtils() {
		//disable instance creation
	}

	public static String formatUserThemeName(String userThemeName) {
		return StringUtils.replace(userThemeName, " ", "_");
	}

}
