package com.crate.crateam.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.crate.crateam.R;
import com.crate.crateam.model.ViewInspectionDefectList;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import org.jetbrains.annotations.NotNull;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ViewDefectListAdapter extends FirestoreRecyclerAdapter<ViewInspectionDefectList, ViewDefectListAdapter.ViewDefectsListItem> {
    private Context context;

    public ViewDefectListAdapter(@NonNull FirestoreRecyclerOptions options, Context context) {
        super(options);
        this.context = context;
    }

    @NotNull
    @Override
    public ViewDefectListAdapter.ViewDefectsListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_inspection_defects_row, parent, false);
        return new ViewDefectListAdapter.ViewDefectsListItem(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull final ViewDefectsListItem viewDefectsListItem, int i, @NonNull ViewInspectionDefectList viewInspectionDefectList) {
        viewDefectsListItem.tv_view_vehicle_defects.setText(viewInspectionDefectList.getElement_name());
        viewDefectsListItem.tv_defect_description.setText(viewInspectionDefectList.getElement_defect());
        if (viewInspectionDefectList.getAdditional_report().equals(""))
            viewDefectsListItem.tv_additional_reported.setText("No");
        else
            viewDefectsListItem.tv_additional_reported.setText(viewInspectionDefectList.getAdditional_report());
        if (viewInspectionDefectList.getPre_existing_defect().equals(""))
            viewDefectsListItem.tv_pre_existing_defect.setText("No");
        else
            viewDefectsListItem.tv_pre_existing_defect.setText(viewInspectionDefectList.getPre_existing_defect());
        if (viewInspectionDefectList.getWorkshop_manager_comment().equals(""))
            viewDefectsListItem.tv_workshop_managers_comment.setText("No");
        else
            viewDefectsListItem.tv_workshop_managers_comment.setText(viewInspectionDefectList.getWorkshop_manager_comment());
        viewDefectsListItem.iv_view_vehicle_defects_close.setVisibility(View.GONE);
        viewDefectsListItem.iv_view_vehicle_defects_expand.setVisibility(View.VISIBLE);
        viewDefectsListItem.ll_view_defects.setVisibility(View.GONE);
        viewDefectsListItem.rl_defects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewDefectsListItem.iv_view_vehicle_defects_expand.setVisibility(View.GONE);
                viewDefectsListItem.iv_view_vehicle_defects_close.setVisibility(View.VISIBLE);
                viewDefectsListItem.ll_view_defects.setVisibility(View.VISIBLE);
            }
        });
        viewDefectsListItem.iv_view_vehicle_defects_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewDefectsListItem.iv_view_vehicle_defects_close.setVisibility(View.GONE);
                viewDefectsListItem.iv_view_vehicle_defects_expand.setVisibility(View.VISIBLE);
                viewDefectsListItem.ll_view_defects.setVisibility(View.GONE);
            }
        });
    }

    public class ViewDefectsListItem extends RecyclerView.ViewHolder {
        private TextView tv_view_vehicle_defects,tv_defect_description,tv_additional_reported,tv_workshop_managers_comment,tv_pre_existing_defect;
        private ImageView iv_view_vehicle_defects_expand,iv_view_vehicle_defects_close;
        private LinearLayout ll_view_defects;
        private RelativeLayout rl_defects;
        private ViewDefectsListItem(View itemView) {
            super(itemView);
            tv_defect_description = itemView.findViewById(R.id.tv_defect_description);
            tv_additional_reported = itemView.findViewById(R.id.tv_additional_reported);
            tv_workshop_managers_comment = itemView.findViewById(R.id.tv_workshop_managers_comment);
            tv_pre_existing_defect = itemView.findViewById(R.id.tv_pre_existing_defect);
            tv_view_vehicle_defects = itemView.findViewById(R.id.tv_view_vehicle_defects);
            iv_view_vehicle_defects_expand= itemView.findViewById(R.id.iv_view_vehicle_defects_expand);
            iv_view_vehicle_defects_close= itemView.findViewById(R.id.iv_view_vehicle_defects_close);
            ll_view_defects = itemView.findViewById(R.id.ll_view_defects);
            rl_defects = itemView.findViewById(R.id.rl_defects);
        }
    }
}
