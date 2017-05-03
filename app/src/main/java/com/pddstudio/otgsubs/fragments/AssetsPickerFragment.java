package com.pddstudio.otgsubs.fragments;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.pddstudio.otgsubs.beans.EventBusBean;
import com.pddstudio.otgsubs.R;
import com.pddstudio.otgsubs.events.FileChooserDialogEvent;
import com.pddstudio.otgsubs.models.FileChooserType;
import com.pddstudio.otgsubs.services.PackageService;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.CheckedChange;
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

	@ViewById(R.id.theme_mode_radio_group)
	protected RadioGroup themeModeRadioGroup;

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

	private void setThemeTemplate(boolean enabled) {
		Toast.makeText(getContext(), "Scheme Extension Enabled:" + enabled, Toast.LENGTH_SHORT).show();
	}

	@AfterViews
	protected void setupUi() {
		themeModeRadioGroup.check(R.id.radio_button_new_theme);
	}

	@Click(R.id.assets_picker_button)
	protected void onAssetsPickerButtonClicked() {
		eventBus.post(new FileChooserDialogEvent(true, FileChooserType.IGNORE));
	}

	@Click(R.id.build_apk_button)
	protected void onBuildApkButtonClicked() {
		toggleLoadingDialog(true);
		PackageService.packageApplication(getContext(), assets);
	}

	@CheckedChange({ R.id.radio_button_new_theme, R.id.radio_button_existing_theme})
	protected void onCheckboxChanged(CompoundButton radioButton, boolean isChecked) {
		switch(radioButton.getId()) {
			case R.id.radio_button_new_theme:
				if(radioButton.isChecked()) {
					setThemeTemplate(true);
				}
				break;
			case R.id.radio_button_existing_theme:
				if(radioButton.isChecked()) {
					setThemeTemplate(false);
				}
				break;
			default:
				throw new RuntimeException("Unknown state!");
		}
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
			String result = event.getResultLocation();
			Log.d(TAG, "ResultDir: " + result);
			if(result != null) {
				//TODO: allow multiple resources to be selected via picker
				selectedDirTextView.setText(result);
				assets.add(result);
			}
		}
	}

}
