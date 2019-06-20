package com.multitv.multitvplayersdk.utils;

import android.util.Log;

import com.multitv.multitvplayersdk.metadata.AddInfo;
import com.multitv.multitvplayersdk.metadata.CampaignInfo;
import com.multitv.multitvplayersdk.metadata.ChannelInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonParser {

    private final String TAG = "CommonParser";


    public ChannelInfo parseContentData(int decider, String response) throws JSONException {
        ChannelInfo metadata = new ChannelInfo();

        if (response == null || response.isEmpty())
            return null;


        JSONObject object = new JSONObject(response);
        String code = object.optString("code");
        if (code == null)
            return null;
        JSONObject resultObject = object.optJSONObject("result");
//        Log.d(TAG, resultObject.toString());

        if (resultObject == null)
            return null;

        if (decider == 1 || decider == 3) {
            metadata.setChannelID(resultObject.optString("channel_id"));
            metadata.setChannelName(resultObject.optString("name"));
            metadata.setChannelToken(resultObject.optString("channel_token"));

            JSONObject urlObj = resultObject.optJSONObject("url");
            HashMap<String, String> urlMap = new HashMap<>();

            if (decider == 3 || decider == 4) {
                urlMap.put("2g", urlObj.optString("2g"));
                urlMap.put("3g", urlObj.optString("3g"));
                urlMap.put("wifi", urlObj.optString("wifi"));
                urlMap.put("main", urlObj.optString("main"));
            }
            urlMap.put("abr", urlObj.optString("abr"));
            metadata.setChannelContentUrl(urlMap);
        }
        JSONArray adArray = resultObject.optJSONArray("ad");

        AddInfo adInfoList = new AddInfo();
        if (adArray != null) {
            ArrayList<CampaignInfo> campaignInfoList = new ArrayList<>();
            for (int i = 0; i < adArray.length(); i++) {

                JSONObject campainInfoObject = adArray.optJSONObject(i)
                        .optJSONObject("campain_info");
                if (campainInfoObject != null) {
                    CampaignInfo campaignMetadata = new CampaignInfo();
                    ArrayList<String> adUrlList = new ArrayList<>();
                    ArrayList<Integer> quePointList = new ArrayList<>();
                    campaignMetadata.setCompaignType(campainInfoObject.optString("campaign_type"));
//                    Log.d(TAG, "Parsing vod type " + campainInfoObject.optString("campaign_type"));
                    JSONArray tagUrlArray = campainInfoObject.optJSONArray("tag_url");
                    if (tagUrlArray != null) {
                        for (int j = 0; j < tagUrlArray.length(); j++) {
//                            Log.d(TAG, "Parsing vod " + tagUrlArray.optString(j));
                            adUrlList.add(tagUrlArray.optString(j));
                        }
                        campaignMetadata.setCompaignUrl(adUrlList);
                    }
                    JSONArray queuePointArray = campainInfoObject.optJSONArray("que_points");
                    if (queuePointArray != null) {
                        for (int k = 0; k < queuePointArray.length(); k++) {
                            quePointList.add(Integer.parseInt(queuePointArray.optString(k)));
                        }
                        campaignMetadata.setQuePoints(quePointList);
                    }
                    campaignInfoList.add(campaignMetadata);
                }

            }
            adInfoList.setCompaignInfoList(campaignInfoList);
        }
        metadata.setAdinfo(adInfoList);


        return metadata;
    }


    public HashMap<String, String> parseHLSMetadata(InputStream i, String mUrl) {

        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(i,
                    "UTF-8"));
            String line;
            HashMap<String, String> segmentsMap = null;
            String digitRegex = "RESOLUTION=(.*?),";
            Pattern p = Pattern.compile(digitRegex);

            while ((line = r.readLine()) != null) {
                if (line.equals("#EXTM3U")) { // start of m3u8
                    segmentsMap = new HashMap<String, String>();
                    segmentsMap.put("Auto", mUrl);
                } else if (line.contains("#EXT-X-STREAM-INF")) {

                    Matcher matcher = p.matcher(line);
                    matcher.find();

                    if (line.contains("RESOLUTION")) {
                        segmentsMap.put(matcher.group(1), r.readLine());
                    }
                }
            }
            r.close();
            return segmentsMap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
