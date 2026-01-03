package com.crate.crateam.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.crate.crateam.R;
import com.crate.crateam.activities.MainActivity;

public class SuccessFragment extends Fragment {
    private Fragment fragment = null;
    private String tag;

    protected static final String TAG = "Success";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.success_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView iv_cross = getActivity().findViewById(R.id.iv_cross);
        iv_cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment = new DashboardFragment();
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame,fragment,"DASHBOARD");
                ft.addToBackStack("DASHBOARD");
                ft.commit();
                tag = fragment.getTag();
                Log.d(TAG,"CHILD_TAG" +fragment.getTag());
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.getChildTag(tag);
            }
        });
    }
}
