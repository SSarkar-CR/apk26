package com.crate.crateam.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crate.crateam.R;
import com.crate.crateam.adapters.AssetElementsAdapter;
import com.crate.crateam.adapters.AssetRegimeAdapter;
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
import com.google.firebase.Firebase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheIndexManager;
import com.crate.crateam.utility.FirestoreManager;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageKt;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class AssetInspection extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener{
    private FirebaseFirestore db = FirestoreManager.getInstance();
    // Firebase Storage instance
    private FirebaseStorage storage;
    // Reference to the Firebase Storage root directory
    private StorageReference storageReference;
    private CollectionReference assetDetailsReference,assetTypeReference,assetTypeElementReference,
            assetInspectionSubmissionReference,assetInspectionDefectsReference, regimeDetailsReference,assetInspectionRegimeReference,amAssignSiteReference,
            siteLocationReference,assetRegimeReference;
    private Button bt_submit,bt_discard,bt_view_certificates,bt_asset_history,bt_nfc,bt_qr_scan,bt_id_search,bt_inspector_yes,
            bt_inspector_no,bt_inspector_sign;
    private EditText et_inspector_name,et_defect_details,et_value,et_notes;
    private TextView tv_asset_type,tv_asset_id,tv_asset_name,tv_asset_status,tv_current_asset_status,tv_asset_location,tv_inspected_by,tv_last_inspection,
            tv_nfc_tag,tv_clear_sign,tv_value;
    private RadioGroup rg_on_off_value,rg_yes_no_value,rg_layout;
    private RadioButton rb_additional,rb_reported;
    private CustomSearchableSpinner sp_asset_type,sp_asset_details;
    private LinearLayout ll_asset_details,ll_nfc_tag,ll_images,ll_inspector_name,ll_inspector_sign,ll_previous_defect_view;
    private Bitmap getDrawable1, getDrawable2;
    private boolean hasImage = false,hasImageTwo = false,isSigned = false,validation_done = false;
    private RecyclerView rv_asset_elements,rv_asset_regimes;
    private SignaturePad signature_pad_inspector;
    private String asset_type="",asset_number="",asset_name="",tagId,identification_method="",image1="",image2="",
            inspector_login="",inspector_sign="",user_name="",current_date="",current_time="",latitude="",longitude="",address="",
            inspection_id="",defect_inspection="",element_defect="",defected,asset_status="",last_inspection_id="",regime_element_value="",
            regime_defect="",device_id ="",siteLocationName="",last_inspection_user_name="",last_inspection_status="",last_inspection_date="",
            pre_existing_defect="",qrCode="",next_routine_inspection_date="",routine_inspection_duration="",activity="",
            asset_current_status="",site_type="";
    private int assetId = 0,assetTypeId=0,user_id=0,id_count=0,doc_count=0,regimeId=0,regime_id_count=0,regime_doc_count=0,groupId=0,
            siteLocationId=0,last_inspection_user_id=0,last_inspection_location=0,asset_current_location=0;
    private ImageView iv_cross,iv_camera,iv_photo_one,iv_photo_two,iv_close_one,iv_close_two,iv_defect_no,iv_defect_yes,iv_defect_moderate;
    private NfcAdapter mNfcAdapter;
    private AssetElementsAdapter assetElementsAdapter;
    private AssetRegimeAdapter assetRegimeAdapter;
    private ArrayList<String> arraylist_asset_type_name = new ArrayList<>();
    private ArrayList<Integer> arraylist_asset_type_id = new ArrayList<>();
    private ArrayList<String> arraylist_identification_method = new ArrayList<>();
    private ArrayList<Integer> arrayList_current_location  = new ArrayList<>();
    private ArrayList<Integer> arrayList_regime_id  = new ArrayList<>();
    private ArrayList<Integer> arrayList_regime_element_id  = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_name = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_value = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_view = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_value_new = new ArrayList<>();
    private ArrayList<Integer> arrayList_regime_element_id_sorted  = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_name_sorted = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_value_sorted = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_view_sorted = new ArrayList<>();
    private ArrayList<String> arraylist_regime_element_defects = new ArrayList<>();
    private ArrayList<Integer> arraylist_group_id = new ArrayList<>();
    private ArrayList<Integer> arraylist_asset_id = new ArrayList<>();
    private ArrayList<String> arraylist_asset_name = new ArrayList<>();
    private ArrayList<String> arraylist_asset_number = new ArrayList<>();
    private ArrayList<String> arraylist_asset_current_status = new ArrayList<>();
    private ArrayList<String> assetElementsNameList = new ArrayList<>();
    private ArrayList<Integer> assetElementsIdList = new ArrayList<>();
    private ArrayList<String> assetDefectedElementsNameList = new ArrayList<>();
    private ArrayList<String> assetDefectedElementsDefectList = new ArrayList<>();
    private ArrayList<Integer> assetDefectedElementsIdList = new ArrayList<>();
    private ArrayList<String> defects = new ArrayList<>();
    private ArrayList<String> defectComments = new ArrayList<>();
    private ArrayList<String> all_defects = new ArrayList<>();
    private ArrayList<String> preExistingDefects = new ArrayList<>();
    private ArrayList<String> all_defects_comment = new ArrayList<>();
    private ArrayList<String> all_asset_inspection_ids = new ArrayList<>();
    private PopupWindow popupWindow;
    private static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "AssetInspection";
    private static final int REQUEST_CAMERA= 0;
    private Uri imageUri,firebaseUri;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.asset_inspection);
        initView();
    }
    private void initView(){
        progressDialog = Dialog.showProgressDialog(this);
        FirestoreManager.initPersistentIndexManager();
        // Initialize Firebase Storage
//        storage = StorageKt.getStorage(Firebase.INSTANCE);
        storageReference = FirebaseStorage.getInstance().getReference();
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        assetRegimeReference = db.collection("AM_regime");
        assetDetailsReference = db.collection("AM_asset_details");
        assetTypeReference = db.collection("AM_asset_type");
        assetTypeElementReference = db.collection("AM_asset_type_elements");
        assetInspectionSubmissionReference = db.collection("AM_asset_inspection_submission");
        assetInspectionDefectsReference = db.collection("AM_asset_inspection_defects");
        regimeDetailsReference = db.collection("AM_asset_revalidation_details");
        assetInspectionRegimeReference = db.collection("AM_asset_inspection_regimes");
        amAssignSiteReference = db.collection("AM_assign_site");
        siteLocationReference = db.collection("AM_site_location");
        
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        SessionManager sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_name = user.get(SessionManager.KEY_FULL_NAME);
        user_id = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            assetId = extras.getInt("assetId");
            activity = extras.getString("activity");
        }
        ll_asset_details = findViewById(R.id.ll_asset_details);
        ll_images = findViewById(R.id.ll_images);
        ll_inspector_name = findViewById(R.id.ll_inspector_name);
        ll_inspector_sign = findViewById(R.id.ll_inspector_sign);
        signature_pad_inspector = findViewById(R.id.signature_pad_inspector);
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start,
                                       int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.toString(source.charAt(i)).matches("[a-zA-Z0-9., ]+")) {
                        return "";
                    }
                }
                return null;
            }
        };
        et_notes = findViewById(R.id.et_notes);
        et_notes.setFilters(new InputFilter[] { filter });
        et_inspector_name = findViewById(R.id.et_inspector_name);
        et_inspector_name.setFilters(new InputFilter[] { filter });
        tv_asset_type = findViewById(R.id.tv_asset_type);
        tv_asset_id = findViewById(R.id.tv_asset_id);
        tv_asset_name = findViewById(R.id.tv_asset_name);
        tv_asset_status = findViewById(R.id.tv_asset_status);
        tv_current_asset_status = findViewById(R.id.tv_current_asset_status);
        tv_asset_location = findViewById(R.id.tv_asset_location);
        tv_inspected_by = findViewById(R.id.tv_inspected_by);
        tv_last_inspection = findViewById(R.id.tv_last_inspection);
        tv_nfc_tag = findViewById(R.id.tv_nfc_tag);
        tv_clear_sign = findViewById(R.id.tv_clear_sign);
        bt_nfc = findViewById(R.id.bt_nfc);
        bt_id_search = findViewById(R.id.bt_id_search);
        bt_qr_scan = findViewById(R.id.bt_qr_scan);
        bt_inspector_yes = findViewById(R.id.bt_inspector_yes);
        bt_inspector_no = findViewById(R.id.bt_inspector_no);
        bt_inspector_sign = findViewById(R.id.bt_inspector_sign);
        bt_submit = findViewById(R.id.bt_submit);
        bt_discard = findViewById(R.id.bt_discard);
        bt_view_certificates = findViewById(R.id.bt_view_certificates);
        bt_asset_history = findViewById(R.id.bt_asset_history);
        iv_cross = findViewById(R.id.iv_cross);
        iv_camera = findViewById(R.id.iv_camera);
        iv_photo_one = findViewById(R.id.iv_photo_one);
        iv_photo_two = findViewById(R.id.iv_photo_two);
        iv_close_one = findViewById(R.id.iv_close_one);
        iv_close_two = findViewById(R.id.iv_close_two);
        ll_nfc_tag = findViewById(R.id.ll_nfc_tag);
        sp_asset_type = findViewById(R.id.sp_asset_type);
        sp_asset_details = findViewById(R.id.sp_asset_details);
        rv_asset_elements = findViewById(R.id.rv_asset_elements);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rv_asset_elements.setLayoutManager(layoutManager);
        rv_asset_regimes = findViewById(R.id.rv_asset_regimes);
        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(this);
        rv_asset_regimes.setLayoutManager(layoutManager2);
//        if (activity.equals("DashboardListing")) {
//            progressDialog.show();
//            bt_nfc.setVisibility(View.GONE);
//            bt_qr_scan.setVisibility(View.GONE);
//            bt_id_search.setVisibility(View.GONE);
//            getAssetDetailsFromDashboardListing(assetId);
//        }
//        else {
//            bt_nfc.setVisibility(View.VISIBLE);
//            bt_qr_scan.setVisibility(View.VISIBLE);
//            bt_id_search.setVisibility(View.VISIBLE);
//        }
        initializeOnClick();
        checkNfcAdapter();
        setSignInspector();
        createInspectionId();
        dateTime();
        latLon();
        getAssignSiteName();
        device_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        System.out.println("Device Id:" +device_id);
    }

    private void initializeOnClick(){
        bt_nfc.setOnClickListener(this);
        bt_id_search.setOnClickListener(this);
        bt_qr_scan.setOnClickListener(this);
        bt_inspector_yes.setOnClickListener(this);
        bt_inspector_no.setOnClickListener(this);
        bt_inspector_sign.setOnClickListener(this);
        bt_submit.setOnClickListener(this);
        bt_discard.setOnClickListener(this);
        tv_clear_sign.setOnClickListener(this);
        iv_cross.setOnClickListener(this);
        iv_camera.setOnClickListener(this);
        iv_close_one.setOnClickListener(this);
        iv_close_two.setOnClickListener(this);
        iv_photo_one.setOnClickListener(this);
        iv_photo_two.setOnClickListener(this);
        bt_view_certificates.setOnClickListener(this);
        bt_asset_history.setOnClickListener(this);
        sp_asset_type.setOnItemSelectedListener(this);
        sp_asset_details.setOnItemSelectedListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_nfc:
                if (ll_asset_details.getVisibility()==View.VISIBLE)
                    ll_asset_details.setVisibility(View.GONE);
                sp_asset_type.setVisibility(View.GONE);
                sp_asset_details.setVisibility(View.GONE);
                ll_nfc_tag.setVisibility(View.VISIBLE);
                ll_nfc_tag.setBackgroundResource(R.drawable.nfc_bg_focus);
                tv_nfc_tag.setText("");
                break;
            case R.id.bt_id_search:
                if (ll_asset_details.getVisibility()==View.VISIBLE)
                    ll_asset_details.setVisibility(View.GONE);
                sp_asset_type.setVisibility(View.VISIBLE);
                sp_asset_details.setVisibility(View.VISIBLE);
                ll_nfc_tag.setVisibility(View.GONE);
                getAssetTypes();
                break;
            case R.id.bt_qr_scan:
                if (ll_asset_details.getVisibility()==View.VISIBLE)
                    ll_asset_details.setVisibility(View.GONE);
                sp_asset_type.setVisibility(View.GONE);
                sp_asset_details.setVisibility(View.GONE);
                ll_nfc_tag.setVisibility(View.GONE);
                scanQR();
                break;
            case R.id.bt_discard:
            case R.id.iv_cross:
                alertDialogWithYesNo("Are you sure you'd like to close this form ? Any unsaved data will be lost.");
                break;
            case R.id.iv_camera:
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "New Picture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                imageUri = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, REQUEST_CAMERA);
                ll_images.setVisibility(View.VISIBLE);
                break;
            case R.id.iv_photo_one:
                getDrawable1 = ((BitmapDrawable) iv_photo_one.getDrawable()).getBitmap();
                zoomImagePopup(getDrawable1);
                break;
            case R.id.iv_photo_two:
                getDrawable2 = ((BitmapDrawable) iv_photo_two.getDrawable()).getBitmap();
                zoomImagePopup(getDrawable2);
                break;
            case R.id.iv_close_one:
                iv_photo_one.setImageDrawable(getResources().getDrawable(R.drawable.placeholder_image));
                hasImage = false;
                break;
            case R.id.iv_close_two:
                iv_photo_two.setImageDrawable(getResources().getDrawable(R.drawable.placeholder_image));
                hasImage = true;
                hasImageTwo = false;
                break;
            case R.id.bt_view_certificates:
                Intent to_view_certificates = new Intent(AssetInspection.this,AssetCertificates.class);
                startActivity(to_view_certificates);
                break;
            case R.id.bt_asset_history:
                Intent to_asset_history = new Intent(AssetInspection.this,AssetHistory.class);
                startActivity(to_asset_history);
                break;
            case R.id.bt_inspector_yes:
                inspector_login = "Yes";
                setSelectedButtonBackground(bt_inspector_yes);
                setDefaultButtonBackground(bt_inspector_no);
                ll_inspector_name.setVisibility(View.GONE);
                break;
            case R.id.bt_inspector_no:
                inspector_login = "No";
                setSelectedButtonBackground(bt_inspector_no);
                setDefaultButtonBackground(bt_inspector_yes);
                ll_inspector_name.setVisibility(View.VISIBLE);
                break;
            case R.id.bt_inspector_sign:
                ll_inspector_sign.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_clear_sign:
                signature_pad_inspector.clear();
                Toast.makeText(this, "Sign Cleared", Toast.LENGTH_SHORT).show();
                break;
            case R.id.bt_submit:
                if (checkValidation()){
                    progressDialog.show();
                    sendAssetElementData();
                }
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

    private void setSelectedButtonBackground(Button selected_button) {
        selected_button.setBackground(getResources().getDrawable(R.drawable.login_button_background));
        selected_button.setTextColor(getResources().getColor(R.color.white));
    }

    private void setDefaultButtonBackground(Button default_button) {
        default_button.setBackground(getResources().getDrawable(R.drawable.edit_text_background));
        default_button.setTextColor(getResources().getColor(R.color.black_shade_two));
    }

    private boolean checkValidation() {
        if (isSigned) {
                Bitmap signatureBitmap = signature_pad_inspector.getSignatureBitmap();
                inspector_sign = convertTOBase64Image(signatureBitmap);
        }
        if (image1.isEmpty())
            Dialog.alertDialog(this, "Please take a photo.");
        else if (inspector_login.isEmpty())
            Dialog.alertDialog(this, "Please select inspector logged in option.");
        else if (inspector_login.equals("No") && et_inspector_name.getText().toString().isEmpty())
            Dialog.alertDialog(this, "Please enter inspector name.");
        else if (!isSigned)
            Dialog.alertDialog(this, "Inspector sign mandatory.");
        else
            validation_done = true;
        return validation_done;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_asset_type:
                if (ll_asset_details.getVisibility()==View.VISIBLE)
                    ll_asset_details.setVisibility(View.GONE);
                assetTypeId = arraylist_asset_type_id.get(position);
                identification_method = arraylist_identification_method.get(position);
                getAssetDetailsByAssetTypeId(assetTypeId);
                getAssetTypeName(assetTypeId);
                getAssetElements(assetTypeId);
                break;
            case R.id.sp_asset_details:
                ll_asset_details.setVisibility(View.VISIBLE);
                regimeId = arrayList_regime_id.get(position);
                groupId = arraylist_group_id.get(position);
                assetId = arraylist_asset_id.get(position);
                asset_number = arraylist_asset_number.get(position);
                asset_name = arraylist_asset_name.get(position);
                asset_current_status = arraylist_asset_current_status.get(position);
                asset_current_location = arrayList_current_location.get(position);
                tv_asset_id.setText(asset_number);
                tv_asset_name.setText(asset_name);
                tv_current_asset_status.setText(asset_current_status);
                setAssetStatusColour(asset_current_status);
                getAssetDetailsFromAssetInspection(assetId);
                Log.d(TAG,"RTFYJT :" +assetId+" "+regimeId);
                getAssetRegimes(assetId);
                fetchDefectedElements(assetId);
                getRegimeInspectionDuration(regimeId);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, dataIntent);
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG,"RDYFJF " +intentResult.getContents());
                qrCode = intentResult.getContents();
                getAssetDetailsByQR();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, dataIntent);
        }
        switch (requestCode) {
            case REQUEST_CAMERA:
                if(resultCode == Activity.RESULT_OK){
                    try {
                        Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(
                                getContentResolver(), imageUri);
//                        firebaseUri = dataIntent.getData();
//                        Log.d(TAG,"VHGjk :" +firebaseUri);
//                        storeImagesToFireBaseStorage(firebaseUri);
                        Matrix rotationMatrix = new Matrix();
                        if(imageBitmap.getWidth() >= imageBitmap.getHeight()){
                            rotationMatrix.setRotate(90);
                        }else{
                            rotationMatrix.setRotate(0);
                        }
                        Bitmap rotatedBitmap = Bitmap.createBitmap(imageBitmap,0,0,imageBitmap.getWidth(),
                                imageBitmap.getHeight(),rotationMatrix,true);
                        if (rotatedBitmap != null) {
                            if (!hasImage && !hasImageTwo) {
                                iv_photo_one.setImageBitmap(rotatedBitmap);
                                image1 = convertTOBase64Image(getResizedBitmap(rotatedBitmap,700));
                                hasImage = true;
                            } else if (hasImage && !hasImageTwo) {
                                iv_photo_two.setImageBitmap(rotatedBitmap);
                                image2 = convertTOBase64Image(getResizedBitmap(rotatedBitmap,700));
                                hasImageTwo = false;
                                hasImage = false;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

        }
    }

    private void getAssetDetailsByQR(){
        Query query = assetDetailsReference.whereEqualTo("status", "Active").whereEqualTo("qr_code",qrCode);
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            assetId = Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
                            asset_name = queryDocumentSnapshot.getString("asset_name");
                            asset_number = queryDocumentSnapshot.getString("asset_number");
                            regimeId = Objects.requireNonNull(queryDocumentSnapshot.getLong("regime_id")).intValue();
                            assetTypeId = Objects.requireNonNull(queryDocumentSnapshot.getLong("asset_type_id")).intValue();
                            asset_current_status = queryDocumentSnapshot.getString("asset_current_status");
                            asset_current_location = Objects.requireNonNull(queryDocumentSnapshot.getLong("assign_location")).intValue();
                        }
                        ll_asset_details.setVisibility(View.VISIBLE);
                        getAssetTypeName(assetTypeId);
                        getAssetElements(assetTypeId);
                        tv_asset_name.setText(asset_name);
                        tv_asset_id.setText(asset_number);
                        tv_current_asset_status.setText(asset_current_status);
                        setAssetStatusColour(asset_current_status);
                        getAssetDetailsFromAssetInspection(assetId);
                        getAssetRegimes(assetId);
                        fetchDefectedElements(assetId);
                        getRegimeInspectionDuration(regimeId);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetInspection.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createInspectionId(){
        @SuppressLint("HardwareIds") String uniqueID = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        String currentDateAndTime = sdf.format(new Date());
        System.out.println("UUID :" +uniqueID+currentDateAndTime);
        String inspection_id_first = uniqueID.substring(0,4);
        String inspection_id_last = currentDateAndTime;
        inspection_id =  inspection_id_first+inspection_id_last;
    }

    private void dateTime() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        current_date = simpleDateFormat.format(date);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
        current_time = time_format.format(calendar.getTime());
    }

    private void latLon(){
        GPSTracker finder = new GPSTracker(AssetInspection.this);
        Double lat = 0.0, lon = 0.0;
        if (!finder.checkLocationPermission()) {
            finder.requestPermission();
        }
        if (finder.canGetLocation()) {
            lat = finder.getLatitude();
            lon = finder.getLongitude();
            double latOnline = round(lat, 4);
            double lonOnline = round(lon, 4);
            latitude = String.valueOf(latOnline);
            longitude = String.valueOf(lonOnline);
            address = finder.getAddress(lat,lon);
        }
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void setSignInspector() {
            signature_pad_inspector.setOnSignedListener(new SignaturePad.OnSignedListener() {
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

    private void getAssignSiteName(){
        Query query = amAssignSiteReference.whereEqualTo("user_id", user_id).whereEqualTo("date", current_date);
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            siteLocationId = Objects.requireNonNull(queryDocumentSnapshot.getLong("site_id")).intValue();
                            siteLocationName= queryDocumentSnapshot.getString("site_name");
                        }
                        Log.d(TAG,"Site Location Id :" +siteLocationId);
                        fetchStorageLocation(siteLocationId);
                    }else
                        alertDialogAssignSite("You are not assigned with any site.Please assign yourself with a site.");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AssetInspection.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchStorageLocation(int siteLocationId){
        Query query = siteLocationReference.whereEqualTo("status", "Active").whereEqualTo("id",siteLocationId);
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            site_type  = queryDocumentSnapshot.getString("site_type");
                        }
                        Log.d(TAG,"Site Type :" +site_type);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetInspection.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void alertDialogAssignSite(String message){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent assignSite = new Intent(AssetInspection.this,AssetAssignSiteActivity.class);
                        startActivity(assignSite);
                        finish();
                    }
                });
        alertDialog.show();
    }

    private void getAssetTypes() {
        Query query = assetTypeReference.whereEqualTo("status", "Active").orderBy("asset_type_name")
                .whereEqualTo("identification_method","UID");
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    arraylist_asset_type_name.clear();
                    arraylist_asset_type_id.clear();
                    arraylist_identification_method.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        arraylist_asset_type_name.add(queryDocumentSnapshot.getString("asset_type_name"));
                        arraylist_asset_type_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                        arraylist_identification_method.add(queryDocumentSnapshot.getString("identification_method"));
                    }
                    Log.d(TAG,"Asset Name ID:" +arraylist_asset_type_name+" "+arraylist_asset_type_id);
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(AssetInspection.this, R.layout.spinner_custom_layout, arraylist_asset_type_name);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_asset_type.setAdapter(spinnerArrayAdapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetInspection.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAssetDetailsByAssetTypeId(int asset_type_id){
        Query query = assetDetailsReference.whereEqualTo("status", "Active")
                .whereEqualTo("asset_type_id",asset_type_id).orderBy("asset_name");
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    arraylist_asset_id.clear();
                    arraylist_asset_name.clear();
                    arraylist_asset_number.clear();
                    arraylist_asset_current_status.clear();
                    arrayList_regime_id.clear();
                    arraylist_group_id.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        arraylist_asset_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                        arraylist_asset_name.add(queryDocumentSnapshot.getString("asset_name"));
                        arraylist_asset_number.add(queryDocumentSnapshot.getString("asset_number"));
                        arraylist_asset_current_status.add(queryDocumentSnapshot.getString("asset_current_status"));
                        arrayList_regime_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("regime_id")).intValue());
                        arrayList_current_location.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("assign_location")).intValue());
                        arraylist_group_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("asset_group_id")).intValue());
                    }
                    ArrayList<String> combinedList = new ArrayList<>();
                    for (int i=0;i<arraylist_asset_id.size();i++){
                        combinedList.add(arraylist_asset_name.get(i)+" , "+arraylist_asset_number.get(i));
                    }
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(AssetInspection.this, R.layout.spinner_custom_layout, combinedList);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_asset_details.setAdapter(spinnerArrayAdapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetInspection.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAssetDetailsFromAssetInspection(int assetId){
        Query query = assetInspectionSubmissionReference.whereEqualTo("status", "Active").whereEqualTo("asset_id",assetId)
                .whereEqualTo("user_role","Other")
                .orderBy("time_second_format",Query.Direction.DESCENDING);
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    all_asset_inspection_ids.clear();
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            all_asset_inspection_ids.add(queryDocumentSnapshot.getString("inspection_id"));
                        }
                        last_inspection_id = all_asset_inspection_ids.get(0);
                        Log.d(TAG,"YUGKY :" +last_inspection_id);
                        fetchLastInspectionDetails(last_inspection_id);
                    }else {
                        tv_last_inspection.setText("");
                        tv_asset_status.setText("");
                        tv_inspected_by.setText("");
                        tv_asset_location.setText("");
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetInspection.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchLastInspectionDetails(String inspection_id){
        Query query = assetInspectionSubmissionReference.whereEqualTo("status", "Active").whereEqualTo("inspection_id",inspection_id)
                .whereEqualTo("user_role","Other");
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            last_inspection_user_id = Objects.requireNonNull(queryDocumentSnapshot.getLong("user_id")).intValue();
                            if (Objects.requireNonNull(queryDocumentSnapshot.getString("inspector_name")).equals(""))
                                last_inspection_user_name = queryDocumentSnapshot.getString("user_name");
                            else
                                last_inspection_user_name = queryDocumentSnapshot.getString("inspector_name");
                            last_inspection_status = queryDocumentSnapshot.getString("asset_status");
                            last_inspection_location = queryDocumentSnapshot.getLong("assign_location").intValue();
                            last_inspection_date = queryDocumentSnapshot.getString("conducted_on");
                        }
                        tv_inspected_by.setText(last_inspection_user_name);
                        if (last_inspection_status.equals("Defected not safe to use."))
                            tv_asset_status.setTextColor(Color.RED);
                        else if (last_inspection_status.equals("Defected but safe to use."))
                            tv_asset_status.setTextColor(Color.parseColor("#FFA500"));
                        else if (last_inspection_status.equals("Good working order."))
                            tv_asset_status.setTextColor(Color.GREEN);
                        tv_asset_status.setText(last_inspection_status);
                        tv_last_inspection.setText(last_inspection_date);
                        getLastInspectionLocationName(last_inspection_location);
                    }

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetInspection.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLastInspectionLocationName(int site_id){
        Query query = siteLocationReference.whereEqualTo("id", site_id);
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            String siteName= queryDocumentSnapshot.getString("site_name");
                            Log.d(TAG,"SITE Name : " + siteName);
                            tv_asset_location.setText(siteName);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetInspection.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAssetTypeName(int assetTypeId) {
        Query query = assetTypeReference.whereEqualTo("status", "Active").whereEqualTo("id",assetTypeId);
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                Toast.makeText(AssetInspection.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getRegimeInspectionDuration(int regime_id) {
        Query query = assetRegimeReference.whereEqualTo("status", "Active").whereEqualTo("id",regime_id);
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        routine_inspection_duration = queryDocumentSnapshot.getString("inspection_duration");
                    }
                    nextRoutineInspectionDate(current_date,routine_inspection_duration);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetInspection.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void nextRoutineInspectionDate(String start_routine_inspection_date,String routine_inspection_duration){
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date = null;
        try {
            date = formatter.parse(start_routine_inspection_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String dater = formatter.format(date);
        int duration = Integer.parseInt(routine_inspection_duration);
        Date dateNew = new Date(date.getYear(), date.getMonth(), date.getDate() +duration);
        next_routine_inspection_date = formatter.format(dateNew);
        Log.d(TAG,"TRRYT :" +formatter.format(dateNew));
    }

    private void getAssetRegimes(int asset_details_id){
        Query query = regimeDetailsReference.whereEqualTo("status", "Active").whereEqualTo("asset_details_id",asset_details_id)
                .orderBy("time_second_format", Query.Direction.DESCENDING);
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            Log.d(TAG,"RDRYDYT :" + task.getResult());
                            arraylist_regime_element_name.add(queryDocumentSnapshot.getString("regime_element_name"));
                            arrayList_regime_element_id.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("regime_element_id")).intValue());
                            arraylist_regime_element_value.add(queryDocumentSnapshot.getString("regime_element_value"));
                            arraylist_regime_element_view.add(queryDocumentSnapshot.getString("regime_element_view"));
                        }
                    }else {
                        Dialog.alertDialog(AssetInspection.this,"Unable to fetch data.");
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
                    Log.d(TAG,"htfjht :" +arraylist_regime_element_value_sorted+" "+arrayList_regime_element_id_sorted);
                    assetRegimeAdapter = new AssetRegimeAdapter(arraylist_regime_element_name_sorted,arraylist_regime_element_view_sorted,arraylist_regime_element_value_sorted);
                    rv_asset_regimes.setAdapter(assetRegimeAdapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetInspection.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAssetElements(int asset_type_id){
        Query query = assetTypeElementReference.whereEqualTo("status", "Active").whereEqualTo("asset_type_id",asset_type_id);
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    assetElementsNameList.clear();
                    assetElementsIdList.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        assetElementsNameList.add(queryDocumentSnapshot.getString("element_name"));
                        assetElementsIdList.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                    }
                    assetElementsAdapter = new AssetElementsAdapter(assetElementsNameList,assetElementsIdList,assetDefectedElementsIdList
                                                                   ,assetDefectedElementsDefectList);
                    rv_asset_elements.setAdapter(assetElementsAdapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetInspection.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchDefectedElements(int assetId){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Task task1 =  assetInspectionDefectsReference.whereEqualTo("status", "Active").whereEqualTo("asset_id",assetId)
                .whereEqualTo("defected","Yes").orderBy("time_second_format",Query.Direction.DESCENDING).get(source);
        Task task2 =  assetInspectionDefectsReference.whereEqualTo("status", "Active").whereEqualTo("asset_id",assetId)
                .whereEqualTo("defected","Yes but safe").orderBy("time_second_format",Query.Direction.DESCENDING).get(source);
        Task<List<QuerySnapshot>> allTask = Tasks.whenAllSuccess(task1,task2);
        allTask.addOnSuccessListener(new OnSuccessListener<List<QuerySnapshot>>() {
            @Override
            public void onSuccess(List<QuerySnapshot> querySnapshots) {
                assetDefectedElementsNameList.clear();
                assetDefectedElementsIdList.clear();
                assetDefectedElementsDefectList.clear();
                for (QuerySnapshot queryDocumentSnapshot : querySnapshots) {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshot) {
                        assetDefectedElementsNameList.add(documentSnapshot.getString("element_name"));
                        assetDefectedElementsIdList.add(Objects.requireNonNull(documentSnapshot.getLong("element_id")).intValue());
                        assetDefectedElementsDefectList.add(documentSnapshot.getString("element_defect"));
                    }
                }
                Log.d(TAG,"RTR :" + assetElementsIdList + " " + assetDefectedElementsIdList + " " + assetDefectedElementsDefectList);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AssetInspection.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendAssetInspectionData(){
        Log.d(TAG,"RRRRR 1:" +asset_status);
        Bitmap signatureBitmap = signature_pad_inspector.getSignatureBitmap();
        inspector_sign = convertTOBase64Image(signatureBitmap);
        final Map<String,Object> AssetInspection = new HashMap<>();
        AssetInspection.put("user_id",user_id);
        AssetInspection.put("user_name",user_name);
        AssetInspection.put("asset_id",assetId);
        AssetInspection.put("asset_type_id",assetTypeId);
        AssetInspection.put("regime",regimeId);
        AssetInspection.put("asset_number",asset_number);
        AssetInspection.put("asset_name",asset_name);
        AssetInspection.put("assign_location", siteLocationId);
        AssetInspection.put("inspection_id",inspection_id);
        AssetInspection.put("conducted_on",current_date);
        AssetInspection.put("submission_time",current_time);
        AssetInspection.put("id",inspection_id);
        AssetInspection.put("z_image_one",image1);
        AssetInspection.put("z_image_two",image2);
        AssetInspection.put("defected",defect_inspection);
        AssetInspection.put("defect_inspection",defect_inspection);
        AssetInspection.put("asset_status",asset_status);
        if (defected.equals("Yes"))
            AssetInspection.put("asset_status_wm","No");
        else
            AssetInspection.put("asset_status_wm","");
        AssetInspection.put("user_role","Other");
        AssetInspection.put("inspector_name",et_inspector_name.getText().toString());
        AssetInspection.put("z_inspector_sign",inspector_sign);
        AssetInspection.put("status","Active");
        AssetInspection.put("latitude",latitude);
        AssetInspection.put("longitude",longitude);
        AssetInspection.put("address",address);
        AssetInspection.put("time_second_format", AppData.getTimeSecond());
        AssetInspection.put("device_id",device_id);
        AssetInspection.put("assign_user_status","No");
        AssetInspection.put("site_type",site_type);
        AssetInspection.put("next_routine_inspection_date",next_routine_inspection_date);
        AssetInspection.put("observation",et_notes.getText().toString());
        assetInspectionSubmissionReference.document(inspection_id).set(AssetInspection).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                toConfirmation("Form submitted successfully.");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AssetInspection.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendAssetElementData(){
        defects.clear();
        defectComments.clear();
        all_defects_comment.clear();
        preExistingDefects.clear();
        for (int i = 0; i < assetElementsAdapter.getItemCount(); i++) {
            View view1 = rv_asset_elements.getChildAt(i);
            if (view1 != null) {
                iv_defect_yes = view1.findViewById(R.id.iv_defect_yes);
                iv_defect_no = view1.findViewById(R.id.iv_defect_no);
                iv_defect_moderate = view1.findViewById(R.id.iv_defect_moderate);
                et_defect_details = view1.findViewById(R.id.et_defect_details);
                ll_previous_defect_view = view1.findViewById(R.id.ll_previous_defect_view);
                rg_layout = view1.findViewById(R.id.rg_layout);
                rb_additional = view1.findViewById(R.id.rb_additional);
                rb_reported = view1.findViewById(R.id.rb_reported);
            }
            if (iv_defect_no.getTag().equals("Defected")){
                defected = "Yes";
                defect_inspection = "Yes";
                element_defect = et_defect_details.getText().toString();
                defects.add(defected);
                if (!element_defect.equals(""))
                    defectComments.add(element_defect);
                if (ll_previous_defect_view.getVisibility()==View.VISIBLE) {
                    if (rb_additional.isChecked()) {
                        pre_existing_defect = "No";
                    }else if (rb_reported.isChecked()) {
                        pre_existing_defect = "Yes";
                    }
                }
            }else if (iv_defect_yes.getTag().equals("Not Defected")){
                defected = "No";
                element_defect="";
                defect_inspection = "No";
                pre_existing_defect = "";
            }else if (iv_defect_moderate.getTag().equals("Moderate")){
                defected = "Yes but safe";
                defect_inspection = "Yes";
                pre_existing_defect = "";
                element_defect = et_defect_details.getText().toString();
                defects.add(defected);
                if (!element_defect.equals(""))
                    defectComments.add(element_defect);
            }else {
                defected ="";
            }
            all_defects.add(defected);
            preExistingDefects.add(pre_existing_defect);
            Log.d(TAG,"Defects :" +all_defects.size()+"  "+preExistingDefects);
            if (defects.size()==defectComments.size())
                all_defects_comment.add(element_defect);

            if (!defectComments.isEmpty())
                defect_inspection="Yes";
            else
                defect_inspection ="No";
        }
        if (all_defects.contains("Yes"))
            asset_status = "Defected not safe to use.";
        else if (all_defects.contains("Yes but safe") && !all_defects.contains("Yes"))
            asset_status = "Defected but safe to use.";
        else
            asset_status = "Good working order.";
        if (defects.size()>defectComments.size()) {
            Dialog.alertDialog(this, "Please give comments");
            progressDialog.dismiss();
        }else if (assetElementsNameList.size()> all_defects.size() && all_defects.contains("")) {
            Dialog.alertDialog(this, "Please select all elements defect.");
            progressDialog.dismiss();
        }else if ( all_defects.contains("")) {
            Dialog.alertDialog(this, "Please select all elements defect.");
            all_defects.clear();
            progressDialog.dismiss();
        }else {
            sendRegimeElementsData();
        }
    }

    private void sendRegimeElementsData(){
        arraylist_regime_element_defects.clear();
        arraylist_regime_element_value_new.clear();
        for (int i = 0; i < arraylist_regime_element_name_sorted.size(); i++) {
            View view1 = rv_asset_regimes.getChildAt(i);
            Log.d(TAG,"RYTJ :" +view1);
            tv_value = view1.findViewById(R.id.tv_value);
            et_value = view1.findViewById(R.id.et_value);
            rg_on_off_value = view1.findViewById(R.id.rg_value_on_off);
            rg_yes_no_value = view1.findViewById(R.id.rg_value_yes_no);
            Log.d(TAG,"RYTJ :" +arraylist_regime_element_view_sorted);
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
            Log.d(TAG,"RTTY :" +arraylist_regime_element_value_new);
            if (arraylist_regime_element_view_sorted.get(i).equals("EditText") ){
                if (!arraylist_regime_element_value_new.get(i).equals("")) {
                    int value_one = Integer.parseInt(arraylist_regime_element_value_new.get(i).trim());
                    int value_two = Integer.parseInt(arraylist_regime_element_value_sorted.get(i).trim());
                    if (value_one > value_two)
                        regime_defect = "Yes";
                    else
                        regime_defect = "No";
                }else {
                    regime_defect = "";
                }
            }else if (arraylist_regime_element_view_sorted.get(i).equals("RadioButtonYesNo")){
                if (!arraylist_regime_element_value_new.get(i).equals(arraylist_regime_element_value_sorted.get(i)))
                    regime_defect = "Yes";
                else
                    regime_defect = "No";
            }else if (arraylist_regime_element_view_sorted.get(i).equals("RadioButtonOnOff")){
                if (!arraylist_regime_element_value_new.get(i).equals(arraylist_regime_element_value_sorted.get(i)))
                    regime_defect = "Yes";
                else
                    regime_defect = "No";
            }else if (arraylist_regime_element_view_sorted.get(i).equals("Text")){
                regime_defect = "No";
            }else if (arraylist_regime_element_view_sorted.get(i).equals("TextView")){
                regime_defect = "No";
            }
            arraylist_regime_element_defects.add(regime_defect);
        }
        Log.d(TAG,"ZFDFD :" +arraylist_regime_element_defects+" "+arraylist_regime_element_value_new);
        if (arraylist_regime_element_defects.contains("Yes")) {
            defect_inspection = "Yes";
            asset_status = "Defected not safe to use.";
        }
        if (arraylist_regime_element_value_new.contains("")){
            alertDialog("Please give safety critical values.");
            progressDialog.dismiss();
        }else {
            sendElementsDefects();
            sendRegimeDefects();
            System.out.println("RRRRR 1:" +asset_status);
            sendAssetInspectionData();
            updateAssetDetailsLocation();
            if (!AppData.internetOnline(AssetInspection.this))
                toConfirmation("Form submitted successfully.");
        }
    }

    private void sendRegimeDefects(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        for (int i=0; i< assetRegimeAdapter.getItemCount(); i++) {
            final Map<String, Object> regimeDefect = new HashMap<>();
            regimeDefect.put("regime_id", arrayList_regime_element_id_sorted.get(i));
            regimeDefect.put("inspection_id", inspection_id);
            regimeDefect.put("asset_id",assetId);
            regimeDefect.put("asset_type_id",assetTypeId);
            regimeDefect.put("assign_location", siteLocationId);
            regimeDefect.put("regime", regimeId);
            regimeDefect.put("regime_name", arraylist_regime_element_name_sorted.get(i));
            regimeDefect.put("regime_value", arraylist_regime_element_value_new.get(i));
            regimeDefect.put("regime_value_wm","");
            regimeDefect.put("regime_view", arraylist_regime_element_view_sorted.get(i));
            regimeDefect.put("conducted_on", current_date);
            regimeDefect.put("defected", arraylist_regime_element_defects.get(i));
            regimeDefect.put("user_role","Other");
            regimeDefect.put("submission_time", current_time);
            regimeDefect.put("id", inspection_id + "_" + regime_id_count++);
            regimeDefect.put("status", "Active");
            regimeDefect.put("time_second_format", AppData.getTimeSecond());

            Query query = assetInspectionRegimeReference.whereEqualTo("inspection_id", inspection_id);
            query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        assetInspectionRegimeReference.document(inspection_id + "_" + regime_doc_count++).set(regimeDefect);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(AssetInspection.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void sendElementsDefects(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        for (int i=0; i< assetElementsAdapter.getItemCount(); i++) {
            final Map<String, Object> elementsDefect = new HashMap<>();
            elementsDefect.put("element_id", assetElementsIdList.get(i));
            elementsDefect.put("inspection_id", inspection_id);
            elementsDefect.put("asset_id",assetId);
            elementsDefect.put("asset_type_id",assetTypeId);
            elementsDefect.put("assign_location", siteLocationId);
            elementsDefect.put("regime", regimeId);
            elementsDefect.put("element_name", assetElementsNameList.get(i));
            elementsDefect.put("element_defect", all_defects_comment.get(i));
            elementsDefect.put("defect_comment","");
            elementsDefect.put("z_image_one","");
            elementsDefect.put("z_image_two","");
            elementsDefect.put("wm_sign","");
            elementsDefect.put("conducted_on", current_date);
            elementsDefect.put("defected", all_defects.get(i));
            elementsDefect.put("pre_existing_defect", preExistingDefects.get(i));
            elementsDefect.put("submission_time", current_time);
            elementsDefect.put("user_role","Other");
            elementsDefect.put("id", inspection_id + "_" + id_count++);
            elementsDefect.put("status", "Active");
            elementsDefect.put("time_second_format", AppData.getTimeSecond());

            Query query = assetInspectionDefectsReference.whereEqualTo("inspection_id", inspection_id);
            query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        assetInspectionDefectsReference.document(inspection_id + "_" + doc_count++).set(elementsDefect);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(AssetInspection.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateAssetDetailsLocation(){
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
                            update_asset_details.put("assign_location",siteLocationId);
                            update_asset_details.put("last_inspection_date",current_date);
                            update_asset_details.put("previous_assign_location",asset_current_location);
                            update_asset_details.put("next_routine_inspection_date",next_routine_inspection_date);
                            update_asset_details.put("asset_inspection_status",asset_status);
                            update_asset_details.put("last_inspected_user_id",user_id);
                            assetDetailsReference.document(doc_id).update(update_asset_details);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetInspection.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
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
                Log.d(TAG,"NFC_SERIAL_NO :" +tagId);
                ndefmessage(tag);
                Log.d("NDEF_Message", "Detected: " +ndefmessage(tag));
            } else {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                tagId = ByteArrayToHexString(Objects.requireNonNull(tag).getId());
                Log.d(TAG,"NFC_SERIAL_NO :" +tagId);
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
            e.printStackTrace();
        }
        return null;
    }

    private void getAssetDetailsByNFC(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = assetDetailsReference.whereEqualTo("status", "Active").whereEqualTo("nfc_code",tagId);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            assetId = Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
                            asset_name = queryDocumentSnapshot.getString("asset_name");
                            asset_number = queryDocumentSnapshot.getString("asset_number");
                            regimeId = Objects.requireNonNull(queryDocumentSnapshot.getLong("regime_id")).intValue();
                            assetTypeId = Objects.requireNonNull(queryDocumentSnapshot.getLong("asset_type_id")).intValue();
                            asset_current_status = queryDocumentSnapshot.getString("asset_current_status");
                            asset_current_location = Objects.requireNonNull(queryDocumentSnapshot.getLong("assign_location")).intValue();
                        }
                        successfullyScanDialog();
                        ll_asset_details.setVisibility(View.VISIBLE);
                        tv_nfc_tag.setText(tagId);
                        getAssetTypeName(assetTypeId);
                        getAssetElements(assetTypeId);
                        tv_asset_name.setText(asset_name);
                        tv_asset_id.setText(asset_number);
                        tv_current_asset_status.setText(asset_current_status);
                        setAssetStatusColour(asset_current_status);
                        ll_nfc_tag.setBackgroundColor(Color.parseColor("#00CC66"));
                        getAssetDetailsFromAssetInspection(assetId);
                        getAssetRegimes(assetId);
                        fetchDefectedElements(assetId);
                        getRegimeInspectionDuration(regimeId);
                    }else {
                        onWrongTagDetection();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AssetInspection.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setAssetStatusColour(String asset_current_status){
        if (asset_current_status.equals("Defected not safe to use."))
            tv_current_asset_status.setTextColor(Color.RED);
        else if (asset_current_status.equals("Defected but safe to use."))
            tv_current_asset_status.setTextColor(Color.parseColor("#FFA500"));
        else if (asset_current_status.equals("Good working order."))
            tv_current_asset_status.setTextColor(Color.GREEN);
    }

    private void onWrongTagDetection(){
        Dialog.alertDialog(AssetInspection.this, "Tag not recognised. Please scan again.");
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

    private String convertTOBase64Image(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.NO_WRAP);
        return encoded;
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void toConfirmation(String message) {
        progressDialog.dismiss();
        final View popupView = getLayoutInflater().inflate(R.layout.change_pass_popup_layout, null, true);
        boolean isActivityInForeground = AssetInspection.this.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED);
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

    private void alertDialogWithYesNo(String message){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(message);
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
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
    private void storeImagesToFireBaseStorage(Uri uri){
        // Create a unique path under 'images/' using UUID
        StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());
        ref.putFile(uri) .addOnSuccessListener(taskSnapshot -> {
                    // Dismiss the dialog and show success message
                    progressDialog.dismiss();
                    Toast.makeText(AssetInspection.this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Dismiss the dialog and show failure message
                    progressDialog.dismiss();
                    Toast.makeText(AssetInspection.this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnProgressListener(taskSnapshot -> {
                    // Calculate and update progress percentage in the dialog
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                });

    }

}