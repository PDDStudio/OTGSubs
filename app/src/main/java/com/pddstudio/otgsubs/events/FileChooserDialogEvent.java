package com.pddstudio.otgsubs.events;

/**
 * Created by pddstudio on 21/04/2017.
 */

public class FileChooserDialogEvent {

	private String resultDir;
	private boolean openRequest;

	public FileChooserDialogEvent(boolean openRequest) {
		this.openRequest = openRequest;
	}

	public void setResultDir(String resultDir) {
		this.resultDir = resultDir;
	}

	public String getResultDir() {
		return resultDir;
	}

	public boolean isOpenRequest() {
		return openRequest;
	}
}
