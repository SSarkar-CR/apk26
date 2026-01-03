package com.crate.crateam.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.crate.crateam.R;
import com.crate.crateam.model.ViewInspectionElementDefects;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import java.io.ByteArrayOutputStream;

public class ViewInspectionElementAdapter extends FirestoreRecyclerAdapter<ViewInspectionElementDefects, ViewInspectionElementAdapter.ViewAdHocDefectsListItem> {
    private Context context;
    private OnImageClickListener mlistener;

    public ViewInspectionElementAdapter(@NonNull FirestoreRecyclerOptions options, Context context) {
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
    public ViewInspectionElementAdapter.ViewAdHocDefectsListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_inspection_element_row, parent, false);
        return new ViewInspectionElementAdapter.ViewAdHocDefectsListItem(view,mlistener);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewAdHocDefectsListItem viewAdHocDefectsListItem, int i, @NonNull ViewInspectionElementDefects viewInspectionElementDefects) {
        viewAdHocDefectsListItem.tv_element_name.setText("Element name : "+viewInspectionElementDefects.getElement_name());
        if (viewInspectionElementDefects.getElement_defect().equals(""))
            viewAdHocDefectsListItem.tv_defect_description.setText("Element defects : Not Defected");
        else
            viewAdHocDefectsListItem.tv_defect_description.setText("Element defects : "+viewInspectionElementDefects.getElement_defect());
        if (viewInspectionElementDefects.getDefect_comment().equals("")){
            viewAdHocDefectsListItem.tv_defect_image.setVisibility(View.GONE);
            viewAdHocDefectsListItem.ll_defect_image.setVisibility(View.GONE);
        }else {
            viewAdHocDefectsListItem.tv_wm_comment.setText("Workshop Manager Comment : "+viewInspectionElementDefects.getDefect_comment());
            viewAdHocDefectsListItem.tv_defect_image.setVisibility(View.VISIBLE);
            viewAdHocDefectsListItem.ll_defect_image.setVisibility(View.VISIBLE);
        }
        if (viewInspectionElementDefects.getZ_image_one()!=null) {
            Bitmap decodedImage1 = decodeImageString(viewInspectionElementDefects.getZ_image_one());
            viewAdHocDefectsListItem.iv_photo_one.setImageBitmap(decodedImage1);
        }
        if (viewInspectionElementDefects.getZ_image_two()!=null) {
            Bitmap decodedImage2 = decodeImageString(viewInspectionElementDefects.getZ_image_two());
            viewAdHocDefectsListItem.iv_photo_two.setImageBitmap(decodedImage2);
        }
    }

    public class ViewAdHocDefectsListItem extends RecyclerView.ViewHolder {
        private TextView tv_element_name,tv_defect_image;
        private TextView tv_defect_description;
        private TextView tv_wm_comment;
        private ImageView iv_photo_one,iv_photo_two ;
        private LinearLayout ll_defect_image;

        private ViewAdHocDefectsListItem(View itemView,final OnImageClickListener listener) {
            super(itemView);
            tv_element_name = itemView.findViewById(R.id.tv_element_name);
            tv_defect_description = itemView.findViewById(R.id.tv_defect_description);
            tv_defect_image = itemView.findViewById(R.id.tv_defect_image);
            tv_wm_comment = itemView.findViewById(R.id.tv_wm_comment);
            ll_defect_image = itemView.findViewById(R.id.ll_defect_image);
            iv_photo_one = itemView.findViewById(R.id.iv_photo_one);
            iv_photo_two = itemView.findViewById(R.id.iv_photo_two);
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
