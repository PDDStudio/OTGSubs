package com.pddstudio.otgsubs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;

import com.pddstudio.otgsubs.utils.DeviceUtils;
import com.pddstudio.otgsubs.utils.Preferences;
import com.pddstudio.otgsubs.utils.Preferences_;
import com.pddstudio.otgsubs.utils.ThemeUtils;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterPreferences;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.PreferenceByKey;
import org.androidannotations.annotations.PreferenceChange;
import org.androidannotations.annotations.PreferenceScreen;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

@EActivity(R.layout.activity_settings)
public class SettingsActivity extends AppCompatActivity {

	public static void open(@NonNull Context context, int requestCode) {
		SettingsActivity_.intent(context).startForResult(requestCode);
	}

	@ViewById(R.id.toolbar)
	Toolbar toolbar;

	@Bean
	DeviceUtils deviceUtils;

	@Pref
	Preferences_ preferences;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeUtils.applySelectedTheme(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		deviceUtils.setNavigationBarColor(this);
	}

	@AfterViews
	void prepareLayout() {
		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		if (getFragmentManager().findFragmentById(R.id.pref_placeholder) == null) {
			getFragmentManager().beginTransaction()
								.replace(R.id.pref_placeholder, SettingsFragment.create())
								.commit();
		}
	}

	@AfterInject
	void prepareActivity() {
		deviceUtils.setNavigationBarColor(this);
	}

	@OptionsItem(android.R.id.home)
	void onHomePressed() {
		onBackPressed();
	}

	@Override
	public void onBackPressed() {
		Intent returnIntent = new Intent();
		setResult(RESULT_OK, returnIntent);
		finish();
	}

	@PreferenceScreen(R.xml.settings)
	@EFragment
	public static class SettingsFragment extends PreferenceFragment {

		public static final String PREFERENCE_NAME = Preferences.class.getSimpleName();

		public static SettingsFragment create() {
			return SettingsActivity_.SettingsFragment_.builder().build();
		}

		@Pref
		Preferences_ preferences;

		@Bean
		DeviceUtils deviceUtils;

		@PreferenceByKey(R.string.pref_item_tint_nav_bar_key)
		CheckBoxPreference tintNavigationBarPreference;

		@PreferenceByKey(R.string.pref_item_crash_reports_key)
		CheckBoxPreference crashReportPreference;

		@PreferenceByKey(R.string.pref_item_app_theme_key)
		ListPreference applicationThemePreference;

		@PreferenceByKey(R.string.pref_item_build_info_key)
		EditTextPreference editTextPreference;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			getPreferenceManager().setSharedPreferencesName(PREFERENCE_NAME);
		}

		@AfterPreferences
		void setupPreferences() {
			//prepare the tint checkbox preference
			tintNavigationBarPreference.setSummary(Html.fromHtml(getString(R.string.pref_item_tint_nav_bar_summary)));
			tintNavigationBarPreference.setEnabled(true);
			//prepare the crash report preference
			crashReportPreference.setChecked(preferences.crashReportEnabled().get());
			//prepare the application theme preference
			applicationThemePreference.setValueIndex(preferences.themeId().get());
			applicationThemePreference.setSummary(String.format(getString(R.string.pref_item_app_theme_summary), applicationThemePreference.getEntry()));
			//set build information
			String versionName = BuildConfig.VERSION_NAME;
			String versionCode = BuildConfig.VERSION_CODE + "";
			editTextPreference.setSummary(getString(R.string.pref_item_build_info_summary, versionName, versionCode));
		}

		@PreferenceChange(R.string.pref_item_tint_nav_bar_key)
		void onTintNavigationBarChanged(boolean newValue) {
			if (deviceUtils.isLollipopOrHigher()) {
				if (newValue) {
					getActivity().getWindow().setNavigationBarColor(getColorForTheme(getActivity()));
				} else {
					getActivity().getWindow().setNavigationBarColor(getResources().getColor(android.R.color.black));
				}
			}
		}

		@PreferenceChange(R.string.pref_item_crash_reports_key)
		void onCrashReportsChanged(boolean newValue) {
			deviceUtils.setCrashReportsEnabled(newValue);
		}

		@PreferenceChange(R.string.pref_item_app_theme_key)
		void onApplicationThemeChanged(int newValue) {
			applicationThemePreference.setSummary(String.format(getString(R.string.pref_item_app_theme_summary), getThemeNameForIndex(newValue)));
			ThemeUtils.restartActivity(getActivity());
			preferences.themeChanged().put(true);
			Log.d("SettingsFragment", "onApplicationThemeChanged() preferences theme changed => " + preferences.themeChanged().get());
		}

		private String getThemeNameForIndex(int index) {
			return getResources().getStringArray(R.array.pref_theme_strings)[index];
		}

		@ColorInt
		private int getColorForTheme(Activity activity) {
			TypedValue typedValue = new TypedValue();

			TypedArray a = activity.obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimary});
			int color = a.getColor(0, 0);

			a.recycle();

			return color;
		}

	}


}
