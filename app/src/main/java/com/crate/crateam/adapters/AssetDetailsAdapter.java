package com.crate.crateam.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.crate.crateam.R;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;

public class AssetDetailsAdapter extends RecyclerView.Adapter<ViewHolder>{

    ArrayList<String> assetRegimeLists;
    ArrayList<String> arraylist_regime_element_value;

    public AssetDetailsAdapter(ArrayList<String>  assetRegimeLists,
                               ArrayList<String> arraylist_regime_element_value) {
        this.assetRegimeLists = assetRegimeLists;
        this.arraylist_regime_element_value = arraylist_regime_element_value;
    }

    @NotNull
    @Override
    public MainListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.asset_details_regimes_list, parent, false);
        return new MainListItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainListItem mainListItem = (MainListItem) holder;
        mainListItem.tv_regime_element_name.setText(String.valueOf(assetRegimeLists.get(position)));
        mainListItem.tv_value.setText(String.valueOf(arraylist_regime_element_value.get(position)));
    }

    @Override
    public int getItemCount() {
        return assetRegimeLists.size();
    }

     public class MainListItem extends ViewHolder {
         private TextView tv_regime_element_name;
         private TextView tv_value;
        public MainListItem(View itemView) {
            super(itemView);
            tv_regime_element_name = itemView.findViewById(R.id.tv_regime_element_name);
            tv_value= itemView.findViewById(R.id.tv_value);
        }
    }
}
