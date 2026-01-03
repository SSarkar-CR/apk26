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
import android.text.InputFilter;
import android.text.Spanned;
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

public class WasteTransferFormOperator extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private ImageView iv_cross,iv_add_driver,iv_camera,iv_photo_one,iv_close_one;
    private Bitmap waste_transfer_print_layout,getDrawable1;
    private Button bt_ok,bt_cancel_waiting,bt_ok_two,bt_cancel_waiting_two,bt_select_on_site,bt_select_off_site,bt_discard, bt_driver_sign, bt_operator_sign, bt_print_ticket_yes, bt_print_ticket_no, bt_submit,bt_submit_new,
            bt_exit, bt_add_reg_no, bt_size_16y, bt_size_20y, bt_size_30y, bt_size_40y, bt_size_6y, bt_size_8y, bt_size_12y, bt_size_15y, bt_size_18T,bt_weight,bt_unit;
    private LinearLayout ll_assign_site,ll_operator_sign, ll_driver_sign,ll_container1,ll_container2,ll_container3,ll_current_site,
            ll_weight_unit,ll_unit,ll_gross,ll_tare,ll_net,ll_images,ll_container_type;
    private TextView tv_on_site,tv_off_site,tv_new_reg_no, tv_ticket_no, tv_operator, tv_date, tv_time, tv_current_site, tv_current_site_address, tv_vehicle_type, tv_haulier, tv_waste_carrier_no,
            tv_clear_operator, tv_clear_driver,tv_driver_name,tv_waste_carrier_no_new,tv_header,tv_caution_two,tv_vehicle_reg_no,tv_net_weight,tv_material_value_caution;
    private EditText et_new_reg_no,et_comment,et_new_driver_name,et_add_driver_name,et_gross_weight,et_tare,et_unit,et_collection_reference,et_external_ticket;
    private SignaturePad signaturePadDriver, signaturePadOperator;
    private CustomSearchableSpinner sp_hour,sp_minute,sp_hour_two,sp_minute_two,sp_current_site,sp_materials, sp_delivery_site,sp_delivery_reference,sp_delivery_area,sp_container_type, sp_reg_no,sp_driver_name_w,sp_trailer_no;
    private String user_name="", first_container_size = "", second_container_size = "", third_container_size = "", print_ticket = "",new_driver_name = "",
            driver_sign = "", operator_sign = "", current_date,new_haulier_company="",current_site_address = "",current_site_name = "", material_des = "", registration_no = "",
            selected_delivery_site_name="",selected_delivery_site_address="",new_reg_no = "", new_vehicle_type = "", new_waste_carrier = "",comment = "",hour="",minute="",hourTwo="",minuteTwo="",deviceMacAddress="",current_time="",permit_no="",
            doc_id="",doc_id_task="", ewc_code="",store_sic_code="",project_no="",submission_date="",submission_time="",max_docId_user_details="",max_docId_vehicle_details="",
            latitude="",longitude="",delivery_site_name="",vehicle_type_id = "",waste_carrier_id = "",haulier_company_id="",
            weight_unit = "", gross = "", tare = "",image1="",image2="",material_value="",selected_container_type="";
    double net = 0.0;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference supplyWasteFormsReference, assignMaterialReference, materialReference, vehicleDetailsReference,trailerDetailsReference,
            newRegistrationReference,userDetailsReference,assignSiteReference, sortKeyTableReference,siteManagementReference,
            taskManagementReference,currentVehicleReference,inspectionSubmissionReference,containerTypeReference;
    private int selected_position=0, user_id = 0, user_role_id = 0, site_id = 0,material_id = 0, registration_id = 0,
            new_waste_carrier_id = 0,selected_delivery_site_id=0,delivery_site_id_siteManagement=0, selected_user_id=0,doc_id_length=0,
            doc_id_length_task=0,haulier_id=0,address_management_id=0,new_user_id=0,new_vehicle_id=0,
            trailer_id=0;
    private boolean isSigned = false, isSignedOperator = false, validation_done = false, validation_window = false,
            showAddDriverButton = true,isInspectionDone = false,hasImage = false;
    private ProgressDialog progressDialog,printerDialog;
    private PopupWindow popupWindow;
    private AlertDialog alertDialog;
    private CustomSearchableSpinner sp_new_vehicle_type, sp_new_haulier;
    private ArrayList<String> material_des_list= new ArrayList<>();
    private ArrayList<Integer> selected_material_id_list = new ArrayList<>();
    private ArrayList<String> new_vehicle_type_list = new ArrayList<>();
    private ArrayList<String>siteName = new ArrayList<>();
    private ArrayList<String>allUserNames = new ArrayList<>();
    private ArrayList<String> store_ewc_code_list = new ArrayList<>();
    private ArrayList<String> ewc_code_list = new ArrayList<>();
    private ArrayList<String> store_material_value_list = new ArrayList<>();
    private ArrayList<String> material_value_list = new ArrayList<>();
    private ArrayList<String> registration_no_list = new ArrayList<>();
    private ArrayList<String>external_vehicle_list = new ArrayList<>();
    private ArrayList<String> newHaulierList = new ArrayList<>();
    private ArrayList<String> haulierList= new ArrayList<>();
    private ArrayList<String> haulierCarrierNoList = new ArrayList<>();
    private ArrayList<String> vehicle_type_list= new ArrayList<>();
    private ArrayList<Integer> registration_no_id_list= new ArrayList<>();
    private ArrayList<Integer> allUserIds= new ArrayList<>();
    private ArrayList<Integer>siteId = new ArrayList<>();
    private ArrayList<Integer> assignMaterialDocumentId= new ArrayList<>();
    private ArrayList<Integer> material_id_list= new ArrayList<>();
    private ArrayList<Integer> arrayList_user_id = new ArrayList<>();
    private ArrayList<String> arrayList_user_name = new ArrayList<>();
    private ArrayList<Integer> store_material_doc_list = new ArrayList<>();
    private ArrayList<String> store_material_des_list = new ArrayList<>();
    private ArrayList<String> arrayList_hour = new ArrayList<>();
    private ArrayList<String> arrayList_minute = new ArrayList<>();
    private ArrayList<String> arrayList_second = new ArrayList<>();
    private ArrayList<Integer> arrayList_site_id = new ArrayList<>();
    private ArrayList<String> arrayList_assign_site_name= new ArrayList<>();
    private ArrayList<Integer> arrayList_assign_site_id = new ArrayList<>();
    private ArrayList<Integer> arrayList_delivery_reference_id = new ArrayList<>();
    private ArrayList<Integer> arrayList_delivery_area_id = new ArrayList<>();
    private ArrayList<String> arrayList_container_type = new ArrayList<>();
    private ArrayList<String> trailer_no_list = new ArrayList<>();
    private ArrayList<Integer> trailer_id_list = new ArrayList<>();
    private LinearLayout ll_add_new_reg;
    private AlertDialog alertDialog_time;
    private RecyclerView rv_hour,rv_minute,rv_hour_two,rv_minute_two;
    private HourAdapter hourAdapter;
    private MinuteAdapter minuteAdapter;
    protected static final String TAG = "TAG";
    private static final int REQUEST_CAMERA= 0;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    BluetoothAdapter mBluetoothAdapter;
    private ZebraPrinter printer;
    private Connection connection;
    private Uri imageUri;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waste_transfer_form_operator);
        initView();
    }

    private void initView() {
        FirestoreManager.initPersistentIndexManager();
        userDetailsReference = db.collection("CR_user_details");
        supplyWasteFormsReference = db.collection("CR_supply_waste_forms");
        assignMaterialReference = db.collection("CR_assign_material");
        materialReference = db.collection("CR_material");
        vehicleDetailsReference = db.collection("CR_vehicle_details");
        trailerDetailsReference = db.collection("CR_trailer_details");
        newRegistrationReference = db.collection("CR_new_registration");
        assignSiteReference = db.collection("CR_assign_site");
        siteManagementReference = db.collection("CR_site_management");
        sortKeyTableReference = db.collection("CR_sort_key");
        taskManagementReference = db.collection("CR_task_management");
        currentVehicleReference = db.collection("CR_current_vehicle");
        inspectionSubmissionReference = db.collection("CR_inspection_submission");
        containerTypeReference = db.collection("CR_container_types");

        SessionManager sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_name = user.get(SessionManager.KEY_FULL_NAME);
        user_id = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        String role = user.get(SessionManager.KEY_ROLE_ONE);
        if (role != null)
            user_role_id = Integer.parseInt(role);

        Log.d(TAG,"USER_NAME: " + user_name);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        iv_cross = findViewById(R.id.iv_cross);
        iv_camera = findViewById(R.id.iv_camera);
        iv_photo_one = findViewById(R.id.iv_photo_one);
        iv_close_one = findViewById(R.id.iv_close_one);
        bt_select_on_site = findViewById(R.id.bt_select_on_site);
        bt_select_off_site = findViewById(R.id.bt_select_off_site);
        bt_discard = findViewById(R.id.bt_discard);
        bt_driver_sign = findViewById(R.id.bt_driver_sign);
        bt_operator_sign = findViewById(R.id.bt_operator_sign);
        bt_print_ticket_yes = findViewById(R.id.bt_print_ticket_yes);
        bt_print_ticket_no = findViewById(R.id.bt_print_ticket_no);
        bt_submit = findViewById(R.id.bt_submit);
        ll_add_new_reg = findViewById(R.id.ll_add_new_reg);
        ll_assign_site = findViewById(R.id.ll_assign_site);
        ll_driver_sign = findViewById(R.id.ll_driver_sign);
        ll_operator_sign = findViewById(R.id.ll_operator_sign);
        ll_container1 = findViewById(R.id.ll_container1);
        ll_container2 = findViewById(R.id.ll_container2);
        ll_container3 = findViewById(R.id.ll_container3);
        ll_container_type = findViewById(R.id.ll_container_type);
        ll_images = findViewById(R.id.ll_images);
        ll_weight_unit = findViewById(R.id.ll_weight_unit);
        ll_unit = findViewById(R.id.ll_unit);
        ll_gross = findViewById(R.id.ll_gross);
        ll_tare = findViewById(R.id.ll_tare);
        ll_net = findViewById(R.id.ll_net);
        ll_current_site = findViewById(R.id.ll_current_site);
        if (user_role_id == 4)
            ll_current_site.setVisibility(View.GONE);
        tv_header = findViewById(R.id.tv_header);
        tv_ticket_no = findViewById(R.id.tv_ticket_no);
        tv_operator = findViewById(R.id.tv_operator);
        tv_operator.setText(user_name);
        tv_driver_name = findViewById(R.id.tv_driver_name);
        tv_vehicle_reg_no = findViewById(R.id.tv_vehicle_reg_no);
        tv_date = findViewById(R.id.tv_date);
        tv_time = findViewById(R.id.tv_time);
        tv_current_site = findViewById(R.id.tv_current_site);
        tv_current_site_address = findViewById(R.id.tv_current_site_address);
        tv_vehicle_type = findViewById(R.id.tv_vehicle_type);
        tv_haulier = findViewById(R.id.tv_haulier);
        tv_waste_carrier_no = findViewById(R.id.tv_waste_carrier_no);
        tv_on_site = findViewById(R.id.tv_on_site);
        tv_off_site = findViewById(R.id.tv_off_site);
        tv_caution_two = findViewById(R.id.tv_caution_two);
        tv_material_value_caution = findViewById(R.id.tv_material_value_caution);
        tv_net_weight = findViewById(R.id.tv_net_weight);
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start,
                                       int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.toString(source.charAt(i)).matches("[a-zA-Z0-9 ]+")) {
                        return "";
                    }
                }
                return null;
            }
        };
        et_collection_reference = findViewById(R.id.et_collection_reference);
        et_external_ticket = findViewById(R.id.et_external_ticket);
        et_comment = findViewById(R.id.et_comment);
        et_collection_reference.setFilters(new InputFilter[] { filter });
        et_external_ticket.setFilters(new InputFilter[] { filter });
        et_comment.setFilters(new InputFilter[] { filter });
        et_add_driver_name = findViewById(R.id.et_add_driver_name);
        et_gross_weight = findViewById(R.id.et_gross_weight);
        et_tare = findViewById(R.id.et_tare);
        et_unit = findViewById(R.id.et_unit);
        sp_current_site = findViewById(R.id.sp_current_site);
        sp_materials = findViewById(R.id.sp_materials);
        sp_reg_no = findViewById(R.id.sp_reg_no);
        sp_driver_name_w = findViewById(R.id.sp_driver_name_w);
        sp_delivery_site = findViewById(R.id.sp_delivery_site);
        sp_delivery_reference = findViewById(R.id.sp_delivery_reference);
        sp_delivery_area = findViewById(R.id.sp_delivery_area);
        sp_container_type  = findViewById(R.id.sp_container_type);
        sp_trailer_no = findViewById(R.id.sp_trailer_no);
        tv_new_reg_no = findViewById(R.id.tv_new_reg_no);
        tv_clear_driver = findViewById(R.id.tv_clear_driver);
        tv_clear_operator = findViewById(R.id.tv_clear_operator);
        progressDialog = Dialog.showProgressDialog(this);

        bt_add_reg_no = findViewById(R.id.bt_add_reg_no);
        bt_size_16y = findViewById(R.id.bt_size_16y);
        bt_size_20y = findViewById(R.id.bt_size_20y);
        bt_size_30y = findViewById(R.id.bt_size_30y);
        bt_size_40y = findViewById(R.id.bt_size_40y);
        bt_size_6y = findViewById(R.id.bt_size_6y);
        bt_size_12y = findViewById(R.id.bt_size_12y);
        bt_size_8y = findViewById(R.id.bt_size_8y);
        bt_size_15y = findViewById(R.id.bt_size_15y);
        bt_size_18T = findViewById(R.id.bt_size_18T);
        bt_weight = findViewById(R.id.bt_weight);
        bt_unit = findViewById(R.id.bt_unit);
        iv_add_driver = findViewById(R.id.iv_add_driver);
        signaturePadDriver = findViewById(R.id.signature_pad_driver);
        signaturePadOperator = findViewById(R.id.signature_pad_operator);
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
        if (user_role_id!=4) {
            getCurrentSiteName();
            ll_assign_site.setVisibility(View.GONE);
            tv_vehicle_reg_no.setVisibility(View.GONE);
            tv_driver_name.setVisibility(View.GONE);
            sp_reg_no.setVisibility(View.VISIBLE);
            sp_driver_name_w.setVisibility(View.VISIBLE);
        }
        else {
            ll_assign_site.setVisibility(View.VISIBLE);
            getSiteAddress();
            getInspectionSubmission();
        }
        initializeOnClick();
        generateTicket();
        setSignDriver();
        setSignOperator();
        getMaximumLength();
        getMaximumLengthTaskManagement();
        fetchVehicleRegistrationNumber();
        getMaxDocIDUserDetails();
        getMaxDocIDVehicleDetails();
        getTrailerNumber();
        if (!AppData.internetOnline(this))
            progressDialog.dismiss();
    }

    private void initializeOnClick() {
        iv_cross.setOnClickListener(this);
        iv_camera.setOnClickListener(this);
        iv_close_one.setOnClickListener(this);
        iv_photo_one.setOnClickListener(this);
        bt_add_reg_no.setOnClickListener(this);
        bt_select_on_site.setOnClickListener(this);
        bt_select_off_site.setOnClickListener(this);
        bt_discard.setOnClickListener(this);
        bt_driver_sign.setOnClickListener(this);
        bt_operator_sign.setOnClickListener(this);
        tv_clear_driver.setOnClickListener(this);
        tv_clear_operator.setOnClickListener(this);
        bt_print_ticket_yes.setOnClickListener(this);
        bt_print_ticket_no.setOnClickListener(this);
        sp_current_site.setOnItemSelectedListener(this);
        sp_materials.setOnItemSelectedListener(this);
        sp_delivery_site.setOnItemSelectedListener(this);
        sp_container_type.setOnItemSelectedListener(this);
        sp_reg_no.setOnItemSelectedListener(this);
        sp_trailer_no.setOnItemSelectedListener(this);
        sp_driver_name_w.setOnItemSelectedListener(this);
        bt_size_16y.setOnClickListener(this);
        bt_size_20y.setOnClickListener(this);
        bt_size_30y.setOnClickListener(this);
        bt_size_40y.setOnClickListener(this);
        bt_size_6y.setOnClickListener(this);
        bt_size_8y.setOnClickListener(this);
        bt_size_12y.setOnClickListener(this);
        bt_size_15y.setOnClickListener(this);
        bt_size_18T.setOnClickListener(this);
        bt_weight.setOnClickListener(this);
        bt_unit.setOnClickListener(this);
        bt_submit.setOnClickListener(this);
        iv_add_driver.setOnClickListener(this);
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_cross:
                finish();
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
            case R.id.iv_close_one:
                iv_photo_one.setImageDrawable(getResources().getDrawable(R.drawable.placeholder_image));
                hasImage = false;
                break;
            case R.id.bt_submit:
                Log.d(TAG,"gfjytfty :" +checkValidation());
                if (checkValidation()) {
                    submitWasteTransferFormOperator();
                    if (!AppData.internetOnline(WasteTransferFormOperator.this)) {
                        progressDialog.dismiss();
                        toConfirmation("Form submitted successfully.");
                    }
                }
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
            case R.id.bt_print_ticket_yes:
                print_ticket = "Yes";
                if (checkValidation()){
                    bt_print_ticket_no.setEnabled(false);
                    bt_discard.setVisibility(View.GONE);
                    iv_cross.setVisibility(View.GONE);
                    setSelectedButtonBackground(bt_print_ticket_yes);
                    setDefaultButtonBackground(bt_print_ticket_no);
                    enableBluetooth();
                }
                break;
            case R.id.bt_print_ticket_no:
                print_ticket = "No";
                setSelectedButtonBackground(bt_print_ticket_no);
                setDefaultButtonBackground(bt_print_ticket_yes);
                bt_discard.setVisibility(View.VISIBLE);
                iv_cross.setVisibility(View.VISIBLE);
                break;
            case R.id.bt_size_16y:
                first_container_size = bt_size_16y.getText().toString().trim();
                setSelectContainerBackground(bt_size_16y);
                if (!vehicle_type_id.equals("Skiploader") && !vehicle_type_id.equals("8w Roro") && !vehicle_type_id.equals("8w Tipper")){
                    selected_position = 1;
                    setDefaultContainerBackground(bt_size_20y, bt_size_30y, bt_size_40y,bt_size_6y,bt_size_8y, bt_size_12y, bt_size_15y);
                }
                else
                    setDefaultContainerBackground(bt_size_20y, bt_size_30y, bt_size_40y);
                break;
            case R.id.bt_size_20y:
                first_container_size = bt_size_20y.getText().toString().trim();
                setSelectContainerBackground(bt_size_20y);
                if (!vehicle_type_id.equals("Skiploader") && !vehicle_type_id.equals("8w Roro") && !vehicle_type_id.equals("8w Tipper")){
                    selected_position = 2;
                    setDefaultContainerBackground(bt_size_16y, bt_size_30y, bt_size_40y,bt_size_6y,bt_size_8y, bt_size_12y, bt_size_15y);
                }
                else
                    setDefaultContainerBackground(bt_size_16y, bt_size_30y, bt_size_40y);
                break;
            case R.id.bt_size_30y:
                first_container_size = bt_size_30y.getText().toString().trim();
                setSelectContainerBackground(bt_size_30y);
                if (!vehicle_type_id.equals("Skiploader") && !vehicle_type_id.equals("8w Roro") && !vehicle_type_id.equals("8w Tipper")){
                    selected_position = 3;
                    setDefaultContainerBackground(bt_size_16y, bt_size_20y, bt_size_40y,bt_size_6y,bt_size_8y, bt_size_12y, bt_size_15y);
                }
                else
                    setDefaultContainerBackground(bt_size_20y, bt_size_16y, bt_size_40y);
                break;
            case R.id.bt_size_40y:
                first_container_size = bt_size_40y.getText().toString().trim();
                setSelectContainerBackground(bt_size_40y);
                if (!vehicle_type_id.equals("Skiploader") && !vehicle_type_id.equals("8w Roro") && !vehicle_type_id.equals("8w Tipper")){
                    selected_position = 4;
                    setDefaultContainerBackground(bt_size_16y, bt_size_20y, bt_size_30y,bt_size_6y,bt_size_8y, bt_size_12y, bt_size_15y);
                }
                else
                    setDefaultContainerBackground(bt_size_20y, bt_size_30y, bt_size_16y);
                break;
            case R.id.bt_size_6y:
                second_container_size = bt_size_6y.getText().toString().trim();
                setSelectContainerBackground(bt_size_6y);
                if (!vehicle_type_id.equals("Skiploader") && !vehicle_type_id.equals("8w Roro") && !vehicle_type_id.equals("8w Tipper")){
                    selected_position = 5;
                    setDefaultContainerBackground(bt_size_16y, bt_size_20y, bt_size_30y,bt_size_40y,bt_size_8y, bt_size_12y, bt_size_15y);
                }
                else
                    setDefaultContainerBackground(bt_size_8y, bt_size_12y, bt_size_15y);
                break;
            case R.id.bt_size_8y:
                second_container_size = bt_size_8y.getText().toString().trim();
                setSelectContainerBackground(bt_size_8y);
                if (!vehicle_type_id.equals("Skiploader") && !vehicle_type_id.equals("8w Roro") && !vehicle_type_id.equals("8w Tipper")){
                    selected_position = 6;
                    setDefaultContainerBackground(bt_size_16y, bt_size_20y, bt_size_30y,bt_size_40y,bt_size_6y, bt_size_12y, bt_size_15y);
                }
                else
                    setDefaultContainerBackground(bt_size_6y, bt_size_12y, bt_size_15y);
                break;
            case R.id.bt_size_12y:
                second_container_size = bt_size_12y.getText().toString().trim();
                setSelectContainerBackground(bt_size_12y);
                if (!vehicle_type_id.equals("Skiploader") && !vehicle_type_id.equals("8w Roro") && !vehicle_type_id.equals("8w Tipper")){
                    selected_position = 7;
                    setDefaultContainerBackground(bt_size_16y, bt_size_20y, bt_size_30y,bt_size_40y,bt_size_6y, bt_size_8y, bt_size_15y);
                }
                else
                    setDefaultContainerBackground(bt_size_8y, bt_size_15y, bt_size_6y);
                break;
            case R.id.bt_size_15y:
                second_container_size = bt_size_15y.getText().toString().trim();
                setSelectContainerBackground(bt_size_15y);
                if (!vehicle_type_id.equals("Skiploader") && !vehicle_type_id.equals("8w Roro") && !vehicle_type_id.equals("8w Tipper")) {
                    setDefaultContainerBackground(bt_size_16y, bt_size_20y, bt_size_30y, bt_size_40y, bt_size_6y, bt_size_8y, bt_size_12y);
                    selected_position = 8;
                }
                else
                    setDefaultContainerBackground(bt_size_8y, bt_size_12y, bt_size_6y);
                break;
            case R.id.bt_size_18T:
                third_container_size = bt_size_18T.getText().toString().trim();
                setSelectContainerBackground(bt_size_18T);
                break;
            case R.id.bt_weight:
                weight_unit = "weight";
                ll_gross.setVisibility(View.VISIBLE);
                ll_tare.setVisibility(View.VISIBLE);
                ll_net.setVisibility(View.VISIBLE);
                ll_unit.setVisibility(View.GONE);

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
                    vehicle_type_id = new_vehicle_type;
                    containerSize(vehicle_type_id);
                    tv_waste_carrier_no.setText(new_waste_carrier);
                    tv_haulier.setText(new_haulier_company);
                    alertDialog.cancel();
                    tv_driver_name.setVisibility(View.VISIBLE);
                    tv_new_reg_no.setVisibility(View.VISIBLE);
                    sp_driver_name_w.setVisibility(View.GONE);
                    bt_add_reg_no.setText("Edit");
                }
                break;
            case R.id.bt_exit:
                alertDialog.cancel();
                progressDialog.dismiss();
                break;
            case R.id.bt_select_on_site:
                customizeWaitingTime();
                break;
            case R.id.bt_select_off_site:
                customizeWaitingTimeOffSite();
                break;
            case R.id.bt_ok:
                if (hour.equals(""))
                    Dialog.alertDialog(this,"Please select hour value.");
                else if (minute.equals(""))
                    Dialog.alertDialog(this,"Please select minute value.");
                else {
                    tv_on_site.setText(hour + ":" + minute);
                    alertDialog_time.cancel();
                }
                break;
            case R.id.bt_ok_two:
                if (hourTwo.equals(""))
                    Dialog.alertDialog(this,"Please select hour value.");
                else if (minuteTwo.equals(""))
                    Dialog.alertDialog(this,"Please select minute value.");
                else {
                    tv_off_site.setText(hourTwo + ":" + minuteTwo);
                    alertDialog_time.cancel();
                }
                break;
            case R.id.bt_cancel_waiting:
                hour = "";
                minute = "";
                alertDialog_time.cancel();
                break;
            case R.id.bt_cancel_waiting_two:
                hourTwo = "";
                minuteTwo = "";
                alertDialog_time.cancel();
                break;
            case R.id.iv_add_driver:
                iv_add_driver.setImageDrawable(null);
                if (showAddDriverButton) {
                    sp_driver_name_w.setVisibility(View.GONE);
                    sp_driver_name_w.setAdapter(null);
                    et_add_driver_name.setVisibility(View.VISIBLE);
                    iv_add_driver.setBackgroundResource(R.drawable.remove);
                    showAddDriverButton = false;
                }else {
                    sp_driver_name_w.setVisibility(View.VISIBLE);
                    getUserIdList(registration_id);
                    et_add_driver_name.setVisibility(View.GONE);
                    et_add_driver_name.setText("");
                    iv_add_driver.setBackgroundResource(R.drawable.add);
                    showAddDriverButton = true;
                }
                break;
        }
    }

    private void setSelectContainerBackground(Button bt_container_size_select) {
        bt_container_size_select.setBackground(getResources().getDrawable(R.drawable.login_button_background));
        bt_container_size_select.setTextColor(getResources().getColor(R.color.white));
    }

    private void setDefaultContainerBackground(Button bt_container_one, Button bt_container_two, Button bt_container_three) {
        bt_container_one.setBackground(getResources().getDrawable(R.drawable.defects_details_bg));
        bt_container_one.setTextColor(getResources().getColor(R.color.blue_shade_three));
        bt_container_two.setBackground(getResources().getDrawable(R.drawable.defects_details_bg));
        bt_container_two.setTextColor(getResources().getColor(R.color.blue_shade_three));
        bt_container_three.setBackground(getResources().getDrawable(R.drawable.defects_details_bg));
        bt_container_three.setTextColor(getResources().getColor(R.color.blue_shade_three));
    }

    private void setDefaultContainerBackground(Button bt_container_one, Button bt_container_two, Button bt_container_three,
                                               Button bt_container_four, Button bt_container_five, Button bt_container_six, Button bt_container_seven) {
        bt_container_one.setBackground(getResources().getDrawable(R.drawable.defects_details_bg));
        bt_container_one.setTextColor(getResources().getColor(R.color.blue_shade_three));
        bt_container_two.setBackground(getResources().getDrawable(R.drawable.defects_details_bg));
        bt_container_two.setTextColor(getResources().getColor(R.color.blue_shade_three));
        bt_container_three.setBackground(getResources().getDrawable(R.drawable.defects_details_bg));
        bt_container_three.setTextColor(getResources().getColor(R.color.blue_shade_three));

        bt_container_four.setBackground(getResources().getDrawable(R.drawable.defects_details_bg));
        bt_container_four.setTextColor(getResources().getColor(R.color.blue_shade_three));

        bt_container_five.setBackground(getResources().getDrawable(R.drawable.defects_details_bg));
        bt_container_five.setTextColor(getResources().getColor(R.color.blue_shade_three));

        bt_container_six.setBackground(getResources().getDrawable(R.drawable.defects_details_bg));
        bt_container_six.setTextColor(getResources().getColor(R.color.blue_shade_three));

        bt_container_seven.setBackground(getResources().getDrawable(R.drawable.defects_details_bg));
        bt_container_seven.setTextColor(getResources().getColor(R.color.blue_shade_three));
    }

    private void setSelectedButtonBackground(Button selected_button) {
        selected_button.setBackground(getResources().getDrawable(R.drawable.login_button_background));
        selected_button.setTextColor(getResources().getColor(R.color.white));
    }

    private void setDefaultButtonBackground(Button default_button) {
        default_button.setBackground(getResources().getDrawable(R.drawable.edit_text_background));
        default_button.setTextColor(getResources().getColor(R.color.black_shade_two));
    }

    private void containerSize(String vehicle_type_id){
        if (vehicle_type_id.equals("Skiploader")){
            ll_container1.setVisibility(View.GONE);
            ll_container2.setVisibility(View.VISIBLE);
            ll_container3.setVisibility(View.GONE);
            ll_container_type.setVisibility(View.GONE);
            first_container_size ="";
            third_container_size="";
            invisibleWeightUnit();
        }else if (vehicle_type_id.equals("8w Roro")){
            ll_container1.setVisibility(View.VISIBLE);
            ll_container2.setVisibility(View.GONE);
            ll_container3.setVisibility(View.GONE);
            ll_container_type.setVisibility(View.GONE);
            second_container_size="";
            third_container_size="";
            invisibleWeightUnit();
        } else if (vehicle_type_id.equals("8w Tipper")){
            ll_container1.setVisibility(View.GONE);
            ll_container2.setVisibility(View.GONE);
            ll_container3.setVisibility(View.GONE);
            ll_container_type.setVisibility(View.GONE);
            first_container_size ="";
            second_container_size="";
            third_container_size="";
            invisibleWeightUnit();
        } else if (vehicle_type_id.equals("N1")||vehicle_type_id.equals("N2")||vehicle_type_id.equals("N3")){
            ll_container_type.setVisibility(View.GONE);
            visibleWeightUnit();
        } else if (vehicle_type_id.equals("Artic Unit")){
            ll_container1.setVisibility(View.GONE);
            ll_container2.setVisibility(View.GONE);
            ll_container3.setVisibility(View.GONE);
            ll_container_type.setVisibility(View.VISIBLE);
            visibleWeightUnit();
            getContainerType();
        }else {
            ll_container1.setVisibility(View.VISIBLE);
            ll_container2.setVisibility(View.VISIBLE);
            ll_container3.setVisibility(View.VISIBLE);
            ll_container_type.setVisibility(View.GONE);
            invisibleWeightUnit();
        }
    }

    private void visibleWeightUnit(){
       ll_weight_unit.setVisibility(View.VISIBLE);
       ll_container1.setVisibility(View.GONE);
       ll_container2.setVisibility(View.GONE);
       ll_container3.setVisibility(View.GONE);
       first_container_size ="";
       second_container_size="";
       third_container_size="";
    }
    private void invisibleWeightUnit(){
        ll_weight_unit.setVisibility(View.GONE);
        ll_unit.setVisibility(View.GONE);
        ll_gross.setVisibility(View.GONE);
        ll_tare.setVisibility(View.GONE);
        ll_net.setVisibility(View.GONE);
        gross="";
        tare="";
        net=0.0;
        et_unit.setText("");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);
        switch (requestCode) {
            case REQUEST_CAMERA:
                if(resultCode == Activity.RESULT_OK){
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
                            if (!hasImage) {
                                iv_photo_one.setImageBitmap(rotatedBitmap);
                                image1 = AppData.convertTOBase64Image(getResizedBitmap(rotatedBitmap,700));
                                hasImage = true;
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
                    Toast.makeText(WasteTransferFormOperator.this, "Message", Toast.LENGTH_SHORT).show();
                }
                break;
        }
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

    private void generateTicket() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");
        String currentDate = sdf.format(new Date());
        String ticket_no = "W" + currentDate + String.format("%06d", number);
        Log.d(TAG,"Ticket No:" + ticket_no);
        tv_ticket_no.setText(ticket_no);
        Log.e("ticket_no",ticket_no);
    }
    
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_current_site:
                current_site_name = arrayList_assign_site_name.get(position).toString().trim();
                site_id = arrayList_assign_site_id.get(position);
                Log.d(TAG,"Collection site id:" +site_id+" "+user_role_id);
                if (user_role_id == 4){
                    progressDialog.show();
                    getSicCode(site_id);
                }else
                    getCurrentSiteName();
                if (!AppData.internetOnline(this)){
                    progressDialog.dismiss();
                }
                break;
            case R.id.sp_materials:
                material_des = sp_materials.getSelectedItem().toString();
                material_id = selected_material_id_list.get(position); // material_id_list
                ewc_code = ewc_code_list.get(position);
                material_value = material_value_list.get(position);
                if (material_value.equals("High"))
                    tv_material_value_caution.setVisibility(View.VISIBLE);
                else
                    tv_material_value_caution.setVisibility(View.GONE);
                if (ewc_code.equals("")){
                    tv_header.setText("CL:aire Ticket \n- Collection");
                    tv_caution_two.setText(R.string.sign_caution_three);
                }else {
                    tv_header.setText("Waste Transfer Ticket \n- Collection");
                    tv_caution_two.setText(R.string.sign_caution_two);
                }
                assignMaterialsDeliverySite(material_id);
                if (!AppData.internetOnline(this))
                    progressDialog.dismiss();
                break;
            case R.id.sp_delivery_site:
                selected_delivery_site_name = siteName.get(position).trim();
                selected_delivery_site_id = siteId.get(position);                                                              
                getDeliverySiteID(selected_delivery_site_id);
                break;
            case R.id.sp_delivery_reference:

                break;
            case R.id.sp_delivery_area:

                break;
            case R.id.sp_container_type:
                 selected_container_type = arrayList_container_type.get(position).trim();
                break;
            case R.id.sp_reg_no:
                registration_no = sp_reg_no.getSelectedItem().toString().trim();
                tv_vehicle_type.setText("");
                tv_haulier.setText("");
                tv_waste_carrier_no.setText("");
                Log.d(TAG,"Reg no: " +registration_no);
                Log.d(TAG,"Reg id :" +registration_id);
                if (registration_no.equals("Add new")) {
                    ll_add_new_reg.setVisibility(View.VISIBLE);
                    int last_element =registration_no_list.lastIndexOf(registration_no);
                    registration_no_list.remove(last_element);
                    tv_driver_name.setVisibility(View.VISIBLE);
                    tv_new_reg_no.setVisibility(View.VISIBLE);
                    sp_driver_name_w.setVisibility(View.GONE);
                    tv_new_reg_no.setText("");
                    tv_driver_name.setText("");
                    bt_add_reg_no.setText("Add");
                }else {
                    Log.d(TAG,"VT 11 :" +registration_no_id_list);
                    registration_id = registration_no_id_list.get(position);
                    vehicle_type_id = vehicle_type_list.get(position);
                    haulier_company_id = haulierList.get(position);
                    waste_carrier_id = haulierCarrierNoList.get(position);
                    tv_vehicle_type.setText(vehicle_type_id);
                    tv_waste_carrier_no.setText(waste_carrier_id);
                    tv_haulier.setText(haulier_company_id);
                    Log.d(TAG,"VT :" +vehicle_type_id);
                    containerSize(vehicle_type_id);
                    getUserIdList(registration_id);
                    ll_add_new_reg.setVisibility(View.GONE);
                    tv_driver_name.setVisibility(View.GONE);
                    tv_new_reg_no.setVisibility(View.GONE);
                    sp_driver_name_w.setVisibility(View.VISIBLE);
                }
                iv_add_driver.setVisibility(View.VISIBLE);
                if (!registration_no_list.contains("Add new"))
                    registration_no_list.add("Add new");
                if (!AppData.internetOnline(this))
                    progressDialog.dismiss();
                break;
            case R.id.sp_driver_name_w:
                selected_user_id =0;
                new_driver_name = sp_driver_name_w.getSelectedItem().toString().trim();
                selected_user_id= arrayList_user_id.get(position);
                break;
            case R.id.sp_new_vehicle_type:
                new_vehicle_type = sp_new_vehicle_type.getSelectedItem().toString().trim();
                break;
            case R.id.sp_new_haulier:
                new_haulier_company = sp_new_haulier.getSelectedItem().toString().trim();
                ArrayList<Integer> select_position = new ArrayList<>();
                select_position.add(haulierList.indexOf(new_haulier_company)) ;
                new_waste_carrier = haulierCarrierNoList.get(select_position.get(0));
                Log.d(TAG,"Waste Carrier No:" +new_waste_carrier);
                if (tv_waste_carrier_no_new!=null)
                    tv_waste_carrier_no_new.setText(new_waste_carrier);
                tv_waste_carrier_no.setText(new_waste_carrier);
                break;
            case R.id.sp_hour:
                hour = sp_hour.getSelectedItem().toString().trim();
                break;
            case R.id.sp_minute:
                minute = sp_minute.getSelectedItem().toString().trim();
                break;
            case R.id.sp_hour_two:
                hourTwo = sp_hour_two.getSelectedItem().toString().trim();
                break;
            case R.id.sp_minute_two:
                minuteTwo = sp_minute_two.getSelectedItem().toString().trim();
                break;
            case R.id.sp_trailer_no:
                trailer_id = trailer_id_list.get(position);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private boolean checkValidation() {
        String weight_unit_container = "";
        if (vehicle_type_id.equals("N1") || vehicle_type_id.equals("N2") || vehicle_type_id.equals("N3") || vehicle_type_id.equals("Artic Unit")){
            weight_unit_container = "WeightUnit";
        }else{
            weight_unit_container = "Container";
        }
        if (isSigned) {
            Bitmap signatureBitmap = signaturePadDriver.getSignatureBitmap();
            driver_sign = AppData.convertTOBase64Image(signatureBitmap);
        }
        if (isSignedOperator) {
            Bitmap signatureBitmapOperator = signaturePadOperator.getSignatureBitmap();
            operator_sign = AppData.convertTOBase64Image(signatureBitmapOperator);
        }

        if (user_role_id==4 &&  tv_current_site.getText().toString().trim().isEmpty())
            Dialog.alertDialog(this, "Please select any assign site");
        else if (material_des.equals("select Material") || material_des.equals(""))
            Dialog.alertDialog(this, "Please select material.");
        else if (selected_delivery_site_id==0)
            Dialog.alertDialog(this,"Please select delivery site");
        else if (selected_delivery_site_name.equals("Select delivery site"))
            Dialog.alertDialog(this, "Please select delivery site");
        else if (material_value.equals("High") && image1.equals(""))
            Dialog.alertDialog(this,"Please take a picture of the material being transferred.");
        else if (registration_no.equals("") && new_reg_no.equals(""))
            Dialog.alertDialog(this, "Please select registration number");
        else if (new_driver_name.equals("") && et_add_driver_name.getText().toString().isEmpty())
            Dialog.alertDialog(this,"Please select driver name");
        else if (trailer_id==0)
            Dialog.alertDialog(this, "Please select trailer number.");
        else if (tv_on_site.getText().toString().trim().isEmpty())
            Dialog.alertDialog(this, "Please select on site time.");
        else if (tv_off_site.getText().toString().trim().isEmpty())
            Dialog.alertDialog(this, "Please select off site time.");
        else if (weight_unit_container.equals("WeightUnit") && weight_unit.equals("")) {
            Dialog.alertDialog(this, "Please select weight or unit option");
        }else if (weight_unit_container.equals("WeightUnit")&&weight_unit.equals("weight") && et_gross_weight.getText().toString().trim().isEmpty()) {
            Dialog.alertDialog(this, "Please enter gross weight value");
        }else if (weight_unit_container.equals("WeightUnit")&&weight_unit.equals("weight") && et_tare.getText().toString().trim().isEmpty()) {
            Dialog.alertDialog(this, "Please enter tare value");
        }else if (weight_unit_container.equals("WeightUnit")&&weight_unit.equals("unit") && et_unit.getText().toString().trim().isEmpty()) {
            Dialog.alertDialog(this, "Please select unit value.");
        }
        else if (weight_unit_container.equals("Container")&&vehicle_type_id.equals("Skiploader") && second_container_size.isEmpty())
            Dialog.alertDialog(this, "Please select container size");
        else if (weight_unit_container.equals("Container")&&vehicle_type_id.equals("8w Roro") && first_container_size.isEmpty())
            Dialog.alertDialog(this, "Please select container size");
        else if (weight_unit_container.equals("Container")&&!vehicle_type_id.equals("Skiploader") && !vehicle_type_id.equals("8w Roro") && !vehicle_type_id.equals("8w Tipper")
                && first_container_size.isEmpty() && second_container_size.isEmpty())
            Dialog.alertDialog(this, "Please select container size");
        else if (weight_unit_container.equals("Container")&&!vehicle_type_id.equals("Skiploader") && !vehicle_type_id.equals("8w Roro") && !vehicle_type_id.equals("8w Tipper")
                && third_container_size.isEmpty())
            Dialog.alertDialog(this, "Please select container size");
        else if (!isSigned)
            Dialog.alertDialog(this, "Driver sign mandatory.");
        else if (!isSignedOperator)
            Dialog.alertDialog(this, "Operator sign mandatory.");
        else if (print_ticket.equals(""))
            Dialog.alertDialog(this, "Please select print ticket option");
        else
            validation_done = true;
        return validation_done;
    }

    private void setSignDriver() {
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

    private void submitWasteTransferFormOperator() {
        progressDialog.show();
        latLon();
        submissionTime();
        if (image1==null)
            image1="";
        if (image2==null)
            image2="";
        comment = et_comment.getText().toString().trim();
        if (!new_reg_no.equals(""))
            registration_id=0;
        if (!et_add_driver_name.getText().toString().isEmpty()) {
            new_driver_name = et_add_driver_name.getText().toString();
            selected_user_id = Integer.parseInt(max_docId_user_details)+1;
        }
        new_vehicle_id = Integer.parseInt(max_docId_vehicle_details) + 1;
        new_user_id=Integer.parseInt(max_docId_user_details)+1;
        Log.d(TAG,"Selected user id:" +selected_user_id);
        Log.d(TAG,"EWC CODE :" +ewc_code+" "+waste_carrier_id+" "+haulier_id+" "+new_waste_carrier_id);
        Map<String, Object> mapWasteTransferOperator = new HashMap<>();
        mapWasteTransferOperator.put("user_id", user_id);
        mapWasteTransferOperator.put("user_role_id", user_role_id);
        mapWasteTransferOperator.put("operator", user_name);
        mapWasteTransferOperator.put("ticket_no", tv_ticket_no.getText()+"C");
        mapWasteTransferOperator.put("ticket_type", "Collection");
        mapWasteTransferOperator.put("collection_ticket_no","");
        mapWasteTransferOperator.put("current_site_id", address_management_id);
        mapWasteTransferOperator.put("current_site_id_new", 0);
        mapWasteTransferOperator.put("current_site_address", current_site_address);
        mapWasteTransferOperator.put("material_id", material_id);
        mapWasteTransferOperator.put("material_id_new", 0);
        mapWasteTransferOperator.put("material_value","");
        mapWasteTransferOperator.put("delivery_site_id",selected_delivery_site_id);
        mapWasteTransferOperator.put("delivery_site_id_new",0);
        mapWasteTransferOperator.put("collection_reference",et_collection_reference.getText().toString());
        mapWasteTransferOperator.put("container_type",selected_container_type);
        mapWasteTransferOperator.put("container_type_new","");
        mapWasteTransferOperator.put("ewc_code",ewc_code);
        if (ewc_code.equals("")) {
            mapWasteTransferOperator.put("ewc_available", "No");
            mapWasteTransferOperator.put("sic_code", "43.12");
        }else {
            mapWasteTransferOperator.put("ewc_available", "Yes");
            mapWasteTransferOperator.put("sic_code",store_sic_code);
        }
        mapWasteTransferOperator.put("waste_carrier_no",tv_waste_carrier_no.getText().toString());
        mapWasteTransferOperator.put("project_no",project_no);
        mapWasteTransferOperator.put("submission_date", submission_date);
        mapWasteTransferOperator.put("submission_time", submission_time);
        if (driver_sign.equals(""))
            driver_sign="No";
        mapWasteTransferOperator.put("z_driver_sign", driver_sign);
        mapWasteTransferOperator.put("z_operator_sign", operator_sign);
        mapWasteTransferOperator.put("print_ticket", print_ticket);
        mapWasteTransferOperator.put("job_id",0);
        mapWasteTransferOperator.put("task_order_id",0);
        mapWasteTransferOperator.put("external_ticket_no", et_external_ticket.getText().toString());
        mapWasteTransferOperator.put("z_image_one",image1);
        mapWasteTransferOperator.put("material_id_new",0);
        mapWasteTransferOperator.put("vehicle_type", tv_vehicle_type.getText().toString());
//        mapWasteTransferOperator.put("vehicle_type_new", new_vehicle_type_id);
        mapWasteTransferOperator.put("vehicle_type_new","");
        mapWasteTransferOperator.put("haulier_id", tv_haulier.getText().toString());
//        mapWasteTransferOperator.put("haulier_id_new", new_waste_carrier_id);
        mapWasteTransferOperator.put("haulier_id_new","");
        mapWasteTransferOperator.put("waste_carrier_no_new","");
        mapWasteTransferOperator.put("add_comment",comment);
        mapWasteTransferOperator.put("permit_no_collection","");
        mapWasteTransferOperator.put("permit_no_delivery","");
        mapWasteTransferOperator.put("permit_no",permit_no);
        mapWasteTransferOperator.put("on_site_time",tv_on_site.getText().toString().trim());
        mapWasteTransferOperator.put("off_site_time",tv_off_site.getText().toString().trim());
        if (tv_vehicle_type.getText().toString().equals("N1") || tv_vehicle_type.getText().toString().equals("N2")
        || tv_vehicle_type.getText().toString().equals("N3")|| tv_vehicle_type.getText().toString().equals("Artic Unit")){
            mapWasteTransferOperator.put("gross_weight", gross);
            mapWasteTransferOperator.put("tare", tare);
            mapWasteTransferOperator.put("net", String.valueOf(net));
            mapWasteTransferOperator.put("unit", et_unit.getText().toString().trim());
            mapWasteTransferOperator.put("container_size_one", "");
            mapWasteTransferOperator.put("container_size_two", "");
            mapWasteTransferOperator.put("container_size_three","");
        }else {
            if (selected_position!=0){
                if (selected_position<=4){
                    mapWasteTransferOperator.put("container_size_one", first_container_size);
                    mapWasteTransferOperator.put("container_size_two", "");
                }
                else {
                    mapWasteTransferOperator.put("container_size_one", "");
                    mapWasteTransferOperator.put("container_size_two", second_container_size);
                }
            }
            else {
                mapWasteTransferOperator.put("container_size_one", first_container_size);
                mapWasteTransferOperator.put("container_size_two", second_container_size);
            }
            mapWasteTransferOperator.put("container_size_three", third_container_size);
            mapWasteTransferOperator.put("gross_weight","");
            mapWasteTransferOperator.put("tare","");
            mapWasteTransferOperator.put("net","");
            mapWasteTransferOperator.put("unit","");
        }
        if(user_role_id !=4 && !tv_driver_name.getText().toString().isEmpty()) {
            mapWasteTransferOperator.put("vehicle_id",new_vehicle_id);
            mapWasteTransferOperator.put("new_driver_id", new_user_id);
        } else if (user_role_id == 4 && !tv_driver_name.getText().toString().isEmpty() && !isInspectionDone) {
            mapWasteTransferOperator.put("vehicle_id", new_vehicle_id);
            mapWasteTransferOperator.put("new_driver_id", new_user_id);
        } else {
            mapWasteTransferOperator.put("vehicle_id", registration_id);
            mapWasteTransferOperator.put("new_driver_id", selected_user_id);
        }
        mapWasteTransferOperator.put("new_driver_name",new_driver_name);
        mapWasteTransferOperator.put("registration_no_new","");
        mapWasteTransferOperator.put("registration_no","");
        mapWasteTransferOperator.put("note_type","Collection");
        mapWasteTransferOperator.put("trailer_id", trailer_id);
        mapWasteTransferOperator.put("id",tv_ticket_no.getText().toString());
        mapWasteTransferOperator.put("status","Active");
        doc_id_length = doc_id_length+1;
        mapWasteTransferOperator.put("sort_key",doc_id_length);
        mapWasteTransferOperator.put("form_status","Completed");
        mapWasteTransferOperator.put("ticket_status","Open");
        mapWasteTransferOperator.put("ticket_edited","No");
        mapWasteTransferOperator.put("backend_edited","No");
        mapWasteTransferOperator.put("mail_send","No");
        mapWasteTransferOperator.put("pdf_send","No");
        mapWasteTransferOperator.put("latitude",latitude);
        mapWasteTransferOperator.put("longitude",longitude);
        mapWasteTransferOperator.put("time_second_format",AppData.getTimeSecond());
        mapWasteTransferOperator.put("form_edited","");
        mapWasteTransferOperator.put("is_updated","Yes");

        supplyWasteFormsReference.document(tv_ticket_no.getText()+"C").set(mapWasteTransferOperator).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    toConfirmation("Form submitted successfully.");
                    sendTaskData(submission_date,submission_time,tv_ticket_no.getText().toString());
                    Map<String, Object> objectMap_update = new HashMap<>();
                    objectMap_update.put("supply_waste_key", doc_id_length);
                    sortKeyTableReference.document(doc_id).update(objectMap_update);
                    String reg_no = "", form_type = "", pdf_type = "";
                    if (new_reg_no.isEmpty())
                        reg_no = registration_no;
                    else
                        reg_no = new_reg_no;
                    if (ewc_code.equals("")) {
                        form_type = "CL:aire Ticket";
                        pdf_type = "ClaireTicket.pdf";
                    } else {
                        form_type = "Waste Transfer Ticket";
                        pdf_type = "WasteTransferTicket.pdf";
                    }
                    Log.d(TAG,"Pdf and Form type :" +pdf_type+ " "+form_type);
                    if (!tv_driver_name.getText().toString().isEmpty() && !isInspectionDone) {
                        updateVehicleDetails();
                        updateUserDetails();
                        addDataToNewRegistrationTable(new_reg_no, user_id, new_driver_name, submission_date, submission_time, tv_ticket_no.getText().toString(),
                                tv_vehicle_type.getText().toString(),tv_haulier.getText().toString(), site_id, selected_delivery_site_id, new_vehicle_id, new_user_id);

                    }else if (!et_add_driver_name.getText().toString().isEmpty() && !isInspectionDone){
                        updateUserDetails();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(WasteTransferFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toConfirmation(String message) {
        progressDialog.dismiss();
        final View popupView = getLayoutInflater().inflate(R.layout.change_pass_popup_layout, null, true);
         boolean isActivityInForeground = WasteTransferFormOperator.this.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED);
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
                Intent toDashboard = new Intent(WasteTransferFormOperator.this, MainActivity.class);
                startActivity(toDashboard);
            }
        });
    }

    private void getCurrentSiteName(){
        progressDialog.show();
        Query query = assignSiteReference.whereEqualTo("user_id", user_id).whereEqualTo("date", current_date);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            site_id = Objects.requireNonNull(queryDocumentSnapshot.getLong("site_id")).intValue();
                            current_site_name = queryDocumentSnapshot.getString("site_name");
                        }
                        Log.d(TAG,"SITE ID : " + site_id);
                        getSicCode(site_id);
                    }else {
                        progressDialog.dismiss();
                        if (user_role_id != 4)
                            alertDialogAssignSite("You are not assigned with any site.Please assign yourself with a site.");
                    }
                }  else
                    progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(WasteTransferFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
                Toast.makeText(WasteTransferFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void material() {
        Query query = materialReference.whereEqualTo("status", "Active").whereEqualTo("material_type","Collection");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
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
                    for (int i = 0;i<material_id_list.size();i++){
                        for (int j = 0;j<store_material_doc_list.size();j++){
                            if (material_id_list.get(i).equals(store_material_doc_list.get(j))){
                                material_des_list.add(store_material_des_list.get(j));
                                selected_material_id_list.add(material_id_list.get(i));
                                ewc_code_list.add(store_ewc_code_list.get(j));
                                material_value_list.add(store_material_value_list.get(j));
                                break;
                            }
                        }
                    }
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_custom_layout, material_des_list);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                    sp_materials.setAdapter(spinnerArrayAdapter);
                    int position = material_id_list.indexOf(material_id);
                    sp_materials.setSelection(position);
                    getProjectNo();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(WasteTransferFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(WasteTransferFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void customizeWindow() {
        AlertDialog.Builder customize_alert_dialog = new AlertDialog.Builder(this);
        customize_alert_dialog.setTitle("Add new registration number");
        customize_alert_dialog.setCancelable(false);
        View customize_view = getLayoutInflater().inflate(R.layout.add_new_reg, null);
        et_new_driver_name = customize_view.findViewById(R.id.et_new_driver_name);
        et_new_reg_no = customize_view.findViewById(R.id.et_new_reg_no);
        sp_new_vehicle_type = customize_view.findViewById(R.id.sp_new_vehicle_type);
        sp_new_haulier = customize_view.findViewById(R.id.sp_new_haulier);
        tv_waste_carrier_no_new = customize_view.findViewById(R.id.tv_waste_carrier_no_new);
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

    private boolean newVehiclePopupValidation() {
        validation_window = false;
        if (et_new_driver_name.getText().toString().equals(""))
            Dialog.alertDialog(this, "Please select driver name.");
        else if (et_new_reg_no.getText().toString().trim().isEmpty())
            Dialog.alertDialog(this, "Please enter registration number");
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
        newRegistrationMap.put("status","Active");
        newRegistrationMap.put("new_vehicle_id",vehicle_id);
        newRegistrationMap.put("new_driver_id",user_id);
        newRegistrationMap.put("is_updated","Yes");
        newRegistrationReference.document(ticket_no).set(newRegistrationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                Log.d(TAG,"Success :"+"New registration data submitted successfully.");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG,"Error :"+"Unable to submit new registration data.");
            }
        });
    }
    private void getUserIdList(int selected_vehicle_id){
        progressDialog.show();
        Query query = vehicleDetailsReference.whereEqualTo("id",selected_vehicle_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    String userIds = "";
                    arrayList_user_id.clear();
                    sp_driver_name_w.setAdapter(null);
                    new_driver_name="";
                    selected_user_id = 0;
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            userIds = queryDocumentSnapshot.getString("vehicle_user_mappings");
                        }
                        if (userIds != null && userIds.contains(",")) {
                            ArrayList<String> userIdListString = new ArrayList<String>(Arrays.asList(userIds.split(",")));
                            for (int i = 0; i < userIdListString.size(); i++) {
                                arrayList_user_id.add(Integer.valueOf(userIdListString.get(i)));
                            }
                        } else {
                            if (userIds != null) {
                                if (!userIds.equals("")) {
                                    arrayList_user_id.add(Integer.valueOf(userIds));
                                }
                            }
                        }
                        Log.d(TAG,"User Ids :" +arrayList_user_id);
                        getFullName();
                    }
                    else {
                        progressDialog.dismiss();
                        dataNotFound("No driver assigned with this vehicle.");
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(WasteTransferFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getFullName(){
        Query query = userDetailsReference.whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    arrayList_user_name.clear();
                    if (!task.getResult().isEmpty()){
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
                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_custom_layout,arrayList_user_name);
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        sp_driver_name_w.setAdapter(spinnerArrayAdapter);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(WasteTransferFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void dataNotFound(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog  = builder.create();
        alertDialog.show();
    }
    private void alertDialogAssignSite(String message){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent assignSite = new Intent(WasteTransferFormOperator.this,AssignSiteActivity.class);
                        startActivity(assignSite);
                        finish();    
                    }
                });
        alertDialog.show();
    }
    private void getSiteAddress(){
        Query query = siteManagementReference.whereEqualTo("site_type","Collection").whereEqualTo("address_status","Active")
                .whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    Log.d(TAG,"I am here:" +"1");
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            Log.d(TAG,"I am here:" +"2");
                            arrayList_assign_site_name.add(queryDocumentSnapshot.getString("site_name"));
                            arrayList_assign_site_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                        }
                        Log.d(TAG,"Site Names :" +arrayList_assign_site_name);
                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_custom_layout, arrayList_assign_site_name);
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                        sp_current_site.setAdapter(spinnerArrayAdapter);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(WasteTransferFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void customizeWaitingTime(){
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
        for(int i=0;i<=100;i++){
            if (i<10)
                arrayList_hour.add("0"+String.valueOf(i));
            else
                arrayList_hour.add(String.valueOf(i));
        }
        for(int i=0;i<60;i++){
            if (i<10)
                arrayList_minute.add("0"+String.valueOf(i));
            else
                arrayList_minute.add(String.valueOf(i));
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(WasteTransferFormOperator.this,R.layout.spinner_custom_layout,arrayList_hour);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_hour.setAdapter(spinnerArrayAdapter);
        ArrayAdapter<String> spinnerArrayAdapter1 = new ArrayAdapter<String>(WasteTransferFormOperator.this,R.layout.spinner_custom_layout,arrayList_minute);
        spinnerArrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_minute.setAdapter(spinnerArrayAdapter1);
        hourAdapter = new HourAdapter(WasteTransferFormOperator.this,arrayList_hour);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv_hour.setLayoutManager(mLayoutManager);
        rv_hour.setItemAnimator(new DefaultItemAnimator());
        rv_hour.setAdapter(hourAdapter);

        minuteAdapter = new MinuteAdapter(WasteTransferFormOperator.this,arrayList_minute);
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

    private void customizeWaitingTimeOffSite(){
        AlertDialog.Builder customize_alert_dialog = new AlertDialog.Builder(this);
        customize_alert_dialog.setTitle("Set waiting time");
        customize_alert_dialog.setCancelable(false);
        View customize_view = getLayoutInflater().inflate(R.layout.waiting_time_waste_two, null);
        sp_hour_two = customize_view.findViewById(R.id.sp_hour_two);
        sp_minute_two = customize_view.findViewById(R.id.sp_minute_two);
        bt_ok_two = customize_view.findViewById(R.id.bt_ok_two);
        bt_cancel_waiting_two = customize_view.findViewById(R.id.bt_cancel_waiting_two);
        bt_ok_two.setOnClickListener(this);
        bt_cancel_waiting_two.setOnClickListener(this);
        sp_hour_two.setOnItemSelectedListener(this);
        sp_minute_two.setOnItemSelectedListener(this);
        rv_hour_two = customize_view.findViewById(R.id.rv_hour_two);
        rv_minute_two = customize_view.findViewById(R.id.rv_minute_two);
        customize_alert_dialog.setView(customize_view);
        alertDialog_time = customize_alert_dialog.create();
        alertDialog_time.show();
        arrayList_hour.clear();
        arrayList_minute.clear();
        arrayList_second.clear();
        for(int i=0;i<=100;i++){
            if (i<10)
                arrayList_hour.add("0"+String.valueOf(i));
            else
                arrayList_hour.add(String.valueOf(i));
        }
        for(int i=0;i<60;i++){
            if (i<10)
                arrayList_minute.add("0"+String.valueOf(i));
            else
                arrayList_minute.add(String.valueOf(i));
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(WasteTransferFormOperator.this,R.layout.spinner_custom_layout,arrayList_hour);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_hour_two.setAdapter(spinnerArrayAdapter);
        ArrayAdapter<String> spinnerArrayAdapter1 = new ArrayAdapter<String>(WasteTransferFormOperator.this,R.layout.spinner_custom_layout,arrayList_minute);
        spinnerArrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_minute_two.setAdapter(spinnerArrayAdapter1);
        hourAdapter = new HourAdapter(WasteTransferFormOperator.this,arrayList_hour);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv_hour_two.setLayoutManager(mLayoutManager);
        rv_hour_two.setItemAnimator(new DefaultItemAnimator());
        rv_hour_two.setAdapter(hourAdapter);

        minuteAdapter = new MinuteAdapter(WasteTransferFormOperator.this,arrayList_minute);
        RecyclerView.LayoutManager mLayoutManager1 = new LinearLayoutManager(getApplicationContext());
        rv_minute_two.setLayoutManager(mLayoutManager1);
        rv_minute_two.setItemAnimator(new DefaultItemAnimator());
        rv_minute_two.setAdapter(minuteAdapter);

        WaitingTimeInterfaces.waiting_hour_interface = new HourAdapter.waiting_hour() {
            @Override
            public void hour_time(String hour_text) {
                hourTwo = hour_text;
            }
        };
        WaitingTimeInterfaces.waiting_minute_interface = new MinuteAdapter.Waiting_minute() {
            @Override
            public void minute_time(String minute_text) {
                minuteTwo = minute_text;
            }
        };
    }

    private void enableBluetooth(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(WasteTransferFormOperator.this, "Message1", Toast.LENGTH_SHORT).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                if (ContextCompat.checkSelfPermission(WasteTransferFormOperator.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(WasteTransferFormOperator.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
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
        if (ContextCompat.checkSelfPermission(WasteTransferFormOperator.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(WasteTransferFormOperator.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
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
            Intent connectIntent = new Intent(WasteTransferFormOperator.this,
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
                Toast.makeText(this,"Printer Head Open",Toast.LENGTH_SHORT).show();
            } else if (printerStatus.isPaused) {
                Toast.makeText(this,"Printer is Paused",Toast.LENGTH_SHORT).show();
            } else if (printerStatus.isPaperOut) {
                Toast.makeText(this,"Printer Media Out",Toast.LENGTH_SHORT).show();
            }
        } catch (ConnectionException e) {
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
        } finally {
            if (printerDialog!=null)
                printerDialog.dismiss();
        }
    }

    public ZebraPrinter connect() {
        connection = null;
        connection = new BluetoothConnection(getMacAddress());
        try {
            if (ContextCompat.checkSelfPermission(WasteTransferFormOperator.this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(WasteTransferFormOperator.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
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
            if (connection != null) {
                connection.close();
            }
            if (printerDialog!=null)
              printerDialog.dismiss();
        } catch (ConnectionException e) {

        }
    }

    private void doConnectionTest() {
        printer = connect();
        if (printer!= null) {
            sendTestLabel();
        }else
            disconnect();
    }

    private String getMacAddress() {
        return deviceMacAddress;
    }

    private byte[] getConfigLabel() {
        byte[] configLabel = null;
        try {
            PrinterLanguage printerLanguage = printer.getPrinterControlLanguage();
            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            Bitmap bmp = Bitmap.createBitmap(1, 1, conf); // this creates a MUTABLE bitmap
            if (!et_add_driver_name.getText().toString().isEmpty())
                new_driver_name = et_add_driver_name.getText().toString();
            if (selected_container_type.equals("Select Container Type"))
                selected_container_type ="";
            String reg_no = "";
            if (new_reg_no.isEmpty())
                reg_no=registration_no;
            else
                reg_no=new_reg_no;
            SGD.SET("device.languages", "zpl", connection);
            if (ewc_code.equals("")) {
                waste_transfer_print_layout = new PrintingLayoutFormat(this).claireFormToBitmap(tv_ticket_no.getText().toString(), tv_haulier.getText().toString(), user_name, current_site_name,
                        "43.12", current_site_address, material_des+" "+ewc_code,et_unit.getText().toString().trim(),gross,tare,String.valueOf(net), current_site_name, selected_delivery_site_name, current_site_address, selected_delivery_site_address,
                        "","",et_collection_reference.getText().toString(),"",tv_on_site.getText().toString(),tv_off_site.getText().toString(),et_external_ticket.getText().toString(), permit_no,
                        tv_haulier.getText().toString(),new_driver_name,current_date + " " + current_time, reg_no
                        , tv_vehicle_type.getText().toString(), first_container_size + " " + second_container_size + " " + third_container_size,selected_container_type,signaturePadOperator.getSignatureBitmap(),
                        signaturePadDriver.getSignatureBitmap());
            }else {
                waste_transfer_print_layout = new PrintingLayoutFormat(this).wasteTransferFormToBitmap(tv_ticket_no.getText().toString(), tv_haulier.getText().toString(), user_name, tv_waste_carrier_no.getText().toString(),
                        current_site_name, store_sic_code, current_site_address, material_des+" "+ewc_code,et_unit.getText().toString().trim(),gross,tare,String.valueOf(net), current_site_name, selected_delivery_site_name,
                        current_site_address, selected_delivery_site_address,"","",et_collection_reference.getText().toString(),"",tv_on_site.getText().toString(),tv_off_site.getText().toString(),et_external_ticket.getText().toString(),
                        permit_no, tv_haulier.getText().toString(),new_driver_name, tv_waste_carrier_no.getText().toString(),
                        current_date + " " + current_time, reg_no, tv_vehicle_type.getText().toString(), first_container_size + " " + second_container_size + " " + third_container_size,selected_container_type,
                        signaturePadOperator.getSignatureBitmap(), signaturePadDriver.getSignatureBitmap());
            }

            Bitmap resize_bmp = getResizedBitmap(waste_transfer_print_layout,600,1800);
            ZPLConverter zp = new ZPLConverter();
            zp.setCompressHex(true);
            zp.setBlacknessLimitPercentage(90);
            if (printerLanguage == PrinterLanguage.ZPL) {
                configLabel = zp.convertFromWasteImage(resize_bmp).getBytes();
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
    private void getMaximumLength(){
        sortKeyTableReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if (queryDocumentSnapshot.contains("supply_waste_key")){
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
                Toast.makeText(WasteTransferFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(WasteTransferFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
                Toast.makeText(WasteTransferFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deliveryAddManagement() {
        Query query = siteManagementReference.whereEqualTo("status", "Active").whereEqualTo("address_status","Active")
                .whereEqualTo("site_type", "Delivery");
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
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WasteTransferFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        tv_current_site.setText(current_site_address);
                        tv_current_site_address.setText(current_site_address);
                        if (tv_current_site.getText().toString().isEmpty()) {
                            if (user_role_id != 4)
                                alertDialogAssignSite("You are not assigned with any site.Please assign yourself with a site.");
                        }
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
                Toast.makeText(WasteTransferFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getProjectNo(){
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
                Toast.makeText(WasteTransferFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // get delivery site id
    private void getDeliverySiteID(int site_id){
        progressDialog.show();
        Query query = siteManagementReference.whereEqualTo("id",site_id).whereEqualTo("site_type","Delivery").whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            selected_delivery_site_id= Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
                            selected_delivery_site_address = queryDocumentSnapshot.getString("address");
                            permit_no = queryDocumentSnapshot.getString("permit_no");
                            delivery_site_id_siteManagement = Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
                        }
                        getDeliverySiteName(delivery_site_id_siteManagement);
                    }
                    else
                        progressDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(WasteTransferFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDeliverySiteName(int site_id){
        Query siteManagement = siteManagementReference.whereEqualTo("id",site_id).whereEqualTo("status","Active");
        siteManagement.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            delivery_site_name = queryDocumentSnapshot.getString("site_name");
                        }
                        Log.d(TAG,"Delivery Site Name:" + delivery_site_name);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WasteTransferFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getContainerType(){
        Query query = containerTypeReference.whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    arrayList_container_type.clear();
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot :task.getResult()){
                            arrayList_container_type.add(queryDocumentSnapshot.getString("container_type"));
                        }
                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_custom_layout,arrayList_container_type);
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        sp_container_type.setAdapter(spinnerArrayAdapter);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WasteTransferFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Log.d(TAG,"max_user_id :"+max_docId_user_details);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WasteTransferFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Log.d(TAG,"max_vehicle_id :"+max_docId_vehicle_details);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WasteTransferFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateVehicleDetails() {
        Map<String, Object> updateVehicleDetailsMap = new HashMap<>();
        updateVehicleDetailsMap.put("created_at",submission_date+" "+submission_time);
        updateVehicleDetailsMap.put("external_vehicle","Yes");
        updateVehicleDetailsMap.put("haulier",tv_haulier.getText().toString());
        updateVehicleDetailsMap.put("haulier_carrier_no",tv_waste_carrier_no.getText().toString());
        updateVehicleDetailsMap.put("id",new_vehicle_id);
        updateVehicleDetailsMap.put("manufacturer","");
        updateVehicleDetailsMap.put("model","");
        updateVehicleDetailsMap.put("prefix_registration_no", 0);
        updateVehicleDetailsMap.put("registration_no",new_reg_no);
        updateVehicleDetailsMap.put("registration_no_no_space","");
        updateVehicleDetailsMap.put("status","Active");
        updateVehicleDetailsMap.put("trailer","");
        updateVehicleDetailsMap.put("updated_at",submission_date+" "+submission_time);
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
                Dialog.alertDialog(WasteTransferFormOperator.this, e.getMessage());
            }
        });
    }

    private void updateUserDetails(){
        HashMap<String,Object> update_user_map = new HashMap();
        update_user_map.put("access_module",0);
        update_user_map.put("created_at",submission_date+" "+submission_time);
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
        update_user_map.put("updated_at",submission_date+" "+submission_time);
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
                    Log.d(TAG,"Success :"+"User details updated successfully.");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WasteTransferFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    objUpdate.put("updated_at",submission_date+" "+submission_time);
                    objUpdate.put("is_updated","Yes");
                    vehicleDetailsReference.document(docId).update(objUpdate);
                }
                Log.d(TAG,"Success :"+"Vehicle user mapping updated successfully.");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WasteTransferFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
        mapTask.put("material_type","Collection");
        progressDialog.show();
        taskManagementReference.document(ticket_no).set(mapTask).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    Map<String,Object> objectMap_update = new HashMap<>();
                    objectMap_update.put("task_management_key",doc_id_length_task);
                    sortKeyTableReference.document(doc_id_task).update(objectMap_update);
                    Log.d(TAG,"Success :" +"Task Start" );
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG,"Error :" +"Unable to start task." );
            }
        });
    }

    private void latLon(){
        GPSTracker finder = new GPSTracker(WasteTransferFormOperator.this);
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
                        sp_driver_name_w.setVisibility(View.GONE);
                        tv_driver_name.setText(user_name);
                        new_driver_name = user_name;
                        selected_user_id = user_id;
                        Log.d(TAG,"RDTD :" +selected_user_id);
                        getVehicleID();
                    } else {
                        isInspectionDone = false;
                        tv_vehicle_reg_no.setVisibility(View.GONE);
                        tv_driver_name.setVisibility(View.GONE);
                        sp_reg_no.setVisibility(View.VISIBLE);
                        sp_driver_name_w.setVisibility(View.VISIBLE);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WasteTransferFormOperator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            Log.d(TAG,"VEHICLE REG ID:" + registration_id);
                        }
                    }
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
        Query query = vehicleDetailsReference.whereEqualTo("external_vehicle","No").whereEqualTo("id",vehicle_id).whereEqualTo("status","Active");
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
                    tv_waste_carrier_no.setText(waste_carrier_id);
                    containerSize(vehicle_type_id);
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

    private void getTrailerNumber(){
        Query query = trailerDetailsReference.whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                        if (queryDocumentSnapshot.exists()){
                            trailer_id_list.add(queryDocumentSnapshot.getLong("id").intValue());
                            trailer_no_list.add(queryDocumentSnapshot.getString("model_no"));
                        }
                    }
                }
                ArrayAdapter<String> spinnerArrayAdapter7 = new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_custom_layout,trailer_no_list);
                spinnerArrayAdapter7.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sp_trailer_no.setAdapter(spinnerArrayAdapter7);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.e("Error", "Unable to fetch trailer details.");
            }
        });
    }

    private void submissionTime(){
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        submission_date = simpleDateFormat.format(date);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        submission_time = time_format.format(calendar.getTime());
    }

    private void zoomImagePopup(Bitmap bitmap){
        View popupView = LayoutInflater.from(this).inflate(R.layout.zoom_image_popup, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        ImageView iv_zoom_image =popupView.findViewById(R.id.iv_zoom_image);
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
}
