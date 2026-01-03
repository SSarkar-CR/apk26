package com.crate.crateam.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.crate.crateam.R;
import com.crate.crateam.model.ReportsList;
import com.crate.crateam.utility.SessionManager;
import com.crate.crateam.utility.SwipeRevealLayout;
import com.crate.crateam.utility.ViewBinderHelper;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

public class RoadworthyListAdapter extends FirestoreRecyclerAdapter<ReportsList, RoadworthyListAdapter.MainListItem>{
    Context context;
    private SessionManager sessionManager;
    private OnViewClickListener mlistener;
    private final ViewBinderHelper binderHelper = new ViewBinderHelper();

    public RoadworthyListAdapter(@NonNull FirestoreRecyclerOptions options, Context context) {
        super(options);
        this.context = context;
    }

    public interface OnViewClickListener{
        void onViewClick(int position);
        void onEditClick(int position);
    }
    public void setOnItemClickListener(OnViewClickListener listener){
        mlistener = listener;
    }

    @NotNull
    @Override
    public MainListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.roadworthy_list_item, parent, false);
        return new MainListItem(view,mlistener);
    }

    @Override
    protected void onBindViewHolder(@NonNull MainListItem mainListItem, int i, @NonNull ReportsList reportsList) {
        sessionManager = new SessionManager(context);
        int role_id = 0;
        HashMap<String, String> user = sessionManager.getUserDetails();
        String user_role = user.get(SessionManager.KEY_ROLE_ONE);
        if (user_role != null)
          role_id = Integer.valueOf(user_role);
        binderHelper.bind(mainListItem.swipeLayout, String.valueOf(reportsList));
        mainListItem.tv_vehicle_reg.setText(reportsList.getRegistration_no());
        mainListItem.tv_driver_name.setText(reportsList.getDriver_name());
        mainListItem.tv_date.setText(reportsList.getConducted_on());
        if (reportsList.getVehicle_defect()!= null) {
            Log.d("VE DEFECT :" ,reportsList.getVehicle_defect());
            if (reportsList.getVehicle_defect().equals("Yes") || reportsList.getTrailer_defect().equals("Yes")) {
                mainListItem.iv_car.setImageDrawable(context.getResources().getDrawable(R.drawable.defected_vehicle));
                if (role_id == 3)
                    mainListItem.bt_edit.setVisibility(View.VISIBLE);
                else
                    mainListItem.bt_edit.setVisibility(View.GONE);

            } else if (reportsList.getVehicle_defect().equals("No") && reportsList.getTrailer_defect().equals("No")) {
                mainListItem.iv_car.setImageDrawable(context.getResources().getDrawable(R.drawable.car));
                mainListItem.bt_edit.setVisibility(View.GONE);
            }
        }else {
            mainListItem.iv_car.setImageDrawable(context.getResources().getDrawable(R.drawable.form));
            mainListItem.bt_edit.setVisibility(View.GONE);
        }
    }

     public class MainListItem extends ViewHolder {
        private SwipeRevealLayout swipeLayout;
        private TextView tv_vehicle_reg;
        private TextView tv_driver_name;
        private TextView tv_date;
        private ImageButton bt_view,bt_edit;
        private ImageView iv_car;

        private MainListItem(View itemView,final OnViewClickListener listener) {
            super(itemView);
            swipeLayout = itemView.findViewById(R.id.swipe_layout);
            tv_vehicle_reg = itemView.findViewById(R.id.tv_vehicle_reg);
            tv_driver_name = itemView.findViewById(R.id.tv_driver_name);
            tv_date= itemView.findViewById(R.id.tv_date);
            bt_view= itemView.findViewById(R.id.bt_view);
            bt_edit = itemView.findViewById(R.id.bt_edit);
            iv_car = itemView.findViewById(R.id.iv_car);
            bt_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener!= null){
                        int position = getAdapterPosition();
                        if (position!=RecyclerView.NO_POSITION){
                            listener.onViewClick(position);
                        }
                    }
                }
            });
            bt_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener!= null){
                        int position = getAdapterPosition();
                        if (position!=RecyclerView.NO_POSITION){
                            listener.onEditClick(position);
                        }
                    }
                }
            });
        }
    }
}
