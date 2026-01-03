package com.crate.crateam.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.crate.crateam.R;

import java.util.List;

public class AdHocDefectListAdapterTrailer extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<String> defectList;
    private OnCameraClickListener mlistener;

    public interface OnCameraClickListener{
        void onCameraClick(int position);
        void onExpandClick(int position);
        void onCloseClick(int position);
    }

    public void setOnItemClickListener(OnCameraClickListener listener){
        mlistener = listener;
    }

    public AdHocDefectListAdapterTrailer(List<String> defectedVehicleList) {
        this.defectList = defectedVehicleList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adhoc_defect_row_trailer, parent, false);
        return new DefectsListItem(view,mlistener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final DefectsListItem defectsListItem = (DefectsListItem) holder;
        defectsListItem.tv_defects.setText(defectList.get(position));
    }

    @Override
    public int getItemCount() {
        return defectList.size();
    }

    private static class DefectsListItem extends RecyclerView.ViewHolder {
        private TextView tv_defects;
        private ImageView iv_defect_expand,iv_defect_close,iv_take_photo;
        private LinearLayout ll_comment,ll_images;

        private DefectsListItem(View itemView,final OnCameraClickListener listener) {
            super(itemView);
            iv_take_photo = itemView.findViewById(R.id.iv_take_photo_trailer);
            tv_defects = itemView.findViewById(R.id.tv_defects_trailer);
            iv_defect_expand= itemView.findViewById(R.id.iv_vehicle_defects_expand);
            iv_defect_close= itemView.findViewById(R.id.iv_vehicle_defects_close);
            ll_comment = itemView.findViewById(R.id.ll_comment_trailer);
            ll_images = itemView.findViewById(R.id.ll_images_trailer);
            iv_take_photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener!= null){
                        int position = getAdapterPosition();
                        if (position!=RecyclerView.NO_POSITION){
                            listener.onCameraClick(position);
                            ll_images.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
            iv_defect_expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener!= null){
                        int position = getAdapterPosition();
                        if (position!=RecyclerView.NO_POSITION){
                            listener.onExpandClick(position);
                            iv_defect_expand.setVisibility(View.GONE);
                            iv_defect_close.setVisibility(View.VISIBLE);
                            ll_comment.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
            iv_defect_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener!= null){
                        int position = getAdapterPosition();
                        if (position!=RecyclerView.NO_POSITION){
                            listener.onCloseClick(position);
                            iv_defect_expand.setVisibility(View.VISIBLE);
                            iv_defect_close.setVisibility(View.GONE);
                            ll_comment.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }
    }
}
