package com.crate.crateam.fragments;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crate.crateam.R;
import com.crate.crateam.activities.GPSTracker;
import com.crate.crateam.activities.MainActivity;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.CustomViewPagerInspection;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.SessionManager;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.crate.crateam.utility.FirestoreManager;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class InspectionSignatureFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "INSPECTION_SIGN_FRAG";
    private ProgressDialog progressDialog;
    private LinearLayout ll_driver_signature,ll_completion_tag;
    private Button bt_submit_inspection,bt_add_signature;
    Fragment fragment = null;
    private SignaturePad signaturePad;
    private SessionManager sessionManager;
    private CustomViewPagerInspection viewPager;
    private ImageView iv_nfcTag_cabin,iv_cross,iv_previous_page;
    private TextView tv_clear,tv_inside_cabin_tag,tv_previous_page;
    private EditText et_odometer_reading;
    private ScrollView sv_inspection_signature;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String tag,nfc_data,trailer="",user_name,conducted_on="" ,completion_tag="" ,submission_time="",
            vehicle_defect="",trailer_defect="",registration_id,inspection_id="",defected,latitude="",longitude="",address="",odometer_reading,
            driver_sign="",close_issue,trailer_id="",doc_id="",vehicle_type="",haulier="",haulier_carrier="",manufacturer="",
            model="",email="",inspection_result ="",email_pdf_logo="",with_nfc="";
    private int count=0,logged_by,user_role,vehicle_no=0,doc_id_length=0,failed_element_count=0,failed_element_vehicle=0,failed_element_trailer=0;
    private boolean isSigned = false,isListenOfflineData = false;
    MainActivity mainActivity ;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference vehicleDefectsReference,inspectionSubmissionReference,currentVehicleReference,sortKeyTableReference,vehicleElementsReference,
            vehicleDefectInspectionsComment,jobManagementDetailsNewReference;
    ArrayList<Integer> newDefectsElementId = new ArrayList<>();
    private String mail_send="";
    private ArrayList<String> defectsListVehicle = new ArrayList<>();
    private ArrayList<String> document_id = new ArrayList<>();
    private ArrayList<Integer> defectsListIdVehicle = new ArrayList<>();
    private ArrayList<String> elementDefectListVehicle = new ArrayList<>();
    private ArrayList<String> wmCommentListVehicle = new ArrayList<>();
    private ArrayList<Integer> elementIdListVehicle = new ArrayList<>();
    private ArrayList<Integer> storePositionVehicle = new ArrayList<>();
    private ArrayList<Integer> storePositionVehicleId = new ArrayList<>();
    private ArrayList<String> defectsListTrailer = new ArrayList<>();
    private ArrayList<Integer> defectsListIdTrailer = new ArrayList<>();
    private ArrayList<String> elementDefectListTrailer = new ArrayList<>();
    private ArrayList<String> wmCommentListTrailer = new ArrayList<>();
    private ArrayList<Integer> elementIdListTrailer = new ArrayList<>();
    private ArrayList<Integer> storePositionTrailer = new ArrayList<>();
    private ArrayList<Integer> storePositionTrailerId = new ArrayList<>();
    private ArrayList<Bitmap> defectsImagesList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.inspection_signature_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initializeOnClick();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView(View view){
        FirestoreManager.initPersistentIndexManager();
        vehicleDefectsReference = db.collection("CR_vehicle_defect_inspections");
        inspectionSubmissionReference = db.collection("CR_inspection_submission");
        currentVehicleReference = db.collection("CR_current_vehicle");
        sortKeyTableReference = db.collection("CR_sort_key");
        vehicleElementsReference = db.collection("CR_vehicle_elements");
        jobManagementDetailsNewReference = db.collection("CR_job_management_new_details");
        sessionManager = new SessionManager(getActivity());
        iv_cross = getActivity().findViewById(R.id.iv_cross);
        viewPager = getActivity().findViewById(R.id.viewpager);
        viewPager.setAllowedSwipeDirection(CustomViewPagerInspection.SwipeDirection.NONE);
        progressDialog = Dialog.showProgressDialog(getActivity());
        bt_submit_inspection = view.findViewById(R.id.bt_submit_inspection);
        bt_add_signature = view.findViewById(R.id.bt_add_signature);
        ll_completion_tag = view.findViewById(R.id.ll_completion_tag);
        ll_driver_signature = view.findViewById(R.id.ll_driver_signature);
        tv_clear = view.findViewById(R.id.tv_clear);
        tv_previous_page = view.findViewById(R.id.tv_previous_page);
        tv_inside_cabin_tag = view.findViewById(R.id.tv_inside_cabin_tag);
        et_odometer_reading = view.findViewById(R.id.et_odometer_reading);
        signaturePad = view.findViewById(R.id.signature_pad);
        sv_inspection_signature = view.findViewById(R.id.sv_inspection_signature);
        iv_nfcTag_cabin = view.findViewById(R.id.iv_nfcTag_cabin);
        iv_previous_page= view.findViewById(R.id.iv_previous_page);
        HashMap<String, String> vehicle = sessionManager.getVehicleDetails();
        HashMap<String, String> user = sessionManager.getUserDetails();
        completion_tag = vehicle.get(SessionManager.KEY_VEHICLE_COMPLETION);
        if (vehicle.get(SessionManager.KEY_TRAILER)!= null)
            trailer = vehicle.get(SessionManager.KEY_TRAILER);
        if (vehicle.get(SessionManager.KEY_VEHICLE_ID)!= null)
            vehicle_no = Integer.parseInt(Objects.requireNonNull(vehicle.get(SessionManager.KEY_VEHICLE_ID)));
        if (vehicle.get(SessionManager.KEY_TRAILER_ID)!= null)
            trailer_id = vehicle.get(SessionManager.KEY_TRAILER_ID);
        registration_id = vehicle.get(SessionManager.KEY_REGISTRATION_NO);
        Log.d(TAG,"Vehicle Id :" +vehicle_no+ "  "+registration_id+" "+trailer);
        logged_by = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        user_name = user.get(SessionManager.KEY_FULL_NAME);
        user_role = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ROLE_ONE)));
        vehicle_type = vehicle.get(SessionManager.KEY_VEHICLE_TYPE);
        haulier = vehicle.get(SessionManager.KEY_HAULIER);
        haulier_carrier = vehicle.get(SessionManager.KEY_HAULIER_CARRIER);
        manufacturer = vehicle.get(SessionManager.KEY__MANUFACTURER);
        model = vehicle.get(SessionManager.KEY_MODEL);
        email = user.get(SessionManager.KEY_EMAIL);
        pref = getActivity().getSharedPreferences("MyPref", 0); //0 - for private mode
        editor = pref.edit();
        if (viewPager.getCurrentItem()==3) {
            inspection_id = pref.getString("inspection_id", null);
            vehicle_defect = pref.getString("vehicle_defect", null);
            trailer_defect = pref.getString("trailer_defect", null);
            with_nfc = pref.getString("with_nfc",null);
        }

        mainActivity = (MainActivity) getActivity();
        mainActivity.getChildTag(TAG);
        getVehicleDefects();
        getTrailerDefects();
        getDefectImages(inspection_id);
        getMaximumLength();
        Log.d(TAG,"VEHICLE_INSPECTION_ID :" +inspection_id);
        if (with_nfc.equals("Yes")) {
            ll_completion_tag.setVisibility(View.VISIBLE);
            setTag();
        }else
            ll_completion_tag.setVisibility(View.GONE);
        setSignaturePad();
    }

    private void setTag(){
        if (getArguments()!= null){
            nfc_data = this.getArguments().getString("message");
            count = this.getArguments().getInt("counter");
            Log.d(TAG,"NFC_Data Sign: "  +nfc_data +" "+count+" "+completion_tag);
        }
        if (count!= 0){
            iv_previous_page.setVisibility(View.GONE);
            tv_previous_page.setVisibility(View.GONE);
        }
        if (trailer != null) {
            if (trailer.equals("No")) {
                if (count >= 4) {
                    if (nfc_data.equals(completion_tag)) {
                        Dialog.successfullyScanDialog(getActivity());
                        ll_completion_tag.setBackgroundColor(Color.parseColor("#00CC66"));
                        tv_inside_cabin_tag.setVisibility(View.VISIBLE);
                        editor.putString("nfc4", nfc_data); // Storing string
                        editor.apply();
                        tv_inside_cabin_tag.setText(completion_tag);
                        Log.d(TAG,"NFC6  :" + count);
                    } else {
                        Dialog.alertDialog(getActivity(),"Tag not recognised. Please scan again.");
                        count = 3;
                        mainActivity.getCount(count);
                    }
                }
            }
            if (trailer.equals("Yes")) {
                if (count >= 6) {
                    if (nfc_data.equals(completion_tag)) {
                        Dialog.successfullyScanDialog(getActivity());
                        ll_completion_tag.setBackgroundColor(Color.parseColor("#00CC66"));
                        tv_inside_cabin_tag.setVisibility(View.VISIBLE);
                        editor.putString("nfc6", nfc_data); // Storing string
                        editor.apply();
                        tv_inside_cabin_tag.setText(completion_tag);
                        Log.d(TAG,"NFC6  :" + nfc_data);
                    } else {
                        Dialog.alertDialog(getActivity(),"Tag not recognised. Please scan again.");
                        count = 5;
                        mainActivity.getCount(count);
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setSignaturePad(){
        signaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
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
        signaturePad.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                switch (action){
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        // Disable the scroll view to intercept the touch event
                        sv_inspection_signature.requestDisallowInterceptTouchEvent(true);
                        return false;
                    case MotionEvent.ACTION_UP:
                        // Allow scroll View to intercept the touch event
                        sv_inspection_signature.requestDisallowInterceptTouchEvent(false);
                        return true;
                    default:
                        return true;
                }
            }
        });
    }
    private void initializeOnClick(){
        bt_submit_inspection.setOnClickListener(this);
        tv_clear.setOnClickListener(this);
        iv_nfcTag_cabin.setOnClickListener(this);
        bt_add_signature.setOnClickListener(this);
        iv_previous_page.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_cross:
                alertDialogFailed("Do you want to cancel inspection ?");
                break;
            case R.id.bt_submit_inspection:
                if (with_nfc.equals("Yes") && TextUtils.isEmpty(tv_inside_cabin_tag.getText().toString()))
                    Dialog.alertDialog(getActivity(),"Please scan completion NFC tag placed on the vehicle.");
                else if (with_nfc.equals("Yes") && !tv_inside_cabin_tag.getText().toString().equals(completion_tag))
                    Dialog.alertDialog(getActivity(),"Tag not recognised. Please scan again.");
                else {
                    iv_previous_page.setVisibility(View.INVISIBLE);
                    sendInspectionSubmissionData();
                }
                break;
            case R.id.tv_clear:
                signaturePad.clear();
                Toast.makeText(getActivity(),"Sign Cleared",Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_nfcTag_cabin:
                Dialog.readyToScanDialog(getActivity());
                break;
            case R.id.iv_previous_page:
                if (trailer.equals("Yes"))
                viewPager.setCurrentItem(2);
                else
                    viewPager.setCurrentItem(1);
                break;
            case R.id.bt_add_signature:
                bt_add_signature.setVisibility(View.GONE);
                ll_driver_signature.setVisibility(View.VISIBLE);
                bt_submit_inspection.setVisibility(View.VISIBLE);
                break;
        }
    }

    private String convertTOBase64Image(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return encoded;
    }

    private void sendInspectionSubmissionData(){
        // get current date
        Date c = Calendar.getInstance().getTime();
        Log.d(TAG,"Current time => " + c);
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = df.format(c);
        conducted_on = formattedDate ;
        //get current time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        submission_time = dateFormatter.format(calendar.getTime());
        odometer_reading = et_odometer_reading.getText().toString();
        Log.d(TAG,"InsId :"+inspection_id +vehicle_defect+trailer_defect);
        Bitmap signatureBitmap = signaturePad.getSignatureBitmap();
        driver_sign = convertTOBase64Image(signatureBitmap);
        HashMap<String, String> vehicle = sessionManager.getVehicleDetails();
        if (vehicle.get(SessionManager.KEY_TRAILER)!= null)
            trailer = vehicle.get(SessionManager.KEY_TRAILER);
        if (vehicle.get(SessionManager.KEY_VEHICLE_ID)!= null)
            vehicle_no = Integer.parseInt(Objects.requireNonNull(vehicle.get(SessionManager.KEY_VEHICLE_ID)));
        if (vehicle.get(SessionManager.KEY_TRAILER_ID)!= null)
            trailer_id = vehicle.get(SessionManager.KEY_TRAILER_ID);
        registration_id = vehicle.get(SessionManager.KEY_REGISTRATION_NO);
        Log.d(TAG,"Vehicle Id :" +vehicle_no+ "  "+registration_id+" "+trailer+" "+trailer_id);
        GPSTracker finder = new GPSTracker(getActivity());
        Double lat= 0.0 , lon = 0.0 ;
        if (!finder.checkLocationPermission()){
            finder.requestPermission();
        }
        if (finder.canGetLocation()) {
            close_issue = "";
            lat = finder.getLatitude();
            lon = finder.getLongitude();
            double latOnline = round(lat,4);
            double lonOnline = round(lon,4);
             latitude = String.valueOf(lat);
             longitude = String.valueOf(lon);
             address = finder.getAddress(lat,lon);
            Log.d(TAG,"ADDRESS :" + trailer+" "+registration_id);
            if (trailer != null) {
                if (trailer.equals("No")) {
                    progressDialog.show();
                    if (vehicle_defect != null) {
                        if (vehicle_defect.equals("Yes")) {
                            defected = "Yes";
                            close_issue = "No";
                        } else {
                            defected = "No";
                            close_issue = "Yes";
                        }
                    }
                    if (TextUtils.isEmpty(odometer_reading)) {
                        Dialog.alertDialog(getActivity(), "Please enter odometer reading.");
                        progressDialog.dismiss();
                    } else if (!isSigned) {
                        Dialog.alertDialog(getActivity(), "Please sign the form.");
                        progressDialog.dismiss();
                    } else {
                        if (trailer.equals("No")) {
                            sendInspectionData(inspection_id, registration_id, vehicle_no, 0, user_name, logged_by, user_role, conducted_on,
                                    submission_time, odometer_reading, driver_sign, vehicle_defect, "No", defected, latitude, longitude,
                                    FieldValue.serverTimestamp(), FieldValue.serverTimestamp(), mail_send, close_issue, address, "");
                        } else {
                            if (trailer_id.equals(""))
                                trailer_id = String.valueOf(0);
                            sendInspectionData(inspection_id, registration_id, vehicle_no, Integer.parseInt(trailer_id), user_name, logged_by, user_role, conducted_on,
                                    submission_time, odometer_reading, driver_sign, vehicle_defect, trailer_defect, defected, latitude, longitude,
                                    FieldValue.serverTimestamp(), FieldValue.serverTimestamp(), mail_send, close_issue, address, "");
                        }
                        Log.d(TAG,"All_SUBMISSION_DATA :" + inspection_id + " " + registration_id + " " + vehicle_no + " " + user_name + " " + logged_by + " "
                                + user_role + " " + conducted_on + " " + submission_time + " " + odometer_reading + " " + driver_sign + " " + vehicle_defect + " " + trailer_defect + " "
                                + defected + " " + latitude + " " + longitude + " " + close_issue + " " + address + " ");
                        if (!AppData.internetOnline(getActivity())) {
                            toConfirmation();
                        }
                    }
                } else {
                    progressDialog.show();
                    if (vehicle_defect != null && trailer_defect != null) {
                        if (vehicle_defect.equals("Yes") || trailer_defect.equals("Yes")) {
                            defected = "Yes";
                            close_issue = "No";
                        } else {
                            defected = "No";
                            close_issue = "Yes";
                        }
                    }
                    if (odometer_reading.length() == 0) {
                        Dialog.alertDialog(getActivity(), "Please enter odometer reading.");
                        progressDialog.dismiss();
                    } else if (!isSigned) {
                        Dialog.alertDialog(getActivity(), "Please sign the form.");
                        progressDialog.dismiss();
                    } else {
                        if (trailer.equals("No")) {
                            sendInspectionData(inspection_id, registration_id, vehicle_no, 0, user_name, logged_by, user_role, conducted_on,
                                    submission_time, odometer_reading, driver_sign, vehicle_defect, "No", defected, latitude, longitude,
                                    FieldValue.serverTimestamp(), FieldValue.serverTimestamp(), mail_send, close_issue, address, "");
                        } else {
                            if (trailer_id.equals(""))
                                trailer_id = String.valueOf(0);
                            sendInspectionData(inspection_id, registration_id, vehicle_no, Integer.parseInt(trailer_id), user_name, logged_by, user_role, conducted_on,
                                    submission_time, odometer_reading, driver_sign, vehicle_defect, trailer_defect, defected, latitude, longitude,
                                    FieldValue.serverTimestamp(), FieldValue.serverTimestamp(), mail_send, close_issue, address, "");
                        }
                    }
                }
            }
        } else {
            finder.checkLocationPermission();
            Dialog.alertDialog(getActivity(),"Please enable your device location before submitting the form.");
        }
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void deleteDocument(){
        Query deleteQuey = vehicleDefectsReference.whereEqualTo("inspection_id",inspection_id);
        deleteQuey.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                Toast.makeText(getActivity(), "CLICKED",Toast.LENGTH_SHORT).show();
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    String document_id = documentSnapshot.getId();
                    vehicleDefectsReference.document(document_id).delete();
                }
            }
        });
    }

    @SuppressLint("DefaultLocale")
    private void sendInspectionData(String inspectionId, String registration_id, final int vehicle_no, int trailer_id, String driver_name, final int logged_by, int user_role, final String conducted_on,
                                    final String submission_time, final String odometer_reading, final String driver_sign, String vehicle_defect, String trailer_defect, String defected, String latitude, String longitude,
                                    FieldValue created_at, FieldValue updated_at, String mail_sent, String close_issue, final String address, String previous_inspection_id){
        Map<String,Object> inspectionData = new HashMap<>();
        inspectionData.put("inspection_id",inspectionId);
        inspectionData.put("id",inspectionId);
        inspectionData.put("registration_no",registration_id);
        inspectionData.put("vehicle_id",vehicle_no);
        inspectionData.put("trailer_id",trailer_id);
        inspectionData.put("driver_name",driver_name);
        inspectionData.put("logged_by",logged_by);
        inspectionData.put("driver_id",logged_by);
        inspectionData.put("user_role",user_role);
        inspectionData.put("conducted_on",conducted_on);
        inspectionData.put("submission_time",submission_time);
        inspectionData.put("odometer_reading",odometer_reading);
        inspectionData.put("driver_sign",driver_sign);
        inspectionData.put("vehicle_defect",vehicle_defect);
        inspectionData.put("trailer_defect",trailer_defect);
        inspectionData.put("defected",defected);
        inspectionData.put("latitude",latitude);
        inspectionData.put("longitude",longitude);
        inspectionData.put("created_at", created_at);
        inspectionData.put("updated_at", updated_at);
        inspectionData.put("wm_sign","");
        inspectionData.put("wm_sign_available","No");
        inspectionData.put("mail_send","No");
        inspectionData.put("pdf_send","No");
        inspectionData.put("close_issue",close_issue);
        inspectionData.put("address",address);
        inspectionData.put("previous_inspection_id",previous_inspection_id);
        inspectionData.put("status","Active");
        doc_id_length = doc_id_length+1;
        inspectionData.put("sort_key",doc_id_length);
        inspectionData.put("drivers_daily_report","Yes");
        inspectionData.put("time_second_format",AppData.getTimeSecond());
        inspectionData.put("is_updated","Yes");
        Log.e("inspection_id1234",inspection_id);
        inspectionSubmissionReference.document(inspection_id).set(inspectionData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onSuccess(Void aVoid) {
                if (!isListenOfflineData)
                 toConfirmation();
                updateCurrentVehicle(logged_by,vehicle_no);
                updateJobManagement(logged_by,vehicle_no,conducted_on);
                getOpenTaskJobIds();
                updatedVehicleLatestDefect(inspection_id);
                updatedTrailerLatestDefect(inspection_id);
                Map<String,Object> objectMap_update = new HashMap<>();
                objectMap_update.put("inspection_submission_key",doc_id_length);
                sortKeyTableReference.document(doc_id).update(objectMap_update);
                Log.d(TAG, "Inspection submitted successfully");
                Log.d(TAG,"INSPECTION :" +"I am here.");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                alertDialogFailed("Unfortunately, Crate App has stopped working. You may have lost your Data. Please try again.");
                Log.e(TAG, "Unable to submit post.");
            }
        });
    }

    private void updatedVehicleLatestDefect(final String inspectionId){
        Query query = vehicleDefectsReference.whereEqualTo("inspection_id",inspectionId).whereEqualTo("element_type","vehicle")
                .whereEqualTo("close_issue","No");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                        String doc_id = queryDocumentSnapshot.getId();
                        newDefectsElementId.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("element_id")).intValue());
                        HashMap<String,Object> updateDefects = new HashMap<>();
                        updateDefects.put("latest_defect","Yes");
                        vehicleDefectsReference.document(doc_id).update(updateDefects);
                    }
                    for (int i=0; i<newDefectsElementId.size(); i++){
                        updateOldVehicleDefect(newDefectsElementId.get(i));
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Unable to update latest defects.");
            }
        });
    }

    private void updateOldVehicleDefect(int element_id){
        Query query = vehicleDefectsReference.whereEqualTo("vehicle_id",vehicle_no).whereEqualTo("element_id",element_id)
                .whereEqualTo("element_type","vehicle").whereEqualTo("close_issue","No");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                        String doc_id = queryDocumentSnapshot.getId();
                        String inspectionId= queryDocumentSnapshot.getString("inspection_id");
                        if (!inspectionId.equals(inspection_id)) {
                            HashMap<String, Object> updateDefects = new HashMap<>();
                            updateDefects.put("latest_defect", "No");
                            vehicleDefectsReference.document(doc_id).update(updateDefects);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Unable to update old defects.");
            }
        });
    }

    private void updatedTrailerLatestDefect(final String inspectionId){
        Query query = vehicleDefectsReference.whereEqualTo("inspection_id",inspectionId).whereEqualTo("element_type","trailer")
                .whereEqualTo("close_issue","No");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                        String doc_id = queryDocumentSnapshot.getId();
                        newDefectsElementId.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("element_id")).intValue());
                        HashMap<String,Object> updateDefects = new HashMap<>();
                        updateDefects.put("latest_defect","Yes");
                        vehicleDefectsReference.document(doc_id).update(updateDefects);
                    }
                    for (int i=0; i<newDefectsElementId.size(); i++){
                        updateOldTrailerDefect(newDefectsElementId.get(i));
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Unable to update latest defects.");
            }
        });
    }

    private void updateOldTrailerDefect(int element_id){
        Query query = vehicleDefectsReference.whereEqualTo("vehicle_id",vehicle_no).whereEqualTo("element_id",element_id)
                .whereEqualTo("element_type","trailer").whereEqualTo("close_issue","No");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                        String doc_id = queryDocumentSnapshot.getId();
                        String inspectionId= queryDocumentSnapshot.getString("inspection_id");
                        if (!inspectionId.equals(inspection_id)) {
                            HashMap<String, Object> updateDefects = new HashMap<>();
                            updateDefects.put("latest_defect", "No");
                            vehicleDefectsReference.document(doc_id).update(updateDefects);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Unable to update old defects.");
            }
        });
    }

    private void updateCurrentVehicle(int user_id,int vehicle_id){
        Map<String,Object> currentVehicle = new HashMap<>();
        currentVehicle.put("user_id",user_id);
        currentVehicle.put("inspected","Yes");
        currentVehicle.put("vehicle_id",vehicle_id);
        currentVehicleReference.document(String.valueOf(user_id)).set(currentVehicle).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.e(TAG, "Current vehicle changed successfully.");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Unable to change current vehicle.");
            }
        });
    }

    private void updateJobManagement(final int user_id, int vehicle_id,String conducted_on){
        Query query = jobManagementDetailsNewReference.whereEqualTo("vehicle_registration_no_id",vehicle_id).whereEqualTo("show_job_date",conducted_on)
                .whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        if (queryDocumentSnapshot.exists()) {
                            String doc_id = queryDocumentSnapshot.getId();
                            document_id.add(doc_id);
                            Log.d(TAG,"Doc ID: " +doc_id);
                        }
                    }
                    for ( int i =0; i<document_id.size();i++) {
                        Map<String, Object> userJob = new HashMap<>();
                        userJob.put("assign_driver_id", user_id);
                        userJob.put("pending_task","Yes");
                        userJob.put("time_second_format",AppData.getDateMonthYearFormat());
                        jobManagementDetailsNewReference.document(document_id.get(i)).update(userJob);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Unable to update user id.");
            }
        });
    }

    private void getOpenTaskJobIds(){
        Query query = jobManagementDetailsNewReference.whereEqualTo("pending_task","Open").whereEqualTo("vehicle_registration_no_id",vehicle_no)
                .whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                        String doc_id = queryDocumentSnapshot.getId();
                        Map<String,Object> update_user = new HashMap<>();
                        update_user.put("assign_driver_id",logged_by);
                        jobManagementDetailsNewReference.document(doc_id).update(update_user);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Unable to get open task job ids.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toConfirmation(){
        isListenOfflineData = true;
        progressDialog.dismiss();
        removePreferences();
        fragment = new SuccessFragment();
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment, "SUCCESS");
        ft.addToBackStack("SUCCESS");
        ft.commit();
        tag = fragment.getTag();
        Log.d(TAG,"CHILD_TAG" + fragment.getTag());
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getChildTag(tag);
    }

    private void removePreferences(){
        editor.remove("vehicle_defect");
        editor.remove("trailer_defect");
        editor.remove("nfc1");
        editor.remove("nfc2");
        editor.remove("nfc3");
        editor.remove("nfc4");
        editor.remove("nfc5");
        editor.remove("nfc6");
        editor.remove("with_nfc");
        editor.apply();
        count = 0;
        mainActivity.getCount(count);
    }

    private void alertDialogFailed(String message){
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deleteDocument();
                        fragment = new com.crate.crateam.fragments.DashboardFragment();
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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (viewPager != null) {
                if (viewPager.getCurrentItem() == 3) {
                    mainActivity.getChildTag(TAG);
                    viewPager.setAllowedSwipeDirection(CustomViewPagerInspection.SwipeDirection.NONE);
                    with_nfc = pref.getString("with_nfc",null);
                    if (with_nfc.equals("Yes")) {
                        ll_completion_tag.setVisibility(View.VISIBLE);
                        setTag();
                    }else
                        ll_completion_tag.setVisibility(View.GONE);
                    if (vehicle_defect != null && trailer_defect != null && inspection_id != null) {
                        inspection_id= pref.getString("inspection_id",null);
                        vehicle_defect = pref.getString("vehicle_defect", null);
                        trailer_defect = pref.getString("trailer_defect", null);
                        Log.d(TAG,"VEHICLE_TRAILER_DEFECT_SUBMISSION:" + vehicle_defect + ", " + trailer_defect);
                    }
                }
            }
        }
    }

    // sort key table
    private void getMaximumLength(){
        sortKeyTableReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if (queryDocumentSnapshot.contains("inspection_submission_key")){
                                doc_id_length = queryDocumentSnapshot.getLong("inspection_submission_key").intValue();
                                doc_id = queryDocumentSnapshot.getId();
//                                break;
                            }
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getVehicleDefects() {
        Query query = vehicleElementsReference.whereEqualTo("status","Active").whereEqualTo("car_type","Vehicle").orderBy("id");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    defectsListVehicle.clear();
                    if(!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            Log.d(TAG,"Vehicle Elements:" + queryDocumentSnapshot.getString("element"));
                            defectsListVehicle.add(queryDocumentSnapshot.getString("element"));
                            defectsListIdVehicle.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                        }
                        getVehicleDefectElement(inspection_id);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTrailerDefects(){
        Query query = vehicleElementsReference.whereEqualTo("status","Active").whereEqualTo("car_type","Trailer").orderBy("id");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    defectsListTrailer.clear();
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            defectsListTrailer.add(queryDocumentSnapshot.getString("element"));
                            defectsListIdTrailer.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                        }
                        getTrailerDefectElement(inspection_id);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDefectImages(String inspection_id){
        Query query = vehicleDefectsReference.whereEqualTo("inspection_id",inspection_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        defectsImagesList.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            String defect_image1 = queryDocumentSnapshot.getString("defect_image1");
                            String defect_image2 = queryDocumentSnapshot.getString("defect_image2");
                            if (!defect_image1.isEmpty())
                                defectsImagesList.add(decodeImageString(defect_image1));
                            if (!defect_image2.isEmpty())
                                defectsImagesList.add(decodeImageString(defect_image2));
                        }
                        Log.d(TAG,"defectsImagesList :"+String.valueOf(defectsImagesList));
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // vehicle defect element and work shop manager comment for layout two
    private void getVehicleDefectElement(String inspection_id){
        vehicleDefectInspectionsComment = db.collection("CR_vehicle_defect_inspections");
        Query query = vehicleDefectInspectionsComment.whereEqualTo("inspection_id",inspection_id).whereEqualTo("element_type","vehicle");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        elementDefectListVehicle.clear();
                        wmCommentListVehicle.clear();
                        elementIdListVehicle.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            String element_defect = queryDocumentSnapshot.getString("element_defect");
                            String wm_comment = queryDocumentSnapshot.getString("workshop_manager_comment");
                            if (!element_defect.equals("No")) {
                                elementDefectListVehicle.add(element_defect);
                                wmCommentListVehicle.add(wm_comment);
                            }
                            int element_id = Objects.requireNonNull(queryDocumentSnapshot.getLong("element_id")).intValue();
                            if (element_id!=0)
                                elementIdListVehicle.add(element_id);
                        }
                        failed_element_vehicle = elementDefectListVehicle.size();
                        // store position
                        for (int i=0;i<elementIdListVehicle.size();i++){
                            for (int j=0;j<defectsListIdVehicle.size();j++){
                                if (elementIdListVehicle.get(i) == defectsListIdVehicle.get(j)){
                                    storePositionVehicle.add(j);
                                    storePositionVehicleId.add(defectsListIdVehicle.get(j));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // vehicle defect element and work shop manager comment for layout two
    private void getTrailerDefectElement(String inspection_id){
        vehicleDefectInspectionsComment = db.collection("CR_vehicle_defect_inspections");
        Query query = vehicleDefectInspectionsComment.whereEqualTo("inspection_id",inspection_id).whereEqualTo("element_type","trailer");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        elementDefectListTrailer.clear();
                        wmCommentListTrailer.clear();
                        elementIdListTrailer.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            String element_defect = queryDocumentSnapshot.getString("element_defect");
                            String wm_comment = queryDocumentSnapshot.getString("workshop_manager_comment");
                            if (!element_defect.equals("No")) {
                                elementDefectListTrailer.add(element_defect);
                                wmCommentListTrailer.add(wm_comment);
                            }
                            int element_id = Objects.requireNonNull(queryDocumentSnapshot.getLong("element_id")).intValue();
                            if (element_id!=0)
                                elementIdListTrailer.add(element_id);
                        }
                        failed_element_trailer = elementDefectListTrailer.size();
                        // store position
                        for (int i=0;i<elementIdListTrailer.size();i++){
                            for (int j=0;j<defectsListIdTrailer.size();j++){
                                if (elementIdListTrailer.get(i) == defectsListIdTrailer.get(j)){
                                    storePositionTrailer.add(j);
                                    storePositionTrailerId.add(defectsListIdTrailer.get(j));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Bitmap decodeImageString(String imageString){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] imageBytes = baos.toByteArray();
        imageBytes = Base64.decode(imageString, Base64.DEFAULT);
        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        return decodedImage;
    }

    public void requestPermission() {
        ActivityCompat.requestPermissions((Activity) getActivity(), new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, 200);
    }
}
