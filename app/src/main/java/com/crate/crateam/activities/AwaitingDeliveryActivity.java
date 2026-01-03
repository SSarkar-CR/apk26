package com.crate.crateam.activities;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.crate.crateam.R;
import com.crate.crateam.adapters.AssignTasksAdapter;
import com.crate.crateam.adapters.AwaitingDeliveryAdapter;
import com.crate.crateam.adapters.TaskCompletedAdapter;
import com.crate.crateam.fragments.ViewInspectionFragment;
import com.crate.crateam.model.TaskCompletedList;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.CustomSearchableSpinner;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class AwaitingDeliveryActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private TextView tv_user;
    private String dropdownClicked="" ,user_name="";
    private Integer registration_id=0,trailer_id=0,user_id=0;
    private RecyclerView recyclerView;
    private ImageView iv_cross;
    private SharedPreferences.Editor editor;
    private CustomSearchableSpinner sp_reg_no,sp_trailer_no;
    private AwaitingDeliveryAdapter awaitingDeliveryAdapter;
    private ArrayList<String> reg_no_list = new ArrayList<>();
    private ArrayList<Integer> reg_id_list = new ArrayList<>();
    private ArrayList<String> trailer_no_list = new ArrayList<>();
    private ArrayList<Integer> trailer_id_list = new ArrayList<>();
    private ProgressDialog progressDialog;
    private CollectionReference supplyWasteFormReference,siteManagementReference,materialReference,
            vehicleDetailsReference,trailerDetailsReference;
    private ArrayList<TaskCompletedList> taskAwaitingDeliveryDataModel = new ArrayList<>();
    private ArrayList<Integer> arrayList_collection_site_id= new ArrayList<>();
    private ArrayList<Integer> arrayList_delivery_site_id= new ArrayList<>();
    private ArrayList<Integer> arrayList_material_id = new ArrayList<>();
    private ArrayList<String>  arrayList_ticket_no = new ArrayList<>();
    private ArrayList<Integer> arrayList_all_site_id= new ArrayList<>();
    private ArrayList<String> arrayList_all_site_address = new ArrayList<>();
    private ArrayList<String> arrayList_collection_address = new ArrayList<>();
    private ArrayList<String> arrayList_delivery_address = new ArrayList<>();
    private ArrayList<Integer> arrayList_all_material_id= new ArrayList<>();
    private ArrayList<String> arrayList_all_material_name = new ArrayList<>();
    private ArrayList<String> arrayList_material_name = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.awaiting_delivery_layout);
        initView();
    }

    private void initView(){
        FirestoreManager.initPersistentIndexManager();
        progressDialog = Dialog.showProgressDialog(this);
        SharedPreferences preferences = this.getSharedPreferences("Preference", 0);
        editor = preferences.edit();
        SessionManager sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_name = user.get(SessionManager.KEY_FULL_NAME);
        user_id = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        supplyWasteFormReference = db.collection("CR_supply_waste_forms");
        siteManagementReference = db.collection("CR_site_management");
        materialReference = db.collection("CR_material");
        vehicleDetailsReference = db.collection("CR_vehicle_details");
        trailerDetailsReference = db.collection("CR_trailer_details");
        iv_cross = findViewById(R.id.iv_cross);
        sp_reg_no = findViewById(R.id.sp_reg_no);
        sp_trailer_no = findViewById(R.id.sp_trailer_no);
        recyclerView = findViewById(R.id.rv_awaiting_delivery);
        tv_user =findViewById(R.id.tv_user);
        tv_user.setText(user_name);
        initializeOnClick();
        fetchVehicleNumber();
        fetchTrailerNumber();
    }



    private void initializeOnClick() {
        iv_cross.setOnClickListener(this);
        sp_reg_no.setOnItemSelectedListener(this);
        sp_trailer_no.setOnItemSelectedListener(this);
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_cross:
                finish();
                break;
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_reg_no:
                registration_id = reg_id_list.get(position);
                dropdownClicked = "Vehicle";
                fetchOpenTickets();
                break;
            case R.id.sp_trailer_no:
                trailer_id = trailer_id_list.get(position);
                dropdownClicked = "Trailer";
                fetchOpenTickets();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void fetchVehicleNumber() {
        progressDialog.show();
        Query query = vehicleDetailsReference.whereEqualTo("status", "Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        if (documentSnapshot.exists()) {
                            reg_no_list.add(documentSnapshot.getString("registration_no"));
                            reg_id_list.add(Objects.requireNonNull(documentSnapshot.getLong("id")).intValue());
                        }
                    }
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_custom_layout, reg_no_list);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                    sp_reg_no.setAdapter(spinnerArrayAdapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AwaitingDeliveryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTrailerNumber(){
        Query query = trailerDetailsReference.whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                        if (queryDocumentSnapshot.exists()){
                            trailer_id_list.add(queryDocumentSnapshot.getLong("id").intValue());
                            trailer_no_list.add(queryDocumentSnapshot.getString("model_no"));
                        }
                    }
                }
                ArrayAdapter<String> spinnerArrayAdapter7 = new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_custom_layout,trailer_no_list);
                spinnerArrayAdapter7.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sp_trailer_no.setAdapter(spinnerArrayAdapter7);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.e("Error", "Unable to fetch trailer details.");
            }
        });
    }

    private void fetchOpenTickets(){
        progressDialog.show();
        Query query = null;
        if (dropdownClicked.equals("Vehicle"))
        query = supplyWasteFormReference.whereEqualTo("vehicle_id", registration_id).whereEqualTo("job_id",0)
                .whereEqualTo("ticket_status","Open").whereEqualTo("ticket_type","Collection")
                .orderBy("time_second_format", Query.Direction.DESCENDING);
        else if (dropdownClicked.equals("Trailer"))
            query = supplyWasteFormReference.whereEqualTo("trailer_id", trailer_id).whereEqualTo("job_id",0)
                    .whereEqualTo("ticket_status","Open").whereEqualTo("ticket_type","Collection")
                    .orderBy("time_second_format", Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    progressDialog.dismiss();
                    if (!task.getResult().isEmpty()) {
                        arrayList_ticket_no.clear();
                        arrayList_collection_site_id.clear();
                        arrayList_delivery_site_id.clear();
                        arrayList_material_id.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            arrayList_ticket_no.add(queryDocumentSnapshot.getString("ticket_no"));
                            if (Objects.requireNonNull(queryDocumentSnapshot.getLong("delivery_site_id_new")).intValue() == 0)
                                arrayList_collection_site_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("current_site_id")).intValue());
                            else
                                arrayList_collection_site_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("current_site_id_new")).intValue());
                            if (Objects.requireNonNull(queryDocumentSnapshot.getLong("delivery_site_id_new")).intValue() == 0)
                                arrayList_delivery_site_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("delivery_site_id")).intValue());
                            else
                                arrayList_delivery_site_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("delivery_site_id_new")).intValue());
                            if (Objects.requireNonNull(queryDocumentSnapshot.getLong("material_id_new")).intValue() == 0)
                                arrayList_material_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("material_id")).intValue());
                            else
                                arrayList_material_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("material_id_new")).intValue());
                        }
                        getSiteAddress();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AwaitingDeliveryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getSiteAddress(){
        Query query = siteManagementReference.whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        arrayList_delivery_address.clear();
                        arrayList_collection_address.clear();
                        arrayList_all_site_id.clear();
                        arrayList_all_site_address.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            arrayList_all_site_id.add(queryDocumentSnapshot.getLong("id").intValue());
                            arrayList_all_site_address.add(queryDocumentSnapshot.getString("address"));
                        }
                        for (int i =0; i<arrayList_collection_site_id.size();i++){
                            for (int j=0;j<arrayList_all_site_id.size();j++){
                                if (arrayList_collection_site_id.get(i).equals(arrayList_all_site_id.get(j))){
                                    arrayList_collection_address.add(arrayList_all_site_address.get(j));
                                    break;
                                }
                            }
                        }
                        for (int i =0; i<arrayList_delivery_site_id.size();i++){
                            for (int j=0;j<arrayList_all_site_id.size();j++){
                                if (arrayList_delivery_site_id.get(i).equals(arrayList_all_site_id.get(j))){
                                    arrayList_delivery_address.add(arrayList_all_site_address.get(j));
                                    break;
                                }
                            }
                        }
                        getMaterialName();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AwaitingDeliveryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMaterialName(){
        Query query = materialReference.whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    arrayList_material_name.clear();
                    arrayList_all_material_id.clear();
                    arrayList_all_material_name.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot  : task.getResult()){
                        arrayList_all_material_id.add(Integer.parseInt(queryDocumentSnapshot.getId()));
                        arrayList_all_material_name.add(queryDocumentSnapshot.getString("material_description"));
                    }
                    for (int i=0;i<arrayList_material_id.size();i++){
                        for (int j=0;j<arrayList_all_material_id.size();j++){
                            if (arrayList_material_id.get(i).equals(arrayList_all_material_id.get(j))){
                                arrayList_material_name.add(arrayList_all_material_name.get(j));
                                break;
                            }
                        }
                    }
                    setAwaitingDeliveryAdapter();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AwaitingDeliveryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setAwaitingDeliveryAdapter(){
        progressDialog.dismiss();
        taskAwaitingDeliveryDataModel.clear();
        for (int i=0;i<arrayList_ticket_no.size();i++){
            TaskCompletedList taskCompletedList = new TaskCompletedList();
            taskCompletedList.setTicket_no(arrayList_ticket_no.get(i));
            taskCompletedList.setCollection_point(arrayList_collection_address.get(i));
            taskCompletedList.setDelivery_point(arrayList_delivery_address.get(i));
            taskCompletedList.setMaterial_name(arrayList_material_name.get(i));
            taskAwaitingDeliveryDataModel.add(taskCompletedList);
        }
        awaitingDeliveryAdapter = new AwaitingDeliveryAdapter(this,taskAwaitingDeliveryDataModel);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(awaitingDeliveryAdapter);
        adapterClick();
    }

    private void adapterClick(){
        awaitingDeliveryAdapter.setOnItemClickListener(new AwaitingDeliveryAdapter.OnViewClickListener() {
            @Override
            public void onViewClick(int position) {
                String ticket_no = arrayList_ticket_no.get(position);
                Log.d("Ticket_no:" ,ticket_no);
                Intent intent = new Intent(AwaitingDeliveryActivity.this,WasteTransferDelivery.class);
                intent.putExtra("ticket_no",ticket_no);
                startActivity(intent);
                finish();
            }
        });
    }

}