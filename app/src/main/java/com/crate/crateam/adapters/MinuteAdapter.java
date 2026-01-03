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

public class MinuteAdapter extends RecyclerView.Adapter<MinuteAdapter.SubMinute> {
    private Context context;
    private ArrayList<String> arraylist_minute;
    private int selected_position = -1;
    public MinuteAdapter(Context context, ArrayList<String> arraylist_minute){
        this.context = context;
        this.arraylist_minute = arraylist_minute;
    }
    @NonNull
    @Override
    public SubMinute onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.minute_list, parent, false);
        return new SubMinute(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubMinute holder, final int position) {
        holder.tv_minute.setText(arraylist_minute.get(position));
        holder.cl_minute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected_position = position;
                notifyDataSetChanged();
                WaitingTimeInterfaces.waiting_minute_interface.minute_time(arraylist_minute.get(position));
            }
        });
        if(selected_position==position){
            holder.tv_minute.setTextColor(context.getResources().getColor(R.color.white));
            holder.cv_minute.setBackgroundColor(context.getResources().getColor(R.color.black_shade_one));
        }
        else
        {
            holder.cv_minute.setBackgroundColor(context.getResources().getColor(R.color.bg_layout));
            holder.tv_minute.setTextColor(context.getResources().getColor(R.color.grey_shade_two));
        }
    }

    @Override
    public int getItemCount() {
        return arraylist_minute.size();
    }
    public class SubMinute extends RecyclerView.ViewHolder {
        private TextView tv_minute;
        private ConstraintLayout cl_minute;
        private CardView cv_minute;
        public SubMinute(@NonNull View itemView) {
            super(itemView);
            tv_minute = itemView.findViewById(R.id.tv_minute);
            cl_minute = itemView.findViewById(R.id.cl_minute);
            cv_minute = itemView.findViewById(R.id.cv_minute);
        }
    }
    public interface Waiting_minute{
        void minute_time(String minute_text);
    }
}
