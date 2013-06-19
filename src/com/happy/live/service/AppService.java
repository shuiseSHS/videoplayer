package com.happy.live.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.happy.live.entity.TURL;
import com.happy.live.util.AppConstants;
import com.happy.live.util.SharedDataUtils;

public class AppService {
	
	public static boolean getSoftDecode(Context context) {
		return SharedDataUtils.getSharedBooleanData(context, SharedDataUtils.DECODE_KEY);
	}
	
	public static void setSoftDecode(Context context, boolean b) {
		SharedDataUtils.setSharedBooleanData(context, SharedDataUtils.DECODE_KEY, b);
	}

	public static void addCollect(Context context, TURL turl) {
		String str = SharedDataUtils.getSharedStringData(context, SharedDataUtils.COLLECT_KEY);
		JSONArray jsa = null;
		try {
			jsa = new JSONArray(str);
		} catch (JSONException e) {
			jsa = new JSONArray();
		}

		boolean hasCollect = false;
		for (int i = 0; i < jsa.length(); i ++) {
			try {
				JSONObject jo = jsa.getJSONObject(i);
				String curl = jo.getString("url");
				if (curl.equals(turl.url)) {
					hasCollect = true;
					break;
				}
			} catch (JSONException e) {
			}
		}
		
		if (hasCollect) {
			Toast.makeText(context, turl.name + "已经收藏过了", Toast.LENGTH_SHORT).show();
		} else {
			try {
				JSONObject jo = new JSONObject();
				jo.put("url", turl.url);
				jo.put("name", turl.name);
				jo.put("icon", turl.icon);
				jsa.put(jo);
				SharedDataUtils.setSharedStringData(context, SharedDataUtils.COLLECT_KEY, jsa.toString());
				Toast.makeText(context, turl.name + "收藏成功", Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
			}
		}
	}

	public static void delCollect(Context context, TURL turl) {
		String str = SharedDataUtils.getSharedStringData(context, SharedDataUtils.COLLECT_KEY);
		JSONArray jsa = null;
		try {
			jsa = new JSONArray(str);
		} catch (JSONException e) {
			jsa = new JSONArray();
		}
		JSONArray newJsa = new JSONArray();
		for (int i = 0; i < jsa.length(); i ++) {
			try {
				JSONObject jo = jsa.getJSONObject(i);
				String curl = jo.getString("url");
				if (!curl.equals(turl.url)) {
					newJsa.put(jo);
				}
			} catch (JSONException e) {
			}
		}
		SharedDataUtils.setSharedStringData(context, SharedDataUtils.COLLECT_KEY, newJsa.toString());
		Toast.makeText(context, turl.name + "删除成功", Toast.LENGTH_SHORT).show();
	}

	public static String getCollect(Context context) {
		return SharedDataUtils.getSharedStringData(context, SharedDataUtils.COLLECT_KEY);
	}
	
	public static void startPlay(Context context, TURL turl) {
		Intent in = new Intent();
		if (AppConstants.SOFT_DECODE) {
			in.setAction("com.qiyi.live.vlcplay");
		} else {
			in.setAction("com.qiyi.live.play");
		}
		in.putExtra("url", turl.url);
		in.putExtra("title", turl.name);
		in.putExtra("icon", turl.icon);
		context.startActivity(in);
	}
}
