package com.pddstudio.otgsubs.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.pddstudio.otgsubs.R;
import com.pddstudio.otgsubs.models.AssetsModificationType;
import com.pddstudio.substratum.packager.models.AssetFileInfo;

/**
 * Created by pddstudio on 25/04/2017.
 */

public class DialogUtils {

	private DialogUtils() {}

	public static MaterialSimpleListAdapter createAssetsItemSelectionAdapter(@NonNull Context context, @NonNull MaterialSimpleListAdapter.Callback callback, @NonNull AssetFileInfo assetFileInfo) {
		MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter(callback);
		/*adapter.add(new MaterialSimpleListItem.Builder(context).content("Rename Item")
															   .iconPaddingDp(4)
															   .icon(R.drawable.ic_mode_edit_24dp)
															   .tag(new TagPair<>(assetFileInfo, AssetsModificationType.RENAME))
															   .build());*/
		//TODO: fix rename action not working properly
		adapter.add(new MaterialSimpleListItem.Builder(context).content("Delete Item")
															   .iconPaddingDp(4)
															   .icon(R.drawable.ic_delete_forever_24dp)
															   .tag(new TagPair<>(assetFileInfo, AssetsModificationType.DELETE))
															   .build());
		return adapter;
	}

	public static class TagPair<K, V> {

		private final K key;
		private final V value;

		private TagPair(K key, V value) {
			this.key = key;
			this.value = value;
		}

		public K getKey() {
			return key;
		}

		public V getValue() {
			return value;
		}

	}

}
