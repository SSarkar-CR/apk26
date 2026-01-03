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

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class WasteTransferDelivery extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private ImageView iv_cross,iv_camera,iv_photo_one,iv_close_one;
    private Bitmap waste_transfer_print_layout,getDrawable1;
    private Button bt_ok,bt_cancel_waiting,bt_ok_two,bt_cancel_waiting_two,bt_select_on_site,bt_select_off_site,bt_discard, bt_driver_sign, bt_operator_sign, bt_print_ticket_yes, bt_print_ticket_no, bt_submit,bt_submit_new,
            bt_exit, bt_size_16y, bt_size_20y, bt_size_30y, bt_size_40y, bt_size_6y, bt_size_8y, bt_size_12y, bt_size_15y, bt_size_18T,bt_weight,bt_unit,bt_close_ticket_yes,bt_close_ticket_no;
    private LinearLayout ll_assign_site,ll_operator_sign, ll_driver_sign, ll_container1,ll_container2,ll_container3,ll_current_site,
            ll_weight_unit,ll_unit,ll_gross,ll_tare,ll_net,ll_images,ll_container_type;
    private TextView tv_on_site,tv_off_site, tv_ticket_no, tv_operator, tv_date, tv_time, tv_vehicle_type, tv_haulier, tv_waste_carrier_no,
            tv_clear_operator, tv_clear_driver,tv_header,tv_caution_two,tv_net_weight,tv_material_value_caution;
    private EditText et_comment,et_gross_weight,et_tare,et_unit,et_delivery_reference,et_external_ticket;
    private SignaturePad signaturePadDriver, signaturePadOperator;
    private CustomSearchableSpinner sp_hour,sp_minute,sp_hour_two,sp_minute_two,sp_current_site,sp_materials, sp_delivery_site,sp_delivery_reference,sp_delivery_area,sp_container_type,sp_reg_no,sp_driver_name_w,sp_trailer_no;
    private String user_name="", first_container_size = "", second_container_size = "", third_container_size = "",ticket_no = "", print_ticket = "",new_driver_name = "",
            driver_sign = "", operator_sign = "", current_date,current_site_address = "",current_site_name = "",selected_current_site_name="",
            material_des = "", registration_no = "", selected_delivery_site_name="",selected_delivery_site_address="",
            hour="",minute="",hourTwo="",minuteTwo="", deviceMacAddress="",current_time="",permit_no="", ewc_code="",store_sic_code="",project_no="",submission_date="",submission_time="",
            latitude="",longitude="",delivery_site_name="",site_name="",vehicle_type_id="",waste_carrier_id="",
            haulier_company_id="",weight_unit = "", gross = "", tare = "",image1="",image2="",material_value="",
            selected_delivery_reference_name="",selected_delivery_area_name="",selected_container_type="";
    double net = 0.0;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference supplyWasteFormsReference, assignMaterialReference, materialReference, vehicleDetailsReference,trailerDetailsReference,
            userDetailsReference,siteManagementReference, deliveryReferenceDetails,deliveryLocationReference,containerTypeReference;
    private int selected_position=0, user_id = 0, user_role_id = 0, site_id = 0,material_id = 0,fetched_material_id=0, registration_id = 0,
            selected_delivery_site_id=0,delivery_site_id_addressManagement=0, selected_user_id=0,doc_id_length=0,
            address_management_id=0,selected_delivery_reference_id =0,selected_delivery_area_id=0,trailer_id=0;
    private boolean isSigned = false, isSignedOperator = false, validation_done = false, hasImage = false;
    private ProgressDialog progressDialog,printerDialog;
    private PopupWindow popupWindow;
    private ArrayList<String> material_des_list= new ArrayList<>();
    private ArrayList<Integer> selected_material_id_list = new ArrayList<>();
    private ArrayList<String>siteName = new ArrayList<>();
    private ArrayList<String> store_ewc_code_list = new ArrayList<>();
    private ArrayList<String> ewc_code_list = new ArrayList<>();
    private ArrayList<String> store_material_value_list = new ArrayList<>();
    private ArrayList<String> material_value_list = new ArrayList<>();
    private ArrayList<String> registration_no_list = new ArrayList<>();
    private ArrayList<String>external_vehicle_list = new ArrayList<>();
    private ArrayList<String> haulierList= new ArrayList<>();
    private ArrayList<String> haulierCarrierNoList = new ArrayList<>();
    private ArrayList<String> vehicle_type_list= new ArrayList<>();
    private ArrayList<Integer> registration_no_id_list= new ArrayList<>();
    private ArrayList<Integer> arrayList_user_id = new ArrayList<>();
    private ArrayList<String>allUserNames = new ArrayList<>();
    private ArrayList<String> arrayList_user_name = new ArrayList<>();
    private ArrayList<Integer> allUserIds= new ArrayList<>();
    private ArrayList<Integer>siteId = new ArrayList<>();
    private ArrayList<Integer> assignMaterialDocumentId= new ArrayList<>();
    private ArrayList<Integer> material_id_list= new ArrayList<>();
    private ArrayList<Integer> store_material_doc_list = new ArrayList<>();
    private ArrayList<String> store_material_des_list = new ArrayList<>();
    private ArrayList<String> arrayList_hour = new ArrayList<>();
    private ArrayList<String> arrayList_minute = new ArrayList<>();
    private ArrayList<String> arrayList_second = new ArrayList<>();
    private ArrayList<Integer> arrayList_site_id = new ArrayList<>();
    private ArrayList<String> arrayList_assign_site_name;
    private ArrayList<Integer> arrayList_assign_site_id;
    private ArrayList<Integer> arrayList_delivery_reference_id = new ArrayList<>();
    private ArrayList<String> arrayList_delivery_reference_name = new ArrayList<>();
    private ArrayList<Integer> allDeliveryReferenceIds= new ArrayList<>();
    private ArrayList<String>allDeliveryReferenceNames = new ArrayList<>();
    private ArrayList<Integer> arrayList_delivery_area_id = new ArrayList<>();
    private ArrayList<String> arrayList_delivery_area_name = new ArrayList<>();
    private ArrayList<Integer> allDeliveryAreaIds= new ArrayList<>();
    private ArrayList<String>allDeliveryAreaNames = new ArrayList<>();
    private ArrayList<String> arrayList_container_type = new ArrayList<>();
    private ArrayList<String> trailer_no_list = new ArrayList<>();
    private ArrayList<Integer> trailer_id_list = new ArrayList<>();
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
        setContentView(R.layout.waste_transfer_delivery);
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
        siteManagementReference = db.collection("CR_site_management");
        deliveryReferenceDetails = db.collection("CR_delivery_reference_details");
        deliveryLocationReference = db.collection("CR_delivery_location_details");
        containerTypeReference = db.collection("CR_container_types");

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
        iv_close_one = findViewById(R.id.iv_close_one);
        bt_select_on_site = findViewById(R.id.bt_select_on_site);
        bt_select_off_site = findViewById(R.id.bt_select_off_site);
        bt_discard = findViewById(R.id.bt_discard);
        bt_driver_sign = findViewById(R.id.bt_driver_sign);
        bt_operator_sign = findViewById(R.id.bt_operator_sign);
        bt_print_ticket_yes = findViewById(R.id.bt_print_ticket_yes);
        bt_print_ticket_no = findViewById(R.id.bt_print_ticket_no);
        bt_close_ticket_yes = findViewById(R.id.bt_close_ticket_yes);
        setSelectedButtonBackground(bt_close_ticket_yes);
        bt_close_ticket_no = findViewById(R.id.bt_close_ticket_no);
        bt_submit = findViewById(R.id.bt_submit);
        ll_assign_site = findViewById(R.id.ll_assign_site);
        ll_driver_sign = findViewById(R.id.ll_driver_sign);
        ll_operator_sign = findViewById(R.id.ll_operator_sign);
        ll_container1 = findViewById(R.id.ll_container1);
        ll_container2 = findViewById(R.id.ll_container2);
        ll_container3 = findViewById(R.id.ll_container3);
        ll_container_type = findViewById(R.id.ll_container_type);
        ll_current_site = findViewById(R.id.ll_current_site);
//        if (user_role_id == 4)
            ll_current_site.setVisibility(View.GONE);
        ll_images = findViewById(R.id.ll_images);
        ll_weight_unit = findViewById(R.id.ll_weight_unit);
        ll_unit = findViewById(R.id.ll_unit);
        ll_gross = findViewById(R.id.ll_gross);
        ll_tare = findViewById(R.id.ll_tare);
        ll_net = findViewById(R.id.ll_net);
        tv_header = findViewById(R.id.tv_header);
        tv_ticket_no = findViewById(R.id.tv_ticket_no);
        tv_operator = findViewById(R.id.tv_operator);
        tv_operator.setText(user_name);
        tv_date = findViewById(R.id.tv_date);
        tv_time = findViewById(R.id.tv_time);
        tv_vehicle_type = findViewById(R.id.tv_vehicle_type);
        tv_haulier = findViewById(R.id.tv_haulier);
        tv_waste_carrier_no = findViewById(R.id.tv_waste_carrier_no);
        tv_on_site = findViewById(R.id.tv_on_site);
        tv_off_site = findViewById(R.id.tv_off_site);
        tv_caution_two = findViewById(R.id.tv_caution_two);
        tv_material_value_caution = findViewById(R.id.tv_material_value_caution);
        tv_net_weight = findViewById(R.id.tv_net_weight);
        et_delivery_reference = findViewById(R.id.et_delivery_reference);
        et_external_ticket = findViewById(R.id.et_external_ticket);
        et_comment = findViewById(R.id.et_comment);
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start,
                                       int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.toString(source.charAt(i)).matches("[a-zA-Z0-9 ]+")) {
//                        et_comment.setError("Special characters are not allowed.");
                        return "";
                    }
                }
                return null;
            }
        };
        et_delivery_reference.setFilters(new InputFilter[] { filter });
        et_external_ticket.setFilters(new InputFilter[] { filter });
        et_comment.setFilters(new InputFilter[] { filter });
        et_gross_weight = findViewById(R.id.et_gross_weight);
        et_tare = findViewById(R.id.et_tare);
        et_unit = findViewById(R.id.et_unit);
        sp_current_site = findViewById(R.id.sp_current_site);
        sp_materials = findViewById(R.id.sp_materials);
        sp_delivery_site = findViewById(R.id.sp_delivery_site);
        sp_delivery_reference = findViewById(R.id.sp_delivery_reference);
        sp_delivery_area = findViewById(R.id.sp_delivery_area);
        sp_reg_no = findViewById(R.id.sp_reg_no);
        sp_driver_name_w = findViewById(R.id.sp_driver_name_w);
        sp_container_type  = findViewById(R.id.sp_container_type);
        sp_trailer_no = findViewById(R.id.sp_trailer_no);
        tv_clear_driver = findViewById(R.id.tv_clear_driver);
        tv_clear_operator = findViewById(R.id.tv_clear_operator);
        progressDialog = Dialog.showProgressDialog(this);
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
        initializeOnClick();
        ll_assign_site.setVisibility(View.VISIBLE);
        getSiteAddress();
        ticket_no = getIntent().getStringExtra("ticket_no");
        Log.d("ticket_no:" ,ticket_no);
        tv_ticket_no.setText(ticket_no.replace("C","D"));
        if (ticket_no != null) {
            if (!ticket_no.equals("")) {
                fetchTicketDetails(ticket_no);
            }
        }
        if (!AppData.internetOnline(this))
            progressDialog.dismiss();
        setSignDriver();
        setSignOperator();
    }

//

    private void initializeOnClick() {
        iv_cross.setOnClickListener(this);
        iv_camera.setOnClickListener(this);
        iv_close_one.setOnClickListener(this);
        iv_photo_one.setOnClickListener(this);
        bt_select_on_site.setOnClickListener(this);
        bt_select_off_site.setOnClickListener(this);
        bt_discard.setOnClickListener(this);
        bt_driver_sign.setOnClickListener(this);
        bt_operator_sign.setOnClickListener(this);
        bt_close_ticket_yes.setOnClickListener(this);
        bt_close_ticket_no.setOnClickListener(this);
        tv_clear_driver.setOnClickListener(this);
        tv_clear_operator.setOnClickListener(this);
        bt_print_ticket_yes.setOnClickListener(this);
        bt_print_ticket_no.setOnClickListener(this);
        sp_current_site.setOnItemSelectedListener(this);
        sp_materials.setOnItemSelectedListener(this);
        sp_delivery_site.setOnItemSelectedListener(this);
        sp_delivery_reference.setOnItemSelectedListener(this);
        sp_delivery_area.setOnItemSelectedListener(this);
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
        bt_submit.setOnClickListener(this);
        bt_weight.setOnClickListener(this);
        bt_unit.setOnClickListener(this);
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
                if (checkValidation()) {
                    submitDeliveryForm();
                    if (!AppData.internetOnline(WasteTransferDelivery.this)) {
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
            case R.id.bt_close_ticket_yes:
                setSelectedButtonBackground(bt_close_ticket_yes);
                setDefaultButtonBackground(bt_close_ticket_no);
                break;
            case R.id.bt_close_ticket_no:
                setSelectedButtonBackground(bt_close_ticket_no);
                setDefaultButtonBackground(bt_close_ticket_yes);
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
                weightVisible();
                break;
            case R.id.bt_unit:
                unitVisible();
                break;
            case R.id.bt_exit:
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
            getContainerType();
            visibleWeightUnit();
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
                    Toast.makeText(WasteTransferDelivery.this, "Message", Toast.LENGTH_SHORT).show();
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

        if (site_id==0)
            Dialog.alertDialog(this, "Please select any assign site");
        else if (material_des.equals("select Material") || material_des.equals(""))
            Dialog.alertDialog(this, "Please select material.");
        else if (selected_delivery_site_id==0)
            Dialog.alertDialog(this,"Please select delivery site");
        else if (selected_delivery_site_name.equals("Select delivery site"))
            Dialog.alertDialog(this, "Please select delivery site");
        else if (registration_no.equals(""))
            Dialog.alertDialog(this, "Please select registration number");
        else if (new_driver_name.equals(""))
            Dialog.alertDialog(this,"Please select driver name");
        else if (trailer_id==0)
            Dialog.alertDialog(this, "Please select trailer number.");
        else if (tv_on_site.getText().toString().trim().isEmpty())
            Dialog.alertDialog(this, "Please select on site time.");
        else if (tv_off_site.getText().toString().trim().isEmpty())
            Dialog.alertDialog(this, "Please select off site time.");
        else if (material_value.equals("High") && image1.equals(""))
            Dialog.alertDialog(this,"Please take a picture of the material being transferred.");
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

    private void toConfirmation(String message) {
        progressDialog.dismiss();
        final View popupView = getLayoutInflater().inflate(R.layout.change_pass_popup_layout, null, true);
         boolean isActivityInForeground = WasteTransferDelivery.this.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED);
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
                Intent toDashboard = new Intent(WasteTransferDelivery.this, MainActivity.class);
                startActivity(toDashboard);
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
                    Log.d("Material ids:" , String.valueOf(material_id_list));
                    material();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WasteTransferDelivery.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Log.d("RDTTH :" , String.valueOf(selected_material_id_list));
                    int position = selected_material_id_list.indexOf(fetched_material_id);
                    sp_materials.setSelection(position);
                    getProjectNo();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(WasteTransferDelivery.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

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
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_custom_layout, registration_no_list);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                    sp_reg_no.setAdapter(spinnerArrayAdapter);
                    Log.d("Trail :" ,registration_no_id_list+"  "+registration_id);
                    int position = registration_no_id_list.indexOf(registration_id);
                    sp_reg_no.setSelection(position);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(WasteTransferDelivery.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Log.d("User Ids :" , String.valueOf(arrayList_user_id));
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
                Toast.makeText(WasteTransferDelivery.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Log.d("UTRY :" ,selected_user_id+" "+arrayList_user_id);
                        int position = arrayList_user_id.indexOf(selected_user_id);
                        sp_driver_name_w.setSelection(position);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(WasteTransferDelivery.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_current_site:
                selected_current_site_name = arrayList_assign_site_name.get(position).trim( );
                site_id = arrayList_assign_site_id.get(position);
                Log.d("Selected Site Collection:" , site_id+" "+selected_current_site_name);
                progressDialog.show();
                getSicCode(site_id);
                if (!AppData.internetOnline(this)) {
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
                    tv_header.setText("CL:aire Ticket \n- Delivery");
                    tv_caution_two.setText(R.string.sign_caution_three);
                }else {
                    tv_header.setText("Waste Transfer Ticket \n- Delivery");
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
                tv_waste_carrier_no.setText("");
                Log.d("Reg no: " , registration_no);
                Log.d("Reg id :" , String.valueOf(registration_id));
                registration_id = registration_no_id_list.get(position);
                vehicle_type_id = vehicle_type_list.get(position);
                haulier_company_id = haulierList.get(position);
                waste_carrier_id = haulierCarrierNoList.get(position);
                tv_vehicle_type.setText(vehicle_type_id);
                tv_waste_carrier_no.setText(waste_carrier_id);
                tv_haulier.setText(haulier_company_id);
                Log.d("VT :" ,vehicle_type_id);
                containerSize(vehicle_type_id);
                getUserIdList(registration_id);
                sp_driver_name_w.setVisibility(View.VISIBLE);
                break;
            case R.id.sp_driver_name_w:
                new_driver_name = sp_driver_name_w.getSelectedItem().toString().trim();
                selected_user_id= arrayList_user_id.get(position);
                break;
            case R.id.sp_container_type:
                selected_container_type = arrayList_container_type.get(position).trim();
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

    private void getSiteAddress(){
        arrayList_assign_site_name = new ArrayList<>();
        arrayList_assign_site_id = new ArrayList<>();
        Query query = siteManagementReference.whereEqualTo("site_type","Collection").whereEqualTo("address_status","Active")
                .whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        arrayList_assign_site_name.add(queryDocumentSnapshot.getString("site_name"));
                        arrayList_assign_site_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                    }
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_custom_layout,arrayList_assign_site_name);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                    sp_current_site.setAdapter(spinnerArrayAdapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(WasteTransferDelivery.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                int position = trailer_id_list.indexOf(trailer_id);
                sp_trailer_no.setSelection(position);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.e("Error", "Unable to fetch trailer details.");
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
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(WasteTransferDelivery.this,R.layout.spinner_custom_layout,arrayList_hour);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_hour.setAdapter(spinnerArrayAdapter);
        ArrayAdapter<String> spinnerArrayAdapter1 = new ArrayAdapter<String>(WasteTransferDelivery.this,R.layout.spinner_custom_layout,arrayList_minute);
        spinnerArrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_minute.setAdapter(spinnerArrayAdapter1);
        hourAdapter = new HourAdapter(WasteTransferDelivery.this,arrayList_hour);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv_hour.setLayoutManager(mLayoutManager);
        rv_hour.setItemAnimator(new DefaultItemAnimator());
        rv_hour.setAdapter(hourAdapter);

        minuteAdapter = new MinuteAdapter(WasteTransferDelivery.this,arrayList_minute);
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
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(WasteTransferDelivery.this,R.layout.spinner_custom_layout,arrayList_hour);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_hour_two.setAdapter(spinnerArrayAdapter);
        ArrayAdapter<String> spinnerArrayAdapter1 = new ArrayAdapter<String>(WasteTransferDelivery.this,R.layout.spinner_custom_layout,arrayList_minute);
        spinnerArrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_minute_two.setAdapter(spinnerArrayAdapter1);
        hourAdapter = new HourAdapter(WasteTransferDelivery.this,arrayList_hour);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv_hour_two.setLayoutManager(mLayoutManager);
        rv_hour_two.setItemAnimator(new DefaultItemAnimator());
        rv_hour_two.setAdapter(hourAdapter);

        minuteAdapter = new MinuteAdapter(WasteTransferDelivery.this,arrayList_minute);
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
            Toast.makeText(WasteTransferDelivery.this, "Message1", Toast.LENGTH_SHORT).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                if (ContextCompat.checkSelfPermission(WasteTransferDelivery.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(WasteTransferDelivery.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
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
        if (ContextCompat.checkSelfPermission(WasteTransferDelivery.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(WasteTransferDelivery.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
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
            Intent connectIntent = new Intent(WasteTransferDelivery.this,
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
            if (ContextCompat.checkSelfPermission(WasteTransferDelivery.this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(WasteTransferDelivery.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
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
        if (printer!= null)
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
            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            if (selected_container_type.equals("Select Container Type"))
                selected_container_type ="";
            if (selected_delivery_area_id==0)
                selected_delivery_area_name="";
            if (selected_delivery_reference_id==0)
                selected_delivery_reference_name = "";

            SGD.SET("device.languages", "zpl", connection);

            if (ewc_code.equals("")) {
                waste_transfer_print_layout = new PrintingLayoutFormat(this).claireFormToBitmap(tv_ticket_no.getText().toString(), tv_haulier.getText().toString(), user_name, current_site_name,
                        "43.12", current_site_address, material_des+" "+ewc_code,et_unit.getText().toString().trim(),gross,tare,String.valueOf(net), current_site_name, selected_delivery_site_name, current_site_address, selected_delivery_site_address,
                        selected_delivery_area_name,selected_delivery_reference_name,"",et_delivery_reference.getText().toString(),tv_on_site.getText().toString(),tv_off_site.getText().toString(),et_external_ticket.getText().toString(), permit_no,
                        tv_haulier.getText().toString(),new_driver_name,current_date + " " + current_time, registration_no
                        , tv_vehicle_type.getText().toString(), first_container_size + " " + second_container_size + " " + third_container_size,selected_container_type,signaturePadOperator.getSignatureBitmap(),
                        signaturePadDriver.getSignatureBitmap());
            }else {
                waste_transfer_print_layout = new PrintingLayoutFormat(this).wasteTransferFormToBitmap(tv_ticket_no.getText().toString(), tv_haulier.getText().toString(), user_name, tv_waste_carrier_no.getText().toString(),
                        current_site_name, store_sic_code, current_site_address, material_des+" "+ewc_code,et_unit.getText().toString().trim(),gross,tare,String.valueOf(net), current_site_name, selected_delivery_site_name,
                        current_site_address, selected_delivery_site_address,selected_delivery_area_name,selected_delivery_reference_name,"",et_delivery_reference.getText().toString(),tv_on_site.getText().toString(),
                        tv_off_site.getText().toString(),et_external_ticket.getText().toString(), permit_no, tv_haulier.getText().toString(),new_driver_name, tv_waste_carrier_no.getText().toString(),
                        current_date + " " + current_time, registration_no, tv_vehicle_type.getText().toString(), first_container_size + " " + second_container_size + " " + third_container_size,selected_container_type,
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
                Toast.makeText(WasteTransferDelivery.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Log.d("Site Management Site Ids :" ,siteId+"  "+siteName);
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
                Toast.makeText(WasteTransferDelivery.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(WasteTransferDelivery.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(WasteTransferDelivery.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // get delivery site id
    private void getDeliverySiteID(int site_id){
        progressDialog.show();
        Query query = siteManagementReference.whereEqualTo("id",site_id).whereEqualTo("site_type","Delivery")
                .whereEqualTo("status","Active");
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
                            delivery_site_name = queryDocumentSnapshot.getString("site_name");
                        }
                        Log.d("Delivery Site Name:" ,delivery_site_name);
                    }
                    else
                        progressDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(WasteTransferDelivery.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(WasteTransferDelivery.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        return site_name;
    }

    private void getVehicleNumber(int vehicle_id){
        Query query = vehicleDetailsReference.whereEqualTo("id",vehicle_id).whereEqualTo("status","Active");
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
    private void fetchTicketDetails(String ticket_no){
        progressDialog.show();
        Query query = supplyWasteFormsReference.whereEqualTo("ticket_no",ticket_no);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    current_date = AppData.date();
                    current_time = AppData.Time();
                    tv_date.setText(current_date);
                    tv_time.setText(current_time);
                    for (QueryDocumentSnapshot documentSnapshot: Objects.requireNonNull(task.getResult())) {
                        store_sic_code = documentSnapshot.getString("sic_code");
                        trailer_id = documentSnapshot.getLong("trailer_id").intValue();
                        ewc_code = documentSnapshot.getString("ewc_code");
                        if (ewc_code.equals("")){
                            tv_header.setText("CL:aire Form");
                            tv_caution_two.setText(R.string.sign_caution_three);
                        }else {
                            tv_header.setText("Waste Transfer Form");
                            tv_caution_two.setText(R.string.sign_caution_two);
                        }
                        new_driver_name = documentSnapshot.getString("new_driver_name");
                        selected_user_id = documentSnapshot.getLong("new_driver_id").intValue();
                        String unit = documentSnapshot.getString("unit");
                        vehicle_type_id = documentSnapshot.getString("vehicle_type");
                        if (vehicle_type_id != null) {
                            if (vehicle_type_id.equals("N1") || vehicle_type_id.equals("N2") || vehicle_type_id.equals("N3") || vehicle_type_id.equals("Artic Unit")){
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
                                }
                            }else {
                                first_container_size = documentSnapshot.getString("container_size_one");
                                second_container_size = documentSnapshot.getString("container_size_two");
                                third_container_size = documentSnapshot.getString("container_size_three");
                            }
                        }
                        selected_container_type = documentSnapshot.getString("container_type");
                        registration_id = documentSnapshot.getLong("vehicle_id").intValue();
                        getVehicleNumber(registration_id);
                        Log.d("RDTYYJ :" ,trailer_id+"  "+registration_id+"  "+selected_user_id);
                        et_comment.setText(documentSnapshot.getString("add_comment"));
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
                        Log.d("FDGF :" ,address_management_id+" "+fetched_material_id+" "+delivery_site_id_addressManagement);

                        assignMaterialsDeliverySite(fetched_material_id);
                    }
                    fetchVehicleRegistrationNumber();
                    getTrailerNumber();
                    Log.d("Container Size :" ,first_container_size +" "+second_container_size+" "+third_container_size);
                    selectedContainerSize(first_container_size, second_container_size, third_container_size);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(WasteTransferDelivery.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unitVisible() {
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
    }

    private void weightVisible(){
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
    }

    private void selectedContainerSize(String first_container_size,String second_container_size,String third_container_size){
        if (first_container_size.equals("16y"))
            setSelectContainerBackground(bt_size_16y);
        else if (first_container_size.equals("20y"))
            setSelectContainerBackground(bt_size_20y);
        else if (first_container_size.equals("30y"))
            setSelectContainerBackground(bt_size_30y);
        else if (first_container_size.equals("40y"))
            setSelectContainerBackground(bt_size_40y);

        if (second_container_size.equals("6y"))
            setSelectContainerBackground(bt_size_6y);
        else if (second_container_size.equals("8y"))
            setSelectContainerBackground(bt_size_8y);
        else if (second_container_size.equals("12y"))
            setSelectContainerBackground(bt_size_12y);
        else if (second_container_size.equals("15y"))
            setSelectContainerBackground(bt_size_15y);

        if (third_container_size.equals("18T")){
            setSelectContainerBackground(bt_size_18T);
        }
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
                    Log.d("Show Collection Site Id:" ,site_id+" "+position+" "+arrayList_assign_site_id);
                    sp_current_site.setSelection(position);
                    current_site_name = getSiteName(site_id);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WasteTransferDelivery.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Log.d("Show Delivery Site Id:" ,selected_delivery_site_id+" "+position+" "+siteId);
                    sp_delivery_site.setSelection(position);
                    delivery_site_name = getSiteName(selected_delivery_site_id);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WasteTransferDelivery.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDeliveryReferenceIds(int site_id){
        Query query =  siteManagementReference.whereEqualTo("id",site_id).whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    arrayList_delivery_reference_id.clear();
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
                Toast.makeText(WasteTransferDelivery.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(WasteTransferDelivery.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDeliveryAreaIds(int site_id){
        Query query =  siteManagementReference.whereEqualTo("id",site_id).whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    arrayList_delivery_area_id.clear();
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
                Toast.makeText(WasteTransferDelivery.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(WasteTransferDelivery.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                arrayList_container_type.add(0,"Select Container Type");
                int position = arrayList_container_type.indexOf(selected_container_type);
                Log.d("Show Container Type:" , selected_container_type + " " + position + " " + arrayList_container_type);
                sp_container_type.setSelection(position);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WasteTransferDelivery.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitDeliveryForm() {
        progressDialog.show();
        latLon();
        submissionTime();
        if (image1 == null)
            image1 = "";
        if (image2 == null)
            image2 = "";

        Map<String, Object> mapWasteTransferDelivery = new HashMap<>();
        mapWasteTransferDelivery.put("user_id", user_id);
        mapWasteTransferDelivery.put("user_role_id", user_role_id);
        mapWasteTransferDelivery.put("operator", tv_operator.getText());
        mapWasteTransferDelivery.put("ticket_no", tv_ticket_no.getText());
        mapWasteTransferDelivery.put("id",tv_ticket_no.getText());
        mapWasteTransferDelivery.put("collection_ticket_no",ticket_no);
        mapWasteTransferDelivery.put("ticket_type", "Delivery");
        if (vehicle_type_id.equals("N1") || vehicle_type_id.equals("N2") || vehicle_type_id.equals("N3") || vehicle_type_id.equals("Artic Unit")) {
            mapWasteTransferDelivery.put("gross_weight", gross);
            mapWasteTransferDelivery.put("tare", tare);
            mapWasteTransferDelivery.put("net", String.valueOf(net));
            mapWasteTransferDelivery.put("unit", et_unit.getText().toString().trim());
            mapWasteTransferDelivery.put("container_size_one", "");
            mapWasteTransferDelivery.put("container_size_two", "");
            mapWasteTransferDelivery.put("container_size_three", "");
        } else {
            if (selected_position != 0) {
                if (selected_position <= 4) {
                    mapWasteTransferDelivery.put("container_size_one", first_container_size);
                    mapWasteTransferDelivery.put("container_size_two", "");
                } else {
                    mapWasteTransferDelivery.put("container_size_one", "");
                    mapWasteTransferDelivery.put("container_size_two", second_container_size);
                }
            } else {
                mapWasteTransferDelivery.put("container_size_one", first_container_size);
                mapWasteTransferDelivery.put("container_size_two", second_container_size);
            }
            mapWasteTransferDelivery.put("container_size_three", third_container_size);
            mapWasteTransferDelivery.put("gross_weight", "");
            mapWasteTransferDelivery.put("tare", "");
            mapWasteTransferDelivery.put("net", "");
            mapWasteTransferDelivery.put("unit", "");
        }
        mapWasteTransferDelivery.put("current_site_id", site_id);
        mapWasteTransferDelivery.put("current_site_id_new", 0);
        mapWasteTransferDelivery.put("current_site_address", current_site_address);
        mapWasteTransferDelivery.put("material_id", material_id);
        mapWasteTransferDelivery.put("material_id_new", 0);
        mapWasteTransferDelivery.put("delivery_site_id", selected_delivery_site_id);
        mapWasteTransferDelivery.put("delivery_site_id_new", 0);
        mapWasteTransferDelivery.put("vehicle_id", registration_id);
        mapWasteTransferDelivery.put("new_driver_id", selected_user_id);
        mapWasteTransferDelivery.put("new_driver_name",new_driver_name);
        mapWasteTransferDelivery.put("status","Active");
        mapWasteTransferDelivery.put("sort_key",0);
        doc_id_length = doc_id_length+1;
        mapWasteTransferDelivery.put("sort_key",doc_id_length);
        mapWasteTransferDelivery.put("delivery_reference", et_delivery_reference.getText().toString());
        mapWasteTransferDelivery.put("site_reference",selected_delivery_reference_id);
        mapWasteTransferDelivery.put("site_location",selected_delivery_area_id);
        mapWasteTransferDelivery.put("external_ticket_no", et_external_ticket.getText().toString());
        if (selected_container_type.equals("Select Container Type"))
            mapWasteTransferDelivery.put("container_type", "");
        else
            mapWasteTransferDelivery.put("container_type", selected_container_type);
        mapWasteTransferDelivery.put("container_type_new", "");
        mapWasteTransferDelivery.put("ewc_code", ewc_code);
        if (ewc_code.equals("")) {
            mapWasteTransferDelivery.put("ewc_available", "No");
            mapWasteTransferDelivery.put("sic_code", "43.12");
        } else {
            mapWasteTransferDelivery.put("ewc_available", "Yes");
            mapWasteTransferDelivery.put("sic_code", store_sic_code);
        }
        mapWasteTransferDelivery.put("vehicle_type", tv_vehicle_type.getText().toString());
        mapWasteTransferDelivery.put("vehicle_type_new","");
        mapWasteTransferDelivery.put("haulier_id", tv_haulier.getText().toString());
        mapWasteTransferDelivery.put("haulier_id_new","");
        mapWasteTransferDelivery.put("waste_carrier_no",tv_waste_carrier_no.getText().toString());
        mapWasteTransferDelivery.put("waste_carrier_no_new","");
        mapWasteTransferDelivery.put("permit_no",permit_no);
        mapWasteTransferDelivery.put("note_type","Delivery");
        mapWasteTransferDelivery.put("trailer_id", trailer_id);
        mapWasteTransferDelivery.put("on_site_time",tv_on_site.getText().toString().trim());
        mapWasteTransferDelivery.put("off_site_time",tv_off_site.getText().toString().trim());
        mapWasteTransferDelivery.put("project_no", project_no);
        mapWasteTransferDelivery.put("submission_date", submission_date);
        mapWasteTransferDelivery.put("submission_time", submission_time);
        mapWasteTransferDelivery.put("job_id",0);
        mapWasteTransferDelivery.put("task_order_id",0);
        if (driver_sign.equals(""))
            driver_sign = "No";
        mapWasteTransferDelivery.put("z_driver_sign", driver_sign);
        mapWasteTransferDelivery.put("z_operator_sign", operator_sign);
        mapWasteTransferDelivery.put("z_image_one", image1);
        mapWasteTransferDelivery.put("z_image_two", image2);
        mapWasteTransferDelivery.put("print_ticket", print_ticket);
        mapWasteTransferDelivery.put("add_comment", et_comment.getText().toString().trim());
        mapWasteTransferDelivery.put("permit_no", permit_no);
        mapWasteTransferDelivery.put("time_second_format", AppData.getTimeSecond());
        mapWasteTransferDelivery.put("ticket_status", "Close");
        mapWasteTransferDelivery.put("ticket_edited","No");
        mapWasteTransferDelivery.put("form_edited", "Yes");
        mapWasteTransferDelivery.put("backend_edited","No");
        mapWasteTransferDelivery.put("mail_send","No");
        mapWasteTransferDelivery.put("pdf_send","No");
        mapWasteTransferDelivery.put("latitude",latitude);
        mapWasteTransferDelivery.put("longitude",longitude);
        mapWasteTransferDelivery.put("is_updated", "Yes");
        supplyWasteFormsReference.document(tv_ticket_no.getText().toString()).set(mapWasteTransferDelivery).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    toConfirmation("Form submitted successfully.");
                    Map<String, Object> updateTicketStatus = new HashMap<>();
                    updateTicketStatus.put("ticket_status", "Completed");
                    supplyWasteFormsReference.document(ticket_no).update(updateTicketStatus);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.e("Error :", e.getMessage());
            }
        });
    }

    private void latLon(){
        GPSTracker finder = new GPSTracker(WasteTransferDelivery.this);
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
