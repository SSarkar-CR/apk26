package com.crate.crateam.model;

/**
 * InspectionDetailsModel - Optimized data model for inspection details
 *
 * Consolidates inspection data for efficient data transfer between activities
 * and reduces memory overhead from passing multiple extras.
 */
public class InspectionDetailsModel {

    private String inspectionId;
    private String assetType;
    private String assetNumber;
    private String assetName;
    private int assetId;
    private int assetTypeId;
    private int regimeId;
    private String conductedOn;
    private String userName;
    private int userId;
    private String userRole;
    private String inspectorName;
    private String assetStatus;
    private String defected;
    private String defectInspection;
    private int assignLocation;
    private String siteType;
    private String latitude;
    private String longitude;
    private String address;
    private String submissionTime;
    private String deviceId;
    private String imageOne;
    private String imageTwo;
    private String inspectorSign;
    private String wmSign;
    private String activity;
    private String status;
    private String timeSecondFormat;
    private String assignUserStatus;

    // Default constructor
    public InspectionDetailsModel() {
    }

    // Constructor for basic data
    public InspectionDetailsModel(String inspectionId, String assetType, String assetNumber,
                                  String assetName, int assetId, int regimeId) {
        this.inspectionId = inspectionId;
        this.assetType = assetType;
        this.assetNumber = assetNumber;
        this.assetName = assetName;
        this.assetId = assetId;
        this.regimeId = regimeId;
    }

    // Builder pattern for complex object creation
    public static class Builder {
        private final InspectionDetailsModel model = new InspectionDetailsModel();

        public Builder setInspectionId(String inspectionId) {
            model.inspectionId = inspectionId;
            return this;
        }

        public Builder setAssetType(String assetType) {
            model.assetType = assetType;
            return this;
        }

        public Builder setAssetNumber(String assetNumber) {
            model.assetNumber = assetNumber;
            return this;
        }

        public Builder setAssetName(String assetName) {
            model.assetName = assetName;
            return this;
        }

        public Builder setAssetId(int assetId) {
            model.assetId = assetId;
            return this;
        }

        public Builder setAssetTypeId(int assetTypeId) {
            model.assetTypeId = assetTypeId;
            return this;
        }

        public Builder setRegimeId(int regimeId) {
            model.regimeId = regimeId;
            return this;
        }

        public Builder setConductedOn(String conductedOn) {
            model.conductedOn = conductedOn;
            return this;
        }

        public Builder setUserName(String userName) {
            model.userName = userName;
            return this;
        }

        public Builder setUserId(int userId) {
            model.userId = userId;
            return this;
        }

        public Builder setUserRole(String userRole) {
            model.userRole = userRole;
            return this;
        }

        public Builder setInspectorName(String inspectorName) {
            model.inspectorName = inspectorName;
            return this;
        }

        public Builder setAssetStatus(String assetStatus) {
            model.assetStatus = assetStatus;
            return this;
        }

        public Builder setDefected(String defected) {
            model.defected = defected;
            return this;
        }

        public Builder setDefectInspection(String defectInspection) {
            model.defectInspection = defectInspection;
            return this;
        }

        public Builder setAssignLocation(int assignLocation) {
            model.assignLocation = assignLocation;
            return this;
        }

        public Builder setSiteType(String siteType) {
            model.siteType = siteType;
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

        public Builder setAddress(String address) {
            model.address = address;
            return this;
        }

        public Builder setSubmissionTime(String submissionTime) {
            model.submissionTime = submissionTime;
            return this;
        }

        public Builder setDeviceId(String deviceId) {
            model.deviceId = deviceId;
            return this;
        }

        public Builder setImageOne(String imageOne) {
            model.imageOne = imageOne;
            return this;
        }

        public Builder setImageTwo(String imageTwo) {
            model.imageTwo = imageTwo;
            return this;
        }

        public Builder setInspectorSign(String inspectorSign) {
            model.inspectorSign = inspectorSign;
            return this;
        }

        public Builder setWmSign(String wmSign) {
            model.wmSign = wmSign;
            return this;
        }

        public Builder setActivity(String activity) {
            model.activity = activity;
            return this;
        }

        public Builder setStatus(String status) {
            model.status = status;
            return this;
        }

        public Builder setTimeSecondFormat(String timeSecondFormat) {
            model.timeSecondFormat = timeSecondFormat;
            return this;
        }

        public Builder setAssignUserStatus(String assignUserStatus) {
            model.assignUserStatus = assignUserStatus;
            return this;
        }

        public InspectionDetailsModel build() {
            return model;
        }
    }

    // Getters
    public String getInspectionId() {
        return inspectionId;
    }

    public String getAssetType() {
        return assetType;
    }

    public String getAssetNumber() {
        return assetNumber;
    }

    public String getAssetName() {
        return assetName;
    }

    public int getAssetId() {
        return assetId;
    }

    public int getAssetTypeId() {
        return assetTypeId;
    }

    public int getRegimeId() {
        return regimeId;
    }

    public String getConductedOn() {
        return conductedOn;
    }

    public String getUserName() {
        return userName;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserRole() {
        return userRole;
    }

    public String getInspectorName() {
        return inspectorName;
    }

    public String getAssetStatus() {
        return assetStatus;
    }

    public String getDefected() {
        return defected;
    }

    public String getDefectInspection() {
        return defectInspection;
    }

    public int getAssignLocation() {
        return assignLocation;
    }

    public String getSiteType() {
        return siteType;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public String getSubmissionTime() {
        return submissionTime;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getImageOne() {
        return imageOne;
    }

    public String getImageTwo() {
        return imageTwo;
    }

    public String getInspectorSign() {
        return inspectorSign;
    }

    public String getWmSign() {
        return wmSign;
    }

    public String getActivity() {
        return activity;
    }

    public String getStatus() {
        return status;
    }

    public String getTimeSecondFormat() {
        return timeSecondFormat;
    }

    public String getAssignUserStatus() {
        return assignUserStatus;
    }

    // Setters
    public void setInspectionId(String inspectionId) {
        this.inspectionId = inspectionId;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public void setAssetNumber(String assetNumber) {
        this.assetNumber = assetNumber;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public void setAssetTypeId(int assetTypeId) {
        this.assetTypeId = assetTypeId;
    }

    public void setRegimeId(int regimeId) {
        this.regimeId = regimeId;
    }

    public void setConductedOn(String conductedOn) {
        this.conductedOn = conductedOn;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public void setInspectorName(String inspectorName) {
        this.inspectorName = inspectorName;
    }

    public void setAssetStatus(String assetStatus) {
        this.assetStatus = assetStatus;
    }

    public void setDefected(String defected) {
        this.defected = defected;
    }

    public void setDefectInspection(String defectInspection) {
        this.defectInspection = defectInspection;
    }

    public void setAssignLocation(int assignLocation) {
        this.assignLocation = assignLocation;
    }

    public void setSiteType(String siteType) {
        this.siteType = siteType;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setSubmissionTime(String submissionTime) {
        this.submissionTime = submissionTime;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setImageOne(String imageOne) {
        this.imageOne = imageOne;
    }

    public void setImageTwo(String imageTwo) {
        this.imageTwo = imageTwo;
    }

    public void setInspectorSign(String inspectorSign) {
        this.inspectorSign = inspectorSign;
    }

    public void setWmSign(String wmSign) {
        this.wmSign = wmSign;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTimeSecondFormat(String timeSecondFormat) {
        this.timeSecondFormat = timeSecondFormat;
    }

    public void setAssignUserStatus(String assignUserStatus) {
        this.assignUserStatus = assignUserStatus;
    }

    @Override
    public String toString() {
        return "InspectionDetailsModel{" +
                "inspectionId='" + inspectionId + '\'' +
                ", assetName='" + assetName + '\'' +
                ", assetStatus='" + assetStatus + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InspectionDetailsModel that = (InspectionDetailsModel) o;
        return inspectionId != null ? inspectionId.equals(that.inspectionId) : that.inspectionId == null;
    }

    @Override
    public int hashCode() {
        return inspectionId != null ? inspectionId.hashCode() : 0;
    }
}
