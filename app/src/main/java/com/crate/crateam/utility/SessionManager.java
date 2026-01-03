package com.crate.crateam.utility;

import java.util.HashMap;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.crate.crateam.activities.LogInActivity;

public class SessionManager {
    private SharedPreferences pref;
    private Editor editor;
    private Context _context;
    private int PRIVATE_MODE = 0;
    public static final String PREFER_NAME = "Preference";
    // All Shared Preferences Keys
    private static final String IS_USER_LOGIN = "IsUserLoggedIn";
    public static final String KEY_FNAME = "first_name";
    public static final String KEY_LNAME = "last_name";
    public static final String KEY_FULL_NAME = "full_name";
    public static final String KEY_ID = "id";
    public static final String KEY_VEHICLE_ID = "vehicle_id";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_COMPANY_NAME = "company_name";
    public static final String KEY_ADDRESS1 = "address1";
    public static final String KEY_ADDRESS2 = "address2";
    public static final String KEY_MOBILE = "mobile";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_CITY = "city";
    public static final String KEY_COUNTRY = "country";
    public static final String KEY_ACCESS_MODULE = "access_module";
    public static final String KEY_STATUS = "status";
    public static final String KEY_CREATED = "created_at";
    public static final String KEY_UPDATED = "updated_at";
    public static final String KEY_REGISTRATION_ID = "registration_id";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_PASSWORD = "password_show";
    public static final String KEY_LAST_LOGIN = "last_login_date";
    public static final String KEY_ASSET_ROLE = "asset_role";
    public static final String KEY_ROLE_ONE = "role";
    public static final String KEY_ASSET_ACCESS = "asset_access";
    public static final String KEY_REGISTRATION_NO = "registration_no";
    public static final String KEY_HAULIER ="haulier";
    public static final String KEY_HAULIER_CARRIER ="haulier_carrier";
    public static final String KEY__MANUFACTURER = "manufacturer";
    public static final String KEY_MODEL = "model";
    public static final String KEY_TRAILER = "trailer";
    public static final String KEY_VEHICLE_TYPE = "vehicle_type";
    public static final String KEY_DRIVER_NAME = "driver_name";
    public static final String KEY_VEHICLE_FRONT = "vehicle_front";
    public static final String KEY_VEHICLE_NEAR = "vehicle_near";
    public static final String KEY_VEHICLE_OFF = "vehicle_off";
    public static final String KEY_VEHICLE_COMPLETION = "vehicle_completion";
    public static final String KEY_TRAILER_ID ="trailer_id";
    public static final String KEY_TRAILER_NEAR = "trailer_near";
    public static final String KEY_TRAILER_OFF = "trailer_off";

    // add these key for app access
    public static final String KEY_NFC_TAG_MANAGER_ACCESS = "NFC_tag_manager";
    public static final String KEY_VEHICLE_INSPECTION_ACCESS = "vehicle_inspection";
    public static final String KEY_VIEW_INSPECTION_ACCESS = "view_inspection";
    public static final String KEY_MANAGER_COMMENT_ACCESS = "manager_comment";
    public static final String KEY_AD_HOC_DEFECT_REPORT_ACCESS = "Ad_hoc_defect_report";
    public static final String KEY_VIEW_AD_HOC_ACCESS = "view_ad_hoc";
    public static final String KEY_WASTE_TRANSFER_FOR_OPERATOR_ACCESS = "waste_transfer_form_operator";
    public static final String KEY_WASTE_TRANSFER_FORM_DRIVER_ACCESS = "waste_transfer_form_driver";
    public static final String KEY_SUPPLY_FORM_OPERATOR_ACCESS = "supply_form_operator";
    public static final String KEY_SUPPLY_FORM_DRIVER_ACCESS = "supply_form_driver";
    public static final String KEY_DIESEL_DELIVERY_ACCESS = "diesel_delivery";
    public static final String KEY_SERVICE_DRIVER_NOTE_ACCESS = "service_driver_note";
    public static final String KEY_ASSIGN_SITE_ACCESS = "assign_site";
    public static final String KEY_VIEW_REPORTS_ACCESS = "view_reports";

    // add these key for asset access
    public static final String KEY_ASSIGN_SITE_ASSET = "assign_site_asset";
    public static final String KEY_ASSET_DETAILS = "asset_details";
    public static final String KEY_INSPECTION_ELEMENTS = "inspection_elements";
    public static final String KEY_DEFECT_CLOSE_OUT = "defect_close_out";
    public static final String KEY_INSPECT_ASSET = "inspect_asset";
    public static final String KEY_SEARCH_FOR_ASSET = "search_for_asset";
    public static final String KEY_NEW_ASSET= "new_asset";
    public static final String KEY_MANAGE_ASSET = "manage_asset";
    public static final String KEY_MY_TASK = "my_task";


    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }
    //Create login session
    public void createUserLoginSession(String name, String email,String id,String company_name,String address1,String address2,String mobile,String image,String city,String country,String access_module,
                                      String status,String created_at,String updated_at,String registration_id,String user_name,String password,String last_login_date,String asset_role,String role,String asset_access){
        editor.putBoolean(IS_USER_LOGIN, true);
        editor.putString(KEY_FULL_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_ID, id);
        editor.putString(KEY_COMPANY_NAME, company_name);
        editor.putString(KEY_ADDRESS1, address1);
        editor.putString(KEY_ADDRESS2, address2);
        editor.putString(KEY_MOBILE, mobile);
        editor.putString(KEY_IMAGE, image);
        editor.putString(KEY_CITY, city);
        editor.putString(KEY_COUNTRY, country);
        editor.putString(KEY_ACCESS_MODULE, access_module);
        editor.putString(KEY_STATUS, status);
        editor.putString(KEY_CREATED, created_at);
        editor.putString(KEY_UPDATED, updated_at);
        editor.putString(KEY_REGISTRATION_ID, registration_id);
        editor.putString(KEY_USER_NAME, user_name);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_LAST_LOGIN, last_login_date);
        editor.putString(KEY_ASSET_ROLE, asset_role);
        editor.putString(KEY_ROLE_ONE,role);
        editor.putString(KEY_ASSET_ACCESS,asset_access);
        editor.apply();
    }

    public void createAppViewControllerSession( String no24,String no25, String no26,String no27, String no28,String no29, String no30,String no31,
                                                String no32,String no33, String no34,String no35, String no36,String no37){
        editor.putString(KEY_NFC_TAG_MANAGER_ACCESS,no24);
        editor.putString(KEY_VEHICLE_INSPECTION_ACCESS,no25);
        editor.putString(KEY_VIEW_INSPECTION_ACCESS,no26);
        editor.putString(KEY_MANAGER_COMMENT_ACCESS,no27);
        editor.putString(KEY_AD_HOC_DEFECT_REPORT_ACCESS,no28);
        editor.putString(KEY_VIEW_AD_HOC_ACCESS,no29);
        editor.putString(KEY_WASTE_TRANSFER_FOR_OPERATOR_ACCESS,no30);
        editor.putString(KEY_WASTE_TRANSFER_FORM_DRIVER_ACCESS,no31);
        editor.putString(KEY_SUPPLY_FORM_OPERATOR_ACCESS,no32);
        editor.putString(KEY_SUPPLY_FORM_DRIVER_ACCESS,no33);
        editor.putString(KEY_DIESEL_DELIVERY_ACCESS,no34);
        editor.putString(KEY_SERVICE_DRIVER_NOTE_ACCESS,no35);
        editor.putString(KEY_ASSIGN_SITE_ACCESS,no36);
        editor.putString(KEY_VIEW_REPORTS_ACCESS,no37);
        editor.apply();
    }

    public void createAssetViewControllerSession( String no3,String no4, String no5,String no12, String no15,String no16, String no17,String no18, String no19){
        editor.putString(KEY_ASSIGN_SITE_ASSET,no3);
        editor.putString(KEY_ASSET_DETAILS,no4);
        editor.putString(KEY_INSPECTION_ELEMENTS,no5);
        editor.putString(KEY_DEFECT_CLOSE_OUT,no12);
        editor.putString(KEY_INSPECT_ASSET,no15);
        editor.putString(KEY_SEARCH_FOR_ASSET,no16);
        editor.putString(KEY_NEW_ASSET,no17);
        editor.putString(KEY_MANAGE_ASSET,no18);
        editor.putString(KEY_MY_TASK,no19);
        editor.apply();
    }

    public void createVehicleSession(String registration_no,String vehicle_id,String haulier,String haulier_carrier,String manufacturer,String model,String trailer,String vehicle_type,String vehicle_front,String vehicle_near,String vehicle_off,String vehicle_completion,
                                     String trailer_id,String trailer_near,String trailer_off){
        editor.putBoolean(IS_USER_LOGIN, true);
        editor.putString(KEY_REGISTRATION_NO, registration_no);
        editor.putString(KEY_VEHICLE_ID,vehicle_id);
        editor.putString(KEY_HAULIER, haulier);
        editor.putString(KEY_HAULIER_CARRIER, haulier_carrier);
        editor.putString(KEY__MANUFACTURER, manufacturer);
        editor.putString(KEY_MODEL, model);
        editor.putString(KEY_TRAILER, trailer);
        editor.putString(KEY_VEHICLE_TYPE, vehicle_type);
        editor.putString(KEY_VEHICLE_FRONT, vehicle_front);
        editor.putString(KEY_VEHICLE_NEAR, vehicle_near);
        editor.putString(KEY_VEHICLE_OFF, vehicle_off);
        editor.putString(KEY_VEHICLE_COMPLETION, vehicle_completion);
        editor.putString(KEY_TRAILER_ID, trailer_id);
        editor.putString(KEY_TRAILER_NEAR, trailer_near);
        editor.putString(KEY_TRAILER_OFF, trailer_off);
        editor.apply();
    }
    /**
     * Check login method will check user login status
     * If false it will redirect user to login page
     * Else do anything
     * */
    public boolean checkLogin(){
        // Check login status
        if(!this.isUserLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, LogInActivity.class);
            // Closing all the Activities from stack
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // Staring Login Activity
            _context.startActivity(i);
            return true;
        }
        return false;
    }
    /*** Get stored user session data* */
    public HashMap<String, String> getUserDetails(){
        //Use hashmap to store user credentials
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(KEY_FULL_NAME, pref.getString(KEY_FULL_NAME, null));
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
        user.put(KEY_PASSWORD, pref.getString(KEY_PASSWORD, null));
        user.put(KEY_USER_NAME, pref.getString(KEY_USER_NAME, null));
        user.put(KEY_ID, pref.getString(KEY_ID, null));
        user.put(KEY_REGISTRATION_ID, pref.getString(KEY_REGISTRATION_ID, null));
        user.put(KEY_MOBILE, pref.getString(KEY_MOBILE, null));
        user.put(KEY_IMAGE, pref.getString(KEY_IMAGE, null));
        user.put(KEY_STATUS, pref.getString(KEY_STATUS, null));
        user.put(KEY_COMPANY_NAME, pref.getString(KEY_COMPANY_NAME, null));
        user.put(KEY_COUNTRY, pref.getString(KEY_COUNTRY, null));
        user.put(KEY_CITY, pref.getString(KEY_CITY, null));
        user.put(KEY_ROLE_ONE,pref.getString(KEY_ROLE_ONE,null));
        user.put(KEY_ASSET_ROLE,pref.getString(KEY_ASSET_ROLE,null));
        user.put(KEY_ASSET_ACCESS,pref.getString(KEY_ASSET_ACCESS,null));
        return user;
    }

    public void updatePassword(String password){
        editor.putString(KEY_PASSWORD,password);
        editor.apply();
    }

    /*** Get stored vehicle session data* */
    public HashMap<String, String> getVehicleDetails(){
        HashMap<String, String> vehicle = new HashMap<String, String>();
        vehicle.put(KEY_REGISTRATION_NO, pref.getString(KEY_REGISTRATION_NO, null));
        vehicle.put(KEY_VEHICLE_ID, pref.getString(KEY_VEHICLE_ID, null));
        vehicle.put(KEY_HAULIER, pref.getString(KEY_HAULIER, null));
        vehicle.put(KEY_HAULIER_CARRIER, pref.getString(KEY_HAULIER_CARRIER, null));
        vehicle.put(KEY__MANUFACTURER, pref.getString(KEY__MANUFACTURER, null));
        vehicle.put(KEY_MODEL, pref.getString(KEY_MODEL, null));
        vehicle.put(KEY_TRAILER, pref.getString(KEY_TRAILER, null));
        vehicle.put(KEY_VEHICLE_TYPE, pref.getString(KEY_VEHICLE_TYPE, null));
        vehicle.put(KEY_VEHICLE_FRONT, pref.getString(KEY_VEHICLE_FRONT, null));
        vehicle.put(KEY_VEHICLE_NEAR, pref.getString(KEY_VEHICLE_NEAR, null));
        vehicle.put(KEY_VEHICLE_OFF, pref.getString(KEY_VEHICLE_OFF, null));
        vehicle.put(KEY_VEHICLE_COMPLETION, pref.getString(KEY_VEHICLE_COMPLETION, null));
        vehicle.put(KEY_TRAILER_ID, pref.getString(KEY_TRAILER_ID, null));
        vehicle.put(KEY_TRAILER_NEAR, pref.getString(KEY_TRAILER_NEAR, null));
        vehicle.put(KEY_TRAILER_OFF, pref.getString(KEY_TRAILER_OFF, null));
        return vehicle;
    }

    public void updateVehicleId(int vehicleId){
        editor.putString(KEY_VEHICLE_ID,String.valueOf(vehicleId));
        editor.apply();
    }

    public void removeVehicleId(){
        editor.remove(KEY_VEHICLE_ID);
        editor.remove(KEY_REGISTRATION_NO);
        editor.apply();
    }

    /*get value for transport session*/
    public HashMap<String, String> getAppViewController(){
        HashMap<String, String> app_controller = new HashMap<String, String>();
        app_controller.put(KEY_NFC_TAG_MANAGER_ACCESS, pref.getString(KEY_NFC_TAG_MANAGER_ACCESS, null));
        app_controller.put(KEY_VEHICLE_INSPECTION_ACCESS, pref.getString(KEY_VEHICLE_INSPECTION_ACCESS, null));
        app_controller.put(KEY_VIEW_INSPECTION_ACCESS, pref.getString(KEY_VIEW_INSPECTION_ACCESS, null));
        app_controller.put(KEY_MANAGER_COMMENT_ACCESS, pref.getString(KEY_MANAGER_COMMENT_ACCESS, null));
        app_controller.put(KEY_AD_HOC_DEFECT_REPORT_ACCESS, pref.getString(KEY_AD_HOC_DEFECT_REPORT_ACCESS, null));
        app_controller.put(KEY_WASTE_TRANSFER_FOR_OPERATOR_ACCESS, pref.getString(KEY_WASTE_TRANSFER_FOR_OPERATOR_ACCESS, null));
        app_controller.put(KEY_WASTE_TRANSFER_FORM_DRIVER_ACCESS, pref.getString(KEY_WASTE_TRANSFER_FORM_DRIVER_ACCESS, null));
        app_controller.put(KEY_SUPPLY_FORM_OPERATOR_ACCESS, pref.getString(KEY_SUPPLY_FORM_OPERATOR_ACCESS, null));
        app_controller.put(KEY_SUPPLY_FORM_DRIVER_ACCESS, pref.getString(KEY_SUPPLY_FORM_DRIVER_ACCESS, null));
        app_controller.put(KEY_DIESEL_DELIVERY_ACCESS, pref.getString(KEY_DIESEL_DELIVERY_ACCESS, null));
        app_controller.put(KEY_SERVICE_DRIVER_NOTE_ACCESS, pref.getString(KEY_SERVICE_DRIVER_NOTE_ACCESS, null));
        app_controller.put(KEY_ASSIGN_SITE_ACCESS, pref.getString(KEY_ASSIGN_SITE_ACCESS, null));
        app_controller.put(KEY_VIEW_REPORTS_ACCESS, pref.getString(KEY_VIEW_REPORTS_ACCESS, null));
        return app_controller;
    }

    /*get value for asset session*/
    public HashMap<String, String> getAssetAppViewController(){
        HashMap<String, String> app_controller = new HashMap<String, String>();
        app_controller.put(KEY_ASSIGN_SITE_ASSET, pref.getString(KEY_ASSIGN_SITE_ASSET, null));
        app_controller.put(KEY_ASSET_DETAILS, pref.getString(KEY_ASSET_DETAILS, null));
        app_controller.put(KEY_INSPECTION_ELEMENTS, pref.getString(KEY_INSPECTION_ELEMENTS, null));
        app_controller.put(KEY_DEFECT_CLOSE_OUT, pref.getString(KEY_DEFECT_CLOSE_OUT, null));
        app_controller.put(KEY_INSPECT_ASSET, pref.getString(KEY_INSPECT_ASSET, null));
        app_controller.put(KEY_SEARCH_FOR_ASSET, pref.getString(KEY_SEARCH_FOR_ASSET, null));
        app_controller.put(KEY_NEW_ASSET, pref.getString(KEY_NEW_ASSET, null));
        app_controller.put(KEY_MANAGE_ASSET, pref.getString(KEY_MANAGE_ASSET, null));
        app_controller.put(KEY_MY_TASK, pref.getString(KEY_MY_TASK, null));
        return app_controller;
    }

    /** Clear session details* */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
        // After logout redirect user to Loging Activity
        Intent i = new Intent(_context, LogInActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Staring Login Activity
        _context.startActivity(i);
    }
    // Check for login
    private boolean isUserLoggedIn(){
        return pref.getBoolean(IS_USER_LOGIN, false);
    }
}
