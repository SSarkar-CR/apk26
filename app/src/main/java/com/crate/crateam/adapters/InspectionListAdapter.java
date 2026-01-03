package com.crate.crateam.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.crate.crateam.R;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;

public class InspectionListAdapter extends RecyclerView.Adapter<ViewHolder>{

    private ArrayList<Integer> assetIds;
    private ArrayList<String> assetNames;
    private ArrayList<String> assetNRI;
    private ArrayList<String> assetNTE;
    private ArrayList<String> assetStatus;
    private Context context;
    private OnViewClickListener mlistener;

    public interface OnViewClickListener{
        void onViewClick(int position);
        void onDatesClick(int position);
        void onDatesSecondClick(int position);
        void onIssueClick(int position);
        void onInspectionClick(int position);
    }

    public void setOnItemClickListener(OnViewClickListener listener){
        mlistener = listener;
    }

    public InspectionListAdapter(Context context, ArrayList<Integer>  assetIds, ArrayList<String> assetNames,
                                 ArrayList<String> assetNRI, ArrayList<String> assetNTE, ArrayList<String> assetStatus) {
        this.context = context;
        this.assetIds = assetIds;
        this.assetNames = assetNames;
        this.assetNRI = assetNRI;
        this.assetNTE = assetNTE;
        this.assetStatus = assetStatus;
    }

    @NotNull
    @Override
    public MainListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inspection_list_row, parent, false);
        return new MainListItem(view,mlistener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainListItem mainListItem = (MainListItem) holder;
        int adapterPosition = holder.getAdapterPosition();
        if (assetNames.size()>0 && adapterPosition!=RecyclerView.NO_POSITION)
           mainListItem.tv_asset_name.setText(String.valueOf(assetNames.get(position)));
        if (assetNRI.size()>0 && adapterPosition!=RecyclerView.NO_POSITION )
           mainListItem.tv_nri.setText(String.valueOf(assetNRI.get(position)));
        if (assetNTE.size()>0 && adapterPosition!=RecyclerView.NO_POSITION)
           mainListItem.tv_nte.setText(String.valueOf(assetNTE.get(position)));
        if (assetStatus.size()>0 && adapterPosition!=RecyclerView.NO_POSITION) {
            if (assetStatus.get(position).equals("Orange")) {
                mainListItem.iv_status.setImageResource(R.drawable.orange_circle);
                mainListItem.ll_buttons.setVisibility(View.VISIBLE);
                mainListItem.ll_inspection.setVisibility(View.VISIBLE);
            } else if (assetStatus.get(position).equals("Red")) {
                mainListItem.iv_status.setImageResource(R.drawable.red_circle);
                mainListItem.ll_buttons.setVisibility(View.VISIBLE);
                mainListItem.ll_issue_date.setVisibility(View.VISIBLE);
            } else if (assetStatus.get(position).equals("Yellow")) {
                mainListItem.iv_status.setImageResource(R.drawable.yellow_circle);
            } else if (assetStatus.get(position).equals("Green")) {
                mainListItem.iv_status.setImageResource(R.drawable.green_circle);
            }else {
                mainListItem.iv_status.setImageResource(R.drawable.grey_circle);
            }
        }
    }

    @Override
    public int getItemCount() {
        return assetIds.size();
    }

     public class MainListItem extends ViewHolder {
        private TextView tv_asset_name,tv_nri,tv_nte;
        private ImageView iv_view,iv_status;
        private LinearLayout ll_buttons,ll_issue_date,ll_inspection;
        private Button bt_edit_key_dates,bt_edit_key_dates_second,bt_manage_issues,bt_inspect_asset;
        public MainListItem(View itemView,final OnViewClickListener listener) {
            super(itemView);
            tv_asset_name = itemView.findViewById(R.id.tv_asset_name);
            tv_nri= itemView.findViewById(R.id.tv_nri);
            tv_nte= itemView.findViewById(R.id.tv_nte);
            iv_status = itemView.findViewById(R.id.iv_status);
            ll_buttons = itemView.findViewById(R.id.ll_buttons);
            ll_issue_date = itemView.findViewById(R.id.ll_issue_date);
            ll_inspection = itemView.findViewById(R.id.ll_inspection);
            bt_edit_key_dates = itemView.findViewById(R.id.bt_edit_key_dates);
            bt_edit_key_dates_second = itemView.findViewById(R.id.bt_edit_key_dates_second);
            bt_manage_issues = itemView.findViewById(R.id.bt_manage_issues);
            bt_inspect_asset = itemView.findViewById(R.id.bt_inspect_asset);
            iv_view= itemView.findViewById(R.id.iv_view);
            iv_view.setOnClickListener(new View.OnClickListener() {
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
            bt_edit_key_dates.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener!= null){
                        int position = getAdapterPosition();
                        if (position!=RecyclerView.NO_POSITION){
                            listener.onDatesClick(position);
                        }
                    }
                }
            });
            bt_edit_key_dates_second.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener!= null){
                        int position = getAdapterPosition();
                        if (position!=RecyclerView.NO_POSITION){
                            listener.onDatesSecondClick(position);
                        }
                    }
                }
            });
            bt_manage_issues.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener!= null){
                        int position = getAdapterPosition();
                        if (position!=RecyclerView.NO_POSITION){
                            listener.onIssueClick(position);
                        }
                    }
                }
            });
            bt_inspect_asset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener!= null){
                        int position = getAdapterPosition();
                        if (position!=RecyclerView.NO_POSITION){
                            listener.onInspectionClick(position);
                        }
                    }
                }
            });
        }
    }
}
