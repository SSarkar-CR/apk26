package com.crate.crateam.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.crate.crateam.R;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;

public class AssetRegimeDefectsAdapter extends RecyclerView.Adapter<ViewHolder>{

    ArrayList<String> assetElementsLists;
    private Context context;
    private OnItemClickListener mlistener;

    public interface OnItemClickListener{
        void onViewClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mlistener = listener;
    }

    public AssetRegimeDefectsAdapter(Context context, ArrayList<String> assetElementsLists) {
        this.context = context;
        this.assetElementsLists = assetElementsLists;
    }

    @NotNull
    @Override
    public MainListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.asset_elements_defects_list_row, parent, false);
        return new MainListItem(view,mlistener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainListItem mainListItem = (MainListItem) holder;
        mainListItem.tv_element_name.setText(String.valueOf(assetElementsLists.get(position)));
    }

    @Override
    public int getItemCount() {
        return assetElementsLists.size();
    }

     public class MainListItem extends ViewHolder {
         private TextView tv_element_name;
         private LinearLayout rl_defects_wm;

        private MainListItem(View itemView,final OnItemClickListener listener) {
            super(itemView);
            tv_element_name = itemView.findViewById(R.id.tv_element_name);
            rl_defects_wm = itemView.findViewById(R.id.rl_defects_wm);
            rl_defects_wm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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
