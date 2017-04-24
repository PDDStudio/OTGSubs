package com.pddstudio.otgsubs.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.pddstudio.otgsubs.fragments.AssetsListFragment;
import com.pddstudio.substratum.packager.models.AssetsType;

/**
 * Created by pddstudio on 24/04/2017.
 */

public class AssetsPageAdapter extends FragmentStatePagerAdapter {

	private static final int PAGES_COUNT = 4;

	private Context context;

	public AssetsPageAdapter(Context context, FragmentManager fm) {
		super(fm);
		this.context = context.getApplicationContext();
	}

	private String getTitleForAssetsType(AssetsType assetsType) {
		return context.getString(assetsType.getTitleRes());
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case 0:
				return AssetsListFragment.newInstance(AssetsType.FONTS);
			case 1:
				return AssetsListFragment.newInstance(AssetsType.OVERLAYS);
			case 2:
				return AssetsListFragment.newInstance(AssetsType.AUDIO);
			case 3:
				return AssetsListFragment.newInstance(AssetsType.BOOT_ANIMATIONS);
			default:
				throw new RuntimeException("Unknown fragment position " + position);

		}
	}

	@Override
	public int getCount() {
		return PAGES_COUNT;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
			case 0:
				return getTitleForAssetsType(AssetsType.FONTS);
			case 1:
				return getTitleForAssetsType(AssetsType.OVERLAYS);
			case 2:
				return getTitleForAssetsType(AssetsType.AUDIO);
			case 3:
				return getTitleForAssetsType(AssetsType.BOOT_ANIMATIONS);
			default:
				throw new RuntimeException("Unknown fragment position " + position);

		}
	}
}
