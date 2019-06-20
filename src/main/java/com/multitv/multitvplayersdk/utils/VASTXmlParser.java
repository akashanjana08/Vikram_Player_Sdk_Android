package com.multitv.multitvplayersdk.utils;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import com.multitv.multitvcommonsdk.utils.ToastMessage;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of VAST 2.0 XML Parser using XmlPullParser. The parser finds
 * all trackings, settings and the actual mediafile. If the plain data contains
 * a wrapped VAST xml, it is fetched by triggering a callback on the UI thread
 * which should fetch the additional file.
 * <p/>
 * Wrapped VAST xml trackings and original trackings will be combined, i.e. you
 * will receive a list of URLs for all trackings.
 *
 * @author stein16
 */
public class VASTXmlParser {

    private class MediaFile {

        public int w;

        public int h;

        public int bitrate;

        public String url;

        public MediaFile(String url, int b, int w, int h) {
            this.w = w;
            this.h = h;
            this.bitrate = b;
            this.url = url;
        }
    }

    /**
     * Simple bean to hold tracking URLs for various VAST events
     *
     * @author stein16
     */
    public class Tracking {

        /**
         * The player has closed
         */
        public final static int EVENT_FINAL_RETURN = 0;

        /**
         * The XML has loaded
         */
        public final static int EVENT_IMPRESSION = 1;

        /**
         * The video starts
         */
        public final static int EVENT_START = 2;

        /**
         * 25% of the video have player
         */
        public final static int EVENT_FIRSTQ = 3;

        /**
         * 50% of the video have played
         */
        public final static int EVENT_MID = 4;

        /**
         * 75% of the video have played
         */
        public final static int EVENT_THIRDQ = 5;

        /**
         * 100% of the video have played
         */
        public final static int EVENT_COMPLETE = 6;

        /**
         * Sound of player was muted
         */
        public final static int EVENT_MUTE = 7;

        /**
         * Sound of player was unmuted
         */
        public final static int EVENT_UNMUTE = 8;

        /**
         * Video was paused
         */
        public final static int EVENT_PAUSE = 9;

        /**
         * Video was resumed
         */
        public final static int EVENT_RESUME = 10;

        /**
         * Player went fullscreen
         */
        public final static int EVENT_FULLSCREEN = 11;

        /**
         * Mapping of event descriptions in VAST xml to internal names
         */
        private final String[] EVENT_MAPPING = new String[]{"finalReturn",
                "impression", "start", "firstQuartile", "midpoint",
                "thirdQuartile", "complete", "mute", "unmute", "pause",
                "resume", "fullscreen"};

        private int event;

        private String url;

        /**
         * Create a new bean for pinging tracking URLs upon specific VAST events
         *
         * @param e   VAST Event name
         * @param url Tracking URL
         */
        private Tracking(String e, String url) {
            this.event = findEvent(e);
            this.url = url;
        }

        private int findEvent(String event) {
            for (int i = 0; i < EVENT_MAPPING.length; i++) {
                if (EVENT_MAPPING[i].equals(event)) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * Get the static integer as internal representation of the VAST event
         *
         * @return Internal integer for the event
         */
        public int getEvent() {
            return this.event;
        }

        /**
         * Get the tracking url associated with this event
         *
         * @return Tracking url
         */
        public String getUrl() {
            return this.url;
        }

    }

    /**
     * Interface providing method to be executed when a wrapper was found within
     * VAST xml and XML was completely parsed
     *
     * @author stein16
     */
    public interface VASTXmlListener {

        /**
         * Listener method for vast parsing to be complete
         *
         * @param vast initialized vast object
         */
        public void onVASTReady(VASTXmlParser vast, boolean autoStart);

        /**
         * Listener method for wrapped VAST xml
         *
         * @param url URL of the wrapped VAST xml
         */
        public void onVASTWrapperFound(String url);
    }

    private Context context;

    private VASTXmlListener vastListener;

    private final static String TAG = "VASTXmlParser";

    private final static String VAST_ADTAGURI_TAG = "VASTAdTagURI";

    private final static String VAST_START_TAG = "VAST";

	/* private final static String VAST_START_TAG = "VideoAdServingTemplate"; */

    private final static String VAST_AD_TAG = "Ad";

    private final static String VAST_INLINE_TAG = "InLine";

    private final static String VAST_WRAPPER_TAG = "Wrapper";

    private final static String VAST_IMPRESSION_TAG = "Impression";

    private final static String VAST_CREATIVES_TAG = "Creatives";

    private final static String VAST_CREATIVE_TAG = "Creative";

    private final static String VAST_LINEAR_TAG = "Linear";

    private final static String VAST_DURATION_TAG = "Duration";

    private final static String VAST_TRACKINGEVENTS_TAG = "TrackingEvents";

    private final static String VAST_TRACKING_TAG = "Tracking";

    private final static String VAST_MEDIAFILES_TAG = "MediaFiles";

    private final static String VAST_MEDIAFILE_TAG = "MediaFile";

    private final static String VAST_VIDEOCLICKS_TAG = "VideoClicks";

    private final static String VAST_CLICKTHROUGH_TAG = "ClickThrough";

    private final static String VAST_CLICKTRACKING_TAG = "ClickTracking";

    private boolean ready, autoStartPlayer;

    private volatile boolean hasWrapper;

    private volatile VASTXmlParser wrappedVASTXml;

    private String clickThroughUrl;

    private String clickTrackingUrl;

    private int skipOffset;

    private String impressionTrackerUrl;

    private String duration;

    private String mediaFileUrl;

    private List<Tracking> trackings;

    private boolean isInWrapper;

    private List<String> urls = new ArrayList<String>();

    /**
     * Constructor for simple VAST parser
     *
     * @param c        Android Application context
     * @param listener Listener for VASTWrapper callbacks
     * @param data     data of the initial VAST response/file
     */

    public VASTXmlParser(Context c, VASTXmlListener listener, String data,
                         boolean bool) {
        isInWrapper = false;
        this.hasWrapper = false;
        this.trackings = new ArrayList<Tracking>();
        this.ready = false;
        this.context = c;
        this.autoStartPlayer = bool;
        this.vastListener = listener;
        /*
         * if (SdkUtil.getContext() == null) { SdkUtil.setContext(context); }
		 */
        try {
            readVAST(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.ready = true;
        if (!isInWrapper) {
            if (listener != null) {
                listener.onVASTReady(this, autoStartPlayer);
            }
        }
    }

    /**
     * Get URL to load upon click on player
     *
     * @return Target URL for clicks on player
     */
    public String getClickThroughUrl() {
        waitForWrapper();

        String url = this.clickThroughUrl;
        ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, url);
        if (url == null && wrappedVASTXml != null) {
            url = wrappedVASTXml.getClickThroughUrl();
        }

        return url;
    }

    /**
     * Tracking URLs for clickthru event
     *
     * @return List of clicktracking URLs
     */
    public List<String> getClickTrackingUrl() {
        waitForWrapper();

        List<String> urls = new ArrayList<String>();
        if (this.clickTrackingUrl != null) {
            urls.add(this.clickTrackingUrl);
        }
        if (wrappedVASTXml != null) {
            urls.addAll(wrappedVASTXml.getClickTrackingUrl());
        }

        return urls;
    }

    /**
     * Get duration specified in URLs
     *
     * @return String containing duration
     */
    public String getDuration() {
        waitForWrapper();

        if (duration == null && wrappedVASTXml != null) {
            return wrappedVASTXml.getDuration();
        }
        return duration;
    }

    /**
     * Get a list of all impression tracking URLs
     *
     * @return List of impression tracking URLs
     */
    public List<String> getImpressionTrackerUrl() {
        waitForWrapper();

        if (wrappedVASTXml != null) {
            urls.addAll(wrappedVASTXml.getImpressionTrackerUrl());
        }

        return urls;
    }

    /**
     * Get URL of actual media file
     *
     * @return Mediafile URL string
     */
    public String getMediaFileUrl() {
        waitForWrapper();

        if (mediaFileUrl == null && wrappedVASTXml != null) {
            return wrappedVASTXml.getMediaFileUrl();
        }
        return mediaFileUrl;
    }

    /**
     * Get time until skip button should be shown
     *
     * @return Integer defining time in millis until skip button should be shown
     */
    public int getSkipOffset() {
        waitForWrapper();

        if (skipOffset <= 0 && wrappedVASTXml != null) {
            return wrappedVASTXml.getSkipOffset();
        }
        return skipOffset;
    }

    /**
     * Get a list of tracking URLs for a specific event
     *
     * @param type VAST event
     * @return List of tracking URLs for event
     */
    public List<String> getTrackingByType(int type) {
        waitForWrapper();

        Iterator<Tracking> i = this.trackings.iterator();
        List<String> urls = new ArrayList<String>();
        while (i.hasNext()) {
            Tracking t = i.next();
            if (t.getEvent() == type) {
                urls.add(t.getUrl());
            }
        }
        if (wrappedVASTXml != null) {
            urls.addAll(wrappedVASTXml.getTrackingByType(type));
        }
        return urls;
    }

    /**
     * Get a list of all available tracking beans
     *
     * @return List of tracking beans
     */
    public List<Tracking> getTrackings() {
        waitForWrapper();

        List<Tracking> t = trackings;
        if (wrappedVASTXml != null) {
            t.addAll(wrappedVASTXml.getTrackings());
        }
        return t;
    }

    private void getWrappedVast(XmlPullParser p) throws IOException,
            XmlPullParserException {
        p.require(XmlPullParser.START_TAG, null, VAST_ADTAGURI_TAG);
        String url = readText(p);
        p.require(XmlPullParser.END_TAG, null, VAST_ADTAGURI_TAG);
        isInWrapper = true;

        if (vastListener != null) {
            // Log.d(TAG, "Notifying VAST listener of new location " + url);
            vastListener.onVASTWrapperFound(url);
        } else {
            // Log.e(TAG, "No listener set for wrapped VAST xml.");
        }

    }

    /**
     * @return null if no wrapped XML is present, a reference to a wrapped
     * VASTXmlParse if it is
     */
    public VASTXmlParser getWrappedVASTXml() {
        return this.wrappedVASTXml;
    }

    /**
     * @return true if VAST XML contains wrapped VAST
     */
    public boolean hasWrapper() {
        return hasWrapper;
    }

    /**
     * Determine whether the contents of a wrapped VAST XML have been loaded
     *
     * @return true if wrapped XML is loaded
     */
    public synchronized boolean isReady() {
        waitForWrapper();

        return ready
                && (wrappedVASTXml != null ? wrappedVASTXml.isReady()
                : !hasWrapper);
    }

    private void readAd(XmlPullParser p) throws IOException,
            XmlPullParserException {
        p.require(XmlPullParser.START_TAG, null, VAST_AD_TAG);
        while (p.next() != XmlPullParser.END_TAG) {
            if (p.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = p.getName();
            if (name.equals(VAST_INLINE_TAG)) {
                // Log.i(TAG, "VAST file contains inline ad information.");
                readInLine(p);
            }
            if (name.equals(VAST_WRAPPER_TAG)) {
                // Log.i(TAG, "VAST file contains wrapped ad information. ["
                // + this + "]");
                hasWrapper = true;
                readWrapper(p);
            }
        }
    }

    private void readCreative(XmlPullParser p) throws IOException,
            XmlPullParserException {
        p.require(XmlPullParser.START_TAG, null, VAST_CREATIVE_TAG);
        while (p.next() != XmlPullParser.END_TAG) {
            if (p.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = p.getName();
            if (name != null && name.equals(VAST_LINEAR_TAG)) {
                String skipoffsetStr = p.getAttributeValue(null, "skipoffset");
                if (skipoffsetStr != null && skipoffsetStr.indexOf(":") < 0) {
                    skipOffset = Integer.parseInt(skipoffsetStr.substring(0,
                            skipoffsetStr.length() - 1));
                    // Log.i(TAG, "Linear skipoffset is " + skipOffset +
                    // " [%]");
                } else if (skipoffsetStr != null
                        && skipoffsetStr.indexOf(":") >= 0) {
                    skipOffset = -1;
                    // Log.w(TAG,
                    // "Absolute time value ignored for skipOffset in VAST xml. Only percentage values will pe parsed.");
                }
                readLinear(p);
            } else {
                skip(p);
            }
        }
    }

    private void readCreatives(XmlPullParser p) throws IOException,
            XmlPullParserException {
        p.require(XmlPullParser.START_TAG, null, VAST_CREATIVES_TAG);
        while (p.next() != XmlPullParser.END_TAG) {
            if (p.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = p.getName();
            if (name != null && name.equals(VAST_CREATIVE_TAG)) {
                readCreative(p);
            } else {
                skip(p);
            }
        }
    }

    private void readInLine(XmlPullParser p) throws IOException,
            XmlPullParserException {
        p.require(XmlPullParser.START_TAG, null, VAST_INLINE_TAG);
        while (p.next() != XmlPullParser.END_TAG) {
            if (p.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = p.getName();
            if (name != null && name.equals(VAST_IMPRESSION_TAG)) {
                p.require(XmlPullParser.START_TAG, null, VAST_IMPRESSION_TAG);
                this.impressionTrackerUrl = readText(p);
                urls.add(this.impressionTrackerUrl);
                // if (impression != null && impression.contains("vdopia"))
                // this.impressionTrackerUrl = impression;
//                System.out.println("Impression is:::::::::::"
//                        + this.impressionTrackerUrl);
                p.require(XmlPullParser.END_TAG, null, VAST_IMPRESSION_TAG);

                // Log.d(TAG, "Impression tracker url: "
                // + this.impressionTrackerUrl);
            } else if (name != null && name.equals(VAST_CREATIVES_TAG)) {
                readCreatives(p);
            } else {
                skip(p);
            }
        }
    }

    private void readLinear(XmlPullParser p) throws IOException,
            XmlPullParserException {
        p.require(XmlPullParser.START_TAG, null, VAST_LINEAR_TAG);
        while (p.next() != XmlPullParser.END_TAG) {
            String name = p.getName();
            if (p.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            if (name != null && name.equals(VAST_DURATION_TAG)) {
                p.require(XmlPullParser.START_TAG, null, VAST_DURATION_TAG);
                this.duration = readText(p);
                p.require(XmlPullParser.END_TAG, null, VAST_DURATION_TAG);

                // Log.d(TAG, "Video duration: " + this.duration);
            } else if (name != null && name.equals(VAST_TRACKINGEVENTS_TAG)) {
                readTrackingEvents(p);
            } else if (name != null && name.equals(VAST_MEDIAFILES_TAG)) {
                readMediaFiles(p);
            } else if (name != null && name.equals(VAST_VIDEOCLICKS_TAG)) {
                readVideoClicks(p);
//                skip(p);
            } else {
                skip(p);
            }
        }
    }

    private void readMediaFiles(XmlPullParser p) throws IOException,
            XmlPullParserException {
        List<MediaFile> files = null;
        p.require(XmlPullParser.START_TAG, null, VAST_MEDIAFILES_TAG);
        while (p.next() != XmlPullParser.END_TAG) {
            if (p.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = p.getName();
            if (name != null && name.equals(VAST_MEDIAFILE_TAG)) {

                p.require(XmlPullParser.START_TAG, null, VAST_MEDIAFILE_TAG);
                String mimeType = p.getAttributeValue(null, "type");
                String bitrate = p.getAttributeValue(null, "bitrate");
                String width = p.getAttributeValue(null, "width");
                String height = p.getAttributeValue(null, "height");
                String url = readText(p).replaceAll("&amp;", "&")
                        .replaceAll("&lt;", "<").replaceAll("&gt;", ">");

                if (mimeType != null && "video/mp4".equals(mimeType)) {
                    if (files != null && files.size() > 0) {
                        if (files.get(0).bitrate > Integer
                                .valueOf(bitrate))
                            files = new ArrayList<MediaFile>();
                    } else {
                        files = new ArrayList<MediaFile>();
                    }
                    files.add(new MediaFile(url, bitrate != null ? Integer
                            .valueOf(bitrate) : 0, width != null ? Integer
                            .valueOf(width) : 0, height != null ? Integer
                            .valueOf(height) : 0));
                }

                p.require(XmlPullParser.END_TAG, null, VAST_MEDIAFILE_TAG);
            } else {
                skip(p);
            }
        }
        if (files.size() == 1) {
            // Log.d(TAG,
            // "Found 1 mediafile: " + files.get(0).url + " "
            // + files.get(0).w + "x" + files.get(0).h + "@"
            // + files.get(0).bitrate);
            Log.d(TAG, files.size() + "::::" + files.get(0).bitrate);
            this.mediaFileUrl = files.get(0).url;
        } else if (files.size() > 1) {

			/*
             * this.mediaFileUrl = files.get(0).url;
			 * 
			 * int limit = SdkUtil.isWifi() ? 1000 : (SdkUtil.is3G() ||
			 * SdkUtil.is4G() ? 600 : 0); int select = -1;
			 * 
			 * for (int i = 0; i < files.size(); i++) { Log.d(TAG, "Found " +
			 * files.get(i).url + " " + files.get(i).w + "x" + files.get(i).h +
			 * "@" + files.get(i).bitrate); if (files.get(i).bitrate != 0 &&
			 * files.get(i).bitrate <= limit) { if (select >= 0 &&
			 * files.get(select).bitrate >= files.get(i).bitrate) { //
			 * Log.d(TAG, "Keeping " + files.get(select).bitrate // +
			 * " as chosen bitrate"); } else { select = i; } } else { select =
			 * i; } }
			 */

            // Log.d(TAG,
            // "Selected " + files.get(select).url + " "
            // + files.get(select).w + "x" + files.get(select).h
            // + "@" + files.get(select).bitrate);
            Log.d(TAG, files.size() + "::::" + files.get(0).bitrate);
            this.mediaFileUrl = files.get(0).url;
            // Log.d(TAG, "Media file url is :: " + this.mediaFileUrl);
        } else {
            // Log.w(TAG, "No compatible mediafile found.");
        }

    }

    private String readText(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        } else {
            Log.w(TAG, "No text: " + parser.getName());
        }
        return result.trim();
    }

    private void readTrackingEvents(XmlPullParser p) throws IOException,
            XmlPullParserException {
        p.require(XmlPullParser.START_TAG, null, VAST_TRACKINGEVENTS_TAG);
        while (p.next() != XmlPullParser.END_TAG) {
            if (p.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = p.getName();
            if (name != null && name.equals(VAST_TRACKING_TAG)) {
                String ev = p.getAttributeValue(null, "event");
                p.require(XmlPullParser.START_TAG, null, VAST_TRACKING_TAG);
                this.trackings.add(new Tracking(ev, readText(p)));
                // Log.d(TAG, "Added VAST tracking \"" + ev + "\"");
                p.require(XmlPullParser.END_TAG, null, VAST_TRACKING_TAG);
            } else {
                skip(p);
            }
        }
    }

    private void readVAST(String data) throws XmlPullParserException,
            IOException {

        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(new StringReader(data));
        parser.nextTag();
        parser.require(XmlPullParser.START_TAG, null, VAST_START_TAG);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            if (parser.getName().equals(VAST_AD_TAG)) {
                readAd(parser);
            }
        }
    }

    private void readVideoClicks(XmlPullParser p) throws IOException,
            XmlPullParserException {
        p.require(XmlPullParser.START_TAG, null, VAST_VIDEOCLICKS_TAG);
        while (p.next() != XmlPullParser.END_TAG) {
            if (p.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = p.getName();
            if (name != null && name.equals(VAST_CLICKTHROUGH_TAG)) {
                p.require(XmlPullParser.START_TAG, null, VAST_CLICKTHROUGH_TAG);
                this.clickThroughUrl = readText(p);
                // Log.d(TAG, "Video clickthrough url: " + clickThroughUrl);
                p.require(XmlPullParser.END_TAG, null, VAST_CLICKTHROUGH_TAG);
            } else if (name != null && name.equals(VAST_CLICKTRACKING_TAG)) {
                p.require(XmlPullParser.START_TAG, null, VAST_CLICKTRACKING_TAG);
                this.clickTrackingUrl = readText(p);
                // Log.d(TAG, "Video clicktracking url: " + clickThroughUrl);
                p.require(XmlPullParser.END_TAG, null, VAST_CLICKTRACKING_TAG);
            } else {
                skip(p);
            }
        }
    }

    private void readWrapper(XmlPullParser p) throws IOException,
            XmlPullParserException {
        p.require(XmlPullParser.START_TAG, null, VAST_WRAPPER_TAG);
        while (p.next() != XmlPullParser.END_TAG) {
            if (p.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = p.getName();
            if (name != null && name.equals(VAST_IMPRESSION_TAG)) {
                p.require(XmlPullParser.START_TAG, null, VAST_IMPRESSION_TAG);
                this.impressionTrackerUrl = readText(p);
                p.require(XmlPullParser.END_TAG, null, VAST_IMPRESSION_TAG);

                // Log.d(TAG, "Impression tracker url: "
                // + this.impressionTrackerUrl);
            } else if (name != null && name.equals(VAST_CREATIVES_TAG)) {
                readCreatives(p);
            } else if (name != null && name.equals(VAST_ADTAGURI_TAG)) {
                getWrappedVast(p);
            } else {
                skip(p);
            }
        }
    }

    /**
     * Set the additional parser for wrapped VAST xml
     *
     * @param vastXml the parser for wrapped VAST xml
     */
    public void setWrapper(VASTXmlParser vastXml) {
        hasWrapper = true;
        this.wrappedVASTXml = vastXml;
        // Log.d(TAG, "Setting wrapper for " + this + " to " + vastXml);
    }

    private void skip(XmlPullParser p) throws XmlPullParserException,
            IOException {
        if (p.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (p.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private void waitForWrapper() {
        if (!hasWrapper) {
            return;
        }
        // TODO define maximum time to wait
        // while (true) {
        // if (hasWrapper
        // && (wrappedVASTXml == null || !wrappedVASTXml.isReady())) {
        // try {
        // Log.d(TAG, "Executing worker thread");
        // Thread.sleep(750);
        // } catch (Exception e) {
        // Log.e(TAG, "Error wraiting for wrapper", e);
        // }
        // Thread.yield();
        // } else {
        // return;
        // }
        // }
    }

}
