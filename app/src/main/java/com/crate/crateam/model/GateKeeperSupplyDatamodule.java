package com.crate.crateam.model;

public class GateKeeperSupplyDatamodule {
    private String collection_address;
    private String delivery_address;
    private String ticket_no;
    private String material_name;
    public GateKeeperSupplyDatamodule(){}


    public String getCollection_address() {
        return collection_address;
    }

    public void setCollection_address(String collection_address) {
        this.collection_address = collection_address;
    }

    public String getDelivery_address() {
        return delivery_address;
    }

    public void setDelivery_address(String delivery_address) {
        this.delivery_address = delivery_address;
    }

    public String getTicket_no() {
        return ticket_no;
    }

    public void setTicket_no(String ticket_no) {
        this.ticket_no = ticket_no;
    }

    public String getMaterial_name() {
        return material_name;
    }

    public void setMaterial_name(String material_name) {
        this.material_name = material_name;
    }
}
