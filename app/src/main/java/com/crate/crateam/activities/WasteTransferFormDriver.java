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
import android.content.SharedPreferences;
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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crate.crateam.adapters.HourAdapter;
import com.crate.crateam.adapters.MinuteAdapter;
import com.crate.crateam.interfaces.StartTask;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.CollectionDeliveryPoint;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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

import com.crate.crateam.R;

public class  WasteTransferFormDriver extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemSelectedListener {

    private ImageView iv_cross,iv_camera,iv_photo_one,iv_photo_two,iv_close_one,iv_close_two;
    private Button bt_ok,bt_cancel_waiting,bt_ok_two,bt_cancel_waiting_two,bt_discard,bt_driver_sign,bt_operator_sign,bt_third_party_yes,bt_third_party_no,bt_print_ticket_yes,bt_print_ticket_no,
            bt_submit,bt_edit_form,bt_size_16y,bt_size_20y,bt_size_30y,bt_size_40y,bt_size_6y,bt_size_8y,bt_size_12y,bt_size_15y, bt_size_18T,bt_weight,bt_unit,bt_select_on_site,bt_select_off_site;
    private LinearLayout ll_operator_signature,ll_operator_sign,ll_driver_sign,ll_images,ll_third_party_sign,ll_container1,ll_container2,ll_container3,
            ll_weight_unit,ll_unit,ll_gross,ll_tare,ll_net,ll_container_type;
    private TextView tv_on_site,tv_off_site,tv_clear_operator,tv_clear_driver,tv_clear_third_party,tv_waste_carrier_no,tv_date,tv_time,tv_header,
            tv_ticket_no,tv_registration_no,tv_waste_transfer_declaration,tv_sic_code,tv_operator,tv_net_weight,tv_material_value_caution;
    private CustomSearchableSpinner sp_current_site,sp_materials,sp_delivery_site,sp_container_type,sp_hour,sp_minute,sp_hour_two,sp_minute_two;
    private SignaturePad signaturePadDriver,signaturePadOperator,signaturePadThirdParty;
    private String user_name,image1,image2,waste_coming_form ="",driver_sign="",operator_sign="",third_party_signature="",store_collection_site_address="",store_collection_site_name="",
            store_delivery_point_address="",store_material_description="",store_note_type = "",store_material_type="",store_sic_code="",
            store_permit_no_collection="",store_permit_no_delivery="",first_container_size="",second_container_size="",third_container_size="",
            print_ticket = "",vehicle_registration_number = "",selected_material_name="",selected_collection_site_name ="",selected_delivery_site_name="",selected_collection_site_address="",
            selected_delivery_site_address="",ticket_no="",store_ewc_code="", date ="",time ="",comment = "",permit_no="",deviceMacAddress="",haulier_company="",doc_id="",hour="",minute="",
            hourTwo="",minuteTwo="",store_project_no="",task_status="",waste_carrier_no= "",vehicle_type_id="",weight_unit = "", gross = "", tare = "",form_edited="No",store_material_value="",
            selected_container_type="",latitude="",longitude="";
    private int selected_position = 0,store_material_id = 0,store_job_id = 0,store_task_order_id = 0,user_id=0,vehicle_number=0,store_collection_site_id=0,
            user_role=0,selected_material_id=0,store_delivery_site_id=0,selected_collection_site_id=0,selected_delivery_site_id=0,doc_id_length=0, given_material_id=0, given_material_id_new = 0,
            given_collection_site_id=0,given_collection_site_id_new=0,given_delivery_site_id=0,given_delivery_site_id_new=0,material_id=0,address_management_id=0;
    double net = 0.0;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference vehicleDetailsReference,supplyWasteFormsReference, sortKeyTableReference,
            taskManagementReference,materialReference,siteManagementReference,assignMaterialReference, jobManagementDetailsNewReference,
            containerTypeReference;
    private ProgressDialog progressDialog,printerDialog;
    private CollectionDeliveryPoint collectionDeliveryPoint;
    private boolean hasImage = false,hasImageTwo = false,isSigned = false,isSignedOperator= false,isSignedThird = false, isValidationDone = false,show_method_execute_material = false,
            show_method_execute_collection_site=false,show_method_execute_del_site=false;
    private Bitmap getDrawable1, getDrawable2,operator_sign_bitmap,waste_transfer_print_layout;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Intent intent;
    private ArrayList<String> store_material_description_list = new ArrayList<>();
    private ArrayList<Integer> store_material_id_list = new ArrayList<>();
    private ArrayList<String> store_site_address_collection = new ArrayList<>();
    private ArrayList<Integer> store_site_id_collection = new ArrayList<>();
    private ArrayList<Integer> store_site_id_delivery = new ArrayList<>();
    private ArrayList<String> store_site_address_delivery = new ArrayList<>();
    private ArrayList<String> arrayList_hour = new ArrayList<>();
    private ArrayList<String> arrayList_minute = new ArrayList<>();
    private ArrayList<String> arrayList_second = new ArrayList<>();
    private ArrayList<String> store_ewc_code_list = new ArrayList<>();
    private ArrayList<String> store_material_value_list = new ArrayList<>();
    private ArrayList<String> material_value_list = new ArrayList<>();
    private ArrayList<Integer> arrayList_add_management_collection = new ArrayList<>();
    private ArrayList<Integer> arrayList_site_id_delivery = new ArrayList<>();
    private ArrayList<String>  store_material_type_list = new ArrayList<>();
    private ArrayList<Integer> assignMaterialDocumentId= new ArrayList<>();
    private ArrayList<Integer> material_desc_doc_id = new ArrayList<>();
    public static ArrayList<String> material_description_list = new ArrayList<>();
    private ArrayList<String> ewc_code_doc_list = new ArrayList<>();
    private ArrayList<String> arrayList_material_type_doc_list = new ArrayList<>();
    private ArrayList<String> arrayList_container_type = new ArrayList<>();
    private EditText et_comment,et_gross_weight,et_tare,et_unit,et_external_ticket,et_collection_reference;
    protected static final String TAG = "TAG";
    private static final int REQUEST_CAMERA= 0;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    BluetoothAdapter mBluetoothAdapter;
    private ZebraPrinter printer;
    private Connection connection;
    private AlertDialog alertDialog_time;
    private RecyclerView rv_hour,rv_minute,rv_hour_two,rv_minute_two;
    private HourAdapter hourAdapter;
    private MinuteAdapter minuteAdapter;
    private Uri imageUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waste_transfer_form_driver);
        initView();
    }

    private void initView(){
        FirestoreManager.initPersistentIndexManager();
        vehicleDetailsReference = db.collection("CR_vehicle_details");
        supplyWasteFormsReference = db.collection("CR_supply_waste_forms");
        sortKeyTableReference = db.collection("CR_sort_key");
        taskManagementReference = db.collection("CR_task_management");
        materialReference = db.collection("CR_material");
        siteManagementReference = db.collection("CR_site_management");
        assignMaterialReference = db.collection("CR_assign_material");
        jobManagementDetailsNewReference = db.collection("CR_job_management_new_details");
        containerTypeReference = db.collection("CR_container_types");
        progressDialog = Dialog.showProgressDialog(this);
        sharedPreferences = getApplicationContext().getSharedPreferences("MyPref",0);
        editor = sharedPreferences.edit();
        waste_coming_form = sharedPreferences.getString("waste_coming_form", null);
        SessionManager sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_name = user.get(SessionManager.KEY_FULL_NAME);
        String role = user.get(SessionManager.KEY_ROLE_ONE);
        if (role!=null)
            user_role = Integer.parseInt(role);
        user_id = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        HashMap<String, String> vehicle = sessionManager.getVehicleDetails();
        vehicle_number = Integer.parseInt(Objects.requireNonNull(vehicle.get(SessionManager.KEY_VEHICLE_ID)));
        Log.d(TAG,"USER_ID : " +user_name);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        iv_cross = findViewById(R.id.iv_cross);
        iv_cross.setVisibility(View.GONE);
        iv_camera = findViewById(R.id.iv_camera);
        iv_photo_one = findViewById(R.id.iv_photo_one);
        iv_photo_two = findViewById(R.id.iv_photo_two);
        iv_close_one = findViewById(R.id.iv_close_one);
        iv_close_two = findViewById(R.id.iv_close_two);
        bt_discard = findViewById(R.id.bt_discard);
        bt_select_on_site = findViewById(R.id.bt_select_on_site);
        bt_select_off_site = findViewById(R.id.bt_select_off_site);
        bt_third_party_yes = findViewById(R.id.bt_third_party_yes);
        bt_third_party_no = findViewById(R.id.bt_third_party_no);
        bt_print_ticket_yes = findViewById(R.id.bt_print_ticket_yes);
        bt_print_ticket_no = findViewById(R.id.bt_print_ticket_no);
        bt_driver_sign = findViewById(R.id.bt_driver_sign);
        bt_operator_sign = findViewById(R.id.bt_operator_sign);
        bt_submit = findViewById(R.id.bt_submit);
        bt_edit_form = findViewById(R.id.bt_edit_form);
        bt_size_16y = findViewById(R.id.bt_size_16y);
        bt_size_20y = findViewById(R.id.bt_size_20y);
        bt_size_30y = findViewById(R.id.bt_size_30y);
        bt_size_40y = findViewById(R.id.bt_size_40y);
        bt_size_6y = findViewById(R.id.bt_size_6y);
        bt_size_8y = findViewById(R.id.bt_size_8y);
        bt_size_12y = findViewById(R.id.bt_size_12y);
        bt_size_15y = findViewById(R.id.bt_size_15y);
        bt_size_18T = findViewById(R.id.bt_size_18T);
        bt_weight = findViewById(R.id.bt_weight);
        bt_unit = findViewById(R.id.bt_unit);
        ll_driver_sign = findViewById(R.id.ll_driver_sign);
        ll_operator_sign = findViewById(R.id.ll_operator_sign);
        ll_operator_signature = findViewById(R.id.ll_operator_signature);
        ll_third_party_sign = findViewById(R.id.ll_third_party_sign);
        ll_container1 = findViewById(R.id.ll_container1);
        ll_container2 = findViewById(R.id.ll_container2);
        ll_container3 = findViewById(R.id.ll_container3);
        ll_container_type = findViewById(R.id.ll_container_type);
        ll_weight_unit = findViewById(R.id.ll_weight_unit);
        ll_unit = findViewById(R.id.ll_unit);
        ll_gross = findViewById(R.id.ll_gross);
        ll_tare = findViewById(R.id.ll_tare);
        ll_net = findViewById(R.id.ll_net);
        ll_images = findViewById(R.id.ll_images);
        tv_header = findViewById(R.id.tv_header);
        tv_ticket_no = findViewById(R.id.tv_ticket_no);
        tv_date = findViewById(R.id.tv_date);
        tv_time = findViewById(R.id.tv_time);
        tv_registration_no = findViewById(R.id.tv_registration_no);
        tv_clear_driver = findViewById(R.id.tv_clear_driver);
        tv_clear_operator = findViewById(R.id.tv_clear_operator);
        tv_clear_third_party = findViewById(R.id.tv_clear_third_party);
        tv_waste_carrier_no = findViewById(R.id.tv_waste_carrier_no);
        tv_waste_transfer_declaration = findViewById(R.id.tv_waste_transfer_declaration);
        tv_operator = findViewById(R.id.tv_operator);
        tv_sic_code = findViewById(R.id.tv_sic_code);
        tv_net_weight = findViewById(R.id.tv_net_weight);
        tv_material_value_caution = findViewById(R.id.tv_material_value_caution);
        tv_on_site = findViewById(R.id.tv_on_site);
        tv_off_site = findViewById(R.id.tv_off_site);
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
        et_collection_reference = findViewById(R.id.et_collection_reference);
        et_external_ticket = findViewById(R.id.et_external_ticket);
        et_comment = findViewById(R.id.et_comment);
        et_collection_reference.setFilters(new InputFilter[] { filter });
        et_external_ticket.setFilters(new InputFilter[] { filter });
        et_comment = findViewById(R.id.et_comment);
        et_gross_weight = findViewById(R.id.et_gross_weight);
        et_tare = findViewById(R.id.et_tare);
        et_unit = findViewById(R.id.et_unit);
        sp_materials = findViewById(R.id.sp_materials);
        sp_current_site = findViewById(R.id.sp_current_site);
        sp_delivery_site = findViewById(R.id.sp_delivery_site);
        sp_container_type  = findViewById(R.id.sp_container_type);
        signaturePadDriver = findViewById(R.id.signature_pad_driver);
        signaturePadOperator = findViewById(R.id.signature_pad_operator);
        signaturePadThirdParty = findViewById(R.id.signature_pad_third_party);
        date = AppData.date();
time = AppData.Time();
        getDataFromCollectionDelivery();
        fetchVehicleRegistrationNumber();
        setSignDriver();
        setSignOperator();
        setSignaturePadThirdParty();
        getMaximumLength();
        initializeOnClick();
        showNetValueFromGross();
        showNetValueFromTare();
    }

    private String generateTicket(){
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");
        String currentDate = sdf.format(new Date());
        String ticket_no =  "W"+currentDate+String.format("%06d", number);
        Log.d(TAG,"Ticket No:" +ticket_no);
        return ticket_no;
    }

    private void getDataFromCollectionDelivery(){
        collectionDeliveryPoint = new CollectionDeliveryPoint(WasteTransferFormDriver.this,user_id,vehicle_number);
        // offline support for collectionDeliveryPoint class
        collectionDeliveryPoint.enableOfflineSupportCommon();
        collectionDeliveryPoint.jobManagement();
        CollectionDeliveryPoint.startTask = new StartTask() {
            @Override
            public void getDetails(int collection_site_id,String collection_site_name,String collection_address, String delivery_address,int delivery_site_id, int job_id, int task_order_id, int material_id,String material_description,
                                   String ewc_code,String material_value,String note_type,String sic_code,String carrier_no, String permit_no_collection,String permit_no_delivery,String site_type,String material_type,String project_no) {
                store_collection_site_id = collection_site_id;
                store_collection_site_name = collection_site_name;
                store_collection_site_address = collection_address;
                store_delivery_point_address = delivery_address;
                store_delivery_site_id = delivery_site_id;
                store_project_no = project_no;
                store_job_id = job_id;
                store_task_order_id = task_order_id;
                store_material_id = material_id;
                store_material_description = material_description;
                store_ewc_code = ewc_code;
                store_material_value = material_value;
                store_note_type = note_type;
                store_material_type = material_type;
                Log.d(TAG,"STORE EWC :" +store_ewc_code+"  "+store_material_type+" "+waste_coming_form
                +" "+store_collection_site_id);
                if (store_ewc_code.equals("") && store_material_type.equals("Collection")) {
                    tv_header.setText("CL:aire Form");
                    tv_waste_transfer_declaration.setText(R.string.sign_caution_three);
                }
                if (store_material_value.equals("High"))
                    tv_material_value_caution.setVisibility(View.VISIBLE);
                else
                    tv_material_value_caution.setVisibility(View.GONE);
                store_sic_code = sic_code;
                store_permit_no_collection = permit_no_collection;
                store_permit_no_delivery = permit_no_delivery;
                collectionSiteAddressManagement();
                sp_current_site.setPrompt(store_collection_site_address);
                sp_delivery_site.setPrompt(store_delivery_point_address);
                getMaterialDetails(store_collection_site_id);
                getCollectionSiteIdFromAddressManagement(store_collection_site_id);
                getDeliverySiteIdFromAddressManagement(store_delivery_site_id);
                assignMaterialsDeliverySite(store_material_id);
                if (waste_coming_form.equals("LeftCollectionPoint") || waste_coming_form.equals("ArrivedDeliveryPoint") || waste_coming_form.equals("FinishTask")) {
                    showWasteTransferFormData();
                    ll_operator_signature.setVisibility(View.GONE);
                } else if (waste_coming_form.equals("ArrivedCollectionPoint") ){
                    if (store_ewc_code.equals("") && store_material_type.equals("Collection"))
                        tv_sic_code.setText("43.12");
                    else
                        tv_sic_code.setText(store_sic_code);
                    tv_operator.setText(user_name);
                    if (ticket_no.equals(""))
                        tv_ticket_no.setText(generateTicket());
                    else
                        tv_ticket_no.setText(ticket_no);
                }
            }
        };
    }

    // get material and sic according to collection point
    private void getMaterialDetails(int site_id){
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
                            Log.d(TAG,"store_sic_code :" + store_sic_code);
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
                Toast.makeText(WasteTransferFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void assignMaterial(int site_id){
        Query query = assignMaterialReference.whereEqualTo("site_id",site_id).whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    String material_ids = "";
                    store_material_id_list.clear();
                    assignMaterialDocumentId.clear();
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (queryDocumentSnapshot.exists()) {
                                material_ids = queryDocumentSnapshot.getString("material_ids");
                                assignMaterialDocumentId.add(Integer.parseInt(queryDocumentSnapshot.getId()));
                            }
                        }
                        if (material_ids != null && material_ids.contains(",")) {
                            ArrayList<String> materialIdListString = new ArrayList<String>(Arrays.asList(material_ids.split(",")));
                            for (int i = 0; i < materialIdListString.size(); i++) {
                                store_material_id_list.add(Integer.valueOf(materialIdListString.get(i)));
                            }
                        } else {
                            if (material_ids != null ) {
                                if (!material_ids.equals("")) {
                                    store_material_id_list.add(Integer.valueOf(material_ids));
                                }
                            }
                        }
                        Log.d(TAG,"Material id list :" +store_material_id_list);
                        if (!store_material_id_list.isEmpty())
                            material();
                    }
                    else {
                        progressDialog.dismiss();
                        Dialog.alertDialog(WasteTransferFormDriver.this,"No material assigned with this site.");
                        store_material_id_list.clear();
                        store_material_description_list.clear();
                        material_desc_doc_id.clear();
                        sp_materials.setAdapter(null);
                    }
                }
                else {
                    progressDialog.dismiss();
                    Dialog.alertDialogToDashBoard(WasteTransferFormDriver.this,"Unable to get task details.");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WasteTransferFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void material(){
        Query query = materialReference.whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    material_desc_doc_id.clear();
                    store_material_description_list.clear();
                    material_description_list.clear();
                    store_ewc_code_list.clear();
                    ewc_code_doc_list.clear();
                    store_material_value_list.clear();
                    material_value_list.clear();
                    arrayList_material_type_doc_list.clear();
                    store_material_type_list.clear();
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (queryDocumentSnapshot.exists()) {
                                int document_id = Integer.parseInt(queryDocumentSnapshot.getId());
                                material_desc_doc_id.add(document_id);
                                store_material_description_list.add(queryDocumentSnapshot.getString("material_description"));
                                ewc_code_doc_list.add(queryDocumentSnapshot.getString("ewc_code"));
                                store_material_value_list.add(queryDocumentSnapshot.getString("material_value"));
                                arrayList_material_type_doc_list.add(queryDocumentSnapshot.getString("material_type"));
                            }
                        }
                        Log.d(TAG,"TFUU : " +material_desc_doc_id+" " +store_material_id_list);
                        for (int i = 0; i < store_material_id_list.size(); i++) {
                            for (int j = 0; j < material_desc_doc_id.size(); j++) {
                                if (store_material_id_list.get(i).equals(material_desc_doc_id.get(j))) {
                                    material_description_list.add(store_material_description_list.get(j));
                                    store_ewc_code_list.add(ewc_code_doc_list.get(j));
                                    material_value_list.add(store_material_value_list.get(j));
                                    store_material_type_list.add(arrayList_material_type_doc_list.get(j));
                                }
                            }
                        }
                        Log.d(TAG,"GTREET 1 :" +show_method_execute_material+" "+given_material_id_new+" "+
                                store_material_id);
                        int material_id=0;
                        if (!show_method_execute_material) {
                            if (given_material_id_new ==0) {
                                for (int j = 0; j < store_material_id_list.size(); j++) {
                                    if (store_material_id == store_material_id_list.get(j)) {
                                        material_id = j;
                                    }
                                }
                            }else {
                                for (int j = 0; j < store_material_id_list.size(); j++) {
                                    if (given_material_id_new == store_material_id_list.get(j)) {
                                        material_id = j;
                                    }
                                }
                            }
                            Log.d(TAG,"I am :" +"0"+"   "+material_id);
                            setMaterialAdapter(material_id);
                        }
                        if (show_method_execute_material) { //  && !store_ewc_code.isEmpty()
                            if (given_material_id_new == 0) {
                                Log.d(TAG,"I am :" +"1");
                                for (int i = 0; i < store_material_id_list.size(); i++) {
                                    if (given_material_id == store_material_id_list.get(i)) {
                                       material_id = i;
                                    }
                                }
                            } else {
                                Log.d(TAG,"I am :" +"2");
                                for (int i = 0; i < store_material_id_list.size(); i++) {
                                    if (given_material_id_new == store_material_id_list.get(i)) {
                                        material_id = i;
                                    }
                                }
                            }
                            Log.d(TAG,"FFJ :" +material_id+"  "+store_material_id_list);
                            setMaterialAdapter(material_id);
                        }

                    }
                    else {
                        progressDialog.dismiss();
                        Dialog.alertDialog(WasteTransferFormDriver.this,"Unable to get task details.");
                    }
                }
                else {
                    progressDialog.dismiss();
                    Dialog.alertDialog(WasteTransferFormDriver.this,"Unable to get task details.");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(WasteTransferFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setMaterialAdapter(int material_id){
        Log.d(TAG,"MMMM :" +material_id);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_custom_layout, material_description_list);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        Log.d(TAG,"Material ids:" + store_material_id_list);
        Log.d(TAG,"Material des:" + material_description_list);
        sp_materials.setAdapter(spinnerArrayAdapter);
        sp_materials.setSelection(material_id);
    }



    private void initializeOnClick(){
        iv_cross.setOnClickListener(this);
        bt_discard.setOnClickListener(this);
        bt_driver_sign.setOnClickListener(this);
        bt_operator_sign.setOnClickListener(this);
        tv_clear_driver.setOnClickListener(this);
        tv_clear_operator.setOnClickListener(this);
        tv_clear_third_party.setOnClickListener(this);
        bt_select_on_site.setOnClickListener(this);
        bt_select_off_site.setOnClickListener(this);
        bt_third_party_yes.setOnClickListener(this);
        bt_third_party_no.setOnClickListener(this);
        bt_print_ticket_yes.setOnClickListener(this);
        bt_print_ticket_no.setOnClickListener(this);
        bt_submit.setOnClickListener(this);
        bt_edit_form.setOnClickListener(this);
        bt_weight.setOnClickListener(this);
        bt_unit.setOnClickListener(this);
        iv_camera.setOnClickListener(this);
        iv_close_one.setOnClickListener(this);
        iv_close_two.setOnClickListener(this);
        iv_photo_one.setOnClickListener(this);
        iv_photo_two.setOnClickListener(this);
        bt_size_16y.setOnClickListener(this);
        bt_size_6y.setOnClickListener(this);
        bt_size_20y.setOnClickListener(this);
        bt_size_30y.setOnClickListener(this);
        bt_size_40y.setOnClickListener(this);
        bt_size_6y.setOnClickListener(this);
        bt_size_8y.setOnClickListener(this);
        bt_size_12y.setOnClickListener(this);
        bt_size_15y.setOnClickListener(this);
        bt_size_18T.setOnClickListener(this);
        sp_current_site.setOnItemSelectedListener(this);
        sp_delivery_site.setOnItemSelectedListener(this);
        sp_materials.setOnItemSelectedListener(this);
        sp_container_type.setOnItemSelectedListener(this);
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_cross:
                finish();
                break;
            case R.id.bt_submit:
                if (checkValidation()) {
                    editor.putBoolean("isSubmittedWaste", true);
                    editor.apply();
                    updateJobManagementDetails();
                    if (waste_coming_form.equals("ArrivedCollectionPoint")) {
                        storeWasteTransferFormData();
                        intent = new Intent(WasteTransferFormDriver.this, com.crate.crateam.activities.LeftCollectionPoint.class);
                        startActivity(intent);
                        finish();
                        if (!AppData.internetOnline(WasteTransferFormDriver.this)) {
                            progressDialog.dismiss();
                        }
                    } else if (waste_coming_form.equals("LeftCollectionPoint")) {
                        updateWasteFormDriver();
                        intent = new Intent(WasteTransferFormDriver.this, com.crate.crateam.activities.LeftCollectionPoint.class);
                        startActivity(intent);
                        finish();
                        if (!AppData.internetOnline(WasteTransferFormDriver.this)) {
                            progressDialog.dismiss();
                        }
                    } else if (waste_coming_form.equals("ArrivedDeliveryPoint")) {
                        updateWasteFormDriver();
                        intent = new Intent(WasteTransferFormDriver.this, ArrivedDeliveryPoint.class);
                        startActivity(intent);
                        finish();
                        if (!AppData.internetOnline(WasteTransferFormDriver.this)) {
                            progressDialog.dismiss();
                        }
                    } else if (waste_coming_form.equals("FinishTask")) {
                        updateWasteFormDriver();
                        intent = new Intent(WasteTransferFormDriver.this, com.crate.crateam.activities.FinishTaskActivity.class);
                        startActivity(intent);
                        finish();
                        if (!AppData.internetOnline(WasteTransferFormDriver.this)) {
                            progressDialog.dismiss();
                        }
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
                Toast.makeText(this,"Sign Cleared",Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_clear_operator:
                signaturePadOperator.clear();
                Toast.makeText(this,"Sign Cleared",Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_clear_third_party:
                signaturePadThirdParty.clear();
                Toast.makeText(this,"Sign Cleared",Toast.LENGTH_SHORT).show();
                break;
            case R.id.bt_third_party_yes:
                third_party_signature = "Yes";
                setSelectedButtonBackground(bt_third_party_yes);
                setDefaultButtonBackground(bt_third_party_no);
                ll_third_party_sign.setVisibility(View.VISIBLE);
                break;
            case R.id.bt_third_party_no:
                third_party_signature = "No";
                setSelectedButtonBackground(bt_third_party_no);
                setDefaultButtonBackground(bt_third_party_yes);
                ll_third_party_sign.setVisibility(View.GONE);
                signaturePadThirdParty.clear();
                break;
            case R.id.bt_print_ticket_yes:
                 print_ticket = "Yes";
                 if (checkValidation()){
                    bt_print_ticket_no.setEnabled(false);
                    bt_discard.setVisibility(View.GONE);
                    setSelectedButtonBackground(bt_print_ticket_yes);
                    setDefaultButtonBackground(bt_print_ticket_no);
                    enableBluetooth();
                }
                break;
            case R.id.bt_print_ticket_no:
                setSelectedButtonBackground(bt_print_ticket_no);
                setDefaultButtonBackground(bt_print_ticket_yes);
                print_ticket = "No";
                bt_discard.setVisibility(View.VISIBLE);
                break;
            case R.id.bt_edit_form:
                setSelectedButtonBackground(bt_edit_form);
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
                third_container_size=bt_size_18T.getText().toString().trim();
                setSelectContainerBackground(bt_size_18T);
                break;
            case R.id.bt_weight:
                weightVisible();
                break;
            case R.id.bt_unit:
                unitVisible();
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
    private void setSignaturePadThirdParty() {
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

    private void storeWasteTransferFormData(){
        String operator_name = tv_operator.getText().toString();
        date = AppData.date();
time = AppData.Time();
        sendSignatureAndImage();
        sendWasteTransferFormData(date,time,image1,image2,driver_sign,operator_sign,third_party_signature,operator_name);
    }

    private void sendSignatureAndImage(){
        if (image1==null)
            image1="";
        if (image2==null)
            image2="";
        if (isSigned&&isSignedOperator) {
            Bitmap signatureBitmap = signaturePadDriver.getSignatureBitmap();
            driver_sign = AppData.convertTOBase64Image(signatureBitmap);
            Bitmap signatureBitmapOperator = signaturePadOperator.getSignatureBitmap();
            operator_sign = AppData.convertTOBase64Image(signatureBitmapOperator);
        }
        if (!isSigned)
            driver_sign="No";
        if (isSignedThird) {
            Bitmap signatureBitmapThirdParty = signaturePadThirdParty.getSignatureBitmap();
            third_party_signature = AppData.convertTOBase64Image(signatureBitmapThirdParty);
        }
    }

    // at the edit time operator sign layout not visible
    private void editSignatureAndImage(){
        if (image1==null)
            image1="";
        if (image2==null)
            image2="";
        if (isSigned) {
            Bitmap signatureBitmap = signaturePadDriver.getSignatureBitmap();
            driver_sign = AppData.convertTOBase64Image(signatureBitmap);
        }
        if (!isSigned)
            driver_sign="No";
        if (isSignedThird) {
            Bitmap signatureBitmapThirdParty = signaturePadThirdParty.getSignatureBitmap();
            third_party_signature = AppData.convertTOBase64Image(signatureBitmapThirdParty);
        }
    }

    private Boolean checkValidation(){
        String weight_unit_container = "";
        if (vehicle_type_id.equals("N1") || vehicle_type_id.equals("N2") || vehicle_type_id.equals("N3") || vehicle_type_id.equals("Artic Unit")){
            weight_unit_container = "WeightUnit";
        }else{
            weight_unit_container = "Container";
        }
        if (selected_collection_site_name.equals("Select collection site"))
            Dialog.alertDialog(this, "Please select collection site");
        else if (selected_material_id!=0 && selected_delivery_site_id==0)
            Dialog.alertDialog(this, "Please select delivery site");
        else if (selected_delivery_site_name.equals("Select delivery site"))
            Dialog.alertDialog(this, "Please select delivery site");
        else if (tv_on_site.getText().toString().trim().isEmpty())
            Dialog.alertDialog(this, "Please select on site time.");
        else if (tv_off_site.getText().toString().trim().isEmpty())
            Dialog.alertDialog(this, "Please select off site time.");
        else if (store_material_value.equals("High") && image1.equals(""))
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
            Dialog.alertDialog(this,"Driver sign mandatory.");
        else if (!isSignedOperator && waste_coming_form.equals("ArrivedCollectionPoint"))
            Dialog.alertDialog(this,"Operator sign mandatory.");
        else if (third_party_signature.equals(""))
            Dialog.alertDialog(this,"Please select third party signature option.");
        else if (third_party_signature.equals("Yes")&& !isSignedThird)
            Dialog.alertDialog(this,"Please sign the form by third party.");
        else if (print_ticket.equals(""))
            Dialog.alertDialog(this,"Please select print ticket option.");
        else
            isValidationDone = true;
        return isValidationDone;
    }

    private void sendWasteTransferFormData(final String date, final String time, String image1, String image2, String driver_sign, String operator_sign, String third_party_signature, String operator_name){
        ticket_no = tv_ticket_no.getText().toString();
        comment = et_comment.getText().toString().trim();
        latLon();
        Log.d(TAG,"EWC CODE :" +store_ewc_code+" "+vehicle_type_id+" ");
        Log.d(TAG,"Delivery Site:" +store_delivery_site_id);
        Map<String,Object> mapWasteTransferDriver = new HashMap<>();
        mapWasteTransferDriver.put("user_id",user_id);
        mapWasteTransferDriver.put("user_role_id",user_role);
        mapWasteTransferDriver.put("operator",operator_name);
        mapWasteTransferDriver.put("ticket_no",tv_ticket_no.getText()+"C");
        mapWasteTransferDriver.put("ticket_id",tv_ticket_no.getText());
        mapWasteTransferDriver.put("ticket_type", "Collection");
        mapWasteTransferDriver.put("collection_ticket_no","");
        mapWasteTransferDriver.put("current_site_id",store_collection_site_id);
        mapWasteTransferDriver.put("current_site_address",store_collection_site_address);
        mapWasteTransferDriver.put("material_id",store_material_id);
        mapWasteTransferDriver.put("material_value",store_material_value);
        mapWasteTransferDriver.put("delivery_site_id",store_delivery_site_id);
        mapWasteTransferDriver.put("delivery_reference","");
        mapWasteTransferDriver.put("delivery_location","");
        mapWasteTransferDriver.put("container_type",selected_container_type);
        mapWasteTransferDriver.put("vehicle_id",vehicle_number);
        mapWasteTransferDriver.put("waste_carrier_no",tv_waste_carrier_no.getText().toString());
        mapWasteTransferDriver.put("project_no",store_project_no);
        mapWasteTransferDriver.put("submission_date",date);
        mapWasteTransferDriver.put("submission_time",time);
        mapWasteTransferDriver.put("z_driver_sign",driver_sign);
        mapWasteTransferDriver.put("z_operator_sign",operator_sign);
        mapWasteTransferDriver.put("z_third_party_sign",third_party_signature);
        mapWasteTransferDriver.put("print_ticket",print_ticket);
        mapWasteTransferDriver.put("job_id",store_job_id);
        mapWasteTransferDriver.put("task_order_id",store_task_order_id);
        mapWasteTransferDriver.put("z_image_one",image1);
        mapWasteTransferDriver.put("z_image_two",image2);
        if (store_ewc_code.equals("") && store_material_type.equals("Collection"))
            mapWasteTransferDriver.put("sic_code","43.12");
        else
            mapWasteTransferDriver.put("sic_code",store_sic_code);
        if (selected_collection_site_id == store_collection_site_id)
            mapWasteTransferDriver.put("current_site_id_new",0);
        else {
            mapWasteTransferDriver.put("current_site_id_new", selected_collection_site_id);
            form_edited ="Yes";
        }
        if (selected_delivery_site_id == store_delivery_site_id)
            mapWasteTransferDriver.put("delivery_site_id_new",0);
        else {
            mapWasteTransferDriver.put("delivery_site_id_new", selected_delivery_site_id);
            form_edited ="Yes";
        }
        if (selected_material_id==store_material_id)
            mapWasteTransferDriver.put("material_id_new", 0);
        else {
            mapWasteTransferDriver.put("material_id_new", selected_material_id);
            form_edited ="Yes";
        }
        mapWasteTransferDriver.put("ewc_code", store_ewc_code);
        if (store_ewc_code.equals(""))
            mapWasteTransferDriver.put("ewc_available","No");
        else
            mapWasteTransferDriver.put("ewc_available","Yes");
        mapWasteTransferDriver.put("vehicle_type", vehicle_type_id);
        mapWasteTransferDriver.put("vehicle_type_new", "");
        mapWasteTransferDriver.put("haulier_id",haulier_company);
        mapWasteTransferDriver.put("haulier_id_new","");
        mapWasteTransferDriver.put("waste_carrier_no_new","");
        if (vehicle_type_id.equals("N1") || vehicle_type_id.equals("N2") || vehicle_type_id.equals("N3")|| vehicle_type_id.equals("Artic Unit")){
            mapWasteTransferDriver.put("gross_weight", et_gross_weight.getText().toString());
            mapWasteTransferDriver.put("tare", et_tare.getText().toString());
            mapWasteTransferDriver.put("net", String.valueOf(net));
            mapWasteTransferDriver.put("unit", et_unit.getText().toString().trim());
            mapWasteTransferDriver.put("container_size_one", "");
            mapWasteTransferDriver.put("container_size_two", "");
            mapWasteTransferDriver.put("container_size_three","");
        }else {
            if (selected_position!=0){
                if (selected_position<=4){
                    mapWasteTransferDriver.put("container_size_one", first_container_size);
                    mapWasteTransferDriver.put("container_size_two", "");
                }
                else {
                    mapWasteTransferDriver.put("container_size_one", "");
                    mapWasteTransferDriver.put("container_size_two", second_container_size);
                }
            }
            else {
                mapWasteTransferDriver.put("container_size_one", first_container_size);
                mapWasteTransferDriver.put("container_size_two", second_container_size);
            }
            mapWasteTransferDriver.put("container_size_three", third_container_size);
            mapWasteTransferDriver.put("gross_weight","");
            mapWasteTransferDriver.put("tare","");
            mapWasteTransferDriver.put("net","");
            mapWasteTransferDriver.put("unit","");
        }
        mapWasteTransferDriver.put("add_comment",comment);
        mapWasteTransferDriver.put("permit_no_collection",store_permit_no_collection);
        mapWasteTransferDriver.put("permit_no_delivery",store_permit_no_delivery);
        mapWasteTransferDriver.put("permit_no",permit_no);
        mapWasteTransferDriver.put("collection_reference",et_collection_reference.getText().toString());
        mapWasteTransferDriver.put("on_site_time",tv_on_site.getText().toString().trim());
        mapWasteTransferDriver.put("off_site_time",tv_off_site.getText().toString().trim());
        mapWasteTransferDriver.put("new_driver_id","");
        mapWasteTransferDriver.put("new_driver_name","");
        mapWasteTransferDriver.put("registration_no_new","");
        mapWasteTransferDriver.put("registration_no",vehicle_registration_number);
        mapWasteTransferDriver.put("external_ticket_no",et_external_ticket.getText().toString());
        mapWasteTransferDriver.put("note_type",store_note_type);
        mapWasteTransferDriver.put("id",ticket_no);
        mapWasteTransferDriver.put("status","Active");
        doc_id_length = doc_id_length+1;
        mapWasteTransferDriver.put("sort_key",doc_id_length);
        mapWasteTransferDriver.put("ticket_status","Open");
        mapWasteTransferDriver.put("backend_edited","No");
        mapWasteTransferDriver.put("is_updated","Yes");
        mapWasteTransferDriver.put("latitude",latitude);
        mapWasteTransferDriver.put("longitude",longitude);
        mapWasteTransferDriver.put("time_second_format", AppData.getTimeSecond());

        supplyWasteFormsReference.document(tv_ticket_no.getText()+"C").set(mapWasteTransferDriver).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Map<String,Object> objectMap_update = new HashMap<>();
                    objectMap_update.put("supply_waste_key",doc_id_length);
                    sortKeyTableReference.document(doc_id).update(objectMap_update);
                    updateTaskManagement(store_job_id,store_task_order_id);
                    Log.d(TAG,"DELIVERY & MATERIAL :"+selected_delivery_site_id+" "+selected_material_id+" "+store_delivery_site_id+" "+store_material_id);
                    if (selected_collection_site_id != store_collection_site_id){
                        Map<String, Object> task_management_update1 = new HashMap<>();
                        task_management_update1.put("collection_point",selected_collection_site_address);
                        taskManagementReference.document(store_job_id+"_"+store_task_order_id).update(task_management_update1);
                    }
                    if (selected_material_id!=store_material_id){
                        Map<String, Object> task_management_update2 = new HashMap<>();
                        task_management_update2.put("material_id",selected_material_id);
                        task_management_update2.put("ewc_code",store_ewc_code);
                        taskManagementReference.document(store_job_id+"_"+store_task_order_id).update(task_management_update2);
                    }
                    if (selected_delivery_site_id != store_delivery_site_id){
                        Map<String, Object> task_management_update3 = new HashMap<>();
                        task_management_update3.put("delivery_point",selected_delivery_site_address);
                        taskManagementReference.document(store_job_id+"_"+store_task_order_id).update(task_management_update3);
                    }
                    Log.d(TAG,"Success :" + "Form submitted successfully.");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"Error:" + "Form submission failed.");
            }
        });
    }

    private void latLon(){
        GPSTracker finder = new GPSTracker(WasteTransferFormDriver.this);
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

    // displaying data when edit the supply_form_driver table
    private void showWasteTransferFormData(){
        progressDialog.show();
        Query query = supplyWasteFormsReference.whereEqualTo("job_id",store_job_id).whereEqualTo("task_order_id",store_task_order_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    date = AppData.date();
time = AppData.Time();
                    for (QueryDocumentSnapshot documentSnapshot: Objects.requireNonNull(task.getResult())) {
                        ticket_no = documentSnapshot.getString("ticket_no");
                        tv_ticket_no.setText(ticket_no);
                        store_ewc_code = documentSnapshot.getString("ewc_code");
                        Log.d(TAG,"Ticket :" + ticket_no+" "+store_ewc_code);
                        if (store_ewc_code.equals("") && store_material_type.equals("Collection")){
                            tv_header.setText("CL:aire Form");
                            tv_waste_transfer_declaration.setText(R.string.sign_caution_three);
                            tv_sic_code.setText("43.12");
                        }else {
                            tv_header.setText("Waste Transfer Form - Collection");
                            tv_waste_transfer_declaration.setText(R.string.sign_caution_two);
                            tv_sic_code.setText(store_sic_code);
                        }
                        store_material_value = documentSnapshot.getString("material_value");
                        if (store_material_value.equals("High"))
                            tv_material_value_caution.setVisibility(View.VISIBLE);
                        else
                            tv_material_value_caution.setVisibility(View.GONE);
                        tv_operator.setText(user_name);
                        String vehicle_registration_no = documentSnapshot.getString("registration_no");
                        tv_registration_no.setText(vehicle_registration_no);
                        waste_carrier_no = documentSnapshot.getString("waste_carrier_no");
                        tv_waste_carrier_no.setText(waste_carrier_no);
                        haulier_company = documentSnapshot.getString("carrier_company");
                        vehicle_type_id = documentSnapshot.getString("vehicle_type");
                        String unit = documentSnapshot.getString("unit");
                        if (vehicle_type_id != null) {
                            if (vehicle_type_id.equals("N1") || vehicle_type_id.equals("N2") || vehicle_type_id.equals("N3")){
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
                        operator_sign = documentSnapshot.getString("z_operator_sign");
                        operator_sign_bitmap = convertBase64ToBitmap(operator_sign);
                        int show_collection_site_id_new = documentSnapshot.getLong("current_site_id_new").intValue();
                        getCollectionSiteIdNewFromAddressManagement(show_collection_site_id_new);
                        int show_collection_site_id = documentSnapshot.getLong("current_site_id").intValue();
                        getCollectionSiteIdFromAddressManagement(show_collection_site_id);
                        Log.d(TAG,"Show Collection Id Old & New:" +show_collection_site_id+" "+show_collection_site_id_new);
                        show_method_execute_collection_site=true;
                        show_method_execute_material = true;
                        given_material_id = documentSnapshot.getLong("material_id").intValue();
                        given_material_id_new = documentSnapshot.getLong("material_id_new").intValue();
                        if (given_material_id_new == 0){
                            for (int i =0;i<store_material_id_list.size();i++){
                                if (given_material_id == store_material_id_list.get(i)) {
                                    sp_materials.setSelection(i);
                                    break;
                                }
                            }
                        }
                        else {
                            for (int i =0;i<store_material_id_list.size();i++){
                                if (given_material_id_new == store_material_id_list.get(i)) {
                                    Log.d(TAG,"MMM : " +given_material_id_new);
                                    sp_materials.setSelection(i);
                                    break;
                                }
                            }
                        }
                        show_method_execute_del_site=true;
                        given_delivery_site_id = documentSnapshot.getLong("delivery_site_id").intValue();
                        given_delivery_site_id_new = documentSnapshot.getLong("delivery_site_id_new").intValue();
                        getDeliverySiteIdFromAddressManagement(given_delivery_site_id);
                        getDeliverySiteIdNewFromAddressManagement(given_delivery_site_id_new);
                        if (given_delivery_site_id_new==0) {
                            for (int i = 0; i < store_site_id_delivery.size(); i++) {
                                if (given_delivery_site_id == store_site_id_delivery.get(i)) {
                                    sp_delivery_site.setSelection(i);
                                    break;
                                }
                            }
                        }else {
                            for (int i=0;i<store_site_id_delivery.size();i++){
                                if (given_delivery_site_id_new == store_site_id_delivery.get(i)){
                                    Log.d(TAG,"DDD : " +given_delivery_site_id_new);
                                    sp_delivery_site.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }
                    Log.d(TAG,"Collection Material Delivery :" +given_collection_site_id_new +" "+given_delivery_site_id_new+" "+given_material_id_new);
                    Log.d(TAG,"Container Size :" +first_container_size +" "+second_container_size+" "+third_container_size);
                    selectedContainerSize(first_container_size, second_container_size, third_container_size);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(WasteTransferFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void dateTime(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        date = df.format(c);
        tv_date.setText(date);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        time = simpleDateFormat.format(calendar.getTime());
        tv_time.setText(time);
    }

    private void setSelectContainerBackground(Button bt_container_size_select){
        bt_container_size_select.setBackground(getResources().getDrawable(R.drawable.login_button_background));
        bt_container_size_select.setTextColor(getResources().getColor(R.color.white));
    }
    private void setDefaultContainerBackground(Button bt_container_one, Button bt_container_two,Button bt_container_three ){
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
    private void setSelectedButtonBackground(Button selected_button){
        selected_button.setBackground(getResources().getDrawable(R.drawable.login_button_background));
        selected_button.setTextColor(getResources().getColor(R.color.white));
    }
    private void setDefaultButtonBackground(Button default_button){
        default_button.setBackground(getResources().getDrawable(R.drawable.edit_text_background));
        default_button.setTextColor(getResources().getColor(R.color.black_shade_two));
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
                Toast.makeText(WasteTransferFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchVehicleRegistrationNumber(){
        progressDialog.show();
        Query query=vehicleDetailsReference.whereEqualTo("external_vehicle","No").whereEqualTo("id",vehicle_number).whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        if (documentSnapshot.exists()) {
                            vehicle_registration_number = documentSnapshot.getString("registration_no");
                            haulier_company = documentSnapshot.getString("haulier");
                            waste_carrier_no = documentSnapshot.getString("haulier_carrier_no");
                            vehicle_type_id = documentSnapshot.getString("vehicle_type");
                        }
                    }
                    tv_registration_no.setText(vehicle_registration_number);
                    tv_waste_carrier_no.setText(waste_carrier_no);
                    containerSize(vehicle_type_id);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(WasteTransferFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMaterialDescription(String id){
        materialReference.document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        store_ewc_code = document.getString("ewc_code");
                        store_material_value = document.getString("material_value");
                        store_material_type = document.getString("material_type");
                        selected_material_name = document.getString("material_description");
                    }
                    if (store_material_value.equals("High"))
                        tv_material_value_caution.setVisibility(View.VISIBLE);
                    else
                        tv_material_value_caution.setVisibility(View.GONE);
                    if (store_ewc_code.equals("") && store_material_type.equals("Collection")){
                        tv_header.setText("CL:aire Form");
                        tv_waste_transfer_declaration.setText(R.string.sign_caution_three);
                        tv_sic_code.setText("43.12");
                    }else {
                        tv_header.setText("Waste Transfer Form - Collection");
                        tv_waste_transfer_declaration.setText(R.string.sign_caution_two);
                        tv_sic_code.setText(store_sic_code);
                    }
                } else {
                    Log.d(TAG, "Failed to fetch ewc code.", task.getException());
                }
            }
        });
    }

    private void updateWasteFormDriver(){
        Query query = supplyWasteFormsReference.whereEqualTo("job_id",store_job_id).whereEqualTo("task_order_id",store_task_order_id)
                .whereEqualTo("ticket_no",tv_ticket_no.getText().toString());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    //sendSignatureAndImage();
                    editSignatureAndImage();
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()) {
                            String doc_id = queryDocumentSnapshot.getId();
                            Map<String, Object> objectMapUpdate = new HashMap<>();
                        String gross_weight = et_gross_weight.getText().toString().trim();
                        String tare = et_tare.getText().toString().trim();
                        if (vehicle_type_id.equals("N1") || vehicle_type_id.equals("N2") || vehicle_type_id.equals("N3")){
                            if (!gross_weight.isEmpty()) {
                                net = Double.parseDouble(gross_weight) - Double.parseDouble(tare);
                                showNetValueFromGross();
                                showNetValueFromTare();
                                objectMapUpdate.put("gross_weight", gross_weight);
                                objectMapUpdate.put("tare", tare);
                                objectMapUpdate.put("net", String.valueOf(net));
                            }else {
                                objectMapUpdate.put("gross_weight", "");
                                objectMapUpdate.put("tare", "");
                                objectMapUpdate.put("net", "");
                            }
                            if (!et_unit.getText().toString().trim().isEmpty())
                                objectMapUpdate.put("unit",et_unit.getText().toString().trim());
                            else
                                objectMapUpdate.put("unit","");
                            objectMapUpdate.put("container_size_one", "");
                            objectMapUpdate.put("container_size_two", "");
                            objectMapUpdate.put("container_size_three","");
                        }else {
                            if (selected_position!=0){
                                if (selected_position<=4){
                                    objectMapUpdate.put("container_size_one", first_container_size);
                                    objectMapUpdate.put("container_size_two", "");
                                }
                                else {
                                    objectMapUpdate.put("container_size_one", "");
                                    objectMapUpdate.put("container_size_two", second_container_size);
                                }
                            }
                            else {
                                objectMapUpdate.put("container_size_one", first_container_size);
                                objectMapUpdate.put("container_size_two", second_container_size);
                            }
                            objectMapUpdate.put("container_size_three", third_container_size);
                            objectMapUpdate.put("gross_weight","");
                            objectMapUpdate.put("tare","");
                            objectMapUpdate.put("net","");
                            objectMapUpdate.put("unit","");
                        }
                        if (selected_collection_site_id==store_collection_site_id) {
                            getCollectionSiteID(store_collection_site_id);
                            objectMapUpdate.put("current_site_id_new", 0);
                            Log.d(TAG,"collection_site_address :" +store_collection_site_address);
                            updateCollectionTaskManagement(store_collection_site_address);
                        }else {
                            getCollectionSiteID(selected_collection_site_id);
                            objectMapUpdate.put("current_site_id_new", selected_collection_site_id);
                            Log.d(TAG,"collection_site_address :" +selected_collection_site_address);
                            updateCollectionTaskManagement(selected_collection_site_address);
                        }

                        if (selected_material_id == store_material_id) {
                            objectMapUpdate.put("material_id_new", 0);
                            updateMaterialTaskManagement(store_material_id);
                        }else {
                            objectMapUpdate.put("material_id_new", selected_material_id);
                            updateMaterialTaskManagement(selected_material_id);
                        }
                        if (selected_delivery_site_id == store_delivery_site_id) {
                            objectMapUpdate.put("delivery_site_id_new", 0);
                            updateDeliveryTaskManagement(store_delivery_point_address);
                        }else {
                            objectMapUpdate.put("delivery_site_id_new", selected_delivery_site_id);
                            updateDeliveryTaskManagement(selected_delivery_site_address);
                        }
                            objectMapUpdate.put("ewc_code",store_ewc_code);
                            if(store_ewc_code.equals(""))
                                objectMapUpdate.put("ewc_available","No");
                            else
                                objectMapUpdate.put("ewc_available","Yes");
                            if (store_ewc_code.equals("") && store_material_type.equals("Collection"))
                                objectMapUpdate.put("sic_code","43.12");
                            else
                                objectMapUpdate.put("sic_code", store_sic_code);
                            if (!image1.isEmpty())
                               objectMapUpdate.put("z_image_one", image1);
                            if (!image2.isEmpty())
                               objectMapUpdate.put("z_image_two", image2);
                            objectMapUpdate.put("z_operator_sign", operator_sign);
                            objectMapUpdate.put("z_driver_sign", driver_sign);
                            objectMapUpdate.put("z_third_party_sign", third_party_signature);
                            objectMapUpdate.put("is_updated","Yes");
                            objectMapUpdate.put("time_second_format",AppData.getTimeSecond());
                            objectMapUpdate.put("print_ticket", print_ticket);
                            if (!et_comment.getText().toString().trim().isEmpty()) {
                                comment = et_comment.getText().toString().trim();
                                objectMapUpdate.put("add_comment", comment);
                            }
                            Log.d(TAG,"ADGRDF :" +selected_collection_site_address+" "+selected_collection_site_id+" "+
                                                   selected_delivery_site_address+"  "+selected_delivery_site_id);
                            supplyWasteFormsReference.document(doc_id).update(objectMapUpdate);
                            Log.d(TAG,"DELIVERY & MATERIAL :"+selected_delivery_site_id+" "+selected_material_id+" "+store_delivery_site_id+" "+store_material_id);
                            if (selected_collection_site_id != store_collection_site_id || selected_delivery_site_id != store_delivery_site_id || selected_material_id!=store_material_id) {
                                Map<String, Object> task_management_update1 = new HashMap<>();
                                task_management_update1.put("form_edited", "Yes");
                                task_management_update1.put("is_updated","Yes");
                                taskManagementReference.document(store_job_id+"_"+store_task_order_id).update(task_management_update1);
                            }
                            finish();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WasteTransferFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCollectionTaskManagement(String collection_site_address){
        Map<String, Object> task_management_update = new HashMap<>();
        task_management_update.put("collection_point",collection_site_address);
        taskManagementReference.document(store_job_id+"_"+store_task_order_id).update(task_management_update);
    }
    private void updateMaterialTaskManagement(int material_id){
        Map<String, Object> task_management_update = new HashMap<>();
        task_management_update.put("material_id",material_id);
        task_management_update.put("ewc_code",store_ewc_code);
        taskManagementReference.document(store_job_id+"_"+store_task_order_id).update(task_management_update);
    }
    private void updateDeliveryTaskManagement(String delivery_site_address){
        Map<String, Object> task_management_update = new HashMap<>();
        task_management_update.put("delivery_point",delivery_site_address);
        taskManagementReference.document(store_job_id+"_"+store_task_order_id).update(task_management_update);
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
                    Toast.makeText(WasteTransferFormDriver.this, "Message", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void enableBluetooth(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(WasteTransferFormDriver.this, "Message1", Toast.LENGTH_SHORT).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                if (ContextCompat.checkSelfPermission(WasteTransferFormDriver.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(WasteTransferFormDriver.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
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
        if (ContextCompat.checkSelfPermission(WasteTransferFormDriver.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(WasteTransferFormDriver.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
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
            Intent connectIntent = new Intent(WasteTransferFormDriver.this,
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
            disconnect();
        }
    }

    public ZebraPrinter connect() {
        connection = null;
        connection = new BluetoothConnection(getMacAddress());
        try { if (ContextCompat.checkSelfPermission(WasteTransferFormDriver.this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(WasteTransferFormDriver.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
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
            SGD.SET("device.languages", "zpl", connection);
            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            Bitmap bmp = Bitmap.createBitmap(1, 1, conf); // this creates a MUTABLE bitmap
            Bitmap operatorSign;
            if (isSignedOperator)
                operatorSign = signaturePadOperator.getSignatureBitmap();
            else
                operatorSign = operator_sign_bitmap;
            if (selected_collection_site_id==0){
                selected_collection_site_address = store_collection_site_address;
                selected_collection_site_name = store_collection_site_name;
            }
            if (selected_delivery_site_id==0){
                selected_delivery_site_address = store_delivery_point_address;
            }
            if (selected_material_id==0){
                selected_material_name = store_material_description;
            }
            if (selected_container_type.equals("Select Container Type"))
                selected_container_type ="";
            Log.d(TAG,"FHGGHVMH :" +selected_material_name+"  "+selected_material_id);
            if (store_ewc_code.equals("") && store_material_type.equals("Collection")) {
                waste_transfer_print_layout = new PrintingLayoutFormat(this).claireFormToBitmap(tv_ticket_no.getText().toString(), haulier_company, user_name, selected_collection_site_name,
                        "43.12", selected_collection_site_address, selected_material_name+" "+store_ewc_code,et_unit.getText().toString().trim(),gross,tare,String.valueOf(net),
                        selected_collection_site_name, selected_delivery_site_name, selected_collection_site_address, selected_delivery_site_address,"","",et_collection_reference.getText().toString(),"",
                        tv_on_site.getText().toString(),tv_off_site.getText().toString(),et_external_ticket.getText().toString(), permit_no, haulier_company, user_name, date + " " + time, vehicle_registration_number
                        , vehicle_type_id, first_container_size + " " + second_container_size + " " + third_container_size,selected_container_type,operatorSign, signaturePadDriver.getSignatureBitmap());
            }else {
                waste_transfer_print_layout = new PrintingLayoutFormat(this).wasteTransferFormToBitmap(tv_ticket_no.getText().toString(), haulier_company, user_name, tv_waste_carrier_no.getText().toString(), selected_collection_site_name,
                        tv_sic_code.getText().toString(), selected_collection_site_address, selected_material_name+" "+store_ewc_code,et_unit.getText().toString().trim(),gross,tare,String.valueOf(net),
                        selected_collection_site_name, selected_delivery_site_name, selected_collection_site_address, selected_delivery_site_address,"","",et_collection_reference.getText().toString(),"",
                        tv_on_site.getText().toString(),tv_off_site.getText().toString(),et_external_ticket.getText().toString(), permit_no, haulier_company, user_name, tv_waste_carrier_no.getText().toString(), date + " " + time, vehicle_registration_number
                        , vehicle_type_id, first_container_size + " " + second_container_size + " " + third_container_size,selected_container_type,operatorSign, signaturePadDriver.getSignatureBitmap());
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

 

    private Bitmap convertBase64ToBitmap(String b64) {
        byte[] imageAsBytes = Base64.decode(b64.getBytes(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }

    private void alertDialog(String message){
        AlertDialog.Builder alertDialog=  new  AlertDialog.Builder(this);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton( "Discard",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        editor.putBoolean("isSubmittedWaste", false);
                        editor.apply();
                        if (waste_coming_form.equals("ArrivedCollectionPoint"))
                            intent = new Intent(WasteTransferFormDriver.this, com.crate.crateam.activities.ArrivedCollectionPoint.class);
                        else if (waste_coming_form.equals("LeftCollectionPoint"))
                            intent = new Intent(WasteTransferFormDriver.this, com.crate.crateam.activities.LeftCollectionPoint.class);
                        else if (waste_coming_form.equals("ArrivedDeliveryPoint"))
                            intent = new Intent(WasteTransferFormDriver.this,ArrivedDeliveryPoint.class);
                        else if (waste_coming_form.equals("FinishTask"))
                            intent = new Intent(WasteTransferFormDriver.this, com.crate.crateam.activities.FinishTaskActivity.class);
                        startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton( "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
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
                Toast.makeText(WasteTransferFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // insert ticket_no value
    private void updateTaskManagement(final int job_id, final int task_order_id){
        taskManagementReference.document(job_id+"_"+task_order_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        Map<String,Object> objectMap_update_task_management = new HashMap<>();
                        objectMap_update_task_management.put("ticket_no",ticket_no);
                        objectMap_update_task_management.put("is_updated","Yes");
                        objectMap_update_task_management.put("ewc_code",store_ewc_code);
                        objectMap_update_task_management.put("form_edited", form_edited);
                        taskManagementReference.document(job_id+"_"+task_order_id).update(objectMap_update_task_management);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WasteTransferFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {

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
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(WasteTransferFormDriver.this,R.layout.spinner_custom_layout,arrayList_hour);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_hour.setAdapter(spinnerArrayAdapter);
        ArrayAdapter<String> spinnerArrayAdapter1 = new ArrayAdapter<String>(WasteTransferFormDriver.this,R.layout.spinner_custom_layout,arrayList_minute);
        spinnerArrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_minute.setAdapter(spinnerArrayAdapter1);
        hourAdapter = new HourAdapter(WasteTransferFormDriver.this,arrayList_hour);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv_hour.setLayoutManager(mLayoutManager);
        rv_hour.setItemAnimator(new DefaultItemAnimator());
        rv_hour.setAdapter(hourAdapter);

        minuteAdapter = new MinuteAdapter(WasteTransferFormDriver.this,arrayList_minute);
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
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(WasteTransferFormDriver.this,R.layout.spinner_custom_layout,arrayList_hour);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_hour_two.setAdapter(spinnerArrayAdapter);
        ArrayAdapter<String> spinnerArrayAdapter1 = new ArrayAdapter<String>(WasteTransferFormDriver.this,R.layout.spinner_custom_layout,arrayList_minute);
        spinnerArrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_minute_two.setAdapter(spinnerArrayAdapter1);
        hourAdapter = new HourAdapter(WasteTransferFormDriver.this,arrayList_hour);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv_hour_two.setLayoutManager(mLayoutManager);
        rv_hour_two.setItemAnimator(new DefaultItemAnimator());
        rv_hour_two.setAdapter(hourAdapter);

        minuteAdapter = new MinuteAdapter(WasteTransferFormDriver.this,arrayList_minute);
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

      // getting delivery site name according to material id
      private void assignMaterialsDeliverySite(int material_id){
          Query query = assignMaterialReference.whereEqualTo("status","Active").whereEqualTo("site_type","Delivery");
          query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
              @Override
              public void onComplete(@NonNull Task<QuerySnapshot> task) {
                  if (task.isSuccessful()){
                      String materialIds = "";
                      arrayList_site_id_delivery.clear();
                      ArrayList<Integer> arrayList_material_id = new ArrayList<>();
                      if (!task.getResult().isEmpty()){
                          for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                              materialIds = queryDocumentSnapshot.getString("material_ids");
                              if (materialIds != null && materialIds.contains(",")) {
                                  ArrayList<String> materialIdListString = new ArrayList<String>(Arrays.asList(materialIds.split(",")));
                                  for (int i=0;i<materialIdListString.size();i++) {
                                      arrayList_material_id.add(Integer.valueOf(materialIdListString.get(i)));
                                      if (Integer.valueOf(materialIdListString.get(i)) == material_id) {
                                          arrayList_site_id_delivery.add(queryDocumentSnapshot.getLong("site_id").intValue());
                                      }
                                  }
                              }else {
                                  if (materialIds != null) {
                                      arrayList_material_id.add(Integer.valueOf(materialIds));
                                      if (Integer.valueOf(materialIds) == material_id){
                                          arrayList_site_id_delivery.add(queryDocumentSnapshot.getLong("site_id").intValue());
                                      }
                                  }
                              }
                          }
                          deliverySiteAddressManagement();
                      }
                      else progressDialog.dismiss();
                  }
              }
          }).addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                  Toast.makeText(WasteTransferFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
              }
          });
      }

    // get collection site address management table
    private void collectionSiteAddressManagement(){
        Query query = siteManagementReference.whereEqualTo("status", "Active").whereEqualTo("address_status","Active")
                .whereEqualTo("site_type","Collection");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    arrayList_add_management_collection.clear();
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                    arrayList_add_management_collection.add(queryDocumentSnapshot.getLong("id").intValue());
                                    Log.d(TAG,"Add Management Site Ids :" +arrayList_add_management_collection);
                        }
                        getCollectionSiteAddress();
                    }
                    else progressDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WasteTransferFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCollectionSiteAddress(){
        Query query = siteManagementReference.whereEqualTo("site_type","Collection").whereEqualTo("address_status","Active")
                .whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    store_site_id_collection.clear();
                    store_site_address_collection.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        int id = Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
                        for (int i = 0; i < arrayList_add_management_collection.size(); i++) {
                            if (id == arrayList_add_management_collection.get(i)) {
                                store_site_id_collection.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                                store_site_address_collection.add(queryDocumentSnapshot.getString("site_name"));
                                Log.d(TAG,"Site Management Site Ids :" +store_site_id_delivery+"  "+store_site_address_delivery);
                                break;
                            }
                        }
                    }
                    sp_current_site.setTitle("Sites");
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_custom_layout,store_site_address_collection);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_current_site.setAdapter(spinnerArrayAdapter);
                    if (store_site_address_collection.size()>0){
                        store_site_address_collection.add(0,"Select collection site");
                        store_site_id_collection.add(0,0);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(WasteTransferFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deliverySiteAddressManagement() {
        Query queryAddressManagement = siteManagementReference.whereEqualTo("status", "Active").whereEqualTo("address_status","Active")
                .whereEqualTo("site_type","Delivery");
        queryAddressManagement.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    store_site_id_delivery.clear();
                    store_site_address_delivery.clear();
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            int id = Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
                            for (int i = 0; i < arrayList_site_id_delivery.size(); i++) {
                                if (id == arrayList_site_id_delivery.get(i)) {
                                    store_site_id_delivery.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                                    store_site_address_delivery.add(queryDocumentSnapshot.getString("site_name"));
                                    Log.d(TAG,"Site Management Site Ids :" +store_site_id_delivery+"  "+store_site_address_delivery);
                                    break;
                                }
                            }
                        }
                        if (store_site_address_delivery.size()>0){
                            Log.d(TAG,"SSSSSSS :" +"I am here");
                            store_site_address_delivery.add(0,"Select delivery site");
                            store_site_id_delivery.add(0,0);
                        }
                        int del_site_id=0;
                        if (!show_method_execute_del_site) {
                            for (int j = 0; j < store_site_id_delivery.size(); j++) {
                                if (store_delivery_site_id == store_site_id_delivery.get(j)) {
                                    del_site_id = j;
                                }
                            }
                               setDeliverySiteAdapter(del_site_id);
                        }else {
                            Log.d(TAG,"DDDDDD : " +given_delivery_site_id_new +" "+given_delivery_site_id);
                            if (given_delivery_site_id_new == 0 && selected_delivery_site_id == 0) {
                                for (int i = 0; i < store_site_id_delivery.size(); i++) {
                                    if (given_delivery_site_id == store_site_id_delivery.get(i)) {
                                        del_site_id = i;
                                        Log.d(TAG,"DDDDDD 1: " +del_site_id);
                                    }
                                }
                                setDeliverySiteAdapter(del_site_id);
                            } else if (given_delivery_site_id_new != 0 && selected_delivery_site_id == 0) {
                                for (int i = 0; i < store_site_id_delivery.size(); i++) {
                                    if (given_delivery_site_id_new == store_site_id_delivery.get(i)) {
                                        del_site_id = i;
                                        Log.d(TAG,"DDDDDD 2: " +del_site_id);
                                    }
                                }
                                setDeliverySiteAdapter(del_site_id);
                            }
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(WasteTransferFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDeliverySiteAdapter(int del_site_id){
        Log.d(TAG,"Delivery site id:" +del_site_id);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_custom_layout,store_site_address_delivery);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        Log.d(TAG,"Task Status:" +task_status);
        sp_delivery_site.setAdapter(spinnerArrayAdapter);
        sp_delivery_site.setSelection(del_site_id);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_current_site:
                Log.d(TAG," Collection Id All:" +store_site_id_collection+" "+given_collection_site_id
                +" "+given_collection_site_id_new+" "+selected_collection_site_id);
                if (given_collection_site_id_new == 0) {
                    for (int i = 0; i < store_site_id_collection.size(); i++) {
                        if (given_collection_site_id == store_site_id_collection.get(i)) {
                            sp_current_site.setSelection(i);
                            break;
                        }
                    }
                    Log.d(TAG,"I am here :" +"Collection 1");
                }
                if (position >0 ) {
                    selected_collection_site_name = store_site_address_collection.get(position).trim();
                    selected_collection_site_id = store_site_id_collection.get(position);
                    Log.d(TAG,"Selected Site Collection:" +selected_collection_site_id);
                    if (selected_collection_site_id!=0){
                        for (int i = 0; i < store_site_id_collection.size(); i++) {
                            if (selected_collection_site_id == store_site_id_collection.get(i)) {
                                sp_current_site.setSelection(i);
                                break;
                            }
                        }
                        Log.d(TAG,"EEEEE :" +selected_collection_site_id);
                        Log.d(TAG,"I am here :" +"Collection 2  "+selected_collection_site_id);
                        getCollectionSiteID(selected_collection_site_id);
                        getMaterialDetails(selected_collection_site_id);
                    }
                }
                if (show_method_execute_collection_site) {
                    if (given_collection_site_id_new == 0 && selected_collection_site_id == 0) {
                        for (int i = 0; i < store_site_id_collection.size(); i++) {
                            if (given_collection_site_id == store_site_id_collection.get(i)) {
                                sp_current_site.setSelection(i);
                                break;
                            }
                        }
                        Log.d(TAG,"I am here :" +"Collection 3");
                        getCollectionSiteID(given_collection_site_id);
                        getMaterialDetails(given_collection_site_id);
                    } else if (given_collection_site_id_new != 0 && selected_collection_site_id == 0) {
                        for (int i = 0; i < store_site_id_collection.size(); i++) {
                            if (given_collection_site_id_new == store_site_id_collection.get(i)) {
                                sp_current_site.setSelection(i);
                                break;
                            }
                        }
                        Log.d(TAG,"I am here :" +"Collection 4");
                        getCollectionSiteID(given_collection_site_id_new);
                        getMaterialDetails(given_collection_site_id_new);
                    }
                }
                Log.d(TAG,"HGYUI :" +show_method_execute_collection_site);
                break;
            case R.id.sp_delivery_site:
                    Log.d(TAG,"Given Delivery Site Id :" +given_delivery_site_id);
                    Log.d(TAG,"Store Site Id List:" +store_site_id_delivery);
                    if (given_delivery_site_id_new == 0) {
                        for (int i = 0; i < store_site_id_delivery.size(); i++) {
                            if (given_delivery_site_id == store_site_id_delivery.get(i)) {
                                sp_delivery_site.setSelection(i);
                                break;
                            }
                        }
                    }
                    if (position > 0) {
                        selected_delivery_site_name = store_site_address_delivery.get(position).trim();
                        selected_delivery_site_id = store_site_id_delivery.get(position);
                        Log.d(TAG,"Selected Site :" +selected_delivery_site_id);
                        if (selected_delivery_site_id!=0){
                            for (int i = 0; i < store_site_id_delivery.size(); i++) {
                                if (selected_delivery_site_id == store_site_id_delivery.get(i)) {
                                    sp_delivery_site.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }
                    if (show_method_execute_del_site) {
                        if (given_delivery_site_id_new == 0 && selected_delivery_site_id == 0) {
                            for (int i = 0; i < store_site_id_delivery.size(); i++) {
                                if (given_delivery_site_id == store_site_id_delivery.get(i)) {
                                    sp_delivery_site.setSelection(i);
                                    break;
                                }
                            }
                        } else if (given_delivery_site_id_new != 0 && selected_delivery_site_id == 0) {
                            for (int i = 0; i < store_site_id_delivery.size(); i++) {
                                if (given_delivery_site_id_new == store_site_id_delivery.get(i)) {
                                    sp_delivery_site.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }
                getDeliverySiteID(selected_delivery_site_id);
                break;
            case R.id.sp_materials:
                selected_delivery_site_id = 0;
                if (given_material_id_new == 0) {
                    for (int i = 0; i < store_material_id_list.size(); i++) {
                        if (given_material_id == store_material_id_list.get(i)) {
                            sp_materials.setSelection(i);
                            break;
                        }
                    }
                }
                if (position >= 0) {
                    selected_material_id = store_material_id_list.get(position);
                    selected_material_name = store_material_description_list.get(position).trim();
                    if (selected_material_id!=0){
                        for (int i = 0; i < store_material_id_list.size(); i++) {
                            if (selected_material_id == store_material_id_list.get(i)) {
                                sp_materials.setSelection(i);
                                break;
                            }
                        }
                        Log.d(TAG,"Material iii 1: " +selected_material_id);
                        getMaterialDescription(String.valueOf(selected_material_id));
                        assignMaterialsDeliverySite(selected_material_id);
                        Log.d(TAG,"Selected material :" +selected_material_id+" "+selected_material_name);
                    }
                }
                if (show_method_execute_material) {
                    if (given_material_id_new == 0 && selected_material_id == 0) {
                        for (int i = 0; i < store_material_id_list.size(); i++) {
                            if (given_material_id == store_material_id_list.get(i)) {
                                sp_materials.setSelection(i);
                                break;
                            }
                        }
                        Log.d(TAG,"Material iii 2: " +given_material_id);
                        getMaterialDescription(String.valueOf(given_material_id));
                        assignMaterialsDeliverySite(given_material_id);
                    } else if (given_material_id_new != 0 && selected_material_id == 0) {
                        for (int i = 0; i < store_material_id_list.size(); i++) {
                            if (given_material_id_new == store_material_id_list.get(i)) {
                                sp_materials.setSelection(i);
                                break;
                            }
                        }
                        Log.d(TAG,"Material iii: " +given_material_id_new);
                        getMaterialDescription(String.valueOf(given_material_id_new));
                        assignMaterialsDeliverySite(given_material_id_new);
                    }
                    show_method_execute_material= false;
                }
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
        }
    }

    // get collection site id
    private void getCollectionSiteID(int site_id){
        Query query = siteManagementReference.whereEqualTo("id",site_id).whereEqualTo("site_type","Collection").whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            selected_collection_site_id= Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
                            selected_collection_site_address = queryDocumentSnapshot.getString("address");
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
                Toast.makeText(WasteTransferFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCollectionSiteIdFromAddressManagement(int address_management_id){
        Query query = siteManagementReference.whereEqualTo("id",address_management_id).whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        given_collection_site_id = Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
                    }
                    Log.d(TAG,"Show Collection Id:" +given_collection_site_id);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WasteTransferFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCollectionSiteIdNewFromAddressManagement(int address_management_id){
        Query query = siteManagementReference.whereEqualTo("id",address_management_id).whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        given_collection_site_id_new = Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
                    }
                    Log.d(TAG,"Show Collection Id New:" +given_collection_site_id_new);
                    if (given_collection_site_id_new==0) {
                        for (int i = 0; i < store_site_id_collection.size(); i++) {
                            if (given_collection_site_id == store_site_id_collection.get(i)) {
                                sp_current_site.setSelection(i);
                                break;
                            }
                        }
                    }else {
                        for (int i=0;i<store_site_id_collection.size();i++){
                            if (given_collection_site_id_new == store_site_id_collection.get(i)){
                                Log.d(TAG,"CCC : " +given_collection_site_id_new);
                                sp_current_site.setSelection(i);
                                break;
                            }
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WasteTransferFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(WasteTransferFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void getDeliverySiteIdNewFromAddressManagement(int address_management_id){
        Query query = siteManagementReference.whereEqualTo("id",address_management_id).whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        given_delivery_site_id_new = Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WasteTransferFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        given_delivery_site_id = Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WasteTransferFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateJobManagementDetails(){
        Query query = jobManagementDetailsNewReference.whereEqualTo("pending_task","Yes").whereEqualTo("job_id",store_job_id)
                .whereEqualTo("task_order_no",store_task_order_id).whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                        String doc_id = queryDocumentSnapshot.getId();
                        Map<String,Object> pendingTask = new HashMap<>();
                        pendingTask.put("pending_task","Open");
                        jobManagementDetailsNewReference.document(doc_id).update(pendingTask);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WasteTransferFormDriver.this, "Unable to update task management details.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
