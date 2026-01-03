package com.crate.crateam.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.crate.crateam.R;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ViewCertificates extends AppCompatActivity {

    private  String document_name="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_certificates);
        ImageView iv_certificate_image = findViewById(R.id.iv_certificate_image);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
           document_name = extras.getString("document_name");
        }
        Log.d("utuy :" , Objects.requireNonNull(document_name));
        if (document_name.contains(".PNG") || document_name.contains(".jpg")
                || document_name.contains(".png")|| document_name.contains(".JPG")){
            iv_certificate_image.setVisibility(View.VISIBLE);
            Picasso.with(ViewCertificates.this)
                    .load(document_name)
                    .into(iv_certificate_image);
        }else if (document_name.contains(".pdf")){
            iv_certificate_image.setVisibility(View.GONE);
            openPDFViewer(document_name);
        }
    }

    private void openPDFViewer(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url), "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        } catch (Exception e) {
            // Error...
        }
    }
}