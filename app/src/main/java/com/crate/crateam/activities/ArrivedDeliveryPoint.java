package com.crate.crateam.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.crate.crateam.R;
import com.crate.crateam.interfaces.ChangeVehicleOption;
import com.crate.crateam.interfaces.StartTask;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.ChangeVehicle;
import com.crate.crateam.utility.CollectionDeliveryPoint;
import com.crate.crateam.utility.CustomSearchableSpinner;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ArrivedDeliveryPoint extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private ImageView iv_cross,iv_next_page;
    private Button bt_add_fuel, bt_arrived_delivery_point, bt_abort,bt_complete_delivery_note,bt_edit_supply,bt_submit_supply_form,bt_submit_new,bt_exit;
    private Intent intent;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean isSubmittedFinalArrivedDelivery = false,isSubmitSupplyArrivedDelivery = false,isSubmittedWasteArrivedDelivery = false,isArrivedDeliveryClicked = false;
    private TextView  tv_name,tv_role,tv_vehicle,tv_ticket_type,tv_change_vehicle_arrived_delivery,tv_collection_point,tv_material,tv_delivery_point;
    private ProgressDialog progressDialog;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference taskManagementReference,jobManagementDetailsNewReference,inspectionSubmissionReference,supplyWasteReference,
            materialReference,siteManagementReference;
    private int user_id=0,store_delivery_site_id=0, user_role_id=0,store_job_id=0,store_task_order_id=0,store_material_id=0,vehicle_number=0, reg_no_id=0,
            store_collection_site_id=0,updated_collection_site_id=0,updated_material_id=0,updated_delivery_site_id=0;
    private String store_note_type="",date ="",time="",latitude="",longitude="",store_material_description="",store_ewc_code="",store_material_type="",store_collection_point="",
            store_delivery_point="",reg_no="", user_name="",task_status="",inspection_id="";
    private CollectionDeliveryPoint collectionDeliveryPoint;

    private AlertDialog alertDialog;
    private CustomSearchableSpinner sp_change_vehicle;
    private ChangeVehicle changeVehicle;
    private ArrayList<String> store_array_list_reg_no;
    private ArrayList<Integer> store_array_list_reg_no_id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.arrived_delivery_point_layout);
        if (db==null)
            db = FirebaseFirestore.getInstance();
        FirestoreManager.initPersistentIndexManager();
        initView();
    }

    private void initView() {
        sharedPreferences = getApplicationContext().getSharedPreferences("MyPref",0);
        editor = sharedPreferences.edit();
        SessionManager sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_id = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        HashMap<String, String> vehicle = sessionManager.getVehicleDetails();
        vehicle_number = Integer.parseInt(Objects.requireNonNull(vehicle.get(SessionManager.KEY_VEHICLE_ID)));

        user_name = user.get(SessionManager.KEY_FULL_NAME);
        String user_role_first = user.get(SessionManager.KEY_ROLE_ONE);
        if (user_role_first.equals(String.valueOf(4)))
            user_role_first = "Driver";
        else if (user_role_first.equals(String.valueOf(3)))
            user_role_first = "Workshop Manager";
        else if (user_role_first.equals(String.valueOf(2)))
            user_role_first = "Transport Manager";
        else if (user_role_first.equals(String.valueOf(1)))
            user_role_first = "Administrator";
        String role = user.get(SessionManager.KEY_ROLE_ONE);
        user_role_id = Integer.parseInt(role);

        iv_cross = findViewById(R.id.iv_cross);
        bt_add_fuel = findViewById(R.id.bt_add_fuel);
        bt_arrived_delivery_point = findViewById(R.id.bt_arrived_delivery_point);
        bt_abort = findViewById(R.id.bt_abort);
        iv_next_page = findViewById(R.id.iv_next_page);
        bt_complete_delivery_note = findViewById(R.id.bt_complete_delivery_note);
        bt_edit_supply =findViewById(R.id.bt_edit_supply);
        bt_submit_supply_form = findViewById(R.id.bt_submit_supply_form);

        tv_name = findViewById(R.id.tv_name);
        tv_role = findViewById(R.id.tv_role);
        tv_vehicle = findViewById(R.id.tv_vehicle);

        tv_ticket_type = findViewById(R.id.tv_ticket_type);
        tv_material = findViewById(R.id.tv_material);
        tv_collection_point = findViewById(R.id.tv_collection_point);
        tv_delivery_point = findViewById(R.id.tv_delivery_point);
        tv_change_vehicle_arrived_delivery = findViewById(R.id.tv_change_vehicle_arrived_delivery);
        progressDialog = Dialog.showProgressDialog(this);
        progressDialog.show();

        taskManagementReference = db.collection("CR_task_management");
        inspectionSubmissionReference = db.collection("CR_inspection_submission");
        supplyWasteReference = db.collection("CR_supply_waste_forms");
        materialReference = db.collection("CR_material");
        siteManagementReference = db.collection("CR_site_management");
        jobManagementDetailsNewReference = db.collection("CR_job_management_new_details");

        collectionDeliveryPoint = new CollectionDeliveryPoint(ArrivedDeliveryPoint.this,user_id,vehicle_number);
        // offline support for collectionDeliveryPoint class
        collectionDeliveryPoint.enableOfflineSupportCommon();
        collectionDeliveryPoint.jobManagement();
        CollectionDeliveryPoint.startTask =new StartTask() {
            @Override
            public void getDetails(int collection_site_id,String collection_site_name,String collection_address, String delivery_address,int delivery_site_id, int job_id, int task_order_id, int material_id,String material_description,
                                   String ewc_code,String material_value,String note_type,String sic_code,String carrier_no, String permit_no_collection,String permit_no_delivery,String site_type,String material_type,String project_no) {
                store_collection_site_id = collection_site_id;
                store_collection_point = collection_address;
                store_delivery_point = delivery_address;
                store_delivery_site_id = delivery_site_id;
                store_job_id = job_id;
                store_task_order_id = task_order_id;
                store_material_id = material_id;
                store_material_description = material_description;
                store_ewc_code = ewc_code;
                store_note_type = note_type;
                store_material_type = material_type;
                tv_collection_point.setText(store_collection_point);
                tv_delivery_point.setText(store_delivery_point);
                tv_material.setText(material_description);
                if(store_note_type.equals("Collection")){
                    if (store_material_type.equals("Collection") && store_ewc_code.equals(""))
                        tv_ticket_type.setText("CL:aire Form");
                    else
                        tv_ticket_type.setText("Waste Transfer Form");
                }
                else
                    tv_ticket_type.setText("Supply Form");
                getTaskStatus();
                fetchUpdatedMaterial();
                getVehicleType();
                progressDialog.dismiss();
            }
        };
        tv_name.setText(user_name);
        tv_role.setText(user_role_first);
        collectionDeliveryPoint.fetchVehicleRegistrationNumber(tv_vehicle);
        date = AppData.date();
time = AppData.Time();
        fetchInspectionId();
        initializeOnClick();
    }

    private void saveState(){
        isSubmitSupplyArrivedDelivery = sharedPreferences.getBoolean("isSubmitSupply",false);
        Log.d("Arrived Delivery Clicked :" ,isArrivedDeliveryClicked +" "+isSubmitSupplyArrivedDelivery);
        if (isArrivedDeliveryClicked) {
            bt_arrived_delivery_point.setBackground(getResources().getDrawable(R.drawable.grey_button_bg));
            bt_arrived_delivery_point.setTextColor(getResources().getColor(R.color.black_shade_two));
            bt_arrived_delivery_point.setEnabled(false);
            if (store_note_type.equals("Delivery")) {
                bt_edit_supply.setVisibility(View.VISIBLE);
                bt_submit_supply_form.setVisibility(View.VISIBLE);
                bt_complete_delivery_note.setVisibility(View.GONE);
                if (!isSubmitSupplyArrivedDelivery){
                    iv_next_page.setEnabled(false);
                    iv_next_page.setImageDrawable(getResources().getDrawable(R.drawable.arrow_unselect));
                }else {
                    bt_submit_supply_form.setBackground(getResources().getDrawable(R.drawable.grey_button_bg));
                    bt_submit_supply_form.setTextColor(getResources().getColor(R.color.black_shade_two));
                    bt_submit_supply_form.setEnabled(false);
                    iv_next_page.setEnabled(true);
                    iv_next_page.setImageDrawable(getResources().getDrawable(R.drawable.arrow));
                }
            }else{
                bt_edit_supply.setVisibility(View.GONE);
                bt_submit_supply_form.setVisibility(View.GONE);
                bt_complete_delivery_note.setVisibility(View.VISIBLE);
                iv_next_page.setEnabled(false);
                iv_next_page.setImageDrawable(getResources().getDrawable(R.drawable.arrow_unselect));
            }
        } else {
            iv_next_page.setEnabled(false);
            iv_next_page.setImageDrawable(getResources().getDrawable(R.drawable.arrow_unselect));
            bt_arrived_delivery_point.setBackground(getResources().getDrawable(R.drawable.login_button_background));
            bt_arrived_delivery_point.setTextColor(getResources().getColor(R.color.white));
            bt_arrived_delivery_point.setEnabled(true);
            if (store_note_type.equals("Delivery")) {
                bt_edit_supply.setVisibility(View.VISIBLE);
                bt_submit_supply_form.setVisibility(View.GONE);
                bt_complete_delivery_note.setVisibility(View.GONE);
            }else{
                bt_edit_supply.setVisibility(View.GONE);
                bt_submit_supply_form.setVisibility(View.GONE);
                bt_complete_delivery_note.setVisibility(View.GONE);
            }
            iv_next_page.setEnabled(false);
            iv_next_page.setImageDrawable(getResources().getDrawable(R.drawable.arrow_unselect));
        }
        bt_abort.setVisibility(View.VISIBLE);
        bt_abort.setEnabled(true);
    }

    private void initializeOnClick() {
        iv_cross.setOnClickListener(this);
        bt_add_fuel.setOnClickListener(this);
        bt_arrived_delivery_point.setOnClickListener(this);
        bt_abort.setOnClickListener(this);
        iv_next_page.setOnClickListener(this);
        bt_complete_delivery_note.setOnClickListener(this);
        bt_edit_supply.setOnClickListener(this);
        bt_submit_supply_form.setOnClickListener(this);
        tv_change_vehicle_arrived_delivery.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_cross:
                alertDialogToDashBoard("Do you want to leave this task ?");
                break;
            case R.id.iv_next_page:
                intent = new Intent(ArrivedDeliveryPoint.this, com.crate.crateam.activities.FinishTaskActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.bt_complete_delivery_note:
                isSubmittedWasteArrivedDelivery = true;
                editor.putString("waste_coming_form","ArrivedDeliveryPoint");
                editor.putBoolean("isSubmittedWaste",isSubmittedWasteArrivedDelivery);
                editor.apply();
                intent = new Intent(ArrivedDeliveryPoint.this, com.crate.crateam.activities.WasteTransferFormDeliveryDriver.class);
                startActivity(intent);
                finish();
                break;
            case R.id.bt_submit_supply_form:
                isSubmittedFinalArrivedDelivery = true;
                bt_submit_supply_form.setEnabled(false);
                intent = new Intent(ArrivedDeliveryPoint.this, com.crate.crateam.activities.SupplyFormDriver.class);
                editor.putString("supply_coming_form","ArrivedDeliveryPoint_SubmitForm");
                editor.putBoolean("isSubmitted",isSubmittedFinalArrivedDelivery);
                editor.apply();
                startActivity(intent);
                break;
            case R.id.bt_edit_supply:
                isSubmittedFinalArrivedDelivery = false;
                editor.putString("supply_coming_form","ArrivedDeliveryPoint");
                editor.putBoolean("isSubmitted",isSubmittedFinalArrivedDelivery);
                editor.apply();
                intent = new Intent(ArrivedDeliveryPoint.this, com.crate.crateam.activities.SupplyFormDriver.class);
                startActivity(intent);
                finish();
                break;
            case R.id.bt_add_fuel:
                intent = new Intent(ArrivedDeliveryPoint.this, EndOfDayActivity.class);
                startActivity(intent);
                break;
            case R.id.bt_arrived_delivery_point:
                editor.putBoolean("arrivedDeliveryClicked", true);
                editor.apply();
                if (store_note_type.equals("Delivery")) {
                    bt_edit_supply.setVisibility(View.VISIBLE);
                    bt_submit_supply_form.setVisibility(View.VISIBLE);
                    bt_submit_supply_form.setEnabled(true);
                    bt_complete_delivery_note.setVisibility(View.GONE);
                    iv_next_page.setEnabled(false);
                    iv_next_page.setImageDrawable(getResources().getDrawable(R.drawable.arrow_unselect));
                }else{
                    bt_edit_supply.setVisibility(View.GONE);
                    bt_submit_supply_form.setVisibility(View.GONE);
                    bt_complete_delivery_note.setVisibility(View.VISIBLE);
                    iv_next_page.setEnabled(false);
                    iv_next_page.setImageDrawable(getResources().getDrawable(R.drawable.arrow_unselect));
                }
                bt_arrived_delivery_point.setEnabled(false);
                bt_arrived_delivery_point.setBackground(getResources().getDrawable(R.drawable.grey_button_bg));
                bt_arrived_delivery_point.setTextColor(getResources().getColor(R.color.black_shade_two));
                date = AppData.date();
time = AppData.Time();
                latLon();
                Log.d("Inspection Id :" ,inspection_id);
                sendArrivedDeliveryDetails(date,time,latitude,longitude);
                if (!AppData.internetOnline(this))
                    progressDialog.dismiss();
                break;
            case R.id.bt_abort:
                alertDialogAbortTask("Do you want to do abort this task?");
                break;
            case R.id.tv_change_vehicle_arrived_delivery:
                customizeWindow();
                break;
            case R.id.bt_submit_new:
                if (reg_no.equals(""))
                    Dialog.alertDialog(this,"Please select registration number.");
                else {
                    changeVehicle.inspectionSubmission(reg_no_id,alertDialog);
                }
                break;
            case R.id.bt_exit:
                reg_no="";
                alertDialog.cancel();
                break;
        }
    }

    private void dateTime(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        date = df.format(c);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        time = simpleDateFormat.format(calendar.getTime());
    }

    private void latLon(){
        GPSTracker finder = new GPSTracker(ArrivedDeliveryPoint.this);
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

    private void alertDialogAbortTask(String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        date = AppData.date();
time = AppData.Time();
                        latLon();
                        sendAbortTaskDetails(date,time,latitude,longitude);
                        intent = new Intent(ArrivedDeliveryPoint.this,MainActivity.class);
                        startActivity(intent);
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
    protected void onResume() {
        super.onResume();
        getTaskStatus();
    }

    @Override
    public void onBackPressed() {

    }

    // update task_management table
    private void sendArrivedDeliveryDetails(final String task_date,final String task_time, final String latitude, final String longitude){
        Query query = taskManagementReference.whereEqualTo("job_id",store_job_id).whereEqualTo("task_order_no",store_task_order_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()) {
                        if (queryDocumentSnapshot.exists()) {
                            String doc_id = queryDocumentSnapshot.getId();
                            Map<String,Object> mapDeliveryTask = new HashMap<>();
                            mapDeliveryTask.put("arrived_delivery_time",task_time);
                            mapDeliveryTask.put("arrived_delivery_date",task_date);
                            mapDeliveryTask.put("arrived_delivery_latitude",latitude);
                            mapDeliveryTask.put("arrived_delivery_longitude",longitude);
                            mapDeliveryTask.put("arrived_delivery_user_id",user_id);
                            mapDeliveryTask.put("arrived_delivery_inspection_id",inspection_id);
                            mapDeliveryTask.put("task_status","ArrivedDeliveryPoint");
                            mapDeliveryTask.put("is_updated","Yes");
                            mapDeliveryTask.put("time_second_format", AppData.getTimeSecond());
                            taskManagementReference.document(doc_id).update(mapDeliveryTask);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ArrivedDeliveryPoint.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // update task_management table
    private void sendAbortTaskDetails(final String task_date,final String task_time, final String latitude, final String longitude){
        Query query = taskManagementReference.whereEqualTo("job_id",store_job_id).whereEqualTo("task_order_no",store_task_order_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()) {
                        if (queryDocumentSnapshot.exists()) {
                            String doc_id = queryDocumentSnapshot.getId();
                            Map<String,Object> mapAbortTask = new HashMap<>();
                            mapAbortTask.put("abort_task_time",task_time);
                            mapAbortTask.put("abort_task_date",task_date);
                            mapAbortTask.put("abort_task_latitude",latitude);
                            mapAbortTask.put("abort_task_longitude",longitude);
                            mapAbortTask.put("abort_task_user_id",user_id);
                            mapAbortTask.put("abort_task_inspection_id",inspection_id);
                            mapAbortTask.put("task_status","AbortTask");
                            mapAbortTask.put("is_updated","Yes");
                            mapAbortTask.put("time_second_format", AppData.getTimeSecond());
                            taskManagementReference.document(doc_id).update(mapAbortTask);
                            updateJobManagementDetails();
                            removePreferenceValues();
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ArrivedDeliveryPoint.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateJobManagementDetails(){
        Query queryJobManagementDetailsOne = jobManagementDetailsNewReference.whereEqualTo("job_id",store_job_id).whereEqualTo("task_order_no",store_task_order_id).whereEqualTo("status","Active");
        queryJobManagementDetailsOne.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        if (queryDocumentSnapshot.exists()){
                            String doc_id = queryDocumentSnapshot.getId();
                            Map<String,Object> updateJobDetails= new HashMap<>();
                            updateJobDetails.put("status","Completed");
                            updateJobDetails.put("pending_task","No");
                            updateJobDetails.put("is_updated","Yes");
                            updateJobDetails.put("current_task","No");
                            jobManagementDetailsNewReference.document(doc_id).update(updateJobDetails);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ArrivedDeliveryPoint.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void customizeWindow() {
        AlertDialog.Builder customize_alert_dialog = new AlertDialog.Builder(this);
        customize_alert_dialog.setTitle("Change vehicle");
        customize_alert_dialog.setCancelable(false);
        View customize_view = getLayoutInflater().inflate(R.layout.change_vehicle, null);
        sp_change_vehicle = customize_view.findViewById(R.id.sp_change_vehicle);
        bt_submit_new = customize_view.findViewById(R.id.bt_submit_new);
        bt_exit = customize_view.findViewById(R.id.bt_exit);
        bt_submit_new.setOnClickListener(this);
        bt_exit.setOnClickListener(this);
        sp_change_vehicle.setOnItemSelectedListener(this);
        customize_alert_dialog.setView(customize_view);
        alertDialog = customize_alert_dialog.create();
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_custom_layout, store_array_list_reg_no);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_change_vehicle.setAdapter(spinnerArrayAdapter);
        alertDialog.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.sp_change_vehicle:
                reg_no = sp_change_vehicle.getSelectedItem().toString().trim();
                reg_no_id = store_array_list_reg_no_id.get(position);
                if (!AppData.internetOnline(this))
                    progressDialog.dismiss();
                break;
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
    private void getVehicleType(){
        store_array_list_reg_no = new ArrayList<>();
        store_array_list_reg_no_id = new ArrayList<>();
        changeVehicle = new ChangeVehicle(ArrivedDeliveryPoint.this,user_id,store_job_id);
        changeVehicle.vehicleUserMapping();
        ChangeVehicle.changeVehicleOption = new ChangeVehicleOption() {
            @Override
            public void getRegistrationNoList(ArrayList<String> arrayList_reg_no, ArrayList<Integer> arrayList_reg_no_id, int vehicle_registration_no_id) {
                store_array_list_reg_no = arrayList_reg_no;
                store_array_list_reg_no_id = arrayList_reg_no_id;
                for (int i=0;i<store_array_list_reg_no_id.size();i++){
                    if (store_array_list_reg_no_id.get(i) == vehicle_registration_no_id){
                        store_array_list_reg_no_id.remove(i);
                        store_array_list_reg_no.remove(i);
                    }
                }
            }
        };
    }
    private void alertDialogToDashBoard(String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        intent = new Intent(ArrivedDeliveryPoint.this,MainActivity.class);
                        startActivity(intent);
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
    private void removePreferenceValues(){
        if (sharedPreferences.contains("supply_coming_form"))
            editor.remove("supply_coming_form");
        if (sharedPreferences.contains("waste_coming_form"))
            editor.remove("waste_coming_form");
        editor.remove("isSubmitted");
        editor.remove("isSubmitSupply");
        editor.remove("isSubmittedWaste");
        editor.remove("startTaskClicked");
        editor.remove("arrivedCollectionClicked");
        editor.remove("leftCollectionClicked");
        editor.remove("arrivedDeliveryClicked");
        editor.apply();
    }

    private void getTaskStatus(){
        taskManagementReference.document(store_job_id+"_"+store_task_order_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    task_status= documentSnapshot.getString("task_status");
                    if (task_status.equals("ArrivedDeliveryPoint"))
                        isArrivedDeliveryClicked = true;
                }
                saveState();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ArrivedDeliveryPoint.this, "Unable to get task status.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchInspectionId(){
        Query query = inspectionSubmissionReference.whereEqualTo("logged_by",user_id).whereEqualTo("conducted_on",date)
                .whereEqualTo("vehicle_id",vehicle_number).whereEqualTo("user_role",4).whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        Log.d("Inspection Id :" ,"I am here");
                        inspection_id = queryDocumentSnapshot.getString("inspection_id");
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d("Error :" ,"Unable to fetch inspection id." );
            }
        });
    }

    private void fetchUpdatedMaterial(){
        Query query = supplyWasteReference.whereEqualTo("job_id",store_job_id).whereEqualTo("task_order_id",store_task_order_id)
                .whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        updated_collection_site_id = queryDocumentSnapshot.getLong("current_site_id_new").intValue();
                        updated_material_id = queryDocumentSnapshot.getLong("material_id_new").intValue();
                        updated_delivery_site_id = queryDocumentSnapshot.getLong("delivery_site_id_new").intValue();
                    }
                    Log.d("Material Id:" , String.valueOf(updated_material_id));
                    if (updated_collection_site_id != 0)
                        getUpdatedCollectionPoint(updated_collection_site_id);
                    if (updated_material_id != 0)
                        getUpdatedMaterial(updated_material_id);
                    if (updated_delivery_site_id != 0)
                        getUpdatedDeliveryPoint(updated_delivery_site_id);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Error :" ,"Unable to fetch updated material." );
            }
        });
    }

    private void getUpdatedMaterial(int material_id){
        materialReference.document(String.valueOf(material_id)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        DocumentSnapshot document = task.getResult();
                        store_material_description = document.getString("material_description");
                        store_ewc_code = document.getString("ewc_code");
                        store_material_type = document.getString("material_type");
                        Log.d("DESCRIPTION :" ,store_material_description);
                        tv_material.setText(store_material_description);
                        if(store_note_type.equals("Collection")){
                            if (store_material_type.equals("Collection") && store_ewc_code.equals(""))
                                tv_ticket_type.setText("CL:aire Form");
                            else
                                tv_ticket_type.setText("Waste Transfer Form");
                        }
                        else
                            tv_ticket_type.setText("Supply Form");
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ArrivedDeliveryPoint.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUpdatedCollectionPoint(int collection_site_id){
        Query queryAddressManagement = siteManagementReference.whereEqualTo("id",collection_site_id).whereEqualTo("status","Active");
        queryAddressManagement.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            store_collection_point = queryDocumentSnapshot.getString("address");
                        }
                        tv_collection_point.setText(store_collection_point);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ArrivedDeliveryPoint.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUpdatedDeliveryPoint(int delivery_site_id){
        Query queryAddressManagement = siteManagementReference.whereEqualTo("id",delivery_site_id).whereEqualTo("status","Active");
        queryAddressManagement.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            store_delivery_point = queryDocumentSnapshot.getString("address");
                        }
                        tv_delivery_point.setText(store_delivery_point);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ArrivedDeliveryPoint.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

