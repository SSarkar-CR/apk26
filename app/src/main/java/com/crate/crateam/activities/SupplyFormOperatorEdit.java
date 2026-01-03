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
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class SupplyFormOperatorEdit extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private ImageView iv_cross, iv_camera, iv_photo_one, iv_photo_two, iv_close_one, iv_close_two;
    private Button bt_ok, bt_cancel_waiting, bt_select_time, bt_exit, bt_discard, bt_driver_sign, bt_operator_sign, bt_third_party_yes, bt_third_party_no, bt_print_ticket_yes, bt_print_ticket_no, bt_submit,
            bt_weight, bt_unit;
    private LinearLayout ll_assign_site, ll_operator_sign, ll_driver_sign, ll_images, ll_third_party_sign, ll_internal_users;
    private TextView tv_waiting_time,tv_ticket_no, tv_user_name, tv_date, tv_time, tv_current_site, tv_sic_code, tv_vehicle_type, tv_haulier, tv_waste_carrier_no,
            tv_net_weight, tv_clear_operator, tv_clear_driver, tv_clear_third_party, tv_driver_name,tv_vehicle_reg_no,tv_material_value_caution;
    private EditText et_gross_weight, et_tare,et_unit, et_external_ticket, et_comment,et_add_driver_name;
    private SignaturePad signaturePadDriver, signaturePadOperator, signaturePadThirdParty;
    private CustomSearchableSpinner sp_current_site, sp_materials, sp_delivery_site,sp_delivery_reference,sp_delivery_area, sp_hour, sp_minute;
    private String user_name = "", image1="", image2="", selected_delivery_site_name = "", gross = "", tare = "",     driver_sign = "", operator_sign = "",
            third_party_sign = "", print_ticket = "", registration_no = "", third_party_signature_option = "", store_sic_code = "",
            current_site_address = "", current_site_name = "", current_date = "", current_time = "", material_des = "", hour = "", minute = "", deviceMacAddress = "",
            new_driver_name = "", new_reg_no = "", weight_unit = "", ewc_code = "",selected_delivery_site_address="",ticket_no="",site_name="",
            waste_carrier_id = "", haulier_company_id = "", vehicle_type_id = "",material_value="",
            selected_delivery_reference_name="",selected_delivery_area_name="";
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference  materialReference, assignMaterialReference, vehicleDetailsReference, supplyWasteFormsReference,
            siteManagementReference, taskManagementReference, deliveryReferenceDetails,deliveryLocationReference;
    private boolean hasImage = false, hasImageTwo = false, isSigned = false, isSignedOperator = false, isSignedThird = false, validation_done = false;
    private Bitmap getDrawable1, getDrawable2;
    private int user_id = 0, user_role_id, selected_delivery_site_id = 0, site_id = 0, material_id = 0,fetched_material_id=0, registration_id = 0,
            selected_user_id = 0, address_management_id=0,delivery_site_id_addressManagement=0,selected_delivery_reference_id =0,selected_delivery_area_id=0;
    private ProgressDialog progressDialog, printerDialog;
    double net = 0.0;
    private PopupWindow popupWindow;
    private AlertDialog  alertDialog_time;
    private ArrayList<Integer> assignMaterialDocumentId = new ArrayList<>();
    private ArrayList<Integer> material_id_list = new ArrayList<>();
    private ArrayList<String> store_ewc_code_list = new ArrayList<>();
    private ArrayList<String> ewc_code_list = new ArrayList<>();
    private ArrayList<String> store_material_value_list = new ArrayList<>();
    private ArrayList<String> material_value_list = new ArrayList<>();
    private ArrayList<String> material_des_list = new ArrayList<>();
    private ArrayList<String> arrayList_hour = new ArrayList<>();
    private ArrayList<String> arrayList_minute = new ArrayList<>();
    private ArrayList<String> arrayList_second = new ArrayList<>();
    private ArrayList<Integer> siteId = new ArrayList<>();
    private ArrayList<String> siteName = new ArrayList<>();
    private ArrayList<Integer> store_material_doc_list = new ArrayList<>();
    private ArrayList<String> store_material_des_list = new ArrayList<>();
    private ArrayList<Integer> selected_material_id_list = new ArrayList<>();
    private ArrayList<Integer> arrayList_site_id = new ArrayList<>();
    private ArrayList<Integer> arrayList_delivery_reference_id = new ArrayList<>();
    private ArrayList<String> arrayList_delivery_reference_name = new ArrayList<>();
    private ArrayList<Integer> allDeliveryReferenceIds= new ArrayList<>();
    private ArrayList<String>allDeliveryReferenceNames = new ArrayList<>();
    private ArrayList<Integer> arrayList_delivery_area_id = new ArrayList<>();
    private ArrayList<String> arrayList_delivery_area_name = new ArrayList<>();
    private ArrayList<Integer> allDeliveryAreaIds= new ArrayList<>();
    private ArrayList<String>allDeliveryAreaNames = new ArrayList<>();
    private LinearLayout  ll_gross, ll_tare, ll_net, ll_unit;
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
        setContentView(R.layout.supply_form_operator_edit);
        initView();
    }

    private void initView() {
        FirestoreManager.initPersistentIndexManager();
        progressDialog = Dialog.showProgressDialog(this);
        supplyWasteFormsReference = db.collection("CR_supply_waste_forms");
        vehicleDetailsReference = db.collection("CR_vehicle_details");
        assignMaterialReference = db.collection("CR_assign_material");
        materialReference = db.collection("CR_material");
        siteManagementReference = db.collection("CR_site_management");
        taskManagementReference = db.collection("CR_task_management");
        deliveryReferenceDetails = db.collection("CR_delivery_reference_details");
        deliveryLocationReference = db.collection("CR_delivery_location_details");
        SessionManager sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_name = user.get(SessionManager.KEY_FULL_NAME);
        user_id = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        String role = user.get(SessionManager.KEY_ROLE_ONE);
        if (role != null)
            user_role_id = Integer.parseInt(role);
        Log.d(TAG,"USER_ID : " + user_name);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        iv_cross = findViewById(R.id.iv_cross);
        iv_camera = findViewById(R.id.iv_camera);
        iv_photo_one = findViewById(R.id.iv_photo_one);
        iv_photo_two = findViewById(R.id.iv_photo_two);
        iv_close_one = findViewById(R.id.iv_close_one);
        iv_close_two = findViewById(R.id.iv_close_two);
        bt_third_party_yes = findViewById(R.id.bt_third_party_yes);
        bt_third_party_no = findViewById(R.id.bt_third_party_no);
        bt_print_ticket_yes = findViewById(R.id.bt_print_ticket_yes);
        bt_print_ticket_no = findViewById(R.id.bt_print_ticket_no);
        bt_discard = findViewById(R.id.bt_discard);
        bt_exit = findViewById(R.id.bt_exit);
        bt_select_time = findViewById(R.id.bt_select_time);
        bt_driver_sign = findViewById(R.id.bt_driver_sign);
        bt_operator_sign = findViewById(R.id.bt_operator_sign);
        bt_submit = findViewById(R.id.bt_submit);
        ll_assign_site = findViewById(R.id.ll_assign_site);
        ll_driver_sign = findViewById(R.id.ll_driver_sign);
        ll_operator_sign = findViewById(R.id.ll_operator_sign);
        ll_internal_users = findViewById(R.id.ll_internal_users);
        ll_third_party_sign = findViewById(R.id.ll_third_party_sign);
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
        tv_current_site = findViewById(R.id.tv_current_site);
        tv_sic_code = findViewById(R.id.tv_sic_code);
        et_external_ticket = findViewById(R.id.et_external_ticket);
        et_comment = findViewById(R.id.et_comment);
        et_add_driver_name = findViewById(R.id.et_add_driver_name);
        tv_ticket_no = findViewById(R.id.tv_ticket_no);
        tv_waiting_time = findViewById(R.id.tv_waiting_time);
        tv_date = findViewById(R.id.tv_date);
        tv_time = findViewById(R.id.tv_time);
        tv_vehicle_type = findViewById(R.id.tv_vehicle_type);
        tv_haulier = findViewById(R.id.tv_haulier);
        tv_material_value_caution = findViewById(R.id.tv_material_value_caution);
        signaturePadDriver = findViewById(R.id.signature_pad_driver);
        signaturePadOperator = findViewById(R.id.signature_pad_operator);
        signaturePadThirdParty = findViewById(R.id.signature_pad_third_party);
        sp_materials = findViewById(R.id.sp_materials);
        sp_current_site = findViewById(R.id.sp_current_site);
        sp_delivery_site = findViewById(R.id.sp_delivery_site);
        sp_delivery_reference = findViewById(R.id.sp_delivery_reference);
        sp_delivery_area = findViewById(R.id.sp_delivery_area);
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
        initializeOnClick();
        ll_assign_site.setVisibility(View.VISIBLE);
        getSiteAddress();
        ticket_no = getIntent().getStringExtra("ticket_no");
        if (ticket_no != null) {
            if (!ticket_no.equals("")) {
                fetchLastTaskDetails(ticket_no);
            }
        }
        if (!AppData.internetOnline(this))
            progressDialog.dismiss();
        setSignedDriver();
        setSignOperator();
        setSignedThirdParty();
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
                    Log.d(TAG,"Material ids:" +material_id_list);
                    material();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormOperatorEdit.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(SupplyFormOperatorEdit.this, R.layout.spinner_custom_layout, material_des_list);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                    sp_materials.setAdapter(spinnerArrayAdapter);
                    int position = selected_material_id_list.indexOf(fetched_material_id);
                    sp_materials.setSelection(position);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SupplyFormOperatorEdit.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
        bt_third_party_yes.setOnClickListener(this);
        bt_third_party_no.setOnClickListener(this);
        bt_print_ticket_yes.setOnClickListener(this);
        bt_print_ticket_no.setOnClickListener(this);
        bt_weight.setOnClickListener(this);
        bt_unit.setOnClickListener(this);
        bt_submit.setOnClickListener(this);
        sp_current_site.setOnItemSelectedListener(this);
        sp_materials.setOnItemSelectedListener(this);
        sp_delivery_site.setOnItemSelectedListener(this);
        sp_delivery_reference.setOnItemSelectedListener(this);
        sp_delivery_area.setOnItemSelectedListener(this);
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
                    updateSupplyForm();
                    if (!AppData.internetOnline(this)) {
                        progressDialog.dismiss();
                        toConfirmation("Form updated successfully.");
                    }
                }
                break;
            case R.id.bt_weight:
                weightVisible();
                break;
            case R.id.bt_unit:
                unitVisible();
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
            case R.id.bt_exit:
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
                    Toast.makeText(SupplyFormOperatorEdit.this, "Message", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_current_site:
                current_site_name = arrayList_assign_site_name.get(position).toString().trim();
                site_id = arrayList_assign_site_id.get(position);
                Log.d(TAG,"Current site id :" + site_id);
                progressDialog.show();
                getSicCode(site_id);
                if (!AppData.internetOnline(SupplyFormOperatorEdit.this)) {
                    progressDialog.dismiss();
                }
                break;
            case R.id.sp_materials:
                material_des = sp_materials.getSelectedItem().toString();
                material_id = selected_material_id_list.get(position);
                Log.d(TAG,"Material id list :" + material_id_list + " " + ewc_code_list);
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
                Log.d(TAG,"AERREA :" +selected_delivery_site_id);
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
        Log.d(TAG,"New Reg No: " +new_reg_no+"  "+registration_no);
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
        if (site_id==0)
            Dialog.alertDialog(this, "Please select any assign site");
        if (material_des.equals("select Material") || material_des.equals(""))
            Dialog.alertDialog(this, "Please select  material");
        else if (selected_delivery_site_id == 0)
            Dialog.alertDialog(this, "Please select delivery site");
        else if (selected_delivery_site_name.equals("Select delivery site"))
            Dialog.alertDialog(this, "Please select delivery site");
        else if (material_value.equals("High") && image1.equals(""))
            Dialog.alertDialog(this,"Please take a picture of the material being transferred.");
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

    private void toConfirmation(String message) {
        progressDialog.dismiss();
        final View popupView = getLayoutInflater().inflate(R.layout.change_pass_popup_layout, null, true);
         boolean isActivityInForeground = SupplyFormOperatorEdit.this.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED);
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
                Intent toDashboard = new Intent(SupplyFormOperatorEdit.this, MainActivity.class);
                startActivity(toDashboard);
            }
        });
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
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(SupplyFormOperatorEdit.this, R.layout.spinner_custom_layout, arrayList_hour);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_hour.setAdapter(spinnerArrayAdapter);
        ArrayAdapter<String> spinnerArrayAdapter1 = new ArrayAdapter<String>(SupplyFormOperatorEdit.this, R.layout.spinner_custom_layout, arrayList_minute);
        spinnerArrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_minute.setAdapter(spinnerArrayAdapter1);
        hourAdapter = new HourAdapter(SupplyFormOperatorEdit.this, arrayList_hour);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv_hour.setLayoutManager(mLayoutManager);
        rv_hour.setItemAnimator(new DefaultItemAnimator());
        rv_hour.setAdapter(hourAdapter);

        minuteAdapter = new MinuteAdapter(SupplyFormOperatorEdit.this, arrayList_minute);
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

 

    private Bitmap decodeImageString(String imageString){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] imageBytes = baos.toByteArray();
        imageBytes = Base64.decode(imageString, Base64.DEFAULT);
        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        return decodedImage;
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

    private void getSiteAddress() {
        arrayList_assign_site_name = new ArrayList<>();
        arrayList_assign_site_id = new ArrayList<>();
        Query query = siteManagementReference.whereEqualTo("site_type","Collection").whereEqualTo("address_status","Active").whereEqualTo("status", "Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        arrayList_assign_site_name.add(queryDocumentSnapshot.getString("site_name"));
                        arrayList_assign_site_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                    }
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(SupplyFormOperatorEdit.this, R.layout.spinner_custom_layout, arrayList_assign_site_name);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_current_site.setAdapter(spinnerArrayAdapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SupplyFormOperatorEdit.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enableBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(SupplyFormOperatorEdit.this, "Message1", Toast.LENGTH_SHORT).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                if (ContextCompat.checkSelfPermission(SupplyFormOperatorEdit.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(SupplyFormOperatorEdit.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
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
        if (ContextCompat.checkSelfPermission(SupplyFormOperatorEdit.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(SupplyFormOperatorEdit.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
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
            Intent connectIntent = new Intent(SupplyFormOperatorEdit.this,
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
            if (ContextCompat.checkSelfPermission(SupplyFormOperatorEdit.this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(SupplyFormOperatorEdit.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
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
            if (selected_delivery_area_id==0)
                selected_delivery_area_name="";
            if (selected_delivery_reference_id==0)
                selected_delivery_reference_name = "";

            Bitmap supply_layout = new PrintingLayoutFormat(this).supplyFormLayoutToBitmap(tv_ticket_no.getText().toString(), et_comment.getText().toString(), user_name, current_site_name,selected_delivery_site_name,
                    current_site_address, selected_delivery_site_address,selected_delivery_area_name,selected_delivery_reference_name, tv_haulier.getText().toString(), new_driver_name, waste_carrier_id, current_date + " " + current_time, registration_no, material_des,
                    et_unit.getText().toString(), et_gross_weight.getText().toString(), et_tare.getText().toString(), tv_net_weight.getText().toString(), signaturePadOperator.getSignatureBitmap(), signaturePadDriver.getSignatureBitmap());
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

    // getting delivery site name according to material id
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
                Toast.makeText(SupplyFormOperatorEdit.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Log.d(TAG,"Site Management Site Ids :" +siteId+"  "+siteName);
                        if (siteName.size() > 0) {
                            siteName.add(0, "Select delivery site");
                            siteId.add(0, 0);
                        }
                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_custom_layout, siteName);
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                        sp_delivery_site.setAdapter(spinnerArrayAdapter);
                        getDeliverySiteIdFromAddressManagement(delivery_site_id_addressManagement);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormOperatorEdit.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            current_site_address = queryDocumentSnapshot.getString("address");
                        }
                        assignMaterial(site_id);
                    }
                    else
                        progressDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SupplyFormOperatorEdit.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SupplyFormOperatorEdit.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getVehicleNumber(int vehicle_id){
        Query query = vehicleDetailsReference.whereEqualTo("id",vehicle_id);
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
                            Log.d(TAG,"VEHICLE REG NO:" + registration_no);
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

    private void fetchLastTaskDetails(String ticket_no){
        progressDialog.show();
        Query query = supplyWasteFormsReference.whereEqualTo("ticket_no",ticket_no);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    current_date = AppData.date();
                    current_time = AppData.Time();
                    for (QueryDocumentSnapshot documentSnapshot: Objects.requireNonNull(task.getResult())) {
                        tv_ticket_no.setText(ticket_no);
                        store_sic_code = documentSnapshot.getString("sic_code");
                        ewc_code = documentSnapshot.getString("ewc_code");
                        user_name = documentSnapshot.getString("operator");
                        new_driver_name = documentSnapshot.getString("new_driver_name");
                        selected_user_id = documentSnapshot.getLong("new_driver_id").intValue();
                        String vehicle_registration_no = documentSnapshot.getString("registration_no");
                        tv_vehicle_reg_no.setText(vehicle_registration_no);
                        tv_waiting_time.setText(documentSnapshot.getString("waiting_time"));
                        registration_id = documentSnapshot.getLong("vehicle_id").intValue();
                        getVehicleNumber(registration_id);
                        tv_vehicle_reg_no.setVisibility(View.VISIBLE);
                        String unit = documentSnapshot.getString("unit");
                        if (unit.equals("")) {
                            Log.e("weight","weight");
                            weightVisible();
                            String gross_weight = documentSnapshot.getString("gross_weight");
                            et_gross_weight.setText(gross_weight);
                            String tare = documentSnapshot.getString("tare");
                            et_tare.setText(tare);
                            String net_weight = documentSnapshot.getString("net");
                            tv_net_weight.setText(net_weight);
                        }else {
                            unitVisible();
                            et_unit.setText(unit);
                            Log.e("unit","unit");
                        }
                        tv_driver_name.setVisibility(View.VISIBLE);
                        tv_driver_name.setText(new_driver_name);
                        et_comment.setText(documentSnapshot.getString("add_comment"));
                        et_external_ticket.setText(documentSnapshot.getString("external_ticket_no"));
                        if (documentSnapshot.getLong("current_site_id_new").intValue()== 0)
                            address_management_id = documentSnapshot.getLong("current_site_id").intValue();
                        else
                            address_management_id = documentSnapshot.getLong("current_site_id_new").intValue();
                        getCollectionSiteIdFromAddressManagement(address_management_id);
                        if (documentSnapshot.getLong("material_id_new").intValue()== 0 )
                            fetched_material_id = documentSnapshot.getLong("material_id").intValue();
                        else
                            fetched_material_id = documentSnapshot.getLong("material_id_new").intValue();
                        if (documentSnapshot.getLong("delivery_site_id_new").intValue() == 0)
                            delivery_site_id_addressManagement = documentSnapshot.getLong("delivery_site_id").intValue();
                        else
                            delivery_site_id_addressManagement = documentSnapshot.getLong("delivery_site_id_new").intValue();
                        selected_delivery_reference_id = documentSnapshot.getLong("delivery_reference").intValue();
                        selected_delivery_area_id = documentSnapshot.getLong("delivery_location").intValue();
                        assignMaterialsDeliverySite(fetched_material_id);
                        image1 = documentSnapshot.getString("z_image_one");
                        if (!image1.isEmpty()) {
                            ll_images.setVisibility(View.VISIBLE);
                            iv_photo_one.setImageBitmap(decodeImageString(image1));
                        }
                        image2 = documentSnapshot.getString("z_image_two");
                        if (!image2.isEmpty())
                            iv_photo_two.setImageBitmap(decodeImageString(image2));
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SupplyFormOperatorEdit.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void unitVisible(){
        weight_unit="unit";
        ll_gross.setVisibility(View.GONE);
        ll_tare.setVisibility(View.GONE);
        ll_net.setVisibility(View.GONE);
        et_gross_weight.setText("");
        et_tare.setText("");
        tv_net_weight.setText("");
        ll_unit.setVisibility(View.VISIBLE);
        bt_weight.setBackground(getResources().getDrawable(R.drawable.edit_text_background));
        bt_unit.setBackground(getResources().getDrawable(R.drawable.login_button_background));
        bt_weight.setTextColor(getResources().getColor(R.color.black_shade_two));
        bt_unit.setTextColor(getResources().getColor(R.color.white));
    }
    private void weightVisible(){
        weight_unit="weight";
        ll_gross.setVisibility(View.VISIBLE);
        ll_tare.setVisibility(View.VISIBLE);
        ll_net.setVisibility(View.VISIBLE);
        ll_unit.setVisibility(View.GONE);
        et_unit.setText("");
        bt_weight.setBackground(getResources().getDrawable(R.drawable.login_button_background));
        bt_unit.setBackground(getResources().getDrawable(R.drawable.edit_text_background));
        bt_weight.setTextColor(getResources().getColor(R.color.white));
        bt_unit.setTextColor(getResources().getColor(R.color.black_shade_two));
    }
    private void getCollectionSiteIdFromAddressManagement(int address_management_id){
        Query query = siteManagementReference.whereEqualTo("id",address_management_id).whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        site_id= Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
                    }
                    int position = arrayList_assign_site_id.indexOf(site_id);
                    Log.d(TAG,"Show Collection Site Id:" +site_id+" "+position+" "+arrayList_assign_site_id);
                    sp_current_site.setSelection(position);
                    current_site_name = getSiteName(site_id);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormOperatorEdit.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getDeliverySiteIdFromAddressManagement(int address_management_id){
        Query query = siteManagementReference.whereEqualTo("id",address_management_id).whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        selected_delivery_site_id= Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
                    }
                    int position = siteId.indexOf(selected_delivery_site_id);
                    Log.d(TAG,"Show Delivery Id:" +selected_delivery_site_id+" "+position+" "+siteId);
                    sp_delivery_site.setSelection(position);
                    selected_delivery_site_name = getSiteName(selected_delivery_site_id);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormOperatorEdit.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String getSiteName(int site_id){
        Query siteManagement = siteManagementReference.whereEqualTo("id",site_id).whereEqualTo("status","Active");
        siteManagement.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            site_name = queryDocumentSnapshot.getString("site_name");
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormOperatorEdit.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        return site_name;
    }

    private void getDeliveryReferenceIds(int site_id){
        Query query =  siteManagementReference.whereEqualTo("id",site_id).whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    String delivery_reference_ids = "";
                    if (!task.getResult().isEmpty()){
                        arrayList_delivery_reference_id.clear();
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
                        arrayList_delivery_reference_id.add(0,0);
                    }
                    getDeliveryReferenceNames();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormOperatorEdit.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDeliveryReferenceNames(){
        Query query = deliveryReferenceDetails.whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        allDeliveryReferenceIds.clear();
                        arrayList_delivery_reference_name.clear();
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
                        arrayList_delivery_reference_name.add(0,"Select Delivery Reference");
                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_custom_layout,arrayList_delivery_reference_name);
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        sp_delivery_reference.setAdapter(spinnerArrayAdapter);
                        int position = arrayList_delivery_reference_id.indexOf(selected_delivery_reference_id);
                        Log.d(TAG,"Show Delivery Reference Id:" +selected_delivery_reference_id+" "+position+" "+arrayList_delivery_reference_id+" " +arrayList_delivery_reference_name);
                        sp_delivery_reference.setSelection(position);

                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormOperatorEdit.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        arrayList_delivery_area_id.clear();
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
                        arrayList_delivery_area_id.add(0,0);
                    }
                    getDeliveryAreaNames();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormOperatorEdit.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        arrayList_delivery_area_name.add(0,"Select Site Location");
                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_custom_layout,arrayList_delivery_area_name);
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        sp_delivery_area.setAdapter(spinnerArrayAdapter);
                        int position = arrayList_delivery_area_id.indexOf(selected_delivery_area_id);
                        Log.d(TAG,"Show Delivery Area Id:" +selected_delivery_area_id+" "+position+" "+arrayList_delivery_area_id+" " +arrayList_delivery_area_name);
                        sp_delivery_area.setSelection(position);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormOperatorEdit.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSupplyForm(){
        Query query = supplyWasteFormsReference.whereEqualTo("ticket_no",ticket_no);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        submissionTime();
                        for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                            String doc_id = queryDocumentSnapshot.getId();
                            String gross_weight = et_gross_weight.getText().toString().trim();
                            String tare = et_tare.getText().toString().trim();
                            Map<String,Object> updateSupplyForm= new HashMap<>();
                            if (address_management_id != site_id)
                                updateSupplyForm.put("current_site_id_new", site_id);
                            updateSupplyForm.put("current_site_address", current_site_address);
                            if (fetched_material_id != material_id)
                                updateSupplyForm.put("material_id_new", material_id);
                            if (delivery_site_id_addressManagement != selected_delivery_site_id)
                                updateSupplyForm.put("delivery_site_id_new",selected_delivery_site_id);
                            updateSupplyForm.put("delivery_reference",selected_delivery_reference_id);
                            updateSupplyForm.put("delivery_location",selected_delivery_area_id);
                            updateSupplyForm.put("ewc_code",ewc_code);
                            if (ewc_code.equals("")) {
                                updateSupplyForm.put("ewc_available", "No");
                                updateSupplyForm.put("sic_code", "43.12");
                            }else {
                                updateSupplyForm.put("ewc_available", "Yes");
                                updateSupplyForm.put("sic_code",store_sic_code);
                            }
                            updateSupplyForm.put("submission_date", current_date);
                            updateSupplyForm.put("submission_time", current_time);
                            if (!gross_weight.isEmpty()) {
                                net = Double.parseDouble(gross_weight) - Double.parseDouble(tare);
                                showNetValueFromGross();
                                showNetValueFromTare();
                                updateSupplyForm.put("gross_weight", gross_weight);
                                updateSupplyForm.put("tare", tare);
                                updateSupplyForm.put("net", String.valueOf(net));
                            }else {
                                updateSupplyForm.put("gross_weight", "");
                                updateSupplyForm.put("tare", "");
                                updateSupplyForm.put("net", "");
                            }
                            if (!et_unit.getText().toString().trim().isEmpty())
                                updateSupplyForm.put("unit",et_unit.getText().toString().trim());
                            else
                                updateSupplyForm.put("unit","");
                            if (driver_sign.equals(""))
                                driver_sign="No";
                            updateSupplyForm.put("z_driver_sign", driver_sign);
                            updateSupplyForm.put("z_operator_sign", operator_sign);
                            if (third_party_signature_option.equals("No"))
                                third_party_sign = "No";
                            updateSupplyForm.put("z_third_party_sign", third_party_sign);
                            updateSupplyForm.put("print_ticket", print_ticket);
                            updateSupplyForm.put("add_comment",et_comment.getText().toString().trim());
                            updateSupplyForm.put("waiting_time",tv_waiting_time.getText().toString().trim());
                            updateSupplyForm.put("external_ticket_no",et_external_ticket.getText().toString().trim());
                            updateSupplyForm.put("time_second_format",AppData.getTimeSecond());
                            updateSupplyForm.put("form_edited","Yes");
                            updateSupplyForm.put("is_updated","Yes");
                            supplyWasteFormsReference.document(doc_id).update(updateSupplyForm);
                            toConfirmation("Form submitted successfully.");
                            updateTaskManagement();
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Error :" , e.getMessage());
            }
        });
    }
    private void showNetValueFromGross(){
        et_gross_weight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                String value_gross = et_gross_weight.getText().toString().trim();
                String value_tare = et_tare.getText().toString().trim();
                if (!value_gross.isEmpty() && !value_tare.isEmpty()) {
                    if (Double.parseDouble(value_gross)>=Double.parseDouble(value_tare)) {
                        net = Double.parseDouble(value_gross) - Double.parseDouble(value_tare);
                        Log.d(TAG,"NET value :" + net);
                        tv_net_weight.setText(String.valueOf(net));
                    }else {
                        tv_net_weight.setText("");
                    }
                }else
                    tv_net_weight.setText("");
            }
        });
    }
    private void showNetValueFromTare(){
        et_tare.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                String value_gross = et_gross_weight.getText().toString().trim();
                String value_tare = et_tare.getText().toString().trim();
                if (!value_gross.isEmpty() && !value_tare.isEmpty()) {
                    if (Double.parseDouble(value_gross)>=Double.parseDouble(value_tare)) {
                        net = Double.parseDouble(value_gross) - Double.parseDouble(value_tare);
                        Log.d(TAG,"NET value :" + net);
                        tv_net_weight.setText(String.valueOf(net));
                    }else {
                        tv_net_weight.setText("");
                    }
                }else
                    tv_net_weight.setText("");
            }
        });
    }
    private void updateTaskManagement(){
        Query query = taskManagementReference.whereEqualTo("id",ticket_no);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                            String docId = queryDocumentSnapshot.getId();
                            Map<String,Object> updateTask = new HashMap<>();
                            updateTask.put("ewc_code",ewc_code);
                            updateTask.put("form_edited","Yes");
                            updateTask.put("is_updated","Yes");
                            updateTask.put("time_second_format",AppData.getTimeSecond());
                            taskManagementReference.document(docId).update(updateTask);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"Error :" + e.getMessage());
            }
        });
    }
    private void submissionTime(){
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        current_date = simpleDateFormat.format(date);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        current_time = time_format.format(calendar.getTime());
    }
}
