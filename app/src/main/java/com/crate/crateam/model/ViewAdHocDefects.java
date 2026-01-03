package com.crate.crateam.model;

public class ViewAdHocDefects {
    private int element_id;
    private String element_defect;
    private String defect_image1;
    private String defect_image2;
    private String element_name;
    private String element_type;
    private String workshop_manager_comment;
    private String close_issue;

    public ViewAdHocDefects(){
        //no-argument constructor for Firebase.
    }

    public int getElement_id() {
        return element_id;
    }
    public void setElement_id(int element_id) {
        this.element_id = element_id;
    }
    public String getElement_defect() {
        return element_defect;
    }
    public void setElement_defect(String element_defect) {
        this.element_defect = element_defect;
    }
    public String getDefect_image1() {
        return defect_image1;
    }
    public void setDefect_image1(String defect_image1) {
        this.defect_image1 = defect_image1;
    }
    public String getDefect_image2() {
        return defect_image2;
    }
    public void setDefect_image2(String defect_image2) {
        this.defect_image2 = defect_image2;
    }
    public String getElement_name() {
        return element_name;
    }
    public void setElement_name(String element_name) {
        this.element_name = element_name;
    }
    public String getElement_type() {
        return element_type;
    }
    public void setElement_type(String element_type) {
        this.element_type = element_type;
    }
    public String getWorkshop_manager_comment() { return workshop_manager_comment; }
    public void setWorkshop_manager_comment(String workshop_manager_comment) { this.workshop_manager_comment = workshop_manager_comment; }
    public String getClose_issue() { return close_issue; }
    public void setClose_issue(String close_issue) { this.close_issue = close_issue; }
}
