package com.crate.crateam.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.crate.crateam.R;
import com.crate.crateam.utility.WaitingTimeInterfaces;

import java.util.ArrayList;

public class HourAdapter extends RecyclerView.Adapter<HourAdapter.SubHour> {
    private Context context;
    private ArrayList<String> arraylist_hour;
    private int selected_position = -1;
    private int selected_id = 0;
    public HourAdapter(Context context,ArrayList<String> arraylist_hour){
        this.context = context;
        this.arraylist_hour = arraylist_hour;
    }
    @NonNull
    @Override
    public SubHour onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hour_list, parent, false);
        return new SubHour(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SubHour holder, final int position) {
        holder.tv_hour.setText(arraylist_hour.get(position));
        holder.cl_hour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected_id = v.getId();
                /*WaitingTimeInterfaces.waiting_hour_interface.hour_time(arraylist_hour.get(position));
                holder.tv_hour.setTextColor(context.getResources().getColor(R.color.red));
                holder.cv_hour.setBackgroundColor(context.getResources().getColor(R.color.blue));
                selectedUnselected(selected_id);*/
                selected_position = position;
                notifyDataSetChanged();
                WaitingTimeInterfaces.waiting_hour_interface.hour_time(arraylist_hour.get(position));
            }
        });

        if(selected_position==position){
            holder.tv_hour.setTextColor(context.getResources().getColor(R.color.white));
            holder.cv_hour.setBackgroundColor(context.getResources().getColor(R.color.black_shade_one));
        }
        else
        {
            holder.cv_hour.setBackgroundColor(context.getResources().getColor(R.color.bg_layout));
            holder.tv_hour.setTextColor(context.getResources().getColor(R.color.grey_shade_two));
        }
    }

    @Override
    public int getItemCount() {
        return arraylist_hour.size();
    }

    public class SubHour extends RecyclerView.ViewHolder {
        private TextView tv_hour;
        private CardView cv_hour;
        private ConstraintLayout cl_hour;
        public SubHour(@NonNull View itemView) {
            super(itemView);
            tv_hour = itemView.findViewById(R.id.tv_hour);
            cv_hour = itemView.findViewById(R.id.cv_hour);
            cl_hour = itemView.findViewById(R.id.cl_hour);

        }
    }
    public interface waiting_hour{
        void  hour_time(String hour_text);
    }
}
