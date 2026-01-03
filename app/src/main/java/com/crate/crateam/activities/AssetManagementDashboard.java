package com.crate.crateam.activities;

import static androidx.constraintlayout.widget.Constraints.TAG;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.crate.crateam.R;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.crate.crateam.utility.FirestoreManager;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class AssetManagementDashboard extends AppCompatActivity 
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private final FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference assetDetailsReference, assetInspectionSubmissionReference,
            assetInspectionDefectsReference, assetInspectionElementsReference,
            assetRegimeInspectionElementDetailsReference, regimeDetailsReference,
            amAssignSiteReference, assetGroupReference, assetTypeReference,
            assetTypeElementReference, assetInspectionRegimeReference, siteLocationReference,
            assetRegimeReference, assetCustomerDetailsReference, vehicleManufacturerReference,
            vehicleModelReference, assetNfcDetailsReference, assetQRDetailsReference,
            assetMoreDetailsReference, offHireReference, userDetailsReference,
            sortKeyTableReference, assignTaskReference;

    private TextView tv_logout, tv_last_refresh, tv_forms, tv_assign_site, tv_user_name,
            tv_asset_attention_count, tv_asset_inspection_count, tv_inspection_24hr,
            tv_asset_good_working, tv_all_assets_count, tv_location;
    private ImageView iv_navigation, iv_refresh;
    private DrawerLayout mDrawerLayout;
    private LinearLayout ll_all_assets, ll_asset_attention, ll_asset_inspection,
            ll_inspection_24hr, ll_assets_working;
    private String userName = "",siteLocationName = "",currentDate,currentTime;
    private int userId = 0,siteLocationId = 0,role_id = 0;
    // Thread-safe sync counter for parallel operations
    private final AtomicInteger syncCounter = new AtomicInteger(0);
    private static final int TOTAL_SYNC_COLLECTIONS = 23;

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = 
            ThreadLocal.withInitial(() -> new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()));
    private static final ThreadLocal<SimpleDateFormat> TIME_FORMAT = 
            ThreadLocal.withInitial(() -> new SimpleDateFormat("HH:mm:ss", Locale.getDefault()));
    private Date currentDateParsed;

    private static class AssetData implements Comparable<AssetData> {
        final int id;
        final String name;
        final String inspectionStatus;
        final String dateStatus;
        final String nextRoutineInspectionDate;
        final String nteDate;
        String status = "Grey";
        Date nriDateParsed;
        Date nteDateParsed;
        boolean isNriOverdue = false;
        boolean isNteOverdue = false;
        Date oneDayBeforeNri;

        AssetData(int id, String name, String inspectionStatus, String dateStatus,
                  String nextRoutineInspectionDate, String nteDate) {
            this.id = id;
            this.name = name;
            this.inspectionStatus = inspectionStatus != null ? inspectionStatus : "";
            this.dateStatus = dateStatus != null ? dateStatus : "";
            this.nextRoutineInspectionDate = nextRoutineInspectionDate != null ? nextRoutineInspectionDate : "";
            this.nteDate = nteDate != null ? nteDate : "";
        }

        @Override
        public int compareTo(AssetData other) {
            return Integer.compare(this.id, other.id);
        }
        // Check if inspection is due within 24 hours
        boolean isInspectionDueNextDay(Date currentDate) {
            return oneDayBeforeNri != null && isSameDay(oneDayBeforeNri, currentDate);
        }

        boolean requiresInspection() {
            return (isNriOverdue || isNteOverdue || "Non Compliant".equals(dateStatus))
                    && ("Good working order.".equals(inspectionStatus) || inspectionStatus.isEmpty());
        }

        boolean isDefected() {
            return "Defected not safe to use.".equals(inspectionStatus) 
                    || "Defected but safe to use.".equals(inspectionStatus);
        }

        boolean isGoodWorking() {
            return "Good working order.".equals(inspectionStatus) && "Compliant".equals(dateStatus);
        }

        private static boolean isSameDay(Date d1, Date d2) {
            if (d1 == null || d2 == null) return false;
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(d1);
            cal2.setTime(d2);
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                    && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        }
    }
    private final List<AssetData> allAssets = new ArrayList<>(100);
    private final List<AssetData> assetsRequiringInspection = new ArrayList<>(50);
    private final List<AssetData> assetsRequiringAttention = new ArrayList<>(50);
    private final List<AssetData> assetsGoodWorking = new ArrayList<>(50);
    private final List<AssetData> assetsInspectionNextDay = new ArrayList<>(20);
    private final Map<Integer, AssetData> assetById = new HashMap<>(100);
    private final Map<Integer, String> inspectionStatusByAssetId = new HashMap<>(100);
    private final Set<Integer> inspectionRequiredIds = new HashSet<>(50);
    private ProgressDialog progressDialog;
    private ProgressDialog progressDialogWithMessage;
    private SessionManager sessionManager;
    private SharedPreferences pref;
    private final Calendar reusableCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.asset_main);
        initView();
    }

    private void initView() {
        initializeDialogs();
        initializeFirestoreReferences();
        initializeDates();
        initializeUserSession();
        initializeUIComponents();
        initializeClickListeners();
        
        listenOfflineData(regimeDetailsReference);
        
        progressDialog.show();
        getAssignSiteName();
        checkPermission();
    }

    private void initializeDialogs() {
        progressDialog = Dialog.showProgressDialog(this);
        progressDialogWithMessage = Dialog.showProgressDialogWithMessage(this);
    }

    private void initializeFirestoreReferences() {
        // Initialize all collection references in a single block
        assetDetailsReference = db.collection("AM_asset_details");
        assetInspectionSubmissionReference = db.collection("AM_asset_inspection_submission");
        amAssignSiteReference = db.collection("AM_assign_site");
        regimeDetailsReference = db.collection("AM_asset_revalidation_details");
        assetGroupReference = db.collection("AM_group");
        assetTypeReference = db.collection("AM_asset_type");
        assetTypeElementReference = db.collection("AM_asset_type_elements");
        assetInspectionDefectsReference = db.collection("AM_asset_inspection_defects");
        assetRegimeInspectionElementDetailsReference = db.collection("AM_regime_inspection_elements_details");
        assetInspectionElementsReference = db.collection("AM_inspection_elements");
        assetInspectionRegimeReference = db.collection("AM_asset_inspection_regimes");
        siteLocationReference = db.collection("AM_site_location");
        assetRegimeReference = db.collection("AM_regime");
        assetCustomerDetailsReference = db.collection("AM_customer_details");
        vehicleManufacturerReference = db.collection("AM_vehicle_manufacturers");
        vehicleModelReference = db.collection("AM_vehicle_models");
        assetNfcDetailsReference = db.collection("AM_NFC_details");
        assetQRDetailsReference = db.collection("AM_QR_details");
        assetMoreDetailsReference = db.collection("AM_asset_more_details");
        offHireReference = db.collection("AM_asset_off_hire");
        userDetailsReference = db.collection("CR_user_details");
        sortKeyTableReference = db.collection("CR_sort_key");
        assignTaskReference = db.collection("AM_task_assigner");
    }

    private void initializeDates() {
        Date now = new Date();
        SimpleDateFormat dateFormat = DATE_FORMAT.get();
        SimpleDateFormat timeFormat = TIME_FORMAT.get();
        currentDate = dateFormat.format(now);
        currentTime = timeFormat.format(now);
        
        try {
            currentDateParsed = dateFormat.parse(currentDate);
        } catch (ParseException e) {
            Log.e(TAG, "Failed to parse current date", e);
            currentDateParsed = now;
        }
        pref = getSharedPreferences("MyPref", MODE_PRIVATE);
    }

    private void initializeUserSession() {
        sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        userName = user.get(SessionManager.KEY_FULL_NAME);
        userId = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        
        String roleStr = user.get(SessionManager.KEY_ROLE_ONE);
        if (roleStr != null) {
            role_id = Integer.parseInt(roleStr);
        }
        Log.d(TAG, "USER_ID: " + userId + " role: " + role_id + " time: " + AppData.getTimeSecond());
    }

    private void initializeUIComponents() {
        // Dashboard cards
        ll_asset_attention = findViewById(R.id.ll_asset_attention);
        ll_asset_inspection = findViewById(R.id.ll_asset_inspection);
        ll_inspection_24hr = findViewById(R.id.ll_inspection_24hr);
        ll_assets_working = findViewById(R.id.ll_assets_working);
        ll_all_assets = findViewById(R.id.ll_all_assets);
        
        // Text views
        tv_user_name = findViewById(R.id.tv_user_name);
        tv_user_name.setText("Hello, " + userName);
        iv_refresh = findViewById(R.id.iv_refresh);
        tv_last_refresh = findViewById(R.id.tv_last_refresh);
        tv_forms = findViewById(R.id.tv_forms);
        tv_assign_site = findViewById(R.id.tv_assign_site);
        tv_location = findViewById(R.id.tv_location);
        tv_asset_attention_count = findViewById(R.id.tv_asset_attention_count);
        tv_asset_inspection_count = findViewById(R.id.tv_asset_inspection_count);
        tv_inspection_24hr = findViewById(R.id.tv_inspection_24hr);
        tv_asset_good_working = findViewById(R.id.tv_asset_good_working);
        tv_all_assets_count = findViewById(R.id.tv_all_assets_count);
        
        // Navigation
        iv_navigation = findViewById(R.id.iv_navigation);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);
        
        // Disable over-scroll on navigation children
        for (int i = 0; i < navigationView.getChildCount(); i++) {
            navigationView.getChildAt(i).setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
        
        tv_logout = navigationView.findViewById(R.id.tv_logout);
        
        View headerView = navigationView.getHeaderView(0);
        TextView tv_userName = headerView.findViewById(R.id.tv_userName);
        if (userName != null && !userName.isEmpty()) {
            tv_userName.setText(userName);
        }
    }

    private void initializeClickListeners() {
        View[] clickables = {
            iv_refresh, tv_forms, tv_assign_site, ll_asset_attention,
            ll_asset_inspection, ll_inspection_24hr, ll_assets_working,
            ll_all_assets, iv_navigation, tv_logout
        };
        for (View v : clickables) {
            v.setOnClickListener(this);
        }
    }
    private void checkPermission() {
        String[] permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ActivityCompat.requestPermissions(this, permissions, 101);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        
        if (id == R.id.iv_navigation) {
            toggleDrawer();
        } else if (id == R.id.tv_logout) {
            handleLogout();
        } else if (id == R.id.iv_refresh) {
            handleRefresh();
        } else if (id == R.id.tv_forms) {
            startActivity(new Intent(this, AssetForms.class));
        } else if (id == R.id.ll_asset_attention) {
            navigateToNonCompliantAssets();
        } else if (id == R.id.ll_assets_working) {
            navigateToCompliantAssets();
        } else if (id == R.id.ll_inspection_24hr) {
            navigateToInspectionDue();
        } else if (id == R.id.ll_asset_inspection) {
            navigateToInspectionList();
        } else if (id == R.id.ll_all_assets) {
            navigateToAllAssets();
        } else if (id == R.id.tv_assign_site) {
            startActivity(new Intent(this, AssetAssignSiteActivity.class));
            finish();
        }
    }

    private void toggleDrawer() {
        if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void handleLogout() {
        if (AppData.internetOnline(getApplicationContext())) {
            progressDialog.show();
            logoutDialog();
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void handleRefresh() {
        progressDialog.show();
        Dialog.DismissProgressDialog(progressDialog, this);
        getAssignSiteName();
        
        String refreshTime = TIME_FORMAT.get().format(Calendar.getInstance().getTime());
        tv_last_refresh.setText("Last refresh : " + refreshTime);
    }

    private void navigateToNonCompliantAssets() {
        Intent intent = new Intent(this, NonCompliantAssets.class);
        putAssetListExtras(intent, assetsRequiringAttention);
        startActivity(intent);
    }

    private void navigateToCompliantAssets() {
        Intent intent = new Intent(this, CompliantAssets.class);
        putAssetListExtras(intent, assetsGoodWorking);
        startActivity(intent);
    }

    private void navigateToInspectionDue() {
        Intent intent = new Intent(this, InspectionDue.class);
        putAssetListExtras(intent, assetsInspectionNextDay);
        startActivity(intent);
    }

    private void navigateToInspectionList() {
        Intent intent = new Intent(this, InspectionList.class);
        putAssetListExtras(intent, assetsRequiringInspection);
        startActivity(intent);
    }

    private void navigateToAllAssets() {
        Intent intent = new Intent(this, AllAssets.class);
        // For all assets, also include status
        ArrayList<Integer> ids = new ArrayList<>(allAssets.size());
        ArrayList<String> nris = new ArrayList<>(allAssets.size());
        ArrayList<String> ntes = new ArrayList<>(allAssets.size());
        ArrayList<String> names = new ArrayList<>(allAssets.size());
        ArrayList<String> statuses = new ArrayList<>(allAssets.size());
        
        for (AssetData asset : allAssets) {
            ids.add(asset.id);
            nris.add(asset.nextRoutineInspectionDate);
            ntes.add(asset.nteDate);
            names.add(asset.name);
            statuses.add(asset.status);
        }
        
        intent.putExtra("asset_ids", ids);
        intent.putExtra("asset_nri", nris);
        intent.putExtra("asset_nte", ntes);
        intent.putExtra("asset_name", names);
        intent.putExtra("asset_status", statuses);
        startActivity(intent);
    }

    private void putAssetListExtras(Intent intent, List<AssetData> assets) {
        int size = assets.size();
        ArrayList<Integer> ids = new ArrayList<>(size);
        ArrayList<String> nris = new ArrayList<>(size);
        ArrayList<String> ntes = new ArrayList<>(size);
        ArrayList<String> names = new ArrayList<>(size);
        
        for (AssetData asset : assets) {
            ids.add(asset.id);
            nris.add(asset.nextRoutineInspectionDate);
            ntes.add(asset.nteDate);
            names.add(asset.name);
        }
        intent.putExtra("asset_ids", ids);
        intent.putExtra("asset_nri", nris);
        intent.putExtra("asset_nte", ntes);
        intent.putExtra("asset_name", names);
    }

    @Override
    public void onRestart() {
        super.onRestart();
        this.recreate();
    }

    private Source getDataSource() {
        return AppData.internetOnline(this) ? Source.SERVER : Source.CACHE;
    }
    private void getAssignSiteName() {
        Query query = amAssignSiteReference
                .whereEqualTo("user_id", userId)
                .whereEqualTo("date", currentDate);
        
        query.get(getDataSource())
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        siteLocationId = Objects.requireNonNull(doc.getLong("site_id")).intValue();
                        siteLocationName = doc.getString("site_name");
                    }
                    Log.d(TAG, "SITE ID: " + siteLocationId);
                    tv_location.setText(siteLocationName);
                    getAllAssets();
                } else {
                    alertDialogAssignSite("You are not assigned with any site. Please assign yourself with a site.");
                }
            })
            .addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void getAllAssets() {
        Query query = assetDetailsReference
                .whereEqualTo("status", "Active")
                .whereEqualTo("assign_location", siteLocationId);
        
        query.get(getDataSource())
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    clearAllAssetData();
                    SimpleDateFormat dateFormat = DATE_FORMAT.get();
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        AssetData asset = new AssetData(
                            Objects.requireNonNull(doc.getLong("id")).intValue(),
                            doc.getString("asset_name"),
                            doc.getString("asset_inspection_status"),
                            doc.getString("asset_current_status"),
                            doc.getString("next_routine_inspection_date"),
                            doc.getString("date_1")
                        );
                        parseDates(asset, dateFormat);
                        computeDateFlags(asset);
                        
                        allAssets.add(asset);
                        assetById.put(asset.id, asset);
                    }

                    Collections.sort(allAssets);
                    categorizeAssets();
                    tv_all_assets_count.setText(String.valueOf(allAssets.size()));
                    tv_inspection_24hr.setText(String.valueOf(assetsInspectionNextDay.size()));
                    tv_asset_inspection_count.setText(String.valueOf(assetsRequiringInspection.size()));
                    
                    Log.d(TAG, "Loaded " + allAssets.size() + " assets");
                    getAssetStatusFromAssetInspection();
                }
            })
            .addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void clearAllAssetData() {
        allAssets.clear();
        assetById.clear();
        assetsRequiringInspection.clear();
        assetsRequiringAttention.clear();
        assetsGoodWorking.clear();
        assetsInspectionNextDay.clear();
        inspectionStatusByAssetId.clear();
        inspectionRequiredIds.clear();
    }
    private void parseDates(AssetData asset, SimpleDateFormat dateFormat) {
        try {
            if (!asset.nextRoutineInspectionDate.isEmpty()) {
                asset.nriDateParsed = dateFormat.parse(asset.nextRoutineInspectionDate);
                // Compute one day before NRI using reusable calendar
                if (asset.nriDateParsed != null) {
                    reusableCalendar.setTime(asset.nriDateParsed);
                    reusableCalendar.add(Calendar.DATE, -1);
                    asset.oneDayBeforeNri = reusableCalendar.getTime();
                }
            }
            if (!asset.nteDate.isEmpty()) {
                asset.nteDateParsed = dateFormat.parse(asset.nteDate);
            }
        } catch (ParseException e) {
            Log.w(TAG, "Failed to parse date for asset " + asset.id, e);
        }
    }

    private void computeDateFlags(AssetData asset) {
        if (currentDateParsed != null) {
            if (asset.nriDateParsed != null) {
                asset.isNriOverdue = asset.nriDateParsed.before(currentDateParsed);
            }
            if (asset.nteDateParsed != null) {
                asset.isNteOverdue = asset.nteDateParsed.before(currentDateParsed);
            }
        }
    }

    private void categorizeAssets() {
        for (AssetData asset : allAssets) {
            // Check if inspection is due next day
            if (asset.isInspectionDueNextDay(currentDateParsed)) {
                assetsInspectionNextDay.add(asset);
            }
            // Initial inspection requirement check
            if (asset.requiresInspection()) {
                assetsRequiringInspection.add(asset);
                inspectionRequiredIds.add(asset.id);
            }
        }
    }

    private void getAssetStatusFromAssetInspection() {
        Query query = assetInspectionSubmissionReference.whereEqualTo("status", "Active");
        
        query.get(getDataSource())
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    // Build lookup map - O(n)
                    inspectionStatusByAssetId.clear();
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        int assetId = doc.getLong("asset_id").intValue();
                        String status = doc.getString("asset_status");
                        inspectionStatusByAssetId.put(assetId, status);
                    }
                    
                    Log.d(TAG, "Loaded " + inspectionStatusByAssetId.size() + " inspection records");
                }
                finalizeAssetCategorization();
                updateUI();
                progressDialog.dismiss();
            })
            .addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void finalizeAssetCategorization() {
        assetsRequiringAttention.clear();
        assetsGoodWorking.clear();
        
        for (AssetData asset : allAssets) {
            // Determine status color
            if (asset.isDefected()) {
                asset.status = "Red";
                assetsRequiringAttention.add(asset);
            } else if ("Non Compliant".equals(asset.dateStatus) && 
                       ("Good working order.".equals(asset.inspectionStatus) || asset.inspectionStatus.isEmpty())) {
                asset.status = "Orange";
            } else if (asset.isGoodWorking()) {
                asset.status = "Green";
                // Only add to good working if not requiring inspection
                if (!inspectionRequiredIds.contains(asset.id)) {
                    assetsGoodWorking.add(asset);
                }
            } else if (asset.inspectionStatus.isEmpty() && "Compliant".equals(asset.dateStatus)) {
                asset.status = "Grey";
            } else {
                asset.status = "Grey";
            }
        }
    }
    private void updateUI() {
        tv_asset_attention_count.setText(String.valueOf(assetsRequiringAttention.size()));
        tv_asset_good_working.setText(String.valueOf(assetsGoodWorking.size()));
        
        Log.d(TAG, "UI Updated - Attention: " + assetsRequiringAttention.size() + 
                   ", Good: " + assetsGoodWorking.size() +
                   ", Inspection: " + assetsRequiringInspection.size());
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_transport) {
            if (role_id != 0) {
                startActivity(new Intent(this, MainActivity.class));
            } else {
                Dialog.alertDialog(this, "You don't have any permission to access Transport.");
            }
        } else if (id == R.id.nav_sync_data) {
            startDataSync();
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    private void startDataSync() {
        progressDialogWithMessage.show();
        syncCounter.set(0);
        
        CollectionReference[] collections = {
            assetDetailsReference, assetInspectionSubmissionReference, amAssignSiteReference,
            regimeDetailsReference, assetGroupReference, assetTypeReference,
            assetTypeElementReference, assetRegimeInspectionElementDetailsReference,
            assetInspectionElementsReference, assetInspectionRegimeReference,
            siteLocationReference, assetRegimeReference, assetCustomerDetailsReference,
            vehicleManufacturerReference, vehicleModelReference, assetNfcDetailsReference,
            assetQRDetailsReference, assetMoreDetailsReference, offHireReference,
            userDetailsReference, sortKeyTableReference, assignTaskReference,
            assetInspectionDefectsReference
        };
        for (CollectionReference collection : collections) {
            syncData(collection);
        }
    }

    private void syncData(CollectionReference collectionReference) {
        Query query = collectionReference.whereEqualTo("status", "Active");
        
        query.get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    int count = syncCounter.incrementAndGet();
                    Log.d(TAG, "Sync done: " + collectionReference.getId() + " " + count);
                    
                    if (count >= TOTAL_SYNC_COLLECTIONS || task.getResult().isEmpty()) {
                        progressDialogWithMessage.dismiss();
                        Dialog.alertDialog(this, "Data Synced Completed.");
                    }
                }
            })
            .addOnFailureListener(e -> {
                progressDialogWithMessage.dismiss();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    // ==================== OFFLINE DATA LISTENER ====================
    private void listenOfflineData(CollectionReference collectionReference) {
        collectionReference.addSnapshotListener(MetadataChanges.INCLUDE, (querySnapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen error", e);
                return;
            }
            
            if (querySnapshot != null) {
                for (DocumentChange change : querySnapshot.getDocumentChanges()) {
                    if (change.getType() == DocumentChange.Type.ADDED) {
                        Log.d(TAG, "Data: " + change.getDocument().getData());
                    }
                }
                String source = querySnapshot.getMetadata().isFromCache() ? "local cache" : "server";
                Log.d(TAG, "Data fetched from " + source);
            }
        });
    }

    // ==================== DIALOGS ====================
    private void alertDialogAssignSite(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent assignSite = new Intent(AssetManagementDashboard.this,AssetAssignSiteActivity.class);
                        startActivity(assignSite);
                        finish();
                    }
                });
        alertDialog.show();
    }

    private void logoutDialog() {
        new AlertDialog.Builder(this)
            .setMessage("Are you sure you want to logout?")
            .setCancelable(false)
            .setPositiveButton("Yes", (dialog, which) -> {
                FirebaseAuth.getInstance().signOut();
                sessionManager.logoutUser();
                removeTaskPreferences();
            })
            .setNegativeButton("No", (dialog, which) -> {
                dialog.cancel();
                progressDialog.dismiss();
            })
            .show();
    }

    private void removeTaskPreferences() {
        SharedPreferences.Editor editor = pref.edit();
        String[] keysToRemove = {
            "supply_coming_form", "waste_coming_form", "isSubmitted",
            "isSubmitSupply", "isSubmittedWaste", "startTaskClicked",
            "arrivedCollectionClicked", "leftCollectionClicked", "arrivedDeliveryClicked"
        };
        for (String key : keysToRemove) {
            editor.remove(key);
        }
        editor.apply();
    }
}
