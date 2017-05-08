package com.pddstudio.otgsubs.events;

import com.pddstudio.substratum.template.patcher.TemplateConfiguration;
import com.pddstudio.substratum.template.patcher.ThemeConfiguration;

import java.util.List;

/**
 * Created by pddstudio on 04/05/2017.
 */

public class PatchThemePreparationEvent {

	private boolean success;
	private List<TemplateConfiguration> templateConfigurations;
	private ThemeConfiguration themeConfiguration;

	public PatchThemePreparationEvent(boolean success, List<TemplateConfiguration> templateConfigurations) {
		this.success = success;
		this.templateConfigurations = templateConfigurations;
	}

	public PatchThemePreparationEvent withThemeConfiguration(ThemeConfiguration themeConfiguration) {
		this.themeConfiguration = themeConfiguration;
		return this;
	}

	public boolean isSuccess() {
		return success;
	}

	public List<TemplateConfiguration> getTemplateConfigurations() {
		return templateConfigurations;
	}

	public ThemeConfiguration getThemeConfiguration() {
		return themeConfiguration;
	}
}
