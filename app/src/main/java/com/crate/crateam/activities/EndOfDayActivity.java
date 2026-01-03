package com.crate.crateam.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import com.crate.crateam.R;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.SessionManager;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class EndOfDayActivity extends Activity implements View.OnClickListener {
    private ImageView iv_back_arrow;
    private TextView tv_header, tv_driver_name_nme_value;
    private AppCompatTextView tv_date, tv_time_value;
    private AppCompatEditText et_liters_filled, et_odometer_reading, et_comment;
    private AppCompatButton bt_finish_day, bt_continue_day, bt_close;
    private String user_name = "", comment = "", filled_fuel = "",doc_id="",inspection_id="",date="",submission_date="",send_submission_time="";
    private int user_id = 0,doc_id_length=0,vehicle_number=0;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference endDaySubmissionReference,sortKeyReference,vehicleInspectionReference;
    private ProgressDialog progressDialog;
    private PopupWindow popupWindow;
    private RadioGroup rg_have_fuel;
    private boolean check = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.end_of_day_layout);
        FirestoreManager.initPersistentIndexManager();
        initialized();
        initializeOnClick();
        tv_header.setText("End of Day");
        iv_back_arrow.setImageDrawable(getDrawable(R.drawable.closed));

        rg_have_fuel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) group.findViewById(checkedId);
                filled_fuel = rb.getText().toString().trim();
            }
        });
    }

    private void initialized() {
        SessionManager sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_name = user.get(SessionManager.KEY_FULL_NAME);
        user_id = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        HashMap<String, String> vehicle = sessionManager.getVehicleDetails();
        if (vehicle.get(SessionManager.KEY_VEHICLE_ID) != null)
            vehicle_number = Integer.parseInt((Objects.requireNonNull(vehicle.get(SessionManager.KEY_VEHICLE_ID))));
        iv_back_arrow = findViewById(R.id.iv_back_arrow);
        tv_header = findViewById(R.id.tv_header);
        tv_date = findViewById(R.id.tv_date);
        tv_driver_name_nme_value = findViewById(R.id.tv_driver_name_nme_value);
        et_liters_filled = findViewById(R.id.et_liters_filled);
        et_odometer_reading = findViewById(R.id.et_odometer_reading);
        et_comment = findViewById(R.id.et_comment);
        tv_time_value = findViewById(R.id.tv_time_value);
        bt_finish_day = findViewById(R.id.bt_finish_day);
        bt_continue_day = findViewById(R.id.bt_continue_day);
        bt_close = findViewById(R.id.bt_close);
        rg_have_fuel = findViewById(R.id.rg_have_fuel);
        progressDialog = Dialog.showProgressDialog(this);
        endDaySubmissionReference = db.collection("CR_end_of_day");
        vehicleInspectionReference = db.collection("CR_inspection_submission");
        sortKeyReference = db.collection("CR_sort_key");
        tv_driver_name_nme_value.setText(user_name);
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        date = simpleDateFormat.format(c);
        tv_date.setText(date);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("hh:mm a");
        String time = dateFormatter.format(calendar.getTime());
        tv_time_value.setText(time);
        getMaximumLength();
        fetchInspectionId();
    }

    private void initializeOnClick() {
        bt_finish_day.setOnClickListener(this);
        bt_continue_day.setOnClickListener(this);
        bt_close.setOnClickListener(this);
        iv_back_arrow.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_finish_day:
                if (checkValidation()) {
                    sendEndOfDayData();
                    SharedPreferences preferences = getApplication().getSharedPreferences("", 0);
                    SharedPreferences.Editor editor = preferences.edit();
                    if (preferences.contains("isSubmitted"))
                        editor.remove("isSubmitted");
                    if (preferences.contains("coming_from"))
                        editor.remove("coming_from");
                    editor.clear();
                    editor.apply();
                    if (!AppData.internetOnline(EndOfDayActivity.this)) {
                        progressDialog.dismiss();
                        toConfirmation("You have finished your day successfully.");
                    }
                }
                break;
            case R.id.bt_continue_day:
                nextPage("Do you want to continue?");
                break;
            case R.id.bt_close:
            case R.id.iv_back_arrow:
                finish();
                break;
        }
    }

    private void sendEndOfDayData() {
        String manifest_date = tv_date.getText().toString().trim();
        String time = tv_time_value.getText().toString().trim();
        String liters_filled = et_liters_filled.getText().toString().trim();
        String odometer_reading = et_odometer_reading.getText().toString().trim();
        comment = et_comment.getText().toString().trim();
        storeEndOfDayData(manifest_date, time, filled_fuel, liters_filled, odometer_reading, comment);
    }

    private void storeEndOfDayData(String manifest_date, String time, String filled_fuel, String liters_filled, String odometer_reading, String comment) {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("ddMMyyyy");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = df.format(c);
        String conducted_on = formattedDate;

        submission_date = simpleDateFormat.format(c);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HHmm", Locale.getDefault());
        SimpleDateFormat end_day_management = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String submission_time = dateFormatter.format(calendar.getTime());
        send_submission_time = end_day_management.format(calendar.getTime());

        Log.d("Inspection Id :" ,inspection_id);
        Map<String, Object> stringStringMap = new HashMap<>();
        stringStringMap.put("driver_name", tv_driver_name_nme_value.getText().toString().trim());
        stringStringMap.put("manifest_date", manifest_date);
        stringStringMap.put("filled_fuel", filled_fuel);
        stringStringMap.put("liters_filled", liters_filled);
        stringStringMap.put("odometer_reading", odometer_reading);
        stringStringMap.put("comment", comment);
        stringStringMap.put("user_id", user_id);
        stringStringMap.put("submission_date", submission_date);
        stringStringMap.put("submission_time", send_submission_time);
        stringStringMap.put("status","Active");
        stringStringMap.put("id",user_id + "_" + conducted_on + "_" + submission_time);
        doc_id_length= doc_id_length+1;
        stringStringMap.put("sort_key",doc_id_length);
        stringStringMap.put("inspection_id",inspection_id);
        stringStringMap.put("is_updated","Yes");
        stringStringMap.put("time_second_format",AppData.getTimeSecond());
        progressDialog.show();
        endDaySubmissionReference.document(user_id + "_" + conducted_on + "_" + submission_time).set(stringStringMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    toConfirmation("You have finished your day successfully.");
                    Map<String,Object> objectMap_update = new HashMap<>();
                    objectMap_update.put("end_day_key",doc_id_length);
                    sortKeyReference.document(doc_id).update(objectMap_update);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Dialog.alertDialog(EndOfDayActivity.this, e.getMessage());
            }
        });
    }

    private void fetchInspectionId(){
        Query query = vehicleInspectionReference.whereEqualTo("logged_by",user_id).whereEqualTo("conducted_on",date)
                .whereEqualTo("vehicle_id",vehicle_number).whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
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

    private void toConfirmation(String message) {
        progressDialog.dismiss();
        final View popupView = getLayoutInflater().inflate(R.layout.change_pass_popup_layout, null, true);
        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, true);
        popupWindow.showAtLocation(popupView, Gravity.BOTTOM, 0, 0);
        Button cancelPopup = popupView.findViewById(R.id.bt_ok);
        TextView tv_change_pass = popupView.findViewById(R.id.tv_change_pass);
        tv_change_pass.setText(message);
        cancelPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                Intent toDashboard = new Intent(EndOfDayActivity.this, MainActivity.class);
                startActivity(toDashboard);
            }
        });
    }

    
//

    private void nextPage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private boolean checkValidation() {
        if (filled_fuel.isEmpty())
            Dialog.alertDialog(EndOfDayActivity.this, "Please select Yes or No of  Have yo filled fuel?");
        else if (et_liters_filled.getText().toString().isEmpty() && filled_fuel.equals("Yes"))
            Dialog.alertDialog(EndOfDayActivity.this, "Please enter the value of fuel");
        else if (et_odometer_reading.getText().toString().trim().isEmpty())
            Dialog.alertDialog(EndOfDayActivity.this, "Please enter odometer reading");
        else
            check = true;
        return check;
    }

    @Override
    public void onBackPressed() {
    }

    // sort key table
    private void getMaximumLength(){
        sortKeyReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if (queryDocumentSnapshot.contains("end_day_key")){
                                doc_id_length = queryDocumentSnapshot.getLong("end_day_key").intValue();
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
                Toast.makeText(EndOfDayActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
