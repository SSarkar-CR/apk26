package com.crate.crateam.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.crate.crateam.R;
import com.crate.crateam.activities.MainActivity;
import com.crate.crateam.adapters.ViewInspectionDefectsWMAdapter;
import com.crate.crateam.model.ViewInspectionDefectList;
import com.crate.crateam.utility.CustomViewPager;
import com.crate.crateam.utility.Dialog;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheIndexManager;
import com.crate.crateam.utility.FirestoreManager;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class ViewVehicleInspectionDefectsWM extends Fragment {
    private Fragment fragment = null;
    private EditText et_resolution;
    private CheckBox checkBox;
    private RelativeLayout rl_no_defects;
    private ProgressDialog progressDialog;
    private ViewInspectionDefectsWMAdapter adapter;
    private RecyclerView rv_view_inspection_defects_wm;
    private CustomViewPager viewPagerManagerDefects;
    private ImageView iv_next_page,iv_cross;
    private String inspection_id,vehicle_no,close_issue,tag;
    private int doc_count = 0,id_count= 0,element_id,driver_id,vehicle_id=0;
    private ArrayList<Integer> elementId = new ArrayList<>();
    private ArrayList<String> elementName = new ArrayList<>();
    private Boolean CheckEditTextEmpty ;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference vehicleDefectsReference,vehicleDefectsReferenceWM;

    protected static final String TAG = "ViewVehicleDefectsWM";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_inspection_defects_wm, container, false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view){
      FirestoreManager.initPersistentIndexManager();
      vehicleDefectsReference = db.collection("CR_vehicle_defect_inspections");
      vehicleDefectsReferenceWM = db.collection("CR_vehicle_defects_wm");
      SharedPreferences preferences = getActivity().getSharedPreferences("Preference", 0); // 0 - for private mode
      progressDialog = Dialog.showProgressDialog(getActivity());
      CustomViewPager viewPager = getActivity().findViewById(R.id.viewpager_workshop_manager);
      viewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
      rl_no_defects = view.findViewById(R.id.rl_no_defects);
      iv_next_page = view.findViewById(R.id.iv_next_page);
      iv_next_page.setEnabled(false);
      iv_next_page.setBackgroundResource(R.drawable.arrow_unselect);
      viewPagerManagerDefects = getActivity().findViewById(R.id.viewpager_manager_defects);
      viewPagerManagerDefects.disableScroll(true);
      iv_cross = getActivity().findViewById(R.id.iv_cross);
      rv_view_inspection_defects_wm = view.findViewById(R.id.rv_view_inspection_defects_wm);
      rv_view_inspection_defects_wm.setOverScrollMode(View.OVER_SCROLL_NEVER);
      inspection_id = preferences.getString("view_inspection_id",null);
      vehicle_no = preferences.getString("registration_id",null);
      vehicle_id = preferences.getInt("vehicle_no",0);
      driver_id = preferences.getInt("driver_id", 0);
      Log.d(TAG,"VEHICLE_NO: " +vehicle_no +" "+vehicle_id);
      progressDialog.show();
      setAdapter();
      onClick();
    }

    private void onClick(){
        iv_next_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               sendVehicleResolutionData();
               viewPagerManagerDefects.setCurrentItem(1);
            }
        });
        iv_cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialogFailed("Do you want to cancel report submission ?");
            }
        });
    }

    private void setAdapter(){
        progressDialog.dismiss();
        Query vehicleDefectsQuery = vehicleDefectsReference.whereEqualTo("inspection_id",inspection_id).whereEqualTo("element_type","vehicle").whereEqualTo("close_issue","No");
        FirestoreRecyclerOptions<ViewInspectionDefectList> options = new FirestoreRecyclerOptions.Builder<ViewInspectionDefectList>()
                .setQuery(vehicleDefectsQuery, ViewInspectionDefectList.class)
                .build();
        adapter = new ViewInspectionDefectsWMAdapter(options, getActivity(), new ViewInspectionDefectsWMAdapter.OnEditTextChanged() {
            @Override
            public void onTextChanged(int position,int size) {
                View view1 = rv_view_inspection_defects_wm.getChildAt(position);
                if (view1 != null) {
                    et_resolution = rv_view_inspection_defects_wm.getChildAt(position).findViewById(R.id.et_resolution);
                }
                if (position == size -1) {
                    if (!et_resolution.getText().toString().isEmpty()) {
                        iv_next_page.setEnabled(true);
                        iv_next_page.setBackgroundResource(R.drawable.arrow);
                    }
                }
            }
        });
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv_view_inspection_defects_wm.setLayoutManager(layoutManager);
        rv_view_inspection_defects_wm.setAdapter(adapter);
        vehicleDefectsQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    if (task.getResult().isEmpty()){
                        iv_next_page.setEnabled(true);
                        iv_next_page.setBackgroundResource(R.drawable.arrow);
                    }else {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            elementId.add(Objects.requireNonNull(documentSnapshot.getLong("element_id")).intValue());
                            elementName.add(documentSnapshot.getString("element_name"));
                            if (documentSnapshot.getString("element_name").equals("No")) {
                                rl_no_defects.setVisibility(View.VISIBLE);
                                rv_view_inspection_defects_wm.setVisibility(View.GONE);
                                viewPagerManagerDefects.disableScroll(false);
                                iv_next_page.setEnabled(true);
                                iv_next_page.setBackgroundResource(R.drawable.arrow);
                            }
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG,"LOAD_ERROR: " +e.getMessage());
                Dialog.alertDialog(getActivity(),"Unable to fetch results.");
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();
        if (adapter != null)
            adapter.startListening();
    }

    @Override
    public void onStop(){
        super.onStop();
        if(adapter != null)
            adapter.stopListening();
    }

    private void sendVehicleResolutionData(){
        for (int i = 0; i <adapter.getItemCount(); i++) {
            View view1 = rv_view_inspection_defects_wm.getChildAt(i);
            if (view1 != null) {
                et_resolution = view1.findViewById(R.id.et_resolution);
                checkBox = view1.findViewById(R.id.checkBox_element);
            }
            et_resolution.requestFocus();
            String resolutions = et_resolution.getText().toString();
            element_id = elementId.get(i);
            String element_name = elementName.get(i);
            boolean isChecked = checkBox.isChecked();
            if (isChecked)
                close_issue = "Yes";
            else
                close_issue = "No";
            // get current date
            Date c = Calendar.getInstance().getTime();
            Log.d(TAG,"Current time => " + c);
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
            String formattedDate = df.format(c);
            String current_date = formattedDate ;
            //get current time
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String current_time = dateFormatter.format(calendar.getTime());
            CheckEditTextIsEmptyOrNot(resolutions);
            if(CheckEditTextEmpty)
            {
                Log.d(TAG,"RESOLUTION_DATA :" +inspection_id + " "+element_id + " " +vehicle_no+ " "+resolutions +" "+driver_id+" "+close_issue);
                progressDialog.show();
                sendVehicleResolutionWM(inspection_id,vehicle_id, element_id, element_name, vehicle_no, resolutions, driver_id, "vehicle", close_issue, FieldValue.serverTimestamp(), FieldValue.serverTimestamp());
            }
        }
    }

    private void CheckEditTextIsEmptyOrNot(String resolutions){
        if(TextUtils.isEmpty(resolutions))
            CheckEditTextEmpty = false ;
        else
            CheckEditTextEmpty = true ;
    }

    private void sendVehicleResolutionWM(final String inspectionId,int vehicle_id, int elementId, final String element_name, String vehicle_no, final String wm_resolution, int driver_id, final String element_type, final String closeIssue, FieldValue created_at, FieldValue updated_at){
        progressDialog.dismiss();
        final Map<String,Object> vehicleDefectsResolution = new HashMap<>();
        vehicleDefectsResolution.put("inspection_id",inspectionId);
        vehicleDefectsResolution.put("vehicle_id",vehicle_id);
        vehicleDefectsResolution.put("element_id",elementId);
        vehicleDefectsResolution.put("element_name",element_name);
        vehicleDefectsResolution.put("vehicle_no",vehicle_no);
        vehicleDefectsResolution.put("wm_resolution",wm_resolution);
        vehicleDefectsResolution.put("driver_id",driver_id);
        vehicleDefectsResolution.put("element_type",element_type);
        vehicleDefectsResolution.put("close_issue",closeIssue);
        vehicleDefectsResolution.put("created_at",created_at);
        vehicleDefectsResolution.put("updated_at",updated_at);
        vehicleDefectsResolution.put("is_updated","Yes");
        vehicleDefectsResolution.put("id",inspection_id+"_"+id_count+++"_Vehicle");
        Query resolutionQuery = vehicleDefectsReferenceWM.whereEqualTo("inspection_id",inspection_id).whereEqualTo("element_type","vehicle").whereEqualTo("element_id",elementId);
        resolutionQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().isEmpty()) {
                        vehicleDefectsReferenceWM.document(inspection_id+"_"+doc_count+++"_Vehicle").set(vehicleDefectsResolution);
                        updateVehicleDefectTable(inspectionId,element_name,element_type,wm_resolution,closeIssue);
                    }else {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            if (documentSnapshot.exists()) {
                                String doc_id = documentSnapshot.getId();
                                Log.d(TAG,"CLOSE_ISSUE :" + wm_resolution + " " + closeIssue);
                                Map<String, Object> update = new HashMap<>();
                                update.put("wm_resolution", wm_resolution);
                                update.put("close_issue", closeIssue);
                                update.put("is_updated","Yes");
                                update.put("updated_at", FieldValue.serverTimestamp());
                                vehicleDefectsReferenceWM.document(doc_id).update(update);
                                updateVehicleDefectTable(inspectionId,element_name,element_type,wm_resolution,closeIssue);
                            }
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                viewPagerManagerDefects.disableScroll(true);
                Dialog.alertDialog(getActivity(),"Unable to submit resolution");
            }
        });
    }

    private void alertDialogFailed(String message){
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        fragment = new DashboardFragment();
                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.content_frame,fragment,"DASHBOARD");
                        ft.addToBackStack("DASHBOARD");
                        ft.commit();
                        tag = fragment.getTag();
                        Log.d(TAG,"CHILD_TAG" +fragment.getTag());
                        MainActivity mainActivity = (MainActivity) getActivity();
                        mainActivity.getChildTag(tag);
                    }
                });
        alertDialog.show();
    }

    private void updateVehicleDefectTable(String inspection_id, String element_name, String element_type, final String wm_comment, final String close_issue){
        Query updateWmComments = vehicleDefectsReference.whereEqualTo("inspection_id",inspection_id).whereEqualTo("element_name",element_name).whereEqualTo("element_type",element_type);
        updateWmComments.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        String doc_id = documentSnapshot.getId();
                        Log.d(TAG,"DOC :" +doc_id);
                        Map<String, Object> updateDefectsTable = new HashMap<>();
                        updateDefectsTable.put("workshop_manager_comment", wm_comment);
                        updateDefectsTable.put("is_updated","Yes");
                        updateDefectsTable.put("close_issue",close_issue);
                        vehicleDefectsReference.document(doc_id).update(updateDefectsTable);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Unable to update wm comments.");
            }
        });
    }
}
