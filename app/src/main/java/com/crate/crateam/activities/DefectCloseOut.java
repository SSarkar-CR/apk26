package com.crate.crateam.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crate.crateam.R;
import com.crate.crateam.adapters.AssetElementsDefectsAdapter;
import com.crate.crateam.adapters.AssetRegimeDefectsAdapter;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.FirestoreManager;
import com.crate.crateam.utility.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheIndexManager;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DefectCloseOut extends AppCompatActivity implements View.OnClickListener {
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference assetDetailsReference, assetInspectionSubmissionReference,assetInspectionDefectsReference,
            regimeDetailsReference,assetInspectionRegimeReference,amAssignSiteReference,
            siteLocationReference;
    private Button bt_save,bt_back;
    private ImageView iv_refresh;
    private LinearLayout ll_safety_critical,ll_defected_element;
    private String asset_type="",asset_number="",asset_name="",current_date="",current_time="",latitude="",longitude="",address="",
            defect_regime="",defect_element="",defect_inspection ="",asset_status="",asset_regime_status="",asset_element_status="",
            device_id ="",last_inspection_user_name="",last_inspection_date="",
            maxDocIdRegimeDetails="",inspection_id="",conducted_on="",user_name="",current_user_name="",activity="",site_type="";
    private int assetId = 0,assetTypeId=0,user_id=0,regimeId=0, siteLocationId=0,last_inspection_location=0;
    private EditText et_value;
    private TextView tv_asset_type,tv_asset_id,tv_asset_name,tv_inspection_id,tv_asset_location,tv_inspected_by,tv_date,tv_value;
    private RecyclerView rv_asset_elements,rv_asset_regimes;
    private AssetElementsDefectsAdapter assetElementsDefectsAdapter;

    private AssetRegimeDefectsAdapter assetRegimeDefectsAdapter;
    private ArrayList<Integer> regimeElementsIds = new ArrayList<>();
    private ArrayList<String> arraylist_all_regime_element_name = new ArrayList<>();
    private ArrayList<Integer> arrayList_all_regime_element_id  = new ArrayList<>();
    private ArrayList<String> arraylist_all_regime_element_defect = new ArrayList<>();
    private ArrayList<Integer> arrayList_regime_element_id  = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_name = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_value = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_view = new ArrayList<>();
    private ArrayList<String> assetAllDefectedElementsNameList = new ArrayList<>();
    private ArrayList<Integer> assetAllDefectedElementsIdList = new ArrayList<>();
    private ArrayList<String> assetAllDefectedElementsDefectsList = new ArrayList<>();
    private ArrayList<String> assetDefectedElementsNameList = new ArrayList<>();
    private ArrayList<Integer> assetDefectedElementsIdList = new ArrayList<>();
    private ArrayList<String> assetDefectedElementsCommentList = new ArrayList<>();
    private PopupWindow popupWindow;
    private ProgressDialog progressDialog;
    public static final String TAG = "DefectClose";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.defect_close_out);
        initView();
        initializeOnClick();
    }
    private void initView(){
        progressDialog = Dialog.showProgressDialog(this);
        assetDetailsReference = db.collection("AM_asset_details");
        assetInspectionSubmissionReference = db.collection("AM_asset_inspection_submission");
        assetInspectionDefectsReference = db.collection("AM_asset_inspection_defects");
        regimeDetailsReference = db.collection("AM_asset_revalidation_details");
        assetInspectionRegimeReference = db.collection("AM_asset_inspection_regimes");
        amAssignSiteReference = db.collection("AM_assign_site");
        siteLocationReference = db.collection("AM_site_location");

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        SessionManager sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        current_user_name = user.get(SessionManager.KEY_FULL_NAME);
        user_id = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        Log.d(TAG,"USER_ID: " + user_id);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            asset_type= extras.getString("asset_type");
            asset_number= extras.getString("asset_id");
            asset_name= extras.getString("asset_name");
            assetId = extras.getInt("assetId");
            regimeId = extras.getInt("regime");
            inspection_id= extras.getString("inspection_id");
            conducted_on= extras.getString("conducted_on");
            user_name= extras.getString("user_name");
            activity = extras.getString("activity");
        }
        Log.d(TAG,"inspection id :" +inspection_id+" "+activity+" "+regimeId+" "+user_name);
        tv_asset_type = findViewById(R.id.tv_asset_type);
        tv_asset_id = findViewById(R.id.tv_asset_id);
        tv_asset_name = findViewById(R.id.tv_asset_name);
        tv_inspection_id = findViewById(R.id.tv_inspection_id);
        tv_asset_location = findViewById(R.id.tv_asset_location);
        tv_inspected_by  = findViewById(R.id.tv_inspected_by);
        tv_date = findViewById(R.id.tv_date);
        tv_asset_type.setText(asset_type);
        tv_asset_id.setText(asset_number);
        tv_asset_name.setText(asset_name);
        tv_inspection_id.setText(inspection_id);
        tv_inspected_by.setText(user_name);
        tv_date.setText(conducted_on);
        iv_refresh = findViewById(R.id.iv_refresh);
        bt_save = findViewById(R.id.bt_save);
        bt_back = findViewById(R.id.bt_back);
        ll_safety_critical = findViewById(R.id.ll_safety_critical);
        ll_defected_element = findViewById(R.id.ll_defected_element);
        rv_asset_elements = findViewById(R.id.rv_asset_elements);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rv_asset_elements.setLayoutManager(layoutManager);
        rv_asset_regimes = findViewById(R.id.rv_asset_regimes);
        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(this);
        rv_asset_regimes.setLayoutManager(layoutManager2);
        progressDialog.show();
        fetchInspectionDetails(inspection_id);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getDefectedRegimes(inspection_id);
            }
        }, 2000);
        current_date = AppData.date();
        current_time = AppData.Time();
        getAssignSiteName();
        getMaxDocIDRegimeDetails();
        getAllDefectedRegimes(assetId);
        getAllDefectedElements(assetId);
        Dialog.DismissProgressDialog(progressDialog,this);
        device_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG,"Device Id:" +device_id);
    }
    
    private void getAssignSiteName(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.DEFAULT;
        Query query = amAssignSiteReference.whereEqualTo("user_id", user_id).whereEqualTo("date", current_date);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            siteLocationId = Objects.requireNonNull(queryDocumentSnapshot.getLong("site_id")).intValue();
                        }
                        Log.d(TAG,"SITE ID : " + siteLocationId);
                        fetchStorageLocation(siteLocationId);
                    }
                    else
                        alertDialogAssignSite("You are not assigned with any site.Please assign yourself with a site.");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(DefectCloseOut.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchStorageLocation(int siteLocationId){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.DEFAULT;
        Query query = siteLocationReference.whereEqualTo("status", "Active").whereEqualTo("id",siteLocationId);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            site_type = queryDocumentSnapshot.getString("site_type");
                        }
                        Log.d(TAG,"TRDTR :" + site_type);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DefectCloseOut.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void alertDialogAssignSite(String message){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent assignSite = new Intent(DefectCloseOut.this,AssetAssignSiteActivity.class);
                        startActivity(assignSite);
                        finish();
                    }
                });
        alertDialog.show();
    }

    private void initializeOnClick(){
        iv_refresh.setOnClickListener(this);
        bt_save.setOnClickListener(this);
        bt_back.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_refresh:
                progressDialog.show();
                Dialog.DismissProgressDialog(progressDialog,this);
                getDefectedRegimes(inspection_id);
                getAllDefectedRegimes(assetId);
                updateAssetStatus(assetId);
                break;
            case R.id.bt_save:
                progressDialog.show();
                sendAssetInspectionData();
                Dialog.DismissProgressDialog(progressDialog,this);
                break;
            case R.id.bt_back:
                updateAssetStatus(assetId);
                Intent to_date_re_validation= new Intent();
                if (activity.equals("MyTask"))
                    to_date_re_validation = new Intent(DefectCloseOut.this, MyTask.class);
                else if (activity.equals("IssueManagement"))
                    to_date_re_validation = new Intent(DefectCloseOut.this, IssueManagement.class);
                to_date_re_validation.putExtra("asset_type",asset_type);
                to_date_re_validation.putExtra("asset_id",asset_number);
                to_date_re_validation.putExtra("asset_name",asset_name);
                to_date_re_validation.putExtra("assetId",assetId);
                to_date_re_validation.putExtra("regime",regimeId);
                startActivity(to_date_re_validation);
                finish();
                break;
        }
    }

    private void fetchInspectionDetails(String inspection_id){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.DEFAULT;
        Query query = assetInspectionSubmissionReference.whereEqualTo("status", "Active").whereEqualTo("inspection_id",inspection_id)
                .whereEqualTo("user_role","Other");
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            assetTypeId = Objects.requireNonNull(queryDocumentSnapshot.getLong("asset_type_id")).intValue();
                            if (Objects.requireNonNull(queryDocumentSnapshot.getString("inspector_name")).equals(""))
                                last_inspection_user_name = queryDocumentSnapshot.getString("user_name");
                            else
                                last_inspection_user_name = queryDocumentSnapshot.getString("inspector_name");
                            last_inspection_location = queryDocumentSnapshot.getLong("assign_location").intValue();
                            last_inspection_date = queryDocumentSnapshot.getString("conducted_on");
                        }
                        Log.d(TAG,"gyuguky :" + last_inspection_location + " " + last_inspection_date + " " + assetTypeId);
                        tv_inspected_by.setText(last_inspection_user_name);
                        tv_date.setText(last_inspection_date);
                        getLastInspectionLocationName(last_inspection_location);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DefectCloseOut.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLastInspectionLocationName(int site_id){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.DEFAULT;
        Query query = siteLocationReference.whereEqualTo("id", site_id);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            String siteName = queryDocumentSnapshot.getString("site_name");
                            Log.d(TAG,"SITE Name : " + siteName);
                            tv_asset_location.setText(siteName);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DefectCloseOut.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDefectedRegimes(String inspection_id){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.DEFAULT;
        Query query = assetInspectionRegimeReference.whereEqualTo("status", "Active").whereEqualTo("defected","Yes")
                .whereEqualTo("inspection_id",inspection_id);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG,"hguu :" + "online 1");
                    arraylist_regime_element_name.clear();
                    arrayList_regime_element_id.clear();
                    arraylist_regime_element_value.clear();
                    arraylist_regime_element_view.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        arraylist_regime_element_name.add(queryDocumentSnapshot.getString("regime_name"));
                        arrayList_regime_element_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("regime_id")).intValue());
                        arraylist_regime_element_value.add(queryDocumentSnapshot.getString("regime_value"));
                        arraylist_regime_element_view.add(queryDocumentSnapshot.getString("regime_view"));
                    }
                    Log.d(TAG,"TRTT :" + arrayList_regime_element_id + "  " + arraylist_regime_element_value +
                            " " + arraylist_regime_element_name + " " + arraylist_regime_element_view);
                    assetRegimeDefectsAdapter = new AssetRegimeDefectsAdapter(DefectCloseOut.this, arraylist_regime_element_name);
                    rv_asset_regimes.setAdapter(assetRegimeDefectsAdapter);
                    if (arraylist_regime_element_name.isEmpty()) {
                        ll_safety_critical.setVisibility(View.GONE);
                        defect_regime = "No";
                    } else {
                        ll_safety_critical.setVisibility(View.VISIBLE);
                        defect_regime = "Yes";
                    }
                    regimeAdapterClick();
                }
                getDefectedElements(inspection_id);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DefectCloseOut.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void regimeAdapterClick(){
        assetRegimeDefectsAdapter.setOnItemClickListener(new AssetRegimeDefectsAdapter.OnItemClickListener() {
            @Override
            public void onViewClick(int position) {
                Intent toCloseRegimeDefects = new Intent(DefectCloseOut.this, CloseRegimeDefects.class);
                toCloseRegimeDefects.putExtra("asset_type",asset_type);
                toCloseRegimeDefects.putExtra("asset_id",asset_number);
                toCloseRegimeDefects.putExtra("asset_type_id",assetTypeId);
                toCloseRegimeDefects.putExtra("asset_name",asset_name);
                toCloseRegimeDefects.putExtra("regime",regimeId);
                toCloseRegimeDefects.putExtra("inspection_id",inspection_id);
                toCloseRegimeDefects.putExtra("conducted_on",conducted_on);
                toCloseRegimeDefects.putExtra("user_name",user_name);
                toCloseRegimeDefects.putExtra("assetId",assetId);
                toCloseRegimeDefects.putExtra("regime_name",arraylist_regime_element_name.get(position));
                toCloseRegimeDefects.putExtra("regime_id",arrayList_regime_element_id.get(position));
                toCloseRegimeDefects.putExtra("regime_value",arraylist_regime_element_value.get(position));
                toCloseRegimeDefects.putExtra("regime_view",arraylist_regime_element_view.get(position));
                toCloseRegimeDefects.putExtra("activity",activity);
                finish();
                startActivity(toCloseRegimeDefects);
            }
        });
    }

    private void getDefectedElements(String inspection_id){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.DEFAULT;
        Query query = assetInspectionDefectsReference.whereEqualTo("status", "Active").whereNotEqualTo("defected","No")
                .whereEqualTo("inspection_id",inspection_id);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG,"hguu :" + "online 2");
                    assetDefectedElementsNameList.clear();
                    assetDefectedElementsIdList.clear();
                    assetDefectedElementsCommentList.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        assetDefectedElementsNameList.add(queryDocumentSnapshot.getString("element_name"));
                        assetDefectedElementsIdList.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("element_id")).intValue());
                        assetDefectedElementsCommentList.add(queryDocumentSnapshot.getString("element_defect"));
                    }
                    Log.d(TAG,"FHHI :" + assetDefectedElementsNameList + " " + assetDefectedElementsIdList + " " + assetDefectedElementsCommentList);
                    assetElementsDefectsAdapter = new AssetElementsDefectsAdapter(DefectCloseOut.this, assetDefectedElementsNameList);
                    rv_asset_elements.setAdapter(assetElementsDefectsAdapter);
                    if (assetDefectedElementsNameList.isEmpty()) {
                        defect_element = "No";
                        ll_defected_element.setVisibility(View.GONE);
                    } else {
                        defect_element = "Yes";
                        ll_defected_element.setVisibility(View.VISIBLE);
                    }
                    elementAdapterClick();
                }
                Log.d(TAG,"RYRU :" + defect_element + " " + defect_regime);
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (defect_element.equals("No") && defect_regime.equals("No")) {
                            bt_save.setVisibility(View.VISIBLE);
                            bt_back.setVisibility(View.GONE);
                        } else {
                            bt_save.setVisibility(View.GONE);
                            bt_back.setVisibility(View.VISIBLE);
                        }
                        progressDialog.dismiss();
                    }
                }, 1000);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(DefectCloseOut.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void elementAdapterClick(){
        assetElementsDefectsAdapter.setOnItemClickListener(new AssetElementsDefectsAdapter.OnItemClickListener() {
            @Override
            public void onViewClick(int position) {
                Intent to_date_re_validation = new Intent(DefectCloseOut.this, CloseElementDefects.class);
                to_date_re_validation.putExtra("asset_type",asset_type);
                to_date_re_validation.putExtra("asset_id",asset_number);
                to_date_re_validation.putExtra("asset_type_id",assetTypeId);
                to_date_re_validation.putExtra("asset_name",asset_name);
                to_date_re_validation.putExtra("regime",regimeId);
                to_date_re_validation.putExtra("inspection_id",inspection_id);
                to_date_re_validation.putExtra("conducted_on",conducted_on);
                to_date_re_validation.putExtra("user_name",user_name);
                to_date_re_validation.putExtra("assetId",assetId);
                to_date_re_validation.putExtra("element_name",assetDefectedElementsNameList.get(position));
                to_date_re_validation.putExtra("element_id",assetDefectedElementsIdList.get(position));
                to_date_re_validation.putExtra("element_defect",assetDefectedElementsCommentList.get(position));
                to_date_re_validation.putExtra("activity",activity);
                finish();
                startActivity(to_date_re_validation);
            }
        });
    }

    private void getMaxDocIDRegimeDetails(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.DEFAULT;
        regimeDetailsReference.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            regimeElementsIds.add(Integer.parseInt(queryDocumentSnapshot.getId()));
                        }
                        maxDocIdRegimeDetails = String.valueOf(getMax(regimeElementsIds));
                        Log.d(TAG,"max_regime_details_id :" + maxDocIdRegimeDetails);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DefectCloseOut.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getMax(ArrayList<Integer> list){
        int max = 0;
        for(int i=0; i<list.size(); i++){
            if(list.get(i) > max){
                max = list.get(i);
            }
        }
        return max;
    }

    private void getAllDefectedRegimes(int assetId){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.DEFAULT;
        Query query = assetInspectionRegimeReference.whereEqualTo("status", "Active").whereEqualTo("defected","Yes")
                .whereEqualTo("asset_id",assetId);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    arraylist_all_regime_element_name.clear();
                    arrayList_all_regime_element_id.clear();
                    arraylist_all_regime_element_defect.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        arraylist_all_regime_element_name.add(queryDocumentSnapshot.getString("regime_name"));
                        arrayList_all_regime_element_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("regime_id")).intValue());
                        arraylist_all_regime_element_defect.add(queryDocumentSnapshot.getString("defected"));
                    }
                    Log.d(TAG,"TRTT :" + arrayList_all_regime_element_id + "  " + arraylist_all_regime_element_defect);
                    if (arraylist_all_regime_element_defect.contains("Yes")) {
                        asset_regime_status = "Defected not safe to use.";
                    } else
                        asset_regime_status = "Good working order.";
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DefectCloseOut.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAllDefectedElements(int assetId){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.DEFAULT;
        Query query = assetInspectionDefectsReference.whereEqualTo("status", "Active").whereNotEqualTo("defected","No")
                .whereEqualTo("asset_id",assetId);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    assetAllDefectedElementsNameList.clear();
                    assetAllDefectedElementsIdList.clear();
                    assetAllDefectedElementsDefectsList.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        assetAllDefectedElementsNameList.add(queryDocumentSnapshot.getString("element_name"));
                        assetAllDefectedElementsIdList.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("element_id")).intValue());
                        assetAllDefectedElementsDefectsList.add(queryDocumentSnapshot.getString("defected"));
                    }
                    Log.d(TAG,"TDY:" + assetAllDefectedElementsIdList + " " + assetAllDefectedElementsDefectsList);
                    if (assetAllDefectedElementsDefectsList.contains("Yes")) {
                        asset_element_status = "Defected not safe to use.";
                    } else if (!assetAllDefectedElementsDefectsList.contains("Yes") && assetAllDefectedElementsDefectsList.contains("Yes but safe")) {
                        asset_element_status = "Defected but safe to use.";
                    } else
                        asset_element_status = "Good working order.";
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DefectCloseOut.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendAssetInspectionData(){
        if (defect_regime.equals("Yes") || defect_element.equals("Yes") ) {
            defect_inspection = "Yes";
        } else {
            defect_inspection = "No";
        }
        final Map<String,Object> AssetInspection = new HashMap<>();
        AssetInspection.put("user_id",user_id);
        AssetInspection.put("user_name",current_user_name);
        AssetInspection.put("asset_id",assetId);
        AssetInspection.put("asset_type_id",assetTypeId);
        AssetInspection.put("regime",regimeId);
        AssetInspection.put("asset_number",asset_number);
        AssetInspection.put("asset_name",asset_name);
        AssetInspection.put("assign_location", siteLocationId);
        AssetInspection.put("inspection_id",inspection_id);
        AssetInspection.put("user_role","WM");
        AssetInspection.put("conducted_on",current_date);
        AssetInspection.put("submission_time",current_time);
        AssetInspection.put("id",inspection_id+"_WM");
        AssetInspection.put("z_image_one","");
        AssetInspection.put("z_image_two","");
        AssetInspection.put("defected",defect_inspection);
        AssetInspection.put("defect_inspection",defect_inspection);
        AssetInspection.put("asset_status",asset_status);
        AssetInspection.put("inspector_name","");
        AssetInspection.put("z_inspector_sign","");
        AssetInspection.put("status","Active");
        AssetInspection.put("latitude",latitude);
        AssetInspection.put("longitude",longitude);
        AssetInspection.put("address",address);
        AssetInspection.put("wm_sign", "");
        AssetInspection.put("time_second_format", AppData.getTimeSecond());
        AssetInspection.put("device_id",device_id);
        AssetInspection.put("assign_user_status","Yes");
        AssetInspection.put("site_type",site_type);
        if (!AppData.internetOnline(this)) {
            assetInspectionSubmissionReference.document(inspection_id + "_WM").get(Source.CACHE).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        String docId = task.getResult().getId();
                        assetInspectionSubmissionReference.document(docId).update(AssetInspection);
                        updateInspectionDefect(inspection_id);
                        updateAssetStatus(assetId);
                        updateAssetDetails();
                        toConfirmation("Form submitted successfully.");
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(DefectCloseOut.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            assetInspectionSubmissionReference.document(inspection_id + "_WM").set(AssetInspection).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    updateInspectionDefect(inspection_id);
                    updateAssetStatus(assetId);
                    updateAssetDetails();
                    toConfirmation("Form submitted successfully.");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(DefectCloseOut.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateAssetStatus(int assetId){
        if (asset_regime_status.equals("Defected not safe to use.") || asset_element_status.equals("Defected not safe to use."))
            asset_status = "Defected not safe to use.";
        else if (asset_regime_status.equals("Good working order.") && asset_element_status.equals("Defected but safe to use."))
            asset_status = "Defected but safe to use.";
        else if (asset_regime_status.equals("Good working order.") && asset_element_status.equals("Good working order."))
            asset_status = "Good working order.";
        Log.d(TAG,"EEEE :" +asset_status);
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.DEFAULT;
        Query query = assetInspectionSubmissionReference.whereEqualTo("status", "Active").whereEqualTo("asset_id", assetId);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        String docId = queryDocumentSnapshot.getId();
                        Map<String, Object> objectMap_update = new HashMap<>();
                        objectMap_update.put("asset_status", asset_status);
                        assetInspectionSubmissionReference.document(docId).update(objectMap_update);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DefectCloseOut.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateInspectionDefect(String inspection_id){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.DEFAULT;
        Query query = assetInspectionSubmissionReference.whereEqualTo("inspection_id", inspection_id);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        String docId = queryDocumentSnapshot.getId();
                        Map<String, Object> objectMap_update = new HashMap<>();
                        objectMap_update.put("defected", defect_inspection);
                        objectMap_update.put("asset_status_wm", asset_status);
                        objectMap_update.put("assign_user_status", "Yes");
                        assetInspectionSubmissionReference.document(docId).update(objectMap_update);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DefectCloseOut.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAssetDetails(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.DEFAULT;
        Query query = assetDetailsReference.whereEqualTo("status", "Active").whereEqualTo("id",assetId);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            String doc_id = queryDocumentSnapshot.getId();
                            Map<String, Object> update_asset_details = new HashMap<>();
                            update_asset_details.put("asset_inspection_status", asset_status);
                            assetDetailsReference.document(doc_id).update(update_asset_details);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DefectCloseOut.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toConfirmation(String message) {
        progressDialog.dismiss();
        final View popupView = getLayoutInflater().inflate(R.layout.change_pass_popup_layout, null, true);
         boolean isActivityInForeground = DefectCloseOut.this.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED);
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
                Intent to_date_re_validation= new Intent();
                if (activity!=null) {
                    if (activity.equals("MyTask"))
                        to_date_re_validation = new Intent(DefectCloseOut.this, MyTask.class);
                    else if (activity.equals("IssueManagement"))
                        to_date_re_validation = new Intent(DefectCloseOut.this, IssueManagement.class);
                }
                to_date_re_validation.putExtra("asset_type",asset_type);
                to_date_re_validation.putExtra("asset_id",asset_number);
                to_date_re_validation.putExtra("asset_name",asset_name);
                to_date_re_validation.putExtra("assetId",assetId);
                startActivity(to_date_re_validation);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

}