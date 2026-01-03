package com.crate.crateam.adapters;

import com.crate.crateam.fragments.ViewInspectionTrailerDefects;
import com.crate.crateam.fragments.ViewInspectionVehicleDefects;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ViewInspectionDefectsPagerAdapter extends FragmentPagerAdapter {
    int mNumOfTabs;
    public ViewInspectionDefectsPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }
    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new ViewInspectionVehicleDefects();
            case 1:
                return new ViewInspectionTrailerDefects();
            default:
                return null;
        }
    }
    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}

