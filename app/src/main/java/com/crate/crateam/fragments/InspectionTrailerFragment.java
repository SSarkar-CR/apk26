package com.crate.crateam.fragments;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
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
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheIndexManager;
import com.crate.crateam.utility.FirestoreManager;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class InspectionTrailerFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "INSPECTION_TRAILER_FRAG";
    private ProgressDialog progressDialog;
    private Fragment fragment = null;
    private RecyclerView rv_defects_list;
    private LinearLayout ll_trailer_defects,ll_element_list,ll_additional,ll_nfc_trailer,ll_nfc_trailer_near,ll_nfc_trailer_off,ll_previous_defect;
    private RelativeLayout rl_manager_comment;
    private DefectListAdapter defectListAdapter;
    private Switch switch_trailer;
    private RadioButton rb_additional,rb_reported;
    private TextView tv_near_tag_trailer, tv_off_tag_trailer,tv_previous_page,tv_previous_defect;
    private ImageView iv_nfcTag_near_trailer,iv_nfcTag_off_trailer,iv_cross;
    private ImageView iv_one,iv_two, iv_close_one,iv_close_two,imageView1,imageView2,iv_next_page,iv_previous_page;
    private String tag,elementName,elementDefect,trailer_defect,add_reported,wm_comment,pre_existing_defect,
            previous_inspection_id,nfc_data="",trailer,conducted_on="",submission_time="",trailer_near="",trailer_off="",attach_trailers="",with_nfc="";
    private Bitmap getDrawable1, getDrawable2;
    private boolean hasImage = false;
    private boolean hasImageTwo = true;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor ;
    private Boolean CheckEditTextEmpty ;
    private int count=0,click = 0 ,click_yes = 0,doc_count=0,id_count=0, visited = 0,vehicle_id=0,elementId=0,trailer_id=0,element_list_size=0;
    private CustomViewPagerInspection viewPager;
    private CheckBox cb_checked;
    private EditText et_defect_details ;
    private TextView tv_wm_comment,tv_previousIns_id;
    private ArrayList<String> defectsListTrailer = new ArrayList<>();
    private ArrayList<Integer> defectsListIdTrailer = new ArrayList<>();
    private String defect_image1 = "",defect_image2 = "",inspection_id;
    private SessionManager sessionManager;
    private ArrayList<Integer> elementIdWm = new ArrayList<>();
    private ArrayList<String> elementResolutionWm = new ArrayList<>();
    private ArrayList<String> previousInspectionIdWm = new ArrayList<>();
    private ArrayList<Integer> defectedElementId = new ArrayList<>();
    private ArrayList<String> defectedElementName = new ArrayList<>();
    private ArrayList<String> trailerElementDefectList = new ArrayList<>();
    MainActivity mainActivity ;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference vehicleDefectsReference,vehicleDefectsWMReference,trailerReference,vehicleTrailerMappingReference,vehicleElementsReference;
    private boolean tag_exists = false;
    private Uri imageUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.inspection_trailer_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setUpTag();
        initializeOnClick();
    }

    private void initView(View view){
        FirestoreManager.initPersistentIndexManager();
        vehicleDefectsReference = db.collection("CR_vehicle_defect_inspections");
        vehicleDefectsWMReference = db.collection("CR_vehicle_defects_wm");
        trailerReference = db.collection("CR_trailer_details");
        vehicleTrailerMappingReference = db.collection("CR_vehicle_trailer_mapping");
        vehicleElementsReference = db.collection("CR_vehicle_elements");
        sessionManager = new SessionManager(getActivity());
        progressDialog = Dialog.showProgressDialog(getActivity());
        HashMap<String, String> vehicle = sessionManager.getVehicleDetails();
        if (vehicle.get(SessionManager.KEY_VEHICLE_ID)!= null)
         vehicle_id = Integer.parseInt(Objects.requireNonNull(vehicle.get(SessionManager.KEY_VEHICLE_ID)));
        pref = getActivity().getSharedPreferences("MyPref", 0); // 0 - for private mode
        inspection_id= pref.getString("inspection_id",null);
        editor = pref.edit();
        iv_cross = getActivity().findViewById(R.id.iv_cross);
        viewPager = getActivity().findViewById(R.id.viewpager);
        viewPager.setAllowedSwipeDirection(CustomViewPagerInspection.SwipeDirection.NONE);
        iv_previous_page= view.findViewById(R.id.iv_previous_page);
        tv_previous_page = view.findViewById(R.id.tv_previous_page);
        iv_next_page = view.findViewById(R.id.iv_next_page);
        iv_next_page.setBackgroundResource(R.drawable.arrow);
        switch_trailer = view.findViewById(R.id.switch_trailer);
        switch_trailer.setChecked(false);
        iv_nfcTag_near_trailer = view.findViewById(R.id.iv_nfcTag_near_trailer);
        iv_nfcTag_off_trailer = view.findViewById(R.id.iv_nfcTag_off_trailer);
        tv_near_tag_trailer = view.findViewById(R.id.tv_near_tag_trailer);
        tv_off_tag_trailer = view.findViewById(R.id.tv_off_tag_trailer);
        ll_trailer_defects = view.findViewById(R.id.ll_trailer_defects);
        ll_nfc_trailer = view.findViewById(R.id.ll_nfc_trailer);
        ll_nfc_trailer.setVisibility(View.GONE);
        ll_element_list = view.findViewById(R.id.ll_element_list);
        ll_element_list.setVisibility(View.GONE);
        ll_nfc_trailer_near = view.findViewById(R.id.ll_nfc_trailer_near);
        ll_nfc_trailer_off = view.findViewById(R.id.ll_nfc_trailer_off);
        rv_defects_list = view.findViewById(R.id.rv_defects_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv_defects_list.setLayoutManager(layoutManager);
        getTrailerElements();
        mainActivity = (MainActivity) getActivity();
        iv_previous_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(1);
            }
        });
        iv_next_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count = 5;
                mainActivity.getCount(count);
                editor.putString("trailer_defect","No");
                editor.remove("nfc5");
                editor.remove("nfc4");
                editor.apply();
                viewPager.setCurrentItem(3);
            }
        });
    }

    private void onAdapterPositionClick(){
        final InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        element_list_size = defectsListTrailer.size();
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
                View view = getActivity().getCurrentFocus();
                if (view != null) {
                    assert imm != null;
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                if (viewPager.getCurrentItem()==2) {
                    if (!cb_checked.isChecked()) {
                        cb_checked.setChecked(true);
                        click++;
                        click_yes++;
                    }
                    if (click_yes >= element_list_size && click != click_yes)
                        enableNextIcon();
                    else if (click >= element_list_size && click != click_yes)
                        enableNextIcon();
                    else if (click == click_yes && click >= element_list_size )
                        enableNextIcon();
                }
                Log.d(TAG,"DETECT CLICK : " +click);
                Log.d(TAG,"DETECT CLICK_YES : " +click_yes);
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
                tv_previousIns_id = rv_defects_list.getChildAt(position).findViewById(R.id.tv_previousIns_id);
                et_defect_details = rv_defects_list.getChildAt(position).findViewById(R.id.et_defect_details);
                et_defect_details.requestFocus();
                assert imm != null;
                imm.showSoftInput(et_defect_details, 0);
                setPreviousDefect(position);
                setWMComments(position);
                String wm_comment = tv_wm_comment.getText().toString();
                String element_defect = et_defect_details.getText().toString();
                String previous_defect = tv_previous_defect.getText().toString();
                CheckEditTextIsEmptyOrNot(element_defect);
                Log.d(TAG,"WM_comment Trailer :" +element_defect+" "+previous_defect);
                if (previous_defect.equals("No") || previous_defect.equals(""))
                    ll_previous_defect.setVisibility(View.GONE);
                else
                    ll_previous_defect.setVisibility(View.VISIBLE);
                if (!wm_comment.equals(""))
                    rl_manager_comment.setVisibility(View.VISIBLE);
                else
                    ll_additional.setVisibility(View.VISIBLE);

                if (viewPager.getCurrentItem()==2) {
                    if (!cb_checked.isChecked()) {
                        cb_checked.setChecked(true);
                        click++;
                    }
                    if (click >= element_list_size) {
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
                sendTrailerData();
                viewPager.setCurrentItem(3);
                count = 5;
                mainActivity.getCount(count);
            }
        });
    }

    private void setUpTag(){
        if (getArguments()!= null){
            nfc_data = this.getArguments().getString("message");
            count = this.getArguments().getInt("counter");
            Log.d(TAG,"NFC_Data Trailer: "  +nfc_data+" "+count);
        }
        if (count==3 || count==0) {
            iv_previous_page.setVisibility(View.VISIBLE);
            tv_previous_page.setVisibility(View.VISIBLE);
        }else {
            iv_previous_page.setVisibility(View.GONE);
            tv_previous_page.setVisibility(View.GONE);
        }
        if (viewPager.getCurrentItem()==2) {
            checkVehicleTrailerMapping(vehicle_id);
        }
    }

    private void checkVehicleTrailerMapping(int vehicle_id){
        progressDialog.show();
        Query query = vehicleTrailerMappingReference.whereEqualTo("vehicle_id",vehicle_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            attach_trailers = queryDocumentSnapshot.getString("attached_trailer_ids");
                            Log.d(TAG,"Attach Trailer :" + attach_trailers);
                            getTrailerNfc(attach_trailers);
                        }
                    }else {
                        progressDialog.dismiss();
                        Dialog.alertDialog(getActivity(), "Tag not recognised.Please scan again.");
                        count = count - 1;
                        mainActivity.getCount(count);
                        switch_trailer.setChecked(true);
                        ll_nfc_trailer.setVisibility(View.VISIBLE);
                        iv_next_page.setEnabled(true);
                        iv_next_page.setBackgroundResource(R.drawable.arrow);
                        setNfcDataFromSharePreference();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(),"Unable to fetch attach trailer nfc tag.",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTrailerNfc(final String attach_trailers){
        Task task1 = trailerReference.whereEqualTo("status","Active").whereEqualTo("trailer_near_rear",nfc_data).get();
        Task task2 = trailerReference.whereEqualTo("status","Active").whereEqualTo("trailer_off_rear",nfc_data).get();
        final Task<List<QuerySnapshot>> allTask = Tasks.whenAllSuccess(task1,task2);
        allTask.addOnSuccessListener(new OnSuccessListener<List<QuerySnapshot>>() {
            @Override
            public void onSuccess(List<QuerySnapshot> querySnapshots) {
                for (QuerySnapshot querySnapshot : querySnapshots){
                    for (QueryDocumentSnapshot queryDocumentSnapshot :querySnapshot){
                        if (queryDocumentSnapshot.exists()){
                            trailer_id = Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue();
                            Log.d(TAG,"Attach Trailers Ids :" +attach_trailers+" "+trailer_id);
                            if (attach_trailers.contains(String.valueOf(trailer_id))) {
                                tag_exists = true;
                                trailer_near = queryDocumentSnapshot.getString("trailer_near_rear");
                                trailer_off = queryDocumentSnapshot.getString("trailer_off_rear");
                                Log.d(TAG,"Trailer Near Off :" +trailer_near+" "+trailer_off);
                                setTrailerNfcData();
                                saveToSession(String.valueOf(trailer_id),trailer_near,trailer_off);
                            }
                        }
                    }
                }
                if (!tag_exists){
                    progressDialog.dismiss();
                    Dialog.alertDialog(getActivity(),"Tag not recognised.Please scan again.");
                    count = count - 1;
                    mainActivity.getCount(count);
                    switch_trailer.setChecked(true);
                    ll_nfc_trailer.setVisibility(View.VISIBLE);
                    iv_next_page.setEnabled(true);
                    iv_next_page.setBackgroundResource(R.drawable.arrow);
                    setNfcDataFromSharePreference();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(),"Unable to get trailer details.",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveToSession(String trailer_id,String trailer_near,String trailer_off){
        SharedPreferences pref = getActivity().getSharedPreferences(SessionManager.PREFER_NAME, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(SessionManager.KEY_TRAILER_ID,trailer_id);
        editor.putString(SessionManager.KEY_TRAILER_NEAR,trailer_near);
        editor.putString(SessionManager.KEY_TRAILER_OFF,trailer_off);
        editor.apply();
    }

    private void setTrailerNfcData(){
        progressDialog.dismiss();
        HashMap<String, String> vehicle = sessionManager.getVehicleDetails();
        trailer = vehicle.get(SessionManager.KEY_TRAILER);
        Log.d(TAG,"Trailer Connected : " +trailer +" "+trailer_near+" "+trailer_off);
        if (trailer != null) {
                if (trailer.equals("Yes")) {
                    if (nfc_data != null && nfc_data.equals(trailer_near) || nfc_data.equals(trailer_off)) {
                        if (count == 4 || count == 5) {
                            switch_trailer.setChecked(true);
                            iv_next_page.setEnabled(false);
                            iv_next_page.setBackgroundResource(R.drawable.arrow_unselect);
                            if (nfc_data.equals(trailer_near)) {
                                Dialog.successfullyScanDialog(getActivity());
                                ll_nfc_trailer_near.setBackgroundColor(Color.parseColor("#00CC66"));
                                tv_near_tag_trailer.setVisibility(View.VISIBLE);
                                tv_near_tag_trailer.setText(trailer_near);
                                editor.putString("nfc4", trailer_near);
                                editor.apply();
                                setNfcDataFromSharePreference();
                            } else if (nfc_data.equals(trailer_off)) {
                                Dialog.successfullyScanDialog(getActivity());
                                ll_nfc_trailer_off.setBackgroundColor(Color.parseColor("#00CC66"));
                                tv_off_tag_trailer.setVisibility(View.VISIBLE);
                                tv_off_tag_trailer.setText(nfc_data);
                                editor.putString("nfc5", trailer_off);
                                editor.apply();
                                setNfcDataFromSharePreference();
                            }
                        }
                        else {
                            Dialog.alertDialog(getActivity(),"Tag not recognised.Please scan again.");
                            count = 3;
                            mainActivity.getCount(count);
                            switch_trailer.setChecked(true);
                            ll_nfc_trailer.setVisibility(View.VISIBLE);
                            iv_next_page.setEnabled(true);
                            iv_next_page.setBackgroundResource(R.drawable.arrow);
                            setNfcDataFromSharePreference();
                        }
                        if (tv_near_tag_trailer.getText().toString().equals(trailer_near) && tv_off_tag_trailer.getText().toString().equals(trailer_off)){
                            ll_element_list.setVisibility(View.VISIBLE);
                            ll_nfc_trailer.setVisibility(View.GONE);
                            count = 5;
                            mainActivity.getCount(count);
                            switch_trailer.setEnabled(false);
                        } else if (tv_near_tag_trailer.getText().toString().equals(trailer_near) || tv_off_tag_trailer.getText().toString().equals(trailer_off)){
                            ll_element_list.setVisibility(View.GONE);
                            ll_nfc_trailer.setVisibility(View.VISIBLE);
                            count = 4;
                            mainActivity.getCount(count);
                        }
                    }else {
                        Dialog.alertDialog(getActivity(),"Tag not recognised.Please scan again.");
                        count = 3;
                        mainActivity.getCount(count);
                        switch_trailer.setChecked(true);
                        ll_nfc_trailer.setVisibility(View.VISIBLE);
                        iv_next_page.setEnabled(true);
                        iv_next_page.setBackgroundResource(R.drawable.arrow);
                        setNfcDataFromSharePreference();
                    }
                }
        }
    }

    private void setNfcDataFromSharePreference(){
        if (pref.getString("nfc5", null)!=null){
            tv_off_tag_trailer.setText(pref.getString("nfc5", null));
            tv_off_tag_trailer.setVisibility(View.VISIBLE);
            ll_nfc_trailer_off.setBackgroundColor(Color.parseColor("#00CC66"));
        }
        if (pref.getString("nfc4", null)!=null) {
            tv_near_tag_trailer.setText(pref.getString("nfc4", null));
            tv_near_tag_trailer.setVisibility(View.VISIBLE);
            ll_nfc_trailer_near.setBackgroundColor(Color.parseColor("#00CC66"));
        }
    }

    private void initializeOnClick(){
        iv_nfcTag_near_trailer.setOnClickListener(this);
        iv_nfcTag_off_trailer.setOnClickListener(this);
        switch_trailer.setOnCheckedChangeListener(this);
        iv_cross.setOnClickListener(this);
    }

    public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
        if (trailer != null) {
            if (trailer.equals("Yes")) {
                if (isChecked) {
                    switch_trailer.setChecked(true);
                    if (with_nfc.equals("Yes")) {
                        ll_nfc_trailer.setVisibility(View.VISIBLE);
                        ll_element_list.setVisibility(View.GONE);
                    }else {
                        ll_nfc_trailer.setVisibility(View.GONE);
                        ll_element_list.setVisibility(View.VISIBLE);
                    }
                    if (count!=5 && with_nfc.equals("Yes"))
                        ll_nfc_trailer.setVisibility(View.VISIBLE);
                    else
                        ll_element_list.setVisibility(View.VISIBLE);
                    ll_trailer_defects.setVisibility(View.VISIBLE);
                    iv_next_page.setEnabled(false);
                    iv_next_page.setBackgroundResource(R.drawable.arrow_unselect);
                    Log.d(TAG,"You are :"+ "Checked");
                }else {
                    ll_trailer_defects.setVisibility(View.VISIBLE);
                    iv_next_page.setEnabled(true);
                    iv_next_page.setBackgroundResource(R.drawable.arrow);
                    ll_nfc_trailer.setVisibility(View.GONE);
                    ll_element_list.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_cross:
                alertDialogFailed("Do you want to cancel inspection ?");
                break;
            case R.id.iv_nfcTag_near_trailer:
            case R.id.iv_nfcTag_off_trailer:
                Dialog.readyToScanDialog(getActivity());
                break;
        }
    }
    private void CheckEditTextIsEmptyOrNot(String elementDefect){
        if(TextUtils.isEmpty(elementDefect))
            CheckEditTextEmpty = false ;
        else
            CheckEditTextEmpty = true ;
    }

    private void getTrailerElements(){
        Query query = vehicleElementsReference.whereEqualTo("status","Active").whereEqualTo("car_type","Trailer").orderBy("id");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        defectsListTrailer.add(queryDocumentSnapshot.getString("element"));
                        defectsListIdTrailer.add(Objects.requireNonNull(queryDocumentSnapshot.getLong("id")).intValue());
                    }
                    Log.d(TAG,"DEFECT LIST Trailer:" +defectsListTrailer+" "+defectsListIdTrailer+" "+vehicle_id);
                    defectListAdapter = new DefectListAdapter(defectsListTrailer);
                    rv_defects_list.setAdapter(defectListAdapter);
                    if (defectListAdapter.getItemCount() != 0) {
                        loadPreviousDefect();
                    }
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

    private void sendTrailerData(){
        for (int i = 0; i < defectListAdapter.getItemCount(); i++) {
            View view1 = rv_defects_list.getChildAt(i);
            if (view1 != null) {
                rb_additional = view1.findViewById(R.id.rb_additional);
                rb_reported = view1.findViewById(R.id.rb_reported);
                et_defect_details = view1.findViewById(R.id.et_defect_details);
                tv_previousIns_id = view1.findViewById(R.id.tv_previousIns_id);
                tv_wm_comment = view1.findViewById(R.id.tv_manager_comment);
                imageView1 = view1.findViewById(R.id.iv_photo_one);
                imageView2 = view1.findViewById(R.id.iv_photo_two);
            }
            elementDefect = et_defect_details.getText().toString();
            previous_inspection_id = tv_previousIns_id.getText().toString();
            wm_comment = tv_wm_comment.getText().toString();
            elementName = defectsListTrailer.get(i);
            Bitmap image_one =  ((BitmapDrawable) imageView1.getDrawable()).getBitmap();
            Bitmap image_two =  ((BitmapDrawable) imageView2.getDrawable()).getBitmap();
            Bitmap placeholder= BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_image);
            String placeholder_image = convertTOBase64Image(placeholder);
            String image1 = convertTOBase64Image(getResizedBitmap(image_one,600));
            if (image1.equals(placeholder_image))
                defect_image1 = "";
            String image2 = convertTOBase64Image(getResizedBitmap(image_two,600));
            if (defect_image2.equals(placeholder_image))
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
                trailerElementDefectList.add(elementDefect);
                int element_id = defectsListIdTrailer.get(i);
                Log.d(TAG,"TRAILER_ELEMENT_DEFECT :" +element_id + " "+ inspection_id +" "+elementName+ " "+ elementDefect + "  " + defectsListTrailer.get(i) + " " + add_reported + " " + pre_existing_defect + " " + wm_comment + " " + previous_inspection_id);
                progressDialog.show();
                sendTrailerDefects(vehicle_id,trailer_id, element_id, inspection_id, elementName, "trailer", elementDefect, defect_image1, defect_image2,
                        add_reported, pre_existing_defect, wm_comment, previous_inspection_id,"No",conducted_on,submission_time,"");
                trailer_defect = "Yes";
                editor.putString("trailer_defect",trailer_defect);
                editor.apply();
            }
        }
        if (trailerElementDefectList.isEmpty()){
            sendTrailerDefects(vehicle_id,trailer_id,0, inspection_id, "No", "trailer", "No", "No",
                    "No", "No", "No", "No", "No","No",conducted_on,submission_time,"");
            trailer_defect = "No";
            editor.putString("trailer_defect",trailer_defect);
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

    private void loadPreviousDefect(){
        if (!defectsListIdTrailer.isEmpty()) {
            for (int i = 0; i < defectListAdapter.getItemCount(); i++) {
                int elementID = defectsListIdTrailer.get(i);
                getDefectedElements(elementID);
                loadWMComments(elementID);
            }
        }
    }

    private void loadWMComments(int element_id){
        Query loadWMCommentsQuery = vehicleDefectsWMReference.whereEqualTo("element_id",element_id).whereEqualTo("vehicle_id",vehicle_id).whereEqualTo("element_type","trailer").whereEqualTo("close_issue","No");
        loadWMCommentsQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot: task.getResult()){
                        String element_name = documentSnapshot.getString("element_name");
                        int elementId = Objects.requireNonNull(documentSnapshot.getLong("element_id")).intValue();
                        String wm_resolution = documentSnapshot.getString("wm_resolution");
                        Log.d(TAG,"WM_COMMENT :" + wm_resolution);
                        String previous_inspection_id = documentSnapshot.getString("inspection_id");
                        elementResolutionWm.add(wm_resolution);
                        elementIdWm.add(elementId);
                        previousInspectionIdWm.add(previous_inspection_id);
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

    private void getDefectedElements(final int element_id){
        Query query = vehicleDefectsReference.whereEqualTo("element_id",element_id).whereEqualTo("vehicle_id",vehicle_id)
                .whereEqualTo("latest_defect","Yes").whereEqualTo("element_type","trailer");
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
                            int elementId = Objects.requireNonNull(queryDocumentSnapshot.getLong("element_id")).intValue();
                            Log.d(TAG,"ALL DETAILS :"+conducted_on+" "+submission_time+" "+inspection_id+" "+element_defect);
                            defectedElementId.add(elementId);
                            defectedElementName.add(element_defect);
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

    private void setWMComments(int position){
        for (int i=0; i<defectListAdapter.getItemCount();i++){
            int elementId = defectsListIdTrailer.get(position);
            for (int j=0; j<elementIdWm.size();j++){
                int defected_element_id = elementIdWm.get(j);
                if (elementId==defected_element_id){
                    Log.d(TAG,"ELEMENT RESOLUTIONS TRAILER: " +elementResolutionWm.get(j));
                    tv_wm_comment.setText(elementResolutionWm.get(j));
                    tv_previousIns_id.setText(previousInspectionIdWm.get(j));
                }
            }
        }
    }

    private void setPreviousDefect(int position){
        for (int i=0; i<defectListAdapter.getItemCount();i++){
            int elementId = defectsListIdTrailer.get(position);
            for (int j=0; j<defectedElementId.size();j++){
                int defected_element_id = defectedElementId.get(j);
                if (elementId==defected_element_id){
                    Log.d(TAG,"DEFECTED ELEMENT TRAILER: " +defectedElementName.get(j));
                    tv_previous_defect.setText(defectedElementName.get(j));
                }
            }
        }
    }

    private void sendTrailerDefects(int vehicle_id,int trailer_id,int element_id, String inspectionId, String element_name, String element_type, String element_defect, String defect_image1, String defect_image2,
                                    String additional_report,String pre_existing_defect, String workshop_manager_comment,String previous_inspection_id,String close_issue,
                                    String conducted_on,String submission_time,String latest_defect){
        progressDialog.dismiss();
        final Map<String,Object> trailerDefects = new HashMap<>();
        trailerDefects.put("vehicle_id",vehicle_id);
        trailerDefects.put("trailer_id",trailer_id);
        trailerDefects.put("element_id",element_id);
        trailerDefects.put("inspection_id",inspectionId);
        trailerDefects.put("element_name",element_name);
        trailerDefects.put("element_type",element_type);
        trailerDefects.put("element_defect",element_defect);
        trailerDefects.put("defect_image1",defect_image1);
        trailerDefects.put("defect_image2",defect_image2);
        trailerDefects.put("additional_report",additional_report);
        trailerDefects.put("pre_existing_defect",pre_existing_defect);
        trailerDefects.put("workshop_manager_comment",workshop_manager_comment);
        trailerDefects.put("previous_inspection_id",previous_inspection_id);
        trailerDefects.put("close_issue",close_issue);
        trailerDefects.put("conducted_on",conducted_on);
        trailerDefects.put("submission_time",submission_time);
        trailerDefects.put("latest_defect",latest_defect);
        trailerDefects.put("id",inspection_id+"_"+id_count+++"_Trailer");
        trailerDefects.put("status","Active");
        trailerDefects.put("is_updated","Yes");
        Query query = vehicleDefectsReference.whereEqualTo("inspection_id",inspection_id).whereEqualTo("element_type","trailer");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.e("Message :","Trailer defects submitted successfully.");
                    vehicleDefectsReference.document(inspection_id+"_"+doc_count+++"_Trailer").set(trailerDefects);
                    iv_next_page.setEnabled(true);
                    iv_next_page.setBackgroundResource(R.drawable.arrow);
                    count = element_list_size;
                    visited = 1;
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                viewPager.setCurrentItem(2);
                iv_next_page.setEnabled(false);
                iv_next_page.setBackgroundResource(R.drawable.arrow_unselect);
                alertDialogFailed("Unfortunately, Crate App has stopped working. You may have lost your Data.Please try again.");
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
                        deleteDocument();
                        fragment = new com.crate.crateam.fragments.DashboardFragment();
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

    private void deleteTrailerDocument(){
        Query deleteQuery = vehicleDefectsReference.whereEqualTo("inspection_id",inspection_id).whereEqualTo("element_type","trailer");
        deleteQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    String document_id = documentSnapshot.getId();
                    vehicleDefectsReference.document(document_id).delete();
                }
            }
        });
    }

    private void deleteDocument(){
        Query deleteQuery = vehicleDefectsReference.whereEqualTo("inspection_id",inspection_id);
        deleteQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    String document_id = documentSnapshot.getId();
                    vehicleDefectsReference.document(document_id).delete();
                }
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (viewPager != null) {
                if (viewPager.getCurrentItem() == 2)
                    mainActivity.getChildTag(TAG);
            }
            if (sessionManager != null) {
                HashMap<String, String> vehicle = sessionManager.getVehicleDetails();
                trailer = vehicle.get(SessionManager.KEY_TRAILER);
                with_nfc = pref.getString("with_nfc",null);
                Log.d(TAG,"With NFC : " +with_nfc);
            }
            if (trailer != null) {
                if (trailer.equals("Yes")) {
                    if (!tv_near_tag_trailer.getText().toString().isEmpty() && !tv_off_tag_trailer.getText().toString().isEmpty()){
                        count = 5;
                        mainActivity.getCount(count);
                    }else if (!tv_near_tag_trailer.getText().toString().isEmpty() || !tv_off_tag_trailer.getText().toString().isEmpty()){
                        count = 4;
                        mainActivity.getCount(count);
                    }else {
                        count = 3;
                        mainActivity.getCount(count);
                    }
                    if (viewPager != null) {
                        viewPager.setAllowedSwipeDirection(CustomViewPagerInspection.SwipeDirection.NONE);
                    }
                    id_count = 0;
                    doc_count = 0;
                    Log.d(TAG,"COUNTER :" +count);
                    Log.d(TAG,"You are :"+ "Checked");
                    deleteTrailerDocument();
                    trailerElementDefectList.clear();
                }
            }
        }
    }
}
