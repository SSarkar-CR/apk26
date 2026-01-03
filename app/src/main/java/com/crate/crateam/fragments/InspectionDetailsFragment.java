package com.crate.crateam.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.crate.crateam.R;
import com.crate.crateam.activities.MainActivity;
import com.crate.crateam.utility.CustomSearchableSpinner;
import com.crate.crateam.utility.CustomViewPagerInspection;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheIndexManager;
import com.crate.crateam.utility.FirestoreManager;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class InspectionDetailsFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = "INSPECTION_DETAILS_FRAG";
    private ImageView iv_nfcTag_front,iv_nfcTag_near,iv_nfcTag_off,iv_next_page;
    private LinearLayout ll_tag1,ll_tag2,ll_tag3,ll_nfc;
    private RelativeLayout rl_arrow;
    private TextView tv_front_tag,tv_near_tag,tv_off_tag;
    private CheckBox cb_nfc;
    private CustomSearchableSpinner sp_reg_no;
    private CustomViewPagerInspection vp;
    private String nfc_data,vehicle_type,manufacturer,model,trailer,vehicle_front="",vehicle_near="",vehicle_off="",vehicle_completion="",
            user_name,registration_no,haulier_company="",haulier_carrier_name="";
    private TextView tv_registration_no,tv_driver_name,tv_fleet_no,tv_manufacturer,tv_model;
    private int count=0,vehicle_id=0,user_id=0;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    MainActivity mainActivity ;
    private ProgressDialog progressDialog;
    private SessionManager sessionManager;
    private InputMethodManager imm ;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference vehicleDetailsReference;
    private boolean tag_exists = false,nfc_checked= false;
    private ArrayList<Integer> user_list = new ArrayList<>();
    private ArrayList<String> registration_no_list = new ArrayList<>();
    private ArrayList<Integer> vehicle_id_list = new ArrayList<>();
    private ArrayList<Integer> assigned_vehicle_id = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.inspection_details_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initializeOnClick();
        setUpTag();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView(View view){
        FirestoreManager.initPersistentIndexManager();
        vehicleDetailsReference = db.collection("CR_vehicle_details");
        sessionManager = new SessionManager(getActivity());
        progressDialog = Dialog.showProgressDialog(getActivity());
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        iv_nfcTag_front = view.findViewById(R.id.iv_nfcTag_front);
        iv_nfcTag_near = view.findViewById(R.id.iv_nfcTag_near);
        iv_nfcTag_off = view.findViewById(R.id.iv_nfcTag_off);
        iv_next_page = view.findViewById(R.id.iv_next_page);
        ll_nfc = view.findViewById(R.id.ll_nfc);
        ll_tag1 = view.findViewById(R.id.ll_tag1);
        ll_tag2 = view.findViewById(R.id.ll_tag2);
        ll_tag3 = view.findViewById(R.id.ll_tag3);
        rl_arrow = view.findViewById(R.id.rl_arrow);
        tv_registration_no = view.findViewById(R.id.tv_registration_no);
        tv_driver_name = view.findViewById(R.id.tv_driver_name);
        tv_fleet_no = view.findViewById(R.id.tv_fleet_no);
        tv_manufacturer= view.findViewById(R.id.tv_manufacturer);
        tv_model = view.findViewById(R.id.tv_model);
        tv_front_tag = view.findViewById(R.id.tv_front_tag);
        tv_near_tag = view.findViewById(R.id.tv_near_tag);
        tv_off_tag = view.findViewById(R.id.tv_off_tag);
        cb_nfc = view.findViewById(R.id.cb_nfc);
        sp_reg_no = view.findViewById(R.id.sp_reg_no);
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_id = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        user_name = user.get(SessionManager.KEY_FULL_NAME);
        Log.d(TAG,"USER_ID : " +user_id);
        pref = getActivity().getSharedPreferences("MyPref", 0); // 0 - for private mode
        editor = pref.edit();
        editor.putString("with_nfc", "Yes");
        editor.apply();
        vp= getActivity().findViewById(R.id.viewpager);
        mainActivity = (MainActivity) getActivity();
        mainActivity.getChildTag(TAG);
        mainActivity.getNfcSelection("No");
    }

    private void setUpTag(){
        vp.setAllowedSwipeDirection(CustomViewPagerInspection.SwipeDirection.NONE);
        Bundle args = getArguments();
        if (getArguments()!= null){
            nfc_data = args.getString("message");
            count = args.getInt("counter");
            Log.d(TAG,"NFC_Data Details: "  +nfc_data +" "+count);
        }
        if (nfc_data != null)
            loadNfcDetails();
    }

    private void initializeOnClick(){
        iv_nfcTag_front.setOnClickListener(this);
        iv_nfcTag_near.setOnClickListener(this);
        iv_nfcTag_off.setOnClickListener(this);
        iv_next_page.setOnClickListener(this);
        cb_nfc.setOnClickListener(this);
        sp_reg_no.setOnItemSelectedListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_nfcTag_front:
            case R.id.iv_nfcTag_near:
            case R.id.iv_nfcTag_off:
                 Dialog.readyToScanDialog(getActivity());
                 break;
            case R.id.cb_nfc:
                tv_driver_name.setText("");
                tv_registration_no.setText("");
                tv_fleet_no.setText("");
                tv_manufacturer.setText("");
                tv_model.setText("");
                if (cb_nfc.isChecked()) {
                    nfc_checked = true;
                    editor.putString("with_nfc", "No");
                    editor.apply();
                    mainActivity.getNfcSelection("Yes");
                    ll_nfc.setVisibility(View.GONE);
                    sp_reg_no.setVisibility(View.VISIBLE);
                    vehicleUserMapping();
                } else {
                    nfc_checked = false;
                    editor.putString("with_nfc", "Yes");
                    editor.apply();
                    mainActivity.getNfcSelection("No");
                    ll_nfc.setVisibility(View.VISIBLE);
                    sp_reg_no.setVisibility(View.GONE);
                    rl_arrow.setVisibility(View.GONE);
                }
                break;
            case R.id.iv_next_page:
                vp.setCurrentItem(1);
                break;
        }
    }



    public void vehicleUserMapping(){
        Query query = vehicleDetailsReference.whereEqualTo("external_vehicle","No").whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    String userIds = "";
                    assigned_vehicle_id.clear();
                    ArrayList<Integer> arrayList_user_id = new ArrayList<>();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        userIds = queryDocumentSnapshot.getString("vehicle_user_mappings");
                        if (userIds != null && userIds.contains(",")) {
                            ArrayList<String> userIdListString = new ArrayList<String>(Arrays.asList(userIds.split(",")));
                            for (int i = 0; i < userIdListString.size(); i++) {
                                arrayList_user_id.add(Integer.valueOf(userIdListString.get(i)));
                                if (Integer.valueOf(userIdListString.get(i)) == user_id) {
                                    assigned_vehicle_id.add(queryDocumentSnapshot.getLong("id").intValue());
                                }
                            }
                        } else {
                            if (userIds != null) {
                                if (!userIds.equals("")) {
                                    arrayList_user_id.add(Integer.valueOf(userIds));
                                    if (Integer.valueOf(userIds) == user_id) {
                                        assigned_vehicle_id.add(queryDocumentSnapshot.getLong("id").intValue());
                                    }
                                }
                            }
                        }
                    }
                    Log.d(TAG,"Vehicle id list :" + assigned_vehicle_id);
                    fetchVehicleRegistrationNumber();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void fetchVehicleRegistrationNumber(){
        final Query query = vehicleDetailsReference.whereEqualTo("external_vehicle","No").whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        int id = queryDocumentSnapshot.getLong("id").intValue();
                        for (int i=0;i<assigned_vehicle_id.size();i++){
                            if (assigned_vehicle_id.get(i) == id){
                                vehicle_id_list.add(id);
                                registration_no_list.add(queryDocumentSnapshot.getString("registration_no"));
                            }
                        }
                    }
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_custom_layout, registration_no_list);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                    sp_reg_no.setAdapter(spinnerArrayAdapter);
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

    private void loadNfcDetails(){
        progressDialog.show();
        Task task1 = vehicleDetailsReference.whereEqualTo("external_vehicle","No").whereEqualTo("status","Active").whereEqualTo("vehicle_front",nfc_data).get();
        Task task2 = vehicleDetailsReference.whereEqualTo("external_vehicle","No").whereEqualTo("status","Active").whereEqualTo("vehicle_near_rear",nfc_data).get();
        Task task3 = vehicleDetailsReference.whereEqualTo("external_vehicle","No").whereEqualTo("status","Active").whereEqualTo("vehicle_off_rear",nfc_data).get();
        final Task<List<QuerySnapshot>> allTask = Tasks.whenAllSuccess(task1,task2,task3);
        allTask.addOnSuccessListener(new OnSuccessListener<List<QuerySnapshot>>() {
            @Override
            public void onSuccess(List<QuerySnapshot> querySnapshots) {
                for (QuerySnapshot querySnapshot : querySnapshots){
                    for (QueryDocumentSnapshot queryDocumentSnapshot :querySnapshot){
                        if (queryDocumentSnapshot.exists()){
                            tag_exists = true;
                            vehicle_id = Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
                            vehicle_front = queryDocumentSnapshot.getString("vehicle_front");
                            vehicle_near = queryDocumentSnapshot.getString("vehicle_near_rear");
                            vehicle_off = queryDocumentSnapshot.getString("vehicle_off_rear");
                            vehicle_completion = queryDocumentSnapshot.getString("vehicle_completion");
                            trailer = queryDocumentSnapshot.getString("trailer");
                            Log.d(TAG,"Vehicle Id:" +vehicle_id);
                        }
                    }
                }
                getUserIdList(vehicle_id);
                if (!tag_exists){
                    progressDialog.dismiss();
                    Dialog.alertDialog(getActivity(),"Tag not recognised.Please scan again.");
                    count = count - 1;
                    mainActivity.getCount(count);
                    setNfcDataFromSharePreference();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.e("Error :" , "Unable to get vehicle nfc.");
            }
        });
    }



    private void getUserIdList(int vehicle_id){
        Query query = vehicleDetailsReference.whereEqualTo("id",vehicle_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    user_list.clear();
                    String userIds = "";
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (queryDocumentSnapshot.exists()) {
                                userIds = queryDocumentSnapshot.getString("vehicle_user_mappings");
                            }
                        }
                        if (userIds != null && userIds.contains(",")) {
                            ArrayList<String> userIdListString = new ArrayList<String>(Arrays.asList(userIds.split(",")));
                            for (int i = 0; i < userIdListString.size(); i++) {
                                user_list.add(Integer.valueOf(userIdListString.get(i)));
                            }
                        } else {
                            if (userIds != null) {
                                if (!userIds.equals("")) {
                                    user_list.add(Integer.valueOf(userIds));
                                }
                            }
                        }
                        Log.d(TAG,"User Ids :" +user_list);
                        if (user_list.contains(user_id) && nfc_checked)
                            rl_arrow.setVisibility(View.VISIBLE);
                        else
                            rl_arrow.setVisibility(View.GONE);
                        if (user_list.contains(user_id))
                            loadVehicleDetails(vehicle_id);
                        else {
                            progressDialog.dismiss();
                            Dialog.alertDialog(getContext(), "You are not assign with this vehicle.");
                            count = count - 1;
                            mainActivity.getCount(count);
                        }
                    } else {
                        progressDialog.dismiss();
                        Dialog.alertDialog(getActivity(),"Tag not recognised.Please scan again.");
                        count = count - 1;
                        mainActivity.getCount(count);
                        Log.d(TAG,"I am here:" +"inside vehicle user mapping 4");
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Dialog.alertDialog(getActivity(),"Tag not recognised.Please scan again.");
                count = count - 1;
                mainActivity.getCount(count);
                setNfcDataFromSharePreference();
            }
        });
    }

    private void loadVehicleDetails(final int vehicle_id){
        Query vehicleDetailsQuery = vehicleDetailsReference.whereEqualTo("external_vehicle","No").whereEqualTo("id",vehicle_id).whereEqualTo("status","Active");
        vehicleDetailsQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    progressDialog.dismiss();
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                            if (queryDocumentSnapshot.exists()){
                                registration_no = queryDocumentSnapshot.getString("registration_no");
                                trailer = queryDocumentSnapshot.getString("trailer");
                                manufacturer = queryDocumentSnapshot.getString("manufacturer");
                                model = queryDocumentSnapshot.getString("model");
                                vehicle_type = queryDocumentSnapshot.getString("vehicle_type");
                                tv_driver_name.setText(user_name);
                                tv_registration_no.setText(registration_no);
                                tv_fleet_no.setText(trailer);
                                tv_manufacturer.setText(manufacturer);
                                tv_model.setText(model);
                                Log.d(TAG,"VEHICLE DETAILS :" + registration_no + " " + manufacturer+ " " + model+ " " + trailer + " " + vehicle_type +
                                        " " + haulier_company+" "+haulier_carrier_name+ " " + vehicle_front + " " + vehicle_near + " " + vehicle_off + " "
                                        + vehicle_completion);
                                sessionManager.createVehicleSession(registration_no,String.valueOf(vehicle_id),haulier_company,haulier_carrier_name,manufacturer,model,trailer,vehicle_type,
                                        vehicle_front,vehicle_near,vehicle_off,vehicle_completion,"","","");
                                setValuesAfterScan();
                            }else {
                                count = count - 1;
                                mainActivity.getCount(count);
                                Dialog.alertDialog(getActivity(),"Unable to get vehicle details.");
                            }
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.e("Error :" , "Unable to get vehicle details.");
            }
        });
    }

    private void setValuesAfterScan(){
        if (!nfc_checked) {
            if (vp.getCurrentItem() == 0) {
                if (count < 4) {
                    if (nfc_data.equals(vehicle_front)) {
                        tv_front_tag.setVisibility(View.VISIBLE);
                        Dialog.successfullyScanDialog(getActivity());
                        ll_tag1.setBackgroundColor(Color.parseColor("#00CC66"));
                        tv_front_tag.setText(vehicle_front);
                        editor.putString("nfc1", vehicle_front);
                        editor.apply();
                        setNfcDataFromSharePreference();
                    } else if (nfc_data.equals(vehicle_near)) {
                        tv_near_tag.setVisibility(View.VISIBLE);
                        Dialog.successfullyScanDialog(getActivity());
                        ll_tag2.setBackgroundColor(Color.parseColor("#00CC66"));
                        tv_near_tag.setText(vehicle_near);
                        editor.putString("nfc2", vehicle_near);
                        editor.apply();
                        setNfcDataFromSharePreference();
                    } else if (nfc_data.equals(vehicle_off)) {
                        tv_off_tag.setVisibility(View.VISIBLE);
                        Dialog.successfullyScanDialog(getActivity());
                        ll_tag3.setBackgroundColor(Color.parseColor("#00CC66"));
                        tv_off_tag.setText(vehicle_off);
                        editor.putString("nfc3", vehicle_off);
                        editor.apply();
                        setNfcDataFromSharePreference();
                    } else {
                        Dialog.alertDialog(getActivity(), "Tag not recognised.Please scan again.");
                        count = count - 1;
                        mainActivity.getCount(count);
                    }
                    if (tv_front_tag.getText().toString().equals(vehicle_front) && tv_near_tag.getText().toString().equals(vehicle_near) && tv_off_tag.getText().toString().equals(vehicle_off) && count <= 3) {
                        if (nfc_data.equals(vehicle_front) || nfc_data.equals(vehicle_near) || nfc_data.equals(vehicle_off)) {
                            count = 3;
                            mainActivity.getCount(count);
                            vp.setCurrentItem(1);
                        }
                    } else if (tv_front_tag.getText().toString().equals(vehicle_front) && tv_near_tag.getText().toString().equals(vehicle_near) || tv_near_tag.getText().toString().equals(vehicle_near) && tv_off_tag.getText().toString().equals(vehicle_off)
                            || tv_front_tag.getText().toString().equals(vehicle_front) && tv_off_tag.getText().toString().equals(vehicle_off) && count >= 2) {
                        count = 2;
                        mainActivity.getCount(count);
                    } else if (tv_front_tag.getText().toString().equals(vehicle_front) || tv_near_tag.getText().toString().equals(vehicle_near) || tv_off_tag.getText().toString().equals(vehicle_off) && count >= 1) {
                        count = 1;
                        mainActivity.getCount(count);
                    }
                }
            } else if (tv_front_tag.getText().toString().equals(vehicle_front) && tv_near_tag.getText().toString().equals(vehicle_near) && tv_off_tag.getText().toString().equals(vehicle_off) && count < 3) {
                count = 3;
                mainActivity.getCount(count);
                vp.setCurrentItem(1);
            }
        }
    }

    private void setNfcDataFromSharePreference(){
        Log.d(TAG,"COUNT 55:" +count+" "+nfc_data+" "+vehicle_front+" "+vehicle_near+" "+vehicle_off+" "+pref.getString("nfc1", null)
        +" "+pref.getString("nfc2", null)+" "+pref.getString("nfc3", null));
        if (!tag_exists) {
            if (pref.getString("nfc1", null) != null) {
                tv_front_tag.setVisibility(View.VISIBLE);
                tv_front_tag.setText(pref.getString("nfc1", null));
                ll_tag1.setBackgroundColor(Color.parseColor("#00CC66"));
            }
            if (pref.getString("nfc2", null) != null) {
                tv_near_tag.setVisibility(View.VISIBLE);
                tv_near_tag.setText(pref.getString("nfc2", null));
                ll_tag2.setBackgroundColor(Color.parseColor("#00CC66"));
            }
            if (pref.getString("nfc3", null) != null) {
                tv_off_tag.setVisibility(View.VISIBLE);
                tv_off_tag.setText(pref.getString("nfc3", null));
                ll_tag3.setBackgroundColor(Color.parseColor("#00CC66"));
            }
        }else {
            if (Objects.equals(pref.getString("nfc1", null), vehicle_front)) {
                tv_front_tag.setVisibility(View.VISIBLE);
                tv_front_tag.setText(pref.getString("nfc1", null));
                ll_tag1.setBackgroundColor(Color.parseColor("#00CC66"));
            }else {
                tv_front_tag.setText("");
                ll_tag1.setBackgroundColor(Color.parseColor("#FFFFFF"));
                editor.putString("nfc1", "");
                editor.apply();
            }
            if (Objects.equals(pref.getString("nfc2", null), vehicle_near)) {
                tv_near_tag.setVisibility(View.VISIBLE);
                tv_near_tag.setText(pref.getString("nfc2", null));
                ll_tag2.setBackgroundColor(Color.parseColor("#00CC66"));
            }else {
                tv_near_tag.setText("");
                ll_tag2.setBackgroundColor(Color.parseColor("#FFFFFF"));
                editor.putString("nfc2", "");
                editor.apply();
            }
            if (Objects.equals(pref.getString("nfc3", null), vehicle_off)) {
                tv_off_tag.setVisibility(View.VISIBLE);
                tv_off_tag.setText(pref.getString("nfc3", null));
                ll_tag3.setBackgroundColor(Color.parseColor("#00CC66"));
            }else {
                tv_off_tag.setText("");
                ll_tag3.setBackgroundColor(Color.parseColor("#FFFFFF"));
                editor.putString("nfc3", "");
                editor.apply();
            }
        }

        if (tv_front_tag.getText().toString().isEmpty())
            ll_tag1.setBackgroundColor(Color.parseColor("#FFFFFF"));
        if (tv_near_tag.getText().toString().isEmpty())
            ll_tag2.setBackgroundColor(Color.parseColor("#FFFFFF"));
        if (tv_off_tag.getText().toString().isEmpty())
            ll_tag3.setBackgroundColor(Color.parseColor("#FFFFFF"));
        HashMap<String, String> vehicle = sessionManager.getVehicleDetails();
        String vehicle_no = vehicle.get(SessionManager.KEY_REGISTRATION_NO);
        String trailer = vehicle.get(SessionManager.KEY_TRAILER);
        String manufacturer = vehicle.get(SessionManager.KEY__MANUFACTURER);
        String model = vehicle.get(SessionManager.KEY_MODEL);
        tv_driver_name.setText(user_name);
        tv_registration_no.setText(vehicle_no);
        tv_fleet_no.setText(trailer);
        tv_manufacturer.setText(manufacturer);
        tv_model.setText(model);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (vp != null) {
                if (vp.getCurrentItem() == 0) {
                    mainActivity.getChildTag(TAG);
                    vp.setAllowedSwipeDirection(CustomViewPagerInspection.SwipeDirection.NONE);
                }
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.sp_reg_no:
                registration_no = sp_reg_no.getSelectedItem().toString().trim();
                vehicle_id = vehicle_id_list.get(position);
                Log.d(TAG,"GYGUGIU :" +vehicle_id);
                getUserIdList(vehicle_id);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

