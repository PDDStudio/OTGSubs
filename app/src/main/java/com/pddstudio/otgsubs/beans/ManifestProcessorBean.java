package com.pddstudio.otgsubs.beans;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.pddstudio.substratum.packager.models.ApkInfo;
import com.pddstudio.substratum.packager.utils.ApkHelper;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.List;

import java8.util.Objects;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

/**
 * Created by pddstudio on 28/04/2017.
 */

@EBean(scope = EBean.Scope.Singleton)
public class ManifestProcessorBean {

	private static final String TAG = ManifestProcessorBean.class.getSimpleName();

	private static final String OTG_SUBS_META_DATA_FLAG = "OTGSubs_Supported";

	@RootContext
	protected Context context;

	@Bean
	ApkHelper apkHelper;

	public List<ApkInfo> getSupportedThemes() {
		return StreamSupport.stream(apkHelper.getInstalledApks()).map(apkInfo -> {
			try {
				ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(apkInfo.getPackageName(), PackageManager.GET_META_DATA);
				if(applicationInfo == null) {
					return null;
				}

				Bundle bundle = applicationInfo.metaData;
				if(bundle == null) {
					return null;
				}

				boolean supported = bundle.getBoolean(OTG_SUBS_META_DATA_FLAG, false);
				if(supported) {
					Log.d(TAG, "Found OTGSubs supported apk: " + apkInfo.getPackageName());
					return apkInfo;
				} else {
					return null;
				}
			} catch (PackageManager.NameNotFoundException e) {
				//ignore
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	public boolean isPackageSupported(String packageName) {
		if(packageName == null || packageName.isEmpty()) {
			return false;
		}
		return StreamSupport.stream(getSupportedThemes()).anyMatch(apkInfo -> apkInfo.getPackageName().equals(packageName));
	}

	public ApkInfo getApkInfoForPackage(String packageName) {
		if(!isPackageSupported(packageName)) {
			return null;
		}
		return StreamSupport.stream(getSupportedThemes()).filter(apkInfo -> apkInfo.getPackageName().equals(packageName)).findAny().orElse(null);
	}

	public Context getRootContext() {
		return context;
	}

}
