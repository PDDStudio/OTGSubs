package com.pddstudio.substratum.packager;

import java.io.File;

/**
 * Created by pddstudio on 21/04/2017.
 */

public interface PackageCallback {
	void onPackagingFailed(int errorCode);
	void onPackagingSucceeded(File signedApk);
}
