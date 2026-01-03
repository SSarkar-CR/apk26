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

import com.crate.crateam.R;
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
import java.util.Random;
import java.util.Set;

public class SupplyFormDriver extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemSelectedListener {

    private ImageView iv_cross,iv_camera,iv_photo_one,iv_photo_two,iv_close_one,iv_close_two;
    private LinearLayout ll_supply_form_data,ll_sign_supply_form,ll_third_party_sign,ll_container1,ll_container2,ll_container3,ll_gross,ll_tare,ll_unit,ll_net;
    private EditText et_gross_weight,et_tare,et_unit,et_comment;
    private CustomSearchableSpinner sp_current_site,sp_materials,sp_delivery_site;
    private Button bt_ok,bt_cancel_waiting,bt_select_time,bt_print_ticket_yes_update,bt_print_ticket_no_update,bt_discard,bt_driver_sign_first_phase,bt_operator_sign_first_phase,bt_driver_sign,bt_operator_sign,bt_submit,bt_third_party_yes,bt_third_party_no,bt_print_ticket_yes,bt_print_ticket_no,
            bt_size_6y,bt_size_20y,bt_size_30y,bt_size_40y,bt_size_16y,bt_size_8y,bt_size_12y,bt_size_15y, bt_size_18T,bt_weight,bt_unit;
    private LinearLayout ll_driver_sign_first_phase,ll_operator_sign_first_phase,ll_avoid_operator_sign,ll_operator_sign,ll_driver_sign,ll_nfc_tag,ll_images,ll_weight_unit;
    private TextView tv_waiting_time,tv_clear_driver_first_phase,tv_clear_operator_first_phase,tv_ticket_no,tv_date,tv_time,tv_current_site_address, tv_clear_operator,tv_clear_driver,tv_clear_third_party,
            tv_vehicle_registration_no,tv_net_weight,tv_sic_code,tv_operator,tv_material_value_caution;
    private SignaturePad signature_pad_driver_first_phase,signature_pad_operator_first_phase,signaturePadDriver,signaturePadOperator,signaturePadThirdParty;
    private String user_name="",image1,image2,selected_delivery_site_name="",ticket_no="",store_supply_coming_value = "",store_collection_site_address="",date ="",time ="",
            store_delivery_point_address="",store_site_type="",store_material_description="",store_note_type = "",store_material_type="",store_carrier_no="",store_permit_no_collection="",
            store_permit_no_delivery="",first_container_size="",second_container_size="",third_container_size="",driver_sign="",operator_sign="",third_party_signature="",
            print_ticket = "",vehicle_registration_number = "",selected_material_name="",weight_unit ="",deviceMacAddress="",
            haulier_company="",comment = "",haulier_carrier_no="",doc_id="",store_ewc_code="",hour="",minute="",store_collection_site_name="",
            selected_delivery_site_address="",store_project_no="",selected_collection_site_address="",selected_collection_site_name="",vehicle_type_id = "",
            form_edited="No",gross = "", tare = "",unit="",net_weight="",store_material_value="";
    private int selected_position=0,user_role=0,store_delivery_site_id,selected_delivery_site_id=0,selected_material_id = 0,user_id=0,vehicle_number=0,
            store_material_id = 0,store_job_id = 0,store_task_order_id = 0,store_collection_site_id=0,doc_id_length=0,
            given_material_id=0, given_material_id_new = 0, given_delivery_site_id=0,given_delivery_site_id_new=0,given_collection_site_id=0,given_collection_site_id_new=0,
            selected_collection_site_id=0,address_management_id=0;
    private double net = 0.0;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference supplyWasteFormsReference,vehicleDetailsReference,materialReference, sortKeyTableReference,taskManagementReference,
            siteManagementReference,assignMaterialReference,jobManagementDetailsNewReference;
    private boolean hasImage = false,hasImageTwo = false,isSignedDriverFirstPhase=false,isSignedOperatorFirstPhase=false,isSigned = false,isSignedOperator= false,isSignedThird = false,isValidationFirstDone = false,
            isValidationSecondDone = false,isSubmitted = false, isClickedArrivedCollectionBTN = false, show_method_execute_material=false, show_method_execute_del_site=false,
            show_method_execute_collection_site=false;
    private Bitmap getDrawable1, getDrawable2,supply_layout,operator_sign_first_phase;
    private  SharedPreferences sharedPreferences;
    private Intent intent;
    private SharedPreferences.Editor editor;
    private CollectionDeliveryPoint collectionDeliveryPoint;
    private ProgressDialog progressDialog,printerDialog;
    private ArrayList<String> store_material_description_list = new ArrayList<>();
    private ArrayList<Integer> store_material_id_list = new ArrayList<>();
    private ArrayList<String> store_site_address_collection = new ArrayList<>();
    private ArrayList<Integer> store_site_id_collection = new ArrayList<>();
    private ArrayList<Integer> store_site_id_delivery = new ArrayList<>();
    private ArrayList<String> store_site_address_delivery = new ArrayList<>();
    private ArrayList<String> arrayList_hour = new ArrayList<>();
    private ArrayList<String> arrayList_minute = new ArrayList<>();
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

    protected static final String TAG = "TAG";
    private static final int REQUEST_CAMERA= 0;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    BluetoothAdapter mBluetoothAdapter;
    private ZebraPrinter printer;
    private Connection connection;
    private AlertDialog alertDialog_time;
    private RecyclerView rv_hour,rv_minute;
    private HourAdapter hourAdapter;
    private MinuteAdapter minuteAdapter;
    private Uri imageUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.supply_form_driver);
        initView();
    }

    private void initView(){
        FirestoreManager.initPersistentIndexManager();
        supplyWasteFormsReference = db.collection("CR_supply_waste_forms");
        vehicleDetailsReference = db.collection("CR_vehicle_details");
        materialReference = db.collection("CR_material");
        sortKeyTableReference = db.collection("CR_sort_key");
        taskManagementReference = db.collection("CR_task_management");
        siteManagementReference = db.collection("CR_site_management");
        assignMaterialReference = db.collection("CR_assign_material");
        jobManagementDetailsNewReference = db.collection("CR_job_management_new_details");
        SessionManager sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_name = user.get(SessionManager.KEY_FULL_NAME);
        String role = user.get(SessionManager.KEY_ROLE_ONE);
        if (role!=null)
          user_role = Integer.parseInt(role);
        user_id = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        HashMap<String, String> vehicle = sessionManager.getVehicleDetails();
        vehicle_number = Integer.parseInt(Objects.requireNonNull(vehicle.get(SessionManager.KEY_VEHICLE_ID)));
        Log.d("USER_ID : " ,user_name+" "+vehicle_number);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        iv_cross = findViewById(R.id.iv_cross);
        iv_cross.setVisibility(View.GONE);
        iv_camera = findViewById(R.id.iv_camera);
        iv_photo_one = findViewById(R.id.iv_photo_one);
        iv_photo_two = findViewById(R.id.iv_photo_two);
        iv_close_one = findViewById(R.id.iv_close_one);
        iv_close_two = findViewById(R.id.iv_close_two);
        bt_print_ticket_yes_update = findViewById(R.id.bt_print_ticket_yes_update);
        bt_print_ticket_no_update = findViewById(R.id.bt_print_ticket_no_update);
        bt_discard = findViewById(R.id.bt_discard);
        bt_driver_sign_first_phase = findViewById(R.id.bt_driver_sign_first_phase);
        tv_clear_driver_first_phase = findViewById(R.id.tv_clear_driver_first_phase);
        bt_operator_sign_first_phase = findViewById(R.id.bt_operator_sign_first_phase);
        tv_clear_operator_first_phase = findViewById(R.id.tv_clear_operator_first_phase);
        bt_third_party_yes = findViewById(R.id.bt_third_party_yes);
        bt_third_party_no = findViewById(R.id.bt_third_party_no);
        bt_print_ticket_yes = findViewById(R.id.bt_print_ticket_yes);
        bt_print_ticket_no = findViewById(R.id.bt_print_ticket_no);
        bt_driver_sign = findViewById(R.id.bt_driver_sign);
        bt_operator_sign = findViewById(R.id.bt_operator_sign);
        ll_weight_unit = findViewById(R.id.ll_weight_unit);
        bt_weight = findViewById(R.id.bt_weight);
        bt_unit = findViewById(R.id.bt_unit);
        bt_select_time = findViewById(R.id.bt_select_time);
        ll_driver_sign = findViewById(R.id.ll_driver_sign);
        ll_operator_sign = findViewById(R.id.ll_operator_sign);
        ll_driver_sign_first_phase = findViewById(R.id.ll_driver_sign_first_phase);
        ll_operator_sign_first_phase = findViewById(R.id.ll_operator_sign_first_phase);
        ll_avoid_operator_sign = findViewById(R.id.ll_avoid_operator_sign);
        ll_nfc_tag = findViewById(R.id.ll_nfc_tag);
        ll_container1 = findViewById(R.id.ll_container1);
        ll_container2 = findViewById(R.id.ll_container2);
        ll_container3 = findViewById(R.id.ll_container3);
        ll_images = findViewById(R.id.ll_images);
        ll_gross = findViewById(R.id.ll_gross);
        ll_tare = findViewById(R.id.ll_tare);
        ll_net = findViewById(R.id.ll_net);
        ll_unit = findViewById(R.id.ll_unit);
        tv_ticket_no = findViewById(R.id.tv_ticket_no);
        tv_date = findViewById(R.id.tv_date);
        tv_time = findViewById(R.id.tv_time);
        tv_current_site_address = findViewById(R.id.tv_current_site_address);
        tv_clear_driver = findViewById(R.id.tv_clear_driver);
        tv_clear_operator = findViewById(R.id.tv_clear_operator);
        tv_clear_third_party = findViewById(R.id.tv_clear_third_party);
        tv_vehicle_registration_no = findViewById(R.id.tv_vehicle_registration_no);
        tv_net_weight = findViewById(R.id.tv_net_weight);
        tv_waiting_time = findViewById(R.id.tv_waiting_time);
        tv_material_value_caution = findViewById(R.id.tv_material_value_caution);
        signature_pad_driver_first_phase = findViewById(R.id.signature_pad_driver_first_phase);
        signature_pad_operator_first_phase = findViewById(R.id.signature_pad_operator_first_phase);
        signaturePadDriver = findViewById(R.id.signature_pad_driver);
        signaturePadOperator = findViewById(R.id.signature_pad_operator);
        signaturePadThirdParty = findViewById(R.id.signature_pad_third_party);
        ll_supply_form_data = findViewById(R.id.ll_supply_form_data);
        ll_sign_supply_form = findViewById(R.id.ll_sign_supply_form);
        ll_third_party_sign = findViewById(R.id.ll_third_party_sign);
        bt_submit = findViewById(R.id.bt_submit);
        bt_size_16y = findViewById(R.id.bt_size_16y);
        bt_size_20y = findViewById(R.id.bt_size_20y);
        bt_size_30y = findViewById(R.id.bt_size_30y);
        bt_size_40y = findViewById(R.id.bt_size_40y);
        bt_size_6y = findViewById(R.id.bt_size_6y);
        bt_size_8y = findViewById(R.id.bt_size_8y);
        bt_size_12y = findViewById(R.id.bt_size_12y);
        bt_size_15y = findViewById(R.id.bt_size_15y);
        bt_size_18T = findViewById(R.id.bt_size_18T);
        et_gross_weight = findViewById(R.id.et_gross_weight);
        et_tare = findViewById(R.id.et_tare);
        et_unit = findViewById(R.id.et_unit);
        et_comment = findViewById(R.id.et_comment);
        tv_sic_code = findViewById(R.id.tv_sic_code);
        tv_operator = findViewById(R.id.tv_operator);
        sp_materials = findViewById(R.id.sp_materials);
        sp_current_site = findViewById(R.id.sp_current_site);
        sp_delivery_site = findViewById(R.id.sp_delivery_site);
        progressDialog = Dialog.showProgressDialog(this);
        sharedPreferences = getApplicationContext().getSharedPreferences("MyPref",0);
        editor = sharedPreferences.edit();
        store_supply_coming_value = sharedPreferences.getString("supply_coming_form",null);
        isClickedArrivedCollectionBTN= sharedPreferences.getBoolean("arrivedCollectionClicked",false);
        Log.e("ss12345",String.valueOf(isClickedArrivedCollectionBTN));
        isSubmitted = sharedPreferences.getBoolean("isSubmitted", false);
        Log.e("ss123456",String.valueOf(isSubmitted)+" "+store_supply_coming_value);
        if (store_supply_coming_value.equals("ArrivedCollectionPoint") || store_supply_coming_value.equals("LeftCollectionPoint") ||
                store_supply_coming_value.equals("ArrivedDeliveryPoint") || store_supply_coming_value.equals("FinishTask")){
            ll_supply_form_data.setVisibility(View.VISIBLE);
            ll_sign_supply_form.setVisibility(View.GONE);
        }
        else {
            ll_sign_supply_form.setVisibility(View.VISIBLE);
            ll_supply_form_data.setVisibility(View.GONE);
        }
        date = AppData.date();
time = AppData.Time();
        getDataFromCollectionDelivery();
        fetchVehicleRegistrationNumber();
        initializeOnClick();
        setSignDriverFirstPhase();
        setSignOperatorFirstPhase();
        setSignDriver();
        setSignOperator();
        setSignaturePadThirdParty();
        showNetValueFromGross();
        showNetValueFromTare();
        getMaximumLength();
    }

    private String generateTicket(){
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");
        String currentDate = sdf.format(new Date());
        String ticket_no =  "S"+currentDate+String.format("%06d", number);
        Log.d("Ticket No:" ,ticket_no);
        return ticket_no;
    }

    private void getDataFromCollectionDelivery(){
        collectionDeliveryPoint = new CollectionDeliveryPoint(SupplyFormDriver.this,user_id,vehicle_number);
        collectionDeliveryPoint.enableOfflineSupportCommon();
        collectionDeliveryPoint.jobManagement();
        CollectionDeliveryPoint.startTask = new StartTask() {
            @Override
            public void getDetails(int collection_site_id,String collection_site_name,String collection_address, String delivery_address,int delivery_site_id, int job_id, int task_order_id, int material_id,String material_description,
                                   String ewc_code,String material_value,String note_type, String sic_code,String carrier_no,String permit_no_collection,String permit_no_delivery,String site_type,String material_type,String project_no) {
                store_collection_site_id = collection_site_id;
                store_collection_site_name = collection_site_name;
                store_collection_site_address = collection_address;
                store_delivery_point_address = delivery_address;
                store_delivery_site_id = delivery_site_id;
                store_job_id = job_id;
                store_task_order_id = task_order_id;
                store_material_id = material_id;
                store_material_description = material_description;
                store_ewc_code = ewc_code;
                store_material_value = material_value;
                if (store_material_value.equals("High"))
                    tv_material_value_caution.setVisibility(View.VISIBLE);
                else
                    tv_material_value_caution.setVisibility(View.GONE);
                store_note_type = note_type;
                store_material_type = material_type;
                store_project_no=project_no;
                store_carrier_no = carrier_no;
                store_permit_no_collection = permit_no_collection;
                store_permit_no_delivery = permit_no_delivery;
                store_site_type = site_type;
                collectionSiteAddressManagement();
                tv_current_site_address.setText(store_collection_site_address);
                sp_current_site.setPrompt(store_collection_site_address);
                sp_delivery_site.setPrompt(store_delivery_point_address);
                getMaterialDetails(store_collection_site_id);
                getCollectionSiteIdFromAddressManagement(store_collection_site_id);
                getDeliverySiteIdFromAddressManagement(store_delivery_site_id);
                assignMaterialsDeliverySite(store_material_id);
                Log.d("STORE EWC :" ,store_ewc_code+"  "+store_material_id+" "+store_supply_coming_value
                        +" "+store_collection_site_id);
                if (store_supply_coming_value.equals("ArrivedCollectionPoint") && isSubmitted)
                    showSupplyFormData();
                if ( store_supply_coming_value.equals("LeftCollectionPoint") || store_supply_coming_value.equals("ArrivedDeliveryPoint")
                        || store_supply_coming_value.equals("ArrivedDeliveryPoint_SubmitForm")|| store_supply_coming_value.equals("FinishTask"))
                         showSupplyFormData();
                else {
                    tv_operator.setText(user_name);
                    if (ticket_no.equals("")) tv_ticket_no.setText(generateTicket());
                    else tv_ticket_no.setText(ticket_no);
                }
                sendSupplyFromPrintData();
            }
        };
    }

    // get material collection point
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
                            address_management_id= Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
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
                Toast.makeText(SupplyFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void assignMaterial(int collection_site_id){
        Query query = assignMaterialReference.whereEqualTo("site_id",collection_site_id).whereEqualTo("status","Active");
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
                        Log.d("Material id list :" , String.valueOf(store_material_id_list));
                        if (!store_material_id_list.isEmpty())
                            material();
                    }
                    else {
                        progressDialog.dismiss();
                        Dialog.alertDialog(SupplyFormDriver.this,"No material assigned with this site.");
                        store_material_id_list.clear();
                        store_material_description_list.clear();
                        material_desc_doc_id.clear();
                        sp_materials.setAdapter(null);
                    }
                }
                else {
                    progressDialog.dismiss();
                    Dialog.alertDialogToDashBoard(SupplyFormDriver.this,"Unable to get task details.");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SupplyFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            setMaterialAdapter(material_id);
                        }
                        if (show_method_execute_material) { //  && !store_ewc_code.isEmpty()
                            if (given_material_id_new == 0) {
                                for (int i = 0; i < store_material_id_list.size(); i++) {
                                    if (given_material_id == store_material_id_list.get(i)) {
                                        material_id = i;
                                    }
                                }
                            } else {
                                for (int i = 0; i < store_material_id_list.size(); i++) {
                                    if (given_material_id_new == store_material_id_list.get(i)) {
                                        material_id = i;
                                    }
                                }
                            }
                            setMaterialAdapter(material_id);
                        }
                    }
                    else {
                        progressDialog.dismiss();
                        Dialog.alertDialog(SupplyFormDriver.this,"Unable to get task details.");
                    }
                }
                else {
                    progressDialog.dismiss();
                    Dialog.alertDialog(SupplyFormDriver.this,"Unable to get task details.");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SupplyFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setMaterialAdapter(int material_id){
        Log.d("MMMM :" , String.valueOf(material_id));
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_custom_layout, material_description_list);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        Log.d("Material ids:" , String.valueOf(store_material_id_list));
        Log.d("Material des:" , String.valueOf(material_description_list));
        sp_materials.setAdapter(spinnerArrayAdapter);
        sp_materials.setSelection(material_id);
    }



    private void initializeOnClick(){
        iv_cross.setOnClickListener(this);
        bt_print_ticket_yes_update.setOnClickListener(this);
        bt_print_ticket_no_update.setOnClickListener(this);
        bt_discard.setOnClickListener(this);
        bt_driver_sign_first_phase.setOnClickListener(this);
        tv_clear_driver_first_phase.setOnClickListener(this);
        bt_operator_sign_first_phase.setOnClickListener(this);
        tv_clear_operator_first_phase.setOnClickListener(this);
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
        bt_submit.setOnClickListener(this);
        bt_third_party_yes.setOnClickListener(this);
        bt_third_party_no.setOnClickListener(this);
        bt_print_ticket_yes.setOnClickListener(this);
        bt_print_ticket_no.setOnClickListener(this);
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
        bt_weight.setOnClickListener(this);
        bt_unit.setOnClickListener(this);
        bt_select_time.setOnClickListener(this);
        sp_current_site.setOnItemSelectedListener(this);
        sp_materials.setOnItemSelectedListener(this);
        sp_delivery_site.setOnItemSelectedListener(this);
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
            case R.id.bt_driver_sign_first_phase:
                ll_driver_sign_first_phase.setVisibility(View.VISIBLE);
                bt_driver_sign_first_phase.setVisibility(View.GONE);
                break;
            case R.id.tv_clear_driver_first_phase:
                signature_pad_driver_first_phase.clear();
                Toast.makeText(this,"Sign Cleared",Toast.LENGTH_SHORT).show();
                break;
            case R.id.bt_driver_sign:
                ll_driver_sign.setVisibility(View.VISIBLE);
                bt_driver_sign.setVisibility(View.GONE);
                break;
            case R.id.bt_operator_sign_first_phase:
                ll_operator_sign_first_phase.setVisibility(View.VISIBLE);
                bt_operator_sign_first_phase.setVisibility(View.GONE);
                break;
            case R.id.tv_clear_operator_first_phase:
                signature_pad_operator_first_phase.clear();
                Toast.makeText(this,"Sign Cleared",Toast.LENGTH_SHORT).show();
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
                if (checkValidationSecond()){
                    bt_print_ticket_no.setEnabled(false);
                    bt_discard.setVisibility(View.GONE);
                    iv_cross.setVisibility(View.GONE);
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
                /*only for first part layout*/
            case R.id.bt_print_ticket_yes_update:
                print_ticket = "Yes";
                if (checkValidationFirst()) {
                    bt_print_ticket_no_update.setEnabled(false);
                    bt_discard.setVisibility(View.GONE);
                    iv_cross.setVisibility(View.GONE);
                    setSelectedButtonBackground(bt_print_ticket_yes_update);
                    setDefaultButtonBackground(bt_print_ticket_no_update);
                    enableBluetooth();
                }
                break;
            case R.id.bt_print_ticket_no_update:
                setSelectedButtonBackground(bt_print_ticket_no_update);
                setDefaultButtonBackground(bt_print_ticket_yes_update);
                print_ticket = "No";
                bt_discard.setVisibility(View.VISIBLE);
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
            case R.id.bt_submit:
                editor.putBoolean("isSubmitted", true);
                editor.apply();
                    if (store_supply_coming_value.equals("ArrivedCollectionPoint")){
                        if (checkValidationFirst()) {
                            updateJobManagementDetails();
                            if (!isClickedArrivedCollectionBTN && !isSubmitted) {
                                storeSupplyFormDate();
                                if (!AppData.internetOnline(SupplyFormDriver.this))
                                progressDialog.dismiss();
                                intent = new Intent(SupplyFormDriver.this, com.crate.crateam.activities.ArrivedCollectionPoint.class);
                                startActivity(intent);
                                finish();
                                if (!AppData.internetOnline(SupplyFormDriver.this)) {
                                    progressDialog.dismiss();
                                }
                            } else if (!isClickedArrivedCollectionBTN) {
                                updateSupplyFormDriver();
                                if (!AppData.internetOnline(this)) {
                                    progressDialog.dismiss();
                                }
                            } else if (!isSubmitted){
                                storeSupplyFormDate();
                                if (!AppData.internetOnline(SupplyFormDriver.this))
                                    progressDialog.dismiss();
                                intent = new Intent(SupplyFormDriver.this, com.crate.crateam.activities.LeftCollectionPoint.class);
                                finish();
                                startActivity(intent);
                                if (!AppData.internetOnline(SupplyFormDriver.this)) {
                                    progressDialog.dismiss();
                                }
                            } else {
                                storeSupplyFormDate();
                                if (!AppData.internetOnline(SupplyFormDriver.this))
                                    progressDialog.dismiss();
                                intent = new Intent(SupplyFormDriver.this, com.crate.crateam.activities.LeftCollectionPoint.class);
                                startActivity(intent);
                                finish();
                                if (!AppData.internetOnline(SupplyFormDriver.this)) {
                                    progressDialog.dismiss();
                                }
                            }
                        }
                    }
                    else if (store_supply_coming_value.equals("LeftCollectionPoint")){
                        if (checkValidationFirst()) {
                            updateSupplyFormDriver();
                            intent = new Intent(SupplyFormDriver.this, com.crate.crateam.activities.LeftCollectionPoint.class);
                            startActivity(intent);
                            finish();
                            if (!AppData.internetOnline(this)) {
                                progressDialog.dismiss();
                            }
                        }
                    }
                    else if (store_supply_coming_value.equals("ArrivedDeliveryPoint")){
                        if (checkValidationFirst()){
                            updateSupplyFormDriver();
                            intent = new Intent(SupplyFormDriver.this, com.crate.crateam.activities.ArrivedDeliveryPoint.class);
                            startActivity(intent);
                            finish();
                            if (!AppData.internetOnline(this)) {
                                progressDialog.dismiss();
                            }
                        }
                    }
                    else if (store_supply_coming_value.equals("ArrivedDeliveryPoint_SubmitForm")){
                        editor.putBoolean("isSubmitSupply", true);
                        editor.apply();
                        if (checkValidationSecond()) {
                            updateSignature();
                            intent = new Intent(SupplyFormDriver.this, com.crate.crateam.activities.FinishTaskActivity.class);
                            startActivity(intent);
                            finish();
                            if (!AppData.internetOnline(this)) {
                                progressDialog.dismiss();
                            }
                        }
                    }
                    else if (store_supply_coming_value.equals("FinishTask")){
                        if (checkValidationFirst()){
                            updateSupplyFormDriver();
                            intent = new Intent(SupplyFormDriver.this, com.crate.crateam.activities.FinishTaskActivity.class);
                            startActivity(intent);
                            finish();
                            if (!AppData.internetOnline(this)) {
                                progressDialog.dismiss();
                            }
                        }
                    }
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
                setSelectedButtonBackground(bt_size_18T);
                break;
            case R.id.bt_weight:
                weightVisible();
                break;
            case R.id.bt_unit:
                unitVisible();
                break;
            case R.id.bt_select_time:
                customizeWaitingTime();
                break;
            case R.id.bt_ok:
                if (hour.equals(""))
                    Dialog.alertDialog(this,"Please select hour value.");
                else if (minute.equals(""))
                    Dialog.alertDialog(this,"Please select minute value.");
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
        }
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
                    Toast.makeText(SupplyFormDriver.this, "Message", Toast.LENGTH_SHORT).show();
                }
                break;
        }
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
    private void setSignDriverFirstPhase(){
        signature_pad_driver_first_phase.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
                isSignedDriverFirstPhase = true;
            }
            @Override
            public void onSigned() {
                isSignedDriverFirstPhase = true;
            }
            @Override
            public void onClear() {
                isSignedDriverFirstPhase = false;
            }
        });
    }
    private void setSignOperatorFirstPhase(){
        signature_pad_operator_first_phase.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
                isSignedOperatorFirstPhase = true;
            }
            @Override
            public void onSigned() {
                isSignedOperatorFirstPhase = true;
            }
            @Override
            public void onClear() {
                isSignedOperatorFirstPhase = false;
            }
        });
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

    private void storeSupplyFormDate(){
            String operator_name = tv_operator.getText().toString();
            String gross_weight = et_gross_weight.getText().toString().trim();
            String tare = et_tare.getText().toString().trim();
            date = AppData.date();
time = AppData.Time();
            sendSignatureAndImage();
            if (isSignedDriverFirstPhase) {
                Bitmap signatureBitmap = signature_pad_driver_first_phase.getSignatureBitmap();
                driver_sign = AppData.convertTOBase64Image(signatureBitmap);
            }
            if (isSignedOperatorFirstPhase){
                Bitmap signatureBitmapOperator = signature_pad_operator_first_phase.getSignatureBitmap();
                operator_sign = AppData.convertTOBase64Image(signatureBitmapOperator);
            }
            sendSupplyForm(date,time,gross_weight,tare,image1,image2,driver_sign,operator_sign,third_party_signature,operator_name);
    }

    private void sendSignatureAndImage(){
        if (image1==null)
            image1="";
        if (image2==null)
            image2="";
        if (isSigned) {
            Bitmap signatureBitmap = signaturePadDriver.getSignatureBitmap();
            driver_sign = AppData.convertTOBase64Image(signatureBitmap);
        }
        if (isSignedOperator){
            Bitmap signatureBitmap_operator = signaturePadOperator.getSignatureBitmap();
            operator_sign = AppData.convertTOBase64Image(signatureBitmap_operator);
        }
        if (!isSigned)
            driver_sign = "No";
        if (isSignedThird) {
            Bitmap signatureBitmapThirdParty = signaturePadThirdParty.getSignatureBitmap();
            third_party_signature = AppData.convertTOBase64Image(signatureBitmapThirdParty);
        }
    }

    private Boolean checkValidationFirst(){
        String weight_unit_container = "";
        if (vehicle_type_id.equals("N1") || vehicle_type_id.equals("N2") || vehicle_type_id.equals("N3")){
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
        else if (!isSignedDriverFirstPhase)
             Dialog.alertDialog(this,"Driver sign mandatory.");
        else if (print_ticket.equals(""))
            Dialog.alertDialog(this,"Please select print ticket option.");
        else
            isValidationFirstDone = true;
            return isValidationFirstDone;
    }

    private boolean checkValidationSecond(){
        if (!isSigned)
            Dialog.alertDialog(this,"Driver sign mandatory.");
        else if (!isSignedOperator)
            Dialog.alertDialog(this,"Operator sign mandatory.");
        else if (third_party_signature.equals(""))
            Dialog.alertDialog(this,"Please select third party signature option.");
        else if (third_party_signature.equals("Yes")&& !isSignedThird)
            Dialog.alertDialog(this,"Please sign the form by third party.");
        else if (print_ticket.equals(""))
            Dialog.alertDialog(this,"Please select print ticket option.");
        else
            isValidationSecondDone = true;
        return isValidationSecondDone;
    }

    private void sendSupplyForm(String date,String time,String gross_weight,String tare,String image1,String image2,String driver_sign,String operator_sign,String third_party_signature,String operator_name){
        ticket_no = tv_ticket_no.getText().toString();
        comment = et_comment.getText().toString().trim();
        Log.d("EWC CODE :" ,store_ewc_code);
        Map<String,Object> mapSupplyDriver = new HashMap<>();
        mapSupplyDriver.put("user_id",user_id);
        mapSupplyDriver.put("user_role_id",user_role);
        mapSupplyDriver.put("operator",operator_name);
        mapSupplyDriver.put("ticket_no",ticket_no);
        mapSupplyDriver.put("current_site_id",store_collection_site_id);
        mapSupplyDriver.put("current_site_address",store_collection_site_address);
        mapSupplyDriver.put("material_id",store_material_id);
        mapSupplyDriver.put("material_value",store_material_value);
        mapSupplyDriver.put("delivery_site_id",store_delivery_site_id);
        mapSupplyDriver.put("delivery_reference","");
        mapSupplyDriver.put("delivery_location","");
        mapSupplyDriver.put("container_type","");
        mapSupplyDriver.put("vehicle_id",vehicle_number);
        mapSupplyDriver.put("waste_carrier_no",haulier_carrier_no);
        mapSupplyDriver.put("project_no",store_project_no);
        mapSupplyDriver.put("submission_date",date);
        mapSupplyDriver.put("submission_time",time);
        mapSupplyDriver.put("z_driver_sign",driver_sign);
        mapSupplyDriver.put("z_operator_sign",operator_sign);
        mapSupplyDriver.put("z_third_party_sign",third_party_signature);
        mapSupplyDriver.put("print_ticket",print_ticket);
        mapSupplyDriver.put("job_id",store_job_id);
        mapSupplyDriver.put("task_order_id",store_task_order_id);
        mapSupplyDriver.put("z_image_one",image1);
        mapSupplyDriver.put("z_image_two",image2);
        mapSupplyDriver.put("sic_code","");
        if (selected_collection_site_id == store_collection_site_id)
            mapSupplyDriver.put("current_site_id_new",0);
        else {
            mapSupplyDriver.put("current_site_id_new", selected_collection_site_id);
            form_edited ="Yes";
        }
        if (selected_delivery_site_id == store_delivery_site_id)
            mapSupplyDriver.put("delivery_site_id_new",0);
        else {
            mapSupplyDriver.put("delivery_site_id_new", selected_delivery_site_id);
            form_edited ="Yes";
        }
        if (selected_material_id==store_material_id)
            mapSupplyDriver.put("material_id_new", 0);
        else {
            mapSupplyDriver.put("material_id_new", selected_material_id);
            form_edited ="Yes";
        }
        mapSupplyDriver.put("ewc_code",store_ewc_code);
        mapSupplyDriver.put("ewc_available","Yes");
        mapSupplyDriver.put("vehicle_type",vehicle_type_id);
        mapSupplyDriver.put("vehicle_type_new","");
        mapSupplyDriver.put("haulier_id",haulier_company);
        mapSupplyDriver.put("haulier_id_new","");
        mapSupplyDriver.put("waste_carrier_no_new","");
        if (vehicle_type_id.equals("N1") || vehicle_type_id.equals("N2") || vehicle_type_id.equals("N3")){
            mapSupplyDriver.put("gross_weight", gross_weight);
            mapSupplyDriver.put("tare", tare);
            mapSupplyDriver.put("net", String.valueOf(net));
            mapSupplyDriver.put("unit", et_unit.getText().toString().trim());
            mapSupplyDriver.put("container_size_one", "");
            mapSupplyDriver.put("container_size_two", "");
            mapSupplyDriver.put("container_size_three","");
        }else {
            if (selected_position!=0){
                if (selected_position<=4){
                    mapSupplyDriver.put("container_size_one", first_container_size);
                    mapSupplyDriver.put("container_size_two", "");
                }
                else {
                    mapSupplyDriver.put("container_size_one", "");
                    mapSupplyDriver.put("container_size_two", second_container_size);
                }
            }
            else {
                mapSupplyDriver.put("container_size_one", first_container_size);
                mapSupplyDriver.put("container_size_two", second_container_size);
            }
            mapSupplyDriver.put("container_size_three", third_container_size);
            mapSupplyDriver.put("gross_weight","");
            mapSupplyDriver.put("tare","");
            mapSupplyDriver.put("net","");
            mapSupplyDriver.put("unit","");
        }
        mapSupplyDriver.put("add_comment",comment);
        mapSupplyDriver.put("permit_no_collection",store_permit_no_collection);
        mapSupplyDriver.put("permit_no_delivery",store_permit_no_delivery);
        mapSupplyDriver.put("permit_no","") ;
        mapSupplyDriver.put("waiting_time",tv_waiting_time.getText().toString().trim());
        mapSupplyDriver.put("new_driver_id",0);
        mapSupplyDriver.put("new_driver_name","");
        mapSupplyDriver.put("registration_no_new","");
        mapSupplyDriver.put("registration_no",vehicle_registration_number);
        mapSupplyDriver.put("note_type",store_note_type);
        mapSupplyDriver.put("external_ticket_no","");
        mapSupplyDriver.put("id",ticket_no);
        mapSupplyDriver.put("status","Active");
        doc_id_length = doc_id_length+1;
        mapSupplyDriver.put("sort_key",doc_id_length);
        mapSupplyDriver.put("backend_edited","No");
        mapSupplyDriver.put("is_updated","Yes");
        mapSupplyDriver.put("time_second_format", AppData.getTimeSecond());
        mapSupplyDriver.put("ticket_status", "");

        supplyWasteFormsReference.document(ticket_no).set(mapSupplyDriver).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Map<String,Object> objectMap_update = new HashMap<>();
                    objectMap_update.put("supply_waste_key",doc_id_length);
                    sortKeyTableReference.document(doc_id).update(objectMap_update);
                    updateTaskManagement(store_job_id,store_task_order_id);
                    Log.d("All Address:" ,selected_collection_site_address+" "+selected_collection_site_id+" "+
                            selected_delivery_site_address+"  "+selected_delivery_site_id);
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
                    Log.d("Success :" , "Form submitted successfully.");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Error:" , "Form submission failed.");
            }
        });
    }
    // displaying data when edit the supply_form_driver table
    private void showSupplyFormData(){
        progressDialog.show();
        Query query = supplyWasteFormsReference.whereEqualTo("job_id",store_job_id).whereEqualTo("task_order_id",store_task_order_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    date = AppData.date();
time = AppData.Time();
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        if (documentSnapshot != null) {
                            ticket_no = documentSnapshot.getString("ticket_no");
                            Log.d("Ticket :" , Objects.requireNonNull(ticket_no));
                            tv_ticket_no.setText(ticket_no);
                            String address = documentSnapshot.getString("current_site_address");
                            tv_current_site_address.setText(address);
                            tv_operator.setText(user_name);
                            String vehicle_registration_no = documentSnapshot.getString("registration_no");
                            tv_vehicle_registration_no.setText(vehicle_registration_no);
                            store_material_value = documentSnapshot.getString("material_value");
                            if (store_material_value.equals("High"))
                                tv_material_value_caution.setVisibility(View.VISIBLE);
                            else
                                tv_material_value_caution.setVisibility(View.GONE);
                            unit = documentSnapshot.getString("unit");
                            if (vehicle_type_id != null) {
                                if (vehicle_type_id.equals("N1") || vehicle_type_id.equals("N2") || vehicle_type_id.equals("N3")){
                                    if (unit.equals("")) {
                                        Log.e("weight","weight");
                                        weightVisible();
                                        gross = documentSnapshot.getString("gross_weight");
                                        et_gross_weight.setText(gross);
                                        tare = documentSnapshot.getString("tare");
                                        et_tare.setText(tare);
                                        net_weight = documentSnapshot.getString("net");
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
                            int show_collection_site_id_new = documentSnapshot.getLong("current_site_id_new").intValue();
                            getCollectionSiteIdNewFromAddressManagement(show_collection_site_id_new);
                            int show_collection_site_id = documentSnapshot.getLong("current_site_id").intValue();
                            getCollectionSiteIdFromAddressManagement(show_collection_site_id);
                            Log.d("Show Collection Id Old & New:" ,show_collection_site_id+" "+show_collection_site_id_new);
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
                                        Log.d("MID : " , String.valueOf(given_material_id_new));
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
                                        Log.d("DID : " , String.valueOf(given_delivery_site_id_new));
                                        sp_delivery_site.setSelection(i);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    Log.d("Collection Material Delivery :" ,given_collection_site_id_new +" "+given_delivery_site_id_new+" "+given_material_id_new);
                    selectedContainerSize(first_container_size, second_container_size, third_container_size);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SupplyFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendSupplyFromPrintData(){
        Query query = supplyWasteFormsReference.whereEqualTo("job_id",store_job_id).whereEqualTo("task_order_id",store_task_order_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        if (documentSnapshot != null) {
                            ticket_no = documentSnapshot.getString("ticket_no");
                            comment = documentSnapshot.getString("add_comment");
                            selected_material_id = Objects.requireNonNull(documentSnapshot.getLong("material_id_new")).intValue();
                            if (selected_material_id==0)
                                selected_material_id = Objects.requireNonNull(documentSnapshot.getLong("material_id")).intValue();
                            selected_delivery_site_id = Objects.requireNonNull(documentSnapshot.getLong("delivery_site_id_new")).intValue();
                            if (selected_delivery_site_id == 0)
                                selected_delivery_site_id = Objects.requireNonNull(documentSnapshot.getLong("delivery_site_id")).intValue();
                            getMaterialDescription(String.valueOf(selected_material_id));
                            getDeliverySiteAddress(String.valueOf(selected_delivery_site_id));
                            Log.d("Print Data:" , ticket_no +" "+comment+" "+selected_material_name+" "+selected_delivery_site_address);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        selected_material_name = document.getString("material_description");
                        store_ewc_code = document.getString("ewc_code");
                        store_material_value = document.getString("material_value");
                    }
                    if (store_material_value.equals("High"))
                        tv_material_value_caution.setVisibility(View.VISIBLE);
                    else
                        tv_material_value_caution.setVisibility(View.GONE);
                } else {
                    Log.d(TAG, "Failed to fetch new material name.", task.getException());
                }
            }
        });
    }

    private void getDeliverySiteAddress(String id){
        siteManagementReference.document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        selected_delivery_site_address = document.getString("address");
                    }
                } else {
                    Log.d(TAG, "Failed to fetch delivery address : ", task.getException());
                }
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
            first_container_size ="";
            third_container_size="";
            invisibleWeightUnit();
        }else if (vehicle_type_id.equals("8w Roro")){
            ll_container1.setVisibility(View.VISIBLE);
            ll_container2.setVisibility(View.GONE);
            ll_container3.setVisibility(View.GONE);
            second_container_size="";
            third_container_size="";
            invisibleWeightUnit();
        } else if (vehicle_type_id.equals("8w Tipper")){
            ll_container1.setVisibility(View.GONE);
            ll_container2.setVisibility(View.GONE);
            ll_container3.setVisibility(View.GONE);
            first_container_size ="";
            second_container_size="";
            third_container_size="";
            invisibleWeightUnit();
        } else if (vehicle_type_id.equals("N1")||vehicle_type_id.equals("N2")||vehicle_type_id.equals("N3")){
            visibleWeightUnit();
        }else {
            ll_container1.setVisibility(View.VISIBLE);
            ll_container2.setVisibility(View.VISIBLE);
            ll_container3.setVisibility(View.VISIBLE);
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
    //only for when three layout container size is visible
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
                            haulier_carrier_no = documentSnapshot.getString("haulier_carrier_no");
                            vehicle_type_id = documentSnapshot.getString("vehicle_type");
                        }
                    }
                    tv_vehicle_registration_no.setText(vehicle_registration_number);
                    containerSize(vehicle_type_id);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SupplyFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSupplyFormDriver(){
        progressDialog.show();
        Query query = supplyWasteFormsReference.whereEqualTo("job_id",store_job_id).whereEqualTo("task_order_id",store_task_order_id)
                .whereEqualTo("ticket_no",tv_ticket_no.getText().toString());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()) {
                        if (queryDocumentSnapshot.exists()) {
                            String doc_id = queryDocumentSnapshot.getId();
                            String gross_weight = et_gross_weight.getText().toString().trim();
                            String tare = et_tare.getText().toString().trim();

                            if (isSignedDriverFirstPhase) {
                                Bitmap signatureBitmap = signature_pad_driver_first_phase.getSignatureBitmap();
                                driver_sign = AppData.convertTOBase64Image(signatureBitmap);
                            }
                            if (isSignedOperatorFirstPhase){
                                Bitmap signatureBitmapOperator = signature_pad_operator_first_phase.getSignatureBitmap();
                                operator_sign = AppData.convertTOBase64Image(signatureBitmapOperator);
                            }
                            Map<String, Object> objectMapUpdate = new HashMap<>();
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
                            objectMapUpdate.put("print_ticket",print_ticket);
                            objectMapUpdate.put("z_operator_sign",operator_sign);
                            objectMapUpdate.put("z_driver_sign",driver_sign);
                            objectMapUpdate.put("time_second_format", AppData.getTimeSecond());
                            if (selected_collection_site_id==store_collection_site_id) {
                                getCollectionSiteID(store_collection_site_id);
                                objectMapUpdate.put("current_site_id_new", 0);
                                Log.d("collection_site_address :" ,store_collection_site_address);
                                updateCollectionTaskManagement(store_collection_site_address);
                            }else {
                                getCollectionSiteID(selected_collection_site_id);
                                objectMapUpdate.put("current_site_id_new", selected_collection_site_id);
                                Log.d("collection_site_address :" ,selected_collection_site_address);
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
                            if (!et_comment.getText().toString().trim().isEmpty()) {
                                comment = et_comment.getText().toString().trim();
                                objectMapUpdate.put("add_comment", comment);
                            }
                            if (!tv_waiting_time.getText().toString().trim().isEmpty())
                                objectMapUpdate.put("waiting_time", tv_waiting_time.getText().toString().trim());
                            supplyWasteFormsReference.document(doc_id).update(objectMapUpdate);
                            Log.d("site ids :" ,selected_collection_site_address+" "+selected_collection_site_id+" "+
                                    selected_delivery_site_address+"  "+selected_delivery_site_id);
                            if (selected_collection_site_id != store_collection_site_id || selected_delivery_site_id != store_delivery_site_id || selected_material_id!=store_material_id) {
                                Map<String, Object> task_management_update1 = new HashMap<>();
                                task_management_update1.put("form_edited", "Yes");
                                task_management_update1.put("is_updated","Yes");
                                taskManagementReference.document(store_job_id+"_"+store_task_order_id).update(task_management_update1);
                            }
                        }
                        finish();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SupplyFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void updateSignature(){
        Query query = supplyWasteFormsReference.whereEqualTo("job_id",store_job_id).whereEqualTo("task_order_id",store_task_order_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    sendSignatureAndImage();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        String doc_id = queryDocumentSnapshot.getId();
                        Map<String, Object> objectMapSignature = new HashMap<>();
                        if (!image1.isEmpty())
                          objectMapSignature.put("z_image_one", image1);
                        if (!image2.isEmpty())
                           objectMapSignature.put("z_image_two", image2);
                        objectMapSignature.put("z_operator_sign", operator_sign);
                        objectMapSignature.put("z_driver_sign", driver_sign);
                        objectMapSignature.put("z_third_party_sign", third_party_signature);
                        objectMapSignature.put("print_ticket", print_ticket);
                        objectMapSignature.put("is_updated","Yes");
                        objectMapSignature.put("time_second_format", AppData.getTimeSecond());
                        supplyWasteFormsReference.document(doc_id).update(objectMapSignature);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enableBluetooth(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(SupplyFormDriver.this, "Message1", Toast.LENGTH_SHORT).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                if (ContextCompat.checkSelfPermission(SupplyFormDriver.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(SupplyFormDriver.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
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
        if (ContextCompat.checkSelfPermission(SupplyFormDriver.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(SupplyFormDriver.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
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
            Intent connectIntent = new Intent(SupplyFormDriver.this,
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
        try {
            if (ContextCompat.checkSelfPermission(SupplyFormDriver.this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(SupplyFormDriver.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
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
            if (connection != null) connection.close();
            if (printerDialog!=null) printerDialog.dismiss();
        } catch (ConnectionException e) {
        }
    }

    private void doConnectionTest() {
        printer = connect();
        if (printer!= null) sendTestLabel();
        else disconnect();
    }

    private String getMacAddress() {
        return deviceMacAddress;
    }

    private byte[] getConfigLabel() {
        byte[] configLabel = null;
        try {
            PrinterLanguage printerLanguage = printer.getPrinterControlLanguage();
            SGD.SET("device.languages", "zpl", connection);
            if (selected_collection_site_id==0){
                selected_collection_site_address = store_collection_site_address;
                selected_collection_site_name = store_collection_site_name;
            }
            if (selected_delivery_site_id==0) selected_delivery_site_address = store_delivery_point_address;
            if (selected_material_id==0) selected_material_name = store_material_description;

            if (isSignedDriverFirstPhase) {
                if (isSignedOperatorFirstPhase)
                    operator_sign_first_phase = signature_pad_operator_first_phase.getSignatureBitmap();
                else
                    operator_sign_first_phase = Bitmap.createBitmap(0, 0, Bitmap.Config.ARGB_8888);
                supply_layout = new PrintingLayoutFormat(this).supplyFormLayoutToBitmap(tv_ticket_no.getText().toString(), et_comment.getText().toString().trim(), user_name,selected_collection_site_name,selected_delivery_site_name,
                        selected_collection_site_address, selected_delivery_site_address,"","", haulier_company, user_name, haulier_carrier_no, date + " " + time, vehicle_registration_number, selected_material_name,
                        et_unit.getText().toString().trim(),et_gross_weight.getText().toString(),et_tare.getText().toString(),tv_net_weight.getText().toString(),
                        operator_sign_first_phase , signature_pad_driver_first_phase.getSignatureBitmap());
            }else if (isSigned){
                supply_layout = new PrintingLayoutFormat(this).supplyFormLayoutToBitmap(ticket_no, comment, user_name,selected_collection_site_name,selected_delivery_site_name,selected_collection_site_address,
                        selected_delivery_site_address,"","", haulier_company, user_name, haulier_carrier_no, date + " " + time, vehicle_registration_number, selected_material_name,
                        unit,gross,tare,net_weight, signaturePadOperator.getSignatureBitmap(), signaturePadDriver.getSignatureBitmap());
            }
            Bitmap resize_bmp = getResizedBitmap(supply_layout,600,1100);
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
        AlertDialog.Builder alertDialog=  new  AlertDialog.Builder(this);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton( "Discard",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //editor.putBoolean("isSubmitted", false);
                        //editor.apply();
                        if (store_supply_coming_value.equals("ArrivedCollectionPoint"))
                            intent = new Intent(SupplyFormDriver.this, com.crate.crateam.activities.ArrivedCollectionPoint.class);
                        else if (store_supply_coming_value.equals("LeftCollectionPoint"))
                            intent = new Intent(SupplyFormDriver.this, com.crate.crateam.activities.LeftCollectionPoint.class);
                        else if (store_supply_coming_value.equals("ArrivedDeliveryPoint"))
                            intent = new Intent(SupplyFormDriver.this, com.crate.crateam.activities.ArrivedDeliveryPoint.class);
                        else if (store_supply_coming_value.equals("ArrivedDeliveryPoint_SubmitForm"))
                            intent = new Intent(SupplyFormDriver.this, com.crate.crateam.activities.ArrivedDeliveryPoint.class);
                        else if (store_supply_coming_value.equals("FinishTask"))
                            intent = new Intent(SupplyFormDriver.this, com.crate.crateam.activities.FinishTaskActivity.class);
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
                Toast.makeText(SupplyFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SupplyFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
        bt_ok = customize_view.findViewById(R.id.bt_ok);
        bt_cancel_waiting = customize_view.findViewById(R.id.bt_cancel_waiting);
        bt_ok.setOnClickListener(this);
        bt_cancel_waiting.setOnClickListener(this);
        rv_hour = customize_view.findViewById(R.id.rv_hour);
        rv_minute = customize_view.findViewById(R.id.rv_minute);
        customize_alert_dialog.setView(customize_view);
        alertDialog_time = customize_alert_dialog.create();
        alertDialog_time.show();
        arrayList_hour.clear();
        arrayList_minute.clear();
        for(int i=0;i<=100;i++){
            if (i<10) arrayList_hour.add("0"+String.valueOf(i));
            else arrayList_hour.add(String.valueOf(i));
        }
        for(int i=0;i<60;i++){
            if (i<10) arrayList_minute.add("0"+String.valueOf(i));
            else arrayList_minute.add(String.valueOf(i));
        }
        hourAdapter = new HourAdapter(SupplyFormDriver.this,arrayList_hour);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv_hour.setLayoutManager(mLayoutManager);
        rv_hour.setItemAnimator(new DefaultItemAnimator());
        rv_hour.setAdapter(hourAdapter);

        minuteAdapter = new MinuteAdapter(SupplyFormDriver.this,arrayList_minute);
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
                Toast.makeText(SupplyFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            Log.d("Add Management Site Ids :" , String.valueOf(arrayList_add_management_collection));
                        }
                        getCollectionSiteAddress();
                    }
                    else progressDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCollectionSiteAddress(){
        Query query = siteManagementReference.whereEqualTo("status","Active").whereEqualTo("address_status","Active").whereEqualTo("site_type","Collection");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    store_site_id_collection.clear();
                    store_site_address_collection.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        store_site_id_collection.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                        store_site_address_collection.add(queryDocumentSnapshot.getString("site_name"));
                    }
                }
                Log.d("Site Management Site Ids :" , store_site_id_delivery + "  " + store_site_address_delivery);
                sp_current_site.setTitle("Sites");
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_custom_layout, store_site_address_collection);
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sp_current_site.setAdapter(spinnerArrayAdapter);
                if (store_site_address_collection.size()>0){
                    store_site_address_collection.add(0,"Select collection site");
                    store_site_id_collection.add(0,0);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SupplyFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deliverySiteAddressManagement(){
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
                                    Log.d("Site Management Site Ids :" ,store_site_id_delivery+"  "+store_site_address_delivery);
                                    break;
                                }
                            }
                        }
                        if (store_site_address_delivery.size()>0){
                            Log.d("SSSSSSS :" ,"I am here");
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
                            Log.d("DDDDDD : " ,given_delivery_site_id_new +" "+given_delivery_site_id);
                            if (given_delivery_site_id_new == 0 && selected_delivery_site_id == 0) {
                                for (int i = 0; i < store_site_id_delivery.size(); i++) {
                                    if (given_delivery_site_id == store_site_id_delivery.get(i)) {
                                        del_site_id = i;
                                        Log.d("DDDDDD 1: " , String.valueOf(del_site_id));
                                    }
                                }
                                setDeliverySiteAdapter(del_site_id);
                            } else if (given_delivery_site_id_new != 0 && selected_delivery_site_id == 0) {
                                for (int i = 0; i < store_site_id_delivery.size(); i++) {
                                    if (given_delivery_site_id_new == store_site_id_delivery.get(i)) {
                                        del_site_id = i;
                                        Log.d("DDDDDD 2: " , String.valueOf(del_site_id));
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
                Toast.makeText(SupplyFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDeliverySiteAdapter(int del_site_id){
        Log.d("FFFFF :" , String.valueOf(del_site_id));
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_custom_layout,store_site_address_delivery);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        sp_delivery_site.setAdapter(spinnerArrayAdapter);
        sp_delivery_site.setSelection(del_site_id);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_current_site:
                Log.d(" Collection Id All:" ,store_site_id_collection+" "+given_collection_site_id
                        +" "+given_collection_site_id_new+" "+selected_collection_site_id);
                if (given_collection_site_id_new == 0) {
                    for (int i = 0; i < store_site_id_collection.size(); i++) {
                        if (given_collection_site_id == store_site_id_collection.get(i)) {
                            sp_current_site.setSelection(i);
                            break;
                        }
                    }
                    Log.d("I am here :" ,"Collection 1");
                }
                if (position > 0) {
                    selected_collection_site_name = store_site_address_collection.get(position).trim();
                    selected_collection_site_id = store_site_id_collection.get(position);
                    Log.d("Selected Site Collection:" , String.valueOf(selected_collection_site_id));
                    if (selected_collection_site_id!=0){
                        for (int i = 0; i < store_site_id_collection.size(); i++) {
                            if (selected_collection_site_id == store_site_id_collection.get(i)) {
                                sp_current_site.setSelection(i);
                                break;
                            }
                        }
                        Log.d("EEEEE :" , String.valueOf(selected_collection_site_id));
                        Log.d("I am here :" ,"Collection 2  "+selected_collection_site_id);
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
                        Log.d("I am here :" ,"Collection 3");
                        getCollectionSiteID(given_collection_site_id);
                        getMaterialDetails(given_collection_site_id);
                    } else if (given_collection_site_id_new != 0 && selected_collection_site_id == 0) {
                        for (int i = 0; i < store_site_id_collection.size(); i++) {
                            if (given_collection_site_id_new == store_site_id_collection.get(i)) {
                                sp_current_site.setSelection(i);
                                break;
                            }
                        }
                        Log.d("I am here :" ,"Collection 4");
                        getCollectionSiteID(given_collection_site_id_new);
                        getMaterialDetails(given_collection_site_id_new);
                    }
                }
                break;
            case R.id.sp_delivery_site:
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
                    Log.d("Selected Delivery Site :" ,selected_delivery_site_id+" "+given_delivery_site_id+" "+
                            show_method_execute_del_site);

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
                        getMaterialDescription(String.valueOf(selected_material_id));
                        assignMaterialsDeliverySite(selected_material_id);
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
                        getMaterialDescription(String.valueOf(given_material_id));
                        assignMaterialsDeliverySite(given_material_id);
                    } else if (given_material_id_new != 0 && selected_material_id == 0) {
                        for (int i = 0; i < store_material_id_list.size(); i++) {
                            if (given_material_id_new == store_material_id_list.get(i)) {
                                sp_materials.setSelection(i);
                                break;
                            }
                        }
                        getMaterialDescription(String.valueOf(given_material_id_new));
                        assignMaterialsDeliverySite(given_material_id_new);
                    }
                    show_method_execute_material= false;
                }
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
                Toast.makeText(SupplyFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Log.d("Show Collection Id New:" , String.valueOf(given_collection_site_id_new));
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
                                Log.d("CCC : " , String.valueOf(given_collection_site_id_new));
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
                Toast.makeText(SupplyFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SupplyFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDeliverySiteIdNewFromAddressManagement(int address_management_id){
        Query query = siteManagementReference.whereEqualTo("id",address_management_id).whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        given_delivery_site_id_new = queryDocumentSnapshot.getLong("id").intValue();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        given_delivery_site_id = queryDocumentSnapshot.getLong("id").intValue();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SupplyFormDriver.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SupplyFormDriver.this, "Unable to update task management details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
