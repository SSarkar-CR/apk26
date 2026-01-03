package com.crate.crateam.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.badoualy.stepperindicator.StepperIndicator;
import com.crate.crateam.R;
import com.crate.crateam.adapters.ViewPagerAdapter;
import com.crate.crateam.utility.AppData;
import com.crate.crateam.utility.CustomViewPagerInspection;
import com.crate.crateam.utility.FirestoreManager;
import com.crate.crateam.utility.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class InspectionFragment extends Fragment {
    private String message;
    private int count=0;
    Fragment fragment = null ;

    protected static final String TAG = "InspectionFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.inspection_layout, container, false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewPager(view);
    }

    private void setupViewPager(View view) {
        SessionManager sessionManager = new SessionManager(getActivity());
        HashMap<String, String> vehicle = sessionManager.getVehicleDetails();
        String trailer= vehicle.get(SessionManager.KEY_TRAILER);
         Log.d(TAG,"TRAILER CONNECTED " +trailer);
         String arr[] = {"Details","Defects","Trailer","Signature"};
         StepperIndicator stepperIndicator = view.findViewById(R.id.indicator);
         CustomViewPagerInspection viewPager = view.findViewById(R.id.viewpager);
         viewPager.setOffscreenPageLimit(4);
         ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
         final Bundle args = getArguments();
         if (getArguments()!= null){
            message = args.getString("message");
            count = args.getInt("counter");
            Log.d(TAG,"Message"+"NFC_Data_Inspection : "  +message +" " +count);
         }
         Bundle bundle = new Bundle();
         bundle.putString("message", message);
         bundle.putInt("counter",count);
         fragment = new InspectionDetailsFragment();
         if(count< 4 ) {
             fragment.setArguments(bundle);
         }
         viewPagerAdapter.addFragment(fragment, "InspectionDetails","InspectionDetails");
         fragment = new InspectionDefectsFragment();
         viewPagerAdapter.addFragment(fragment, "InspectionDefects","InspectionDefects");
         fragment = new InspectionTrailerFragment();
         if (trailer != null) {
             if (trailer.equals("Yes")) {
                 if (count == 4 || count == 5) {
                     fragment.setArguments(bundle);
                 }
             }
         }
         viewPagerAdapter.addFragment(fragment, "InspectionTrailer","InspectionTrailer");
         fragment = new InspectionSignatureFragment();
         if (trailer != null) {
             if (trailer.equals("No") && count >= 4) {
                 fragment.setArguments(bundle);
             } else if (trailer.equals("Yes") && count >= 6) {
                 fragment.setArguments(bundle);
             }
         }
         viewPagerAdapter.addFragment(fragment, "InspectionSignature","InspectionSignature");
         viewPager.setAdapter(viewPagerAdapter);
         stepperIndicator.setViewPager(viewPager);
         stepperIndicator.setAnimCheckRadius(1);
         stepperIndicator.setAnimIndicatorRadius(1);
         stepperIndicator.setLabelColor(getResources().getColor(R.color.black_shade_three));
         stepperIndicator.setLabels(arr);
        if (trailer != null) {
            if (trailer.equals("No") && count >= 4) {
                viewPager.setCurrentItem(3);
            } else if (trailer.equals("Yes") && count == 4 || count == 5) {
                viewPager.setCurrentItem(2);
            } else if (trailer.equals("Yes") && count >= 6) {
                viewPager.setCurrentItem(3);
            }
        }
    }
}
