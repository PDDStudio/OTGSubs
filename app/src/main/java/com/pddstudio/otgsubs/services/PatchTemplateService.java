package com.pddstudio.otgsubs.services;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.pddstudio.otgsubs.EventBusBean;
import com.pddstudio.otgsubs.events.ImportAssetsFromApkEvent;
import com.pddstudio.substratum.packager.ApkExtractor;
import com.pddstudio.substratum.packager.models.ApkInfo;
import com.pddstudio.substratum.packager.models.AssetFileInfo;
import com.pddstudio.substratum.packager.models.AssetsType;
import com.pddstudio.substratum.template.patcher.PatchingException;
import com.pddstudio.substratum.template.patcher.TemplatePatcher;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.ServiceAction;
import org.androidannotations.api.support.app.AbstractIntentService;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
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
	public static final String EXTRA_PATCHING_SUCCESS = "com.pddstudio.otgsubs.services.EXTRA_PATCHING_SUCCESS";
	public static final String EXTRA_PATCHING_ASSETS = "com.pddstudio.otgsubs.services.EXTRA_PATCHING_ASSETS";

	private static final String ASSETS_DIR         = "assets";
	private static final String OVERLAYS_DIR       = "overlays";
	private static final String AUDIO_DIR          = "audio";
	private static final String FONTS_DIR          = "fonts";
	private static final String BOOT_ANIMATION_DIR = "bootanimation";

	private static final String THEME_CONFIG_RELATIVE_LOCATION = "overlays/android/";
	private static final String THEME_CONFIG_FILE_NAME         = "theme_config.json";

	private static final String THEME_NAME_TEMPLATE = "type1a_%s.xml";

	public static void patchTargetTheme(@NonNull Context context, @NonNull ApkInfo apkInfo, @Nullable String title, @NonNull HashMap<String, String> themeColorMappings) {
		if(title == null) {
			title = "PatchedColor";
		}
		PatchTemplateService_.intent(context).patchApkIfPossible(apkInfo, title, themeColorMappings).start();
	}

	@Bean
	protected ApkExtractor apkExtractor;

	@Bean
	protected EventBusBean eventBus;

	private File cachedAssetDir;
	private HashMap<String, String> themeColorMappings;
	private String name;

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

	@ServiceAction
	protected void patchApkIfPossible(ApkInfo apkInfo, String name, HashMap<String, String> themeColorMappings) {
		this.name = name;
		this.themeColorMappings = themeColorMappings;
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
			boolean patchSuccess = patcher.patch(this);
			sendResultBroadcast(patchSuccess, cachedAssetDir);
			sendEvent(cachedAssetDir);
		} catch (Exception e) {
			e.printStackTrace();
			sendResultBroadcast(false, null);
		}
	}

	@Override
	public HashMap<String, String> getPatchedValuesMappings() {
		return themeColorMappings;
	}

	@Override
	public File resolveTemplateFileName(String templateName) throws PatchingException {
		return new File(cachedAssetDir, THEME_CONFIG_RELATIVE_LOCATION + templateName);
	}

	@Override
	public String getPatchedFileName() {
		return getThemeName();
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

	private String getThemeName() {
		return String.format(THEME_NAME_TEMPLATE, name);
	}

}
