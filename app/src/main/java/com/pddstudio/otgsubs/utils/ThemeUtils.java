package com.pddstudio.otgsubs.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.support.annotation.ColorInt;
import android.util.TypedValue;

import com.pddstudio.otgsubs.R;

/**
 * Created by pddstudio on 29/07/16.
 */
public class ThemeUtils {

	public static final String PREFERENCE_NAME = Preferences.class.getSimpleName();

	public static void applySelectedTheme(Activity activity) {
		SharedPreferences preferences = activity.getSharedPreferences(PREFERENCE_NAME, 0);
		String themePreference = activity.getString(R.string.pref_item_app_theme_key);
		switch (preferences.getString(themePreference, "0")) {
			case "0":
				activity.setTheme(R.style.AppTheme_Blue);
				break;
			case "1":
				activity.setTheme(R.style.AppTheme_Red);
				break;
			case "2":
				activity.setTheme(R.style.AppTheme_Green);
				break;
			case "3":
				activity.setTheme(R.style.AppTheme_DarkOrange);
				break;
		}
	}

	public static int getDrawerHeaderColorForSelectedTheme(Activity activity) {
		SharedPreferences preferences = activity.getSharedPreferences(PREFERENCE_NAME, 0);
		String themePreference = activity.getString(R.string.pref_item_app_theme_key);
		switch (preferences.getString(themePreference, "0")) {
			case "0":
			default:
				return R.color.colorPrimary;
			case "1":
				return R.color.colorPrimaryRed;
			case "2":
				return R.color.colorPrimaryGreen;
			case "3":
				return R.color.colorPrimaryDarkOrange;
		}
	}

	public static void restartActivity(Activity activity) {
		activity.finish();
		final Intent intent = activity.getIntent();
		activity.startActivity(intent);
	}

	@ColorInt
	public static int getPrimaryColorForTheme(Activity activity) {
		TypedValue typedValue = new TypedValue();

		TypedArray a = activity.obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimary});
		int color = a.getColor(0, 0);

		a.recycle();

		return color;
	}


}
