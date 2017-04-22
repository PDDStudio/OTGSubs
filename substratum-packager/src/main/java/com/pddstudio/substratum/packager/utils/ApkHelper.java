package com.pddstudio.substratum.packager.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.pddstudio.substratum.packager.models.ApkInfo;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.io.File;
import java.util.List;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

/**
 * Created by pddstudio on 21/04/2017.
 */

@EBean(scope = EBean.Scope.Singleton)
public class ApkHelper {

	private static final String TAG = ApkHelper.class.getSimpleName();

	@RootContext
	Context context;
	PackageManager packageManager;

	public List<ApkInfo> getInstalledApks() {
		packageManager = context.getPackageManager();
		List<PackageInfo> packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA);
		return StreamSupport.stream(packages).map(this::convertPackageInfoToApkInfo).filter(apkInfo -> apkInfo != null).collect(Collectors.toList());
	}

	private ApkInfo convertPackageInfoToApkInfo(PackageInfo packageInfo) {
		if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
			return new ApkInfo(packageManager.getApplicationLabel(packageInfo.applicationInfo).toString(),
							   packageInfo.packageName,
							   packageInfo.versionName,
							   packageInfo.applicationInfo.sourceDir,
							   packageInfo.applicationInfo.dataDir);
		} else {
			return null;
		}
	}

	public static Intent getShareIntent(File file) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		intent.setType("application/vnd.android.package-archive");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		return intent;
	}

}
