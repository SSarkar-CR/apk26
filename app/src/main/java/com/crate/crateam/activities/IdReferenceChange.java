package com.crate.crateam.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheIndexManager;
import com.crate.crateam.utility.FirestoreManager;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class IdReferenceChange extends AppCompatActivity implements View.OnClickListener {
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference assetDetailsReference,assetNfcDetailsReference,assetMoreDetailsReference,assetQRDetailsReference;
    private Button bt_save,bt_back,bt_scan_qr;
    private String asset_type="",asset_number="",asset_name="",identification_method="",tagId,maxDocIdNFCDetails="",
            currentDate="",currentTime="",currentTag="",maxDocIdAssetMoreDetails="",currentQR="",qrCode="",maxDocIdQRDetails="";
    private int assetId=0,userId=0,assign_location=0,assign_site=0,previous_assign_location=0,previous_assign_site=0,nfcDetailsId=0,
            qrDetailsId=0,assetMoreId=0;
    private TextView tv_asset_type,tv_asset_id,tv_asset_name,tv_tag,tv_tag_confirmation,tv_current_tag,qr_value;
    private ImageView iv_nfcTag_one,iv_nfcTag_two;
    private LinearLayout ll_tag_one,ll_tag_two,ll_qr;
    private PopupWindow popupWindow,readyToScan;
    private NfcAdapter mNfcAdapter;
    private ArrayList<Integer> nfcIds = new ArrayList<>();
    private ArrayList<String> nfc_tags  = new ArrayList<>();
    private ArrayList<Integer> qrIds = new ArrayList<>();
    private ArrayList<String> qrValues  = new ArrayList<>();
    private ArrayList<Integer> assetMoreIds = new ArrayList<>();
    private ProgressDialog progressDialog;
    public static final String MIME_TEXT_PLAIN = "text/plain";

    public static final String TAG = "IDReference";
    private boolean isValidationDone = false,nfc_tags_exist = false,qr_exist=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.id_reference_change);
        initView();
    }
    private void initView(){
        FirestoreManager.initPersistentIndexManager();
        progressDialog = Dialog.showProgressDialog(this);
        assetDetailsReference = db.collection("AM_asset_details");
        assetNfcDetailsReference = db.collection("AM_NFC_details");
        assetMoreDetailsReference = db.collection("AM_asset_more_details");
        assetQRDetailsReference = db.collection("AM_QR_details");
        SessionManager sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        userId = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        iv_nfcTag_one = findViewById(R.id.iv_nfcTag_one);
        iv_nfcTag_two = findViewById(R.id.iv_nfcTag_two);
        ll_qr = findViewById(R.id.ll_qr);
        ll_tag_one = findViewById(R.id.ll_tag_one);
        ll_tag_two = findViewById(R.id.ll_tag_two);
        bt_scan_qr = findViewById(R.id.bt_scan_qr);
        bt_back = findViewById(R.id.bt_back);
        bt_save = findViewById(R.id.bt_save);
        tv_asset_type = findViewById(R.id.tv_asset_type);
        tv_asset_id = findViewById(R.id.tv_asset_id);
        tv_asset_name = findViewById(R.id.tv_asset_name);
        tv_tag = findViewById(R.id.tv_tag);
        tv_tag_confirmation = findViewById(R.id.tv_tag_confirmation);
        tv_current_tag = findViewById(R.id.tv_current_tag);
        qr_value = findViewById(R.id.qr_value);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            asset_type= extras.getString("asset_type");
            asset_number= extras.getString("asset_id");
            asset_name= extras.getString("asset_name");
            identification_method= extras.getString("identification_method");
            assetId = extras.getInt("assetId");
        }
        tv_asset_type.setText(asset_type);
        tv_asset_id.setText(asset_number);
        tv_asset_name.setText(asset_name);
        if (identification_method.equals("NFC")){
            ll_tag_one.setVisibility(View.VISIBLE);
            ll_tag_two.setVisibility(View.VISIBLE);
            ll_qr.setVisibility(View.GONE);
            fetchCurrentTag();
            getMaxDocIDNFCDetails();
        }else if (identification_method.equals("QR")){
            ll_tag_one.setVisibility(View.GONE);
            ll_tag_two.setVisibility(View.GONE);
            ll_qr.setVisibility(View.VISIBLE);
            fetchCurrentQR();
            getMaxDocIDQRDetails();
        }
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
       currentDate = AppData.date();
currentTime = AppData.Time();
        initializeOnClick();
        checkNfcAdapter();
        getMaxDocIDAssetMoreDetails();
    }
    private void initializeOnClick(){
        bt_back.setOnClickListener(this);
        bt_save.setOnClickListener(this);
        bt_scan_qr.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_back:
                finish();
                break;
            case R.id.bt_save:
                if (checkValidation()){
                    updateAssetDetailsTag();
                }
                break;
            case R.id.bt_scan_qr:
                scanQR();
                break;
        }
    }

    private void scanQR(){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setOrientationLocked(true);
        integrator.setPrompt(" ");
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("RDYFJF " ,intentResult.getContents());
                qrCode = intentResult.getContents();
                getQRDetails();
                bt_scan_qr.setBackgroundResource(R.color.green);
                qr_value.setVisibility(View.VISIBLE);
                qr_value.setText("QR Value : "+qrCode);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
            bt_scan_qr.setBackgroundResource(R.drawable.blue_button_background);
            qr_value.setVisibility(View.GONE);
        }
    }

    private void getQRDetails(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        assetQRDetailsReference.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            qrValues.add(queryDocumentSnapshot.getString("qr_code"));
                        }
                    }
                    if(qrValues.contains(qrCode))
                        qr_exist = true;
                    else
                        qr_exist = false;
                    if (qr_exist){
                        alertDialog("This QR is already in use and assigned to another asset. To use this QR please edit the\n" +
                                " asset linked to the QR, or use a new QR that is not assigned to an asset.");
                        qr_value.setText("");
                        qrCode="";
                        qr_exist = false;
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(IdReferenceChange.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCurrentQR(){
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
                            assign_location = Objects.requireNonNull(queryDocumentSnapshot.getLong("assign_location")).intValue();
                            assign_site = Objects.requireNonNull(queryDocumentSnapshot.getLong("assign_site")).intValue();
                            previous_assign_location = Objects.requireNonNull(queryDocumentSnapshot.getLong("previous_assign_location")).intValue();
                            previous_assign_site = Objects.requireNonNull(queryDocumentSnapshot.getLong("previous_assign_site")).intValue();
                            currentQR = queryDocumentSnapshot.getString("qr_code");
                        }
                        qr_value.setText("QR Value:" +currentQR);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(IdReferenceChange.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMaxDocIDQRDetails(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        assetQRDetailsReference.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            qrIds.add(Integer.parseInt(queryDocumentSnapshot.getId()));
                        }
                        maxDocIdQRDetails = String.valueOf(getMax(qrIds));
                        Log.d("max_qr_details_id :",maxDocIdQRDetails);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(IdReferenceChange.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitQRDetails() {
        progressDialog.show();
        if (maxDocIdQRDetails.equals(""))
            qrDetailsId = 1;
        else
            qrDetailsId = Integer.parseInt(maxDocIdQRDetails)+1;

        Map<String, Object> mapRegimeDetails = new HashMap<>();
        mapRegimeDetails.put("id",qrDetailsId);
        mapRegimeDetails.put("insert_user",userId);
        mapRegimeDetails.put("insert_time",currentTime);
        mapRegimeDetails.put("insert_date",currentDate);
        mapRegimeDetails.put("asset_id",assetId);
        mapRegimeDetails.put("qr_code",qrCode);
        mapRegimeDetails.put("status","Active");
        Query query = assetQRDetailsReference;
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    assetQRDetailsReference.document(String.valueOf(qrDetailsId)).set(mapRegimeDetails);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(IdReferenceChange.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCurrentTag(){
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
                            assign_location = Objects.requireNonNull(queryDocumentSnapshot.getLong("assign_location")).intValue();
                            assign_site = Objects.requireNonNull(queryDocumentSnapshot.getLong("assign_site")).intValue();
                            previous_assign_location = Objects.requireNonNull(queryDocumentSnapshot.getLong("previous_assign_location")).intValue();
                            previous_assign_site = Objects.requireNonNull(queryDocumentSnapshot.getLong("previous_assign_site")).intValue();
                            currentTag = queryDocumentSnapshot.getString("nfc_code");
                        }
                        tv_current_tag.setText("NFC Tag :" +currentTag);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(IdReferenceChange.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAssetDetailsTag(){
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
                            Map<String, Object> update_nfc = new HashMap<>();
                            if (identification_method.equals("NFC")) {
                                update_nfc.put("nfc_code", tagId);
                                submitNFCDetails();
                                assetDetailsReference.document(doc_id).update(update_nfc);
                                toConfirmation("NFC Tag successfully updated.");
                            }else if (identification_method.equals("QR")) {
                                update_nfc.put("qr_code", qrCode);
                                submitQRDetails();
                                assetDetailsReference.document(doc_id).update(update_nfc);
                                toConfirmation("QR code successfully updated.");
                            }
                            submitAssetMoreDetails();
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(IdReferenceChange.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMaxDocIDNFCDetails(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        assetNfcDetailsReference.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            nfcIds.add(Integer.parseInt(queryDocumentSnapshot.getId()));
                        }
                        maxDocIdNFCDetails = String.valueOf(getMax(nfcIds));
                        Log.d("max_nfc_details_id :",maxDocIdNFCDetails);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(IdReferenceChange.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void getNFCDetails(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        assetNfcDetailsReference.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            nfc_tags.add(queryDocumentSnapshot.getString("nfc_code"));
                        }
                    }
                    if(nfc_tags.contains(tagId))
                        nfc_tags_exist = true;
                    else
                        nfc_tags_exist = false;
                    if (nfc_tags_exist){
                        alertDialog("This tag is already in use and assigned to another asset. To use this tag please edit the\n" +
                                " asset linked to the tag, or use a new tag that is not assigned to an asset.");
                        tv_tag.setText("Tag ID");
                        tv_tag_confirmation.setText("Confirm Tag ID");
                        iv_nfcTag_one.setImageResource(R.drawable.nfc);
                        iv_nfcTag_two.setImageResource(R.drawable.nfc);
                        nfc_tags_exist = false;
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(IdReferenceChange.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitNFCDetails() {
            progressDialog.show();
            if (maxDocIdNFCDetails.equals(""))
                nfcDetailsId = 1;
            else
                nfcDetailsId = Integer.parseInt(maxDocIdNFCDetails)+1;

            Map<String, Object> mapRegimeDetails = new HashMap<>();
            mapRegimeDetails.put("id",nfcDetailsId);
            mapRegimeDetails.put("insert_user",userId);
            mapRegimeDetails.put("insert_time",currentTime);
            mapRegimeDetails.put("insert_date",currentDate);
            mapRegimeDetails.put("asset_id",assetId);
            mapRegimeDetails.put("nfc_code",tagId);
            mapRegimeDetails.put("status","Active");
        Query query = assetNfcDetailsReference;
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    assetNfcDetailsReference.document(String.valueOf(nfcDetailsId)).set(mapRegimeDetails);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(IdReferenceChange.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMaxDocIDAssetMoreDetails(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        assetMoreDetailsReference.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            assetMoreIds.add(Integer.parseInt(queryDocumentSnapshot.getId()));
                        }
                        maxDocIdAssetMoreDetails = String.valueOf(getMax(assetMoreIds));
                        Log.d("max_asset_more_details_id :",maxDocIdAssetMoreDetails);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(IdReferenceChange.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitAssetMoreDetails(){
        progressDialog.show();
        if (maxDocIdAssetMoreDetails.equals(""))
            assetMoreId = 1;
        else
            assetMoreId = Integer.parseInt(maxDocIdAssetMoreDetails)+1;

        Map<String, Object> mapRegimeDetails = new HashMap<>();
        mapRegimeDetails.put("id",assetMoreId);
        mapRegimeDetails.put("user_id",userId);
        mapRegimeDetails.put("asset_id",assetId);
        mapRegimeDetails.put("asset_name",asset_name);
        mapRegimeDetails.put("asset_number",asset_number);
        mapRegimeDetails.put("assign_location",assign_location);
        mapRegimeDetails.put("assign_site",assign_site);
        mapRegimeDetails.put("created_at",FieldValue.serverTimestamp());
        mapRegimeDetails.put("nfc_code",tagId);
        mapRegimeDetails.put("previous_assign_location",previous_assign_location);
        mapRegimeDetails.put("previous_assign_site",previous_assign_site);
        mapRegimeDetails.put("previous_nfc_code",currentTag);
        mapRegimeDetails.put("previous_qr_code",currentQR);
        mapRegimeDetails.put("qr_code",qrCode);
        mapRegimeDetails.put("submit_type","App");
        mapRegimeDetails.put("time_second_format", AppData.getTimeSecond());
        mapRegimeDetails.put("updated_at",FieldValue.serverTimestamp());
        mapRegimeDetails.put("updated_date",currentDate);
        mapRegimeDetails.put("status","Active");
        Query query = assetMoreDetailsReference;
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    assetMoreDetailsReference.document(String.valueOf(assetMoreId)).set(mapRegimeDetails);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(IdReferenceChange.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Boolean checkValidation(){
        if (identification_method.equals("NFC") && tv_tag.getText().toString().equals("Tag ID")) {
            Dialog.alertDialog(this, "Please scan NFC Tag.");
        } else if (identification_method.equals("NFC") && tv_tag_confirmation.getText().toString().equals("Confirm Tag ID"))
            Dialog.alertDialog(this, "Please scan NFC Tag again.");
        else if (identification_method.equals("QR") && qrCode.equals(""))
            Dialog.alertDialog(this, "Please scan QR.");
        else
            isValidationDone = true;
        return isValidationDone;
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
        handleNfcIntent(intent);
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
    private void handleNfcIntent(Intent intent)
    {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) ||NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                tagId = ByteArrayToHexString(Objects.requireNonNull(tag).getId());
                Log.d("NFC_SERIAL_NO_Tag_Manager :" ,tagId);
                ndefmessage(tag);
                Log.d("NDEF_Message", "Detected: " +ndefmessage(tag));
            } else {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                tagId = ByteArrayToHexString(Objects.requireNonNull(tag).getId());
                Log.d("NFC_SERIAL_NO_Tag_Manager :" ,tagId);
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
        getNFCDetails();
        String tag_one = tv_tag.getText().toString();
        if (tag_one.equals("Tag ID")) {
            tv_tag.setText(tagId);
            iv_nfcTag_one.setImageResource(R.drawable.nfc_green);
            successfullyScanDialog();
        }else {
            tv_tag_confirmation.setText(tagId);
            iv_nfcTag_two.setImageResource(R.drawable.nfc_green);
            if (!tv_tag_confirmation.getText().toString().equals(tv_tag.getText().toString())) {
                alertDialog("NFC Tags does not match.Please try again.");
                tv_tag_confirmation.setText("Confirm Tag ID");
                iv_nfcTag_two.setImageResource(R.drawable.nfc);
            }
        }
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

    private void toConfirmation(String message) {
        final View popupView = getLayoutInflater().inflate(R.layout.change_pass_popup_layout, null, true);
         boolean isActivityInForeground = IdReferenceChange.this.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED);
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