package com.crate.crateam.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.crate.crateam.R;
import com.crate.crateam.adapters.AssetDateManagementAdapter;
import com.crate.crateam.adapters.AssetViewCertificatesAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheIndexManager;
import com.crate.crateam.utility.FirestoreManager;
import com.crate.crateam.utility.FirestoreManager;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class AssetCertificates extends AppCompatActivity implements View.OnClickListener{
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference assetInspectionElementsReference,assetDocumentReference;
    private AssetViewCertificatesAdapter assetViewCertificatesAdapter;
    private Button bt_return_to_inspection,bt_back;
    private RecyclerView rv_asset_regimes;
    private TextView tv_asset_type,tv_asset_id,tv_asset_name;
    private String asset_type="",asset_number="",asset_name="";
    private int assetId=0;
    private ArrayList<Integer> inspectionElementId  = new ArrayList<>();
    private ArrayList<String> inspectionElementName = new ArrayList<>();
    private ArrayList<String> arraylist_document_name = new ArrayList<>();

    protected static final String TAG = "AssetCertificate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.asset_certificates);
        initView();
        initializeOnClick();
    }
    private void initView(){
        FirestoreManager.initPersistentIndexManager();
        assetDocumentReference = db.collection("AM_documents");
        assetInspectionElementsReference = db.collection("AM_inspection_elements");
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            asset_type= extras.getString("asset_type");
            asset_number= extras.getString("asset_id");
            asset_name= extras.getString("asset_name");
            assetId = extras.getInt("assetId");
        }
        bt_back = findViewById(R.id.bt_back);
        bt_return_to_inspection = findViewById(R.id.bt_return_to_inspection);
        tv_asset_type = findViewById(R.id.tv_asset_type);
        tv_asset_id = findViewById(R.id.tv_asset_id);
        tv_asset_name = findViewById(R.id.tv_asset_name);
        tv_asset_type.setText(asset_type);
        tv_asset_id.setText(asset_number);
        tv_asset_name.setText(asset_name);
        rv_asset_regimes = findViewById(R.id.rv_asset_regimes);
        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(this);
        rv_asset_regimes.setLayoutManager(layoutManager2);
        getAssetDocument(assetId);
    }

    private void initializeOnClick(){
        bt_back.setOnClickListener(this);
        bt_return_to_inspection.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_back:
            case R.id.bt_return_to_inspection:
                finish();
                break;
        }
    }

    private void getAssetDocument(int assetId){
        Query query = assetDocumentReference.whereEqualTo("status","Active").whereEqualTo("asset_id",assetId);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        inspectionElementId.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("classification")).intValue());
                        arraylist_document_name.add(queryDocumentSnapshot.getString("document_name"));
                    }
                }
                Log.d("FHFHG :" , String.valueOf(inspectionElementId));
                for (int i=0; i< inspectionElementId.size(); i++){
                    fetchInspectionElementName(inspectionElementId.get(i));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetCertificates.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void fetchInspectionElementName(int inspection_element_id){
        Query query = assetInspectionElementsReference.whereEqualTo("status","Active").whereEqualTo("id",inspection_element_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        inspectionElementName.add(queryDocumentSnapshot.getString("inspection_element_name"));
                    }
                }
                assetViewCertificatesAdapter = new AssetViewCertificatesAdapter(AssetCertificates.this,inspectionElementName);
                rv_asset_regimes.setAdapter(assetViewCertificatesAdapter);
                Log.d(TAG,"element name:"+inspectionElementName);
                adapterClick();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetCertificates.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void adapterClick(){
        assetViewCertificatesAdapter.setOnItemClickListener(new AssetViewCertificatesAdapter.OnViewClickListener() {
            @Override
            public void onViewClick(int position) {
                Intent to_view_certificates = new Intent(AssetCertificates.this, ViewCertificates.class);
                to_view_certificates.putExtra("document_name",arraylist_document_name.get(position));
                startActivity(to_view_certificates);
            }
        });
    }

}