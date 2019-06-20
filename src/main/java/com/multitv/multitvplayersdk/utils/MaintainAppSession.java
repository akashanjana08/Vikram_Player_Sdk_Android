package com.multitv.multitvplayersdk.utils;

import android.content.Context;
import android.util.Log;

import com.multitv.multitvcommonsdk.utils.Constant;
import com.multitv.multitvcommonsdk.utils.DeviceInfo;
import com.multitv.multitvcommonsdk.utils.HttpUtils;
import com.multitv.multitvcommonsdk.utils.ToastMessage;

import org.json.JSONObject;

import java.util.concurrent.ScheduledFuture;

/**
 * Created by root on 15/4/16.
 */
public class MaintainAppSession implements Runnable {

    private String TAG = "MaintainAppSession";
    private String token;
    private Context context;
    private String sessionID;
    private ScheduledFuture scheduledFuture;

    public MaintainAppSession(String appToken, Context ctx) {
        this.token = appToken;
        this.context = ctx;
//        Log.d(TAG, "MaintainAppSession constructor");
    }

    @Override
    public void run() {
        try {
            DeviceInfo info = new DeviceInfo(context);
            sessionID = info.getAppSessionID();
            Log.d(TAG, "" + sessionID);
            if (sessionID != null && sessionID.trim().length() > 0) {
                String urlParams = "sid/" + sessionID + "/token/" + token + "/screen_resolution/" + info.getDeviceResolution();
                HttpUtils utils = new HttpUtils();
                String response = utils.executeHttpGetRequest(Constant.getInstance().getHeartBeatUrl() + urlParams);

                if (response != null && response.trim().length() > 0) {
                    JSONObject jsonObject = new JSONObject(response);

                    int code = jsonObject.optInt("code");
                    if (code == 1) {
                        sessionID = jsonObject.optString("sid");
                    }

                    ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, sessionID);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}