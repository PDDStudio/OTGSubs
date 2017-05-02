package com.pddstudio.substratum.template.patcher;

import java.util.HashMap;

/**
 * Created by pddstudio on 25/04/2017.
 */

public interface Configuration {
	HashMap<String, String> getThemeMappings();
	void updateThemeMappings(HashMap<String, String> updatedMap);
	String getThemeName();
	String getThemeAuthor();
	String getTemplateFileName();
}
