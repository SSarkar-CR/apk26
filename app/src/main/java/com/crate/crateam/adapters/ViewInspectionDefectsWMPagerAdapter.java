package com.crate.crateam.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import com.crate.crateam.fragments.ViewTrailerInspectionDefectsWM;
import com.crate.crateam.fragments.ViewVehicleInspectionDefectsWM;

public class ViewInspectionDefectsWMPagerAdapter extends FragmentPagerAdapter {
    int mNumOfTabs;
    public ViewInspectionDefectsWMPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ViewVehicleInspectionDefectsWM();
            case 1:
                return new ViewTrailerInspectionDefectsWM();
            default:
                return null;
        }
    }
    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}

