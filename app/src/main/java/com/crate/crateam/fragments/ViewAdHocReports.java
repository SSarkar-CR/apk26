package com.crate.crateam.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import com.crate.crateam.R;
import com.crate.crateam.activities.MainActivity;
import com.crate.crateam.adapters.ViewAdHocDefectsAdapter;
import com.crate.crateam.model.ViewAdHocDefects;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.SessionManager;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class ViewAdHocReports extends Fragment implements View.OnClickListener {
    private Fragment fragment;
    private ProgressDialog progressDialog;
    private TextView tv_report_id,tv_date_time,tv_driver_name,tv_nfc_tag,tv_vehicle_no,tv_trailer,tv_manufacturer,tv_model,tv_clear;
    private EditText et_resolution;
    private CheckBox cb_close_issue;
    private ImageView iv_driver_sign;
    private Button bt_add_signature,bt_submit_resolution;
    private SignaturePad signature_manager;
    private LinearLayout ll_manager_sign;
    private String inspection_id,element_name,element_type,close_issue,tag,wm_sign,user_name,registration_no,driver_name="",trailer="",
            manufacturer ="",model="",all_closed,doc_id="",doc_id_adHoc ="",submission_time="",conducted_on="";
    private SharedPreferences preferences;
    private int role_id=0,doc_count = 0,element_id,driver_id,vehicle_id,trailer_id=0,doc_id_length=0,logged_by=0,doc_id_length_adHoc = 0;
    private Boolean CheckEditTextEmpty ;
    private boolean isSigned = false,isResolution = false,isNotClosed = false,isListenOfflineData = false;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference adHocReportReference,vehicleDefectAdHocReference,adHocResolutionReference,sortKeyTableReference;
    private RecyclerView rv_ad_hoc_defects;
    private ViewAdHocDefectsAdapter adapter;
    private ArrayList<String> elementName = new ArrayList<>();
    private ArrayList<Integer> elementId = new ArrayList<>();
    private ArrayList<String> elementType = new ArrayList<>();
    private ArrayList<String> wmResolution = new ArrayList<>();
    private ArrayList<String> closeIssues = new ArrayList<>();
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_ad_hoc_report, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view){
        FirestoreManager.initPersistentIndexManager();
        adHocReportReference = db.collection("CR_ad_hoc_submission");
        vehicleDefectAdHocReference = db.collection("CR_vehicle_defect_adHoc");
        adHocResolutionReference = db.collection("CR_vehicle_resolution_adHoc");
        sortKeyTableReference = db.collection("CR_sort_key");
        preferences = getActivity().getSharedPreferences("Preference", 0); // 0 - for private mode
        progressDialog = Dialog.showProgressDialog(getActivity());
        signature_manager = view.findViewById(R.id.signature_manager);
        tv_report_id = view.findViewById(R.id.tv_report_id);
        tv_date_time = view.findViewById(R.id.tv_date_time);
        tv_driver_name = view.findViewById(R.id.tv_driver_name);
        tv_vehicle_no = view.findViewById(R.id.tv_vehicle_no);
        tv_trailer = view.findViewById(R.id.tv_trailer);
        tv_manufacturer = view.findViewById(R.id.tv_manufacturer);
        tv_model = view.findViewById(R.id.tv_model);
        iv_driver_sign = view.findViewById(R.id.iv_driver_sign);
        tv_nfc_tag = view.findViewById(R.id.tv_nfc_tag);
        tv_clear = view.findViewById(R.id.tv_clear);
        rv_ad_hoc_defects = view.findViewById(R.id.rv_ad_hoc_defects);
        bt_add_signature = view.findViewById(R.id.bt_add_signature);
        bt_submit_resolution = view.findViewById(R.id.bt_submit_resolution);
        ll_manager_sign = view.findViewById(R.id.ll_manager_sign);
        inspection_id = preferences.getString("view_inspection_id_adHoc",null);
        driver_id = preferences.getInt("driver_id_adHoc", 0);
        registration_no = preferences.getString("registration_id_adHoc",null);
        SessionManager sessionManager = new SessionManager(getActivity());
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_name = user.get(SessionManager.KEY_FULL_NAME);
        String user_role = user.get(SessionManager.KEY_ROLE_ONE);
        logged_by = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        role_id = Integer.parseInt(user_role);
        Log.d(TAG,"InAdHocReportDetails :"+inspection_id +" "+driver_id);
        progressDialog.show();
        loadAdHocReportDetails();
        if (role_id == 3){
            bt_add_signature.setVisibility(View.VISIBLE);
        }
        setAdapter();
        getAdHocDefectsData();
        setSignaturePad();
        initializeOnClick();
        getMaximumLength();
        getMaximumLengthAdHoc();
    }



    private void  loadAdHocReportDetails(){
        Query query1 = adHocReportReference.whereEqualTo("inspection_id",inspection_id).whereEqualTo("user_role",4);
        query1.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    progressDialog.dismiss();
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        String inspection_id = documentSnapshot.getString("inspection_id");
                        vehicle_id = Objects.requireNonNull(documentSnapshot.getLong("vehicle_id")).intValue();
                        trailer_id = Objects.requireNonNull(documentSnapshot.getLong("trailer_id")).intValue();
                        registration_no = documentSnapshot.getString("registration_no");
                        String driver_sign = documentSnapshot.getString("z_driver_sign");
                        String conducted_on = documentSnapshot.getString("conducted_on");
                        String submission_time = documentSnapshot.getString("submission_time");
                        driver_name = documentSnapshot.getString("driver_name");
                        String nfc_tag= documentSnapshot.getString("scanned_tag");
                        trailer = documentSnapshot.getString("trailer");
                        manufacturer = documentSnapshot.getString("manufacturer");
                        model = documentSnapshot.getString("model");
                        tv_report_id.setText(inspection_id);
                        tv_date_time.setText(conducted_on + " , "+submission_time);
                        tv_driver_name.setText(driver_name);
                        tv_vehicle_no.setText(registration_no);
                        tv_nfc_tag.setText(nfc_tag);
                        tv_trailer.setText(trailer);
                        tv_manufacturer.setText(manufacturer);
                        tv_model.setText(model);
                        Bitmap decodedImage = decodeImageString(driver_sign);
                        iv_driver_sign.setImageBitmap(decodedImage);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Dialog.alertDialog(getActivity(),"Unable to get vehicle inspection details.");
            }
        });
    }

    private void setAdapter(){
        Query getDefectsQuery = vehicleDefectAdHocReference.whereEqualTo("inspection_id",inspection_id).whereEqualTo("close_issue","No");
        FirestoreRecyclerOptions<ViewAdHocDefects> options = new FirestoreRecyclerOptions.Builder<ViewAdHocDefects>()
                .setQuery(getDefectsQuery, ViewAdHocDefects.class)
                .build();
        adapter = new ViewAdHocDefectsAdapter(options,getActivity());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv_ad_hoc_defects.setLayoutManager(layoutManager);
        rv_ad_hoc_defects.setAdapter(adapter);
        getDefectsQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        if (documentSnapshot.exists()) {
                            adapter.setOnItemClickListener(new ViewAdHocDefectsAdapter.OnImageClickListener() {
                                @Override
                                public void onImageClick(final int position) {
                                    final ImageView image_one = rv_ad_hoc_defects.getChildAt(position).findViewById(R.id.iv_photo_one);
                                    final ImageView image_two = rv_ad_hoc_defects.getChildAt(position).findViewById(R.id.iv_photo_two);
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
                Log.d(TAG,"LOAD_ERROR: " +e.getMessage());
                Dialog.alertDialog(getActivity(),"Unable to fetch results.");
            }
        });
    }

    private void initializeOnClick(){
        bt_add_signature.setOnClickListener(this);
        bt_submit_resolution.setOnClickListener(this);
        tv_clear.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_add_signature:
                ll_manager_sign.setVisibility(View.VISIBLE);
                bt_add_signature.setVisibility(View.GONE);
                break;
            case R.id.bt_submit_resolution:
                progressDialog.show();
                if (!isSigned) {
                    Dialog.alertDialog(getActivity(),"Please sign the form.");
                    progressDialog.dismiss();
                }else {
                    submitDefectsResolution();
                    if (!AppData.internetOnline(getActivity())){
                        toSuccessFragment();
                    }
                }
                break;
            case R.id.tv_clear:
                signature_manager.clear();
                break;
        }
    }

    private void submitDefectsResolution(){
        Bitmap signatureBitmap = signature_manager.getSignatureBitmap();
        wm_sign = convertTOBase64Image(signatureBitmap);
        if (adapter != null) {
            wmResolution.clear();
            closeIssues.clear();
            for (int i = 0; i < adapter.getItemCount(); i++) {
                View view1 = rv_ad_hoc_defects.getChildAt(i);
                if (view1 != null) {
                    et_resolution = view1.findViewById(R.id.et_resolution);
                    cb_close_issue = view1.findViewById(R.id.cb_close_issue);
                }
                et_resolution.requestFocus();
                String resolutions = et_resolution.getText().toString();
                wmResolution.add(resolutions);
                boolean isChecked = cb_close_issue.isChecked();
                if (isChecked)
                    close_issue = "Yes";
                else {
                    close_issue = "No";
                }
                closeIssues.add(close_issue);
                CheckEditTextIsEmptyOrNot(resolutions);
                if (CheckEditTextEmpty) {
                    isResolution = true;
                }else
                    isResolution = false;
            }

            if (!isResolution){
                progressDialog.dismiss();
                Dialog.alertDialog(getActivity(), "Please enter resolutions.");
            }else {
                Log.d(TAG,"RESOLUTIONS :" +wmResolution+" "+closeIssues+" "+elementName);
                for (int i=0; i<elementName.size();i++) {
                    element_id = elementId.get(i);
                    element_name = elementName.get(i);
                    element_type = elementType.get(i);
                    if (closeIssues.get(i).equals("No"))
                        all_closed = "No";
                    else
                        all_closed = "Yes";
                    Log.d(TAG,"Ad-Hoc data set:" + inspection_id + " " + element_id + " " + element_name + " " + element_type + " "
                            + wmResolution.get(i) + " " + closeIssues.get(i) + " " + vehicle_id + " " + driver_id);
                    wmResolutionData(inspection_id, element_id, element_name, element_type, wmResolution.get(i), closeIssues.get(i), vehicle_id, driver_id,
                            registration_no, FieldValue.serverTimestamp(), FieldValue.serverTimestamp(), user_name);
                }
                insertAdHocSubmission();
            }
        }
    }

    private void insertAdHocSubmission(){
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
        Log.d(TAG,"trailer_id"+ trailer_id);
        Map<String,Object> inspectionData = new HashMap<>();
        inspectionData.put("inspection_id",inspection_id);
        inspectionData.put("vehicle_id",vehicle_id);
        inspectionData.put("driver_name",driver_name);
        inspectionData.put("logged_by",logged_by);
        inspectionData.put("trailer",trailer);
        inspectionData.put("trailer_id",trailer_id);
        Log.d(TAG,"trailer_id"+trailer_id);
        inspectionData.put("close_issue",all_closed);
        inspectionData.put("user_role",role_id);
        inspectionData.put("conducted_on",conducted_on);
        inspectionData.put("submission_time",submission_time);
        inspectionData.put("manufacturer",manufacturer);
        inspectionData.put("z_driver_sign","");
        inspectionData.put("model",model);
        inspectionData.put("scanned_tag","");
        inspectionData.put("latitude","");
        inspectionData.put("longitude","");
        inspectionData.put("address","");
        inspectionData.put("created_at", FieldValue.serverTimestamp());
        inspectionData.put("registration_no",registration_no);
        inspectionData.put("id",inspection_id+"_WM");
        inspectionData.put("status","Active");
        inspectionData.put("z_wm_sign",wm_sign);
        inspectionData.put("wm_sign_available","Yes");
        doc_id_length_adHoc = doc_id_length_adHoc+1;
        inspectionData.put("sort_key",doc_id_length_adHoc);
        inspectionData.put("time_second_format",AppData.getTimeSecond());
        inspectionData.put("is_updated","Yes");
        adHocReportReference.document(inspection_id+"_WM").set(inspectionData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                Map<String,Object> objectMap_update = new HashMap<>();
                objectMap_update.put("ad_hoc_submission_key",doc_id_length_adHoc);
                sortKeyTableReference.document(doc_id_adHoc).update(objectMap_update);
                Log.d(TAG,"Message"+"Ad-Hoc report submitted successfully");
                if (!isListenOfflineData)
                    toSuccessFragment();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                alertDialogFailed("Unable to submit Ad Hoc report.");
                Log.e("Message", "Unable to submit Ad Hoc report.");
            }
        });
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
    }

    private void CheckEditTextIsEmptyOrNot(String resolutions){
        if(TextUtils.isEmpty(resolutions))
            CheckEditTextEmpty = false ;
        else
            CheckEditTextEmpty = true ;
    }


    private void getAdHocDefectsData(){
        Query adHocData = vehicleDefectAdHocReference.whereEqualTo("inspection_id",inspection_id).whereEqualTo("close_issue","No");
        adHocData.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        int element_id = Objects.requireNonNull(queryDocumentSnapshot.getLong("element_id")).intValue();
                        String element_name = queryDocumentSnapshot.getString("element_name");
                        String element_type = queryDocumentSnapshot.getString("element_type");
                        elementId.add(element_id);
                        elementName.add(element_name);
                        elementType.add(element_type);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Message", "Unable to get ad-hoc data.");
            }
        });
    }

    private void wmResolutionData(final String inspection_id, final int element_id, final String element_name, final String element_type, final String wm_resolution,
                                  final String close_issue, int vehicle_id, int driver_id,String registration_no, FieldValue created_at, final FieldValue updated_at, final String user_name){
        progressDialog.dismiss();
        final Map<String,Object> adHocResolutionData = new HashMap<>();
        adHocResolutionData.put("inspection_id",inspection_id);
        adHocResolutionData.put("id",inspection_id+"_"+element_id+"_"+element_type);
        adHocResolutionData.put("element_id",element_id);
        adHocResolutionData.put("element_name",element_name);
        adHocResolutionData.put("element_type",element_type);
        adHocResolutionData.put("workshop_manager_comment",wm_resolution);
        adHocResolutionData.put("close_issue",close_issue);
        adHocResolutionData.put("vehicle_id",vehicle_id);
        adHocResolutionData.put("driver_id",driver_id);
        adHocResolutionData.put("registration_no",registration_no);
        adHocResolutionData.put("created_at",created_at);
        adHocResolutionData.put("updated_at",updated_at);
        adHocResolutionData.put("wm_name",user_name);
        doc_id_length = doc_id_length+1;
        adHocResolutionData.put("sort_key",doc_id_length);
        adHocResolutionData.put("is_updated","Yes");
        Query resolutionQuery = adHocResolutionReference.whereEqualTo("inspection_id",inspection_id).whereEqualTo("element_id",element_id).whereEqualTo("element_type",element_type);
        resolutionQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().isEmpty()) {
                        adHocResolutionReference.document(inspection_id+"_"+element_id+"_"+element_type).set(adHocResolutionData);
                        updateAdHocDefectsTable(inspection_id,element_id,element_type,wm_resolution,close_issue);
                        updateAdHocSubmissionTable(inspection_id,close_issue);
                        Map<String,Object> objectMap_update = new HashMap<>();
                        objectMap_update.put("ad_hoc_resolution_key",doc_id_length);
                        sortKeyTableReference.document(doc_id).update(objectMap_update);
                    }else {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            if (documentSnapshot.exists()) {
                                String doc_id = documentSnapshot.getId();
                                Log.d(TAG,"CLOSE_ISSUE :" + wm_resolution + " " + close_issue);
                                Map<String, Object> update = new HashMap<>();
                                update.put("workshop_manager_comment", wm_resolution);
                                update.put("close_issue", close_issue);
                                update.put("wm_name",user_name);
                                update.put("is_updated","Yes");
                                update.put("updated_at", FieldValue.serverTimestamp());
                                adHocResolutionReference.document(doc_id).update(update);
                                updateAdHocDefectsTable(inspection_id,element_id,element_type,wm_resolution,close_issue);
                                updateAdHocSubmissionTable(inspection_id,close_issue);
                            }
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                alertDialogFailed("Unable to submit resolutions.");
                Log.e("Message", "Unable to submit Ad Hoc resolutions.");
            }
        });
    }

    private void updateAdHocSubmissionTable(String inspection_id, final String close_issue){
        Query query = adHocReportReference.whereEqualTo("inspection_id",inspection_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        if (documentSnapshot.exists()) {
                            String doc_id = documentSnapshot.getId();
                            Log.d(TAG,"DOC :" + doc_id);
                            Map<String, Object> updateAdHocResolutionTable = new HashMap<>();
                            updateAdHocResolutionTable.put("close_issue", all_closed);
                            updateAdHocResolutionTable.put("is_updated","Yes");
                            adHocReportReference.document(doc_id).update(updateAdHocResolutionTable);
                        }
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

    private void updateAdHocDefectsTable(String inspection_id,int element_id,String element_type,final String wm_resolution, final String close_issue){
        Query updateWmComments = vehicleDefectAdHocReference.whereEqualTo("inspection_id",inspection_id).whereEqualTo("element_id",element_id).whereEqualTo("element_type",element_type);
        updateWmComments.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        if (documentSnapshot.exists()) {
                            String doc_id = documentSnapshot.getId();
                            Log.d(TAG,"DOC :" + doc_id);
                            Map<String, Object> updateAdHocDefectsTable = new HashMap<>();
                            updateAdHocDefectsTable.put("workshop_manager_comment", wm_resolution);
                            updateAdHocDefectsTable.put("is_updated","Yes");
                            updateAdHocDefectsTable.put("close_issue", close_issue);
                            vehicleDefectAdHocReference.document(doc_id).update(updateAdHocDefectsTable);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Unable to update wm comments.");
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

    private String convertTOBase64Image(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return encoded;
    }

    private Bitmap decodeImageString(String imageString){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] imageBytes = baos.toByteArray();
        imageBytes = Base64.decode(imageString, Base64.DEFAULT);
        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        return decodedImage;
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
                    }
                });
        alertDialog.show();
    }
    // sort key table
    private void getMaximumLength(){
        sortKeyTableReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if (queryDocumentSnapshot.contains("ad_hoc_resolution_key")){
                                doc_id_length = queryDocumentSnapshot.getLong("ad_hoc_resolution_key").intValue();
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
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // sort key table
    private void getMaximumLengthAdHoc(){
        sortKeyTableReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if (queryDocumentSnapshot.contains("ad_hoc_submission_key")){
                                doc_id_length_adHoc = queryDocumentSnapshot.getLong("ad_hoc_submission_key").intValue();
                                doc_id_adHoc = queryDocumentSnapshot.getId();
                                break;
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
}
