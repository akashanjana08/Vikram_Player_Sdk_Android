package com.multitv.multitvcommonsdk.utils;

public class Constant {

    private static Constant constant;
    private final boolean enableDebug = true;
    private boolean isAdSkipable = true;
    private boolean isPreRollEnabled = true;
    private boolean isReadStreamTag = false;

    private String baseUrl = "http://automator.multitvsolution.com/api/v1/"; //Production
    /*private String baseUrl = "http://dev.multitvsolution.com/automator/api/v1/";*/
    //private String baseUrl = "http://dev.multitvsolution.com/automator/api/v2/token/";
    /*private String baseUrl = "http://dev.multitvsolution.com/automator/api/";*/
    private String channelListUrl = baseUrl
            + "stream/channel_list/platform/android/token/";
    private String heartBeatUrl = baseUrl + "stream/heart_beat_web/";
    private String getEPGUrl = baseUrl + "content/epg/current_version/0.0/token/";
    private String postAnalyticsUrl = baseUrl + "analytics/event/";
    /*private String postAnalyticsUrl = baseUrl + "analytics/event/token/";*/
    private String detailedAnalyticsUrl = "http://dev.multitvsolution.com/mongodata/insert.php";
    private String broadcastServerUrl = "http://49.40.0.136:80/"; //"http://notification.multitvsolution.com:80/";
    //    private String broadcastServerUrl = "http://multitvsolution.com:8020/";
    private String adMediation = "http://multitvsolution.com/automator/assets/admediation/default.xml";

    /*private final String getLiveDetail = baseUrl + "stream/stream_detail/type/android";*/
    private final String getLiveDetail = baseUrl + "stream/stream_detail/platform/android";
    private final String getVodDetail = baseUrl + "ads/adDetail/device/android";
    private final String postServerAnalytics = baseUrl + "stream/cue_sheet/token/";
    private final String postAdImpression = baseUrl + "impression/ck/token/";
    private final String postVODAdImpression = baseUrl + "impression/ckvod/token/";
    private final String postClickEvent = baseUrl + "impression/click/token/";
    private final String fetchGPlusProfile = " http://dev.multitvsolution.com/plus/info/user.php?device_type=android&email=";

    /*
     * private String broadcastServerUrl =
	 * "http://admin.multitvsolution.com:3000";
	 */

    //    private String postAnalyticsUrl = baseUrl + "stream/analytics/token/";

//	private String broadcastServerUrl = "http://52.74.85.115:80";


    public String getFetchGPlusProfile() {
        return fetchGPlusProfile;
    }

    public String getGetVodDetail() {
        return getVodDetail;
    }

    public String getGetLiveDetail() {
        return getLiveDetail;
    }

    public String getAdMediation() {
        return adMediation;
    }

    public static Constant getInstance() {

        if (constant == null) {
            constant = new Constant();
        }
        return constant;
    }

    private Constant() {
    }

    public String getChannelListUrl() {
        return this.channelListUrl;
    }

    public String getPostAnalyticsUrl() {
        return this.postAnalyticsUrl;
    }

    public String getHeartBeatUrl() {
        //TODO: Still pending to implement
        return heartBeatUrl;
    }

    public String getBroadcastServerUrl() {
        return broadcastServerUrl;
    }

    public String getPostAdImpression() {
        return postAdImpression;
    }

    public String getPostServerAnalytics() {
        return postServerAnalytics;
    }

    public boolean isEnableDebug() {
        return enableDebug;
    }

    public boolean isAdSkipable() {
        return isAdSkipable;
    }

    public static Constant getConstant() {
        return constant;
    }

    public String getPostVODAdImpression() {
        return postVODAdImpression;
    }

    public String getPostClickEvent() {
        return postClickEvent;
    }

    public boolean isPreRollEnabled() {
        return isPreRollEnabled;
    }

    public void setPreRollEnabled(boolean preRollEnabled) {
        isPreRollEnabled = preRollEnabled;
    }

    public void setAdSkipable(boolean adSkipable) {
        isAdSkipable = adSkipable;
    }

    public String getDetailedAnalyticsUrl() {
        return detailedAnalyticsUrl;
    }

    public void setDetailedAnalyticsUrl(String detailedAnalyticsUrl) {
        this.detailedAnalyticsUrl = detailedAnalyticsUrl;
    }

    public String getGetEPGUrl() {
        return getEPGUrl;
    }

    public void setGetEPGUrl(String getEPGUrl) {
        this.getEPGUrl = getEPGUrl;
    }

    public boolean isReadStreamTag() {
        return isReadStreamTag;
    }

    public void setReadStreamTag(boolean readStreamTag) {
        this.isReadStreamTag = readStreamTag;
    }
}
