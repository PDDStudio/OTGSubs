package com.pddstudio.otgsubs.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.Log;
import android.util.TypedValue;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.pddstudio.otgsubs.R;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.sharedpreferences.Pref;

/**
 * Created by pddstudio on 29/07/16.
 */
@EBean(scope = EBean.Scope.Singleton)
public class DeviceUtils {

	@RootContext
	Context context;

	@Pref
	Preferences_ preferences;

	public boolean isLollipopOrHigher() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
	}

	public Context getRootContext() {
		return context;
	}

	@ColorInt
	private int getColorForTheme(Activity activity) {
		TypedValue typedValue = new TypedValue();

		TypedArray a = activity.obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimary});
		int color = a.getColor(0, 0);

		a.recycle();

		return color;
	}

	public void setNavigationBarColor(Activity activity) {
		if (isLollipopOrHigher()) {
			if (preferences.tintNavigationBar().get()) {
				activity.getWindow().setNavigationBarColor(getColorForTheme(activity));
			} else {
				activity.getWindow().setNavigationBarColor(context.getResources().getColor(android.R.color.black));
			}
		}
	}

	public void reloadCrashReportConfig() {
		Log.d("DeviceUtils", "reloadCrashReportConfig() enabled => " + preferences.crashReportEnabled().get());
		FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(preferences.crashReportEnabled().get());
	}

	public void setCrashReportsEnabled(boolean enabled) {
		Log.d("DeviceUtils", "setCrashReportsEnabled() changed to => " + enabled);
		FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(enabled);
	}

}