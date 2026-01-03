package com.crate.crateam.fragments;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.crate.crateam.R;
import com.crate.crateam.adapters.ViewAttachmentsListAdapter;
import com.crate.crateam.model.ViewInspectionAttachments;
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
import static androidx.constraintlayout.widget.Constraints.TAG;

public class ViewInspectionTrailerAttachments extends Fragment{
    private RecyclerView rv_view_attachments_list;
    private String inspection_id;
    private ProgressDialog progressDialog;
    private RelativeLayout rl_no_attachments;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference viewInspectionDefectsReference;
    private ViewAttachmentsListAdapter adapter;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_inspection_attachments_vehicle_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view){
        FirestoreManager.initPersistentIndexManager();
        viewInspectionDefectsReference = db.collection("CR_vehicle_defect_inspections");
        SharedPreferences preferences = getActivity().getSharedPreferences("Preference", 0); // 0 - for private mode
        progressDialog = Dialog.showProgressDialog(getActivity());
        rv_view_attachments_list = view.findViewById(R.id.rv_view_attachments_list);
        rl_no_attachments = view.findViewById(R.id.rl_no_attachments);
        inspection_id = preferences.getString("view_inspection_id",null);
        Log.d(TAG,"InViewInspection"+"InsIdViewInspection : "  +inspection_id);
        progressDialog.show();
        setAdapter();
    }

    private void setAdapter(){
        Query viewInspectionDefectsQuery = viewInspectionDefectsReference.whereEqualTo("inspection_id", inspection_id).whereEqualTo("element_type", "trailer").whereEqualTo("close_issue","No");
        FirestoreRecyclerOptions<ViewInspectionAttachments> options = new FirestoreRecyclerOptions.Builder<ViewInspectionAttachments>()
                .setQuery(viewInspectionDefectsQuery, ViewInspectionAttachments.class)
                .build();
        adapter = new ViewAttachmentsListAdapter(options,getActivity());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv_view_attachments_list.setLayoutManager(layoutManager);
        rv_view_attachments_list.setAdapter(adapter);
        viewInspectionDefectsQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    if (task.getResult().getDocuments().isEmpty())
                        rl_no_attachments.setVisibility(View.VISIBLE);
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        if (documentSnapshot.getString("element_name").equals("No"))
                            rl_no_attachments.setVisibility(View.VISIBLE);
                        adapter.setOnItemClickListener(new ViewAttachmentsListAdapter.OnImageClickListener() {
                            @Override
                            public void onImageClick(final int position) {
                                final ImageView image_one =  rv_view_attachments_list.getChildAt(position).findViewById(R.id.iv_photo_one);
                                final ImageView image_two =  rv_view_attachments_list.getChildAt(position).findViewById(R.id.iv_photo_two);
                                image_one.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Bitmap getDrawable1 = ((BitmapDrawable) image_one.getDrawable()).getBitmap();
                                        zoomImagePopup(getDrawable1);
                                    }
                                });
                                image_two.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Bitmap getDrawable2 = ((BitmapDrawable) image_two.getDrawable()).getBitmap();
                                        zoomImagePopup(getDrawable2);
                                    }
                                });
                            }
                        });
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

    private void zoomImagePopup(Bitmap bitmap){
        View popupView = LayoutInflater.from(getActivity()).inflate(R.layout.zoom_image_popup, null);
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
}
