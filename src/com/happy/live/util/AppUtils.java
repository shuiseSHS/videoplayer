package com.happy.live.util;

import java.util.List;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.os.Vibrator;

public class AppUtils {

	public static boolean startActivityByPackageName(Context context,
			String packageName) {
		List<ResolveInfo> list = findActivitiesForPackage(context, packageName);
		if (list == null) {
			return false;
		}
		ResolveInfo info = list.get(0);
		ComponentName componentName = new ComponentName(
				info.activityInfo.packageName, info.activityInfo.name);

		Intent intent = getActivity(componentName,
				Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		context.startActivity(intent);
		return true;
	}

	public static List<ResolveInfo> findActivitiesForPackage(Context context,
			String packageName) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		mainIntent.setPackage(packageName);
		final List<ResolveInfo> apps = packageManager.queryIntentActivities(
				mainIntent, 0);
		return apps;
	}

	final static Intent getActivity(ComponentName className, int launchFlags) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(className);
		intent.setFlags(launchFlags);
		return intent;
	}

	public static boolean checkNetworkState(Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connManager.getActiveNetworkInfo() != null) {
			return connManager.getActiveNetworkInfo().isAvailable();
		}
		return false;
	}

	public static void vibrate(Activity activity, long milliseconds) {
		Vibrator vib = (Vibrator) activity
				.getSystemService(Service.VIBRATOR_SERVICE);
		vib.vibrate(milliseconds);
	}

	public static void vibrate(Activity activity, long[] pattern,
			boolean isRepeat) {
		Vibrator vib = (Vibrator) activity
				.getSystemService(Service.VIBRATOR_SERVICE);
		vib.vibrate(pattern, isRepeat ? 1 : -1);
	}

	public static String toHexString(String b) {
		String a = "";
		char[] c = b.toCharArray();

		for (int i = 0; i < b.length(); i++) {
			String hex = Integer.toHexString(c[i] & 0xFF);

			if (hex.length() == 1) {
				hex = '0' + hex;
			}

			a = a + hex;
		}
		return a;
	}

}
