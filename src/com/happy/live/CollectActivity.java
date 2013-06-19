package com.happy.live;

import java.util.ArrayList;
import java.util.List;

import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;
import net.youmi.android.banner.AdViewLinstener;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.happy.live.entity.PromoInfo;
import com.happy.live.entity.TURL;
import com.happy.live.service.AppService;
import comhappy.live.view.ListAdapter;
import comhappy.live.view.PromoAdapter;

public class CollectActivity extends Activity {

	public static CollectActivity instance;

	public List<TURL> urlList = new ArrayList<TURL>();
	private ListAdapter adapter = null;
	private ListView list_channel;
	private LinearLayout view_advertise1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN); // 隐藏状态栏
		setContentView(R.layout.collect_list);

		instance = this;
		initListView();
		initAdvertise();
	}

	private void initAdvertise() {
		view_advertise1 = (LinearLayout) findViewById(R.id.view_advertise1);
		view_advertise1.addView(initAdView());
	}
	
	private View initAdView() {
        AdView adView = new AdView(this, AdSize.SIZE_320x50);
        // 监听广告条接口
        adView.setAdListener(new AdViewLinstener() {
            @Override
            public void onSwitchedAd(AdView arg0) {
                Log.i("YoumiSample", "广告条切换");
            }
            @Override
            public void onReceivedAd(AdView arg0) {
                Log.i("YoumiSample", "请求广告成功");
            }
            @Override
            public void onFailedToReceivedAd(AdView arg0) {
                Log.i("YoumiSample", "请求广告失败");
            }
        });
        return adView;
	}

	private void initListView() {
		adapter = new ListAdapter(this, urlList);
		list_channel = (ListView) this.findViewById(R.id.list_channel);
		list_channel.setAdapter(adapter);
		list_channel.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ListAdapter.ViewHolder vh = (ListAdapter.ViewHolder) view.getTag();
				AppService.startPlay(CollectActivity.this, vh.turl);
			}
		});

		list_channel.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				ListAdapter.ViewHolder vh = (ListAdapter.ViewHolder) view.getTag();
				deleteCollectDialog(vh.turl);
				return true;
			}
		});

//		list_channel.setonRefreshListener(new OnRefreshListener() {
//			@Override
//			public void onRefresh() {
//				try {
//					String str = AppService.getCollect(instance);
//					Thread.sleep(1000);
//					JSONArray list = new JSONArray(str);
//					for (int i = list.length() - 1; i >= 0; i --) {
//						JSONObject jo = (JSONObject) list.get(i);
//						TURL url = new TURL(jo.getString("url"), jo.getString("name"),
//								jo.getString("icon"));
//						urlList.add(url);
//					}
//					mHandler.sendEmptyMessage(0);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
	}

	private void deleteCollectDialog(final TURL turl) {
		AlertDialog.Builder ab = new Builder(this);
		ab.setTitle("提示").setMessage("删除" + turl.name + "?");
		ab.setPositiveButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AppService.delCollect(CollectActivity.this, turl);
				loadListData();
			}
		});
		ab.setNegativeButton("取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		ab.create().show();
	}

	public void initPromoList(final TURL turl, final ListView listView) {
		final List<PromoInfo> data = new ArrayList<PromoInfo>();
		final PromoAdapter adapter = new PromoAdapter(this, data);
		listView.setAdapter(adapter);
		new Thread() {
			public void run() {
				for (int i = 0; i < 9; i ++) {
					data.add(new PromoInfo(turl.name + "_" + i, i + "点"));
				}
				adapter.notifyDataSetChanged();
			}
		}.start();
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadListData();
	}

	public void loadListData() {
		try {
			String str = AppService.getCollect(instance);
			JSONArray list = new JSONArray(str);
			urlList.clear();
			for (int i = list.length() - 1; i >= 0; i --) {
				JSONObject jo = (JSONObject) list.get(i);
				TURL url = new TURL(jo.getString("url"), jo.getString("name"),
						jo.getString("icon"));
				urlList.add(url);
			}
			mHandler.sendEmptyMessage(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handle resize of the surface and the overlay
	 */
	private final Handler mHandler = new VideoPlayerHandler(this);

	private static class VideoPlayerHandler extends WeakHandler<CollectActivity> {
		public VideoPlayerHandler(CollectActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			CollectActivity activity = getOwner();
			if (activity == null) // WeakReference could be GC'ed early
				return;
			activity.adapter.notifyDataSetChanged();
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}
}