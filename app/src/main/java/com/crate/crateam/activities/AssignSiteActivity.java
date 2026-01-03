package com.crate.crateam.activities;

import android.app.ProgressDialog;
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
import com.crate.crateam.R;
import com.crate.crateam.utility.AppData;
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
import com.crate.crateam.utility.CustomSearchableSpinner;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AssignSiteActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView iv_cross;
    private Button bt_submit;
    private String user_name,selected_site_name="",user_id="",project_no="",doc_id="";
    private int selected_site_id,doc_id_length=0;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference assignSiteReference,siteManagementReference,sortKeyTableReference;
    private PopupWindow popupWindow;
    private CustomSearchableSpinner sp_current_site;
    private ProgressDialog progressDialog;
    private ArrayList<String> siteList = new ArrayList<>();
    private ArrayList<Integer> siteId = new ArrayList<>();

    public static final String TAG = "AssignSite";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.assign_site_layout);
        FirestoreManager.initPersistentIndexManager();
        initView();
    }

    private void initView(){
        SessionManager sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_name = user.get(SessionManager.KEY_FULL_NAME);
        user_id = user.get(SessionManager.KEY_ID);
        Log.d("USER_ID : " ,user_name);
        iv_cross = findViewById(R.id.iv_cross);
        sp_current_site = findViewById(R.id.sp_current_site);
        bt_submit = findViewById(R.id.bt_submit);
        progressDialog = Dialog.showProgressDialog(this);
        assignSiteReference = db.collection("CR_assign_site");
        siteManagementReference = db.collection("CR_site_management");
        sortKeyTableReference = db.collection("CR_sort_key");
        getSiteName();
        sp_current_site.setTitle("Sites");
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_custom_layout,siteList);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        sp_current_site.setAdapter(spinnerArrayAdapter);
        sp_current_site.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_site_name = siteList.get(position).toString().trim();
                selected_site_id = siteId.get(position);
                getProjectNo(selected_site_id);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        initializeOnClick();
        getMaximumLength();
    }

    private void initializeOnClick(){
        iv_cross.setOnClickListener(this);
        bt_submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_cross:
                finish();
                break;
            case R.id.bt_submit:
                sendAssignSiteData();
                if (!AppData.internetOnline(AssignSiteActivity.this) && !selected_site_name.equals(""))
                    toConfirmation("Site assigned successfully");
                break;
        }
    }
    private void sendAssignSiteData(){
        if (selected_site_name.equalsIgnoreCase("Select Site") || selected_site_name.equals(""))
            Dialog.alertDialog(AssignSiteActivity.this,"Please select Site Name");
        else
            storeAssignSiteData(selected_site_name,selected_site_id,user_name,user_id,project_no);
    }
    private void storeAssignSiteData(String selected_site_name,int selected_site_id,String name,String user_id,String project_no){
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
        stringStringMap.put("site_name",selected_site_name);
        stringStringMap.put("site_id",selected_site_id);
        stringStringMap.put("name",name);
        stringStringMap.put("user_id",Integer.valueOf(user_id));
        stringStringMap.put("project_no",project_no);
        stringStringMap.put("date",send_current_date);
        stringStringMap.put("time",send_submission_time);
        stringStringMap.put("is_updated","Yes");
        progressDialog.setCancelable(false);
        progressDialog.show();
        assignSiteReference.document(user_id+"_"+conducted_on+"_"+submission_time).set(stringStringMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    toConfirmation("Site assigned successfully");
                    Map<String, Object> objectMap_update = new HashMap<>();
                    objectMap_update.put("assign_site_key", doc_id_length);
                    sortKeyTableReference.document(doc_id).update(objectMap_update);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AssignSiteActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // sort key table
    private void getMaximumLength(){
        sortKeyTableReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if (queryDocumentSnapshot.contains("assign_site_key")){
                                doc_id_length = queryDocumentSnapshot.getLong("assign_site_key").intValue();
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
                Toast.makeText(AssignSiteActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getSiteName(){
        Query query = siteManagementReference.whereEqualTo("status","Active").whereEqualTo("address_status","Active")
                .whereEqualTo("site_type","Collection");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if (task.isSuccessful()){
                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                    siteList.add(queryDocumentSnapshot.getString("site_name"));
                    siteId.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                }
            }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssignSiteActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getProjectNo(int site_id){
        Query query = siteManagementReference.whereEqualTo("id",site_id).whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                  for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()) {
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
                Toast.makeText(AssignSiteActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toConfirmation(String message){
        progressDialog.dismiss();
        final View popupView = getLayoutInflater().inflate(R.layout.change_pass_popup_layout,null,true);
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
                finish();
            }
        });
    }
}
