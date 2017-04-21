package com.pddstudio.substratum.packager.utils;

import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by pddstudio on 20/04/2017.
 */

public class AssetUtils {

	public static boolean copyFromAssetsToCache(File cacheDir, AssetManager assetManager, String assetFileName) throws IOException {
		InputStream inputStream = assetManager.open(assetFileName);
		File outputFile = new File(cacheDir, assetFileName);
		OutputStream outputStream = new FileOutputStream(outputFile);
		copyFile(inputStream, outputStream);
		return outputFile.exists();
	}

	private static void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
	}

}
