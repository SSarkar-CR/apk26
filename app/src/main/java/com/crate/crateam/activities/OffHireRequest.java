package com.crate.crateam.activities;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;

import com.crate.crateam.R;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.CustomSearchableSpinner;
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

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class OffHireRequest extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference assetDetailsReference,offHireReference,siteLocationReference,amAssignSiteReference;
    private TextView tv_asset_type,tv_asset_id,tv_asset_name;
    private Button bt_submit,bt_back;
    private ImageView iv_camera,iv_photo_one,iv_photo_two,iv_close_one,iv_close_two;
    private int assetId=0,userId=0,assignLocationId=0,storage_location=0,siteLocationId=0,offHireId=0;
    private String asset_type="",asset_number="",asset_name="", currentDate="",currentTime="",
            maxDocIdOffHire="",image1="",image2="",off_hire="",storage_location_name="";
    private LinearLayout ll_images;
    private CustomSearchableSpinner sp_storage_location;
    private Bitmap getDrawable1, getDrawable2;
    private boolean hasImage = false,hasImageTwo = false;
    private CheckBox cb_off_hire_caution;
    private boolean isValidationDone = false;
    private ArrayList<Integer> offHireIds = new ArrayList<>();
    private ArrayList<Integer> storageLocationId = new ArrayList<>();
    private ArrayList<String> storageLocation = new ArrayList<>();
    private ProgressDialog progressDialog;
    private PopupWindow popupWindow;
    private static final int REQUEST_CAMERA= 0;
    private Uri imageUri;

    public static final String TAG = "OffHire";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.asset_off_hire);
        initView();
        initializeOnClick();
    }
    private void initView(){
        FirestoreManager.initPersistentIndexManager();
        progressDialog = Dialog.showProgressDialog(this);
        assetDetailsReference = db.collection("AM_asset_details");
        offHireReference = db.collection("AM_asset_off_hire");
        siteLocationReference = db.collection("AM_site_location");
        amAssignSiteReference = db.collection("AM_assign_site");
        SessionManager sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        userId = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            asset_type= extras.getString("asset_type");
            asset_number= extras.getString("asset_id");
            asset_name= extras.getString("asset_name");
            assetId = extras.getInt("assetId");
            assignLocationId = extras.getInt("assignLocationId");
            Log.d("RTUUD :" , String.valueOf(assignLocationId));
        }
        tv_asset_type = findViewById(R.id.tv_asset_type);
        tv_asset_id = findViewById(R.id.tv_asset_id);
        tv_asset_name = findViewById(R.id.tv_asset_name);
        tv_asset_type.setText(asset_type);
        tv_asset_id.setText(asset_number);
        tv_asset_name.setText(asset_name);
        bt_back = findViewById(R.id.bt_back);
        bt_submit = findViewById(R.id.bt_submit);
        cb_off_hire_caution = findViewById(R.id.cb_off_hire_caution);
        iv_camera = findViewById(R.id.iv_camera);
        iv_photo_one = findViewById(R.id.iv_photo_one);
        iv_photo_two = findViewById(R.id.iv_photo_two);
        iv_close_one = findViewById(R.id.iv_close_one);
        iv_close_two = findViewById(R.id.iv_close_two);
        ll_images = findViewById(R.id.ll_images);
        sp_storage_location = findViewById(R.id.sp_storage_location);
       currentDate = AppData.date();
currentTime = AppData.Time();
        getMaxDocIDOffHireDetails();
        fetchStorageLocation();
        checkAssetDetailsOffHire();
        getAssignSiteId();
    }

    private void initializeOnClick(){
        bt_back.setOnClickListener(this);
        bt_submit.setOnClickListener(this);
        iv_camera.setOnClickListener(this);
        iv_close_one.setOnClickListener(this);
        iv_close_two.setOnClickListener(this);
        iv_photo_one.setOnClickListener(this);
        iv_photo_two.setOnClickListener(this);
        sp_storage_location.setOnItemSelectedListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_back:
                finish();
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
            case R.id.bt_submit:
                if (checkValidation()){
                    offHireRequest();
                }
                break;
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);
        switch (requestCode) {
            case REQUEST_CAMERA:
                if(resultCode == Activity.RESULT_OK){
                    try {
                        Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(
                                getContentResolver(), imageUri);
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
                                image1 = AppData.convertTOBase64Image(getResizedBitmap(rotatedBitmap,700));
                                hasImage = true;
                            } else if (hasImage && !hasImageTwo) {
                                iv_photo_two.setImageBitmap(rotatedBitmap);
                                image2 = AppData.convertTOBase64Image(getResizedBitmap(rotatedBitmap,700));
                                hasImageTwo = false;
                                hasImage = false;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "An unexpected error occurred", e);
                    }
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_storage_location:
                storage_location = storageLocationId.get(position);
                storage_location_name = storageLocation.get(position);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void getAssignSiteId(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = amAssignSiteReference.whereEqualTo("user_id", userId).whereEqualTo("date", currentDate);
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            siteLocationId = Objects.requireNonNull(queryDocumentSnapshot.getLong("site_id")).intValue();
                        }
                        Log.d("Current SITE ID :" , String.valueOf(siteLocationId));
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(OffHireRequest.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMaxDocIDOffHireDetails(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        offHireReference.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            offHireIds.add(Integer.parseInt(queryDocumentSnapshot.getId()));
                        }
                        maxDocIdOffHire = String.valueOf(getMax(offHireIds));
                        Log.d("max_regime_details_id :",maxDocIdOffHire);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OffHireRequest.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAssetDetailsOffHire(){
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
                            off_hire = queryDocumentSnapshot.getString("off_hire");
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OffHireRequest.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void offHireRequest() {
        progressDialog.show();
        if (maxDocIdOffHire.equals(""))
            offHireId = 1;
        else
            offHireId = Integer.parseInt(maxDocIdOffHire) + 1;
        Map<String, Object> mapOffHireDetails = new HashMap<>();
        mapOffHireDetails.put("id", offHireId);
        mapOffHireDetails.put("asset_details_id", assetId);
        mapOffHireDetails.put("date", currentDate);
        mapOffHireDetails.put("next_thorough_examination", "");
        mapOffHireDetails.put("insert_user", userId);
        mapOffHireDetails.put("insert_time", currentTime);
        mapOffHireDetails.put("insert_date", currentDate);
        mapOffHireDetails.put("assign_location", storage_location);
        mapOffHireDetails.put("storage_location",storage_location);
        mapOffHireDetails.put("off_hire_location",siteLocationId);
        mapOffHireDetails.put("z_image_one",image1);
        mapOffHireDetails.put("z_image_two",image2);
        mapOffHireDetails.put("created_at", FieldValue.serverTimestamp());
        mapOffHireDetails.put("updated_at", FieldValue.serverTimestamp());
        mapOffHireDetails.put("status", "Active");
        mapOffHireDetails.put("time_second_format", AppData.getTimeSecond());
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = offHireReference;
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    offHireReference.document(String.valueOf(offHireId)).set(mapOffHireDetails);
                    updateAssetDetails();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(OffHireRequest.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            Map<String, Object> update_site = new HashMap<>();
                            update_site.put("assign_location", storage_location);
                            update_site.put("off_hire","Yes");
                            update_site.put("previous_assign_location",assignLocationId);
                            update_site.put("storage_location",storage_location);
                            assetDetailsReference.document(doc_id).update(update_site);
                            toConfirmation("Request submitted successfully.");
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OffHireRequest.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchStorageLocation(){
        Source source;
        if (!AppData.internetOnline(this))
            source = Source.CACHE;
        else source = Source.SERVER;
        Query query = siteLocationReference.whereEqualTo("status", "Active").whereEqualTo("site_type","Offhire");
        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            storageLocationId.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                            storageLocation.add(queryDocumentSnapshot.getString("site_name"));
                        }
                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(OffHireRequest.this, R.layout.spinner_custom_layout, storageLocation);
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                        sp_storage_location.setAdapter(spinnerArrayAdapter);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OffHireRequest.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private Boolean checkValidation(){
        if (off_hire!=null && off_hire.equals("Yes"))
            Dialog.alertDialog(this, "This asset already off hired.");
        else if (image1.isEmpty())
            Dialog.alertDialog(this, "Please take a photo.");
        else if (storage_location==0)
            Dialog.alertDialog(this,"Please select storage location.");
        else if (!cb_off_hire_caution.isChecked())
            Dialog.alertDialog(this,"Please select check box.");
        else
            isValidationDone = true;
        return isValidationDone;
    }

    private void toConfirmation(String message) {
        progressDialog.dismiss();
        final View popupView = getLayoutInflater().inflate(R.layout.change_pass_popup_layout, null, true);
         boolean isActivityInForeground = OffHireRequest.this.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED);
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
                Intent toDashboard = new Intent(OffHireRequest.this, AssetManagementDashboard.class);
                startActivity(toDashboard);
                finish();
            }
        });
    }
}