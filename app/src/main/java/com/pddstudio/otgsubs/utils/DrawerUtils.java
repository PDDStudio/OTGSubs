package com.pddstudio.otgsubs.utils;

import android.support.annotation.StringRes;

import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;

/**
 * Created by pddstudio on 21/04/2017.
 */

public final class DrawerUtils {

	private DrawerUtils() {}

	public static PrimaryDrawerItem createPrimaryDrawerItem(IIcon itemIcon, @StringRes int name, @StringRes int desc, long id) {
		PrimaryDrawerItem drawerItem = new PrimaryDrawerItem().withName(name).withIcon(itemIcon).withIdentifier(id);
		if(desc != 0) {
			return drawerItem.withDescription(desc);
		} else {
			return drawerItem;
		}
	}

	public static SectionDrawerItem createSectionHeaderDrawerItem(@StringRes int nameStringRes, boolean divider) {
		return new SectionDrawerItem().withName(nameStringRes).withDivider(divider);
	}

}
