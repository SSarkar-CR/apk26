package com.crate.crateam.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.badoualy.stepperindicator.StepperIndicator;
import com.crate.crateam.R;
import com.crate.crateam.adapters.ViewPagerAdapter;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.CustomViewPager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.crate.crateam.utility.FirestoreManager;

public class WorkshopManagerFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.manager_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewPager(view);
    }

    private void setupViewPager(View view) {
        String arr[] = {"Details","Defects","Signature"};
        StepperIndicator stepperIndicator =view.findViewById(R.id.indicator);
        CustomViewPager viewPager = view.findViewById(R.id.viewpager_workshop_manager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        viewPagerAdapter.addFragment(new ManagerDetailsFragment(), "ManagerDetails","ManagerDetails");
        viewPagerAdapter.addFragment(new ManagerDefectsFragment(), "ManagerDefects","ManagerDefects");
        viewPagerAdapter.addFragment(new ManagerResolutionFragment(), "ManagerResolution","ManagerResolution");
        viewPager.setAdapter(viewPagerAdapter);
        stepperIndicator.setViewPager(viewPager);
        stepperIndicator.setAnimCheckRadius(1);
        stepperIndicator.setAnimIndicatorRadius(1);
        stepperIndicator.setLabelColor(getResources().getColor(R.color.black_shade_three));
        stepperIndicator.setLabels(arr);
        viewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }
}
