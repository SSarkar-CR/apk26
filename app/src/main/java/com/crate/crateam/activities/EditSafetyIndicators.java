package com.crate.crateam.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.crate.crateam.R;
import com.crate.crateam.adapters.AssetDateManagementAdapter;
import com.crate.crateam.adapters.AssetRegimeAdapter;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

public class EditSafetyIndicators extends AppCompatActivity implements View.OnClickListener{
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference regimeDetailsReference;
    private TextView tv_asset_type,tv_asset_id,tv_asset_name,tv_value;
    private Button bt_back,bt_submit;
    private RadioGroup rg_on_off_value,rg_yes_no_value;
    private EditText et_value;
    private RecyclerView rv_asset_regimes;
    private AssetRegimeAdapter assetRegimeAdapter;
    private String asset_type="",asset_number="",asset_name="",identification_method="",
            regime_element_value="",current_date="",current_time="",
            maxDocIdRegimeDetails="";
    private int assetId=0,userId=0;
    private ArrayList<Integer> arrayList_regime_element_id  = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_name = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_value = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_value_new = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_view = new ArrayList<>();
    private ArrayList<Integer> arrayList_regime_element_id_sorted  = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_name_sorted = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_value_sorted = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_view_sorted = new ArrayList<>();
    private ArrayList<Integer> regimeElementsIds = new ArrayList<>();
    private ProgressDialog progressDialog;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_safety_indicators);
        initView();
        initializeOnClick();
    }

    private void initView(){
        progressDialog = Dialog.showProgressDialog(this);
        FirestoreManager.initPersistentIndexManager();
        regimeDetailsReference = db.collection("AM_asset_revalidation_details");
        SessionManager sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        userId = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            asset_type= extras.getString("asset_type");
            asset_number= extras.getString("asset_id");
            asset_name= extras.getString("asset_name");
            identification_method= extras.getString("identification_method");
            assetId = extras.getInt("assetId");
        }
        bt_submit = findViewById(R.id.bt_submit);
        tv_asset_type = findViewById(R.id.tv_asset_type);
        tv_asset_id = findViewById(R.id.tv_asset_id);
        tv_asset_name = findViewById(R.id.tv_asset_name);
        tv_asset_type.setText(asset_type);
        tv_asset_id.setText(asset_number);
        tv_asset_name.setText(asset_name);
        bt_back = findViewById(R.id.bt_back);
        rv_asset_regimes = findViewById(R.id.rv_asset_regimes);
        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(this);
        rv_asset_regimes.setLayoutManager(layoutManager2);
        getAssetRegimes(assetId);
        getMaxDocIDRegimeDetails();
    }

    private void initializeOnClick(){
        bt_back.setOnClickListener(this);
        bt_submit.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_back:
                finish();
                break;
            case R.id.bt_submit:
                    progressDialog.show();
                    sendRegimeElementsData();
                break;
        }
    }

    private void getMaxDocIDRegimeDetails(){
        regimeDetailsReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            regimeElementsIds.add(Integer.parseInt(queryDocumentSnapshot.getId()));
                        }
                        maxDocIdRegimeDetails = String.valueOf(getMax(regimeElementsIds));
                        Log.d("max_regime_details_id :",maxDocIdRegimeDetails);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditSafetyIndicators.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getMax(ArrayList<Integer> list){
        int max = 0;
        for(int i=0; i<list.size(); i++){
            if(list.get(i) > max){
                max = list.get(i);
            }
        }
        return max;
    }

    private void getAssetRegimes(int asset_details_id){
        Query query = regimeDetailsReference.whereEqualTo("status", "Active").whereEqualTo("asset_details_id",asset_details_id)
                .orderBy("time_second_format", Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    arrayList_regime_element_id.clear();
                    arraylist_regime_element_name.clear();
                    arraylist_regime_element_value.clear();
                    arraylist_regime_element_view.clear();
                    arrayList_regime_element_id_sorted.clear();
                    arraylist_regime_element_name_sorted.clear();
                    arraylist_regime_element_value_sorted.clear();
                    arraylist_regime_element_view_sorted.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        arraylist_regime_element_name.add(queryDocumentSnapshot.getString("regime_element_name"));
                        arrayList_regime_element_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("regime_element_id")).intValue());
                        arraylist_regime_element_value.add(queryDocumentSnapshot.getString("regime_element_value"));
                        arraylist_regime_element_view.add(queryDocumentSnapshot.getString("regime_element_view"));
                    }
                    ArrayList<Integer> arrayList_regime_element_id_all = new ArrayList<>();
                    arrayList_regime_element_id_all.addAll(arrayList_regime_element_id);
                    HashSet<Integer> hashSet = new HashSet<Integer>();
                    hashSet.addAll(arrayList_regime_element_id);
                    arrayList_regime_element_id.clear();
                    arrayList_regime_element_id_sorted.addAll(hashSet);
                    for (int i = 0;i<arrayList_regime_element_id_sorted.size();i++){
                        for (int j = 0;j<arrayList_regime_element_id_all.size();j++){
                            if (arrayList_regime_element_id_sorted.get(i).equals(arrayList_regime_element_id_all.get(j))){
                                arraylist_regime_element_name_sorted.add(arraylist_regime_element_name.get(j));
                                arraylist_regime_element_value_sorted.add(arraylist_regime_element_value.get(j));
                                arraylist_regime_element_view_sorted.add(arraylist_regime_element_view.get(j));
                                break;
                            }
                        }
                    }
                    Log.d("htfjht :" ,arraylist_regime_element_value_sorted+" "+arrayList_regime_element_id_sorted);
                    assetRegimeAdapter = new AssetRegimeAdapter(arraylist_regime_element_name_sorted,arraylist_regime_element_view_sorted,arraylist_regime_element_value_sorted);
                    rv_asset_regimes.setAdapter(assetRegimeAdapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditSafetyIndicators.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendRegimeElementsData(){
        for (int i = 0; i < arraylist_regime_element_name_sorted.size(); i++) {
            View view1 = rv_asset_regimes.getChildAt(i);
            tv_value = view1.findViewById(R.id.tv_value);
            et_value = view1.findViewById(R.id.et_value);
            rg_on_off_value = view1.findViewById(R.id.rg_value_on_off);
            rg_yes_no_value = view1.findViewById(R.id.rg_value_yes_no);
            Log.d("RYTJ :" , String.valueOf(arraylist_regime_element_view_sorted));
            if (tv_value.getVisibility()==View.VISIBLE)
                regime_element_value = tv_value.getText().toString();
            else if (et_value.getVisibility()==View.VISIBLE)
                regime_element_value = et_value.getText().toString().trim();
            else if (rg_on_off_value.getVisibility()==View.VISIBLE) {
                int selectedId = rg_on_off_value.getCheckedRadioButtonId();
                RadioButton radioButton = findViewById(selectedId);
                regime_element_value = (String) radioButton.getText();
                rg_on_off_value.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        if (checkedId == R.id.rb_on)
                            regime_element_value = "On";
                        if (checkedId == R.id.rb_off)
                            regime_element_value = "Off";
                    }
                });
            }else if (rg_yes_no_value.getVisibility()==View.VISIBLE) {
                int selectedId = rg_yes_no_value.getCheckedRadioButtonId();
                RadioButton radioButton = findViewById(selectedId);
                regime_element_value = (String) radioButton.getText();
                rg_yes_no_value.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        if (checkedId == R.id.rb_yes)
                            regime_element_value = "Yes";
                        if (checkedId == R.id.rb_no)
                            regime_element_value = "No";
                    }
                });
            }
            arraylist_regime_element_value_new.add(regime_element_value);
        }
        updateRegimeDate();
    }

    private void updateRegimeDate() {
        progressDialog.show();
        int id = 0;
        if (maxDocIdRegimeDetails.equals(""))
            id = 1;
        else
            id = Integer.parseInt(maxDocIdRegimeDetails) + 1;
        current_date = AppData.date();
        current_time = AppData.Time();
        for (int i=0; i< assetRegimeAdapter.getItemCount(); i++) {
            Map<String, Object> mapRegimeDetails = new HashMap<>();
            mapRegimeDetails.put("id", id+i);
            mapRegimeDetails.put("asset_details_id", assetId);
            mapRegimeDetails.put("date", current_date);
            mapRegimeDetails.put("next_thorough_examination", "");
            mapRegimeDetails.put("insert_user", userId);
            mapRegimeDetails.put("insert_time", current_time);
            mapRegimeDetails.put("insert_date", current_date);
            mapRegimeDetails.put("created_at", FieldValue.serverTimestamp());
            mapRegimeDetails.put("updated_at", FieldValue.serverTimestamp());
            mapRegimeDetails.put("submit_type", "App");
            mapRegimeDetails.put("status", "Active");
            mapRegimeDetails.put("regime_element_id", arrayList_regime_element_id_sorted.get(i));
            mapRegimeDetails.put("regime_element_name", arraylist_regime_element_name_sorted.get(i));
            mapRegimeDetails.put("regime_element_value", arraylist_regime_element_value_new.get(i));
            mapRegimeDetails.put("regime_element_view", arraylist_regime_element_view_sorted.get(i));
            mapRegimeDetails.put("time_second_format", AppData.getTimeSecond());
            regimeDetailsReference.document(String.valueOf(id+i)).set(mapRegimeDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        toConfirmation("Safety Indicators Changed successfully.");
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(EditSafetyIndicators.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



    private void toConfirmation(String message) {
        progressDialog.dismiss();
        final View popupView = getLayoutInflater().inflate(R.layout.change_pass_popup_layout, null, true);
         boolean isActivityInForeground = EditSafetyIndicators.this.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED);
        if (isActivityInForeground) {
            popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, true);
            popupWindow.showAtLocation(popupView, Gravity.BOTTOM, 0, 0);
        }
        Button cancelPopup = popupView.findViewById(R.id.bt_ok);
        TextView tv_change_pass = popupView.findViewById(R.id.tv_change_pass);
        tv_change_pass.setText(message);
        cancelPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                Intent toDashboard = new Intent(EditSafetyIndicators.this, AssetManagementDashboard.class);
                startActivity(toDashboard);
                finish();
            }
        });
    }
}