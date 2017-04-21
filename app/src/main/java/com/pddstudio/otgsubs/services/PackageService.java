package com.pddstudio.otgsubs.services;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.pddstudio.substratum.packager.PackageCallback;
import com.pddstudio.substratum.packager.SubstratumPackager;

import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.ServiceAction;
import org.androidannotations.api.support.app.AbstractIntentService;
import org.zeroturnaround.zip.commons.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import java8.util.stream.StreamSupport;

/**
 * Created by pddstudio on 21/04/2017.
 */
@EIntentService
public class PackageService extends AbstractIntentService implements PackageCallback {

	public static final String TAG = PackageService.class.getSimpleName();

	public static final String ACTION_PACKAGING_DONE = "com.pddstudio.otgsubs.services.ACTION_PACKAGING_DONE";
	public static final String EXTRA_PACKAGING_DONE_STATUS = "com.pddstudio.otgsubs.services.EXTRA_PACKAGING_DONE_STATUS";
	public static final String EXTRA_PACKAGING_DONE_FILE = "com.pddstudio.otgsubs.services.EXTRA_PACKAGING_DONE_FILE";

	public static void packageApplication(@NonNull Context context, @Nullable List<String> assetsList) {
		PackageService_.intent(context).doPackage(assetsList).start();
	}

	private SubstratumPackager packager;

	public PackageService() {
		super(PackageService.class.getSimpleName());
	}

	private void sendPackagingResultBroadcast(boolean packageSuccess, @Nullable File result) {
		Intent intent = new Intent(ACTION_PACKAGING_DONE);
		intent.putExtra(EXTRA_PACKAGING_DONE_STATUS, packageSuccess);
		if(result != null) {
			intent.putExtra(EXTRA_PACKAGING_DONE_FILE, result.getAbsolutePath());
		}
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	@ServiceAction
	void doPackage(@Nullable List<String> fileList) {
		SubstratumPackager.Builder builder = new SubstratumPackager.Builder(this);
		if(fileList != null && !fileList.isEmpty()) {
			StreamSupport.stream(fileList)
						 .map(File::new)
						 .peek(file -> Log.d(TAG, "adding directory: " + file.getAbsolutePath()))
						 .forEach(builder::addAssetsDir);
		}
		packager = builder.build();
		packager.doWork(this);
	}

	@Override
	public void onPackagingFailed(int errorCode) {
		sendPackagingResultBroadcast(false, null);
	}

	@Override
	public void onPackagingSucceeded(File signedApk) {
		if(signedApk != null && signedApk.exists()) {
			try {
				File resultApk = new File(Environment.getExternalStorageDirectory(), "otg.subs." + System.nanoTime() + ".apk");
				FileUtils.copyFile(signedApk, resultApk);
				if(resultApk.exists()) {
					sendPackagingResultBroadcast(true, resultApk);
				} else {
					throw new IOException("Couldn't copy file from " + signedApk + " to " + resultApk);
				}
			} catch (IOException e) {
				e.printStackTrace();
				sendPackagingResultBroadcast(true, signedApk);
			}
		}
		packager.cleanCache();
	}

}
