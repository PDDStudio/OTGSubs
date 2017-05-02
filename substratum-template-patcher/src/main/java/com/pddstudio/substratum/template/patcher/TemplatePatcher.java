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
import java.util.HashMap;
import java.util.List;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

/**
 * Created by pddstudio on 25/04/2017.
 */

public class TemplatePatcher {

	private static final String TAG = TemplatePatcher.class.getSimpleName();

	private final LinePatcher linePatcher;
	private final List<File> targetFiles;
	private final Configuration configuration;

	private TemplatePatcher(Builder builder) {
		this.linePatcher = builder.linePatcher;
		this.targetFiles = builder.targetFiles;
		this.configuration = builder.configuration;
	}

	private List<String> patchLines(List<String> lines) {
		return StreamSupport.stream(lines).map(this::patchLine).collect(Collectors.toList());
	}

	private String patchLine(String line) {
		final String[] patchedLine = {null};
		StreamSupport.stream(configuration.getThemeMappings().keySet()).forEach(key -> {
			String value = configuration.getThemeMappings().get(key);
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

	//NOTE: Template file must be copied to new file after
	public boolean patch(PatchingCallback patchingCallback) throws PatchingException {
		configuration.updateThemeMappings(patchingCallback.getPatchedValuesMappings());
		File templateFile = patchingCallback.resolveTemplateFileName(configuration.getTemplateFileName());
		if(templateFile == null ||!templateFile.exists() || templateFile.isDirectory()) {
			throw new PatchingException("Template File must not be null, empty or a directory!");
		}
		try {
			List<String> content = FileUtils.readLines(templateFile, Charset.forName("utf-8"));
			List<String> patchedContent = patchLines(content);
			if(patchedContent != null && !patchedContent.isEmpty()) {
				File newDestination = new File(templateFile.getParentFile(), FilenameUtils.removeExtension(patchingCallback.getPatchedFileName() + ".xml"));
				StreamSupport.stream(patchedContent).forEach(line -> Log.d(TAG, "PATCHED: " + line));
				FileUtils.writeLines(newDestination, patchedContent, false);
			}
		} catch (IOException e) {
			throw new PatchingException(e);
		}
		return true;
	}

	public static final class Builder {

		private final List<File> targetFiles;
		private LinePatcher linePatcher = XmlPatcher.getInstance();
		private final Configuration configuration;

		public static Builder fromJson(String jsonContent) throws PatchingException {
			Log.d("TemplatePatcher$Builder", "JSON: " + jsonContent);
			Gson gson = new Gson();
			JsonConfigModel configModel = gson.fromJson(jsonContent, JsonConfigModel.class);
			return new Builder(configModel);
		}

		public static Builder fromConfig(Configuration configuration) throws PatchingException {
			return new Builder(configuration);
		}

		private Builder(Configuration configuration) throws PatchingException {
			if(configuration == null) {
				throw new PatchingException("Configuration must not be null!");
			}
			this.configuration = configuration;
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
		//Callback which should resolve the target file in cache and return its result as file
		HashMap<String, String> getPatchedValuesMappings();
		File resolveTemplateFileName(String templateName) throws PatchingException;
		String getPatchedFileName();
	}

}
