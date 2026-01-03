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

public class ArrivedCollectionPoint extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private ImageView iv_cross,iv_next_page;
    private TextView  tv_name,tv_role,tv_vehicle,tv_ticket_type,tv_change_vehicle_arrived_task,tv_collection_point,tv_material,tv_delivery_point;
    private Button bt_add_fuel, bt_arrived_collection_point, bt_abort,bt_edit_waste_transfer_note,bt_edit_supply_form,bt_complete_waste,bt_complete_supply,bt_submit_new,bt_exit;
    private Intent intent;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean isSubmittedSupplyArrivedCollection = false,isSubmittedWasteArrivedCollection = false,isArrivedCollectionPoint = false;
    private CollectionDeliveryPoint collectionDeliveryPoint;
    private String user_name="",date = "",time="",latitude="",longitude="",store_collection_point="",store_delivery_point="",store_material_description="",store_ewc_code="",
            store_note_type="",store_material_type="",reg_no="",task_status="",inspection_id="";
    private int vehicle_number=0,store_material_id = 0,store_delivery_site_id=0,store_job_id = 0,store_task_order_id = 0,reg_no_id=0,user_id=0,
            updated_collection_site_id=0,updated_material_id=0,updated_delivery_site_id=0;
    private CollectionReference taskManagementReference,inspectionSubmissionReference,jobManagementDetailsNewReference,
            supplyWasteReference,materialReference,siteManagementReference;
    private FirebaseFirestore db= FirebaseFirestore.getInstance();
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    private CustomSearchableSpinner sp_change_vehicle;
    private ChangeVehicle changeVehicle;
    private ArrayList<String> store_array_list_reg_no = new ArrayList<>();
    private ArrayList<Integer> store_array_list_reg_no_id = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.arrived_task_layout);
        if (db==null)
            db = FirebaseFirestore.getInstance();
        FirestoreManager.initPersistentIndexManager();
        initView();
    }

    private void initView() {
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

        sharedPreferences = getApplicationContext().getSharedPreferences("MyPref",0);
        editor = sharedPreferences.edit();
        iv_cross = findViewById(R.id.iv_cross);
        bt_add_fuel = findViewById(R.id.bt_add_fuel);
        bt_arrived_collection_point = findViewById(R.id.bt_arrived_collection_point);
        bt_abort = findViewById(R.id.bt_abort);
        iv_next_page = findViewById(R.id.iv_next_page);
        bt_edit_waste_transfer_note = findViewById(R.id.bt_edit_waste_transfer_note);
        bt_complete_waste = findViewById(R.id.bt_complete_waste);
        bt_complete_supply = findViewById(R.id.bt_complete_supply);
        bt_edit_supply_form = findViewById(R.id.bt_edit_supply_form);
        tv_material = findViewById(R.id.tv_material);
        tv_collection_point = findViewById(R.id.tv_collection_point);
        tv_change_vehicle_arrived_task = findViewById(R.id.tv_change_vehicle_arrived_task);
        tv_delivery_point = findViewById(R.id.tv_delivery_point);

        tv_name = findViewById(R.id.tv_name);
        tv_role = findViewById(R.id.tv_role);
        tv_vehicle = findViewById(R.id.tv_vehicle);

        tv_ticket_type = findViewById(R.id.tv_ticket_type);
        progressDialog = Dialog.showProgressDialog(this);
        progressDialog.show();
        bt_edit_waste_transfer_note.setEnabled(false);
        iv_next_page.setEnabled(false);
        taskManagementReference = db.collection("CR_task_management");
        inspectionSubmissionReference = db.collection("CR_inspection_submission");
        supplyWasteReference = db.collection("CR_supply_waste_forms");
        materialReference = db.collection("CR_material");
        siteManagementReference = db.collection("CR_site_management");
        jobManagementDetailsNewReference = db.collection("CR_job_management_new_details");
        collectionDeliveryPoint = new CollectionDeliveryPoint(ArrivedCollectionPoint.this,user_id,vehicle_number);
        collectionDeliveryPoint.enableOfflineSupportCommon();
        collectionDeliveryPoint.jobManagement();
        CollectionDeliveryPoint.startTask = new StartTask() {
            @Override
            public void getDetails(int collection_site_id,String collection_site_name,String collection_address, String delivery_address,int delivery_site_id, int job_id, int task_order_id, int material_id,String material_despcription,
                                   String ewc_code,String material_value,String note_type,String sic_code,String carrier_no,String permit_no_collection,String permit_no_delivery,String site_type,String material_type,String project_no) {
                store_collection_point = collection_address;
                store_delivery_point = delivery_address;
                store_delivery_site_id = delivery_site_id;
                store_material_description = material_despcription;
                store_ewc_code = ewc_code;
                store_job_id = job_id;
                store_task_order_id = task_order_id;
                store_material_id = material_id;
                store_note_type = note_type;
                store_material_type = material_type;
                tv_collection_point.setText(store_collection_point);
                tv_delivery_point.setText(store_delivery_point);
                if(store_note_type.equals("Collection")){
                    if (store_material_type.equals("Collection") && store_ewc_code.equals(""))
                        tv_ticket_type.setText("CL:aire Form");
                    else
                        tv_ticket_type.setText("Waste Transfer Form");
                }
                else
                    tv_ticket_type.setText("Supply Form");
                tv_material.setText(material_despcription);
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
        Log.d("Is Arrived and Submitted:",isSubmittedWasteArrivedCollection+" "+isSubmittedSupplyArrivedCollection +" "+isArrivedCollectionPoint + " "+store_material_type);
        if (isArrivedCollectionPoint) {
            bt_arrived_collection_point.setBackground(getResources().getDrawable(R.drawable.grey_button_bg));
            bt_arrived_collection_point.setTextColor(getResources().getColor(R.color.black_shade_two));
            bt_arrived_collection_point.setEnabled(false);
            if (store_note_type.equals("Delivery")) {
                bt_complete_supply.setVisibility(View.VISIBLE);
                bt_complete_waste.setVisibility(View.GONE);
                if (!isSubmittedSupplyArrivedCollection){
                    bt_complete_supply.setBackground(getResources().getDrawable(R.drawable.login_button_background));
                    bt_complete_supply.setTextColor(getResources().getColor(R.color.white));
                    iv_next_page.setEnabled(false);
                    iv_next_page.setImageDrawable(getResources().getDrawable(R.drawable.arrow_unselect));
                }else {
                    bt_complete_supply.setVisibility(View.GONE);
                    bt_edit_supply_form.setVisibility(View.VISIBLE);
                    iv_next_page.setEnabled(true);
                    iv_next_page.setImageDrawable(getResources().getDrawable(R.drawable.arrow));
                }
            }
            else {
                bt_complete_supply.setVisibility(View.GONE);
                bt_complete_waste.setVisibility(View.VISIBLE);
                if (!isSubmittedWasteArrivedCollection){
                    bt_complete_waste.setBackground(getResources().getDrawable(R.drawable.login_button_background));
                    bt_complete_waste.setTextColor(getResources().getColor(R.color.white));
                    iv_next_page.setEnabled(false);
                    iv_next_page.setImageDrawable(getResources().getDrawable(R.drawable.arrow_unselect));
                }else {
                    bt_complete_waste.setBackground(getResources().getDrawable(R.drawable.grey_button_bg));
                    bt_complete_waste.setTextColor(getResources().getColor(R.color.black_shade_two));
                    bt_complete_waste.setEnabled(false);
                    iv_next_page.setEnabled(true);
                    iv_next_page.setImageDrawable(getResources().getDrawable(R.drawable.arrow));
                }
            }
        } else {
            bt_arrived_collection_point.setBackground(getResources().getDrawable(R.drawable.login_button_background));
            bt_arrived_collection_point.setTextColor(getResources().getColor(R.color.white));
            bt_arrived_collection_point.setEnabled(true);
            iv_next_page.setEnabled(false);
            iv_next_page.setImageDrawable(getResources().getDrawable(R.drawable.arrow_unselect));
           checkedButton();
        }
        bt_abort.setVisibility(View.VISIBLE);
        bt_abort.setEnabled(true);
    }

    private void checkedButton(){
        if (store_note_type.equals("Delivery")) {
            bt_complete_supply.setVisibility(View.VISIBLE);
            bt_complete_waste.setVisibility(View.GONE);
            if (!isSubmittedSupplyArrivedCollection){
                bt_complete_supply.setBackground(getResources().getDrawable(R.drawable.login_button_background));
                bt_complete_supply.setTextColor(getResources().getColor(R.color.white));
                iv_next_page.setEnabled(false);
                iv_next_page.setImageDrawable(getResources().getDrawable(R.drawable.arrow_unselect));
            }else {
                bt_complete_supply.setVisibility(View.GONE);
                bt_edit_supply_form.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initializeOnClick() {
        iv_cross.setOnClickListener(this);
        bt_add_fuel.setOnClickListener(this);
        bt_arrived_collection_point.setOnClickListener(this);
        bt_abort.setOnClickListener(this);
        iv_next_page.setOnClickListener(this);
        bt_edit_waste_transfer_note.setOnClickListener(this);
        bt_complete_waste.setOnClickListener(this);
        bt_complete_supply.setOnClickListener(this);
        bt_edit_supply_form.setOnClickListener(this);
        tv_change_vehicle_arrived_task.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_cross:
                alertDialogToDashBoard("Do you want to leave this task ?");
                break;
            case R.id.iv_next_page:
                intent = new Intent(ArrivedCollectionPoint.this, com.crate.crateam.activities.LeftCollectionPoint.class);
                startActivity(intent);
                finish();
                break;
            case R.id.bt_edit_waste_transfer_note:
            case R.id.bt_complete_waste:
                editor.putString("waste_coming_form","ArrivedCollectionPoint");
                editor.apply();
                intent = new Intent(ArrivedCollectionPoint.this, com.crate.crateam.activities.WasteTransferFormDriver.class);
                startActivity(intent);
                if (isArrivedCollectionPoint)
                    finish();
                break;
            case R.id.bt_complete_supply:
            case R.id.bt_edit_supply_form:
                editor.putString("supply_coming_form","ArrivedCollectionPoint");
                editor.apply();
                intent = new Intent(ArrivedCollectionPoint.this, SupplyFormDriver.class);
                startActivity(intent);
                finish();
                break;
            case R.id.bt_add_fuel:
                intent = new Intent(ArrivedCollectionPoint.this, com.crate.crateam.activities.EndOfDayActivity.class);
                startActivity(intent);
                break;
            case R.id.bt_arrived_collection_point:
                editor.putBoolean("arrivedCollectionClicked", true);
                editor.putString("supply_coming_form","ArrivedCollectionPoint");
                editor.apply();
                bt_arrived_collection_point.setEnabled(false);
                bt_arrived_collection_point.setBackground(getResources().getDrawable(R.drawable.grey_button_bg));
                bt_arrived_collection_point.setTextColor(getResources().getColor(R.color.black_shade_two));
                if (store_note_type.equals("Delivery") && isSubmittedSupplyArrivedCollection){
                    bt_complete_waste.setVisibility(View.GONE);
                    iv_next_page.setEnabled(true);
                    iv_next_page.setImageDrawable(getResources().getDrawable(R.drawable.arrow));
                }
                else if (store_note_type.equals("Delivery")) {
                    bt_complete_waste.setVisibility(View.GONE);
                }
                else {
                    bt_complete_supply.setVisibility(View.GONE);
                    bt_complete_waste.setVisibility(View.VISIBLE);
                }
                date = AppData.date();
time = AppData.Time();
                latLon();
                Log.d("Inspection Id :" ,inspection_id);
                sendArrivedTaskDetails(date,time,latitude,longitude);
                if (!AppData.internetOnline(this)){
                    progressDialog.dismiss();
                }
                break;
            case R.id.bt_abort:
                alertDialogAbortTask("Do you want to do abort this task?");
                break;
            case R.id.tv_change_vehicle_arrived_task:
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
        GPSTracker finder = new GPSTracker(ArrivedCollectionPoint.this);
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
                        intent = new Intent(ArrivedCollectionPoint.this,MainActivity.class);
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
    // update task_management table
    private void sendArrivedTaskDetails(final String task_date,final String task_time, final String latitude, final String longitude){
        progressDialog.show();
        Query query = taskManagementReference.whereEqualTo("job_id",store_job_id).whereEqualTo("task_order_no",store_task_order_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()) {
                        if (queryDocumentSnapshot.exists()) {
                            String doc_id = queryDocumentSnapshot.getId();
                            Map<String,Object> mapArrivedTask = new HashMap<>();
                            mapArrivedTask.put("arrived_collection_time",task_time);
                            mapArrivedTask.put("arrived_collection_date",task_date);
                            mapArrivedTask.put("arrived_collection_latitude",latitude);
                            mapArrivedTask.put("arrived_collection_longitude",longitude);
                            mapArrivedTask.put("arrived_collection_user_id",user_id);
                            mapArrivedTask.put("arrived_collection_inspection_id",inspection_id);
                            mapArrivedTask.put("task_status","ArrivedCollectionPoint");
                            mapArrivedTask.put("is_updated","Yes");
                            mapArrivedTask.put("time_second_format", AppData.getTimeSecond());
                            taskManagementReference.document(doc_id).update(mapArrivedTask);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ArrivedCollectionPoint.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                progressDialog.dismiss();
                Toast.makeText(ArrivedCollectionPoint.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ArrivedCollectionPoint.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

    @Override
    public void onBackPressed() {

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
        changeVehicle = new ChangeVehicle(ArrivedCollectionPoint.this,user_id,store_job_id);
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
                        intent = new Intent(ArrivedCollectionPoint.this,MainActivity.class);
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

    private void getTaskStatus(){
        taskManagementReference.document(store_job_id+"_"+store_task_order_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    task_status= documentSnapshot.getString("task_status");
                    String ticket_no = documentSnapshot.getString("ticket_no");
                    if (!ticket_no.equals("") && store_note_type.equals("Delivery"))
                        isSubmittedSupplyArrivedCollection = true;
                    else if (!ticket_no.equals("") && store_note_type.equals("Collection"))
                        isSubmittedWasteArrivedCollection = true;
                    if (task_status.equals("ArrivedCollectionPoint"))
                        isArrivedCollectionPoint = true;
                }
                saveState();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ArrivedCollectionPoint.this, "Unable to get task status.", Toast.LENGTH_SHORT).show();
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
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                updated_collection_site_id = (Integer)queryDocumentSnapshot.getLong("current_site_id_new").intValue();
                                updated_material_id = queryDocumentSnapshot.getLong("material_id_new").intValue();
                                updated_delivery_site_id = queryDocumentSnapshot.getLong("delivery_site_id_new").intValue();
                        }
                    }else {
                        Log.d("Error :", "DOES NOT EXIST");
                    }
                    Log.d("Material Id:" , String.valueOf(updated_material_id));
                    if (updated_collection_site_id != 0)
                        getUpdatedCollectionPoint(updated_collection_site_id);
                    if (updated_material_id != 0)
                        getUpdatedMaterial(updated_material_id);
                    if (updated_delivery_site_id != 0)
                        getUpdatedDeliveryPoint(updated_delivery_site_id);
                }else {
                    Log.d("Error :" ,"No material id found." );
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
                Toast.makeText(ArrivedCollectionPoint.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Log.d("ADDRESS :" , Objects.requireNonNull(store_collection_point));
                        tv_collection_point.setText(store_collection_point);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ArrivedCollectionPoint.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Log.d("ADDRESS :" , store_delivery_point);
                        tv_delivery_point.setText(store_delivery_point);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ArrivedCollectionPoint.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
