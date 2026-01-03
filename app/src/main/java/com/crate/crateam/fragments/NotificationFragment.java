package com.crate.crateam.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.crate.crateam.R;
import com.crate.crateam.activities.MainActivity;

import java.util.Objects;

public class NotificationFragment extends Fragment implements View.OnClickListener {
    private Button bt_view_dashboard ;
    Fragment fragment = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.notifications_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initializeOnClick();
    }

    private void initView(View view){
        bt_view_dashboard = view.findViewById(R.id.bt_view_dashboard);
    }

    private void initializeOnClick(){
        bt_view_dashboard.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_view_dashboard:
                fragment = new DashboardFragment();
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame,fragment,"DASHBOARD");
                ft.addToBackStack("DASHBOARD");
                ft.commit();
                String tag = fragment.getTag();
                Log.d("CHILD_TAG" , Objects.requireNonNull(fragment.getTag()));
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.getChildTag(tag);
        }
    }
}
