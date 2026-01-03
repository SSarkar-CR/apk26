package com.crate.crateam.model;

public class AssignTasksList {
    private String status;
    private String collection_point;
    private String delivery_point;
    private String material_name;
    private String contact_name;
    private String comments;

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getCollection_point() {
        return collection_point;
    }

    public void setCollection_point(String collection_point) {
        this.collection_point = collection_point;
    }

    public String getDelivery_point() {
        return delivery_point;
    }

    public void setDelivery_point(String delivery_point) {
        this.delivery_point = delivery_point;
    }

    public String getMaterial_name() {
        return material_name;
    }

    public void setMaterial_name(String material_name) {
        this.material_name = material_name;
    }

    public String getContact_name() {
        return contact_name;
    }
    public void setContact_name(String contact_name) {
        this.contact_name = contact_name;
    }

    public String getComments() {
        return comments;
    }
    public void setComments(String comments) {
        this.comments = comments;
    }

    public AssignTasksList(){
      //no-argument constructor for Firebase.
    }
}
