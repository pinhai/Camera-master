package com.example.binbin.photo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * 权限工具类
 * 
 * @author Administrator
 *
 */
public class PermissionUtil {
	private static String TAG = "PermissionUtil";

	public static final int REQUEST_CODE_PERMISSION_SETTING = 102;

	/**
	 * 启动应用的设置
	 * 
	 * @param context
	 */
	public static void startAppSettings(Context context) {
		Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		intent.setData(Uri.parse("package:" + context.getPackageName()));
		((Activity) context).startActivityForResult(intent, REQUEST_CODE_PERMISSION_SETTING);
	}

	/**
	 * 申请权限 ,覆盖activity的onRequestPermissionsResult()方法获取结果
	 * 
	 * @param context
	 * @param perm
	 * @param requestCode
	 */
	public static void requestPermission(Context context, String perm[], int requestCode) {
		if (!checkPermissions(context, perm)) {
			Log.v(TAG, "请求权限:" + perm.toString());
			ActivityCompat.requestPermissions((Activity) context, perm, requestCode);
		}
	}

	/**
	 * 是否获取到了权限
	 * 
	 * @return
	 */
	public static boolean checkPermissions(Context context, String perm[]) {
		int checkPermission = 0;
		for (String p : perm) {
			checkPermission = ContextCompat.checkSelfPermission(context, p);
			if (checkPermission != PackageManager.PERMISSION_GRANTED) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 是否获取到了权限
	 * 
	 * @return
	 */
	public static boolean checkPermission(Context context, String perm) {
		int checkPermission = ContextCompat.checkSelfPermission(context, perm);
		return checkPermission == PackageManager.PERMISSION_GRANTED;
	}

	public static final int REQUEST_COARSE_LOCATION = 0;

	/** 23以上版本蓝牙扫描需要定位权限 */
	public static void requestCoarseLocationPerm(Context context) {
		if (!checkCoarseLocationPermission(context)) {
			// 判断是否需要 向用户解释，为什么要申请该权限
			if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
					Manifest.permission.ACCESS_COARSE_LOCATION)) {
//				Toast.makeText(context, R.string.bt_discovery_need_location_permission, 1).show();
			}
			Log.v(TAG, "请求定位权限");
			ActivityCompat.requestPermissions((Activity) context,
					new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, REQUEST_COARSE_LOCATION);
		}
	}

	/**
	 * 是否获取到了定位权限
	 * 
	 * @return
	 */
	public static boolean checkCoarseLocationPermission(Context context) {
		int checkPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
		return checkPermission == PackageManager.PERMISSION_GRANTED;
	}

	public static final int REQUEST_RECORD_AUDIO = 1;

	/** 申请录音权限 */
	public static void requestAudioPerm(Context context) {
		if (!checkAudioPermission(context)) {
			// 判断是否需要 向用户解释，为什么要申请该权限
			Log.v(TAG, "请求录音权限");
			ActivityCompat.requestPermissions((Activity) context, new String[] { Manifest.permission.RECORD_AUDIO },
					REQUEST_RECORD_AUDIO);
		}
	}

	/**
	 * 是否获取到了录音权限
	 * 
	 * @return
	 */
	public static boolean checkAudioPermission(Context context) {
		int checkPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO);
		return checkPermission == PackageManager.PERMISSION_GRANTED;
	}

}
