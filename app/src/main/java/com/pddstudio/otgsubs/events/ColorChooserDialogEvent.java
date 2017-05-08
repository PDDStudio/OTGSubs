package com.pddstudio.otgsubs.events;

/**
 * Created by pddstudio on 26/04/2017.
 */

public class ColorChooserDialogEvent {

	private String  resultColor;
	private boolean openRequest;
	private String templateId;
	private String templateKey;

	public ColorChooserDialogEvent(boolean openRequest, String templateId) {
		this.openRequest = openRequest;
		this.templateId = templateId;
	}

	public ColorChooserDialogEvent withTemplateKey(String templateKey) {
		this.templateKey = templateKey;
		return this;

	}

	public void setOpenRequest(boolean openRequest) {
		this.openRequest = openRequest;
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

	public String getTemplateId() {
		return templateId;
	}

	public String getTemplateKey() {
		return templateKey;
	}

}
