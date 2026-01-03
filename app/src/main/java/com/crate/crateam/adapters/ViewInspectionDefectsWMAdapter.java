package com.crate.crateam.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.crate.crateam.R;
import com.crate.crateam.model.ViewInspectionDefectList;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;

public class ViewInspectionDefectsWMAdapter extends FirestoreRecyclerAdapter<ViewInspectionDefectList, ViewInspectionDefectsWMAdapter.ViewDefectsListWM> {

    private Context context;
    private OnEditTextChanged onEditTextChanged;
    private ArrayList<String> wmComments = new ArrayList<>();

    public interface OnEditTextChanged {
        void onTextChanged(int position,int size);
    }

    public ViewInspectionDefectsWMAdapter(@NonNull FirestoreRecyclerOptions options, Context context,OnEditTextChanged onEditTextChanged) {
        super(options);
        this.context = context;
        this.onEditTextChanged = onEditTextChanged;
    }

    @NotNull
    @Override
    public  ViewInspectionDefectsWMAdapter.ViewDefectsListWM onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_inspection_defects_wm_row, parent, false);
        return new  ViewInspectionDefectsWMAdapter.ViewDefectsListWM(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull final ViewDefectsListWM viewDefectsListItem, int i, @NonNull ViewInspectionDefectList viewInspectionDefectList) {
        viewDefectsListItem.tv_view_vehicle_defects_wm.setText(viewInspectionDefectList.getElement_name());
        viewDefectsListItem.iv_view_vehicle_defects_close_wm.setVisibility(View.GONE);
        viewDefectsListItem.iv_view_vehicle_defects_expand_wm.setVisibility(View.VISIBLE);
        viewDefectsListItem.ll_resolution.setVisibility(View.GONE);
        wmComments.add(i,viewDefectsListItem.et_resolution.getText().toString());
        final int position = i;
        viewDefectsListItem.et_resolution.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                onEditTextChanged.onTextChanged(position,wmComments.size());
            }
        });
        viewDefectsListItem.rl_defects_wm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewDefectsListItem.iv_view_vehicle_defects_expand_wm.setVisibility(View.GONE);
                viewDefectsListItem.iv_view_vehicle_defects_close_wm.setVisibility(View.VISIBLE);
                viewDefectsListItem.ll_resolution.setVisibility(View.VISIBLE);
            }
        });
        viewDefectsListItem.iv_view_vehicle_defects_close_wm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewDefectsListItem.iv_view_vehicle_defects_close_wm.setVisibility(View.GONE);
                viewDefectsListItem.iv_view_vehicle_defects_expand_wm.setVisibility(View.VISIBLE);
                viewDefectsListItem.ll_resolution.setVisibility(View.GONE);
            }
        });
    }

     public class ViewDefectsListWM extends RecyclerView.ViewHolder {
        private TextView tv_view_vehicle_defects_wm;
        private ImageView iv_view_vehicle_defects_expand_wm,iv_view_vehicle_defects_close_wm;
        private LinearLayout ll_resolution;
        private RelativeLayout rl_defects_wm;
        private EditText et_resolution;
        private CheckBox checkBox;
        private ViewDefectsListWM(View itemView) {
            super(itemView);
            tv_view_vehicle_defects_wm = itemView.findViewById(R.id.tv_view_vehicle_defects_wm);
            iv_view_vehicle_defects_expand_wm= itemView.findViewById(R.id.iv_view_vehicle_defects_expand_wm);
            iv_view_vehicle_defects_close_wm= itemView.findViewById(R.id.iv_view_vehicle_defects_close_wm);
            ll_resolution = itemView.findViewById(R.id.ll_resolution);
            rl_defects_wm = itemView.findViewById(R.id.rl_defects_wm);
            et_resolution = itemView.findViewById(R.id.et_resolution);
            checkBox = itemView.findViewById(R.id.checkBox_element);
        }
    }
}
