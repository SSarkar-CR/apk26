package com.crate.crateam.model;

/**
 * SiteLocationModel - Optimized data model for site location
 *
 * Consolidates site location data for efficient caching and data transfer.
 */
public class SiteLocationModel {

    private int id;
    private String siteName;
    private String siteType;
    private String status;
    private String address;
    private String latitude;
    private String longitude;
    private int userId;
    private String date;

    // Default constructor
    public SiteLocationModel() {
    }

    // Constructor for basic data
    public SiteLocationModel(int id, String siteName, String siteType) {
        this.id = id;
        this.siteName = siteName;
        this.siteType = siteType;
    }

    // Full constructor
    public SiteLocationModel(int id, String siteName, String siteType, String status) {
        this.id = id;
        this.siteName = siteName;
        this.siteType = siteType;
        this.status = status;
    }

    // Builder pattern for complex object creation
    public static class Builder {
        private final SiteLocationModel model = new SiteLocationModel();

        public Builder setId(int id) {
            model.id = id;
            return this;
        }

        public Builder setSiteName(String siteName) {
            model.siteName = siteName;
            return this;
        }

        public Builder setSiteType(String siteType) {
            model.siteType = siteType;
            return this;
        }

        public Builder setStatus(String status) {
            model.status = status;
            return this;
        }

        public Builder setAddress(String address) {
            model.address = address;
            return this;
        }

        public Builder setLatitude(String latitude) {
            model.latitude = latitude;
            return this;
        }

        public Builder setLongitude(String longitude) {
            model.longitude = longitude;
            return this;
        }

        public Builder setUserId(int userId) {
            model.userId = userId;
            return this;
        }

        public Builder setDate(String date) {
            model.date = date;
            return this;
        }

        public SiteLocationModel build() {
            return model;
        }
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getSiteName() {
        return siteName;
    }

    public String getSiteType() {
        return siteType;
    }

    public String getStatus() {
        return status;
    }

    public String getAddress() {
        return address;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public int getUserId() {
        return userId;
    }

    public String getDate() {
        return date;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public void setSiteType(String siteType) {
        this.siteType = siteType;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "SiteLocationModel{" +
                "id=" + id +
                ", siteName='" + siteName + '\'' +
                ", siteType='" + siteType + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SiteLocationModel that = (SiteLocationModel) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
