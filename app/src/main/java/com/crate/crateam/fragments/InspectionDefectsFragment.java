package com.crate.crateam.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.crate.crateam.R;
import com.crate.crateam.activities.MainActivity;
import com.crate.crateam.adapters.DefectListAdapter;
import com.crate.crateam.utility.CustomViewPagerInspection;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
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
import static android.app.Activity.RESULT_OK;

public class InspectionDefectsFragment extends Fragment {
    private static final String TAG = "INSPECTION_DEFECTS_FRAG";
    private Fragment fragment = null;
    private SessionManager sessionManager;
    private ProgressDialog progressDialog;
    private LinearLayout ll_additional,ll_previous_defect;
    private RelativeLayout rl_manager_comment;
    private RecyclerView rv_defects_list;
    private DefectListAdapter defectListAdapter;
    private RadioButton rb_additional,rb_reported;
    private ImageView iv_one,iv_two, iv_close_one,iv_close_two,imageView1,imageView2,iv_next_page;
    private Bitmap getDrawable1, getDrawable2;
    private boolean hasImage = false,hasImageTwo = false;
    MainActivity mainActivity ;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Boolean CheckEditTextEmpty ;
    private ArrayList<String> defectsList = new ArrayList<>();
    private ArrayList<Integer> defectsListId = new ArrayList<>();
    private String tag,elementName="",elementDefect,inspection_id,add_reported, wm_comment,pre_existing_defect,previous_inspection_id,trailer;
    private EditText et_defect_details ;
    private TextView tv_wm_comment,tv_previousIns_id,tv_previous_defect;
    private CustomViewPagerInspection viewPager;
    private CheckBox cb_checked;
    private int element_id,count = 0, count_yes= 0 ,doc_count=0,id_count=0,visited = 0,vehicle_id=0,element_list_size=0;
    private String defect_image1 = "",defect_image2 = "",vehicle_defect,conducted_on ="",submission_time="";
    private ArrayList<Integer> elementIdWm = new ArrayList<>();
    private ArrayList<String> elementNameWm = new ArrayList<>();
    private ArrayList<String> vehicleElementDefectList = new ArrayList<>();
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference vehicleDefectsReference,vehicleDefectsWMReference,vehicleElementsReference;
    private Uri imageUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.inspection_defects_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    @SuppressLint("DefaultLocale")
    private void initView(View view){
        FirestoreManager.initPersistentIndexManager();
        vehicleDefectsReference = db.collection("CR_vehicle_defect_inspections");
        vehicleDefectsWMReference = db.collection("CR_vehicle_defects_wm");
        vehicleElementsReference = db.collection("CR_vehicle_elements");
        sessionManager = new SessionManager(getActivity());
        HashMap<String, String> vehicle = sessionManager.getVehicleDetails();
        if (vehicle.get(SessionManager.KEY_VEHICLE_ID)!= null)
        vehicle_id = Integer.parseInt(Objects.requireNonNull(vehicle.get(SessionManager.KEY_VEHICLE_ID)));
        progressDialog = Dialog.showProgressDialog(getActivity());
        pref = getActivity().getSharedPreferences("MyPref", 0); // 0 - for private mode
        editor = pref.edit();
        inspection_id= pref.getString("inspection_id",null);
        Log.d(TAG,"INS_ID_DEFECTS :" + inspection_id);
        viewPager = getActivity().findViewById(R.id.viewpager);
        viewPager.setAllowedSwipeDirection(CustomViewPagerInspection.SwipeDirection.NONE);
        iv_next_page = view.findViewById(R.id.iv_next_page);
        iv_next_page.setEnabled(false);
        iv_next_page.setBackgroundResource(R.drawable.arrow_unselect);
        rv_defects_list = view.findViewById(R.id.rv_defects_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv_defects_list.setLayoutManager(layoutManager);
        getVehicleElements();
        mainActivity = (MainActivity) getActivity();
    }

    private void onAdapterPositionClick(){
        final InputMethodManager imm = (InputMethodManager)(getActivity()).getSystemService(Activity.INPUT_METHOD_SERVICE);
        element_list_size = defectsList.size();
        Log.d(TAG,"SIZE :  "+element_list_size);
        defectListAdapter.setOnItemClickListener(new DefectListAdapter.OnCameraClickListener() {
            @Override
            public void onCameraClick(final int position) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "New Picture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                imageUri = requireActivity().getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, 0);
                iv_one = rv_defects_list.getChildAt(position).findViewById(R.id.iv_photo_one);
                iv_two = rv_defects_list.getChildAt(position).findViewById(R.id.iv_photo_two);
                iv_close_one = rv_defects_list.getChildAt(position).findViewById(R.id.iv_close_one);
                iv_close_two = rv_defects_list.getChildAt(position).findViewById(R.id.iv_close_two);
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
                        iv_one = rv_defects_list.getChildAt(position).findViewById(R.id.iv_photo_one);
                        getDrawable1 = ((BitmapDrawable) iv_one.getDrawable()).getBitmap();
                        zoomImagePopup(getDrawable1);
                    }
                });
                iv_two.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        iv_two = rv_defects_list.getChildAt(position).findViewById(R.id.iv_photo_two);
                        getDrawable2 = ((BitmapDrawable) iv_two.getDrawable()).getBitmap();
                        zoomImagePopup(getDrawable2);
                    }
                });
            }
            @Override
            public void onYesClick(int position) {
                cb_checked = rv_defects_list.getChildAt(position).findViewById(R.id.cb_checked);
                et_defect_details = rv_defects_list.getChildAt(position).findViewById(R.id.et_defect_details);
                et_defect_details.setText("");
                View view = (getActivity()).getCurrentFocus();
                if (view != null) {
                    assert imm != null;
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                if (viewPager.getCurrentItem()==1) {
                    if (!cb_checked.isChecked()) {
                        cb_checked.setChecked(true);
                        count++;
                        count_yes++;
                    }
                    if (count_yes >= element_list_size && count != count_yes)
                        enableNextIcon();
                    else if (count >= element_list_size && count != count_yes)
                        enableNextIcon();
                    else if (count == count_yes && count >= element_list_size )
                        enableNextIcon();
                }
                Log.d(TAG,"DETECT CLICK : " +count);
                Log.d(TAG,"DETECT CLICK_YES : " +count_yes);
            }
            @Override
            public void onNoClick(int position) {
                hasImage = false;
                hasImageTwo = false;
                cb_checked = rv_defects_list.getChildAt(position).findViewById(R.id.cb_checked);
                rl_manager_comment = rv_defects_list.getChildAt(position).findViewById(R.id.rl_manager_comment);
                ll_additional = rv_defects_list.getChildAt(position).findViewById(R.id.ll_additional);
                ll_previous_defect = rv_defects_list.getChildAt(position).findViewById(R.id.ll_previous_defect);
                tv_previous_defect = rv_defects_list.getChildAt(position).findViewById(R.id.tv_previous_defect);
                tv_wm_comment = rv_defects_list.getChildAt(position).findViewById(R.id.tv_manager_comment);
                et_defect_details = rv_defects_list.getChildAt(position).findViewById(R.id.et_defect_details);
                et_defect_details.requestFocus();
                assert imm != null;
                imm.showSoftInput(et_defect_details, 0);
                String wm_comment = tv_wm_comment.getText().toString();
                String element_defect = et_defect_details.getText().toString();
                String previous_defect = tv_previous_defect.getText().toString();
                CheckEditTextIsEmptyOrNot(element_defect);
                Log.d(TAG,"WM_comment Vehicle:" +element_defect+" "+previous_defect);
                if (previous_defect.equals("No") || previous_defect.equals(""))
                    ll_previous_defect.setVisibility(View.GONE);
                else
                    ll_previous_defect.setVisibility(View.VISIBLE);
                if (!wm_comment.equals(""))
                    rl_manager_comment.setVisibility(View.VISIBLE);
                else
                    ll_additional.setVisibility(View.VISIBLE);

                if (viewPager.getCurrentItem()==1) {
                    if (!cb_checked.isChecked()) {
                        cb_checked.setChecked(true);
                        count++;
                    }
                    if (count >= element_list_size ) {
                        iv_next_page.setEnabled(true);
                        iv_next_page.setBackgroundResource(R.drawable.arrow);
                        onNextPageClick();
                    }
                }
                Log.d(TAG,"DETECT CLICK : " +count);
            }
        });
    }

    private void enableNextIcon(){
        iv_next_page.setEnabled(true);
        iv_next_page.setBackgroundResource(R.drawable.arrow);
        View view =(getActivity()).getCurrentFocus();
        if (view != null)
            view.clearFocus();
        onNextPageClick();
    }

    private void onNextPageClick(){
        iv_next_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVehicleData();
                HashMap<String, String> vehicle = sessionManager.getVehicleDetails();
                trailer = vehicle.get(SessionManager.KEY_TRAILER);
                if (trailer != null) {
                    if (trailer.equals("Yes"))
                        viewPager.setCurrentItem(2);
                    else
                        viewPager.setCurrentItem(3);
                }
            }
        });
    }



    private void CheckEditTextIsEmptyOrNot(String elementDefect){
        if(TextUtils.isEmpty(elementDefect))
            CheckEditTextEmpty = false ;
        else
            CheckEditTextEmpty = true ;
    }

    private void getVehicleElements(){
        Query query = vehicleElementsReference.whereEqualTo("status","Active").whereEqualTo("car_type","Vehicle").orderBy("id");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        defectsList.add(queryDocumentSnapshot.getString("element"));
                        defectsListId.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                    }
                    Log.d(TAG,"DEFECT LIST :" +defectsList+" "+defectsListId);
                    defectListAdapter = new DefectListAdapter(defectsList);
                    rv_defects_list.setAdapter(defectListAdapter);
                    onAdapterPositionClick();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if(resultCode == RESULT_OK){
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(
                        requireActivity().getContentResolver(), imageUri);
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
                        iv_one.setImageBitmap(rotatedBitmap);
                        defect_image1= convertTOBase64Image(getResizedBitmap(rotatedBitmap,700));
                        hasImage = true;
                    } else if (hasImage && !hasImageTwo) {
                        iv_two.setImageBitmap(rotatedBitmap);
                        defect_image2 = convertTOBase64Image(getResizedBitmap(rotatedBitmap,700));
                        hasImageTwo = false;
                        hasImage = false;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "An unexpected error occurred", e);
            }
        }
    }

    private void sendVehicleData(){
        for (int i = 0; i < defectListAdapter.getItemCount(); i++) {
            View view1 = rv_defects_list.getChildAt(i);
            if (view1 != null) {
                rb_additional = view1.findViewById(R.id.rb_additional);
                rb_reported = view1.findViewById(R.id.rb_reported);
                et_defect_details = view1.findViewById(R.id.et_defect_details);
                tv_wm_comment = view1.findViewById(R.id.tv_manager_comment);
                tv_previousIns_id = view1.findViewById(R.id.tv_previousIns_id);
                imageView1 = view1.findViewById(R.id.iv_photo_one);
                imageView2 = view1.findViewById(R.id.iv_photo_two);
            }
            elementDefect = et_defect_details.getText().toString();
            wm_comment = tv_wm_comment.getText().toString();
            previous_inspection_id = tv_previousIns_id.getText().toString();
            elementName = defectsList.get(i);
            Bitmap image_one =  ((BitmapDrawable) imageView1.getDrawable()).getBitmap();
            Bitmap image_two =  ((BitmapDrawable) imageView2.getDrawable()).getBitmap();
            Bitmap placeholder= BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_image);
            String placeholder_image = convertTOBase64Image(placeholder);
            String image1 = convertTOBase64Image(getResizedBitmap(image_one,600));
            if (image1.equals(placeholder_image))
                defect_image1 = "";
            String image2 = convertTOBase64Image(getResizedBitmap(image_two,600));
            if (image2.equals(placeholder_image))
                defect_image2 = "";
            if (rb_additional.isChecked()){
                add_reported = "additional";
                pre_existing_defect = "No";
            }else if (rb_reported.isChecked()){
                add_reported = "reported";
                pre_existing_defect = "Yes";
            }else{
                add_reported = "";
                pre_existing_defect = "";
            }
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
            CheckEditTextIsEmptyOrNot(elementDefect);
            if (CheckEditTextEmpty) {
                vehicleElementDefectList.add(elementDefect);
                element_id = defectsListId.get(i);
                Log.d(TAG,"VEHICLE_ELEMENT_DEFECT :" + element_id + " " + inspection_id + " " +elementName +" "+elementDefect + "  " + defectsList.get(i) + " "
                        + add_reported + " " + pre_existing_defect + " " + wm_comment + " " + previous_inspection_id + " " + defect_image1 + " " + defect_image2);
                sendVehicleDefects(vehicle_id,0,element_id, inspection_id, elementName, "vehicle", elementDefect, defect_image1, defect_image2,
                        add_reported, pre_existing_defect, wm_comment, previous_inspection_id,"No",conducted_on,submission_time,"");
                vehicle_defect = "Yes";
                editor.putString("vehicle_defect", vehicle_defect);
                editor.apply();
            }
        }
        if (vehicleElementDefectList.isEmpty()){
            sendVehicleDefects(vehicle_id,0,0, inspection_id, "No", "vehicle", "No", "No",
                    "No", "No", "No", "No", "No","No",conducted_on,submission_time,"");
            vehicle_defect = "No";
            editor.putString("vehicle_defect",vehicle_defect);
            editor.apply();
        }
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

    private String convertTOBase64Image(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.NO_WRAP);
        return encoded;
    }

    private void loadWMComments(int vehicle_id){
        Query loadWMCommentsQuery = vehicleDefectsWMReference.whereEqualTo("vehicle_id",vehicle_id).whereEqualTo("element_type","vehicle").whereEqualTo("close_issue","No");
        loadWMCommentsQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot: task.getResult()){
                        String element_name = documentSnapshot.getString("element_name");
                        int elementId = Objects.requireNonNull(documentSnapshot.getLong("element_id")).intValue();
                        String wm_resolution = documentSnapshot.getString("wm_resolution");
                        String previous_inspection_id = documentSnapshot.getString("inspection_id");
                        elementNameWm.add(element_name);
                        elementIdWm.add(elementId);
                        if (!defectsList.isEmpty()) {
                            for (int i = 0; i < defectListAdapter.getItemCount(); i++) {
                                View view1 = rv_defects_list.getChildAt(i);
                                if (view1 != null) {
                                    tv_wm_comment = view1.findViewById(R.id.tv_manager_comment);
                                    tv_previousIns_id = view1.findViewById(R.id.tv_previousIns_id);
                                    int element_id = defectsListId.get(i);
                                    if (elementId == element_id) {
                                        tv_wm_comment.setText(wm_resolution);
                                        tv_previousIns_id.setText(previous_inspection_id);
                                    }
                                }
                                Log.d(TAG,"PREVIOUS_INS_ID :" + previous_inspection_id);
                            }
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
               Log.d(TAG,"ERROR :"+"Unable to get workshop manager comments.");
            }
        });
    }

    private void loadPreviousDefect(){
        if (!defectsListId.isEmpty()){
             for (int i=0; i<defectListAdapter.getItemCount();i++) {
                 int elementID = defectsListId.get(i);
                 getDefectedElements(elementID);
             }
        }
    }

    private void getDefectedElements(final int element_id){
        Query query = vehicleDefectsReference.whereEqualTo("element_id",element_id).whereEqualTo("vehicle_id",vehicle_id)
                .whereEqualTo("latest_defect","Yes").whereEqualTo("element_type","vehicle");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            String element_defect = queryDocumentSnapshot.getString("element_defect");
                            String conducted_on = queryDocumentSnapshot.getString("conducted_on");
                            String submission_time = queryDocumentSnapshot.getString("submission_time");
                            String inspection_id = queryDocumentSnapshot.getString("inspection_id");
                            Log.d(TAG,"ALL DETAILS :"+conducted_on+" "+submission_time+" "+inspection_id+" "+element_defect);
                            int elementId = Objects.requireNonNull(queryDocumentSnapshot.getLong("element_id")).intValue();
                            Log.d(TAG,"LIST OF ELEMENT DEFECT: " +elementId);
                            for (int i=0; i<defectListAdapter.getItemCount(); i++){
                                View view1 = rv_defects_list.getChildAt(i);
                                if (view1 != null) {
                                    tv_previous_defect = view1.findViewById(R.id.tv_previous_defect);
                                    int elementID = defectsListId.get(i);
                                    if (elementId==elementID) {
                                        tv_previous_defect.setText(element_defect);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"ERROR :"+"Unable to get defected elements.");
            }
        });
    }

    private void sendVehicleDefects(final int vehicle_id,int trailer_id,final int elementId, final String inspectionId, final String element_name, final String element_type, final String element_defect, final String defect_image1, final String defect_image2,
                                    final String additional_report, final String pre_existing_defect, final String workshop_manager_comment, final String previous_inspection_id, final String close_issue,
                                    String conducted_on,String submission_time,String latest_defect){
        progressDialog.dismiss();
        final Map<String,Object> vehicleDefects = new HashMap<>();
        vehicleDefects.put("vehicle_id",vehicle_id);
        vehicleDefects.put("trailer_id",trailer_id);
        vehicleDefects.put("element_id",elementId);
        vehicleDefects.put("inspection_id",inspectionId);
        vehicleDefects.put("element_name",element_name);
        vehicleDefects.put("element_type",element_type);
        vehicleDefects.put("element_defect",element_defect);
        vehicleDefects.put("defect_image1",defect_image1);
        vehicleDefects.put("defect_image2",defect_image2);
        vehicleDefects.put("additional_report",additional_report);
        vehicleDefects.put("pre_existing_defect",pre_existing_defect);
        vehicleDefects.put("workshop_manager_comment",workshop_manager_comment);
        vehicleDefects.put("previous_inspection_id",previous_inspection_id);
        vehicleDefects.put("close_issue",close_issue);
        vehicleDefects.put("conducted_on",conducted_on);
        vehicleDefects.put("submission_time",submission_time);
        vehicleDefects.put("latest_defect",latest_defect);
        vehicleDefects.put("id",inspection_id+"_"+id_count+++"_Vehicle");
        vehicleDefects.put("status","Active");
        vehicleDefects.put("is_updated","Yes");
        Query query = vehicleDefectsReference.whereEqualTo("inspection_id",inspection_id).whereEqualTo("element_type","vehicle");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    vehicleDefectsReference.document(inspection_id+"_"+doc_count+++"_Vehicle").set(vehicleDefects);
                    iv_next_page.setEnabled(true);
                    iv_next_page.setBackgroundResource(R.drawable.arrow);
                    count = element_list_size;
                    visited= 1;
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                viewPager.setCurrentItem(1);
                iv_next_page.setEnabled(false);
                iv_next_page.setBackgroundResource(R.drawable.arrow_unselect);
                alertDialogFailed("Unfortunately, Crate App has stopped working. You may have lost your Data.Please try again.");
            }
        });
    }

    private void deleteVehicleDocument(){
        Query deleteQuery = vehicleDefectsReference.whereEqualTo("inspection_id",inspection_id).whereEqualTo("element_type","vehicle");
        deleteQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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

    private void alertDialogFailed(String message){
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        fragment = new DashboardFragment();
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
                if (viewPager.getCurrentItem() == 1)
                    mainActivity.getChildTag(TAG);
            }
            id_count = 0;
            doc_count = 0;
            HashMap<String, String> vehicle = sessionManager.getVehicleDetails();
            vehicle_id = Integer.parseInt(Objects.requireNonNull(vehicle.get(SessionManager.KEY_VEHICLE_ID)));
            Log.d(TAG,"Vehicle Id:" +vehicle_id);
            Log.d(TAG,"Count Value :" +count);
            sessionManager.updateVehicleId(vehicle_id);
            loadWMComments(vehicle_id);
            loadPreviousDefect();
            deleteVehicleDocument();
            vehicleElementDefectList.clear();
            viewPager.setAllowedSwipeDirection(CustomViewPagerInspection.SwipeDirection.NONE);
        }
    }
}
