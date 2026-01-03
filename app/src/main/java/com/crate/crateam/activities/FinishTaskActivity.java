package com.crate.crateam.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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

public class FinishTaskActivity  extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private ImageView iv_cross;
    private Button bt_add_fuel, bt_submit, bt_abort,bt_edit_waste_transfer_note,bt_edit_supply,bt_submit_new,bt_exit;
    private Intent intent;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean isSubmitted = false,isSubmittedWaste = false,finishAllTask = false,finishTaskClicked=false;
    private TextView tv_name,tv_role,tv_vehicle,tv_ticket_type,tv_change_vehicle_finish,tv_collection_point,tv_material,tv_delivery_point;
    private ProgressDialog progressDialog;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference taskManagementReference,jobManagementDetailsNewReference,inspectionSubmissionReference,materialReference,
            supplyWasteFormReference,siteManagementReference;
    private int user_id=0,store_job_id=0,store_delivery_addressManagementId =0,store_delivery_site_id=0,store_task_order_id=0,
            store_material_id=0,vehicle_number=0,reg_no_id=0,updated_collection_address_id=0,updated_collection_site_id=0,
            updated_material_id=0, updated_delivery_address_id=0,updated_delivery_site_id=0;
    private String user_name="",date="",time="",latitude="",longitude="",store_note_type="",store_material_description="",store_material_type="",
            store_collection_point="",store_delivery_point="",reg_no="",store_ewc_code="",inspection_id="",ticket_no="",
            updated_collection_site_name="", store_delivery_site_name="",updated_delivery_site_name="",email_pdf_logo="";
    private CollectionDeliveryPoint collectionDeliveryPoint;
    private AlertDialog alertDialog;
    private CustomSearchableSpinner sp_change_vehicle;
    private ChangeVehicle changeVehicle;
    private ArrayList<String> store_array_list_reg_no;
    private ArrayList<Integer> store_array_list_reg_no_id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finish_task_layout);
        FirestoreManager.initPersistentIndexManager();
        initView();
    }

    private void initView() {
        iv_cross = findViewById(R.id.iv_cross);
        bt_add_fuel = findViewById(R.id.bt_add_fuel);
        bt_submit = findViewById(R.id.bt_submit);
        bt_abort = findViewById(R.id.bt_abort);
        bt_edit_waste_transfer_note = findViewById(R.id.bt_edit_waste_transfer_note);
        bt_edit_supply = findViewById(R.id.bt_edit_supply);

        tv_ticket_type = findViewById(R.id.tv_ticket_type);
        tv_change_vehicle_finish = findViewById(R.id.tv_change_vehicle_finish);
        tv_name = findViewById(R.id.tv_name);
        tv_role = findViewById(R.id.tv_role);
        tv_vehicle = findViewById(R.id.tv_vehicle);

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

        progressDialog = Dialog.showProgressDialog(this);
        progressDialog.show();
        tv_material = findViewById(R.id.tv_material);
        tv_collection_point = findViewById(R.id.tv_collection_point);
        tv_delivery_point = findViewById(R.id.tv_delivery_point);
        sharedPreferences = getApplicationContext().getSharedPreferences("MyPref",0);
        editor = sharedPreferences.edit();

        taskManagementReference = db.collection("CR_task_management");
        inspectionSubmissionReference = db.collection("CR_inspection_submission");
        materialReference = db.collection("CR_material");
        supplyWasteFormReference = db.collection("CR_supply_waste_forms");
        siteManagementReference = db.collection("CR_site_management");
        jobManagementDetailsNewReference = db.collection("CR_job_management_new_details");
        collectionDeliveryPoint = new CollectionDeliveryPoint(FinishTaskActivity.this,user_id,vehicle_number);
        // offline support for collectionDeliveryPoint class
        collectionDeliveryPoint.enableOfflineSupportCommon();
        collectionDeliveryPoint.jobManagement();
        CollectionDeliveryPoint.startTask =new StartTask() {
            @Override
            public void getDetails(int collection_site_id,String collection_site_name,String collection_address, String delivery_address,int delivery_site_id, int job_id, int task_order_id, int material_id,String material_despcription,
                                   String ewc_code,String material_value,String note_type,String sic_code, String carrier_no,String permit_no_collection,String permit_no_delivery,String site_type,String material_type,String project_no) {
                store_collection_point = collection_address;
                store_delivery_point = delivery_address;
                store_delivery_addressManagementId = delivery_site_id;
                store_job_id = job_id;
                store_task_order_id = task_order_id;
                store_material_id = material_id;
                store_material_description = material_despcription;
                store_ewc_code = ewc_code;
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
                if (store_note_type.equals("Delivery"))
                    bt_edit_supply.setVisibility(View.VISIBLE);
                else
                    bt_edit_waste_transfer_note.setVisibility(View.GONE);
                getStoredDeliverySiteId(store_delivery_addressManagementId);
                fetchUpdatedMaterial();
                getVehicleType();
                fetchTicketNo();
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
    private void initializeOnClick() {
        iv_cross.setOnClickListener(this);
        bt_add_fuel.setOnClickListener(this);
        bt_submit.setOnClickListener(this);
        bt_abort.setOnClickListener(this);
        bt_edit_waste_transfer_note.setOnClickListener(this);
        bt_edit_supply.setOnClickListener(this);
        tv_change_vehicle_finish.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_cross:
                alertDialogToDashBoard("Do you want to leave this task ?");
                break;
            case R.id.bt_edit_waste_transfer_note:
                isSubmittedWaste = true;
                intent = new Intent(FinishTaskActivity.this, com.crate.crateam.activities.WasteTransferFormDriver.class);
                startActivity(intent);
                editor.putString("waste_coming_form","FinishTask");
                editor.putBoolean("isSubmittedWaste",isSubmittedWaste);
                editor.apply();
                break;
            case R.id.bt_edit_supply:
                editor.putString("supply_coming_form","FinishTask");
                editor.putBoolean("isSubmitted",isSubmitted);
                editor.apply();
                intent = new Intent(FinishTaskActivity.this, SupplyFormDriver.class);
                startActivity(intent);
                finish();
                break;
            case R.id.bt_add_fuel:
                intent = new Intent(FinishTaskActivity.this, com.crate.crateam.activities.EndOfDayActivity.class);
                startActivity(intent);
                break;
            case R.id.bt_submit:
                finishTaskClicked=true;
                bt_submit.setEnabled(false);
                bt_submit.setBackground(getResources().getDrawable(R.drawable.grey_button_bg));
                bt_submit.setTextColor(getResources().getColor(R.color.black_shade_two));
                iv_cross.setEnabled(false);
                bt_abort.setEnabled(false);
                bt_edit_supply.setEnabled(false);
                bt_edit_waste_transfer_note.setEnabled(false);
                tv_change_vehicle_finish.setEnabled(false);
                date = AppData.date();
time = AppData.Time();
                latLon();
                Log.d("Inspection Id :" ,inspection_id);
                sendFinishTaskDetails(date,time,latitude,longitude,"FinishTask");
                updateSupplyWasteForm();
                Log.d("Complete Status Value 1:" , String.valueOf(finishAllTask));
                if (!AppData.internetOnline(this))
                    progressDialog.dismiss();
                break;
            case R.id.bt_abort:
                alertDialogAbortTask("Do you want to do abort this task?");
                break;
            case R.id.tv_change_vehicle_finish:
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
    protected void onResume() {
        super.onResume();
    }

    private void dateTime(){
        // get current date
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        date = df.format(c);
        //get current time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        time = simpleDateFormat.format(calendar.getTime());
    }

    private void latLon(){
        GPSTracker finder = new GPSTracker(FinishTaskActivity.this);
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
                        intent = new Intent(FinishTaskActivity.this,MainActivity.class);
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
    private void alertDialogFinishTask(String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (finishAllTask)
                            intent = new Intent(FinishTaskActivity.this,MainActivity.class);
                        else
                            intent = new Intent(FinishTaskActivity.this, JobDetailsActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
        alertDialog.show();
    }

    // update task_management table
    private void sendFinishTaskDetails(final String task_date,final String task_time, final String latitude, final String longitude,final String status){
        progressDialog.show();
        Query query = taskManagementReference.whereEqualTo("job_id",store_job_id).whereEqualTo("task_order_no",store_task_order_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()) {
                        if (queryDocumentSnapshot.exists()) {
                            String doc_id = queryDocumentSnapshot.getId();
                            Map<String,Object> mapFinishTask = new HashMap<>();
                            mapFinishTask.put("finish_task_time",task_time);
                            mapFinishTask.put("finish_task_date",task_date);
                            mapFinishTask.put("finish_task_latitude",latitude);
                            mapFinishTask.put("finish_task_longitude",longitude);
                            mapFinishTask.put("material_description",store_material_description);
                            mapFinishTask.put("finish_task_user_id",user_id);
                            mapFinishTask.put("finish_task_inspection_id",inspection_id);
                            mapFinishTask.put("task_status",status);
                            mapFinishTask.put("is_updated","Yes");
                            mapFinishTask.put("time_second_format", AppData.getTimeSecond());
                            taskManagementReference.document(doc_id).update(mapFinishTask);
                            if (AppData.internetOnline(FinishTaskActivity.this)) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateJobManagementDetails();
                                    }
                                }, 2000);
                            }else
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
                Toast.makeText(FinishTaskActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

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
                Toast.makeText(FinishTaskActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateJobManagementDetails(){
        Query queryJobManagementDetailsOne = jobManagementDetailsNewReference.whereEqualTo("job_id",store_job_id).whereEqualTo("task_order_no",store_task_order_id).whereEqualTo("status","Active");
        queryJobManagementDetailsOne.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    progressDialog.dismiss();
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
                    checkJobStatus();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(FinishTaskActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void checkJobStatus(){
        Log.d("Vehicle no:" ,vehicle_number +" "+user_id);
        Query queryJobManagement = jobManagementDetailsNewReference.whereEqualTo("assign_driver_id",user_id).whereEqualTo("status","Active")
                .whereEqualTo("vehicle_registration_no_id",vehicle_number);
        queryJobManagement.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().isEmpty()){
                        finishAllTask = true;
                        progressDialog.dismiss();
                        Log.d("Complete Status Value 2:" , String.valueOf(finishAllTask));
                    }
                    if (finishTaskClicked)
                        alertDialogFinishTask("Task completed successfully.");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Error :", Objects.requireNonNull(e.getMessage()));
            }
        });
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
        store_array_list_reg_no = new ArrayList<>();
        store_array_list_reg_no_id = new ArrayList<>();
        changeVehicle = new ChangeVehicle(FinishTaskActivity.this,user_id,store_job_id);
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

    private void alertDialogToDashBoard(String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        intent = new Intent(FinishTaskActivity.this,MainActivity.class);
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

    private void fetchUpdatedMaterial(){
        Query query = supplyWasteFormReference.whereEqualTo("job_id",store_job_id).whereEqualTo("task_order_id",store_task_order_id)
                .whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        updated_collection_address_id = queryDocumentSnapshot.getLong("current_site_id_new").intValue();
                        updated_material_id = queryDocumentSnapshot.getLong("material_id_new").intValue();
                        updated_delivery_address_id = queryDocumentSnapshot.getLong("delivery_site_id_new").intValue();
                    }
                    Log.d("Updated Ids:" ,updated_collection_site_id+"  "+updated_material_id+" "+updated_delivery_site_id);
                    if (updated_collection_address_id != 0) {
                        getUpdatedCollectionPoint(updated_collection_address_id);
                    }if (updated_material_id != 0)
                        getUpdatedMaterial(updated_material_id);
                    if (updated_delivery_address_id != 0) {
                        getUpdatedDeliveryPoint(updated_delivery_address_id);
                    }
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
                Toast.makeText(FinishTaskActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUpdatedCollectionPoint(int collection_site_id){
        int site_id;
        Query queryAddressManagement = siteManagementReference.whereEqualTo("id",collection_site_id).whereEqualTo("status","Active");
        queryAddressManagement.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            store_collection_point = queryDocumentSnapshot.getString("address");
                            updated_collection_site_id = queryDocumentSnapshot.getLong("id").intValue();
                        }
                        Log.d("Collection address :" , store_collection_point);
                        tv_collection_point.setText(store_collection_point);
                        getUpdatedCollectionSiteName(updated_collection_site_id);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FinishTaskActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUpdatedCollectionSiteName(int site_id){
        Query siteManagement = siteManagementReference.whereEqualTo("id",site_id).whereEqualTo("status","Active");
        siteManagement.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            updated_collection_site_name = queryDocumentSnapshot.getString("site_name");
                        }
                        Log.d("Collection Site Name:" , Objects.requireNonNull(updated_collection_site_name));
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FinishTaskActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            updated_delivery_site_id = queryDocumentSnapshot.getLong("id").intValue();
                        }
                        Log.d("Delivery address :" , store_delivery_point);
                        tv_delivery_point.setText(store_delivery_point);
                        getUpdatedDeliverySiteName(updated_delivery_site_id);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FinishTaskActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getStoredDeliverySiteId(int address_management_id){
        Query queryAddressManagement = siteManagementReference.whereEqualTo("id",address_management_id).whereEqualTo("status","Active");
        queryAddressManagement.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            store_delivery_site_id = queryDocumentSnapshot.getLong("id").intValue();
                        }
                        Log.d("Delivery address :" , store_delivery_point);
                        tv_delivery_point.setText(store_delivery_point);
                        getStoredDeliverySiteName(store_delivery_site_id);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FinishTaskActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getStoredDeliverySiteName(int site_id){
        Query siteManagement = siteManagementReference.whereEqualTo("id",site_id).whereEqualTo("status","Active");
        siteManagement.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            store_delivery_site_name = queryDocumentSnapshot.getString("site_name");
                        }
                        Log.d("Store Delivery Site Name:" , Objects.requireNonNull(store_delivery_site_name));
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FinishTaskActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUpdatedDeliverySiteName(int site_id){
        Query siteManagement = siteManagementReference.whereEqualTo("id",site_id).whereEqualTo("status","Active");
        siteManagement.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            updated_delivery_site_name = queryDocumentSnapshot.getString("site_name");
                        }
                        Log.d("Updated Delivery Site Name:" , Objects.requireNonNull(updated_delivery_site_name));
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FinishTaskActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTicketNo(){
        Query query = supplyWasteFormReference.whereEqualTo("job_id",store_job_id).whereEqualTo("task_order_id",store_task_order_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()) {
                        if (queryDocumentSnapshot.exists()) {
                            ticket_no = queryDocumentSnapshot.getString("ticket_no");
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FinishTaskActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSupplyWasteForm(){
        Query query = supplyWasteFormReference.whereEqualTo("ticket_no",ticket_no);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                            String doc_id = queryDocumentSnapshot.getId();
                            Map<String,Object> updateSupplyWasteForm= new HashMap<>();
                            updateSupplyWasteForm.put("mail_send","No");
                            updateSupplyWasteForm.put("pdf_send","No");
                            supplyWasteFormReference.document(doc_id).update(updateSupplyWasteForm);
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
}

