package com.pddstudio.otgsubs.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.pddstudio.otgsubs.PatcherActivity_;
import com.pddstudio.otgsubs.R;
import com.pddstudio.otgsubs.beans.ManifestProcessorBean;
import com.pddstudio.substratum.packager.models.ApkInfo;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EReceiver;
import org.androidannotations.annotations.ReceiverAction;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.api.support.content.AbstractBroadcastReceiver;

/**
 * Created by pddstudio on 11/05/2017.
 */

@EReceiver
public class PackageManagerReceiver extends AbstractBroadcastReceiver {

	private static final String TAG             = PackageManagerReceiver.class.getSimpleName();
	private static final int    NOTIFICATION_ID = 42;

	@SystemService
	protected NotificationManager notificationManager;

	@Bean
	protected ManifestProcessorBean manifestProcessorBean;

	@ReceiverAction(actions = Intent.ACTION_PACKAGE_ADDED, dataSchemes = "package")
	protected void onPackageAdded(Intent intent) {
		Log.d(TAG, "onPackageAdded() : " + intent);
		validatePackageCompatibility(intent);
	}

	@ReceiverAction(actions = Intent.ACTION_PACKAGE_REPLACED, dataSchemes = "package")
	protected void onPackageUpdated(Intent intent) {
		Log.d(TAG, "onPackageUpdated() : " + intent);
		validatePackageCompatibility(intent);
	}

	private void validatePackageCompatibility(Intent intent) {
		int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);

		if (uid == -1) {
			return;
		}

		PackageManager packageManager = manifestProcessorBean.getRootContext().getPackageManager();
		String packageName = packageManager.getNameForUid(uid);
		if (manifestProcessorBean.isPackageSupported(packageName)) {
			ApkInfo info = manifestProcessorBean.getApkInfoForPackage(packageName);
			if (info != null) {
				sendNotificationForPackage(info);
			}
		}

	}

	private void sendNotificationForPackage(ApkInfo apkInfo) {
		Context context = manifestProcessorBean.getRootContext();
		String title = context.getString(R.string.notification_otgsubs_theme_found_title);
		String summary = context.getString(R.string.notification_otgsubs_theme_found_summary, apkInfo.getAppName());

		Intent intent = PatcherActivity_.intent(context).apkInfo(apkInfo).get();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
				.setContentTitle(title)
				.setContentText(summary)
				.setSmallIcon(R.mipmap.ic_launcher)
				.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
				.setAutoCancel(true)
				.setContentIntent(pendingIntent);

		notificationManager.notify(NOTIFICATION_ID, builder.build());
	}

}
