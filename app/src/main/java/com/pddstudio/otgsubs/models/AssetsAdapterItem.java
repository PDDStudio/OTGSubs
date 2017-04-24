package com.pddstudio.otgsubs.models;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;
import com.pddstudio.otgsubs.R;
import com.pddstudio.substratum.packager.models.AssetFileInfo;

import java.util.List;

/**
 * Created by pddstudio on 24/04/2017.
 */

public class AssetsAdapterItem extends AbstractItem<AssetsAdapterItem, AssetsAdapterItem.ViewHolder> {

	private AssetFileInfo fileInfo;

	public AssetsAdapterItem(AssetFileInfo fileInfo) {
		this.fileInfo = fileInfo;
	}

	public AssetFileInfo getAssetFileInfo() {
		return fileInfo;
	}

	@Override
	public ViewHolder getViewHolder(View v) {
		return new ViewHolder(v);
	}

	@Override
	public int getType() {
		return R.id.assets_adapter_item;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.item_assets_adapter;
	}

	@Override
	public void bindView(ViewHolder holder, List<Object> payloads) {
		super.bindView(holder, payloads);
		holder.assetFileName.setText(fileInfo.getFileName());
		holder.assetFileLocation.setText(fileInfo.getFileLocation());
	}

	@Override
	public void unbindView(ViewHolder holder) {
		super.unbindView(holder);
		holder.assetFileName.setText(null);
		holder.assetFileLocation.setText(null);
	}

	protected static class ViewHolder extends RecyclerView.ViewHolder {

		protected ImageView itemIcon;
		protected TextView assetFileName;
		protected TextView assetFileLocation;

		public ViewHolder(View itemView) {
			super(itemView);
			itemIcon = (ImageView) itemView.findViewById(R.id.assets_item_icon);
			assetFileName = (TextView) itemView.findViewById(R.id.assets_item_name);
			assetFileLocation = (TextView) itemView.findViewById(R.id.assets_item_location);
		}
	}

}
