package com.pddstudio.substratum.template.patcher;

import java.util.HashMap;

/**
 * Created by pddstudio on 25/04/2017.
 */

public interface TemplateConfiguration {
	HashMap<String, String> getThemeMappings();
	void updateThemeMappings(HashMap<String, String> updatedMap);
	void setTemplateName(String templateName);
	String getTemplateName();
	String getTemplateFileName();
	String getTemplateId();
	String getTemplateTitle();
	String getTemplateType();
	String getTemplateDescription();
}
