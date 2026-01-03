package com.crate.crateam.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.crate.crateam.R;
import com.crate.crateam.model.ViewAdHocDefects;
import com.crate.crateam.utility.SessionManager;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class ViewAdHocDefectsAdapter extends FirestoreRecyclerAdapter<ViewAdHocDefects, ViewAdHocDefectsAdapter.ViewAdHocDefectsListItem> {
    private Context context;
    private OnImageClickListener mlistener;

    public ViewAdHocDefectsAdapter(@NonNull FirestoreRecyclerOptions options, Context context) {
        super(options);
        this.context = context;
    }

    public interface OnImageClickListener{
        void onImageClick(int position);
    }

    public void setOnItemClickListener(OnImageClickListener listener){
        mlistener = listener;
    }

    @Override
    public ViewAdHocDefectsAdapter.ViewAdHocDefectsListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_ad_hoc_defects_row, parent, false);
        return new ViewAdHocDefectsAdapter.ViewAdHocDefectsListItem(view,mlistener);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewAdHocDefectsListItem viewAdHocDefectsListItem, int i, @NonNull ViewAdHocDefects viewAdHocDefects) {
        SessionManager sessionManager = new SessionManager(context);
        HashMap<String, String> user = sessionManager.getUserDetails();
        String user_role = user.get(SessionManager.KEY_ROLE_ONE);
        int role_id = Integer.valueOf(user_role);
        if (role_id == 3){
            viewAdHocDefectsListItem.ll_resolution.setVisibility(View.VISIBLE);
        }
        viewAdHocDefectsListItem.tv_element_name.setText("Element name : "+viewAdHocDefects.getElement_name());
        viewAdHocDefectsListItem.tv_element_type.setText("Element type : "+viewAdHocDefects.getElement_type());
        viewAdHocDefectsListItem.tv_defect_description.setText("Element defects : "+viewAdHocDefects.getElement_defect());
        viewAdHocDefectsListItem.tv_wm_comment.setText("Workshop Manager Comment : "+viewAdHocDefects.getWorkshop_manager_comment());
        Bitmap decodedImage1 = decodeImageString(viewAdHocDefects.getDefect_image1());
        viewAdHocDefectsListItem.iv_photo_one.setImageBitmap(decodedImage1);
        Bitmap decodedImage2 = decodeImageString(viewAdHocDefects.getDefect_image2());
        viewAdHocDefectsListItem.iv_photo_two.setImageBitmap(decodedImage2);
        viewAdHocDefectsListItem.iv_photo_two.setImageBitmap(decodedImage2);
    }

    public class ViewAdHocDefectsListItem extends RecyclerView.ViewHolder {
        private TextView tv_element_name;
        private TextView tv_element_type;
        private TextView tv_defect_description;
        private TextView tv_wm_comment;
        private ImageView iv_photo_one,iv_photo_two ;
        private LinearLayout ll_resolution;
        private EditText et_resolution;

        private ViewAdHocDefectsListItem(View itemView,final OnImageClickListener listener) {
            super(itemView);
            ll_resolution = itemView.findViewById(R.id.ll_resolution);
            tv_element_name = itemView.findViewById(R.id.tv_element_name);
            tv_element_type = itemView.findViewById(R.id.tv_element_type);
            tv_defect_description = itemView.findViewById(R.id.tv_defect_description);
            tv_wm_comment = itemView.findViewById(R.id.tv_wm_comment);
            iv_photo_one = itemView.findViewById(R.id.iv_photo_one);
            iv_photo_two = itemView.findViewById(R.id.iv_photo_two);
            et_resolution = itemView.findViewById(R.id.et_resolution);
            iv_photo_one.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener!= null){
                        int position = getAdapterPosition();
                        if (position!=RecyclerView.NO_POSITION){
                            listener.onImageClick(position);
                        }
                    }
                }
            });
            iv_photo_two.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener!= null){
                        int position = getAdapterPosition();
                        if (position!=RecyclerView.NO_POSITION){
                            listener.onImageClick(position);
                        }
                    }
                }
            });
        }
    }

    private Bitmap decodeImageString(String imageString){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] imageBytes = baos.toByteArray();
        imageBytes = Base64.decode(imageString, Base64.DEFAULT);
        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        return decodedImage;
    }
}
