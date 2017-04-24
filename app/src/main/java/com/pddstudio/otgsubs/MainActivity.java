package com.pddstudio.otgsubs;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.folderselector.FileChooserDialog;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.github.clans.fab.FloatingActionMenu;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.pddstudio.otgsubs.adapters.AssetsPageAdapter;
import com.pddstudio.otgsubs.events.AssetTypeAddedEvent;
import com.pddstudio.otgsubs.events.FileChooserDialogEvent;
import com.pddstudio.otgsubs.models.FileChooserType;
import com.pddstudio.otgsubs.utils.DrawerUtils;
import com.pddstudio.substratum.packager.models.AssetFileInfo;
import com.pddstudio.substratum.packager.models.AssetsType;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements FolderChooserDialog.FolderCallback, FileChooserDialog.FileCallback {

	private static final int      REQUEST_PERMISSIONS  = 42;
	private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

	private static final int ASSETS_PICKER_ITEM = 88;

	@Bean
	EventBusBean eventBus;

	@Bean
	PackageInfoBean packageInfoBean;

	@ViewById(R.id.toolbar)
	Toolbar toolbar;

	@ViewById(R.id.assets_list_tab_layout)
	TabLayout tabLayout;

	@ViewById(R.id.fragment_view_pager)
	ViewPager viewPager;

	@ViewById(R.id.floating_action_menu)
	FloatingActionMenu floatingActionMenu;

	private Drawer drawer;
	private FileChooserType fileChooserType = FileChooserType.IGNORE;
	private AssetsType assetsType;

	@Override
	public void onBackPressed() {
		if (drawer.isDrawerOpen()) {
			drawer.closeDrawer();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int grantResults[]) {
		if (requestCode == REQUEST_PERMISSIONS) {
			for (int grantResult : grantResults) {
				if (grantResult != PackageManager.PERMISSION_GRANTED) {
					checkPermissions();
					return;
				}
			}
			int id = (int) drawer.getCurrentSelection();
			switchPageForDrawerSelection(id);
		}
	}

	@Subscribe
	public void onDialogPickerEvent(FileChooserDialogEvent event) {
		if (event != null && event.isOpenRequest()) {
			if(event.getFileChooserType().equals(FileChooserType.DIRECTORY)) {
				new FolderChooserDialog.Builder(this)
						.initialPath(Environment.getExternalStorageDirectory().getAbsolutePath())
						.show();
			} else if(event.getFileChooserType().equals(FileChooserType.ZIP_FILE)) {
				new FileChooserDialog.Builder(this)
						.extensionsFilter(".zip")
						.initialPath(Environment.getExternalStorageDirectory().getAbsolutePath())
						.show();
			} else {
				Log.w("FileEvent", "Unknown FileType: " + event.getFileChooserType());
			}

		}
	}

	@Override
	public void onFolderSelection(@NonNull FolderChooserDialog dialog, @NonNull File folder) {
		sendLocationChosenEvent(folder.exists() ? folder.getAbsolutePath() : null);
		dialog.dismiss();
	}

	@Override
	public void onFolderChooserDismissed(@NonNull FolderChooserDialog dialog) {
		sendLocationChosenEvent(null);
		dialog.dismiss();
	}


	@Override
	public void onFileSelection(@NonNull FileChooserDialog dialog, @NonNull File file) {
		sendLocationChosenEvent(file.exists() ? file.getAbsolutePath() : null);
		dialog.dismiss();
	}

	@Override
	public void onFileChooserDismissed(@NonNull FileChooserDialog dialog) {
		sendLocationChosenEvent(null);
		dialog.dismiss();
	}

	@AfterViews
	protected void setupUi() {
		if (!hasRequiredPermissions()) {
			checkPermissions();
		} else {
			switchPageForDrawerSelection(ASSETS_PICKER_ITEM);
		}

		AccountHeader drawerHeader = new AccountHeaderBuilder().withActivity(this)
															   .addProfiles(new ProfileDrawerItem().withName("OpenCamera Showcase").withEmail("Various implementations of Camera2 API."))
															   .withHeaderBackground(R.color.colorPrimary)
															   .withSelectionListEnabled(false)
															   .withSelectionListEnabledForSingleProfile(false)
															   .withAlternativeProfileHeaderSwitching(false)
															   .withOnlyMainProfileImageVisible(true)
															   .build();

		drawer = new DrawerBuilder().withActivity(this)
									.withToolbar(toolbar)
									.withAccountHeader(drawerHeader)
									.withActionBarDrawerToggle(true)
									.withActionBarDrawerToggleAnimated(true)
									.withCloseOnClick(true)
									.withDrawerItems(getDrawerItems())
									.withOnDrawerItemClickListener((view, position, drawerItem) -> {
										int id = (int) drawerItem.getIdentifier();
										switchPageForDrawerSelection(id);
										if (drawer != null && drawer.isDrawerOpen()) {
											drawer.closeDrawer();
											return true;
										} else {
											return false;
										}
									})
									.withCloseOnClick(true)
									.withDelayOnDrawerClose(150)
									.withSelectedItem(ASSETS_PICKER_ITEM)
									.withFireOnInitialOnClick(true)
									.build();
	}

	@Override
	protected void onStart() {
		super.onStart();
		eventBus.register(this);
		packageInfoBean.register();
	}

	@Override
	protected void onStop() {
		packageInfoBean.unregister();
		eventBus.unregister(this);
		super.onStop();
	}

	@Click({R.id.fab_add_font, R.id.fab_add_boot_animation, R.id.fab_add_audio, R.id.fab_add_overlay})
	protected void onFabClicked(View view) {
		switch(view.getId()) {
			case R.id.fab_add_font:
				assetsType = AssetsType.FONTS;
				fileChooserType = FileChooserType.ZIP_FILE;
				break;
			case R.id.fab_add_boot_animation:
				assetsType = AssetsType.BOOT_ANIMATIONS;
				fileChooserType = FileChooserType.ZIP_FILE;
				break;
			case R.id.fab_add_audio:
				assetsType = AssetsType.AUDIO;
				fileChooserType = FileChooserType.ZIP_FILE;
				break;
			case R.id.fab_add_overlay:
				assetsType = AssetsType.OVERLAYS;
				fileChooserType = FileChooserType.DIRECTORY;
				break;
		}
		eventBus.post(new FileChooserDialogEvent(true, fileChooserType));
		floatingActionMenu.close(true);
	}

	private List<IDrawerItem> getDrawerItems() {
		List<IDrawerItem> items = new ArrayList<>();
		items.add(DrawerUtils.createPrimaryDrawerItem("Assets Picker", "Specify target assets", ASSETS_PICKER_ITEM));
		return items;
	}

	private void sendLocationChosenEvent(@Nullable String selectedLocation) {
		if(selectedLocation != null && assetsType != null) {
			File file = new File(selectedLocation);
			AssetFileInfo fileInfo = new AssetFileInfo(assetsType, file.getAbsolutePath(), file.getName());
			AssetTypeAddedEvent assetTypeAddedEvent = new AssetTypeAddedEvent(assetsType, fileInfo);
			assetTypeAddedEvent.setFragmentIgnore(true);
			eventBus.post(assetTypeAddedEvent);
		}
		assetsType = null;
		fileChooserType = FileChooserType.IGNORE;
	}

	private void checkPermissions() {
		if (hasRequiredPermissions()) {
			return;
		}
		ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_PERMISSIONS);
	}

	private boolean hasRequiredPermissions() {
		for (String permissionName : REQUIRED_PERMISSIONS) {
			if (ContextCompat.checkSelfPermission(this, permissionName) != PackageManager.PERMISSION_GRANTED) {
				return false;
			}
		}
		return true;
	}

	private void switchPageForDrawerSelection(int drawerItemId) {
		switch (drawerItemId) {
			case ASSETS_PICKER_ITEM:
			default:
				viewPager.setAdapter(new AssetsPageAdapter(this, getSupportFragmentManager()));
				tabLayout.setupWithViewPager(viewPager, true);
				//setMainFragment(AssetsPickerFragment.createInstance(), AssetsPickerFragment.TAG);
				break;
		}
	}

}
