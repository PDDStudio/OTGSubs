package com.pddstudio.otgsubs.fragments;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.pddstudio.otgsubs.EventBusBean;
import com.pddstudio.otgsubs.PackageInfoBean;
import com.pddstudio.otgsubs.R;
import com.pddstudio.otgsubs.events.AssetTypeAddedEvent;
import com.pddstudio.otgsubs.events.RefreshItemListEvent;
import com.pddstudio.otgsubs.models.AssetsAdapterItem;
import com.pddstudio.otgsubs.views.EmptyAssetsView;
import com.pddstudio.substratum.packager.models.AssetFileInfo;
import com.pddstudio.substratum.packager.models.AssetsType;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import java8.util.stream.StreamSupport;

/**
 * Created by pddstudio on 24/04/2017.
 */

@EFragment(R.layout.fragment_assets_list)
public class AssetsListFragment extends Fragment {

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

	@AfterViews
	protected void setupFragment() {
		emptyAssetsView.setEmptyText(R.string.empty_assets_text, getString(assetsType.getTitleRes()));
		assetsAdapter = new FastItemAdapter<>();
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

}
