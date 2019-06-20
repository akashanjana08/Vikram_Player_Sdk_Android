package com.multitv.multitvcommonsdk.permission;

/**
 * Created by naseeb on 8/8/2016.
 */

public interface PermissionCheckerInterface {
    void permissionStatusSuccessful(int requestCode, String[] permissions, int[] grantResults);
    void permissionStatusDenied(int requestCode, String[] permissions, int[] grantResults);
}
