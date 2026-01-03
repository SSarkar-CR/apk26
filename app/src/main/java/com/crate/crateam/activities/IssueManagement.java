package com.crate.crateam.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.crate.crateam.adapters.IssueManagementAdapter;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.SessionManager;
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
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class IssueManagement extends AppCompatActivity implements View.OnClickListener {

    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference assetDetailsReference,assetTypeReference,assetInspectionSubmissionReference,siteLocationReference;
    private TextView tv_asset_type,tv_asset_id,tv_asset_name;
    private Button bt_back;
    private ImageView iv_refresh;
    private IssueManagementAdapter issueManagementAdapter;
    private RecyclerView rv_inspection_list;
    private int assetId=0,userId=0,regimeId=0,assetTypeId=0;
    private String asset_type="",asset_number="",asset_name="",regime_name="",activity="";
    private ArrayList<String> inspectionIds = new ArrayList<>();
    private ArrayList<String> inspectionDate = new ArrayList<>();
    private ArrayList<String> inspectionRaisedBy = new ArrayList<>();
    private ArrayList<Integer> assetLocationId = new ArrayList<>();
    private ArrayList<String> assetLocationName = new ArrayList<>();
    private ArrayList<String> assetStatus = new ArrayList<>();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.issue_management);
        initView();
        initializeOnClick();
    }
    private void initView(){
        progressDialog = Dialog.showProgressDialog(this);
        FirestoreManager.initPersistentIndexManager();
        assetDetailsReference = db.collection("AM_asset_details");
        assetTypeReference = db.collection("AM_asset_type");
        assetInspectionSubmissionReference = db.collection("AM_asset_inspection_submission");
        siteLocationReference = db.collection("AM_site_location");
        SessionManager sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        userId = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            asset_type= extras.getString("asset_type");
            asset_number= extras.getString("asset_id");
            asset_name= extras.getString("asset_name");
            regime_name = extras.getString("regime_name");
            assetId = extras.getInt("assetId");
            regimeId = extras.getInt("regime");
            activity = extras.getString("activity");
        }
        if (activity!=null) {
            if (activity.equals("DashboardListing")) {
                progressDialog.show();
                getAssetDetailsFromDashboardListing(assetId);
            }
        }
        Log.d("FUKYF :" ,regimeId+" "+regime_name);
        tv_asset_type = findViewById(R.id.tv_asset_type);
        tv_asset_id = findViewById(R.id.tv_asset_id);
        tv_asset_name = findViewById(R.id.tv_asset_name);
        tv_asset_type.setText(asset_type);
        tv_asset_id.setText(asset_number);
        tv_asset_name.setText(asset_name);
        iv_refresh = findViewById(R.id.iv_refresh);
        bt_back = findViewById(R.id.bt_back);
        rv_inspection_list = findViewById(R.id.rv_inspection_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rv_inspection_list.setLayoutManager(layoutManager);
        progressDialog.show();
        getAssetDetailsFromAssetInspection(assetId);
    }
    private void initializeOnClick(){
        iv_refresh.setOnClickListener(this);
        bt_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_refresh:
                progressDialog.show();
                Log.d("hgj :" ,"I am here");
                getAssetDetailsFromAssetInspection(assetId);
                break;
            case R.id.bt_back:
                finish();
                break;
        }
    }

    private void getAssetDetailsFromDashboardListing(int asset_id){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = assetDetailsReference.whereEqualTo("status", "Active").whereEqualTo("id",asset_id);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            assetId = Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
                            asset_name = queryDocumentSnapshot.getString("asset_name");
                            asset_number = queryDocumentSnapshot.getString("asset_number");
                            regimeId = Objects.requireNonNull(queryDocumentSnapshot.getLong("regime_id")).intValue();
                            assetTypeId = Objects.requireNonNull(queryDocumentSnapshot.getLong("asset_type_id")).intValue();
                        }
                        getAssetTypeName(assetTypeId);
                        tv_asset_name.setText(asset_name);
                        tv_asset_id.setText(asset_number);
                        getAssetDetailsFromAssetInspection(assetId);
                    }
                    progressDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(IssueManagement.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAssetTypeName(int assetTypeId) {
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = assetTypeReference.whereEqualTo("status", "Active").whereEqualTo("id",assetTypeId);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        asset_type = queryDocumentSnapshot.getString("asset_type_name");
                    }
                    tv_asset_type.setText(asset_type);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(IssueManagement.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAssetDetailsFromAssetInspection(int assetId){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = assetInspectionSubmissionReference.whereEqualTo("status", "Active").whereEqualTo("asset_id",assetId)
                .whereEqualTo("defected","Yes").whereEqualTo("user_role","Other").
                        orderBy("time_second_format",Query.Direction.DESCENDING);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    inspectionIds.clear();
                    inspectionDate.clear();
                    inspectionRaisedBy.clear();
                    assetLocationId.clear();
                    assetStatus.clear();
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            inspectionIds.add(queryDocumentSnapshot.getString("inspection_id"));
                            inspectionDate.add(queryDocumentSnapshot.getString("conducted_on"));
                            if (Objects.requireNonNull(queryDocumentSnapshot.getString("inspector_name")).equals(""))
                                inspectionRaisedBy.add(queryDocumentSnapshot.getString("user_name"));
                            else
                                inspectionRaisedBy.add(queryDocumentSnapshot.getString("inspector_name"));
                            assetLocationId.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("assign_location")).intValue());
                            assetStatus.add(queryDocumentSnapshot.getString("asset_status"));
                        }
                        for (int i = 0; i < assetLocationId.size(); i++) {
                            getLastInspectionLocationName(assetLocationId.get(i));
                        }
                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("SITE 1:" , String.valueOf(inspectionRaisedBy));
                                issueManagementAdapter = new IssueManagementAdapter(IssueManagement.this, inspectionIds, inspectionDate,
                                        inspectionRaisedBy, assetLocationName, assetStatus);
                                rv_inspection_list.setAdapter(issueManagementAdapter);
                                adapterClick();
                            }
                        }, 1000);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(IssueManagement.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLastInspectionLocationName(int site_id){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = siteLocationReference.whereEqualTo("id", site_id);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            assetLocationName.add(queryDocumentSnapshot.getString("site_name"));
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(IssueManagement.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void adapterClick(){
        issueManagementAdapter.setOnItemClickListener(new IssueManagementAdapter.OnViewClickListener() {
            @Override
            public void onViewClick(int position) {
                Intent to_date_re_validation = new Intent(IssueManagement.this, DefectCloseOut.class);
                to_date_re_validation.putExtra("asset_type",asset_type);
                to_date_re_validation.putExtra("asset_id",asset_number);
                to_date_re_validation.putExtra("asset_name",asset_name);
                to_date_re_validation.putExtra("inspection_id",inspectionIds.get(position));
                to_date_re_validation.putExtra("conducted_on",inspectionDate.get(position));
                to_date_re_validation.putExtra("user_name",inspectionRaisedBy.get(position));
                to_date_re_validation.putExtra("assetId",assetId);
                to_date_re_validation.putExtra("regime",regimeId);
                to_date_re_validation.putExtra("activity","IssueManagement");
                startActivity(to_date_re_validation);
                finish();
            }
        });
    }
}