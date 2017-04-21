package com.pddstudio.otgsubs;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.pddstudio.otgsubs.events.FileChooserDialogEvent;
import com.pddstudio.otgsubs.fragments.AssetsPickerFragment;
import com.pddstudio.otgsubs.utils.DrawerUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements FolderChooserDialog.FolderCallback {

	private static final int      REQUEST_PERMISSIONS  = 42;
	private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

	private static final int ASSETS_PICKER_ITEM = 88;

	@Bean
	EventBusBean eventBus;

	@ViewById(R.id.toolbar)
	Toolbar toolbar;

	private Drawer drawer;

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
			new FolderChooserDialog.Builder(this).initialPath("/sdcard/").show();
		}
	}

	@Override
	public void onFolderSelection(@NonNull FolderChooserDialog dialog, @NonNull File folder) {
		sendDirectoryChooserEvent(folder.exists() ? folder.getAbsolutePath() : null);
		dialog.dismiss();
	}

	@Override
	public void onFolderChooserDismissed(@NonNull FolderChooserDialog dialog) {
		sendDirectoryChooserEvent(null);
		dialog.dismiss();
	}

	@AfterViews
	protected void setupUi() {
		if (!hasRequiredPermissions()) {
			checkPermissions();
		} else {
			if (getSupportFragmentManager().findFragmentById(R.id.container) == null) {
				setMainFragment(AssetsPickerFragment.createInstance(), AssetsPickerFragment.TAG);
			}
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
									.withActionBarDrawerToggle(false)
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
	}

	@Override
	protected void onStop() {
		eventBus.unregister(this);
		super.onStop();
	}

	private List<IDrawerItem> getDrawerItems() {
		List<IDrawerItem> items = new ArrayList<>();
		items.add(DrawerUtils.createPrimaryDrawerItem("Assets Picker", "Specify target assets", ASSETS_PICKER_ITEM));
		return items;
	}

	private void sendDirectoryChooserEvent(@Nullable String selectedDir) {
		FileChooserDialogEvent event = new FileChooserDialogEvent(false);
		event.setResultDir(selectedDir);
		eventBus.post(event);
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
				setMainFragment(AssetsPickerFragment.createInstance(), AssetsPickerFragment.TAG);
				break;
		}
	}

	private void setMainFragment(Fragment fragment, String tag) {
		FragmentManager manager = getSupportFragmentManager();
		Fragment current = manager.findFragmentById(R.id.container);
		if (current == null || !(tag.equals(fragment.getTag())) || !current.getClass().isAssignableFrom(fragment.getClass())) {
			manager.beginTransaction().replace(R.id.container, fragment, tag).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
		}
		Toast.makeText(this, "Variant: " + tag, Toast.LENGTH_SHORT).show();
	}

}
