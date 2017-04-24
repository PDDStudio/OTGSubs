package com.pddstudio.substratum.packager;

import android.util.Log;

import com.pddstudio.substratum.packager.models.ApkInfo;
import com.pddstudio.substratum.packager.utils.ApkHelper;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.commons.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import java8.util.stream.StreamSupport;

/**
 * Created by pddstudio on 21/04/2017.
 */

@EBean(scope = EBean.Scope.Singleton)
public class ApkExtractor {

	private static final String TAG = ApkExtractor.class.getSimpleName();

	private List<ApkInfo> apkInfos;

	@Bean
	protected ApkHelper apkHelper;

	@AfterInject
	protected void loadApks() {
		apkInfos = apkHelper.getInstalledApks();
		StreamSupport.stream(apkInfos).forEach(apkInfo -> Log.d(TAG, "APK INFO: " + apkInfo));
	}

	public File copyApkToCache(File cacheDir, ApkInfo apkInfo) throws IOException {
		File apkSource = new File(apkInfo.getSource());
		File outputLoc = new File(cacheDir, apkSource.getName());
		FileUtils.copyFile(apkSource, outputLoc);
		return outputLoc;
	}

	public List<ApkInfo> getApkInfoList() {
		return apkInfos;
	}

	public void extractAssetsFromApk(File cachedApkFile, File destDir) {
		ZipUtil.unpack(cachedApkFile, destDir, name -> name.startsWith("assets/") ? name : null);
	}

}
