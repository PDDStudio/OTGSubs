package com.pddstudio.substratum.template.patcher.internal.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pddstudio.substratum.template.patcher.TemplateConfiguration;

import java.io.Serializable;
import java.util.HashMap;

import java8.util.stream.StreamSupport;

/**
 * Created by pddstudio on 04/05/2017.
 */

public class Template implements Serializable, TemplateConfiguration {

	@SerializedName("id")
	private String                  id;

	@SerializedName("title")
	private String                  title;

	@SerializedName("description")
	private String                  description;

	@SerializedName("templateFile")
	private String                  templateFile;

	@SerializedName("templateType")
	private String templateType;

	@SerializedName("themes")
	private HashMap<String, String> themeValueMap;

	@Expose
	private String templateName;

	@Override
	public HashMap<String, String> getThemeMappings() {
		return themeValueMap;
	}

	@Override
	public void updateThemeMappings(HashMap<String, String> updatedMap) {
		StreamSupport.stream(updatedMap.keySet()).forEachOrdered(key -> themeValueMap.put(key, updatedMap.get(key)));
	}

	@Override
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	@Override
	public String getTemplateName() {
		return templateName;
	}

	@Override
	public String getTemplateFileName() {
		return templateFile;
	}

	@Override
	public String getTemplateId() {
		return id;
	}

	@Override
	public String getTemplateTitle() {
		return title;
	}

	@Override
	public String getTemplateType() {
		return templateType;
	}

	@Override
	public String getTemplateDescription() {
		return description;
	}

}
