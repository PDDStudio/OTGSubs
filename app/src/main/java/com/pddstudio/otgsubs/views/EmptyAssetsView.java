package com.pddstudio.otgsubs.views;

import android.content.Context;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pddstudio.otgsubs.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by pddstudio on 24/04/2017.
 */

@EViewGroup(R.layout.view_empty_assets)
public class EmptyAssetsView extends RelativeLayout {

	@ViewById(R.id.empty_text)
	protected TextView emptyText;

	public EmptyAssetsView(Context context) {
		super(context);
	}

	public EmptyAssetsView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public EmptyAssetsView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public EmptyAssetsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public void setViewVisible(boolean visible) {
		setVisibility(visible ? VISIBLE : GONE);
	}

	public void setEmptyText(@StringRes int text, Object... formatArgs) {
		emptyText.setText(getContext().getString(text, formatArgs));
	}

}
