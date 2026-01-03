package com.crate.crateam.model;

public class ViewInspectionElementDefects {
    private int element_id;
    private String element_defect;
    private String z_image_one;
    private String z_image_two;
    private String element_name;
    private String defect_comment;
    private String defected;

    public ViewInspectionElementDefects(){
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

    public String getZ_image_one() {
        return z_image_one;
    }

    public void setZ_image_one(String z_image_one) {
        this.z_image_one = z_image_one;
    }

    public String getZ_image_two() {
        return z_image_two;
    }

    public void setZ_image_two(String z_image_two) {
        this.z_image_two = z_image_two;
    }

    public String getElement_name() {
        return element_name;
    }

    public void setElement_name(String element_name) {
        this.element_name = element_name;
    }

    public String getDefect_comment() {
        return defect_comment;
    }

    public void setDefect_comment(String defect_comment) {
        this.defect_comment = defect_comment;
    }

    public String getDefected() {
        return defected;
    }

    public void setDefected(String defected) {
        this.defected = defected;
    }

}
