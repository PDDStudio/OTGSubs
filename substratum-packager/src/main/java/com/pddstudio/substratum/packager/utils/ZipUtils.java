package com.pddstudio.substratum.packager.utils;

import android.util.Log;

import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.commons.FileUtils;

import java.io.File;
import java.io.IOException;

import kellinwood.security.zipsigner.ZipSigner;

/**
 * Created by pddstudio on 20/04/2017.
 */

public class ZipUtils {

	public static void extractZip(File zipFile, File destFile) {
		ZipUtil.unpack(zipFile, destFile);
	}

	public static void mergeDirectories(File destDir, File... directories) throws IOException {
		if(!destDir.exists()) {
			destDir.mkdirs();
		}
		for(File dir : directories) {
			FileUtils.copyDirectory(dir, destDir);
		}
	}

	public static void createApkFromDir(File dir, File destFile) throws Exception {
		ZipUtil.pack(dir, destFile);
		File signedApk = new File(destFile.getParent(), "app-signed.apk");
		ZipSigner zipSigner = new ZipSigner();
		zipSigner.setKeymode("auto-testkey");
		zipSigner.signZip(destFile.getAbsolutePath(), signedApk.getAbsolutePath());
		Log.d("ZipUtils", "signedZip() : file present -> " + signedApk.exists() + " at " + signedApk.getAbsolutePath());
	}

}
