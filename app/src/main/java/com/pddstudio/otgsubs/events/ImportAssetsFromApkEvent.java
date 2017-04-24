package com.pddstudio.otgsubs.events;

import com.pddstudio.substratum.packager.models.AssetFileInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pddstudio on 24/04/2017.
 */

public class ImportAssetsFromApkEvent {

	private final List<AssetFileInfo> assets;

	public ImportAssetsFromApkEvent() {
		assets = new ArrayList<>();
	}

	public ImportAssetsFromApkEvent withList(List<AssetFileInfo> files) {
		this.assets.addAll(files);
		return this;
	}

	public List<AssetFileInfo> getAssets() {
		return assets;
	}

}
