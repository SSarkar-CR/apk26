package com.crate.crateam.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.crate.crateam.R;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheIndexManager;
import com.crate.crateam.utility.FirestoreManager;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class SubmitFeedback extends Fragment {
    private PopupWindow popupWindow;
    private ProgressDialog progressDialog;
    private SessionManager sessionManager;
    private int user_id;
    private String title,content;
    private EditText et_feedback_title,et_feedback_body;
    private FirebaseFirestore db = FirestoreManager.getInstance();
    private CollectionReference feedbackReference;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.feedback_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirestoreManager.initPersistentIndexManager();
        feedbackReference = db.collection("CR_feedback_submission");
        sessionManager = new SessionManager(getActivity());
        progressDialog = Dialog.showProgressDialog(getActivity());
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_id = Integer.valueOf(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        et_feedback_title = view.findViewById(R.id.et_feedback_title);
        et_feedback_body = view.findViewById(R.id.et_feedback_body);
        Button bt_submit_feedback = view.findViewById(R.id.bt_submit_feedback);
        bt_submit_feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title = et_feedback_title.getText().toString();
                content = et_feedback_body.getText().toString();
                if (title.equals(""))
                    Dialog.alertDialog(getActivity(),"Title is required.");
                else if (content.equals(""))
                    Dialog.alertDialog(getActivity(),"Please write some feedback to submit.");
                else
                    sendFeedback();
            }
        });
    }

    private void sendFeedback(){
        Date c = Calendar.getInstance().getTime();
        Log.d(TAG,"Current time => " + c);
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String date = df.format(c);
        DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
        final String uniqueString = dateFormat.format(c) ;
        final Map<String,Object> feedbackData = new HashMap<>();
        feedbackData.put("user_id",user_id);
        feedbackData.put("title",title);
        feedbackData.put("feedback",content);
        feedbackData.put("date",date);
        feedbackData.put("is_updated","Yes");
        feedbackReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    feedbackReference.document(user_id+"_"+uniqueString+"_Feedback").set(feedbackData);
                    progressDialog.dismiss();
                    openDialog("Feedback submitted successfully.");
                    et_feedback_title.setText("");
                    et_feedback_body.setText("");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Dialog.alertDialog(getActivity(),"Unable to submit feedback.");
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
                popupWindow.dismiss();
                getActivity().onBackPressed();
            }
        });
    }
}
