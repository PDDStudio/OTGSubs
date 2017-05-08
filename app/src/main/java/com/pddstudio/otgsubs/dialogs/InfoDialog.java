package com.pddstudio.otgsubs.dialogs;

import android.content.Context;
import android.support.annotation.StringRes;

import com.afollestad.materialdialogs.MaterialDialog;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

/**
 * Created by pddstudio on 08/05/2017.
 */

@EBean
public class InfoDialog {

	@RootContext
	Context context;

	private MaterialDialog infoDialog;

	public void show(@StringRes int title, @StringRes int content) {
		infoDialog = new MaterialDialog.Builder(context).title(title).content(content).show();
	}

	public void dismiss() {
		if(infoDialog != null && infoDialog.isShowing()) {
			infoDialog.dismiss();
		}
	}

}
