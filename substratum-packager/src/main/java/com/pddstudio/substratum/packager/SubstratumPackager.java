package com.pddstudio.substratum.packager;

import android.content.Context;
import android.util.Log;

import com.pddstudio.substratum.packager.utils.AssetUtils;
import com.pddstudio.substratum.packager.utils.ZipUtils;

import org.zeroturnaround.zip.commons.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java8.util.stream.StreamSupport;

/**
 * Created by pddstudio on 20/04/2017.
 */

public class SubstratumPackager {

	private static final String TAG = SubstratumPackager.class.getSimpleName();

	private static final String SOURCES_ZIP = "source.zip";
	private static final String SUBSTRATUM_ZIP = "substratum.zip";

	private final File       cacheDir;
	private final Context    context;
	private final List<File> assetDirs;

	private SubstratumPackager(Builder builder) {
		this.cacheDir = builder.cacheDir;
		this.context = builder.context.getApplicationContext();
		this.assetDirs = builder.assetDirs;
	}

	private boolean unzipDefaultArchives() {
		try {
			cleanCache();
			boolean copySources = AssetUtils.copyFromAssetsToCache(cacheDir, context.getAssets(), SOURCES_ZIP);
			boolean copySubs = AssetUtils.copyFromAssetsToCache(cacheDir, context.getAssets(), SUBSTRATUM_ZIP);
			return copySources && copySubs;
		} catch (IOException io) {
			io.printStackTrace();
			return false;
		}
	}

	public void doWork(PackageCallback packageCallback) {
		if(unzipDefaultArchives()) {
			File destDir = new File(cacheDir, "result");
			try {
				ZipUtils.extractZip(new File(cacheDir, SOURCES_ZIP), destDir);
				ZipUtils.extractZip(new File(cacheDir, SUBSTRATUM_ZIP), destDir);
				ZipUtils.mergeDirectories(destDir, assetDirs.toArray(new File[assetDirs.size()]));
				File apkFile = new File(cacheDir, "dummy.apk");
				File signedApk = ZipUtils.createApkFromDir(destDir, apkFile);
				if(signedApk.exists()) {
					packageCallback.onPackagingSucceeded(signedApk);
				} else {
					packageCallback.onPackagingFailed(-1);
				}
			} catch (Exception e) {
				e.printStackTrace();
				//TODO: implement proper error handling
				packageCallback.onPackagingFailed(0);
			}
		} else {
			packageCallback.onPackagingFailed(1);
		}
	}

	public void cleanCache() {
		try {
			StreamSupport.stream(org.apache.commons.io.FileUtils.listFiles(cacheDir, null, true)).forEach(file -> Log.d(TAG, "File to delete: " + file.getAbsolutePath()));
			FileUtils.cleanDirectory(cacheDir);
			Log.i(TAG, "Cache directory cleaned at " + cacheDir.getAbsolutePath());
		} catch (IOException io) {
			io.printStackTrace();
			Log.e(TAG, "Unable to clean cache directory!");
		}
	}

	public static final class Builder {

		private final File       cacheDir;
		private       Context    context;
		private       List<File> assetDirs;

		public Builder(Context context) {
			this.cacheDir = context.getCacheDir();
			this.context = context.getApplicationContext();
			this.assetDirs = new ArrayList<>();
		}

		public Builder addAssetsDir(File dir) {
			this.assetDirs.add(dir);
			return this;
		}

		public SubstratumPackager build() {
			return new SubstratumPackager(this);
		}

	}

}
