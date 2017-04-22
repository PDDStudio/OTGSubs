package com.pddstudio.substratum.packager.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by pddstudio on 21/04/2017.
 */

public class ApkInfo {

	private String appName;
	private String apk;
	private String version;
	private String source;
	private String data;

	public ApkInfo(String appName, String apk, String version, String source, String data) {
		this.appName = appName;
		this.apk = apk;
		this.version = version;
		this.source = source;
		this.data = data;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getApk() {
		return apk;
	}

	public void setApk(String apk) {
		this.apk = apk;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	@Override
	public String toString() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(this);
	}
}
