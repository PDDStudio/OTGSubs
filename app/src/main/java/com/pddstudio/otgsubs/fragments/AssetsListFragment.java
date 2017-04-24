package com.pddstudio.otgsubs.fragments;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.pddstudio.otgsubs.EventBusBean;
import com.pddstudio.otgsubs.PackageInfoBean;
import com.pddstudio.otgsubs.R;
import com.pddstudio.otgsubs.events.AssetTypeAddedEvent;
import com.pddstudio.otgsubs.models.AssetsAdapterItem;
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

	private FastItemAdapter<AssetsAdapterItem> assetsAdapter;

	private boolean isAssetInfoPresent(AssetFileInfo fileInfo) {
		return StreamSupport.stream(assetsAdapter.getAdapterItems()).filter(file -> file.getAssetFileInfo().getFileLocation().equals(fileInfo.getFileLocation())).findAny().isPresent();
	}

	@AfterViews
	protected void setupFragment() {
		assetsAdapter = new FastItemAdapter<>();
		assetsRecyclerView.setAdapter(assetsAdapter);
		assetsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		refreshLayout.setEnabled(false);
		List<AssetFileInfo> info = packageBean.getExistingInformation(assetsType);
		if (info != null && !info.isEmpty()) {
			StreamSupport.stream(info).map(AssetsAdapterItem::new).forEach(assetsAdapter::add);
		}
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
	}

}
