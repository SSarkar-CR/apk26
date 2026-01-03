package com.crate.crateam.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.crate.crateam.R;
import com.crate.crateam.activities.AdHocReportActivity;
import com.crate.crateam.activities.DieselDeliveryActivity;
import com.crate.crateam.activities.MainActivity;
import com.crate.crateam.activities.ServicesDriverNoteActivity;
import com.crate.crateam.activities.SupplyFormOperator;
import com.crate.crateam.activities.WasteTransferFormOperator;
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

public class FormExpandFragment extends Fragment {
    Fragment fragment = null;
    private String tag, vehicle_inspection = "", ad_hoc_defect_report = "", waste_transfer_form_operator="",
            supply_form_operator="", diesel_delivery="", service_driver_note="" ;
    private SharedPreferences.Editor editor;
    private int vehicle_number=0,user_id=0;
    MainActivity mainActivity ;
    private CollectionReference collInspectionSubmission,collInspectionSubmissionService;
    private ArrayList<Integer> arrayList_vehicle_id;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private ProgressDialog progressDialog;
    private RelativeLayout rl_waste_transfer_form,rl_supply_note,rl_vehicle_form,rl_ad_hoc_form,rl_diesel_delivery,rl_service_driver_note;

    protected static final String TAG = "FormExpand";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.form_expand_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirestoreManager.initPersistentIndexManager();
        collInspectionSubmission = db.collection("CR_inspection_submission");
        collInspectionSubmissionService = db.collection("CR_inspection_submission");
        arrayList_vehicle_id = new ArrayList<>();
        progressDialog = Dialog.showProgressDialog(getActivity());
        SessionManager sessionManager = new SessionManager(getActivity());
        HashMap<String,String> app_view_controller = sessionManager.getAppViewController();
        vehicle_inspection = app_view_controller.get(SessionManager.KEY_VEHICLE_INSPECTION_ACCESS);
        ad_hoc_defect_report = app_view_controller.get(SessionManager.KEY_AD_HOC_DEFECT_REPORT_ACCESS);
        waste_transfer_form_operator = app_view_controller.get(SessionManager.KEY_WASTE_TRANSFER_FOR_OPERATOR_ACCESS);
        supply_form_operator = app_view_controller.get(SessionManager.KEY_SUPPLY_FORM_OPERATOR_ACCESS);

        diesel_delivery = app_view_controller.get(SessionManager.KEY_DIESEL_DELIVERY_ACCESS);
        service_driver_note = app_view_controller.get(SessionManager.KEY_SERVICE_DRIVER_NOTE_ACCESS);

        HashMap<String, String> user = sessionManager.getUserDetails();
        HashMap<String, String> vehicle = sessionManager.getVehicleDetails();
        if (vehicle.get(SessionManager.KEY_VEHICLE_ID) != null)
            vehicle_number = Integer.parseInt((Objects.requireNonNull(vehicle.get(SessionManager.KEY_VEHICLE_ID))));
        Log.e("vehicle_number1", String.valueOf(vehicle_number));
        user_id = Integer.parseInt(user.get(SessionManager.KEY_ID));

        SharedPreferences pref = getActivity().getSharedPreferences("MyPref", 0); // 0 - for private mode
        editor = pref.edit();
        mainActivity = (MainActivity)getActivity();
        rl_waste_transfer_form = view.findViewById(R.id.rl_waste_transfer_form);
        rl_supply_note = view.findViewById(R.id.rl_supply_note);
        rl_vehicle_form = view.findViewById(R.id.rl_vehicle_form);
        rl_ad_hoc_form = view.findViewById(R.id.rl_ad_hoc_form);
        rl_diesel_delivery = view.findViewById(R.id.rl_diesel_delivery);
        rl_service_driver_note = view.findViewById(R.id.rl_service_driver_note);

        if (vehicle_inspection.equals("yes"))
            rl_vehicle_form.setVisibility(View.VISIBLE);
        if (ad_hoc_defect_report.equals("yes"))
            rl_ad_hoc_form.setVisibility(View.VISIBLE);
        if (waste_transfer_form_operator.equals("yes"))
            rl_waste_transfer_form.setVisibility(View.VISIBLE);
        if (supply_form_operator.equals("yes"))
            rl_supply_note.setVisibility(View.VISIBLE);
        if (diesel_delivery.equals("yes"))
            rl_diesel_delivery.setVisibility(View.VISIBLE);
        if (service_driver_note.equals("yes"))
            rl_service_driver_note.setVisibility(View.VISIBLE);
        rl_vehicle_form.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    fragment = new InspectionFragment();
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame, fragment,"INSPECTION");
                    ft.addToBackStack("INSPECTION");
                    ft.commit();
                    tag = fragment.getTag();
                    Log.d(TAG,"CHILD_TAG" +fragment.getTag());
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.getChildTag(tag);
            }
        });
        rl_ad_hoc_form.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ad_hoc_intent = new Intent(getActivity(), AdHocReportActivity.class);
                startActivity(ad_hoc_intent);
            }
        });
        rl_waste_transfer_form.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent waste_transfer_intent = new Intent(getActivity(), WasteTransferFormOperator.class);
                    startActivity(waste_transfer_intent);
            }
        });
        rl_supply_note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent supply_note_intent = new Intent(getActivity(), SupplyFormOperator.class);
                    startActivity(supply_note_intent);
            }
        });
        rl_diesel_delivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getInspectionSubmission();
                if (!AppData.internetOnline(getActivity()))
                    progressDialog.dismiss();
            }
        });
        rl_service_driver_note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getInspectionSubmissionService();
                if (!AppData.internetOnline(getActivity()))
                    progressDialog.dismiss();
            }
        });
        removePreferences();
    }

    private void removePreferences(){
        editor.remove("nfc1");
        editor.remove("nfc2");
        editor.remove("nfc3");
        editor.remove("nfc4");
        editor.remove("nfc5");
        editor.remove("nfc6");
        editor.apply();
        int count = 0;
        mainActivity.getCount(count);
    }
    private void showMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Please inspect a vehicle first.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void getInspectionSubmission(){
        // get current date
        Date c = Calendar.getInstance().getTime();
        Log.d(TAG,"Current time => " + c);
        SimpleDateFormat df = new SimpleDateFormat("ddMMyyyy");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String current_date = simpleDateFormat.format(c);
        progressDialog.show();
        Query query = collInspectionSubmission.whereEqualTo("logged_by",user_id).whereEqualTo("conducted_on",current_date);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    arrayList_vehicle_id.clear();
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            arrayList_vehicle_id.add(queryDocumentSnapshot.getLong("vehicle_id").intValue());
                        }
                        if (arrayList_vehicle_id.size() != 0) {
                            Intent diesel_delivery_intent = new Intent(getActivity(), DieselDeliveryActivity.class);
                            startActivity(diesel_delivery_intent);
                        } else
                            showMessage();
                    }
                    else
                        showMessage();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getInspectionSubmissionService(){
        // get current date
        Date c = Calendar.getInstance().getTime();
        Log.d(TAG,"Current time => " + c);
        SimpleDateFormat df = new SimpleDateFormat("ddMMyyyy");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String current_date = simpleDateFormat.format(c);
        progressDialog.show();
        Query query = collInspectionSubmissionService.whereEqualTo("logged_by",user_id).whereEqualTo("conducted_on",current_date);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    arrayList_vehicle_id.clear();
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            arrayList_vehicle_id.add(queryDocumentSnapshot.getLong("vehicle_id").intValue());
                        }
                        if (arrayList_vehicle_id.size() != 0) {
                            Intent diesel_delivery_intent = new Intent(getActivity(), ServicesDriverNoteActivity.class);
                            startActivity(diesel_delivery_intent);
                        } else
                            showMessage();
                    }
                    else
                        showMessage();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
