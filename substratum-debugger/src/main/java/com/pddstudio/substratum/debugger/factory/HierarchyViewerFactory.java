package com.pddstudio.substratum.debugger.factory;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by pddstudio on 02/05/2017.
 */

public class HierarchyViewerFactory implements LayoutInflater.Factory2 {

	private final LayoutInflater.Factory2 baseFactory;
	private InflationCallback callback;

	public HierarchyViewerFactory(@NonNull LayoutInflater.Factory2 factory) {
		this.baseFactory = factory;
	}

	public HierarchyViewerFactory(@NonNull LayoutInflater.Factory2 factory, @Nullable InflationCallback callback) {
		this.baseFactory = factory;
		this.callback = callback;
	}

	private void onInterceptLayout(View parent, String name, Context context, AttributeSet attrs) {
		if(callback != null) {
			callback.onLayoutInflated(parent, name, context, attrs);
		}
	}

	@Override
	public View onCreateView(String name, Context context, AttributeSet attrs) {
		onInterceptLayout(null, name, context, attrs);
		return baseFactory.onCreateView(name, context, attrs);
	}

	@Override
	public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
		onInterceptLayout(parent, name, context, attrs);
		return baseFactory.onCreateView(parent, name, context, attrs);
	}

	public interface InflationCallback {
		void onLayoutInflated(@Nullable View parent, String name, Context context, AttributeSet attrs);
	}

}
