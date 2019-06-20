package com.multitv.multitvcommonsdk.utils;

import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewConfiguration;

public class ScreenUtils {

	@SuppressLint("NewApi")
	public int[] calculateScreenDimension(Context mContext) {

		int[] dimension = new int[2];

		Display display = ((Activity) mContext).getWindowManager()
				.getDefaultDisplay();
		if (Build.VERSION.SDK_INT >= 17) {

			if (ViewConfiguration.get(mContext).hasPermanentMenuKey()) {
				DisplayMetrics realMetrics = new DisplayMetrics();
				display.getRealMetrics(realMetrics);
				dimension[0] = realMetrics.heightPixels;
				dimension[1] = realMetrics.widthPixels;
			} else {
				DisplayMetrics realMetrics = new DisplayMetrics();
				display.getRealMetrics(realMetrics);
				int screenHeight = realMetrics.heightPixels, screenWidth = realMetrics.widthPixels;
				int result = 0;
				int resourceId = mContext.getResources().getIdentifier(
						"status_bar_height", "dimen", "android");
				if (resourceId > 0)
					result = mContext.getResources().getDimensionPixelSize(
							resourceId);
				screenHeight -= result;
				result = 0;
				if (screenHeight >= screenWidth)
					resourceId = mContext.getResources().getIdentifier(
							"navigation_bar_height", "dimen", "android");
				else
					resourceId = mContext.getResources().getIdentifier(
							"navigation_bar_height_landscape", "dimen",
							"android");
				if (resourceId > 0)
					result = mContext.getResources().getDimensionPixelSize(
							resourceId);
				screenHeight -= result;
				System.out.println("Screenn height=======" + screenHeight);

				if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
					dimension[0] = screenHeight;
					dimension[1] = realMetrics.widthPixels;
				} else {
					dimension[0] = realMetrics.heightPixels;
					dimension[1] = realMetrics.widthPixels;
				}
			}

		} else if (Build.VERSION.SDK_INT >= 14) {
			try {
				Method mGetRawH = Display.class.getMethod("getRawHeight");
				Method mGetRawW = Display.class.getMethod("getRawWidth");
				dimension[0] = (Integer) mGetRawH.invoke(display);
				dimension[1] = (Integer) mGetRawW.invoke(display);
			} catch (Exception e) {
				dimension[0] = display.getHeight();
				dimension[1] = display.getWidth();
			}
		} else {
			dimension[0] = display.getHeight();
			dimension[1] = display.getWidth();
		}

		return dimension;
	}

	public int getDeviceDensity(Context mContext) {
		int Density = 0;
		switch (mContext.getResources().getDisplayMetrics().densityDpi) {
		case DisplayMetrics.DENSITY_LOW:
			Density = 0;
			break;
		case DisplayMetrics.DENSITY_MEDIUM:
			Density = 1;
			break;
		case DisplayMetrics.DENSITY_HIGH:
			Density = 2;
			break;
		case DisplayMetrics.DENSITY_XHIGH:
			Density = 3;
			break;
		default:
			Density = 3;
			break;
		}

		return Density;
	}

}
