package com.pddstudio.substratum.debugger;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by pddstudio on 02/05/2017.
 */

public class HierarchyViewer {

	/*
	Source: http://stackoverflow.com/questions/6559520/print-a-view-hierarchy-on-a-device
	 */

	public static String getViewHierarchy(@NonNull View v) {
		StringBuilder desc = new StringBuilder();
		getViewHierarchy(v, desc, 0);
		return desc.toString();
	}

	private static void getViewHierarchy(View v, StringBuilder desc, int margin) {
		desc.append(getViewMessage(v, margin));
		if (v instanceof ViewGroup) {
			margin++;
			ViewGroup vg = (ViewGroup) v;
			for (int i = 0; i < vg.getChildCount(); i++) {
				getViewHierarchy(vg.getChildAt(i), desc, margin);
			}
		}
	}

	private static String getViewMessage(View v, int marginOffset) {
		String repeated = new String(new char[marginOffset]).replace("\0", "  ");
		String resourceId = v.getId() > 0 ? v.getResources().getResourceName(v.getId()) : "no_id";
		return repeated + "[" + v.getClass().getSimpleName() + "] " + resourceId + "\n";
	}

}
