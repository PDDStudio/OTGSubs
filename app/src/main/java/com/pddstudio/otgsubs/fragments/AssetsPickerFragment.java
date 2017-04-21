package com.pddstudio.otgsubs.fragments;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.pddstudio.otgsubs.EventBusBean;
import com.pddstudio.otgsubs.R;
import com.pddstudio.otgsubs.events.FileChooserDialogEvent;
import com.pddstudio.otgsubs.services.PackageService;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pddstudio on 21/04/2017.
 */
@EFragment(R.layout.fragment_assets_picker)
public class AssetsPickerFragment extends Fragment {

	public static final String TAG = AssetsPickerFragment.class.getSimpleName();

	public static AssetsPickerFragment createInstance() {
		return AssetsPickerFragment_.builder().build();
	}

	private List<String> assets = new ArrayList<>();
	private MaterialDialog loadingDialog;

	@Bean
	protected EventBusBean eventBus;

	@ViewById(R.id.assets_dir_text_view)
	protected TextView selectedDirTextView;

	private void toggleLoadingDialog(boolean showDialog) {
		if(showDialog) {
			loadingDialog = new MaterialDialog.Builder(getContext())
					.title(R.string.dialog_packaging_title)
					.content(R.string.dialog_packaging_content)
					.progress(true, -1)
					.cancelable(false)
					.canceledOnTouchOutside(false)
					.autoDismiss(false)
					.show();
		} else {
			if(loadingDialog != null && loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
			loadingDialog = null;
		}
	}

	@Click(R.id.assets_picker_button)
	protected void onAssetsPickerButtonClicked() {
		eventBus.post(new FileChooserDialogEvent(true));
	}

	@Click(R.id.build_apk_button)
	protected void onBuildApkButtonClicked() {
		toggleLoadingDialog(true);
		PackageService.packageApplication(getContext(), assets);
	}

	@Receiver(actions = PackageService.ACTION_PACKAGING_DONE, local = true)
	protected void onPackagingResultReceived(Intent intent) {
		boolean packagingSuccess = intent.getBooleanExtra(PackageService.EXTRA_PACKAGING_DONE_STATUS, false);
		String apkPath = intent.getStringExtra(PackageService.EXTRA_PACKAGING_DONE_FILE);
		toggleLoadingDialog(false);
		if(packagingSuccess) {
			Toast.makeText(getContext(), "APK Created: " + apkPath, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(getContext(), "Failed to create APK!", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		eventBus.register(this);
	}

	@Override
	public void onStop() {
		eventBus.unregister(this);
		super.onStop();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onDirPicked(FileChooserDialogEvent event) {
		if(event != null && !event.isOpenRequest()) {
			String result = event.getResultDir();
			Log.d(TAG, "ResultDir: " + result);
			if(result != null) {
				//TODO: allow multiple resources to be selected via picker
				selectedDirTextView.setText(result);
				assets.add(result);
			}
		}
	}

}
