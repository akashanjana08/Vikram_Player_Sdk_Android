package com.multitv.multitvplayersdk.metadata;

import java.util.HashMap;

public class ChannelInfo {

    private String channelID;
    private HashMap<String, String> channelContentUrl;
    private String channelName;
    private String channelToken;
    private AddInfo adinfo;

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelToken() {
        return channelToken;
    }

    public void setChannelToken(String channelToken) {
        this.channelToken = channelToken;
    }

    public String getChannelID() {
        return channelID;
    }

    public void setChannelID(String channelID) {
        this.channelID = channelID;
    }

    public HashMap<String, String> getChannelContentUrl() {
        return channelContentUrl;
    }

    public void setChannelContentUrl(HashMap<String, String> channelContentUrl) {
        this.channelContentUrl = channelContentUrl;
    }

    /*  public String getChannelContentUrl() {
        return channelContentUrl;
    }

    public void setChannelContentUrl(String channelContentUrl) {
        this.channelContentUrl = channelContentUrl;
    }*/

    public AddInfo getAdinfo() {
        return adinfo;
    }

    public void setAdinfo(AddInfo adinfoList) {
        this.adinfo = adinfoList;
    }

}
