package com.multitv.multitvcommonsdk.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.multitv.multitvcommonsdk.ServerSyncService;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class SendAnalytics extends AsyncTask<String, Void, String> {

    private String TAG = "SendAnalytics";
    private Context context;
    private double lat = 0, lng = 0;
    private String appToken;
    private BroadcastReceiver receivers;
    private GPSTracker tracker;
    private String channelID, channelToken;
    private int decider;

    public SendAnalytics(Context ctx, String token, String channelID, String channelToken,
                         BroadcastReceiver receiver, int decider) {
        this.context = ctx;
        this.appToken = token;
        this.receivers = receiver;
        this.channelID = channelID;
        this.channelToken = channelToken;
        this.decider = decider;
        tracker = new GPSTracker(context);
    }

    @Override
    protected String doInBackground(String... params) {

        try {
            HttpUtils httpClientAnalytics = new HttpUtils();
            if (tracker.canGetLocation()) {
                lat = tracker.getLatitude();
                lng = tracker.getLongitude();
            }

            String paramAnalytics = prepareAnalyticParam(decider);
            ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, params[0] + " :::: " + paramAnalytics);
            String responseAnalytics = httpClientAnalytics.excuteHttpPostRequest(params[0], paramAnalytics);
            ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "Response Analytics ::: " + responseAnalytics);

            HttpUtils httpClientDetailedAnalytics = new HttpUtils();
            String paramDetailedAnalytics = prepareDetailedAnalyticParam();
            ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, Constant.getInstance().getDetailedAnalyticsUrl() + " :::: " + paramDetailedAnalytics);
            String responseDetailedAnalytics = httpClientDetailedAnalytics.excuteHttpPostRequest(Constant.getInstance().getDetailedAnalyticsUrl()
                    , paramDetailedAnalytics);

            return responseAnalytics;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result != null && result.trim().length() > 0)
            try {
                String sessionID = "";
                JSONObject object = new JSONObject(result);
                try {
                    if (object != null) {
                        int code = object.optInt("code");
                        if (code == 1) {
                            String rslt = object.optString("result");
                            System.out.println("Analytics response is:: " + rslt);
                            if (rslt != null) {
                                JSONObject resultObj = new JSONObject(rslt);
                                if (resultObj != null) {
                                    sessionID = resultObj
                                            .getString("app_session_id");
                                    new DeviceInfo(context).storeAppSessionID(sessionID);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (decider == 1) {
                    ToastMessage.showToastMsg(context, "Initilizing service", Toast.LENGTH_LONG);
                    Intent intent = new Intent(context, ServerSyncService.class);
                    intent.putExtra("TOKEN", appToken);
                    intent.putExtra("CHANNEL_ID", channelID);
                    intent.putExtra("CHANNEL_TOKEN", channelToken);
                    intent.putExtra("SESSION_ID", sessionID);

                    LocalBroadcastManager.getInstance(context).registerReceiver(
                            (receivers), new IntentFilter("Multi_Tv_Filter"));

                    if (isMyServiceRunning(ServerSyncService.class)) {
                        context.stopService(intent);
                    }
                    context.startService(intent);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private String prepareAnalyticParam(int decider) throws UnsupportedEncodingException {

        int live = 0;
        String urlParams = "";
        DeviceInfo deviceInfo = new DeviceInfo(context);

        if (decider == 1 || decider == 2)
            live = 1;
        else
            live = 2;

        if (channelID != null && channelID.trim().length() > 0)
            urlParams = "id=" + channelID;


        /*urlParams = urlParams + "&ak=" + appToken + "&type=" + live
                + "&od="
                + URLEncoder.encode(deviceInfo.getDeviceOtherDetail(),
                "UTF-8") + "&dd="
                + URLEncoder.encode(deviceInfo.getDeviceDetail(), "UTF-8")
                + "&lat=" + lat + "&lng=" + lng;*/

        urlParams = urlParams + "&ak=" + appToken + "&type=" + live
                + "&od="
                + deviceInfo.getDeviceOtherDetail()
                + "&dd="
                + deviceInfo.getDeviceDetail()
                + "&lat=" + lat + "&lng=" + lng;

        return urlParams;
    }

    private String prepareDetailedAnalyticParam() {
        return "table=detailed_analytics" + "&data=" + new EasyDeviceInfo(context).createJson();
    }
}
