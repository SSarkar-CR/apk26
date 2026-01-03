package com.crate.crateam.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.provider.Settings;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crate.crateam.R;
import com.crate.crateam.adapters.AssetDetailsAdapter;
import com.crate.crateam.adapters.AssetElementsAdapter;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.CustomSearchableSpinner;
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
import com.google.firebase.firestore.Source;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class AssetDetails extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference assetDetailsReference,assetTypeReference,assetTypeElementReference,
            assetInspectionSubmissionReference, regimeDetailsReference,siteLocationReference;
    private Button bt_discard,bt_nfc,bt_qr_scan,bt_id_search,bt_view_certificates,bt_asset_history;
    private ImageView iv_cross;
    private TextView tv_asset_type,tv_asset_id,tv_asset_name,tv_asset_status,tv_asset_location,tv_inspected_by,tv_last_inspection,
            tv_nfc_tag,tv_thorough_exam_date,tv_current_asset_status;
    private LinearLayout ll_asset_details,ll_nfc_tag;
    private CustomSearchableSpinner sp_asset_type,sp_asset_details;
    private RecyclerView rv_asset_regimes,rv_asset_elements;
    private String asset_type="",asset_number="",asset_name="",tagId,identification_method="",
            user_name="",current_date="",current_time="",latitude="",longitude="",address="",
            last_inspection_id="", device_id ="",last_inspection_user_name="",last_inspection_status="",last_inspection_date="",qrCode="",
            activity="",asset_current_status="";
    private int assetId = 0,assetTypeId=0,user_id=0,regimeId=0,last_inspection_user_id=0,last_inspection_location=0,groupId=0;
    private NfcAdapter mNfcAdapter;
    private AssetElementsAdapter assetElementsAdapter;
    private AssetDetailsAdapter assetDetailsAdapter;
    private ArrayList<String> arraylist_asset_type_name = new ArrayList<>();
    private ArrayList<Integer> arraylist_asset_type_id = new ArrayList<>();
    private ArrayList<Integer> arrayList_regime_id  = new ArrayList<>();
    private ArrayList<Integer> arrayList_regime_element_id  = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_name = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_value = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_view = new ArrayList<>();
    private ArrayList<Integer> arrayList_regime_element_id_sorted  = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_name_sorted = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_value_sorted = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_view_sorted = new ArrayList<>();
    private ArrayList<Integer> arraylist_group_id = new ArrayList<>();
    private ArrayList<Integer> arraylist_asset_id = new ArrayList<>();
    private ArrayList<String> arraylist_asset_name = new ArrayList<>();
    private ArrayList<String> arraylist_asset_number = new ArrayList<>();
    private ArrayList<String> arraylist_asset_current_status = new ArrayList<>();
    private ArrayList<String> assetElementsNameList = new ArrayList<>();
    private ArrayList<Integer> assetElementsIdList = new ArrayList<>();
    private ArrayList<String> all_defects = new ArrayList<>();
    private ArrayList<String> all_asset_inspection_ids = new ArrayList<>();
    private ArrayList<Integer> defects_id = new ArrayList<>();
    private PopupWindow popupWindow;
    private static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "AssetDetails";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.asset_details);
        initView();
    }
    private void initView(){
        progressDialog = Dialog.showProgressDialog(this);
        FirestoreManager.initPersistentIndexManager();
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        assetDetailsReference = db.collection("AM_asset_details");
        assetTypeReference = db.collection("AM_asset_type");
        assetTypeElementReference = db.collection("AM_asset_type_elements");
        assetInspectionSubmissionReference = db.collection("AM_asset_inspection_submission");
        regimeDetailsReference = db.collection("AM_asset_revalidation_details");
        siteLocationReference = db.collection("AM_site_location");
        SessionManager sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_name = user.get(SessionManager.KEY_FULL_NAME);
        user_id = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            assetId = extras.getInt("assetId");
            activity = extras.getString("activity");
        }
        ll_asset_details = findViewById(R.id.ll_asset_details);
        tv_asset_type = findViewById(R.id.tv_asset_type);
        tv_asset_id = findViewById(R.id.tv_asset_id);
        tv_asset_name = findViewById(R.id.tv_asset_name);
        tv_asset_status = findViewById(R.id.tv_asset_status);
        tv_current_asset_status = findViewById(R.id.tv_current_asset_status);
        tv_asset_location = findViewById(R.id.tv_asset_location);
        tv_inspected_by = findViewById(R.id.tv_inspected_by);
        tv_last_inspection = findViewById(R.id.tv_last_inspection);
        tv_nfc_tag = findViewById(R.id.tv_nfc_tag);
        tv_thorough_exam_date = findViewById(R.id.tv_thorough_exam_date);
        bt_nfc = findViewById(R.id.bt_nfc);
        bt_id_search = findViewById(R.id.bt_id_search);
        bt_qr_scan = findViewById(R.id.bt_qr_scan);
        bt_discard = findViewById(R.id.bt_discard);
        bt_view_certificates = findViewById(R.id.bt_view_certificates);
        bt_asset_history = findViewById(R.id.bt_asset_history);
        ll_nfc_tag = findViewById(R.id.ll_nfc_tag);
        sp_asset_type = findViewById(R.id.sp_asset_type);
        sp_asset_details = findViewById(R.id.sp_asset_details);
        rv_asset_regimes = findViewById(R.id.rv_asset_regimes);
        rv_asset_elements = findViewById(R.id.rv_asset_elements);
        iv_cross = findViewById(R.id.iv_cross);
        if (activity.equals("DashboardListing")) {
            progressDialog.show();
            bt_nfc.setVisibility(View.GONE);
            bt_qr_scan.setVisibility(View.GONE);
            bt_id_search.setVisibility(View.GONE);
            getAssetDetailsFromDashboardListing(assetId);
        }else {
            bt_nfc.setVisibility(View.VISIBLE);
            bt_qr_scan.setVisibility(View.VISIBLE);
            bt_id_search.setVisibility(View.VISIBLE);
        }
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rv_asset_elements.setLayoutManager(layoutManager);
        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(this);
        rv_asset_regimes.setLayoutManager(layoutManager2);
        initializeOnClick();
        checkNfcAdapter();
        current_date = AppData.date();
        current_time = AppData.Time();
        latLon();
        device_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("Device Id:" ,device_id);
    }

    private void initializeOnClick(){
        bt_nfc.setOnClickListener(this);
        bt_id_search.setOnClickListener(this);
        bt_qr_scan.setOnClickListener(this);
        bt_discard.setOnClickListener(this);
        bt_view_certificates.setOnClickListener(this);
        bt_asset_history.setOnClickListener(this);
        iv_cross.setOnClickListener(this);
        sp_asset_type.setOnItemSelectedListener(this);
        sp_asset_details.setOnItemSelectedListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_nfc:
                sp_asset_type.setVisibility(View.GONE);
                sp_asset_details.setVisibility(View.GONE);
                ll_nfc_tag.setVisibility(View.VISIBLE);
                break;
            case R.id.bt_id_search:
                sp_asset_type.setVisibility(View.VISIBLE);
                sp_asset_details.setVisibility(View.VISIBLE);
                ll_nfc_tag.setVisibility(View.GONE);
                getAssetTypes();
                break;
            case R.id.bt_qr_scan:
                sp_asset_type.setVisibility(View.GONE);
                sp_asset_details.setVisibility(View.GONE);
                ll_nfc_tag.setVisibility(View.GONE);
                scanQR();
                break;
            case R.id.bt_view_certificates:
                Intent to_view_certificates = new Intent(AssetDetails.this,AssetCertificates.class);
                to_view_certificates.putExtra("asset_type",asset_type);
                to_view_certificates.putExtra("asset_id",asset_number);
                to_view_certificates.putExtra("asset_name",asset_name);
                to_view_certificates.putExtra("assetId",assetId);
                startActivity(to_view_certificates);
                break;
            case R.id.bt_asset_history:
                Intent to_asset_history = new Intent(AssetDetails.this,AssetHistory.class);
                to_asset_history.putExtra("asset_type",asset_type);
                to_asset_history.putExtra("asset_type_id",assetTypeId);
                to_asset_history.putExtra("asset_id",asset_number);
                to_asset_history.putExtra("asset_name",asset_name);
                to_asset_history.putExtra("assetId",assetId);
                startActivity(to_asset_history);
                break;
            case R.id.bt_discard:
            case R.id.iv_cross:
                finish();
                break;
        }
    }

    private void scanQR(){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setOrientationLocked(true);
        integrator.setPrompt(" ");
        integrator.initiateScan();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, dataIntent);
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("RDYFJF " ,intentResult.getContents());
                qrCode = intentResult.getContents();
                getAssetDetailsByQR();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, dataIntent);
        }
    }

    private void getAssetDetailsByQR(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = assetDetailsReference.whereEqualTo("status", "Active").whereEqualTo("qr_code",qrCode);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            assetId = Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
                            asset_name = queryDocumentSnapshot.getString("asset_name");
                            asset_number = queryDocumentSnapshot.getString("asset_number");
                            asset_current_status = queryDocumentSnapshot.getString("asset_current_status");
                            regimeId = Objects.requireNonNull(queryDocumentSnapshot.getLong("regime_id")).intValue();
                            assetTypeId = Objects.requireNonNull(queryDocumentSnapshot.getLong("asset_type_id")).intValue();
                        }
                        ll_asset_details.setVisibility(View.VISIBLE);
                        getAssetTypeName(assetTypeId);
                        getAssetElements(assetTypeId);
                        tv_asset_name.setText(asset_name);
                        tv_asset_id.setText(asset_number);
                        setAssetStatusColour(asset_current_status);
                        tv_current_asset_status.setText(asset_current_status);
                        getAssetDetailsFromAssetInspection(assetId);
                        getAssetRegimes(assetId);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetDetails.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAssetDetailsFromDashboardListing(int asset_id){
        Query query = assetDetailsReference.whereEqualTo("status", "Active").whereEqualTo("id",asset_id);
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            assetId = Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
                            asset_name = queryDocumentSnapshot.getString("asset_name");
                            asset_number = queryDocumentSnapshot.getString("asset_number");
                            regimeId = Objects.requireNonNull(queryDocumentSnapshot.getLong("regime_id")).intValue();
                            assetTypeId = Objects.requireNonNull(queryDocumentSnapshot.getLong("asset_type_id")).intValue();
                            asset_current_status = queryDocumentSnapshot.getString("asset_current_status");
                        }
                        ll_asset_details.setVisibility(View.VISIBLE);
                        getAssetTypeName(assetTypeId);
                        getAssetElements(assetTypeId);
                        tv_asset_name.setText(asset_name);
                        tv_asset_id.setText(asset_number);
                        setAssetStatusColour(asset_current_status);
                        tv_current_asset_status.setText(asset_current_status);
                        getAssetDetailsFromAssetInspection(assetId);
                        getAssetRegimes(assetId);
                    }
                    progressDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AssetDetails.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_asset_type:
                assetTypeId = arraylist_asset_type_id.get(position);
                getAssetDetailsByAssetTypeId(assetTypeId);
                getAssetTypeName(assetTypeId);
                break;
            case R.id.sp_asset_details:
                ll_asset_details.setVisibility(View.VISIBLE);
                regimeId = arrayList_regime_id.get(position);
                groupId = arraylist_group_id.get(position);
                assetId = arraylist_asset_id.get(position);
                asset_number = arraylist_asset_number.get(position);
                asset_name = arraylist_asset_name.get(position);
                asset_current_status = arraylist_asset_current_status.get(position);
                tv_asset_id.setText(asset_number);
                tv_asset_name.setText(asset_name);
                setAssetStatusColour(asset_current_status);
                tv_current_asset_status.setText(asset_current_status);
                Log.d("FTFUY :" , String.valueOf(assetId));
                getAssetDetailsFromAssetInspection(assetId);
                getAssetRegimes(assetId);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void setAssetStatusColour(String asset_current_status){
        if (asset_current_status.equals("Defected not safe to use."))
            tv_current_asset_status.setTextColor(Color.RED);
        else if (asset_current_status.equals("Defected but safe to use."))
            tv_current_asset_status.setTextColor(Color.parseColor("#FFA500"));
        else if (asset_current_status.equals("Good working order."))
            tv_current_asset_status.setTextColor(Color.GREEN);
    }



    private void latLon(){
        GPSTracker finder = new GPSTracker(AssetDetails.this);
        Double lat = 0.0, lon = 0.0;
        if (!finder.checkLocationPermission()) {
            finder.requestPermission();
        }
        if (finder.canGetLocation()) {
            lat = finder.getLatitude();
            lon = finder.getLongitude();
            double latOnline = round(lat, 4);
            double lonOnline = round(lon, 4);
            latitude = String.valueOf(latOnline);
            longitude = String.valueOf(lonOnline);
            address = finder.getAddress(lat,lon);
        }
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void getAssetTypes() {
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = assetTypeReference.whereEqualTo("status", "Active").orderBy("asset_type_name");
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    arraylist_asset_type_name.clear();
                    arraylist_asset_type_id.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        arraylist_asset_type_name.add(queryDocumentSnapshot.getString("asset_type_name"));
                        arraylist_asset_type_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                    }
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(AssetDetails.this, R.layout.spinner_custom_layout, arraylist_asset_type_name);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_asset_type.setAdapter(spinnerArrayAdapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetDetails.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAssetDetailsByAssetTypeId(int asset_type_id){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = assetDetailsReference.whereEqualTo("status", "Active").whereEqualTo("asset_type_id",asset_type_id)
                .orderBy("asset_name");
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    arraylist_asset_id.clear();
                    arraylist_asset_name.clear();
                    arraylist_asset_number.clear();
                    arraylist_asset_current_status.clear();
                    arraylist_group_id.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        arraylist_asset_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                        arraylist_asset_name.add(queryDocumentSnapshot.getString("asset_name"));
                        arraylist_asset_number.add(queryDocumentSnapshot.getString("asset_number"));
                        arraylist_asset_current_status.add(queryDocumentSnapshot.getString("asset_current_status"));
                        arrayList_regime_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("regime_id")).intValue());
                        arraylist_group_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("asset_group_id")).intValue());
                    }
                    ArrayList<String> combinedList = new ArrayList<>();
                    for (int i=0;i<arraylist_asset_id.size();i++){
                       combinedList.add(arraylist_asset_name.get(i)+" , "+arraylist_asset_number.get(i));
                    }
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(AssetDetails.this, R.layout.spinner_custom_layout, combinedList);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_asset_details.setAdapter(spinnerArrayAdapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetDetails.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAssetDetailsFromAssetInspection(int assetId){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = assetInspectionSubmissionReference.whereEqualTo("status", "Active").whereEqualTo("asset_id",assetId)
                .whereEqualTo("user_role","Other")
                .orderBy("time_second_format",Query.Direction.DESCENDING);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    all_asset_inspection_ids.clear();
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            all_asset_inspection_ids.add(queryDocumentSnapshot.getString("inspection_id"));
                        }
                        last_inspection_id = all_asset_inspection_ids.get(0);
                        Log.d("YTUTUY :" ,last_inspection_id+" "+all_asset_inspection_ids);
                        fetchLastInspectionDetails(last_inspection_id);
                    }else {
                        tv_last_inspection.setText("");
                        tv_asset_status.setText("");
                        tv_inspected_by.setText("");
                        tv_asset_location.setText("");
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetDetails.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchLastInspectionDetails(String inspection_id){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = assetInspectionSubmissionReference.whereEqualTo("status", "Active").whereEqualTo("inspection_id",inspection_id)
                .whereEqualTo("user_role","Other");
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            last_inspection_user_id = Objects.requireNonNull(queryDocumentSnapshot.getLong("user_id")).intValue();
                            if (Objects.requireNonNull(queryDocumentSnapshot.getString("inspector_name")).equals(""))
                                last_inspection_user_name = queryDocumentSnapshot.getString("user_name");
                            else
                                last_inspection_user_name = queryDocumentSnapshot.getString("inspector_name");
                            last_inspection_status = queryDocumentSnapshot.getString("asset_status");
                            last_inspection_location = queryDocumentSnapshot.getLong("assign_location").intValue();
                            last_inspection_date = queryDocumentSnapshot.getString("conducted_on");
                        }
                        Log.d("gyuguky :" ,last_inspection_location+" "+last_inspection_date);
                        tv_inspected_by.setText(last_inspection_user_name);
                        if (last_inspection_status.equals("Defected not safe to use."))
                            tv_asset_status.setTextColor(Color.RED);
                        else if (last_inspection_status.equals("Defected but safe to use."))
                            tv_asset_status.setTextColor(Color.parseColor("#FFA500"));
                        else if (last_inspection_status.equals("Good working order."))
                            tv_asset_status.setTextColor(Color.GREEN);
                        tv_asset_status.setText(last_inspection_status);
                        tv_last_inspection.setText(last_inspection_date);
                        getLastInspectionLocationName(last_inspection_location);
                    }

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetDetails.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLastInspectionLocationName(int site_id){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = siteLocationReference.whereEqualTo("id", site_id);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            String siteName= queryDocumentSnapshot.getString("site_name");
                            Log.d("SITE Name : " ,siteName);
                            tv_asset_location.setText(siteName);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetDetails.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAssetTypeName(int assetTypeId) {
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = assetTypeReference.whereEqualTo("status", "Active").whereEqualTo("id",assetTypeId);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        asset_type = queryDocumentSnapshot.getString("asset_type_name");
                        identification_method = queryDocumentSnapshot.getString("identification_method");
                    }
                    tv_asset_type.setText(asset_type);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetDetails.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAssetRegimes(int asset_details_id){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = regimeDetailsReference.whereEqualTo("status", "Active").whereEqualTo("asset_details_id",asset_details_id)
                .orderBy("time_second_format", Query.Direction.DESCENDING);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    arrayList_regime_element_id.clear();
                    arraylist_regime_element_name.clear();
                    arraylist_regime_element_value.clear();
                    arraylist_regime_element_view.clear();
                    arraylist_regime_element_name_sorted.clear();
                    arraylist_regime_element_view_sorted.clear();
                    arraylist_regime_element_value_sorted.clear();
                    arrayList_regime_element_id_sorted.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        arraylist_regime_element_name.add(queryDocumentSnapshot.getString("regime_element_name"));
                        arrayList_regime_element_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("regime_element_id")).intValue());
                        arraylist_regime_element_value.add(queryDocumentSnapshot.getString("regime_element_value"));
                        arraylist_regime_element_view.add(queryDocumentSnapshot.getString("regime_element_view"));
                    }
                    ArrayList<Integer> arrayList_regime_element_id_all = new ArrayList<>();
                    arrayList_regime_element_id_all.addAll(arrayList_regime_element_id);
                    HashSet<Integer> hashSet = new HashSet<Integer>();
                    hashSet.addAll(arrayList_regime_element_id);
                    arrayList_regime_element_id.clear();
                    arrayList_regime_element_id_sorted.addAll(hashSet);
                    for (int i = 0;i<arrayList_regime_element_id_sorted.size();i++){
                        for (int j = 0;j<arrayList_regime_element_id_all.size();j++){
                            if (arrayList_regime_element_id_sorted.get(i).equals(arrayList_regime_element_id_all.get(j))){
                                arraylist_regime_element_name_sorted.add(arraylist_regime_element_name.get(j));
                                arraylist_regime_element_value_sorted.add(arraylist_regime_element_value.get(j));
                                arraylist_regime_element_view_sorted.add(arraylist_regime_element_view.get(j));
                                break;
                            }
                        }
                    }
                    Log.d("TUITU :" ,arraylist_regime_element_name_sorted+"  "+arraylist_regime_element_value_sorted);
                    assetDetailsAdapter = new AssetDetailsAdapter(arraylist_regime_element_name_sorted,arraylist_regime_element_value_sorted);
                    rv_asset_regimes.setAdapter(assetDetailsAdapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetDetails.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAssetElements(int asset_type_id){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = assetTypeElementReference.whereEqualTo("status", "Active").whereEqualTo("asset_type_id",asset_type_id);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    assetElementsNameList.clear();
                    assetElementsIdList.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        assetElementsNameList.add(queryDocumentSnapshot.getString("element_name"));
                        assetElementsIdList.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                    }
                    assetElementsAdapter = new AssetElementsAdapter(assetElementsNameList,assetElementsIdList,defects_id
                            ,all_defects);
                    rv_asset_elements.setAdapter(assetElementsAdapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetDetails.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private  void checkNfcAdapter(){
        if (mNfcAdapter == null) {
            alertDialog("This device doesn't support NFC.");
        }
        if (mNfcAdapter != null) {
            if (!mNfcAdapter.isEnabled())
                alertDialog("NFC is disabled.Please enable NFC from your mobile settings.");
            else
                Toast.makeText(this, "NFC is Enabled.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        stopForegroundDispatch(this, mNfcAdapter);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }
    private void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, PendingIntent.FLAG_MUTABLE);
        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{ new String[] { Ndef.class.getName() }};
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        if (adapter != null)
            adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    private void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        if (adapter != null)
            adapter.disableForegroundDispatch(activity);
    }
    //     get new intent ...
    private void handleIntent(Intent intent)
    {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) ||NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                tagId = ByteArrayToHexString(Objects.requireNonNull(tag).getId());
                Log.d("NFC_SERIAL_NO :" ,tagId);
                ndefmessage(tag);
                Log.d("NDEF_Message", "Detected: " +ndefmessage(tag));
            } else {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                tagId = ByteArrayToHexString(Objects.requireNonNull(tag).getId());
                Log.d("NFC_SERIAL_NO :" ,tagId);
                getAssetDetailsByNFC();
            }
        }
    }

    private String ByteArrayToHexString(byte [] inarray) {
        int i, j, in;
        String [] hex = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
        String out= "";
        for(j = 0 ; j < inarray.length ; ++j)
        {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }
    //    // read tag data ...
    private String ndefmessage(Tag tag)
    {
        try {
            Ndef ndef = Ndef.get(tag);
            ndef.connect();
            NdefMessage ndefMessage = ndef.getNdefMessage();
            if (ndefMessage!= null) {
                getAssetDetailsByNFC();
                ndef.close();
            }
            else
                scanFailedDialog();
        } catch (IOException | FormatException e) {
            Log.e(TAG, "An unexpected error occurred", e);
        }
        return null;
    }

    private void getAssetDetailsByNFC(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = assetDetailsReference.whereEqualTo("status", "Active").whereEqualTo("nfc_code",tagId);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            assetId = Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
                            asset_name = queryDocumentSnapshot.getString("asset_name");
                            asset_number = queryDocumentSnapshot.getString("asset_number");
                            assetTypeId = Objects.requireNonNull(queryDocumentSnapshot.getLong("asset_type_id")).intValue();
                            asset_current_status = queryDocumentSnapshot.getString("asset_current_status");
                        }
                        successfullyScanDialog();
                        ll_asset_details.setVisibility(View.VISIBLE);
                        tv_nfc_tag.setText(tagId);
                        getAssetTypeName(assetTypeId);
                        tv_asset_name.setText(asset_name);
                        tv_asset_id.setText(asset_number);
                        tv_current_asset_status.setText(asset_current_status);
                        setAssetStatusColour(asset_current_status);
                        ll_nfc_tag.setBackgroundColor(Color.parseColor("#00CC66"));
                        getAssetDetailsFromAssetInspection(assetId);
                        getAssetRegimes(assetId);
                    }else {
                        onWrongTagDetection();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetDetails.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onWrongTagDetection(){
        Dialog.alertDialog(AssetDetails.this, "Tag not recognised. Please scan again.");
        tv_asset_type.setText("");
        tv_asset_id.setText("");
        tv_asset_name.setText("");
        ll_nfc_tag.setBackgroundColor(Color.parseColor("#FFFFFF"));
        ll_asset_details.setVisibility(View.GONE);
    }

    private void scanFailedDialog() {
        final View popupView = getLayoutInflater().inflate(R.layout.scan_failed_layout,null,true);
        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,true);
        popupWindow.showAtLocation(popupView, Gravity.BOTTOM,0,0);
        Button bt_cancel_popup = popupView.findViewById(R.id.bt_cancel_popup);
        Button bt_retry = popupView.findViewById(R.id.bt_retry);
        bt_cancel_popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
        bt_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
    }

    private void successfullyScanDialog() {
        final View popupView = getLayoutInflater().inflate(R.layout.scan_success_layout,null,true);
        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,true);
        popupWindow.showAtLocation(popupView, Gravity.BOTTOM,0,0);
        Button bt_done = popupView.findViewById(R.id.bt_done);
        bt_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
        Dialog.dialogTimer(popupWindow);
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void alertDialog(String message){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}