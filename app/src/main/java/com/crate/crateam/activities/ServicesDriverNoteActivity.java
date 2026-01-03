package com.crate.crateam.activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.crate.crateam.R;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.SessionManager;
import com.github.gcacace.signaturepad.views.SignaturePad;
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
import com.crate.crateam.utility.CustomSearchableSpinner;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ServicesDriverNoteActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView iv_cross,iv_camera,iv_photo_one,iv_photo_two,iv_close_one,iv_close_two;
    private TextView tv_date,tv_time,tv_driver_name,tv_vehicle_reg,tv_clear_driver;
    private CustomSearchableSpinner sp_current_site;
    private RadioGroup rg_collection_delivery;
    private EditText et_general_comment;
    private Button bt_submit,bt_driver_sign;
    private SignaturePad signaturePadDriver;
    private LinearLayout ll_images,ll_driver_sign;
    private String user_name,image1,image2,delivery_type= "",doc_id="";
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference siteManagementReference;
    private CollectionReference serviceDriverReference,currentVehicleReference,vehicleDetailsReference,sortKeyReference;
    private boolean hasImage = false,hasImageTwo = false,isSigned = false;
    private Bitmap getDrawable1, getDrawable2;
    private String selected_site_name="",user_id = "", driver_sign = "",vehicle_number = "",submission_date="",send_submission_time ="";
    private ProgressDialog progressDialog;
    private PopupWindow popupWindow;
    private int selected_site_id,vehicle_id,doc_id_length = 0;
    private ArrayList<String> siteList = new ArrayList<>();
    private ArrayList<Integer> siteId = new ArrayList<>();
    private Uri imageUri;

    public static final String TAG = "ServiceDriver";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.services_driver_note_layout);
        FirestoreManager.initPersistentIndexManager();
        initView();
    }

    private void initView(){
        SessionManager sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_name = user.get(SessionManager.KEY_FULL_NAME);
        user_id = user.get(SessionManager.KEY_ID);
        iv_cross = findViewById(R.id.iv_cross);
        iv_camera = findViewById(R.id.iv_camera);
        iv_photo_one = findViewById(R.id.iv_photo_one);
        iv_photo_two = findViewById(R.id.iv_photo_two);
        iv_close_one = findViewById(R.id.iv_close_one);
        iv_close_two = findViewById(R.id.iv_close_two);
        tv_date = findViewById(R.id.tv_date);
        tv_time = findViewById(R.id.tv_time);
        tv_driver_name = findViewById(R.id.tv_driver_name);
        tv_vehicle_reg = findViewById(R.id.tv_vehicle_reg);
        tv_clear_driver = findViewById(R.id.tv_clear_driver);
        ll_images = findViewById(R.id.ll_images);
        ll_driver_sign = findViewById(R.id.ll_driver_sign);
        sp_current_site = findViewById(R.id.sp_current_site);
        rg_collection_delivery = findViewById(R.id.rg_collection_delivery);
        signaturePadDriver = findViewById(R.id.signature_pad_driver);
        et_general_comment = findViewById(R.id.et_general_comment);
        bt_submit = findViewById(R.id.bt_submit);
        bt_driver_sign = findViewById(R.id.bt_driver_sign);
        progressDialog = Dialog.showProgressDialog(this);
        progressDialog.show();
        siteManagementReference = db.collection("CR_site_management");
        serviceDriverReference = db.collection("CR_service_driver_note");
        vehicleDetailsReference = db.collection("CR_vehicle_details");
        currentVehicleReference  = db.collection("CR_current_vehicle");
        sortKeyReference = db.collection("CR_sort_key");
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String date = simpleDateFormat.format(c);
        tv_date.setText(date);
        getSiteAddress();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("hh:mm a");
        String time = dateFormatter.format(calendar.getTime());
        tv_time.setText(time);
        tv_driver_name.setText(user_name);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_custom_layout,siteList);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        sp_current_site.setAdapter(spinnerArrayAdapter);
        sp_current_site.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_site_name = siteList.get(position).toString().trim();
                selected_site_id = siteId.get(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        getVehicleID();
        setSigned();
        getMaximumLength();
        setRadioButton();
        initializeOnClick();
    }

    private void initializeOnClick(){
        iv_cross.setOnClickListener(this);
        iv_camera.setOnClickListener(this);
        iv_close_one.setOnClickListener(this);
        iv_close_two.setOnClickListener(this);
        iv_photo_one.setOnClickListener(this);
        iv_photo_two.setOnClickListener(this);
        bt_submit.setOnClickListener(this);
        bt_driver_sign.setOnClickListener(this);
        tv_clear_driver.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_cross:
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
                startActivityForResult(intent,0);
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
            case R.id.bt_driver_sign:
                bt_driver_sign.setVisibility(View.GONE);
                ll_driver_sign.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_clear_driver:
                signaturePadDriver.clear();
                Toast.makeText(this,"Sign Cleared",Toast.LENGTH_SHORT).show();
                break;
            case R.id.bt_submit:
                sendServiceNoteData();
                if (!AppData.internetOnline(this)){
                    toConfirmation("Service note submitted successfully");
                    progressDialog.dismiss();
                }
                break;
        }
    }

    private void setSigned() {
        signaturePadDriver.setOnSignedListener(new SignaturePad.OnSignedListener() {
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

    private void setRadioButton(){
        rg_collection_delivery.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i==R.id.rb_collection) {
                    delivery_type = "collection";
                }
                if (i==R.id.rb_delivery) {
                    delivery_type = "delivery";
                }
            }
        });
    }

    private void getSiteAddress(){
        Query query = siteManagementReference.whereEqualTo("status","Active").whereEqualTo("address_status","Active").whereEqualTo("site_type","Collection");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        siteList.add(queryDocumentSnapshot.getString("site_name"));
                        siteId.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ServicesDriverNoteActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendServiceNoteData( ){
        if (selected_site_name.equalsIgnoreCase("Select Site Name") || selected_site_name.isEmpty())
            Dialog.alertDialog(ServicesDriverNoteActivity.this,"Please select any site name.");
        else if (TextUtils.isEmpty(delivery_type))
            Dialog.alertDialog(ServicesDriverNoteActivity.this,"Please select delivery type.");
        else if (et_general_comment.getText().toString().trim().isEmpty())
            Dialog.alertDialog(ServicesDriverNoteActivity.this,"Please give comment.");
        else if (!isSigned)
            Dialog.alertDialog(this,"Please sign the form.");
        else {
            if (image1 == null)
                image1 = "";
            if (image2 == null)
                image2 = "";
            String comment = et_general_comment.getText().toString().trim();
            storeServiceDriverNoteData(tv_driver_name.getText().toString().trim(),vehicle_id,vehicle_number,
                    selected_site_name,selected_site_id,delivery_type,comment,image1,image2);
        }
    }
    private void storeServiceDriverNoteData(String driver_name,int vehicle_id,String vehicle_reg_no,String selected_site_name,int selected_site_id,String delivery_type,String comment,String image_one,String image_two){
       // get current date
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("ddMMyyyy");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = df.format(c);
        final String conducted_on = formattedDate ;
        submission_date = simpleDateFormat.format(c);
        //get current time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HHmm", Locale.getDefault());
        SimpleDateFormat service_driver = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        final String submission_time = dateFormatter.format(calendar.getTime());
        send_submission_time = service_driver.format(calendar.getTime());
        Bitmap bitmap_operator = signaturePadDriver.getSignatureBitmap();
        driver_sign = AppData.convertTOBase64Image(bitmap_operator);
        Map<String,Object> driverNoteMap = new HashMap<>();
        driverNoteMap.put("driver_name",driver_name);
        driverNoteMap.put("vehicle_id",vehicle_id);
        driverNoteMap.put("vehicle_reg_no",vehicle_reg_no);
        driverNoteMap.put("site_name",selected_site_name);
        driverNoteMap.put("site_id",selected_site_id);
        driverNoteMap.put("delivery_type",delivery_type);
        driverNoteMap.put("comment",comment);
        driverNoteMap.put("status","Active");
        driverNoteMap.put("user_id",Integer.valueOf(user_id));
        driverNoteMap.put("submission_date",submission_date);
        driverNoteMap.put("submission_time",send_submission_time);
        driverNoteMap.put("image_one",image_one);
        driverNoteMap.put("image_two",image_two);
        driverNoteMap.put("driver_sign",driver_sign);
        driverNoteMap.put("id",user_id+"_"+conducted_on+"_"+submission_time);
        doc_id_length = doc_id_length+1;
        driverNoteMap.put("sort_key",doc_id_length);
        driverNoteMap.put("is_updated","Yes");
        driverNoteMap.put("time_second_format",AppData.getTimeSecond());
        progressDialog.show();
        serviceDriverReference.document(user_id+"_"+conducted_on+"_"+submission_time).set(driverNoteMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                        toConfirmation("Service note submitted successfully");
                    Map<String,Object> objectMap_update = new HashMap<>();
                    objectMap_update.put("service_note_key",doc_id_length);
                    sortKeyReference.document(doc_id).update(objectMap_update);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ServicesDriverNoteActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if(resultCode == RESULT_OK){
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

 

    private void toConfirmation(String message){
        progressDialog.dismiss();
        final View popupView = getLayoutInflater().inflate(R.layout.change_pass_popup_layout,null,true);
        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,false);
        popupWindow.showAtLocation(popupView, Gravity.BOTTOM,0,0);
        Button cancelPopup = popupView.findViewById(R.id.bt_ok);
        TextView tv_change_pass = popupView.findViewById(R.id.tv_change_pass);
        tv_change_pass.setText(message);
        cancelPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                Intent toDashboard = new Intent(ServicesDriverNoteActivity.this,MainActivity.class);
                startActivity(toDashboard);
            }
        });
    }
    private  void getVehicleID(){
        Query query = currentVehicleReference.whereEqualTo("user_id",Integer.parseInt(user_id));
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                        if (queryDocumentSnapshot.exists()){
                            vehicle_id = queryDocumentSnapshot.getLong("vehicle_id").intValue();
                            Log.d("VEHICLE ID:" , String.valueOf(vehicle_id));
                        }
                    }
                    getVehicleNumber(vehicle_id);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.e("Error", "Unable to fetch current vehicle.");
            }
        });
    }
    private void getVehicleNumber(int vehicle_id){
        Query query = vehicleDetailsReference.whereEqualTo("external_vehicle","No").whereEqualTo("id",vehicle_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                        if (queryDocumentSnapshot.exists()){
                            vehicle_number = queryDocumentSnapshot.getString("registration_no");
                            Log.d("VEHICLE ID:" , Objects.requireNonNull(vehicle_number));
                            tv_vehicle_reg.setText(vehicle_number);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.e("Error", "Unable to fetch current vehicle.");
            }
        });
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
                            if (queryDocumentSnapshot.contains("service_note_key")){
                                doc_id_length = queryDocumentSnapshot.getLong("service_note_key").intValue();
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
                Toast.makeText(ServicesDriverNoteActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
