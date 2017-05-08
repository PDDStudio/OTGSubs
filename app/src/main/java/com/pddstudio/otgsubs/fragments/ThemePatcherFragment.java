package com.pddstudio.otgsubs.fragments;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.pddstudio.otgsubs.R;
import com.pddstudio.otgsubs.beans.EventBusBean;
import com.pddstudio.otgsubs.beans.ManifestProcessorBean;
import com.pddstudio.otgsubs.dialogs.InputDialog;
import com.pddstudio.otgsubs.events.ColorChooserDialogEvent;
import com.pddstudio.otgsubs.events.PatchThemeDialogEvent;
import com.pddstudio.otgsubs.events.PatchThemePreparationEvent;
import com.pddstudio.otgsubs.services.PatchTemplateService;
import com.pddstudio.otgsubs.utils.FormattingUtils;
import com.pddstudio.otgsubs.views.TemplateItemView;
import com.pddstudio.substratum.packager.models.ApkInfo;
import com.pddstudio.substratum.template.patcher.TemplateConfiguration;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;

import java8.util.stream.StreamSupport;

/**
 * Created by pddstudio on 26/04/2017.
 */

@EFragment(R.layout.fragment_theme_patcher)
public class ThemePatcherFragment extends Fragment implements TemplateItemView.OpenColorPickerCallback {

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

	@Bean
	protected InputDialog inputDialog;

	@ViewById(R.id.theme_author_text)
	protected TextView authorTextView;

	@ViewById(R.id.theme_name_text)
	protected TextView themeNameTextView;

	@ViewById(R.id.theme_patcher_linear_layout)
	protected LinearLayout themePatcherLayout;

	private List<TemplateConfiguration> templates;
	private MaterialDialog preparationDialog;

	@Subscribe
	public void onColorSelectionCompleted(ColorChooserDialogEvent event) {
		if (event != null && !event.isOpenRequest()) {
			String resultColor = event.getResultColor();
			updateTemplate(event.getTemplateId(), event.getTemplateKey(), resultColor);
			Log.d(TAG, "Selected Color: " + resultColor + " id: " + event.getTemplateId());
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onPatchingPreparationResultReceived(PatchThemePreparationEvent event) {
		togglePreparationDialog(false);
		if (event != null && event.isSuccess()) {
			this.templates = event.getTemplateConfigurations();
			if(event.getThemeConfiguration() != null) {
				themeNameTextView.setText(event.getThemeConfiguration().getThemeName());
				authorTextView.setText(event.getThemeConfiguration().getThemeAuthor());
			}
			onPreparationSucceeded();
		} else {
			onPreparationFailed();
		}
	}

	@AfterViews
	protected void prepareManifest() {
		togglePreparationDialog(true);
		PatchTemplateService.prepareTargetTheme(getContext(), apkInfo);
		getActivity().findViewById(R.id.build_patched_theme_btn).setNestedScrollingEnabled(false);
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

	@Click(R.id.build_patched_theme_btn)
	public void onBuildPatchedTheme() {
		//TODO: update names for each template
		inputDialog.show(R.string.dialog_name_title, R.string.dialog_name_content, dialogInput -> {
			String templateName = FormattingUtils.formatUserThemeName(dialogInput);
			updateTemplateNames(templateName);
			inputDialog.dismiss();
			eventBus.post(new PatchThemeDialogEvent(true));
			PatchTemplateService.patchTargetTheme(getContext(), apkInfo, templates);
		});
	}

	private void onPreparationFailed() {
		//TODO: show error dialog and quit activity when pressing "okay"
		getActivity().finish();
	}

	private void onPreparationSucceeded() {
		//TODO: create TemplateItemView for each TemplateConfig and add it to this layout
		StreamSupport.stream(templates).peek(templateConfiguration -> Log.d(TAG, "Template: " + templateConfiguration.getTemplateId())).forEach(templateConfiguration -> {
			TemplateItemView templateItemView = new TemplateItemView(getContext());
			templateItemView.setTemplateConfiguration(templateConfiguration, ThemePatcherFragment.this);
			templateItemView.setTitle(templateConfiguration.getTemplateTitle());
			templateItemView.setDescription(templateConfiguration.getTemplateDescription());
			themePatcherLayout.addView(templateItemView);
		});
	}

	private void updateTemplateNames(String name) {
		if(name == null || name.isEmpty()) {
			//in case the name is missing "OTGSubs" is used instead of null.
			StreamSupport.stream(templates).forEach(templateConfiguration -> templateConfiguration.setTemplateName(getString(R.string.app_name)));
		} else {
			StreamSupport.stream(templates).forEach(templateConfiguration -> templateConfiguration.setTemplateName(name));
		}
		StreamSupport.stream(templates).forEach(templateConfiguration -> Log.d(TAG, "updateName() : " + templateConfiguration.getTemplateName()));
	}

	private void updateTemplate(String templateId, String templateKey, String templateValue) {
		StreamSupport.stream(templates).filter(templateConfiguration -> templateConfiguration.getTemplateId().equals(templateId)).forEach(templateConfiguration -> {
			HashMap<String, String> map = templateConfiguration.getThemeMappings();
			map.put(templateKey, templateValue);
			templateConfiguration.updateThemeMappings(map);
		});
		themePatcherLayout.removeAllViews();
		onPreparationSucceeded();
	}

	@Override
	public void openColorPicker(TemplateConfiguration templateConfiguration, String key, String value) {
		ColorChooserDialogEvent event = new ColorChooserDialogEvent(true, templateConfiguration.getTemplateId()).withTemplateKey(key);
		eventBus.post(event);
	}

	private void togglePreparationDialog(boolean showDialog) {
		if (showDialog) {
			preparationDialog = new MaterialDialog.Builder(getContext()).title(R.string.dialog_preparing_title)
															.content(R.string.dialog_preparing_content)
															.progress(true, -1)
															.cancelable(false)
															.canceledOnTouchOutside(false)
															.autoDismiss(false)
															.show();
		} else {
			if (preparationDialog != null && preparationDialog.isShowing()) {
				preparationDialog.dismiss();
			}
			preparationDialog = null;
		}
	}

}
