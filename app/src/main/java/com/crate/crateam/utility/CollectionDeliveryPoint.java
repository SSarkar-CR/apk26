package com.crate.crateam.utility;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.crate.crateam.R;
import com.crate.crateam.interfaces.StartTask;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheIndexManager;
import com.crate.crateam.utility.FirestoreManager;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CollectionDeliveryPoint{
    public CollectionReference collectionReferenceJobManagementDetailsNew,collectionReferenceMaterial,
            vehicleDetailsReference,collectionReferenceAssignMaterial,siteManagementReference;
    public FirebaseFirestore dbTask;
    public ProgressDialog progressDialog;
    private SharedPreferences pref;
    public int user_id=0;
    public int job_id=0;
    public int task_order_id=0;
    public int material_id=0;
    public String material_description="";
    public String ewc_code ="";
    public String material_value ="";
    public String material_type="";
    public int  vehicle_number=0;
    public List<Integer> stringList;
    public int collection_site_id=0;
    public String  site_type="";
    public int delivery_site_id=0;
    public String note_type="";
    public  String collection_point="",collection_site_name="";
    public String delivery_point="";
    public String latitude="";
    public String longitude="";
    public String sic_code="";
    public String project_no="";
    public String carrier_no="";
    public String permit_no_collection="";
    public String permit_no_delivery="";
    public Context context;
    public static StartTask startTask =null;
    public String status="";
    public TextView tv_vehicle_no1;
    public String vehicle_registration_number = "";

    public CollectionDeliveryPoint(Context context,int user_id,int vehicle_number){
        this.context = context;
        this.user_id = user_id;
        this.vehicle_number = vehicle_number;
        dbTask = FirebaseFirestore.getInstance();
        collectionReferenceJobManagementDetailsNew = dbTask.collection("CR_job_management_new_details");
        collectionReferenceMaterial = dbTask.collection("CR_material");
        vehicleDetailsReference = dbTask.collection("CR_vehicle_details");
        collectionReferenceAssignMaterial = dbTask.collection("CR_assign_material");
        siteManagementReference = dbTask.collection("CR_site_management");
        progressDialog = com.crate.crateam.utility.Dialog.showProgressDialog(context);
        stringList = new ArrayList<>();
    }

    public void jobManagement(){
        pref = context.getSharedPreferences("MyPref", 0);
        job_id = pref.getInt("job_id",0);
        task_order_id = pref.getInt("task_order_id",0);
        Log.d("gvvjhvj :" ,job_id+" "+task_order_id);
        Query queryJobManagementDetails = collectionReferenceJobManagementDetailsNew.whereEqualTo("job_id",job_id).whereEqualTo("task_order_no",task_order_id)
                .whereEqualTo("status","Active");
        queryJobManagementDetails.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (queryDocumentSnapshot.exists()) {
                                collection_site_id = queryDocumentSnapshot.getLong("collection_site_id").intValue();
                                delivery_site_id = queryDocumentSnapshot.getLong("delivery_site_id").intValue();
                                note_type = queryDocumentSnapshot.getString("note_type");
                                material_id = Objects.requireNonNull(queryDocumentSnapshot.getLong("material_id")).intValue();
                                Log.d("note_type :" , collection_site_id + " " + delivery_site_id + note_type + material_id);
                                getMaterialData(material_id);
                            }
                        }
                    } else {
                        progressDialog.dismiss();
                        com.crate.crateam.utility.Dialog.alertDialogToDashBoard(context, "Unable to get task details.");
                    }
                }else {
                    progressDialog.dismiss();
                    com.crate.crateam.utility.Dialog.alertDialogToDashBoard(context,"Unable to get task details.");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getMaterialData(int material_id){
        collectionReferenceMaterial.document(String.valueOf(material_id)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        DocumentSnapshot document = task.getResult();
                        material_description = document.getString("material_description");
                        ewc_code = document.getString("ewc_code");
                        material_value = document.getString("material_value");
                        material_type = document.getString("material_type");
                        Log.d("DESCRIPTION :" , Objects.requireNonNull(material_type));
                        addressManagement();
                    }
                    else {
                        progressDialog.dismiss();
                        Dialog.alertDialogToDashBoard(context,"Unable to get task details.");
                    }
                }
                else {
                    progressDialog.dismiss();
                    Dialog.alertDialogToDashBoard(context,"Unable to get task details.");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void addressManagement(){
        Query queryAddressManagement = siteManagementReference.whereEqualTo("id",collection_site_id).whereEqualTo("status","Active");
        queryAddressManagement.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            collection_point = queryDocumentSnapshot.getString("address");
                            sic_code = queryDocumentSnapshot.getString("sic_code");
                            carrier_no = queryDocumentSnapshot.getString("carrier_no");
                            permit_no_collection = queryDocumentSnapshot.getString("permit_no");
                            int site_id = Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
                            addressManagementDelivery();
                            Log.d("SIC CODE :" , sic_code+" "+site_id);
                            getCollectionSiteName(site_id);
                        }
                    }
                    else {
                        progressDialog.dismiss();
                        com.crate.crateam.utility.Dialog.alertDialogToDashBoard(context,"Unable to get task details.");
                    }
                }
                else {
                    progressDialog.dismiss();
                    com.crate.crateam.utility.Dialog.alertDialogToDashBoard(context,"Unable to get task details.");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addressManagementDelivery(){
        Query queryAddressManagement = siteManagementReference.whereEqualTo("id",delivery_site_id).whereEqualTo("status","Active");
        queryAddressManagement.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            delivery_point = queryDocumentSnapshot.getString("address");
                            permit_no_delivery = queryDocumentSnapshot.getString("permit_no");
//                            delivery_site_id = Objects.requireNonNull(queryDocumentSnapshot.getLong("site_id")).intValue();
                            // interface calling
                            assignMaterial();
                            Log.d("ADDRESS :" ,delivery_point + permit_no_delivery);
                        }
                    }
                    else {
                        progressDialog.dismiss();
                        com.crate.crateam.utility.Dialog.alertDialogToDashBoard(context,"Unable to get task details.");
                    }
                }
                else {
                    progressDialog.dismiss();
                    com.crate.crateam.utility.Dialog.alertDialogToDashBoard(context,"Unable to get task details.");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // fetching vehicle registration number from CR_vehicle_details table
    public void fetchVehicleRegistrationNumber(TextView tv_vehicle_registration_number){
        this.tv_vehicle_no1 = tv_vehicle_registration_number;
        progressDialog.show();
        Query query=vehicleDetailsReference.whereEqualTo("external_vehicle","No").whereEqualTo("id",vehicle_number);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        if (documentSnapshot.exists()) {
                            vehicle_registration_number = documentSnapshot.getString("registration_no");
                            tv_vehicle_no1.setText(vehicle_registration_number);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
        public void assignMaterial(){
            Query query = collectionReferenceAssignMaterial.whereEqualTo("site_id",collection_site_id).whereEqualTo("status","Active");
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    progressDialog.dismiss();
                    if (task.isSuccessful()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if (queryDocumentSnapshot.exists()){
                                int site_id = Objects.requireNonNull(queryDocumentSnapshot.getLong("site_id")).intValue();
                                    site_type = queryDocumentSnapshot.getString("site_type");
                                Log.d("SITE TYPE :" , Objects.requireNonNull(site_type));
                                startTask.getDetails(collection_site_id,collection_site_name,collection_point,delivery_point,delivery_site_id,job_id,task_order_id,material_id,material_description,
                                        ewc_code,material_value,note_type,sic_code, carrier_no,permit_no_collection,permit_no_delivery,site_type,material_type,project_no);
                            }
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    private void getCollectionSiteName(int site_id){
        Query query = siteManagementReference.whereEqualTo("id",site_id).whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()) {
                        collection_site_name = queryDocumentSnapshot.getString("site_name");
                        if (queryDocumentSnapshot.getString("project_no")!= null)
                            project_no = queryDocumentSnapshot.getString("project_no");
                        else
                            project_no ="";
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    
    public void enableOfflineSupportCommon(){
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        dbTask.setFirestoreSettings(settings);
    }
}
