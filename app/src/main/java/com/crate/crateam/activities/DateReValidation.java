package com.crate.crateam.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;

import com.crate.crateam.R;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.crate.crateam.utility.FirestoreManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DateReValidation extends AppCompatActivity implements View.OnClickListener,DatePickerDialog.OnDateSetListener {

    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference regimeDetailsReference,assetDetailsReference;
    private TextView tv_asset_type,tv_asset_id,tv_asset_name,tv_date_picker_1,tv_date_picker_2;
    private Button bt_save,bt_back;
    private int assetId=0,userId=0,regime_id=0,selectedDatePicker=0,dateReValidationId=0;
    private String asset_type="",asset_number="",asset_name="", currentDate="",currentTime="",regime_name="",
            datePickerOneValue="",datePickerTwoValue="",maxDocIdRegimeDetails="",asset_nri_failed="";
    private DatePickerDialog datePickerDialog;
    private RelativeLayout rl_date_picker_1,rl_date_picker_2;
    private CheckBox cb_date_caution;
    private boolean isValidationDone = false;
    private ArrayList<Integer> regimeElementsIds = new ArrayList<>();
    private ArrayList<String> assetsRequiringInspection= new ArrayList<>();
    private ProgressDialog progressDialog;
    private PopupWindow popupWindow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.asset_date_re_validation);
        initView();
        initializeOnClick();
    }
    private void initView(){
        progressDialog = Dialog.showProgressDialog(this);
        regimeDetailsReference = db.collection("AM_asset_revalidation_details");
        assetDetailsReference = db.collection("AM_asset_details");
        SessionManager sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        userId = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        Log.d("USER_ID: " , String.valueOf(userId));
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            asset_type= extras.getString("asset_type");
            asset_number= extras.getString("asset_id");
            asset_name= extras.getString("asset_name");
            regime_id = extras.getInt("regime_id");
            regime_name = extras.getString("regime_name");
            assetId = extras.getInt("assetId");
            asset_nri_failed = extras.getString("asset_nri_failed");
        }
        assetsRequiringInspection = (ArrayList<String>) getIntent().getSerializableExtra("asset_date_failed");
        Log.d("FUKYF :" ,regime_id+" "+regime_name+" "+assetsRequiringInspection+" "+asset_nri_failed);
        tv_asset_type = findViewById(R.id.tv_asset_type);
        tv_asset_id = findViewById(R.id.tv_asset_id);
        tv_asset_name = findViewById(R.id.tv_asset_name);
        tv_asset_type.setText(asset_type);
        tv_asset_id.setText(asset_number);
        tv_asset_name.setText(asset_name);
        tv_date_picker_1 = findViewById(R.id.tv_date_picker_1);
        tv_date_picker_2 = findViewById(R.id.tv_date_picker_2);
        bt_back = findViewById(R.id.bt_back);
        bt_save = findViewById(R.id.bt_save);
        cb_date_caution = findViewById(R.id.cb_date_caution);
        rl_date_picker_1 = findViewById(R.id.rl_date_picker_1);
        rl_date_picker_2 = findViewById(R.id.rl_date_picker_2);
        datePickerDialog = new DatePickerDialog(
                DateReValidation.this, DateReValidation.this, Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DATE));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        getMaxDocIDRegimeDetails();
    }
    private void initializeOnClick(){
        bt_back.setOnClickListener(this);
        bt_save.setOnClickListener(this);
        rl_date_picker_1.setOnClickListener(this);
        rl_date_picker_2.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_back:
                finish();
                break;
            case R.id.bt_save:
                if (checkValidation()){
                    updateRegimeDate();
                    updateAssetDetails();
                    if (!AppData.internetOnline(DateReValidation.this))
                        toConfirmation("Date re-validate successfully.");
                }
                break;
            case R.id.rl_date_picker_1:
                selectedDatePicker = 1;
                datePickerDialog.show();
                break;
            case R.id.rl_date_picker_2:
                selectedDatePicker = 2;
                datePickerDialog.show();
                break;
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String day = String.valueOf(dayOfMonth);
        String Month = String.valueOf(month+1);
        String Year = String.valueOf(year);
        if(dayOfMonth < 10){
            day = "0" + dayOfMonth;
        }
        if(month < 9){
            Month  = "0" + Month ;
        }
        String pickedDate = day + "-" + Month + "-" + Year;
        Log.d("HDGHGMC :" ,pickedDate);
        if (selectedDatePicker == 1) {
            datePickerOneValue = pickedDate;
            tv_date_picker_1.setText(datePickerOneValue);
        } else if (selectedDatePicker == 2) {
            datePickerTwoValue = pickedDate;
            tv_date_picker_2.setText(datePickerTwoValue);
        }
    }

    private void getMaxDocIDRegimeDetails(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        regimeDetailsReference.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                Toast.makeText(DateReValidation.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void updateRegimeDate() {
        progressDialog.show();
        if (maxDocIdRegimeDetails.equals(""))
            dateReValidationId = 1;
        else
            dateReValidationId = Integer.parseInt(maxDocIdRegimeDetails) + 1;
       currentDate = AppData.date();
currentTime = AppData.Time();
        Map<String, Object> mapRegimeDetails = new HashMap<>();
        mapRegimeDetails.put("id", dateReValidationId);
        mapRegimeDetails.put("asset_details_id", assetId);
        mapRegimeDetails.put("date", currentDate);
        mapRegimeDetails.put("next_thorough_examination", "");
        mapRegimeDetails.put("insert_user", userId);
        mapRegimeDetails.put("insert_time", currentTime);
        mapRegimeDetails.put("insert_date", currentDate);
        mapRegimeDetails.put("created_at", FieldValue.serverTimestamp());
        mapRegimeDetails.put("updated_at", FieldValue.serverTimestamp());
        mapRegimeDetails.put("submit_type", "App");
        mapRegimeDetails.put("status", "Active");
        mapRegimeDetails.put("regime_element_id", regime_id);
        mapRegimeDetails.put("regime_element_name", regime_name);
        mapRegimeDetails.put("regime_element_value", datePickerOneValue);
        mapRegimeDetails.put("regime_element_view", "TextView");
        mapRegimeDetails.put("current_date_format",AppData.getDateFormatTwo(datePickerOneValue));
        mapRegimeDetails.put("time_second_format", AppData.getTimeSecond());
        regimeDetailsReference.document(String.valueOf(dateReValidationId)).set(mapRegimeDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
//                    updateAssetDetails();
                    toConfirmation("Date re-validate successfully.");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(DateReValidation.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAssetDetails(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = assetDetailsReference.whereEqualTo("status", "Active").whereEqualTo("id",assetId);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            String doc_id = queryDocumentSnapshot.getId();
                            Map<String, Object> update_asset_details = new HashMap<>();
                            update_asset_details.put("date_1",datePickerOneValue);
                            if (assetsRequiringInspection.isEmpty() && asset_nri_failed.equals("No"))
                                update_asset_details.put("asset_current_status","Compliant");
                            else if (assetsRequiringInspection.size()==1)
                                update_asset_details.put("asset_current_status","Non Compliant");
                            assetDetailsReference.document(doc_id).update(update_asset_details);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DateReValidation.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

  

    private Boolean checkValidation(){
        if (datePickerOneValue.equals(""))
            Dialog.alertDialog(this, "Please select new date.");
        else if (datePickerTwoValue.equals(""))
            Dialog.alertDialog(this, "Please confirm new date.");
        else if (!datePickerOneValue.equals(datePickerTwoValue))
            Dialog.alertDialog(this, "New date and confirm date does not match.");
        else if (!cb_date_caution.isChecked())
            Dialog.alertDialog(this,"Please select check box.");
        else
            isValidationDone = true;
        return isValidationDone;
    }

    private void toConfirmation(String message) {
        progressDialog.dismiss();
        final View popupView = getLayoutInflater().inflate(R.layout.change_pass_popup_layout, null, true);
         boolean isActivityInForeground = DateReValidation.this.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED);
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
                finish();
            }
        });
    }

}