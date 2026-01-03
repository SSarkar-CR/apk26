package com.crate.crateam.model;

public class TaskCompletedList {
    private String ticket_no;
    private int task_order_no;
    private String collection_point;
    private String delivery_point;
    private String material_name;

    public String getTicket_no() {
        return ticket_no;
    }
    public void setTicket_no(String ticket_no) {
        this.ticket_no = ticket_no;
    }
    public int getTask_order_no() {
        return task_order_no;
    }

    public void setTask_order_no(int task_order_no) {
        this.task_order_no = task_order_no;
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

    public TaskCompletedList(){
      //no-argument constructor for Firebase.
    }
}
