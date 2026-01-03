package com.crate.crateam.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.crate.crateam.R;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;

public class IssueManagementAdapter extends RecyclerView.Adapter<ViewHolder>{

    ArrayList<String> inspectionIdList;
    ArrayList<String> issueDateList;
    ArrayList<String> issueRaisedBy;
    private Context context;
    private OnViewClickListener mlistener;

    public interface OnViewClickListener{
        void onViewClick(int position);
    }

    public void setOnItemClickListener(OnViewClickListener listener){
        mlistener = listener;
    }

    public IssueManagementAdapter(Context context,ArrayList<String>  inspectionIdList, ArrayList<String> issueDateList,
                                  ArrayList<String> issueRaisedBy,ArrayList<String> assetLocation,ArrayList<String> assetStatus) {
        this.context = context;
        this.inspectionIdList = inspectionIdList;
        this.issueDateList = issueDateList;
        this.issueRaisedBy = issueRaisedBy;
//        this.assetLocation = assetLocation;
//        this.assetStatus = assetStatus;
    }

    @NotNull
    @Override
    public MainListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.issue_management_list_row, parent, false);
        return new MainListItem(view,mlistener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainListItem mainListItem = (MainListItem) holder;
        mainListItem.tv_inspection_id.setText(String.valueOf(inspectionIdList.get(position)));
        mainListItem.tv_date.setText(String.valueOf(issueDateList.get(position)));
        mainListItem.tv_raised_by.setText(String.valueOf(issueRaisedBy.get(position)));
//        mainListItem.tv_location.setText(String.valueOf(assetLocation.get(position)));
//        mainListItem.tv_asset_status.setText(String.valueOf(assetStatus.get(position)));
    }

    @Override
    public int getItemCount() {
        return inspectionIdList.size();
    }

     public class MainListItem extends ViewHolder {
         private TextView tv_inspection_id,tv_date,tv_raised_by,tv_location,tv_asset_status;
        private ImageView iv_view_issue;
        public MainListItem(View itemView,final OnViewClickListener listener) {
            super(itemView);
            tv_inspection_id = itemView.findViewById(R.id.tv_inspection_id);
            tv_location = itemView.findViewById(R.id.tv_location);
            tv_asset_status = itemView.findViewById(R.id.tv_asset_status);
            tv_date= itemView.findViewById(R.id.tv_date);
            tv_raised_by= itemView.findViewById(R.id.tv_raised_by);
            iv_view_issue= itemView.findViewById(R.id.iv_view_issue);
            iv_view_issue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener!= null){
                        int position = getAdapterPosition();
                        if (position!=RecyclerView.NO_POSITION){
                            listener.onViewClick(position);
                        }
                    }
                }
            });
        }
    }
}
