package com.crate.crateam.model;

public class ReportsList {
    private String inspection_id;
    private int vehicle_id;
    private String registration_no;
    private int logged_by;
    private String vehicle_defect;
    private String trailer_defect;
    private String conducted_on;
    private String driver_name;
    private String id;

    public ReportsList(){
      //no-argument constructor for Firebase.
    }

    public String getInspection_id() {
        return inspection_id;
    }

    public void setInspection_id(String inspection_id) {
        this.inspection_id = inspection_id;
    }

    public int getVehicle_id() {
        return vehicle_id;
    }

    public void setVehicle_id(int vehicle_id) {
        this.vehicle_id = vehicle_id;
    }

    public String getRegistration_no() {
        return registration_no;
    }

    public void setRegistration_no(String registration_no) {
        this.registration_no = registration_no;
    }

    public int getLogged_by() {
        return logged_by;
    }

    public void setLogged_by(int logged_by) {
        this.logged_by = logged_by;
    }

    public String getVehicle_defect() {
        return vehicle_defect;
    }

    public void setVehicle_defect(String vehicle_defect) {
        this.vehicle_defect = vehicle_defect;
    }

    public String getTrailer_defect() {
        return trailer_defect;
    }

    public void setTrailer_defect(String trailer_defect) {
        this.trailer_defect = trailer_defect;
    }

    public String getConducted_on() {
        return conducted_on;
    }

    public void setConducted_on(String conducted_on) {
        this.conducted_on = conducted_on;
    }

    public String getDriver_name() {
        return driver_name;
    }

    public void setDriver_name(String driver_name) {
        this.driver_name = driver_name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
