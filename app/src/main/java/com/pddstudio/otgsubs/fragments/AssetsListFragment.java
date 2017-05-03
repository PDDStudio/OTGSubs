package com.pddstudio.otgsubs.fragments;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.pddstudio.otgsubs.beans.EventBusBean;
import com.pddstudio.otgsubs.beans.PackageInfoBean;
import com.pddstudio.otgsubs.R;
import com.pddstudio.otgsubs.events.AssetTypeAddedEvent;
import com.pddstudio.otgsubs.events.ExistingAssetsItemEvent;
import com.pddstudio.otgsubs.events.RefreshItemListEvent;
import com.pddstudio.otgsubs.models.AssetsAdapterItem;
import com.pddstudio.otgsubs.models.AssetsModificationType;
import com.pddstudio.otgsubs.utils.DialogUtils;
import com.pddstudio.otgsubs.views.EmptyAssetsView;
import com.pddstudio.substratum.packager.models.AssetFileInfo;
import com.pddstudio.substratum.packager.models.AssetsType;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.apache.commons.io.FilenameUtils;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import java8.util.stream.StreamSupport;

/**
 * Created by pddstudio on 24/04/2017.
 */

@EFragment(R.layout.fragment_assets_list)
public class AssetsListFragment extends Fragment implements FastAdapter.OnClickListener<AssetsAdapterItem>, MaterialSimpleListAdapter.Callback {

	public static final String TAG = AssetsListFragment.class.getSimpleName();

	public static AssetsListFragment newInstance(@NonNull AssetsType assetsType) {
		return AssetsListFragment_.builder().assetsType(assetsType).build();
	}

	@FragmentArg
	protected AssetsType assetsType;

	@Bean
	protected EventBusBean eventBus;

	@Bean
	protected PackageInfoBean packageBean;

	@ViewById(R.id.assets_recycler_view)
	protected RecyclerView assetsRecyclerView;

	@ViewById(R.id.swipe_refresh_layout)
	protected SwipeRefreshLayout refreshLayout;

	@ViewById(R.id.empty_assets_view)
	EmptyAssetsView emptyAssetsView;

	private FastItemAdapter<AssetsAdapterItem> assetsAdapter;

	private boolean isAssetInfoPresent(AssetFileInfo fileInfo) {
		return StreamSupport.stream(assetsAdapter.getAdapterItems()).filter(file -> file.getAssetFileInfo().getFileLocation().equals(fileInfo.getFileLocation())).findAny().isPresent();
	}

	private void toggleEmptyViewIfRequired() {
		if(assetsAdapter != null) {
			emptyAssetsView.setViewVisible(assetsAdapter.getAdapterItems().isEmpty());
			emptyAssetsView.setVisibility(assetsAdapter.getAdapterItems().isEmpty() ? View.VISIBLE : View.GONE);
		}
	}

	private void showSelectedItemDialog(AssetsAdapterItem item) {
		AssetFileInfo assetFileInfo = item.getAssetFileInfo();
		new MaterialDialog.Builder(getContext())
				.adapter(DialogUtils.createAssetsItemSelectionAdapter(getContext(), this, assetFileInfo), null)
				.title(getString(R.string.dialog_modify_item_title, assetFileInfo.getFileName()))
				.show();
	}

	private void showRenameDialog(DialogUtils.TagPair<AssetFileInfo, AssetsModificationType> typeTagPair) {
		ExistingAssetsItemEvent event = new ExistingAssetsItemEvent(typeTagPair.getKey(), typeTagPair.getValue());
		new MaterialDialog.Builder(getContext())
				.title(getString(R.string.dialog_rename_file_title, typeTagPair.getKey().getFileName()))
				.input(0,0, (dialog, input) -> {
					String newName = input == null ? "" : input.toString();
					if(!newName.isEmpty()) {
						if(FilenameUtils.getExtension(newName).isEmpty()) {
							event.setNewName(newName + FilenameUtils.getExtension(typeTagPair.getKey().getFileName()));
						} else {
							event.setNewName(newName);
						}
						eventBus.post(event);
					}
				}).show();
	}

	@AfterViews
	protected void setupFragment() {
		emptyAssetsView.setEmptyText(R.string.empty_assets_text, getString(assetsType.getTitleRes()));
		assetsAdapter = new FastItemAdapter<>();
		assetsAdapter.withOnClickListener(this);
		assetsRecyclerView.setAdapter(assetsAdapter);
		assetsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		refreshLayout.setEnabled(false);
		List<AssetFileInfo> info = packageBean.getExistingInformation(assetsType);
		if (info != null && !info.isEmpty()) {
			StreamSupport.stream(info).map(AssetsAdapterItem::new).forEach(assetsAdapter::add);
		}
		toggleEmptyViewIfRequired();
	}

	public String getPageTitle() {
		return getString(assetsType.getTitleRes());
	}

	@Override
	public void onStart() {
		super.onStart();
		eventBus.register(this);
	}

	@Override
	public void onStop() {
		eventBus.unregister(this);
		super.onStop();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onAssetAdded(AssetTypeAddedEvent event) {
		if (event != null && event.getAssetsType().equals(assetsType) && isVisible() && !event.isIgnore() && !isAssetInfoPresent(event.getAssetFileInfo())) {
			AssetsAdapterItem adapterItem = new AssetsAdapterItem(event.getAssetFileInfo());
			assetsAdapter.add(adapterItem);
		}
		toggleEmptyViewIfRequired();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onRefreshAssets(RefreshItemListEvent event) {
		assetsAdapter.clear();
		List<AssetFileInfo> info = packageBean.getExistingInformation(assetsType);
		if (info != null && !info.isEmpty()) {
			StreamSupport.stream(info).map(AssetsAdapterItem::new).forEach(assetsAdapter::add);
		}
		toggleEmptyViewIfRequired();
	}

	@Override
	public boolean onClick(View v, IAdapter<AssetsAdapterItem> adapter, AssetsAdapterItem item, int position) {
		showSelectedItemDialog(item);
		return true;
	}

	@Override
	public void onMaterialListItemSelected(MaterialDialog dialog, int index, MaterialSimpleListItem item) {
		DialogUtils.TagPair<AssetFileInfo, AssetsModificationType> tag = (DialogUtils.TagPair<AssetFileInfo, AssetsModificationType>) item.getTag();
		if(tag != null && tag.getValue() != null && tag.getKey() != null) {
			switch (tag.getValue()) {
				case DELETE:
					eventBus.post(new ExistingAssetsItemEvent(tag.getKey(), tag.getValue()));
					break;
				case RENAME:
					showRenameDialog(tag);
					break;
			}
		} else {
			Log.e(TAG, "Unable to resolve action for selected item!");
		}
		dialog.dismiss();
	}
}
