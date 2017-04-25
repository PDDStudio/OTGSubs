package com.pddstudio.otgsubs.utils;

import android.app.Activity;
import android.support.annotation.StringRes;
import android.support.v7.widget.Toolbar;

import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.pddstudio.otgsubs.R;

import org.androidannotations.annotations.EBean;

import java.util.List;

/**
 * Created by pddstudio on 21/04/2017.
 */

@EBean
public class DrawerUtils {

	DrawerUtils() {}

	public PrimaryDrawerItem createPrimaryDrawerItem(Activity activity, IIcon itemIcon, @StringRes int name, @StringRes int desc, long id, boolean selectable) {
		int primaryColor = ThemeUtils.getPrimaryColorForTheme(activity);
		PrimaryDrawerItem drawerItem = new PrimaryDrawerItem().withName(name)
															  .withIcon(itemIcon)
															  .withIdentifier(id)
															  .withSelectable(selectable)
															  .withSelectedTextColor(primaryColor)
															  .withSelectedIconColor(primaryColor);
		if(desc != 0) {
			return drawerItem.withDescription(desc);
		} else {
			return drawerItem;
		}
	}

	public SectionDrawerItem createSectionHeaderDrawerItem(@StringRes int nameStringRes, boolean divider) {
		return new SectionDrawerItem().withName(nameStringRes).withDivider(divider);
	}

	public AccountHeader createAccountHeader(Activity activity) {
		return new AccountHeaderBuilder().withActivity(activity)
										 .addProfiles(new ProfileDrawerItem().withName(activity.getString(R.string.drawer_header)).withEmail(activity.getString(R.string.drawer_header_subtitle)))
										 .withHeaderBackground(ThemeUtils.getDrawerHeaderColorForSelectedTheme(activity))
										 .withProfileImagesVisible(false)
										 .withSelectionListEnabled(false)
										 .withSelectionListEnabledForSingleProfile(false)
										 .withAlternativeProfileHeaderSwitching(false)
										 .withOnlyMainProfileImageVisible(false)
										 .build();
	}

	public Drawer createDrawer(Activity activity, Toolbar toolbar, int selectedItemId, Drawer.OnDrawerItemClickListener onDrawerItemClickListener, List<IDrawerItem> drawerItems) {
		return new DrawerBuilder().withActivity(activity)
								  .withToolbar(toolbar)
								  .withAccountHeader(createAccountHeader(activity))
								  .withActionBarDrawerToggle(true)
								  .withActionBarDrawerToggleAnimated(true)
								  .withCloseOnClick(true)
								  .withDrawerItems(drawerItems)
								  .withOnDrawerItemClickListener(onDrawerItemClickListener)
								  .withCloseOnClick(true)
								  .withDelayOnDrawerClose(150)
								  .withSelectedItem(selectedItemId)
								  .withFireOnInitialOnClick(true)
								  .build();
	}

}
