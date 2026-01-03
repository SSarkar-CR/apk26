package com.crate.crateam.zebraPrintingTools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import com.crate.crateam.R;

public class PrintingLayoutFormat {
    private final Context context;

    public PrintingLayoutFormat(Context context){
        this.context=context;
    }

    public Bitmap supplyFormLayoutToBitmap(String ticket_no, String reference_code,String operator_name,String supplier_site_name,String delivery_site_name,String supplier_name,String delivery_location,
                                           String site_location,String delivery_reference,String haulier_name, String driver_name,String haulier_no,String date_time,String vehicle_no,String material_name,
                                           String unit,String gross,String tare,String net, Bitmap site_sign,Bitmap haulier_sign) {
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout view = new RelativeLayout(context);
        mInflater.inflate(R.layout.supply_form_print_ticket, view, true);
        AppCompatTextView tv_ticket_no_value = view.findViewById(R.id.tv_ticket_no_value);
        AppCompatTextView tv_reference_code_value = view.findViewById(R.id.tv_reference_code_value);
        AppCompatTextView tv_operator_name = view.findViewById(R.id.tv_operator_name);
        AppCompatTextView tv_supplier_site_name = view.findViewById(R.id.tv_supplier_site_name);
        AppCompatTextView tv_delivery_site_name = view.findViewById(R.id.tv_delivery_site_name);
        AppCompatTextView tv_supplier_name = view.findViewById(R.id.tv_supplier_name);
        AppCompatTextView tv_delivery_location = view.findViewById(R.id.tv_delivery_location);
        AppCompatTextView tv_site_location = view.findViewById(R.id.tv_site_location_supply);
        AppCompatTextView tv_delivery_reference = view.findViewById(R.id.tv_delivery_reference_supply);
        AppCompatTextView tv_haulier_name = view.findViewById(R.id.tv_haulier_name);
        AppCompatTextView tv_driver_name_print = view.findViewById(R.id.tv_driver_name_print);
        AppCompatTextView tv_haulier_no = view.findViewById(R.id.tv_haulier_no);
        AppCompatTextView tv_date_time_print = view.findViewById(R.id.tv_date_time_print);
        AppCompatTextView tv_vehicle_no_print = view.findViewById(R.id.tv_vehicle_no_print);
        AppCompatTextView tv_material_name = view.findViewById(R.id.tv_material_name);
        AppCompatTextView tv_unit = view.findViewById(R.id.tv_unit);
        AppCompatTextView tv_gross = view.findViewById(R.id.tv_gross);
        AppCompatTextView tv_tare = view.findViewById(R.id.tv_tare);
        AppCompatTextView tv_net = view.findViewById(R.id.tv_net);
        AppCompatImageView iv_site_sign = view.findViewById(R.id.iv_site_sign);
        AppCompatImageView iv_haulier_sign = view.findViewById(R.id.iv_haulier_sign);

        tv_ticket_no_value.setText(ticket_no);
        tv_reference_code_value.setText(reference_code);
        tv_operator_name.setText(operator_name);
        tv_supplier_site_name.setText(supplier_site_name);
        tv_delivery_site_name.setText(delivery_site_name);
        tv_supplier_name.setText(supplier_name);
        tv_delivery_location.setText(delivery_location);
        tv_site_location.setText(site_location);
        tv_delivery_reference.setText(delivery_reference);
        tv_haulier_name.setText(haulier_name);
        tv_driver_name_print.setText(driver_name);
        tv_haulier_no.setText(haulier_no);
        tv_date_time_print.setText(date_time);
        tv_vehicle_no_print.setText(vehicle_no);
        tv_material_name.setText(material_name);
        tv_unit.setText(unit);
        tv_gross.setText(gross);
        tv_tare.setText(tare);
        tv_net.setText(net);
        iv_site_sign.setImageBitmap(site_sign);
        iv_haulier_sign.setImageBitmap(haulier_sign);

        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Log.e("height11", String.valueOf(view.getMeasuredHeight()));
        Log.e("width11",String.valueOf(view.getMeasuredWidth()));

        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
                view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        c.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), paint);
        Log.e("bitmap_width", String.valueOf(bitmap.getWidth()));
        Log.e("bitmap_height", String.valueOf(bitmap.getHeight()));
        paint.setColor(Color.BLACK);
        view.draw(c);
        Log.e("bitmap11", String.valueOf(bitmap));
        return bitmap;
    }

    public Bitmap wasteTransferFormToBitmap(String ticket_no,String company_name,String operator_name,String waste_carrier_no,
                                            String project_name,String sic_code,String location_name,String material_name,String unit,String gross,String tare,String net,
                                            String collection_point,String delivery_point, String collection_location,String delivery_location,String site_location,String site_reference,
                                            String collection_reference,String delivery_reference,String on_site_time,String off_site_time,String external_ticket,String permit_no,
                                            String haulier_name,String driver_name,String wcl_no, String date_time,String vehicle_no,String vehicle_type,
                                            String container_size,String container_type,Bitmap producer_sign,Bitmap haulier_sign) {
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout view = new RelativeLayout(context);
        mInflater.inflate(R.layout.waste_transfer_form_print_ticket, view, true);
        // waste form note
        AppCompatTextView tv_ticket_no_value_waste = view.findViewById(R.id.tv_ticket_no_value_waste);
        AppCompatTextView tv_company_name = view.findViewById(R.id.tv_company_name);
        AppCompatTextView tv_operator_name_waste = view.findViewById(R.id.tv_operator_name_waste);
        AppCompatTextView tv_waste_carrier_no_print = view.findViewById(R.id.tv_waste_carrier_no_print);
        AppCompatTextView tv_project_name = view.findViewById(R.id.tv_project_name);
        AppCompatTextView tv_sic_code_waste = view.findViewById(R.id.tv_sic_code_waste);
        AppCompatTextView tv_location_name = view.findViewById(R.id.tv_location_name);
        AppCompatTextView tv_material_name_waste = view.findViewById(R.id.tv_material_name_waste);
        AppCompatTextView tv_unit = view.findViewById(R.id.tv_unit);
        AppCompatTextView tv_gross = view.findViewById(R.id.tv_gross);
        AppCompatTextView tv_tare = view.findViewById(R.id.tv_tare);
        AppCompatTextView tv_net = view.findViewById(R.id.tv_net);
        AppCompatTextView tv_collection_point_waste = view.findViewById(R.id.tv_collection_point_waste);
        AppCompatTextView tv_delivery_point_waste = view.findViewById(R.id.tv_delivery_point_waste);
        AppCompatTextView tv_collection_location = view.findViewById(R.id.tv_collection_location);
        AppCompatTextView tv_delivery_location_waste = view.findViewById(R.id.tv_delivery_location_waste);
        AppCompatTextView tv_site_location_waste = view.findViewById(R.id.tv_site_location_waste);
        AppCompatTextView tv_site_reference_waste = view.findViewById(R.id.tv_site_reference_waste);
        AppCompatTextView tv_collection_reference_waste = view.findViewById(R.id.tv_collection_reference_waste);
        AppCompatTextView tv_delivery_reference_waste = view.findViewById(R.id.tv_delivery_reference_waste);
        Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.NORMAL);
        tv_delivery_location_waste.setTypeface(boldTypeface);
        AppCompatTextView tv_on_site_time = view.findViewById(R.id.tv_on_site_waste);
        AppCompatTextView tv_off_site_time = view.findViewById(R.id.tv_off_site_waste);
        AppCompatTextView tv_external_ticket = view.findViewById(R.id.tv_external_ticket_no);
        AppCompatTextView tv_permit_no = view.findViewById(R.id.tv_permit_no);
        AppCompatTextView tv_haulier_name_waste = view.findViewById(R.id.tv_haulier_name_waste);
        AppCompatTextView tv_driver_name_waste = view.findViewById(R.id.tv_driver_name_waste);
        AppCompatTextView tv_wcl_no = view.findViewById(R.id.tv_wcl_no);
        AppCompatTextView tv_date_time_print_waste = view.findViewById(R.id.tv_date_time_print_waste);
        AppCompatTextView tv_vehicle_no_print_waste = view.findViewById(R.id.tv_vehicle_no_print_waste);
        AppCompatTextView tv_vehicle_type = view.findViewById(R.id.tv_vehicle_type);
        AppCompatTextView tv_container_size = view.findViewById(R.id.tv_container_size);
        AppCompatTextView tv_container_type = view.findViewById(R.id.tv_container_type);
        AppCompatImageView iv_producer_sign = view.findViewById(R.id.iv_producer_sign);
        AppCompatImageView iv_haulier_sign_waste = view.findViewById(R.id.iv_haulier_sign_waste);

        tv_ticket_no_value_waste.setText(ticket_no);
        tv_company_name.setText(company_name);
        tv_operator_name_waste.setText(operator_name);
        tv_waste_carrier_no_print.setText(waste_carrier_no);
        tv_project_name.setText(project_name);
        tv_sic_code_waste.setText(sic_code);
        tv_location_name.setText(location_name);
        tv_material_name_waste.setText(material_name);
        tv_unit.setText(unit);
        tv_gross.setText(gross);
        tv_tare.setText(tare);
        tv_net.setText(net);
        tv_collection_point_waste.setText(collection_point);
        tv_delivery_point_waste.setText(delivery_point);
        tv_collection_location.setText(collection_location);
        tv_delivery_location_waste.setText(delivery_location);
        tv_site_location_waste.setText(site_location);
        tv_site_reference_waste.setText(site_reference);
        tv_collection_reference_waste.setText(collection_reference);
        tv_delivery_reference_waste.setText(delivery_reference);
        tv_on_site_time.setText(on_site_time);
        tv_off_site_time.setText(off_site_time);
        tv_external_ticket.setText(external_ticket);
        tv_permit_no.setText(permit_no);
        tv_haulier_name_waste.setText(haulier_name);
        tv_driver_name_waste.setText(driver_name);
        tv_vehicle_type.setText(vehicle_type);
        tv_container_size.setText(container_size);
        tv_container_type.setText(container_type);
        tv_wcl_no.setText(wcl_no);
        tv_date_time_print_waste.setText(date_time);
        tv_vehicle_no_print_waste.setText(vehicle_no);
        tv_wcl_no.setText(waste_carrier_no);
        iv_producer_sign.setImageBitmap(producer_sign);
        iv_haulier_sign_waste.setImageBitmap(haulier_sign);

        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Log.e("height11", String.valueOf(view.getMeasuredHeight()));
        Log.e("width11",String.valueOf(view.getMeasuredWidth()));

        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
                view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        c.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), paint);
        Log.e("bitmap_width", String.valueOf(bitmap.getWidth()));
        Log.e("bitmap_height", String.valueOf(bitmap.getHeight()));
        paint.setColor(Color.BLACK);
        view.draw(c);
        Log.e("bitmap11", String.valueOf(bitmap));
        return bitmap;
    }

    public Bitmap claireFormToBitmap(String ticket_no,String company_name,String operator_name, String project_name,String sic_code,String location_name,String material_name,String unit,String gross,String tare,String net,
                                     String collection_point, String delivery_point, String collection_location,String delivery_location,String site_location,String site_reference,
                                     String collection_reference,String delivery_reference,String on_site_time,String off_site_time,String external_ticket,
                                     String permit_no,String haulier_name,String driver_name,
                                     String date_time,String vehicle_no,String vehicle_type,String container_size,String container_type,Bitmap producer_sign,Bitmap haulier_sign) {
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout view = new RelativeLayout(context);
        mInflater.inflate(R.layout.claire_form_print_ticket, view, true);
        // waste form note
        AppCompatTextView tv_ticket_no_value_waste = view.findViewById(R.id.tv_ticket_no_value_waste);
        AppCompatTextView tv_company_name = view.findViewById(R.id.tv_company_name);
        AppCompatTextView tv_operator_name_waste = view.findViewById(R.id.tv_operator_name_waste);
        AppCompatTextView tv_project_name = view.findViewById(R.id.tv_project_name);
        AppCompatTextView tv_sic_code_waste = view.findViewById(R.id.tv_sic_code_waste);
        AppCompatTextView tv_location_name = view.findViewById(R.id.tv_location_name);
        AppCompatTextView tv_material_name_waste = view.findViewById(R.id.tv_material_name_waste);
        AppCompatTextView tv_unit = view.findViewById(R.id.tv_unit);
        AppCompatTextView tv_gross = view.findViewById(R.id.tv_gross);
        AppCompatTextView tv_tare = view.findViewById(R.id.tv_tare);
        AppCompatTextView tv_net = view.findViewById(R.id.tv_net);
        AppCompatTextView tv_collection_point_waste = view.findViewById(R.id.tv_collection_point_waste);
        AppCompatTextView tv_delivery_point_waste = view.findViewById(R.id.tv_delivery_point_waste);
        AppCompatTextView tv_collection_location = view.findViewById(R.id.tv_collection_location);
        AppCompatTextView tv_delivery_location_waste = view.findViewById(R.id.tv_delivery_location_waste);
        AppCompatTextView tv_site_location_waste = view.findViewById(R.id.tv_site_location_waste);
        AppCompatTextView tv_site_reference_waste = view.findViewById(R.id.tv_site_reference_waste);
        AppCompatTextView tv_collection_reference_waste = view.findViewById(R.id.tv_collection_reference_waste);
        AppCompatTextView tv_delivery_reference_waste = view.findViewById(R.id.tv_delivery_reference_waste);
        Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.NORMAL);
        tv_delivery_location_waste.setTypeface(boldTypeface);
        AppCompatTextView tv_on_site_time = view.findViewById(R.id.tv_on_site_waste);
        AppCompatTextView tv_off_site_time = view.findViewById(R.id.tv_off_site_waste);
        AppCompatTextView tv_external_ticket = view.findViewById(R.id.tv_external_ticket_no);
        AppCompatTextView tv_permit_no = view.findViewById(R.id.tv_permit_no);
        AppCompatTextView tv_haulier_name_waste = view.findViewById(R.id.tv_haulier_name_waste);
        AppCompatTextView tv_driver_name_waste = view.findViewById(R.id.tv_driver_name_waste);
        AppCompatTextView tv_date_time_print_waste = view.findViewById(R.id.tv_date_time_print_waste);
        AppCompatTextView tv_vehicle_no_print_waste = view.findViewById(R.id.tv_vehicle_no_print_waste);
        AppCompatTextView tv_vehicle_type = view.findViewById(R.id.tv_vehicle_type);
        AppCompatTextView tv_container_size = view.findViewById(R.id.tv_container_size);
        AppCompatTextView tv_container_type = view.findViewById(R.id.tv_container_type);
        AppCompatImageView iv_producer_sign = view.findViewById(R.id.iv_producer_sign);
        AppCompatImageView iv_haulier_sign_waste = view.findViewById(R.id.iv_haulier_sign_waste);

        tv_ticket_no_value_waste.setText(ticket_no);
        tv_company_name.setText(company_name);
        tv_operator_name_waste.setText(operator_name);
        tv_project_name.setText(project_name);
        tv_sic_code_waste.setText(sic_code);
        tv_location_name.setText(location_name);
        tv_material_name_waste.setText(material_name);
        tv_unit.setText(unit);
        tv_gross.setText(gross);
        tv_tare.setText(tare);
        tv_net.setText(net);
        tv_collection_point_waste.setText(collection_point);
        tv_delivery_point_waste.setText(delivery_point);
        tv_collection_location.setText(collection_location);
        tv_delivery_location_waste.setText(delivery_location);
        tv_site_location_waste.setText(site_location);
        tv_site_reference_waste.setText(site_reference);
        tv_collection_reference_waste.setText(collection_reference);
        tv_delivery_reference_waste.setText(delivery_reference);
        tv_on_site_time.setText(on_site_time);
        tv_off_site_time.setText(off_site_time);
        tv_external_ticket.setText(external_ticket);
        tv_permit_no.setText(permit_no);
        tv_haulier_name_waste.setText(haulier_name);
        tv_driver_name_waste.setText(driver_name);
        tv_vehicle_type.setText(vehicle_type);
        tv_container_size.setText(container_size);
        tv_container_type.setText(container_type);
        tv_date_time_print_waste.setText(date_time);
        tv_vehicle_no_print_waste.setText(vehicle_no);
        iv_producer_sign.setImageBitmap(producer_sign);
        iv_haulier_sign_waste.setImageBitmap(haulier_sign);

        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Log.e("height11", String.valueOf(view.getMeasuredHeight()));
        Log.e("width11",String.valueOf(view.getMeasuredWidth()));

        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
                view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        c.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), paint);
        Log.e("bitmap_width", String.valueOf(bitmap.getWidth()));
        Log.e("bitmap_height", String.valueOf(bitmap.getHeight()));
        paint.setColor(Color.BLACK);
        view.draw(c);
        Log.e("bitmap11", String.valueOf(bitmap));
        return bitmap;
    }
}
