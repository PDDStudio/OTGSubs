package com.pddstudio.otgsubs.beans;

import android.support.annotation.NonNull;

import com.pddstudio.otgsubs.events.AssetTypeAddedEvent;
import com.pddstudio.otgsubs.events.ExistingAssetsItemEvent;
import com.pddstudio.otgsubs.events.ImportAssetsFromApkEvent;
import com.pddstudio.otgsubs.events.RefreshItemListEvent;
import com.pddstudio.substratum.packager.models.AssetFileInfo;
import com.pddstudio.substratum.packager.models.AssetsType;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import java8.util.stream.StreamSupport;

/**
 * Created by pddstudio on 24/04/2017.
 */

@EBean(scope = EBean.Scope.Singleton)
public class PackageInfoBean {

	private final List<AssetFileInfo> fontInformation;
	private final List<AssetFileInfo> overlayInformation;
	private final List<AssetFileInfo> audioInformation;
	private final List<AssetFileInfo> bootAnimationInformation;

	@Bean
	EventBusBean eventBusBean;

	public PackageInfoBean() {
		this.fontInformation = new ArrayList<>();
		this.overlayInformation = new ArrayList<>();
		this.audioInformation = new ArrayList<>();
		this.bootAnimationInformation = new ArrayList<>();
	}

	private void redirectEvent(AssetTypeAddedEvent event) {
		event.setFragmentIgnore(false);
		eventBusBean.post(event);
	}

	private void storeAssetFileInfo(@NonNull AssetFileInfo assetFileInfo) {
		switch (assetFileInfo.getType()) {
			case AUDIO:
				addItemIfNotPresent(audioInformation, assetFileInfo);
				break;
			case BOOT_ANIMATIONS:
				addItemIfNotPresent(bootAnimationInformation, assetFileInfo);
				break;
			case FONTS:
				addItemIfNotPresent(fontInformation, assetFileInfo);
				break;
			case OVERLAYS:
				addItemIfNotPresent(overlayInformation, assetFileInfo);
				break;
		}
	}

	private void addItemIfNotPresent(List<AssetFileInfo> fileInformation, AssetFileInfo assetFileInfo) {
		boolean itemExist = StreamSupport.stream(fileInformation).filter(assetFileInfo1 ->  assetFileInfo1.getFileLocation().equals(assetFileInfo.getFileLocation())).findAny().isPresent();
		if(!itemExist) {
			fileInformation.add(assetFileInfo);
		}
	}

	private void removeItemIfPresent(AssetFileInfo assetFileInfo) {
		StreamSupport.stream(getExistingInformation(assetFileInfo.getType())).forEach(assetFileInfo1 -> {
			if(assetFileInfo1.getFileLocation().equals(assetFileInfo.getFileLocation())) {
				getExistingInformation(assetFileInfo.getType()).remove(assetFileInfo1);
			}
		});
	}

	private void renameItemIfPresent(AssetFileInfo assetFileInfo, String newFileName) {
		StreamSupport.stream(getExistingInformation(assetFileInfo.getType())).filter(info -> info.getFileLocation().equals(assetFileInfo.getFileLocation())).findAny().ifPresent(oldInfo -> {
			if(oldInfo.getRelativeAssetsDestinationLocation() != null) {
				String parent = new File(oldInfo.getRelativeAssetsDestinationLocation()).getParent();
				File newFile = new File(parent, newFileName);
				oldInfo.setRelativeAssetsDestinationLocation(newFile.getAbsolutePath());
			} else {
				oldInfo.setFileName(newFileName);
			}
		});
	}

	public void register() {
		eventBusBean.register(this);
	}

	public void unregister() {
		eventBusBean.unregister(this);
	}

	@Subscribe
	public void onDialogResultReceived(AssetTypeAddedEvent event) {
		if(event != null && event.isIgnore()) {
			AssetFileInfo info = event.getAssetFileInfo();
			storeAssetFileInfo(info);
			redirectEvent(event);
		}
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onImportEventReceived(ImportAssetsFromApkEvent event) {
		if(event != null) {
			StreamSupport.stream(event.getAssets()).forEach(this::storeAssetFileInfo);
			eventBusBean.post(new RefreshItemListEvent());
		}
	}

	@Subscribe
	public void onExistingAssetsModifiedEventReceived(ExistingAssetsItemEvent event) {
		if(event != null) {
			switch (event.getModificationType()) {
				case DELETE:
					removeItemIfPresent(event.getAssetFileInfo());
					break;
				case RENAME:
					renameItemIfPresent(event.getAssetFileInfo(), event.getNewName());
					break;
			}
			eventBusBean.post(new RefreshItemListEvent());
		}
	}

	public List<AssetFileInfo> getExistingInformation(AssetsType assetsType) {
		switch (assetsType) {
			case AUDIO:
				return audioInformation;
			case BOOT_ANIMATIONS:
				return bootAnimationInformation;
			case FONTS:
				return fontInformation;
			case OVERLAYS:
				return overlayInformation;
			default:
				return new ArrayList<>();
		}
	}

}
