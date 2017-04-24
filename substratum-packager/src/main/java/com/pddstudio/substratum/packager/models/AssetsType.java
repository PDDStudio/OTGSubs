package com.pddstudio.substratum.packager.models;

import com.pddstudio.substratum.packager.R;

import java.io.Serializable;

/**
 * Created by pddstudio on 24/04/2017.
 */

public enum AssetsType implements Serializable {
	FONTS(R.string.fragment_title_fonts),
	OVERLAYS(R.string.fragment_title_overlays),
	AUDIO(R.string.fragment_title_audio),
	BOOT_ANIMATIONS(R.string.fragment_title_boot_animations);

	private final int titleName;

	AssetsType(int title) {
		this.titleName = title;
	}

	public int getTitleRes() {
		return titleName;
	}

}
