package com.pddstudio.otgsubs.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.pddstudio.otgsubs.R;
import com.pddstudio.substratum.template.patcher.TemplateConfiguration;

/**
 * Created by pddstudio on 04/05/2017.
 */

public class ColorPickerView extends RelativeLayout implements View.OnClickListener {

	private Button colorPickerButton;
	private ImageView colorImageView;

	private TemplateConfiguration templateConfiguration;
	private TemplateItemView.OpenColorPickerCallback callback;

	private String key;
	private String value;

	public ColorPickerView(Context context) {
		super(context);
		init();
	}

	public ColorPickerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	public void setColorPickerCallback(TemplateItemView.OpenColorPickerCallback callback) {
		this.callback = callback;
	}

	public void setPickerConfig(TemplateConfiguration templateConfiguration, TemplateItemView.OpenColorPickerCallback callback, String key, String value) {
		this.templateConfiguration = templateConfiguration;
		this.callback = callback;
		this.key = key;
		this.value = value;
		colorPickerButton.setText(getContext().getString(R.string.patcher_btn_color, key, value));
		changeCircleColor(value);
	}

	private void init() {
		View view = LayoutInflater.from(getContext()).inflate(R.layout.view_color_picker, this, true);
		colorPickerButton = (Button) view.findViewById(R.id.color_btn);
		colorPickerButton.setOnClickListener(this);
		colorImageView = (ImageView) view.findViewById(R.id.color_img);
	}

	@Override
	public void onClick(View v) {
		if(callback != null) {
			callback.openColorPicker(templateConfiguration, key, value);
		}
	}

	private void changeCircleColor(String colorValue) {
		GradientDrawable bgShape = (GradientDrawable) colorImageView.getDrawable();
		bgShape.setColor(Color.parseColor(colorValue));
	}
}
