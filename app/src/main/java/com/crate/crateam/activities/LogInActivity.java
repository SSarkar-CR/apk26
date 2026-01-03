package com.crate.crateam.activities;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.crate.crateam.R;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.FirestoreManager;
import com.crate.crateam.utility.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.MemoryCacheSettings;
import com.google.firebase.firestore.PersistentCacheIndexManager;
import com.crate.crateam.utility.FirestoreManager;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.PersistentCacheSettings;
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

public class LogInActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tv_forgotPass;
    private Button bt_logIn;
    private EditText et_email;
    private EditText et_password;
    private String device_id, nfc_tag_manager = "", vehicle_inspection = "", view_inspection = "", manager_comment = "", ad_hoc_defect_report = "",
            view_ad_hoc = "", waste_transfer_form_operator="", waste_transfer_form_driver="",
            supply_form_operator="", supply_form_driver="", diesel_delivery="", service_driver_note="" ,assign_site="",view_reports="",
            asset_access="",type="",assign_location="",asset_details="",inspection_elements="",defect_close_out="",
            asset_inspection="",search_for_asset="",new_asset="",manage_asset="",my_task="";
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;
    private boolean onDoubleBackPress;
    private ProgressDialog progressDialog;
    private SessionManager sessionManager;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference userDetailsReference,userRoleReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        initView();
        initializeOnClick();
    }

    private void initView() {
        FirestoreManager.initPersistentIndexManager();
        userDetailsReference = db.collection("CR_user_details");
        userRoleReference = db.collection("CR_user_role");
        firebaseAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(getApplicationContext());
        progressDialog = Dialog.showProgressDialog(this);
        tv_forgotPass = findViewById(R.id.tv_forgotPass);
        bt_logIn = findViewById(R.id.bt_logIn);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        device_id =  Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
        //getChildMenuOptions();
        Log.e("device_id",device_id);
    }
    private void initializeOnClick(){
        tv_forgotPass.setOnClickListener(this);
        bt_logIn.setOnClickListener(this);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHONE_STATE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                doPermissionGrantedStuffs();
            else
                Log.e(TAG,"Permission not granted.");
        }
    }

    @SuppressLint("HardwareIds")
    public void doPermissionGrantedStuffs() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_PHONE_STATE}, 0);
            return;
        }
    }

    private void loadUserDetails(String email_id){
        Query userNameQuery = userDetailsReference.whereEqualTo("email_address", email_id).whereEqualTo("status","Active");
        userNameQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d("UUUUU 3:" , "789");
                    if (!task.getResult().isEmpty()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            if (document.exists()) {
                                String f_name = document.getString("first_name");
                                String l_name = document.getString("last_name");
                                String full_name = document.getString("full_name");
                                String id = Objects.requireNonNull(document.getLong("id")).toString();
                                String email_address = document.getString("email_address");
                                String company_name = document.getString("company_name");
                                String address_line1 = document.getString("address_line1");
                                String address_line2 = document.getString("address_line2");
                                String mobile_number = document.getString("mobile_number");
                                String display_image = document.getString("display_image");
                                String city = document.getString("city");
                                String country = document.getString("country");
                                String access_module = document.getLong("access_module").toString();
                                String status = document.getString("status");
                                String created_at = document.getString("created_at");
                                String updated_at = document.getString("updated_at");
                                String registration_id = document.getString("registration_id");
                                String asset_role = Objects.requireNonNull(document.getLong("asset_role")).toString();
                                String role = Objects.requireNonNull(document.getLong("role")).toString();
                                String last_login_date = document.getString("last_login_date");
                                String userName = document.getString("user_name");
                                String passWord = et_password.getText().toString();
                                if (!id.isEmpty()) {
                                    if (!document.contains("device_token")) {
                                        Map<String, Object> objectsMap = new HashMap<>();
                                        objectsMap.put("device_token", device_id);
                                        userDetailsReference.document(id).update(objectsMap);
                                        checkAppAccess(full_name, email_address, id, company_name, address_line1, address_line2, mobile_number, display_image, city, country, access_module,
                                                status, created_at, updated_at, registration_id, userName, passWord, last_login_date, asset_role,role);
                                    } else {
                                        String store_device_id = document.getString("device_token");
                                        if (store_device_id.isEmpty()) {
                                            Map<String, Object> objectsMap = new HashMap<>();
                                            objectsMap.put("device_token", device_id);
                                            userDetailsReference.document(id).update(objectsMap);
                                            checkAppAccess(full_name, email_address, id, company_name, address_line1, address_line2, mobile_number, display_image, city, country, access_module,
                                                    status, created_at, updated_at, registration_id, userName, passWord, last_login_date,asset_role, role);
                                        } else {
                                            Map<String, Object> objectsMap = new HashMap<>();
                                            objectsMap.put("device_token", device_id);
                                            userDetailsReference.document(id).update(objectsMap);
                                            checkAppAccess(full_name, email_address, id, company_name, address_line1, address_line2, mobile_number, display_image, city, country, access_module,
                                                    status, created_at, updated_at, registration_id, userName, passWord, last_login_date, asset_role,role);
                                        }
                                    }

                                } else {
                                    Log.d("UUUUU 3:" ,"123");
                                    alertDialog("Login Error");
                                }

                                Log.d("RESPONSE :" , id + " " + f_name + " " + l_name + " " + full_name + " " + email_address +  " " +
                                        " " + company_name + " " + address_line1 + " " + address_line2 + " " + mobile_number + " " + display_image
                                        + " " + city + " " + country + " " + access_module + " " + status + " " + created_at + " " + updated_at + " " + registration_id + " "
                                        +asset_role+" " + role);

                            }
                        }
                    }
                    else {
                        Log.d("UUUUU 3:" , "456");
                        Dialog.alertDialog(LogInActivity.this,"Login Error");
                    }
                } else {
                    progressDialog.dismiss();
                    Log.d("TAG", "Error getting user details : ", task.getException());
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_logIn:
                if (AppData.internetOnline(LogInActivity.this)) {
                    final String email_id = et_email.getText().toString();
                    String password = et_password.getText().toString();
                    progressDialog.show();
                    if (email_id.isEmpty())
                        alertDialog("Please enter user name.");
                    else if (password.isEmpty())
                        alertDialog("Please enter password.");
                    else {
                        firebaseAuth.signInWithEmailAndPassword(email_id, password).addOnCompleteListener(LogInActivity.this, new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {
                                    progressDialog.dismiss();
                                    Log.d("EMAIL :" , firebaseAuth.getCurrentUser().getEmail());
                                    loadUserDetails(firebaseAuth.getCurrentUser().getEmail());
                                } else {
                                    progressDialog.dismiss();
                                    alertDialog(task.getException().getMessage());
                                }
                            }
                        });
                    }
                }
                else
                    alertDialog("No internet connection.");
                break;
            case R.id.tv_forgotPass:

                break;
        }
    }

    @Override
    public void onBackPressed() {
        onApplicationBackPressed();
    }

    private void onApplicationBackPressed() {
        if (onDoubleBackPress) {
            this.moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
            return;
        }
        onDoubleBackPress = true;
        Toast.makeText(this, "Press Again To Exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onDoubleBackPress = false;
            }
        }, 2000);
    }

    private void alertDialog(String message){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        progressDialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public void onResume() {
        super.onResume();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
        if (nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    public void onPause() {
        super.onPause();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);
    }

    @SuppressLint("MissingSuperCall")
    public void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            // drop NFC events
        }
    }

    // check access app or not
    private void checkAppAccess( final String full_name, final String email_address, final String  id, final String company_name, final String address_line1, final String  address_line2,
                                 final String mobile_number, final String display_image, final String city, final String country, final String access_module,
                                 final String status, final String created_at, final String updated_at, final String registration_id, final String userName,
                                 final String passWord, final String last_login_date,final String asset_role, final String user_role_id){
        String role="";
        if (!user_role_id.equals("0"))
            role = user_role_id;
        else
            role = asset_role;
        userRoleReference.document(role).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()){
                        String app_access = documentSnapshot.getString("user_system_access");
                        String child_menu_options = documentSnapshot.getString("child_menu_options");
                        if (app_access.contains("2")){
                            Log.d("UUUUU :" ,user_role_id+" "+asset_role);
                            Log.e("app_access11","yes");
                            Log.d("UUUUU 3:" , type + " " + asset_access);
                            sessionManager.createUserLoginSession(full_name, email_address, id, company_name, address_line1, address_line2, mobile_number, display_image, city, country, access_module,
                                    status, created_at, updated_at, registration_id, userName, passWord, last_login_date, asset_role,user_role_id,asset_access);
                            if (!asset_role.equals("0")){
                                checkAssetManagementAccess(asset_role);
                            }
                            if (!user_role_id.equals("0")){
                                if (child_menu_options != null) {
                                    accessChildMenuOptions(child_menu_options);
                                }
                                sessionManager.createAppViewControllerSession(nfc_tag_manager,vehicle_inspection,view_inspection,manager_comment,ad_hoc_defect_report,view_ad_hoc,waste_transfer_form_operator,waste_transfer_form_driver,
                                        supply_form_operator,supply_form_driver,diesel_delivery,service_driver_note,assign_site,view_reports);
                                Intent toDashboard = new Intent(LogInActivity.this, MainActivity.class);
                                startActivity(toDashboard);
                            }else {
                                Intent toDashboard = new Intent(LogInActivity.this, AssetManagementDashboard.class);
                                startActivity(toDashboard);
                            }
                        }
                        else {
                            Log.e("app_access12", "No");
                            alertDialog("You don't have app access");
                        }
                    }
                }
            }
        });
    }

    private void checkAssetManagementAccess(String asset_role){
        userRoleReference.document(String.valueOf(asset_role)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        String type = documentSnapshot.getString("type");
                        String app_access = documentSnapshot.getString("user_system_access");
                        String child_menu_options = documentSnapshot.getString("child_menu_options");
                        Log.d("UUUUU :" + type , " " + child_menu_options);
                        if (child_menu_options != null) {
                            assetChildMenuOptions(child_menu_options);
                            sessionManager.createAssetViewControllerSession(assign_location,asset_details,inspection_elements,defect_close_out,
                                    asset_inspection,search_for_asset,new_asset,manage_asset,my_task);
                        }
                    }
                }
            }
        });
    }

    private void accessChildMenuOptions(String contain_child_menu_options){
        if (contain_child_menu_options.contains("24"))
            nfc_tag_manager = "yes";
        if (contain_child_menu_options.contains("25"))
            vehicle_inspection = "yes";
        if (contain_child_menu_options.contains("26"))
            view_inspection = "yes";
        if (contain_child_menu_options.contains("27"))
            manager_comment = "yes";
        if (contain_child_menu_options.contains("28"))
            ad_hoc_defect_report = "yes";
        if (contain_child_menu_options.contains("29"))
            view_ad_hoc = "yes";
        if (contain_child_menu_options.contains("30"))
            waste_transfer_form_operator = "yes";
        if (contain_child_menu_options.contains("31"))
            waste_transfer_form_driver = "yes";
        if (contain_child_menu_options.contains("32"))
            supply_form_operator = "yes";
        if (contain_child_menu_options.contains("33"))
            supply_form_driver = "yes";
        if (contain_child_menu_options.contains("34"))
            diesel_delivery = "yes";
        if (contain_child_menu_options.contains("35"))
            service_driver_note = "yes";
        if (contain_child_menu_options.contains("36"))
            assign_site = "yes";
        if (contain_child_menu_options.contains("37"))
            view_reports = "yes";
        Log.e("child_12",contain_child_menu_options);
    }

    private void assetChildMenuOptions(String contain_child_menu_options){
        if (contain_child_menu_options.contains("3"))
            assign_location = "yes";
        if (contain_child_menu_options.contains("4"))
            asset_details = "yes";
        if (contain_child_menu_options.contains("5"))
            inspection_elements = "yes";
        if (contain_child_menu_options.contains("12"))
            defect_close_out = "yes";
        if (contain_child_menu_options.contains("15"))
            asset_inspection = "yes";
        if (contain_child_menu_options.contains("16"))
            search_for_asset = "yes";
        if (contain_child_menu_options.contains("17"))
            new_asset = "yes";
        if (contain_child_menu_options.contains("18"))
            manage_asset = "yes";
        if (contain_child_menu_options.contains("19"))
            my_task = "yes";
        Log.d("Child_12 :",contain_child_menu_options);
    }

    private void dateTime(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String  date = df.format(c);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String time = simpleDateFormat.format(calendar.getTime());
    }
}