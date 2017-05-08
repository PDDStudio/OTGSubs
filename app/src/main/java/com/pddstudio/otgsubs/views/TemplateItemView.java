package com.pddstudio.otgsubs.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pddstudio.otgsubs.R;
import com.pddstudio.substratum.template.patcher.TemplateConfiguration;

import java8.util.stream.StreamSupport;

/**
 * Created by pddstudio on 04/05/2017.
 */

public class TemplateItemView extends RelativeLayout {

	private TemplateConfiguration templateConfiguration;
	private OpenColorPickerCallback callback;

	private LinearLayout layout;
	private TextView titleText;
	private TextView descText;

	public TemplateItemView(Context context) {
		super(context);
		View view = LayoutInflater.from(context).inflate(R.layout.view_template_item, this, true);
		layout = (LinearLayout) view.findViewById(R.id.template_layout);
		titleText = (TextView) view.findViewById(R.id.template_title);
		descText = (TextView) view.findViewById(R.id.template_description);
	}

	public void setTemplateConfiguration(TemplateConfiguration templateConfiguration, OpenColorPickerCallback callback) {
		this.templateConfiguration = templateConfiguration;
		this.callback = callback;
		createItemsForTemplateConfiguration();
	}

	public void setTitle(String title) {
		this.titleText.setText(title);
	}

	public void setDescription(String description) {
		this.descText.setText(description);
	}

	private void createItemsForTemplateConfiguration() {
		StreamSupport.stream(templateConfiguration.getThemeMappings().keySet()).forEach(key -> {
			String value = templateConfiguration.getThemeMappings().get(key);
			ColorPickerView colorPickerView = new ColorPickerView(getContext());
			colorPickerView.setPickerConfig(templateConfiguration, callback, key, value);
			layout.addView(colorPickerView);
		});
	}

	public interface OpenColorPickerCallback {
		void openColorPicker(TemplateConfiguration templateConfiguration, String forKey, String forValue);
	}

}
