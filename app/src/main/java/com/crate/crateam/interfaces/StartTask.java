package com.crate.crateam.interfaces;

public interface StartTask {
    void getDetails(int collection_site_id,String collection_site_name,String collection_address, String delivery_address,int delivery_site_id, int job_id, int task_order_id, int material_id,
                    String material_description, String ewc_code,String material_value,String note_type, String sic_code, String carrier_no,
                    String permit_no_collection, String permit_no_delivery, String site_type,String material_type,String project_no);
}
