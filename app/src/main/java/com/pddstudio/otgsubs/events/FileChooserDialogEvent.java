package com.pddstudio.otgsubs.events;

import com.pddstudio.otgsubs.models.FileChooserType;

/**
 * Created by pddstudio on 21/04/2017.
 */

public class FileChooserDialogEvent {

	private String resultLocation;
	private boolean openRequest;
	private FileChooserType fileChooserType;

	public FileChooserDialogEvent(boolean openRequest, FileChooserType fileChooserType) {
		this.openRequest = openRequest;
		this.fileChooserType = fileChooserType;
	}

	public void setResult(String resultLocation) {
		this.resultLocation = resultLocation;
	}

	public String getResultLocation() {
		return resultLocation;
	}

	public boolean isOpenRequest() {
		return openRequest;
	}

	public FileChooserType getFileChooserType() {
		return fileChooserType;
	}

}
