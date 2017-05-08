package com.pddstudio.substratum.template.patcher;

import java.util.List;

/**
 * Created by pddstudio on 04/05/2017.
 */

public interface ThemeConfiguration {
	String getThemeName();
	String getThemeAuthor();
	List<TemplateConfiguration> getThemeTemplates();
}
