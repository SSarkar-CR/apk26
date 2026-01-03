package com.crate.crateam.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.crate.crateam.R;
import com.crate.crateam.utility.SessionManager;
import java.util.HashMap;

public class AssetForms extends AppCompatActivity {
    private ImageView iv_cross;
    private RelativeLayout rl_new_asset,rl_my_tasks,rl_search_asset,rl_inspect_asset,rl_manage_asset;
    private String  asset_inspection="",search_for_asset="", new_asset="",manage_asset="",my_task="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.asset_forms_layout);
        iv_cross = findViewById(R.id.iv_cross);
        rl_inspect_asset = findViewById(R.id.rl_inspect_asset);
        rl_new_asset = findViewById(R.id.rl_new_asset);
        rl_search_asset = findViewById(R.id.rl_search_asset);
        rl_manage_asset = findViewById(R.id.rl_manage_asset);
        rl_my_tasks = findViewById(R.id.rl_my_tasks);
        SessionManager sessionManager = new SessionManager(this);
        HashMap<String,String> asset_app_view_controller = sessionManager.getAssetAppViewController();
        asset_inspection = asset_app_view_controller.get(SessionManager.KEY_INSPECT_ASSET);
        search_for_asset = asset_app_view_controller.get(SessionManager.KEY_SEARCH_FOR_ASSET);
        new_asset = asset_app_view_controller.get(SessionManager.KEY_NEW_ASSET);
        manage_asset = asset_app_view_controller.get(SessionManager.KEY_MANAGE_ASSET);
        my_task = asset_app_view_controller.get(SessionManager.KEY_MY_TASK);
        Log.d("TYRYT :", asset_inspection+" "+search_for_asset+" "+new_asset+" "+manage_asset+" "+my_task);
        if (asset_inspection.equals("yes"))
            rl_inspect_asset.setVisibility(View.VISIBLE);
        if (search_for_asset.equals("yes"))
            rl_search_asset.setVisibility(View.VISIBLE);
        if (new_asset.equals("yes"))
            rl_new_asset.setVisibility(View.VISIBLE);
        if (manage_asset.equals("yes"))
            rl_manage_asset.setVisibility(View.VISIBLE);
        if (my_task.equals("yes"))
            rl_my_tasks.setVisibility(View.VISIBLE);

        iv_cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        rl_inspect_asset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent to_asset_inspection = new Intent(AssetForms.this, AssetInspection.class);
                startActivity(to_asset_inspection);
            }
        });
        rl_new_asset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent to_create_asset = new Intent(AssetForms.this,CreateAssetForm.class);
                startActivity(to_create_asset);
            }
        });
        rl_manage_asset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent to_manage_asset = new Intent(AssetForms.this,ManageAsset.class);
                startActivity(to_manage_asset);
            }
        });
        rl_my_tasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent to_my_task = new Intent(AssetForms.this,MyTask.class);
                startActivity(to_my_task);
            }
        });
        rl_search_asset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent to_search_asset = new Intent(AssetForms.this,AssetDetails.class);
                startActivity(to_search_asset);
            }
        });
    }
}