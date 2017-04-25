package com.pddstudio.otgsubs.events;

import com.pddstudio.otgsubs.models.AssetsModificationType;
import com.pddstudio.substratum.packager.models.AssetFileInfo;

/**
 * Created by pddstudio on 25/04/2017.
 */

public class ExistingAssetsItemEvent {

	private AssetFileInfo assetFileInfo;
	private AssetsModificationType modificationType;
	private String newName;

	public ExistingAssetsItemEvent(AssetFileInfo assetFileInfo, AssetsModificationType modificationType) {
		this.assetFileInfo = assetFileInfo;
		this.modificationType = modificationType;
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}

	public AssetFileInfo getAssetFileInfo() {
		return assetFileInfo;
	}

	public AssetsModificationType getModificationType() {
		return modificationType;
	}

	public String getNewName() {
		return newName;
	}
}
