package com.crate.crateam.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.crate.crateam.R;
import com.crate.crateam.model.AssignTasksList;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;

public class AssignTasksAdapter extends RecyclerView.Adapter<ViewHolder>{

    Context context;
    private OnViewClickListener mlistener;
    ArrayList<AssignTasksList> assignTasksList;

    public AssignTasksAdapter(Context context, ArrayList<AssignTasksList> assignTasksList) {
        this.context = context;
        this.assignTasksList = assignTasksList;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.assign_tasks_list_item, parent, false);
        return new MainListItem(view,mlistener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainListItem mainListItem = (MainListItem) holder;
        mainListItem.tv_task_status.setText(assignTasksList.get(position).getStatus());
        mainListItem.tv_collection_point.setText(assignTasksList.get(position).getCollection_point());
        mainListItem.tv_delivery_point.setText(assignTasksList.get(position).getDelivery_point());
        mainListItem.tv_material_name.setText(assignTasksList.get(position).getMaterial_name());
        mainListItem.tv_contact_name.setText(assignTasksList.get(position).getContact_name());
        mainListItem.tv_comments.setText(assignTasksList.get(position).getComments());
    }

    @Override
    public int getItemCount() {
        return assignTasksList.size();
    }

     public class MainListItem extends ViewHolder{
        private LinearLayout ll_assign_tasks;
        private TextView tv_task_status,tv_material_name,tv_collection_point,tv_delivery_point,
                tv_contact_name,tv_comments;
        private ImageView iv_car;

        private MainListItem(View itemView,final OnViewClickListener listener) {
            super(itemView);
            ll_assign_tasks = itemView.findViewById(R.id.ll_assign_tasks);
            tv_task_status = itemView.findViewById(R.id.tv_task_status);
            tv_material_name = itemView.findViewById(R.id.tv_material_name);
            tv_collection_point = itemView.findViewById(R.id.tv_collection_point);
            tv_delivery_point= itemView.findViewById(R.id.tv_delivery_point);
            tv_contact_name= itemView.findViewById(R.id.tv_contact_name);
            tv_comments= itemView.findViewById(R.id.tv_comments);
            iv_car = itemView.findViewById(R.id.iv_car);
            ll_assign_tasks.setOnClickListener(new View.OnClickListener() {
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
