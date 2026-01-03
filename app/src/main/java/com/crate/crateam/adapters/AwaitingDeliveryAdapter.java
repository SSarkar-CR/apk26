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
import com.crate.crateam.model.TaskCompletedList;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class AwaitingDeliveryAdapter extends RecyclerView.Adapter<ViewHolder>{

    Context context;
    private OnViewClickListener mlistener;
    ArrayList<TaskCompletedList> taskCompletedLists;

    public AwaitingDeliveryAdapter(Context context, ArrayList<TaskCompletedList> taskCompletedLists) {
        this.context = context;
        this.taskCompletedLists = taskCompletedLists;
    }

    public interface OnViewClickListener{
        void onViewClick(int position);
    }

    public void setOnItemClickListener(OnViewClickListener listener){
        mlistener = listener;
    }

    @NotNull
    @Override
    public MainListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.awaiting_delivery_list_item, parent, false);
        return new MainListItem(view,mlistener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainListItem mainListItem = (MainListItem) holder;
        mainListItem.tv_ticket_no.setText(String.valueOf(taskCompletedLists.get(position).getTicket_no()));
        mainListItem.tv_collection_point.setText(taskCompletedLists.get(position).getCollection_point());
        mainListItem.tv_delivery_point.setText(taskCompletedLists.get(position).getDelivery_point());
        mainListItem.tv_material_name.setText(taskCompletedLists.get(position).getMaterial_name());
    }

    @Override
    public int getItemCount() {
        return taskCompletedLists.size();
    }

     public class MainListItem extends ViewHolder{
        private LinearLayout ll_awaiting_delivery;
        private TextView tv_ticket_no,tv_material_name,tv_collection_point,tv_delivery_point;
        private Button bt_complete_delivery;
        private ImageView iv_car;

        private MainListItem(View itemView,final OnViewClickListener listener) {
            super(itemView);
            ll_awaiting_delivery = itemView.findViewById(R.id.ll_awaiting_delivery);
            tv_ticket_no = itemView.findViewById(R.id.tv_ticket_no);
            tv_material_name = itemView.findViewById(R.id.tv_material_name);
            tv_collection_point = itemView.findViewById(R.id.tv_collection_point);
            tv_delivery_point= itemView.findViewById(R.id.tv_delivery_point);
            bt_complete_delivery = itemView.findViewById(R.id.bt_complete_delivery);
            iv_car = itemView.findViewById(R.id.iv_car);
            bt_complete_delivery.setOnClickListener(new View.OnClickListener() {
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
