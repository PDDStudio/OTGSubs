package com.pddstudio.otgsubs.services;

import android.content.Context;
import android.support.annotation.NonNull;

import com.pddstudio.otgsubs.beans.EventBusBean;
import com.pddstudio.otgsubs.events.ImportAssetsFromApkEvent;
import com.pddstudio.substratum.packager.ApkExtractor;
import com.pddstudio.substratum.packager.models.ApkInfo;
import com.pddstudio.substratum.packager.models.AssetFileInfo;
import com.pddstudio.substratum.packager.models.AssetsType;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.ServiceAction;
import org.androidannotations.api.support.app.AbstractIntentService;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

/**
 * Created by pddstudio on 24/04/2017.
 */

@EIntentService
public class ImportApkService extends AbstractIntentService {

	private static final String TAG = ImportApkService.class.getSimpleName();

	private static final String ASSETS_DIR         = "assets";
	private static final String OVERLAYS_DIR       = "overlays";
	private static final String AUDIO_DIR          = "audio";
	private static final String FONTS_DIR          = "fonts";
	private static final String BOOT_ANIMATION_DIR = "bootanimation";

	public static void processImportRequest(@NonNull Context context, ApkInfo apkInfo) {
		ImportApkService_.intent(context).processApkImportRequest(apkInfo).start();
	}

	@Bean
	protected ApkExtractor apkExtractor;

	@Bean
	protected EventBusBean eventBus;

	public ImportApkService() {
		super(TAG);
	}

	@ServiceAction
	protected void processApkImportRequest(ApkInfo apkInfo) {
		File targetCacheDir = new File(getCacheDir(), apkInfo.getPackageName());
		try {
			File cachedApkFile = apkExtractor.copyApkToCache(targetCacheDir, apkInfo);
			apkExtractor.extractAssetsFromApk(cachedApkFile, targetCacheDir);
			FileUtils.forceDelete(cachedApkFile);
		} catch (IOException io) {
			io.printStackTrace();
		}
		sendEvent(new File(targetCacheDir, ASSETS_DIR));
	}

	private void sendEvent(File cachedAssetsFile) {
		File overlayDir = new File(cachedAssetsFile, OVERLAYS_DIR);
		if(!overlayDir.exists()) {
			overlayDir.mkdirs();
		}

		File fontsDir = new File(cachedAssetsFile, FONTS_DIR);
		if(!fontsDir.exists()) {
			fontsDir.mkdirs();
		}

		File audioDir = new File(cachedAssetsFile, AUDIO_DIR);
		if(!audioDir.exists()) {
			audioDir.mkdirs();
		}

		File bootAnimationDir = new File(cachedAssetsFile, BOOT_ANIMATION_DIR);
		if(!bootAnimationDir.exists()) {
			bootAnimationDir.mkdirs();
		}

		Collection<File> overlays = FileUtils.listFiles(overlayDir, null, true);
		Collection<File> fonts = FileUtils.listFiles(fontsDir, null, true);
		Collection<File> audio = FileUtils.listFiles(audioDir, null, true);
		Collection<File> bootAnimation = FileUtils.listFiles(bootAnimationDir, null, true);

		ImportAssetsFromApkEvent event = new ImportAssetsFromApkEvent();
		event.withList(convertCollectionToAssetsFileInfo(overlays, AssetsType.OVERLAYS));
		event.withList(convertCollectionToAssetsFileInfo(fonts, AssetsType.FONTS));
		event.withList(convertCollectionToAssetsFileInfo(audio, AssetsType.AUDIO));
		event.withList(convertCollectionToAssetsFileInfo(bootAnimation, AssetsType.BOOT_ANIMATIONS));
		eventBus.post(event);
	}

	private List<AssetFileInfo> convertCollectionToAssetsFileInfo(Collection<File> files, AssetsType assetsType) {
		return StreamSupport.stream(files).map(file -> {
			if (file != null && file.exists()) {
				AssetFileInfo assetFileInfo = new AssetFileInfo(assetsType, file.getAbsolutePath(), file.getName());
				String[] splits = file.getAbsolutePath().split(ASSETS_DIR);
				File file1 = new File(splits[1]);
				if(file1.isDirectory()) {
					assetFileInfo.setRelativeAssetsDestinationLocation(file1.getAbsolutePath());
				} else {
					assetFileInfo.setRelativeAssetsDestinationLocation(file1.getParent());
				}
				return assetFileInfo;
			} else {
				return null;
			}
		}).filter(assetFileInfo -> assetFileInfo != null).collect(Collectors.toList());
	}

}
