package com.multitv.multitvcommonsdk.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Point;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Display;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class DeviceInfo {
    private Context context;
    private String MY_PREF = "MYSDK_M";

    public DeviceInfo(Context ctx) {
        this.context = ctx;
    }

    public String getDeviceOtherDetail() {
        String returnString = "";
        TelephonyManager manager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String carrierName = manager.getNetworkOperatorName();
        PackageInfo pInfo;
        String version = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            version = pInfo.versionName;
/*        returnString = "{\"network_type\" : \""
                + NetowrkUtil.getNetworkInfo(context).getTypeName()
                + "\",\"network_provider\" : \"" + carrierName
                + "\",\"os_version\" :\"" + Build.VERSION.RELEASE
                + "\",\"app_version\" :\"" + version + "\"}";*/

            returnString = "{\"network_type\" : \""
                    + NetowrkUtil.getNetworkInfo(context).getTypeName()
                    + "\",\"network_provider\" : \"" + carrierName
                    + "\",\"os_version\" :\"" + Build.VERSION.RELEASE
                    + "\",\"app_version\" :\"" + version + "\",\"browser\" :\"application\"}";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnString;
    }

    public String getDeviceDetail() {
        ScreenUtils utils = new ScreenUtils();
        int[] dimension = utils.calculateScreenDimension(context);
        String returnString = "";

        try {
            returnString = "{\"make_model\" : \"" + getDeviceName()
                    + "\",\"os\" : \"" + Build.VERSION.RELEASE
                    + "\",\"screen_resolution\" : \"" + dimension[0] + "*"
                    + dimension[1] + "\",\"push_device_token\" : \"" + ""
                    + "\",\"device_type\" : \"" + Build.MANUFACTURER
                    + "\",\"platform\" : \"Android\",\"device_unique_id\" : \""
                    + getMACAddress("wlan0") + "\"}";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnString;
    }

    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        if (manufacturer.equalsIgnoreCase("HTC")) {
            return "HTC " + model;
        }
        return capitalize(manufacturer) + " " + model;
    }

    private String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        String phrase = "";
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c);
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase += c;
        }
        return phrase;
    }

    private static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections
                    .list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName))
                        continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac == null)
                    return "";
                StringBuilder buf = new StringBuilder();
                for (int idx = 0; idx < mac.length; idx++)
                    buf.append(String.format("%02X:", mac[idx]));
                if (buf.length() > 0)
                    buf.deleteCharAt(buf.length() - 1);
                return buf.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } // for now eat exceptions
        return "";
    }

    public String getDeviceResolution() {
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        String width = "" + size.x;
        String height = "" + size.y;

        return width + "x" + height;
    }

    public String getDeviceId() {
        TelephonyManager tManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String uid = tManager.getDeviceId();
        return uid;
    }

    public String getPrimaryAccountDetail() {
        Account[] accounts = AccountManager.get(context).getAccountsByType("com.google");
        String possibleEmail = null;

        for (Account account : accounts) {
            if (account.name != null && account.name.contains("gmail"))
                possibleEmail = account.name;

        }
        return possibleEmail;
    }

    public String getLatLong() {
        try {
            GPSTracker gps = new GPSTracker(context);
            return gps.getLongitude() + " " + gps.getLatitude();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void storeAppSessionID(String ID) {
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREF, context.MODE_PRIVATE).edit();
        editor.putString("sesionID", ID);
        editor.commit();
    }

    public String getAppSessionID() {
        SharedPreferences prefs = context.getSharedPreferences(MY_PREF, context.MODE_PRIVATE);
        String session = prefs.getString("sesionID", null);
        return session;
    }

    public void storeArrayInfo(String key, String[] value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREF, context.MODE_PRIVATE).edit();
        editor.putInt("array_size", value.length);
        for (int i = 0; i < value.length; i++)
            editor.putString(key + i, value[i]);
        editor.commit();
    }

    public String[] getArrayInfo(String key) {
        SharedPreferences prefs = context.getSharedPreferences(MY_PREF, context.MODE_PRIVATE);
        int size = prefs.getInt("array_size", 0);
        String[] array = new String[size];
        for (int i = 0; i < size; i++)
            array[i] = prefs.getString(key + i, null);

        return array;

    }
}
