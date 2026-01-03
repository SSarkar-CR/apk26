package com.crate.crateam.adapters;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.crate.crateam.R;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;

public class AssetElementsAdapter extends RecyclerView.Adapter<ViewHolder>{

    ArrayList<String> assetElementsLists;
    ArrayList<String> assetDefectedElementsDefectList;
    ArrayList<Integer> assetElementsIdList;
    ArrayList<Integer> assetDefectedElementsIdList;
    private OnItemClickListener mlistener;

    public interface OnItemClickListener{
        void onYesClick(int position);
        void onNoClick(int position);
        void onModerateClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mlistener = listener;
    }

    public AssetElementsAdapter(ArrayList<String> assetElementsLists,ArrayList<Integer> assetElementsIdList,
                                ArrayList<Integer> assetDefectedElementsIdList,ArrayList<String> assetDefectedElementsDefectList) {
        this.assetElementsLists = assetElementsLists;
        this.assetElementsIdList = assetElementsIdList;
        this.assetDefectedElementsIdList = assetDefectedElementsIdList;
        this.assetDefectedElementsDefectList = assetDefectedElementsDefectList;
    }

    @NotNull
    @Override
    public MainListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.asset_elements_list_row, parent, false);
        return new MainListItem(view,mlistener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainListItem mainListItem = (MainListItem) holder;
        mainListItem.tv_asset_elements.setText(String.valueOf(assetElementsLists.get(position)));
    }

    @Override
    public int getItemCount() {
        return assetElementsLists.size();
    }

     public class MainListItem extends ViewHolder {
         private TextView tv_asset_elements,tv_previous_defect;
         private EditText et_defect_details;
         private ImageView iv_defect_no,iv_defect_yes,iv_defect_moderate;
         private LinearLayout ll_comment,ll_previous_defect_view;

        private MainListItem(View itemView,final OnItemClickListener listener) {
            super(itemView);
            tv_previous_defect = itemView.findViewById(R.id.tv_previous_defect);
            tv_asset_elements = itemView.findViewById(R.id.tv_asset_elements);
            et_defect_details = itemView.findViewById(R.id.et_defect_details);
            InputFilter filter = new InputFilter() {
                public CharSequence filter(CharSequence source, int start,
                                           int end, Spanned dest, int dstart, int dend) {
                    for (int i = start; i < end; i++) {
                        if (!Character.toString(source.charAt(i)).matches("[a-zA-Z0-9., ]+")) {
                            return "";
                        }
                    }
                    return null;
                }
            };
            et_defect_details.setFilters(new InputFilter[] { filter });
            ll_comment = itemView.findViewById(R.id.ll_comment);
            ll_previous_defect_view = itemView.findViewById(R.id.ll_previous_defect_view);
            iv_defect_no= itemView.findViewById(R.id.iv_defect_no);
            iv_defect_moderate= itemView.findViewById(R.id.iv_defect_moderate);
            iv_defect_yes= itemView.findViewById(R.id.iv_defect_yes);
            iv_defect_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    iv_defect_yes.setImageResource(R.drawable.yes_selected);
                    iv_defect_no.setImageResource(R.drawable.no_unselected);
                    iv_defect_moderate.setImageResource(R.drawable.moderate_unselected);
                    ll_comment.setVisibility(View.GONE);
                    iv_defect_yes.setTag("Not Defected");
                    iv_defect_no.setTag("");
                    iv_defect_moderate.setTag("");
                }
            });
            iv_defect_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    iv_defect_no.setImageResource(R.drawable.no_selected);
                    iv_defect_yes.setImageResource(R.drawable.yes_unselected);
                    iv_defect_moderate.setImageResource(R.drawable.moderate_unselected);
                    Log.d("RRRR :" , assetElementsIdList+" "+assetDefectedElementsIdList+" "+assetDefectedElementsDefectList);
                    int position = getAdapterPosition();
                    if (position!=RecyclerView.NO_POSITION){
                        if (assetDefectedElementsIdList.contains(assetElementsIdList.get(position))){
                            ll_previous_defect_view.setVisibility(View.VISIBLE);
                            int position1 = assetDefectedElementsIdList.indexOf(assetElementsIdList.get(position));
                            tv_previous_defect.setText(assetDefectedElementsDefectList.get(position1));
                        }else {
                            ll_previous_defect_view.setVisibility(View.GONE);
                        }
                    }
                    ll_comment.setVisibility(View.VISIBLE);
                    iv_defect_no.setTag("Defected");
                    iv_defect_yes.setTag("");
                    iv_defect_moderate.setTag("");
                }
            });
            iv_defect_moderate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    iv_defect_no.setImageResource(R.drawable.no_unselected);
                    iv_defect_yes.setImageResource(R.drawable.yes_unselected);
                    iv_defect_moderate.setImageResource(R.drawable.moderate_selected);
                    Log.d("RRRR :" ,assetElementsIdList+" "+assetDefectedElementsIdList+" "+assetDefectedElementsDefectList);
                    int position = getAdapterPosition();
                    if (position!=RecyclerView.NO_POSITION){
                        if (assetDefectedElementsIdList.contains(assetElementsIdList.get(position))){
                            ll_previous_defect_view.setVisibility(View.VISIBLE);
                            int position1 = assetDefectedElementsIdList.indexOf(assetElementsIdList.get(position));
                            tv_previous_defect.setText(assetDefectedElementsDefectList.get(position1));
                        }else {
                            ll_previous_defect_view.setVisibility(View.GONE);
                        }
                    }
                    ll_comment.setVisibility(View.VISIBLE);
                    iv_defect_no.setTag("");
                    iv_defect_yes.setTag("");
                    iv_defect_moderate.setTag("Moderate");
                }
            });
        }
    }
}
