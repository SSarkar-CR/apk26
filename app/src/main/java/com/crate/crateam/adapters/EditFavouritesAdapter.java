package com.crate.crateam.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.crate.crateam.R;
import java.util.List;

public class EditFavouritesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> favouritesList;

    public EditFavouritesAdapter(List<String> defectedVehicleList) {
        this.favouritesList = defectedVehicleList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.edit_favourites_row, parent, false);
        return new ViewDefectsListItem(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ViewDefectsListItem viewDefectsListItem = (ViewDefectsListItem) holder;
        viewDefectsListItem.tv_favourites_title.setText(favouritesList.get(position));
        viewDefectsListItem.iv_favourites_deselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewDefectsListItem.iv_favourites_select.setVisibility(View.VISIBLE);
                viewDefectsListItem.iv_favourites_deselect.setVisibility(View.GONE);
            }
        });
        viewDefectsListItem.iv_favourites_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewDefectsListItem.iv_favourites_select.setVisibility(View.GONE);
                viewDefectsListItem.iv_favourites_deselect.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return favouritesList.size();
    }

    private static class ViewDefectsListItem extends RecyclerView.ViewHolder {
        private TextView tv_favourites_title;
        private ImageView iv_favourites_deselect,iv_favourites_select;

        private ViewDefectsListItem(View itemView) {
            super(itemView);
            tv_favourites_title = itemView.findViewById(R.id.tv_favourites_title);
            iv_favourites_select= itemView.findViewById(R.id.iv_favourites_select);
            iv_favourites_deselect= itemView.findViewById(R.id.iv_favourites_deselect);
        }
    }
}
