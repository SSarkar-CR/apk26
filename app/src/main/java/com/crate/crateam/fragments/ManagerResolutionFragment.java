package com.crate.crateam.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.CustomViewPager;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.SessionManager;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.crate.crateam.R;
import com.crate.crateam.activities.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.crate.crateam.utility.FirestoreManager;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ManagerResolutionFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "ManagerResolution";
    private Button bt_submit_inspection;
    private SignaturePad signature_manager;
    private CustomViewPager viewPager;
    private CheckBox checkBox;
    private ScrollView sv_manager_resolution;
    private TextView tv_clear,tv_report_id,tv_reported_by,tv_reported_date;
    private SharedPreferences preferences;
    private ProgressDialog progressDialog;
    private String tag,registration_id,odometer_reading,latitude,longitude,address,inspection_id,close_issue ="No",
            driver_name,defected,vehicle_defect,trailer_defect,driver_sign,user_name,sign_date,sign_time;
    Fragment fragment = null;
    private int logged_by,user_role,vehicle_no,trailer_id=0,driver_id=0;
    private boolean isSigned = false,isListenOfflineData = false;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference inspectionSubmissionReference;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.manager_resolution_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initializeView();
    }

    private void initView(View view){
        FirestoreManager.initPersistentIndexManager();
        inspectionSubmissionReference = db.collection("CR_inspection_submission");
        SessionManager sessionManager = new SessionManager(getActivity());
        preferences = getActivity().getSharedPreferences("Preference", 0); // 0 - for private mode
        progressDialog = Dialog.showProgressDialog(getActivity());
        signature_manager = view.findViewById(R.id.signature_manager);
        checkBox = view.findViewById(R.id.checkBox);
        ImageView iv_cross = getActivity().findViewById(R.id.iv_cross);
        viewPager = getActivity().findViewById(R.id.viewpager_workshop_manager);
        viewPager.disableScroll(true);
        sv_manager_resolution = view.findViewById(R.id.sv_manager_resolution);
        bt_submit_inspection = view.findViewById(R.id.bt_submit_inspection);
        tv_report_id = view.findViewById(R.id.tv_report_id);
        tv_reported_by = view.findViewById(R.id.tv_reported_by);
        tv_reported_date = view.findViewById(R.id.tv_reported_date);
        tv_clear = view.findViewById(R.id.tv_clear);
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_role = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ROLE_ONE)));
        logged_by = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        user_name = user.get(SessionManager.KEY_FULL_NAME);
        inspection_id = preferences.getString("view_inspection_id",null);
        loadInspectionData();
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBox.isChecked())
                    close_issue = "Yes";
                else
                    close_issue = "No";
            }
        });
        iv_cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialogFailed("Do you want to cancel report submission ?");
            }
        });
        setSignaturePad();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setSignaturePad(){
        signature_manager.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
                isSigned = true;
            }
            @Override
            public void onSigned() {
                isSigned = true;
            }
            @Override
            public void onClear() {
                isSigned = false;
            }
        });
        signature_manager.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                switch (action){
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        // Disable the scroll view to intercept the touch event
                        sv_manager_resolution.requestDisallowInterceptTouchEvent(true);
                        return false;
                    case MotionEvent.ACTION_UP:
                        // Allow scroll View to intercept the touch event
                        sv_manager_resolution.requestDisallowInterceptTouchEvent(false);
                        return true;
                    default:
                        return true;
                }
            }
        });
    }

    private void initializeView(){
        bt_submit_inspection.setOnClickListener(this);
        tv_clear.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_submit_inspection:
                if (viewPager.getCurrentItem()==2) {
                    sendInspectionSubmissionData();
                    if (!AppData.internetOnline(getActivity())){
                        toSuccessFragment();
                    }
                }
                break;
            case R.id.tv_clear:
                signature_manager.clear();
        }
    }

    private void loadInspectionData(){
        Query inspectionDetails = inspectionSubmissionReference.whereEqualTo("inspection_id",inspection_id);
        inspectionDetails.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if (task.isSuccessful()){
                for (QueryDocumentSnapshot documentSnapshot: task.getResult()){
                    driver_sign = documentSnapshot.getString("driver_sign");
                    driver_name = documentSnapshot.getString("driver_name");
                    latitude = documentSnapshot.getString("latitude");
                    longitude = documentSnapshot.getString("longitude");
                    address = documentSnapshot.getString("address");
                    inspection_id = documentSnapshot.getString("inspection_id");
                    odometer_reading = documentSnapshot.getString("odometer_reading");
                    registration_id = documentSnapshot.getString("registration_no");
                    driver_id = Objects.requireNonNull(documentSnapshot.getLong("driver_id")).intValue();
                    defected = documentSnapshot.getString("defected");
                    vehicle_defect = documentSnapshot.getString("vehicle_defect");
                    trailer_defect = documentSnapshot.getString("trailer_defect");
                    vehicle_no = Objects.requireNonNull(documentSnapshot.getLong("vehicle_id")).intValue();
                    trailer_id = Objects.requireNonNull(documentSnapshot.getLong("trailer_id")).intValue();
                    String conducted_on = documentSnapshot.getString("conducted_on");
                    tv_report_id.setText(inspection_id);
                    tv_reported_by.setText(driver_name);
                    tv_reported_date.setText(conducted_on);
                }
            }
            }
        });
    }

    private void sendInspectionSubmissionData(){
        // get current date
        Date c = Calendar.getInstance().getTime();
        Log.d(TAG,"Current time => " + c);
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = df.format(c);
        sign_date = formattedDate ;
        Bitmap signatureBitmap = signature_manager.getSignatureBitmap();
        String wm_sign = convertTOBase64Image(signatureBitmap);
        //get current time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        sign_time = dateFormatter.format(calendar.getTime());
        Log.d(TAG,"INSPECTION_SUBMISSION_WM :" +inspection_id + " " +close_issue+ " "+wm_sign +" "+sign_date +" "+wm_sign+ " "+user_role);
        progressDialog.show();
        if (!isSigned) {
            Dialog.alertDialog(getActivity(),"Please sign the form.");
            progressDialog.dismiss();
        }else {
            if (close_issue.equals("Yes"))
            reportSubmissionWM(inspection_id,registration_id,vehicle_no,trailer_id,driver_name,driver_sign,logged_by,driver_id,odometer_reading,vehicle_defect,
                    trailer_defect,defected,latitude,longitude,address,sign_date,sign_time,user_role,close_issue, wm_sign,user_name,
                    FieldValue.serverTimestamp(),FieldValue.serverTimestamp(),"");
            else
                reportSubmissionWM(inspection_id,registration_id,vehicle_no,trailer_id,driver_name,driver_sign,logged_by,driver_id,odometer_reading,vehicle_defect,
                        trailer_defect,defected,latitude,longitude,address,sign_date,sign_time,user_role,close_issue, wm_sign,"",
                        FieldValue.serverTimestamp(),FieldValue.serverTimestamp(),"");
            removePreferences();
        }
    }

    private String convertTOBase64Image(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return encoded;
    }

    private void reportSubmissionWM(final String inspection_id, String registration_id, int vehicle_no,int trailer_id, String driver_name, String driver_sign, int logged_by,int driver_id, String odometer_reading,
                                    String vehicle_defect, String trailer_defect, String defected, String latitude, String longitude, String address, String conducted_on, String submission_time,
                                    int user_role, final String close_issue, String wm_sign,String workshop_manager_name, FieldValue created_at, final FieldValue updated_at, String previous_inspection_id){
        Map<String,Object> inspectionData = new HashMap<>();
        inspectionData.put("inspection_id",inspection_id);
        inspectionData.put("id",inspection_id+"_WM");
        inspectionData.put("registration_no",registration_id);
        inspectionData.put("vehicle_id",vehicle_no);
        inspectionData.put("trailer_id",trailer_id);
        inspectionData.put("driver_name",driver_name);
        inspectionData.put("driver_sign",driver_sign);
        inspectionData.put("logged_by",logged_by);
        inspectionData.put("driver_id",driver_id);
        inspectionData.put("odometer_reading",odometer_reading);
        inspectionData.put("vehicle_defect",vehicle_defect);
        inspectionData.put("trailer_defect",trailer_defect);
        inspectionData.put("defected",defected);
        inspectionData.put("latitude",latitude);
        inspectionData.put("longitude",longitude);
        inspectionData.put("address",address);
        inspectionData.put("conducted_on",conducted_on);
        inspectionData.put("submission_time",submission_time);
        inspectionData.put("user_role",user_role);
        inspectionData.put("close_issue",close_issue);
        inspectionData.put("wm_sign",wm_sign);
        inspectionData.put("wm_sign_available","Yes");
        inspectionData.put("workshop_manager_name",workshop_manager_name);
        inspectionData.put("created_at",created_at);
        inspectionData.put("updated_at",updated_at);
        inspectionData.put("previous_inspection_id",previous_inspection_id);
        inspectionData.put("status","Active");
        inspectionData.put("sort_key",5);
        inspectionData.put("drivers_daily_report","Yes");
        inspectionData.put("is_updated","Yes");
        inspectionData.put("time_second_format",AppData.getTimeSecond());
        inspectionSubmissionReference.document(inspection_id+"_WM").set(inspectionData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "WM reports submitted successfully");
                progressDialog.dismiss();
                if (!isListenOfflineData) {
                    updateInspectionSubmissionTable(inspection_id, close_issue, updated_at);
                    toSuccessFragment();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                alertDialogFailed("Unfortunately, Crate App has stopped working. You may have lost your Data. Please try again.");
                Log.e(TAG, "Unable to submit report.");
            }
        });
    }

    private void updateInspectionSubmissionTable(String inspection_id, final String close_issue, final FieldValue updated_at){
        Query updateSubmissionTable = inspectionSubmissionReference.whereEqualTo("inspection_id",inspection_id);
        updateSubmissionTable.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        String doc_id = documentSnapshot.getId();
                        Log.d(TAG,"DOC :" +doc_id);
                        Map<String, Object> updateInspectionTable = new HashMap<>();
                        updateInspectionTable.put("close_issue",close_issue);
                        updateInspectionTable.put("updated_at",updated_at);
                        updateInspectionTable.put("is_updated","Yes");
                        inspectionSubmissionReference.document(doc_id).update(updateInspectionTable);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Unable to update close issue.");
            }
        });
    }

    private void toSuccessFragment(){
        isListenOfflineData = true;
        progressDialog.dismiss();
        fragment = new SuccessFragment();
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment,"REPORT_SUBMIT_SUCCESS");
        ft.addToBackStack("REPORT_SUBMIT_SUCCESS");
        ft.commit();
        tag = fragment.getTag();
        Log.d(TAG,"CHILD_TAG" +fragment.getTag());
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getChildTag(tag);
    }

    private void alertDialogFailed(String message){
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
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
        alertDialog.show();
    }

    private void removePreferences(){
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("vehicle_no");
        editor.remove("driver_name");
        editor.remove("reported_date");
        editor.remove("view_inspection_id");
        editor.apply();
    }
}
