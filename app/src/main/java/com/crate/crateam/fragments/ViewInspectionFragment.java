package com.crate.crateam.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.badoualy.stepperindicator.StepperIndicator;
import com.crate.crateam.R;
import com.crate.crateam.adapters.ViewPagerAdapter;
import com.crate.crateam.utility.AppData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.crate.crateam.utility.FirestoreManager;

public class ViewInspectionFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_inspection_layout, container, false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewPager(view);
    }
    private void setupViewPager(View view) {
        String arr[] = {"Details","Defects","Attachments"};
        StepperIndicator stepperIndicator = view.findViewById(R.id.indicator_view);
        ViewPager viewPager = view.findViewById(R.id.viewpager_view_inspection);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        viewPagerAdapter.addFragment(new ViewInspectionDetailsFragment(), "ViewInspectionDetails","ViewInspectionDetails");
        viewPagerAdapter.addFragment(new ViewInspectionDefectsFragment(), "ViewInspectionDefects","ViewInspectionDefects");
        viewPagerAdapter.addFragment(new ViewInspectionAttachmentsFragment(), "ViewInspectionAttachments","ViewInspectionAttachments");
        viewPager.setAdapter(viewPagerAdapter);
        stepperIndicator.setViewPager(viewPager);
        stepperIndicator.setAnimCheckRadius(1);
        stepperIndicator.setAnimIndicatorRadius(1);
        stepperIndicator.setLabelColor(getResources().getColor(R.color.black_shade_three));
        stepperIndicator.setLabels(arr);
    }
}
