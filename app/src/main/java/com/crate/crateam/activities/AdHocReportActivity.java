package com.crate.crateam.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crate.crateam.R;
import com.crate.crateam.adapters.AdHocDefectListAdapter;
import com.crate.crateam.adapters.AdHocDefectListAdapterTrailer;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.CustomSearchableSpinner;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.SessionManager;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.crate.crateam.utility.FirestoreManager; import com.google.firebase.firestore.PersistentCacheIndexManager;
import com.crate.crateam.utility.FirestoreManager;import com.crate.crateam.utility.FirestoreManager;   
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AdHocReportActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String MIME_TEXT_PLAIN = "text/plain";

    public static final String TAG = "AdHoc";
    private SignaturePad signaturePad;
    private NestedScrollView sv_ad_hoc_report;
    private EditText et_defect_details;
    private CustomSearchableSpinner sp_vehicle_defect,sp_trailer_defect;
    private Button bt_submit_report,bt_add_signature;
    private RecyclerView rv_vehicle_defects_adHoc,rv_trailer_defects_adHoc;
    private TextView tv_clear,tv_nfc_tag,tv_user_name,tv_vehicle_no,tv_trailer,tv_manufacturer,tv_model;
    private ImageView iv_cross,iv_nfcTag,iv_one,iv_two,iv_close_one,iv_close_two,imageView1,imageView2;
    private NfcAdapter mNfcAdapter;
    private AdHocDefectListAdapter adHocDefectListAdapterVehicle;
    private AdHocDefectListAdapterTrailer adHocDefectListAdapterTrailer;
    private PopupWindow popupWindow;
    private LinearLayout ll_nfc_tag,ll_vehicle_defects,ll_trailer_defects,ll_driver_signature;
    private String tagId,inspection_id,user_name,manufacturer,model,registration_no,trailer,
            defect_image1 = "",defect_image2 = "",latitude,longitude,address,conducted_on,submission_time,driver_sign,
            elementDefect,selectedVehicleDefect,selectedTrailerDefect,doc_id="";
    private int trailer_id = 0,vehicle_element_id,trailer_element_id,doc_count = 0,id_count=0,logged_by,vehicle_id,user_role,doc_id_length=0;
    private Bitmap getDrawable1, getDrawable2;
    private List<String> vehicleElements = new ArrayList<>();
    private List<Integer> vehicleElementsId = new ArrayList<>();
    private List<String> trailerElements = new ArrayList<>();
    private List<Integer> trailerElementsId = new ArrayList<>();
    private ProgressDialog progressDialog;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference inspectionSubmissionReference,adHocSubmissionReference,
            vehicleDefectAdHocReference,vehicleDetailsReference,sortKeyTableReference,vehicleElementsReference;
    private Boolean CheckEditTextEmpty ;
    private boolean isSigned = false, isVehicleDefectExist = false,isTrailerDefectExist = false,hasImage = false,hasImageTwo = false,isTagExist = false;
    private ArrayList<String> selectedVehicleElementDefectList = new ArrayList<>();
    private ArrayList<String> selectedTrailerElementDefectList = new ArrayList<>();
    private ArrayList<String> vehicleElementDefectList = new ArrayList<>();
    private ArrayList<String> trailerElementDefectList = new ArrayList<>();
    private ArrayList<Integer> arrayList_vehicle_id = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ad_hoc_layout);
        initView();
    }

    private void initView(){
        FirestoreManager.initPersistentIndexManager();
        adHocSubmissionReference = db.collection("CR_ad_hoc_submission");
        vehicleDefectAdHocReference = db.collection("CR_vehicle_defect_adHoc");
        vehicleDetailsReference = db.collection("CR_vehicle_details");
        vehicleElementsReference = db.collection("CR_vehicle_elements");
        sortKeyTableReference = db.collection("CR_sort_key");
        inspectionSubmissionReference = db.collection("CR_inspection_submission");

        Date c = Calendar.getInstance().getTime();
        Log.d("Current time => " , String.valueOf(c));
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = df.format(c);
        conducted_on = formattedDate ;

        SessionManager sessionManager = new SessionManager(this);
        progressDialog = Dialog.showProgressDialog(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_name = user.get(SessionManager.KEY_FULL_NAME);
        logged_by = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        user_role = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ROLE_ONE)));
        Log.d("USER_ID : " ,user_name+" "+logged_by);
        signaturePad = findViewById(R.id.signature_pad);
        sv_ad_hoc_report = findViewById(R.id.sv_ad_hoc_report);
        bt_submit_report = findViewById(R.id.bt_submit_report);
        bt_add_signature = findViewById(R.id.bt_add_signature);
        sp_vehicle_defect = findViewById(R.id.sp_vehicle_defect);
        sp_trailer_defect = findViewById(R.id.sp_trailer_defect);
        tv_clear = findViewById(R.id.tv_clear);
        tv_user_name = findViewById(R.id.tv_user_name);
        tv_vehicle_no = findViewById(R.id.tv_vehicle_no);
        tv_trailer = findViewById(R.id.tv_trailer);
        tv_manufacturer = findViewById(R.id.tv_manufacturer);
        tv_model = findViewById(R.id.tv_model);
        tv_nfc_tag = findViewById(R.id.tv_nfc_tag);
        iv_cross = findViewById(R.id.iv_cross);
        iv_nfcTag = findViewById(R.id.iv_nfcTag);
        ll_nfc_tag = findViewById(R.id.ll_nfc_tag);
        ll_vehicle_defects = findViewById(R.id.ll_vehicle_defects);
        ll_trailer_defects = findViewById(R.id.ll_trailer_defects);
        ll_driver_signature = findViewById(R.id.ll_driver_signature);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        rv_vehicle_defects_adHoc = findViewById(R.id.rv_vehicle_defects_adHoc);
        RecyclerView.LayoutManager layoutManager_vehicle= new LinearLayoutManager(this);
        rv_vehicle_defects_adHoc.setLayoutManager(layoutManager_vehicle);
        rv_trailer_defects_adHoc = findViewById(R.id.rv_trailer_defects_adHoc);
        RecyclerView.LayoutManager layoutManager_trailer = new LinearLayoutManager(this);
        rv_trailer_defects_adHoc.setLayoutManager(layoutManager_trailer);
        createInspectionId();
        setSignaturePad();
        initializeOnClick();
        checkNfcAdapter();
        setSpinnerData();
        getMaximumLength();
    }

    private void createInspectionId(){
        @SuppressLint("HardwareIds") String uniqueID = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        String currentDateAndTime = sdf.format(new Date());
        Log.d("UUID :" ,uniqueID+currentDateAndTime);
        String inspection_id_first = uniqueID.substring(0,4);
        String inspection_id_last = currentDateAndTime;
        inspection_id =  inspection_id_first+inspection_id_last;
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
                        sv_ad_hoc_report.requestDisallowInterceptTouchEvent(true);
                        return false;
                    case MotionEvent.ACTION_UP:
                        // Allow scroll View to intercept the touch event
                        sv_ad_hoc_report.requestDisallowInterceptTouchEvent(false);
                        return true;
                    default:
                        return true;
                }
            }
        });
    }

    private void initializeOnClick(){
        tv_clear.setOnClickListener(this);
        iv_cross.setOnClickListener(this);
        iv_nfcTag.setOnClickListener(this);
        bt_submit_report.setOnClickListener(this);
        bt_add_signature.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_clear:
                signaturePad.clear();
                Toast.makeText(this,"Sign Cleared",Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_cross:
                alertDialogFailed("Do you want to cancel inspection ?");
                break;
            case R.id.iv_nfcTag:
                Dialog.readyToScanDialog(this);
                break;
            case R.id.bt_add_signature:
                ll_driver_signature.setVisibility(View.VISIBLE);
                bt_add_signature.setVisibility(View.GONE);
                break;
            case R.id.bt_submit_report:
                if (TextUtils.isEmpty(tagId))
                    Dialog.alertDialog(this,"Please scan any NFC tag placed on the vehicle.");
                else if (!isSigned)
                    Dialog.alertDialog(this,"Please sign the form.");
                else {
                    progressDialog.show();
                    sendVehicleData();
                    sendTrailerData();
                    sendAdHocData();
                    if (!AppData.internetOnline(this)) {
                        if (isVehicleDefectExist || isTrailerDefectExist) {
                            progressDialog.dismiss();
                            toConfirmation("Ad-Hoc report submitted successfully");
                        }
                    }
                }
                break;
        }
    }

    private  void checkNfcAdapter(){
        if (mNfcAdapter == null) {
            alertDialog("This device doesn't support NFC.");
        }
        if (mNfcAdapter != null) {
            if (!mNfcAdapter.isEnabled())
                alertDialog("NFC is disabled.Please enable NFC from your mobile settings.");
            else
                Toast.makeText(this, "NFC is Enabled.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        stopForegroundDispatch(this, mNfcAdapter);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }
    private void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, PendingIntent.FLAG_MUTABLE);
        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{ new String[] { Ndef.class.getName() }};
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        if (adapter != null)
            adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    private void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        if (adapter != null)
            adapter.disableForegroundDispatch(activity);
    }
    //     get new intent ...
    private void handleIntent(Intent intent)
    {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) ||NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                tagId = ByteArrayToHexString(Objects.requireNonNull(tag).getId());
                Log.d("NFC_SERIAL_NO :", tagId);
                ndefmessage(tag);
                Log.d("NDEF_Message", "Detected: " +ndefmessage(tag));
            } else {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                tagId = ByteArrayToHexString(Objects.requireNonNull(tag).getId());
                Log.d("NFC_SERIAL_NO :" , tagId);
                setTagValue();
            }
        }
    }

    private String ByteArrayToHexString(byte [] inarray) {
        int i, j, in;
        String [] hex = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
        String out= "";
        for(j = 0 ; j < inarray.length ; ++j)
        {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }
    //    // read tag data ...
    private String ndefmessage(Tag tag)
    {
        try {
            Ndef ndef = Ndef.get(tag);
            ndef.connect();
            NdefMessage ndefMessage = ndef.getNdefMessage();
            if (ndefMessage!= null) {
                setTagValue();
                ndef.close();
            }
            else
                scanFailedDialog();
        } catch (IOException | FormatException e) {
            Log.e(TAG, "An unexpected error occurred", e);
        }
        return null;
    }

    private void setTagValue(){
        isTagExist = false;
        if (tagId != null) {
            vehicleUserMapping();
        }
    }

    private void setSpinnerData(){
        ArrayAdapter<String> vehicleSpinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_custom_layout,getVehicleElementsList());
        vehicleSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        sp_vehicle_defect.setAdapter(vehicleSpinnerArrayAdapter);
        ArrayAdapter<String> trailerSpinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_custom_layout,getTrailerElementsList());
        trailerSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        sp_trailer_defect.setAdapter(trailerSpinnerArrayAdapter);
        sp_vehicle_defect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!selectedVehicleElementDefectList.isEmpty())
                   selectedVehicleElementDefectList.remove(0);
                selectedVehicleDefect = getVehicleElementsList().get(position);
                vehicle_element_id = vehicleElementsId.get(position);
                selectedVehicleElementDefectList.add(selectedVehicleDefect);
                adHocDefectListAdapterVehicle = new AdHocDefectListAdapter(selectedVehicleElementDefectList);
                rv_vehicle_defects_adHoc.setAdapter(adHocDefectListAdapterVehicle);
                onVehicleAdapterPositionClick();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        sp_trailer_defect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!selectedTrailerElementDefectList.isEmpty())
                    selectedTrailerElementDefectList.remove(0);
                selectedTrailerDefect = getTrailerElementsList().get(position);
                trailer_element_id = trailerElementsId.get(position);
                selectedTrailerElementDefectList.add(selectedTrailerDefect);
                adHocDefectListAdapterTrailer = new AdHocDefectListAdapterTrailer(selectedTrailerElementDefectList);
                rv_trailer_defects_adHoc.setAdapter(adHocDefectListAdapterTrailer);
                onTrailerAdapterPositionClick();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void storeAdHocData(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        submission_time = dateFormatter.format(calendar.getTime());
        Bitmap signatureBitmap = signaturePad.getSignatureBitmap();
        driver_sign = AppData.convertTOBase64Image(signatureBitmap);
        GPSTracker finder = new GPSTracker(this);
        Double lat= 0.0 , lon = 0.0 ;
        if (!finder.checkLocationPermission()){
            finder.requestPermission();
        }
        if (finder.canGetLocation()) {
            String close_issue = "";
            lat = finder.getLatitude();
            lon = finder.getLongitude();
            double latOnline = round(lat,4);
            double lonOnline = round(lon,4);
            latitude = String.valueOf(latOnline);
            longitude = String.valueOf(lonOnline);
            address = finder.getAddress(lat,lon);
            Log.d("LAT-LAN :" , address);
        } else
            finder.checkLocationPermission();
    }

    private void sendVehicleData(){
        if (adHocDefectListAdapterVehicle != null) {
            for (int i = 0; i < adHocDefectListAdapterVehicle.getItemCount(); i++) {
                View view1 = rv_vehicle_defects_adHoc.getChildAt(i);
                if (view1 != null) {
                    et_defect_details = view1.findViewById(R.id.et_defect_details);
                    imageView1 = view1.findViewById(R.id.iv_photo_one);
                    imageView2 = view1.findViewById(R.id.iv_photo_two);
                }
                elementDefect = et_defect_details.getText().toString();
                Bitmap image_one = ((BitmapDrawable) imageView1.getDrawable()).getBitmap();
                Bitmap image_two = ((BitmapDrawable) imageView2.getDrawable()).getBitmap();
                Bitmap placeholder = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_image);
                String placeholder_image = AppData.convertTOBase64Image(placeholder);
                defect_image1 = AppData.convertTOBase64Image(image_one);
                if (defect_image1.equals(placeholder_image))
                    defect_image1 = "";
                defect_image2 = AppData.convertTOBase64Image(image_two);
                if (defect_image2.equals(placeholder_image))
                    defect_image2 = "";
                CheckEditTextIsEmptyOrNot(elementDefect);
                if (CheckEditTextEmpty) {
                    vehicleElementDefectList.add(elementDefect);
                    Log.d("VEHICLE_ELEMENT_DEFECT :" ,vehicle_element_id + " " + inspection_id + " " + selectedVehicleDefect + " " + elementDefect + " " + defect_image1 + " " + defect_image2);
                    sendVehicleDefects(inspection_id, vehicle_element_id, selectedVehicleDefect, "vehicle", elementDefect, defect_image1, defect_image2,"No","");
                }
            }
            Log.e("Defects List Size :" , String.valueOf(vehicleElementDefectList.size()));
            isVehicleDefectExist = !vehicleElementDefectList.isEmpty();
        }
    }

    private void sendTrailerData(){
        if (adHocDefectListAdapterTrailer != null) {
            for (int i = 0; i < adHocDefectListAdapterTrailer.getItemCount(); i++) {
                View view1 = rv_trailer_defects_adHoc.getChildAt(i);
                if (view1 != null) {
                    et_defect_details = view1.findViewById(R.id.et_defect_details_trailer);
                    imageView1 = view1.findViewById(R.id.iv_photo_one_trailer);
                    imageView2 = view1.findViewById(R.id.iv_photo_two_trailer);
                }
                elementDefect = et_defect_details.getText().toString();
                Bitmap image_one = ((BitmapDrawable) imageView1.getDrawable()).getBitmap();
                Bitmap image_two = ((BitmapDrawable) imageView2.getDrawable()).getBitmap();
                Bitmap placeholder = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_image);
                String placeholder_image = AppData.convertTOBase64Image(placeholder);
                defect_image1 = AppData.convertTOBase64Image(image_one);
                if (defect_image1.equals(placeholder_image))
                    defect_image1 = "";
                defect_image2 = AppData.convertTOBase64Image(image_two);
                if (defect_image2.equals(placeholder_image))
                    defect_image2 = "";
                CheckEditTextIsEmptyOrNot(elementDefect);
                if (CheckEditTextEmpty) {
                    trailerElementDefectList.add(elementDefect);
                    Log.d("TRAILER_ELEMENT_DEFECT :" ,trailer_element_id + " " + inspection_id + " " + selectedTrailerDefect + " " + elementDefect + " " + defect_image1 + " " + defect_image2);
                    sendTrailerDefects(inspection_id, trailer_element_id, selectedTrailerDefect, "trailer", elementDefect, defect_image1, defect_image2,"No","");
                }
            }
            Log.d("Defects List Size :" , String.valueOf(trailerElementDefectList.size()));
            isTrailerDefectExist = !trailerElementDefectList.isEmpty();
        }
    }

    private void sendVehicleDefects(String inspection_id,int element_id,String element_name,String element_type,String defect_details,
                                    String defect_image1,String defect_image2,String close_issue,String workshop_manager_comment){
        Map<String,Object> vehicleDefectData = new HashMap<>();
        vehicleDefectData.put("inspection_id",inspection_id);
        vehicleDefectData.put("element_id",element_id);
        vehicleDefectData.put("element_name",element_name);
        vehicleDefectData.put("element_type",element_type);
        vehicleDefectData.put("element_defect",defect_details);
        vehicleDefectData.put("defect_image1",defect_image1);
        vehicleDefectData.put("defect_image2",defect_image2);
        vehicleDefectData.put("close_issue",close_issue);
        vehicleDefectData.put("workshop_manager_comment",workshop_manager_comment);
        vehicleDefectData.put("is_updated","Yes");
        vehicleDefectData.put("id",inspection_id+"_"+id_count+++"_Vehicle");
        vehicleDefectAdHocReference.document(inspection_id+"_"+doc_count+++"_Vehicle").set(vehicleDefectData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                Log.d("Message", "Ad-Hoc vehicle defects submitted successfully");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                alertDialogFailed("Unable to submit Ad Hoc vehicle defects.");
                Log.e("Message", "Unable to submit Ad Hoc vehicle defects.");
            }
        });
    }

    private void sendTrailerDefects(String inspection_id,int element_id,String element_name,String element_type,String defect_details
            ,String defect_image1,String defect_image2,String close_issue,String workshop_manager_comment){
        Map<String,Object> vehicleDefectData = new HashMap<>();
        vehicleDefectData.put("inspection_id",inspection_id);
        vehicleDefectData.put("element_id",element_id);
        vehicleDefectData.put("element_name",element_name);
        vehicleDefectData.put("element_type",element_type);
        vehicleDefectData.put("element_defect",defect_details);
        vehicleDefectData.put("defect_image1",defect_image1);
        vehicleDefectData.put("defect_image2",defect_image2);
        vehicleDefectData.put("close_issue",close_issue);
        vehicleDefectData.put("workshop_manager_comment",workshop_manager_comment);
        vehicleDefectData.put("is_updated","Yes");
        vehicleDefectData.put("id",inspection_id+"_"+id_count+++"_Trailer");
        vehicleDefectAdHocReference.document(inspection_id+"_"+doc_count+++"_Trailer").set(vehicleDefectData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                Log.d("Message", "Ad-Hoc trailer defects submitted successfully");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                alertDialogFailed("Unable to submit Ad Hoc trailer defects.");
                Log.e("Message", "Unable to submit Ad Hoc trailer defects.");
            }
        });
    }

    private void sendAdHocData(){
        storeAdHocData();
        Log.d("trailer_id", String.valueOf(trailer_id));
        Map<String,Object> inspectionData = new HashMap<>();
        inspectionData.put("inspection_id",inspection_id);
        inspectionData.put("vehicle_id",vehicle_id);
        inspectionData.put("driver_name",user_name);
        inspectionData.put("logged_by",logged_by);
        inspectionData.put("trailer",trailer);
        inspectionData.put("trailer_id",trailer_id);
        Log.d("trailer__id11", String.valueOf(trailer_id));
        inspectionData.put("close_issue","No");
        inspectionData.put("user_role",user_role);
        inspectionData.put("conducted_on",conducted_on);
        inspectionData.put("submission_time",submission_time);
        inspectionData.put("manufacturer",manufacturer);
        inspectionData.put("z_driver_sign",driver_sign);
        inspectionData.put("model",model);
        inspectionData.put("scanned_tag",tagId);
        inspectionData.put("latitude",latitude);
        inspectionData.put("longitude",longitude);
        inspectionData.put("address",address);
        inspectionData.put("created_at", FieldValue.serverTimestamp());
        inspectionData.put("registration_no",registration_no);
        inspectionData.put("id",inspection_id);
        inspectionData.put("status","Active");
        inspectionData.put("z_wm_sign","");
        inspectionData.put("wm_sign_available","No");
        doc_id_length = doc_id_length+1;
        inspectionData.put("sort_key",doc_id_length);
        inspectionData.put("time_second_format",AppData.getTimeSecond());
        inspectionData.put("is_updated","Yes");
        if (isVehicleDefectExist || isTrailerDefectExist) {
            adHocSubmissionReference.document( inspection_id).set(inspectionData).addOnSuccessListener(new OnSuccessListener<Void>() {
                @SuppressLint("DefaultLocale")
                @Override
                public void onSuccess(Void aVoid) {
                    progressDialog.dismiss();
                    toConfirmation("Ad-Hoc report submitted successfully");
                    Map<String,Object> objectMap_update = new HashMap<>();
                    objectMap_update.put("ad_hoc_submission_key",doc_id_length);
                    sortKeyTableReference.document(doc_id).update(objectMap_update);
                    Log.d("Message", "Ad-Hoc report submitted successfully");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    alertDialogFailed("Unable to submit Ad Hoc report.");
                    Log.e("Message", "Unable to submit Ad Hoc report.");
                }
            });
        }else {
            progressDialog.dismiss();
            Dialog.alertDialog(this, "Please enter defect details");
        }
    }

    private void onVehicleAdapterPositionClick(){
        if (adHocDefectListAdapterVehicle != null) {
            adHocDefectListAdapterVehicle.setOnItemClickListener(new AdHocDefectListAdapter.OnCameraClickListener() {
                @Override
                public void onCameraClick(final int position) {
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);
                    iv_one = rv_vehicle_defects_adHoc.getChildAt(position).findViewById(R.id.iv_photo_one);
                    iv_two = rv_vehicle_defects_adHoc.getChildAt(position).findViewById(R.id.iv_photo_two);
                    iv_close_one = rv_vehicle_defects_adHoc.getChildAt(position).findViewById(R.id.iv_close_one);
                    iv_close_two = rv_vehicle_defects_adHoc.getChildAt(position).findViewById(R.id.iv_close_two);
                    iv_close_one.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            iv_one.setImageDrawable(getResources().getDrawable(R.drawable.placeholder_image));
                            hasImage = false;
                        }
                    });
                    iv_close_two.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            iv_two.setImageDrawable(getResources().getDrawable(R.drawable.placeholder_image));
                            hasImage = true;
                            hasImageTwo = false;
                        }
                    });
                    iv_one.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            iv_one = rv_vehicle_defects_adHoc.getChildAt(position).findViewById(R.id.iv_photo_one);
                            getDrawable1 = ((BitmapDrawable) iv_one.getDrawable()).getBitmap();
                            zoomImagePopup(getDrawable1);
                        }
                    });
                    iv_two.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            iv_two = rv_vehicle_defects_adHoc.getChildAt(position).findViewById(R.id.iv_photo_two);
                            getDrawable2 = ((BitmapDrawable) iv_two.getDrawable()).getBitmap();
                            zoomImagePopup(getDrawable2);
                        }
                    });
                }

                @Override
                public void onExpandClick(int position) {
                    hasImage = false;
                    hasImageTwo = false;
                    et_defect_details = rv_vehicle_defects_adHoc.getChildAt(position).findViewById(R.id.et_defect_details);
                    et_defect_details.requestFocus();
                }

                @Override
                public void onCloseClick(int position) {
                    et_defect_details = rv_vehicle_defects_adHoc.getChildAt(position).findViewById(R.id.et_defect_details);
                    et_defect_details.setText("");
                }
            });
        }
    }

    private void onTrailerAdapterPositionClick(){
        if (adHocDefectListAdapterTrailer != null) {
            adHocDefectListAdapterTrailer.setOnItemClickListener(new AdHocDefectListAdapterTrailer.OnCameraClickListener() {
                @Override
                public void onCameraClick(final int position) {
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);
                    iv_one = rv_trailer_defects_adHoc.getChildAt(position).findViewById(R.id.iv_photo_one_trailer);
                    iv_two = rv_trailer_defects_adHoc.getChildAt(position).findViewById(R.id.iv_photo_two_trailer);
                    iv_close_one = rv_trailer_defects_adHoc.getChildAt(position).findViewById(R.id.iv_close_one_trailer);
                    iv_close_two = rv_trailer_defects_adHoc.getChildAt(position).findViewById(R.id.iv_close_two_trailer);
                    iv_close_one.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            iv_one.setImageDrawable(getResources().getDrawable(R.drawable.placeholder_image));
                            hasImage = false;
                        }
                    });
                    iv_close_two.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            iv_two.setImageDrawable(getResources().getDrawable(R.drawable.placeholder_image));
                            hasImage = true;
                            hasImageTwo = false;
                        }
                    });
                    iv_one.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            iv_one = rv_trailer_defects_adHoc.getChildAt(position).findViewById(R.id.iv_photo_one_trailer);
                            getDrawable1 = ((BitmapDrawable) iv_one.getDrawable()).getBitmap();
                            zoomImagePopup(getDrawable1);
                        }
                    });
                    iv_two.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            iv_two = rv_trailer_defects_adHoc.getChildAt(position).findViewById(R.id.iv_photo_two_trailer);
                            getDrawable2 = ((BitmapDrawable) iv_two.getDrawable()).getBitmap();
                            zoomImagePopup(getDrawable2);
                        }
                    });
                }

                @Override
                public void onExpandClick(int position) {
                    hasImage = false;
                    hasImageTwo = false;
                    et_defect_details = rv_trailer_defects_adHoc.getChildAt(position).findViewById(R.id.et_defect_details_trailer);
                    et_defect_details.requestFocus();
                }

                @Override
                public void onCloseClick(int position) {
                    et_defect_details = rv_trailer_defects_adHoc.getChildAt(position).findViewById(R.id.et_defect_details_trailer);
                    et_defect_details.setText("");
                }
            });
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if(resultCode == RESULT_OK){
            Bundle extras = imageReturnedIntent.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            if (imageBitmap != null) {
                if (!hasImage && !hasImageTwo) {
                    iv_one.setImageBitmap(imageBitmap);
                    defect_image1 = AppData.convertTOBase64Image(imageBitmap);
                    hasImage = true;
                } else if (hasImage && !hasImageTwo) {
                    iv_two.setImageBitmap(imageBitmap);
                    defect_image2 = AppData.convertTOBase64Image(imageBitmap);
                    hasImageTwo = false;
                    hasImage = false;
                }
            }
        }
    }

    private void CheckEditTextIsEmptyOrNot(String elementDefect){
        if(TextUtils.isEmpty(elementDefect))
            CheckEditTextEmpty = false ;
        else
            CheckEditTextEmpty = true ;
    }

    private List<String> getVehicleElementsList() {
        Query query = vehicleElementsReference.whereEqualTo("status","Active").whereEqualTo("car_type","Vehicle").orderBy("id");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    vehicleElements.clear();
                    vehicleElementsId.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        vehicleElements.add(queryDocumentSnapshot.getString("element"));
                        vehicleElementsId.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AdHocReportActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        return vehicleElements;
    }

    private List<String> getTrailerElementsList() {
        Query query = vehicleElementsReference.whereEqualTo("status","Active").whereEqualTo("car_type","Trailer").orderBy("id");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    trailerElements.clear();
                    trailerElementsId.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        trailerElements.add(queryDocumentSnapshot.getString("element"));
                        trailerElementsId.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AdHocReportActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        return trailerElements;
    }

    private void toConfirmation(String message){
        progressDialog.dismiss();
        final View popupView = getLayoutInflater().inflate(R.layout.change_pass_popup_layout,null,true);
        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,true);
        popupWindow.showAtLocation(popupView, Gravity.BOTTOM,0,0);
        Button cancelPopup = popupView.findViewById(R.id.bt_ok);
        TextView tv_change_pass = popupView.findViewById(R.id.tv_change_pass);
        tv_change_pass.setText(message);
        cancelPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                Intent toDashboard = new Intent(AdHocReportActivity.this,MainActivity.class);
                startActivity(toDashboard);
            }
        });
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
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

    private void scanFailedDialog() {
        final View popupView = getLayoutInflater().inflate(R.layout.scan_failed_layout,null,true);
        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,true);
        popupWindow.showAtLocation(popupView, Gravity.BOTTOM,0,0);
        Button bt_cancel_popup = popupView.findViewById(R.id.bt_cancel_popup);
        Button bt_retry = popupView.findViewById(R.id.bt_retry);
        bt_cancel_popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
        bt_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
    }

    private void alertDialog(String message){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void successfullyScanDialog() {
        final View popupView = getLayoutInflater().inflate(R.layout.scan_success_layout,null,true);
        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,true);
        popupWindow.showAtLocation(popupView, Gravity.BOTTOM,0,0);
        Button bt_done = popupView.findViewById(R.id.bt_done);
        bt_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
        Dialog.dialogTimer(popupWindow);
    }

    private void alertDialogFailed(String message){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {

    }
    public void vehicleUserMapping(){
        Query query = vehicleDetailsReference.whereEqualTo("external_vehicle","No").whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    String userIds = "";
                    arrayList_vehicle_id.clear();
                    ArrayList<Integer> arrayList_user_id = new ArrayList<>();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        userIds = queryDocumentSnapshot.getString("vehicle_user_mappings");
                        if (userIds != null && userIds.contains(",")) {
                            ArrayList<String> userIdListString = new ArrayList<String>(Arrays.asList(userIds.split(",")));
                            for (int i=0;i<userIdListString.size();i++) {
                                arrayList_user_id.add(Integer.valueOf(userIdListString.get(i)));
                                if (Integer.valueOf(userIdListString.get(i)) == logged_by) {
                                    arrayList_vehicle_id.add(queryDocumentSnapshot.getLong("id").intValue());
                                }
                            }
                        }else {
                            if (userIds != null) {
                                if (!userIds.equals("")) {
                                    arrayList_user_id.add(Integer.valueOf(userIds));
                                    if (Integer.valueOf(userIds) == logged_by) {
                                        arrayList_vehicle_id.add(queryDocumentSnapshot.getLong("id").intValue());
                                    }
                                }
                            }
                        }
                    }
                    Log.d("Vehicle id list :" , String.valueOf(arrayList_vehicle_id));
                    vehicleNFCS();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AdHocReportActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void vehicleNFCS(){
        progressDialog.show();
        Task task1 = vehicleDetailsReference.whereEqualTo("status","Active").whereEqualTo("external_vehicle","No").whereEqualTo("vehicle_completion",tagId).get();
        Task task2 = vehicleDetailsReference.whereEqualTo("status","Active").whereEqualTo("external_vehicle","No").whereEqualTo("vehicle_front",tagId).get();
        Task task3 = vehicleDetailsReference.whereEqualTo("status","Active").whereEqualTo("external_vehicle","No").whereEqualTo("vehicle_near_rear",tagId).get();
        Task task4 = vehicleDetailsReference.whereEqualTo("status","Active").whereEqualTo("external_vehicle","No").whereEqualTo("vehicle_off_rear",tagId).get();
        Task<List<QuerySnapshot>> allTask = Tasks.whenAllSuccess(task1,task2,task3,task4);
        allTask.addOnSuccessListener(new OnSuccessListener<List<QuerySnapshot>>() {
            @Override
            public void onSuccess(List<QuerySnapshot> querySnapshots) {
                progressDialog.dismiss();
                if (!querySnapshots.isEmpty()) {
                    for (QuerySnapshot querySnapshot_object : querySnapshots) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : querySnapshot_object) {
                            if (queryDocumentSnapshot.exists()) {
                                vehicle_id = queryDocumentSnapshot.getLong("id").intValue();
                                for (int i = 0;i<arrayList_vehicle_id.size();i++){
                                    if (vehicle_id == arrayList_vehicle_id.get(i)){
                                        isTagExist = true;
                                        Log.d("I am here :" ,"inside vehicle details");
                                        successfullyScanDialog();
                                        tv_nfc_tag.setText(tagId);
                                        tv_user_name.setText(user_name);
                                        ll_nfc_tag.setBackgroundColor(Color.parseColor("#00CC66"));
                                        vehicleDetails(vehicle_id);
                                        break;
                                    }
                                }

                            }
                        }
                    }
                    if (!isTagExist)
                        onWrongTagDetection();
                    else
                        inspectionSubmission();
                }
                else {
                    if (!isTagExist) {
                        progressDialog.dismiss();
                        onWrongTagDetection();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AdHocReportActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onWrongTagDetection(){
        Dialog.alertDialog(AdHocReportActivity.this, "Tag not recognised. Please scan again.");
        tv_user_name.setText("");
        tv_vehicle_no.setText("");
        tv_trailer.setText("");
        tv_manufacturer.setText("");
        tv_model.setText("");
        tv_nfc_tag.setText("");
        ll_nfc_tag.setBackgroundColor(Color.parseColor("#FFFFFF"));
        ll_vehicle_defects.setVisibility(View.GONE);
        ll_trailer_defects.setVisibility(View.GONE);
    }

    private void vehicleDetails(int vehicle_id){
        progressDialog.show();
        Query query = vehicleDetailsReference.whereEqualTo("status","Active").whereEqualTo("external_vehicle","No").whereEqualTo("id",vehicle_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (queryDocumentSnapshot.exists()){
                                for (int i = 0; i < arrayList_vehicle_id.size(); i++) {
                                    trailer = queryDocumentSnapshot.getString("trailer");
                                    registration_no = queryDocumentSnapshot.getString("registration_no");
                                    ll_vehicle_defects.setVisibility(View.VISIBLE);
                                    if (trailer.equals("Yes")) {
                                        ll_trailer_defects.setVisibility(View.VISIBLE);
                                    } else
                                        ll_trailer_defects.setVisibility(View.GONE);
                                    manufacturer = queryDocumentSnapshot.getString("manufacturer");
                                    model = queryDocumentSnapshot.getString("model");
                                }
                                Log.d("MODEL :"  ,manufacturer+" "+model);
                                tv_trailer.setText(trailer);
                                tv_vehicle_no.setText(registration_no);
                                tv_manufacturer.setText(manufacturer);
                                tv_model.setText(model);
                          }
                        }
                    }
                    else
                        progressDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AdHocReportActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // sort key table
    private void getMaximumLength(){
        sortKeyTableReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if (queryDocumentSnapshot.contains("ad_hoc_submission_key")){
                                doc_id_length = queryDocumentSnapshot.getLong("ad_hoc_submission_key").intValue();
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
                Toast.makeText(AdHocReportActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // getting trailer id
    private void inspectionSubmission(){
        Query query = inspectionSubmissionReference.whereEqualTo("conducted_on",conducted_on).whereEqualTo("vehicle_id",vehicle_id)
                .whereEqualTo("logged_by",logged_by).whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                trailer_id = queryDocumentSnapshot.getLong("trailer_id").intValue();
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AdHocReportActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
