package com.pddstudio.substratum.template.patcher;

import com.pddstudio.phrase.java.Phrase;
import com.pddstudio.substratum.template.patcher.internal.LinePatcher;

/**
 * Created by pddstudio on 25/04/2017.
 */

final class XmlPatcher extends LinePatcher {

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
		return Phrase.from(line).put(targetKey, replacementValue).formatString();
	}

}
