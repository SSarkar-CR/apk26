package com.crate.crateam.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import com.crate.crateam.fragments.ViewInspectionTrailerDetails;
import com.crate.crateam.fragments.ViewInspectionVehicleDetails;

public class ViewInspectionPagerAdapter extends FragmentPagerAdapter {
    int mNumOfTabs;

    public ViewInspectionPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }
    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new ViewInspectionVehicleDetails();
            case 1:
                return new ViewInspectionTrailerDetails();

            default:
                return null;
        }
    }
    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}

