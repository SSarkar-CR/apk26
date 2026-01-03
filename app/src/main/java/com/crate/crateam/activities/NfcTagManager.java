package com.crate.crateam.activities;


import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.crate.crateam.R;
import com.crate.crateam.utility.Dialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheIndexManager;
import com.crate.crateam.utility.FirestoreManager;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NfcTagManager extends Activity implements View.OnClickListener {
    public static final String MIME_TEXT_PLAIN = "text/plain";
    private ImageView iv_cross_nfc,iv_nfcTag_one,iv_nfcTag_two;
    private TextView tv_tag,tv_tag_confirmation,tv_current_tag;
    private Button bt_save_nfc,bt_discard;
    private Spinner sp_vehicle_trailer_list;
    private RadioGroup rg_vehicle_trailer,rg_vehicle_tags,rg_trailer_tags;
    private LinearLayout ll_vehicle_tags,ll_trailer_tags;
    private String doc_id,spinner_position="",tag_position,vehicle_type,tagId,position;
    private int vehicle_id,trailer_id;
    private ProgressDialog progressDialog;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private NfcAdapter mNfcAdapter;
    private PopupWindow popupWindow;
    private PopupWindow readyToScan;
    private ArrayList<Integer> vehicleId = new ArrayList<Integer>();
    private ArrayList<String> trailer_model_no = new ArrayList<String>();
    private ArrayList<Integer> trailerId = new ArrayList<Integer>();
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference vehicleDetailsReference,trailerDetailsReference;
    private boolean vehicle_nfc_tags_exist = false;
    private boolean trailer_nfc_tags_exist = false;

    public static final String TAG = "NfcTagManager";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc_tag_manager_layout);
        initView();
        initializeOnClick();
    }
    private void initView(){
        FirestoreManager.initPersistentIndexManager();
        vehicleDetailsReference = db.collection("CR_vehicle_details");
        trailerDetailsReference = db.collection("CR_trailer_details");
        pref = this.getSharedPreferences("MyPref", 0); // 0 - for private mode
        progressDialog = Dialog.showProgressDialog(this);
        iv_cross_nfc = findViewById(R.id.iv_cross_nfc);
        rg_vehicle_trailer = findViewById(R.id.rg_vehicle_trailer);
        rg_vehicle_tags = findViewById(R.id.rg_vehicle_tags);
        rg_trailer_tags = findViewById(R.id.rg_trailer_tags);
        ll_vehicle_tags = findViewById(R.id.ll_vehicle_tags);
        ll_trailer_tags = findViewById(R.id.ll_trailer_tags);
        sp_vehicle_trailer_list = findViewById(R.id.sp_vehicle_trailer_list);
        bt_save_nfc = findViewById(R.id.bt_save_nfc);
        bt_discard = findViewById(R.id.bt_discard);
        tv_tag = findViewById(R.id.tv_tag);
        tv_tag_confirmation = findViewById(R.id.tv_tag_confirmation);
        tv_current_tag = findViewById(R.id.tv_current_tag);
        iv_nfcTag_one = findViewById(R.id.iv_nfcTag_one);
        iv_nfcTag_two = findViewById(R.id.iv_nfcTag_two);
        rg_vehicle_trailer.check(R.id.rb_vehicle);
        vehicle_type = "vehicle";
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        progressDialog.show();
        loadVehicleDetails();
        manageRadioButton();
        checkNfcAdapter();
    }



    private void initializeOnClick(){
        iv_cross_nfc.setOnClickListener(this);
        bt_save_nfc.setOnClickListener(this);
        bt_discard.setOnClickListener(this);
        iv_nfcTag_one.setOnClickListener(this);
        iv_nfcTag_two.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_cross_nfc:
                finish();
                break;
            case R.id.iv_nfcTag_one:
            case R.id.iv_nfcTag_two:
                 readyToScanDialog();
                 break;
            case R.id.bt_save_nfc:
                progressDialog.show();
                Log.d("NFC_TAG_Manager : ",vehicle_id+" "+vehicle_type+" "+tagId+" "+tag_position);
                if (position==null || position.isEmpty()){
                    if (vehicle_type.equals("trailer"))
                    alertDialog("Please select trailer position.");
                    else if (vehicle_type.equals("vehicle"))
                        alertDialog("Please select vehicle position.");
                    progressDialog.dismiss();
                } else if (tv_tag.getText().toString().equals("Tag ID")) {
                    alertDialog("Please scan NFC Tag.");
                    progressDialog.dismiss();
                } else if (tv_tag_confirmation.getText().toString().equals("Confirm Tag ID")) {
                    alertDialog("Please scan NFC Tag again.");
                    progressDialog.dismiss();
                } else
                    if (position.equals("trailer_near_rear") || position.equals("trailer_off_rear") || position.equals("front") || position.equals("near_rear") || position.equals("off_rear") || position.equals("completion"))
                        getNfcTgsVehicleDetails();
                break;
            case R.id.bt_discard:
                iv_nfcTag_one.setImageResource(R.drawable.nfc);
                iv_nfcTag_two.setImageResource(R.drawable.nfc);
                tv_tag.setText("Tag ID");
                tv_tag_confirmation.setText("Confirm Tag ID");
                Intent toDashboard = new Intent(NfcTagManager.this,MainActivity.class);
                startActivity(toDashboard);
                finish();
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

    private void manageRadioButton(){
        rg_vehicle_trailer.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i==R.id.rb_vehicle) {
                    vehicle_type = "vehicle";
                    loadVehicleDetails();
                    ll_vehicle_tags.setVisibility(View.VISIBLE);
                    ll_trailer_tags.setVisibility(View.GONE);
                    rg_vehicle_tags.clearCheck();
                    tv_current_tag.setVisibility(View.INVISIBLE);
                    tagId="";
                    tag_position = "";
                   // position = "";
                }
                if (i==R.id.rb_trailer) {
                    vehicle_type = "trailer";
                    loadTrailerDetails();
                    ll_vehicle_tags.setVisibility(View.GONE);
                    ll_trailer_tags.setVisibility(View.VISIBLE);
                    rg_trailer_tags.clearCheck();
                    tv_current_tag.setVisibility(View.INVISIBLE);
                    tagId="";
                    tag_position = "";
                   // position = "";
                }
            }
        });
        rg_vehicle_tags.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                position = "";
                tag_position = "";
                if (i==R.id.rb_front_tag) {
                    tag_position = "Front";
                    position = "front";
                    tv_current_tag.setVisibility(View.VISIBLE);
                    onChangeButton();
                    loadVehicleNfcTags();
                }
                if (i==R.id.rb_near_tag) {
                    tag_position = "Near (Rear)";
                    position = "near_rear";
                    tv_current_tag.setVisibility(View.VISIBLE);
                    onChangeButton();
                    loadVehicleNfcTags();
                }
                if (i==R.id.rb_off_tag) {
                    tag_position = "Off (Rear)";
                    position = "off_rear";
                    tv_current_tag.setVisibility(View.VISIBLE);
                    onChangeButton();
                    loadVehicleNfcTags();
                }
                if (i==R.id.rb_completion_tag) {
                    tag_position = "Completion";
                    position = "completion";
                    tv_current_tag.setVisibility(View.VISIBLE);
                    onChangeButton();
                    loadVehicleNfcTags();
                }
            }
        });
        rg_trailer_tags.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                position="";
                tag_position = "";
                if (i==R.id.rb_near_tag_trailer) {
                    tag_position = "Trailer-Near (Rear)";
                    position = "trailer_near_rear";
                    tv_current_tag.setVisibility(View.VISIBLE);
                    onChangeButton();
                    loadTrailerNfcTags();
                }
                if (i==R.id.rb_off_tag_trailer) {
                    tag_position = "Trailer-Off (Rear)";
                    position = "trailer_off_rear";
                    tv_current_tag.setVisibility(View.VISIBLE);
                    onChangeButton();
                    loadTrailerNfcTags();
                }
            }
        });
    }

    private  void loadVehicleDetails(){
        Query vehicleDetailsQry = vehicleDetailsReference.whereEqualTo("external_vehicle","No").whereEqualTo("status", "Active");
        final List<String> subjects = new ArrayList<>();
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_custom_layout,subjects);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        sp_vehicle_trailer_list.setAdapter(spinnerArrayAdapter);
        vehicleDetailsQry.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String vehicle_reg = document.getString("registration_no");
                        vehicle_id = Objects.requireNonNull(document.getLong("id")).intValue();
                        vehicleId.add(vehicle_id);
                        subjects.add(vehicle_reg);
                    }
                    spinnerArrayAdapter.notifyDataSetChanged();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("ERROR :", "Unable to get active vehicle list");
                progressDialog.dismiss();
                alertDialog("Unable to get active vehicle list");
            }
        });
        sp_vehicle_trailer_list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                onVehicleSpinnerSelection();
                vehicle_id = vehicleId.get(i);
                spinner_position = sp_vehicle_trailer_list.getSelectedItem().toString();
                editor = pref.edit();
                editor.putString("spinnerChoice",spinner_position);
                editor.apply();
                Log.d("SPINNER_POSITION :" ,spinner_position);
                Log.d("SPINNER_POSITION_VALUES :" , String.valueOf(vehicle_id));
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                String spinnerValue = pref.getString("spinnerChoice",null);
                if(!spinnerValue.equals("null")) {
                    ArrayAdapter myAdapter = (ArrayAdapter) sp_vehicle_trailer_list.getAdapter();
                    int spinnerPosition = myAdapter.getPosition(spinnerValue);
                    sp_vehicle_trailer_list.setSelection(spinnerPosition);
                    rg_vehicle_tags.clearCheck();
                    rg_trailer_tags.clearCheck();
                    tv_current_tag.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void loadTrailerDetails(){
        Query trailerDetailsQry = trailerDetailsReference.whereEqualTo("status", "Active");
        final List<String> subjects = new ArrayList<>();
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_custom_layout,subjects);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        sp_vehicle_trailer_list.setAdapter(spinnerArrayAdapter);
        trailerDetailsQry.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        int trailer_id = Objects.requireNonNull(document.getLong("id")).intValue();
                        String model_no = document.getString("model_no");
                        trailer_model_no.add(model_no);
                        trailerId.add(trailer_id);
                        subjects.add(model_no);
                    }
                    spinnerArrayAdapter.notifyDataSetChanged();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("ERROR :", "Unable to get active vehicle list");
                progressDialog.dismiss();
                alertDialog("Unable to get active vehicle list");
            }
        });
        sp_vehicle_trailer_list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                onTrailerSpinnerSelection();
                trailer_id = trailerId.get(i);
                if (sp_vehicle_trailer_list != null && sp_vehicle_trailer_list.getSelectedItem() != null) {
                    spinner_position = sp_vehicle_trailer_list.getSelectedItem().toString();
                    editor = pref.edit();
                    editor.putString("trailerSpinnerChoice",spinner_position);
                    editor.apply();
                    Log.d("SPINNER_POSITION :",spinner_position);
                    Log.d("SPINNER_POSITION_VALUES :", String.valueOf(trailer_id));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                String spinnerValue = pref.getString("trailerSpinnerChoice",null);
                if(!spinnerValue.equals("null")) {
                    ArrayAdapter myAdapter = (ArrayAdapter) sp_vehicle_trailer_list.getAdapter();
                    int spinnerPosition = myAdapter.getPosition(spinnerValue);
                    sp_vehicle_trailer_list.setSelection(spinnerPosition);
                    rg_vehicle_tags.clearCheck();
                    rg_trailer_tags.clearCheck();
                    tv_current_tag.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void onVehicleSpinnerSelection(){
        ll_vehicle_tags.setVisibility(View.VISIBLE);
        ll_trailer_tags.setVisibility(View.GONE);
        rg_vehicle_tags.clearCheck();
        rg_trailer_tags.clearCheck();
        rg_vehicle_trailer.check(R.id.rb_vehicle);
        tv_current_tag.setVisibility(View.INVISIBLE);
        tagId="";
        tag_position = "";
    }

    private void onTrailerSpinnerSelection(){
        ll_vehicle_tags.setVisibility(View.GONE);
        ll_trailer_tags.setVisibility(View.VISIBLE);
        rg_vehicle_tags.clearCheck();
        rg_trailer_tags.clearCheck();
        rg_vehicle_trailer.check(R.id.rb_trailer);
        tv_current_tag.setVisibility(View.INVISIBLE);
        tagId="";
        tag_position = "";
    }

    private void onChangeButton(){
        tv_tag.setText("Tag ID");
        tv_tag_confirmation.setText("Confirm Tag ID");
        iv_nfcTag_one.setImageResource(R.drawable.nfc);
        iv_nfcTag_two.setImageResource(R.drawable.nfc);
    }

    private void loadVehicleNfcTags(){
        Query nfcDetailsQuery = vehicleDetailsReference.whereEqualTo("external_vehicle","No").whereEqualTo("id", vehicle_id).whereEqualTo("status","Active");
        nfcDetailsQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().isEmpty())
                        tv_current_tag.setVisibility(View.INVISIBLE);
                    else {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            String tag_value = "";
                            if (position.equals("completion"))
                                tag_value = queryDocumentSnapshot.getString("vehicle_completion");
                            else if (position.equals("front"))
                                tag_value = queryDocumentSnapshot.getString("vehicle_front");
                            else if (position.equals("near_rear"))
                                tag_value = queryDocumentSnapshot.getString("vehicle_near_rear");
                            else
                                tag_value = queryDocumentSnapshot.getString("vehicle_off_rear");

                            tv_current_tag.setText("Current " + tag_position + " Tag :" + tag_value);
                        }
                    }
                }
            }
        });
    }

    private void loadTrailerNfcTags(){
        Query nfcDetailsQuery = trailerDetailsReference.whereEqualTo("id", trailer_id).whereEqualTo("status","Active");
        nfcDetailsQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().isEmpty())
                        tv_current_tag.setVisibility(View.INVISIBLE);
                    else {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String tag_value = "";
                            if (position.equals("trailer_near_rear"))
                                tag_value = document.getString("trailer_near_rear");
                            else
                                tag_value = document.getString("trailer_off_rear");
                            doc_id = document.getId();
                            Log.d("TAG_VALUE :" ,tag_value + " " + doc_id);
                            tv_current_tag.setText("Current " + tag_position + " Tag :" + tag_value);
                        }
                    }
                }
            }
        });
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

    private void readyToScanDialog() {
        final View popupView = getLayoutInflater().inflate(R.layout.ready_to_scan_layout,null,true);
        readyToScan = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,true);
        readyToScan.showAtLocation(popupView, Gravity.BOTTOM,0,0);
        Button cancelPopup = popupView.findViewById(R.id.bt_cancel_popup);
        cancelPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readyToScan.dismiss();
            }
        });
        Dialog.dialogTimer(readyToScan);
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

    private void alertDialogSuccess(String message){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
//                        refresh();
                    }
                });
        alertDialog.show();
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

    private void updateValue(){
        Map<String, Object> objectMapUpdate = new HashMap<>();
        if (position.equals("trailer_near_rear"))
            objectMapUpdate.put("trailer_near_rear", tv_tag.getText().toString());
        else
            objectMapUpdate.put("trailer_off_rear", tv_tag.getText().toString());

        objectMapUpdate.put("is_updated","Yes");
        objectMapUpdate.put("updated_at", FieldValue.serverTimestamp());
        trailerDetailsReference.document(String.valueOf(trailer_id)).update(objectMapUpdate);

        vehicle_nfc_tags_exist = false;
        trailer_nfc_tags_exist = false;
        position = "";
        tag_position = "";
        tv_tag.setText("Tag ID");
        tv_tag_confirmation.setText("Confirm Tag ID");
        rg_vehicle_tags.clearCheck();
        rg_trailer_tags.clearCheck();
        iv_nfcTag_one.setImageResource(R.drawable.nfc);
        iv_nfcTag_two.setImageResource(R.drawable.nfc);
        tv_current_tag.setText("");
        alertDialogSuccess("NFC tag submitted successfully.");
        tv_current_tag.setVisibility(View.INVISIBLE);
    }

    private void updateVehicleDetails(){
        Map<String, Object> objectMapUpdate = new HashMap<>();
        if (position.equals("completion"))
            objectMapUpdate.put("vehicle_completion",tv_tag.getText().toString().trim());
       else if (position.equals("front"))
            objectMapUpdate.put("vehicle_front",tv_tag.getText().toString().trim());
       else if (position.equals("near_rear"))
            objectMapUpdate.put("vehicle_near_rear", tv_tag.getText().toString());
        else
            objectMapUpdate.put("vehicle_off_rear", tv_tag.getText().toString());

        objectMapUpdate.put("is_updated","Yes");
        objectMapUpdate.put("updated_at", FieldValue.serverTimestamp());
        vehicleDetailsReference.document(String.valueOf(vehicle_id)).update(objectMapUpdate);

        vehicle_nfc_tags_exist = false;
        trailer_nfc_tags_exist = false;
        tag_position = "";
        position = "";
        tv_tag.setText("Tag ID");
        tv_tag_confirmation.setText("Confirm Tag ID");
        rg_vehicle_tags.clearCheck();
        rg_trailer_tags.clearCheck();
        iv_nfcTag_one.setImageResource(R.drawable.nfc);
        iv_nfcTag_two.setImageResource(R.drawable.nfc);
        tv_current_tag.setText("");
        alertDialogSuccess("NFC tag submitted successfully.");
        tv_current_tag.setVisibility(View.INVISIBLE);
    }

    private void getNfcTgsVehicleDetails(){
        Task task1 = vehicleDetailsReference.whereEqualTo("external_vehicle","No").whereEqualTo("vehicle_completion",tv_tag.getText().toString().trim()).whereEqualTo("status","Active").get();
        Task task2 = vehicleDetailsReference.whereEqualTo("external_vehicle","No").whereEqualTo("vehicle_front",tv_tag.getText().toString().trim()).whereEqualTo("status","Active").get();
        Task task3 = vehicleDetailsReference.whereEqualTo("external_vehicle","No").whereEqualTo("vehicle_near_rear",tv_tag.getText().toString().trim()).whereEqualTo("status","Active").get();
        Task task4 = vehicleDetailsReference.whereEqualTo("external_vehicle","No").whereEqualTo("vehicle_off_rear",tv_tag.getText().toString().trim()).whereEqualTo("status","Active").get();
        Task<List<QuerySnapshot>> allTask = Tasks.whenAllSuccess(task1,task2,task3,task4);
        allTask.addOnCompleteListener(new OnCompleteListener<List<QuerySnapshot>>() {
            @Override
            public void onComplete(@NonNull Task<List<QuerySnapshot>> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()){
                        for (QuerySnapshot querySnapshot : task.getResult()){
                            if (!querySnapshot.isEmpty()){
                                vehicle_nfc_tags_exist = true;
                                break;
                            }
                        }
                    }
                  getNfcTgsTrailerDetails();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(NfcTagManager.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getNfcTgsTrailerDetails(){
        Task task1 = trailerDetailsReference.whereEqualTo("trailer_near_rear",tv_tag.getText().toString().trim()).whereEqualTo("status","Active").get();
        Task task2 = trailerDetailsReference.whereEqualTo("trailer_off_rear",tv_tag.getText().toString().trim()).whereEqualTo("status","Active").get();
        Task<List<QuerySnapshot>> allTask = Tasks.whenAllSuccess(task1,task2);
        allTask.addOnCompleteListener(new OnCompleteListener<List<QuerySnapshot>>() {
            @Override
            public void onComplete(@NonNull Task<List<QuerySnapshot>> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()) {
                        for (QuerySnapshot querySnapshot : task.getResult()) {
                            if (!querySnapshot.isEmpty()) {
                                trailer_nfc_tags_exist = true;
                                break;
                            }
                        }
                    }
                    if (vehicle_nfc_tags_exist == true || trailer_nfc_tags_exist == true){
                        alertDialog("NFC Tag already exists.");
                        tv_tag.setText("Tag ID");
                        tv_tag_confirmation.setText("Confirm Tag ID");
                        iv_nfcTag_one.setImageResource(R.drawable.nfc);
                        iv_nfcTag_two.setImageResource(R.drawable.nfc);
                        vehicle_nfc_tags_exist = false;
                        trailer_nfc_tags_exist = false;
                        rg_vehicle_tags.clearCheck();
                        rg_trailer_tags.clearCheck();
                        position = "";
                        tag_position="";
                        tv_current_tag.setVisibility(View.INVISIBLE);
                    }
                    else {
                        if (vehicle_type.equals("trailer"))
                            updateValue();
                         else if(vehicle_type.equals("vehicle"))
                            updateVehicleDetails();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(NfcTagManager.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
