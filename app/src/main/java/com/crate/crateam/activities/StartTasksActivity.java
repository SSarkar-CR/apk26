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

public class StartTasksActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private ImageView iv_cross,iv_next_page;
    private TextView tv_name,tv_role,tv_vehicle,tv_ticket_type,tv_change_vechicle,tv_collection_point,tv_material,tv_delivery_point;
    private Button bt_add_fuel,bt_start_task,bt_abort,bt_submit_new,bt_exit;
    private Intent intent;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private CollectionReference taskManagementReference,inspectionSubmissionReference,jobManagementDetailsNewReference,
            sortKeyTableReference;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private ProgressDialog progressDialog;
    private int user_id=0, user_role_id=0,store_job_id=0,store_task_order_id=0,store_delivery_site_id,store_material_id=0,vehicle_number=0,
            reg_no_id=0,doc_id_length=0;
    private String user_name="",date="",time="",store_note_type="",store_material_description="",store_ewc_code="",store_material_type="",store_collection_point="",
            store_delivery_point="",latitude="",longitude="",reg_no="", doc_id="",inspection_id="",task_status="";
    private CollectionDeliveryPoint collectionDeliveryPoint;
    private Boolean isSubmitted = false;
    private AlertDialog alertDialog;
    private CustomSearchableSpinner sp_change_vehicle;
    private ChangeVehicle changeVehicle;
    private ArrayList<String> store_array_list_reg_no;
    private ArrayList<Integer> store_array_list_reg_no_id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_task_layout);
        FirestoreManager.initPersistentIndexManager();
        initView();
    }

    private void initView(){
        sharedPreferences = getApplicationContext().getSharedPreferences("MyPref",0);
        editor = sharedPreferences.edit();
        SessionManager sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_id = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
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

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        date = df.format(c);

        HashMap<String, String> vehicle = sessionManager.getVehicleDetails();
        if (vehicle.get(SessionManager.KEY_VEHICLE_ID) != null)
            vehicle_number = Integer.parseInt((Objects.requireNonNull(vehicle.get(SessionManager.KEY_VEHICLE_ID))));
        iv_cross = findViewById(R.id.iv_cross);
        iv_next_page = findViewById(R.id.iv_next_page);
        tv_material = findViewById(R.id.tv_material);
        tv_collection_point = findViewById(R.id.tv_collection_point);
        tv_delivery_point = findViewById(R.id.tv_delivery_point);
        tv_ticket_type = findViewById(R.id.tv_ticket_type);

        tv_name = findViewById(R.id.tv_name);
        tv_role = findViewById(R.id.tv_role);
        tv_vehicle = findViewById(R.id.tv_vehicle);

        tv_change_vechicle = findViewById(R.id.tv_change_vehicle);
        bt_add_fuel=findViewById(R.id.bt_add_fuel);
        bt_start_task = findViewById(R.id.bt_start_task);
        bt_abort = findViewById(R.id.bt_abort);

        progressDialog = Dialog.showProgressDialog(this);
        progressDialog.show();
        taskManagementReference = db.collection("CR_task_management");
        inspectionSubmissionReference = db.collection("CR_inspection_submission");
        jobManagementDetailsNewReference = db.collection("CR_job_management_new_details");
        sortKeyTableReference = db.collection("CR_sort_key");
        collectionDeliveryPoint = new CollectionDeliveryPoint(StartTasksActivity.this,user_id,vehicle_number);
        collectionDeliveryPoint.enableOfflineSupportCommon();
        collectionDeliveryPoint.jobManagement();

        CollectionDeliveryPoint.startTask =new StartTask() {
            @Override
            public void getDetails(int collection_site_id,String collection_site_name,String collection_address, String delivery_address,int delivery_site_id, int job_id, int task_order_id, int material_id,String material_despcription,
                                   String ewc_code,String material_value,String note_type,String sic_code, String carrier_no,String permit_no_collection,String permit_no_delivery,String site_type,String material_type,String project_no) {
                 store_collection_point = collection_address;
                 store_delivery_point = delivery_address;
                 store_delivery_site_id = delivery_site_id;
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
                 getTaskStatus();
                 getVehicleType();
                 progressDialog.dismiss();
                 Log.d("GET DATA Start task :",store_collection_point+" "+store_delivery_point +" "+store_job_id+" "+store_task_order_id);
            }
        };
        tv_name.setText(user_name);
        tv_role.setText(user_role_first);
        collectionDeliveryPoint.fetchVehicleRegistrationNumber(tv_vehicle);
        initializeOnClick();
        getMaximumLength();
        fetchInspectionId();
    }

    private void saveState(){
        Log.d("IS CLICKED:" , String.valueOf(isSubmitted));
        if (isSubmitted) {
            iv_next_page.setEnabled(true);
            iv_next_page.setImageDrawable(getResources().getDrawable(R.drawable.arrow));
            bt_start_task.setBackground(getResources().getDrawable(R.drawable.grey_button_bg));
            bt_start_task.setTextColor(getResources().getColor(R.color.black_shade_two));
            bt_start_task.setEnabled(false);
            bt_abort.setVisibility(View.VISIBLE);
            bt_abort.setEnabled(true);
        } else {
            iv_next_page.setEnabled(false);
            iv_next_page.setImageDrawable(getResources().getDrawable(R.drawable.arrow_unselect));
            bt_start_task.setBackground(getResources().getDrawable(R.drawable.login_button_background));
            bt_start_task.setTextColor(getResources().getColor(R.color.white));
            bt_start_task.setEnabled(true);
            bt_abort.setVisibility(View.INVISIBLE);
            bt_abort.setEnabled(false);
        }
    }

    private void initializeOnClick(){
        iv_cross.setOnClickListener(this);
        bt_add_fuel.setOnClickListener(this);
        bt_start_task.setOnClickListener(this);
        bt_abort.setOnClickListener(this);
        iv_next_page.setOnClickListener(this);
        tv_change_vechicle.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_cross:
                alertDialogToDashBoard("Do you want to leave this task ?");
                break;
            case R.id.bt_add_fuel:
                intent = new Intent(StartTasksActivity.this,EndOfDayActivity.class);
                startActivity(intent);
                break;
            case R.id.bt_start_task:
                editor.putBoolean("startTaskClicked",true);
                editor.apply();
                iv_next_page.setEnabled(true);
                iv_next_page.setImageDrawable(getResources().getDrawable(R.drawable.arrow));
                bt_start_task.setBackground(getResources().getDrawable(R.drawable.grey_button_bg));
                bt_start_task.setTextColor(getResources().getColor(R.color.black_shade_two));
                bt_start_task.setEnabled(false);
                bt_abort.setVisibility(View.VISIBLE);
                bt_abort.setEnabled(true);
                date = AppData.date();
time = AppData.Time();
                latLon();
                sendTaskData(date,time);
                updateJobManagementDetailsCurrentTask();
                if (!AppData.internetOnline(this))
                    progressDialog.dismiss();
                Intent toArrivedTask = new Intent(this, com.crate.crateam.activities.ArrivedCollectionPoint.class);
                startActivity(toArrivedTask);
                break;
            case R.id.bt_abort:
                alertDialogAbortTask("Do you want to do abort this task?");
                break;
            case R.id.iv_next_page:
                 Intent arrive_task = new Intent(this, com.crate.crateam.activities.ArrivedCollectionPoint.class);
                 startActivity(arrive_task);
                break;
            case R.id.tv_change_vehicle:
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
        GPSTracker finder = new GPSTracker(StartTasksActivity.this);
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

    private void sendTaskData(String task_date,String task_time){
        Log.d("Inspection Id ADCE:" ,inspection_id);
        Map<String,Object> mapTask = new HashMap<>();
        mapTask.put("job_id",store_job_id);
        mapTask.put("task_order_no",store_task_order_id);
        mapTask.put("assigned_vehicle_id",vehicle_number);
        mapTask.put("assigned_driver_id",user_id);
        mapTask.put("collection_point",store_collection_point);
        mapTask.put("delivery_point",store_delivery_point);
        mapTask.put("ewc_code",store_ewc_code);
        mapTask.put("material_type",store_material_type);
        mapTask.put("material_id",store_material_id);
        mapTask.put("task_date",task_date);
        mapTask.put("task_status","StartTask");
                //start task activity
        mapTask.put("start_task_time",task_time);
        mapTask.put("start_task_date",task_date);
        mapTask.put("start_task_latitude",latitude);
        mapTask.put("start_task_longitude",longitude);
        mapTask.put("start_task_user_id",user_id);
        mapTask.put("start_task_inspection_id",inspection_id);
                // arrived at collection point activity
        mapTask.put("arrived_collection_time","");
        mapTask.put("arrived_collection_date","");
        mapTask.put("arrived_collection_latitude","");
        mapTask.put("arrived_collection_longitude","");
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
        mapTask.put("id",store_job_id+"_"+store_task_order_id);
        doc_id_length =doc_id_length+1;
        mapTask.put("sort_key",doc_id_length);
        mapTask.put("ticket_no","");
        mapTask.put("form_edited","No");
        mapTask.put("inspection_id",inspection_id);
        mapTask.put("is_updated","Yes");
        mapTask.put("time_second_format", AppData.getTimeSecond());
        mapTask.put("vehicle_id",vehicle_number);
        mapTask.put("material_type",store_material_type);
        Log.e("document_id",store_job_id+"_"+store_task_order_id);
        Log.e("document_id",store_job_id+"_"+store_task_order_id);
        progressDialog.show();
        taskManagementReference.document(store_job_id+"_"+store_task_order_id).set(mapTask).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    Map<String,Object> objectMap_update = new HashMap<>();
                    objectMap_update.put("task_management_key",doc_id_length);
                    sortKeyTableReference.document(doc_id).update(objectMap_update);
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
                        Log.d("Inspection Id :" , Objects.requireNonNull(inspection_id));
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
                            mapAbortTask.put("abort_task_time",task_date);
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
                Toast.makeText(StartTasksActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTaskStatus(){
        taskManagementReference.document(store_job_id+"_"+store_task_order_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    task_status= documentSnapshot.getString("task_status");
                    if (task_status.equals("StartTask"))
                        isSubmitted = true;
                }
                saveState();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(StartTasksActivity.this, "Unable to get task status.", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(StartTasksActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateJobManagementDetailsCurrentTask(){
        Query queryJobManagementDetailsOne = jobManagementDetailsNewReference.whereEqualTo("job_id",store_job_id).whereEqualTo("task_order_no",store_task_order_id).whereEqualTo("status","Active");
        queryJobManagementDetailsOne.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        if (queryDocumentSnapshot.exists()){
                            String doc_id = queryDocumentSnapshot.getId();
                            Map<String,Object> updateJobDetails= new HashMap<>();
                            updateJobDetails.put("current_task","Yes");
                            jobManagementDetailsNewReference.document(doc_id).update(updateJobDetails);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(StartTasksActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
    private void alertDialogAbortTask(String message){
        AlertDialog.Builder alertDialog=  new  AlertDialog.Builder(this);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton( "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        date = AppData.date();
time = AppData.Time();
                        latLon();
                        sendAbortTaskDetails(date,time,latitude,longitude);
                        finish();
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
        changeVehicle = new ChangeVehicle(StartTasksActivity.this,user_id,store_job_id);
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
    // sort key table
    private void getMaximumLength(){
        sortKeyTableReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if (queryDocumentSnapshot.contains("task_management_key")){
                                doc_id_length = queryDocumentSnapshot.getLong("task_management_key").intValue();
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
                Toast.makeText(StartTasksActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void alertDialogToDashBoard(String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        intent = new Intent(StartTasksActivity.this,MainActivity.class);
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
}
