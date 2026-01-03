package com.crate.crateam.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.crate.crateam.R;
import com.crate.crateam.adapters.EditFavouritesAdapter;
import java.util.ArrayList;
import java.util.List;

public class FavouritesFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.edit_favourites_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view){
        RecyclerView rv_edit_favourites = view.findViewById(R.id.rv_edit_favourites);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv_edit_favourites.setLayoutManager(layoutManager);
        EditFavouritesAdapter editFavouritesAdapter = new EditFavouritesAdapter(getFavouritesList());
        rv_edit_favourites.setAdapter(editFavouritesAdapter);
    }

    private List<String> getFavouritesList() {
        List<String> favouritesList = new ArrayList<>();
        favouritesList.add("Vehicle Inspection");
        return favouritesList;
    }
}
