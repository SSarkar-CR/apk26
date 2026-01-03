package com.crate.crateam.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.crate.crateam.R;
import com.crate.crateam.model.ViewInspectionAttachments;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import java.io.ByteArrayOutputStream;

public class ViewAttachmentsListAdapter extends FirestoreRecyclerAdapter<ViewInspectionAttachments, ViewAttachmentsListAdapter.ViewAttachmentsListItem> {
    private Context context;
    private OnImageClickListener mlistener;

    public ViewAttachmentsListAdapter(@NonNull FirestoreRecyclerOptions options, Context context) {
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
    public ViewAttachmentsListAdapter.ViewAttachmentsListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_inspection_attachment_row, parent, false);
        return new ViewAttachmentsListAdapter.ViewAttachmentsListItem(view,mlistener);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewAttachmentsListItem viewAttachmentsListItem, int i, @NonNull ViewInspectionAttachments viewInspectionAttachments) {
        viewAttachmentsListItem.tv_defect_name.setText(viewInspectionAttachments.getElement_name());
        viewAttachmentsListItem.tv_defect_desc.setText(viewInspectionAttachments.getElement_defect());
        Bitmap decodedImage1 = decodeImageString(viewInspectionAttachments.getDefect_image1());
        viewAttachmentsListItem.iv_photo_one.setImageBitmap(decodedImage1);
        Bitmap decodedImage2 = decodeImageString(viewInspectionAttachments.getDefect_image2());
        viewAttachmentsListItem.iv_photo_two.setImageBitmap(decodedImage2);
    }

    public class ViewAttachmentsListItem extends RecyclerView.ViewHolder {
        private TextView tv_defect_name;
        private TextView tv_defect_desc;
        private ImageView iv_photo_one,iv_photo_two ;

        private ViewAttachmentsListItem(View itemView,final OnImageClickListener listener) {
            super(itemView);
            tv_defect_name = itemView.findViewById(R.id.tv_defect_name);
            tv_defect_desc = itemView.findViewById(R.id.tv_defect_desc);
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
