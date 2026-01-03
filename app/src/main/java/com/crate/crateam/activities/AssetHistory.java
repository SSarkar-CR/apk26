package com.crate.crateam.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class AssetHistory extends AppCompatActivity implements View.OnClickListener {

    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference assetInspectionSubmissionReference,siteLocationReference;
    private TextView tv_asset_type,tv_asset_id,tv_asset_name;
    private Button bt_back;
    private IssueManagementAdapter issueManagementAdapter;
    private RecyclerView rv_inspection_list;
    private int assetId=0,userId=0,regime_id=0;
    private String asset_type="",asset_number="",asset_name="",regime_name="";
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
        setContentView(R.layout.asset_history);
        initView();
        initializeOnClick();
    }
    private void initView(){
        progressDialog = Dialog.showProgressDialog(this);
        FirestoreManager.initPersistentIndexManager();
        assetInspectionSubmissionReference = db.collection("AM_asset_inspection_submission");
        siteLocationReference = db.collection("AM_site_location");
        SessionManager sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        userId = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        Log.d("USER_ID: " , String.valueOf(userId));
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            asset_type= extras.getString("asset_type");
            asset_number= extras.getString("asset_id");
            asset_name= extras.getString("asset_name");
            regime_id = extras.getInt("regime_id");
            regime_name = extras.getString("regime_name");
            assetId = extras.getInt("assetId");
        }
        Log.d("FUKYF :" ,regime_id+" "+regime_name);
        tv_asset_type = findViewById(R.id.tv_asset_type);
        tv_asset_id = findViewById(R.id.tv_asset_id);
        tv_asset_name = findViewById(R.id.tv_asset_name);
        tv_asset_type.setText(asset_type);
        tv_asset_id.setText(asset_number);
        tv_asset_name.setText(asset_name);
        bt_back = findViewById(R.id.bt_back);
        rv_inspection_list = findViewById(R.id.rv_inspection_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rv_inspection_list.setLayoutManager(layoutManager);
        progressDialog.show();
        getAssetDetailsFromAssetInspection(assetId);
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

    @Override
    protected void onRestart() {
        super.onRestart();
        this.recreate();
    }

    private void getAssetDetailsFromAssetInspection(int assetId){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = assetInspectionSubmissionReference.whereEqualTo("status", "Active")
                .whereEqualTo("user_role","Other").whereEqualTo("asset_id",assetId)
                .whereEqualTo("defect_inspection",
                        "Yes")
                .orderBy("time_second_format",Query.Direction.DESCENDING);

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
                        for (int i=0;i<assetLocationId.size();i++){
                            getLastInspectionLocationName(assetLocationId.get(i));
                        }
                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("SITE 1:" , String.valueOf(assetLocationName));
                                issueManagementAdapter = new IssueManagementAdapter(AssetHistory.this,inspectionIds,inspectionDate,
                                        inspectionRaisedBy,assetLocationName,assetStatus);
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
                Toast.makeText(AssetHistory.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(AssetHistory.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void adapterClick(){
        issueManagementAdapter.setOnItemClickListener(new IssueManagementAdapter.OnViewClickListener() {
            @Override
            public void onViewClick(int position) {
                Intent to_view_asset_history = new Intent(AssetHistory.this, ViewAssetHistory.class);
                to_view_asset_history.putExtra("asset_type",asset_type);
                to_view_asset_history.putExtra("asset_id",asset_number);
                to_view_asset_history.putExtra("asset_name",asset_name);
                to_view_asset_history.putExtra("inspection_id",inspectionIds.get(position));
                to_view_asset_history.putExtra("conducted_on",inspectionDate.get(position));
                to_view_asset_history.putExtra("user_name",inspectionRaisedBy.get(position));
                to_view_asset_history.putExtra("assetId",assetId);
                startActivity(to_view_asset_history);
            }
        });
    }
}