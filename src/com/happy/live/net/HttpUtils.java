package com.happy.live.net;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class HttpUtils {

	public static void testHttp(final String url) {
		new Thread() {
			public void run() {
				try {
					URL u = new URL(url);
					HttpURLConnection conn = (HttpURLConnection) u.openConnection();
					int code = conn.getResponseCode();
					if (code == 200) {
						System.out.println(conn.getInputStream().available());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * 通过Get方式获取网络请求（建议异步调用）
	 * 
	 * @param url
	 * @return
	 */
	public static String getHttp(String url) {
		String str = null;
		try {
			HttpGet httpGet = new HttpGet(url);
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(httpGet);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				str = EntityUtils.toString(response.getEntity());
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return str;
	}

	/**
	 * 通过Post方式获取网络请求（建议异步调用）
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	public static String postHttp(String url, List<NameValuePair> params) {
		String str = null;
		try {
			HttpPost httpPost = new HttpPost(url);
			HttpClient client = new DefaultHttpClient();
			HttpEntity httpentity = new UrlEncodedFormEntity(params);
			httpPost.setEntity(httpentity);
			HttpResponse response = client.execute(httpPost);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				str = EntityUtils.toString(response.getEntity());
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return str;
	}

	/**
	 * 通过Get方式获取网络请求
	 * 
	 * @param url
	 * @param hc
	 */
	public static void getHttp(final String url, final HttpCallback hc) {
		new Thread() {
			public void run() {
				try {
					HttpGet httpGet = new HttpGet(url);
					HttpClient client = new DefaultHttpClient();
					HttpResponse response = client.execute(httpGet);
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						String str = EntityUtils.toString(response.getEntity(), "utf-8");
						hc.callback(str);
					} else {
						hc.callback(null);
					}
				} catch (ClientProtocolException e) {
					hc.callback(null);
				} catch (IOException e) {
					hc.callback(null);
				}
			}
		}.start();
	}

	/**
	 * 通过Post方式获取网络请求
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	public static void postHttp(final String url, final List<NameValuePair> params,
			final HttpCallback hc) {
		new Thread() {
			public void run() {
				try {
					HttpPost httpPost = new HttpPost(url);
					HttpClient client = new DefaultHttpClient();
					HttpEntity httpentity = new UrlEncodedFormEntity(params);
					httpPost.setEntity(httpentity);
					HttpResponse response = client.execute(httpPost);
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						String str = EntityUtils.toString(response.getEntity());
						hc.callback(str);
					} else {
						hc.callback(null);
					}
				} catch (ClientProtocolException e) {
					hc.callback(null);
				} catch (IOException e) {
					hc.callback(null);
				}
			}
		}.start();
	}

}
