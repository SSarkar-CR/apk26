package com.crate.crateam.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.crate.crateam.R;
import com.crate.crateam.model.DynamicViewDieselDelivery;
import com.crate.crateam.utility.Dialog;
import java.util.ArrayList;

public class DefaultViewDieselDeliveryAdapter extends RecyclerView.Adapter<DefaultViewDieselDeliveryAdapter.MychildClass> {
    Context context;
    int add_item;
    private ArrayList<DynamicViewDieselDelivery> dynamicViewDieselDeliveryArrayList;
    private ArrayList<DynamicViewDieselDelivery> list  = new ArrayList<>();
    public DefaultViewDieselDeliveryAdapter(Context context, ArrayList<DynamicViewDieselDelivery> dynamicViewDieselDeliveryArrayList){
        this.context = context;
        this.dynamicViewDieselDeliveryArrayList = dynamicViewDieselDeliveryArrayList;
    }
    @NonNull
    @Override
    public MychildClass onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dynamic_view_diesel_delivery_layout,parent,false);
        return new MychildClass(view);
    }
    @Override
    public void onBindViewHolder(@NonNull final MychildClass holder, final int position) {
        holder.et_item_fulled.setText(dynamicViewDieselDeliveryArrayList.get(position).getItem_fulled());
        holder.et_item_plant_number.setText(dynamicViewDieselDeliveryArrayList.get(position).getItem_plant_no());
        holder.et_liters_fuelled.setText(dynamicViewDieselDeliveryArrayList.get(position).getLiters_fulled());
        holder.et_general_comment.setText(dynamicViewDieselDeliveryArrayList.get(position).getGeneral_comment());
        holder.iv_add_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog.addViewDieselDeliveryInterface.addView(add_item, position,dynamicViewDieselDeliveryArrayList);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dynamicViewDieselDeliveryArrayList.size();
    }
    public class MychildClass extends RecyclerView.ViewHolder{
        EditText et_item_fulled,et_item_plant_number,et_liters_fuelled,et_general_comment;
        ConstraintLayout cl_add_item;
        TextView tv_item_fuelled;
        ImageView iv_add_item;
        public MychildClass(@NonNull View itemView) {
            super(itemView);
            et_item_fulled = itemView.findViewById(R.id.et_item_fulled);
            et_item_plant_number = itemView.findViewById(R.id.et_item_plant_number);
            et_liters_fuelled = itemView.findViewById(R.id.et_liters_fuelled);
            et_general_comment = itemView.findViewById(R.id.et_general_comment);
            cl_add_item = itemView.findViewById(R.id.cl_add_item);
            tv_item_fuelled = itemView.findViewById(R.id.tv_item_fuelled);
            iv_add_item = itemView.findViewById(R.id.iv_add_item);
        }
    }
    public interface AddViewDieselDelivery{
        void addView(int numbet_of_item, int position, ArrayList<DynamicViewDieselDelivery> dynamicViewDieselDeliveryArrayList1);
    }
}
