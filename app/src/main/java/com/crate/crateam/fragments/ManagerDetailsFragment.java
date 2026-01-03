package com.crate.crateam.fragments;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.crate.crateam.R;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheIndexManager;
import com.crate.crateam.utility.FirestoreManager;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.HashMap;
import java.util.Objects;

public class ManagerDetailsFragment extends Fragment {
    public static final String TAG = "ManagerDetails";
    private String inspection_id, registration_no,user_name,manufacturer_id="",model_id="";
    private int vehicle_id=0;
    private TextView tv_report_id, tv_user_name, tv_vehicle_no, tv_trailer, tv_manufacturer, tv_model;
    private ProgressDialog progressDialog;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference vehicleDetailsReference;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.manager_details_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view) {
        FirestoreManager.initPersistentIndexManager();
        vehicleDetailsReference = db.collection("CR_vehicle_details");
        listenOfflineData(vehicleDetailsReference);
        SharedPreferences preferences = getActivity().getSharedPreferences("Preference", 0); // 0 - for private mode
        SessionManager sessionManager = new SessionManager(getActivity());
        progressDialog = Dialog.showProgressDialog(getActivity());
        tv_report_id = view.findViewById(R.id.tv_report_id);
        tv_user_name = view.findViewById(R.id.tv_user_name);
        tv_vehicle_no = view.findViewById(R.id.tv_vehicle_no);
        tv_trailer = view.findViewById(R.id.tv_trailer);
        tv_manufacturer = view.findViewById(R.id.tv_manufacturer);
        tv_model = view.findViewById(R.id.tv_model);
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_name= user.get(SessionManager.KEY_FULL_NAME);
        inspection_id = preferences.getString("view_inspection_id", null);
        registration_no = preferences.getString("registration_id", null);
        vehicle_id = preferences.getInt("vehicle_no",0);
        Log.d("InViewInspection", "InsIdViewInspection : " + inspection_id);
        progressDialog.show();
        loadVehicleDetails();
    }



    private void listenOfflineData(CollectionReference collectionReference){
        collectionReference.addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshot,
                                @Nullable FirebaseFirestoreException e) {
            if (e != null) {
                Log.w(TAG, "Listen error", e);
                return;
            }
            for (DocumentChange change : querySnapshot.getDocumentChanges()) {
                if (change.getType() == DocumentChange.Type.ADDED) {
                    Log.d(TAG, "Data :" + change.getDocument().getData());
                }
                String source = querySnapshot.getMetadata().isFromCache() ?
                        "local cache" : "server";
                Log.d(TAG, "Data fetched from " + source);
            }
            }
        });
    }

    private void loadVehicleDetails(){
        Query vehicleDetailsQuery = vehicleDetailsReference.whereEqualTo("external_vehicle","No").whereEqualTo("id",vehicle_id).whereEqualTo("status","Active");
        vehicleDetailsQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    progressDialog.dismiss();
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        String trailer = documentSnapshot.getString("trailer");
                        Log.d("KEY :" , Objects.requireNonNull(trailer));
                        manufacturer_id = documentSnapshot.getString("manufacturer");
                        model_id = documentSnapshot.getString("model");
                        tv_report_id.setText(inspection_id);
                        tv_vehicle_no.setText(registration_no);
                        tv_trailer.setText(trailer);
                        tv_user_name.setText(user_name);
                        tv_manufacturer.setText(manufacturer_id);
                        tv_model.setText(model_id);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Dialog.alertDialog(getActivity(),"Unable to get results.");
            }
        });
    }
}
