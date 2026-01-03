package com.crate.crateam.fragments;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.crate.crateam.R;
import com.crate.crateam.adapters.HelpListAdapter;
import java.util.ArrayList;
import java.util.List;

public class HelpFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.help_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
       initView(view);
    }

    private void initView(View view){
        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint(Html.fromHtml("<font color = #828691>" +
                getResources().getString(R.string.how_can_help) + "</font>"));
        if(!searchView.isFocused()) {
            searchView.clearFocus();
        }
        int id = searchView.getContext()
                .getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        TextView textView = searchView.findViewById(id);
        textView.setTextColor(getResources().getColor(R.color.black_shade_three));
        RecyclerView rv_help = view.findViewById(R.id.rv_help);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv_help.setLayoutManager(layoutManager);
        HelpListAdapter helpListAdapter = new HelpListAdapter(getHelpList());
        rv_help.setAdapter(helpListAdapter);
    }

    private List<String> getHelpList() {
        List<String> helpList = new ArrayList<>();
        helpList.add("How to change profile picture?");
        helpList.add("How to change my password?");
        return helpList;
    }
}
