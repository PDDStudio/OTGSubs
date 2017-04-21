package com.pddstudio.substratum.packager.utils;

import android.util.Log;

import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.commons.FileUtils;

import java.io.File;
import java.io.IOException;

import java8.util.stream.StreamSupport;
import kellinwood.security.zipsigner.ZipSigner;

/**
 * Created by pddstudio on 20/04/2017.
 */

public class ZipUtils {

	private static final String TAG = ZipUtils.class.getSimpleName();

	public static void extractZip(File zipFile, File destFile) {
		ZipUtil.unpack(zipFile, destFile);
	}

	public static void mergeDirectories(File destDir, File... directories) throws IOException {
		if(!destDir.exists()) {
			destDir.mkdirs();
		}
		for(File dir : directories) {
			Log.d(TAG, "Copying from " + dir.getAbsolutePath() + " to " + destDir.getAbsolutePath());
			StreamSupport.stream(org.apache.commons.io.FileUtils.listFiles(dir, null, true)).forEach(file -> {
				Log.d(TAG, "Content: " + file.getAbsolutePath());
			});
			FileUtils.copyDirectory(dir, destDir);
		}
	}

	public static File createApkFromDir(File dir, File destFile) throws Exception {
		ZipUtil.pack(dir, destFile);
		File signedApk = new File(destFile.getParent(), "app-signed.apk");
		ZipSigner zipSigner = new ZipSigner();
		zipSigner.setKeymode("auto-testkey");
		zipSigner.signZip(destFile.getAbsolutePath(), signedApk.getAbsolutePath());
		Log.d("ZipUtils", "signedZip() : file present -> " + signedApk.exists() + " at " + signedApk.getAbsolutePath());
		return signedApk;
	}

}
