package com.crate.crateam.utility;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.crate.crateam.R;
import com.crate.crateam.activities.MainActivity;
import com.crate.crateam.interfaces.ChangeVehicleOption;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChangeVehicle {
    public FirebaseFirestore db;
    public CollectionReference vehicleDetailsReference,refInspectionSubmission,refJobManagementDetailsNew,currentVehicleReference;
    public Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    public int user_id =0, store_job_id=0, vehicle_registration_no_id=0;
    private ArrayList<Integer> arraylist_vehicle_id;
    private ArrayList<Integer> arraylist_reg_no_id;
    private ArrayList<String> arrayliist_reg_no;
    private PopupWindow popupWindow;
    public static ChangeVehicleOption changeVehicleOption = null;
    private ProgressDialog progressDialog;
    private Intent intent;
    public ChangeVehicle(Context context,int user_id,int job_id){
        this.context = context;
        store_job_id = job_id;
        db = FirebaseFirestore.getInstance();
        vehicleDetailsReference = db.collection("CR_vehicle_details");
        refInspectionSubmission = db.collection("CR_inspection_submission");
        refJobManagementDetailsNew = db.collection("CR_job_management_new_details");
        currentVehicleReference = db.collection("CR_current_vehicle");
        FirestoreManager.initPersistentIndexManager();
        this.user_id = user_id;
        arraylist_vehicle_id = new ArrayList<>();
        arraylist_reg_no_id = new ArrayList<>();
        arrayliist_reg_no = new ArrayList<>();
        progressDialog = Dialog.showProgressDialog(context);
    }

    public void vehicleUserMapping(){
        Query query = vehicleDetailsReference.whereEqualTo("external_vehicle","No").whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    String userIds = "";
                    arraylist_vehicle_id.clear();
                    ArrayList<Integer> arrayList_user_id = new ArrayList<>();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        userIds = queryDocumentSnapshot.getString("vehicle_user_mappings");
                        if (userIds != null && userIds.contains(",")) {
                            ArrayList<String> userIdListString = new ArrayList<String>(Arrays.asList(userIds.split(",")));
                            for (int i = 0; i < userIdListString.size(); i++) {
                                arrayList_user_id.add(Integer.valueOf(userIdListString.get(i)));
                                if (Integer.valueOf(userIdListString.get(i)) == user_id) {
                                    arraylist_vehicle_id.add(queryDocumentSnapshot.getLong("id").intValue());
                                }
                            }
                        } else {
                            if (userIds != null) {
                                if (!userIds.equals("")) {
                                    arrayList_user_id.add(Integer.valueOf(userIds));
                                    if (Integer.valueOf(userIds) == user_id) {
                                        arraylist_vehicle_id.add(queryDocumentSnapshot.getLong("id").intValue());
                                    }
                                }
                            }
                        }
                    }
                    Log.d("Vehicle id list :" , String.valueOf(arraylist_vehicle_id));
                    vehicleDetails();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void vehicleDetails(){
        final Query query = vehicleDetailsReference.whereEqualTo("external_vehicle","No").whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        int id = queryDocumentSnapshot.getLong("id").intValue();
                        for (int i=0;i<arraylist_vehicle_id.size();i++){
                            if (arraylist_vehicle_id.get(i) == id){
                                arraylist_reg_no_id.add(id);
                                arrayliist_reg_no.add(queryDocumentSnapshot.getString("registration_no"));
                            }
                        }
                    }
                    getRunningVehicle(store_job_id);
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

    public void inspectionSubmission(final int reg_no_id, final  AlertDialog alertDialog){
        progressDialog.show();
        // get current date
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String current_date = df.format(c);
        Query query =refInspectionSubmission.whereEqualTo("logged_by",user_id).whereEqualTo("vehicle_id",reg_no_id).whereEqualTo("conducted_on",current_date);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                alertDialog.cancel();
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if (queryDocumentSnapshot.exists()) {
                                toConfirmation("Vehicle changed successfully.");
                                updateCurrentVehicle(reg_no_id);
                                removePreferenceValues();
                            }
                        }
                    }
                    else
                        toConfirmation("You have changed vehicle.Please carry out an inspection.");
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
    public void getRunningVehicle(final int job_id){
        refJobManagementDetailsNew.document(String.valueOf(job_id)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if (task.isSuccessful()){
                DocumentSnapshot queryDocumentSnapshot =task.getResult();
                if (Objects.equals(queryDocumentSnapshot.getString("status"), "Active"))
                    vehicle_registration_no_id = queryDocumentSnapshot.getLong("vehicle_registration_no_id").intValue();
                changeVehicleOption.getRegistrationNoList(arrayliist_reg_no,arraylist_reg_no_id,vehicle_registration_no_id);
            }
        }
    }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    });
    }

    private void updateCurrentVehicle(final int vehicle_id){
        Query query = currentVehicleReference.whereEqualTo("user_id",user_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                        String docId = queryDocumentSnapshot.getId();
                        Map<String,Object> updateCurrentVehicle = new HashMap<>();
                        updateCurrentVehicle.put("user_id",user_id);
                        updateCurrentVehicle.put("inspected","Yes");
                        updateCurrentVehicle.put("vehicle_id",vehicle_id);
                        currentVehicleReference.document(docId).update(updateCurrentVehicle);
                        Log.e("Success", "Current vehicle changed successfully.");
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Error", "Unable to change current vehicle.");
            }
        });
    }

    private void removePreferenceValues(){
        sharedPreferences = context.getSharedPreferences("MyPref",0);
        editor = sharedPreferences.edit();
        if (sharedPreferences.contains("supply_coming_form"))
            editor.remove("supply_coming_form");
        if (sharedPreferences.contains("waste_coming_form"))
            editor.remove("waste_coming_form");
        editor.remove("isSubmitted");
        editor.remove("isSubmitSupply");
        editor.remove("isSubmittedWaste");
        editor.remove("startTaskClicked");
        editor.remove("arrivedCollectionClicked");
        editor.remove("leftCollectionClicked");
        editor.remove("arrivedDeliveryClicked");
        editor.apply();
    }

    public void toConfirmation(String message){
        progressDialog.dismiss();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View popupView = inflater.inflate(R.layout.notification_popup_layout,null,true);
        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,true);
        popupWindow.showAtLocation(popupView, Gravity.BOTTOM,0,0);
        Button cancelPopup = popupView.findViewById(R.id.bt_ok);
        TextView tv_change_pass = popupView.findViewById(R.id.tv_change_pass);
        tv_change_pass.setText(message);
        cancelPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                Intent toDashboard = new Intent(context,MainActivity.class);
                context. startActivity(toDashboard);
            }
        });
    }
}
