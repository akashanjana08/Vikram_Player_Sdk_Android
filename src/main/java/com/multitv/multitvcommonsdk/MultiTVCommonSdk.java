package com.multitv.multitvcommonsdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.os.AsyncTaskCompat;

import com.multitv.multitvcommonsdk.metadata.ChannelContent;
import com.multitv.multitvcommonsdk.utils.AsyncThreadUtils;
import com.multitv.multitvcommonsdk.utils.CommonParser;
import com.multitv.multitvcommonsdk.utils.Constant;
import com.multitv.multitvcommonsdk.utils.DeviceInfo;
import com.multitv.multitvcommonsdk.utils.HttpUtils;
import com.multitv.multitvcommonsdk.utils.SendAnalytics;
import com.multitv.multitvcommonsdk.utils.ToastMessage;

import java.util.ArrayList;

public class MultiTVCommonSdk {

    private final String TAG = "MultiTVCommonSdk";
    private Context context;
    private String appToken;
    private AdDetectionListner detectionListner;

    /**
     * CommonSDK constructor
     *
     * @param ctx   Application/ Activity context
     * @param token Registered app token from MultiTV platform
     */
    public MultiTVCommonSdk(Context ctx, String token, AdDetectionListner object) {
        if (token == null || ctx == null || object == null)
            throw new NullPointerException();
        this.context = ctx;
        this.appToken = token;
        this.detectionListner = object;
    }

    public MultiTVCommonSdk(Context ctx, AdDetectionListner object) {
        this(ctx, "", object);

    }

    public ArrayList<ChannelContent> getChannelList() {
        ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, Constant
                .getInstance().getChannelListUrl() + appToken);
        String response = new HttpUtils().executeHttpGetRequest(Constant
                .getInstance().getChannelListUrl() + appToken);
        ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, response);
        CommonParser parser = new CommonParser();
        ArrayList<ChannelContent> resultList = parser
                .parseChannelInfo(response);
        String responseUserInfo = new HttpUtils().executeHttpGetRequest(Constant
                .getInstance().getFetchGPlusProfile() + new DeviceInfo(context).getPrimaryAccountDetail());
        parser.parseUserInfo(responseUserInfo, context);


        return resultList;
    }

    public void initiateProcess(String contentID, String channelToken, int decider) {
        if (decider == 1 || decider == 3)
            if (contentID == null)
                throw new NullPointerException();
        SendAnalytics analyticsAsync = new SendAnalytics(context, appToken,
                contentID, channelToken, mMessageReceiver, decider);
        AsyncTaskCompat.executeParallel(analyticsAsync, Constant.getInstance().getPostAnalyticsUrl() + appToken);
    }

    public void release() {
        try {
            Intent intent = new Intent(context, ServerSyncService.class);
            context.stopService(intent);
            LocalBroadcastManager.getInstance(context).unregisterReceiver(
                    mMessageReceiver);
            detectionListner = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // private class FetchChannelDetails extends
    // AsyncTask<String, Void, ArrayList<ChannelInfo>> {
    //
    // @Override
    // protected ArrayList<ChannelInfo> doInBackground(String... params) {
    // String url = params[0];
    //
    // String response = new HttpUtils().executeHttpGetRequest(url);
    // ArrayList<ChannelInfo> resultList = new CommonParser()
    // .parseChannelInfo(response);
    // return resultList;
    // }
    //
    // @Override
    // protected void onPostExecute(ArrayList<ChannelInfo> result) {
    // super.onPostExecute(result);
    // }
    //
    // }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String currentStatusTag = intent.getExtras().getString("VALUE");
                String sessionID = intent.getExtras().getString("SESSION_ID");
                if (currentStatusTag != null
                        && currentStatusTag.trim().length() > 0)
                    if (detectionListner != null)
                        detectionListner.onAdCallback(currentStatusTag,
                                sessionID);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    public interface AdDetectionListner {
        void onAdCallback(String value, String session);
    }
}
