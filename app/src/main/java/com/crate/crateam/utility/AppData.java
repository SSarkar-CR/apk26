package com.crate.crateam.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AppData {

    public static final String TAG = "AppData";

    public static boolean internetOnline(Context c) {
        boolean status = false;
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                status = true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                status = true;
            }
        } else {
            // not connected to the internet
            status = false;
        }
        return status;
    }

    public static long getTimeSecond() {
        Date c1 = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String date_one = simpleDateFormat.format(c1);

        Calendar calendar_one = Calendar.getInstance();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss");
        String time = dateFormatter.format(calendar_one.getTime());

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String dateInString = date_one + " " + time;
        Date date = new Date();
        try {
            date = sdf.parse(dateInString);
        } catch (ParseException e) {
            Log.e(TAG, "An unexpected error occurred", e);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getTimeInMillis();
    }

    public static long getDateFormatTwo(String regime_date){
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date();
        try {
            date = formatter.parse(regime_date);
        } catch (ParseException e) {
            Log.e(TAG, "An unexpected error occurred", e);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Log.d("CALENDER","BHKkk :" +calendar.getTimeInMillis());
        return calendar.getTimeInMillis();
    }

    public static int getDateMonthYearFormat(){
        Date c1 = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        String date_one= simpleDateFormat.format(c1);
        return Integer.parseInt(date_one);
    }

    public static String changeDateFormat(String date) {
        String inputPattern = "dd-MM-yyyy";
        String outputPattern = "yyyyMMdd";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
        Date date_fomat = null;
        String str = null;
        try {
            date_fomat = inputFormat.parse(date);
            str = outputFormat.format(date_fomat);
        } catch (ParseException e) {
            Log.e(TAG, "An unexpected error occurred", e);
        }
        return str;
    }

    public static String date() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String current_date = simpleDateFormat.format(date);
        return current_date;
    }

    public static String Time() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat time_format = new SimpleDateFormat("hh:mm a");
        String current_time = time_format.format(calendar.getTime());
        return current_time;
    }

    public static String convertTOBase64Image(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return encoded;
    }

}
