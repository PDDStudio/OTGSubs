package com.pddstudio.otgsubs.dialogs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.afollestad.materialdialogs.MaterialDialog;
import com.pddstudio.otgsubs.R;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

/**
 * Created by pddstudio on 08/05/2017.
 */

@EBean
public class InputDialog implements MaterialDialog.InputCallback {

	public interface InputCallback {
		void onInputReceived(String dialogInput);
	}

	@RootContext
	protected Context context;

	private MaterialDialog inputDialog;
	private InputCallback  callback;

	@StringRes
	private int inputHint = 0;

	@StringRes
	private int inputPreFill = 0;

	public void setInputHint(@StringRes int inputHint) {
		this.inputHint = inputHint;
	}

	public void setInputPreFill(@StringRes int inputPreFill) {
		this.inputPreFill = inputPreFill;
	}

	public void show(@StringRes int titleRes, @StringRes int contentRes, InputCallback callback) {
		this.callback = callback;
		inputDialog = new MaterialDialog.Builder(context).title(titleRes).content(contentRes).input(inputHint, inputPreFill, false, this).inputRangeRes(1, 20, R.color.md_red_500).show();
	}

	public void dismiss() {
		if (inputDialog != null && inputDialog.isShowing()) {
			inputDialog.dismiss();
		}
		inputPreFill = 0;
		inputHint = 0;
	}

	@Override
	public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
		if (callback != null) {
			callback.onInputReceived(input.toString());
		}
	}

}
