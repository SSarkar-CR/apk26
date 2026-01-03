package com.crate.crateam.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import com.crate.crateam.fragments.ViewInspectionTrailerAttachments;
import com.crate.crateam.fragments.ViewInspectionVehicleAttachments;

public class ViewInspectionAttachmentsPagerAdapter extends FragmentPagerAdapter {
    int mNumOfTabs;

    public ViewInspectionAttachmentsPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new ViewInspectionVehicleAttachments();
            case 1:
                return new ViewInspectionTrailerAttachments();
            default:
                return null;
        }
    }
    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}

