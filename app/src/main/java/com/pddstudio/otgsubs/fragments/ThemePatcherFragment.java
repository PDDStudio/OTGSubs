package com.pddstudio.otgsubs.fragments;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.pddstudio.otgsubs.EventBusBean;
import com.pddstudio.otgsubs.R;
import com.pddstudio.otgsubs.beans.ManifestProcessorBean;
import com.pddstudio.otgsubs.events.ColorChooserDialogEvent;
import com.pddstudio.otgsubs.models.ColorChooserType;
import com.pddstudio.otgsubs.services.PatchTemplateService;
import com.pddstudio.substratum.packager.models.ApkInfo;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.List;

/**
 * Created by pddstudio on 26/04/2017.
 */

@EFragment(R.layout.fragment_theme_patcher)
public class ThemePatcherFragment extends Fragment {

	public static final String TAG = ThemePatcherFragment.class.getSimpleName();

	public static ThemePatcherFragment newInstance(ApkInfo apkInfo) {
		return ThemePatcherFragment_.builder().apkInfo(apkInfo).build();
	}

	@FragmentArg
	protected ApkInfo apkInfo;

	@Bean
	protected EventBusBean eventBus;

	@Bean
	protected ManifestProcessorBean manifestProcessor;

	@ViewById(R.id.primary_color_dark_btn)
	protected Button primaryDarkColorButton;

	@ViewById(R.id.primary_color_btn)
	protected Button primaryColorButton;

	@ViewById(R.id.primary_color_dark_img)
	protected ImageView primaryColorDarkImage;

	@ViewById(R.id.primary_color_img)
	protected ImageView primaryColorImage;

	@ViewById(R.id.theme_name_input)
	protected EditText themeNameEditText;

	private HashMap<String, String> userMappings = new HashMap<>();

	@Subscribe
	public void onColorSelectionCompleted(ColorChooserDialogEvent event) {
		if(event != null && !event.isOpenRequest()) {
			String resultColor = event.getResultColor();
			Log.d(TAG, "Selected Color: " + resultColor + " type: " + event.getColorChooserType());
			switch (event.getColorChooserType()) {
				case IGNORE:
					default:
						return;
				case PRIMARY_COLOR:
					GradientDrawable bgShape = (GradientDrawable) primaryColorImage.getDrawable();
					bgShape.setColor(Color.parseColor(resultColor));
					primaryColorButton.setText(getString(R.string.patcher_btn_primary_color, resultColor));
					userMappings.put("primaryColor", resultColor);
					break;
				case PRIMARY_DARK_COLOR:
					GradientDrawable bgShape2 = (GradientDrawable) primaryColorDarkImage.getDrawable();
					bgShape2.setColor(Color.parseColor(resultColor));
					primaryDarkColorButton.setText(getString(R.string.patcher_btn_primary_dark_color, resultColor));
					userMappings.put("primaryColorDark", resultColor);
					break;
			}
		}
	}

	@AfterInject
	protected void prepareManifest() {
		List<ApkInfo> apkInfos = manifestProcessor.getSupportedThemes();
		Toast.makeText(this.getContext(), "Compatible themes found: " + apkInfos.size(), Toast.LENGTH_LONG).show();
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

	@Click({R.id.primary_color_dark_btn, R.id.primary_color_btn})
	protected void openColorPickerDialog(View view) {
		boolean isForPrimaryColor = view.getId() == R.id.primary_color_btn;
		ColorChooserDialogEvent colorChooserDialogEvent = new ColorChooserDialogEvent(true, isForPrimaryColor ? ColorChooserType.PRIMARY_COLOR : ColorChooserType.PRIMARY_DARK_COLOR);
		eventBus.post(colorChooserDialogEvent);
	}

	@Click(R.id.build_patched_theme_btn)
	protected void onBuildPatchedTheme() {
		PatchTemplateService.patchTargetTheme(getContext(), apkInfo, themeNameEditText.getText().toString(), userMappings);
	}

}
