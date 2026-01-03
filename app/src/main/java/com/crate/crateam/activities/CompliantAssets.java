package com.crate.crateam.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crate.crateam.R;
import com.crate.crateam.adapters.InspectionListAdapter;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.FirestoreManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompliantAssets extends AppCompatActivity {
    
    private static final String TAG = "CompliantAssets";
    private static final long SORT_DELAY_MS = 300; // Reduced from 1000ms
    private ImageView iv_cross;
    private RecyclerView rv_inspection_list;
    private ProgressDialog progressDialog;
    private InspectionListAdapter inspectionListAdapter;
    private ArrayList<Integer> assetsRequiringInspectionIds = new ArrayList<>(50);
    private ArrayList<String> assetsRequiringInspectionNames = new ArrayList<>(50);
    private ArrayList<String> assetsRequiringInspectionNamesSorted = new ArrayList<>(50);
    private ArrayList<String> assetsRequiringInspectionNRI = new ArrayList<>(50);
    private ArrayList<String> assetsRequiringInspectionNRISorted = new ArrayList<>(50);
    private ArrayList<String> assetsRequiringInspectionNTESorted = new ArrayList<>(50);
    private ArrayList<String> assetsRequiringInspectionStatus = new ArrayList<>(50);
    private ExecutorService executorService;
    private Handler mainHandler;
    private boolean isActivityDestroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compliant_asset_layout);
        // Initialize threading components
        initializeThreading();
        // Initialize UI and data
        initializeComponents();
        // Load and process data
        loadDataFromIntent();
        // Setup UI
        setupRecyclerView();
        setupClickListeners();
    }

    private void initializeThreading() {
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    private void initializeComponents() {
        progressDialog = Dialog.showProgressDialog(this);
        FirestoreManager.initPersistentIndexManager();
        
        iv_cross = findViewById(R.id.iv_cross);
        rv_inspection_list = findViewById(R.id.rv_inspection_list);
    }

    @SuppressWarnings("unchecked")
    private void loadDataFromIntent() {
        Intent intent = getIntent();
        // Safe data extraction with null checks
        ArrayList<String> names = (ArrayList<String>) intent.getSerializableExtra("asset_name");
        if (names != null) {
            assetsRequiringInspectionNames = new ArrayList<>(names);
        }
        
        ArrayList<Integer> ids = (ArrayList<Integer>) intent.getSerializableExtra("asset_ids");
        if (ids != null) {
            assetsRequiringInspectionIds = new ArrayList<>(ids);
        }
        
        ArrayList<String> nri = (ArrayList<String>) intent.getSerializableExtra("asset_nri");
        if (nri != null) {
            assetsRequiringInspectionNRI = new ArrayList<>(nri);
        }
        
        ArrayList<String> nte = (ArrayList<String>) intent.getSerializableExtra("asset_nte");
        if (nte != null) {
            assetsRequiringInspectionNTESorted = new ArrayList<>(nte);
        }
        processAssetData();
    }

    private void processAssetData() {
        if (assetsRequiringInspectionIds.isEmpty()) {
            dismissProgress();
            return;
        }
        progressDialog.show();
        assetsRequiringInspectionStatus.clear();
        for (int i = 0; i < assetsRequiringInspectionIds.size(); i++) {
            assetsRequiringInspectionStatus.add("Green");
        }
        Log.d(TAG, "Processing " + assetsRequiringInspectionIds.size() + " assets");
        // Sort data in background thread
        sortListInBackground();
    }

    private void setupRecyclerView() {
        if (rv_inspection_list != null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            rv_inspection_list.setLayoutManager(layoutManager);
            // RecyclerView optimizations
            rv_inspection_list.setHasFixedSize(true);
            rv_inspection_list.setItemViewCacheSize(20);
            rv_inspection_list.setDrawingCacheEnabled(true);
            rv_inspection_list.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        }
    }

    private void setupClickListeners() {
        if (iv_cross != null) {
            iv_cross.setOnClickListener(v -> finish());
        }
    }

    private void sortListInBackground() {
        executorService.execute(() -> {
            try {
                sortList();
                mainHandler.postDelayed(() -> {
                    if (!isActivityDestroyed && !isFinishing()) {
                        updateUIWithSortedData();
                    }
                }, SORT_DELAY_MS);
                
            } catch (Exception e) {
                Log.e(TAG, "Error sorting list", e);
                mainHandler.post(() -> {
                    dismissProgress();
                    showError("Error processing data");
                });
            }
        });
    }

    private void updateUIWithSortedData() {
        dismissProgress();
        
        if (rv_inspection_list != null) {
            inspectionListAdapter = new InspectionListAdapter(
                    CompliantAssets.this,
                    assetsRequiringInspectionIds,
                    assetsRequiringInspectionNamesSorted,
                    assetsRequiringInspectionNRISorted,
                    assetsRequiringInspectionNTESorted,
                    assetsRequiringInspectionStatus
            );
            rv_inspection_list.setAdapter(inspectionListAdapter);
            setupAdapterClick();
        }
    }

    private void sortList() {
        int size = assetsRequiringInspectionIds.size();
        
        // Validate data consistency
        if (!validateDataConsistency(size)) {
            Log.e(TAG, "Data inconsistency detected");
            return;
        }

        List<CompliantEntity> entities = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            entities.add(new CompliantEntity(
                    assetsRequiringInspectionNames.get(i),
                    assetsRequiringInspectionNRI.get(i),
                    assetsRequiringInspectionNTESorted.get(i),
                    assetsRequiringInspectionIds.get(i)
            ));
        }
        Collections.sort(entities);
        clearSortedLists();
        for (CompliantEntity entity : entities) {
            assetsRequiringInspectionNamesSorted.add(entity.name);
            assetsRequiringInspectionNRISorted.add(entity.nri);
            assetsRequiringInspectionNTESorted.add(entity.nte);
            assetsRequiringInspectionIds.add(entity.id);
        }
        
        Log.d(TAG, "Sorting completed for " + entities.size() + " items");
    }

    private boolean validateDataConsistency(int expectedSize) {
        return assetsRequiringInspectionNames.size() == expectedSize &&
               assetsRequiringInspectionNRI.size() == expectedSize &&
               assetsRequiringInspectionNTESorted.size() == expectedSize;
    }

    private void clearSortedLists() {
        assetsRequiringInspectionNamesSorted.clear();
        assetsRequiringInspectionNRISorted.clear();
        assetsRequiringInspectionNTESorted.clear();
        assetsRequiringInspectionIds.clear();
    }

    static class CompliantEntity implements Comparable<CompliantEntity> {
        final String name;
        final String nri;
        final String nte;
        final int id;
        CompliantEntity(String name, String nri, String nte, int id) {
            this.name = name;
            this.nri = nri;
            this.nte = nte;
            this.id = id;
        }
        
        @Override
        public int compareTo(@NonNull CompliantEntity other) {
            // Use Integer.compare for cleaner comparison
            return Integer.compare(this.id, other.id);
        }
    }

    private void setupAdapterClick() {
        if (inspectionListAdapter == null) return;
        
        inspectionListAdapter.setOnItemClickListener(new InspectionListAdapter.OnViewClickListener() {
            @Override
            public void onViewClick(int position) {
                if (isValidPosition(position)) {
                    navigateToAssetDetails(position);
                }
            }
            
            @Override
            public void onDatesClick(int position) {
                // Implementation as needed
            }
            
            @Override
            public void onDatesSecondClick(int position) {
                // Implementation as needed
            }
            
            @Override
            public void onIssueClick(int position) {
                // Implementation as needed
            }
            
            @Override
            public void onInspectionClick(int position) {
                // Implementation as needed
            }
        });
    }

    private boolean isValidPosition(int position) {
        return position >= 0 && position < assetsRequiringInspectionIds.size();
    }

    private void navigateToAssetDetails(int position) {
        Intent intent = new Intent(CompliantAssets.this, AssetDetails.class);
        intent.putExtra("assetId", assetsRequiringInspectionIds.get(position));
        intent.putExtra("activity", "DashboardListing");
        startActivity(intent);
    }

    private void dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            try {
                progressDialog.dismiss();
            } catch (Exception e) {
                Log.e(TAG, "Error dismissing progress dialog", e);
            }
        }
    }

    private void showError(String message) {
        // Implement error display as needed (Toast, Snackbar, etc.)
        Log.e(TAG, message);
    }

    private void clearAllLists() {
        if (assetsRequiringInspectionIds != null) assetsRequiringInspectionIds.clear();
        if (assetsRequiringInspectionNames != null) assetsRequiringInspectionNames.clear();
        if (assetsRequiringInspectionNamesSorted != null) assetsRequiringInspectionNamesSorted.clear();
        if (assetsRequiringInspectionNRI != null) assetsRequiringInspectionNRI.clear();
        if (assetsRequiringInspectionNRISorted != null) assetsRequiringInspectionNRISorted.clear();
        if (assetsRequiringInspectionNTESorted != null) assetsRequiringInspectionNTESorted.clear();
        if (assetsRequiringInspectionStatus != null) assetsRequiringInspectionStatus.clear();
    }

    @Override
    protected void onPause() {
        super.onPause();
        dismissProgress();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
    }
    
    @Override
    protected void onDestroy() {
        isActivityDestroyed = true;
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
            mainHandler = null;
        }
        if (inspectionListAdapter != null) {
            inspectionListAdapter = null;
        }
        if (rv_inspection_list != null) {
            rv_inspection_list.setAdapter(null);
        }
        clearAllLists();
        dismissProgress();
        super.onDestroy();
    }
    
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        
        switch (level) {
            case TRIM_MEMORY_RUNNING_CRITICAL:
            case TRIM_MEMORY_RUNNING_LOW:
                // Clear data when memory is critical
                clearAllLists();
                break;
            case TRIM_MEMORY_MODERATE:
            case TRIM_MEMORY_BACKGROUND:
                // Trim as needed
                System.gc();
                break;
        }
    }
    
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        clearAllLists();
        System.gc();
    }
}
