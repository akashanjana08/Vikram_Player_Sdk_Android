/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.multitv.multitvplayersdk.controls;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.android.exoplayer.multitv.AspectRatioFrameLayout;
import com.google.android.exoplayer.multitv.util.PlayerControl;
import com.multitv.multitvplayersdk.R;
import com.multitv.multitvplayersdk.customeviews.CustomSeekBar;
import com.multitv.multitvplayersdk.utils.AspectRatioUtils;
import com.multitv.multitvplayersdk.utils.CommonUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;


/**
 * A view containing controls for a MediaPlayer. Typically contains the
 * buttons like "Play/Pause", "Rewind", "Fast Forward" and a progress
 * slider. It takes care of synchronizing the controls with the state
 * of the MediaPlayer.
 * <p/>
 * The way to use this class is to instantiate it programatically.
 * The MediaController will create a default set of controls
 * and put them in a window floating above your application. Specifically,
 * the controls will float above the view specified with setAnchorView().
 * The window will disappear if left idle for three seconds and reappear
 * when the user touches the anchor view.
 * <p/>
 * Functions like show() and hide() have no effect when MediaController
 * is created in an xml layout.
 * <p/>
 * MediaController will hide and
 * show the buttons according to these rules:
 * <ul>
 * <li> The "previous" and "next" buttons are hidden until setPrevNextListeners()
 * has been called
 * <li> The "previous" and "next" buttons are visible but disabled if
 * setPrevNextListeners() was called with null listeners
 * <li> The "rewind" and "fastforward" buttons are shown unless requested
 * otherwise by using the MediaController(Context, boolean) constructor
 * with the boolean set to false
 * </ul>
 */
public class VideoControllerView extends FrameLayout {
    private static final String TAG = "VideoControllerView";

    private PlayerControl mPlayer;
    private Context mContext;
    private ViewGroup mAnchor;
    private View mRoot;
    private CustomSeekBar mProgress;
    private SeekBar mVolume, mBrightness;
    private TextView mEndTime, mCurrentTime;
    private boolean mShowing;
    private boolean mDragging;
    private static final int sDefaultTimeout = 3000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private boolean mUseFastForward;
    private boolean mFromXml;
    private boolean mListenersSet;
    private OnClickListener mNextListener, mPrevListener;
    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    private ImageButton mPauseButton, mFfwdButton, mRewButton, mNextButton, mPrevButton, mFullscreenButton, mLockButton, mBrightnessButton, mVolumeButton, mAspectRatioButton, mResolutionSelector;
    private ImageView mUnlockButton;
    private LinearLayout mVolumeLayout, mBrightnessLayout, mVideoProgressLayout;
    private Handler mHandler = new MessageHandler(this);
    private AudioManager audioManager;
    private CommonUtils utils;
    private Controlers cntrlers;
    private SurfaceView mSurfaceView;
    private int aspectRatio = AspectRatioUtils.ASPECT_RATIO_STRETCH; //Default
    private int videoOriginalWidth, videoOriginalHeight;
    private boolean isShowRemainingDuration;
    private ArrayList<Integer> vodQuePointList;

    public VideoControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRoot = null;
        mContext = context;
        mUseFastForward = true;
        mFromXml = true;

        Log.i(TAG, TAG);
    }

    public VideoControllerView(Context context, boolean useFastForward, Controlers cntrls) {
        super(context);
        mContext = context;
        mUseFastForward = useFastForward;
        this.cntrlers = cntrls;

        Log.i(TAG, TAG);
    }

    public VideoControllerView(Context context, Controlers controlers) {
        this(context, true, controlers);

        Log.i(TAG, TAG);
    }

    @Override
    public void onFinishInflate() {
        if (mRoot != null)
            initControllerView(mRoot);
    }

    public void setMediaPlayer(PlayerControl player) {
        mPlayer = player;
        updatePausePlay();
        /*updateFullScreen();*/
    }

    /**
     * Set the view that acts as the anchor for the control view.
     * This can for example be a VideoView, or your Activity's main view.
     *
     * @param view The view to which to anchor the controller when it is visible.
     */
    public void setAnchorView(ViewGroup view) {
        mAnchor = view;

        LayoutParams frameParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        removeAllViews();
        View v = makeControllerView();
        addView(v, frameParams);
    }

    public void setOriginalVideoHeightAndWidth(int width, int height) {
        videoOriginalWidth = width;
        videoOriginalHeight = height;
    }

    public void setResolutionViewListener(ResolutionViewListener mResolutionViewListener) {
        if (mResolutionViewListener != null)
            mResolutionViewListener.getResolutionView(mResolutionSelector);
    }

    public void setVodQuePointList(ArrayList<Integer> vodQuePointList) {
        if (vodQuePointList != null && vodQuePointList.size() != 0) {
            this.vodQuePointList = vodQuePointList;
            /*initDataToSeekbar();*/
        }
    }


    /**
     * Create the view that holds the widgets that control playback.
     * Derived classes can override this to create their own.
     *
     * @return The controller view.
     * @hide This doesn't work as advertised
     */
    protected View makeControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = inflate.inflate(R.layout.media_controller, null);
        utils = new CommonUtils();

        initControllerView(mRoot);

        return mRoot;
    }

    private void initControllerView(View v) {
        final Handler handler = new Handler(Looper.getMainLooper());
        mPauseButton = (ImageButton) v.findViewById(R.id.pause);
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }

       /* mFullscreenButton = (ImageButton) v.findViewById(R.id.fullscreen);
        if (mFullscreenButton != null) {
            mFullscreenButton.requestFocus();
            mFullscreenButton.setOnClickListener(mFullscreenListener);
        }*/

        mFfwdButton = (ImageButton) v.findViewById(R.id.ffwd);
        if (mFfwdButton != null) {
            mFfwdButton.setOnClickListener(mFfwdListener);
            if (!mFromXml) {
                mFfwdButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
            }
        }

        mRewButton = (ImageButton) v.findViewById(R.id.rew);
        if (mRewButton != null) {
            mRewButton.setOnClickListener(mRewListener);
            if (!mFromXml) {
                mRewButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
            }
        }

        // By default these are hidden. They will be enabled when setPrevNextListeners() is called
        mNextButton = (ImageButton) v.findViewById(R.id.next);
        if (mNextButton != null && !mFromXml && !mListenersSet) {
            mNextButton.setVisibility(View.GONE);
        }
        mPrevButton = (ImageButton) v.findViewById(R.id.prev);
        if (mPrevButton != null && !mFromXml && !mListenersSet) {
            mPrevButton.setVisibility(View.GONE);
        }

        mVideoProgressLayout = (LinearLayout) v.findViewById(R.id.videoprogress);
        mBrightnessLayout = (LinearLayout) v.findViewById(R.id.brightnessprogress);
        mVolumeLayout = (LinearLayout) v.findViewById(R.id.volumeprogress);
        mUnlockButton = (ImageView) mAnchor.findViewById(R.id.unlock);
        mLockButton = (ImageButton) v.findViewById(R.id.lock);
        mUnlockButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mRoot.setVisibility(VISIBLE);
                mUnlockButton.setVisibility(GONE);
            }
        });
        mLockButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mRoot.setVisibility(GONE);
                mUnlockButton.setVisibility(VISIBLE);
            }
        });
        mBrightnessButton = (ImageButton) v.findViewById(R.id.brightness);
        mBrightnessButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSystemConfig();
                if (mBrightnessLayout.isShown()) {
                    mBrightnessLayout.setVisibility(GONE);
                    mVideoProgressLayout.setVisibility(VISIBLE);
                    mVolumeLayout.setVisibility(GONE);
                } else {
                    mBrightnessLayout.setVisibility(VISIBLE);
                    mVideoProgressLayout.setVisibility(GONE);
                    mVolumeLayout.setVisibility(GONE);
                }
                handler.removeCallbacks(updateControlLayout);
                handler.postDelayed(updateControlLayout, 5000);
            }
        });

        mVolumeButton = (ImageButton) v.findViewById(R.id.volume);
        mVolumeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                updateSystemConfig();
                if (mVolumeLayout.isShown()) {
                    mVolumeLayout.setVisibility(GONE);
                    mVideoProgressLayout.setVisibility(VISIBLE);
                    mBrightnessLayout.setVisibility(GONE);
                } else {
                    mVolumeLayout.setVisibility(VISIBLE);
                    mVideoProgressLayout.setVisibility(GONE);
                    mBrightnessLayout.setVisibility(GONE);
                }
                handler.removeCallbacks(updateControlLayout);
                handler.postDelayed(updateControlLayout, 5000);
            }
        });

        mAspectRatioButton = (ImageButton) v.findViewById(R.id.aspect_ratio);
        mAspectRatioButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setAspectRatio();
            }
        });

        mResolutionSelector = (ImageButton) v.findViewById(R.id.resolution_selector);
        mSurfaceView = (SurfaceView) ((AspectRatioFrameLayout)
                mAnchor.findViewById(R.id.video_frame)).getChildAt(0);
        setAspectRatio(); //Setting default aspect ratio

        mProgress = (CustomSeekBar) v.findViewById(R.id.mediacontroller_progress);

        mBrightness = (SeekBar) v.findViewById(R.id.brightness_progress);
        mBrightness.getProgressDrawable().setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN);
        /*mBrightness.getThumb().setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN);*/

        mVolume = (SeekBar) v.findViewById(R.id.volume_progress);
        mVolume.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
        /*mVolume.getThumb().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);*/

        mVolume.setOnSeekBarChangeListener(mSeekVolume);
        mBrightness.setOnSeekBarChangeListener(mSeekBrightnessListner);

//        int volume = utils.getValue("Volume", mContext);
//        if (volume == 0)
//            volume = 5;
//        int brightness = utils.getValue("Brightness", mContext);
//        if (brightness == 0)
//            brightness = 40;
        audioManager = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        updateSystemConfig();

        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            mProgress.setMax(1000);
        }

        mEndTime = (TextView) v.findViewById(R.id.time);
        mEndTime.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isShowRemainingDuration)
                    isShowRemainingDuration = true;
                else
                    isShowRemainingDuration = false;
            }
        });
        mCurrentTime = (TextView) v.findViewById(R.id.time_current);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        installPrevNextListeners();
    }

   /* private void setSeekBarColorOnBasisOfQuePoints(final ArrayList<Integer> vodQuePointList) {
        ShapeDrawable.ShaderFactory shaderFactory = new ShapeDrawable.ShaderFactory() {
            @Override
            public Shader resize(int width, int height) {
                ArrayList<Integer> colorArrayList = new ArrayList<>();
                colorArrayList.add(Color.BLUE);
                ArrayList<Float> pointArrayList = new ArrayList<>();
                pointArrayList.add(0f);
                for(int i = 0; i < vodQuePointList.size(); i++) {
                    int point =  vodQuePointList.get(i);
                    Log.e(TAG, "position = " + i + " Value = " + point);
                    pointArrayList.add((float) point / 100);
                    colorArrayList.add(Color.YELLOW);
                    pointArrayList.add(((float) point / 100) + 0.001f);
                    colorArrayList.add(Color.BLUE);
                }

                pointArrayList.add(1 - pointArrayList.get(pointArrayList.size() - 1));
                colorArrayList.add(Color.BLUE);
                pointArrayList.add(1f);
                colorArrayList.add(Color.BLUE);

                int[] intArray = new int[colorArrayList.size()];
                for(int i = 0; i < colorArrayList.size(); i++)
                    intArray[i] = colorArrayList.get(i);

                float[] floatArray = new float[pointArrayList.size()];
                for(int i = 0; i < pointArrayList.size(); i++)
                    floatArray[i] = pointArrayList.get(i);

                for(int i = 0; i < intArray.length; i++)
                    Log.e("", "" + intArray[i]);

                for(int i = 0; i < floatArray.length; i++)
                    Log.e("", "" + floatArray[i]);

                LinearGradient linearGradient = new LinearGradient(0, 0, width, height,
                        new int[]{
                                Color.BLUE,
                                Color.YELLOW,
                                Color.BLUE,
                                Color.BLUE,
                                Color.BLUE
                        }, //substitute the correct colors for these
                        new float[]{
                                0, 0.30f, 0.30000001f, 0.699f, 1},
                        *//*intArray, floatArray,*//*
                       *//* new int[]{
                                Color.BLUE,
                                Color.GRAY,
                                Color.GREEN,
                                Color.RED}, //substitute the correct colors for these
                        new float[]{
                                0, 0.40f, 0.60f, 1},*//*
                        Shader.TileMode.REPEAT);
                return linearGradient;
               *//* else {
                    LinearGradient linearGradient = new LinearGradient(0, 0, width, height,
                            Color.BLUE , //substitute the correct colors for these
                            Color.BLUE,
                            Shader.TileMode.REPEAT);
                    return linearGradient;
                }*//*
            }
        };
        PaintDrawable paint = new PaintDrawable();
        paint.setShape(new RectShape());
        paint.setShaderFactory(shaderFactory);

        mProgress.setProgressDrawable(paint);
    }*/

    private void setSeekBarColorOnBasisOfQuePoints(final ArrayList<Integer> vodQuePointList) {
        if (mPlayer == null)
            return;

        ArrayList<CustomSeekBar.ProgressItem> progressItemList = new ArrayList<>();
        int blueColor = mContext.getResources().getColor(R.color.seekbar_progess_color);

        if(vodQuePointList != null && vodQuePointList.size() != 0 && mPlayer.getDuration() != 0) {
            float spanElapsed = 0;

            int duration = mPlayer.getDuration();

            int previousPoint;
            int previousPointsSum = 0;
            int adSlotWidth = 5;

            for (int i = 0; i < vodQuePointList.size(); i++) {
                int point = vodQuePointList.get(i) * 1000;

                CustomSeekBar.ProgressItem mProgressItem = new CustomSeekBar.ProgressItem();
                if (previousPointsSum != 0)
                    point = point - previousPointsSum;

                mProgressItem.progressItemPercentage = ((point * mProgress.getMax()) / duration);
                mProgressItem.color = blueColor;
                progressItemList.add(mProgressItem);

                spanElapsed = spanElapsed + mProgressItem.progressItemPercentage;

                mProgressItem = new CustomSeekBar.ProgressItem();
                mProgressItem.progressItemPercentage = adSlotWidth;
                mProgressItem.color = Color.RED;
                progressItemList.add(mProgressItem);

                spanElapsed = spanElapsed + mProgressItem.progressItemPercentage;

                previousPoint = point - adSlotWidth;

                previousPointsSum = previousPointsSum + previousPoint + adSlotWidth;
            }

            CustomSeekBar.ProgressItem mProgressItem = new CustomSeekBar.ProgressItem();
            mProgressItem.progressItemPercentage = mProgress.getMax() - spanElapsed;
            mProgressItem.color = blueColor;
            progressItemList.add(mProgressItem);
        }
        else { //If no cue point is there
            CustomSeekBar.ProgressItem mProgressItem = new CustomSeekBar.ProgressItem();
            mProgressItem.progressItemPercentage = mProgress.getMax();
            mProgressItem.color = blueColor;
            progressItemList.add(mProgressItem);
        }

        mProgress.initData(progressItemList);
        mProgress.invalidate();
    }

    public void setAspectRatio(int aspectRatio) {
        this.aspectRatio = aspectRatio;
        setAspectRatio();
    }

    public void setAspectRatio() {
        LayoutParams params = (LayoutParams) mSurfaceView.getLayoutParams();
        if (params != null) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            switch (aspectRatio) {
                case AspectRatioUtils.ASPECT_RATIO_STRETCH:
                    /*params.width = metrics.widthPixels;
                    params.height = metrics.heightPixels;*/
                    mAspectRatioButton.setImageDrawable(mContext.getResources()
                            .getDrawable(R.drawable.aspect_crop));
                    aspectRatio = AspectRatioUtils.ASPECT_RATIO_CROP;
                    break;
                case AspectRatioUtils.ASPECT_RATIO_CROP:
                    params.width = metrics.widthPixels;
                    params.height = metrics.heightPixels + 200;
                    mAspectRatioButton.setImageDrawable(mContext.getResources()
                            .getDrawable(R.drawable.aspect_100_percent));
                    aspectRatio = AspectRatioUtils.ASPECT_RATIO_HUNDERD_PERCENT;
                    break;
                case AspectRatioUtils.ASPECT_RATIO_HUNDERD_PERCENT:
                    params.width = videoOriginalWidth;
                    params.height = videoOriginalHeight;
                    mAspectRatioButton.setImageDrawable(mContext.getResources()
                            .getDrawable(R.drawable.aspect_fit_to_screen));
                    aspectRatio = AspectRatioUtils.ASPECT_RATIO_FIT_TO_SCREEN;
                    break;
                case AspectRatioUtils.ASPECT_RATIO_FIT_TO_SCREEN:
                    int width, height;
                    if (metrics.widthPixels < metrics.heightPixels) {
                        width = metrics.widthPixels;
                        height = (metrics.widthPixels / 3) * 4;
                    } else {
                        width = (metrics.heightPixels / 3) * 4;
                        height = metrics.heightPixels;
                    }
                    params.width = width;
                    params.height = height;
                    mAspectRatioButton.setImageDrawable(mContext.getResources()
                            .getDrawable(R.drawable.aspect_stretch));
                    aspectRatio = AspectRatioUtils.ASPECT_RATIO_STRETCH;
                    break;
            }
            params.gravity = Gravity.CENTER;
            mSurfaceView.setLayoutParams(params);
        }
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 3 seconds of inactivity.
     */
    public void show() {
        show(sDefaultTimeout);
    }

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked.
     * This requires the control interface to be a MediaPlayerControlExt
     */
    private void disableUnsupportedButtons() {
        if (mPlayer == null) {
            return;
        }

        try {
            if (mPauseButton != null && !mPlayer.canPause()) {
                mPauseButton.setEnabled(false);
            }
            if (mRewButton != null && !mPlayer.canSeekBackward()) {
                mRewButton.setEnabled(false);
            }
            if (mFfwdButton != null && !mPlayer.canSeekForward()) {
                mFfwdButton.setEnabled(false);
            }
        } catch (IncompatibleClassChangeError ex) {
            // We were given an old version of the interface, that doesn't have
            // the canPause/canSeekXYZ methods. This is OK, it just means we
            // assume the media can be paused and seeked, and so we don't disable
            // the buttons.
        }
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     *
     * @param timeout The timeout in milliseconds. Use 0 to show
     *                the controller until hide() is called.
     */
    public void show(int timeout) {
        if (!mShowing && mAnchor != null) {
            setProgress();
            setSeekBarColorOnBasisOfQuePoints(vodQuePointList);
            /*initDataToSeekbar();*/

            if (mPauseButton != null) {
                mPauseButton.requestFocus();
            }
            disableUnsupportedButtons();

            LayoutParams tlp = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM
            );

            mAnchor.addView(this, tlp);
            mShowing = true;
        }
        updatePausePlay();
        /*updateFullScreen();*/

        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = mHandler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
    }

    public boolean isShowing() {
        return mShowing;
    }

    /**
     * Remove the controller from the screen.
     */
    public void hide() {
        if (mAnchor == null) {
            return;
        }

        try {
            mAnchor.removeView(this);
            mHandler.removeMessages(SHOW_PROGRESS);
        } catch (IllegalArgumentException ex) {
            Log.w("MediaController", "already removed");
        }
        mShowing = false;
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }

        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null) {
            if (isShowRemainingDuration && duration >= position) //Showing only remaining duration
                mEndTime.setText("-" + stringForTime(duration - position));
            else
                mEndTime.setText(stringForTime(duration));
        }
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));

        return position;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        show(sDefaultTimeout);
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mPlayer == null) {
            return true;
        }

        int keyCode = event.getKeyCode();
//        Log.d(TAG, "" + keyCode);
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                show(sDefaultTimeout);
                if (mPauseButton != null) {
                    mPauseButton.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mPlayer.isPlaying()) {
                mPlayer.start();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mPlayer.isPlaying()) {
                mPlayer.pause();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }

        show(sDefaultTimeout);
        return super.dispatchKeyEvent(event);
    }

    private OnClickListener mPauseListener = new OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
            show(sDefaultTimeout);
        }
    };

/*    private OnClickListener mFullscreenListener = new OnClickListener() {
        public void onClick(View v) {
            if (mProgress.isShown())
                mProgress.setVisibility(GONE);
            else
                mProgress.setVisibility(VISIBLE);
            doToggleFullscreen();
            show(sDefaultTimeout);
        }
    };*/

    public void updatePausePlay() {
        if (mRoot == null || mPauseButton == null || mPlayer == null) {
            return;
        }

        if (mPlayer.isPlaying()) {
            mPauseButton.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            mPauseButton.setImageResource(android.R.drawable.ic_media_play);
        }
    }

  /*  public void updateFullScreen() {
        if (mRoot == null || mFullscreenButton == null || mPlayer == null) {
            return;
        }

        if (mPlayer.isFullScreen()) {
            mFullscreenButton.setImageResource(android.R.drawable.ic_menu_directions);
        } else {
            mFullscreenButton.setImageResource(android.R.drawable.ic_menu_rotate);
        }
    }*/

    private void doPauseResume() {
        if (mPlayer == null) {
            return;
        }

        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            cntrlers.pause();
            ;
        } else {
            mPlayer.start();
            cntrlers.play();
        }
        updatePausePlay();
    }

   /* private void doToggleFullscreen() {
        if (mPlayer == null) {
            return;
        }

        mPlayer.toggleFullScreen();
    }*/

    // There are two scenarios that can trigger the seekbar listener to trigger:
    //
    // The first is the user using the touchpad to adjust the posititon of the
    // seekbar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            mHandler.removeMessages(SHOW_PROGRESS);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (mPlayer == null) {
                return;
            }

            if (!fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = mPlayer.getDuration();
            long newposition = (duration * progress) / 1000L;
            mPlayer.seekTo((int) newposition);
            if (mCurrentTime != null)
                mCurrentTime.setText(stringForTime((int) newposition));
        }

        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setProgress();
            updatePausePlay();
            show(sDefaultTimeout);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };


    private OnSeekBarChangeListener mSeekBrightnessListner = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {

        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            /*android.provider.Settings.System.putInt(
                    mContext.getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS,
                    progress);*/
            float brightness = progress / (float) 255;
            WindowManager.LayoutParams windowLayoutParams = ((Activity) mContext).getWindow().getAttributes();
            windowLayoutParams.screenBrightness = brightness;
            ((Activity) mContext).getWindow().setAttributes(windowLayoutParams);

            utils.storeValue("Brightness", progress, mContext);
        }

        public void onStopTrackingTouch(SeekBar bar) {

        }
    };


    private OnSeekBarChangeListener mSeekVolume = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            audioManager
                    .setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            utils.storeValue("Volume", progress, mContext);
        }

        public void onStopTrackingTouch(SeekBar bar) {
        }
    };

    @Override
    public void setEnabled(boolean enabled) {
        if (mPauseButton != null) {
            mPauseButton.setEnabled(enabled);
        }
        if (mFfwdButton != null) {
            mFfwdButton.setEnabled(enabled);
        }
        if (mRewButton != null) {
            mRewButton.setEnabled(enabled);
        }
        if (mNextButton != null) {
            mNextButton.setEnabled(enabled && mNextListener != null);
        }
        if (mPrevButton != null) {
            mPrevButton.setEnabled(enabled && mPrevListener != null);
        }
        if (mProgress != null) {
            mProgress.setEnabled(enabled);
        }
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(VideoControllerView.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(VideoControllerView.class.getName());
    }

    private OnClickListener mRewListener = new OnClickListener() {
        public void onClick(View v) {
            if (mPlayer == null) {
                return;
            }

            int pos = mPlayer.getCurrentPosition();
            pos -= 5000; // milliseconds
            mPlayer.seekTo(pos);
            setProgress();

            show(sDefaultTimeout);
        }
    };

    private OnClickListener mFfwdListener = new OnClickListener() {
        public void onClick(View v) {
            if (mPlayer == null) {
                return;
            }

            int pos = mPlayer.getCurrentPosition();
            pos += 15000; // milliseconds
            mPlayer.seekTo(pos);
            setProgress();

            show(sDefaultTimeout);
        }
    };

    private void installPrevNextListeners() {
        if (mNextButton != null) {
            mNextButton.setOnClickListener(mNextListener);
            mNextButton.setEnabled(mNextListener != null);
        }

        if (mPrevButton != null) {
            mPrevButton.setOnClickListener(mPrevListener);
            mPrevButton.setEnabled(mPrevListener != null);
        }
    }

    public void setPrevNextListeners(OnClickListener next, OnClickListener prev) {
        mNextListener = next;
        mPrevListener = prev;
        mListenersSet = true;

        if (mRoot != null) {
            installPrevNextListeners();

            if (mNextButton != null && !mFromXml) {
                mNextButton.setVisibility(View.VISIBLE);
            }
            if (mPrevButton != null && !mFromXml) {
                mPrevButton.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface MediaPlayerControl {
        void start();

        void pause();

        int getDuration();

        int getCurrentPosition();

        void seekTo(int pos);

        boolean isPlaying();

        int getBufferPercentage();

        boolean canPause();

        boolean canSeekBackward();

        boolean canSeekForward();

        boolean isFullScreen();

        void toggleFullScreen();

        void selectResolution();
    }

    public interface ResolutionViewListener {
        void getResolutionView(ImageButton imageButton);
    }

    private static class MessageHandler extends Handler {
        private final WeakReference<VideoControllerView> mView;

        MessageHandler(VideoControllerView view) {
            mView = new WeakReference<VideoControllerView>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            VideoControllerView view = mView.get();
            if (view == null || view.mPlayer == null) {
                return;
            }

            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    view.hide();
                    break;
                case SHOW_PROGRESS:
                    pos = view.setProgress();
                    if (!view.mDragging && view.mShowing && view.mPlayer.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    }

    public void updateSystemConfig() {
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.d(TAG, "Current Volume is " + currentVolume);
        mVolume.setProgress(currentVolume);
        mVolume.setMax(audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC));

        float curBrightnessValue = utils.getValue("Brightness", mContext);
        if (curBrightnessValue != 0)
            mBrightness.setProgress(Math.round(curBrightnessValue));
        else
            mBrightness.setProgress(40);
        mBrightness.setMax(255);
        Log.d(TAG, "Current Brightness is " + curBrightnessValue);

       /* try {
            float curBrightnessValue = android.provider.Settings.System.getInt(
                    mContext.getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
            mBrightness.setProgress(Math.round(curBrightnessValue));
            mBrightness.setMax(255);
            Log.d(TAG, "Current Brightness is " + curBrightnessValue);
        } catch (Settings.SettingNotFoundException e) {
            android.provider.Settings.System.putInt(mContext.getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS, 40);
            mBrightness.setProgress(40);
        }*/

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int progress = mVolume.getProgress();
        Log.d(TAG, "Volume progress:  " + progress);
//        int progressGesture = mVolume.getProgress();

        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (progress < 15) {
                    mVolume.setProgress(progress + 1);
                    utils.storeValue("Volume", progress + 1, mContext);
//                    mVolume.setProgress(progressGesture + 1);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (progress > 0) {
                    mVolume.setProgress(progress - 1);
                    utils.storeValue("Volume", progress - 1, mContext);
//                    mVolume.setProgress(progressGesture - 1);
                }
                return true;
            default:
                return false;
        }
    }

    Runnable updateControlLayout = new Runnable() {
        @Override
        public void run() {
            mVolumeLayout.setVisibility(GONE);
            mVideoProgressLayout.setVisibility(VISIBLE);
            mBrightnessLayout.setVisibility(GONE);
        }
    };

    public interface Controlers {
        public void pause();

        public void play();
    }
}