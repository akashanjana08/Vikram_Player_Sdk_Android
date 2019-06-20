package com.multitv.multitvcommonsdk;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.multitv.multitvcommonsdk.utils.Constant;
import com.multitv.multitvcommonsdk.utils.DeviceInfo;
import com.multitv.multitvcommonsdk.utils.ToastMessage;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ServerSyncService extends Service {

    private String TAG = "ServerSyncService";

    // private String PING_URL;
    private String previousValue = "";
    // private Thread thread;
    // private HttpUtils httpCLient;
    private boolean test = false;
    // private boolean executeTask = true;
    private String sessionID, channelID, channelToken;
    private Socket socketio;

    private static boolean textBool = false;

	/*
     * public ServerSyncService() { super("ServerSyncService"); }
	 */

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        try {
            if (intent != null) {
                sessionID = intent.getStringExtra("SESSION_ID");
                channelID = intent.getStringExtra("CHANNEL_ID");
                channelToken = intent.getStringExtra("CHANNEL_TOKEN");
                if (!test) {
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            IO.Options opts = new IO.Options();
                            opts.forceNew = true;
                            try {
                                opts.query = "name="
                                        + channelToken
                                        + "&nm="
                                        + new DeviceInfo(ServerSyncService.this)
                                        .getDeviceId();
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            }

                            try {
                                socketio = IO.socket(Constant.getInstance()
                                        .getBroadcastServerUrl(), opts);
                                ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG,
                                        "Service URL: " + Constant.getInstance()
                                        .getBroadcastServerUrl() + opts.query);
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            }
                            socketio.on(Socket.EVENT_CONNECT_ERROR,
                                    onConnectError);
                            socketio.on(Socket.EVENT_CONNECT, onConnect);
                            socketio.on(Socket.EVENT_CONNECT_TIMEOUT,
                                    onConnectError);
                            socketio.on("awd", onMessageReceived);
                            socketio.connect();
                        }
                    }).start();
                } else {

                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            while (true)
                                if (textBool) {
                                    textBool = false;
                                    sendResultMessage("MP");
                                    try {
                                        Thread.sleep(70000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    textBool = true;
                                    sendResultMessage("AD");
                                    try {
                                        Thread.sleep(70000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

                        }
                    }).start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Service.START_STICKY;
    }

	/*
     * @Override protected void onHandleIntent(Intent intent) {
	 * 
	 * }
	 */

    @Override
    public void onDestroy() {
        if (socketio != null && socketio.connected()) {
            socketio.disconnect();
            socketio.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
            socketio.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            socketio.off(Socket.EVENT_CONNECT, onConnect);
            socketio = null;
            previousValue = "";
        }
        super.onDestroy();
        ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "Socket disconnected");
        ToastMessage.showToastMsg(ServerSyncService.this, "Service Killed", Toast.LENGTH_SHORT);
        // executeTask = false;
    }

    private void sendResultMessage(String data) {
        Log.d(TAG, "Broadcasting result message: " + data);
        Intent intent = new Intent("Multi_Tv_Filter");
        intent.putExtra("VALUE", data);
        intent.putExtra("SESSION_ID", sessionID);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "Socket connection error");
        }
    };
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "Socket connected");
        }
    };

    private Emitter.Listener onMessageReceived = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            String currentProgramInfo = (String) args[0];

            ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG,
                    "Socket current program info : " + currentProgramInfo);

            if (currentProgramInfo != null) {
                if (previousValue != null
                        && !previousValue.equalsIgnoreCase(currentProgramInfo)) {

                    previousValue = currentProgramInfo;
                    sendResultMessage(currentProgramInfo);
                }
            }
        }
    };

}