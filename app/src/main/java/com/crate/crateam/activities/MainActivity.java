package com.crate.crateam.activities;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.FirestoreManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.crate.crateam.R;
import com.crate.crateam.fragments.DashboardFragment;
import com.crate.crateam.fragments.DefectedVehicleFragment;
import com.crate.crateam.fragments.FavouritesFragment;
import com.crate.crateam.fragments.FormFragment;
import com.crate.crateam.fragments.HelpFragment;
import com.crate.crateam.fragments.InspectionFragment;
import com.crate.crateam.fragments.ProfileFragment;
import com.crate.crateam.fragments.ReportsFragment;
import com.crate.crateam.fragments.SearchFragment;
import com.crate.crateam.fragments.SettingsFragment;
import com.crate.crateam.fragments.WebFormsFragment;
import com.crate.crateam.utility.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    public static final String MIME_TEXT = "text/plain";
    public static final String TAG = "Nfc";
    private String message,user_image;
    private TextView tv_temp;
    private TextView tv_logout,tv_header;
    private ImageView iv_weather;
    private ImageView iv_navigation,iv_home,iv_form,iv_crate,iv_notification,iv_favourites,iv_back_arrow,iv_user_image,iv_refresh,iv_cross;
    private DrawerLayout mDrawerLayout;
    private SpotsDialog spotsDialog;
    Fragment fragment = null;
    private int select = 0, user_id = 0, role_id = 0, asset_role = 0, count = 0;
    private FragmentTransaction ft;
    private String tag="", nfc_tag_manager = "", vehicle_inspection = "", view_inspection = "", manager_comment = "", ad_hoc_defect_report = "", view_ad_hoc = "", waste_transfer_form_operator="", waste_transfer_form_driver="",
            supply_form_operator="", supply_form_driver="", diesel_delivery="", service_driver_note="" ,assign_site="",view_reports="",with_nfc="";
    private boolean onDoubleBackPress;
    private NfcAdapter mNfcAdapter;
    private SessionManager sessionManager;
    private ProgressDialog progressDialog;
    private PopupWindow popupWindow;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initializeOnClick();
    }

    private void initView() {
        pref = this.getSharedPreferences("MyPref", 0); //0 - for private mode
        sessionManager = new SessionManager(getApplicationContext());
        progressDialog = Dialog.showProgressDialog(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        final String user_name = user.get(SessionManager.KEY_FULL_NAME);
        if (user.get(SessionManager.KEY_ID) != null)
            user_id = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        if (user.get(SessionManager.KEY_ROLE_ONE) != null)
            role_id = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ROLE_ONE)));
        if (user.get(SessionManager.KEY_ASSET_ROLE) != null)
            asset_role = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ASSET_ROLE)));
        user_image = user.get(SessionManager.KEY_IMAGE);

        Log.d("USER_ID :" , user_id + " " + asset_role);
        if (user_image != null)
            user_image = user.get(SessionManager.KEY_IMAGE);
        else
            user_image = "";
        tv_temp = findViewById(R.id.tv_temp);
        tv_logout = findViewById(R.id.tv_logout);
        tv_header = findViewById(R.id.tv_header);
        iv_weather = findViewById(R.id.iv_weather);
        iv_refresh = findViewById(R.id.iv_refresh);
        iv_navigation =  findViewById(R.id.iv_navigation);
        iv_home = findViewById(R.id.iv_home);
        iv_form = findViewById(R.id.iv_form);
        iv_crate = findViewById(R.id.iv_crate);
        iv_notification = findViewById(R.id.iv_notification);
        iv_favourites = findViewById(R.id.iv_favourites);
        iv_back_arrow = findViewById(R.id.iv_back_arrow);
        iv_cross = findViewById(R.id.iv_cross);
        spotsDialog = new SpotsDialog(this, R.style.Custom);
        mDrawerLayout =  this.findViewById(R.id.drawer_layout);
        final NavigationView navigationView = this.findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);
        for (int i = 0; i < navigationView.getChildCount(); i++) {
            navigationView.getChildAt(i).setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
        tv_logout = navigationView.findViewById(R.id.tv_logout);
        View header_view = navigationView.getHeaderView(0);
        TextView tv_view_profile = header_view.findViewById(R.id.tv_view_profile);
        TextView tv_userName = header_view.findViewById(R.id.tv_userName);
        iv_user_image = header_view.findViewById(R.id.iv_user_image);
        if (user_name != null) {
            if (!user_name.isEmpty())
                tv_userName.setText(user_name);
        }
        tv_view_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                select = 13;
                fragment = new ProfileFragment();
                ft.replace(R.id.content_frame,fragment,"MY PROFILE");
                ft.addToBackStack("MY PROFILE");
                ft.commit();
                tag = fragment.getTag();
                manageFragmentBackStack();
            }
        });
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }
            @Override
            public void onDrawerOpened(View drawerView) {
            }
            @Override
            public void onDrawerClosed(View drawerView) {
            }
            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
        }
        if (mNfcAdapter != null) {
            if (!mNfcAdapter.isEnabled())
                Toast.makeText(this, "NFC is disabled.Please enable it.", Toast.LENGTH_LONG).show();
        }
        fragment = new DashboardFragment();
        ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.content_frame, fragment, "DASHBOARD");
        ft.addToBackStack("DASHBOARD");
        ft.commit();
        tv_header.setTextColor(getResources().getColor(R.color.blue_shade_three));
        Picasso.with(getApplicationContext()).invalidate(user_image);
        Log.d("LAUNCH :" , Objects.requireNonNull(fragment.getTag()));
        manageFragmentBackStack();
        checkPermission();
        setupBackPressHandler();
    }

    private void initializeOnClick() {
        iv_navigation.setOnClickListener(this);
        iv_home.setOnClickListener(this);
        iv_form.setOnClickListener(this);
        iv_crate.setOnClickListener(this);
        iv_notification.setOnClickListener(this);
        iv_favourites.setOnClickListener(this);
        tv_logout.setOnClickListener(this);
        iv_notification.setOnClickListener(this);
        iv_back_arrow.setOnClickListener(this);
        iv_cross.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        ft = getSupportFragmentManager().beginTransaction();
        FragmentManager fm = MainActivity.this.getSupportFragmentManager();
        switch (view.getId()){
            case R.id.iv_navigation:
                if (mDrawerLayout.isDrawerVisible(GravityCompat.START))
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                else
                    mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.iv_back_arrow:
                if (fm!=null) {
                    getSupportFragmentManager().popBackStackImmediate();
                    tag = fm.getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
                    manageFragmentBackStack();
                }
                break;
            case R.id.iv_cross:
                select = 1;
                fragment = new DashboardFragment();
                ft.replace(R.id.content_frame,fragment,"DASHBOARD");
                ft.addToBackStack("DASHBOARD");
                ft.commit();
                tag = fragment.getTag();
                removeNfcPreferences();
                break;
            case R.id.iv_home:
                select = 1;
                fragment = new DashboardFragment();
                ft.replace(R.id.content_frame,fragment,"DASHBOARD");
                ft.addToBackStack("DASHBOARD");
                ft.commit();
                tag = fragment.getTag();
                break;
            case R.id.tv_logout:
                if (AppData.internetOnline(getApplicationContext())) {
                    progressDialog.show();
                    logoutDialog();
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                }
                break;
            case R.id.iv_form:
                select = 2;
                fragment = new FormFragment();
                ft.replace(R.id.content_frame,fragment,"FORM");
                ft.addToBackStack("FORM");
                ft.commit();
                tag = fragment.getTag();
                break;
            case R.id.iv_crate:
                if (view_reports.equals("yes")) {
                    select = 3;
                    fragment = new ReportsFragment();
                    ft.replace(R.id.content_frame, fragment, "REPORTS");
                    ft.addToBackStack("REPORTS");
                    ft.commit();
                    tag = fragment.getTag();
                }
                break;
            case R.id.iv_notification:
                select = 4;
                if (role_id !=5) {
                    fragment = new DefectedVehicleFragment();
                    ft.replace(R.id.content_frame, fragment, "DEFECT VEHICLE");
                    ft.addToBackStack("DEFECT VEHICLE");
                    ft.commit();
                    tag = fragment.getTag();
                }
                break;
            case R.id.iv_favourites:
                select = 5;
                if (role_id != 5) {
                    fragment = new FavouritesFragment();
                    ft.replace(R.id.content_frame, fragment, "FAVOURITES");
                    ft.addToBackStack("FAVOURITES");
                    ft.commit();
                    tag = fragment.getTag();
                }
                break;
        }
        manageFragmentBackStack();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        ft = getSupportFragmentManager().beginTransaction();
        int id = item.getItemId();
        if (id == R.id.nav_search) {
            select = 6;
            fragment = new SearchFragment();
            ft.replace(R.id.content_frame,fragment,"SEARCH");
            ft.addToBackStack("SEARCH");
            ft.commit();
            tag = fragment.getTag();
        } else if (id == R.id.nav_settings) {
            select = 7;
            fragment = new SettingsFragment();
            ft.replace(R.id.content_frame,fragment,"SETTINGS");
            ft.addToBackStack("SETTINGS");
            ft.commit();
            tag = fragment.getTag();
        } else if (id == R.id.nav_help) {
            select = 8;
            fragment = new HelpFragment();
            ft.replace(R.id.content_frame,fragment,"HELP");
            ft.addToBackStack("HELP");
            ft.commit();
            tag = fragment.getTag();
        } else if (id == R.id.nav_edit_favourites) {
            select = 5;
            fragment = new FavouritesFragment();
            ft.replace(R.id.content_frame,fragment,"FAVOURITES");
            ft.addToBackStack("FAVOURITES");
            ft.commit();
            tag = fragment.getTag();
        } else if (id == R.id.nav_web_forms) {
            if (nfc_tag_manager.equals("yes")) {
                select = 11;
                fragment = new WebFormsFragment();
                ft.replace(R.id.content_frame, fragment, "WEB FORMS");
                ft.addToBackStack("WEB FORMS");
                ft.commit();
                tag = fragment.getTag();
            }
        } else if (id == R.id.nav_assign_site) {
            if (assign_site.equals("yes")) {
                Intent assign_site_intent = new Intent(this, AssignSiteActivity.class);
                startActivity(assign_site_intent);
            }
        } else if (id == R.id.nav_asset_management) {
            if (asset_role != 0) {
                Intent asset_forms_intent = new Intent(this, AssetManagementDashboard.class);
                startActivity(asset_forms_intent);
            } else
                Dialog.alertDialog(this, "You don't have any permission to access Asset Management.");
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        manageFragmentBackStack();
        return true;
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentManager fm = MainActivity.this.getSupportFragmentManager();
                if (fm!=null) {
                    tag = fm.getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
                    Log.d("TAG:" , tag);
                    Log.d("BACK STACK ENTRY: " , String.valueOf(fm.getBackStackEntryCount()));
                    manageFragmentBackStack();
                    if (fm.getBackStackEntryCount()==1 || tag.equals("DASHBOARD")) {
                        onApplicationBackPressed();
                    }else
                        getSupportFragmentManager().popBackStackImmediate();
                    tag = fm.getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
                    manageFragmentBackStack();
                }
            }
        });
    }


    public void getChildTag(String childTag){
        tag = childTag;
        Log.d("CHILD  :"  ,tag);
        manageFragmentBackStack();
    }

    public void getCount(int count1){
        count= count1;
        Log.d("GET_COUNT  :"  , String.valueOf(count));
    }

    public void getNfcSelection(String nfc_checked){
        with_nfc = nfc_checked;
        Log.d("WITH NFC  :"  ,with_nfc);
    }

    public void getViewControllerData(String no24,String no25, String no26,String no27, String no28,String no29, String no30,String no31,
                                      String no32,String no33, String no34,String no35, String no36,String no37){
        nfc_tag_manager = no24;
        vehicle_inspection = no25;
        view_inspection = no26;
        manager_comment=no27;
        ad_hoc_defect_report=no28;
        view_ad_hoc=no29;
        waste_transfer_form_operator=no30;
        waste_transfer_form_driver=no31;
        supply_form_operator=no32;
        supply_form_driver=no33;
        diesel_delivery=no34;
        service_driver_note=no35;
        assign_site=no36;
        view_reports = no37;
        Log.d("Access View :",nfc_tag_manager+" "+assign_site+" "+view_reports);
    }

    private void manageFragmentBackStack(){
        Log.d("MY_TAG" , tag);
        if (tag != null ) {
            if (tag.equals("DASHBOARD")) {
                select = 1;
                tv_header.setText(R.string.dashboard);
                iv_back_arrow.setVisibility(View.GONE);
                iv_navigation.setVisibility(View.VISIBLE);
                iv_cross.setVisibility(View.GONE);
                tv_temp.setVisibility(View.INVISIBLE);
                iv_refresh.setVisibility(View.VISIBLE);
                iv_weather.setVisibility(View.INVISIBLE);
                iv_home.setClickable(true);
                iv_form.setClickable(true);
                iv_notification.setClickable(true);
                iv_crate.setClickable(true);
                iv_favourites.setClickable(true);
            }else if (tag.equals("FORM")|| tag.equals("FORM EXPAND")) {
                select = 2;
                tv_header.setText(R.string.form);
                iv_back_arrow.setVisibility(View.VISIBLE);
                iv_navigation.setVisibility(View.GONE);
                iv_cross.setVisibility(View.GONE);
                iv_refresh.setVisibility(View.GONE);
            }else if(tag.equals("REPORTS")){
                select = 3;
                tv_header.setText(R.string.reports);
                iv_back_arrow.setVisibility(View.VISIBLE);
                iv_navigation.setVisibility(View.GONE);
                iv_cross.setVisibility(View.GONE);
                tv_temp.setVisibility(View.GONE);
                iv_weather.setVisibility(View.GONE);
                iv_refresh.setVisibility(View.GONE);
            } else if (tag.equals("NOTIFICATION")) {
                select = 4;
                tv_header.setText(R.string.notification);
                iv_back_arrow.setVisibility(View.VISIBLE);
                iv_navigation.setVisibility(View.GONE);
                iv_cross.setVisibility(View.GONE);
                iv_refresh.setVisibility(View.GONE);
            } else if (tag.equals("FAVOURITES")) {
                select = 5;
                tv_header.setText(R.string.favourites);
                iv_back_arrow.setVisibility(View.VISIBLE);
                iv_navigation.setVisibility(View.GONE);
                iv_cross.setVisibility(View.GONE);
                iv_refresh.setVisibility(View.GONE);
            } else if (tag.equals("SEARCH")) {
                select = 6;
                tv_header.setText(R.string.search);
                iv_back_arrow.setVisibility(View.VISIBLE);
                iv_navigation.setVisibility(View.GONE);
                iv_cross.setVisibility(View.GONE);
            } else if (tag.equals("SETTINGS")) {
                select = 7;
                tv_header.setText(R.string.settings);
                iv_back_arrow.setVisibility(View.VISIBLE);
                iv_navigation.setVisibility(View.GONE);
                iv_cross.setVisibility(View.GONE);
                iv_refresh.setVisibility(View.GONE);
            } else if (tag.equals("HELP")) {
                select = 8;
                tv_header.setText(R.string.help);
                iv_back_arrow.setVisibility(View.VISIBLE);
                iv_navigation.setVisibility(View.GONE);
                iv_cross.setVisibility(View.GONE);
                iv_refresh.setVisibility(View.GONE);
            } else if (tag.equals("WEB FORMS")) {
                select = 11;
                tv_header.setText(R.string.web_forms);
                iv_back_arrow.setVisibility(View.VISIBLE);
                iv_navigation.setVisibility(View.GONE);
                iv_cross.setVisibility(View.GONE);
                iv_refresh.setVisibility(View.GONE);
            }else if (tag.equals("AD-HOC REPORTS") || tag.equals("VIEW AD-HOC REPORT")) {
                select = 16;
                tv_header.setText(R.string.ad_hoc_reports);
                iv_back_arrow.setVisibility(View.VISIBLE);
                iv_navigation.setVisibility(View.GONE);
                iv_cross.setVisibility(View.GONE);
                iv_refresh.setVisibility(View.GONE);
            }else if (tag.equals("INSPECTION")) {
                select = 14;
                tv_header.setText(R.string.carry_out_inspection);
                iv_back_arrow.setVisibility(View.GONE);
                iv_navigation.setVisibility(View.GONE);
                iv_cross.setVisibility(View.VISIBLE);
                iv_refresh.setVisibility(View.GONE);
            }else if (tag.equals("INSPECTION_DETAILS_FRAG") || tag.equals("INSPECTION_TRAILER_FRAG") || tag.equals("INSPECTION_SIGN_FRAG")) {
                select= 15;
                tv_header.setText(R.string.carry_out_inspection);
                iv_back_arrow.setVisibility(View.GONE);
                iv_navigation.setVisibility(View.GONE);
                iv_cross.setVisibility(View.VISIBLE);
                iv_refresh.setVisibility(View.GONE);
                iv_home.setClickable(false);
                iv_form.setClickable(false);
                iv_notification.setClickable(false);
                iv_crate.setClickable(false);
                iv_favourites.setClickable(false);
            }else if (tag.equals("VIEW INSPECTION")) {
                select= 9;
                tv_header.setText(R.string.view_inspection);
                iv_back_arrow.setVisibility(View.GONE);
                iv_navigation.setVisibility(View.GONE);
                iv_cross.setVisibility(View.VISIBLE);
                iv_refresh.setVisibility(View.GONE);
            }else if (tag.equals("VEHICLE DEFECT REPORT")) {
                select = 9;
                tv_header.setText(R.string.vehicle_defect_report);
                iv_back_arrow.setVisibility(View.GONE);
                iv_navigation.setVisibility(View.GONE);
                iv_cross.setVisibility(View.VISIBLE);
                iv_refresh.setVisibility(View.GONE);
            }else if (tag.equals("ROADWORTHY")) {
                select = 9;
                tv_header.setText(R.string.roadworthy);
                iv_navigation.setVisibility(View.INVISIBLE);
                iv_back_arrow.setVisibility(View.VISIBLE);
                tv_temp.setVisibility(View.INVISIBLE);
                iv_weather.setVisibility(View.INVISIBLE);
                iv_cross.setVisibility(View.GONE);
                iv_refresh.setVisibility(View.GONE);
            }else if (tag.equals("DEFECT VEHICLE")) {
                select = 4;
                tv_header.setText(R.string.defected_vehicles);
                iv_navigation.setVisibility(View.INVISIBLE);
                iv_back_arrow.setVisibility(View.VISIBLE);
                tv_temp.setVisibility(View.INVISIBLE);
                iv_weather.setVisibility(View.INVISIBLE);
                iv_cross.setVisibility(View.GONE);
                iv_refresh.setVisibility(View.GONE);
            }else if (tag.equals("WORKSHOP MANAGER")) {
                select = 9;
                tv_header.setText(R.string.vehicle_defect_report);
                iv_navigation.setVisibility(View.INVISIBLE);
                iv_back_arrow.setVisibility(View.INVISIBLE);
                tv_temp.setVisibility(View.INVISIBLE);
                iv_weather.setVisibility(View.INVISIBLE);
                iv_cross.setVisibility(View.VISIBLE);
                iv_refresh.setVisibility(View.GONE);
            }else if (tag.equals("SUCCESS")) {
                select = 9;
                tv_header.setText(R.string.success);
                iv_navigation.setVisibility(View.INVISIBLE);
                iv_back_arrow.setVisibility(View.INVISIBLE);
                tv_temp.setVisibility(View.INVISIBLE);
                iv_weather.setVisibility(View.INVISIBLE);
                iv_cross.setVisibility(View.VISIBLE);
                iv_refresh.setVisibility(View.GONE);
            }else if (tag.equals("REPORT_SUBMIT_SUCCESS")) {
                select = 9;
                tv_header.setText(R.string.success);
                iv_navigation.setVisibility(View.INVISIBLE);
                iv_back_arrow.setVisibility(View.INVISIBLE);
                tv_temp.setVisibility(View.INVISIBLE);
                iv_weather.setVisibility(View.INVISIBLE);
                iv_cross.setVisibility(View.VISIBLE);
                iv_refresh.setVisibility(View.GONE);
            }else if (tag.equals("CHANGE PASSWORD")) {
                select = 10;
                tv_header.setText(R.string.change_password);
                iv_navigation.setVisibility(View.INVISIBLE);
                iv_back_arrow.setVisibility(View.VISIBLE);
                tv_temp.setVisibility(View.INVISIBLE);
                iv_weather.setVisibility(View.INVISIBLE);
                iv_cross.setVisibility(View.INVISIBLE);
                iv_refresh.setVisibility(View.GONE);
            }else if (tag.equals("SUBMIT FEEDBACK")) {
                select = 10;
                tv_header.setText(R.string.submit_feedback);
                iv_navigation.setVisibility(View.INVISIBLE);
                iv_back_arrow.setVisibility(View.VISIBLE);
                tv_temp.setVisibility(View.INVISIBLE);
                iv_weather.setVisibility(View.INVISIBLE);
                iv_cross.setVisibility(View.INVISIBLE);
                iv_refresh.setVisibility(View.GONE);
            }else if (tag.equals("TERMS CONDITION")) {
                select = 10;
                tv_header.setText(R.string.terms_condition_title);
                iv_navigation.setVisibility(View.INVISIBLE);
                iv_back_arrow.setVisibility(View.VISIBLE);
                tv_temp.setVisibility(View.INVISIBLE);
                iv_weather.setVisibility(View.INVISIBLE);
                iv_cross.setVisibility(View.INVISIBLE);
                iv_refresh.setVisibility(View.GONE);
            }else if (tag.equals("MY PROFILE")) {
                select = 13;
                tv_header.setText(R.string.my_profile);
                iv_navigation.setVisibility(View.INVISIBLE);
                iv_back_arrow.setVisibility(View.VISIBLE);
                tv_temp.setVisibility(View.INVISIBLE);
                iv_weather.setVisibility(View.INVISIBLE);
                iv_cross.setVisibility(View.INVISIBLE);
                iv_refresh.setVisibility(View.GONE);
            }
            selector();
        }
    }

    private void selector(){
        if (select ==1) {
            iv_home.setImageResource(R.drawable.home_select);
            tv_header.setTextColor(getResources().getColor(R.color.blue_shade_three));
        } else {
            iv_home.setImageResource(R.drawable.home);
            tv_header.setTextColor(getResources().getColor(R.color.black_shade_five));
        }

        if (select==2 || select == 14)
            iv_form.setImageResource(R.drawable.form_select);
        else
            iv_form.setImageResource(R.drawable.form);

        if (select==3)
            iv_crate.setImageResource(R.drawable.crate_select);
        else
            iv_crate.setImageResource(R.drawable.crate);

        if (select==4)
            iv_notification.setImageResource(R.drawable.notification_select);
        else
            iv_notification.setImageResource(R.drawable.notification);

        if (select==5)
            iv_favourites.setImageResource(R.drawable.favourites_selected);
        else
            iv_favourites.setImageResource(R.drawable.favourite);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        stopForegroundDispatch(this, mNfcAdapter);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (select==15 && with_nfc.equals("No"))
          handleIntent(intent);
    }

    private void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, PendingIntent.FLAG_MUTABLE);
        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{ new String[] { Ndef.class.getName() }};
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        if (adapter != null)
            adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        if (adapter != null)
           adapter.disableForegroundDispatch(activity);
    }

//     get new intent ...
    private void handleIntent(Intent intent)
    {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            String type = intent.getType();
            if (MIME_TEXT.equals(type)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                message = ByteArrayToHexString(Objects.requireNonNull(tag).getId());
                Log.d("NFC_SERIAL_NO :" ,message);
                ndefmessage(tag);
                Log.d("NDEF_Message", "Detected: " +ndefmessage(tag));
            } else {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                message = ByteArrayToHexString(Objects.requireNonNull(tag).getId());
                Log.d("NFC_SERIAL_NO :" ,message);
                setTagValue();
            }
        }
    }

    private String ByteArrayToHexString(byte [] inarray) {
        int i, j, in;
        String [] hex = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
        String out= "";
        for(j = 0 ; j < inarray.length ; ++j)
        {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

//    // read tag data ...
    private String ndefmessage(Tag tag)
    {
        try {
            manageFragmentBackStack();
            Ndef ndef = Ndef.get(tag);
            ndef.connect();
            NdefMessage ndefMessage = ndef.getNdefMessage();
            if (select==15) {
                if (ndefMessage != null) {
                    setTagValue();
                    ndef.close();
                } else
                    scanFailedDialog();
            }
        } catch (IOException | FormatException e) {
            Log.e(TAG, "An unexpected error occurred", e);
        }
        return null;
    }

    private void setTagValue(){
        count = count + 1;
        Log.d(TAG, "readFromNFC: " + message + count);
        Bundle bundle = new Bundle();
        bundle.putString("message", message);
        bundle.putInt("counter", count);
        InspectionFragment inspectionFragment = new InspectionFragment();
        inspectionFragment.setArguments(bundle);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, inspectionFragment);
        ft.commit();
        fragment.setArguments(bundle);
    }

    private void logoutDialog(){
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to logout ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        sessionManager.logoutUser();
                        removeTaskPreferences();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        progressDialog.dismiss();
                    }
                }).show();
    }

    private void removeNfcPreferences(){
        editor = pref.edit();
        editor.remove("nfc1");
        editor.remove("nfc2");
        editor.remove("nfc3");
        editor.remove("nfc4");
        editor.remove("nfc5");
        editor.remove("nfc6");
        editor.apply();
        count = 0;
    }

    private void removeTaskPreferences(){
        editor = pref.edit();
        if (pref.contains("supply_coming_form"))
            editor.remove("supply_coming_form");
        if (pref.contains("waste_coming_form"))
            editor.remove("waste_coming_form");
        editor.remove("isSubmitted");
        editor.remove("isSubmitSupply");
        editor.remove("isSubmittedWaste");
        editor.remove("startTaskClicked");
        editor.remove("arrivedCollectionClicked");
        editor.remove("leftCollectionClicked");
        editor.remove("arrivedDeliveryClicked");
        editor.apply();
    }

    private void scanFailedDialog() {
        final View popupView = getLayoutInflater().inflate(R.layout.scan_failed_layout,null,true);
        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,true);
        popupWindow.showAtLocation(popupView, Gravity.BOTTOM,0,0);
        Button bt_cancel_popup = popupView.findViewById(R.id.bt_cancel_popup);
        Button bt_retry = popupView.findViewById(R.id.bt_retry);
        bt_cancel_popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
        bt_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
    }

    private void onApplicationBackPressed() {
        if (onDoubleBackPress) {
            this.moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
            return;
        }
        onDoubleBackPress = true;
        Toast.makeText(this, "Press Again To Exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onDoubleBackPress = false;
            }
        }, 2000);
    }

    private void checkPermission() {
        int ALL_PERMISSIONS = 101;

        final String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        ActivityCompat.requestPermissions(this, permissions, ALL_PERMISSIONS);
    }
}
