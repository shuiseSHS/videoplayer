package com.happy.live;

import net.youmi.android.AdManager;
import net.youmi.android.dev.AppUpdateInfo;
import net.youmi.android.dev.CheckAppUpdateCallBack;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

public class MainActivity extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN); // 隐藏状态栏
		setContentView(R.layout.main);

		//		AppConstants.kernel.start("", 1, "");

		LayoutInflater inflater = LayoutInflater.from(this);
		View v1 = inflater.inflate(R.layout.tab_1, null);
		final ImageView iv1 = (ImageView) v1.findViewById(R.id.img_view);
		View v2 = inflater.inflate(R.layout.tab_2, null);
		final ImageView iv2 = (ImageView) v2.findViewById(R.id.img_view);
		//		View v3 = inflater.inflate(R.layout.tab_3, null);
		//		final TextView iv3 = (TextView) v3.findViewById(R.id.img_view);
		final TabHost tabHost = getTabHost();
		tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator(v1)
				.setContent(new Intent(this, ListActivity.class)
				.putExtras(getIntent().getExtras())));

		tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator(v2)
				.setContent(new Intent(this, CollectActivity.class)));

		//		tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator(v3)
		//				.setContent(new Intent(this, SettingActivity.class)));

		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				if ("tab1".equals(tabId)) {
					iv1.setImageResource(R.drawable.phone_navi_cate_selected);
				} else {
					iv1.setImageResource(R.drawable.phone_navi_cate);
				}
				if ("tab2".equals(tabId)) {
					iv2.setImageResource(R.drawable.phone_navi_my_selected);
				} else {
					iv2.setImageResource(R.drawable.phone_navi_my);
				}
				//				if ("tab3".equals(tabId)) {
				//					iv3.setTextColor(0xFF669900);
				//					Resources res = getResources();
				//					Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.phone_my_setting_selected);  
				//					BitmapDrawable top = new BitmapDrawable(res, bmp); 
				//					top.setBounds(0, 0, bmp.getWidth(), bmp.getHeight());
				//					iv3.setCompoundDrawables(null, top, null, null);
				//				} else {
				//					iv3.setTextColor(0xFF999999);
				//					Resources res = getResources();
				//					Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.phone_my_setting);  
				//					BitmapDrawable top = new BitmapDrawable(res, bmp); 
				//					top.setBounds(0, 0, bmp.getWidth(), bmp.getHeight());
				//					iv3.setCompoundDrawables(null, top, null, null);
				//				}

			}
		});
		
		checkAppUpdate();
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Dialog alertDialog = new AlertDialog.Builder(this).setTitle("提示")
					.setMessage("确定要退出？")
					.setPositiveButton("确定", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//							AppConstants.kernel.stop();
							finish();
						}
					}).setNegativeButton("取消", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					}).create();
			alertDialog.show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void checkAppUpdate() {
		AdManager.getInstance(MainActivity.this).asyncCheckAppUpdate(
				new CheckAppUpdateCallBack() {

					@Override
					public void onCheckAppUpdateFinish(
							AppUpdateInfo appUpdateInfo) {
						if (appUpdateInfo == null) {
							Toast.makeText(MainActivity.this, "当前版本已经是最新版",
									Toast.LENGTH_SHORT).show();
						} else {
							// 获取版本号
//							int versionCode = appUpdateInfo.getVersionCode();
							// 获取版本
							String versionName = appUpdateInfo.getVersionName();
							// 获取新版本的信息
							String updateTips = appUpdateInfo.getUpdateTips();
							// 获取apk下载地址
							final String downloadUrl = appUpdateInfo.getUrl();

							AlertDialog updateDialog = new AlertDialog.Builder(
									MainActivity.this)
							.setIcon(android.R.drawable.ic_dialog_info)
							.setTitle("发现新版本 " + versionName)
							.setMessage(updateTips)
							.setPositiveButton(
									"更新",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											try {
												Intent intent = Intent
														.parseUri(
																downloadUrl,
																Intent.FLAG_ACTIVITY_NEW_TASK);
												startActivity(intent);
											} catch (Exception e) {
											}
										}
									}).setNegativeButton("下次再说", null)
									.create();
							updateDialog.show();
						}
					}
				});
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
