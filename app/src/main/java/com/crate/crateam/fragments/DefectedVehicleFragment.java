package com.crate.crateam.fragments;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.crate.crateam.R;
import com.crate.crateam.activities.MainActivity;
import com.crate.crateam.adapters.RoadworthyListAdapter;
import com.crate.crateam.model.ReportsList;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.SessionManager;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheIndexManager;
import com.crate.crateam.utility.FirestoreManager;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class DefectedVehicleFragment extends Fragment {
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private SharedPreferences.Editor editor;
    private String tag,user_name,current_date="";
    private int role_id,user_id=0;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference inspectionReference;
    private RoadworthyListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.roadworthy_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirestoreManager.initPersistentIndexManager();
        inspectionReference = db.collection("CR_inspection_submission");
        SharedPreferences preferences = getActivity().getSharedPreferences("Preference", 0); // 0 - for private mode
        editor = preferences.edit();
        progressDialog = Dialog.showProgressDialog(getActivity());
        SessionManager sessionManager = new SessionManager(getActivity());
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_id = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        user_name = user.get(SessionManager.KEY_FULL_NAME);
        String user_role = user.get(SessionManager.KEY_ROLE_ONE);
        role_id = Integer.valueOf(user_role);
        recyclerView = view.findViewById(R.id.rv_roadworthy);
        Date c = Calendar.getInstance().getTime();
        Log.d(TAG,"Current time => " + c);
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = df.format(c);
        current_date = formattedDate;
        progressDialog.show();
        setAdapter();
    }

    private void setAdapter(){
        Query defectedVehicleQuery;
        if (role_id == 4)
        defectedVehicleQuery = inspectionReference.whereEqualTo("driver_id",user_id).whereEqualTo("defected","Yes").whereEqualTo("close_issue","No")
                .whereEqualTo("conducted_on",current_date).orderBy("created_at", Query.Direction.DESCENDING);
        else
            defectedVehicleQuery = inspectionReference.whereEqualTo("defected","Yes").whereEqualTo("close_issue","No").orderBy("created_at", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<ReportsList> options = new FirestoreRecyclerOptions.Builder<ReportsList>()
                .setQuery(defectedVehicleQuery, ReportsList.class)
                .build();
        adapter = new RoadworthyListAdapter(options,getActivity());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
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
                editor.putString("view_inspection_id",inspection_id);
                editor.putString("doc_id",id);
                editor.putInt("driver_id",user_id);
                editor.putString("reported_date",date);
                editor.putString("driver_name",user_name);
                editor.putString("registration_id",registration_no);
                editor.putInt("vehicle_no",vehicle_id);
                editor.apply();
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
                editor.putString("registration_id",registration_no);
                editor.putInt("vehicle_no",vehicle_id);
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
    public void onStart(){
        super.onStart();
        if (adapter != null)
            adapter.startListening();
    }

    @Override
    public void onStop(){
        super.onStop();
        if(adapter != null)
            adapter.stopListening();
    }
}
