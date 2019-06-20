package com.multitv.multitvcommonsdk.utils;

import android.content.Context;

import com.multitv.multitvcommonsdk.permission.PermissionId;

import org.json.JSONObject;

import github.nisrulz.easydeviceinfo.EasyAppMod;
import github.nisrulz.easydeviceinfo.EasyBatteryMod;
import github.nisrulz.easydeviceinfo.EasyBluetoothMod;
import github.nisrulz.easydeviceinfo.EasyConfigMod;
import github.nisrulz.easydeviceinfo.EasyCpuMod;
import github.nisrulz.easydeviceinfo.EasyDeviceMod;
import github.nisrulz.easydeviceinfo.EasyDisplayMod;
import github.nisrulz.easydeviceinfo.EasyIdMod;
import github.nisrulz.easydeviceinfo.EasyLocationMod;
import github.nisrulz.easydeviceinfo.EasyMemoryMod;
import github.nisrulz.easydeviceinfo.EasyNetworkMod;
import github.nisrulz.easydeviceinfo.EasyNfcMod;
import github.nisrulz.easydeviceinfo.EasySimMod;

/**
 * Created by naseeb on 8/30/2016.
 */

public class EasyDeviceInfo {
    private final String TAG = "EasyDeviceInfo";
    private Context context;

    public EasyDeviceInfo(Context context) {
     this.context = context;
    }

    public String createJson() {
        try {
            JSONObject rootJSONObject = new JSONObject();

            EasyAppMod easyAppMod = new EasyAppMod(context);
            JSONObject appModeJSONObject = new JSONObject();
            appModeJSONObject.put("activity_name", easyAppMod.getActivityName());
            appModeJSONObject.put("package_name", easyAppMod.getPackageName());
            appModeJSONObject.put("app_store", easyAppMod.getStore());
            appModeJSONObject.put("app_name", easyAppMod.getAppName());
            appModeJSONObject.put("app_version", easyAppMod.getAppVersion());
            appModeJSONObject.put("app_version_code", easyAppMod.getAppVersionCode());
            appModeJSONObject.put("is_permission_granted", easyAppMod.isPermissionGranted(PermissionId.PERMISSION_ACCESS_COARSE_LOCATION));
            rootJSONObject.put("appmode", appModeJSONObject);

            EasyBatteryMod easyBatteryMod = new EasyBatteryMod(context);
            JSONObject batteryModeJSONObject = new JSONObject();
            batteryModeJSONObject.put("battery_percentage", easyBatteryMod.getBatteryPercentage());
            batteryModeJSONObject.put("is_device_charging", easyBatteryMod.isDeviceCharging());
            batteryModeJSONObject.put("technology_used_by_battery", easyBatteryMod.getBatteryTechnology());
            batteryModeJSONObject.put("temperature", easyBatteryMod.getBatteryTemperature());
            batteryModeJSONObject.put("voltage", easyBatteryMod.getBatteryVoltage());
            batteryModeJSONObject.put("is_battery_present", easyBatteryMod.isBatteryPresent());
            rootJSONObject.put("batterymode", batteryModeJSONObject);

            EasyBluetoothMod easyBluetoothMod = new EasyBluetoothMod(context);
            JSONObject bluetoothModeJSONObject = new JSONObject();
            bluetoothModeJSONObject.put("bluetooth_mac", easyBluetoothMod.getBluetoothMAC());
            rootJSONObject.put("bluetoothmode", bluetoothModeJSONObject);

            EasyConfigMod easyConfigMod = new EasyConfigMod(context);
            JSONObject configModeJSONObject = new JSONObject();
            configModeJSONObject.put("is_running_on_emulator", easyConfigMod.isRunningOnEmulator());
            configModeJSONObject.put("time", easyConfigMod.getTime());
            configModeJSONObject.put("formatted_time", easyConfigMod.getFormattedTime());
            configModeJSONObject.put("up_time", easyConfigMod.getUpTime());
            configModeJSONObject.put("formatted_up_time", easyConfigMod.getFormattedUpTime());
            rootJSONObject.put("configmode", configModeJSONObject);

            EasyCpuMod easyCpuMod = new EasyCpuMod();
            JSONObject cpuModeJSONObject = new JSONObject();
            cpuModeJSONObject.put("supported_abis", easyCpuMod.getStringSupportedABIS());
            cpuModeJSONObject.put("supported_32_bit_abis", easyCpuMod.getStringSupported32bitABIS());
            cpuModeJSONObject.put("supported_64_bit_abis", easyCpuMod.getStringSupported64bitABIS());
            rootJSONObject.put("cpumode", cpuModeJSONObject);

            EasyDeviceMod easyDeviceMod = new EasyDeviceMod(context);
            JSONObject deviceModeJSONObject = new JSONObject();
            deviceModeJSONObject.put("imei", easyDeviceMod.getIMEI());
            deviceModeJSONObject.put("build_version_code_name", easyDeviceMod.getBuildVersionCodename());
            deviceModeJSONObject.put("build_version_incremental", easyDeviceMod.getBuildVersionIncremental());
            deviceModeJSONObject.put("build_version_sdk", easyDeviceMod.getBuildVersionSDK());
            deviceModeJSONObject.put("build_id", easyDeviceMod.getBuildID());
            deviceModeJSONObject.put("manufacturer", easyDeviceMod.getManufacturer());
            deviceModeJSONObject.put("model", easyDeviceMod.getModel());
            deviceModeJSONObject.put("os_codename", easyDeviceMod.getOSCodename());
            deviceModeJSONObject.put("os_version", easyDeviceMod.getOSVersion());
            deviceModeJSONObject.put("phone_number", easyDeviceMod.getPhoneNo());
            deviceModeJSONObject.put("radio_harware_version", easyDeviceMod.getRadioVer());
            deviceModeJSONObject.put("product", easyDeviceMod.getProduct());
            deviceModeJSONObject.put("device", easyDeviceMod.getDevice());
            deviceModeJSONObject.put("board", easyDeviceMod.getBoard());
            deviceModeJSONObject.put("hardware", easyDeviceMod.getHardware());
            deviceModeJSONObject.put("boot_loader", easyDeviceMod.getBootloader());
            deviceModeJSONObject.put("fingerprint", easyDeviceMod.getFingerprint());
            deviceModeJSONObject.put("is_device_rooted", easyDeviceMod.isDeviceRooted());
            deviceModeJSONObject.put("build_brand", easyDeviceMod.getBuildBrand());
            deviceModeJSONObject.put("build_host", easyDeviceMod.getBuildHost());
            deviceModeJSONObject.put("build_tags", easyDeviceMod.getBuildTags());
            deviceModeJSONObject.put("build_time", easyDeviceMod.getBuildTime());
            rootJSONObject.put("devicemode", deviceModeJSONObject);

            EasyDisplayMod easyDisplayMod = new EasyDisplayMod(context);
            JSONObject displayModeJSONObject = new JSONObject();
            displayModeJSONObject.put("display_resolution", easyDisplayMod.getResolution());
            displayModeJSONObject.put("screen_density", easyDisplayMod.getDensity());
            //displayModeJSONObject.put("display_xy_coordinate", easyDisplayMod.getDisplayXYCoordinates(null));
            rootJSONObject.put("displaymode", displayModeJSONObject);

            EasyIdMod easyIdMod = new EasyIdMod(context);
            JSONObject idModeJSONObject = new JSONObject();
            idModeJSONObject.put("pseudo_id", easyIdMod.getPseudoUniqueID());
            idModeJSONObject.put("android_id", easyIdMod.getAndroidID());
            rootJSONObject.put("idmode", idModeJSONObject);

            EasyLocationMod easyLocationMod = new EasyLocationMod(context);
            JSONObject locationModeJSONObject = new JSONObject();
            double[] location = easyLocationMod.getLatLong();
            locationModeJSONObject.put("latitude", String.valueOf(location[0]));
            locationModeJSONObject.put("longitude", String.valueOf(location[1]));
            rootJSONObject.put("locationmode", locationModeJSONObject);

            EasyMemoryMod easyMemoryMod = new EasyMemoryMod(context);
            JSONObject memoryModeJSONObject = new JSONObject();
            memoryModeJSONObject.put("total_ram", easyMemoryMod.getTotalRAM());
            memoryModeJSONObject.put("available_internal_memory", easyMemoryMod.getAvailableInternalMemorySize());
            memoryModeJSONObject.put("available_external_memory", easyMemoryMod.getAvailableExternalMemorySize());
            memoryModeJSONObject.put("total_internal_memory", easyMemoryMod.getTotalInternalMemorySize());
            memoryModeJSONObject.put("total_external_memory", easyMemoryMod.getTotalExternalMemorySize());
            rootJSONObject.put("memorymode", memoryModeJSONObject);

            EasyNetworkMod easyNetworkMod = new EasyNetworkMod(context);
            JSONObject networkModeJSONObject = new JSONObject();
            networkModeJSONObject.put("network_available", easyNetworkMod.isNetworkAvailable());
            networkModeJSONObject.put("wifi_enabled", easyNetworkMod.isWifiEnabled());
            networkModeJSONObject.put("ip_v4_address", easyNetworkMod.getIPv4Address());
            networkModeJSONObject.put("ip_v6_address", easyNetworkMod.getIPv6Address());
            rootJSONObject.put("networkmode", networkModeJSONObject);

            EasyNfcMod easyNfcMod = new EasyNfcMod(context);
            JSONObject nfcModeJSONObject = new JSONObject();
            nfcModeJSONObject.put("is_nfc_present", easyNfcMod.isNfcPresent());
            nfcModeJSONObject.put("is_nfc_enabled", easyNfcMod.isNfcEnabled());
            rootJSONObject.put("nfcmode", nfcModeJSONObject);

            EasySimMod easySimMod = new EasySimMod(context);
            JSONObject simModeJSONObject = new JSONObject();
            simModeJSONObject.put("imsi", easySimMod.getIMSI());
            simModeJSONObject.put("sim_serial_number", easySimMod.getSIMSerial());
            simModeJSONObject.put("country", easySimMod.getCountry());
            simModeJSONObject.put("carrier", easySimMod.getCarrier());
            simModeJSONObject.put("sim_locked", easySimMod.isSimNetworkLocked());
            rootJSONObject.put("simmode", simModeJSONObject);

            return rootJSONObject.toString();
        } catch (Exception e) {
            ToastMessage.showLogs(ToastMessage.LogType.ERROR, TAG, e.getMessage());
        }

        return null;
    }
}
