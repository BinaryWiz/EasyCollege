package com.ultimi.easycollege;

public class CollegeModel {

    private String mCollegeName;
    private String mGrade;
    private String mSatRange;
    private String mAcceptanceRate;
    private String mLocation;
    private String mPrice;

    public CollegeModel(String collegeName, String nicheGrade, String satRange, String acceptanceRate, String location, String price) {
        mCollegeName = collegeName;
        mGrade = nicheGrade;
        mSatRange = satRange;
        mAcceptanceRate = acceptanceRate;
        mLocation = location;
        mPrice = price;
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
}
