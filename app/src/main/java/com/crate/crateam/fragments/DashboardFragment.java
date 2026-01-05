package com.crate.crateam.fragments;

import static androidx.constraintlayout.widget.Constraints.TAG;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.crate.crateam.R;
import com.crate.crateam.activities.ArrivedCollectionPoint;
import com.crate.crateam.activities.ArrivedDeliveryPoint;
import com.crate.crateam.activities.AwaitingDeliveryActivity;
import com.crate.crateam.activities.JobDetailsActivity;
import com.crate.crateam.activities.LeftCollectionPoint;
import com.crate.crateam.activities.MainActivity;
import com.crate.crateam.activities.StartTasksActivity;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class DashboardFragment extends Fragment implements View.OnClickListener {
    private TextView tv_roadworthy_count,tv_defects_count,tv_awaiting_count,tv_last_refresh,tv_synced;
    private LinearLayout ll_roadworthy,ll_defects,ll_awaiting_delivery;
    Fragment fragment = null;
    private ProgressDialog progressDialog;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private SessionManager sessionManager;
    private String user_name="",current_date,inspection_id;
    private Button bt_tasks;
    private int roadworthy_count = 0,role_id=0;
    MainActivity mainActivity ;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference inspectionSubmissionReference,taskManagementReference,endOfDayReference,
            currentVehicleReference,userRole,jobManagementDetailsNewReference,supplyWasteFormReference;
    private int user_id = 0, job_id = 0, vehicle_registration_no=0,task_order_id = 0;
    private ArrayList<Integer> jobId = new ArrayList<>();
    private ArrayList<String> jobStatus = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dashboard_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initializeOnClick();
    }

    private void initializeOnClick() {
        ll_roadworthy.setOnClickListener(this);
        ll_defects.setOnClickListener(this);
        ll_awaiting_delivery.setOnClickListener(this);
        bt_tasks.setOnClickListener(this);
    }

    private void initView(View view){
        inspectionSubmissionReference = db.collection("CR_inspection_submission");
        taskManagementReference = db.collection("CR_task_management");
        endOfDayReference = db.collection("CR_end_of_day");
        currentVehicleReference = db.collection("CR_current_vehicle");
        userRole = db.collection("CR_user_role");
        jobManagementDetailsNewReference = db.collection("CR_job_management_new_details");
        supplyWasteFormReference = db.collection("CR_supply_waste_forms");
        sessionManager = new SessionManager(getActivity());
        mainActivity = (MainActivity)getActivity();
        tv_synced = view.findViewById(R.id.tv_synced);
        TextView tv_userName = view.findViewById(R.id.tv_userName);
        tv_roadworthy_count = view.findViewById(R.id.tv_roadworthy_count);
        tv_defects_count = view.findViewById(R.id.tv_defects_count);
        tv_awaiting_count = view.findViewById(R.id.tv_awaiting_count);
        tv_last_refresh = view.findViewById(R.id.tv_last_refresh);
        bt_tasks = view.findViewById(R.id.bt_tasks);
        ll_roadworthy = view.findViewById(R.id.ll_roadworthy);
        ll_defects = view.findViewById(R.id.ll_defects);
        ll_awaiting_delivery = view.findViewById(R.id.ll_awaiting_delivery);
        progressDialog = Dialog.showProgressDialog(getActivity());
        ImageView iv_refresh = getActivity().findViewById(R.id.iv_refresh);
        iv_refresh.setVisibility(View.VISIBLE);
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_id = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        user_name = user.get(SessionManager.KEY_FULL_NAME);
        String user_role = user.get(SessionManager.KEY_ROLE_ONE);
        if (user_role != null)
           role_id = Integer.parseInt(user_role);
        Log.d("UserRole :",user_name+" "+ user_role+" "+user_id);
        if (user_name != null && !user_name.isEmpty())
           tv_userName.setText(user_name);
        pref = getActivity().getSharedPreferences("MyPref", 0); //0 - for private mode
        editor = pref.edit();
        createInspectionId();
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = df.format(c);
        current_date = formattedDate;
        Log.d("Current date : " , current_date +" "+user_id);
        progressDialog.show();
        loadDashboardData();
        awaitingDeliveryCount();
        getChildMenuOptions();
        iv_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               progressDialog.show();
               loadDashboardData();
               awaitingDeliveryCount();
               getChildMenuOptions();
            }
        });
        removePreferences();
    }

    private void createInspectionId(){
        @SuppressLint("HardwareIds") String uniqueID = Settings.Secure.getString(getActivity().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        String currentDateAndTime = sdf.format(new Date());
        Log.d("UUID :" ,uniqueID+currentDateAndTime);
        String inspection_id_first = uniqueID.substring(0,4);
        String inspection_id_last = currentDateAndTime;
        inspection_id =  inspection_id_first+inspection_id_last;
        editor.putString("inspection_id", inspection_id);
        editor.apply();
    }

    @Override
    public void onClick(View view) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        MainActivity mainActivity= (MainActivity) getActivity();
        switch (view.getId()){
            case R.id.ll_roadworthy:
                if (role_id!=5) {
                    fragment = new RoadWorthyFragment();
                    ft.replace(R.id.content_frame, fragment, "ROADWORTHY");
                    ft.addToBackStack("ROADWORTHY");
                    ft.commit();
                    mainActivity.getChildTag(fragment.getTag());
                }
                break;
            case R.id.ll_defects:
                if (role_id!=5) {
                    fragment = new DefectedVehicleFragment();
                    ft.replace(R.id.content_frame, fragment, "DEFECT VEHICLE");
                    ft.addToBackStack("DEFECT VEHICLE");
                    ft.commit();
                    mainActivity.getChildTag(fragment.getTag());
                }
                break;
            case R.id.ll_awaiting_delivery:
                Intent intent = new Intent(getActivity(), AwaitingDeliveryActivity.class);
                startActivity(intent);
                break;
            case R.id.iv_back_arrow:
                fragment = new DashboardFragment();
                ft.replace(R.id.content_frame,fragment,"DASHBOARD");
                ft.addToBackStack("DASHBOARD");
                ft.commit();
                mainActivity.getChildTag(fragment.getTag());
                break;
            case R.id.bt_tasks:
                currentVehicleJob();
                break;
        }
    }

    private  void currentVehicleJob(){
        progressDialog.show();
        Query query = currentVehicleReference.whereEqualTo("user_id",user_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                        if (queryDocumentSnapshot.exists()){
                            vehicle_registration_no = queryDocumentSnapshot.getLong("vehicle_id").intValue();
                            Log.d("VEHICLE ID:" , String.valueOf(vehicle_registration_no));
                            sessionManager.updateVehicleId(vehicle_registration_no);
                            updateJobManagement(user_id,vehicle_registration_no);
                        }
                    }
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

    public void jobManagement(final int vehicle_registration_no){
        Query queryJobManagement = jobManagementDetailsNewReference.whereEqualTo("assign_driver_id",user_id).whereEqualTo("status","Active")
                .whereEqualTo("vehicle_registration_no_id",vehicle_registration_no);
        queryJobManagement.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    progressDialog.dismiss();
                    if (task.getResult().isEmpty()){
                        Dialog.endOfDayDialog(getActivity());
                    }else {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (queryDocumentSnapshot.exists()) {
                                String show_job_date = queryDocumentSnapshot.getString("show_job_date");
                                int time_second_format = Integer.parseInt(AppData.changeDateFormat(show_job_date));
                                if (time_second_format <= AppData.getDateMonthYearFormat()) {
                                    int job_id = queryDocumentSnapshot.getLong("job_id").intValue();
                                    String job_status = queryDocumentSnapshot.getString("status");
                                    jobId.add(job_id);
                                    jobStatus.add(job_status);
                                }
                            }
                        }
                        Log.d("Job Ids:" ,jobId+"  "+vehicle_registration_no);
                        if (jobId.isEmpty()){
                            Dialog.endOfDayDialog(getActivity());
                        }else{
                            getCurrentJob(vehicle_registration_no);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCurrentJob(int vehicle_registration_no){
        task_order_id = 0;
        Query query= jobManagementDetailsNewReference.whereEqualTo("current_task","Yes").whereEqualTo("vehicle_registration_no_id",vehicle_registration_no)
                .whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                        if (queryDocumentSnapshot.exists()) {
                            task_order_id = Objects.requireNonNull(queryDocumentSnapshot.getLong("task_order_no")).intValue();
                            job_id = Objects.requireNonNull(queryDocumentSnapshot.getLong("job_id")).intValue();
                        }
                    }
                    Log.d("TASK ORDER ID:" ,task_order_id+" "+job_id);
                    if (task_order_id==0){
                        Intent intent = new Intent(getActivity(), JobDetailsActivity.class);
                        startActivity(intent);
                    }else {
                        editor.putInt("job_id",job_id);
                        editor.putInt("task_order_id",task_order_id);
                        editor.commit();
                        taskStatus(task_order_id);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(),"Unable to get task order numbers.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void taskStatus(int task_order_id){
        Query query = taskManagementReference.whereEqualTo("job_id",job_id).whereEqualTo("task_order_no",task_order_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                String task_status_detail="";
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot :task.getResult()){
                        task_status_detail =  queryDocumentSnapshot.getString("task_status");
                    }
                }
                Log.d("STATUS :", Objects.requireNonNull(task_status_detail));
                assert task_status_detail != null;
                showTask(task_status_detail);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showTask(String status){
        Intent intent;
        Log.d("Task status dddd : " ,status);
        if (status.equals("")){
            intent = new Intent(getActivity(), StartTasksActivity.class);
            intent.putExtra("startTaskClicked",false);
            startActivity(intent);
        }
        else if (status.equals("StartTask")) {
            intent = new Intent(getActivity(), StartTasksActivity.class);
            startActivity(intent);
            editor.putBoolean("startTaskClicked",true);
            editor.commit();
        }
        else if (status.equals("ArrivedCollectionPoint")){
            intent = new Intent(getActivity(), ArrivedCollectionPoint.class);
            startActivity(intent);
            editor.putBoolean("arrivedCollectionClicked",true);
            editor.commit();
        }
        else if (status.equals("LeftCollectionPoint")){
            intent = new Intent(getActivity(), LeftCollectionPoint.class);
            startActivity(intent);
            editor.putBoolean("leftCollectionClicked",true);
            editor.commit();
        }
        else if (status.equals("ArrivedDeliveryPoint")){
            intent = new Intent(getActivity(), ArrivedDeliveryPoint.class);
            startActivity(intent);
            editor.putBoolean("arrivedDeliveryClicked",true);
            editor.commit();
        }
    }
    private void loadDashboardData(){
        Query roadworthyCount;
        if (role_id==4)
        roadworthyCount = inspectionSubmissionReference.whereEqualTo("logged_by",user_id).whereEqualTo("vehicle_defect","No")
                .whereEqualTo("trailer_defect","No") .whereEqualTo("conducted_on",current_date);
        else
            roadworthyCount = inspectionSubmissionReference.whereEqualTo("vehicle_defect","No")
                    .whereEqualTo("trailer_defect","No") .whereEqualTo("conducted_on",current_date);
        roadworthyCount.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                roadworthy_count = queryDocumentSnapshots.size();
                if (role_id==5)
                    tv_roadworthy_count.setText("0");
                else
                    tv_roadworthy_count.setText(String.valueOf(roadworthy_count));
                Log.d("ROADWORTHY LIST SIZE TODAY :" , String.valueOf(roadworthy_count));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
            }
        });
        defectCount();
         //get current time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String refresh_time = dateFormatter.format(calendar.getTime());
        tv_last_refresh.setText("Last refresh : "+refresh_time);
        if (role_id==4)
            loadTask();
    }

    private void defectCount(){
        Query defectCount;
        if (role_id == 4)
        defectCount = inspectionSubmissionReference.whereEqualTo("driver_id",user_id).whereEqualTo("defected","Yes")
                .whereEqualTo("close_issue","No").whereEqualTo("conducted_on",current_date);
        else
            defectCount = inspectionSubmissionReference.whereEqualTo("defected","Yes").whereEqualTo("close_issue","No");
        defectCount.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                progressDialog.dismiss();
                if (role_id==5)
                    tv_defects_count.setText("0");
                else
                    tv_defects_count.setText(String.valueOf(queryDocumentSnapshots.size()));
                Log.d("DEFECT LIST SIZE :" , String.valueOf(queryDocumentSnapshots.size()));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
            }
        });
    }

    private void awaitingDeliveryCount(){
        Query query = supplyWasteFormReference.whereEqualTo("job_id",0)
                .whereEqualTo("ticket_status","Open").whereEqualTo("ticket_type","Collection");
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                tv_awaiting_count.setText(String.valueOf(queryDocumentSnapshots.size()));
                Log.d("AWAITING LIST SIZE :" , String.valueOf(queryDocumentSnapshots.size()));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTask(){
        Query taskQuery = inspectionSubmissionReference.whereEqualTo("logged_by",user_id).whereEqualTo("conducted_on",current_date);
        taskQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty())
                   bt_tasks.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Task Error :", "Unable to load tasks.");
            }
        });
    }

    private void updateJobManagement(final int user_id, final int vehicle_id){
        Query query = jobManagementDetailsNewReference.whereEqualTo("vehicle_registration_no_id",vehicle_id)
                .whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                        if (queryDocumentSnapshot.exists()) {
                            String doc_id = queryDocumentSnapshot.getId();
                            Log.d("Doc ID: " ,doc_id);
                            Map<String,Object> userJob = new HashMap<>();
                            userJob.put("assign_driver_id",user_id);
                            userJob.put("is_updated","Yes");
                            userJob.put("time_second_format",AppData.getDateMonthYearFormat());
                            jobManagementDetailsNewReference.document(doc_id).update(userJob);
                        }
                    }
                    jobManagement(vehicle_id);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Unable to update user id.");
            }
        });
    }

    private void removePreferences(){
        editor.remove("nfc1");
        editor.remove("nfc2");
        editor.remove("nfc3");
        editor.remove("nfc4");
        editor.remove("nfc5");
        editor.remove("nfc6");
        editor.apply();
        int count = 0;
        mainActivity.getCount(count);
    }

    private void getChildMenuOptions(){
        userRole.document(String.valueOf(role_id)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    progressDialog.dismiss();
                    if (documentSnapshot.exists()) {
                        String child_menu_options = documentSnapshot.getString("child_menu_options");
                        Log.d("CHILD MENU :" , Objects.requireNonNull(child_menu_options));
                        accessChildMenuOptions(child_menu_options);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void accessChildMenuOptions(String contain_child_menu_options){
        String nfc_tag_manager = "",vehicle_inspection = "",view_inspection = "",manager_comment="",ad_hoc_defect_report="",view_ad_hoc="",waste_transfer_form_operator="",
                waste_transfer_form_driver="",supply_form_operator="",supply_form_driver="",diesel_delivery="",service_driver_note="",assign_site="",view_reports="";
        if (contain_child_menu_options.contains("24"))
            nfc_tag_manager = "yes";
        if (contain_child_menu_options.contains("25"))
            vehicle_inspection = "yes";
        if (contain_child_menu_options.contains("26"))
            view_inspection = "yes";
        if (contain_child_menu_options.contains("27"))
            manager_comment = "yes";
        if (contain_child_menu_options.contains("28"))
            ad_hoc_defect_report = "yes";
        if (contain_child_menu_options.contains("29"))
            view_ad_hoc = "yes";
        if (contain_child_menu_options.contains("30"))
            waste_transfer_form_operator = "yes";
        if (contain_child_menu_options.contains("31"))
            waste_transfer_form_driver = "yes";
        if (contain_child_menu_options.contains("32"))
            supply_form_operator = "yes";
        if (contain_child_menu_options.contains("33"))
            supply_form_driver = "yes";
        if (contain_child_menu_options.contains("34"))
            diesel_delivery = "yes";
        if (contain_child_menu_options.contains("35"))
            service_driver_note = "yes";
        if (contain_child_menu_options.contains("36"))
            assign_site = "yes";
        if (contain_child_menu_options.contains("37"))
            view_reports = "yes";
        sessionManager.createAppViewControllerSession(nfc_tag_manager, vehicle_inspection, view_inspection,manager_comment,ad_hoc_defect_report,view_ad_hoc,
                waste_transfer_form_operator,waste_transfer_form_driver,supply_form_operator,supply_form_driver,diesel_delivery,service_driver_note,assign_site,view_reports);
        mainActivity.getViewControllerData(nfc_tag_manager, vehicle_inspection, view_inspection,manager_comment,ad_hoc_defect_report,view_ad_hoc,
                waste_transfer_form_operator,waste_transfer_form_driver,supply_form_operator,supply_form_driver,diesel_delivery,service_driver_note,assign_site,view_reports);
    }
}
