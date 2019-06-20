package com.multitv.multitvcommonsdk.permission;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by naseeb on 8/4/2016.
 */

public class PermissionChecker {

    private static PermissionCheckerInterface permissionCheckerInterface;

    public static void setPermissionCheckerInterface(PermissionCheckerInterface permissionCheckerInterface) {
        PermissionChecker.permissionCheckerInterface = permissionCheckerInterface;
    }

    public static boolean checkSinglePermission(Activity activity, String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(activity, permission, requestCode);
            return false;
           /* // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(activity,
                    permission)) {

                return false;
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                requestPermissions(activity, permission, requestCode);
            }*/
        }

        return true;
    }

    public static  ArrayList<String> checkMultiplePermissions(Activity activity, ArrayList<String> permissionArrayList, int requestCode) {
        ArrayList<String> permissionsPermanentlyDenied = getListOfPermanentlyDeniedPermissions(activity, permissionArrayList);
        if(permissionArrayList.size() != 0) {
            String[] permissionsArray = permissionArrayList.toArray(new String[0]);
            ActivityCompat.requestPermissions(activity, permissionsArray,
                    requestCode);
        }

        return permissionsPermanentlyDenied;
    }

    private static  ArrayList<String> getListOfPermanentlyDeniedPermissions(Activity activity, ArrayList<String> permissionArrayList) {
        ArrayList<String> permissionsPermanentlyDenied = new ArrayList<>();

        for(int i = 0; i < permissionArrayList.size(); i++) {
            if (!shouldShowRequestPermissionRationale(activity, permissionArrayList.get(i))) {
                permissionsPermanentlyDenied.add(permissionArrayList.get(i));
                //permissionArrayList.remove(i);
            }
        }

        //Removing non mandatory permissions
        if(permissionsPermanentlyDenied.contains(PermissionId.PERMISSION_READ_PHONE_STATE)) {
            permissionsPermanentlyDenied.clear();
            permissionsPermanentlyDenied.add(PermissionId.PERMISSION_READ_PHONE_STATE);
        }

        return permissionsPermanentlyDenied;
    }

    private static boolean addPermission(Activity activity, ArrayList<String> permissionsNeeded, String permission
            , int requestCode) {
        if (!checkSinglePermission(activity, permission, requestCode)) {
            permissionsNeeded.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(activity, permission))
                return false;
        }

        return true;
    }

    private static boolean shouldShowRequestPermissionRationale(Activity activity, String permission) {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
            return false;
        else
            return true;
    }

    private static void requestPermissions(Activity activity, String permission, int requestCode){
        ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
    }

    public static void onRequestPermissionsResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        checkPermissionStatus(activity, requestCode, permissions, grantResults);
    }

    private static void checkPermissionStatus(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults != null &&  grantResults.length != 0) {
            if( PermissionChecker.permissionCheckerInterface != null)
                PermissionChecker.permissionCheckerInterface.permissionStatusSuccessful(requestCode, permissions, grantResults);
        } else {
            /*String message = "";
            switch (requestCode) {
                case PermissionId.PERMISSION_ACCESS_FINE_LOCATION_REQUEST_CODE:
                    message = "ACCESS_FINE_LOCATION";
                    break;
                case PermissionId.PERMISSION_ACCESS_COARSE_LOCATION_REQUEST_CODE:
                    message = "ACCESS_COARSE_LOCATION";
                    break;
                case PermissionId.PERMISSION_READ_PHONE_STATE_REQUEST_CODE:
                    message = "READ_PHONE_STATE";
                    break;
                case PermissionId.PERMISSION_WRITE_SETTINGS_REQUEST_CODE:
                    message = "WRITE_SETTINGS";
                    break;
            }

            if(!message.isEmpty()) {
                // Permission Denied
                *//*Toast.makeText(activity, message + " Denied", Toast.LENGTH_SHORT).show();*//*
            }*/

            PermissionChecker.permissionCheckerInterface.permissionStatusDenied(requestCode, permissions, grantResults);
        }
    }
}
