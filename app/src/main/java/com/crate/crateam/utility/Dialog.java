package com.crate.crateam.utility;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.crate.crateam.R;
import com.crate.crateam.activities.AssetManagementDashboard;
import com.crate.crateam.activities.EndOfDayActivity;
import com.crate.crateam.activities.MainActivity;
import com.crate.crateam.adapters.DefaultViewDieselDeliveryAdapter;

public class Dialog {

    public static DefaultViewDieselDeliveryAdapter.AddViewDieselDelivery addViewDieselDeliveryInterface = null;

    public static ProgressDialog showProgressDialog(Context context){
        ProgressDialog progressDialog = new ProgressDialog(context, R.style.progress_bar_style);
        progressDialog.setIndeterminate(true);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setIndeterminateDrawable(context.getResources().getDrawable(R.drawable.progress_animation, null));
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    public static ProgressDialog showProgressDialogWithMessage(Context context){
        ProgressDialog progressDialog = new ProgressDialog(context, R.style.progress_bar_style);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Syncing data , please wait...");
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setIndeterminateDrawable(context.getResources().getDrawable(R.drawable.progress_animation, null));
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    public static void DismissProgressDialog(ProgressDialog progressDialog,Context context){
        Handler pdCanceller = new Handler();
        Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    Toast.makeText(context,"Something went wrong.Please try again.",Toast.LENGTH_LONG).show();
                }
            }
        };
        pdCanceller.postDelayed(progressRunnable, 80000);
    }

    public static void readyToScanDialog(Context context) {
        final View popupView = LayoutInflater.from(context).inflate(R.layout.ready_to_scan_layout,null,true);
        final PopupWindow readyToScan = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,true);
        readyToScan.showAtLocation(popupView, Gravity.BOTTOM,0,0);
        Button cancelPopup = popupView.findViewById(R.id.bt_cancel_popup);
        cancelPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readyToScan.dismiss();
            }
        });
        dialogTimer(readyToScan);
    }

    public static void successfullyScanDialog(Context context) {
        @SuppressLint("InflateParams") final View popupView = LayoutInflater.from(context).inflate(R.layout.scan_success_layout,null,true);
        final PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,true);
        popupWindow.showAtLocation(popupView, Gravity.BOTTOM,0,0);
        Button bt_done = popupView.findViewById(R.id.bt_done);
        bt_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
        dialogTimer(popupWindow);
    }

    public static void endOfDayDialog(Context context) {
        @SuppressLint("InflateParams") final View popupView = LayoutInflater.from(context).inflate(R.layout.end_of_day_dialog,null,true);
        final PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,true);
        popupWindow.showAtLocation(popupView, Gravity.BOTTOM,0,0);
        Button bt_cancel = popupView.findViewById(R.id.bt_cancel);
        Button bt_end_of_day = popupView.findViewById(R.id.bt_end_of_day);
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
        bt_end_of_day.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                Intent intent = new Intent(context, EndOfDayActivity.class);
                context.startActivity(intent);
            }
        });
    }

    public static void alertDialog(Context context,String message){
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public static void alertDialogToDashBoard(final Context context, String message){
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(context, MainActivity.class);
                        context.startActivity(intent);
                    }
                });
        alertDialog.show();
        alertDialog.setCancelable(false);
    }

    public static void dialogTimer(final PopupWindow popupWindow){
        new CountDownTimer(3000, 1000) {@Override
        public void onTick(long millisUntilFinished) {
            // TODO Auto-generated method stub
        }
            @Override
            public void onFinish() {
                // TODO Auto-generated method stub
                popupWindow.dismiss();
            }
        }.start();
    }
}
