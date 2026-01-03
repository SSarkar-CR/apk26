package com.crate.crateam.model;

public class ViewInspectionDefectList {
    private int element_id;
    private String element_name;
    private String element_defect;
    private String workshop_manager_comment;
    private String additional_report;
    private String pre_existing_defect;
    private int vehicle_id;

    public ViewInspectionDefectList(){
         //no-argument constructor for Firebase.
    }

    public ViewInspectionDefectList(int element_id, String element_name, String element_defect, String workshop_manager_comment, String additional_report, String pre_existing_defect, int vehicle_id) {
        this.element_id = element_id;
        this.element_name = element_name;
        this.element_defect = element_defect;
        this.workshop_manager_comment = workshop_manager_comment;
        this.additional_report = additional_report;
        this.pre_existing_defect = pre_existing_defect;
        this.vehicle_id = vehicle_id;
    }

    public int getElement_id() {
        return element_id;
    }

    public void setElement_id(int element_id) {
        this.element_id = element_id;
    }

    public String getElement_name() {
        return element_name;
    }

    public void setElement_name(String element_name) {
        this.element_name = element_name;
    }

    public String getElement_defect() { return element_defect; }

    public void setElement_defect(String element_defect) { this.element_defect = element_defect; }

    public String getPre_existing_defect() {
        return pre_existing_defect;
    }

    public void setPre_existing_defect(String pre_existing_defect) {
        this.pre_existing_defect = pre_existing_defect;
    }

    public String getAdditional_report() {
        return additional_report;
    }

    public void setAdditional_report(String additional_report) {
        this.additional_report = additional_report;
    }

    public String getWorkshop_manager_comment() {
        return workshop_manager_comment;
    }

    public void setWorkshop_manager_comment(String workshop_manager_comment) {
        this.workshop_manager_comment = workshop_manager_comment;
    }
    public int getVehicle_id() {
        return vehicle_id;
    }
    public void setVehicle_id(int vehicle_id) {
        this.vehicle_id = vehicle_id;
    }

}
