package com.pddstudio.otgsubs.events;

import com.pddstudio.otgsubs.models.ColorChooserType;

/**
 * Created by pddstudio on 26/04/2017.
 */

public class ColorChooserDialogEvent {

	private String  resultColor;
	private boolean openRequest;
	private ColorChooserType colorChooserType;

	public ColorChooserDialogEvent(boolean openRequest, ColorChooserType colorChooserType) {
		this.openRequest = openRequest;
		this.colorChooserType = colorChooserType;
	}

	public void setResult(String resultLocation) {
		this.resultColor = resultLocation;
	}

	public String getResultColor() {
		return resultColor;
	}

	public boolean isOpenRequest() {
		return openRequest;
	}

	public ColorChooserType getColorChooserType() {
		return colorChooserType;
	}

}
