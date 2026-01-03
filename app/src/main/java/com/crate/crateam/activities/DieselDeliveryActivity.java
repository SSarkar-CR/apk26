package com.crate.crateam.activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.crate.crateam.R;
import com.crate.crateam.adapters.DefaultViewDieselDeliveryAdapter;
import com.crate.crateam.model.DynamicViewDieselDelivery;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.SessionManager;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
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

public class DieselDeliveryActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView iv_cross,iv_camera,iv_photo_one,iv_photo_two,iv_close_one,iv_close_two;
    private Button bt_driver_sign,bt_submit;
    private LinearLayout ll_driver_sign,ll_operator_sign,ll_images;
    private TextView tv_clear_driver,tv_vehicle_reg_no,tv_date,tv_time,tv_driver_name;
    private SignaturePad signaturePadDriver;
    private String user_name,image1,image2,vehicle_number="",send_submission_time="",send_current_date="",conducted_on="",submission_time="";
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private boolean hasImage = false,hasImageTwo = false,isSigned = false,add_or_not = false;
    private PopupWindow popupWindow;
    private Bitmap getDrawable1, getDrawable2;
    private CustomSearchableSpinner sp_current_site;
    private String selected_site_name="",driver_sign="",user_id="",diesel_delivery_id = "",add_item_document_name = "",doc_id ="",
            doc_id_items="",inspection_id="";
    private int selected_site_id=0,vehicle_id=0, doc_id_length =0,doc_id_length_items=0;
    private ProgressDialog progressDialog;
    private CollectionReference siteManagementReference,currentVehicleReference,vehicleDetailsReference,dieselSubmissionReference,
            dieselAddItemsReference,sortKeyReference,inspectionSubmissionReference;
    private DefaultViewDieselDeliveryAdapter defaultViewDieselDeliveryAdapter;
    private ArrayList<DynamicViewDieselDelivery> dynamicViewDieselDeliveryArrayList;
    private RecyclerView rv_dynamic_view;
    private ArrayList<String> siteList = new ArrayList<>();
    private ArrayList<Integer> siteId = new ArrayList<>();
    private EditText editText_one,editText_third;
    private int view_position=-0;
    private Uri imageUri;

    public static final String TAG = "DieselDelivery";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diesel_delivery_layout);
        FirestoreManager.initPersistentIndexManager();
        initView();
    }

    private void initView(){
        SessionManager sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_name = user.get(SessionManager.KEY_FULL_NAME);
        user_id = user.get(SessionManager.KEY_ID);
        Log.d("USER_ID : " ,user_name);
        iv_cross = findViewById(R.id.iv_cross);
        iv_camera = findViewById(R.id.iv_camera);
        iv_photo_one = findViewById(R.id.iv_photo_one);
        iv_photo_two = findViewById(R.id.iv_photo_two);
        iv_close_one = findViewById(R.id.iv_close_one);
        iv_close_two = findViewById(R.id.iv_close_two);
        bt_driver_sign = findViewById(R.id.bt_driver_sign);
        ll_driver_sign = findViewById(R.id.ll_driver_sign);
        ll_operator_sign = findViewById(R.id.ll_operator_sign);
        ll_images = findViewById(R.id.ll_images);
        tv_clear_driver = findViewById(R.id.tv_clear_driver);
        signaturePadDriver = findViewById(R.id.signature_pad_driver);
        tv_vehicle_reg_no = findViewById(R.id.tv_vehicle_reg_no);
        tv_vehicle_reg_no.setText(vehicle_number);
        tv_date = findViewById(R.id.tv_date);
        tv_time = findViewById(R.id.tv_time);
        tv_driver_name = findViewById(R.id.tv_driver_name);
        tv_driver_name.setText(user_name);
        rv_dynamic_view = findViewById(R.id.rv_dynamic_view);
        sp_current_site = findViewById(R.id.sp_current_site);
        bt_submit = findViewById(R.id.bt_submit);
        progressDialog = Dialog.showProgressDialog(this);
        progressDialog.show();
        dynamicViewDieselDeliveryArrayList = new ArrayList<>();
        siteManagementReference = db.collection("CR_site_management");
        dieselSubmissionReference = db.collection("CR_diesel_management");
        dieselAddItemsReference = db.collection("CR_diesel_delivery_items");
        vehicleDetailsReference = db.collection("CR_vehicle_details");
        currentVehicleReference  = db.collection("CR_current_vehicle");
        sortKeyReference = db.collection("CR_sort_key");
        inspectionSubmissionReference = db.collection("CR_inspection_submission");
        getSiteAddress();
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
        setDynamicAdapter();
        Dialog.addViewDieselDeliveryInterface = new DefaultViewDieselDeliveryAdapter.AddViewDieselDelivery() {
            @Override
            public void addView(int number_of_item,int position,ArrayList<DynamicViewDieselDelivery> dynamicViewDieselDeliveryArrayList1) {
                if (dynamicViewDieselDeliveryArrayList1.size()<=4) {
                    addItems();
                    if (!add_or_not) {
                        DynamicViewDieselDelivery dynamicViewDieselDeliveryModule = new DynamicViewDieselDelivery();
                        dynamicViewDieselDeliveryModule.setItem_fulled("");
                        dynamicViewDieselDeliveryModule.setItem_plant_no("");
                        dynamicViewDieselDeliveryModule.setLiters_fulled("");
                        dynamicViewDieselDeliveryModule.setGeneral_comment("");
                        dynamicViewDieselDeliveryArrayList.add(dynamicViewDieselDeliveryModule);
                        defaultViewDieselDeliveryAdapter = new DefaultViewDieselDeliveryAdapter(DieselDeliveryActivity.this, dynamicViewDieselDeliveryArrayList);
                        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                        rv_dynamic_view.setLayoutManager(mLayoutManager);
                        rv_dynamic_view.setItemAnimator(new DefaultItemAnimator());
                        rv_dynamic_view.setAdapter(defaultViewDieselDeliveryAdapter);
                    }
                    }
                else {
                    Toast.makeText(DieselDeliveryActivity.this, "You have added maximum item", Toast.LENGTH_SHORT).show();
                }
            }
        };
        currentDate();
        initializeOnClick();
        setSigned();
        getVehicleID();
        getMaximumLength();
    }

    private void getSiteAddress(){
        Query query = siteManagementReference.whereEqualTo("status","Active").whereEqualTo("address_status","Active")
                .whereEqualTo("site_type","Collection");
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
                Toast.makeText(DieselDeliveryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void currentDate(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("ddMMyyyy");
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat simpleDateFormat3 = new SimpleDateFormat("dd.MM.yyyy");
        conducted_on = simpleDateFormat1.format(c) ;
        send_current_date = simpleDateFormat2.format(c);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HHmm", Locale.getDefault());
        SimpleDateFormat diesel_management = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        SimpleDateFormat time_format = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        submission_time = dateFormatter.format(calendar.getTime());
        send_submission_time = diesel_management.format(calendar.getTime());
        diesel_delivery_id = user_id+conducted_on+submission_time;
        tv_date.setText(simpleDateFormat3.format(c));
        tv_time.setText(time_format.format(calendar.getTime()));
        add_item_document_name = user_id+"_"+user_id+conducted_on+submission_time;
    }

    private void addItems(){
        EditText et_item_fulled,et_item_plant_number,et_liters_fuelled,et_general_comment;
        ImageView iv_add_item;
        editText_one = new EditText(DieselDeliveryActivity.this);
        editText_third = new EditText(DieselDeliveryActivity.this);
        for (int i=0;i<defaultViewDieselDeliveryAdapter.getItemCount();i++){
            View view = rv_dynamic_view.getChildAt(i);
            if (view != null){
                et_item_fulled = view.findViewById(R.id.et_item_fulled);
                et_item_plant_number = view.findViewById(R.id.et_item_plant_number);
                et_liters_fuelled = view.findViewById(R.id.et_liters_fuelled);
                et_general_comment = view.findViewById(R.id.et_general_comment);
                iv_add_item = view.findViewById(R.id.iv_add_item);
                String  item_fuelled = et_item_fulled.getText().toString().trim();
                String item_plant_number = et_item_plant_number.getText().toString().trim();
                String liters_fuelled = et_liters_fuelled.getText().toString().trim();
                String general_comment = et_general_comment.getText().toString().trim();

                editText_one = et_item_fulled;
                editText_third=et_liters_fuelled;

                if (item_fuelled.equals(dynamicViewDieselDeliveryArrayList.get(i).getItem_fulled()) && item_plant_number.equals(dynamicViewDieselDeliveryArrayList.get(i).getItem_plant_no()) &&
                        liters_fuelled.equals(dynamicViewDieselDeliveryArrayList.get(i).getLiters_fulled()) && general_comment.equals(dynamicViewDieselDeliveryArrayList.get(i).getGeneral_comment())) {
                    if (i>0) {
                        view_position = i;
                        if (!item_fuelled.equals("")|| !item_plant_number.equals("")||!liters_fuelled.equals("")||!general_comment.equals("")) {
                            if (item_fuelled.equals("")) {
                                add_or_not = true;
                                alertDialogAddSameItem("Please insert item fuelled value.");
                            } else if (liters_fuelled.equals("")) {
                                add_or_not = true;
                                alertDialogAddSameItem("Please insert liters fuelled value.");
                            }
                        }else
                            dynamicViewDieselDeliveryArrayList.remove(i);
                    }else {
                        if (item_fuelled.equals("")) {
                            add_or_not = true;
                            alertDialogAddSameItem("Please insert item fuelled value.");
                        } else if (liters_fuelled.equals("")) {
                            add_or_not = true;
                            alertDialogAddSameItem("Please insert liters fuelled value.");
                        }
                    }
                } else if (item_fuelled.equals("")) {
                    add_or_not = true;
                    alertDialogAddSameItem("Please insert item fuelled value.");
                } else if (liters_fuelled.equals("")) {
                    add_or_not = true;
                    alertDialogAddSameItem("Please insert liters fuelled value.");
                } else {
                    add_or_not = false;
                    DynamicViewDieselDelivery dynamicViewDieselDelivery = new DynamicViewDieselDelivery();
                    dynamicViewDieselDelivery.setItem_fulled(item_fuelled);
                    dynamicViewDieselDelivery.setItem_plant_no(item_plant_number);
                    dynamicViewDieselDelivery.setLiters_fulled(liters_fuelled);
                    dynamicViewDieselDelivery.setGeneral_comment(general_comment);
                    dynamicViewDieselDeliveryArrayList.add(dynamicViewDieselDelivery);
                    for (int j = 0; j < dynamicViewDieselDeliveryArrayList.size(); j++) {
                        if (dynamicViewDieselDeliveryArrayList.get(j).getItem_fulled().isEmpty() && dynamicViewDieselDeliveryArrayList.get(j).getItem_plant_no().isEmpty() &&
                                dynamicViewDieselDeliveryArrayList.get(j).getLiters_fulled().isEmpty() && dynamicViewDieselDeliveryArrayList.get(j).getGeneral_comment().isEmpty()) {
                            dynamicViewDieselDeliveryArrayList.remove(j);
                        }
                    }
                }
            }
        }
    }
    private void setDynamicAdapter(){
        DynamicViewDieselDelivery dynamicViewDieselDeliveryModule = new DynamicViewDieselDelivery();
        dynamicViewDieselDeliveryModule.setItem_fulled("");
        dynamicViewDieselDeliveryModule.setItem_plant_no("");
        dynamicViewDieselDeliveryModule.setLiters_fulled("");
        dynamicViewDieselDeliveryModule.setGeneral_comment("");
        dynamicViewDieselDeliveryArrayList.add(dynamicViewDieselDeliveryModule);
        defaultViewDieselDeliveryAdapter = new DefaultViewDieselDeliveryAdapter(DieselDeliveryActivity.this,dynamicViewDieselDeliveryArrayList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv_dynamic_view.setLayoutManager(mLayoutManager);
        rv_dynamic_view.setItemAnimator(new DefaultItemAnimator());
        rv_dynamic_view.setAdapter(defaultViewDieselDeliveryAdapter);
        defaultViewDieselDeliveryAdapter.notifyDataSetChanged();
    }
    private void initializeOnClick(){
        iv_cross.setOnClickListener(this);
        bt_driver_sign.setOnClickListener(this);
        bt_submit.setOnClickListener(this);
        tv_clear_driver.setOnClickListener(this);
        iv_camera.setOnClickListener(this);
        iv_close_one.setOnClickListener(this);
        iv_close_two.setOnClickListener(this);
        iv_photo_one.setOnClickListener(this);
        iv_photo_two.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_cross:
                finish();
                break;
            case R.id.bt_driver_sign:
                ll_driver_sign.setVisibility(View.VISIBLE);
                bt_driver_sign.setVisibility(View.GONE);
                break;
            case R.id.tv_clear_driver:
                signaturePadDriver.clear();
                Toast.makeText(this,"Sign Cleared",Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_camera:
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "New Picture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                imageUri = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, 0);
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
                sendDieselManagementData();
                break;
            case R.id.bt_discard:
                alertDialog("Do you want to leave this form?");
                break;
        }
    }
    private void sendDieselManagementData(){
        if (selected_site_name.equalsIgnoreCase("Select Site Name") || selected_site_name.isEmpty())
            Dialog.alertDialog(DieselDeliveryActivity.this,"Please select any Site Name");
        else if (!isSigned)
            Dialog.alertDialog(this,"Driver sign mandatory.");
        else {
            Log.e("driver_sign",driver_sign);
            if (image1 == null)
                image1 = "";
            if (image2 == null)
                image2 = "";
                sendAddItemsData();
            if (!add_or_not)
            storeDieselManagementData(selected_site_id,selected_site_name,image1,image2);
            else if (view_position>0 && !editText_one.getText().toString().trim().isEmpty()  && !editText_third.getText().toString().trim().isEmpty()){
                add_or_not=false;
                sendAddItemsData();
                storeDieselManagementData(selected_site_id,selected_site_name,image1,image2);
            }
        }
    }

     private void storeDieselManagementData(int selected_site_id,String selected_site_name,String image_one,String image_two){
        Bitmap signatureBitmap = signaturePadDriver.getSignatureBitmap();
        driver_sign = AppData.convertTOBase64Image(signatureBitmap);
        Map<String,Object> dieselManagementMap = new HashMap<>();
         dieselManagementMap.put("site_id",selected_site_id);
         dieselManagementMap.put("site_name",selected_site_name);
         dieselManagementMap.put("driver_sign",driver_sign);
         dieselManagementMap.put("submission_date",send_current_date);
         dieselManagementMap.put("submission_time",send_submission_time);
         dieselManagementMap.put("driver_name",user_name);
         dieselManagementMap.put("driver_id",Integer.valueOf(user_id));
         dieselManagementMap.put("vehicle_no",vehicle_number);
         dieselManagementMap.put("vehicle_id",vehicle_id);
         dieselManagementMap.put("image_one",image_one);
         dieselManagementMap.put("image_two",image_two);
         dieselManagementMap.put("diesel_delivery_id",diesel_delivery_id);
         dieselManagementMap.put("id",user_id+"_"+conducted_on+"_"+submission_time);
         dieselManagementMap.put("status","Active");
         doc_id_length =doc_id_length+1;
         dieselManagementMap.put("sort_key",doc_id_length);
         dieselManagementMap.put("is_updated","Yes");
         dieselManagementMap.put("time_second_format",AppData.getTimeSecond());
        progressDialog.setCancelable(false);
        progressDialog.show();
        dieselSubmissionReference.document(user_id+"_"+conducted_on+"_"+submission_time).set(dieselManagementMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful())
                    toConfirmation("Diesel Delivery report submitted successfully");
                Map<String,Object> objectMap_update = new HashMap<>();
                objectMap_update.put("diesel_management_key",doc_id_length);
                sortKeyReference.document(doc_id).update(objectMap_update);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(DieselDeliveryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void sendAddItemsData(){
        int length = dynamicViewDieselDeliveryArrayList.size();
        Log.e("length", String.valueOf(length));
        addItems();
        if (!add_or_not)
        for (int i = 0; i < dynamicViewDieselDeliveryArrayList.size(); i++) {
            String item_fulled = dynamicViewDieselDeliveryArrayList.get(i).getItem_fulled();
            String item_plant_number = dynamicViewDieselDeliveryArrayList.get(i).getItem_plant_no();
            String liters_fulled = dynamicViewDieselDeliveryArrayList.get(i).getLiters_fulled();
            String general_comment = dynamicViewDieselDeliveryArrayList.get(i).getGeneral_comment();
            storeAddItemsData(item_fulled, item_plant_number, liters_fulled, general_comment, i);
        }
    }
    private void storeAddItemsData(String item_fulled,String item_plant_number,String liters_fulled,String general_comment,int position){
        Map<String,Object> stringMapAddItems = new HashMap<>();
        stringMapAddItems.put("item_fulled",item_fulled);
        stringMapAddItems.put("item_plant_number",item_plant_number);
        stringMapAddItems.put("liters_fulled",liters_fulled);
        stringMapAddItems.put("general_comment",general_comment);
        stringMapAddItems.put("diesel_delivery_id",diesel_delivery_id);
        stringMapAddItems.put("id",add_item_document_name+"_"+position);
        stringMapAddItems.put("inspection_id",inspection_id);
        stringMapAddItems.put("driver_id",Integer.valueOf(user_id));
        stringMapAddItems.put("submission_date",send_current_date);
        stringMapAddItems.put("vehicle_id",vehicle_id);
        stringMapAddItems.put("status","Active");
        stringMapAddItems.put("is_updated","Yes");
        dieselAddItemsReference.document(add_item_document_name+"_"+position).set(stringMapAddItems).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.e("Success :", "Items added successfully.");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Error :", "Message:" +e.getMessage());
            }
        });
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
                Intent toDashboard = new Intent(DieselDeliveryActivity.this,MainActivity.class);
                startActivity(toDashboard);
            }
        });
    }
    private void alertDialog(String message){
        AlertDialog.Builder alertDialog=  new  AlertDialog.Builder(this);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton( "Discard",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        alertDialog.setNegativeButton( "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void alertDialogAddSameItem(String message){
        AlertDialog.Builder alertDialog=  new  AlertDialog.Builder(this);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton( "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
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
                    fetchInspectionId(vehicle_id);
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
                            tv_vehicle_reg_no.setText(vehicle_number);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
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
                            if (queryDocumentSnapshot.contains("diesel_management_key")){
                                doc_id_length = queryDocumentSnapshot.getLong("diesel_management_key").intValue();
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
                Toast.makeText(DieselDeliveryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchInspectionId(int vehicle_id){
        Query query = inspectionSubmissionReference.whereEqualTo("logged_by",Integer.parseInt(user_id)).whereEqualTo("conducted_on",send_current_date)
                .whereEqualTo("vehicle_id",vehicle_id).whereEqualTo("user_role",4).whereEqualTo("status","Active");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        Log.d("Inspection Id :" ,"I am here");
                        inspection_id = queryDocumentSnapshot.getString("inspection_id");
                        Log.d("Inspection Id :" , Objects.requireNonNull(inspection_id));
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d("Error :" ,"Unable to fetch inspection id." );
            }
        });
    }
}
