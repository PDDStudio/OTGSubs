package com.pddstudio.otgsubs.events;

/**
 * Created by pddstudio on 04/05/2017.
 */

public class PatchThemeDialogEvent {

	private final boolean showDialog;

	public PatchThemeDialogEvent(boolean showDialog) {
		this.showDialog = showDialog;
	}

	public boolean isShowDialogEvent() {
		return showDialog;
	}
}
