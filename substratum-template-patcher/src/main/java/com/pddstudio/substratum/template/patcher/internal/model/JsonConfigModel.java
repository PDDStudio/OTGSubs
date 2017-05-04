package com.pddstudio.substratum.template.patcher.internal.model;

import com.google.gson.annotations.SerializedName;
import com.pddstudio.substratum.template.patcher.TemplateConfiguration;
import com.pddstudio.substratum.template.patcher.ThemeConfiguration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import java8.util.Objects;
import java8.util.stream.StreamSupport;

/**
 * Created by pddstudio on 25/04/2017.
 */

public class JsonConfigModel implements Serializable, ThemeConfiguration {

	@SerializedName("themeName")
	private String themeName;

	@SerializedName("themeAuthor")
	private String themeAuthor;

	@SerializedName("themeTemplates")
	private List<Template> templateList;

	@Override
	public String getThemeName() {
		return themeName;
	}

	@Override
	public String getThemeAuthor() {
		return themeAuthor;
	}

	@Override
	public List<TemplateConfiguration> getThemeTemplates() {
		List<TemplateConfiguration> configurations = new ArrayList<>();
		StreamSupport.stream(templateList).filter(Objects::nonNull).forEach(configurations::add);
		return configurations;
	}

}
