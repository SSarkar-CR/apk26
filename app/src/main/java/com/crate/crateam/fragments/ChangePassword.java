package com.crate.crateam.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.crate.crateam.R;
import com.crate.crateam.activities.MainActivity;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class ChangePassword extends Fragment {
    private SessionManager sessionManager;
    private PopupWindow popupWindow;
    private Fragment fragment = null;
    private ImageView back_arrow;
    private String tag,new_pass;
    private int user_id =0;
    private EditText et_new_password,et_confirm_password;
    private ProgressDialog progressDialog;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference userDetailsReference;
    private String user_password="",email="";
    private FirebaseUser user;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.change_password_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirestoreManager.initPersistentIndexManager();
        userDetailsReference = db.collection("CR_user_details");
        sessionManager = new SessionManager(getActivity());
        progressDialog = Dialog.showProgressDialog(getActivity());
        Button bt_save = view.findViewById(R.id.bt_save);
        et_new_password = view.findViewById(R.id.et_new_password);
        et_confirm_password = view.findViewById(R.id.et_confirm_password);
        back_arrow = view.findViewById(R.id.iv_back_arrow);
        user = FirebaseAuth.getInstance().getCurrentUser();
        HashMap<String, String> user = sessionManager.getUserDetails();
        final String user_email = user.get(SessionManager.KEY_EMAIL);
        final String user_name = user.get(SessionManager.KEY_USER_NAME);
        user_password = user.get(SessionManager.KEY_PASSWORD);
        Log.e("user_password",user_password);
        user_id = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        Log.d(TAG,"UserId :" +user_id);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        bt_save.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                new_pass = et_new_password.getText().toString();
                String confirm_pass = et_confirm_password.getText().toString();
                    Log.d(TAG,"CHANGE_PASS :" +user_id + " " +user_email + " "+new_pass+ " "+confirm_pass);
                    if (new_pass.isEmpty())
                        Dialog.alertDialog(getActivity(),"Please enter new password.");
                    else if (new_pass.length()<8 || new_pass.length()>32)
                        Dialog.alertDialog(getActivity(),"Password must be between 8 to 32 characters long.");
                    else if (!Pattern.compile( "[0-9]" ).matcher(new_pass).find())
                        Dialog.alertDialog(getActivity(),"Password must be combined with characters and at least one digit.");
                    else if (confirm_pass.isEmpty())
                        Dialog.alertDialog(getActivity(),"Please enter confirm password.");
                    else if (!confirm_pass.equals(new_pass))
                        Dialog.alertDialog(getActivity(),"Confirm password does not match.");
                    else if (new_pass.equals(user_password))
                        Dialog.alertDialog(getActivity(),"New password can not be same as current password.");
                    else if (new_pass.equals(user_name))
                        Dialog.alertDialog(getActivity(),"Password and username can not be same.");
                    else {
                        if (AppData.internetOnline(getActivity())) {
                            progressDialog.show();
                            updatePassword();
                        }
                        else
                            Dialog.alertDialog(getActivity(),"Please check internet connection.");
                    }
            }
        });
    }

    private void updateUserDetails(){
        progressDialog.show();
        Query query = userDetailsReference.whereEqualTo("id",user_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
              if (task.isSuccessful()){
                  for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                      String doc_id = documentSnapshot.getId();
                      Map<String, Object> changePass = new HashMap<>();
                      changePass.put("password", new_pass);
                      String updated_at = dateTime();
                      changePass.put("updated_at",updated_at);
                      changePass.put("is_updated","Yes");
                      userDetailsReference.document(doc_id).update(changePass);
                      openDialog("Password changed successfully.");
                      sessionManager.updatePassword(new_pass);
                  }
              }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Dialog.alertDialog(getActivity(),"Unable to change password.");
            }
        });
    }

    private void openDialog(String message) {
        final View popupView = getLayoutInflater().inflate(R.layout.change_pass_popup_layout,null,true);
        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,true);
        popupWindow.showAtLocation(popupView, Gravity.BOTTOM,0,0);
        Button cancelPopup = popupView.findViewById(R.id.bt_ok);
        TextView tv_change_pass = popupView.findViewById(R.id.tv_change_pass);
        tv_change_pass.setText(message);
        cancelPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_new_password.setText("");
                et_confirm_password.setText("");
                popupWindow.dismiss();
                fragment = new SettingsFragment();
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, fragment,"SETTINGS");
                ft.addToBackStack("SETTINGS");
                ft.commit();
                tag = fragment.getTag();
                Log.d(TAG,"CHILD_TAG" +fragment.getTag());
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.getChildTag(tag);
            }
        });
    }
    private void updatePassword(){
        if (user.getEmail()!= null)
            email = user.getEmail();
        AuthCredential credential = EmailAuthProvider.getCredential(email,user_password);
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    user.updatePassword(new_pass).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful())
                                updateUserDetails();
                            else
                                Dialog.alertDialog(getActivity(),"Something went wrong. Please try again");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String dateTime(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
       String date = df.format(c);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String time = simpleDateFormat.format(calendar.getTime());
       String updated_time = date+" "+time;
       return updated_time;
    }
}
