package com.multitv.multitvplayersdk.utils;

/**
 * Created by root on 11/4/16.
 */

import android.util.Log;

import com.multitv.multitvcommonsdk.utils.Constant;
import com.multitv.multitvcommonsdk.utils.ToastMessage;
import com.multitv.multitvplayersdk.MultiTvPlayer.ContentType;

public class PrepareServerPath {

    private final String TAG = "PrepareServerPath";
/*    private final String BASE_URL = "http://dev.multitvsolution.com/automator/api/";
    private final String GET_SERVER_STREAM_LIVE = BASE_URL + "stream/stream_detail/type/android";
    private final String GET_SERVER_STREAM_VOD = BASE_URL + "ads/adDetail/device/android";*/


    public int getContentCallDecider(ContentType contentType, String cID) {
        int i = 0;
        switch (contentType) {
            case LIVE:
                if (cID != null && cID.trim().length() > 0) {
                    i = 1;
                } else {
                    i = 2;
                }
                break;

            case VOD:
                if (cID != null && cID.trim().length() > 0) {
                    i = 3;
                } else {
                    i = 4;
                }
                break;

            default:

                break;
        }
        Log.d(TAG, "::: " + i);
        return i;
    }

    public String getAdNetworkInfoPath(int decider, String cID, String token) {

        String url = null;

        switch (decider) {
            case 1:
                url = Constant.getInstance().getGetLiveDetail() + "/id/" + cID + "/token/" + token;

                break;
            case 2:

                url = Constant.getInstance().getGetLiveDetail() + "/token/" + token;

                break;

            case 3:
                url = Constant.getInstance().getGetVodDetail() + "/cid/" + cID + "/token/" + token;

                break;

            case 4:
                url = Constant.getInstance().getGetVodDetail() + "/token/" + token;

                break;
            default:

                break;
        }

//        Log.d(TAG, url);
        ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, url);

        return url;
    }
}
