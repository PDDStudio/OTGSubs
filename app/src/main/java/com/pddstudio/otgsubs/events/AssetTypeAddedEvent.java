package com.pddstudio.otgsubs.events;

import com.pddstudio.substratum.packager.models.AssetFileInfo;
import com.pddstudio.substratum.packager.models.AssetsType;

/**
 * Created by pddstudio on 24/04/2017.
 */

public class AssetTypeAddedEvent {

	private final AssetsType    assetsType;
	private final AssetFileInfo assetFileInfo;
	private boolean fragmentIgnore = true;

	public AssetTypeAddedEvent(AssetsType assetsType, AssetFileInfo assetFileInfo) {
		this.assetsType = assetsType;
		this.assetFileInfo = assetFileInfo;
	}

	public AssetsType getAssetsType() {
		return assetsType;
	}

	public AssetFileInfo getAssetFileInfo() {
		return assetFileInfo;
	}

	public boolean isIgnore() {
		return fragmentIgnore;
	}

	public void setFragmentIgnore(boolean fragmentIgnore) {
		this.fragmentIgnore = fragmentIgnore;
	}
}
