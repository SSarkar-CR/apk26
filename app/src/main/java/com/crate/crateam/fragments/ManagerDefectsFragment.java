package com.crate.crateam.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.crate.crateam.R;
import com.crate.crateam.adapters.ViewInspectionDefectsWMPagerAdapter;
import com.crate.crateam.utility.CustomViewPager;

public class ManagerDefectsFragment extends Fragment {
    private  CustomViewPager parentViewPager ;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.manager_defects_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TabLayout tabLayout = view.findViewById(R.id.tabs);
        parentViewPager = getActivity().findViewById(R.id.viewpager_workshop_manager);
        parentViewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        tabLayout.addTab(tabLayout.newTab().setText("Vehicle"));
        tabLayout.addTab(tabLayout.newTab().setText("Trailer"));
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        final CustomViewPager viewPager =  view.findViewById(R.id.viewpager_manager_defects);
        viewPager.setAdapter(new ViewInspectionDefectsWMPagerAdapter(getChildFragmentManager(), tabLayout.getTabCount()));
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        viewPager.addOnPageChangeListener(
                new TabLayout.TabLayoutOnPageChangeListener(tabLayout)
        );
        for(int i=0; i < tabLayout.getTabCount(); i++) {
            View tab = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(i);
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) tab.getLayoutParams();
            p.setMargins(100, 0, 100, 0);
            tab.requestLayout();
        }
        viewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (viewPager.getCurrentItem()==0)
                    parentViewPager.disableScroll(true);
                else
                    parentViewPager.disableScroll(false);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        parentViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (parentViewPager.getCurrentItem()==1 && viewPager.getCurrentItem()==0
                    || parentViewPager.getCurrentItem()==1 && viewPager.getCurrentItem()==1)
                    parentViewPager.disableScroll(true);
                else
                    parentViewPager.disableScroll(false);
            }
            @Override
            public void onPageSelected(int position) {
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }
}
