package com.crate.crateam.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.crate.crateam.R;
import com.crate.crateam.model.GateKeeperSupplyDatamodule;
import java.util.ArrayList;

public class GateKeeperAdapter extends RecyclerView.Adapter<GateKeeperAdapter.MySubClass> {
    private Context context;
    private ArrayList<GateKeeperSupplyDatamodule> arrayList_supply;
    public GateKeeperAdapter(Context context,ArrayList<GateKeeperSupplyDatamodule> arrayList_supply){
        this.context = context;
        this.arrayList_supply = arrayList_supply;
    }
    @NonNull
    @Override
    public MySubClass onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_completed_list_item, parent, false);
        MySubClass mySubClass = new MySubClass(view);
        return  mySubClass;
    }

    @Override
    public void onBindViewHolder(@NonNull MySubClass holder, int position) {
        holder.tv_task_id_text.setText("Ticket Number");
        holder.tv_material_name.setText(arrayList_supply.get(position).getMaterial_name());
        holder.tv_collection_point.setText(arrayList_supply.get(position).getCollection_address());
        holder.tv_delivery_point.setText(arrayList_supply.get(position).getDelivery_address());
        holder.tv_task_id.setText(arrayList_supply.get(position).getTicket_no());
    }

    @Override
    public int getItemCount() {
        return arrayList_supply.size();
    }
    public class MySubClass extends RecyclerView.ViewHolder {
        private TextView tv_task_id_text,tv_task_id,tv_material_name;
        private TextView tv_collection_point;
        private TextView tv_delivery_point;
        public MySubClass(@NonNull View itemView) {
            super(itemView);
            tv_task_id_text = itemView.findViewById(R.id.tv_task_id_text);
            tv_task_id = itemView.findViewById(R.id.tv_task_id);
            tv_material_name = itemView.findViewById(R.id.tv_material_name);
            tv_collection_point = itemView.findViewById(R.id.tv_collection_point);
            tv_delivery_point= itemView.findViewById(R.id.tv_delivery_point);
        }
    }
}
