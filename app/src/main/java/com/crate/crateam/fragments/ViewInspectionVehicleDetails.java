package com.crate.crateam.fragments;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.crate.crateam.R;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class ViewInspectionVehicleDetails extends Fragment {
    private ProgressDialog progressDialog;
    private TextView tv_report_id,tv_date_time,tv_user,tv_name,tv_vehicle_no,tv_trailer,tv_manufacturer,tv_model,tv_odometer_reading,tv_front_tag,tv_near_tag,tv_off_tag,tv_completion_tag;
    private ImageView iv_driver_sign;
    private String inspection_id,vehicle_no;
    private int vehicle_id=0,logged_by=0;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference inspectionReference,vehicleDetailsReference,userDetailsReference;

    protected static final String TAG = "ViewVehicleDetails";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_inspection_vehicle_details, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view){
        FirestoreManager.initPersistentIndexManager();
        inspectionReference = db.collection("CR_inspection_submission");
        vehicleDetailsReference = db.collection("CR_vehicle_details");
        userDetailsReference = db.collection("CR_user_details");
        Log.d("InViewInspection","InsIdViewInspection : "  +"I am in View Inspection");
        SharedPreferences preferences = getActivity().getSharedPreferences("Preference", 0); // 0 - for private mode
        progressDialog = Dialog.showProgressDialog(getActivity());
        tv_report_id = view.findViewById(R.id.tv_report_id);
        tv_date_time = view.findViewById(R.id.tv_date_time);
        tv_user = view.findViewById(R.id.tv_user);
        tv_name = view.findViewById(R.id.tv_name);
        tv_vehicle_no = view.findViewById(R.id.tv_vehicle_no);
        tv_trailer = view.findViewById(R.id.tv_trailer);
        tv_manufacturer = view.findViewById(R.id.tv_manufacturer);
        tv_model = view.findViewById(R.id.tv_model);
        tv_odometer_reading = view.findViewById(R.id.tv_odometer_reading);
        tv_front_tag = view.findViewById(R.id.tv_front_tag);
        tv_near_tag = view.findViewById(R.id.tv_near_tag);
        tv_off_tag = view.findViewById(R.id.tv_off_tag);
        tv_completion_tag = view.findViewById(R.id.tv_completion_tag);
        iv_driver_sign = view.findViewById(R.id.iv_driver_sign);
        inspection_id = preferences.getString("doc_id",null);
        Log.d("InViewInspectionDetails","InsIdViewInspection : "  +inspection_id);
        progressDialog.show();
        loadVehicleInspectionDetails();
    }



    private void  loadVehicleInspectionDetails(){
        Query query1 = inspectionReference.whereEqualTo("id",inspection_id);
        query1.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    progressDialog.dismiss();
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        String inspection_id = documentSnapshot.getString("inspection_id");
                        vehicle_no = documentSnapshot.getString("registration_no");
                        vehicle_id = Objects.requireNonNull(documentSnapshot.getLong("vehicle_id")).intValue();
                        logged_by = Objects.requireNonNull(documentSnapshot.getLong("logged_by")).intValue();
                        String odometer_reading = documentSnapshot.getString("odometer_reading");
                        String driver_sign = documentSnapshot.getString("driver_sign");
                        String conducted_on = documentSnapshot.getString("conducted_on");
                        String submission_time = documentSnapshot.getString("submission_time");
                        String driver_name = documentSnapshot.getString("driver_name");
                        String wm_sign_available = documentSnapshot.getString("wm_sign_available");
                        tv_report_id.setText(inspection_id);
                        tv_date_time.setText(conducted_on + " , "+submission_time);
                        tv_name.setText(driver_name);
                        if (Objects.requireNonNull(wm_sign_available).equals("Yes"))
                            loadUserName(logged_by);
                        else
                            tv_user.setText(driver_name);
                        tv_vehicle_no.setText(vehicle_no);
                        tv_odometer_reading.setText(odometer_reading);
                        Bitmap decodedImage = decodeImageString(driver_sign);
                        iv_driver_sign.setImageBitmap(decodedImage);
                        loadManufacturerModel(vehicle_id);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Dialog.alertDialog(getActivity(),"Unable to get vehicle inspection details.");
            }
        });
    }

    private void loadManufacturerModel(int vehicle_id){
        Query vehicleDetails = vehicleDetailsReference.whereEqualTo("external_vehicle","No").whereEqualTo("id",vehicle_id).whereEqualTo("status","Active");
        vehicleDetails.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        String vehicle_front = queryDocumentSnapshot.getString("vehicle_front");
                        String vehicle_near = queryDocumentSnapshot.getString("vehicle_near_rear");
                        String vehicle_off = queryDocumentSnapshot.getString("vehicle_off_rear");
                        String vehicle_completion = queryDocumentSnapshot.getString("vehicle_completion");
                        String trailer = queryDocumentSnapshot.getString("trailer");
                        String manufacturer = queryDocumentSnapshot.getString("manufacturer");
                        String model = queryDocumentSnapshot.getString("model");
                        tv_manufacturer.setText(manufacturer);
                        tv_model.setText(model);
                        tv_trailer.setText(trailer);
                        tv_front_tag.setText(vehicle_front);
                        tv_near_tag.setText(vehicle_near);
                        tv_off_tag.setText(vehicle_off);
                        tv_completion_tag.setText(vehicle_completion);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(),"Unable to get manufacturer and model id.",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserName(int id){
        Query modelNameQuery = userDetailsReference.whereEqualTo("id",id);
        modelNameQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    progressDialog.dismiss();
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        String user = documentSnapshot.getString("full_name");
                        Log.d(TAG,"User Name :" +user);
                        tv_user.setText(user);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(),"Unable to get user name.",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Bitmap decodeImageString(String imageString){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] imageBytes = baos.toByteArray();
        imageBytes = Base64.decode(imageString, Base64.DEFAULT);
        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        return decodedImage;
    }
}
