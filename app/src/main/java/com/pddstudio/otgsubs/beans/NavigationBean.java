package com.pddstudio.otgsubs.beans;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.pddstudio.otgsubs.R;
import com.pddstudio.otgsubs.SettingsActivity;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

/**
 * Created by pddstudio on 11/05/2017.
 */

@EBean
public class NavigationBean {

	@RootContext
	protected Context context;

	public void openAboutScreen() {
		String aboutAppName = context.getString(R.string.app_name);
		String aboutAppDesc = context.getString(R.string.about_app_desc);
		String activityTitle = context.getString(R.string.about_app_title);

		new LibsBuilder().withAboutAppName(aboutAppName)
						 .withAboutIconShown(true)
						 .withAboutDescription(aboutAppDesc)
						 .withAboutVersionShown(true)
						 .withAboutVersionShownName(true)
						 .withAboutVersionShownCode(true)
						 .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
						 .withActivityTitle(activityTitle)
						 .withAutoDetect(true)
						 .start(context);
	}

	public void openSettingsScreen(int settingsRequestCode) {
		SettingsActivity.open(context, settingsRequestCode);
	}

	public void openGitHubPage() {
		openWebUrl("https://github.com/PDDStudio/OTGSubs");
	}

	public void openWebUrl(String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		context.startActivity(intent);
	}

}
