package com.crate.crateam.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.crate.crateam.R;
import java.util.List;

public class DefectListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<String> defectList;
    private OnCameraClickListener mlistener;

    public interface OnCameraClickListener{
        void onCameraClick(int position);
        void onYesClick(int position);
        void onNoClick(int position);
    }

    public void setOnItemClickListener(OnCameraClickListener listener){
        mlistener = listener;
    }

    public DefectListAdapter(List<String> defectedVehicleList) {
        this.defectList = defectedVehicleList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inspection_defects_list_row, parent, false);
        return new DefectsListItem(view,mlistener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final DefectsListItem defectsListItem = (DefectsListItem) holder;
        defectsListItem.tv_defectType.setText(defectList.get(position));
    }

    @Override
    public int getItemCount() {
        return defectList.size();
    }

    private static class DefectsListItem extends RecyclerView.ViewHolder {
        private TextView tv_defectType;
        private ImageView iv_defect_no,iv_defect_yes,iv_take_photo;
        private RelativeLayout rl_manager_comment;
        private LinearLayout ll_additional,ll_images,ll_previous_defect;
        private RadioGroup rg_layout;

        private DefectsListItem(View itemView,final OnCameraClickListener listener) {
            super(itemView);
            iv_take_photo = itemView.findViewById(R.id.iv_take_photo);
            tv_defectType = itemView.findViewById(R.id.tv_defectType);
            iv_defect_no= itemView.findViewById(R.id.iv_defect_no);
            iv_defect_yes= itemView.findViewById(R.id.iv_defect_yes);
            rl_manager_comment = itemView.findViewById(R.id.rl_manager_comment);
            ll_additional = itemView.findViewById(R.id.ll_additional);
            ll_previous_defect = itemView.findViewById(R.id.ll_previous_defect);
            ll_images = itemView.findViewById(R.id.ll_images);
            rg_layout = itemView.findViewById(R.id.rg_layout);
            rg_layout.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if (i==R.id.rb_additional)
                        ll_additional.setVisibility(View.VISIBLE);
                    if (i==R.id.rb_reported)
                        ll_additional.setVisibility(View.VISIBLE);
                }
            });
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
            iv_defect_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener!= null){
                        int position = getAdapterPosition();
                        if (position!=RecyclerView.NO_POSITION){
                            listener.onYesClick(position);
                            iv_defect_yes.setImageResource(R.drawable.yes_selected);
                            iv_defect_no.setImageResource(R.drawable.no_unselected);
                            rl_manager_comment.setVisibility(View.GONE);
                            ll_additional.setVisibility(View.GONE);
                            ll_previous_defect.setVisibility(View.GONE);
                        }
                    }
                }
            });
            iv_defect_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener!= null){
                        int position = getAdapterPosition();
                        if (position!=RecyclerView.NO_POSITION){
                            listener.onNoClick(position);
                            iv_defect_no.setImageResource(R.drawable.no_selected);
                            iv_defect_yes.setImageResource(R.drawable.yes_unselected);
                        }
                    }
                }
            });
        }
    }
}
