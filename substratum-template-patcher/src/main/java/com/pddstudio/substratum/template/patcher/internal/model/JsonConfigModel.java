package com.pddstudio.substratum.template.patcher.internal.model;

import com.google.gson.annotations.SerializedName;
import com.pddstudio.substratum.template.patcher.Configuration;

import java.io.Serializable;
import java.util.HashMap;

import java8.util.stream.StreamSupport;

/**
 * Created by pddstudio on 25/04/2017.
 */

public class JsonConfigModel implements Serializable, Configuration {

	@SerializedName("name")
	private String themeName;

	@SerializedName("author")
	private String themeAuthor;

	@SerializedName("themes")
	private HashMap<String, String> themeValueMap;

	@SerializedName("templateFile")
	private String themeTemplateFileName;

	@Override
	public HashMap<String, String> getThemeMappings() {
		return themeValueMap;
	}

	@Override
	public String getThemeName() {
		return themeName;
	}

	@Override
	public String getThemeAuthor() {
		return themeAuthor;
	}

	@Override
	public String getTemplateFileName() {
		return themeTemplateFileName;
	}

	@Override
	public void updateThemeMappings(HashMap<String, String> updatedMap) {
		StreamSupport.stream(updatedMap.keySet()).forEachOrdered(key -> themeValueMap.put(key, updatedMap.get(key)));
	}
}
