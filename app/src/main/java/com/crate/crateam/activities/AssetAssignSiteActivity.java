package com.crate.crateam.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import com.crate.crateam.R;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.CustomSearchableSpinner;
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
import com.google.firebase.firestore.Source;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AssetAssignSiteActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private ImageView iv_cross;
    private Button bt_submit;
    private String user_name,doc_id="",site_location_name="";
    private int doc_id_length=0,site_location_id=0,user_id=0;
    private FirebaseFirestore db = FirestoreManager.getInstance();

    private Source source;
    private CollectionReference amAssignSiteReference,siteLocationReference,sortKeyTableReference,userDetailsReference;
    private PopupWindow popupWindow;
    private CustomSearchableSpinner sp_location;
    private ProgressDialog progressDialog;
    private ArrayList<Integer> arrayList_assign_site_ids = new ArrayList<>();
    private ArrayList<String> arrayList_site_name = new ArrayList<>();
    private ArrayList<Integer> all_site_id  = new ArrayList<>();
    private ArrayList<String> all_site_name  = new ArrayList<>();
    private ArrayList<Integer> all_site_id_sorted  = new ArrayList<>();
    private ArrayList<String> all_site_name_sorted  = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.am_assign_site_layout);
        FirestoreManager.initPersistentIndexManager();
        initView();
    }

    private void initView(){
        SessionManager sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_name = user.get(SessionManager.KEY_FULL_NAME);
        user_id = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        Log.d("USER_ID : " ,user_id +" "+user_name);
        iv_cross = findViewById(R.id.iv_cross);
        sp_location = findViewById(R.id.sp_location);
        bt_submit = findViewById(R.id.bt_submit);
        progressDialog = Dialog.showProgressDialog(this);
        userDetailsReference = db.collection("CR_user_details");
        amAssignSiteReference = db.collection("AM_assign_site");
        siteLocationReference = db.collection("AM_site_location");
        sortKeyTableReference = db.collection("CR_sort_key");
        siteUserMapping(user_id);
        initializeOnClick();
        getMaximumLength();
        Dialog.DismissProgressDialog(progressDialog,this);
    }

    private void initializeOnClick(){
        iv_cross.setOnClickListener(this);
        bt_submit.setOnClickListener(this);
        sp_location.setOnItemSelectedListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_cross:
                Intent toAMDashboard = new Intent(AssetAssignSiteActivity.this,AssetManagementDashboard.class);
                startActivity(toAMDashboard);
                finish();
                break;
            case R.id.bt_submit:
                sendAssignSiteData();
                if (!AppData.internetOnline(AssetAssignSiteActivity.this) && !site_location_name.equals(""))
                    toConfirmation("Site assigned successfully");
                break;
        }
    }

    public void siteUserMapping(int user_id){
        Query query = userDetailsReference.whereEqualTo("id",user_id).whereEqualTo("status","Active");
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    String siteIds = "";
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        siteIds = queryDocumentSnapshot.getString("site_ids");
                        if (siteIds != null && siteIds.contains(",")) {
                            ArrayList<String> siteIdListString = new ArrayList<String>(Arrays.asList(siteIds.split(",")));
                            for (int i=0;i<siteIdListString.size();i++) {
                                arrayList_assign_site_ids.add(Integer.valueOf(siteIdListString.get(i)));
                            }
                        }else {
                            if (siteIds != null) {
                                if (!siteIds.equals("")) {
                                    arrayList_assign_site_ids.add(Integer.valueOf(siteIds));
                                }
                            }
                        }
                        Log.d("site id list :" , String.valueOf(arrayList_assign_site_ids));
                        for (int i=0;i<arrayList_assign_site_ids.size();i++){
                            getSiteName(arrayList_assign_site_ids.get(i));
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetAssignSiteActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getSiteName(int id){
        Query query = siteLocationReference.whereEqualTo("id",id).whereEqualTo("status","Active")
                .orderBy("site_name");
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    arrayList_site_name.clear();
                    all_site_name_sorted.clear();
                    all_site_id_sorted.clear();
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot :task.getResult()){
                            all_site_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                            all_site_name.add(queryDocumentSnapshot.getString("site_name"));
                        }
                        all_site_name_sorted.addAll(all_site_name);
                        Collections.sort(all_site_name_sorted);
                        Log.d("HJGJ :" ,all_site_name_sorted+" "+all_site_name);
                        for (int i = 0;i<all_site_name_sorted.size();i++){
                            for (int j = 0;j<all_site_name.size();j++){
                                if (all_site_name_sorted.get(i).equals(all_site_name.get(j))){
                                    all_site_id_sorted.add(all_site_id.get(j));
                                    break;
                                }
                            }
                        }
                        Log.d("TRYRT XX:" ,all_site_name_sorted+" "+all_site_id_sorted);
                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(AssetAssignSiteActivity.this, R.layout.spinner_custom_layout, all_site_name_sorted);
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        sp_location.setAdapter(spinnerArrayAdapter);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetAssignSiteActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendAssignSiteData(){
        if (site_location_name.equalsIgnoreCase("Select Site") || site_location_name.equals(""))
            Dialog.alertDialog(AssetAssignSiteActivity.this,"Please select Site Name");
        else
            storeAssignSiteData(site_location_name,site_location_id,user_name,user_id);
    }


    private void storeAssignSiteData(String site_location_name,int site_location_id,String name,Integer user_id){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("ddMMyyyy");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = df.format(c);
        String conducted_on = formattedDate ;
        String send_current_date = simpleDateFormat.format(c);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HHmm", Locale.getDefault());
        SimpleDateFormat diesel_management = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String submission_time = dateFormatter.format(calendar.getTime());
        String send_submission_time = diesel_management.format(calendar.getTime());
        doc_id_length = doc_id_length+1;
        Map<String,Object> stringStringMap = new HashMap<>();
        stringStringMap.put("id",user_id+"_"+conducted_on+"_"+submission_time);
        stringStringMap.put("status","Active");
        stringStringMap.put("sort_key",doc_id_length);
        stringStringMap.put("site_name",site_location_name);
        stringStringMap.put("site_id",site_location_id);
        stringStringMap.put("name",name);
        stringStringMap.put("user_id",user_id);
        stringStringMap.put("date",send_current_date);
        stringStringMap.put("time",send_submission_time);
        progressDialog.show();
        amAssignSiteReference.document(user_id+"_"+conducted_on+"_"+submission_time).set(stringStringMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    toConfirmation("Site assigned successfully");
                    Map<String, Object> objectMap_update = new HashMap<>();
                    objectMap_update.put("am_assign_site_key", doc_id_length);
                    sortKeyTableReference.document(doc_id).update(objectMap_update);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AssetAssignSiteActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // sort key table
    private void getMaximumLength(){
        sortKeyTableReference.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if (queryDocumentSnapshot.contains("am_assign_site_key")){
                                doc_id_length = queryDocumentSnapshot.getLong("am_assign_site_key").intValue();
                                doc_id = queryDocumentSnapshot.getId();
                                break;
                            }
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetAssignSiteActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toConfirmation(String message){
        progressDialog.dismiss();
        final View popupView = getLayoutInflater().inflate(R.layout.change_pass_popup_layout,null,true);
        boolean isActivityInForeground = AssetAssignSiteActivity.this.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED);
        if (isActivityInForeground) {
            popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, true);
            popupWindow.showAtLocation(popupView, Gravity.BOTTOM, 0, 0);
        }
        Button cancelPopup = popupView.findViewById(R.id.bt_ok);
        TextView tv_change_pass = popupView.findViewById(R.id.tv_change_pass);
        tv_change_pass.setText(message);
        cancelPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                Intent toAMDashboard = new Intent(AssetAssignSiteActivity.this,AssetManagementDashboard.class);
                startActivity(toAMDashboard);
                finish();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_location:
                site_location_id = all_site_id_sorted.get(position);
                site_location_name = all_site_name_sorted.get(position);
                Log.d("TIUY :" ,site_location_name+" "+site_location_id);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
