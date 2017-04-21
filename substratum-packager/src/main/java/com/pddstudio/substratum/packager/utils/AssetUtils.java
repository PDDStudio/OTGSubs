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

public class ZipUtils {

	public static boolean copyFromAssetsToCache(File cacheDir, AssetManager assetManager, String assetFileName) throws IOException {
		InputStream inputStream = assetManager.open(assetFileName);
		File outputFile = new File(cacheDir, assetFileName);
		OutputStream outputStream = new FileOutputStream(outputFile);
	}

}
