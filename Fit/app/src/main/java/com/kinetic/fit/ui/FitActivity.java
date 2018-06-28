package com.kinetic.fit.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kinetic.fit.BuildConfig;
import com.kinetic.fit.R;
import com.kinetic.fit.connectivity.SensorDataService;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.ui.settings.SensorsActivity_;
import com.kinetic.fit.ui.settings.SettingsActivity;
import com.kinetic.fit.ui.settings.SettingsActivity_;
import com.kinetic.fit.ui.support.SupportActivity_;
import com.kinetic.fit.util.ViewStyling;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EActivity
public abstract class FitActivity extends AppCompatActivity {
    public static final int SETTINGS_REQ_CODE = 666;

    @ViewById(R.id.drawer_left)
    protected View mDrawerLeft;
    @ViewById(R.id.drawer_list)
    protected ListView mDrawerList;
    @ViewById(R.id.drawer_layout)
    protected DrawerLayout mDrawerLayout;
    @ViewById(R.id.drawer_profile_name)
    protected TextView mDrawerProfileName;
    @ViewById(R.id.drawer_version_text)
    protected TextView mVersionText;
    @ViewById(R.id.fit_activity_title)
    protected TextView mTitleText;
    private String mTitle = "Kinetic Fit";

    ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (Profile.current() != null) {
            if (getSharedPreferences(SettingsActivity.getSettingsNamespace(), MODE_PRIVATE)
                    .getBoolean(SettingsActivity.LIGHT_THEME_ON + Profile.getUUID(), false)) {
                setTheme(R.style.KineticTheme_Light);
            }
        }
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.action_bar_fit);
        }
    }

    public void setTitle(String title) {
        mTitle = title;
        if (mTitleText != null) {
            mTitleText.setText(title);
        }
    }

    @AfterViews
    protected void setUpDrawer() {
        if (mDrawerLayout != null && getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name, R.string.app_name) {
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                }

                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                }
            };
            // Set the drawer toggle as the DrawerListener
            mDrawerLayout.addDrawerListener(mDrawerToggle);
            mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    FitMenuItem menuItem = (FitMenuItem) mDrawerList.getAdapter().getItem(i);
                    menuItem.activate();
                }
            });
            updateMenuItems();
        }
        if (mTitleText != null) {
            mTitleText.setText(mTitle);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
        if (mDrawerProfileName != null) {
            if (Profile.current() != null) {
                mDrawerProfileName.setText(Profile.getUserName());
            } else {
                mDrawerProfileName.setText("Kinetic");
            }
        }
        if (mVersionText != null) {
            mVersionText.setText("Version " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void updateMenuItems() {
        mDrawerList.setAdapter(new MenuItemListAdapter(getBaseContext(), getMenuItems()));
    }

    protected List<FitMenuItem> getMenuItems() {
        List<FitMenuItem> items = new ArrayList<>();
        items.add(new FitMenuItem("Support", R.drawable.material_icon_help_outline) {
            protected void activate() {
                SupportActivity_.intent(FitActivity.this).start();
                super.activate();
            }
        });
        items.add(new FitMenuItem("Settings", R.drawable.material_icon_settings){
            protected void activate(){
                super.activate();
                SettingsActivity_.intent(FitActivity.this).startForResult(SETTINGS_REQ_CODE);
            }
        });
        items.add(new FitMenuItem("Sensors", R.drawable.material_icon_bluetooth) {
            protected void activate() {
                super.activate();
                SensorsActivity_.intent(FitActivity.this).start();
            }
        });
        /** logout here is for testing only**/
//        items.add(new FitMenuItem("Logout", R.drawable.material_icon_logout) {
//            protected void activate() {
//                super.activate();
//
//                Profile.current().logOut(FitActivity.this);
//                ActivityCompat.finishAffinity(FitActivity.this);
//
//                Intent intent = new Intent(getApplicationContext(), LoginDispatchActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
//            }
//        });
        return items;
    }

    protected class FitMenuItem {
        String mTitle;
        int mIconId;
        protected FitMenuItem(String title, int iconId) {
            mTitle = title;
            mIconId = iconId;
        }
        protected void activate() {
            FitActivity.this.mDrawerLayout.closeDrawers();
        }
    }

    private class MenuItemListAdapter extends BaseAdapter {
        Context context;
        List<FitMenuItem> data;
        private MenuItemListAdapter(Context context, List<FitMenuItem> data) {
            this.context = context;
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FitMenuItemView view = (FitMenuItemView) convertView;
            if (view == null) {
                view = FitMenuItemView_.build(context, null);
            }
            FitMenuItem menuItem = data.get(position);
            view.mItemTitle.setText(menuItem.mTitle);
            view.mItemIcon.setImageResource(menuItem.mIconId);
            return view;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SETTINGS_REQ_CODE && resultCode == RESULT_OK){
            if(data.getBooleanExtra("recreate", false)){
                recreate();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @UiThread
    @Receiver(actions = SensorDataService.SENSOR_FIRMWARE_PROGRESS_TOAST)
    protected void onSensorToastReceived(Intent intent) {
        ViewStyling.getCustomToast(this, getLayoutInflater(), intent.getStringExtra("message")).show();

    }
}
