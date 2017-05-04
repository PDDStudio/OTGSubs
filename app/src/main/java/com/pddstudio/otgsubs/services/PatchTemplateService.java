package com.pddstudio.otgsubs.services;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.pddstudio.otgsubs.beans.EventBusBean;
import com.pddstudio.otgsubs.events.ImportAssetsFromApkEvent;
import com.pddstudio.otgsubs.events.PatchThemePreparationEvent;
import com.pddstudio.substratum.packager.ApkExtractor;
import com.pddstudio.substratum.packager.models.ApkInfo;
import com.pddstudio.substratum.packager.models.AssetFileInfo;
import com.pddstudio.substratum.packager.models.AssetsType;
import com.pddstudio.substratum.template.patcher.PatchingException;
import com.pddstudio.substratum.template.patcher.TemplateConfiguration;
import com.pddstudio.substratum.template.patcher.TemplatePatcher;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.ServiceAction;
import org.androidannotations.api.support.app.AbstractIntentService;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

/**
 * Created by pddstudio on 26/04/2017.
 */

@EIntentService
public class PatchTemplateService extends AbstractIntentService implements TemplatePatcher.PatchingCallback {

	public static final String TAG = PatchTemplateService.class.getSimpleName();

	public static final String ACTION_PATCHING_DONE = "com.pddstudio.otgsubs.services.ACTION_PATCHING_DONE";
	public static final String ACTION_PREPARING_DONE = "com.pddstudio.otgsubs.services.ACTION_PREPARING_DONE";
	public static final String EXTRA_PATCHING_SUCCESS = "com.pddstudio.otgsubs.services.EXTRA_PATCHING_SUCCESS";
	public static final String EXTRA_PREPARING_SUCCESS = "com.pddstudio.otgsubs.services.EXTRA_PREPARING_SUCCESS";
	public static final String EXTRA_PATCHING_ASSETS = "com.pddstudio.otgsubs.services.EXTRA_PATCHING_ASSETS";
	public static final String EXTRA_PREPARING_TEMPLATES = "com.pddstudio.otgsubs.services.EXTRA_PREPARING_TEMPLATES";

	private static final String ASSETS_DIR         = "assets";
	private static final String OVERLAYS_DIR       = "overlays";
	private static final String AUDIO_DIR          = "audio";
	private static final String FONTS_DIR          = "fonts";
	private static final String BOOT_ANIMATION_DIR = "bootanimation";

	private static final String THEME_CONFIG_RELATIVE_LOCATION = "overlays/android/";
	private static final String THEME_CONFIG_FILE_NAME         = "theme_config.json";

	private static final String THEME_NAME_TEMPLATE = "%s_%s.xml";

	private static final boolean FULL_PACKAGE = false;

	public static void patchTargetTheme(@NonNull Context context, @NonNull ApkInfo apkInfo, @NonNull List<TemplateConfiguration> templateConfigurations) {
		PatchTemplateService_.intent(context).patchApkIfPossible(apkInfo,templateConfigurations).start();
	}

	public static void prepareTargetTheme(@NonNull Context context, @NonNull ApkInfo apkInfo) {
		PatchTemplateService_.intent(context).prepareApk(apkInfo).start();
	}

	@Bean
	protected ApkExtractor apkExtractor;

	@Bean
	protected EventBusBean eventBus;

	private File cachedAssetDir;

	public PatchTemplateService() {
		super(TAG);
	}

	private boolean checkThemeConfigPresent(File cachedAssetDir) {
		File configFile = new File(cachedAssetDir, THEME_CONFIG_RELATIVE_LOCATION + THEME_CONFIG_FILE_NAME);
		return configFile.exists() && configFile.isFile();
	}

	private void sendResultBroadcast(boolean success, @Nullable File assetsDir) {
		Log.d(TAG, "PATCHING FINISHED: " + success);
		Intent intent = new Intent(ACTION_PATCHING_DONE);
		intent.putExtra(EXTRA_PATCHING_SUCCESS, success);
		if(assetsDir != null) {
			intent.putExtra(EXTRA_PATCHING_ASSETS, assetsDir);
		}
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	private void sendPreparationEvent(boolean success, @NonNull List<TemplateConfiguration> templateConfigurations) {
		PatchThemePreparationEvent event = new PatchThemePreparationEvent(success, templateConfigurations);
		eventBus.post(event);
	}

	@ServiceAction
	protected void prepareApk(ApkInfo apkInfo) {
		File targetCacheDir = new File(getCacheDir(), apkInfo.getPackageName());
		try {
			File cachedApkFile = apkExtractor.copyApkToCache(targetCacheDir, apkInfo);
			apkExtractor.extractAssetsFromApk(cachedApkFile, targetCacheDir);
			FileUtils.forceDelete(cachedApkFile);

			cachedAssetDir = new File(targetCacheDir, ASSETS_DIR);
			boolean configPresent = checkThemeConfigPresent(cachedAssetDir);
			Log.d(TAG, "Is ThemeConfig Present: " + configPresent);

			if (!configPresent) {
				return;
			}

			File configFile = new File(cachedAssetDir, THEME_CONFIG_RELATIVE_LOCATION + THEME_CONFIG_FILE_NAME);
			TemplatePatcher patcher = TemplatePatcher.Builder.fromJson(FileUtils.readFileToString(configFile, Charset.forName("utf-8"))).build();
			List<TemplateConfiguration> templateConfigurations = patcher.getTemplateConfigurations();
			if(templateConfigurations != null) {
				sendPreparationEvent(true, templateConfigurations);
			} else {
				sendPreparationEvent(false, new ArrayList<>());
			}
		} catch (Exception e) {
			e.printStackTrace();
			sendPreparationEvent(false, new ArrayList<>());
		}
	}

	@ServiceAction
	protected void patchApkIfPossible(ApkInfo apkInfo, List<TemplateConfiguration> templates) {
		File targetCacheDir = new File(getCacheDir(), apkInfo.getPackageName());
		try {
			File cachedApkFile = apkExtractor.copyApkToCache(targetCacheDir, apkInfo);
			apkExtractor.extractAssetsFromApk(cachedApkFile, targetCacheDir);
			FileUtils.forceDelete(cachedApkFile);

			cachedAssetDir = new File(targetCacheDir, ASSETS_DIR);
			boolean configPresent = checkThemeConfigPresent(cachedAssetDir);
			Log.d(TAG, "Is ThemeConfig Present: " + configPresent);

			if (!configPresent) {
				return;
			}

			File configFile = new File(cachedAssetDir, THEME_CONFIG_RELATIVE_LOCATION + THEME_CONFIG_FILE_NAME);
			TemplatePatcher patcher = TemplatePatcher.Builder.fromJson(FileUtils.readFileToString(configFile, Charset.forName("utf-8"))).build();
			patcher.setTemplateConfigurations(templates);
			boolean patchSuccess = patcher.patchAll(this);
			sendResultBroadcast(patchSuccess, cachedAssetDir);
			sendEvent(cachedAssetDir);
		} catch (Exception e) {
			e.printStackTrace();
			sendResultBroadcast(false, null);
		}
	}

	@Override
	public File resolveTemplateFileName(String templateName) throws PatchingException {
		return new File(cachedAssetDir, THEME_CONFIG_RELATIVE_LOCATION + templateName);
	}

	@Override
	public String getPatchedFileName(TemplateConfiguration templateConfiguration) {
		return String.format(THEME_NAME_TEMPLATE, templateConfiguration.getTemplateType(), templateConfiguration.getTemplateName());
	}

	private void sendEvent(File cachedAssetsFile) {
		File overlayDir;

		if(!FULL_PACKAGE) {
			overlayDir = new File(cachedAssetsFile, THEME_CONFIG_RELATIVE_LOCATION);
		} else {
			overlayDir = new File(cachedAssetsFile, OVERLAYS_DIR);

		}

		if(!overlayDir.exists()) {
			overlayDir.mkdirs();
		}

		Collection<File> overlays = FileUtils.listFiles(overlayDir, null, true);

		ImportAssetsFromApkEvent event = new ImportAssetsFromApkEvent();
		event.withList(convertCollectionToAssetsFileInfo(overlays, AssetsType.OVERLAYS));

		if(FULL_PACKAGE) {

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

			Collection<File> fonts = FileUtils.listFiles(fontsDir, null, true);
			Collection<File> audio = FileUtils.listFiles(audioDir, null, true);
			Collection<File> bootAnimation = FileUtils.listFiles(bootAnimationDir, null, true);

			event.withList(convertCollectionToAssetsFileInfo(fonts, AssetsType.FONTS));
			event.withList(convertCollectionToAssetsFileInfo(audio, AssetsType.AUDIO));
			event.withList(convertCollectionToAssetsFileInfo(bootAnimation, AssetsType.BOOT_ANIMATIONS));
		}

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
