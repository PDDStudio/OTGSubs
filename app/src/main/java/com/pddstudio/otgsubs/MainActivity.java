package com.pddstudio.otgsubs;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.folderselector.FileChooserDialog;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.github.clans.fab.FloatingActionMenu;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.pddstudio.otgsubs.adapters.AssetsPageAdapter;
import com.pddstudio.otgsubs.events.AssetTypeAddedEvent;
import com.pddstudio.otgsubs.events.FileChooserDialogEvent;
import com.pddstudio.otgsubs.events.RefreshItemListEvent;
import com.pddstudio.otgsubs.models.FileChooserType;
import com.pddstudio.otgsubs.services.ImportApkService;
import com.pddstudio.otgsubs.services.PackageService;
import com.pddstudio.otgsubs.utils.DeviceUtils;
import com.pddstudio.otgsubs.utils.DrawerUtils;
import com.pddstudio.otgsubs.utils.Preferences_;
import com.pddstudio.otgsubs.utils.ThemeUtils;
import com.pddstudio.substratum.packager.ApkExtractor;
import com.pddstudio.substratum.packager.models.ApkInfo;
import com.pddstudio.substratum.packager.models.AssetFileInfo;
import com.pddstudio.substratum.packager.models.AssetsType;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.menu_main)
public class MainActivity extends AppCompatActivity
		implements FolderChooserDialog.FolderCallback, FileChooserDialog.FileCallback, MaterialSimpleListAdapter.Callback, Drawer.OnDrawerItemClickListener {

	private static final int      REQUEST_PERMISSIONS   = 42;
	private static final int      SETTINGS_REQUEST_CODE = 69;
	private static final String[] REQUIRED_PERMISSIONS  = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

	private static final int THEME_PACKAGER_ITEM = 88;
	private static final int GITHUB_ITEM         = 89;
	private static final int SETTINGS_ITEM       = 90;
	private static final int ABOUT_ITEM          = 91;

	@Bean
	EventBusBean eventBus;

	@Bean
	PackageInfoBean packageInfoBean;

	@Bean
	ApkExtractor apkExtractor;

	@Bean
	DeviceUtils deviceUtils;

	@Bean
	DrawerUtils drawerUtils;

	@Pref
	Preferences_ preferences;

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
	private AssetsType     assetsType;
	private MaterialDialog loadingDialog;

	private int currentSelection;

	@Override
	public void onBackPressed() {
		if (drawer.isDrawerOpen()) {
			drawer.closeDrawer();
		} else if (floatingActionMenu.isOpened()) {
			floatingActionMenu.close(true);
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
			if (event.getFileChooserType().equals(FileChooserType.DIRECTORY)) {
				new FolderChooserDialog.Builder(this).initialPath(Environment.getExternalStorageDirectory().getAbsolutePath()).show();
			} else if (event.getFileChooserType().equals(FileChooserType.ZIP_FILE)) {
				new FileChooserDialog.Builder(this).extensionsFilter(".zip").initialPath(Environment.getExternalStorageDirectory().getAbsolutePath()).show();
			} else {
				Log.w("FileEvent", "Unknown FileType: " + event.getFileChooserType());
			}

		}
	}

	@Subscribe
	public void onRefreshEventReceived(RefreshItemListEvent event) {
		if (event != null && loadingDialog != null && loadingDialog.isShowing()) {
			loadingDialog.dismiss();
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

	@Override
	public void onMaterialListItemSelected(MaterialDialog dialog, int index, MaterialSimpleListItem item) {
		Log.d("ListAdapter", "Item selected: " + item.getContent());
		ApkInfo selectedApk = ((ApkInfo) item.getTag());
		if (selectedApk != null) {
			ImportApkService.processImportRequest(MainActivity.this, selectedApk);
			loadingDialog = new MaterialDialog.Builder(this).title(R.string.dialog_extract_assets_title)
															.content(R.string.dialog_extract_assets_content, selectedApk.getAppName())
															.progress(true, -1)
															.cancelable(false)
															.canceledOnTouchOutside(false)
															.autoDismiss(false)
															.show();
		} else {
			Toast.makeText(this, "Unable to process selected Application...", Toast.LENGTH_SHORT).show();
		}
		dialog.dismiss();
	}

	@Override
	public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
		if (drawerItem != null) {

			int id = (int) drawerItem.getIdentifier();

			if (id == currentSelection) {
				drawer.closeDrawer();
				return true;
			}

			switch ((int) drawerItem.getIdentifier()) {
				case ABOUT_ITEM:
					//AboutActivity.open(this);
					break;
				case SETTINGS_ITEM:
					SettingsActivity.open(this, SETTINGS_REQUEST_CODE);
					break;
			}
			if (drawerItem.getIdentifier() != SETTINGS_ITEM && drawerItem.getIdentifier() != GITHUB_ITEM) {
				switchPageForDrawerSelection(id);
				currentSelection = (int) drawerItem.getIdentifier();
			}
			if (drawer != null) {
				drawer.closeDrawer();
			}
			return true;
		}
		drawer.closeDrawer();
		return false;

	}

	@AfterViews
	protected void setupUi() {
		toolbar.inflateMenu(R.menu.menu_main);
		setSupportActionBar(toolbar);

		if (!hasRequiredPermissions()) {
			checkPermissions();
		} else {
			switchPageForDrawerSelection(THEME_PACKAGER_ITEM);
		}

		drawer = drawerUtils.createDrawer(this, toolbar, THEME_PACKAGER_ITEM, this, getDrawerItems());
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

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeUtils.applySelectedTheme(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		deviceUtils.setNavigationBarColor(this);
	}

	@Click({R.id.fab_add_font, R.id.fab_add_boot_animation, R.id.fab_add_audio, R.id.fab_add_overlay})
	protected void onFabClicked(View view) {
		switch (view.getId()) {
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

	@OptionsItem(R.id.menu_build_apk)
	protected void onBuildApkMenuItemSelected() {
		toggleLoadingDialog(true);
		PackageService.createApkFromRequestedAssets(this);
	}

	@OptionsItem(R.id.menu_import_apk)
	protected void onImportApkMenuItemSelected() {
		Toast.makeText(this, "Selected ImportApkMenu", Toast.LENGTH_SHORT).show();
		MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter(this);
		StreamSupport.stream(getApplicationListDialogItems()).forEach(adapter::add);
		new MaterialDialog.Builder(this).adapter(adapter, null).show();
	}

	@OptionsItem(R.id.menu_restore_defaults)
	protected void onRestoreDefaultsMenuItemSelected() {
		Toast.makeText(this, "Selected RestoreDefaultsMenu", Toast.LENGTH_SHORT).show();
	}

	@Receiver(actions = PackageService.ACTION_PACKAGING_DONE, local = true)
	protected void onPackagingResultReceived(Intent intent) {
		boolean packagingSuccess = intent.getBooleanExtra(PackageService.EXTRA_PACKAGING_DONE_STATUS, false);
		String apkPath = intent.getStringExtra(PackageService.EXTRA_PACKAGING_DONE_FILE);
		toggleLoadingDialog(false);
		if (packagingSuccess) {
			Toast.makeText(this, "APK Created: " + apkPath, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, "Failed to create APK!", Toast.LENGTH_LONG).show();
		}
	}

	@OnActivityResult(SETTINGS_REQUEST_CODE)
	void onResult() {
		if (preferences.themeChanged().get()) {
			ThemeUtils.restartActivity(this);
			preferences.themeChanged().put(false);
		}
	}

	private List<IDrawerItem> getDrawerItems() {
		List<IDrawerItem> items = new ArrayList<>();
		items.add(drawerUtils.createSectionHeaderDrawerItem(R.string.drawer_section_general, false));
		items.add(drawerUtils.createPrimaryDrawerItem(this, FontAwesome.Icon.faw_crosshairs, R.string.drawer_item_packager_title, R.string.drawer_item_packager_subtitle, THEME_PACKAGER_ITEM, true));
		items.add(drawerUtils.createSectionHeaderDrawerItem(R.string.drawer_section_more, true));
		items.add(drawerUtils.createPrimaryDrawerItem(this, FontAwesome.Icon.faw_github, R.string.drawer_item_github, 0, GITHUB_ITEM, false));
		items.add(drawerUtils.createPrimaryDrawerItem(this, FontAwesome.Icon.faw_info, R.string.drawer_item_about, 0, ABOUT_ITEM, false));
		items.add(drawerUtils.createPrimaryDrawerItem(this, FontAwesome.Icon.faw_cogs, R.string.drawer_item_settings, 0, SETTINGS_ITEM, false));
		return items;
	}

	private void sendLocationChosenEvent(@Nullable String selectedLocation) {
		if (selectedLocation != null && assetsType != null) {
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
			case THEME_PACKAGER_ITEM:
			default:
				viewPager.setAdapter(new AssetsPageAdapter(this, getSupportFragmentManager()));
				tabLayout.setupWithViewPager(viewPager, true);
				break;
		}
	}

	private void toggleLoadingDialog(boolean showDialog) {
		if (showDialog) {
			loadingDialog = new MaterialDialog.Builder(this).title(R.string.dialog_packaging_title)
															.content(R.string.dialog_packaging_content)
															.progress(true, -1)
															.cancelable(false)
															.canceledOnTouchOutside(false)
															.autoDismiss(false)
															.show();
		} else {
			if (loadingDialog != null && loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
			loadingDialog = null;
		}
	}

	private List<MaterialSimpleListItem> getApplicationListDialogItems() {
		return StreamSupport.stream(apkExtractor.getApkInfoList()).map(apkInfo -> {
			MaterialSimpleListItem.Builder builder = new MaterialSimpleListItem.Builder(this);
			try {
				Drawable icon = getPackageManager().getApplicationIcon(apkInfo.getPackageName());
				if (icon != null) {
					builder.icon(icon);
				}
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
			builder.tag(apkInfo);
			builder.content(apkInfo.getAppName());
			return builder.build();
		}).sorted((item1, item2) -> {
			ApkInfo app1 = ((ApkInfo) item1.getTag());
			ApkInfo app2 = ((ApkInfo) item2.getTag());
			if (app1 != null && app2 != null) {
				return app1.getAppName().toLowerCase().compareTo(app2.getAppName().toLowerCase());
			} else {
				return 0;
			}
		}).collect(Collectors.toList());
	}

}
