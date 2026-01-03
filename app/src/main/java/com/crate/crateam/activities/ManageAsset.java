package com.crate.crateam.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import com.crate.crateam.R;
import com.crate.crateam.adapters.AssetElementsAdapter;
import com.crate.crateam.utility.CustomSearchableSpinner;
import com.crate.crateam.utility.Dialog;
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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ManageAsset extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference assetDetailsReference,assetTypeReference;
    private Button bt_manage_isseus,bt_edit_key_dates,bt_change_tag,bt_off_hire_request,bt_deactivate_assets,bt_back,
            bt_nfc,bt_qr_scan,bt_id_search,bt_safety_indicators;
    private ImageView iv_cross;
    private NfcAdapter mNfcAdapter;
    private TextView tv_asset_type,tv_asset_id,tv_asset_name,tv_nfc_tag;
    private CustomSearchableSpinner sp_asset_type,sp_asset_details;
    private LinearLayout ll_asset_details,ll_nfc_tag;
    private String asset_type="",asset_number="",asset_name="",tagId,identification_method="",qrCode="",asset_nri="";
    private int assetId = 0,assetTypeId=0,regimeId=0,groupId=0,assignLocationId=0;
    private ArrayList<String> arraylist_asset_type_name = new ArrayList<>();
    private ArrayList<Integer> arraylist_group_id = new ArrayList<>();
    private ArrayList<Integer> arrayList_regime_id  = new ArrayList<>();
    private ArrayList<Integer> arraylist_asset_id = new ArrayList<>();
    private ArrayList<Integer> arraylist_asset_type_id = new ArrayList<>();
    private ArrayList<Integer> arraylist_assign_location_id = new ArrayList<>();
    private ArrayList<String> arraylist_asset_name = new ArrayList<>();
    private ArrayList<String> arraylist_asset_number = new ArrayList<>();
    private ArrayList<String> arraylist_asset_nri = new ArrayList<>();
    private PopupWindow popupWindow;
    private static final String MIME_TEXT_PLAIN = "text/plain";

    public static final String TAG = "ManageAsset";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_asset);
        initView();
    }
    private void initView(){
        FirestoreManager.initPersistentIndexManager();
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        assetDetailsReference = db.collection("AM_asset_details");
        assetTypeReference = db.collection("AM_asset_type");
        bt_nfc = findViewById(R.id.bt_nfc);
        bt_id_search = findViewById(R.id.bt_id_search);
        bt_qr_scan = findViewById(R.id.bt_qr_scan);
        bt_manage_isseus = findViewById(R.id.bt_manage_isseus);
        bt_edit_key_dates = findViewById(R.id.bt_edit_key_dates);
        bt_change_tag = findViewById(R.id.bt_change_tag);
        bt_off_hire_request = findViewById(R.id.bt_off_hire_request);
        bt_deactivate_assets = findViewById(R.id.bt_deactivate_assets);
        bt_safety_indicators = findViewById(R.id.bt_safety_indicators);
        bt_back = findViewById(R.id.bt_back);
        tv_asset_type = findViewById(R.id.tv_asset_type);
        tv_asset_id = findViewById(R.id.tv_asset_id);
        tv_asset_name = findViewById(R.id.tv_asset_name);
        tv_nfc_tag = findViewById(R.id.tv_nfc_tag);
        iv_cross = findViewById(R.id.iv_cross);
        ll_asset_details = findViewById(R.id.ll_asset_details);
        ll_nfc_tag = findViewById(R.id.ll_nfc_tag);
        sp_asset_type = findViewById(R.id.sp_asset_type);
        sp_asset_details = findViewById(R.id.sp_asset_details);
        initializeOnClick();
        checkNfcAdapter();
    }
    private void initializeOnClick(){
        bt_nfc.setOnClickListener(this);
        bt_id_search.setOnClickListener(this);
        bt_qr_scan.setOnClickListener(this);
        bt_manage_isseus.setOnClickListener(this);
        bt_edit_key_dates.setOnClickListener(this);
        bt_safety_indicators.setOnClickListener(this);
        bt_change_tag.setOnClickListener(this);
        bt_off_hire_request.setOnClickListener(this);
        bt_deactivate_assets.setOnClickListener(this);
        bt_back.setOnClickListener(this);
        sp_asset_type.setOnItemSelectedListener(this);
        sp_asset_details.setOnItemSelectedListener(this);
        iv_cross.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_nfc:
                sp_asset_type.setVisibility(View.GONE);
                sp_asset_details.setVisibility(View.GONE);
                ll_nfc_tag.setVisibility(View.VISIBLE);
                break;
            case R.id.bt_id_search:
                sp_asset_type.setVisibility(View.VISIBLE);
                sp_asset_details.setVisibility(View.VISIBLE);
                ll_nfc_tag.setVisibility(View.GONE);
                getAssetTypes();
                break;
            case R.id.bt_qr_scan:
                sp_asset_type.setVisibility(View.GONE);
                sp_asset_details.setVisibility(View.GONE);
                ll_nfc_tag.setVisibility(View.GONE);
                scanQR();
                break;
            case R.id.bt_manage_isseus:
                Intent to_manage_issue = new Intent(ManageAsset.this,IssueManagement.class);
                to_manage_issue.putExtra("asset_type",asset_type);
                to_manage_issue.putExtra("asset_type_id",assetTypeId);
                to_manage_issue.putExtra("asset_id",asset_number);
                to_manage_issue.putExtra("asset_name",asset_name);
                to_manage_issue.putExtra("assetId",assetId);
                to_manage_issue.putExtra("regime",regimeId);
                startActivity(to_manage_issue);
                break;
            case R.id.bt_edit_key_dates:
                Intent to_edit_dates = new Intent(ManageAsset.this,AssetDateManagement.class);
                to_edit_dates.putExtra("asset_type",asset_type);
                to_edit_dates.putExtra("asset_id",asset_number);
                to_edit_dates.putExtra("asset_name",asset_name);
                to_edit_dates.putExtra("identification_method",identification_method);
                to_edit_dates.putExtra("assetId",assetId);
                to_edit_dates.putExtra("asset_nri",asset_nri);
                startActivity(to_edit_dates);
                break;
            case R.id.bt_safety_indicators:
                Intent to_safety_indicators = new Intent(ManageAsset.this,EditSafetyIndicators.class);
                to_safety_indicators.putExtra("asset_type",asset_type);
                to_safety_indicators.putExtra("asset_id",asset_number);
                to_safety_indicators.putExtra("asset_name",asset_name);
                to_safety_indicators.putExtra("identification_method",identification_method);
                to_safety_indicators.putExtra("assetId",assetId);
                startActivity(to_safety_indicators);
                break;
            case R.id.bt_change_tag:
                Intent to_id_reference = new Intent(ManageAsset.this,IdReferenceChange.class);
                to_id_reference.putExtra("asset_type",asset_type);
                to_id_reference.putExtra("asset_id",asset_number);
                to_id_reference.putExtra("asset_name",asset_name);
                to_id_reference.putExtra("identification_method",identification_method);
                to_id_reference.putExtra("assetId",assetId);
                startActivity(to_id_reference);
                break;
            case R.id.bt_off_hire_request:
                Intent to_off_hire = new Intent(ManageAsset.this,OffHireRequest.class);
                to_off_hire.putExtra("asset_type",asset_type);
                to_off_hire.putExtra("asset_id",asset_number);
                to_off_hire.putExtra("asset_name",asset_name);
                to_off_hire.putExtra("assetId",assetId);
                to_off_hire.putExtra("assignLocationId",assignLocationId);
                startActivity(to_off_hire);
            case R.id.bt_deactivate_assets:

            case R.id.bt_back:
            case R.id.iv_cross:
                finish();
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

    public void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, dataIntent);
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("RDYFJF " ,intentResult.getContents());
                qrCode = intentResult.getContents();
                getAssetDetailsByQR();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, dataIntent);
        }
    }

    private void getAssetDetailsByQR(){
        Query query = assetDetailsReference.whereEqualTo("status", "Active").whereEqualTo("qr_code",qrCode);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            assetId = Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
                            assignLocationId = Objects.requireNonNull(queryDocumentSnapshot.getLong("assign_location")).intValue();
                            asset_name = queryDocumentSnapshot.getString("asset_name");
                            asset_number = queryDocumentSnapshot.getString("asset_number");
                            regimeId = Objects.requireNonNull(queryDocumentSnapshot.getLong("regime_id")).intValue();
                            assetTypeId = Objects.requireNonNull(queryDocumentSnapshot.getLong("asset_type_id")).intValue();
                            asset_nri = queryDocumentSnapshot.getString("next_routine_inspection_date");
                        }
                        ll_asset_details.setVisibility(View.VISIBLE);
                        getAssetTypeName(assetTypeId);
                        tv_asset_name.setText(asset_name);
                        tv_asset_id.setText(asset_number);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ManageAsset.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_asset_type:
                assetTypeId = arraylist_asset_type_id.get(position);
                getAssetDetailsByAssetTypeId(assetTypeId);
                getAssetTypeName(assetTypeId);
                break;
            case R.id.sp_asset_details:
                ll_asset_details.setVisibility(View.VISIBLE);
                groupId = arraylist_group_id.get(position);
                regimeId = arrayList_regime_id.get(position);
                assetId = arraylist_asset_id.get(position);
                asset_nri = arraylist_asset_nri.get(position);
                asset_number = arraylist_asset_number.get(position);
                asset_name = arraylist_asset_name.get(position);
                assignLocationId = arraylist_assign_location_id.get(position);
                tv_asset_id.setText(asset_number);
                tv_asset_name.setText(asset_name);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void getAssetTypes() {
        Query query = assetTypeReference.whereEqualTo("status", "Active").orderBy("asset_type_name");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    arraylist_asset_type_name.clear();
                    arraylist_asset_type_id.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        arraylist_asset_type_name.add(queryDocumentSnapshot.getString("asset_type_name"));
                        arraylist_asset_type_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                    }
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(ManageAsset.this, R.layout.spinner_custom_layout, arraylist_asset_type_name);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_asset_type.setAdapter(spinnerArrayAdapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ManageAsset.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAssetDetailsByAssetTypeId(int asset_type_id){
        Query query = assetDetailsReference.whereEqualTo("status", "Active").whereEqualTo("asset_type_id",asset_type_id)
                .orderBy("asset_name");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    arraylist_asset_id.clear();
                    arraylist_asset_name.clear();
                    arraylist_asset_number.clear();
                    arraylist_group_id.clear();
                    arraylist_assign_location_id.clear();
                    arraylist_asset_nri.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        arraylist_asset_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                        arraylist_asset_name.add(queryDocumentSnapshot.getString("asset_name"));
                        arraylist_asset_number.add(queryDocumentSnapshot.getString("asset_number"));
                        arrayList_regime_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("regime_id")).intValue());
                        arraylist_group_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("asset_group_id")).intValue());
                        arraylist_assign_location_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("assign_location")).intValue());
                        arraylist_asset_nri.add(queryDocumentSnapshot.getString("next_routine_inspection_date"));
                    }
                    ArrayList<String> combinedList = new ArrayList<>();
                    for (int i=0;i<arraylist_asset_id.size();i++){
                        combinedList.add(arraylist_asset_name.get(i)+" , "+arraylist_asset_number.get(i));
                    }
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(ManageAsset.this, R.layout.spinner_custom_layout, combinedList);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_asset_details.setAdapter(spinnerArrayAdapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ManageAsset.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAssetTypeName(int assetTypeId) {
        Query query = assetTypeReference.whereEqualTo("status", "Active").whereEqualTo("id",assetTypeId);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        asset_type = queryDocumentSnapshot.getString("asset_type_name");
                        identification_method = queryDocumentSnapshot.getString("identification_method");
                    }
                    tv_asset_type.setText(asset_type);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ManageAsset.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private  void checkNfcAdapter(){
        if (mNfcAdapter == null) {
            alertDialog("This device doesn't support NFC.");
        }
        if (mNfcAdapter != null) {
            if (!mNfcAdapter.isEnabled())
                alertDialog("NFC is disabled.Please enable NFC from your mobile settings.");
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
                Log.d("NFC_SERIAL_NO :" ,tagId);
                ndefmessage(tag);
                Log.d("NDEF_Message", "Detected: " +ndefmessage(tag));
            } else {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                tagId = ByteArrayToHexString(Objects.requireNonNull(tag).getId());
                Log.d("NFC_SERIAL_NO :" ,tagId);
                getAssetDetailsByNFC();
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
                getAssetDetailsByNFC();
                ndef.close();
            }
            else
                scanFailedDialog();
        } catch (IOException | FormatException e) {
            Log.e(TAG, "An unexpected error occurred", e);
        }
        return null;
    }

    private void getAssetDetailsByNFC(){
        Query query = assetDetailsReference.whereEqualTo("status", "Active").whereEqualTo("nfc_code",tagId);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            assetId = Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
                            assignLocationId = Objects.requireNonNull(queryDocumentSnapshot.getLong("assign_location")).intValue();
                            asset_name = queryDocumentSnapshot.getString("asset_name");
                            asset_number = queryDocumentSnapshot.getString("asset_number");
                            assetTypeId = Objects.requireNonNull(queryDocumentSnapshot.getLong("asset_type_id")).intValue();
                            regimeId = Objects.requireNonNull(queryDocumentSnapshot.getLong("regime_id")).intValue();
                            asset_nri = queryDocumentSnapshot.getString("next_routine_inspection_date");
                        }
                        successfullyScanDialog();
                        ll_asset_details.setVisibility(View.VISIBLE);
                        tv_nfc_tag.setText(tagId);
                        getAssetTypeName(assetTypeId);
                        tv_asset_name.setText(asset_name);
                        tv_asset_id.setText(asset_number);
                        ll_nfc_tag.setBackgroundColor(Color.parseColor("#00CC66"));
                    }else {
                        onWrongTagDetection();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ManageAsset.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onWrongTagDetection(){
        Dialog.alertDialog(ManageAsset.this, "Tag not recognised. Please scan again.");
        tv_asset_type.setText("");
        tv_asset_id.setText("");
        tv_asset_name.setText("");
        ll_nfc_tag.setBackgroundColor(Color.parseColor("#FFFFFF"));
        ll_asset_details.setVisibility(View.GONE);
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

}