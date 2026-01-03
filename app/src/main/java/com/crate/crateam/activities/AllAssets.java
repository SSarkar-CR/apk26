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
import com.crate.crateam.adapters.AllAssetsAdapter;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.FirestoreManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AllAssets extends AppCompatActivity {
    private static final String TAG = "AllAssets";
    private static final long SORT_DELAY_MS = 300; // Reduced from 1000ms
    private ImageView iv_cross;
    private RecyclerView rv_inspection_list;
    private ProgressDialog progressDialog;
    private AllAssetsAdapter allAssetsAdapter;
    private ArrayList<Integer> assetsRequiringInspectionIds = new ArrayList<>(100);
    private ArrayList<String> assetsRequiringInspectionNames = new ArrayList<>(100);
    private ArrayList<String> assetsRequiringInspectionNamesSorted = new ArrayList<>(100);
    private ArrayList<String> assetsRequiringInspectionNRI = new ArrayList<>(100);
    private ArrayList<String> assetsRequiringInspectionNRISorted = new ArrayList<>(100);
    private ArrayList<String> assetsRequiringInspectionNTESorted = new ArrayList<>(100);
    private ArrayList<String> assetsRequiringInspectionStatus = new ArrayList<>(100);
    private ArrayList<String> assetsRequiringInspectionStatusSorted = new ArrayList<>(100);
    private ExecutorService executorService;
    private Handler mainHandler;
    private boolean isActivityDestroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_asset_layout);
        initializeThreading();
        initializeComponents();
        loadDataFromIntent();
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
        
        ArrayList<String> status = (ArrayList<String>) intent.getSerializableExtra("asset_status");
        if (status != null) {
            assetsRequiringInspectionStatus = new ArrayList<>(status);
        }

        processAssetData();
    }

    private void processAssetData() {
        if (assetsRequiringInspectionIds.isEmpty()) {
            dismissProgress();
            return;
        }
        progressDialog.show();
        Log.d(TAG, "Processing " + assetsRequiringInspectionIds.size() + " assets (all statuses)");

        sortListInBackground();
    }

    private void setupRecyclerView() {
        if (rv_inspection_list != null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            rv_inspection_list.setLayoutManager(layoutManager);
            // RecyclerView optimizations for smooth scrolling
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
            allAssetsAdapter = new AllAssetsAdapter(
                    AllAssets.this,
                    assetsRequiringInspectionIds,
                    assetsRequiringInspectionNamesSorted,
                    assetsRequiringInspectionNRISorted,
                    assetsRequiringInspectionNTESorted,
                    assetsRequiringInspectionStatusSorted
            );
            
            rv_inspection_list.setAdapter(allAssetsAdapter);
            setupAdapterClick();
        }
    }

    private void sortList() {
        int size = assetsRequiringInspectionIds.size();

        if (!validateDataConsistency(size)) {
            Log.e(TAG, "Data inconsistency detected");
            return;
        }

        List<AllAssetsEntity> entities = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            entities.add(new AllAssetsEntity(
                    assetsRequiringInspectionNames.get(i),
                    assetsRequiringInspectionNRI.get(i),
                    assetsRequiringInspectionNTESorted.get(i),
                    assetsRequiringInspectionStatus.get(i),
                    assetsRequiringInspectionIds.get(i)
            ));
        }
        Collections.sort(entities);

        clearSortedLists();
        
        for (AllAssetsEntity entity : entities) {
            assetsRequiringInspectionNamesSorted.add(entity.name);
            assetsRequiringInspectionNRISorted.add(entity.nri);
            assetsRequiringInspectionNTESorted.add(entity.nte);
            assetsRequiringInspectionStatusSorted.add(entity.status);
            assetsRequiringInspectionIds.add(entity.id);
        }
        Log.d(TAG, "Sorting completed for " + entities.size() + " items");
    }

    private boolean validateDataConsistency(int expectedSize) {
        return assetsRequiringInspectionNames.size() == expectedSize &&
               assetsRequiringInspectionNRI.size() == expectedSize &&
               assetsRequiringInspectionNTESorted.size() == expectedSize &&
               assetsRequiringInspectionStatus.size() == expectedSize;
    }

    private void clearSortedLists() {
        assetsRequiringInspectionNamesSorted.clear();
        assetsRequiringInspectionNRISorted.clear();
        assetsRequiringInspectionNTESorted.clear();
        assetsRequiringInspectionStatusSorted.clear();
        assetsRequiringInspectionIds.clear();
    }

    static class AllAssetsEntity implements Comparable<AllAssetsEntity> {
        final String name;
        final String nri;
        final String nte;
        final String status;
        final int id;
        
        AllAssetsEntity(String name, String nri, String nte, String status, int id) {
            this.name = name;
            this.nri = nri;
            this.nte = nte;
            this.status = status;
            this.id = id;
        }
        
        @Override
        public int compareTo(@NonNull AllAssetsEntity other) {
            // Use Integer.compare for cleaner comparison
            return Integer.compare(this.id, other.id);
        }
    }

    private void setupAdapterClick() {
        if (allAssetsAdapter == null) return;
        
        allAssetsAdapter.setOnItemClickListener(new AllAssetsAdapter.OnViewClickListener() {
            @Override
            public void onViewClick(int position) {
                if (isValidPosition(position)) {
                    navigateToAssetDetails(position);
                }
            }
            
            @Override
            public void onInspectionClick(int position) {
                if (isValidPosition(position)) {
                    navigateToAssetInspection(position);
                }
            }
        });
    }

    private boolean isValidPosition(int position) {
        return position >= 0 && position < assetsRequiringInspectionIds.size();
    }

    private void navigateToAssetDetails(int position) {
        Intent intent = new Intent(AllAssets.this, AssetDetails.class);
        intent.putExtra("assetId", assetsRequiringInspectionIds.get(position));
        intent.putExtra("activity", "DashboardListing");
        startActivity(intent);
    }

    private void navigateToAssetInspection(int position) {
        Intent intent = new Intent(AllAssets.this, AssetInspection.class);
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
        if (assetsRequiringInspectionStatusSorted != null) assetsRequiringInspectionStatusSorted.clear();
    }

    @Override
    protected void onPause() {
        super.onPause();
        dismissProgress();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        // Remove pending handler callbacks to prevent memory leaks
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

        if (allAssetsAdapter != null) {
            allAssetsAdapter = null;
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
                // Suggest garbage collection
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
