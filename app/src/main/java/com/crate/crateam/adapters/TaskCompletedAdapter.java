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
import com.crate.crateam.model.TaskCompletedList;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TaskCompletedAdapter extends RecyclerView.Adapter<ViewHolder>{
    Context context;
    ArrayList<TaskCompletedList> taskCompletedLists;
    public TaskCompletedAdapter(Context context, ArrayList<TaskCompletedList> taskCompletedLists) {
        this.context = context;
        this.taskCompletedLists = taskCompletedLists;
    }

    @NotNull
    @Override
    public MainListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_completed_list_item, parent, false);
        return new MainListItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainListItem mainListItem = (MainListItem) holder;
        mainListItem.tv_task_id.setText(String.valueOf(taskCompletedLists.get(position).getTicket_no()));
        mainListItem.tv_collection_point.setText(taskCompletedLists.get(position).getCollection_point());
        mainListItem.tv_delivery_point.setText(taskCompletedLists.get(position).getDelivery_point());
        mainListItem.tv_material_name.setText(taskCompletedLists.get(position).getMaterial_name());

    }

    @Override
    public int getItemCount() {
        return taskCompletedLists.size();
    }

     public class MainListItem extends ViewHolder {
        private TextView tv_task_id,tv_material_name;
        private TextView tv_collection_point;
        private TextView tv_delivery_point;
        private ImageView iv_car;

        private MainListItem(View itemView) {
            super(itemView);
            tv_task_id = itemView.findViewById(R.id.tv_task_id);
            tv_material_name = itemView.findViewById(R.id.tv_material_name);
            tv_collection_point = itemView.findViewById(R.id.tv_collection_point);
            tv_delivery_point= itemView.findViewById(R.id.tv_delivery_point);
            iv_car = itemView.findViewById(R.id.iv_car);
        }
    }
}
