package com.multitv.multitvplayersdk.metadata;

import java.util.ArrayList;

public class CampaignInfo {
    private String compaignType;
    private ArrayList<String> compaignUrl;
    private ArrayList<Integer> quePoints;

    public String getCompaignType() {
        return compaignType;
    }

    public void setCompaignType(String compaignType) {
        this.compaignType = compaignType;
    }

    public ArrayList<Integer> getQuePoints() {
        return quePoints;
    }

    public void setQuePoints(ArrayList<Integer> quePoints) {
        this.quePoints = quePoints;
    }

    public ArrayList<String> getCompaignUrl() {
        return compaignUrl;
    }

    public void setCompaignUrl(ArrayList<String> compaignUrl) {
        this.compaignUrl = compaignUrl;
    }
}
