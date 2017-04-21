package com.pddstudio.otgsubs.utils;

import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;

import static com.mikepenz.materialdrawer.icons.MaterialDrawerFont.Icon.mdf_person;

/**
 * Created by pddstudio on 21/04/2017.
 */

public final class DrawerUtils {

	private DrawerUtils() {}

	public static PrimaryDrawerItem createPrimaryDrawerItem(String name, String desc, long id) {
		return new PrimaryDrawerItem().withName(name).withDescription(desc).withIcon(mdf_person).withIdentifier(id);
	}
}
