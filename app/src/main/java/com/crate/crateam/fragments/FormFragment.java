package com.crate.crateam.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.crate.crateam.R;
import com.crate.crateam.activities.MainActivity;

public class FormFragment extends Fragment {
    Fragment fragment = null;
    private String tag;
    private SharedPreferences pref;
    MainActivity mainActivity ;
    protected static final String TAG = "Form";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.forms_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pref = getActivity().getSharedPreferences("MyPref", 0); // 0 - for private mode
        mainActivity = (MainActivity)getActivity();
        RelativeLayout ll_forms = view.findViewById(R.id.ll_forms);
        ll_forms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    fragment = new FormExpandFragment();
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame, fragment,"FORM EXPAND");
                    ft.addToBackStack("FORM EXPAND");
                    ft.commit();
                    tag = fragment.getTag();
                    Log.d(TAG,"CHILD_TAG" +fragment.getTag());
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.getChildTag(tag);
            }
        });
      removePreferences();
    }

    private void removePreferences(){
        SharedPreferences.Editor editor = pref.edit();
        editor.remove("nfc1");
        editor.remove("nfc2");
        editor.remove("nfc3");
        editor.remove("nfc4");
        editor.remove("nfc5");
        editor.remove("nfc6");
        editor.apply();
        int count = 0;
        mainActivity.getCount(count);
    }
}
