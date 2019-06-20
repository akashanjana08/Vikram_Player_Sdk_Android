package com.multitv.multitvplayersdk.metadata;

import java.util.ArrayList;

public class AddInfo {

    private ArrayList<CampaignInfo> compaignInfoList;

/*	private String networkID;

	public String getNetworkID() {
		return networkID;
	}

	public void setNetworkID(String networkID) {
		this.networkID = networkID;
	}*/

    public ArrayList<CampaignInfo> getCompaignInfoList() {
        return compaignInfoList;
    }

    public void setCompaignInfoList(ArrayList<CampaignInfo> compaignInfoList) {
        this.compaignInfoList = compaignInfoList;
    }

}
