package com.ultimi.easycollege;

public class CollegeModel {
    // Stores college information for Adapater
    private String mCollegeName;
    private String mGrade;
    private String mSatRange;
    private String mAcceptanceRate;
    private String mLocation;
    private String mPrice;
    private String mActRange;

    public CollegeModel(String collegeName, String nicheGrade, String satRange, String acceptanceRate, String location, String price, String actRange) {
        mCollegeName = collegeName;
        mGrade = nicheGrade;
        mSatRange = satRange;
        mAcceptanceRate = acceptanceRate;
        mLocation = location;
        mPrice = price;
        mActRange = actRange;
    }

    public String getCollegeName() {
        return mCollegeName;
    }

    public String getNicheGrade() {
        return mGrade;
    }

    public String getSatRange() {
        return mSatRange;
    }

    public String getAcceptanceRate() {
        return mAcceptanceRate;
    }

    public String getLocation() {
        return mLocation;
    }

    public String getPrice() {
        return mPrice;
    }

    public String getActRange() {
        return mActRange;
    }
}
