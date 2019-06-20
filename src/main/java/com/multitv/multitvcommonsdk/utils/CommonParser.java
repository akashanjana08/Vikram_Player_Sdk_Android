package com.multitv.multitvcommonsdk.utils;

import android.content.Context;

import com.multitv.multitvcommonsdk.metadata.ChannelContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CommonParser {

    public ArrayList<ChannelContent> parseChannelInfo(String data) {

        ArrayList<ChannelContent> resultList = new ArrayList<ChannelContent>();
        ChannelContent metadata;
        if (data == null && data.trim().length() == 0)
            return null;
        try {
            JSONObject object = new JSONObject(data);

            if (object == null)
                return null;

            int code = object.optInt("code");
            if (code != 1)
                return null;

            JSONArray resultArray = object.optJSONArray("result");
            if (resultArray == null)
                return null;

            for (int i = 0; i < resultArray.length(); i++) {
                JSONObject valueObject = resultArray.optJSONObject(i);
                if (valueObject == null)
                    continue;
                metadata = new ChannelContent();
                metadata.setChannelID(valueObject.optString("id"));
                metadata.setChannelName(valueObject.optString("channel_name"));
                metadata.setChannelStreamUrl(valueObject.optString("url"));
                resultList.add(metadata);
            }
            return resultList;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    public void parseUserInfo(String response, Context ctx) {

        try {
            JSONObject object = new JSONObject(response);
            String status = object.optString("status");
            if (status != null && status.equalsIgnoreCase("success")) {
                JSONObject dataObj = object.optJSONObject("data");
                if (dataObj != null) {
                    String age = dataObj.optString("birthday");
                    String gender = dataObj.optString("gender");
                    if (gender != null) {
                        if (gender.equalsIgnoreCase("male"))
                            gender = "m";
                        else
                            gender = "f";
                    }

                    String pattern = "yyyy-MM-dd";
                    Date date = new SimpleDateFormat(pattern).parse(age.trim());
                    Calendar birthCalender = Calendar.getInstance();
                    Calendar currentCal = Calendar.getInstance();
                    birthCalender.setTime(date);

                    ToastMessage.showLogs(ToastMessage.LogType.DEBUG, "parseUserInfo", "" + currentCal.YEAR);
                    age = "" + (currentCal.get(Calendar.YEAR) - birthCalender.get(Calendar.YEAR));
                    String[] infoArray = {age, gender};

                    new DeviceInfo(ctx).storeArrayInfo("USER_INFO", infoArray);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
