package com.pddstudio.otgsubs.utils;

import com.pddstudio.otgsubs.R;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.DefaultInt;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

/**
 * Created by pddstudio on 28/07/16.
 */
@SharedPref(SharedPref.Scope.UNIQUE)
public interface Preferences {

	@DefaultBoolean(value = false, keyRes = R.string.pref_item_tint_nav_bar_key)
	boolean tintNavigationBar();

	@DefaultBoolean(value = true, keyRes = R.string.pref_item_crash_reports_key)
	boolean crashReportEnabled();

	@DefaultBoolean(value = true, keyRes = R.string.pref_item_install_build_apk_key)
	boolean openPackageInstaller();

	@DefaultInt(value = 0, keyRes = R.string.pref_item_app_theme_key)
	int themeId();

	@DefaultBoolean(value = false, keyRes = R.string.pref_item_app_theme_changed_runtime_key)
	boolean themeChanged();

}
