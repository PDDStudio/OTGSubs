package com.pddstudio.substratum.template.patcher;

import android.util.Log;

import com.pddstudio.phrase.java.Phrase;
import com.pddstudio.substratum.template.patcher.internal.LinePatcher;

/**
 * Created by pddstudio on 25/04/2017.
 */

final class XmlPatcher extends LinePatcher {

	private static final String TAG = XmlPatcher.class.getSimpleName();

	private static XmlPatcher INSTANCE;

	static XmlPatcher getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new XmlPatcher();
		}
		return INSTANCE;
	}

	private XmlPatcher() {}

	@Override
	public String patchLine(String line, String targetKey, String replacementValue) {
		try {
			Log.d(TAG, "Patch Line: " + line + " key: " + targetKey + " value: " + replacementValue);
			String result = Phrase.from(line, Phrase.KeyIdentifier.CURLY_BRACKETS).put(targetKey, replacementValue).formatString();
			Log.d(TAG, "Patch Succeeded! => " + result);
			return result;
		} catch (IllegalArgumentException e) {
			String replaceLine = line.replace("{" + targetKey + "}", replacementValue);
			Log.d(TAG, "Patching failed! Replacing legacy => " + replaceLine);
			return replaceLine;
		}
	}

}
