package com.pddstudio.otgsubs;

import android.app.Application;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.androidannotations.annotations.EApplication;

/**
 * Created by pddstudio on 25/04/2017.
 */

@EApplication
public class OTGSubsApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		if(BuildConfig.DEBUG) {
			FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false);
		}
	}

}
