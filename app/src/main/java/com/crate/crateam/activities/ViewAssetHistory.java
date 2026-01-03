package com.crate.crateam.activities;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crate.crateam.R;
import com.crate.crateam.adapters.ViewInspectionElementAdapter;
import com.crate.crateam.adapters.ViewInspectionRegimeAdapter;
import com.crate.crateam.model.ViewInspectionElementDefects;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.SessionManager;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheIndexManager;
import com.crate.crateam.utility.FirestoreManager;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ViewAssetHistory extends AppCompatActivity implements View.OnClickListener {
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference assetInspectionSubmissionReference,assetInspectionDefectsReference, assetInspectionRegimeReference,
            siteLocationReference;
    private Button bt_back;
    private ViewInspectionRegimeAdapter viewInspectionRegimeAdapter;
    private LinearLayout ll_safety_critical,ll_defected_element;
    private String asset_type="",asset_number="",asset_name="", last_inspection_user_name="",last_inspection_status="",last_inspection_date="",
            inspection_id="",conducted_on="",user_name="";
    private int assetId = 0,assetTypeId=0,user_id=0, last_inspection_user_id=0,last_inspection_location=0,userId=0;
    private EditText et_value;
    private TextView tv_asset_type,tv_asset_id,tv_asset_name,tv_inspection_id,tv_asset_location,tv_inspected_by,tv_date,tv_value;
    private RecyclerView rv_asset_elements,rv_asset_regimes;
    private ViewInspectionElementAdapter adapter;
    private ArrayList<Integer> arrayList_regime_element_id  = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_name = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_value = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_value_wm = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_view = new ArrayList<>();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_asset_history);
        initView();
        initializeOnClick();
    }
    private void initView(){
        progressDialog = Dialog.showProgressDialog(this);
        FirestoreManager.initPersistentIndexManager();

        assetInspectionSubmissionReference = db.collection("AM_asset_inspection_submission");
        assetInspectionDefectsReference = db.collection("AM_asset_inspection_defects");
        assetInspectionRegimeReference = db.collection("AM_asset_inspection_regimes");
        siteLocationReference = db.collection("AM_site_location");

        SessionManager sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_name = user.get(SessionManager.KEY_FULL_NAME);
        user_id = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            asset_type= extras.getString("asset_type");
            assetTypeId = extras.getInt("asset_type_id");
            asset_number= extras.getString("asset_id");
            asset_name= extras.getString("asset_name");
            assetId = extras.getInt("assetId");
            inspection_id= extras.getString("inspection_id");
            conducted_on= extras.getString("conducted_on");
            user_name= extras.getString("user_name");
        }
        Log.d("inspection id :" ,inspection_id);
        tv_asset_type = findViewById(R.id.tv_asset_type);
        tv_asset_id = findViewById(R.id.tv_asset_id);
        tv_asset_name = findViewById(R.id.tv_asset_name);
        tv_inspection_id = findViewById(R.id.tv_inspection_id);
        tv_asset_location = findViewById(R.id.tv_asset_location);
        tv_inspected_by  = findViewById(R.id.tv_inspected_by);
        tv_date = findViewById(R.id.tv_date);
        tv_asset_type.setText(asset_type);
        tv_asset_id.setText(asset_number);
        tv_asset_name.setText(asset_name);
        tv_inspection_id.setText(inspection_id);
        tv_inspected_by.setText(user_name);
        tv_date.setText(conducted_on);
        bt_back = findViewById(R.id.bt_back);
        ll_safety_critical = findViewById(R.id.ll_safety_critical);
        ll_defected_element = findViewById(R.id.ll_defected_element);
        rv_asset_elements = findViewById(R.id.rv_asset_elements);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rv_asset_elements.setLayoutManager(layoutManager);
        rv_asset_regimes = findViewById(R.id.rv_asset_regimes);
        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(this);
        rv_asset_regimes.setLayoutManager(layoutManager2);
        fetchInspectionDetails(inspection_id);
        getDefectedRegimes(inspection_id);
        setAdapter();
    }

    private void initializeOnClick(){
        bt_back.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_back:
                finish();
                break;
        }
    }

    private void fetchInspectionDetails(String inspection_id){
        Query query = assetInspectionSubmissionReference.whereEqualTo("status", "Active").whereEqualTo("inspection_id",inspection_id)
                .whereEqualTo("user_role","Other");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            last_inspection_user_id = Objects.requireNonNull(queryDocumentSnapshot.getLong("user_id")).intValue();
                            if (Objects.requireNonNull(queryDocumentSnapshot.getString("inspector_name")).equals(""))
                                last_inspection_user_name = queryDocumentSnapshot.getString("user_name");
                            else
                                last_inspection_user_name = queryDocumentSnapshot.getString("inspector_name");
                            last_inspection_status = queryDocumentSnapshot.getString("asset_status");
                            last_inspection_location = queryDocumentSnapshot.getLong("assign_location").intValue();
                            last_inspection_date = queryDocumentSnapshot.getString("conducted_on");
                        }
                        Log.d("gyuguky :" ,last_inspection_location+" "+last_inspection_date);
                        tv_inspected_by.setText(last_inspection_user_name);
                        tv_date.setText(last_inspection_date);
                        getLastInspectionLocationName(last_inspection_location);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ViewAssetHistory.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLastInspectionLocationName(int site_id){
        Query query = siteLocationReference.whereEqualTo("id", site_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            String siteName= queryDocumentSnapshot.getString("site_name");
                            Log.d("SITE Name : " ,siteName);
                            tv_asset_location.setText(siteName);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ViewAssetHistory.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDefectedRegimes(String inspection_id){
        Task task1= assetInspectionRegimeReference.whereEqualTo("status", "Active")
                .whereEqualTo("inspection_id",inspection_id).whereEqualTo("user_role","Other")
                .whereEqualTo("defected","Yes").get();
        Task task2 = assetInspectionRegimeReference.whereEqualTo("status", "Active")
                .whereEqualTo("inspection_id",inspection_id).whereEqualTo("user_role","Other")
                .whereNotEqualTo("regime_value_wm","").get();
        Task<List<QuerySnapshot>> allTask = Tasks.whenAllSuccess(task1,task2);
        allTask.addOnSuccessListener(new OnSuccessListener<List<QuerySnapshot>>() {
            @Override
            public void onSuccess(List<QuerySnapshot> querySnapshots) {
                arraylist_regime_element_name.clear();
                arrayList_regime_element_id.clear();
                arraylist_regime_element_value.clear();
                arraylist_regime_element_value_wm.clear();
                for (QuerySnapshot queryDocumentSnapshot : querySnapshots) {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshot) {
                        arraylist_regime_element_name.add(documentSnapshot.getString("regime_name"));
                        arrayList_regime_element_id.add(Objects.requireNonNull(documentSnapshot.getLong("regime_id")).intValue());
                        arraylist_regime_element_value.add(documentSnapshot.getString("regime_value"));
                        arraylist_regime_element_value_wm.add(documentSnapshot.getString("regime_value_wm"));
                        arraylist_regime_element_view.add(documentSnapshot.getString("regime_view"));
                    }
                }
                viewInspectionRegimeAdapter = new ViewInspectionRegimeAdapter(arraylist_regime_element_name,arraylist_regime_element_value,arraylist_regime_element_value_wm);
                rv_asset_regimes.setAdapter(viewInspectionRegimeAdapter);
                if (arraylist_regime_element_name.isEmpty())
                    ll_safety_critical.setVisibility(View.GONE);
                else
                    ll_safety_critical.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ViewAssetHistory.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setAdapter(){
        Query getDefectsQuery = assetInspectionDefectsReference.whereEqualTo("inspection_id",inspection_id)
                .whereEqualTo("status", "Active").whereNotEqualTo("element_defect","").whereEqualTo("user_role","Other");
        FirestoreRecyclerOptions<ViewInspectionElementDefects> options = new FirestoreRecyclerOptions.Builder<ViewInspectionElementDefects>()
                .setQuery(getDefectsQuery, ViewInspectionElementDefects.class)
                .build();
        adapter = new ViewInspectionElementAdapter(options,this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rv_asset_elements.setLayoutManager(layoutManager);
        rv_asset_elements.setAdapter(adapter);
        getDefectsQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        if (documentSnapshot.exists()) {
                            ViewInspectionElementDefects viewInspectionElementDefects = new ViewInspectionElementDefects();
                            adapter.setOnItemClickListener(new ViewInspectionElementAdapter.OnImageClickListener() {
                                @Override
                                public void onImageClick(final int position) {
                                    final ImageView image_one = rv_asset_elements.getChildAt(position).findViewById(R.id.iv_photo_one);
                                    final ImageView image_two = rv_asset_elements.getChildAt(position).findViewById(R.id.iv_photo_two);
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
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d("LOAD_ERROR: " , Objects.requireNonNull(e.getMessage()));
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

}