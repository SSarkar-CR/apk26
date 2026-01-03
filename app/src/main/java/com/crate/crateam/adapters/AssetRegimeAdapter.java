package com.crate.crateam.adapters;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.crate.crateam.R;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;

public class AssetRegimeAdapter extends RecyclerView.Adapter<ViewHolder>{

    ArrayList<String> assetRegimeLists;
    ArrayList<String> arraylist_regime_element_view;
    ArrayList<String> arraylist_regime_element_value;

    public AssetRegimeAdapter(ArrayList<String>  assetRegimeLists,ArrayList<String> arraylist_regime_element_view,
                              ArrayList<String> arraylist_regime_element_value) {
        this.assetRegimeLists = assetRegimeLists;
        this.arraylist_regime_element_view = arraylist_regime_element_view;
        this.arraylist_regime_element_value = arraylist_regime_element_value;
    }

    @NotNull
    @Override
    public MainListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.asset_regimes_list_row, parent, false);
        return new MainListItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainListItem mainListItem = (MainListItem) holder;
        mainListItem.tv_regime_element_name.setText(String.valueOf(assetRegimeLists.get(position)));
        if (arraylist_regime_element_view.get(position).equals("EditText")) {
            mainListItem.et_value.setVisibility(View.VISIBLE);
            mainListItem.et_value.setInputType(InputType.TYPE_CLASS_NUMBER);
            mainListItem.tv_value.setVisibility(View.GONE);
            mainListItem.rg_value_yes_no.setVisibility(View.GONE);
            mainListItem.rg_value_on_off.setVisibility(View.GONE);
        }else if(arraylist_regime_element_view.get(position).equals("TextView")){
            mainListItem.et_value.setVisibility(View.GONE);
            mainListItem.tv_value.setVisibility(View.VISIBLE);
            mainListItem.rg_value_yes_no.setVisibility(View.GONE);
            mainListItem.rg_value_on_off.setVisibility(View.GONE);
            mainListItem.tv_value.setText(arraylist_regime_element_value.get(position));
        }else if(arraylist_regime_element_view.get(position).equals("RadioButtonYesNo")){
            mainListItem.et_value.setVisibility(View.GONE);
            mainListItem.tv_value.setVisibility(View.GONE);
            mainListItem.rg_value_yes_no.setVisibility(View.VISIBLE);
            mainListItem.rg_value_on_off.setVisibility(View.GONE);
        }else if(arraylist_regime_element_view.get(position).equals("RadioButtonOnOff")){
            mainListItem.et_value.setVisibility(View.GONE);
            mainListItem.tv_value.setVisibility(View.GONE);
            mainListItem.rg_value_yes_no.setVisibility(View.GONE);
            mainListItem.rg_value_on_off.setVisibility(View.VISIBLE);
        }else if(arraylist_regime_element_view.get(position).equals("Text")){
            mainListItem.et_value.setVisibility(View.GONE);
            mainListItem.tv_value.setVisibility(View.VISIBLE);
            mainListItem.tv_value.setText(arraylist_regime_element_value.get(position));
            mainListItem.rg_value_yes_no.setVisibility(View.GONE);
            mainListItem.rg_value_on_off.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return assetRegimeLists.size();
    }

     public class MainListItem extends ViewHolder {
         private TextView tv_regime_element_name;
         private EditText et_value;
         private TextView tv_value;
         private RadioGroup rg_value_yes_no,rg_value_on_off;
        public MainListItem(View itemView) {
            super(itemView);
            tv_regime_element_name = itemView.findViewById(R.id.tv_regime_element_name);
            et_value= itemView.findViewById(R.id.et_value);
            tv_value= itemView.findViewById(R.id.tv_value);
            rg_value_yes_no= itemView.findViewById(R.id.rg_value_yes_no);
            rg_value_on_off= itemView.findViewById(R.id.rg_value_on_off);
        }
    }
}
