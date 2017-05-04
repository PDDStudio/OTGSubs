package com.pddstudio.substratum.template.patcher;

import android.util.Log;

import com.google.gson.Gson;
import com.pddstudio.substratum.template.patcher.internal.LinePatcher;
import com.pddstudio.substratum.template.patcher.internal.model.JsonConfigModel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

/**
 * Created by pddstudio on 25/04/2017.
 */

public class TemplatePatcher {

	private static final String TAG = TemplatePatcher.class.getSimpleName();

	private final LinePatcher           linePatcher;
	private final List<File>            targetFiles;
	private final ThemeConfiguration themeConfiguration;
	private List<TemplateConfiguration> templateConfigurations;

	private TemplatePatcher(Builder builder) {
		this.linePatcher = builder.linePatcher;
		this.targetFiles = builder.targetFiles;
		this.themeConfiguration = builder.themeConfiguration;
		this.templateConfigurations = themeConfiguration.getThemeTemplates();
	}

	private List<String> patchLines(List<String> lines, TemplateConfiguration templateConfiguration) {
		return StreamSupport.stream(lines).map(line -> patchLine(line, templateConfiguration)).collect(Collectors.toList());
	}

	private String patchLine(String line, TemplateConfiguration templateConfiguration) {
		final String[] patchedLine = {null};
		StreamSupport.stream(templateConfiguration.getThemeMappings().keySet()).forEach(key -> {
			String value = templateConfiguration.getThemeMappings().get(key);
			String patched = linePatcher.patch(line, key, value);
			if(patched != null && !patched.isEmpty() && !patched.contains("{") && !patched.contains("}")) {
				patchedLine[0] = patched;
			}
		});
		if(patchedLine[0] == null || patchedLine[0].isEmpty()) {
			return line;
		} else {
			return patchedLine[0];
		}
	}

	public void setTemplateConfigurations(List<TemplateConfiguration> templateConfigurations) {
		this.templateConfigurations = templateConfigurations;
	}

	public List<TemplateConfiguration> getTemplateConfigurations() {
		return templateConfigurations;
	}

	public ThemeConfiguration getThemeConfiguration() {
		return themeConfiguration;
	}

	public boolean patchAll(PatchingCallback patchingCallback) throws PatchingException {
		final PatchingException[] patchingException = {null};
		StreamSupport.stream(templateConfigurations).forEach(templateConfiguration -> {
			try {
				patch(templateConfiguration, patchingCallback);
			} catch (PatchingException e) {
				e.printStackTrace();
				patchingException[0] = e;
			}
		});
		if(patchingException[0] != null) {
			throw new PatchingException(patchingException[0]);
		} else {
			return true;
		}
	}

	private void patch(TemplateConfiguration templateConfiguration, PatchingCallback callback) throws PatchingException {
		File templateFile = callback.resolveTemplateFileName(templateConfiguration.getTemplateFileName());
		if(templateFile == null ||!templateFile.exists() || templateFile.isDirectory()) {
			throw new PatchingException("Template File must not be null, empty or a directory!");
		}
		try {
			List<String> content = FileUtils.readLines(templateFile, Charset.forName("utf-8"));
			List<String> patchedContent = patchLines(content, templateConfiguration);
			if(patchedContent != null && !patchedContent.isEmpty()) {
				File newDestination = new File(templateFile.getParentFile(), FilenameUtils.removeExtension(callback.getPatchedFileName(templateConfiguration) + ".xml"));
				StreamSupport.stream(patchedContent).forEach(line -> Log.d(TAG, "PATCHED: " + line));
				FileUtils.writeLines(newDestination, patchedContent, false);
			}
		} catch (IOException e) {
			throw new PatchingException(e);
		}
	}

	public static final class Builder {

		private final List<File> targetFiles;
		private LinePatcher linePatcher = XmlPatcher.getInstance();
		private final ThemeConfiguration themeConfiguration;

		public static Builder fromJson(String jsonContent) throws PatchingException {
			Log.d("TemplatePatcher$Builder", "JSON: " + jsonContent);
			Gson gson = new Gson();
			JsonConfigModel configModel = gson.fromJson(jsonContent, JsonConfigModel.class);
			return new Builder(configModel);
		}

		public static Builder fromConfig(ThemeConfiguration themeConfiguration) throws PatchingException {
			return new Builder(themeConfiguration);
		}

		private Builder(ThemeConfiguration themeConfiguration) throws PatchingException {
			if(themeConfiguration == null) {
				throw new PatchingException("TemplateConfiguration must not be null!");
			}
			this.themeConfiguration = themeConfiguration;
			this.targetFiles = new ArrayList<>();
		}

		public Builder withLinePatcher(LinePatcher linePatcher) {
			if(linePatcher != null) {
				this.linePatcher = linePatcher;
			}
			return this;
		}

		public Builder addTarget(File file) {
			if(file != null && file.exists()) {
				targetFiles.add(file);
			}
			return this;
		}

		public Builder addTargets(List<File> files) {
			StreamSupport.stream(files).forEach(this::addTarget);
			return this;
		}

		public TemplatePatcher build() {
			return new TemplatePatcher(this);
		}

	}

	public interface PatchingCallback {
		File resolveTemplateFileName(String templateName) throws PatchingException;
		String getPatchedFileName(TemplateConfiguration templateConfiguration);
	}

}
