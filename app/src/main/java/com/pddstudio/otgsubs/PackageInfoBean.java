package com.pddstudio.otgsubs;

import android.support.annotation.NonNull;

import com.pddstudio.otgsubs.events.AssetTypeAddedEvent;
import com.pddstudio.otgsubs.events.ImportAssetsFromApkEvent;
import com.pddstudio.otgsubs.events.RefreshItemListEvent;
import com.pddstudio.substratum.packager.models.AssetFileInfo;
import com.pddstudio.substratum.packager.models.AssetsType;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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

	void register() {
		eventBusBean.register(this);
	}

	void unregister() {
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
