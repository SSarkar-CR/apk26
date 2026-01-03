package com.crate.crateam.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.crate.crateam.R;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.CustomSearchableSpinner;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.FirestoreManager;
import com.crate.crateam.utility.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateAssetForm extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener,DatePickerDialog.OnDateSetListener {

    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference assetTypeReference, assetRegimeReference,assetRegimeInspectionElementDetailsReference,
            assetInspectionElementsReference,assetCustomerDetailsReference, assetDetailsReference,vehicleManufacturerReference,vehicleModelReference,
            regimeDetailsReference,amAssignSiteReference,assetNfcDetailsReference,assetQRDetailsReference;
    private ImageView iv_cross,iv_nfcTag_one,iv_nfcTag_two;
    private EditText et_asset_name, et_asset_uid,et_service_due_hours_1,et_service_due_hours_2,et_service_due_hours_3,et_service_due_hours_4,
            et_service_due_hours_5,et_service_due_hours_6,et_service_due_hours_7,et_service_due_hours_8,et_service_due_hours_9,et_service_due_hours_10;
    private TextView tv_regime,tv_location,tv_tag,tv_tag_confirmation,tv_regime_element_name_1,tv_regime_element_name_2,tv_regime_element_name_3,
            tv_regime_element_name_4,tv_regime_element_name_5,tv_regime_element_name_6,tv_regime_element_name_7,tv_regime_element_name_8,tv_regime_element_name_9,tv_regime_element_name_10,
            tv_date_picker_1,tv_date_picker_2,tv_date_picker_3,tv_date_picker_4, tv_date_picker_5,tv_date_picker_6,tv_date_picker_7,tv_date_picker_8,tv_date_picker_9,tv_date_picker_10,
            qr_value,tv_owner;
    private Button bt_scan_qr,bt_discard,bt_submit, bt_owned, bt_hired;
    private RecyclerView rv_asset_elements;
    private RadioGroup rg_yes_no_1,rg_yes_no_2,rg_yes_no_3,rg_yes_no_4,rg_yes_no_5,rg_yes_no_6,rg_yes_no_7,rg_yes_no_8,rg_yes_no_9,rg_yes_no_10,
            rg_on_off_1,rg_on_off_2,rg_on_off_3,rg_on_off_4, rg_on_off_5,rg_on_off_6,rg_on_off_7,rg_on_off_8,rg_on_off_9,rg_on_off_10;
    private CustomSearchableSpinner sp_asset_type, sp_manufacturer, sp_model, sp_owner;
    private LinearLayout ll_tag_one,ll_tag_two,ll_uid,ll_qr,ll_regime_element_1,ll_regime_element_2,ll_regime_element_3,ll_regime_element_4,ll_regime_element_5,
            ll_regime_element_6,ll_regime_element_7,ll_regime_element_8,ll_regime_element_9,ll_regime_element_10,ll_owner;
    private RelativeLayout rl_date_picker_1,rl_date_picker_2,rl_date_picker_3,rl_date_picker_4,rl_date_picker_5,rl_date_picker_6,
            rl_date_picker_7,rl_date_picker_8,rl_date_picker_9,rl_date_picker_10;
    private ProgressDialog progressDialog;
    private ArrayList<String> arrayList_asset_type_name  = new ArrayList<>();
    private ArrayList<Integer> arrayList_asset_type_id  = new ArrayList<>();
    private ArrayList<Integer> arrayList_regime_element_id  = new ArrayList<>();
    private ArrayList<String> arrayList_regime_element_value  = new ArrayList<>();
    private ArrayList<String> arrayList_regime_element_view  = new ArrayList<>();
    private ArrayList<Integer> arrayList_inspection_element_id = new ArrayList<>();
    private ArrayList<String> arrayList_inspection_element_name  = new ArrayList<>();
    private ArrayList<String> arrayList_date_validation  = new ArrayList<>();
    private ArrayList<String> arrayList_hour_validation  = new ArrayList<>();
    private ArrayList<String> arrayList_on_off_validation  = new ArrayList<>();
    private ArrayList<String> arrayList_yes_no_validation  = new ArrayList<>();
    private ArrayList<String> arrayList_text_validation  = new ArrayList<>();
    private ArrayList<String> arrayList_selected_inspection_element_name  = new ArrayList<>();
    private ArrayList<String> arrayList_selected_date_validation  = new ArrayList<>();
    private ArrayList<String> arrayList_selected_hour_validation  = new ArrayList<>();
    private ArrayList<String> arrayList_selected_on_off_validation  = new ArrayList<>();
    private ArrayList<String> arrayList_selected_yes_no_validation  = new ArrayList<>();
    private ArrayList<String> arrayList_selected_text_validation  = new ArrayList<>();
    private ArrayList<Integer> arrayList_regime_id  = new ArrayList<>();
    private ArrayList<String> arrayList_identification_method  = new ArrayList<>();
    private ArrayList<String> arrayList_group_id  = new ArrayList<>();
    private ArrayList<String> arrayList_manufacturer_name  = new ArrayList<>();
    private ArrayList<String> arrayList_model_name  = new ArrayList<>();
    private ArrayList<String> arrayList_owner_name  = new ArrayList<>();
    private ArrayList<Integer> arrayList_owner_id  = new ArrayList<>();
    private ArrayList<Integer> assetElementsIds = new ArrayList<>();
    private ArrayList<Integer> regimeElementsIds = new ArrayList<>();
    private ArrayList<Integer> nfcIds = new ArrayList<>();
    private ArrayList<String> nfc_tags  = new ArrayList<>();
    private ArrayList<Integer> qrIds = new ArrayList<>();
    private ArrayList<String> qrValues  = new ArrayList<>();
    private ArrayList<Long> regimeDateFormat  = new ArrayList<>();
    private String userName="",assetTypeName="",regimeName="",identificationMethod="",tagId,manufacturer="",model="",assetName="",assetUID="",
            datePickerOneValue="",datePickerTwoValue="", datePickerThreeValue="",datePickerFourValue="",datePickerFiveValue="",datePickerSixValue="",datePickerSevenValue="",datePickerEightValue="",datePickerNineValue="",datePickerTenValue="",
            rgYesNoOneValue="",rgYesNoTwoValue="", rgYesNoThreeValue="",rgYesNoFourValue="",rgYesNoFiveValue="",rgYesNoSixValue="", rgYesNoSevenValue="",rgYesNoEightValue="",rgYesNoNineValue="",rgYesNoTenValue="",
            rgOnOffOneValue="", rgOnOffTwoValue="",rgOnOffThreeValue="",rgOnOffFourValue="",rgOnOffFiveValue="",rgOnOffSixValue="",rgOnOffSevenValue="",rgOnOffEightValue="",rgOnOffNineValue="",rgOnOffTenValue="",
            regimeElementFirstValue="",regimeElementSecondValue="", regimeElementThirdValue="",regimeElementFourthValue="",regimeElementFiveValue="",regimeElementSixValue="",regimeElementSevenValue="",regimeElementEightValue="",regimeElementNineValue="",regimeElementTenValue="",
            maxDocIdAssetDetails="",maxDocIdRegimeDetails="", currentDate="",currentTime="",device_id ="",ownership="",groupId="",siteLocationName="",maxDocIdNFCDetails="",view_type_one="",view_type_two="",
            view_type_three="",view_type_four="",view_type_five="",view_type_six="",view_type_seven="",view_type_eight="",view_type_nine="",view_type_ten="",qrCode="",maxDocIdQRDetails="",routine_inspection_duration="",
            start_routine_inspection_date="",next_routine_inspection_date="",next_through_inspection_date="";
    private int userId=0,selectedRegimeId = 0,selectedRegimeElementRow=0,assetTypeId = 0,owner=0,siteLocationId=0,assetDetailsId=0,nfcDetailsId=0,qrDetailsId=0;
    private long regime_date_format_one=0,regime_date_format_two=0,regime_date_format_three=0,regime_date_format_four=0,regime_date_format_five=0,regime_date_format_six=0,regime_date_format_seven=0,regime_date_format_eight=0,regime_date_format_nine=0,regime_date_format_ten=0;
    private boolean isValidationDone = false,nfc_tags_exist = false,qr_exist=false;
    private DatePickerDialog datePickerDialog;
    private NfcAdapter mNfcAdapter;
    public static final String MIME_TEXT_PLAIN = "text/plain";

    public static final String TAG = "CreateAsset";
    private PopupWindow popupWindow;
    private PopupWindow readyToScan;
    InputFilter filter = new InputFilter() {
        public CharSequence filter(CharSequence source, int start,
                                   int end, Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!Character.toString(source.charAt(i)).matches("[a-zA-Z0-9., ]+")) {
                    return "";
                }
            }
            return null;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_asset_form);
        initView();
    }

    @SuppressLint("HardwareIds")
    private void initView(){
        FirestoreManager.initPersistentIndexManager();
        FirestoreManager.getInstance();
        progressDialog = Dialog.showProgressDialog(this);
        assetTypeReference = db.collection("AM_asset_type");
        assetRegimeReference = db.collection("AM_regime");
        assetRegimeInspectionElementDetailsReference = db.collection("AM_regime_inspection_elements_details");
        assetInspectionElementsReference = db.collection("AM_inspection_elements");
        assetCustomerDetailsReference = db.collection("AM_customer_details");
        assetDetailsReference = db.collection("AM_asset_details");
        vehicleManufacturerReference = db.collection("AM_vehicle_manufacturers");
        vehicleModelReference = db.collection("AM_vehicle_models");
        regimeDetailsReference = db.collection("AM_asset_revalidation_details");
        amAssignSiteReference = db.collection("AM_assign_site");
        assetNfcDetailsReference = db.collection("AM_NFC_details");
        assetQRDetailsReference = db.collection("AM_QR_details");
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        SessionManager sessionManager = new SessionManager(this);
        FirestoreManager firestoreManager ;
        HashMap<String, String> user = sessionManager.getUserDetails();
        userName = user.get(SessionManager.KEY_FULL_NAME);
        userId = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        Log.d("USER_ID: " , String.valueOf(userId));
        tv_regime = findViewById(R.id.tv_regime);
        tv_location = findViewById(R.id.tv_location);
        tv_tag = findViewById(R.id.tv_tag);
        qr_value = findViewById(R.id.qr_value);
        tv_tag_confirmation = findViewById(R.id.tv_tag_confirmation);
        tv_regime_element_name_1 = findViewById(R.id.tv_regime_element_name_1);
        tv_regime_element_name_2 = findViewById(R.id.tv_regime_element_name_2);
        tv_regime_element_name_3 = findViewById(R.id.tv_regime_element_name_3);
        tv_regime_element_name_4 = findViewById(R.id.tv_regime_element_name_4);
        tv_regime_element_name_5 = findViewById(R.id.tv_regime_element_name_5);
        tv_regime_element_name_6 = findViewById(R.id.tv_regime_element_name_6);
        tv_regime_element_name_7 = findViewById(R.id.tv_regime_element_name_7);
        tv_regime_element_name_8 = findViewById(R.id.tv_regime_element_name_8);
        tv_regime_element_name_9 = findViewById(R.id.tv_regime_element_name_9);
        tv_regime_element_name_10 = findViewById(R.id.tv_regime_element_name_10);
        tv_date_picker_1 = findViewById(R.id.tv_date_picker_1);
        tv_date_picker_2 = findViewById(R.id.tv_date_picker_2);
        tv_date_picker_3 = findViewById(R.id.tv_date_picker_3);
        tv_date_picker_4 = findViewById(R.id.tv_date_picker_4);
        tv_date_picker_5 = findViewById(R.id.tv_date_picker_5);
        tv_date_picker_6 = findViewById(R.id.tv_date_picker_6);
        tv_date_picker_7 = findViewById(R.id.tv_date_picker_7);
        tv_date_picker_8 = findViewById(R.id.tv_date_picker_8);
        tv_date_picker_9 = findViewById(R.id.tv_date_picker_9);
        tv_date_picker_10 = findViewById(R.id.tv_date_picker_10);
        tv_owner = findViewById(R.id.tv_owner);
        rv_asset_elements = findViewById(R.id.rv_asset_elements);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rv_asset_elements.setLayoutManager(layoutManager);
        datePickerDialog = new DatePickerDialog(
                CreateAssetForm.this, CreateAssetForm.this, Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DATE));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        iv_cross = findViewById(R.id.iv_cross);
        iv_nfcTag_one = findViewById(R.id.iv_nfcTag_one);
        iv_nfcTag_two = findViewById(R.id.iv_nfcTag_two);
        et_asset_name = findViewById(R.id.et_asset_name);
        et_asset_uid = findViewById(R.id.et_asset_uid);
        et_service_due_hours_1 = findViewById(R.id.et_service_due_hours_1);
        et_service_due_hours_2 = findViewById(R.id.et_service_due_hours_2);
        et_service_due_hours_3 = findViewById(R.id.et_service_due_hours_3);
        et_service_due_hours_4 = findViewById(R.id.et_service_due_hours_4);
        et_service_due_hours_5 = findViewById(R.id.et_service_due_hours_5);
        et_service_due_hours_6 = findViewById(R.id.et_service_due_hours_6);
        et_service_due_hours_7 = findViewById(R.id.et_service_due_hours_7);
        et_service_due_hours_8 = findViewById(R.id.et_service_due_hours_8);
        et_service_due_hours_9 = findViewById(R.id.et_service_due_hours_9);
        et_service_due_hours_10 = findViewById(R.id.et_service_due_hours_10);
        et_asset_name.setFilters(new InputFilter[] { filter });
        et_asset_uid.setFilters(new InputFilter[] { filter ,new InputFilter.AllCaps()});
        et_service_due_hours_1.setFilters(new InputFilter[] { filter });
        et_service_due_hours_2.setFilters(new InputFilter[] { filter });
        et_service_due_hours_3.setFilters(new InputFilter[] { filter });
        et_service_due_hours_4.setFilters(new InputFilter[] { filter });
        et_service_due_hours_5.setFilters(new InputFilter[] { filter });
        et_service_due_hours_6.setFilters(new InputFilter[] { filter });
        et_service_due_hours_7.setFilters(new InputFilter[] { filter });
        et_service_due_hours_8.setFilters(new InputFilter[] { filter });
        et_service_due_hours_9.setFilters(new InputFilter[] { filter });
        et_service_due_hours_10.setFilters(new InputFilter[] { filter });
        rg_yes_no_1 = findViewById(R.id.rg_yes_no_1);
        rg_yes_no_2 = findViewById(R.id.rg_yes_no_2);
        rg_yes_no_3 = findViewById(R.id.rg_yes_no_3);
        rg_yes_no_4 = findViewById(R.id.rg_yes_no_4);
        rg_yes_no_5 = findViewById(R.id.rg_yes_no_5);
        rg_yes_no_6 = findViewById(R.id.rg_yes_no_6);
        rg_yes_no_7 = findViewById(R.id.rg_yes_no_7);
        rg_yes_no_8 = findViewById(R.id.rg_yes_no_8);
        rg_yes_no_9 = findViewById(R.id.rg_yes_no_9);
        rg_yes_no_10 = findViewById(R.id.rg_yes_no_10);
        rg_on_off_1 = findViewById(R.id.rg_on_off_1);
        rg_on_off_2 = findViewById(R.id.rg_on_off_2);
        rg_on_off_3 = findViewById(R.id.rg_on_off_3);
        rg_on_off_4 = findViewById(R.id.rg_on_off_4);
        rg_on_off_5 = findViewById(R.id.rg_on_off_5);
        rg_on_off_6 = findViewById(R.id.rg_on_off_6);
        rg_on_off_7 = findViewById(R.id.rg_on_off_7);
        rg_on_off_8 = findViewById(R.id.rg_on_off_8);
        rg_on_off_9 = findViewById(R.id.rg_on_off_9);
        rg_on_off_10 = findViewById(R.id.rg_on_off_10);
        sp_asset_type = findViewById(R.id.sp_asset_type);
        sp_manufacturer = findViewById(R.id.sp_manufacturer);
        sp_model = findViewById(R.id.sp_model);
        sp_owner = findViewById(R.id.sp_owner);
        ll_tag_one = findViewById(R.id.ll_tag_one);
        ll_tag_two = findViewById(R.id.ll_tag_two);
        ll_uid = findViewById(R.id.ll_uid);
        ll_qr = findViewById(R.id.ll_qr);
        ll_owner = findViewById(R.id.ll_owner);
        ll_regime_element_1 = findViewById(R.id.ll_regime_element_1);
        ll_regime_element_2 = findViewById(R.id.ll_regime_element_2);
        ll_regime_element_3 = findViewById(R.id.ll_regime_element_3);
        ll_regime_element_4 = findViewById(R.id.ll_regime_element_4);
        ll_regime_element_5 = findViewById(R.id.ll_regime_element_5);
        ll_regime_element_6 = findViewById(R.id.ll_regime_element_6);
        ll_regime_element_7 = findViewById(R.id.ll_regime_element_7);
        ll_regime_element_8 = findViewById(R.id.ll_regime_element_8);
        ll_regime_element_9 = findViewById(R.id.ll_regime_element_9);
        ll_regime_element_10 = findViewById(R.id.ll_regime_element_10);
        rl_date_picker_1 = findViewById(R.id.rl_date_picker_1);
        rl_date_picker_2 = findViewById(R.id.rl_date_picker_2);
        rl_date_picker_3 = findViewById(R.id.rl_date_picker_3);
        rl_date_picker_4 = findViewById(R.id.rl_date_picker_4);
        rl_date_picker_5 = findViewById(R.id.rl_date_picker_5);
        rl_date_picker_6 = findViewById(R.id.rl_date_picker_6);
        rl_date_picker_7 = findViewById(R.id.rl_date_picker_7);
        rl_date_picker_8 = findViewById(R.id.rl_date_picker_8);
        rl_date_picker_9 = findViewById(R.id.rl_date_picker_9);
        rl_date_picker_10 = findViewById(R.id.rl_date_picker_10);
        bt_owned = findViewById(R.id.bt_owned);
        bt_hired = findViewById(R.id.bt_hired);
        bt_discard = findViewById(R.id.bt_discard);
        bt_scan_qr = findViewById(R.id.bt_scan_qr);
        bt_submit = findViewById(R.id.bt_submit);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        currentDate = AppData.date();
        currentTime = AppData.Time();
        initializeOnClick();
        getAssetType();
        getAssignSiteName();
        getManufacturer();
        getModel();
        checkNfcAdapter();
        getMaxDocIDAssetDetails();
        getMaxDocIDRegimeDetails();
        getMaxDocIDNFCDetails();
        getMaxDocIQRDetails();
        device_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("Device Id:" ,device_id);
    }

    private void initializeOnClick() {
        iv_cross.setOnClickListener(this);
        sp_asset_type.setOnItemSelectedListener(this);
        sp_manufacturer.setOnItemSelectedListener(this);
        sp_model.setOnItemSelectedListener(this);
        sp_owner.setOnItemSelectedListener(this);
        bt_owned.setOnClickListener(this);
        bt_hired.setOnClickListener(this);
        bt_scan_qr.setOnClickListener(this);
        bt_discard.setOnClickListener(this);
        bt_submit.setOnClickListener(this);
        rl_date_picker_1.setOnClickListener(this);
        rl_date_picker_2.setOnClickListener(this);
        rl_date_picker_3.setOnClickListener(this);
        rl_date_picker_4.setOnClickListener(this);
        rl_date_picker_5.setOnClickListener(this);
        rl_date_picker_6.setOnClickListener(this);
        rl_date_picker_7.setOnClickListener(this);
        rl_date_picker_8.setOnClickListener(this);
        rl_date_picker_9.setOnClickListener(this);
        rl_date_picker_10.setOnClickListener(this);
    }

    private void getAssetType() {
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = assetTypeReference.whereEqualTo("status", "Active").orderBy("asset_type_name");
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        arrayList_asset_type_name.add(queryDocumentSnapshot.getString("asset_type_name"));
                        arrayList_asset_type_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                        arrayList_regime_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("regime_id")).intValue());
                        arrayList_group_id.add(queryDocumentSnapshot.getString("asset_category_name"));
                        arrayList_identification_method.add(queryDocumentSnapshot.getString("identification_method"));
                    }
                    sp_asset_type.setTitle("Asset Type");
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(CreateAssetForm.this, R.layout.spinner_custom_layout, arrayList_asset_type_name);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_asset_type.setAdapter(spinnerArrayAdapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateAssetForm.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getRegimeName(int regime_id) {
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = assetRegimeReference.whereEqualTo("status", "Active").whereEqualTo("id",regime_id);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        regimeName = queryDocumentSnapshot.getString("regime_name");
                        routine_inspection_duration = queryDocumentSnapshot.getString("inspection_duration");
                        start_routine_inspection_date = queryDocumentSnapshot.getString("insert_date");
                    }
                    nextRoutineInspectionDate(currentDate,routine_inspection_duration);
                    tv_regime.setText(regimeName);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateAssetForm.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void nextRoutineInspectionDate(String current_date,String routine_inspection_duration){
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date = null;
        try {
            date = formatter.parse(current_date);
        } catch (ParseException e) {
            Log.e(TAG, "An unexpected error occurred", e);
        }
        String dater = formatter.format(date);
        int duration = Integer.parseInt(routine_inspection_duration);
        Date dateNew = new Date(date.getYear(), date.getMonth(), date.getDate() +duration);
        next_routine_inspection_date = formatter.format(dateNew);
        Log.d("TRTYYJT :" ,formatter.format(dateNew));
    }

    private void getInspectionElements(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = assetInspectionElementsReference.whereEqualTo("status", "Active").whereEqualTo("category","Child");
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    arrayList_inspection_element_id.clear();
                    arrayList_inspection_element_name.clear();
                    arrayList_date_validation.clear();
                    arrayList_hour_validation.clear();
                    arrayList_on_off_validation.clear();
                    arrayList_yes_no_validation.clear();
                    arrayList_text_validation.clear();
                    arrayList_selected_inspection_element_name.clear();
                    arrayList_selected_date_validation.clear();
                    arrayList_selected_hour_validation.clear();
                    arrayList_selected_on_off_validation.clear();
                    arrayList_selected_yes_no_validation.clear();
                    arrayList_selected_text_validation.clear();
//                    arrayList_regime_element_id.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        arrayList_inspection_element_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                        arrayList_inspection_element_name.add(queryDocumentSnapshot.getString("inspection_element_name"));
                        arrayList_date_validation.add(queryDocumentSnapshot.getString("date_validation"));
                        arrayList_hour_validation.add(queryDocumentSnapshot.getString("hours_validation"));
                        arrayList_on_off_validation.add(queryDocumentSnapshot.getString("on_off_validation"));
                        arrayList_yes_no_validation.add(queryDocumentSnapshot.getString("yes_no_validation"));
                        arrayList_text_validation.add(queryDocumentSnapshot.getString("text_validation"));
                    }
                    for (int i = 0;i<arrayList_regime_element_id.size();i++){
                        for (int j = 0;j<arrayList_inspection_element_id.size();j++){
                            if (arrayList_regime_element_id.get(i).equals(arrayList_inspection_element_id.get(j))){
                                arrayList_selected_inspection_element_name.add(arrayList_inspection_element_name.get(j));
                                arrayList_selected_date_validation.add(arrayList_date_validation.get(j));
                                arrayList_selected_hour_validation.add(arrayList_hour_validation.get(j));
                                arrayList_selected_on_off_validation.add(arrayList_on_off_validation.get(j));
                                arrayList_selected_yes_no_validation.add(arrayList_yes_no_validation.get(j));
                                arrayList_selected_text_validation.add(arrayList_text_validation.get(j));
                                break;
                            }
                        }
                    }
                    Log.d("Print Elements" ,arrayList_selected_inspection_element_name+"  "+arrayList_regime_element_id+" "
                            +arrayList_selected_date_validation+" "+arrayList_selected_hour_validation+" "+arrayList_selected_on_off_validation
                            +" "+arrayList_selected_yes_no_validation+" "+arrayList_selected_text_validation);
                    createDynamicView();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateAssetForm.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getRegimeElements(int regime_id){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = assetRegimeInspectionElementDetailsReference.whereEqualTo("status", "Active").whereEqualTo("regime_id",regime_id);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    arrayList_regime_element_id.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        arrayList_regime_element_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("child_category_id")).intValue());
                    }
                    getInspectionElements();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateAssetForm.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createDynamicView(){
        if (arrayList_regime_element_id.size()==1){
            ll_regime_element_1.setVisibility(View.VISIBLE);
            ll_regime_element_2.setVisibility(View.GONE);
            ll_regime_element_3.setVisibility(View.GONE);
            ll_regime_element_4.setVisibility(View.GONE);
            ll_regime_element_5.setVisibility(View.GONE);
            ll_regime_element_6.setVisibility(View.GONE);
            ll_regime_element_7.setVisibility(View.GONE);
            ll_regime_element_8.setVisibility(View.GONE);
            ll_regime_element_9.setVisibility(View.GONE);
            ll_regime_element_10.setVisibility(View.GONE);
            tv_regime_element_name_1.setText(arrayList_selected_inspection_element_name.get(0));
            dynamicView1stRow();
        }else if (arrayList_regime_element_id.size() == 2){
            ll_regime_element_1.setVisibility(View.VISIBLE);
            ll_regime_element_2.setVisibility(View.VISIBLE);
            ll_regime_element_3.setVisibility(View.GONE);
            ll_regime_element_4.setVisibility(View.GONE);
            ll_regime_element_5.setVisibility(View.GONE);
            ll_regime_element_6.setVisibility(View.GONE);
            ll_regime_element_7.setVisibility(View.GONE);
            ll_regime_element_8.setVisibility(View.GONE);
            ll_regime_element_9.setVisibility(View.GONE);
            ll_regime_element_10.setVisibility(View.GONE);
            tv_regime_element_name_1.setText(arrayList_selected_inspection_element_name.get(0));
            tv_regime_element_name_2.setText(arrayList_selected_inspection_element_name.get(1));
            dynamicView1stRow();
            dynamicView2ndRow();

        }else if (arrayList_regime_element_id.size() == 3){
            ll_regime_element_1.setVisibility(View.VISIBLE);
            ll_regime_element_2.setVisibility(View.VISIBLE);
            ll_regime_element_3.setVisibility(View.VISIBLE);
            ll_regime_element_4.setVisibility(View.GONE);
            ll_regime_element_5.setVisibility(View.GONE);
            ll_regime_element_6.setVisibility(View.GONE);
            ll_regime_element_7.setVisibility(View.GONE);
            ll_regime_element_8.setVisibility(View.GONE);
            ll_regime_element_9.setVisibility(View.GONE);
            ll_regime_element_10.setVisibility(View.GONE);
            tv_regime_element_name_1.setText(arrayList_selected_inspection_element_name.get(0));
            tv_regime_element_name_2.setText(arrayList_selected_inspection_element_name.get(1));
            tv_regime_element_name_3.setText(arrayList_selected_inspection_element_name.get(2));
            dynamicView1stRow();
            dynamicView2ndRow();
            dynamicView3rdRow();

        }else if (arrayList_regime_element_id.size() == 4){
            ll_regime_element_1.setVisibility(View.VISIBLE);
            ll_regime_element_2.setVisibility(View.VISIBLE);
            ll_regime_element_3.setVisibility(View.VISIBLE);
            ll_regime_element_4.setVisibility(View.VISIBLE);
            ll_regime_element_5.setVisibility(View.GONE);
            ll_regime_element_6.setVisibility(View.GONE);
            ll_regime_element_7.setVisibility(View.GONE);
            ll_regime_element_8.setVisibility(View.GONE);
            ll_regime_element_9.setVisibility(View.GONE);
            ll_regime_element_10.setVisibility(View.GONE);
            tv_regime_element_name_1.setText(arrayList_selected_inspection_element_name.get(0));
            tv_regime_element_name_2.setText(arrayList_selected_inspection_element_name.get(1));
            tv_regime_element_name_3.setText(arrayList_selected_inspection_element_name.get(2));
            tv_regime_element_name_4.setText(arrayList_selected_inspection_element_name.get(3));
            dynamicView1stRow();
            dynamicView2ndRow();
            dynamicView3rdRow();
            dynamicView4thRow();
        }else if (arrayList_regime_element_id.size() == 5){
            ll_regime_element_1.setVisibility(View.VISIBLE);
            ll_regime_element_2.setVisibility(View.VISIBLE);
            ll_regime_element_3.setVisibility(View.VISIBLE);
            ll_regime_element_4.setVisibility(View.VISIBLE);
            ll_regime_element_5.setVisibility(View.VISIBLE);
            ll_regime_element_6.setVisibility(View.GONE);
            ll_regime_element_7.setVisibility(View.GONE);
            ll_regime_element_8.setVisibility(View.GONE);
            ll_regime_element_9.setVisibility(View.GONE);
            ll_regime_element_10.setVisibility(View.GONE);
            tv_regime_element_name_1.setText(arrayList_selected_inspection_element_name.get(0));
            tv_regime_element_name_2.setText(arrayList_selected_inspection_element_name.get(1));
            tv_regime_element_name_3.setText(arrayList_selected_inspection_element_name.get(2));
            tv_regime_element_name_4.setText(arrayList_selected_inspection_element_name.get(3));
            tv_regime_element_name_5.setText(arrayList_selected_inspection_element_name.get(4));
            dynamicView1stRow();
            dynamicView2ndRow();
            dynamicView3rdRow();
            dynamicView4thRow();
            dynamicView5thRow();
        }else if (arrayList_regime_element_id.size() == 6){
            ll_regime_element_1.setVisibility(View.VISIBLE);
            ll_regime_element_2.setVisibility(View.VISIBLE);
            ll_regime_element_3.setVisibility(View.VISIBLE);
            ll_regime_element_4.setVisibility(View.VISIBLE);
            ll_regime_element_5.setVisibility(View.VISIBLE);
            ll_regime_element_6.setVisibility(View.VISIBLE);
            ll_regime_element_7.setVisibility(View.GONE);
            ll_regime_element_8.setVisibility(View.GONE);
            ll_regime_element_9.setVisibility(View.GONE);
            ll_regime_element_10.setVisibility(View.GONE);
            tv_regime_element_name_1.setText(arrayList_selected_inspection_element_name.get(0));
            tv_regime_element_name_2.setText(arrayList_selected_inspection_element_name.get(1));
            tv_regime_element_name_3.setText(arrayList_selected_inspection_element_name.get(2));
            tv_regime_element_name_4.setText(arrayList_selected_inspection_element_name.get(3));
            tv_regime_element_name_5.setText(arrayList_selected_inspection_element_name.get(4));
            tv_regime_element_name_6.setText(arrayList_selected_inspection_element_name.get(5));
            dynamicView1stRow();
            dynamicView2ndRow();
            dynamicView3rdRow();
            dynamicView4thRow();
            dynamicView5thRow();
            dynamicView6thRow();
        }else if (arrayList_regime_element_id.size() == 7){
            ll_regime_element_1.setVisibility(View.VISIBLE);
            ll_regime_element_2.setVisibility(View.VISIBLE);
            ll_regime_element_3.setVisibility(View.VISIBLE);
            ll_regime_element_4.setVisibility(View.VISIBLE);
            ll_regime_element_5.setVisibility(View.VISIBLE);
            ll_regime_element_6.setVisibility(View.VISIBLE);
            ll_regime_element_7.setVisibility(View.VISIBLE);
            ll_regime_element_8.setVisibility(View.GONE);
            ll_regime_element_9.setVisibility(View.GONE);
            ll_regime_element_10.setVisibility(View.GONE);
            tv_regime_element_name_1.setText(arrayList_selected_inspection_element_name.get(0));
            tv_regime_element_name_2.setText(arrayList_selected_inspection_element_name.get(1));
            tv_regime_element_name_3.setText(arrayList_selected_inspection_element_name.get(2));
            tv_regime_element_name_4.setText(arrayList_selected_inspection_element_name.get(3));
            tv_regime_element_name_5.setText(arrayList_selected_inspection_element_name.get(4));
            tv_regime_element_name_6.setText(arrayList_selected_inspection_element_name.get(5));
            tv_regime_element_name_7.setText(arrayList_selected_inspection_element_name.get(6));
            dynamicView1stRow();
            dynamicView2ndRow();
            dynamicView3rdRow();
            dynamicView4thRow();
            dynamicView5thRow();
            dynamicView6thRow();
            dynamicView7thRow();
        }else if (arrayList_regime_element_id.size() == 8){
            ll_regime_element_1.setVisibility(View.VISIBLE);
            ll_regime_element_2.setVisibility(View.VISIBLE);
            ll_regime_element_3.setVisibility(View.VISIBLE);
            ll_regime_element_4.setVisibility(View.VISIBLE);
            ll_regime_element_5.setVisibility(View.VISIBLE);
            ll_regime_element_6.setVisibility(View.VISIBLE);
            ll_regime_element_7.setVisibility(View.VISIBLE);
            ll_regime_element_8.setVisibility(View.VISIBLE);
            ll_regime_element_9.setVisibility(View.GONE);
            ll_regime_element_10.setVisibility(View.GONE);
            tv_regime_element_name_1.setText(arrayList_selected_inspection_element_name.get(0));
            tv_regime_element_name_2.setText(arrayList_selected_inspection_element_name.get(1));
            tv_regime_element_name_3.setText(arrayList_selected_inspection_element_name.get(2));
            tv_regime_element_name_4.setText(arrayList_selected_inspection_element_name.get(3));
            tv_regime_element_name_5.setText(arrayList_selected_inspection_element_name.get(4));
            tv_regime_element_name_6.setText(arrayList_selected_inspection_element_name.get(5));
            tv_regime_element_name_7.setText(arrayList_selected_inspection_element_name.get(6));
            tv_regime_element_name_8.setText(arrayList_selected_inspection_element_name.get(7));
            dynamicView1stRow();
            dynamicView2ndRow();
            dynamicView3rdRow();
            dynamicView4thRow();
            dynamicView5thRow();
            dynamicView6thRow();
            dynamicView7thRow();
            dynamicView8thRow();
        }else if (arrayList_regime_element_id.size() == 9){
            ll_regime_element_1.setVisibility(View.VISIBLE);
            ll_regime_element_2.setVisibility(View.VISIBLE);
            ll_regime_element_3.setVisibility(View.VISIBLE);
            ll_regime_element_4.setVisibility(View.VISIBLE);
            ll_regime_element_5.setVisibility(View.VISIBLE);
            ll_regime_element_6.setVisibility(View.VISIBLE);
            ll_regime_element_7.setVisibility(View.VISIBLE);
            ll_regime_element_8.setVisibility(View.VISIBLE);
            ll_regime_element_9.setVisibility(View.VISIBLE);
            ll_regime_element_10.setVisibility(View.GONE);
            tv_regime_element_name_1.setText(arrayList_selected_inspection_element_name.get(0));
            tv_regime_element_name_2.setText(arrayList_selected_inspection_element_name.get(1));
            tv_regime_element_name_3.setText(arrayList_selected_inspection_element_name.get(2));
            tv_regime_element_name_4.setText(arrayList_selected_inspection_element_name.get(3));
            tv_regime_element_name_5.setText(arrayList_selected_inspection_element_name.get(4));
            tv_regime_element_name_6.setText(arrayList_selected_inspection_element_name.get(5));
            tv_regime_element_name_7.setText(arrayList_selected_inspection_element_name.get(6));
            tv_regime_element_name_8.setText(arrayList_selected_inspection_element_name.get(7));
            tv_regime_element_name_9.setText(arrayList_selected_inspection_element_name.get(8));
            dynamicView1stRow();
            dynamicView2ndRow();
            dynamicView3rdRow();
            dynamicView4thRow();
            dynamicView5thRow();
            dynamicView6thRow();
            dynamicView7thRow();
            dynamicView8thRow();
            dynamicView9thRow();
        }else if (arrayList_regime_element_id.size() == 10){
            ll_regime_element_1.setVisibility(View.VISIBLE);
            ll_regime_element_2.setVisibility(View.VISIBLE);
            ll_regime_element_3.setVisibility(View.VISIBLE);
            ll_regime_element_4.setVisibility(View.VISIBLE);
            ll_regime_element_5.setVisibility(View.VISIBLE);
            ll_regime_element_6.setVisibility(View.VISIBLE);
            ll_regime_element_7.setVisibility(View.VISIBLE);
            ll_regime_element_8.setVisibility(View.VISIBLE);
            ll_regime_element_9.setVisibility(View.VISIBLE);
            ll_regime_element_10.setVisibility(View.VISIBLE);
            tv_regime_element_name_1.setText(arrayList_selected_inspection_element_name.get(0));
            tv_regime_element_name_2.setText(arrayList_selected_inspection_element_name.get(1));
            tv_regime_element_name_3.setText(arrayList_selected_inspection_element_name.get(2));
            tv_regime_element_name_4.setText(arrayList_selected_inspection_element_name.get(3));
            tv_regime_element_name_5.setText(arrayList_selected_inspection_element_name.get(4));
            tv_regime_element_name_6.setText(arrayList_selected_inspection_element_name.get(5));
            tv_regime_element_name_7.setText(arrayList_selected_inspection_element_name.get(6));
            tv_regime_element_name_8.setText(arrayList_selected_inspection_element_name.get(7));
            tv_regime_element_name_9.setText(arrayList_selected_inspection_element_name.get(8));
            tv_regime_element_name_10.setText(arrayList_selected_inspection_element_name.get(9));
            dynamicView1stRow();
            dynamicView2ndRow();
            dynamicView3rdRow();
            dynamicView4thRow();
            dynamicView5thRow();
            dynamicView6thRow();
            dynamicView7thRow();
            dynamicView8thRow();
            dynamicView9thRow();
            dynamicView10thRow();
        }
    }

    private void dynamicView1stRow(){
        if (arrayList_selected_date_validation.get(0).equals("Yes")) {
            rl_date_picker_1.setVisibility(View.VISIBLE);
            et_service_due_hours_1.setVisibility(View.GONE);
            rg_on_off_1.setVisibility(View.GONE);
            rg_yes_no_1.setVisibility(View.GONE);
            view_type_one = "TextView";
        }else if (arrayList_selected_hour_validation.get(0).equals("Yes")){
            rl_date_picker_1.setVisibility(View.GONE);
            et_service_due_hours_1.setVisibility(View.VISIBLE);
            et_service_due_hours_1.setInputType(InputType.TYPE_CLASS_NUMBER);
            rg_on_off_1.setVisibility(View.GONE);
            rg_yes_no_1.setVisibility(View.GONE);
            view_type_one = "EditText";
        }else if (arrayList_selected_on_off_validation.get(0).equals("Yes")){
            rl_date_picker_1.setVisibility(View.GONE);
            et_service_due_hours_1.setVisibility(View.GONE);
            rg_on_off_1.setVisibility(View.VISIBLE);
            rg_yes_no_1.setVisibility(View.GONE);
            view_type_one = "RadioButtonOnOff";
        }else if (arrayList_selected_yes_no_validation.get(0).equals("Yes")){
            rl_date_picker_1.setVisibility(View.GONE);
            et_service_due_hours_1.setVisibility(View.GONE);
            rg_on_off_1.setVisibility(View.GONE);
            rg_yes_no_1.setVisibility(View.VISIBLE);
            view_type_one = "RadioButtonYesNo";
        }else if (arrayList_selected_text_validation.get(0).equals("Yes")){
            rl_date_picker_1.setVisibility(View.GONE);
            et_service_due_hours_1.setVisibility(View.VISIBLE);
            rg_on_off_1.setVisibility(View.GONE);
            rg_yes_no_1.setVisibility(View.GONE);
            view_type_one = "Text";
        }
    }

    private void dynamicView2ndRow(){
        if (arrayList_selected_date_validation.get(1).equals("Yes")) {
            rl_date_picker_2.setVisibility(View.VISIBLE);
            et_service_due_hours_2.setVisibility(View.GONE);
            rg_on_off_2.setVisibility(View.GONE);
            rg_yes_no_2.setVisibility(View.GONE);
            view_type_two = "TextView";
        }else if (arrayList_selected_hour_validation.get(1).equals("Yes")){
            rl_date_picker_2.setVisibility(View.GONE);
            et_service_due_hours_2.setVisibility(View.VISIBLE);
            et_service_due_hours_2.setInputType(InputType.TYPE_CLASS_NUMBER);
            rg_on_off_2.setVisibility(View.GONE);
            rg_yes_no_2.setVisibility(View.GONE);
            view_type_two = "EditText";
        }else if (arrayList_selected_on_off_validation.get(1).equals("Yes")){
            rl_date_picker_2.setVisibility(View.GONE);
            et_service_due_hours_2.setVisibility(View.GONE);
            rg_on_off_2.setVisibility(View.VISIBLE);
            rg_yes_no_2.setVisibility(View.GONE);
            view_type_two = "RadioButtonOnOff";
        }else if (arrayList_selected_yes_no_validation.get(1).equals("Yes")){
            rl_date_picker_2.setVisibility(View.GONE);
            et_service_due_hours_2.setVisibility(View.GONE);
            rg_on_off_2.setVisibility(View.GONE);
            rg_yes_no_2.setVisibility(View.VISIBLE);
            view_type_two = "RadioButtonYesNo";
        }else if (arrayList_selected_text_validation.get(1).equals("Yes")){
            rl_date_picker_2.setVisibility(View.GONE);
            et_service_due_hours_2.setVisibility(View.VISIBLE);
            rg_on_off_2.setVisibility(View.GONE);
            rg_yes_no_2.setVisibility(View.GONE);
            view_type_two = "Text";
        }
    }

    private void dynamicView3rdRow(){
        if (arrayList_selected_date_validation.get(2).equals("Yes")) {
            rl_date_picker_3.setVisibility(View.VISIBLE);
            et_service_due_hours_3.setVisibility(View.GONE);
            rg_on_off_3.setVisibility(View.GONE);
            rg_yes_no_3.setVisibility(View.GONE);
            view_type_three = "TextView";
        }else if (arrayList_selected_hour_validation.get(2).equals("Yes")){
            rl_date_picker_3.setVisibility(View.GONE);
            et_service_due_hours_3.setVisibility(View.VISIBLE);
            et_service_due_hours_3.setInputType(InputType.TYPE_CLASS_NUMBER);
            rg_on_off_3.setVisibility(View.GONE);
            rg_yes_no_3.setVisibility(View.GONE);
            view_type_three = "EditText";
        }else if (arrayList_selected_on_off_validation.get(2).equals("Yes")){
            rl_date_picker_3.setVisibility(View.GONE);
            et_service_due_hours_3.setVisibility(View.GONE);
            rg_on_off_3.setVisibility(View.VISIBLE);
            rg_yes_no_3.setVisibility(View.GONE);
            view_type_three = "RadioButtonOnOff";
        }else if (arrayList_selected_yes_no_validation.get(2).equals("Yes")){
            rl_date_picker_3.setVisibility(View.GONE);
            et_service_due_hours_3.setVisibility(View.GONE);
            rg_on_off_3.setVisibility(View.GONE);
            rg_yes_no_3.setVisibility(View.VISIBLE);
            view_type_three = "RadioButtonYesNo";
        }else if (arrayList_selected_text_validation.get(2).equals("Yes")){
            rl_date_picker_3.setVisibility(View.GONE);
            et_service_due_hours_3.setVisibility(View.VISIBLE);
            rg_on_off_3.setVisibility(View.GONE);
            rg_yes_no_3.setVisibility(View.GONE);
            view_type_three = "Text";
        }
    }

    private void dynamicView4thRow(){
        if (arrayList_selected_date_validation.get(3).equals("Yes")) {
            rl_date_picker_4.setVisibility(View.VISIBLE);
            et_service_due_hours_4.setVisibility(View.GONE);
            rg_on_off_4.setVisibility(View.GONE);
            rg_yes_no_4.setVisibility(View.GONE);
            view_type_four = "TextView";
        }else if (arrayList_selected_hour_validation.get(3).equals("Yes")){
            rl_date_picker_4.setVisibility(View.GONE);
            et_service_due_hours_4.setVisibility(View.VISIBLE);
            et_service_due_hours_4.setInputType(InputType.TYPE_CLASS_NUMBER);
            rg_on_off_4.setVisibility(View.GONE);
            rg_yes_no_4.setVisibility(View.GONE);
            view_type_four = "EditText";
        }else if (arrayList_selected_on_off_validation.get(3).equals("Yes")){
            rl_date_picker_4.setVisibility(View.GONE);
            et_service_due_hours_4.setVisibility(View.GONE);
            rg_on_off_4.setVisibility(View.VISIBLE);
            rg_yes_no_4.setVisibility(View.GONE);
            view_type_four = "RadioButtonOnOff";
        }else if (arrayList_selected_yes_no_validation.get(3).equals("Yes")){
            rl_date_picker_4.setVisibility(View.GONE);
            et_service_due_hours_4.setVisibility(View.GONE);
            rg_on_off_4.setVisibility(View.GONE);
            rg_yes_no_4.setVisibility(View.VISIBLE);
            view_type_four = "RadioButtonYesNo";
        }else if (arrayList_selected_text_validation.get(3).equals("Yes")){
            rl_date_picker_4.setVisibility(View.GONE);
            et_service_due_hours_4.setVisibility(View.VISIBLE);
            rg_on_off_4.setVisibility(View.GONE);
            rg_yes_no_4.setVisibility(View.GONE);
            view_type_four = "Text";
        }
    }

    private void dynamicView5thRow(){
        if (arrayList_selected_date_validation.get(4).equals("Yes")) {
            rl_date_picker_5.setVisibility(View.VISIBLE);
            et_service_due_hours_5.setVisibility(View.GONE);
            rg_on_off_5.setVisibility(View.GONE);
            rg_yes_no_5.setVisibility(View.GONE);
            view_type_five = "TextView";
        }else if (arrayList_selected_hour_validation.get(4).equals("Yes")){
            rl_date_picker_5.setVisibility(View.GONE);
            et_service_due_hours_5.setVisibility(View.VISIBLE);
            et_service_due_hours_5.setInputType(InputType.TYPE_CLASS_NUMBER);
            rg_on_off_5.setVisibility(View.GONE);
            rg_yes_no_5.setVisibility(View.GONE);
            view_type_five = "EditText";
        }else if (arrayList_selected_on_off_validation.get(4).equals("Yes")){
            rl_date_picker_5.setVisibility(View.GONE);
            et_service_due_hours_5.setVisibility(View.GONE);
            rg_on_off_5.setVisibility(View.VISIBLE);
            rg_yes_no_5.setVisibility(View.GONE);
            view_type_five = "RadioButtonOnOff";
        }else if (arrayList_selected_yes_no_validation.get(4).equals("Yes")){
            rl_date_picker_5.setVisibility(View.GONE);
            et_service_due_hours_5.setVisibility(View.GONE);
            rg_on_off_5.setVisibility(View.GONE);
            rg_yes_no_5.setVisibility(View.VISIBLE);
            view_type_five = "RadioButtonYesNo";
        }else if (arrayList_selected_text_validation.get(4).equals("Yes")){
            rl_date_picker_5.setVisibility(View.GONE);
            et_service_due_hours_5.setVisibility(View.VISIBLE);
            rg_on_off_5.setVisibility(View.GONE);
            rg_yes_no_5.setVisibility(View.GONE);
            view_type_five = "Text";
        }
    }

    private void dynamicView6thRow(){
        if (arrayList_selected_date_validation.get(5).equals("Yes")) {
            rl_date_picker_6.setVisibility(View.VISIBLE);
            et_service_due_hours_6.setVisibility(View.GONE);
            rg_on_off_6.setVisibility(View.GONE);
            rg_yes_no_6.setVisibility(View.GONE);
            view_type_six = "TextView";
        }else if (arrayList_selected_hour_validation.get(5).equals("Yes")){
            rl_date_picker_6.setVisibility(View.GONE);
            et_service_due_hours_6.setVisibility(View.VISIBLE);
            et_service_due_hours_6.setInputType(InputType.TYPE_CLASS_NUMBER);
            rg_on_off_6.setVisibility(View.GONE);
            rg_yes_no_6.setVisibility(View.GONE);
            view_type_six = "EditText";
        }else if (arrayList_selected_on_off_validation.get(5).equals("Yes")){
            rl_date_picker_6.setVisibility(View.GONE);
            et_service_due_hours_6.setVisibility(View.GONE);
            rg_on_off_6.setVisibility(View.VISIBLE);
            rg_yes_no_6.setVisibility(View.GONE);
            view_type_six = "RadioButtonOnOff";
        }else if (arrayList_selected_yes_no_validation.get(5).equals("Yes")){
            rl_date_picker_6.setVisibility(View.GONE);
            et_service_due_hours_6.setVisibility(View.GONE);
            rg_on_off_6.setVisibility(View.GONE);
            rg_yes_no_6.setVisibility(View.VISIBLE);
            view_type_six = "RadioButtonYesNo";
        }else if (arrayList_selected_text_validation.get(5).equals("Yes")){
            rl_date_picker_6.setVisibility(View.GONE);
            et_service_due_hours_6.setVisibility(View.VISIBLE);
            rg_on_off_6.setVisibility(View.GONE);
            rg_yes_no_6.setVisibility(View.GONE);
            view_type_six = "Text";
        }
    }

    private void dynamicView7thRow(){
        if (arrayList_selected_date_validation.get(6).equals("Yes")) {
            rl_date_picker_7.setVisibility(View.VISIBLE);
            et_service_due_hours_7.setVisibility(View.GONE);
            rg_on_off_7.setVisibility(View.GONE);
            rg_yes_no_7.setVisibility(View.GONE);
            view_type_seven= "TextView";
        }else if (arrayList_selected_hour_validation.get(6).equals("Yes")){
            rl_date_picker_7.setVisibility(View.GONE);
            et_service_due_hours_7.setVisibility(View.VISIBLE);
            et_service_due_hours_7.setInputType(InputType.TYPE_CLASS_NUMBER);
            rg_on_off_7.setVisibility(View.GONE);
            rg_yes_no_7.setVisibility(View.GONE);
            view_type_seven = "EditText";
        }else if (arrayList_selected_on_off_validation.get(6).equals("Yes")){
            rl_date_picker_7.setVisibility(View.GONE);
            et_service_due_hours_7.setVisibility(View.GONE);
            rg_on_off_7.setVisibility(View.VISIBLE);
            rg_yes_no_7.setVisibility(View.GONE);
            view_type_seven = "RadioButtonOnOff";
        }else if (arrayList_selected_yes_no_validation.get(6).equals("Yes")){
            rl_date_picker_7.setVisibility(View.GONE);
            et_service_due_hours_7.setVisibility(View.GONE);
            rg_on_off_7.setVisibility(View.GONE);
            rg_yes_no_7.setVisibility(View.VISIBLE);
            view_type_seven = "RadioButtonYesNo";
        }else if (arrayList_selected_text_validation.get(6).equals("Yes")){
            rl_date_picker_7.setVisibility(View.GONE);
            et_service_due_hours_7.setVisibility(View.VISIBLE);
            rg_on_off_7.setVisibility(View.GONE);
            rg_yes_no_7.setVisibility(View.GONE);
            view_type_seven = "Text";
        }
    }

    private void dynamicView8thRow(){
        if (arrayList_selected_date_validation.get(7).equals("Yes")) {
            rl_date_picker_8.setVisibility(View.VISIBLE);
            et_service_due_hours_8.setVisibility(View.GONE);
            rg_on_off_8.setVisibility(View.GONE);
            rg_yes_no_8.setVisibility(View.GONE);
            view_type_eight = "TextView";
        }else if (arrayList_selected_hour_validation.get(7).equals("Yes")){
            rl_date_picker_8.setVisibility(View.GONE);
            et_service_due_hours_8.setVisibility(View.VISIBLE);
            et_service_due_hours_8.setInputType(InputType.TYPE_CLASS_NUMBER);
            rg_on_off_8.setVisibility(View.GONE);
            rg_yes_no_8.setVisibility(View.GONE);
            view_type_eight = "EditText";
        }else if (arrayList_selected_on_off_validation.get(7).equals("Yes")){
            rl_date_picker_8.setVisibility(View.GONE);
            et_service_due_hours_8.setVisibility(View.GONE);
            rg_on_off_8.setVisibility(View.VISIBLE);
            rg_yes_no_8.setVisibility(View.GONE);
            view_type_eight = "RadioButtonOnOff";
        }else if (arrayList_selected_yes_no_validation.get(7).equals("Yes")){
            rl_date_picker_8.setVisibility(View.GONE);
            et_service_due_hours_8.setVisibility(View.GONE);
            rg_on_off_8.setVisibility(View.GONE);
            rg_yes_no_8.setVisibility(View.VISIBLE);
            view_type_eight = "RadioButtonYesNo";
        }else if (arrayList_selected_text_validation.get(7).equals("Yes")){
            rl_date_picker_8.setVisibility(View.GONE);
            et_service_due_hours_8.setVisibility(View.VISIBLE);
            rg_on_off_8.setVisibility(View.GONE);
            rg_yes_no_8.setVisibility(View.GONE);
            view_type_eight = "Text";
        }
    }

    private void dynamicView9thRow(){
        if (arrayList_selected_date_validation.get(8).equals("Yes")) {
            rl_date_picker_9.setVisibility(View.VISIBLE);
            et_service_due_hours_9.setVisibility(View.GONE);
            rg_on_off_9.setVisibility(View.GONE);
            rg_yes_no_9.setVisibility(View.GONE);
            view_type_nine = "TextView";
        }else if (arrayList_selected_hour_validation.get(8).equals("Yes")){
            rl_date_picker_9.setVisibility(View.GONE);
            et_service_due_hours_9.setVisibility(View.VISIBLE);
            et_service_due_hours_9.setInputType(InputType.TYPE_CLASS_NUMBER);
            rg_on_off_9.setVisibility(View.GONE);
            rg_yes_no_9.setVisibility(View.GONE);
            view_type_nine = "EditText";
        }else if (arrayList_selected_on_off_validation.get(8).equals("Yes")){
            rl_date_picker_9.setVisibility(View.GONE);
            et_service_due_hours_9.setVisibility(View.GONE);
            rg_on_off_9.setVisibility(View.VISIBLE);
            rg_yes_no_9.setVisibility(View.GONE);
            view_type_nine = "RadioButtonOnOff";
        }else if (arrayList_selected_yes_no_validation.get(8).equals("Yes")){
            rl_date_picker_9.setVisibility(View.GONE);
            et_service_due_hours_9.setVisibility(View.GONE);
            rg_on_off_9.setVisibility(View.GONE);
            rg_yes_no_9.setVisibility(View.VISIBLE);
            view_type_nine = "RadioButtonYesNo";
        }else if (arrayList_selected_text_validation.get(8).equals("Yes")){
            rl_date_picker_9.setVisibility(View.GONE);
            et_service_due_hours_9.setVisibility(View.VISIBLE);
            rg_on_off_9.setVisibility(View.GONE);
            rg_yes_no_9.setVisibility(View.GONE);
            view_type_nine = "Text";
        }
    }

    private void dynamicView10thRow(){
        if (arrayList_selected_date_validation.get(9).equals("Yes")) {
            rl_date_picker_10.setVisibility(View.VISIBLE);
            et_service_due_hours_10.setVisibility(View.GONE);
            rg_on_off_10.setVisibility(View.GONE);
            rg_yes_no_10.setVisibility(View.GONE);
            view_type_ten = "TextView";
        }else if (arrayList_selected_hour_validation.get(9).equals("Yes")){
            rl_date_picker_10.setVisibility(View.GONE);
            et_service_due_hours_10.setVisibility(View.VISIBLE);
            et_service_due_hours_10.setInputType(InputType.TYPE_CLASS_NUMBER);
            rg_on_off_10.setVisibility(View.GONE);
            rg_yes_no_10.setVisibility(View.GONE);
            view_type_ten = "EditText";
        }else if (arrayList_selected_on_off_validation.get(9).equals("Yes")){
            rl_date_picker_10.setVisibility(View.GONE);
            et_service_due_hours_10.setVisibility(View.GONE);
            rg_on_off_10.setVisibility(View.VISIBLE);
            rg_yes_no_10.setVisibility(View.GONE);
            view_type_ten = "RadioButtonOnOff";
        }else if (arrayList_selected_yes_no_validation.get(9).equals("Yes")){
            rl_date_picker_10.setVisibility(View.GONE);
            et_service_due_hours_10.setVisibility(View.GONE);
            rg_on_off_10.setVisibility(View.GONE);
            rg_yes_no_10.setVisibility(View.VISIBLE);
            view_type_ten = "RadioButtonYesNo";
        }else if (arrayList_selected_text_validation.get(9).equals("Yes")){
            rl_date_picker_10.setVisibility(View.GONE);
            et_service_due_hours_10.setVisibility(View.VISIBLE);
            rg_on_off_10.setVisibility(View.GONE);
            rg_yes_no_10.setVisibility(View.GONE);
            view_type_ten = "Text";
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String day = String.valueOf(dayOfMonth);
        String Month = String.valueOf(month+1);
        String Year = String.valueOf(year);
        if(dayOfMonth < 10){
            day = "0" + dayOfMonth;
        }
        if(month < 9){
            Month  = "0" + Month ;
        }
        String pickedDate = day + "-" + Month + "-" + Year;
        if (selectedRegimeElementRow==1) {
            datePickerOneValue = pickedDate;
            tv_date_picker_1.setText(datePickerOneValue);
        }else if (selectedRegimeElementRow==2){
            datePickerTwoValue = pickedDate;
            tv_date_picker_2.setText(datePickerTwoValue);
        }else if (selectedRegimeElementRow==3){
            datePickerThreeValue = pickedDate;
            tv_date_picker_3.setText(datePickerThreeValue);
        }else if (selectedRegimeElementRow==4){
            datePickerFourValue = pickedDate;
            tv_date_picker_4.setText(datePickerFourValue);
        }else if (selectedRegimeElementRow==5){
            datePickerFiveValue = pickedDate;
            tv_date_picker_5.setText(datePickerFiveValue);
        }else if (selectedRegimeElementRow==6){
            datePickerSixValue = pickedDate;
            tv_date_picker_6.setText(datePickerSixValue);
        }
    }

    private void manageYesNoRadioButton() {
        if (rg_yes_no_1.getVisibility()==View.VISIBLE) {
            int selectedId = rg_yes_no_1.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            rgYesNoOneValue = (String) radioButton.getText();
            rg_yes_no_1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if (i == R.id.rb_yes_1) {
                      rgYesNoOneValue= "Yes";
                    }
                    if (i == R.id.rb_no_1) {
                      rgYesNoOneValue= "No";
                    }
                }
            });
        }
        if (rg_yes_no_2.getVisibility()==View.VISIBLE) {
            int selectedId = rg_yes_no_2.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            rgYesNoTwoValue = (String) radioButton.getText();
            rg_yes_no_2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if (i == R.id.rb_yes_2) {
                        rgYesNoTwoValue= "Yes";
                    }
                    if (i == R.id.rb_no_2) {
                        rgYesNoTwoValue= "No";
                    }
                }
            });
        }
        if (rg_yes_no_3.getVisibility()==View.VISIBLE) {
            Log.d("YTUYK :" ,"I am 3");
            int selectedId = rg_yes_no_3.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            rgYesNoThreeValue = (String) radioButton.getText();
            rg_yes_no_3.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if (i == R.id.rb_yes_3) {
                        rgYesNoThreeValue= "Yes";
                    }
                    if (i == R.id.rb_no_3) {
                        rgYesNoThreeValue= "No";
                    }
                }
            });
        }
        if (rg_yes_no_4.getVisibility()==View.VISIBLE) {
            int selectedId = rg_yes_no_4.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            rgYesNoFourValue = (String) radioButton.getText();
            rg_yes_no_4.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if (i == R.id.rb_yes_4) {
                        rgYesNoFourValue= "Yes";
                    }
                    if (i == R.id.rb_no_4) {
                        rgYesNoFourValue= "No";
                    }
                }
            });
        }
        if (rg_yes_no_5.getVisibility()==View.VISIBLE) {
            int selectedId = rg_yes_no_5.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            rgYesNoFiveValue = (String) radioButton.getText();
            rg_yes_no_5.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if (i == R.id.rb_yes_5) {
                        rgYesNoFiveValue= "Yes";
                    }
                    if (i == R.id.rb_no_5) {
                        rgYesNoFiveValue= "No";
                    }
                }
            });
        }
        if (rg_yes_no_6.getVisibility()==View.VISIBLE) {
            int selectedId = rg_yes_no_6.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            rgYesNoSixValue = (String) radioButton.getText();
            rg_yes_no_6.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if (i == R.id.rb_yes_6) {
                        rgYesNoSixValue= "Yes";
                    }
                    if (i == R.id.rb_no_6) {
                        rgYesNoSixValue= "No";
                    }
                }
            });
        }
        if (rg_yes_no_7.getVisibility()==View.VISIBLE) {
            int selectedId = rg_yes_no_7.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            rgYesNoSevenValue = (String) radioButton.getText();
            rg_yes_no_7.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if (i == R.id.rb_yes_7) {
                        rgYesNoSevenValue= "Yes";
                    }
                    if (i == R.id.rb_no_7) {
                        rgYesNoSevenValue= "No";
                    }
                }
            });
        }
        if (rg_yes_no_8.getVisibility()==View.VISIBLE) {
            int selectedId = rg_yes_no_8.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            rgYesNoEightValue = (String) radioButton.getText();
            rg_yes_no_8.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if (i == R.id.rb_yes_8) {
                        rgYesNoEightValue= "Yes";
                    }
                    if (i == R.id.rb_no_8) {
                        rgYesNoEightValue= "No";
                    }
                }
            });
        }
        if (rg_yes_no_9.getVisibility()==View.VISIBLE) {
            int selectedId = rg_yes_no_9.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            rgYesNoNineValue = (String) radioButton.getText();
            rg_yes_no_9.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if (i == R.id.rb_yes_9) {
                        rgYesNoNineValue= "Yes";
                    }
                    if (i == R.id.rb_no_9) {
                        rgYesNoNineValue= "No";
                    }
                }
            });
        }
        if (rg_yes_no_10.getVisibility()==View.VISIBLE) {
            int selectedId = rg_yes_no_10.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            rgYesNoTenValue = (String) radioButton.getText();
            rg_yes_no_10.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if (i == R.id.rb_yes_10) {
                        rgYesNoTenValue= "Yes";
                    }
                    if (i == R.id.rb_no_10) {
                        rgYesNoTenValue= "No";
                    }
                }
            });
        }
    }


    private void manageOnOffRadioButton() {
        if (rg_on_off_1.getVisibility()==View.VISIBLE) {
            int selectedId = rg_on_off_1.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            rgOnOffOneValue = (String) radioButton.getText();
            rg_on_off_1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if (i == R.id.rb_on_1) {
                        rgOnOffOneValue= "On";
                    }
                    if (i == R.id.rb_off_1) {
                        rgOnOffOneValue= "Off";
                    }
                }
            });
        }
        if (rg_on_off_2.getVisibility()==View.VISIBLE) {
            int selectedId = rg_on_off_2.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            rgOnOffTwoValue = (String) radioButton.getText();
            rg_on_off_2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if (i == R.id.rb_on_2) {
                        rgOnOffTwoValue= "On";
                    }
                    if (i == R.id.rb_off_2) {
                        rgOnOffTwoValue= "Off";
                    }
                }
            });
        }
        if (rg_on_off_3.getVisibility()==View.VISIBLE) {
            int selectedId = rg_on_off_3.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            rgOnOffThreeValue = (String) radioButton.getText();
            rg_on_off_3.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if (i == R.id.rb_on_3) {
                        rgOnOffThreeValue= "On";
                    }
                    if (i == R.id.rb_off_3) {
                        rgOnOffThreeValue= "Off";
                    }
                }
            });
        }
        if (rg_on_off_4.getVisibility()==View.VISIBLE) {
            int selectedId = rg_on_off_4.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            rgOnOffFourValue = (String) radioButton.getText();
            rg_on_off_4.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if (i == R.id.rb_on_4) {
                        rgOnOffFourValue= "On";
                    }
                    if (i == R.id.rb_off_4) {
                        rgOnOffFourValue= "Off";
                    }
                }
            });
        }
        if (rg_on_off_5.getVisibility()==View.VISIBLE) {
            int selectedId = rg_on_off_5.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            rgOnOffFiveValue = (String) radioButton.getText();
            rg_on_off_5.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if (i == R.id.rb_on_5) {
                        rgOnOffFiveValue= "On";
                    }
                    if (i == R.id.rb_off_5) {
                        rgOnOffFiveValue= "Off";
                    }
                }
            });
        }
        if (rg_on_off_6.getVisibility()==View.VISIBLE) {
            int selectedId = rg_on_off_6.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            rgOnOffSixValue = (String) radioButton.getText();
            rg_on_off_6.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if (i == R.id.rb_on_6) {
                        rgOnOffSixValue= "On";
                    }
                    if (i == R.id.rb_off_6) {
                        rgOnOffSixValue= "Off";
                    }
                }
            });
        }
        if (rg_on_off_7.getVisibility()==View.VISIBLE) {
            int selectedId = rg_on_off_7.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            rgOnOffSevenValue = (String) radioButton.getText();
            rg_on_off_7.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if (i == R.id.rb_on_7) {
                        rgOnOffSevenValue= "On";
                    }
                    if (i == R.id.rb_off_7) {
                        rgOnOffSevenValue= "Off";
                    }
                }
            });
        }
        if (rg_on_off_8.getVisibility()==View.VISIBLE) {
            int selectedId = rg_on_off_8.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            rgOnOffEightValue = (String) radioButton.getText();
            rg_on_off_8.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if (i == R.id.rb_on_8) {
                        rgOnOffEightValue= "On";
                    }
                    if (i == R.id.rb_off_8) {
                        rgOnOffEightValue= "Off";
                    }
                }
            });
        }
        if (rg_on_off_9.getVisibility()==View.VISIBLE) {
            int selectedId = rg_on_off_9.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            rgOnOffNineValue = (String) radioButton.getText();
            rg_on_off_9.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if (i == R.id.rb_on_9) {
                        rgOnOffNineValue= "On";
                    }
                    if (i == R.id.rb_off_9) {
                        rgOnOffNineValue= "Off";
                    }
                }
            });
        }
        if (rg_on_off_10.getVisibility()==View.VISIBLE) {
            int selectedId = rg_on_off_10.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            rgOnOffTenValue = (String) radioButton.getText();
            rg_on_off_10.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if (i == R.id.rb_on_10) {
                        rgOnOffTenValue= "On";
                    }
                    if (i == R.id.rb_off_10) {
                        rgOnOffTenValue= "Off";
                    }
                }
            });
        }
    }

    private void regimeElementRowValues(){
        if (ll_regime_element_1.getVisibility()==View.VISIBLE){
            if (rl_date_picker_1.getVisibility()==View.VISIBLE){
                regimeElementFirstValue = tv_date_picker_1.getText().toString();
                if (regimeElementFirstValue.equals("Please Select New Date"))
                    regimeElementFirstValue="";
                else
                    regime_date_format_one = AppData.getDateFormatTwo(regimeElementFirstValue);
                rg_yes_no_1.setVisibility(View.GONE);
                rg_on_off_1.setVisibility(View.GONE);
                et_service_due_hours_1.setVisibility(View.GONE);
            }else if (rg_yes_no_1.getVisibility()== View.VISIBLE){
                regimeElementFirstValue = rgYesNoOneValue;
                rl_date_picker_1.setVisibility(View.GONE);
                rg_on_off_1.setVisibility(View.GONE);
                et_service_due_hours_1.setVisibility(View.GONE);
                regime_date_format_one = 0;
            }else if (rg_on_off_1.getVisibility()== View.VISIBLE){
                regimeElementFirstValue = rgOnOffOneValue;
                rl_date_picker_1.setVisibility(View.GONE);
                rg_yes_no_1.setVisibility(View.GONE);
                et_service_due_hours_1.setVisibility(View.GONE);
                regime_date_format_one = 0;
            }else if (et_service_due_hours_1.getVisibility()==View.VISIBLE){
                regimeElementFirstValue = et_service_due_hours_1.getText().toString();
                rg_yes_no_1.setVisibility(View.GONE);
                rg_on_off_1.setVisibility(View.GONE);
                rl_date_picker_1.setVisibility(View.GONE);
                regime_date_format_one = 0;
            }
        }
        if (ll_regime_element_2.getVisibility()==View.VISIBLE){
            if (rl_date_picker_2.getVisibility()==View.VISIBLE){
                regimeElementSecondValue = tv_date_picker_2.getText().toString();
                if (regimeElementSecondValue.equals("Please Select New Date"))
                    regimeElementSecondValue="";
                else
                    regime_date_format_two = AppData.getDateFormatTwo(regimeElementSecondValue);
                rg_yes_no_2.setVisibility(View.GONE);
                rg_on_off_2.setVisibility(View.GONE);
                et_service_due_hours_2.setVisibility(View.GONE);
            }else if (rg_yes_no_2.getVisibility()== View.VISIBLE){
                regimeElementSecondValue = rgYesNoTwoValue;
                rl_date_picker_2.setVisibility(View.GONE);
                rg_on_off_2.setVisibility(View.GONE);
                et_service_due_hours_2.setVisibility(View.GONE);
                regime_date_format_two = 0;
            }else if (rg_on_off_2.getVisibility()== View.VISIBLE){
                regimeElementSecondValue = rgOnOffTwoValue;
                rl_date_picker_2.setVisibility(View.GONE);
                rg_yes_no_2.setVisibility(View.GONE);
                et_service_due_hours_2.setVisibility(View.GONE);
                regime_date_format_two = 0;
            }else if (et_service_due_hours_2.getVisibility()==View.VISIBLE){
                regimeElementSecondValue = et_service_due_hours_2.getText().toString();
                rg_yes_no_2.setVisibility(View.GONE);
                rg_on_off_2.setVisibility(View.GONE);
                rl_date_picker_2.setVisibility(View.GONE);
                regime_date_format_two = 0;
            }
        }
        if (ll_regime_element_3.getVisibility()==View.VISIBLE){
            if (rl_date_picker_3.getVisibility()==View.VISIBLE){
                regimeElementThirdValue = tv_date_picker_3.getText().toString();
                if (regimeElementThirdValue.equals("Please Select New Date"))
                    regimeElementThirdValue="";
                else
                   regime_date_format_three = AppData.getDateFormatTwo(regimeElementThirdValue);
                rg_yes_no_3.setVisibility(View.GONE);
                rg_on_off_3.setVisibility(View.GONE);
                et_service_due_hours_3.setVisibility(View.GONE);
            }else if (rg_yes_no_3.getVisibility()== View.VISIBLE){
                regimeElementThirdValue = rgYesNoThreeValue;
                regime_date_format_three = 0;
                rl_date_picker_3.setVisibility(View.GONE);
                rg_on_off_3.setVisibility(View.GONE);
                et_service_due_hours_3.setVisibility(View.GONE);
            }else if (rg_on_off_3.getVisibility()== View.VISIBLE){
                regimeElementThirdValue = rgOnOffThreeValue;
                rl_date_picker_3.setVisibility(View.GONE);
                regime_date_format_three = 0;
                rg_yes_no_3.setVisibility(View.GONE);
                et_service_due_hours_3.setVisibility(View.GONE);
            }else if (et_service_due_hours_3.getVisibility()==View.VISIBLE){
                regimeElementThirdValue = et_service_due_hours_3.getText().toString();
                rg_yes_no_3.setVisibility(View.GONE);
                rg_on_off_3.setVisibility(View.GONE);
                rl_date_picker_3.setVisibility(View.GONE);
                regime_date_format_three = 0;
            }
        }
        if (ll_regime_element_4.getVisibility()==View.VISIBLE){
            if (rl_date_picker_4.getVisibility()==View.VISIBLE){
                regimeElementFourthValue = tv_date_picker_4.getText().toString();
                if (regimeElementFourthValue.equals("Please Select New Date"))
                    regimeElementFourthValue="";
                else
                    regime_date_format_four = AppData.getDateFormatTwo(regimeElementFourthValue);
                rg_yes_no_4.setVisibility(View.GONE);
                rg_on_off_4.setVisibility(View.GONE);
                et_service_due_hours_4.setVisibility(View.GONE);
            }else if (rg_yes_no_4.getVisibility()== View.VISIBLE){
                regimeElementFourthValue = rgYesNoFourValue;
                rl_date_picker_4.setVisibility(View.GONE);
                rg_on_off_4.setVisibility(View.GONE);
                et_service_due_hours_4.setVisibility(View.GONE);
                regime_date_format_four = 0;
            }else if (rg_on_off_4.getVisibility()== View.VISIBLE){
                regimeElementFourthValue = rgOnOffFourValue;
                rl_date_picker_4.setVisibility(View.GONE);
                rg_yes_no_4.setVisibility(View.GONE);
                et_service_due_hours_4.setVisibility(View.GONE);
                regime_date_format_four = 0;
            }else if (et_service_due_hours_4.getVisibility()==View.VISIBLE){
                regimeElementFourthValue = et_service_due_hours_4.getText().toString();
                rg_yes_no_4.setVisibility(View.GONE);
                rg_on_off_4.setVisibility(View.GONE);
                rl_date_picker_4.setVisibility(View.GONE);
                regime_date_format_four = 0;
            }
        }

        if (ll_regime_element_5.getVisibility()==View.VISIBLE){
            if (rl_date_picker_5.getVisibility()==View.VISIBLE){
                regimeElementFiveValue = tv_date_picker_5.getText().toString();
                if (regimeElementFiveValue.equals("Please Select New Date"))
                    regimeElementFiveValue="";
                else
                   regime_date_format_five = AppData.getDateFormatTwo(regimeElementFiveValue);
                rg_yes_no_5.setVisibility(View.GONE);
                rg_on_off_5.setVisibility(View.GONE);
                et_service_due_hours_5.setVisibility(View.GONE);
            }else if (rg_yes_no_5.getVisibility()== View.VISIBLE){
                regimeElementFiveValue = rgYesNoFiveValue;
                rl_date_picker_5.setVisibility(View.GONE);
                rg_on_off_5.setVisibility(View.GONE);
                et_service_due_hours_5.setVisibility(View.GONE);
                regime_date_format_five = 0;
            }else if (rg_on_off_5.getVisibility()== View.VISIBLE){
                regimeElementFiveValue = rgOnOffFiveValue;
                rl_date_picker_5.setVisibility(View.GONE);
                rg_yes_no_5.setVisibility(View.GONE);
                et_service_due_hours_5.setVisibility(View.GONE);
                regime_date_format_five = 0;
            }else if (et_service_due_hours_5.getVisibility()==View.VISIBLE){
                regimeElementFiveValue = et_service_due_hours_5.getText().toString();
                rg_yes_no_5.setVisibility(View.GONE);
                rg_on_off_5.setVisibility(View.GONE);
                rl_date_picker_5.setVisibility(View.GONE);
                regime_date_format_five = 0;
            }
        }

        if (ll_regime_element_6.getVisibility()==View.VISIBLE){
            if (rl_date_picker_6.getVisibility()==View.VISIBLE){
                regimeElementSixValue = tv_date_picker_6.getText().toString();
                if (regimeElementSixValue.equals("Please Select New Date"))
                    regimeElementSixValue="";
                else
                   regimeElementSixValue = tv_date_picker_6.getText().toString();
                regime_date_format_six = AppData.getDateFormatTwo(regimeElementSixValue);
                rg_yes_no_6.setVisibility(View.GONE);
                rg_on_off_6.setVisibility(View.GONE);
                et_service_due_hours_6.setVisibility(View.GONE);
            }else if (rg_yes_no_6.getVisibility()== View.VISIBLE){
                regimeElementSixValue = rgYesNoSixValue;
                rl_date_picker_6.setVisibility(View.GONE);
                rg_on_off_6.setVisibility(View.GONE);
                et_service_due_hours_6.setVisibility(View.GONE);
                regime_date_format_six = 0;
            }else if (rg_on_off_6.getVisibility()== View.VISIBLE){
                regimeElementSixValue = rgOnOffSixValue;
                rl_date_picker_6.setVisibility(View.GONE);
                rg_yes_no_6.setVisibility(View.GONE);
                et_service_due_hours_6.setVisibility(View.GONE);
                regime_date_format_six = 0;
            }else if (et_service_due_hours_6.getVisibility()==View.VISIBLE){
                regimeElementSixValue = et_service_due_hours_6.getText().toString();
                rg_yes_no_6.setVisibility(View.GONE);
                rg_on_off_6.setVisibility(View.GONE);
                rl_date_picker_6.setVisibility(View.GONE);
                regime_date_format_six = 0;
            }
        }

        if (ll_regime_element_7.getVisibility()==View.VISIBLE){
            if (rl_date_picker_7.getVisibility()==View.VISIBLE){
                regimeElementSevenValue = tv_date_picker_7.getText().toString();
                if (regimeElementSevenValue.equals("Please Select New Date"))
                    regimeElementSevenValue="";
                else
                    regimeElementSevenValue = tv_date_picker_7.getText().toString();
                regime_date_format_seven = AppData.getDateFormatTwo(regimeElementSevenValue);
                rg_yes_no_7.setVisibility(View.GONE);
                rg_on_off_7.setVisibility(View.GONE);
                et_service_due_hours_7.setVisibility(View.GONE);
            }else if (rg_yes_no_7.getVisibility()== View.VISIBLE){
                regimeElementSevenValue = rgYesNoSevenValue;
                rl_date_picker_7.setVisibility(View.GONE);
                rg_on_off_7.setVisibility(View.GONE);
                et_service_due_hours_7.setVisibility(View.GONE);
                regime_date_format_seven = 0;
            }else if (rg_on_off_7.getVisibility()== View.VISIBLE){
                regimeElementSevenValue = rgOnOffSevenValue;
                rl_date_picker_7.setVisibility(View.GONE);
                rg_yes_no_7.setVisibility(View.GONE);
                et_service_due_hours_7.setVisibility(View.GONE);
                regime_date_format_seven = 0;
            }else if (et_service_due_hours_7.getVisibility()==View.VISIBLE){
                regimeElementSevenValue = et_service_due_hours_7.getText().toString();
                rg_yes_no_7.setVisibility(View.GONE);
                rg_on_off_7.setVisibility(View.GONE);
                rl_date_picker_7.setVisibility(View.GONE);
                regime_date_format_seven = 0;
            }
        }

        if (ll_regime_element_8.getVisibility()==View.VISIBLE){
            if (rl_date_picker_8.getVisibility()==View.VISIBLE){
                regimeElementEightValue = tv_date_picker_8.getText().toString();
                if (regimeElementEightValue.equals("Please Select New Date"))
                    regimeElementEightValue="";
                else
                    regimeElementEightValue = tv_date_picker_8.getText().toString();
                regime_date_format_eight = AppData.getDateFormatTwo(regimeElementEightValue);
                rg_yes_no_8.setVisibility(View.GONE);
                rg_on_off_8.setVisibility(View.GONE);
                et_service_due_hours_8.setVisibility(View.GONE);
            }else if (rg_yes_no_8.getVisibility()== View.VISIBLE){
                regimeElementEightValue = rgYesNoEightValue;
                rl_date_picker_8.setVisibility(View.GONE);
                rg_on_off_8.setVisibility(View.GONE);
                et_service_due_hours_8.setVisibility(View.GONE);
                regime_date_format_eight = 0;
            }else if (rg_on_off_8.getVisibility()== View.VISIBLE){
                regimeElementEightValue = rgOnOffEightValue;
                rl_date_picker_8.setVisibility(View.GONE);
                rg_yes_no_8.setVisibility(View.GONE);
                et_service_due_hours_8.setVisibility(View.GONE);
                regime_date_format_eight = 0;
            }else if (et_service_due_hours_8.getVisibility()==View.VISIBLE){
                regimeElementEightValue = et_service_due_hours_8.getText().toString();
                rg_yes_no_8.setVisibility(View.GONE);
                rg_on_off_8.setVisibility(View.GONE);
                rl_date_picker_8.setVisibility(View.GONE);
                regime_date_format_eight = 0;
            }
        }

        if (ll_regime_element_9.getVisibility()==View.VISIBLE){
            if (rl_date_picker_9.getVisibility()==View.VISIBLE){
                regimeElementNineValue = tv_date_picker_9.getText().toString();
                if (regimeElementNineValue.equals("Please Select New Date"))
                    regimeElementNineValue="";
                else
                    regimeElementNineValue = tv_date_picker_9.getText().toString();
                regime_date_format_nine = AppData.getDateFormatTwo(regimeElementNineValue);
                rg_yes_no_9.setVisibility(View.GONE);
                rg_on_off_9.setVisibility(View.GONE);
                et_service_due_hours_9.setVisibility(View.GONE);
            }else if (rg_yes_no_9.getVisibility()== View.VISIBLE){
                regimeElementNineValue = rgYesNoNineValue;
                rl_date_picker_9.setVisibility(View.GONE);
                rg_on_off_9.setVisibility(View.GONE);
                et_service_due_hours_9.setVisibility(View.GONE);
                regime_date_format_nine = 0;
            }else if (rg_on_off_9.getVisibility()== View.VISIBLE){
                regimeElementNineValue = rgOnOffNineValue;
                rl_date_picker_9.setVisibility(View.GONE);
                rg_yes_no_9.setVisibility(View.GONE);
                et_service_due_hours_9.setVisibility(View.GONE);
                regime_date_format_nine = 0;
            }else if (et_service_due_hours_9.getVisibility()==View.VISIBLE){
                regimeElementNineValue = et_service_due_hours_9.getText().toString();
                rg_yes_no_9.setVisibility(View.GONE);
                rg_on_off_9.setVisibility(View.GONE);
                rl_date_picker_9.setVisibility(View.GONE);
                regime_date_format_nine = 0;
            }
        }

        if (ll_regime_element_10.getVisibility()==View.VISIBLE){
            if (rl_date_picker_10.getVisibility()==View.VISIBLE){
                regimeElementTenValue = tv_date_picker_10.getText().toString();
                if (regimeElementTenValue.equals("Please Select New Date"))
                    regimeElementTenValue="";
                else
                    regimeElementTenValue = tv_date_picker_10.getText().toString();
                regime_date_format_ten = AppData.getDateFormatTwo(regimeElementTenValue);
                rg_yes_no_10.setVisibility(View.GONE);
                rg_on_off_10.setVisibility(View.GONE);
                et_service_due_hours_10.setVisibility(View.GONE);
            }else if (rg_yes_no_10.getVisibility()== View.VISIBLE){
                regimeElementTenValue = rgYesNoTenValue;
                rl_date_picker_10.setVisibility(View.GONE);
                rg_on_off_10.setVisibility(View.GONE);
                et_service_due_hours_10.setVisibility(View.GONE);
                regime_date_format_ten = 0;
            }else if (rg_on_off_10.getVisibility()== View.VISIBLE){
                regimeElementTenValue = rgOnOffTenValue;
                rl_date_picker_10.setVisibility(View.GONE);
                rg_yes_no_10.setVisibility(View.GONE);
                et_service_due_hours_10.setVisibility(View.GONE);
                regime_date_format_ten = 0;
            }else if (et_service_due_hours_10.getVisibility()==View.VISIBLE){
                regimeElementTenValue = et_service_due_hours_10.getText().toString();
                rg_yes_no_10.setVisibility(View.GONE);
                rg_on_off_10.setVisibility(View.GONE);
                rl_date_picker_10.setVisibility(View.GONE);
                regime_date_format_ten = 0;
            }
        }
    }

    private void clearView(){
        selectedRegimeElementRow=0;
        tv_date_picker_1.setText("Please Select New Date");
        tv_date_picker_2.setText("Please Select New Date");
        tv_date_picker_3.setText("Please Select New Date");
        tv_date_picker_4.setText("Please Select New Date");
        tv_date_picker_5.setText("Please Select New Date");
        tv_date_picker_6.setText("Please Select New Date");
        tv_date_picker_7.setText("Please Select New Date");
        tv_date_picker_8.setText("Please Select New Date");
        tv_date_picker_9.setText("Please Select New Date");
        tv_date_picker_10.setText("Please Select New Date");
        et_service_due_hours_1.setText(" ");
        et_service_due_hours_2.setText(" ");
        et_service_due_hours_3.setText(" ");
        et_service_due_hours_4.setText(" ");
        et_service_due_hours_5.setText(" ");
        et_service_due_hours_6.setText(" ");
        et_service_due_hours_7.setText(" ");
        et_service_due_hours_8.setText(" ");
        et_service_due_hours_9.setText(" ");
        et_service_due_hours_10.setText(" ");
        rg_on_off_1.clearCheck();
        rg_on_off_2.clearCheck();
        rg_on_off_3.clearCheck();
        rg_on_off_4.clearCheck();
        rg_on_off_5.clearCheck();
        rg_on_off_6.clearCheck();
        rg_on_off_7.clearCheck();
        rg_on_off_8.clearCheck();
        rg_on_off_9.clearCheck();
        rg_on_off_10.clearCheck();
        rg_yes_no_1.clearCheck();
        rg_yes_no_2.clearCheck();
        rg_yes_no_3.clearCheck();
        rg_yes_no_4.clearCheck();
        rg_yes_no_5.clearCheck();
        rg_yes_no_6.clearCheck();
        rg_yes_no_7.clearCheck();
        rg_yes_no_8.clearCheck();
        rg_yes_no_9.clearCheck();
        rg_yes_no_10.clearCheck();
    }

    private void getManufacturer() {
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = vehicleManufacturerReference.whereEqualTo("status", "Active").orderBy("manufacturer_name");
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        arrayList_manufacturer_name.add(queryDocumentSnapshot.getString("manufacturer_name"));
                    }
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(CreateAssetForm.this, R.layout.spinner_custom_layout, arrayList_manufacturer_name);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_manufacturer.setAdapter(spinnerArrayAdapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateAssetForm.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getModel() {
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = vehicleModelReference.whereEqualTo("status", "Active").orderBy("model");
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        arrayList_model_name.add(queryDocumentSnapshot.getString("model"));
                    }
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(CreateAssetForm.this, R.layout.spinner_custom_layout, arrayList_model_name);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_model.setAdapter(spinnerArrayAdapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateAssetForm.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getOwner() {
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = assetCustomerDetailsReference.whereEqualTo("status", "Active")
                .whereEqualTo("default_customer","No");
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        arrayList_owner_name.add(queryDocumentSnapshot.getString("company"));
                        arrayList_owner_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                    }
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(CreateAssetForm.this, R.layout.spinner_custom_layout, arrayList_owner_name);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_owner.setAdapter(spinnerArrayAdapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateAssetForm.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAssignSiteName(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = amAssignSiteReference.whereEqualTo("user_id", userId).whereEqualTo("date", currentDate);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            siteLocationId = Objects.requireNonNull(queryDocumentSnapshot.getLong("site_id")).intValue();
                            siteLocationName= queryDocumentSnapshot.getString("site_name");
                        }
                        Log.d(TAG,"element name:"+siteLocationId);
                        tv_location.setText(siteLocationName);
                    }else
                            alertDialogAssignSite("You are not assigned with any site.Please assign yourself with a site.");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(CreateAssetForm.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMaxDocIDAssetDetails(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        assetDetailsReference.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            assetElementsIds.add(Integer.parseInt(queryDocumentSnapshot.getId()));
                        }
                        maxDocIdAssetDetails = String.valueOf(getMax(assetElementsIds));
                        Log.d("max_asset_details_id :",maxDocIdAssetDetails);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateAssetForm.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMaxDocIDRegimeDetails(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        regimeDetailsReference.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            regimeElementsIds.add(Integer.parseInt(queryDocumentSnapshot.getId()));
                        }
                        maxDocIdRegimeDetails = String.valueOf(getMax(regimeElementsIds));
                        Log.d("max_regime_details_id :",maxDocIdRegimeDetails);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateAssetForm.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMaxDocIQRDetails(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        assetQRDetailsReference.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            qrIds.add(Integer.parseInt(queryDocumentSnapshot.getId()));
                        }
                        maxDocIdQRDetails = String.valueOf(getMax(qrIds));
                        Log.d("max_qr_details_id :",maxDocIdQRDetails);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateAssetForm.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getQRDetails(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        assetQRDetailsReference.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            qrValues.add(queryDocumentSnapshot.getString("qr_code"));
                        }
                    }
                    if(qrValues.contains(qrCode))
                        qr_exist = true;
                    else
                        qr_exist = false;
                    if (qr_exist){
                        alertDialog("This QR is already in use and assigned to another asset. To use this QR please edit the\n" +
                                " asset linked to the QR, or use a new QR that is not assigned to an asset.");
                        qr_value.setText("");
                        qrCode="";
                        qr_exist = false;
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateAssetForm.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMaxDocIDNFCDetails(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        assetNfcDetailsReference.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            nfcIds.add(Integer.parseInt(queryDocumentSnapshot.getId()));
                        }
                        maxDocIdNFCDetails = String.valueOf(getMax(nfcIds));
                        Log.d("max_nfc_details_id :",maxDocIdNFCDetails);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateAssetForm.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getNFCDetails(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        assetNfcDetailsReference.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            nfc_tags.add(queryDocumentSnapshot.getString("nfc_code"));
                        }

                    }
                    if(nfc_tags.contains(tagId))
                        nfc_tags_exist = true;
                    else
                        nfc_tags_exist = false;
                    if (nfc_tags_exist){
                        alertDialog("This tag is already in use and assigned to another asset. To use this tag please edit the\n" +
                                " asset linked to the tag, or use a new tag that is not assigned to an asset.");
                        tv_tag.setText("Tag ID");
                        tv_tag_confirmation.setText("Confirm Tag ID");
                        iv_nfcTag_one.setImageResource(R.drawable.nfc);
                        iv_nfcTag_two.setImageResource(R.drawable.nfc);
                        nfc_tags_exist = false;
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateAssetForm.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_cross:
                alertDialogWithYesNo("Are you sure you'd like to close this form ? Any unsaved data will be lost.");
                break;
            case R.id.bt_owned:
                ownership = "Owned";
                owner = 1;
                setSelectedButtonBackground(bt_owned);
                setDefaultButtonBackground(bt_hired);
                ll_owner.setVisibility(View.VISIBLE);
                sp_owner.setVisibility(View.GONE);
                tv_owner.setVisibility(View.VISIBLE);
                tv_owner.setText("McGee Group");
                break;
            case R.id.bt_hired:
                ownership = "Hired";
                setSelectedButtonBackground(bt_hired);
                setDefaultButtonBackground(bt_owned);
                ll_owner.setVisibility(View.VISIBLE);
                sp_owner.setVisibility(View.VISIBLE);
                tv_owner.setVisibility(View.GONE);
                getOwner();
                break;
            case R.id.bt_discard:
                iv_nfcTag_one.setImageResource(R.drawable.nfc);
                iv_nfcTag_two.setImageResource(R.drawable.nfc);
                tv_tag.setText("Tag ID");
                tv_tag_confirmation.setText("Confirm Tag ID");
                break;
            case R.id.rl_date_picker_1:
                 selectedRegimeElementRow=1;
                datePickerDialog.show();
                break;
            case R.id.rl_date_picker_2:
                selectedRegimeElementRow=2;
                datePickerDialog.show();
                break;
            case R.id.rl_date_picker_3:
                selectedRegimeElementRow=3;
                datePickerDialog.show();
                break;
            case R.id.rl_date_picker_4:
                selectedRegimeElementRow=4;
                datePickerDialog.show();
                break;
            case R.id.rl_date_picker_5:
                selectedRegimeElementRow=5;
                datePickerDialog.show();
                break;
            case R.id.rl_date_picker_6:
                selectedRegimeElementRow=6;
                datePickerDialog.show();
                break;
            case R.id.rl_date_picker_7:
                selectedRegimeElementRow=7;
                datePickerDialog.show();
                break;
            case R.id.rl_date_picker_8:
                selectedRegimeElementRow=8;
                datePickerDialog.show();
                break;
            case R.id.rl_date_picker_9:
                selectedRegimeElementRow=9;
                datePickerDialog.show();
                break;
            case R.id.rl_date_picker_10:
                selectedRegimeElementRow=10;
                datePickerDialog.show();
                break;
            case R.id.bt_scan_qr:
                scanQR();
                break;
            case R.id.bt_submit:
                manageYesNoRadioButton();
                manageOnOffRadioButton();
                regimeElementRowValues();
                if (checkValidation()){
                  submitAssetDetails();
                }
                break;
        }
    }

    private void setSelectedButtonBackground(Button selected_button) {
        selected_button.setBackground(getResources().getDrawable(R.drawable.login_button_background));
        selected_button.setTextColor(getResources().getColor(R.color.white));
    }

    private void setDefaultButtonBackground(Button default_button) {
        default_button.setBackground(getResources().getDrawable(R.drawable.edit_text_background));
        default_button.setTextColor(getResources().getColor(R.color.black_shade_two));
    }

//

    private void scanQR(){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setOrientationLocked(true);
        integrator.setPrompt(" ");
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("RDYFJF " ,intentResult.getContents());
                qrCode = intentResult.getContents();
                getQRDetails();
                bt_scan_qr.setBackgroundResource(R.color.green);
                qr_value.setVisibility(View.VISIBLE);
                qr_value.setText("QR Value : "+qrCode);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
            bt_scan_qr.setBackgroundResource(R.drawable.blue_button_background);
            qr_value.setVisibility(View.GONE);
        }
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_asset_type:
                clearView();
                assetTypeId = arrayList_asset_type_id.get(position);
                assetTypeName = arrayList_asset_type_name.get(position);
                groupId = arrayList_group_id.get(position);
                selectedRegimeId = arrayList_regime_id.get(position);
                Log.d("RTERTE :" , String.valueOf(selectedRegimeId));
                getRegimeName(selectedRegimeId);
                getRegimeElements(selectedRegimeId);
                identificationMethod = arrayList_identification_method.get(position);
                bt_discard.setVisibility(View.VISIBLE);
                if (identificationMethod.equals("NFC")){
                    ll_tag_one.setVisibility(View.VISIBLE);
                    ll_tag_two.setVisibility(View.VISIBLE);
                    bt_discard.setText("Discard NFC");
                    ll_uid.setVisibility(View.GONE);
                    ll_qr.setVisibility(View.GONE);
                    bt_scan_qr.setBackgroundResource(R.drawable.blue_button_background);
                    qr_value.setVisibility(View.GONE);
                    qrCode = "";
                    qr_value.setText("");
                }else if (identificationMethod.equals("UID")){
                    ll_tag_one.setVisibility(View.GONE);
                    ll_tag_two.setVisibility(View.GONE);
                    tagId ="";
                    bt_discard.setVisibility(View.GONE);
                    ll_uid.setVisibility(View.VISIBLE);
                    ll_qr.setVisibility(View.GONE);
                    bt_scan_qr.setBackgroundResource(R.drawable.blue_button_background);
                    qr_value.setVisibility(View.GONE);
                    qrCode = "";
                    qr_value.setText("");
                }else if (identificationMethod.equals("QR")){
                    ll_tag_one.setVisibility(View.GONE);
                    ll_tag_two.setVisibility(View.GONE);
                    tagId ="";
                    bt_discard.setVisibility(View.GONE);
                    ll_uid.setVisibility(View.GONE);
                    ll_qr.setVisibility(View.VISIBLE);
                    tagId="";
                }
                break;
            case R.id.sp_manufacturer:
                 manufacturer = arrayList_manufacturer_name.get(position);
                break;
            case R.id.sp_model:
                 model = arrayList_model_name.get(position);
                break;
            case R.id.sp_owner:
                 owner = arrayList_owner_id.get(position);
                break;
        }
    }

    private Boolean checkValidation(){
        Log.d("Regime element:" ,regimeElementFirstValue+"  "+regimeElementSecondValue+"  "+regimeElementThirdValue+"  "+regimeElementFourthValue);
        if (assetTypeName.equals(""))
            Dialog.alertDialog(this, "Please select asset type.");
        else if (regimeName.equals(""))
            Dialog.alertDialog(this, "No regime found.");
        else if (manufacturer.equals(""))
            Dialog.alertDialog(this, "Please select manufacturer.");
        else if (model.equals(""))
            Dialog.alertDialog(this,"Please select model.");
        else if (et_asset_name.getText().toString().equals("")) {
            Dialog.alertDialog(this, "Please enter asset name.");
        }else if (et_asset_uid.getText().toString().equals("")) {
            Dialog.alertDialog(this, "Please enter asset uid.");
        }else if (owner==0) {
            Dialog.alertDialog(this, "Please select owner.");
        }else if (identificationMethod.equals("QR") && qrCode.equals("")) {
            Dialog.alertDialog(this, "Please scan QR.");
        }else if (identificationMethod.equals("NFC") && tv_tag.getText().toString().equals("Tag ID")) {
            Dialog.alertDialog(this, "Please scan NFC Tag.");
        } else if (identificationMethod.equals("NFC") && tv_tag_confirmation.getText().toString().equals("Confirm Tag ID"))
            Dialog.alertDialog(this, "Please scan NFC Tag again.");
        else if (ll_regime_element_1.getVisibility()==View.VISIBLE && regimeElementFirstValue.equals(""))
            Dialog.alertDialog(this, "Please select regime elements value.");
        else if (ll_regime_element_2.getVisibility()==View.VISIBLE && regimeElementSecondValue.equals(""))
            Dialog.alertDialog(this, "Please select regime elements value.");
        else if (ll_regime_element_3.getVisibility()==View.VISIBLE && regimeElementThirdValue.equals(""))
            Dialog.alertDialog(this, "Please select regime elements value.");
        else if (ll_regime_element_4.getVisibility()==View.VISIBLE && regimeElementFourthValue.equals(""))
            Dialog.alertDialog(this, "Please select regime elements value.");
        else if (ll_regime_element_5.getVisibility()==View.VISIBLE && regimeElementFiveValue.equals(""))
            Dialog.alertDialog(this, "Please select regime elements value.");
        else if (ll_regime_element_6.getVisibility()==View.VISIBLE && regimeElementSixValue.equals(""))
            Dialog.alertDialog(this, "Please select regime elements value.");
        else if (ll_regime_element_7.getVisibility()==View.VISIBLE && regimeElementSevenValue.equals(""))
            Dialog.alertDialog(this, "Please select regime elements value.");
        else if (ll_regime_element_8.getVisibility()==View.VISIBLE && regimeElementEightValue.equals(""))
            Dialog.alertDialog(this, "Please select regime elements value.");
        else if (ll_regime_element_9.getVisibility()==View.VISIBLE && regimeElementNineValue.equals(""))
            Dialog.alertDialog(this, "Please select regime elements value.");
        else if (ll_regime_element_10.getVisibility()==View.VISIBLE && regimeElementTenValue.equals(""))
            Dialog.alertDialog(this, "Please select regime elements value.");
        else
            isValidationDone = true;
        return isValidationDone;
    }

    private void submitAssetDetails() {
        progressDialog.show();
        assetName = et_asset_name.getText().toString();
        assetUID = et_asset_uid.getText().toString();
        if (maxDocIdAssetDetails.equals(""))
            assetDetailsId = 1;
        else
            assetDetailsId = Integer.parseInt(maxDocIdAssetDetails)+1;
       currentDate = AppData.date();
currentTime = AppData.Time();
        Map<String, Object> mapAssetDetails = new HashMap<>();
        mapAssetDetails.put("asset_group_id", Integer.valueOf(groupId));
        mapAssetDetails.put("asset_name", assetName);
        mapAssetDetails.put("asset_number", assetUID);
        mapAssetDetails.put("asset_owner", owner);
        mapAssetDetails.put("asset_type_id",assetTypeId );
        mapAssetDetails.put("assign_location", siteLocationId);
        mapAssetDetails.put("assign_site", owner);
        mapAssetDetails.put("previous_assign_location", 0);
        mapAssetDetails.put("previous_assign_site",0);
        mapAssetDetails.put("start_routine_inspection_date",currentDate);
        mapAssetDetails.put("routine_inspection_duration",routine_inspection_duration);
        mapAssetDetails.put("next_routine_inspection_date",next_routine_inspection_date);
        mapAssetDetails.put("asset_current_status","Compliant");
        mapAssetDetails.put("asset_inspection_status","");
        mapAssetDetails.put("create_user",userId);
        mapAssetDetails.put("created_at", FieldValue.serverTimestamp());
        mapAssetDetails.put("date_acquired","");
        mapAssetDetails.put("hire_in_price_daily","");
        mapAssetDetails.put("hire_out_price_daily","");
        mapAssetDetails.put("id",assetDetailsId);
        mapAssetDetails.put("insert_date",currentDate);
        mapAssetDetails.put("insert_time",currentTime);
        mapAssetDetails.put("manufacturer",manufacturer);
        mapAssetDetails.put("model",model);
        mapAssetDetails.put("nfc_code",tagId);
        mapAssetDetails.put("ownership",ownership);
        mapAssetDetails.put("purchase_price","");
        mapAssetDetails.put("qr_code",qrCode);
        mapAssetDetails.put("regime_id",selectedRegimeId);
        mapAssetDetails.put("status","Active");
        mapAssetDetails.put("update_date","");
        mapAssetDetails.put("update_time","");
        mapAssetDetails.put("update_user",0);
        mapAssetDetails.put("updated_at","");
        mapAssetDetails.put("submit_type","App");
        mapAssetDetails.put("time_second_format", AppData.getTimeSecond());
        mapAssetDetails.put("device_id",device_id);
        mapAssetDetails.put("last_inspected_user_id",0);
        mapAssetDetails.put("last_inspection_date","");
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = assetDetailsReference;
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    assetDetailsReference.document(String.valueOf(assetDetailsId)).set(mapAssetDetails);
                    submitRegimeElementsDetails();
                    if(identificationMethod.equals("NFC"))
                        submitNFCDetails();
                    else if (identificationMethod.equals("QR"))
                        submitQRDetails();
                    toConfirmation("Asset successfully created.");
                    updateNextThroughExamDate(assetDetailsId);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(CreateAssetForm.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void submitRegimeElementsDetails() {
        progressDialog.show();
        int id =0;
        if (maxDocIdRegimeDetails.equals(""))
            id = 1;
        else
            id = Integer.parseInt(maxDocIdRegimeDetails)+1;
       currentDate = AppData.date();
currentTime = AppData.Time();
        arrayList_regime_element_value.clear();
        if (!regimeElementFirstValue.equals("")) {
            arrayList_regime_element_value.add(0,regimeElementFirstValue);
            arrayList_regime_element_view.add(0,view_type_one);
            regimeDateFormat.add(regime_date_format_one);
        }
        if (!regimeElementSecondValue.equals("")) {
            arrayList_regime_element_value.add(1, regimeElementSecondValue);
            arrayList_regime_element_view.add(1, view_type_two);
            regimeDateFormat.add(regime_date_format_two);
        }
        if (!regimeElementThirdValue.equals("")) {
            arrayList_regime_element_value.add(2, regimeElementThirdValue);
            arrayList_regime_element_view.add(2, view_type_three);
            regimeDateFormat.add(regime_date_format_three);
        }
        if (!regimeElementFourthValue.equals("")) {
            arrayList_regime_element_value.add(3,regimeElementFourthValue);
            arrayList_regime_element_view.add(3,view_type_four);
            regimeDateFormat.add(regime_date_format_four);
        }
        if (!regimeElementFiveValue.equals("")) {
            arrayList_regime_element_value.add(4,regimeElementFiveValue);
            arrayList_regime_element_view.add(4,view_type_five);
            regimeDateFormat.add(regime_date_format_five);
        }
        if (!regimeElementSixValue.equals("")) {
            arrayList_regime_element_value.add(5,regimeElementSixValue);
            arrayList_regime_element_view.add(5,view_type_six);
            regimeDateFormat.add(regime_date_format_six);
        }
        if (!regimeElementSevenValue.equals("")) {
            arrayList_regime_element_value.add(6,regimeElementSevenValue);
            arrayList_regime_element_view.add(6,view_type_seven);
            regimeDateFormat.add(regime_date_format_seven);
        }
        if (!regimeElementEightValue.equals("")) {
            arrayList_regime_element_value.add(7,regimeElementEightValue);
            arrayList_regime_element_view.add(7,view_type_eight);
            regimeDateFormat.add(regime_date_format_eight);
        }
        if (!regimeElementNineValue.equals("")) {
            arrayList_regime_element_value.add(8,regimeElementNineValue);
            arrayList_regime_element_view.add(8,view_type_nine);
            regimeDateFormat.add(regime_date_format_nine);
        }
        if (!regimeElementTenValue.equals("")) {
            arrayList_regime_element_value.add(9,regimeElementTenValue);
            arrayList_regime_element_view.add(9,view_type_ten);
            regimeDateFormat.add(regime_date_format_ten);
        }
        for (int i=0; i< arrayList_regime_element_id.size(); i++){
            Map<String, Object> mapRegimeDetails = new HashMap<>();
            mapRegimeDetails.put("id",id+i);
            mapRegimeDetails.put("asset_details_id",assetDetailsId);
            mapRegimeDetails.put("date",currentDate);
            mapRegimeDetails.put("next_thorough_examination","");
            mapRegimeDetails.put("insert_user",userId);
            mapRegimeDetails.put("insert_time",currentTime);
            mapRegimeDetails.put("insert_date",currentDate);
            mapRegimeDetails.put("created_at", FieldValue.serverTimestamp());
            mapRegimeDetails.put("updated_at", FieldValue.serverTimestamp());
            mapRegimeDetails.put("submit_type","App");
            mapRegimeDetails.put("status","Active");
            mapRegimeDetails.put("regime_element_id", arrayList_regime_element_id.get(i));
            mapRegimeDetails.put("regime_element_name",arrayList_selected_inspection_element_name.get(i));
            mapRegimeDetails.put("regime_element_value",arrayList_regime_element_value.get(i));
            mapRegimeDetails.put("regime_element_view",arrayList_regime_element_view.get(i));
            mapRegimeDetails.put("current_date_format",regimeDateFormat.get(i));
            mapRegimeDetails.put("time_second_format", AppData.getTimeSecond());
            Log.d("RDTUDTY :" ,arrayList_regime_element_value.get(i));
            if (arrayList_selected_inspection_element_name.get(i).equals("Next Thorough Inspection Date"))
                next_through_inspection_date = arrayList_regime_element_value.get(i);
            regimeDetailsReference.document(String.valueOf(id+i)).set(mapRegimeDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
//                        Toast.makeText(CreateAssetForm.this, "Regime details submitted successfully.", Toast.LENGTH_SHORT).show();

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(CreateAssetForm.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateNextThroughExamDate(int assetId){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = assetDetailsReference.whereEqualTo("status", "Active").whereEqualTo("id",assetId);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            String doc_id = queryDocumentSnapshot.getId();
                            Map<String, Object> update_nte = new HashMap<>();
                            update_nte.put("date_1",next_through_inspection_date);
                            assetDetailsReference.document(doc_id).update(update_nte);
                            Log.d("RDYT :" ,next_through_inspection_date);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateAssetForm.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitNFCDetails() {
        progressDialog.show();
        if (maxDocIdNFCDetails.equals(""))
            nfcDetailsId = 1;
        else
            nfcDetailsId = Integer.parseInt(maxDocIdNFCDetails)+1;

        Map<String, Object> mapNfcDetails = new HashMap<>();
        mapNfcDetails.put("id",nfcDetailsId);
        mapNfcDetails.put("insert_user",userId);
        mapNfcDetails.put("insert_time",currentTime);
        mapNfcDetails.put("insert_date",currentDate);
        mapNfcDetails.put("asset_id",assetDetailsId);
        mapNfcDetails.put("nfc_code",tv_tag.getText().toString());
        mapNfcDetails.put("status","Active");
        mapNfcDetails.put("time_second_format", AppData.getTimeSecond());
        Query query = assetNfcDetailsReference;
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    assetNfcDetailsReference.document(String.valueOf(nfcDetailsId)).set(mapNfcDetails);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(CreateAssetForm.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitQRDetails() {
        progressDialog.show();
        if (maxDocIdQRDetails.equals(""))
            qrDetailsId = 1;
        else
            qrDetailsId = Integer.parseInt(maxDocIdQRDetails)+1;

        Map<String, Object> mapQRDetails = new HashMap<>();
        mapQRDetails.put("id",qrDetailsId);
        mapQRDetails.put("insert_user",userId);
        mapQRDetails.put("insert_time",currentTime);
        mapQRDetails.put("insert_date",currentDate);
        mapQRDetails.put("asset_id",assetDetailsId);
        mapQRDetails.put("qr_code",qrCode);
        mapQRDetails.put("status","Active");
        mapQRDetails.put("time_second_format", AppData.getTimeSecond());
        Query query = assetQRDetailsReference;
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    assetQRDetailsReference.document(String.valueOf(qrDetailsId)).set(mapQRDetails);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(CreateAssetForm.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Intent assignSite = new Intent(CreateAssetForm.this,AssetAssignSiteActivity.class);
                        startActivity(assignSite);
                        finish();
                    }
                });
        alertDialog.show();
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
        handleNfcIntent(intent);
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
    private void handleNfcIntent(Intent intent)
    {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) ||NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                tagId = ByteArrayToHexString(Objects.requireNonNull(tag).getId());
                Log.d("NFC_SERIAL_NO_Tag_Manager :" ,tagId);
                ndefmessage(tag);
                Log.d("NDEF_Message", "Detected: " +ndefmessage(tag));
            } else {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                tagId = ByteArrayToHexString(Objects.requireNonNull(tag).getId());
                Log.d("NFC_SERIAL_NO_Tag_Manager :" ,tagId);
                setTagValue();
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
                setTagValue();
                ndef.close();
            }
            else
                scanFailedDialog();
        } catch (IOException | FormatException e) {
            Log.e(TAG, "An unexpected error occurred", e);
        }
        return null;
    }

    private void setTagValue(){
        getNFCDetails();
        String tag_one = tv_tag.getText().toString();
        if (tag_one.equals("Tag ID")) {
            tv_tag.setText(tagId);
            iv_nfcTag_one.setImageResource(R.drawable.nfc_green);
//            successfullyScanDialog();
        }else {
            tv_tag_confirmation.setText(tagId);
            iv_nfcTag_two.setImageResource(R.drawable.nfc_green);
            if (!tv_tag_confirmation.getText().toString().equals(tv_tag.getText().toString())) {
                alertDialog("NFC Tags does not match.Please try again.");
                tv_tag_confirmation.setText("Confirm Tag ID");
                iv_nfcTag_two.setImageResource(R.drawable.nfc);
            }
        }
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

    private void alertDialogWithYesNo(String message){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(message);
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
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

    private void toConfirmation(String message) {
        progressDialog.dismiss();
        final View popupView = getLayoutInflater().inflate(R.layout.change_pass_popup_layout, null, true);
         boolean isActivityInForeground = CreateAssetForm.this.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED);
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
                Intent toDashboard = new Intent(CreateAssetForm.this, AssetManagementDashboard.class);
                startActivity(toDashboard);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {

    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}