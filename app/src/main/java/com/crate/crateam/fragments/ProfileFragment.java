package com.crate.crateam.fragments;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.crate.crateam.R;
import com.crate.crateam.utility.Dialog;
import com.crate.crateam.utility.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import java.util.Objects;

public class ProfileFragment extends Fragment {
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private int user_id;
    private ImageView iv_profile_picture;
    private ProgressDialog progressDialog;

    protected static final String TAG = "Profile";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.profile_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view){
        SessionManager sessionManager = new SessionManager(getActivity());
        progressDialog = Dialog.showProgressDialog(getActivity());
        iv_profile_picture = view.findViewById(R.id.iv_profile_picture);
        TextView tv_userName = view.findViewById(R.id.tv_userName);
        TextView tv_userRole = view.findViewById(R.id.tv_userRole);
        TextView tv_user_id = view.findViewById(R.id.tv_user_id);
        TextView tv_email = view.findViewById(R.id.tv_email);
        HashMap<String,String> user = sessionManager.getUserDetails();
        String user_name = user.get(SessionManager.KEY_USER_NAME);
        String user_full_name = user.get(SessionManager.KEY_FULL_NAME);
        String user_email = user.get(SessionManager.KEY_EMAIL);
        user_id = Integer.parseInt(Objects.requireNonNull(user.get(SessionManager.KEY_ID)));
        String user_role_first = user.get(SessionManager.KEY_ROLE_ONE);
        if (user_role_first.equals(String.valueOf(4)))
            user_role_first = "Driver";
        else if (user_role_first.equals(String.valueOf(3)))
            user_role_first = "Workshop Manager";
        else if (user_role_first.equals(String.valueOf(2)))
            user_role_first = "Transport Manager";
        else if (user_role_first.equals(String.valueOf(1)))
            user_role_first = "Administrator";
        else if (user_role_first.equals(String.valueOf(5)))
            user_role_first = "Gate Keeper";

        if (user_full_name != null) {
            if (!user_full_name.isEmpty())
                tv_userName.setText(user_full_name);
        }
        tv_email.setText(user_email);
        tv_user_id.setText(user_name);
        if (user_role_first != null)
         tv_userRole.setText(user_role_first);
        downloadProfilePic();
    }

    private void downloadProfilePic(){
        storageReference.child("images/" +user_id+"/profile_pic").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                progressDialog.dismiss();
                String downloadUri = uri.toString();
                Log.d(TAG,"URL :" +downloadUri);
                Picasso.with(getActivity())
                        .load(downloadUri)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .placeholder(R.drawable.user)
                        .into(iv_profile_picture);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                progressDialog.dismiss();
                Log.e("Download Pic Error :" , exception.getMessage());
            }
        });
    }
}
