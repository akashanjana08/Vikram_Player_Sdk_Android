package com.multitv.multitvplayersdk;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v4.os.AsyncTaskCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer.multitv.AspectRatioFrameLayout;
import com.google.android.exoplayer.multitv.ExoPlayer;
import com.google.android.exoplayer.multitv.metadata.id3.Id3Frame;
import com.google.android.exoplayer.multitv.metadata.id3.TxxxFrame;
import com.google.android.exoplayer.multitv.util.PlayerControl;
import com.multitv.multitvcommonsdk.MultiTVCommonSdk;
import com.multitv.multitvcommonsdk.MultiTVCommonSdk.AdDetectionListner;
import com.multitv.multitvcommonsdk.permission.PermissionChecker;
import com.multitv.multitvcommonsdk.permission.PermissionCheckerInterface;
import com.multitv.multitvcommonsdk.permission.PermissionId;
import com.multitv.multitvcommonsdk.utils.Constant;
import com.multitv.multitvcommonsdk.utils.DeviceInfo;
import com.multitv.multitvcommonsdk.utils.GPSTracker;
import com.multitv.multitvcommonsdk.utils.HttpUtils;
import com.multitv.multitvcommonsdk.utils.MultiTVException;
import com.multitv.multitvcommonsdk.utils.NetowrkUtil;
import com.multitv.multitvcommonsdk.utils.ToastMessage;
import com.multitv.multitvplayersdk.analytics.CueSheet;
import com.multitv.multitvplayersdk.controls.VideoControllerView;
import com.multitv.multitvplayersdk.customeviews.LoadingView;
import com.multitv.multitvplayersdk.metadata.AddInfo;
import com.multitv.multitvplayersdk.metadata.CampaignInfo;
import com.multitv.multitvplayersdk.metadata.ChannelInfo;
import com.multitv.multitvplayersdk.network.ConnectionClassManager;
import com.multitv.multitvplayersdk.network.ConnectionQuality;
import com.multitv.multitvplayersdk.network.DeviceBandwidthSampler;
import com.multitv.multitvplayersdk.playerconfig.MediaPlayer;
import com.multitv.multitvplayersdk.playerconfig.MediaPlayer.Listener;
import com.multitv.multitvplayersdk.playerconfig.MyDialogFragment;
import com.multitv.multitvplayersdk.playerconfig.MyDialogFragment.ResolutionSelection;
import com.multitv.multitvplayersdk.playerconfig.ScalableView;
import com.multitv.multitvplayersdk.utils.AdvertisingIdClient;
import com.multitv.multitvplayersdk.utils.AdvertisingIdClient.AdInfo;
import com.multitv.multitvplayersdk.utils.AspectRatioUtils;
import com.multitv.multitvplayersdk.utils.CommonParser;
import com.multitv.multitvplayersdk.utils.CommonUtils;
import com.multitv.multitvplayersdk.utils.CountDownTimerWithPause;
import com.multitv.multitvplayersdk.utils.MaintainAppSession;
import com.multitv.multitvplayersdk.utils.PausableExecutor;
import com.multitv.multitvplayersdk.utils.PrepareServerPath;
import com.multitv.multitvplayersdk.utils.VASTXmlParser;
import com.multitv.multitvplayersdk.utils.VASTXmlParser.Tracking;
import com.multitv.multitvplayersdk.utils.VASTXmlParser.VASTXmlListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import mbanje.kurt.fabbutton.FabButton;

public class MultiTvPlayer extends FrameLayout implements View.OnClickListener, AdDetectionListner,
        ResolutionSelection, MediaPlayer.Id3MetadataListener, VideoControllerView.Controlers,
        PermissionCheckerInterface, VideoControllerView.ResolutionViewListener {

    private final String TAG = "MultiTvPlayer";
    private final String AKAMAI_KEY = "5EE0147956950D2E808241B20CDC2E37";
    private String acl, timeWindow = "100";
    private ConnectionClassManager mConnectionClassManager;
    private DeviceBandwidthSampler mDeviceBandwidthSampler;
    private ConnectionChangedListener mListener;
    private ConnectionQuality mConnectionClass = ConnectionQuality.UNKNOWN;
    private VideoControllerView mediaController;

    private Context context;
    private ScalableView mSurfaceView, mSurfaceViewAdd1, mSurfaceViewAdd2;
    private MediaPlayer mMediaPlayer, mMediaPlayerAdd1, mMediaPlayerAdd2;
    private long duration = 0, adTicker = 0;
    private boolean preRollPlayed, isMediationActive;
    private Timer adTimer, checkerTimer;
    private String activeMediaplayer = "", currentPlaying = "";
    private VASTXmlListener listener;
    private VASTXmlParser vastParser;
    /*
     * Resources
     */
    private String mContentUrl;
    private boolean needControls;
    //    private LinearLayout controlLayout;
    private Bitmap logo;
    private String keyToken, channelID, contentUrl;
    private int contentCallDecider = 0, skipableAdCounter = 6, pauseCalledCounter = 0;
    private LoadingView loadingView;
    //    private SeekBar musicSeekbar, brightnesSeekBar, progressSeekBar;
//    private VerticalSeekBar musicSeekBarGesture, brightnessSeekBarGesture;
//    private AudioManager audioManager;
//    private float gestureStoredValue1 = 100, gestureStoredValue2 = 100;
    private String currentStatusTag = "", previousStatusTag, socketStatusTag = "";
    private TextView surfaceText, skipAdIndicator, stitchingStatus;
    private boolean autoStart;
    private String advertisingId;
    private AspectRatioFrameLayout frameLayoutPlayer, frameLayoutPlayerAdd1,
            frameLayoutPlayerAdd2;
    private boolean isStopped;
    private ChannelInfo channelInfo;
    private MultiTvPlayerListner interfaceObject;
    private boolean isPlayerReady, isAdPlayerReady, isFromMPCallBack, isPlayerSeekd = false;
    private String preRollBaseUrl, midRollBaseUrl;
    //            preRollNetworkType,
//            midRollNetworkType;
    private AdDetectionListner adListner;
    private MultiTVCommonSdk commonSDK;
    private CueSheet cueSheetInfo, cueSheetInfoChannel;
    private String sessionID;
    private ArrayList<CueSheet> cueSheetInfoList;
    private PausableExecutor execService;
    private ScheduledFuture<?> vodQuePointScheduler, sessionScheduler, skipAdScheduler;
    private boolean isCallBackreceived, isResolutionSelectionEnable, isCampaignNull;
    private HashMap<String, String> availableResolutionContainer;
    private Point point;
    private ImageButton resolutionSelector;
    private ContentType contentType;
    private ArrayList<Integer> vodQuePointList;
    private int seekPlayerTo;
    private Handler handler;
    private ImageView skipButton, watermarkLogo, unlockButton;
    //    private FrameLayout guestureParentLayout;
    private boolean isReadStatePermissionEnabled, isAccessCoarseLocationPermissionEnabled,
            isAccessFineLocationPermissionEnabled, isRuntimePermissionsCheckedOnce,
            isCheckOnlyPhoneStatePermission, isMakeResolutionSelectorVisible, isResumeFromPreviousPosition;
    private ArrayList<String> permissionsPermanentlyDenied;
    private RelativeLayout logoLayout;
    private int skipAdInMillis, millisecondsForResume;
    private CountDownTimerWithPause countDownTimer;

    public MultiTvPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiTvPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        point = new CommonUtils().calculateScreenDimension(context);
        CommonUtils.setDefaultCookieManager();
    }


    public void setKeyToken(String token) {
        if (token == null)
            throw new NullPointerException();
        this.keyToken = token;
    }

    public void setContentID(String id) {
        if (id == null)
            throw new NullPointerException();
        this.channelID = id;
    }

    public void setPreRollEnable(boolean isPreRollEnabled) {
        Constant.getInstance().setPreRollEnabled(isPreRollEnabled);
    }

    public void setAdSkipEnable(boolean isAdSkip, long skipAdInMillis) throws MultiTVException {
        Constant.getInstance().setAdSkipable(isAdSkip);
        if (skipAdInMillis < 5000)
            throw new MultiTVException("Ad skipable time must not be less than 5000 milliseconds");
        else
            this.skipAdInMillis = (int) skipAdInMillis / 1000;
    }

    public void setContentFilePath(String path) {
        this.contentUrl = path;
    }

    public void setContentType(ContentType type) {
        this.contentType = type;
    }

    public void setMultiTvPlayerListner(MultiTvPlayerListner listner) {
        if (listner == null)
            throw new NullPointerException();
        this.interfaceObject = listner;
    }

    public void preparePlayer() throws MultiTVException {
        if (contentType == null || (channelID == null && contentUrl == null))
            throw new MultiTVException("Content type must not be null");

        if (!isRuntimePermissionsCheckedOnce && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            isRuntimePermissionsCheckedOnce = true;
            checkForRuntimePermissions();
            return;
        }
        /*else if(!isReadStatePermissionEnabled || !isWriteSettingsPermissionEnabled) {
            ToastMessage.showToastMsg(context, "Read state and write settings permissions are must for multitv player",
                    Toast.LENGTH_SHORT);
            return;
        }*/

        ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, new DeviceInfo(context).getPrimaryAccountDetail());

        cueSheetInfoList = new ArrayList<>();
        this.adListner = this;
        initViews();

        ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "Keytoken: " + keyToken + ":::::" + adListner);

        commonSDK = new MultiTVCommonSdk(context, keyToken, adListner);

        loadingView.bringToFront();
        loadingView.show();

        PrepareServerPath prepareServerPath = new PrepareServerPath();
        contentCallDecider = prepareServerPath.getContentCallDecider(contentType, channelID);
        final String url = prepareServerPath.getAdNetworkInfoPath(contentCallDecider, channelID, keyToken);

        ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, url);

        /*AsyncTaskCompat.executeParallel(new FetchChannelURL(), url);*/
        fetchChannelUrl(url);
    }

/*    public void setDefaultControls(boolean value) {
        this.needControls = value;
    }*/

    public void setDisplayMode(ScalableView.DisplayMode mode) {
        if (mSurfaceView != null && mSurfaceViewAdd1 != null && mSurfaceViewAdd2 != null) {
            mSurfaceView.setDisplayMode(mode);
            mSurfaceViewAdd1.setDisplayMode(mode);
            mSurfaceViewAdd2.setDisplayMode(mode);
            if (point != null) {
                mSurfaceView.changeVideoSize(point.y, point.x);
                mSurfaceViewAdd1.changeVideoSize(point.y, point.x);
                mSurfaceViewAdd2.changeVideoSize(point.y, point.x);
            }
        }
    }

    public void onConfigurationChanged(Configuration config) {
        if(mediaController != null)
            mediaController.setAspectRatio(AspectRatioUtils.ASPECT_RATIO_STRETCH);
    }

    public void start() {

        if (isPlayerReady) {
            preRollPlayed = false;

            generateToken();

            mDeviceBandwidthSampler.startSampling();

            if (channelInfo != null) {
                AddInfo adInfo = channelInfo.getAdinfo();

                if (adInfo != null) {

                    ArrayList<CampaignInfo> campaignInfoList = adInfo.getCompaignInfoList();

                    if (campaignInfoList != null && campaignInfoList.size() > 0) {
                        for (int j = 0; j < campaignInfoList.size(); j++) {
                            CampaignInfo campaignInfo = campaignInfoList.get(j);
                            String type = campaignInfo.getCompaignType();
                            if (type != null) {
                                String age = "";
                                String gender = "";
                                double lat = 0, lng = 0;
                                int min = 100000000, max = 500000000;
                                String[] userInfoArray = new DeviceInfo(context).getArrayInfo("USER_INFO");
                                if (userInfoArray != null && userInfoArray.length == 2) {
                                    age = userInfoArray[0];
                                    gender = userInfoArray[1];
                                }

                                if (isAccessCoarseLocationPermissionEnabled && isAccessFineLocationPermissionEnabled) {
                                    GPSTracker tracker = new GPSTracker(context);
                                    if (tracker.canGetLocation()) {
                                        lat = tracker.getLatitude();
                                        lng = tracker.getLongitude();
                                    }
                                }

                                Random r = new Random();
                                int cb = r.nextInt((max - min) + 1) + min;
                                if (type.equalsIgnoreCase("Pre")) {
                                    preRollBaseUrl = new CommonUtils().getAdNetwork(campaignInfo
                                            .getCompaignUrl());
                                    if (preRollBaseUrl != null && preRollBaseUrl.contains("secure.adnxs.com")) {
                                        preRollBaseUrl = preRollBaseUrl + "&age=" + age + "&gender=" + gender + "&loc=" + lat + "," + lng + "&cb=" + cb + "&position=above";
                                    }
                                } else if (type.equalsIgnoreCase("Mid")) {
                                    midRollBaseUrl = new CommonUtils().getAdNetwork(campaignInfo
                                            .getCompaignUrl());
                                    if (midRollBaseUrl != null && midRollBaseUrl.contains("secure.adnxs.com")) {
                                        midRollBaseUrl = midRollBaseUrl + "&age=" + age + "&gender=" + gender + "&loc=" + lat + "," + lng + "&cb=" + cb + "&position=above";
                                    }

                                }
                            }
                        }
                    }

                }
            }

            if (commonSDK != null) {
                String channelToken = "";
                if(channelInfo != null && channelInfo.getChannelToken() != null && !channelInfo.getChannelToken().isEmpty())
                    channelToken = channelInfo.getChannelToken();

                commonSDK.initiateProcess(channelID, channelToken, contentCallDecider);
            }

//            preRollBaseUrl = "http://ads.multitvsolution.com/vast.xml?key=a1809aedce19e413076b4de1bca50910&zone=MID_ROLL&vastv=3.0";
            if (preRollBaseUrl != null && preRollBaseUrl.trim().length() > 0 && Constant.getInstance().isPreRollEnabled()) {

                if (preRollBaseUrl != null)
                    AsyncTaskCompat.executeParallel(new Getvast(true, preRollBaseUrl), null);
            } else if (midRollBaseUrl != null && midRollBaseUrl.trim().length() > 0) {
                preRollPlayed = true;
                skipButton.setVisibility(View.GONE);
                skipAdIndicator.setVisibility(View.GONE);
                startPlayerForMidRollSetup();
            } else {
                preRollPlayed = true;
                skipButton.setVisibility(View.GONE);
                skipAdIndicator.setVisibility(View.GONE);
                isCampaignNull = true;
                initilizeMainPlayer(mContentUrl);
            }
            sessionScheduler = execService.scheduleWithFixedDelay(new MaintainAppSession(keyToken, context), 0, 5, TimeUnit.SECONDS);
            execService.scheduleWithFixedDelay(setNetworkStrength, 0, 1,
                    TimeUnit.SECONDS);
        } else {
            Toast.makeText(context, "Please wait, Player is preparing...",
                    Toast.LENGTH_LONG).show();
        }

    }

    private void startPlayerForMidRollSetup() {

        isCallBackreceived = false;
        /* Use when not reading streaming tag*/
        if (contentType == ContentType.LIVE)
            if (Constant.getInstance().isReadStreamTag()) {
                String channelToken = "";
                if(channelInfo != null && channelInfo.getChannelToken() != null && !channelInfo.getChannelToken().isEmpty())
                    channelToken = channelInfo.getChannelToken();

                commonSDK.initiateProcess(channelID, channelToken, contentCallDecider);
            }
            else
                initilizeMainPlayer(mContentUrl);
        else if (contentType == ContentType.VOD)
            initilizeMainPlayer(mContentUrl);
    }

    @Override
    protected void onFinishInflate() {
        LayoutInflater.from(getContext()).inflate(R.layout.player, this);
        super.onFinishInflate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        /*getRootView().findViewById(R.id.logo_layout).bringToFront();*/
        /*canvas.drawBitmap(logo, 0, 0, new Paint());*/
    }

 /*   @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "Key " + keyCode);
*//*        int progress = musicSeekbar.getProgress();
        int progressGesture = musicSeekBarGesture.getProgress();

        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (progress < 15) {
                    musicSeekbar.setProgress(progress + 1);
                    musicSeekBarGesture.setProgress(progressGesture + 1);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (progress > 0) {
                    musicSeekbar.setProgress(progress - 1);
                    musicSeekBarGesture.setProgress(progressGesture - 1);
                }
                return true;
            default:
           *//*
        return false;
//    }
    }*/

    private void initViews() {
        mConnectionClassManager = ConnectionClassManager.getInstance();
        mDeviceBandwidthSampler = DeviceBandwidthSampler.getInstance();
        mListener = new ConnectionChangedListener();
        logo = BitmapFactory
                .decodeResource(getResources(), R.drawable.app_icon);
        skipButton = (ImageView) this.findViewById(R.id.skip_ad);

        /*unlockButton = (ImageView) this.findViewById(R.id.unlock);*/
        logoLayout = (RelativeLayout) this.findViewById(R.id.logo_layout);

        watermarkLogo = (ImageView) this.findViewById(R.id.logo);
        fetchGoogleAdvertisingID();

        skipAdIndicator = (TextView) this.findViewById(R.id.skip_ad_text_indicator);
        surfaceText = (TextView) this.findViewById(R.id.titleText);

        loadingView = (LoadingView) this.findViewById(R.id.play_video_loading);
        View root = findViewById(R.id.root);
        frameLayoutPlayer = (AspectRatioFrameLayout) this
                .findViewById(R.id.video_frame);

        frameLayoutPlayerAdd1 = (AspectRatioFrameLayout) this
                .findViewById(R.id.video_frame_add1);

        frameLayoutPlayerAdd2 = (AspectRatioFrameLayout) this
                .findViewById(R.id.video_frame_add2);


        stitchingStatus = (TextView) this.findViewById(R.id.stitching_status);

//        guestureParentLayout = (FrameLayout) this.findViewById(R.id.parent_guesture_layout);

        createAndPrepareSurface();

//        brightnessSeekBarGesture = (VerticalSeekBar) this
//                .findViewById(R.id.palyer_seek_brightness_gesture);
//        musicSeekBarGesture = (VerticalSeekBar) this
//                .findViewById(R.id.palyer_seek_volume_gesture);

//        FensterGestureControllerView mGestureView = (FensterGestureControllerView) this
//                .findViewById(R.id.play_gesture_controller);
//
//        FensterGestureControllerView mGestureView2 = (FensterGestureControllerView) this
//                .findViewById(R.id.play_gesture_controller2);

        root.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastMessage.showToastMsg(context, "surface clicked", Toast.LENGTH_LONG);
            }
        });

        root.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                ToastMessage.showToastMsg(context, "surface touched", Toast.LENGTH_LONG);
                if (currentPlaying != null && currentPlaying.equalsIgnoreCase("M1")) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        toggleControlsVisibility();
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        view.performClick();
                    }
                } else {
                    CommonUtils.performCTR(context, vastParser);
                }
                return true;
            }
        });
        root.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                ToastMessage.showToastMsg(context, "surface setOnKeyListener", Toast.LENGTH_LONG);
                if (currentPlaying != null && currentPlaying.equalsIgnoreCase("M1"))
                    if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE
                            || keyCode == KeyEvent.KEYCODE_MENU) {
                        return false;
                    }
                return mediaController.dispatchKeyEvent(event);
            }
        });

        mediaController = new KeyCompatibleMediaController(context, this);
        mediaController.setAnchorView((FrameLayout) root);
        mediaController.setResolutionViewListener(this);

   /*     resolutionSelector = (ImageView) this
                .findViewById(R.id.resolution);*/
//        final Bitmap iconBrightBM = BitmapFactory.decodeResource(
//                getResources(), R.drawable.ic_action_bulb);
//        imageBrightness.setImageBitmap(new CommonUtils().getColoredImage(
//                iconBrightBM, "#2994D2"));
//        imageVolume.setImageResource(R.drawable.ic_action_music_2);

        if (execService != null && !execService.isTerminated())
            execService.shutdownNow();
//        execService = Executors.newSingleThreadScheduledExecutor(5);
//        execService = Executors.newScheduledThreadPool(5);

        execService = new PausableExecutor(20);
        execService.setRemoveOnCancelPolicy(true);

        skipButton.setEnabled(false);

        skipButton.setOnClickListener(this);
        frameLayoutPlayerAdd1.setOnClickListener(this);
        frameLayoutPlayerAdd2.setOnClickListener(this);

/*        brightnesSeekBar.setMax(255);
        brightnesSeekBar.setProgress(50);
        musicSeekbar.setProgress(5);
        musicSeekbar.setMax(audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC));*/

        /**
         * Gor gusture controls
         */

  /*   musicSeekBarGesture.setOnSeekBarChangeListener(musicSeekGestureListner);
        brightnessSeekBarGesture
                .setOnSeekBarChangeListener(brightSeekGestureListner);

        audioManager = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 5, 0);


     brightnessSeekBarGesture.setMax(255);
        brightnessSeekBarGesture.setProgress(50);
        musicSeekBarGesture.setProgress(5);
        musicSeekBarGesture.setMax(audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC));

        android.provider.Settings.System.putInt(context.getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS, 50);
*/
        /*
         * mSurfaceHolder = mSurfaceView.getHolder();
		 *
		 * mSurfaceHolderAdd1 = mSurfaceViewAdd1.getHolder();
		 *
		 * mSurfaceHolderAdd2 = mSurfaceViewAdd2.getHolder();
		 */

   /*     if (!needControls) {
            controlLayout.setVisibility(View.GONE);
            initGestureControl(mGestureView, mGestureView2);
            mGestureView.setVisibility(View.VISIBLE);
            mGestureView2.setVisibility(View.VISIBLE);
            musicSeekBarGesture.setVisibility(View.GONE);
            brightnessSeekBarGesture.setVisibility(View.GONE);
        } else {
            controlLayout.setVisibility(View.VISIBLE);
            controlLayout.bringToFront();
            initPlayPauseForDefault();
            mGestureView.setVisibility(View.GONE);
            mGestureView2.setVisibility(View.GONE);
            musicSeekBarGesture.setVisibility(View.GONE);
            brightnessSeekBarGesture.setVisibility(View.GONE);
        }
*/
        listener = new VASTXmlListener() {

            @Override
            public void onVASTWrapperFound(String url) {
                try {
//                    String encodedUrl = URLEncoder.encode(url, "UTF-8");
                    AsyncTaskCompat.executeParallel(new Getvast(url), null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onVASTReady(final VASTXmlParser vast,
                                    final boolean autoStartPlayer) {
                ((Activity) context).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "Vast URL : "
                                + vast.getMediaFileUrl());

                        if (vast != null && vast.getMediaFileUrl() != null
                                && vast.getMediaFileUrl().trim().length() > 0) {
                            try {
                                vastParser = null;
                                if (activeMediaplayer != null
                                        && activeMediaplayer.trim()
                                        .equalsIgnoreCase("M2")) {
                                    if (mMediaPlayerAdd2 != null)
                                        mMediaPlayerAdd2.release();
                                    mMediaPlayerAdd2 = null;
                                    mMediaPlayerAdd2 = new MediaPlayer(
                                            new CommonUtils()
                                                    .getRendererBuilder(
                                                            context,
                                                            vast.getMediaFileUrl()
                                                                    .trim(),
                                                            false));
                                    mMediaPlayerAdd2
                                            .addListener(stateChangeCallback3);
//                                    mMediaPlayerAdd2
//                                            .setMediaCodecVideoEventListner(mediaCodecViedoRenderListner2);
                                    try {
                                        mMediaPlayerAdd2
                                                .setSurface(mSurfaceViewAdd2
                                                        .getHolder()
                                                        .getSurface());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    mMediaPlayerAdd2.prepare();
                                    autoStart = autoStartPlayer;
                                } else {
                                    if (mMediaPlayerAdd1 != null)
                                        mMediaPlayerAdd1.release();
                                    mMediaPlayerAdd1 = null;
                                    mMediaPlayerAdd1 = new MediaPlayer(
                                            new CommonUtils()
                                                    .getRendererBuilder(
                                                            context,
                                                            vast.getMediaFileUrl()
                                                                    .trim(),
                                                            false));
                                    mMediaPlayerAdd1
                                            .addListener(stateChangeCallback2);
//                                    mMediaPlayerAdd1
//                                            .setMediaCodecVideoEventListner(mediaCodecViedoRenderListner1);
                                    try {
                                        mMediaPlayerAdd1
                                                .setSurface(mSurfaceViewAdd1
                                                        .getHolder()
                                                        .getSurface());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    mMediaPlayerAdd1.prepare();
                                    autoStart = autoStartPlayer;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
//                            String url;
//                            if (preRollBaseUrl != null && preRollBaseUrl.trim().length() > 0 && !preRollPlayed)
//                                url = AD_MEDIATION + keyToken + "/content_type/" + 1 + "/campaign_type/Pre/cid/" + channelInfo.getChannelID();
//                            else
//                                url = AD_MEDIATION + keyToken + "/content_type/" + 2 + "/campaign_type/Mid/cid/" + channelInfo.getChannelID();

                            isMediationActive = true;
                            ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "Calling mediation");
                            AsyncTaskCompat.executeParallel(new Getvast(Constant.getInstance().getAdMediation()), null);
                        }
                    }
                });

            }
        };


/*        imageBrightness.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                imageVolume.setImageResource(R.drawable.ic_action_music_2);
                Bitmap iconBrightBM = BitmapFactory.decodeResource(
                        getResources(), R.drawable.ic_action_bulb);
                if (brightnesSeekBar.isShown()) {
                    musicSeekbar.setVisibility(View.GONE);
                    brightnesSeekBar.setVisibility(View.GONE);
                    if (contentType == ContentType.VOD)
                        progressSeekBar.setVisibility(View.VISIBLE);
                    imageBrightness.setImageResource(R.drawable.ic_action_bulb);
                } else {
                    musicSeekbar.setVisibility(View.GONE);
                    brightnesSeekBar.setVisibility(View.VISIBLE);
                    progressSeekBar.setVisibility(View.GONE);
                    imageBrightness.setImageBitmap(new CommonUtils()
                            .getColoredImage(iconBrightBM, "#2994D2"));
                }
            }
        });
        imageVolume.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                imageBrightness.setImageResource(R.drawable.ic_action_bulb);
                Bitmap iconVolBM = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_action_music_2);
                if (musicSeekbar.isShown()) {
                    musicSeekbar.setVisibility(View.GONE);
                    brightnesSeekBar.setVisibility(View.GONE);
                    if (contentType == ContentType.VOD)
                        progressSeekBar.setVisibility(View.VISIBLE);
                    imageVolume.setImageResource(R.drawable.ic_action_music_2);
                } else {
                    musicSeekbar.setVisibility(View.VISIBLE);
                    brightnesSeekBar.setVisibility(View.GONE);
                    progressSeekBar.setVisibility(View.GONE);
                    imageVolume.setImageBitmap(new CommonUtils()
                            .getColoredImage(iconVolBM, "#2994D2"));
                }

            }
        });
    */
    }

    private boolean checkForRuntimePermissions() {
        PermissionChecker.setPermissionCheckerInterface(this);

        ArrayList<String> permissionsArrayList = new ArrayList<>();
        permissionsArrayList.add(PermissionId.PERMISSION_READ_PHONE_STATE);

        //Check only only phone state permission as this is the mandatory one
        if (!isCheckOnlyPhoneStatePermission) {
            permissionsArrayList.add(PermissionId.PERMISSION_ACCESS_COARSE_LOCATION);
            permissionsArrayList.add(PermissionId.PERMISSION_ACCESS_FINE_LOCATION);
        } else
            isCheckOnlyPhoneStatePermission = false;

        permissionsPermanentlyDenied = PermissionChecker.checkMultiplePermissions
                ((Activity) context, permissionsArrayList, PermissionId.PERMISSION_MULTIPLE_REQUEST_CODE);

       /* if(PermissionChecker.checkSinglePermission((Activity) context,
                PermissionId.PERMISSION_READ_PHONE_STATE,
                PermissionId.PERMISSION_READ_PHONE_STATE_REQUEST_CODE)) {
            isReadStatePermissionEnabled = true;
        }

        if(PermissionChecker.checkSinglePermission((Activity) context,
                PermissionId.PERMISSION_ACCESS_COARSE_LOCATION,
                PermissionId.PERMISSION_ACCESS_COARSE_LOCATION_REQUEST_CODE)) {
            isAccessCoarseLocationPermissionEnabled =  true;
        }

        if(PermissionChecker.checkSinglePermission((Activity) context,
                PermissionId.PERMISSION_ACCESS_FINE_LOCATION,
                PermissionId.PERMISSION_ACCESS_FINE_LOCATION_REQUEST_CODE)) {
            isAccessFineLocationPermissionEnabled =  true;
        }*/

        //Checking for WRITE_SETTINGS permission
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(context)) {
                isWriteSettingsPermissionEnabled = true;
                *//*if(PermissionChecker.checkSinglePermission((Activity) context,
                        PermissionId.PERMISSION_WRITE_SETTINGS,
                        PermissionId.PERMISSION_WRITE_SETTINGS_REQUEST_CODE)) {
                    isWriteSettingsPermissionEnabled =  true;
                }*//*
            }
            else {
                isWriteSettingsPermissionChangeAsked = true;

                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }*/


       /* if(isReadStatePermissionEnabled && isWriteSettingsPermissionEnabled && isAccessCoarseLocationPermissionEnabled
                && isAccessFineLocationPermissionEnabled)
            return true;
        else*/
        return false;
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        if (id == R.id.skip_ad) {
            handlePlayerEndEvent();
        } else if (id == R.id.video_frame_add1) {
            if (vastParser != null && currentPlaying != null &&
                    !currentPlaying.equalsIgnoreCase("M1")) {
                CommonUtils.performCTR(context, vastParser);
                postAdRequests(false);
            }
        } else if (id == R.id.video_frame_add2) {
            if (vastParser != null && currentPlaying != null &&
                    !currentPlaying.equalsIgnoreCase("M1")) {
                CommonUtils.performCTR(context, vastParser);
                postAdRequests(false);
            }
        }
    }

    private void createAndPrepareSurface() {

        mSurfaceView = new ScalableView(context);

        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mSurfaceView.setLayoutParams(params);

        frameLayoutPlayer.addView(mSurfaceView);

        mSurfaceViewAdd1 = new ScalableView(context);
        android.widget.FrameLayout.LayoutParams paramsAdd1 = new android.widget.FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mSurfaceViewAdd1.setLayoutParams(paramsAdd1);
        frameLayoutPlayerAdd1.addView(mSurfaceViewAdd1);

        mSurfaceViewAdd2 = new ScalableView(context);
        android.widget.FrameLayout.LayoutParams paramsAdd2 = new android.widget.FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mSurfaceViewAdd2.setLayoutParams(paramsAdd2);
        frameLayoutPlayerAdd2.addView(mSurfaceViewAdd2);

    }


    Listener stateChangeCallback1 = new Listener() {

        int bufferingCounter = 0;

        @Override
        public void onVideoSizeChanged(int width, int height,
                                       int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            mediaController.setOriginalVideoHeightAndWidth(width, height);
           /* surfaceView.setVideoWidthHeightRatio(
                    height == 0 ? 1 : (width * pixelWidthAspectRatio) / height);*/
        }

        @Override
        public void onStateChanged(boolean playWhenReady, int playbackState) {
            String text = "Main player";
            switch (playbackState) {
                case ExoPlayer.STATE_BUFFERING:
                    text += "buffering";

//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                bufferingCounter++;
//                                if (bufferingCounter == 6) {
////                                    initilizeMainPlayer(mContentUrl);
//                                    bufferingCounter = 0;
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//
//                        }
//                    }).start();


                    break;
                case ExoPlayer.STATE_ENDED:
                    text += "ended";

                    if (contentType == ContentType.VOD) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (contentType == ContentType.VOD && isPlayerReady) {
                                                if (vodQuePointScheduler != null) {
                                                    ToastMessage.showToastMsg(context, "Finishing service", Toast.LENGTH_SHORT);
                                                    vodQuePointScheduler.cancel(true);
                                                }

                                                release();
                                                LinearLayout circularProgressLayout = (LinearLayout) getRootView().
                                                        findViewById(R.id.circular_progress_layout);
                                                circularProgressLayout.setVisibility(VISIBLE);
                                                circularProgressLayout.bringToFront();

                                                final FabButton circularProgressRing = (FabButton)circularProgressLayout.findViewById(R.id.circular_progress_ring);
                                                circularProgressRing.showProgress(true);
                                                circularProgressRing.setProgress(0);

                                                final int totalDuration = 30000, tickDuration = 1000;
                                                countDownTimer = new CountDownTimerWithPause(totalDuration, tickDuration / 10, true) {
                                                    public void onTick(long millisUntilFinished) {
                                                        float progress = (float) millisUntilFinished / totalDuration;
                                                        progress = progress * 100;
                                                        progress = 100 - progress;
                                                        circularProgressRing.setProgress(progress);
                                                    }

                                                    public void onFinish() {
                                                        skipableAdCounter = 6;
                                                        try {
                                                            preparePlayer();
                                                        }
                                                        catch (MultiTVException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }.create();
                                            }
                                        }
                                    });
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                    break;
                case ExoPlayer.STATE_IDLE:
                    text += "idle";
                    break;
                case ExoPlayer.STATE_PREPARING:
                    text += "preparing";
                    /*if(mMediaPlayer != null) {
                        Log.e("Naseeb", "" + mMediaPlayer.getTrackCount(MediaPlayer.TYPE_TEXT));
                        Log.e("Naseeb", "" + mMediaPlayer.getSelectedTrack(MediaPlayer.TYPE_TEXT));
                        mMediaPlayer.setSelectedTrack(MediaPlayer.TYPE_TEXT, MediaPlayer.TRACK_DEFAULT);
                        Log.e("Naseeb", "" + mMediaPlayer.getSelectedTrack(MediaPlayer.TYPE_TEXT));
                    }*/
                    break;
                case ExoPlayer.STATE_READY:
                    text += "ready";
                    if (loadingView != null)
                        loadingView.hide();

                   /* if(activeMediaplayer.equals("M1")) {
                        if(mMediaPlayer != null) {
                            Log.e("Naseeb", "" + mMediaPlayer.getTrackCount(MediaPlayer.TYPE_TEXT));
                            Log.e("Naseeb", "" + mMediaPlayer.getSelectedTrack(MediaPlayer.TYPE_TEXT));
                            mMediaPlayer.setSelectedTrack(MediaPlayer.TYPE_TEXT, MediaPlayer.TRACK_DEFAULT);
                            Log.e("Naseeb", "" + mMediaPlayer.getSelectedTrack(MediaPlayer.TYPE_TEXT));
                        }
                    }*/

                    ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "Tracking Buffering Issue:: Main Player ready " + " isCallBackreceived = "
                            + isCallBackreceived + " isResolutionSelectionEnable = " + isResolutionSelectionEnable);
                    if ((!isCallBackreceived || isResolutionSelectionEnable)) {
                        isResolutionSelectionEnable = false;
                        activeMediaplayer = "M1";
                        currentPlaying = "M1";
                        mMediaPlayer.setPlayWhenReady(true);
                      /* Use when reading streaming tag*/
// if (readStreamTag && !isCallBackreceived) {
                        if ((contentType == ContentType.VOD || Constant.getInstance().isReadStreamTag()) && !isCallBackreceived) {
                            Log.d(TAG, "Initilizing middle layer");
                            isCallBackreceived = true;
                            String channelToken = "";
                            if(channelInfo != null && channelInfo.getChannelToken() != null && !channelInfo.getChannelToken().isEmpty())
                                channelToken = channelInfo.getChannelToken();

                            commonSDK.initiateProcess(channelID, channelToken, contentCallDecider);
                        }
                        isCallBackreceived = true;

                        mSurfaceViewAdd1.setVisibility(View.GONE);
                        mSurfaceViewAdd2.setVisibility(View.GONE);
                        frameLayoutPlayerAdd1.setVisibility(GONE);
                        frameLayoutPlayerAdd2.setVisibility(GONE);
                        frameLayoutPlayer.setVisibility(VISIBLE);
                        mSurfaceView.setVisibility(View.VISIBLE);
                        frameLayoutPlayer.bringToFront();
                        mSurfaceView.bringToFront();
                        logoLayout.bringToFront();
                        fetchMidRollVast();
                    } else if (isCampaignNull) {
                        activeMediaplayer = "M1";
                        currentPlaying = "M1";
                        mMediaPlayer.setPlayWhenReady(true);
                        isCallBackreceived = true;


                        mSurfaceView.bringToFront();
                        logoLayout.bringToFront();
                        logoLayout.bringToFront();
                        mSurfaceView.requestFocus();
                    }
                   /* if (contentType == ContentType.VOD && !isPlayerSeekd) {
                        if (seekPlayerTo > 1) {
                            if (loadingView != null) {
                                loadingView.show();
                                loadingView.bringToFront();
                            }
                            mMediaPlayer.seekTo((seekPlayerTo + 2) * 1000);
                            isPlayerSeekd = true;
                        }
                    }*/
                    break;
                default:
                    text += "unknown";
                    break;
            }
            ToastMessage.showToastMsg(context, text, Toast.LENGTH_SHORT);
            ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, text);
        }

        @Override
        public void onError(Exception e) {
            e.printStackTrace();
            ToastMessage.showToastMsg(context, "Main Player::: " + e.getMessage(), Toast.LENGTH_SHORT);
            ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "Main Player::: " + e.getMessage());
            if (currentStatusTag != null
                    && currentStatusTag.trim().equalsIgnoreCase("AD")) {
                frameLayoutPlayerAdd1.setVisibility(View.GONE);
                frameLayoutPlayer.setVisibility(View.GONE);
                mSurfaceViewAdd1.setVisibility(View.GONE);
                mSurfaceView.setVisibility(View.GONE);
                frameLayoutPlayerAdd2.setVisibility(View.VISIBLE);
                mSurfaceViewAdd2.setVisibility(View.VISIBLE);
                frameLayoutPlayerAdd2.bringToFront();
                mSurfaceViewAdd2.bringToFront();
                logoLayout.bringToFront();
                // surfaceText.setText("Surface Ad 2");
                startADMediaPlayer();
            } else {
                isResolutionSelectionEnable = true;
                generateToken();
                initilizeMainPlayer(mContentUrl);
            }
        }
    };


    Listener stateChangeCallback2 = new Listener() {

        @Override
        public void onVideoSizeChanged(int width, int height,
                                       int unappliedRotationDegrees, float pixelWidthHeightRatio) {

        }

        @Override
        public void onStateChanged(boolean playWhenReady, int playbackState) {

            String text = "Ad player 1 ";
            switch (playbackState) {
                case ExoPlayer.STATE_BUFFERING:
                    text += "buffering";
                    break;
                case ExoPlayer.STATE_ENDED:
                    text += "ended";

                    handlePlayerEndEvent();

                    break;
                case ExoPlayer.STATE_IDLE:
                    text += "idle";
                    break;
                case ExoPlayer.STATE_PREPARING:
                    text += "preparing";
                    break;
                case ExoPlayer.STATE_READY:
                    text += "ready";

                    if (loadingView != null)
                        loadingView.hide();

                    if (skipableAdCounter > -1)
                        if (Constant.getInstance().isAdSkipable() && !preRollPlayed) {
                            skipAdIndicator.setVisibility(View.VISIBLE);
                            skipAdIndicator.bringToFront();
                        } else {
                            skipAdIndicator.setVisibility(View.GONE);
                        }
                    try {
                        isAdPlayerReady = true;
                        activeMediaplayer = "M2";
                        if (autoStart || !isCallBackreceived) {
                            autoStart = false;
                            isCallBackreceived = true;
                            mSurfaceView.setVisibility(View.GONE);
                            mSurfaceViewAdd2.setVisibility(View.GONE);
                            frameLayoutPlayer.setVisibility(View.GONE);
                            frameLayoutPlayerAdd2.setVisibility(View.GONE);
                            frameLayoutPlayerAdd1.setVisibility(View.VISIBLE);
                            mSurfaceViewAdd1.setVisibility(View.VISIBLE);
                            frameLayoutPlayerAdd1.bringToFront();
                            mSurfaceViewAdd1.bringToFront();
                            logoLayout.bringToFront();
                            // surfaceText.setText("Surface Ad 1");
                            startADMediaPlayer();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
                default:
                    text += "unknown";
                    break;
            }
            ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, text);
            ToastMessage.showToastMsg(context, text, Toast.LENGTH_SHORT);
        }

        @Override
        public void onError(Exception e) {
            ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "Ad player 1 " + e.getMessage());
            ToastMessage.showToastMsg(context, "Ad player 1 :: " + e.getMessage(), Toast.LENGTH_SHORT);
        }
    };
    Listener stateChangeCallback3 = new Listener() {

        @Override
        public void onVideoSizeChanged(int width, int height,
                                       int unappliedRotationDegrees, float pixelWidthHeightRatio) {

        }

        @Override
        public void onStateChanged(boolean playWhenReady, int playbackState) {
            String text = "Ad player 2 ";
            switch (playbackState) {
                case ExoPlayer.STATE_BUFFERING:
                    text += "buffering";
                    break;
                case ExoPlayer.STATE_ENDED:
                    text += "ended";

                    cueSheetInfo.setEndTime(CommonUtils.convertDateFormat(System
                            .currentTimeMillis()));
                    cueSheetInfoList.add(cueSheetInfo);
                    cueSheetInfo = null;
                    try {
                        if (mMediaPlayerAdd2 != null)
                            mMediaPlayerAdd2.release();
                        if (currentStatusTag != null
                                && currentStatusTag.trim().equalsIgnoreCase("AD")) {
                            mSurfaceView.setVisibility(View.GONE);
                            frameLayoutPlayer.setVisibility(View.GONE);
                            mSurfaceViewAdd2.setVisibility(View.GONE);
                            frameLayoutPlayerAdd2.setVisibility(View.GONE);
                            frameLayoutPlayerAdd1.setVisibility(View.VISIBLE);
                            mSurfaceViewAdd1.setVisibility(View.VISIBLE);
                            mSurfaceViewAdd1.setVisibility(View.VISIBLE);
                            frameLayoutPlayerAdd1.bringToFront();
                            mSurfaceViewAdd1.bringToFront();
                            logoLayout.bringToFront();
                            // surfaceText.setText("Surface Ad 1");
                            startADMediaPlayer();
                        } else {
                            mSurfaceViewAdd2.setVisibility(View.GONE);
                            mSurfaceViewAdd1.setVisibility(View.GONE);
                            frameLayoutPlayerAdd1.setVisibility(View.GONE);
                            frameLayoutPlayerAdd2.setVisibility(View.GONE);
                            frameLayoutPlayer.setVisibility(View.VISIBLE);
                            mSurfaceView.setVisibility(View.VISIBLE);
                            frameLayoutPlayer.bringToFront();
                            mSurfaceView.bringToFront();
                            logoLayout.bringToFront();
                            activeMediaplayer = "M1";
                            currentPlaying = "M1";
                            mMediaPlayer.setPlayWhenReady(true);
                            mediaController.updateSystemConfig();
                            cueSheetInfoChannel = new CueSheet();
                            cueSheetInfoChannel.setType("MP");
                            cueSheetInfoChannel.setStartTime(CommonUtils
                                    .convertDateFormat(System.currentTimeMillis()));
                            cueSheetInfoChannel.setUrl(mContentUrl);
                            fetchMidRollVast();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case ExoPlayer.STATE_IDLE:
                    text += "idle";
                    break;
                case ExoPlayer.STATE_PREPARING:
                    text += "preparing";
                    break;
                case ExoPlayer.STATE_READY:
                    activeMediaplayer = "M3";
                    text += "ready";
                    isAdPlayerReady = true;
                    break;
                default:
                    text += "unknown";
                    break;
            }

            ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, text);
            ToastMessage.showToastMsg(context, text, Toast.LENGTH_SHORT);
        }

        @Override
        public void onError(Exception e) {
            ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "Ad player 2 " + e.getMessage());
            ToastMessage.showToastMsg(context, "Ad player 2  :: " + e.getMessage(),
                    Toast.LENGTH_LONG);
        }
    };

//    private void handleAdSkip() {
//        handlePlayerEndEvent();
//    }

  /*  OnSeekBarChangeListener musicSeekGestureListner = new OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            audioManager
                    .setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
        }
    };
    OnSeekBarChangeListener brightSeekGestureListner = new OnSeekBarChangeListener() {
        int mprogress = 0;

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            android.provider.Settings.System.putInt(
                    context.getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS,
                    progress);
        }
    };*/

    private void handlePlayerEndEvent() {
        preRollPlayed = true;

        ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "Player2 ended");
        skipButton.setVisibility(View.GONE);
        skipAdIndicator.setVisibility(View.GONE);
        if (cueSheetInfo != null) {
            cueSheetInfo.setEndTime(CommonUtils
                    .convertDateFormat(System.currentTimeMillis()));
            cueSheetInfoList.add(cueSheetInfo);
        }
        cueSheetInfo = null;
        try {
            if (mMediaPlayerAdd1 != null)
                mMediaPlayerAdd1.release();

            if (contentType == ContentType.LIVE) {

                ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "For live switching");
                if (currentStatusTag != null
                        && currentStatusTag.trim().equalsIgnoreCase("AD")) {
                    frameLayoutPlayerAdd1.setVisibility(View.GONE);
                    frameLayoutPlayer.setVisibility(View.GONE);
                    mSurfaceViewAdd1.setVisibility(View.GONE);
                    mSurfaceView.setVisibility(View.GONE);
                    frameLayoutPlayerAdd2.setVisibility(View.VISIBLE);
                    mSurfaceViewAdd2.setVisibility(View.VISIBLE);
                    frameLayoutPlayerAdd2.bringToFront();
                    mSurfaceViewAdd2.bringToFront();
                    logoLayout.bringToFront();
                    // surfaceText.setText("Surface Ad 2");
                    startADMediaPlayer();
                } else {
                    frameLayoutPlayerAdd1.setVisibility(View.GONE);
                    frameLayoutPlayerAdd2.setVisibility(View.GONE);
                    mSurfaceViewAdd2.setVisibility(View.GONE);
                    mSurfaceViewAdd1.setVisibility(View.GONE);
                    frameLayoutPlayer.setVisibility(View.VISIBLE);
                    mSurfaceView.setVisibility(View.VISIBLE);
                    frameLayoutPlayer.bringToFront();
                    mSurfaceView.bringToFront();
                    logoLayout.bringToFront();
                    activeMediaplayer = "M1";
                    currentPlaying = "M1";
                    mMediaPlayer.setPlayWhenReady(true);
                    mediaController.updateSystemConfig();
                    cueSheetInfoChannel = new CueSheet();
                    cueSheetInfoChannel.setType("MP");
                    cueSheetInfoChannel.setStartTime(CommonUtils
                            .convertDateFormat(System.currentTimeMillis()));
                    cueSheetInfoChannel.setUrl(mContentUrl);
                    fetchMidRollVast();
                }
            } else if (contentType == ContentType.VOD) {
                //Todo FOR VOD (Switch to main player)

                ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "For vod switching");
                frameLayoutPlayerAdd1.setVisibility(View.GONE);
                frameLayoutPlayerAdd2.setVisibility(View.GONE);
                mSurfaceViewAdd2.setVisibility(View.GONE);
                mSurfaceViewAdd1.setVisibility(View.GONE);
                frameLayoutPlayer.setVisibility(View.VISIBLE);
                mSurfaceView.setVisibility(View.VISIBLE);
                frameLayoutPlayer.bringToFront();
                mSurfaceView.bringToFront();
                logoLayout.bringToFront();
                activeMediaplayer = "M1";
                currentPlaying = "M1";
                isPlayerSeekd = false;

                mMediaPlayer.setPlayWhenReady(true);
                mediaController.updateSystemConfig();
                 /*           cueSheetInfoChannel = new CueSheet();
                            cueSheetInfoChannel.setType("MP");
                            cueSheetInfoChannel.setStartTime(CommonUtils
                                    .convertDateFormat(System.currentTimeMillis()));
                            cueSheetInfoChannel.setUrl(mContentUrl);*/

                fetchMidRollVast();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void startADMediaPlayer() {
        try {
            if (adTimer != null) {
                adTimer.cancel();
                adTimer = null;
            }

            if (activeMediaplayer != null
                    && activeMediaplayer.equalsIgnoreCase("M2")) {
                duration = mMediaPlayerAdd1.getDuration() / 1000;
                mMediaPlayerAdd1.setPlayWhenReady(true);
                ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "Tracking Buffering Issue::  startADMediaPlayer Player2");
                currentPlaying = "M2";
            } else if (activeMediaplayer != null
                    && activeMediaplayer.equalsIgnoreCase("M3")) {
                duration = mMediaPlayerAdd2.getDuration() / 1000;
                mMediaPlayerAdd2.setPlayWhenReady(true);
                ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "Tracking Buffering Issue::  startADMediaPlayer Player3");
                currentPlaying = "M3";
            }

            cueSheetInfo = new CueSheet();
            cueSheetInfo.setType("AD");
            cueSheetInfo.setStartTime(CommonUtils.convertDateFormat(System
                    .currentTimeMillis()));
            if (vastParser != null)
                cueSheetInfo.setUrl(vastParser.getMediaFileUrl());
            adTicker = 0;
            adTimer = new Timer();
            if (vastParser != null && vastParser.getTrackings() != null
                    && vastParser.getTrackings().size() > 0) {
                adTimer.scheduleAtFixedRate(new TimerTask() {

                    @Override
                    public void run() {

                        try {
                            if (adTicker == duration / 4) {
                                adTicker += 1;
                                if (vastParser != null) {
                                    hitTracking(vastParser.getTrackings()
                                            .get(Tracking.EVENT_FIRSTQ)
                                            .getUrl());
                                }
                            } else if (adTicker == duration / 2) {
                                adTicker += 1;
                                if (vastParser != null) {
                                    hitTracking(vastParser.getTrackings()
                                            .get(Tracking.EVENT_MID).getUrl());
                                }
                            } else if (adTicker == (3 * duration) / 4) {
                                adTicker += 1;
                                if (vastParser != null) {
                                    hitTracking(vastParser.getTrackings()
                                            .get(Tracking.EVENT_THIRDQ)
                                            .getUrl());
                                }
                            } else {
                                adTicker += 1;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 0, 1000);

                mDeviceBandwidthSampler.startSampling();

                if (vastParser != null)
                    hitTracking(vastParser.getTrackings()
                            .get(Tracking.EVENT_START).getUrl());
            }

            if(checkerTimer != null) {
                checkerTimer.cancel();
                checkerTimer = null;
            }

            checkerTimer = new Timer();
            TimerTask task = new TimerTask() {

                @Override
                public void run() {
                    ((Activity) context).runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
//                            Log.d(TAG, "Fetching again vast " + midRollBaseUrl);
                            if (contentType != null && contentType == ContentType.LIVE) {
                                fetchMidRollVast();
                                if (currentPlaying != null && !currentPlaying.equalsIgnoreCase("M1"))
                                    initilizeMainPlayer(mContentUrl);
                            } else if (contentType != null && contentType == ContentType.VOD) {
                                if (mMediaPlayer == null) {
//                                    Log.d(TAG, "Initiliazing main player");
                                    initilizeMainPlayer(mContentUrl);
                                }
                            }
                        }
                    });
                }

            };

            checkerTimer.schedule(task, 500);
            if (vastParser != null) {
                String url;
                for (int i = 0; i < vastParser.getImpressionTrackerUrl().size(); i++) {
                    url = vastParser.getImpressionTrackerUrl().get(i);
                    if (url != null && url.trim().length() > 0) {
                        hitTracking(url);
                    }
                }
                postAdRequests(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initilizeMainPlayer(String url) {
        ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "initilizeMainPlayer");

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "Content url is " + url);
        if (url != null && url.contains(".mp4")) {
            mMediaPlayer = new MediaPlayer(new CommonUtils().getRendererBuilder(
                    context, url, false));
        } else {
            mMediaPlayer = new MediaPlayer(new CommonUtils().getRendererBuilder(
                    context, url, true));
        }
        mMediaPlayer.addListener(stateChangeCallback1);

        if (Constant.getInstance().isReadStreamTag() && contentCallDecider == 1)
            mMediaPlayer.setMetadataListener(this);

        if (contentType == ContentType.VOD) {
            if (channelInfo != null) {
                AddInfo adInfo = channelInfo.getAdinfo();
                if (adInfo != null) {
                    ArrayList<CampaignInfo> campaignList = adInfo.getCompaignInfoList();
                    if (campaignList != null && campaignList.size() > 0) {
                        CampaignInfo campaignInfo;
                        for (int i = 0; i < campaignList.size(); i++) {
                            campaignInfo = campaignList.get(i);
                            if (campaignInfo != null && campaignInfo.getCompaignType().equalsIgnoreCase("Mid")) {
                                vodQuePointList = campaignInfo.getQuePoints();
                                if (vodQuePointList != null && vodQuePointList.size() > 0) {
                                    Collections.sort(vodQuePointList);
                                    mediaController.setVodQuePointList(vodQuePointList);
                                    vodQuePointScheduler = execService.scheduleWithFixedDelay(detectAndPlayAd, 0, 1,
                                            TimeUnit.SECONDS);
                                }
                            }
                        }
                    }
                }
            }
        }
        try {
            ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "Seek to " + seekPlayerTo);
            //&& !isPlayerSeekd          Commented from below condition
            if (contentType == ContentType.VOD) {
                if (seekPlayerTo > 1) {
//                    if (loadingView != null) {
//                        loadingView.show();
//                        loadingView.bringToFront();
//                    }
                    mMediaPlayer.seekTo((seekPlayerTo + 2) * 1000);
//                    seekPlayerTo = 0;
                    isPlayerSeekd = true;
                }
            }

            if(isResumeFromPreviousPosition && mMediaPlayer.getPlayerControl().canSeekForward()) {
                isResumeFromPreviousPosition = false;
                mMediaPlayer.seekTo(millisecondsForResume);
            }

            mMediaPlayer.setSurface(mSurfaceView.getHolder().getSurface());
            mediaController.setMediaPlayer(mMediaPlayer.getPlayerControl());
            mediaController.setEnabled(true);
            mMediaPlayer.prepare();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   /* private void initGestureControl(FensterGestureControllerView view1,
                                    FensterGestureControllerView view2) {

        view1.setFensterEventsListener(new FensterEventsListener() {

            @Override
            public void onTap() {
                playPauseInner();
            }

            @Override
            public void onHorizontalScroll(MotionEvent event, float delta) {
            }

            @Override
            public void onVerticalScroll(MotionEvent event, float delta) {
                if (!needControls) {
                    if (brightnessSeekBarGesture != null
                            && !brightnessSeekBarGesture.isShown()) {
                        brightnessSeekBarGesture.setVisibility(View.VISIBLE);
                    }
                    int currentProgess = brightnesSeekBar.getProgress();
                    int gestureChangeValue = (int) (Math.abs(delta) - Math
                            .abs(gestureStoredValue1));
                    gestureStoredValue1 = delta;
                    int outputProgress = (int) gestureChangeValue / 2;
                    if (delta > 0) {
                        if (currentProgess >= 1) {
                            brightnessSeekBarGesture.setProgress(currentProgess
                                    - outputProgress);
                            android.provider.Settings.System.putInt(
                                    context.getContentResolver(),
                                    android.provider.Settings.System.SCREEN_BRIGHTNESS,
                                    currentProgess - outputProgress);
                        }
                    } else {

                        if (currentProgess < 250) {
                            brightnessSeekBarGesture.setProgress(currentProgess
                                    + outputProgress);
                            android.provider.Settings.System.putInt(
                                    context.getContentResolver(),
                                    android.provider.Settings.System.SCREEN_BRIGHTNESS,
                                    currentProgess + outputProgress);
                        }
                    }
                }
            }

            @Override
            public void onSwipeRight() {
            }

            @Override
            public void onSwipeLeft() {
            }

            @Override
            public void onSwipeBottom() {
                if (!needControls) {
                    gestureStoredValue1 = 100;
                    hideGestureControls(brightnessSeekBarGesture);
                }
            }

            @Override
            public void onSwipeTop() {
                if (!needControls) {
                    gestureStoredValue1 = 100;
                    hideGestureControls(brightnessSeekBarGesture);
                }
            }
        });

        view2.setFensterEventsListener(new FensterEventsListener() {
            @Override
            public void onTap() {
                playPauseInner();
            }

            @Override
            public void onHorizontalScroll(MotionEvent event, float delta) {
            }

            @Override
            public void onVerticalScroll(MotionEvent event, float delta) {
                if (!needControls) {
                    if (musicSeekBarGesture != null
                            && !musicSeekBarGesture.isShown()) {
                        musicSeekBarGesture.setVisibility(View.VISIBLE);
                    }

                    int currentProgess = musicSeekBarGesture.getProgress();
                    int gestureChangeValue = (int) (Math.abs(delta) - Math
                            .abs(gestureStoredValue2));
                    gestureStoredValue2 = delta;
                    int outputProgress = (int) gestureChangeValue / 33;

                    if (delta > 0) {
                        if (currentProgess >= 1) {
                            musicSeekBarGesture.setProgress(currentProgess
                                    - outputProgress);
                            audioManager.setStreamVolume(
                                    AudioManager.STREAM_MUSIC, currentProgess
                                            - outputProgress, 0);
                        }
                    } else {

                        if (currentProgess < 15) {
                            musicSeekBarGesture.setProgress(currentProgess
                                    + outputProgress);
                            audioManager.setStreamVolume(
                                    AudioManager.STREAM_MUSIC, currentProgess
                                            + outputProgress, 0);
                        }
                    }
                }
            }

            @Override
            public void onSwipeRight() {
            }

            @Override
            public void onSwipeLeft() {
            }

            @Override
            public void onSwipeBottom() {
                if (!needControls) {
                    gestureStoredValue2 = 100;
                    hideGestureControls(musicSeekBarGesture);
                }
            }

            @Override
            public void onSwipeTop() {
                if (!needControls) {
                    gestureStoredValue2 = 100;
                    hideGestureControls(musicSeekBarGesture);
                }
            }
        });
    }*/

    private void fetchGoogleAdvertisingID() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    AdInfo adInfo = AdvertisingIdClient
                            .getAdvertisingIdInfo(context);
                    advertisingId = adInfo.getId();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

/*    private void initPlayPauseForDefault() {
        frameLayoutPlayer.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                playPauseInner();

            }
        });
        frameLayoutPlayerAdd1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                playPauseInner();

            }
        });
        frameLayoutPlayerAdd2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                playPauseInner();

            }
        });
    }*/

    private void playPauseInner() {

        try {
            if (frameLayoutPlayer != null
                    && frameLayoutPlayer.getVisibility() == View.VISIBLE) {
                if (!isStopped) {
                    if(currentStatusTag == null || currentStatusTag.isEmpty())
                        currentStatusTag = "MP";
                    previousStatusTag = currentStatusTag;
                    isStopped = true;
                    mMediaPlayer.setBackgrounded(true);
                    mMediaPlayer.setPlayWhenReady(false);
                } else {
                    isStopped = false;
                    if (previousStatusTag.equalsIgnoreCase(currentStatusTag)) {
                        mMediaPlayer.setBackgrounded(false);
                        mMediaPlayer.setPlayWhenReady(true);
                        fetchMidRollVast();
                    } else {
                        handleAdStiching();
                    }
                }
            } else if (frameLayoutPlayerAdd1 != null
                    && frameLayoutPlayerAdd1.getVisibility() == View.VISIBLE) {
                if (!isStopped) {
                    if(currentStatusTag == null || currentStatusTag.isEmpty())
                        currentStatusTag = "AD";
                    previousStatusTag = currentStatusTag;
                    isStopped = true;
                    mMediaPlayerAdd1.setBackgrounded(true);
                    mMediaPlayerAdd1.setPlayWhenReady(false);
                    if (vastParser != null)
                        hitTracking(vastParser.getTrackings()
                                .get(Tracking.EVENT_PAUSE)
                                .getUrl());

                } else {
                    isStopped = false;
                    if (previousStatusTag.equalsIgnoreCase(currentStatusTag)) {
                        mMediaPlayerAdd1.setBackgrounded(false);
                        mMediaPlayerAdd1.setPlayWhenReady(true);
                        fetchMidRollVast();
                        initilizeMainPlayer(mContentUrl);
                        if (vastParser != null)
                            hitTracking(vastParser.getTrackings()
                                    .get(Tracking.EVENT_RESUME)
                                    .getUrl());
                    } else {
                        handleAdStiching();
                    }
                }

            } else if (frameLayoutPlayerAdd2 != null
                    && frameLayoutPlayerAdd2.getVisibility() == View.VISIBLE) {
                if (!isStopped) {
                    if(currentStatusTag == null || currentStatusTag.isEmpty())
                        currentStatusTag = "AD";
                    previousStatusTag = currentStatusTag;
                    isStopped = true;
                    mMediaPlayerAdd2.setBackgrounded(true);
                    mMediaPlayerAdd2.setPlayWhenReady(false);
                    if (vastParser != null)
                        hitTracking(vastParser.getTrackings()
                                .get(Tracking.EVENT_PAUSE)
                                .getUrl());
                } else {
                    isStopped = false;
                    if (previousStatusTag.equalsIgnoreCase(currentStatusTag)) {
                        mMediaPlayerAdd2.setBackgrounded(false);
                        mMediaPlayerAdd2.setPlayWhenReady(true);
                        fetchMidRollVast();
                        initilizeMainPlayer(mContentUrl);
                        if (vastParser != null)
                            hitTracking(vastParser.getTrackings()
                                    .get(Tracking.EVENT_RESUME)
                                    .getUrl());

                    } else {
                        handleAdStiching();
                    }
                }
            }

            if (isStopped)
                execService.pause();
            else
                execService.resume();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*private void hideGestureControls(final SeekBar seekBar) {

        final Activity activity = (Activity) context;

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            seekBar.setVisibility(View.GONE);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }*/

    private Runnable setNetworkStrength = new Runnable() {

        @Override
        public void run() {

            final int strength = (int) mConnectionClassManager
                    .getDownloadKBitsPerSecond();
//            Log.d(TAG, "" + strength);
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (strength >= 0) {
//                        neededControl(true);
                        surfaceText.setVisibility(View.GONE);
                        surfaceText.bringToFront();
                        surfaceText.setText((String.valueOf((strength))
                                + " Kb/s"));
                        watermarkLogo.bringToFront();
                        if (!preRollPlayed && !loadingView.isShown() && Constant.getInstance().isAdSkipable()) {
                            if (skipableAdCounter > 0) {
                                if (mMediaPlayerAdd1 != null) {
                                    skipableAdCounter = skipAdInMillis - (int) mMediaPlayerAdd1.getCurrentPosition() / 1000;
                                    if (skipableAdCounter >= 0)
                                        skipAdIndicator.setText("Skip AD in " + skipableAdCounter + " sec");
                                    else
                                        skipAdIndicator.setText("Skip AD in 0 sec");
                                    skipAdIndicator.bringToFront();
                                }

                            } else if (skipableAdCounter == 0) {
                                skipableAdCounter = skipAdInMillis - (int) mMediaPlayerAdd1.getCurrentPosition() / 1000;
                                skipButton.setVisibility(View.VISIBLE);
                                skipButton.setEnabled(true);
                                skipButton.bringToFront();
                                skipAdIndicator.setVisibility(View.GONE);
                            }
                        }

//                        logo
                    }
                }
            });
        }
    };

    private Runnable detectAndPlayAd = new Runnable() {

        @Override
        public void run() {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    if (!vodQuePointScheduler.isDone())
                    if (mMediaPlayer != null) {
                        long currentTimeInMillis = mMediaPlayer.getCurrentPosition();

                        if (currentTimeInMillis != 0) {
                            int timeInSec = (int) currentTimeInMillis / 1000;

                            if (vodQuePointList.contains(timeInSec) && !(currentPlaying.equalsIgnoreCase("M2") || currentPlaying.equalsIgnoreCase("M3"))) {
//                                    Log.d(TAG, "Switching for Ad");
                                handleVodAd();
                                seekPlayerTo = timeInSec;
                            } else {
                                if (timeInSec == mMediaPlayer.getDuration()) {
                                    if (vodQuePointScheduler != null) {
                                        vodQuePointScheduler.cancel(true);
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }
    };

    private void handleVodAd() {
        if (mMediaPlayer != null) {
            mMediaPlayer.setPlayWhenReady(false);
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        frameLayoutPlayer.setVisibility(View.GONE);
        mSurfaceView.setVisibility(View.GONE);

        if (activeMediaplayer != null
                && activeMediaplayer.equalsIgnoreCase("M2")) {
            frameLayoutPlayerAdd2.setVisibility(View.GONE);
            mSurfaceViewAdd2.setVisibility(View.GONE);
            frameLayoutPlayerAdd1.setVisibility(View.VISIBLE);
            mSurfaceViewAdd1.setVisibility(View.VISIBLE);
            frameLayoutPlayerAdd1.bringToFront();
            mSurfaceViewAdd1.bringToFront();
            logoLayout.bringToFront();
            startADMediaPlayer();
        } else if (activeMediaplayer != null
                && activeMediaplayer.equalsIgnoreCase("M3")) {
            frameLayoutPlayerAdd1.setVisibility(View.GONE);
            mSurfaceViewAdd1.setVisibility(View.GONE);
            frameLayoutPlayerAdd2.setVisibility(View.VISIBLE);
            mSurfaceViewAdd2.setVisibility(View.VISIBLE);
            frameLayoutPlayerAdd2.bringToFront();
            mSurfaceViewAdd2.bringToFront();
            logoLayout.bringToFront();
            startADMediaPlayer();
        }
    }


    private void fetchMidRollVast() {
//        String url;
//        try {
//            // midRollNetworkType
//            url = new CommonUtils().prepareAdUrl(midRollNetworkType,
//                    midRollBaseUrl, advertisingId, context);
//            Log.d(TAG, "Fetching ad:: " + url);
        isMediationActive = false;
        if (midRollBaseUrl != null)
            AsyncTaskCompat.executeParallel(new Getvast(midRollBaseUrl), null);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
    }

    /*public void setMediaPlayback(boolean bool) {
        if (isPlayerReady) {
            isStopped = bool;
            playPauseInner();
        }
    }*/

    public void setBackgrounded(boolean bool) {
        if (pauseCalledCounter == 0) {
            pauseCalledCounter++;
            return;
        }

        if(countDownTimer != null) {
            if(countDownTimer.isRunning() && bool)
                countDownTimer.pause();
            else if(countDownTimer.isPaused() && !bool)
                countDownTimer.resume();
        }

        if (isPlayerReady) {
            isStopped = !bool;
            playPauseInner();
            if (preRollBaseUrl == null || preRollBaseUrl.trim().length() == 0 || preRollPlayed) {
                skipAdIndicator.setVisibility(View.GONE);
                skipButton.setVisibility(View.GONE);
            }
        }
    }

    public void release() {
        try {

            if (cueSheetInfo != null) {
                cueSheetInfo.setEndTime(CommonUtils.convertDateFormat(System
                        .currentTimeMillis()));
                cueSheetInfoList.add(cueSheetInfo);
            }
            if (cueSheetInfoChannel != null) {
                cueSheetInfoChannel.setEndTime(CommonUtils
                        .convertDateFormat(System.currentTimeMillis()));
                cueSheetInfoList.add(cueSheetInfoChannel);
            }

            if(cueSheetInfoList != null) {
                JSONObject object = CommonUtils
                        .createAnalyticsObject(cueSheetInfoList);

                String param = "cs=" + object.toString() + "&sid=" + sessionID;

                AsyncTaskCompat.executeParallel(new PostAnalytics(param), Constant.getInstance().getPostServerAnalytics() + keyToken);
            }

            activeMediaplayer = null;
            currentPlaying = null;
            currentStatusTag = null;
            isCallBackreceived = false;
            isAdPlayerReady = false;
            if (mMediaPlayer != null) {
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
            if (mSurfaceView != null) {
                mSurfaceView.setVisibility(View.GONE);
                mSurfaceView = null;
            }
            if (mMediaPlayerAdd1 != null) {
                mMediaPlayerAdd1.release();
                mMediaPlayerAdd1 = null;
            }
            if (mSurfaceViewAdd1 != null) {
                mSurfaceViewAdd1.setVisibility(View.GONE);
                mSurfaceViewAdd1 = null;
            }
            if (mMediaPlayerAdd2 != null) {
                mMediaPlayerAdd2.release();
                mMediaPlayerAdd2 = null;
            }
            if (mSurfaceViewAdd2 != null) {
                mSurfaceViewAdd2.setVisibility(View.GONE);
                mSurfaceViewAdd2 = null;
            }
            if (adTimer != null)
                adTimer.cancel();
            if (commonSDK != null) {
                commonSDK.release();
                commonSDK = null;
            }
            if (mDeviceBandwidthSampler != null) {
                mDeviceBandwidthSampler.startSampling();
            }
            if (execService != null && !execService.isTerminated()) {
                execService.shutdown();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateToken() {
        if (NetowrkUtil.isNetworkAvailable(context)) {
            if (channelInfo != null) {
                HashMap<String, String> urlMap = channelInfo.getChannelContentUrl();
                if (urlMap != null && urlMap.size() > 0) {
                    if (urlMap.get("abr") != null && !urlMap.get("abr").equals("null") && !urlMap.get("abr").isEmpty()) {
                        mContentUrl = urlMap.get("abr");
                    } else {
                        String networkType = NetowrkUtil.getNetworkClass(context);
                        if (networkType != null) {
                            if (networkType.equalsIgnoreCase("2g")) {
                                mContentUrl = urlMap.get("2g");
                            } else if (networkType.equalsIgnoreCase("3g")) {
                                mContentUrl = urlMap.get("3g");
                            } else if (networkType.equalsIgnoreCase("wifi")) {
                                mContentUrl = urlMap.get("wifi");
                            }
                            if (mContentUrl == null || mContentUrl.isEmpty())
                                mContentUrl = urlMap.get("main");
                        }
                    }
                }
            }
        }
        if (contentCallDecider == 2 || contentCallDecider == 4)
            mContentUrl = contentUrl;

//        mContentUrl = "http://dai.multitvsolution.com:1935/dvr/test/playlist.m3u8";

        // To enable stream security

     /*   if (mContentUrl != null && mContentUrl.contains("akamai")) {
            acl = mContentUrl.substring(mContentUrl.indexOf("/i"), mContentUrl.indexOf("/ma") + 1) + "*";
            String[] args = {"-k", AKAMAI_KEY, "-a", acl, "-w", timeWindow};
            String token = new AkamaiToken().prepareTokenParams(args);
            if (token != null && token.contains("*//*")) {
                token.replace("*//*", "%2F*");
            }
            mContentUrl = mContentUrl + token;

            Log.e(TAG, mContentUrl);
        }*/
    }

    private void hitTracking(final String url) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, url);
                    new HttpUtils().executeHttpGetRequest(url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseM3U8Stream(final String urlPath) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    URL url = new URL(urlPath);
                    InputStream M3U8 = (InputStream) url.getContent();
                    availableResolutionContainer = new HashMap<>();
                    availableResolutionContainer = new CommonParser()
                            .parseHLSMetadata(M3U8, urlPath);
                    if (resolutionSelector != null)
                        resolutionSelector.setVisibility(View.VISIBLE);
                    isMakeResolutionSelectorVisible = true;
                } catch (Exception e) {
                    e.printStackTrace();
//                    if (resolutionSelector != null)
//                        ((Activity) context).runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                resolutionSelector.setVisibility(View.GONE);
//                            }
//                        });
                }
            }
        }).start();

    }

    @Override
    public void permissionStatusSuccessful(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PermissionId.PERMISSION_MULTIPLE_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];

                switch (permission) {
                    case PermissionId.PERMISSION_ACCESS_COARSE_LOCATION:
                        if (grantResults[i] == 0)
                            isAccessCoarseLocationPermissionEnabled = true;
                        break;
                    case PermissionId.PERMISSION_ACCESS_FINE_LOCATION:
                        if (grantResults[i] == 0)
                            isAccessFineLocationPermissionEnabled = true;
                        break;
                    case PermissionId.PERMISSION_READ_PHONE_STATE:
                        if (grantResults[i] == 0)
                            isReadStatePermissionEnabled = true;
                        break;
                }
            }
        }

        if (isReadStatePermissionEnabled) {
            try {
                preparePlayer();
            } catch (MultiTVException e) {
                e.getMessage();
            }
        } else
            showMandatoryPermissionSnackbar(false);
    }

    @Override
    public void permissionStatusDenied(int requestCode, String[] permissions, int[] grantResults) {
     /*   if(requestCode == PermissionId.PERMISSION_MULTIPLE_REQUEST_CODE)
            Toast.makeText(context, "Read phone state permission is denied by user", Toast.LENGTH_SHORT).show();*/
    }

    private void showMandatoryPermissionSnackbar(boolean isShowGoToSettingsInSnackbar) {
      /*  if (isShowGoToSettingsInSnackbar) {
            Snackbar.with(context)
                    .text("Please go to your app settings to enable mandatory Read phone state permission.")
                    .textColor(Color.WHITE)
                    *//*.actionLabel("Go to settings")*//*
                    .actionColor(Color.RED)
                    .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                    *//*.actionListener(new ActionClickListener() {
                        @Override
                        public void onActionClicked(Snackbar snackbar) {
                            Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.getPackageName()));
                            myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                            myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            ((Activity)context).startActivityForResult(myAppSettings, 1234);
                        }
                    })*//*
                    .show((Activity) context);
        } else {
            Snackbar.with(context)
                    .text("Read phone state permission is mandatory")
                    .textColor(Color.WHITE)
                    .actionLabel("Provide permission")
                    .actionColor(Color.RED)
                    .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                    .actionListener(new ActionClickListener() {
                        @Override
                        public void onActionClicked(Snackbar snackbar) {
                            if (permissionsPermanentlyDenied != null && permissionsPermanentlyDenied.size() != 0) {
                                handlePermissionsDenied();
                            } else {
                                isCheckOnlyPhoneStatePermission = true;
                                isRuntimePermissionsCheckedOnce = false;
                                try {
                                    preparePlayer();
                                } catch (MultiTVException e) {
                                    e.getMessage();
                                }
                            }
                        }
                    }).show((Activity) context);
        }*/
    }

    private void handlePermissionsDenied() {
        String permissionsName = "";
        for (int i = 0; i < permissionsPermanentlyDenied.size(); i++) {
            if (permissionsPermanentlyDenied.size() > 1)
                permissionsName = permissionsName + permissionsPermanentlyDenied.get(i) + ", ";
            else
                permissionsName = permissionsPermanentlyDenied.get(i);
        }

        showMandatoryPermissionSnackbar(true);

      /*  showMessageOKCancel("You need to allow access to " + permissionsName,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showMandatoryPermissionSnackbar(true);
                    }
                });*/
    }

    /*private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }*/


    private class PostAnalytics extends AsyncTask<String, Void, Void> {
        String postParam;

        public PostAnalytics(String param) {
            this.postParam = param;
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                new HttpUtils().excuteHttpPostRequest(
                        params[0], postParam);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void fetchChannelUrl(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String response = new HttpUtils().executeHttpGetRequest(url);

                ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "Channel detail is::: " + response);
                try {
                    final ChannelInfo result = new CommonParser().parseContentData(contentCallDecider, response);

                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (result != null) {
                                channelInfo = result;
                                isPlayerReady = true;
                                interfaceObject.onPlayerReady();
                                if (channelInfo.getChannelContentUrl() != null && channelInfo.getChannelContentUrl().get("abr") != null)
                                    parseM3U8Stream(channelInfo.getChannelContentUrl().get("abr"));
                                else if (resolutionSelector != null)
                                    resolutionSelector.setVisibility(View.GONE);
                            } else {
                                isPlayerReady = true;
                                interfaceObject.onPlayerReady();
                            }
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

   /* private class FetchChannelURL extends AsyncTask<String, Void, ChannelInfo> {

        @Override
        protected ChannelInfo doInBackground(String... params) {
            String response = new HttpUtils().executeHttpGetRequest(params[0]);

            ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "Channel detail is::: " + response);
            try {
                return new CommonParser().parseContentData(contentCallDecider, response);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ChannelInfo result) {
            super.onPostExecute(result);
            if (result != null) {
                channelInfo = result;
                isPlayerReady = true;
                interfaceObject.onPlayerReady();
                if (channelInfo.getChannelContentUrl() != null && channelInfo.getChannelContentUrl().get("abr") != null)
                    parseM3U8Stream(channelInfo.getChannelContentUrl().get("abr"));
                else if (resolutionSelector != null)
                    resolutionSelector.setVisibility(View.GONE);
            } else {
                isPlayerReady = true;
                interfaceObject.onPlayerReady();
            }
        }
    }*/

    public long getDuration() {
        if (mMediaPlayer != null)
            return mMediaPlayer.getDuration();
        else
            return 0;
    }

    public long getCurrentPosition() {
        if (mMediaPlayer != null)
            return mMediaPlayer.getCurrentPosition();
        else
            return 0;
    }

    public void resumeFromPosition(int millisecondsForResume) {
        if(millisecondsForResume != 0) {
            this.millisecondsForResume = millisecondsForResume;
            isResumeFromPreviousPosition = true;
        }
    }

    private class Getvast extends AsyncTask<Void, Void, String> {
        private boolean autoStartPlayer;
        private String url;
        private int i = 0;

        public Getvast(boolean bool, String url) {
            this.autoStartPlayer = bool;
            this.url = url;
            i = 1;
        }

        public Getvast(String url) {
            this.url = url;
            i = 2;
        }

        @Override
        protected String doInBackground(Void... params) {
            ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "Vast url is :::::::: " + this.url);
            String response = "";
            if (url != null && url.trim().length() > 0) {
                try {
                    response = new HttpUtils().executeHttpGetRequest(url);
                    return response;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null && result.trim().length() > 0) {
                new VASTXmlParser(context, listener, result, autoStartPlayer);
            } else {
                if (i == 1) {
                    AsyncTaskCompat.executeParallel(new Getvast(autoStartPlayer, url), null);
                } else if (i == 2) {
                    AsyncTaskCompat.executeParallel( new Getvast(url), null);
                }
            }
        }
    }

    private class ConnectionChangedListener implements
            ConnectionClassManager.ConnectionClassStateChangeListener {

        @Override
        public void onBandwidthStateChange(ConnectionQuality bandwidthState) {
            mConnectionClass = bandwidthState;
        }
    }

    @Override
    public void onAdCallback(String value, String session) {

        sessionID = session;
        if (isPlayerReady && value != null && value.trim().length() > 0) {
            if(!socketStatusTag.equals("MP"))
                socketStatusTag = value;
            //currentStatusTag = value;

            if(!Constant.getInstance().isReadStreamTag()) { //If we are not reading from ID3 tag, writing AD and MP both
                stitchingStatus.setText(value);
                currentStatusTag = value;
            }
            else if(value.equals("MP")) { //If we are reading from socket, writing stitching text only in case of MP
                stitchingStatus.setText(value);
                currentStatusTag = "MP";
            }

            if (!isStopped)
                if (preRollBaseUrl != null
                        && preRollBaseUrl.trim().length() > 0) {
                    if (preRollPlayed)
                        handleAdStiching();
                } else {
                    handleAdStiching();
                }
        }
    }

    private void handleAdStiching() {
        if (!isResolutionSelectionEnable)
            if (currentStatusTag.trim().equalsIgnoreCase("MP")) {
                if (!isCallBackreceived) {
                    initilizeMainPlayer(mContentUrl);
                } else {

                    if (currentPlaying != null
                            && !currentPlaying.equalsIgnoreCase("M1")) {
                        isFromMPCallBack = true;
                        if (cueSheetInfo != null) {
                            cueSheetInfo.setEndTime(CommonUtils
                                    .convertDateFormat(System
                                            .currentTimeMillis()));
                            cueSheetInfoList.add(cueSheetInfo);
                            cueSheetInfo = null;
                        }

                        mSurfaceViewAdd2.setVisibility(View.GONE);
                        frameLayoutPlayerAdd1.setVisibility(View.GONE);
                        frameLayoutPlayerAdd2.setVisibility(View.GONE);
                        mSurfaceViewAdd1.setVisibility(View.GONE);
                        mSurfaceView.setVisibility(View.VISIBLE);
                        frameLayoutPlayer.setVisibility(View.VISIBLE);
                        mSurfaceView.bringToFront();
                        frameLayoutPlayer.bringToFront();
                        logoLayout.bringToFront();
                        if (mMediaPlayerAdd1 != null) {
                            mMediaPlayerAdd1.release();
                        }
                        if (mMediaPlayerAdd2 != null) {
                            mMediaPlayerAdd2.release();
                        }
                        activeMediaplayer = "M1";
                        currentPlaying = "M1";
                        mMediaPlayer.setPlayWhenReady(true);

                        mediaController.updateSystemConfig();
                        // surfaceText.setText("Main Program "
                        // + mSurfaceView.getVisibility());

                        cueSheetInfoChannel = new CueSheet();
                        cueSheetInfoChannel.setType("MP");
                        cueSheetInfoChannel.setStartTime(CommonUtils
                                .convertDateFormat(System.currentTimeMillis()));
                        cueSheetInfoChannel.setUrl(mContentUrl);

                        fetchMidRollVast();
                    }
                }

            } else if (!Constant.getInstance().isReadStreamTag() && currentStatusTag.trim().equalsIgnoreCase("AD")) {
                handleStitchingFromStreamForAD();
              /*  if (!isCallBackreceived) {
                    fetchMidRollVast();
                } else {
                    if (cueSheetInfoChannel != null) {
                        cueSheetInfoChannel.setEndTime(CommonUtils
                                .convertDateFormat(System.currentTimeMillis()));
                        cueSheetInfoList.add(cueSheetInfoChannel);
                        cueSheetInfoChannel = null;
                    }
                    if (mMediaPlayer != null) {
                        // if (mMediaPlayer.isPlaying())
                        mMediaPlayer.setPlayWhenReady(false);
                        mMediaPlayer.release();
                    }
                    frameLayoutPlayer.setVisibility(View.GONE);
                    mSurfaceView.setVisibility(View.GONE);

                    if (activeMediaplayer != null
                            && activeMediaplayer.equalsIgnoreCase("M2")) {
                        frameLayoutPlayerAdd2.setVisibility(View.GONE);
                        mSurfaceViewAdd2.setVisibility(View.GONE);
                        frameLayoutPlayerAdd1.setVisibility(View.VISIBLE);
                        mSurfaceViewAdd1.setVisibility(View.VISIBLE);
                        frameLayoutPlayerAdd1.bringToFront();
                        mSurfaceViewAdd1.bringToFront();
                        startADMediaPlayer();
                        // surfaceText.setText("Surface Ad 1");
                    } else if (activeMediaplayer != null
                            && activeMediaplayer.equalsIgnoreCase("M3")) {
                        frameLayoutPlayerAdd1.setVisibility(View.GONE);
                        mSurfaceViewAdd1.setVisibility(View.GONE);
                        frameLayoutPlayerAdd2.setVisibility(View.VISIBLE);
                        mSurfaceViewAdd2.setVisibility(View.VISIBLE);
                        frameLayoutPlayerAdd2.bringToFront();
                        mSurfaceViewAdd2.bringToFront();
                        // surfaceText.setText("Surface Ad 2");
                        startADMediaPlayer();
                    }
                }*/
            }

    }

    public interface MultiTvPlayerListner {
        void onPlayerReady();
    }

    @Override
    public void getResolutionView(ImageButton resolutionSelector) {
        this.resolutionSelector = resolutionSelector;
        if (isMakeResolutionSelectorVisible)
            resolutionSelector.setVisibility(VISIBLE);
        resolutionSelector.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (availableResolutionContainer != null
                        && availableResolutionContainer.size() > 0) {

                    android.app.FragmentTransaction ft = ((Activity) context)
                            .getFragmentManager().beginTransaction();
                    Set<String> keys = availableResolutionContainer.keySet();
                    String[] outputArray = new String[keys.size()];
                    int i = 0;
                    outputArray[i] = "Auto";
                    for (String key : keys) {
                        if (key != null && !key.equalsIgnoreCase("Auto")) {
                            i++;
                            outputArray[i] = key;
                            continue;
                        }

                    }
                    MyDialogFragment fragment = MyDialogFragment.getInstance(
                            outputArray,
                            (ResolutionSelection) MultiTvPlayer.this);
                    fragment.show(ft, "MyDialogFragment");
                }
            }
        });
    }

    @Override
    public void onResolutionSelection(String index) {
        isResolutionSelectionEnable = true;
        if (loadingView != null) {
            loadingView.show();
            loadingView.bringToFront();
        }
        initilizeMainPlayer(availableResolutionContainer.get(index));
    }

    @Override
    public void onId3Metadata(List<Id3Frame> id3Frames) {
        for (Id3Frame id3Frame : id3Frames) {
            if (id3Frame instanceof TxxxFrame) {
                if (isFromMPCallBack) {
                    isFromMPCallBack = false;
                    return;
                }

                TxxxFrame txxxFrame = (TxxxFrame) id3Frame;
                ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, String.format("ID3 TimedMetadata %s: description=%s, value=%s", txxxFrame.id,
                        txxxFrame.description, txxxFrame.value));

                String id3Value = "";
                if (txxxFrame.value.contains("AD"))
                    id3Value = "AD";
                else if (txxxFrame.value.contains("MP"))
                    id3Value = "MP";

                if (socketStatusTag.equals("MP") && !socketStatusTag.equals(id3Value))
                    return;
                else
                    socketStatusTag = "";

                if (!id3Value.isEmpty() && isAdPlayerReady) {
                    if (id3Value.equals("AD")) {
                        currentStatusTag = "AD";
                        //stitchingStatus.setText("AD-ID3");
                        stitchingStatus.setText("AD");
                        ToastMessage.showLogs(ToastMessage.LogType.INFORMATION, TAG,
                                "Broadcasting result message ID3: AD-ID3");
                        handleStitchingFromStreamForAD();
                    } else if (id3Value.equals("MP")) {
                        //stitchingStatus.setText("MP-ID3");
                        ToastMessage.showLogs(ToastMessage.LogType.INFORMATION, TAG,
                                "Broadcasting result message ID3: MP-ID3");
                    }
                } else {
                    ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, String.format("ID3 TimedMetadata %s", id3Frame.id));
                }
            }
        }
    }

    private void toggleControlsVisibility() {
        if (mediaController.isShowing()) {
            mediaController.hide();
        } else {
            showControls();
        }
    }

    private void showControls() {
        mediaController.show(0);
    }


    private void handleStitchingFromStreamForAD() {
        if (!isCallBackreceived) {
            fetchMidRollVast();
        } else {
            //If no ad url is there, there is no point in initializing and
            //starting ad players
           /* if (vastParser == null || vastParser.getMediaFileUrl() == null
                    || vastParser.getMediaFileUrl().isEmpty()) {
                ToastMessage.showLogs(ToastMessage.LogType.WARNING, TAG,
                        "AD url is null or empty in handleStitchingFromStreamForAD(). Hence not starting ad media player.");
                ToastMessage.showToastMsg(context, "AD url is null or empty in handleStitchingFromStreamForAD()", Toast.LENGTH_SHORT);
                return;
            }*/

            if (cueSheetInfoChannel != null) {
                cueSheetInfoChannel.setEndTime(CommonUtils
                        .convertDateFormat(System.currentTimeMillis()));
                cueSheetInfoList.add(cueSheetInfoChannel);
                cueSheetInfoChannel = null;
            }
            if (mMediaPlayer != null) {
                // if (mMediaPlayer.isPlaying())
                mMediaPlayer.setPlayWhenReady(false);
                mMediaPlayer.release();
            }
            frameLayoutPlayer.setVisibility(View.GONE);
            mSurfaceView.setVisibility(View.GONE);

            if (activeMediaplayer != null
                    && activeMediaplayer.equalsIgnoreCase("M2")) {
                frameLayoutPlayerAdd2.setVisibility(View.GONE);
                mSurfaceViewAdd2.setVisibility(View.GONE);
                frameLayoutPlayerAdd1.setVisibility(View.VISIBLE);
                mSurfaceViewAdd1.setVisibility(View.VISIBLE);
                frameLayoutPlayerAdd1.bringToFront();
                mSurfaceViewAdd1.bringToFront();
                logoLayout.bringToFront();
                startADMediaPlayer();
                // surfaceText.setText("Surface Ad 1");
            } else if (activeMediaplayer != null
                    && activeMediaplayer.equalsIgnoreCase("M3")) {
                frameLayoutPlayerAdd1.setVisibility(View.GONE);
                mSurfaceViewAdd1.setVisibility(View.GONE);
                frameLayoutPlayerAdd2.setVisibility(View.VISIBLE);
                mSurfaceViewAdd2.setVisibility(View.VISIBLE);
                frameLayoutPlayerAdd2.bringToFront();
                mSurfaceViewAdd2.bringToFront();
                logoLayout.bringToFront();
                // surfaceText.setText("Surface Ad 2");
                startADMediaPlayer();
            }
        }
    }

    private void postAdRequests(boolean isImpression) {
        String tagUrl;
        if (preRollBaseUrl != null && preRollBaseUrl.trim().length() > 0 && !preRollPlayed) {
            tagUrl = preRollBaseUrl.split("&")[0];
        } else {
            tagUrl = midRollBaseUrl.split("&")[0];
        }
        if (tagUrl != null && !isMediationActive)
            if (contentType == ContentType.LIVE) {
                if (channelID != null && channelID.trim().length() > 0) {
                    if (isImpression)
                        hitTracking(Constant.getInstance().getPostAdImpression() + channelID + "?tag=" + tagUrl);
                    else
                        hitTracking(Constant.getInstance().getPostClickEvent() + channelID + "?tag=" + tagUrl + "&type=1");
                } else {
                    if (isImpression)
                        hitTracking(Constant.getInstance().getPostAdImpression() + keyToken + "?tag=" + tagUrl);
                    else
                        hitTracking(Constant.getInstance().getPostClickEvent() + keyToken + "?tag=" + tagUrl + "&type=1");
                }
            } else {
                if (channelID != null && channelID.trim().length() > 0) {
                    if (isImpression)
                        hitTracking(Constant.getInstance().getPostVODAdImpression() + keyToken + "?tag=" + tagUrl + "&cid=" + channelID);
                    else
                        hitTracking(Constant.getInstance().getPostClickEvent() + channelID + "?tag=" + tagUrl + "&type=2");
                } else {
                    if (isImpression)
                        hitTracking(Constant.getInstance().getPostVODAdImpression() + keyToken + "?tag=" + tagUrl);
                    else
                        hitTracking(Constant.getInstance().getPostClickEvent() + keyToken + "?tag=" + tagUrl + "&type=2");
                }
            }
    }

/*    public void neededControl(boolean bool) {

        if (!bool) {
            controlLayout.setVisibility(View.GONE);
        } else {
            Log.d(TAG, "Showing control bar");
            controlLayout.requestLayout();
            controlLayout.setVisibility(View.VISIBLE);
            controlLayout.bringToFront();
//            controlLayout.invalidate();
//            guestureParentLayout.bringChildToFront(findViewById(R.id.controlsLayout));
        }

    }*/

    private static final class KeyCompatibleMediaController extends VideoControllerView {

        private PlayerControl playerControl;

        public KeyCompatibleMediaController(Context context, Controlers cntrls) {
            super(context, cntrls);
        }

        @Override
        public void setMediaPlayer(PlayerControl playerControl) {
            super.setMediaPlayer(playerControl);
            this.playerControl = playerControl;
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            int keyCode = event.getKeyCode();
            if (playerControl.canSeekForward() && keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    playerControl.seekTo(playerControl.getCurrentPosition() + 15000); // milliseconds
                    show();
                }
                return true;
            } else if (playerControl.canSeekBackward() && keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    playerControl.seekTo(playerControl.getCurrentPosition() - 5000); // milliseconds
                    show();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {

                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {

                return true;
            }
            return super.dispatchKeyEvent(event);
        }
    }

    @Override
    public void play() {
        ToastMessage.showToastMsg(context, "Play", Toast.LENGTH_SHORT);
        execService.resume();
    }

    @Override
    public void pause() {
        ToastMessage.showToastMsg(context, "Pause", Toast.LENGTH_SHORT);
        execService.pause();
    }

    public enum ContentType {
        LIVE, VOD;
    }
}
