package com.pddstudio.substratum.template.patcher.internal;

/**
 * Created by pddstudio on 25/04/2017.
 */

public abstract class LinePatcher implements Patcher<String, String, String> {

	@Override
	public String patch(String target, String key, String value) {
		return patchLine(target, key, value);
	}

	public abstract String patchLine(String line, String targetKey, String replacementValue);
}
