package com.crate.crateam.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crate.crateam.R;
import com.crate.crateam.activities.AwaitingDeliveryActivity;
import com.crate.crateam.activities.MainActivity;
import com.crate.crateam.activities.SupplyFormOperatorEdit;
import com.crate.crateam.activities.WasteTransferDelivery;
import com.crate.crateam.activities.WasteTransferDeliveryEdit;
import com.crate.crateam.activities.WasteTransferFormOperatorEdit;
import com.crate.crateam.adapters.GateKeeperAdapter;
import com.crate.crateam.adapters.RoadworthyListAdapter;
import com.crate.crateam.adapters.TaskCompletedAdapter;
import com.crate.crateam.model.GateKeeperSupplyDatamodule;
import com.crate.crateam.model.ReportsList;
import com.crate.crateam.model.TaskCompletedList;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.SessionManager;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class ReportsFragment extends Fragment {
    private String tag;
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView,rv_driver_operator_data;
    private ConstraintLayout cl_edit_task,cl_operator_tasks;
    private RadioGroup rg_reports;
    private RadioButton rb_defect_reports,rb_roadworthy_reports,rb_ad_hoc_reports,rb_task_completed,rb_delivery_tickets;
    private SharedPreferences.Editor editor;
    private String user_name="",current_date,lastTicketNo ="",lastDeliveryTicketNo="",lastNoteType="",ticketClicked="";
    private TextView tv_task_number,tv_edit_task,tv_task_by_driver;
    private int role_id,user_id=0,count = 0,driver_completed_task=0,driver_operator_completed_task=0,driver_total_completed_task=0;
    private long lastTaskTime=0;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference inspectionReference,adHocReportReference,supplyWasteFormReference,siteManagementReference,taskManagementReference,materialReference;
    private RoadworthyListAdapter adapter;
    private TaskCompletedAdapter taskCompletedAdapter;
    private GateKeeperAdapter gateKeeperAdapter;
    private ArrayList<TaskCompletedList> taskCompletedListsDataModel = new ArrayList<>();
    private ArrayList<GateKeeperSupplyDatamodule> arrayList_supply= new ArrayList<>();
    private ArrayList<String> arrayList_coll_add_supply= new ArrayList<>();
    private ArrayList<String> arrayList_del_add_supply= new ArrayList<>();
    private ArrayList<String> arrayList_ticket_no= new ArrayList<>();
    private ArrayList<Integer> arrayList_del_add_id_supply= new ArrayList<>();
    private ArrayList<Integer> arrayList_mtr_id_supply= new ArrayList<>();
    private ArrayList<String> arrayList_mtr_name_supply= new ArrayList<>();
    private ArrayList<String> arrayList_address_management= new ArrayList<>();
    private ArrayList<Integer> arrayList_address_management_id= new ArrayList<>();
    private ArrayList<Integer> arrayList_mtr_id_all_supply= new ArrayList<>();
    private ArrayList<String> arrayList_mtr_name_all_supply= new ArrayList<>();
    private ArrayList<String> collection_add= new ArrayList<>();
    private ArrayList<String> delivery_add= new ArrayList<>();
    private ArrayList<String> material_name= new ArrayList<>();
    private ArrayList<String> ticket_no= new ArrayList<>();
    private ArrayList<String> deliveryTickets = new ArrayList<>();
    private ArrayList<Integer> material_id= new ArrayList<>();
    private ArrayList<Long> taskTimes = new ArrayList<>();

    protected static final String TAG = "Reports";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.reports_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirestoreManager.initPersistentIndexManager();
        inspectionReference = db.collection("CR_inspection_submission");
        adHocReportReference = db.collection("CR_ad_hoc_submission");
        supplyWasteFormReference = db.collection("CR_supply_waste_forms");
        siteManagementReference = db.collection("CR_site_management");
        taskManagementReference = db.collection("CR_task_management");
        materialReference = db.collection("CR_material");

        SharedPreferences preferences = getActivity().getSharedPreferences("Preference", 0);
        editor = preferences.edit();
        progressDialog = Dialog.showProgressDialog(getActivity());
        SessionManager sessionManager = new SessionManager(getActivity());
        tv_task_number = view.findViewById(R.id.tv_task_number);
        tv_task_by_driver = view.findViewById(R.id.tv_task_by_driver);
        tv_edit_task  = view.findViewById(R.id.tv_edit_task);
        cl_edit_task = view.findViewById(R.id.cl_edit_task);
        cl_operator_tasks = view.findViewById(R.id.cl_operator_tasks);
        recyclerView = view.findViewById(R.id.rv_roadworthy);
        rv_driver_operator_data = view.findViewById(R.id.rv_driver_operator_data);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv_driver_operator_data.setLayoutManager(layoutManager);
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_name = user.get(SessionManager.KEY_FULL_NAME);
        user_id = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        String user_role = user.get(SessionManager.KEY_ROLE_ONE);
        role_id = Integer.valueOf(user_role);
        // get current date
        Date c = Calendar.getInstance().getTime();
        Log.d(TAG,"Current time => " + c + " "+role_id);
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        current_date = df.format(c);
        rg_reports = view.findViewById(R.id.rg_reports);
        rb_defect_reports = view.findViewById(R.id.rb_defect_reports);
        rb_roadworthy_reports = view.findViewById(R.id.rb_roadworthy_reports);
        rb_ad_hoc_reports = view.findViewById(R.id.rb_ad_hoc_reports);
        rb_task_completed = view.findViewById(R.id.rb_task_completed);
        rb_delivery_tickets = view.findViewById(R.id.rb_delivery_tickets);
        if (role_id == 5){
            rb_defect_reports.setVisibility(View.GONE);
            rb_roadworthy_reports.setVisibility(View.GONE);
            rb_ad_hoc_reports.setVisibility(View.GONE);
            rb_task_completed.setVisibility(View.VISIBLE);
            rb_delivery_tickets.setVisibility(View.VISIBLE);
        }else if (role_id==3){
            rb_defect_reports.setVisibility(View.VISIBLE);
            rb_roadworthy_reports.setVisibility(View.VISIBLE);
            rb_ad_hoc_reports.setVisibility(View.VISIBLE);
            rb_task_completed.setVisibility(View.GONE);
            rb_delivery_tickets.setVisibility(View.GONE);
        }else if (role_id==4){
            rb_defect_reports.setVisibility(View.VISIBLE);
            rb_roadworthy_reports.setVisibility(View.VISIBLE);
            rb_ad_hoc_reports.setVisibility(View.VISIBLE);
            rb_task_completed.setVisibility(View.VISIBLE);
            rb_delivery_tickets.setVisibility(View.VISIBLE);
        }
        else {
            rb_defect_reports.setVisibility(View.VISIBLE);
            rb_roadworthy_reports.setVisibility(View.VISIBLE);
            rb_ad_hoc_reports.setVisibility(View.VISIBLE);
            rb_task_completed.setVisibility(View.VISIBLE);
            rb_delivery_tickets.setVisibility(View.VISIBLE);
        }
        rg_reports.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i==R.id.rb_defect_reports) {
                    rv_driver_operator_data.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    cl_edit_task.setVisibility(View.GONE);
                    progressDialog.show();
                    setDefectsAdapter();
                }
                if (i==R.id.rb_roadworthy_reports) {
                    rv_driver_operator_data.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    cl_edit_task.setVisibility(View.GONE);
                    progressDialog.show();
                    setRoadworthyAdapter();
                }
                if (i==R.id.rb_ad_hoc_reports) {
                    rv_driver_operator_data.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    cl_edit_task.setVisibility(View.GONE);
                    progressDialog.show();
                    setAdHocAdapter();
                }
                if (i==R.id.rb_task_completed){
                    arrayList_supply.clear();
                    ticketClicked = "TaskCompleted";
                    progressDialog.show();
                    recyclerView.setVisibility(View.VISIBLE);
                    cl_edit_task.setVisibility(View.VISIBLE);
                    fetchLastOperatorTask();
                    if (role_id ==5){
                        supplyFormDriver();
                    } else {
                        jobIdDriver();
                    }
                    rv_driver_operator_data.setVisibility(View.VISIBLE);
                }
                if (i== R.id.rb_delivery_tickets){
                    arrayList_supply.clear();
                    ticketClicked = "DeliveryTicket";
                    recyclerView.setVisibility(View.GONE);
                    cl_edit_task.setVisibility(View.VISIBLE);
                    rv_driver_operator_data.setVisibility(View.VISIBLE);
                    fetchDeliveryTickets();
                    fetchLastDeliveryTicket();
                }
            }
        });
        tv_edit_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ticketClicked.equals("TaskCompleted")) {
                    if (lastNoteType.equals("Collection")) {
                        Intent intent = new Intent(getActivity(), WasteTransferFormOperatorEdit.class);
                        intent.putExtra("ticket_no", lastTicketNo);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(getActivity(), SupplyFormOperatorEdit.class);
                        intent.putExtra("ticket_no", lastTicketNo);
                        startActivity(intent);
                    }
                } else if (ticketClicked.equals("DeliveryTicket")) {
                    Intent intent = new Intent(getActivity(), WasteTransferDeliveryEdit.class);
                    intent.putExtra("ticket_no",lastDeliveryTicketNo);
                    startActivity(intent);
                }
            }
        });
    }



    private void setDefectsAdapter(){
        Query defectsQuery;
        if (role_id==4)
            defectsQuery = inspectionReference.whereEqualTo("driver_id",user_id).whereEqualTo("close_issue","No")
                    .whereEqualTo("conducted_on",current_date).orderBy("created_at",Query.Direction.DESCENDING);
        else
            defectsQuery = inspectionReference.whereEqualTo("close_issue","No").orderBy("created_at",Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<ReportsList> options = new FirestoreRecyclerOptions.Builder<ReportsList>()
                .setQuery(defectsQuery, ReportsList.class)
                .build();
        adapter = new RoadworthyListAdapter(options,getActivity());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        adapterClick();
    }

    private void setRoadworthyAdapter(){
        Query roadworthyQuery;
        if (role_id == 4)
            roadworthyQuery = inspectionReference.whereEqualTo("driver_id",user_id).whereEqualTo("vehicle_defect","No").whereEqualTo("trailer_defect","No")
                    .whereEqualTo("conducted_on",current_date).orderBy("submission_time",Query.Direction.DESCENDING);
        else
            roadworthyQuery = inspectionReference.whereEqualTo("vehicle_defect","No").whereEqualTo("trailer_defect","No")
                    .whereEqualTo("conducted_on",current_date).orderBy("submission_time",Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<ReportsList> options = new FirestoreRecyclerOptions.Builder<ReportsList>()
                .setQuery(roadworthyQuery, ReportsList.class)
                .build();
        adapter = new RoadworthyListAdapter(options,getActivity());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        adapterClick();
    }

    private void setAdHocAdapter(){
        Query defectedVehicleQuery;
        if (role_id == 4)
            defectedVehicleQuery = adHocReportReference.whereEqualTo("logged_by",user_id).whereEqualTo("close_issue","No")
                    .whereEqualTo("user_role",4).orderBy("time_second_format", Query.Direction.DESCENDING);
        else
            defectedVehicleQuery = adHocReportReference.whereEqualTo("close_issue","No").whereEqualTo("user_role",4).orderBy("time_second_format", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<ReportsList> options = new FirestoreRecyclerOptions.Builder<ReportsList>()
                .setQuery(defectedVehicleQuery, ReportsList.class)
                .build();
        adapter = new RoadworthyListAdapter(options,getActivity());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        if (adapter != null)
            adapter.startListening();
        progressDialog.dismiss();
        adapter.setOnItemClickListener(new RoadworthyListAdapter.OnViewClickListener() {
            @Override
            public void onViewClick(int position) {
                String inspection_id = adapter.getItem(position).getInspection_id();
                int id = adapter.getItem(position).getLogged_by();
                String date = adapter.getItem(position).getConducted_on();
                String user_name = adapter.getItem(position).getDriver_name();
                int vehicle_id = adapter.getItem(position).getVehicle_id();
                String registration_no = adapter.getItem(position).getRegistration_no();
                editor.putString("view_inspection_id_adHoc",inspection_id);
                editor.putInt("driver_id_adHoc",id);
                editor.putString("reported_date_adHoc",date);
                editor.putString("driver_name_adHoc",user_name);
                editor.putInt("vehicle_no_adHoc",vehicle_id);
                editor.putString("registration_id_adHoc",registration_no);
                editor.apply();
                Log.d(TAG,"IDE:" +inspection_id + " " +id);
                MainActivity mainActivity = (MainActivity) getActivity();
                ViewAdHocReports viewAdHocReports = new ViewAdHocReports();
                assert mainActivity != null;
                mainActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame,viewAdHocReports,"VIEW AD-HOC REPORT").addToBackStack("VIEW AD-HOC REPORT").commit();
                tag = viewAdHocReports.getTag();
                mainActivity.getChildTag(tag);
                Log.d(TAG,"ADAPTER_TAG" +tag);
            }
            @Override
            public void onEditClick(int position) {

            }
        });
    }
    private void setCompletedTaskAdapter(){
        progressDialog.dismiss();
        taskCompletedListsDataModel.clear();
        for (int i=0;i<ticket_no.size();i++){
            TaskCompletedList taskCompletedList = new TaskCompletedList();
            taskCompletedList.setTicket_no(ticket_no.get(i));
            taskCompletedList.setCollection_point(collection_add.get(i));
            taskCompletedList.setDelivery_point(delivery_add.get(i));
            taskCompletedList.setMaterial_name(material_name.get(i));
            taskCompletedListsDataModel.add(taskCompletedList);
        }
        taskCompletedAdapter = new TaskCompletedAdapter(getActivity(),taskCompletedListsDataModel);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(taskCompletedAdapter);
        driver_completed_task = taskCompletedAdapter.getItemCount();
    }
    private void setCompletedTaskAdapterGateKeeper(){
        progressDialog.dismiss();
        for (int i=0;i<arrayList_ticket_no.size();i++){
            GateKeeperSupplyDatamodule taskCompletedListSupply =new GateKeeperSupplyDatamodule();
            taskCompletedListSupply.setMaterial_name(arrayList_mtr_name_supply.get(i));
            taskCompletedListSupply.setCollection_address(arrayList_coll_add_supply.get(i));
            taskCompletedListSupply.setDelivery_address(arrayList_del_add_supply.get(i));
            taskCompletedListSupply.setTicket_no(arrayList_ticket_no.get(i));
            arrayList_supply.add(taskCompletedListSupply);
        }
        gateKeeperAdapter = new GateKeeperAdapter(getActivity(),arrayList_supply);
        rv_driver_operator_data.setAdapter(gateKeeperAdapter);
        tv_task_number.setText("Completed Task : " +gateKeeperAdapter.getItemCount());
        Log.d(TAG,"Completed Task : " +gateKeeperAdapter.getItemCount());
    }

    // store operator form fill up by driver
    private void driverOperatorAdapter(){
        progressDialog.dismiss();
        arrayList_supply = new ArrayList<>();
        for (int i=0;i<arrayList_ticket_no.size();i++){
            GateKeeperSupplyDatamodule taskCompletedListSupply =new GateKeeperSupplyDatamodule();
            taskCompletedListSupply.setMaterial_name(arrayList_mtr_name_supply.get(i));
            taskCompletedListSupply.setCollection_address(arrayList_coll_add_supply.get(i));
            if (!arrayList_del_add_supply.isEmpty())
             taskCompletedListSupply.setDelivery_address(arrayList_del_add_supply.get(i));
            taskCompletedListSupply.setTicket_no(arrayList_ticket_no.get(i));
            arrayList_supply.add(taskCompletedListSupply);
        }
        gateKeeperAdapter = new GateKeeperAdapter(getActivity(),arrayList_supply);
        rv_driver_operator_data.setAdapter(gateKeeperAdapter);
        driver_operator_completed_task = gateKeeperAdapter.getItemCount();
        driver_total_completed_task = driver_completed_task+driver_operator_completed_task;
        tv_task_number.setText("Completed Task : " +String.valueOf(driver_total_completed_task));
    }

    private void adapterClick(){
        if (adapter != null)
            adapter.startListening();
        progressDialog.dismiss();
        adapter.setOnItemClickListener(new RoadworthyListAdapter.OnViewClickListener() {
            @Override
            public void onViewClick(int position) {
                String inspection_id = adapter.getItem(position).getInspection_id();
                int user_id = adapter.getItem(position).getLogged_by();
                String date = adapter.getItem(position).getConducted_on();
                String user_name = adapter.getItem(position).getDriver_name();
                int vehicle_id = adapter.getItem(position).getVehicle_id();
                String id = adapter.getItem(position).getId();
                String registration_no = adapter.getItem(position).getRegistration_no();
                Log.d(TAG,"ADAPTER CLICK : "+id);
                editor.putString("view_inspection_id",inspection_id);
                editor.putString("doc_id",id);
                editor.putInt("driver_id",user_id);
                editor.putString("reported_date",date);
                editor.putString("driver_name",user_name);
                editor.putInt("vehicle_no",vehicle_id);
                editor.putString("registration_id",registration_no);
                editor.apply();
                Log.d(TAG,"IDE:" +inspection_id + " " +user_id);
                MainActivity mainActivity = (MainActivity) getActivity();
                ViewInspectionFragment viewInspectionFragment = new ViewInspectionFragment();
                assert mainActivity != null;
                mainActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame,viewInspectionFragment,"VIEW INSPECTION").addToBackStack("VIEW INSPECTION").commit();
                tag = viewInspectionFragment.getTag();
                mainActivity.getChildTag(tag);
                Log.d(TAG,"ADAPTER_TAG" +tag);
            }
            @Override
            public void onEditClick(int position) {
                String inspection_id = adapter.getItem(position).getInspection_id();
                int user_id = adapter.getItem(position).getLogged_by();
                String date = adapter.getItem(position).getConducted_on();
                String user_name = adapter.getItem(position).getDriver_name();
                int vehicle_id = adapter.getItem(position).getVehicle_id();
                String id = adapter.getItem(position).getId();
                String registration_no = adapter.getItem(position).getRegistration_no();
                editor.putString("view_inspection_id",inspection_id);
                editor.putString("doc_id",id);
                editor.putInt("driver_id",user_id);
                editor.putString("reported_date",date);
                editor.putString("driver_name",user_name);
                editor.putInt("vehicle_no",vehicle_id);
                editor.putString("registration_id",registration_no);
                editor.apply();
                MainActivity mainActivity = (MainActivity) getActivity();
                WorkshopManagerFragment workshopManagerFragment = new WorkshopManagerFragment();
                assert mainActivity != null;
                mainActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame,workshopManagerFragment,"WORKSHOP MANAGER").addToBackStack("WORKSHOP MANAGER").commit();
                tag = workshopManagerFragment.getTag();
                mainActivity.getChildTag(tag);
                Log.d(TAG,"ADAPTER_TAG" +tag);
            }
        });
    }

    @Override
    public void onStop(){
        super.onStop();
        if(adapter != null)
            adapter.stopListening();
    }

    private void fetchDeliveryTickets(){
        progressDialog.show();
        Query query = supplyWasteFormReference.whereEqualTo("user_id", user_id).whereEqualTo("job_id",0).whereEqualTo("submission_date",current_date)
                    .whereEqualTo("ticket_type","Delivery").orderBy("time_second_format", Query.Direction.DESCENDING);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    progressDialog.dismiss();
                    if (!task.getResult().isEmpty()) {
                        arrayList_coll_add_supply.clear();
                        arrayList_ticket_no.clear();
                        arrayList_del_add_id_supply.clear();
                        arrayList_mtr_id_supply.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            arrayList_coll_add_supply.add(queryDocumentSnapshot.getString("current_site_address"));
                            arrayList_ticket_no.add(queryDocumentSnapshot.getString("ticket_no"));
                            if (Objects.requireNonNull(queryDocumentSnapshot.getLong("delivery_site_id_new")).intValue()==0)
                                arrayList_del_add_id_supply.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("delivery_site_id")).intValue());
                            else
                                arrayList_del_add_id_supply.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("delivery_site_id_new")).intValue());
                            if (Objects.requireNonNull(queryDocumentSnapshot.getLong("material_id_new")).intValue()==0)
                                arrayList_mtr_id_supply.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("material_id")).intValue());
                            else
                                arrayList_mtr_id_supply.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("material_id_new")).intValue());
                        }
                        getDeliveryAddress();
                        Log.d(TAG,"FTFYT :" +arrayList_ticket_no+ "  "+arrayList_mtr_id_supply);
                    }else {
                        tv_task_number.setText("");
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

    private void supplyFormDriver(){
        progressDialog.show();
        Query query = supplyWasteFormReference.whereEqualTo("user_role_id", role_id).whereEqualTo("submission_date",current_date)
                .whereEqualTo("job_id",0).whereEqualTo("user_id",user_id).whereEqualTo("ticket_status","Open")
                .orderBy("time_second_format", Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        Log.d(TAG,"RTY :" +"I am 33");
                        if (role_id!=5){
                            rv_driver_operator_data.setVisibility(View.VISIBLE);
                        }
                        arrayList_coll_add_supply.clear();
                        arrayList_ticket_no.clear();
                        arrayList_del_add_id_supply.clear();
                        arrayList_mtr_id_supply.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            arrayList_coll_add_supply.add(queryDocumentSnapshot.getString("current_site_address"));
                            arrayList_ticket_no.add(queryDocumentSnapshot.getString("ticket_no"));
                            if (Objects.requireNonNull(queryDocumentSnapshot.getLong("delivery_site_id_new")).intValue()==0)
                                arrayList_del_add_id_supply.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("delivery_site_id")).intValue());
                            else
                                arrayList_del_add_id_supply.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("delivery_site_id_new")).intValue());
                            if (Objects.requireNonNull(queryDocumentSnapshot.getLong("material_id_new")).intValue()==0)
                                arrayList_mtr_id_supply.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("material_id")).intValue());
                            else
                                arrayList_mtr_id_supply.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("material_id_new")).intValue());
                        }
                        getDeliveryAddress();
                    }else {
                        progressDialog.dismiss();
                        if (role_id==5)
                            setCompletedTaskAdapterGateKeeper();
                        else {
                            driverOperatorAdapter();
                            rv_driver_operator_data.setVisibility(View.GONE);
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
    private void getDeliveryAddress(){
        siteManagementReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        arrayList_del_add_supply.clear();
                        arrayList_address_management_id.clear();
                        arrayList_address_management.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            arrayList_address_management_id.add(queryDocumentSnapshot.getLong("id").intValue());
                            arrayList_address_management.add(queryDocumentSnapshot.getString("address"));
                        }
                        for (int i =0; i<arrayList_del_add_id_supply.size();i++){
                            for (int j=0;j<arrayList_address_management_id.size();j++){
                                if (arrayList_del_add_id_supply.get(i).equals(arrayList_address_management_id.get(j))){
                                    arrayList_del_add_supply.add(arrayList_address_management.get(j));
                                    break;
                                }
                            }
                        }
                        Log.e("1100",String.valueOf(arrayList_del_add_supply));
                        getMaterialNameSupply();
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
    private void getMaterialNameSupply(){
        materialReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    arrayList_mtr_name_supply.clear();
                    arrayList_mtr_id_all_supply.clear();
                    arrayList_mtr_name_all_supply.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot  : task.getResult()){
                        arrayList_mtr_id_all_supply.add(Integer.parseInt(queryDocumentSnapshot.getId()));
                        arrayList_mtr_name_all_supply.add(queryDocumentSnapshot.getString("material_description"));
                    }
                    for (int i=0;i<arrayList_mtr_id_supply.size();i++){
                        for (int j=0;j<arrayList_mtr_id_all_supply.size();j++){
                            if (arrayList_mtr_id_supply.get(i).equals(arrayList_mtr_id_all_supply.get(j))){
                                arrayList_mtr_name_supply.add(arrayList_mtr_name_all_supply.get(j));
                                break;
                            }
                        }
                    }
                    Log.e("1200",String.valueOf(arrayList_mtr_name_supply));
                    if (role_id==5)
                        setCompletedTaskAdapterGateKeeper();
                    else driverOperatorAdapter();
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

    /*task completed only for driver*/
    private void jobIdDriver(){
        Query query = taskManagementReference.whereEqualTo("task_status","FinishTask").whereEqualTo("finish_task_user_id",user_id)
                .whereEqualTo("finish_task_date",current_date).orderBy("time_second_format", Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    count = 0;
                    collection_add.clear();
                    delivery_add.clear();
                    ticket_no.clear();
                    material_id.clear();
                    material_name.clear();
                    if (!task.getResult().isEmpty()){
                        tv_task_by_driver.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            collection_add.add(queryDocumentSnapshot.getString("collection_point"));
                            delivery_add.add(queryDocumentSnapshot.getString("delivery_point"));
                            ticket_no.add(queryDocumentSnapshot.getString("ticket_no"));
                            material_name.add(queryDocumentSnapshot.getString("material_description"));
                            material_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("material_id")).intValue());
                        }
                        Log.d(TAG,"material_name :"+material_name);
                    } else {
                        tv_task_by_driver.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                    }
                    setCompletedTaskAdapter();
                    supplyFormDriver();
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

    private void fetchLastOperatorTask(){
        Query query = supplyWasteFormReference.whereEqualTo("user_id",user_id).whereEqualTo("job_id",0)
                .whereEqualTo("submission_date",current_date).whereEqualTo("ticket_status","Open").whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        cl_operator_tasks.setVisibility(View.VISIBLE);
                        for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                            long timeSecondFormat =queryDocumentSnapshot.getLong("time_second_format");
                            taskTimes.add(timeSecondFormat);
                        }
                        Collections.sort(taskTimes);
                        Log.d(TAG,"FGTTY 1:" +taskTimes);
                        for (int i=0;i<taskTimes.size();i++){
                            lastTaskTime = taskTimes.get(taskTimes.size()-1);
                        }
                        Log.d(TAG,"FGTTY :" +lastTaskTime);
                        fetchLastOperatorTaskId(lastTaskTime);
                    }else {
                        cl_operator_tasks.setVisibility(View.GONE);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"Error :"+e.getMessage());
            }
        });
    }

    private void fetchLastOperatorTaskId(long taskTime){
        Query query = supplyWasteFormReference.whereEqualTo("user_id",user_id).whereEqualTo("job_id",0)
                .whereEqualTo("submission_date",current_date).whereEqualTo("time_second_format",taskTime)
                .whereEqualTo("ticket_status","Open").whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                            lastTicketNo = queryDocumentSnapshot.getString("ticket_no");
                            lastNoteType = queryDocumentSnapshot.getString("note_type");
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"Error :"+e.getMessage());
            }
        });
    }

    private void fetchLastDeliveryTicket(){
        Query query = supplyWasteFormReference.whereEqualTo("user_id",user_id).whereEqualTo("submission_date",current_date)
                .whereEqualTo("job_id",0).whereEqualTo("ticket_type","Delivery")
                .orderBy("time_second_format", Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        cl_operator_tasks.setVisibility(View.VISIBLE);
                        for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                            deliveryTickets.add(queryDocumentSnapshot.getString("ticket_no"));
                        }
                        if (!deliveryTickets.isEmpty())
                            lastDeliveryTicketNo = deliveryTickets.get(0);
                        Log.d(TAG,"GYLGUU :" +lastDeliveryTicketNo);
                    }else {
                        cl_operator_tasks.setVisibility(View.GONE);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"Error :"+e.getMessage());
            }
        });
    }
}
