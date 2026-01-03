package com.crate.crateam.fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.bumptech.glide.Glide;
import com.crate.crateam.R;
import com.crate.crateam.activities.AssetInspection;
import com.crate.crateam.activities.MainActivity;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class SettingsFragment extends Fragment implements View.OnClickListener {
    private ImageView iv_camera,iv_profile_pic;
    private RelativeLayout rl_change_pass,rl_feedback,rl_terms_condition;
    Fragment fragment = null;
    private int user_id;
    String path1="";
    SharedPreferences.Editor editor;
    private ProgressDialog progressDialog;
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private static final int camera_request_code=100;
    private File photo_file=null;
    private Uri selected_uri=null;
    private Uri send_uri = null;

    public static final String TAG = "SettingsFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initializeOnClick();
    }

    private void initView(View view){
        SessionManager sessionManager = new SessionManager(getActivity());
        progressDialog = Dialog.showProgressDialog(getActivity());
        rl_change_pass = view.findViewById(R.id.rl_change_pass);
        rl_feedback = view.findViewById(R.id.rl_feedback);
        rl_terms_condition = view.findViewById(R.id.rl_terms_condition);
        iv_camera = view.findViewById(R.id.iv_camera);
        iv_profile_pic = view.findViewById(R.id.iv_profile_pic);
        TextView tv_userName = view.findViewById(R.id.tv_userName);
        HashMap<String,String> user = sessionManager.getUserDetails();
        String user_name = user.get(SessionManager.KEY_FULL_NAME);
        user_id = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        if (user_name != null) {
            if (!user_name.isEmpty())
                tv_userName.setText(user_name);
        }
        progressDialog.show();
        if (AppData.internetOnline(getActivity()))
            retrieveImage();
        else
            progressDialog.dismiss();
    }

    private void initializeOnClick(){
        rl_change_pass.setOnClickListener(this);
        rl_feedback.setOnClickListener(this);
        rl_terms_condition.setOnClickListener(this);
        iv_camera.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        MainActivity mainActivity= (MainActivity) getActivity();
        switch (view.getId()) {
            case R.id.rl_change_pass:
                fragment = new ChangePassword();
                ft.replace(R.id.content_frame, fragment, "CHANGE PASSWORD");
                ft.addToBackStack("CHANGE PASSWORD");
                ft.commit();
                mainActivity.getChildTag(fragment.getTag());
                break;
            case R.id.rl_feedback:
                fragment = new SubmitFeedback();
                ft.replace(R.id.content_frame, fragment, "SUBMIT FEEDBACK");
                ft.addToBackStack("SUBMIT FEEDBACK");
                ft.commit();
                mainActivity.getChildTag(fragment.getTag());
                break;
            case R.id.rl_terms_condition:
                fragment = new TermsCondition();
                ft.replace(R.id.content_frame, fragment, "TERMS CONDITION");
                ft.addToBackStack("TERMS CONDITION");
                ft.commit();
                mainActivity.getChildTag(fragment.getTag());
                break;
            case R.id.iv_camera:
                openCamera();
                break;
        }
    }
    private void openCamera() {
        photo_file = createImageFile();
        selected_uri= FileProvider.getUriForFile(getActivity(),"com.crate.crateam.provider",photo_file);
        send_uri=selected_uri;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,selected_uri);
        startActivityForResult(intent, camera_request_code);
    }

    private File createImageFile(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFilename = "image" +timeStamp+ "_";
        File storagedir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File image =File.createTempFile(imageFilename,".png",storagedir);
            return image;
        } catch (IOException e) {
            Log.e(TAG, "An unexpected error occurred", e);
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==camera_request_code){
            if (resultCode==-1) {
                currentPosition(photo_file);
                uploadImage(photo_file);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==camera_request_code){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                openCamera();
            }
        }
    }
    private void currentPosition(File imageFile) {
                setImageInPlaceHolder(iv_profile_pic,imageFile.getAbsolutePath());
                path1=imageFile.getPath();
    }

    private void setImageInPlaceHolder(ImageView imageInPlaceHolder, String filepath){
        Glide.with(this).load("file://"+filepath).circleCrop().into(imageInPlaceHolder);
    }

    private void uploadImage(File imageFile){
        if (send_uri!=null) {
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            StorageReference sr = storageReference.child("images/"+user_id +"/profile_pic");
            sr.putFile(Uri.fromFile(imageFile)).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Upload Failed.", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            Toast.makeText(getActivity(), "Please select an image first", Toast.LENGTH_SHORT).show();
        }
    }

    private void retrieveImage(){
        progressDialog.setTitle("Downloading image...");
        StorageReference tr=storageReference.child("images/"+user_id+"/profile_pic");
        tr.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                progressDialog.dismiss();
                String str=uri.toString();
                Log.d("jkkhj :" ,str);
                Glide.with(getActivity()).load(str).placeholder(R.drawable.user).circleCrop().into(iv_profile_pic);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Error :", e.getMessage());
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();

            }
        });
    }
}
