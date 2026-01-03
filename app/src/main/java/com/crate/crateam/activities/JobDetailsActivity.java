package com.crate.crateam.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.crate.crateam.R;
import com.crate.crateam.adapters.AssignTasksAdapter;
import com.crate.crateam.model.AssignTasksList;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class JobDetailsActivity extends AppCompatActivity {
    private String current_date = "";
    private int user_id=0,vehicle_id =0;
    private SessionManager sessionManager;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private RecyclerView recyclerView;
    private ImageView iv_cross;
    private AssignTasksAdapter assignTasksAdapter;
    private ArrayList<Integer> collection_site_ids = new ArrayList<>();
    private ArrayList<Integer> delivery_site_ids = new ArrayList<>();
    private ArrayList<Integer> material_ids = new ArrayList<>();
    private ArrayList<Integer> job_ids = new ArrayList<>();
    private ArrayList<Integer> task_order_nos = new ArrayList<>();
    private ArrayList<AssignTasksList> assignTasksListDataModel = new ArrayList<>();
    private ArrayList<String> collection_add= new ArrayList<>();
    private ArrayList<String> delivery_add= new ArrayList<>();
    private ArrayList<String> material_name= new ArrayList<>();
    private ArrayList<String> contact_name= new ArrayList<>();
    private ArrayList<String> comments= new ArrayList<>();
    private ArrayList<String> task_status= new ArrayList<>();
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference jobManagementReference,materialReference,siteManagementReference,currentVehicleReference,
            taskManagementReference;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_details);
        initView();
    }

    private void initView(){
        FirestoreManager.initPersistentIndexManager();
        currentVehicleReference = db.collection("CR_current_vehicle");
        jobManagementReference = db.collection("CR_job_management_new_details");
        materialReference = db.collection("CR_material");
        siteManagementReference = db.collection("CR_site_management");
        taskManagementReference = db.collection("CR_task_management");
        progressDialog = Dialog.showProgressDialog(this);
        recyclerView = findViewById(R.id.rv_assign_tasks);
        iv_cross = findViewById(R.id.iv_cross);
        pref = this.getSharedPreferences("MyPref", 0); //0 - for private mode
        editor = pref.edit();
        sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_id = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = df.format(c);
        current_date = formattedDate;
        currentVehicleId();
        iv_cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JobDetailsActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }



    private void currentVehicleId(){
        progressDialog.show();
        Query query = currentVehicleReference.whereEqualTo("user_id",user_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                        if (queryDocumentSnapshot.exists()){
                            vehicle_id = queryDocumentSnapshot.getLong("vehicle_id").intValue();
                            Log.d("VEHICLE ID:" , vehicle_id+" "+user_id);
                            sessionManager.updateVehicleId(vehicle_id);
                        }
                    }
                    fetchTaskDetails(user_id,vehicle_id);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.e("Error", "Unable to fetch current vehicle.");
            }
        });
    }

    private void fetchTaskDetails(int user_id,int vehicle_id){
        Query query = jobManagementReference.whereEqualTo("assign_driver_id",user_id).whereEqualTo("vehicle_registration_no_id",vehicle_id)
                .whereEqualTo("status","Active").orderBy("id", Query.Direction.ASCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    collection_site_ids.clear();
                    delivery_site_ids.clear();
                    material_ids.clear();
                    job_ids.clear();
                    task_order_nos.clear();
                    task_status.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        String show_job_date = queryDocumentSnapshot.getString("show_job_date");
                        int time_second_format = Integer.parseInt(AppData.changeDateFormat(show_job_date));
                        if (time_second_format <= AppData.getDateMonthYearFormat()) {
                            collection_site_ids.add(queryDocumentSnapshot.getLong("collection_site_id").intValue());
                            delivery_site_ids.add(queryDocumentSnapshot.getLong("delivery_site_id").intValue());
                            material_ids.add(queryDocumentSnapshot.getLong("material_id").intValue());
                            job_ids.add(queryDocumentSnapshot.getLong("job_id").intValue());
                            task_order_nos.add(queryDocumentSnapshot.getLong("task_order_no").intValue());
                            task_status.add(queryDocumentSnapshot.getString("status"));
                        }
                    }
                    Log.d("Job list :" ,job_ids +" "+collection_site_ids+" "+delivery_site_ids+"  "+material_ids);
                    fetchCollectionPointAddress();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Error", "Unable to fetch job details.");
            }
        });
    }

    private void fetchCollectionPointAddress(){
            Query query = siteManagementReference.whereEqualTo("site_type","Collection");
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()){
                        ArrayList<Integer> all_collection_site_ids = new ArrayList<>();
                        ArrayList<String> all_collection_site_name = new ArrayList<>();
                        ArrayList<String> all_contact_name = new ArrayList<>();
                        ArrayList<String> all_comments = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                            all_collection_site_name.add(queryDocumentSnapshot.getString("site_name"));
                            all_collection_site_ids.add(queryDocumentSnapshot.getLong("id").intValue());
                            all_contact_name.add(queryDocumentSnapshot.getString("contact_name"));
                            all_comments.add(queryDocumentSnapshot.getString("comments"));
                        }
                        for (int i =0; i<collection_site_ids.size();i++) {
                            for (int j=0; j<all_collection_site_ids.size();j++) {
                                if (collection_site_ids.get(i).equals(all_collection_site_ids.get(j))) {
                                    collection_add.add(all_collection_site_name.get(j));
                                    contact_name.add(all_contact_name.get(j));
                                    comments.add(all_comments.get(j));
                                    break;
                                }
                            }
                        }
                        fetchDeliveryPointAddress();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("Error", "Unable to fetch job details.");
                }
            });
    }

    private void fetchDeliveryPointAddress(){
        Query query = siteManagementReference.whereEqualTo("site_type","Delivery");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    ArrayList<Integer> all_delivery_site_ids = new ArrayList<>();
                    ArrayList<String> all_delivery_site_name = new ArrayList<>();
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        all_delivery_site_name.add(queryDocumentSnapshot.getString("site_name"));
                        all_delivery_site_ids.add(queryDocumentSnapshot.getLong("id").intValue());
                    }
                    for (int i =0; i<delivery_site_ids.size();i++) {
                        for (int j=0; j<all_delivery_site_ids.size();j++) {
                            if (delivery_site_ids.get(i).equals(all_delivery_site_ids.get(j))) {
                                delivery_add.add(all_delivery_site_name.get(j));
                                break;
                            }
                        }
                    }
                    fetchMaterialNames();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Error", "Unable to fetch job details.");
            }
        });
    }

    private void fetchMaterialNames(){
        Query query = materialReference.whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    progressDialog.dismiss();
                    ArrayList<Integer> all_material_ids = new ArrayList<>();
                    ArrayList<String> all_material_name = new ArrayList<>();
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        all_material_name.add(queryDocumentSnapshot.getString("material_description"));
                        all_material_ids.add(queryDocumentSnapshot.getLong("id").intValue());
                    }
                    for (int i =0; i<material_ids.size();i++) {
                        for (int j=0; j<all_material_ids.size();j++) {
                            if (material_ids.get(i).equals(all_material_ids.get(j))) {
                                material_name.add(all_material_name.get(j));
                                break;
                            }
                        }
                    }
                    setCompletedTaskAdapter();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.e("Error", "Unable to fetch job details.");
            }
        });
    }

    private void setCompletedTaskAdapter(){
        progressDialog.dismiss();
        assignTasksListDataModel.clear();
            for (int i = 0; i < task_status.size(); i++) {
                AssignTasksList assignTasksList = new AssignTasksList();
                assignTasksList.setStatus(task_status.get(i));
                if (!collection_add.isEmpty())
                    assignTasksList.setCollection_point(collection_add.get(i));
                if (!delivery_add.isEmpty())
                    assignTasksList.setDelivery_point(delivery_add.get(i));
                if (!material_name.isEmpty())
                    assignTasksList.setMaterial_name(material_name.get(i));
                if (!contact_name.isEmpty())
                    assignTasksList.setContact_name(contact_name.get(i));
                if (!comments.isEmpty())
                    assignTasksList.setComments(comments.get(i));
                assignTasksListDataModel.add(assignTasksList);
            }
        Log.d("Task Details:" ,collection_add + "  "+delivery_add+" "+material_name+" "+task_order_nos
                           +" "+contact_name+ " "+comments);
        assignTasksAdapter = new AssignTasksAdapter(this,assignTasksListDataModel);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(assignTasksAdapter);
        adapterClick();
    }

    private void adapterClick(){
        assignTasksAdapter.setOnItemClickListener(new AssignTasksAdapter.OnViewClickListener() {
            @Override
            public void onViewClick(int position) {
                int job_id =job_ids.get(position);
                int task_order_id = task_order_nos.get(position);
                Log.d("Selected Job id:" ,job_id+" "+task_order_id);
                editor.putInt("job_id",job_id);
                editor.putInt("task_order_id",task_order_id);
                editor.commit();
                taskStatus(job_id,task_order_id);
            }
        });
    }

    private void taskStatus(int job_id,int task_order_id){
        Query query = taskManagementReference.whereEqualTo("job_id",job_id).whereEqualTo("task_order_no",task_order_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                String task_status_detail="";
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot :task.getResult()){
                        task_status_detail =  queryDocumentSnapshot.getString("task_status");
                    }
                }
                Log.d("STATUS :", Objects.requireNonNull(task_status_detail));
                assert task_status_detail != null;
                showTask(task_status_detail);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(JobDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showTask(String status){
        Intent intent;
        Log.d("Task status dddd : " ,status);
        if (status.equals("")){
            intent = new Intent(this, StartTasksActivity.class);
            intent.putExtra("startTaskClicked",false);
            startActivity(intent);
        }
        else if (status.equals("StartTask")) {
            intent = new Intent(this, StartTasksActivity.class);
            startActivity(intent);
            editor.putBoolean("startTaskClicked",true);
            editor.commit();
        }
        else if (status.equals("ArrivedCollectionPoint")){
            intent = new Intent(this, ArrivedCollectionPoint.class);
            startActivity(intent);
            editor.putBoolean("arrivedCollectionClicked",true);
            editor.commit();
        }
        else if (status.equals("LeftCollectionPoint")){
            intent = new Intent(this, LeftCollectionPoint.class);
            startActivity(intent);
            editor.putBoolean("leftCollectionClicked",true);
            editor.commit();
        }
        else if (status.equals("ArrivedDeliveryPoint")){
            intent = new Intent(this, ArrivedDeliveryPoint.class);
            startActivity(intent);
            editor.putBoolean("arrivedDeliveryClicked",true);
            editor.commit();
        }
    }
}