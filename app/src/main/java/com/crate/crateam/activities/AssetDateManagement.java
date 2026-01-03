package com.crate.crateam.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crate.crateam.R;
import com.crate.crateam.adapters.AssetDateManagementAdapter;
import com.crate.crateam.utility.Dialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheIndexManager;
import com.crate.crateam.utility.FirestoreManager;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;

public class AssetDateManagement extends AppCompatActivity implements View.OnClickListener{
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference assetDetailsReference,assetTypeReference, regimeDetailsReference;
    private TextView tv_asset_type,tv_asset_id,tv_asset_name;
    private Button bt_back;
    private RecyclerView rv_asset_date_regimes;
    private AssetDateManagementAdapter assetDateManagementAdapter;
    private String asset_type="",asset_number="",asset_name="",currentDate="",activity="",asset_nri="",asset_nri_failed="",identification_method="";
    private int assetId=0,assetTypeId=0;
    private ArrayList<Integer> arrayList_regime_element_id  = new ArrayList<>();
    private ArrayList<Integer> arraylist_time_second = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_name = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_value = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_view = new ArrayList<>();
    private ArrayList<Integer> arrayList_regime_element_id_sorted  = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_name_sorted = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_value_sorted = new ArrayList<>();
    private ArrayList<String> assetsRequiringInspection = new ArrayList<>();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.asset_date_management);
        initView();
        initializeOnClick();
    }

    private void initView(){
        progressDialog = Dialog.showProgressDialog(this);
        FirestoreManager.initPersistentIndexManager();
        regimeDetailsReference = db.collection("AM_asset_revalidation_details");
        assetDetailsReference = db.collection("AM_asset_details");
        assetTypeReference = db.collection("AM_asset_type");
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        currentDate = simpleDateFormat.format(date);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            asset_type= extras.getString("asset_type");
            asset_number= extras.getString("asset_id");
            asset_name= extras.getString("asset_name");
            identification_method= extras.getString("identification_method");
            assetId = extras.getInt("assetId");
            activity = extras.getString("activity");
            asset_nri = extras.getString("asset_nri");
            if (asset_nri!=null)
             compareNRIDate(asset_nri);
        }
        if (activity!=null) {
            if (activity.equals("DashboardListing")) {
                progressDialog.show();
                getAssetDetailsFromDashboardListing(assetId);
            }
        }
        Log.d("RTETHD :" ,asset_nri);
        tv_asset_type = findViewById(R.id.tv_asset_type);
        tv_asset_id = findViewById(R.id.tv_asset_id);
        tv_asset_name = findViewById(R.id.tv_asset_name);
        tv_asset_type.setText(asset_type);
        tv_asset_id.setText(asset_number);
        tv_asset_name.setText(asset_name);
        bt_back = findViewById(R.id.bt_back);
        rv_asset_date_regimes = findViewById(R.id.rv_asset_date_regimes);
        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(this);
        rv_asset_date_regimes.setLayoutManager(layoutManager2);
        getAssetRegimes(assetId);
    }

    private void initializeOnClick(){
        bt_back.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_back:
                finish();
                break;
        }
    }

    private void getAssetDetailsFromDashboardListing(int asset_id){
        Query query = assetDetailsReference.whereEqualTo("status", "Active").whereEqualTo("id",asset_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            assetId = Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
                            asset_name = queryDocumentSnapshot.getString("asset_name");
                            asset_number = queryDocumentSnapshot.getString("asset_number");
                            assetTypeId = Objects.requireNonNull(queryDocumentSnapshot.getLong("asset_type_id")).intValue();
                            asset_nri = queryDocumentSnapshot.getString("next_routine_inspection_date");
                        }
                        getAssetTypeName(assetTypeId);
                        tv_asset_name.setText(asset_name);
                        tv_asset_id.setText(asset_number);
                        getAssetRegimes(assetId);
                        compareNRIDate(asset_nri);
                    }
                    progressDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AssetDateManagement.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAssetTypeName(int assetTypeId) {
        Query query = assetTypeReference.whereEqualTo("status", "Active").whereEqualTo("id",assetTypeId);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        asset_type = queryDocumentSnapshot.getString("asset_type_name");
                        identification_method = queryDocumentSnapshot.getString("identification_method");
                    }
                    tv_asset_type.setText(asset_type);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetDateManagement.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void  getAssetRegimes(int asset_details_id){
        Query query = regimeDetailsReference.whereEqualTo("status", "Active").whereEqualTo("asset_details_id",asset_details_id)
                .orderBy("time_second_format", Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    arrayList_regime_element_id.clear();
                    arraylist_regime_element_name.clear();
                    arraylist_regime_element_value.clear();
                    arraylist_regime_element_view.clear();
                    arraylist_regime_element_name_sorted.clear();
                    arraylist_regime_element_value_sorted.clear();
                    arrayList_regime_element_id_sorted.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        if (Objects.equals(queryDocumentSnapshot.getString("regime_element_view"), "TextView") &&
                                Objects.requireNonNull(queryDocumentSnapshot.getString("regime_element_value")).contains("-")) {
                            arraylist_regime_element_name.add(queryDocumentSnapshot.getString("regime_element_name"));
                            arrayList_regime_element_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("regime_element_id")).intValue());
                            arraylist_time_second.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("time_second_format")).intValue());
                            arraylist_regime_element_value.add(queryDocumentSnapshot.getString("regime_element_value"));
                            arraylist_regime_element_view.add(queryDocumentSnapshot.getString("regime_element_view"));
                        }
                    }
                    ArrayList<Integer> arrayList_regime_element_id_all = new ArrayList<>();
                    arrayList_regime_element_id_all.addAll(arrayList_regime_element_id);
                    HashSet<Integer> hashSet = new HashSet<Integer>();
                    hashSet.addAll(arrayList_regime_element_id);
                    arrayList_regime_element_id.clear();
                    arrayList_regime_element_id_sorted.addAll(hashSet);
                    for (int i = 0;i<arrayList_regime_element_id_sorted.size();i++){
                        for (int j = 0;j<arrayList_regime_element_id_all.size();j++){
                            if (arrayList_regime_element_id_sorted.get(i).equals(arrayList_regime_element_id_all.get(j))){
                                arraylist_regime_element_name_sorted.add(arraylist_regime_element_name.get(j));
                                arraylist_regime_element_value_sorted.add(arraylist_regime_element_value.get(j));
                                break;
                            }
                        }
                    }
                    assetDateManagementAdapter = new AssetDateManagementAdapter(AssetDateManagement.this,arraylist_regime_element_name_sorted,arraylist_regime_element_value_sorted);
                    rv_asset_date_regimes.setAdapter(assetDateManagementAdapter);
                    Log.d("TJYTF :" , String.valueOf(arrayList_regime_element_id_sorted));
                    adapterClick();
                    for (int i = 0; i < arrayList_regime_element_id_sorted.size(); i++) {
                        compareDates(arraylist_regime_element_value_sorted.get(i));
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetDateManagement.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void adapterClick(){
        assetDateManagementAdapter.setOnItemClickListener(new AssetDateManagementAdapter.OnViewClickListener() {
            @Override
            public void onViewClick(int position) {
                Intent to_date_re_validation = new Intent(AssetDateManagement.this, DateReValidation.class);
                to_date_re_validation.putExtra("asset_type",asset_type);
                to_date_re_validation.putExtra("asset_id",asset_number);
                to_date_re_validation.putExtra("asset_name",asset_name);
                to_date_re_validation.putExtra("regime_id",arrayList_regime_element_id_sorted.get(position));
                to_date_re_validation.putExtra("regime_name",arraylist_regime_element_name_sorted.get(position));
                to_date_re_validation.putExtra("assetId",assetId);
                to_date_re_validation.putExtra("asset_nri_failed",asset_nri_failed);
                to_date_re_validation.putExtra("asset_date_failed",assetsRequiringInspection);
                startActivity(to_date_re_validation);
            }
        });
    }

    private void compareDates(String nte){
        try{
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            Date date1 = formatter.parse(nte);
            Date date2 = formatter.parse(currentDate);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            formatter.format(cal.getTime());
            if (date1.compareTo(date2)<0)
            {
                assetsRequiringInspection.add("True");
            }
            Log.d("DHHG 2 :", String.valueOf(assetsRequiringInspection));
        }catch (ParseException e1){
            e1.printStackTrace();
        }
    }

    private void compareNRIDate(String nte){
        try{
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            Date date1 = formatter.parse(nte);
            Date date2 = formatter.parse(currentDate);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            formatter.format(cal.getTime());
            if (date1.compareTo(date2)<0)
            {
                asset_nri_failed = "Yes";
            }
            else
                asset_nri_failed = "No";
            Log.d("DHHG 3 :",asset_nri_failed);
        }catch (ParseException e1){
            e1.printStackTrace();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        this.recreate();
    }
}