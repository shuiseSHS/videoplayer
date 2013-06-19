package com.happy.live;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import net.youmi.android.AdManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.happy.live.util.AppUtils;
import com.umeng.analytics.MobclickAgent;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN); // 隐藏状态栏
		setContentView(R.layout.splash);
		// 初始化接口，应用启动的时候调用
		// 参数：appId, appSecret, 调试模式
		AdManager.getInstance(this).init("2d2471d978d279dd",
				"7ad14b0ddafb6c12", false);

		// AppConstants.SOFT_DECODE = SharedDataUtils.getSharedBooleanData(this,
		// getString(R.string.soft_decode));
		// AppConstants.USE_P2P = SharedDataUtils.getSharedBooleanData(this,
		// getString(R.string.setting_p2p));

		if (!AppUtils.checkNetworkState(this)) {
			Dialog alertDialog = new AlertDialog.Builder(this)
					.setTitle("无网络连接").setMessage("当前无网络连接，请检查网络设置！")
					.setPositiveButton("确定", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							SplashActivity.this.finish();
						}
					}).create();
			alertDialog.show();
			return;
		}

		final long start = System.currentTimeMillis();

		new Thread() {
			public void run() {
				try {
					StringBuffer sb = new StringBuffer();
					InputStreamReader isr = new InputStreamReader(getAssets()
							.open("live.list"), "utf-8");
					BufferedReader read = new BufferedReader(isr);
					String l = null;
					while ((l = read.readLine()) != null) {
						sb.append(l);
					}
					
					long time = System.currentTimeMillis() - start;
					if (time < 1000) {
						try {
							Thread.sleep(1000 - time);
						} catch (InterruptedException e) {
						}
					}
					
					Intent in = new Intent(SplashActivity.this, MainActivity.class);
					in.putExtra("data", sb.toString());
					startActivity(in);
					finish();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
//		HttpUtils.getHttp(AppConstants.LIST_URL, new HttpCallback() {
//			@Override
//			public void callback(String str) {
//				long time = System.currentTimeMillis() - start;
//				if (time < 1000) {
//					try {
//						Thread.sleep(1000 - time);
//					} catch (InterruptedException e) {
//					}
//				}
//				Intent in = new Intent(SplashActivity.this, MainActivity.class);
//				in.putExtra("data", str);
//				startActivity(in);
//				finish();
//			}
//		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

}
