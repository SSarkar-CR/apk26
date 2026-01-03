package com.crate.crateam.adapters;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.crate.crateam.R;
import java.util.List;

public class HelpListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> helpList;
    private PopupWindow popupWindow;

    public HelpListAdapter(List<String> defectedVehicleList) {
        this.helpList = defectedVehicleList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.help_list_row, parent, false);
        return new HelpListItem(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final HelpListItem helpListItem = (HelpListItem) holder;
        helpListItem.tv_help_title.setText(helpList.get(position));

            helpListItem.iv_help_title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    changePictureDialog(view);
                }
            });

        if (helpListItem.tv_help_title.getText().equals("How to change my password?")){
            helpListItem.iv_help_title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    changePassDialog(view);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return helpList.size();
    }

    private static class HelpListItem extends RecyclerView.ViewHolder {
        private TextView tv_help_title;
        private ImageView iv_help_title;

        private HelpListItem(View itemView) {
            super(itemView);
            tv_help_title = itemView.findViewById(R.id.tv_help_title);
            iv_help_title= itemView.findViewById(R.id.iv_help_title);
        }
    }

    private void changePassDialog(View view) {
        final View popupView = LayoutInflater.from(view.getContext()).inflate(R.layout.change_pass_content_popup,null,true);
        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,true);
        popupWindow.showAtLocation(popupView, Gravity.BOTTOM,0,0);
        Button cancelPopup = popupView.findViewById(R.id.bt_ok);
        cancelPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
    }
    private void changePictureDialog(View view) {
        final View popupView = LayoutInflater.from(view.getContext()).inflate(R.layout.change_pic_popup_layout,null,true);
        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,true);
        popupWindow.showAtLocation(popupView, Gravity.BOTTOM,0,0);
        Button cancelPopup = popupView.findViewById(R.id.bt_ok);
        cancelPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
    }
}
