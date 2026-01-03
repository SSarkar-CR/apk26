package com.crate.crateam.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crate.crateam.R;
import com.crate.crateam.adapters.HourAdapter;
import com.crate.crateam.adapters.MinuteAdapter;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.CustomSearchableSpinner;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.SessionManager;
import com.crate.crateam.utility.WaitingTimeInterfaces;
import com.crate.crateam.zebraPrintingTools.DeviceListActivity;
import com.crate.crateam.zebraPrintingTools.PrintingLayoutFormat;
import com.crate.crateam.zebraPrintingTools.ZPLConverter;
import com.github.gcacace.signaturepad.views.SignaturePad;
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
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.SGD;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.ZebraPrinterLinkOs;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class SupplyFormOperator extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private ImageView iv_cross, iv_camera, iv_photo_one, iv_photo_two, iv_close_one, iv_close_two,iv_add_driver;
    private Button bt_ok, bt_cancel_waiting, bt_select_time, bt_exit, bt_add_reg_no, bt_discard, bt_driver_sign, bt_operator_sign, bt_third_party_yes, bt_third_party_no, bt_print_ticket_yes, bt_print_ticket_no, bt_submit,
            bt_submit_new, bt_weight, bt_unit;
    private LinearLayout ll_assign_site, ll_operator_sign, ll_driver_sign, ll_nfc_tag, ll_images, ll_third_party_sign, ll_waste_carrier_no_new,
            ll_internal_reg,ll_internal_users;
    private TextView tv_waiting_time, tv_new_reg_no, tv_ticket_no, tv_user_name, tv_date, tv_time,tv_current_site_address,  tv_vehicle_type, tv_haulier,
            tv_net_weight, tv_clear_operator, tv_clear_driver, tv_clear_third_party, tv_driver_name, tv_vehicle_reg_no,tv_material_value_caution;
    private EditText et_new_driver_name,et_new_reg_no, et_gross_weight, et_tare,et_unit, et_waiting_time, et_external_ticket, et_comment,et_add_driver_name;
    private SignaturePad signaturePadDriver, signaturePadOperator, signaturePadThirdParty;
    private CustomSearchableSpinner sp_current_site, sp_materials, sp_delivery_site,sp_delivery_reference,sp_delivery_area, sp_reg_no, sp_hour, sp_minute, sp_new_vehicle_type, sp_new_haulier,
            sp_driver_name;
    private String user_name = "", image1="", image2="", selected_delivery_site_name = "", gross = "", tare = "", driver_sign = "", operator_sign = "",
            third_party_sign = "", print_ticket = "", external_ticket = "", registration_no = "", third_party_signature_option = "", store_sic_code = "",
            current_site_address = "", current_site_name = "", current_date = "", current_time = "", material_des = "", hour = "", minute = "", deviceMacAddress = "",
            new_driver_name = "", new_reg_no = "", new_vehicle_type = "", new_waste_carrier = "", new_haulier_company = "", weight_unit = "", comment = "", doc_id = "", ewc_code = "",
            selected_delivery_site_address="",max_docId_user_details="",max_docId_vehicle_details="",
            external_vehicle="",latitude="",longitude="",doc_id_task="",vehicle_type_id = "",haulier_company_id ="",waste_carrier_id = "",material_value="",
            selected_delivery_reference_name="",selected_delivery_area_name="";
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference  materialReference, assignMaterialReference, vehicleDetailsReference, supplyWasteFormsReference,
            newRegistrationReference, userDetailsReference, assignSiteReference, sortKeyTableReference, siteManagementReference,
            taskManagementReference,websiteSettingsReference,currentVehicleReference,inspectionSubmissionReference,deliveryReferenceDetails,deliveryLocationReference;
    private boolean hasImage = false, hasImageTwo = false, isSigned = false, isSignedOperator = false, isSignedThird = false, validation_done = false,
            validation_window = false,showAddDriverButton=true,isInspectionDone = false;
    private Bitmap getDrawable1, getDrawable2;
    private int user_id = 0, user_role_id, selected_delivery_site_id = 0, site_id = 0, material_id = 0, registration_id = 0, haulier_id = 0,
             selected_user_id = 0, doc_id_length = 0,address_management_id=0,new_vehicle_id=0,new_user_id=0,doc_id_length_task=0,
            selected_delivery_reference_id =0,selected_delivery_area_id=0;
    private ProgressDialog progressDialog, printerDialog;
    double net = 0.0;
    private PopupWindow popupWindow;
    private AlertDialog alertDialog, alertDialog_time;
    private ArrayList<String>allUserNames = new ArrayList<>();
    private ArrayList<Integer> allUserIds= new ArrayList<>();
    private ArrayList<String> new_vehicle_type_list = new ArrayList<>();
    private ArrayList<Integer> assignMaterialDocumentId = new ArrayList<>();
    private ArrayList<Integer> material_id_list = new ArrayList<>();
    private ArrayList<String> store_ewc_code_list = new ArrayList<>();
    private ArrayList<String> ewc_code_list = new ArrayList<>();
    private ArrayList<String> store_material_value_list = new ArrayList<>();
    private ArrayList<String> material_value_list = new ArrayList<>();
    private ArrayList<Integer> registration_no_id_list = new ArrayList<>();
    private ArrayList<String> newHaulierList = new ArrayList<>();
    private ArrayList<String> haulierList= new ArrayList<>();
    private ArrayList<String> haulierCarrierNoList = new ArrayList<>();
    private ArrayList<String> vehicle_type_list= new ArrayList<>();
    private ArrayList<String> material_des_list = new ArrayList<>();
    private ArrayList<String> registration_no_list = new ArrayList<>();
    private ArrayList<String>external_vehicle_list = new ArrayList<>();
    private ArrayList<String> arrayList_hour = new ArrayList<>();
    private ArrayList<String> arrayList_minute = new ArrayList<>();
    private ArrayList<String> arrayList_second = new ArrayList<>();
    private ArrayList<Integer> siteId = new ArrayList<>();
    private ArrayList<String> siteName = new ArrayList<>();
    private ArrayList<Integer> store_material_doc_list = new ArrayList<>();
    private ArrayList<String> store_material_des_list = new ArrayList<>();
    private ArrayList<Integer> selected_material_id_list = new ArrayList<>();
    private ArrayList<Integer> arrayList_user_id = new ArrayList<>();
    private ArrayList<String> arrayList_user_name = new ArrayList<>();
    private ArrayList<Integer> arrayList_site_id = new ArrayList<>();
    private ArrayList<Integer> arrayList_delivery_reference_id = new ArrayList<>();
    private ArrayList<String> arrayList_delivery_reference_name = new ArrayList<>();
    private ArrayList<Integer> allDeliveryReferenceIds= new ArrayList<>();
    private ArrayList<String>allDeliveryReferenceNames = new ArrayList<>();
    private ArrayList<Integer> arrayList_delivery_area_id = new ArrayList<>();
    private ArrayList<String> arrayList_delivery_area_name = new ArrayList<>();
    private ArrayList<Integer> allDeliveryAreaIds= new ArrayList<>();
    private ArrayList<String>allDeliveryAreaNames = new ArrayList<>();
    private LinearLayout ll_add_new_reg, ll_gross, ll_tare, ll_net, ll_unit;
    private ArrayList<String> arrayList_assign_site_name;
    private ArrayList<Integer> arrayList_assign_site_id;
    private RecyclerView rv_hour, rv_minute;
    private HourAdapter hourAdapter;
    private MinuteAdapter minuteAdapter;
    protected static final String TAG = "TAG";
    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    BluetoothAdapter mBluetoothAdapter;
    private ZebraPrinter printer;
    private Connection connection;
    private Uri imageUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.supply_form_operator);
        initView();
    }

    private void initView() {
        FirestoreManager.initPersistentIndexManager();
        progressDialog = Dialog.showProgressDialog(this);
        supplyWasteFormsReference = db.collection("CR_supply_waste_forms");
        vehicleDetailsReference = db.collection("CR_vehicle_details");
        assignMaterialReference = db.collection("CR_assign_material");
        materialReference = db.collection("CR_material");
        newRegistrationReference = db.collection("CR_new_registration");
        userDetailsReference = db.collection("CR_user_details");
        siteManagementReference = db.collection("CR_site_management");
        assignSiteReference = db.collection("CR_assign_site");
        sortKeyTableReference = db.collection("CR_sort_key");
        taskManagementReference = db.collection("CR_task_management");
        websiteSettingsReference = db.collection("CR_website_settings");
        currentVehicleReference = db.collection("CR_current_vehicle");
        inspectionSubmissionReference = db.collection("CR_inspection_submission");
        deliveryReferenceDetails = db.collection("CR_delivery_reference_details");
        deliveryLocationReference = db.collection("CR_delivery_location_details");
        SessionManager sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_name = user.get(SessionManager.KEY_FULL_NAME);
        user_id = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        String role = user.get(SessionManager.KEY_ROLE_ONE);
        if (role != null)
            user_role_id = Integer.parseInt(role);
        Log.d("USER_ID : " , user_name);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        iv_cross = findViewById(R.id.iv_cross);
        iv_camera = findViewById(R.id.iv_camera);
        iv_photo_one = findViewById(R.id.iv_photo_one);
        iv_photo_two = findViewById(R.id.iv_photo_two);
        iv_close_one = findViewById(R.id.iv_close_one);
        iv_close_two = findViewById(R.id.iv_close_two);
        iv_add_driver = findViewById(R.id.iv_add_driver);
        bt_third_party_yes = findViewById(R.id.bt_third_party_yes);
        bt_third_party_no = findViewById(R.id.bt_third_party_no);
        bt_print_ticket_yes = findViewById(R.id.bt_print_ticket_yes);
        bt_print_ticket_no = findViewById(R.id.bt_print_ticket_no);
        bt_discard = findViewById(R.id.bt_discard);
        bt_exit = findViewById(R.id.bt_exit);
        bt_select_time = findViewById(R.id.bt_select_time);
        bt_add_reg_no = findViewById(R.id.bt_add_reg_no);
        bt_driver_sign = findViewById(R.id.bt_driver_sign);
        bt_operator_sign = findViewById(R.id.bt_operator_sign);
        bt_submit = findViewById(R.id.bt_submit);
        ll_internal_reg = findViewById(R.id.ll_internal_reg);
        ll_internal_users = findViewById(R.id.ll_internal_users);
        ll_assign_site = findViewById(R.id.ll_assign_site);
        ll_driver_sign = findViewById(R.id.ll_driver_sign);
        ll_operator_sign = findViewById(R.id.ll_operator_sign);
        ll_third_party_sign = findViewById(R.id.ll_third_party_sign);
        ll_nfc_tag = findViewById(R.id.ll_nfc_tag);
        ll_images = findViewById(R.id.ll_images);
        tv_clear_driver = findViewById(R.id.tv_clear_driver);
        tv_clear_operator = findViewById(R.id.tv_clear_operator);
        tv_clear_third_party = findViewById(R.id.tv_clear_third_party);
        tv_user_name = findViewById(R.id.tv_user_name);
        tv_user_name.setText(user_name);
        tv_driver_name = findViewById(R.id.tv_driver_name);
        tv_vehicle_reg_no = findViewById(R.id.tv_vehicle_reg_no);
        et_gross_weight = findViewById(R.id.et_gross_weight);
        et_tare = findViewById(R.id.et_tare);
        et_unit = findViewById(R.id.et_unit);
        tv_net_weight = findViewById(R.id.tv_net_weight);
        tv_current_site_address = findViewById(R.id.tv_current_site_address);
        tv_material_value_caution = findViewById(R.id.tv_material_value_caution);
        et_external_ticket = findViewById(R.id.et_external_ticket);
        et_comment = findViewById(R.id.et_comment);
        et_add_driver_name = findViewById(R.id.et_add_driver_name);
        tv_ticket_no = findViewById(R.id.tv_ticket_no);
        tv_new_reg_no = findViewById(R.id.tv_new_reg_no);
        tv_waiting_time = findViewById(R.id.tv_waiting_time);
        tv_date = findViewById(R.id.tv_date);
        tv_time = findViewById(R.id.tv_time);
        tv_vehicle_type = findViewById(R.id.tv_vehicle_type);
        tv_haulier = findViewById(R.id.tv_haulier);
        signaturePadDriver = findViewById(R.id.signature_pad_driver);
        signaturePadOperator = findViewById(R.id.signature_pad_operator);
        signaturePadThirdParty = findViewById(R.id.signature_pad_third_party);
        sp_materials = findViewById(R.id.sp_materials);
        sp_current_site = findViewById(R.id.sp_current_site);
        sp_driver_name = findViewById(R.id.sp_driver_name);
        sp_delivery_site = findViewById(R.id.sp_delivery_site);
        sp_delivery_reference = findViewById(R.id.sp_delivery_reference);
        sp_delivery_area = findViewById(R.id.sp_delivery_area);
        sp_reg_no = findViewById(R.id.sp_reg_no);
        ll_add_new_reg = findViewById(R.id.ll_add_new_reg);
        ll_gross = findViewById(R.id.ll_gross);
        ll_tare = findViewById(R.id.ll_tare);
        ll_net = findViewById(R.id.ll_net);
        ll_unit = findViewById(R.id.ll_unit);
        bt_weight = findViewById(R.id.bt_weight);
        bt_unit = findViewById(R.id.bt_unit);
        et_gross_weight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    gross = s.toString();
                    if (!(tare.isEmpty())) {
                        if (Double.parseDouble(gross) >= Double.parseDouble(tare)) {
                            net = Double.parseDouble(gross) - Double.parseDouble(tare);
                            tv_net_weight.setText(String.valueOf(net));
                        } else
                            tv_net_weight.setText("");
                    }
                } else if (et_tare.getText().toString().trim().isEmpty() || et_gross_weight.getText().toString().trim().isEmpty()) {
                    gross = "0.0";
                    tv_net_weight.setText("");
                }
            }
        });
        et_tare.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    tare = s.toString();
                    if (!(gross.isEmpty())) {
                        if (Double.parseDouble(gross) >= Double.parseDouble(tare)) {
                            net = Double.parseDouble(gross) - Double.parseDouble(tare);
                            tv_net_weight.setText(String.valueOf(net));
                        } else
                            tv_net_weight.setText("");
                    }
                } else if (et_gross_weight.getText().toString().trim().isEmpty() || et_tare.getText().toString().trim().isEmpty()) {
                    tare = "0.0";
                    tv_net_weight.setText("");
                }
            }
        });
        current_date = AppData.date();
        current_time = AppData.Time();
        tv_date.setText(current_date);
        tv_time.setText(current_time);
        if (user_role_id != 4) {
            tv_vehicle_reg_no.setVisibility(View.GONE);
            tv_driver_name.setVisibility(View.GONE);
            sp_reg_no.setVisibility(View.VISIBLE);
            sp_driver_name.setVisibility(View.VISIBLE);
        } else {
            getInspectionSubmission();
        }
        getSiteAddress();
        generateTicket();
        initializeOnClick();
        setSignedDriver();
        setSignOperator();
        setSignedThirdParty();
        getMaximumLength();
        getMaximumLengthTaskManagement();
        fetchVehicleRegistrationNumber();
        getMaxDocIDUserDetails();
        getMaxDocIDVehicleDetails();
        if (!AppData.internetOnline(this))
            progressDialog.dismiss();
    }

 

    private void assignMaterial(int site_id) {
        Query query = assignMaterialReference.whereEqualTo("site_id", site_id).whereEqualTo("status", "Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    String material_ids = "";
                    material_id_list.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        material_ids = queryDocumentSnapshot.getString("material_ids");
                        assignMaterialDocumentId.add(Integer.parseInt(queryDocumentSnapshot.getId()));
                    }
                    if (material_ids != null && material_ids.contains(",")) {
                        ArrayList<String> materialIdListString = new ArrayList<String>(Arrays.asList(material_ids.split(",")));
                        for (int i = 0; i < materialIdListString.size(); i++) {
                            material_id_list.add(Integer.valueOf(materialIdListString.get(i)));
                        }
                    } else {
                        if (material_ids != null ) {
                            if (!material_ids.equals("")) {
                                material_id_list.add(Integer.valueOf(material_ids));
                            }
                        }
                    }
                    Log.d("Material ids:" , String.valueOf(material_id_list));
                    material();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void material() {
        Query query = materialReference.whereEqualTo("status", "Active").whereEqualTo("material_type","Delivery");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    material_des_list.clear();
                    ewc_code_list.clear();
                    store_ewc_code_list.clear();
                    material_value_list.clear();
                    store_material_value_list.clear();
                    store_material_doc_list.clear();
                    store_material_des_list.clear();
                    selected_material_id_list.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        int doc_id = Integer.parseInt(queryDocumentSnapshot.getId());
                        store_material_doc_list.add(doc_id);
                        store_material_des_list.add(queryDocumentSnapshot.getString("material_description"));
                        store_ewc_code_list.add(queryDocumentSnapshot.getString("ewc_code"));
                        store_material_value_list.add(queryDocumentSnapshot.getString("material_value"));
                    }
                    for (int i = 0; i < material_id_list.size(); i++) {
                        for (int j = 0; j < store_material_doc_list.size(); j++) {
                            if (material_id_list.get(i).equals(store_material_doc_list.get(j))) {
                                material_des_list.add(store_material_des_list.get(j));
                                selected_material_id_list.add(material_id_list.get(i));
                                ewc_code_list.add(store_ewc_code_list.get(j));
                                material_value_list.add(store_material_value_list.get(j));
                                break;
                            }
                        }
                    }
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(SupplyFormOperator.this, R.layout.spinner_custom_layout, material_des_list);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                    sp_materials.setAdapter(spinnerArrayAdapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SupplyFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // fetching vehicle registration number from CR_vehicle_details table
    public void fetchVehicleRegistrationNumber() {
        progressDialog.show();
        Query query = vehicleDetailsReference.whereEqualTo("status", "Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        if (documentSnapshot.exists()) {
                            registration_no_list.add(documentSnapshot.getString("registration_no"));
                            external_vehicle_list.add(documentSnapshot.getString("external_vehicle"));
                            registration_no_id_list.add(Objects.requireNonNull(documentSnapshot.getLong("id")).intValue());
                            vehicle_type_list.add(documentSnapshot.getString("vehicle_type"));
                            haulierList.add(documentSnapshot.getString("haulier"));
                            haulierCarrierNoList.add(documentSnapshot.getString("haulier_carrier_no"));
                        }
                    }
                    registration_no_list.ensureCapacity(registration_no_list.size()+1);
                    registration_no_list.add("Add new");
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_custom_layout, registration_no_list);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                    sp_reg_no.setAdapter(spinnerArrayAdapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SupplyFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeOnClick() {
        iv_cross.setOnClickListener(this);
        bt_select_time.setOnClickListener(this);
        bt_discard.setOnClickListener(this);
        bt_driver_sign.setOnClickListener(this);
        bt_operator_sign.setOnClickListener(this);
        tv_clear_driver.setOnClickListener(this);
        tv_clear_operator.setOnClickListener(this);
        tv_clear_third_party.setOnClickListener(this);
        iv_camera.setOnClickListener(this);
        iv_close_one.setOnClickListener(this);
        iv_close_two.setOnClickListener(this);
        iv_photo_one.setOnClickListener(this);
        iv_photo_two.setOnClickListener(this);
        iv_add_driver.setOnClickListener(this);
        bt_third_party_yes.setOnClickListener(this);
        bt_third_party_no.setOnClickListener(this);
        bt_print_ticket_yes.setOnClickListener(this);
        bt_print_ticket_no.setOnClickListener(this);
        bt_add_reg_no.setOnClickListener(this);
        bt_weight.setOnClickListener(this);
        bt_unit.setOnClickListener(this);
        bt_submit.setOnClickListener(this);
        sp_current_site.setOnItemSelectedListener(this);
        sp_materials.setOnItemSelectedListener(this);
        sp_delivery_site.setOnItemSelectedListener(this);
        sp_delivery_reference.setOnItemSelectedListener(this);
        sp_delivery_area.setOnItemSelectedListener(this);
        sp_reg_no.setOnItemSelectedListener(this);
        sp_driver_name.setOnItemSelectedListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_cross:
                finish();
                break;
            case R.id.bt_discard:
                alertDialog("Do you want to leave this form?");
                break;
            case R.id.bt_driver_sign:
                ll_driver_sign.setVisibility(View.VISIBLE);
                bt_driver_sign.setVisibility(View.GONE);
                break;
            case R.id.bt_operator_sign:
                ll_operator_sign.setVisibility(View.VISIBLE);
                bt_operator_sign.setVisibility(View.GONE);
                break;
            case R.id.tv_clear_driver:
                signaturePadDriver.clear();
                Toast.makeText(this, "Sign Cleared", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_clear_operator:
                signaturePadOperator.clear();
                Toast.makeText(this, "Sign Cleared", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_clear_third_party:
                signaturePadThirdParty.clear();
                Toast.makeText(this, "Sign Cleared", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_camera:
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "New Picture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                imageUri = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, REQUEST_CAMERA);
                ll_images.setVisibility(View.VISIBLE);
                break;
            case R.id.iv_photo_one:
                getDrawable1 = ((BitmapDrawable) iv_photo_one.getDrawable()).getBitmap();
                zoomImagePopup(getDrawable1);
                break;
            case R.id.iv_photo_two:
                getDrawable2 = ((BitmapDrawable) iv_photo_two.getDrawable()).getBitmap();
                zoomImagePopup(getDrawable2);
                break;
            case R.id.iv_close_one:
                iv_photo_one.setImageDrawable(getResources().getDrawable(R.drawable.placeholder_image));
                hasImage = false;
                break;
            case R.id.iv_close_two:
                iv_photo_two.setImageDrawable(getResources().getDrawable(R.drawable.placeholder_image));
                hasImage = true;
                hasImageTwo = false;
                break;
            case R.id.bt_third_party_yes:
                bt_third_party_yes.setBackground(getResources().getDrawable(R.drawable.login_button_background));
                bt_third_party_yes.setTextColor(getResources().getColor(R.color.white));
                bt_third_party_no.setBackground(getResources().getDrawable(R.drawable.edit_text_background));
                bt_third_party_no.setTextColor(getResources().getColor(R.color.black_shade_two));
                ll_third_party_sign.setVisibility(View.VISIBLE);
                third_party_signature_option = "Yes";
                break;
            case R.id.bt_third_party_no:
                bt_third_party_no.setBackground(getResources().getDrawable(R.drawable.login_button_background));
                bt_third_party_no.setTextColor(getResources().getColor(R.color.white));
                bt_third_party_yes.setBackground(getResources().getDrawable(R.drawable.edit_text_background));
                bt_third_party_yes.setTextColor(getResources().getColor(R.color.black_shade_two));
                ll_third_party_sign.setVisibility(View.GONE);
                signaturePadThirdParty.clear();
                third_party_signature_option = "No";
                break;
            case R.id.bt_print_ticket_yes:
                print_ticket = "Yes";
                if (checkValidation()) {
                    bt_print_ticket_no.setEnabled(false);
                    bt_discard.setVisibility(View.GONE);
                    iv_cross.setVisibility(View.GONE);
                    bt_print_ticket_yes.setBackground(getResources().getDrawable(R.drawable.login_button_background));
                    bt_print_ticket_yes.setTextColor(getResources().getColor(R.color.white));
                    bt_print_ticket_no.setBackground(getResources().getDrawable(R.drawable.edit_text_background));
                    bt_print_ticket_no.setTextColor(getResources().getColor(R.color.black_shade_two));
                    enableBluetooth();
                }
                break;
            case R.id.bt_print_ticket_no:
                print_ticket = "No";
                bt_discard.setVisibility(View.VISIBLE);
                iv_cross.setVisibility(View.VISIBLE);
                bt_print_ticket_no.setBackground(getResources().getDrawable(R.drawable.login_button_background));
                bt_print_ticket_no.setTextColor(getResources().getColor(R.color.white));
                bt_print_ticket_yes.setBackground(getResources().getDrawable(R.drawable.edit_text_background));
                bt_print_ticket_yes.setTextColor(getResources().getColor(R.color.black_shade_two));
                break;
            case R.id.bt_submit:
                if (checkValidation()) {
                    sendSupplyFormOperator();
                    if (!AppData.internetOnline(this)) {
                        progressDialog.dismiss();
                        toConfirmation("Form submitted successfully.");
                    }
                }
                break;
            case R.id.bt_weight:
                weight_unit = "weight";
                ll_gross.setVisibility(View.VISIBLE);
                ll_tare.setVisibility(View.VISIBLE);
                ll_net.setVisibility(View.VISIBLE);
                ll_unit.setVisibility(View.GONE);
                et_unit.setText("");
                bt_weight.setBackground(getResources().getDrawable(R.drawable.login_button_background));
                bt_unit.setBackground(getResources().getDrawable(R.drawable.edit_text_background));
                bt_weight.setTextColor(getResources().getColor(R.color.white));
                bt_unit.setTextColor(getResources().getColor(R.color.black_shade_two));
                break;
            case R.id.bt_unit:
                weight_unit = "unit";
                ll_gross.setVisibility(View.GONE);
                ll_tare.setVisibility(View.GONE);
                ll_net.setVisibility(View.GONE);
                ll_unit.setVisibility(View.VISIBLE);
                et_gross_weight.setText("");
                et_tare.setText("");
                tv_net_weight.setText("");
                gross = "";
                tare = "";
                net = 0.0;
                bt_weight.setBackground(getResources().getDrawable(R.drawable.edit_text_background));
                bt_unit.setBackground(getResources().getDrawable(R.drawable.login_button_background));
                bt_weight.setTextColor(getResources().getColor(R.color.black_shade_two));
                bt_unit.setTextColor(getResources().getColor(R.color.white));
                break;
            case R.id.bt_add_reg_no:
                customizeWindow();
                if (!AppData.internetOnline(this))
                    progressDialog.dismiss();
                break;
            case R.id.bt_submit_new:
                if (newVehiclePopupValidation()) {
                    selected_user_id = 0;
                    new_reg_no = et_new_reg_no.getText().toString().trim();
                    new_driver_name = et_new_driver_name.getText().toString();
                    if (!new_reg_no.isEmpty())
                      tv_new_reg_no.setText(new_reg_no);
                    tv_driver_name.setText(new_driver_name);
                    tv_vehicle_type.setText(new_vehicle_type);
                    tv_haulier.setText(new_haulier_company);
                    alertDialog.cancel();
                    sp_driver_name.setVisibility(View.GONE);
                    tv_driver_name.setVisibility(View.VISIBLE);
                    tv_new_reg_no.setVisibility(View.VISIBLE);
                    bt_add_reg_no.setText("Edit");
                }
                break;
            case R.id.bt_select_time:
                customWaitingTimePopUp();
                break;
            case R.id.bt_ok:
                if (hour.equals(""))
                    Dialog.alertDialog(this, "Please select hour value.");
                else if (minute.equals(""))
                    Dialog.alertDialog(this, "Please select minute value.");
                else {
                    tv_waiting_time.setText(hour + ":" + minute);
                    alertDialog_time.cancel();
                }
                break;
            case R.id.bt_cancel_waiting:
                hour = "";
                minute = "";
                alertDialog_time.cancel();
                break;
            case R.id.iv_add_driver:
                iv_add_driver.setImageDrawable(null);
                if (showAddDriverButton) {
                    sp_driver_name.setVisibility(View.GONE);
                    sp_driver_name.setAdapter(null);
                    et_add_driver_name.setVisibility(View.VISIBLE);
                    iv_add_driver.setBackgroundResource(R.drawable.remove);
                    showAddDriverButton = false;
                }else {
                    sp_driver_name.setVisibility(View.VISIBLE);
                    getUserIdList(registration_id);
                    et_add_driver_name.setVisibility(View.GONE);
                    et_add_driver_name.setText("");
                    iv_add_driver.setBackgroundResource(R.drawable.add);
                    showAddDriverButton = true;
                }
                break;
            case R.id.bt_exit:
                alertDialog.cancel();
                progressDialog.dismiss();
                break;
        }
    }

    private void setSignedDriver() {
        signaturePadDriver.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
                isSigned = true;
            }

            @Override
            public void onSigned() {
                isSigned = true;
            }

            @Override
            public void onClear() {
                isSigned = false;
            }
        });
    }

    private void setSignOperator() {
        signaturePadOperator.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
                isSignedOperator = true;
            }

            @Override
            public void onSigned() {
                isSignedOperator = true;
            }

            @Override
            public void onClear() {
                isSignedOperator = false;
            }
        });
    }

    private void setSignedThirdParty() {
        signaturePadThirdParty.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
                isSignedThird = true;
            }

            @Override
            public void onSigned() {
                isSignedThird = true;
            }

            @Override
            public void onClear() {
                isSignedThird = false;
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(
                                getContentResolver(), imageUri);
                        Matrix rotationMatrix = new Matrix();
                        if(imageBitmap.getWidth() >= imageBitmap.getHeight()){
                            rotationMatrix.setRotate(90);
                        }else{
                            rotationMatrix.setRotate(0);
                        }
                        Bitmap rotatedBitmap = Bitmap.createBitmap(imageBitmap,0,0,imageBitmap.getWidth(),
                                imageBitmap.getHeight(),rotationMatrix,true);
                        if (rotatedBitmap != null) {
                            if (!hasImage && !hasImageTwo) {
                                iv_photo_one.setImageBitmap(rotatedBitmap);
                                image1 = AppData.convertTOBase64Image(getResizedBitmap(rotatedBitmap,700));
                                hasImage = true;
                            } else if (hasImage && !hasImageTwo) {
                                iv_photo_two.setImageBitmap(rotatedBitmap);
                                image2 = AppData.convertTOBase64Image(getResizedBitmap(rotatedBitmap,700));
                                hasImageTwo = false;
                                hasImage = false;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "An unexpected error occurred", e);
                    }
                }
                break;
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle mExtra = dataIntent.getExtras();
                    String mDeviceAddress = mExtra.getString("DeviceAddress");
                    Log.v(TAG, "Printer mac address : " + mDeviceAddress);
                    deviceMacAddress = mDeviceAddress;
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            doConnectionTest();
                        }
                    });
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    ListPairedDevices();
                } else {
                    Toast.makeText(SupplyFormOperator.this, "Message", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void sendSupplyFormOperator() {
        external_ticket = et_external_ticket.getText().toString().trim();
        if (image1 == null)
            image1 = "";
        if (image2 == null)
            image2 = "";
        storeSupplyFormOperator(image1, image2, external_ticket);
    }

    private void storeSupplyFormOperator(String image1, String image2, String external_ticket) {
        progressDialog.show();
        if (!new_reg_no.equals(""))
            registration_id = 0;
        if (!new_waste_carrier.equals(""))
            waste_carrier_id = new_waste_carrier;
        if (!et_add_driver_name.getText().toString().isEmpty()) {
            new_driver_name = et_add_driver_name.getText().toString();
            selected_user_id = Integer.parseInt(max_docId_user_details)+1;
        }
        new_vehicle_id = Integer.parseInt(max_docId_vehicle_details) + 1;
        new_user_id=Integer.parseInt(max_docId_user_details)+1;
        Log.d("EWC CODE :" , ewc_code + " " + waste_carrier_id + " " + haulier_id );
        comment = et_comment.getText().toString().trim();
        Map<String, Object> mapSupplyOperator = new HashMap<>();
        mapSupplyOperator.put("user_id", user_id);
        mapSupplyOperator.put("user_role_id", user_role_id);
        mapSupplyOperator.put("operator", user_name);
        mapSupplyOperator.put("ticket_no", tv_ticket_no.getText());
        mapSupplyOperator.put("current_site_id", address_management_id);
        mapSupplyOperator.put("current_site_id_new", 0);
        mapSupplyOperator.put("current_site_address", current_site_address);
        mapSupplyOperator.put("material_id", material_id);
        mapSupplyOperator.put("material_id_new", 0);
        mapSupplyOperator.put("material_value","");
        mapSupplyOperator.put("delivery_site_id", selected_delivery_site_id);
        mapSupplyOperator.put("delivery_site_id_new", 0);
        mapSupplyOperator.put("delivery_reference",selected_delivery_reference_id);
        mapSupplyOperator.put("delivery_location",selected_delivery_area_id);
        mapSupplyOperator.put("container_type","");
        mapSupplyOperator.put("container_type_new","");
        mapSupplyOperator.put("ewc_code", ewc_code);
        mapSupplyOperator.put("ewc_available", "Yes");
        mapSupplyOperator.put("waste_carrier_no", waste_carrier_id);
        mapSupplyOperator.put("project_no","");
        mapSupplyOperator.put("submission_date", current_date);
        mapSupplyOperator.put("submission_time", current_time);
        mapSupplyOperator.put("z_driver_sign", driver_sign);
        mapSupplyOperator.put("z_operator_sign", operator_sign);
        if (third_party_signature_option.equals("No"))
            third_party_sign = "No";
        mapSupplyOperator.put("z_third_party_sign", third_party_sign);
        mapSupplyOperator.put("print_ticket", print_ticket);
        mapSupplyOperator.put("job_id", 0);
        mapSupplyOperator.put("task_order_id", 0);
        mapSupplyOperator.put("z_image_one", image1);
        mapSupplyOperator.put("z_image_two", image2);
        mapSupplyOperator.put("sic_code", "");
        mapSupplyOperator.put("delivery_site_id_new", 0);
        mapSupplyOperator.put("material_id_new", 0);
        mapSupplyOperator.put("vehicle_type", tv_vehicle_type.getText().toString());
        mapSupplyOperator.put("vehicle_type_new", "");
        mapSupplyOperator.put("haulier_id", tv_haulier.getText().toString());
        mapSupplyOperator.put("haulier_id_new","");
        mapSupplyOperator.put("waste_carrier_no_new", "");
        mapSupplyOperator.put("container_size_one", "");
        mapSupplyOperator.put("container_size_two", "");
        mapSupplyOperator.put("container_size_three", "");
        mapSupplyOperator.put("add_comment", comment);
        mapSupplyOperator.put("permit_no_collection", "");
        mapSupplyOperator.put("permit_no_delivery", "");
        mapSupplyOperator.put("permit_no", "");
        mapSupplyOperator.put("waiting_time", tv_waiting_time.getText().toString().trim());
        mapSupplyOperator.put("gross_weight", gross);
        mapSupplyOperator.put("tare", tare);
        mapSupplyOperator.put("net", String.valueOf(net));
        mapSupplyOperator.put("unit", et_unit.getText().toString().trim());
        if(user_role_id !=4 && !tv_driver_name.getText().toString().isEmpty()) {
            mapSupplyOperator.put("vehicle_id",new_vehicle_id);
            mapSupplyOperator.put("new_driver_id", new_user_id);
        }else if (user_role_id == 4 && !tv_driver_name.getText().toString().isEmpty() && !isInspectionDone) {
            mapSupplyOperator.put("vehicle_id", new_vehicle_id);
            mapSupplyOperator.put("new_driver_id", new_user_id);
        } else {
            mapSupplyOperator.put("vehicle_id",registration_id);
            mapSupplyOperator.put("new_driver_id", selected_user_id);
        }
        mapSupplyOperator.put("new_driver_name", new_driver_name);
        mapSupplyOperator.put("registration_no_new","");
        mapSupplyOperator.put("registration_no", "");
        mapSupplyOperator.put("external_ticket_no", external_ticket);
        mapSupplyOperator.put("note_type", "Delivery");
        mapSupplyOperator.put("id", tv_ticket_no.getText().toString());
        mapSupplyOperator.put("status", "Active");
        doc_id_length = doc_id_length + 1;
        mapSupplyOperator.put("sort_key", doc_id_length);
        mapSupplyOperator.put("form_status", "Completed");
        mapSupplyOperator.put("backend_edited", "No");
        mapSupplyOperator.put("mail_send","No");
        mapSupplyOperator.put("pdf_send","No");
        mapSupplyOperator.put("time_second_format", AppData.getTimeSecond());
        mapSupplyOperator.put("is_updated","Yes");
        mapSupplyOperator.put("ticket_status", "");

        supplyWasteFormsReference.document(tv_ticket_no.getText().toString()).set(mapSupplyOperator).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    toConfirmation("Form submitted successfully.");
                    sendTaskData(current_date,current_time,tv_ticket_no.getText().toString());
                    Map<String, Object> objectMap_update = new HashMap<>();
                    objectMap_update.put("supply_waste_key", doc_id_length);
                    sortKeyTableReference.document(doc_id).update(objectMap_update);

                    if (!tv_driver_name.getText().toString().isEmpty() && !isInspectionDone) {
                        updateVehicleDetails();
                        updateUserDetails();
                        addDataToNewRegistrationTable(new_reg_no, user_id, new_driver_name, current_date, current_time, tv_ticket_no.getText().toString(),
                                tv_vehicle_type.getText().toString(), tv_haulier.getText().toString(), site_id, selected_delivery_site_id, new_vehicle_id, new_user_id);
                    }else if (!et_add_driver_name.getText().toString().isEmpty() && !isInspectionDone){
                        updateUserDetails();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SupplyFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_current_site:
                current_site_name = arrayList_assign_site_name.get(position).toString().trim();
                site_id = arrayList_assign_site_id.get(position);
                Log.d("Current site id :" , String.valueOf(site_id));
                progressDialog.show();
                getSicCode(site_id);
                if (!AppData.internetOnline(SupplyFormOperator.this)) {
                    progressDialog.dismiss();
                }
                break;
            case R.id.sp_materials:
                material_des = sp_materials.getSelectedItem().toString();
                material_id = selected_material_id_list.get(position);
                Log.d("Material id list :" , material_id_list + " " + ewc_code_list);
                ewc_code = ewc_code_list.get(position);
                material_value = material_value_list.get(position);
                if (material_value.equals("High"))
                    tv_material_value_caution.setVisibility(View.VISIBLE);
                else
                    tv_material_value_caution.setVisibility(View.GONE);
                assignMaterialsDeliverySite(material_id);
                if (!AppData.internetOnline(this))
                    progressDialog.dismiss();
                break;
            case R.id.sp_delivery_site:
                selected_delivery_site_name = siteName.get(position).trim();
                selected_delivery_site_id = siteId.get(position);
                getDeliverySiteID(selected_delivery_site_id);
                getDeliveryReferenceIds(selected_delivery_site_id);
                getDeliveryAreaIds(selected_delivery_site_id);
                break;
            case R.id.sp_delivery_reference:
                selected_delivery_reference_name = arrayList_delivery_reference_name.get(position).trim();
                selected_delivery_reference_id = arrayList_delivery_reference_id.get(position);
                break;
            case R.id.sp_delivery_area:
                selected_delivery_area_name = arrayList_delivery_area_name.get(position).trim();
                selected_delivery_area_id = arrayList_delivery_area_id.get(position);
                break;
            case R.id.sp_reg_no:
                registration_no = sp_reg_no.getSelectedItem().toString().trim();
                tv_vehicle_type.setText("");
                tv_haulier.setText("");
                Log.d("Reg no: " ,registration_no);
                Log.d("Reg id :" , String.valueOf(registration_id));
                if (registration_no.equals("Add new")) {
                    ll_add_new_reg.setVisibility(View.VISIBLE);
                    int last_element =registration_no_list.lastIndexOf(registration_no);
                    registration_no_list.remove(last_element);
                    tv_driver_name.setVisibility(View.VISIBLE);
                    tv_new_reg_no.setVisibility(View.VISIBLE);
                    sp_driver_name.setVisibility(View.GONE);
                    tv_new_reg_no.setText("");
                    tv_driver_name.setText("");
                    bt_add_reg_no.setText("Add");
                }else {
                    registration_id = registration_no_id_list.get(position);
                    external_vehicle = external_vehicle_list.get(position);
                    vehicle_type_id = vehicle_type_list.get(position);
                    haulier_company_id = haulierList.get(position);
                    waste_carrier_id = haulierCarrierNoList.get(position);
                    tv_vehicle_type.setText(vehicle_type_id);
                    tv_haulier.setText(haulier_company_id);
                    getUserIdList(registration_id);
                    ll_add_new_reg.setVisibility(View.GONE);
                    tv_driver_name.setVisibility(View.GONE);
                    tv_new_reg_no.setVisibility(View.GONE);
                    sp_driver_name.setVisibility(View.VISIBLE);
                }
                iv_add_driver.setVisibility(View.VISIBLE);
                if (!registration_no_list.contains("Add new"))
                    registration_no_list.add("Add new");
                if (!AppData.internetOnline(this))
                    progressDialog.dismiss();
                break;
            case R.id.sp_new_vehicle_type:
                new_vehicle_type = sp_new_vehicle_type.getSelectedItem().toString().trim();
                break;
            case R.id.sp_new_haulier:
                new_haulier_company = sp_new_haulier.getSelectedItem().toString().trim();
                ArrayList<Integer> select_position = new ArrayList<>();
                select_position.add(haulierList.indexOf(new_haulier_company)) ;
                new_waste_carrier = haulierCarrierNoList.get(select_position.get(0));
                break;
            case R.id.sp_driver_name:
                selected_user_id =0;
                new_driver_name = sp_driver_name.getSelectedItem().toString().trim();
                selected_user_id = arrayList_user_id.get(position);
                Log.d("Selected User Id :" ,selected_user_id+" "+new_driver_name);
                break;
            case R.id.sp_hour:
                hour = sp_hour.getSelectedItem().toString().trim();
                break;
            case R.id.sp_minute:
                minute = sp_minute.getSelectedItem().toString().trim();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @SuppressLint("DefaultLocale")
    private boolean checkValidation() {
        Log.d("New Reg No: " ,new_reg_no+"  "+registration_no);
        if (isSigned) {
            Bitmap signatureBitmap = signaturePadDriver.getSignatureBitmap();
            driver_sign = AppData.convertTOBase64Image(signatureBitmap);
        }
        if (isSignedOperator) {
            Bitmap signatureBitmapOperator = signaturePadOperator.getSignatureBitmap();
            operator_sign = AppData.convertTOBase64Image(signatureBitmapOperator);
        }
        if (isSignedThird) {
            Bitmap signatureBitmapThirdParty = signaturePadThirdParty.getSignatureBitmap();
            third_party_sign = AppData.convertTOBase64Image(signatureBitmapThirdParty);
        }

        if (address_management_id == 0)
            Dialog.alertDialog(this, "Please select any assign site");
        else if (material_des.equals("select Material") || material_des.equals(""))
            Dialog.alertDialog(this, "Please select  material");
        else if (selected_delivery_site_id == 0)
            Dialog.alertDialog(this, "Please select delivery site");
        else if (selected_delivery_site_name.equals("Select delivery site"))
            Dialog.alertDialog(this, "Please select delivery site");
        else if (material_value.equals("High") && image1.equals(""))
            Dialog.alertDialog(this,"Please take a picture of the material being transferred.");
        else if (registration_no.equals("") && new_reg_no.equals(""))
            Dialog.alertDialog(this, "Please select registration number");
        else if (new_driver_name.equals("") && et_add_driver_name.getText().toString().isEmpty())
            Dialog.alertDialog(this, "Please select driver name");
        else if (weight_unit.equals(""))
            Dialog.alertDialog(this, "Please select weight or unit option");
        else if (weight_unit.equals("weight") && et_gross_weight.getText().toString().trim().isEmpty())
            Dialog.alertDialog(this, "Please enter gross weight value");
        else if (weight_unit.equals("weight") && et_tare.getText().toString().trim().isEmpty())
            Dialog.alertDialog(this, "Please enter tare value");
        else if (weight_unit.equals("unit") && et_unit.getText().toString().trim().isEmpty())
            Dialog.alertDialog(this, "Please select unit value.");
        else if (!isSigned)
            Dialog.alertDialog(this, "Driver sign mandatory.");
        else if (!isSignedOperator)
            Dialog.alertDialog(this, "Operator sign mandatory.");
        else if (third_party_signature_option.equals(""))
            Dialog.alertDialog(this, "Please select third party sign option");
        else if (third_party_signature_option.equals("Yes") && !isSignedThird)
            Dialog.alertDialog(this, "Please sign the form by third party");
        else if (print_ticket.equals(""))
            Dialog.alertDialog(this, "Please select print ticket option");
        else
            validation_done = true;
        return validation_done;
    }

    private void generateTicket() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");
        String currentDate = sdf.format(new Date());
        String ticket_no = "S" + currentDate + String.format("%06d", number);
        Log.d("Ticket No:" , ticket_no);
        tv_ticket_no.setText(ticket_no);
    }

    private void toConfirmation(String message) {
        progressDialog.dismiss();
        final View popupView = getLayoutInflater().inflate(R.layout.change_pass_popup_layout, null, true);
         boolean isActivityInForeground = SupplyFormOperator.this.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED);
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
                Intent toDashboard = new Intent(SupplyFormOperator.this, MainActivity.class);
                startActivity(toDashboard);
            }
        });
    }

    // for add registration number
    private void customizeWindow() {
        AlertDialog.Builder customize_alert_dialog = new AlertDialog.Builder(this);
        customize_alert_dialog.setTitle("Add new registration  number");
        customize_alert_dialog.setCancelable(false);
        View customize_view = getLayoutInflater().inflate(R.layout.add_new_reg, null);
        et_new_driver_name = customize_view.findViewById(R.id.et_new_driver_name);
        et_new_reg_no = customize_view.findViewById(R.id.et_new_reg_no);
        sp_new_vehicle_type = customize_view.findViewById(R.id.sp_new_vehicle_type);
        sp_new_haulier = customize_view.findViewById(R.id.sp_new_haulier);
        ll_waste_carrier_no_new = customize_view.findViewById(R.id.ll_waste_carrier_no_new);
        ll_waste_carrier_no_new.setVisibility(View.GONE);
        bt_submit_new = customize_view.findViewById(R.id.bt_submit_new);
        bt_exit = customize_view.findViewById(R.id.bt_exit);
        bt_submit_new.setOnClickListener(this);
        bt_exit.setOnClickListener(this);
        sp_new_vehicle_type.setOnItemSelectedListener(this);
        sp_new_haulier.setOnItemSelectedListener(this);
        customize_alert_dialog.setView(customize_view);
        getVehicleType();
        getHaulierCompanyList();
        alertDialog = customize_alert_dialog.create();
        alertDialog.show();
    }

    private void customWaitingTimePopUp() {
        AlertDialog.Builder customize_alert_dialog = new AlertDialog.Builder(this);
        customize_alert_dialog.setTitle("Set waiting time");
        customize_alert_dialog.setCancelable(false);
        View customize_view = getLayoutInflater().inflate(R.layout.waiting_time_waste, null);
        sp_hour = customize_view.findViewById(R.id.sp_hour);
        sp_minute = customize_view.findViewById(R.id.sp_minute);
        bt_ok = customize_view.findViewById(R.id.bt_ok);
        bt_cancel_waiting = customize_view.findViewById(R.id.bt_cancel_waiting);
        bt_ok.setOnClickListener(this);
        bt_cancel_waiting.setOnClickListener(this);
        sp_hour.setOnItemSelectedListener(this);
        sp_minute.setOnItemSelectedListener(this);
        rv_hour = customize_view.findViewById(R.id.rv_hour);
        rv_minute = customize_view.findViewById(R.id.rv_minute);
        customize_alert_dialog.setView(customize_view);
        alertDialog_time = customize_alert_dialog.create();
        alertDialog_time.show();
        arrayList_hour.clear();
        arrayList_minute.clear();
        arrayList_second.clear();
        for (int i = 0; i <= 100; i++) {
            if (i < 10)
                arrayList_hour.add("0" + String.valueOf(i));
            else
                arrayList_hour.add(String.valueOf(i));
        }
        for (int i = 0; i < 60; i++) {
            if (i < 10)
                arrayList_minute.add("0" + String.valueOf(i));
            else
                arrayList_minute.add(String.valueOf(i));
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(SupplyFormOperator.this, R.layout.spinner_custom_layout, arrayList_hour);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_hour.setAdapter(spinnerArrayAdapter);
        ArrayAdapter<String> spinnerArrayAdapter1 = new ArrayAdapter<String>(SupplyFormOperator.this, R.layout.spinner_custom_layout, arrayList_minute);
        spinnerArrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_minute.setAdapter(spinnerArrayAdapter1);
        hourAdapter = new HourAdapter(SupplyFormOperator.this, arrayList_hour);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv_hour.setLayoutManager(mLayoutManager);
        rv_hour.setItemAnimator(new DefaultItemAnimator());
        rv_hour.setAdapter(hourAdapter);

        minuteAdapter = new MinuteAdapter(SupplyFormOperator.this, arrayList_minute);
        RecyclerView.LayoutManager mLayoutManager1 = new LinearLayoutManager(getApplicationContext());
        rv_minute.setLayoutManager(mLayoutManager1);
        rv_minute.setItemAnimator(new DefaultItemAnimator());
        rv_minute.setAdapter(minuteAdapter);

        WaitingTimeInterfaces.waiting_hour_interface = new HourAdapter.waiting_hour() {
            @Override
            public void hour_time(String hour_text) {
                hour = hour_text;
            }
        };
        WaitingTimeInterfaces.waiting_minute_interface = new MinuteAdapter.Waiting_minute() {
            @Override
            public void minute_time(String minute_text) {
                minute = minute_text;
            }
        };
    }

    private boolean newVehiclePopupValidation() {
        validation_window = false;
        if (et_new_driver_name.getText().toString().equals(""))
            Dialog.alertDialog(this, "Please select driver name.");
        else if (et_new_reg_no.getText().toString().trim().isEmpty())
            Dialog.alertDialog(this, "Please enter registration number.");
        else if (new_vehicle_type.equals(""))
            Dialog.alertDialog(this, "Please select vehicle type.");
        else if (new_waste_carrier.equals(""))
            Dialog.alertDialog(this, "Please select haulier.");
        else
            validation_window = true;
        progressDialog.dismiss();
        return validation_window;
    }

    private void getVehicleType() {
        HashSet<String> hashSet = new HashSet<String>();
        hashSet.addAll(vehicle_type_list);
        vehicle_type_list.clear();
        new_vehicle_type_list.addAll(hashSet);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_custom_layout, new_vehicle_type_list);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_new_vehicle_type.setAdapter(spinnerArrayAdapter);
    }

    private void getHaulierCompanyList(){
        HashSet<String> hashSet = new HashSet<String>();
        hashSet.addAll(haulierList);
        newHaulierList.addAll(hashSet);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_custom_layout,newHaulierList);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_new_haulier.setAdapter(spinnerArrayAdapter);
    }

    private void addDataToNewRegistrationTable(String vehicle_registration_no, int operator, String driver_name, String date, String time, String ticket_no,
                                               String vehicle_type, String haulier, int collection_point_id, int delivery_point_id,int vehicle_id,int user_id) {
        HashMap<String, Object> newRegistrationMap = new HashMap<>();
        newRegistrationMap.put("registration_no", vehicle_registration_no);
        newRegistrationMap.put("operator", operator);
        newRegistrationMap.put("driver_name", driver_name);
        newRegistrationMap.put("submission_date", date);
        newRegistrationMap.put("submission_time", time);
        newRegistrationMap.put("ticket_no", ticket_no);
        newRegistrationMap.put("id", ticket_no);
        newRegistrationMap.put("vehicle_type", vehicle_type);
        newRegistrationMap.put("haulier", haulier);
        newRegistrationMap.put("collection_point_id", collection_point_id);
        newRegistrationMap.put("delivery_point_id", delivery_point_id);
        newRegistrationMap.put("status", "Active");
        newRegistrationMap.put("new_vehicle_id",vehicle_id);
        newRegistrationMap.put("new_driver_id",user_id);
        newRegistrationMap.put("is_updated","Yes");
        newRegistrationReference.document(ticket_no).set(newRegistrationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("Success :", "New registration data submitted successfully.");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Error :", "Unable to submit new registration data.");
            }
        });
    }

    private void zoomImagePopup(Bitmap bitmap) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.zoom_image_popup, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        ImageView iv_zoom_image = popupView.findViewById(R.id.iv_zoom_image);
        ImageView iv_close_popup = popupView.findViewById(R.id.iv_close_popup);
        iv_zoom_image.setImageBitmap(bitmap);
        popupWindow.showAsDropDown(popupView, 0, 0);
        iv_close_popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
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

 

    private void alertDialog(String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("Discard",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
    }

    private void getUserIdList(int selected_vehicle_id) {
        progressDialog.show();
        Query query = vehicleDetailsReference.whereEqualTo("id", selected_vehicle_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    String userIds = "";
                    arrayList_user_id.clear();
                    sp_driver_name.setAdapter(null);
                    new_driver_name="";
                    selected_user_id = 0;
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (queryDocumentSnapshot.exists())
                                userIds = queryDocumentSnapshot.getString("vehicle_user_mappings");
                        }
                        if (userIds != null && userIds.contains(",")) {
                            ArrayList<String> userIdListString = new ArrayList<String>(Arrays.asList(userIds.split(",")));
                            for (int i = 0; i < userIdListString.size(); i++) {
                                arrayList_user_id.add(Integer.valueOf(userIdListString.get(i)));
                            }
                        } else {
                            if (userIds != null) {
                                arrayList_user_id.add(Integer.valueOf(userIds));
                            }
                        }
                        Log.d("User Ids :" , String.valueOf(arrayList_user_id));
                        getFullName();
                    } else {
                        progressDialog.dismiss();
                        dataNotFound("No driver assigned with this vehicle.");
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SupplyFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getFullName() {
        Query query = userDetailsReference.whereEqualTo("status", "Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    arrayList_user_name.clear();
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot :task.getResult()){
                            allUserIds.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                            allUserNames.add(queryDocumentSnapshot.getString("full_name"));
                        }
                        for (int i =0; i<arrayList_user_id.size();i++){
                            for (int j=0;j<allUserIds.size();j++){
                                if (arrayList_user_id.get(i).equals(allUserIds.get(j))){
                                    arrayList_user_name.add(allUserNames.get(j));
                                    break;
                                }
                            }
                        }
                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(SupplyFormOperator.this, R.layout.spinner_custom_layout, arrayList_user_name);
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        sp_driver_name.setAdapter(spinnerArrayAdapter);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SupplyFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void dataNotFound(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getSiteAddress() {
        arrayList_assign_site_name = new ArrayList<>();
        arrayList_assign_site_id = new ArrayList<>();
        Query query = siteManagementReference.whereEqualTo("status", "Active").whereEqualTo("address_status","Active").whereEqualTo("site_type","Collection");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        arrayList_assign_site_name.add(queryDocumentSnapshot.getString("site_name"));
                        arrayList_assign_site_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                    }
                    sp_current_site.setTitle("Sites");
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(SupplyFormOperator.this, R.layout.spinner_custom_layout, arrayList_assign_site_name);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_current_site.setAdapter(spinnerArrayAdapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SupplyFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enableBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(SupplyFormOperator.this, "Message1", Toast.LENGTH_SHORT).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                if (ContextCompat.checkSelfPermission(SupplyFormOperator.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(SupplyFormOperator.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                        return;
                    }
                }
                Intent enableBtIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent,
                        REQUEST_ENABLE_BT);
            } else {
                ListPairedDevices();
            }
        }
    }

    private void ListPairedDevices() {
        if (ContextCompat.checkSelfPermission(SupplyFormOperator.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(SupplyFormOperator.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                return;
            }
        }
        Set<BluetoothDevice> mPairedDevices = mBluetoothAdapter
                .getBondedDevices();
        if (mPairedDevices.size() > 0) {
            for (BluetoothDevice mDevice : mPairedDevices) {
                Log.v(TAG, "PairedDevices: " + mDevice.getName() + "   XXX "
                        + mDevice.getAddress());
            }
            Intent connectIntent = new Intent(SupplyFormOperator.this,
                    DeviceListActivity.class);
            startActivityForResult(connectIntent,
                    REQUEST_CONNECT_DEVICE);
            printerDialog = ProgressDialog.show(this,
                    "Printing...", "Please Wait ...", true, true);
        }else {
            Toast.makeText(this,"No paired bluetooth device available.",Toast.LENGTH_LONG).show();
        }
    }

    private void sendTestLabel() {
        try {
            ZebraPrinterLinkOs linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(printer);
            PrinterStatus printerStatus = (linkOsPrinter != null) ? linkOsPrinter.getCurrentStatus() : printer.getCurrentStatus();
            if (printerStatus.isReadyToPrint) {
                byte[] configLabel = getConfigLabel();
                connection.write(configLabel);
            } else if (printerStatus.isHeadOpen) {
                Toast.makeText(this, "Printer Head Open", Toast.LENGTH_SHORT).show();
            } else if (printerStatus.isPaused) {
                Toast.makeText(this, "Printer is Paused", Toast.LENGTH_SHORT).show();
            } else if (printerStatus.isPaperOut) {
                Toast.makeText(this, "Printer Media Out", Toast.LENGTH_SHORT).show();
            }
        } catch (ConnectionException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            disconnect();
        }
    }

    public ZebraPrinter connect() {
        connection = null;
        connection = new BluetoothConnection(getMacAddress());
        try {
            if (ContextCompat.checkSelfPermission(SupplyFormOperator.this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(SupplyFormOperator.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
                }
            }
            connection.open();
        } catch (ConnectionException e) {
            disconnect();
        }
        ZebraPrinter printer = null;
        if (connection.isConnected()) {
            try {
                printer = ZebraPrinterFactory.getInstance(connection);
                String pl = SGD.GET("device.languages", connection);
            } catch (ConnectionException | ZebraPrinterLanguageUnknownException e) {
                printer = null;
                disconnect();
            }
        }
        return printer;
    }

    public void disconnect() {
        try {
            if (connection != null)
                connection.close();
            if (printerDialog != null)
                printerDialog.dismiss();
        } catch (ConnectionException e) {
        }
    }

    private void doConnectionTest() {
        printer = connect();
        if (printer != null)
            sendTestLabel();
        else
            disconnect();
    }

    private String getMacAddress() {
        return deviceMacAddress;
    }

    private byte[] getConfigLabel() {
        byte[] configLabel = null;
        try {
            PrinterLanguage printerLanguage = printer.getPrinterControlLanguage();
            SGD.SET("device.languages", "zpl", connection);
            String reg_no = "";
            if (selected_delivery_area_id==0)
                selected_delivery_area_name="";
            if (selected_delivery_reference_id==0)
                selected_delivery_reference_name = "";
            if (!et_add_driver_name.getText().toString().isEmpty())
                new_driver_name = et_add_driver_name.getText().toString();
            if (new_reg_no.isEmpty())
                reg_no=registration_no;
            else
                reg_no=new_reg_no;
            if (!new_waste_carrier.equals("")){
                waste_carrier_id = new_waste_carrier;
            }
            Bitmap supply_layout = new PrintingLayoutFormat(this).supplyFormLayoutToBitmap(tv_ticket_no.getText().toString(), et_comment.getText().toString(), user_name,current_site_name,selected_delivery_site_name,
                    current_site_address, selected_delivery_site_address,selected_delivery_area_name,selected_delivery_reference_name, tv_haulier.getText().toString(), new_driver_name, waste_carrier_id , current_date + " " + current_time, reg_no, material_des,
                    et_unit.getText().toString().trim(), et_gross_weight.getText().toString(), et_tare.getText().toString(), tv_net_weight.getText().toString(), signaturePadOperator.getSignatureBitmap(), signaturePadDriver.getSignatureBitmap());
            Bitmap resize_bmp = getResizedBitmap(supply_layout, 600, 1100);
            ZPLConverter zp = new ZPLConverter();
            zp.setCompressHex(true);
            zp.setBlacknessLimitPercentage(90);
            if (printerLanguage == PrinterLanguage.ZPL) {
                configLabel = zp.convertFromSupplyImage(resize_bmp).getBytes();
            } else if (printerLanguage == PrinterLanguage.CPCL) {
                String cpclConfigLabel = "! 0 200 200 406 1\r\n" + "ON-FEED IGNORE\r\n" + "BOX 20 20 220 220 8\r\n" + "T 0 6 137 177 ERROR\r\n" + "PRINT\r\n";
                configLabel = cpclConfigLabel.getBytes();
            }
        } catch (ConnectionException e) {

        }
        return configLabel;
    }

    public Bitmap getResizedBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap resizedBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        float scaleX = newWidth / (float) bitmap.getWidth();
        float scaleY = newHeight / (float) bitmap.getHeight();
        float pivotX = 0;
        float pivotY = 0;
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);
        Canvas canvas = new Canvas(resizedBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
        return resizedBitmap;
    }

    // sort key table
    private void getMaximumLength() {
        sortKeyTableReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (queryDocumentSnapshot.contains("supply_waste_key")) {
                                doc_id_length = queryDocumentSnapshot.getLong("supply_waste_key").intValue();
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
                Toast.makeText(SupplyFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMaximumLengthTaskManagement(){
        sortKeyTableReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if (queryDocumentSnapshot.contains("task_management_key")){
                                doc_id_length_task = queryDocumentSnapshot.getLong("task_management_key").intValue();
                                doc_id_task = queryDocumentSnapshot.getId();
                                break;
                            }
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void assignMaterialsDeliverySite(int material_id) {
        Query query = assignMaterialReference.whereEqualTo("status", "Active")
                .whereEqualTo("site_type", "Delivery");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    String materialIds = "";
                    arrayList_site_id.clear();
                    ArrayList<Integer> arrayList_material_id = new ArrayList<>();
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            materialIds = queryDocumentSnapshot.getString("material_ids");
                            if (materialIds != null && materialIds.contains(",")) {
                                ArrayList<String> materialIdListString = new ArrayList<String>(Arrays.asList(materialIds.split(",")));
                                for (int i=0;i<materialIdListString.size();i++) {
                                    arrayList_material_id.add(Integer.valueOf(materialIdListString.get(i)));
                                    if (Integer.valueOf(materialIdListString.get(i)) == material_id) {
                                        arrayList_site_id.add(queryDocumentSnapshot.getLong("site_id").intValue());
                                    }
                                }
                            }else {
                                if (materialIds != null) {
                                    arrayList_material_id.add(Integer.valueOf(materialIds));
                                    if (Integer.valueOf(materialIds) == material_id){
                                        arrayList_site_id.add(queryDocumentSnapshot.getLong("site_id").intValue());
                                    }
                                }
                            }
                        }
                        deliveryAddManagement();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deliveryAddManagement() {
        Query query = siteManagementReference.whereEqualTo("status", "Active").whereEqualTo("address_status","Active").whereEqualTo("site_type", "Delivery");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    siteId.clear();
                    siteName.clear();
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            int id = queryDocumentSnapshot.getLong("id").intValue();
                            for (int i = 0; i < arrayList_site_id.size(); i++) {
                                if (id == arrayList_site_id.get(i)) {
                                    siteId.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                                    siteName.add(queryDocumentSnapshot.getString("site_name"));
                                    break;
                                }
                            }
                        }
                        Log.d("Site Management Site Ids :" ,siteId+"  "+siteName);
                        if (siteName.size() > 0) {
                            siteName.add(0, "Select delivery site");
                            siteId.add(0, 0);
                        }
                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_custom_layout, siteName);
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                        sp_delivery_site.setAdapter(spinnerArrayAdapter);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // get sic code
    private void getSicCode(int site_id){
        progressDialog.show();
        Query query = siteManagementReference.whereEqualTo("id",site_id).whereEqualTo("site_type","Collection")
                .whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            store_sic_code = queryDocumentSnapshot.getString("sic_code");
                            address_management_id= Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
                            current_site_address = queryDocumentSnapshot.getString("address");
                        }
                        tv_current_site_address.setText(current_site_address);
                        assignMaterial(address_management_id);
                    }
                    else
                        progressDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SupplyFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // get delivery site id
    private void getDeliverySiteID(int site_id){
        progressDialog.show();
        Query query = siteManagementReference.whereEqualTo("id",site_id)
                .whereEqualTo("site_type","Delivery").whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            selected_delivery_site_id= Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
                            selected_delivery_site_address = queryDocumentSnapshot.getString("address");
                        }
                    }
                    else
                        progressDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SupplyFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDeliveryReferenceIds(int site_id){
        Query query =  siteManagementReference.whereEqualTo("id",site_id).whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    String delivery_reference_ids = "";
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            delivery_reference_ids = queryDocumentSnapshot.getString("address_reference_mapping");
                        }
                        if (delivery_reference_ids != null && delivery_reference_ids.contains(",")) {
                            ArrayList<String> deliveryReferenceIdListString = new ArrayList<String>(Arrays.asList(delivery_reference_ids.split(",")));
                            for (int i = 0; i < deliveryReferenceIdListString.size(); i++) {
                                arrayList_delivery_reference_id.add(Integer.valueOf(deliveryReferenceIdListString.get(i)));
                            }
                        } else {
                            if (delivery_reference_ids != null) {
                                if (!delivery_reference_ids.equals("")) {
                                    arrayList_delivery_reference_id.add(Integer.valueOf(delivery_reference_ids));
                                }
                            }
                        }
                        getDeliveryReferenceNames();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDeliveryReferenceNames(){
        Query query = deliveryReferenceDetails.whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    arrayList_delivery_reference_name.clear();
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot :task.getResult()){
                            allDeliveryReferenceIds.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                            allDeliveryReferenceNames.add(queryDocumentSnapshot.getString("reference_name"));
                        }
                        for (int i =0; i<arrayList_delivery_reference_id.size();i++){
                            for (int j=0;j<allDeliveryReferenceIds.size();j++){
                                if (arrayList_delivery_reference_id.get(i).equals(allDeliveryReferenceIds.get(j))){
                                    arrayList_delivery_reference_name.add(allDeliveryReferenceNames.get(j));
                                    break;
                                }
                            }
                        }
                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_custom_layout,arrayList_delivery_reference_name);
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        sp_delivery_reference.setAdapter(spinnerArrayAdapter);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDeliveryAreaIds(int site_id){
        Query query =  siteManagementReference.whereEqualTo("id",site_id).whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    String delivery_area_ids = "";
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            delivery_area_ids = queryDocumentSnapshot.getString("address_location_mapping");
                        }
                        if (delivery_area_ids != null && delivery_area_ids.contains(",")) {
                            ArrayList<String> deliveryAreaIdListString = new ArrayList<String>(Arrays.asList(delivery_area_ids.split(",")));
                            for (int i = 0; i < deliveryAreaIdListString.size(); i++) {
                                arrayList_delivery_area_id.add(Integer.valueOf(deliveryAreaIdListString.get(i)));
                            }
                        } else {
                            if (delivery_area_ids != null) {
                                if (!delivery_area_ids.equals("")) {
                                    arrayList_delivery_area_id.add(Integer.valueOf(delivery_area_ids));
                                }
                            }
                        }
                        getDeliveryAreaNames();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDeliveryAreaNames(){
        Query query = deliveryLocationReference.whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    arrayList_delivery_area_name.clear();
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot :task.getResult()){
                            allDeliveryAreaIds.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                            allDeliveryAreaNames.add(queryDocumentSnapshot.getString("location_name"));
                        }
                        for (int i =0; i<arrayList_delivery_area_id.size();i++){
                            for (int j=0;j<allDeliveryAreaIds.size();j++){
                                if (arrayList_delivery_area_id.get(i).equals(allDeliveryAreaIds.get(j))){
                                    arrayList_delivery_area_name.add(allDeliveryAreaNames.get(j));
                                    break;
                                }
                            }
                        }
                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_custom_layout,arrayList_delivery_area_name);
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        sp_delivery_area.setAdapter(spinnerArrayAdapter);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // max document id
    private void getMaxDocIDUserDetails(){
        userDetailsReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if(!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if(!max_docId_user_details.equals("")){
                                if(Integer.parseInt(max_docId_user_details)<Integer.parseInt(queryDocumentSnapshot.getId()))
                                    max_docId_user_details = queryDocumentSnapshot.getId();
                            }
                            else
                                max_docId_user_details = queryDocumentSnapshot.getId();
                        }
                        Log.d("max_user_id :",max_docId_user_details);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMaxDocIDVehicleDetails(){
        vehicleDetailsReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if(!max_docId_vehicle_details.equals("")){
                                if(Integer.parseInt(max_docId_vehicle_details)<Integer.parseInt(queryDocumentSnapshot.getId()))
                                    max_docId_vehicle_details = queryDocumentSnapshot.getId();
                            }
                            else
                                max_docId_vehicle_details = queryDocumentSnapshot.getId();
                        }
                        Log.d("max_vehicle_id :",max_docId_vehicle_details);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateVehicleDetails() {
        Map<String, Object> updateVehicleDetailsMap = new HashMap<>();
        updateVehicleDetailsMap.put("created_at",current_date+" "+current_time);
        updateVehicleDetailsMap.put("external_vehicle","Yes");
        updateVehicleDetailsMap.put("haulier",tv_haulier.getText().toString());
        updateVehicleDetailsMap.put("haulier_carrier_no",new_waste_carrier);
        updateVehicleDetailsMap.put("id",new_vehicle_id);
        updateVehicleDetailsMap.put("manufacturer","");
        updateVehicleDetailsMap.put("model","");
        updateVehicleDetailsMap.put("prefix_registration_no", 0);
        updateVehicleDetailsMap.put("registration_no",new_reg_no);
        updateVehicleDetailsMap.put("registration_no_no_space","");
        updateVehicleDetailsMap.put("status","Active");
        updateVehicleDetailsMap.put("trailer","");
        updateVehicleDetailsMap.put("updated_at",current_date+" "+current_time);
        updateVehicleDetailsMap.put("vehicle_completion","");
        updateVehicleDetailsMap.put("vehicle_front","");
        updateVehicleDetailsMap.put("vehicle_near_rear","");
        updateVehicleDetailsMap.put("vehicle_off_rear","");
        updateVehicleDetailsMap.put("vehicle_user_mappings",String.valueOf(new_user_id));
        updateVehicleDetailsMap.put("vehicle_type",tv_vehicle_type.getText().toString());
        updateVehicleDetailsMap.put("is_updated","Yes");
        vehicleDetailsReference.document(String.valueOf(new_vehicle_id)).set(updateVehicleDetailsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.e("Success","Vehicle details updated successfully.");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Dialog.alertDialog(SupplyFormOperator.this, e.getMessage());
            }
        });
    }

    private void updateUserDetails(){
        HashMap<String,Object> update_user_map = new HashMap();
        update_user_map.put("access_module",0);
        update_user_map.put("created_at",current_date+" "+current_time);
        update_user_map.put("device_token","");
        update_user_map.put("display_image",null);
        update_user_map.put("email_address","");
        update_user_map.put("external_user","Yes");
        update_user_map.put("first_name","");
        update_user_map.put("full_name",new_driver_name);
        update_user_map.put("id",new_user_id);
        update_user_map.put("last_login_date","");
        update_user_map.put("last_name","");
        update_user_map.put("mobile_number","");
        update_user_map.put("password","");
        update_user_map.put("password_reset_time",0);
        update_user_map.put("password_reset_token","");
        update_user_map.put("role",4);
        update_user_map.put("status","Active");
        update_user_map.put("updated_at",current_date+" "+current_time);
        update_user_map.put("user_name","");
        update_user_map.put("is_updated","Yes");
        userDetailsReference.document(String.valueOf(new_user_id)).set(update_user_map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()){
                    if (!et_add_driver_name.getText().toString().isEmpty())
                        updateVehicleUserMapping(registration_id);
                    else
                        updateVehicleUserMapping(new_vehicle_id);
                    Log.d("Success :","User details updated successfully.");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateVehicleUserMapping(int vehicle_id){
        Query query = vehicleDetailsReference.whereEqualTo("id",vehicle_id).whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    String userIds ="",docId ="";
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        userIds = queryDocumentSnapshot.getString("vehicle_user_mappings");
                        docId = queryDocumentSnapshot.getId();
                    }
                    HashMap<String,Object> objUpdate = new HashMap<>();
                    if (userIds != null) {
                        if (userIds.equals("")){
                            objUpdate.put("vehicle_user_mappings",String.valueOf(new_user_id));
                        }else {
                            objUpdate.put("vehicle_user_mappings",userIds+","+String.valueOf(new_user_id));
                        }
                    }
                    objUpdate.put("updated_at",current_date+" "+current_time);
                    objUpdate.put("is_updated","Yes");
                    vehicleDetailsReference.document(docId).update(objUpdate);
                }
                Log.d("Success :","Vehicle user mapping updated successfully.");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendTaskData(String date,String time,String ticket_no){
        latLon();
        Map<String,Object> mapTask = new HashMap<>();
        mapTask.put("job_id",0);
        mapTask.put("task_order_no",0);
        mapTask.put("assigned_vehicle_id",0);
        mapTask.put("assigned_driver_id",0);
        mapTask.put("collection_point","");
        mapTask.put("delivery_point","");
        mapTask.put("ewc_code",ewc_code);
        mapTask.put("material_type","");
        mapTask.put("material_id",0);
        mapTask.put("task_date",date);
        mapTask.put("task_status","FinishTask");
        //start task activity
        mapTask.put("start_task_time",time);
        mapTask.put("start_task_date",date);
        mapTask.put("start_task_latitude","");
        mapTask.put("start_task_longitude","");
        mapTask.put("start_task_user_id","");
        mapTask.put("start_task_inspection_id","");
        // arrived at collection point activity
        mapTask.put("arrived_collection_time","");
        mapTask.put("arrived_collection_date","");
        mapTask.put("arrived_collection_latitude",latitude);
        mapTask.put("arrived_collection_longitude",longitude);
        mapTask.put("arrived_collection_user_id","");
        mapTask.put("arrived_collection_inspection_id","");
        // left collection point activity
        mapTask.put("left_collection_time","");
        mapTask.put("left_collection_date","");
        mapTask.put("left_collection_latitude","");
        mapTask.put("left_collection_longitude","");
        mapTask.put("left_collection_user_id","");
        mapTask.put("left_collection_inspection_id","");
        // arrived at delivery point activity
        mapTask.put("arrived_delivery_time","");
        mapTask.put("arrived_delivery_date","");
        mapTask.put("arrived_delivery_latitude","");
        mapTask.put("arrived_delivery_longitude","");
        mapTask.put("arrived_delivery_user_id","");
        mapTask.put("arrived_delivery_inspection_id","");
        // finish task activity
        mapTask.put("finish_task_time","");
        mapTask.put("finish_task_date","");
        mapTask.put("finish_task_latitude","");
        mapTask.put("finish_task_longitude","");
        mapTask.put("finish_task_user_id","");
        mapTask.put("finish_task_inspection_id","");
        // abort task
        mapTask.put("abort_task_time","");
        mapTask.put("abort_task_date","");
        mapTask.put("abort_task_latitude","");
        mapTask.put("abort_task_longitude","");
        mapTask.put("abort_task_user_id","");
        mapTask.put("abort_task_inspection_id","");
        mapTask.put("status","Active");
        mapTask.put("id",ticket_no);
        doc_id_length_task =doc_id_length_task+1;
        mapTask.put("sort_key",doc_id_length_task);
        mapTask.put("ticket_no",ticket_no);
        mapTask.put("form_edited","");
        mapTask.put("inspection_id","");
        mapTask.put("is_updated","Yes");
        mapTask.put("time_second_format", AppData.getTimeSecond());
        if(!tv_driver_name.getText().toString().isEmpty() && !isInspectionDone) {
            mapTask.put("vehicle_id",new_vehicle_id);
        }else {
            mapTask.put("vehicle_id",registration_id);
        }
        mapTask.put("material_type","Delivery");
        progressDialog.show();
        taskManagementReference.document(ticket_no).set(mapTask).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    Map<String,Object> objectMap_update = new HashMap<>();
                    objectMap_update.put("task_management_key",doc_id_length_task);
                    sortKeyTableReference.document(doc_id_task).update(objectMap_update);
                    Log.d("Success :" ,"Task Start" );
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d("Error :" ,"Unable to start task." );
            }
        });
    }

    private void latLon(){
        GPSTracker finder = new GPSTracker(SupplyFormOperator.this);
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
        }
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void  getInspectionSubmission(){
        Query query = inspectionSubmissionReference.whereEqualTo("logged_by",user_id).whereEqualTo("conducted_on",current_date);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()) {
                        isInspectionDone = true;
                        tv_vehicle_reg_no.setVisibility(View.VISIBLE);
                        tv_driver_name.setVisibility(View.VISIBLE);
                        sp_reg_no.setVisibility(View.GONE);
                        sp_driver_name.setVisibility(View.GONE);
                        tv_driver_name.setText(user_name);
                        new_driver_name = user_name;
                        selected_user_id = user_id;
                        getVehicleID();
                    } else {
                        isInspectionDone = false;
                        tv_vehicle_reg_no.setVisibility(View.GONE);
                        tv_driver_name.setVisibility(View.GONE);
                        sp_reg_no.setVisibility(View.VISIBLE);
                        sp_driver_name.setVisibility(View.VISIBLE);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private  void getVehicleID(){
        Query query = currentVehicleReference.whereEqualTo("user_id",user_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                        if (queryDocumentSnapshot.exists()){
                            registration_id = queryDocumentSnapshot.getLong("vehicle_id").intValue();
                        }
                    }
                    Log.d("VEHICLE ID:" , String.valueOf(registration_id));
                    getVehicleNumber(registration_id);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.e("Error", "Unable to fetch current vehicle.");
            }
        });
    }
    private void getVehicleNumber(int vehicle_id){
        Query query = vehicleDetailsReference.whereEqualTo("external_vehicle","No").whereEqualTo("id",vehicle_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                        if (queryDocumentSnapshot.exists()){
                            registration_no = queryDocumentSnapshot.getString("registration_no");
                            vehicle_type_id = queryDocumentSnapshot.getString("vehicle_type");
                            haulier_company_id = queryDocumentSnapshot.getString("haulier");
                            waste_carrier_id = queryDocumentSnapshot.getString("haulier_carrier_no");
                            Log.d("VEHICLE REG NO:" ,registration_no);
                            tv_vehicle_reg_no.setText(registration_no);
                        }
                    }
                    tv_vehicle_type.setText(vehicle_type_id);
                    tv_haulier.setText(haulier_company_id);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.e("Error", "Unable to fetch current vehicle.");
            }
        });
    }
}
