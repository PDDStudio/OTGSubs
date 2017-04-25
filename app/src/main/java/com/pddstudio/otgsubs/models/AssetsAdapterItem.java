package com.pddstudio.otgsubs.models;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.iconics.view.IconicsImageView;
import com.pddstudio.otgsubs.R;
import com.pddstudio.substratum.packager.models.AssetFileInfo;
import com.pddstudio.substratum.packager.models.AssetsType;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.List;

/**
 * Created by pddstudio on 24/04/2017.
 */

public class AssetsAdapterItem extends AbstractItem<AssetsAdapterItem, AssetsAdapterItem.ViewHolder> {

	private AssetFileInfo fileInfo;

	public AssetsAdapterItem(AssetFileInfo fileInfo) {
		this.fileInfo = fileInfo;
	}

	private IIcon getIconForAssetsInfo() {
		if (fileInfo.getType().equals(AssetsType.AUDIO)) {
			return FontAwesome.Icon.faw_file_audio_o;
		} else if(new File(fileInfo.getFileLocation()).isDirectory()) {
			return FontAwesome.Icon.faw_folder;
		}
		String extension = FilenameUtils.getExtension(fileInfo.getFileName());
		if(extension.equals("jpg") || extension.equals("png")) {
			return FontAwesome.Icon.faw_file_image_o;
		} else if (extension.equals("xml")) {
			return FontAwesome.Icon.faw_file_code_o;
		} else if (extension.equals("zip")) {
			return FontAwesome.Icon.faw_file_archive_o;
		} else {
			return FontAwesome.Icon.faw_file_o;
		}
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
		holder.itemIcon.setIcon(getIconForAssetsInfo());
	}

	@Override
	public void unbindView(ViewHolder holder) {
		super.unbindView(holder);
		holder.assetFileName.setText(null);
		holder.assetFileLocation.setText(null);
	}

	protected static class ViewHolder extends RecyclerView.ViewHolder {

		protected IconicsImageView itemIcon;
		protected TextView         assetFileName;
		protected TextView         assetFileLocation;

		public ViewHolder(View itemView) {
			super(itemView);
			itemIcon = (IconicsImageView) itemView.findViewById(R.id.assets_item_icon);
			assetFileName = (TextView) itemView.findViewById(R.id.assets_item_name);
			assetFileLocation = (TextView) itemView.findViewById(R.id.assets_item_location);
		}
	}

}
