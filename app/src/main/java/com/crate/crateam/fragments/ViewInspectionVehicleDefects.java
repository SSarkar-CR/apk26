package com.crate.crateam.fragments;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.crate.crateam.R;
import com.crate.crateam.activities.MainActivity;
import com.crate.crateam.adapters.ViewDefectListAdapter;
import com.crate.crateam.model.ViewInspectionDefectList;
import com.crate.crateam.utility.Dialog;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class ViewInspectionVehicleDefects extends Fragment {
    private RecyclerView rv_view_defects_list;
    private String inspection_id,tag;
    Fragment fragment;
    private ProgressDialog progressDialog;
    private RelativeLayout rl_no_defect;
    private ImageView iv_cross;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference viewInspectionDefectsReference,vehicleDefectsReferenceWM;
    private ViewDefectListAdapter adapter;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_inspection_defects_vehicle_layout, container, false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }
    private void initView(View view){
        FirestoreManager.initPersistentIndexManager();
        viewInspectionDefectsReference = db.collection("CR_vehicle_defect_inspections");
        vehicleDefectsReferenceWM = db.collection("CR_vehicle_defects_wm");
        rv_view_defects_list = view.findViewById(R.id.rv_view_defects_list);
        rl_no_defect = view.findViewById(R.id.rl_no_defect);
        iv_cross = getActivity().findViewById(R.id.iv_cross);
        SharedPreferences preferences = getActivity().getSharedPreferences("Preference", 0); // 0 - for private mode
        progressDialog = Dialog.showProgressDialog(getActivity());
        inspection_id = preferences.getString("view_inspection_id",null);
        Log.d(TAG,"InViewInspectionDefects"+"InsIdViewInspection : "  +inspection_id);
        progressDialog.show();
        setAdapter();
        iv_cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment = new DashboardFragment();
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame,fragment,"DASHBOARD");
                ft.addToBackStack("DASHBOARD");
                ft.commit();
                tag = fragment.getTag();
                Log.d(TAG,"CHILD_TAG" +fragment.getTag());
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.getChildTag(tag);
            }
        });
    }

    private void setAdapter(){
        Query viewInspectionDefectsQuery = viewInspectionDefectsReference.whereEqualTo("inspection_id", inspection_id).whereEqualTo("element_type", "vehicle").whereEqualTo("close_issue","No");
        FirestoreRecyclerOptions<ViewInspectionDefectList> options = new FirestoreRecyclerOptions.Builder<ViewInspectionDefectList>()
                .setQuery(viewInspectionDefectsQuery, ViewInspectionDefectList.class)
                .build();
        adapter = new ViewDefectListAdapter(options,getActivity());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv_view_defects_list.setLayoutManager(layoutManager);
        rv_view_defects_list.setAdapter(adapter);
        viewInspectionDefectsQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    if (task.getResult().getDocuments().isEmpty())
                        rl_no_defect.setVisibility(View.VISIBLE);
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        if (documentSnapshot.getString("element_name").equals("No"))
                            rl_no_defect.setVisibility(View.VISIBLE);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG,"LOAD_ERROR: " +e.getMessage());
                Dialog.alertDialog(getActivity(),"Unable to fetch results.");
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
