package com.crate.crateam.model;

public class ViewInspectionAttachments {
    private int element_id;
    private String element_defect;
    private String defect_image1;
    private String defect_image2;
    private String element_name;

    public ViewInspectionAttachments(){
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
}
