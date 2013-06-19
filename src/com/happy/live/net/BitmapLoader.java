package com.happy.live.net;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public class BitmapLoader implements Runnable {
	private static HashMap<String, SoftReference<Bitmap>> bitmapCache = new HashMap<String, SoftReference<Bitmap>>();
	private static boolean start = false;
	private static List<Msg> msgList = new ArrayList<Msg>();

	public static void loadBitmap(ImageView imageView, String url) {
		if (!start) {
			start = true;
			new Thread(new BitmapLoader()).start();
		}

		if (url == null || url.length() == 0) {
			return;
		}

		if (bitmapCache.containsKey(url)) {
			if (bitmapCache.get(url) == null) {
				return;
			}

			if (bitmapCache.get(url).get() != null) {
				imageView.setImageBitmap(bitmapCache.get(url).get());
				return;
			}
		}
		
		synchronized (msgList) {
			Msg msg = new Msg(imageView, url);
			if (!msgList.contains(msg)) {
				msgList.add(msg);
				msgList.notify();
			}
		}
	}

	@Override
	public void run() {
		while (start) {
			try {
				synchronized (msgList) {
					if (msgList.size() == 0) {
						msgList.wait();
					}
				}
				if (msgList.size() > 0) {
					final Msg msg = msgList.get(0);
					try {
						String iconUrl = msg.url;
//						try {
//							String path = iconUrl.substring(0, iconUrl.lastIndexOf("/") + 1);
//							String file = iconUrl.substring(iconUrl.lastIndexOf("/") + 1);
//							iconUrl = path + URLEncoder.encode(file, "utf-8");
//						} catch (UnsupportedEncodingException e) {
//						}

						URL url = new URL(iconUrl);
						InputStream is = (InputStream) url.getContent();
						final Bitmap bitmap = BitmapFactory.decodeStream(is);
						if (bitmap != null) {
							bitmapCache.put(msg.url, new SoftReference<Bitmap>(bitmap));
							((Activity)msg.img.getContext()).runOnUiThread(new Runnable() {
								public void run() {
									msg.img.setImageBitmap(bitmap);
								}
							});
						} else {
							bitmapCache.put(msg.url, null);
						}
					} catch (Exception e) {
						bitmapCache.put(msg.url, null);
					} finally {
						msgList.remove(0);
					}
				}
			} catch (Exception e) {}
		}
	}
}

class Msg {
	ImageView img;
	String url;
	Msg(ImageView img, String url) {
		this.img = img;
		this.url = url;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Msg) {
			Msg om = (Msg)o;
			return url.equals(om.url);
		} else {
			return false;
		}
	}
}