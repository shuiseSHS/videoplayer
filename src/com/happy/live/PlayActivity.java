package com.happy.live;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;
import net.youmi.android.banner.AdViewLinstener;
import net.youmi.android.spot.SpotManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.happy.live.entity.TURL;
import com.happy.live.service.AppService;
import com.happy.live.util.AppConstants;
import com.happy.live.util.AppUtils;
import com.umeng.analytics.MobclickAgent;

public class PlayActivity extends Activity {
	private final static String TAG = "P2PLive";

	private static final int SURFACE_BEST_FIT = 0;
	private static final int SURFACE_FIT_HORIZONTAL = 1;
	private static final int SURFACE_FIT_VERTICAL = 2;
	private static final int SURFACE_FILL = 3;
	private static final int SURFACE_16_9 = 4;
	private static final int SURFACE_4_3 = 5;
	private static final int SURFACE_ORIGINAL = 6;

	private static final int FADE_OUT = 1;
	private static final int SHOW_PROGRESS = 2;
	private static final int DISMISS_PROGRESS = 3;
	private static final int SURFACE_SIZE = 4;
	private static final int FADE_OUT_INFO = 5;
	private static final int LOADLIST = 6;

	private int mVideoHeight, mVideoWidth;
	private int mCurrentSize = SURFACE_ORIGINAL;
	private int mSurfaceAlign;

	private boolean isPlaying = false;
	private int mMaxVolume, windowWidth, windowHeight, mVolume = -1;
	private float density;
	private float mBrightness = -1f;
	private long lastClick = System.currentTimeMillis();
	private MotionEvent old;

	private VideoView movieView;
	private View main_frame, view_control, mVolumeBrightnessLayout, view_title;
	private LinearLayout view_advertise1;
	private ImageView mOperationBg;
	private TextView mPercentText, mInfo, txt_title, txt_time;
	private ImageButton playbutton, fullbutton, fillbutton, collectbutton, backbutton;
	private ProgressDialog progressDialog;

	private AudioManager mAudioManager;

	private DateFormat sdf = new SimpleDateFormat("HH:mm", Locale.CHINA);

	private PlayActivity instance;
	private String title, url, icon, p2pUrl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE); // 隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN); // 隐藏状态栏
		setContentView(R.layout.player);
		instance = this;
		mAudioManager = (AudioManager) instance.getSystemService(PlayActivity.AUDIO_SERVICE);
		initViews();
		initAdvertise();

		title = this.getIntent().getExtras().getString("title");
		url = this.getIntent().getExtras().getString("url");
		icon = this.getIntent().getExtras().getString("icon");

		p2pUrl = url;
		if (AppConstants.USE_P2P) {
			p2pUrl = AppUtils.toHexString(p2pUrl);
			p2pUrl = "http://127.0.0.1:17171/?m3u8=" + p2pUrl;
		}
		play();

		DisplayMetrics d = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(d);
		windowWidth = d.widthPixels;
		windowHeight = d.heightPixels;
		density = d.density;
	}

	private void initViews() {
		movieView = (VideoView) this.findViewById(R.id.surfaceView);

		main_frame = this.findViewById(R.id.main_frame);
		main_frame.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					old = MotionEvent.obtain(event);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					if (view_control.getVisibility() == View.VISIBLE) {
						showOrHideController(false);
					} else {
						showOrHideController(true);
					}
					dismissPic();
					old.recycle();
					old = null;
				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					onTouchMove(old, event);
				}

				return true;
			}
		});

		view_control = this.findViewById(R.id.view_control);
		view_title = this.findViewById(R.id.view_title);
		txt_title = (TextView) findViewById(R.id.txt_title);
		txt_time = (TextView) findViewById(R.id.txt_time);

		backbutton = (ImageButton) this.findViewById(R.id.btn_back);
		playbutton = (ImageButton) this.findViewById(R.id.btn_play);
		fullbutton = (ImageButton) this.findViewById(R.id.btn_fullscreen);
		fillbutton = (ImageButton) this.findViewById(R.id.btn_filltype);
		collectbutton = (ImageButton) this.findViewById(R.id.btn_collect);
		backbutton.setOnClickListener(onClickListener);
		playbutton.setOnClickListener(onClickListener);
		fullbutton.setOnClickListener(onClickListener);
		fillbutton.setOnClickListener(onClickListener);
		collectbutton.setOnClickListener(onClickListener);

		mOperationBg = (ImageView) findViewById(R.id.operation_bg);
		mVolumeBrightnessLayout = findViewById(R.id.operation_volume_brightness);
		mPercentText = (TextView) findViewById(R.id.operation_percent);
		mInfo = (TextView) findViewById(R.id.player_overlay_info);
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
	
	private void showSpotAds() {
		// 展示插播广告，可以不调用loadSpot独立使用
		SpotManager.getInstance(PlayActivity.this).showSpotAds(PlayActivity.this);
	}

	private void showOrHideController(boolean show) {
		if (!show) {
			view_control.setVisibility(View.GONE);
			view_title.setVisibility(View.GONE);
		} else {
			view_control.setVisibility(View.VISIBLE);
			view_title.setVisibility(View.VISIBLE);
			txt_time.setText(sdf.format(new Date()));
//			mHandler.removeMessages(FADE_OUT);
//			mHandler.sendEmptyMessageDelayed(FADE_OUT, 3000);
		}
	}

	private void dismissPic() {
		mVolume = -1;
		mBrightness = -1f;
		mVolumeBrightnessLayout.setVisibility(View.GONE);
	}

	OnClickListener onClickListener = new OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_play:
				if (isPlaying) {
					if (movieView.isPlaying()) {
						movieView.pause();
						v.setBackgroundResource(R.drawable.play_ctrl_played_bg);
						showSpotAds();
					} else {
						movieView.start();
						v.setBackgroundResource(R.drawable.play_ctrl_pause_bg);
					}
				}
				break;
			case R.id.btn_back:
				stopAndFinish();
				break;
			case R.id.btn_fullscreen:
				break;
			case R.id.btn_collect:
				AppService.addCollect(PlayActivity.this, new TURL(url, title, icon));
				break;
			case R.id.btn_filltype:
				mCurrentSize = (mCurrentSize + 1) % 7;
				changeSurfaceSize();
				showFillInfo();
				break;
			default:
				break;
			}
			showOrHideController(true);
		}
	};

	private void play() {
		progressDialog = ProgressDialog.show(this, "", "努力加载中...", true, true);
		movieView.stopPlayback();
		movieView.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				Log.i(TAG, "completed");
				isPlaying = false;
				movieView.stopPlayback();
				movieView.setVideoURI(Uri.parse(p2pUrl));
			}
		});
		movieView.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Log.i(TAG, "error: what " + what + " extra " + extra);
				progressDialog.dismiss();
				if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
					movieView.stopPlayback();
					movieView.setVideoURI(Uri.parse(p2pUrl));
					return true;
				} else {
					switch (extra) {
					case -1004:
						showExitDialog("未找到视频流");
						return true;
					case -1007:
						showExitDialog("不是标准的视频流");
						return true;
					case -1010:
						movieView.start();
						return true;
					case -110:
						showExitDialog("请求超时");
						return true;
					default:
						Log.e(TAG, "error current: extra " + extra);
						isPlaying = false;
						movieView.stopPlayback();
						movieView.setVideoURI(Uri.parse(p2pUrl));
						return true;
					}
				}
			}
		});
		movieView.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				Log.i(TAG, "prepared");
				mVideoWidth = movieView.getWidth();
				mVideoHeight = movieView.getHeight();

				progressDialog.dismiss();
				movieView.start();
				playbutton.setBackgroundResource(R.drawable.play_ctrl_pause_bg);
				isPlaying = true;

				showOrHideController(true);
			}
		});

		movieView.setVideoURI(Uri.parse(p2pUrl));
		txt_title.setText(title);
		txt_time.setText(sdf.format(new Date()));
	}

	private boolean onTouchMove(MotionEvent e1, MotionEvent e2) {
		if (e1 == null || e2 == null) {
			return false;
		}

		float mOldX = e1.getX(), mOldY = e1.getY();
		float x = (int) e2.getRawX();
		float y = (int) e2.getRawY();

		if (Math.abs(mOldY - y) < 2.0 || Math.abs(mOldX - x) < 2.0) {
			return false;
		}

		if (mOldX > windowWidth * 2 / 3.0
				&& x > windowWidth * 2 / 3.0
				&& (Math.abs(mOldY - y) / Math.abs(mOldX - x)) > Math
				.tan(45.0 * Math.PI / 180)) {// 右边滑动
			onVolumeSlide((mOldY - y) / windowHeight);
			return false;
		} else if (mOldX < windowWidth / 3.0
				&& x < windowWidth / 3.0
				&& (Math.abs(mOldY - y) / Math.abs(mOldX - x)) > Math
				.tan(45.0 * Math.PI / 180)) {// 左边滑动
			onBrightnessSlide((mOldY - y) / windowHeight);
			return false;
		}

		return false;
	}

	/**
	 * 滑动改变声音大小
	 * 
	 * @param percent
	 */
	public void onVolumeSlide(float percent) {
		if (mVolume == -1) {
			mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			if (mVolume < 0)
				mVolume = 0;

			// 显示
			mOperationBg.setImageResource(R.drawable.video_volumn_bg);
			mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
		}

		float mIndex = percent * mMaxVolume / 2 + mVolume;
		int index = (int) mIndex;

		if (mIndex > mMaxVolume) {
			index = mMaxVolume;
			mIndex = mMaxVolume;
		} else if (mIndex < 0) {
			index = 0;
			mIndex = 0;
		}

		mPercentText.setText((int) (mIndex * 100 / mMaxVolume) + "%");
		// 变更声音
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
		mMaxVolume = mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	}

	/**
	 * 滑动改变屏幕亮度
	 * 
	 * @param percent
	 */
	public void onBrightnessSlide(float percent) {
		if (mBrightness < 0) {
			mBrightness = getWindow().getAttributes().screenBrightness;
			if (mBrightness <= 0.00f)
				mBrightness = 0.50f;
			if (mBrightness < 0.01f)
				mBrightness = 0.01f;
			// 显示
			mOperationBg.setImageResource(R.drawable.video_brightness_bg);
			mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
		}

		WindowManager.LayoutParams lpa = getWindow().getAttributes();
		lpa.screenBrightness = mBrightness + percent;
		if (lpa.screenBrightness > 1.0f)
			lpa.screenBrightness = 1.0f;
		else if (lpa.screenBrightness < 0.01f)
			lpa.screenBrightness = 0.01f;
		getWindow().setAttributes(lpa);

		mPercentText.setText((int) (100 * lpa.screenBrightness) + "%");
	}

	private void changeSurfaceSize() {
		// mSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);
		movieView.invalidate();
		// get screen size
		int dw = getWindow().getDecorView().getWidth();
		int dh = getWindow().getDecorView().getHeight();

		// getWindow().getDecorView() doesn't always take orientation into
		// account, we have to correct the values
		boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
		if (dw > dh && isPortrait || dw < dh && !isPortrait) {
			int d = dw;
			dw = dh;
			dh = d;
		}

		// sanity check
		if (dw * dh == 0 || mVideoWidth * mVideoHeight == 0) {
			Log.e(TAG, "Invalid surface size");
			return;
		}

		// compute the aspect ratio
		double ar, vw;
		if (density == 1.0) {
			/* No indication about the density, assuming 1:1 */
			vw = mVideoWidth;
			ar = (double) mVideoWidth / (double) mVideoHeight;
		} else {
			/* Use the specified aspect ratio */
			vw = mVideoWidth * density;
			ar = vw / mVideoHeight;
		}

		// compute the display aspect ratio
		double dar = (double) dw / (double) dh;

		switch (mCurrentSize) {
		case SURFACE_BEST_FIT:
			if (dar < ar)
				dh = (int) (dw / ar);
			else
				dw = (int) (dh * ar);
			break;
		case SURFACE_FIT_HORIZONTAL:
			dh = (int) (dw / ar);
			break;
		case SURFACE_FIT_VERTICAL:
			dw = (int) (dh * ar);
			break;
		case SURFACE_FILL:
			break;
		case SURFACE_16_9:
			ar = 16.0 / 9.0;
			if (dar < ar)
				dh = (int) (dw / ar);
			else
				dw = (int) (dh * ar);
			break;
		case SURFACE_4_3:
			ar = 4.0 / 3.0;
			if (dar < ar)
				dh = (int) (dw / ar);
			else
				dw = (int) (dh * ar);
			break;
		case SURFACE_ORIGINAL:
			dh = mVideoHeight;
			dw = (int) vw;
			break;
		}

		// align width on 16bytes
		int alignedWidth = (mVideoWidth + mSurfaceAlign) & ~mSurfaceAlign;

		// set display size
		LayoutParams lp = movieView.getLayoutParams();
		lp.width = dw * alignedWidth / mVideoWidth;
		lp.height = dh;
		movieView.setLayoutParams(lp);
		movieView.invalidate();
	}

	private void showFillInfo() {
		switch (mCurrentSize) {
		case SURFACE_BEST_FIT:
			showInfo(R.string.surface_best_fit, 1000);
			break;
		case SURFACE_FIT_HORIZONTAL:
			showInfo(R.string.surface_fit_horizontal, 1000);
			break;
		case SURFACE_FIT_VERTICAL:
			showInfo(R.string.surface_fit_vertical, 1000);
			break;
		case SURFACE_FILL:
			showInfo(R.string.surface_fill, 1000);
			break;
		case SURFACE_16_9:
			showInfo("16:9", 1000);
			break;
		case SURFACE_4_3:
			showInfo("4:3", 1000);
			break;
		case SURFACE_ORIGINAL:
			showInfo(R.string.surface_original, 1000);
			break;
		}
	}

	/**
	 * Show text in the info view for "duration" milliseconds
	 * 
	 * @param text
	 * @param duration
	 */
	private void showInfo(String text, int duration) {
		mInfo.setVisibility(View.VISIBLE);
		mInfo.setText(text);
		mHandler.removeMessages(FADE_OUT_INFO);
		mHandler.sendEmptyMessageDelayed(FADE_OUT_INFO, duration);
	}

	private void showInfo(int textid, int duration) {
		mInfo.setVisibility(View.VISIBLE);
		mInfo.setText(textid);
		mHandler.removeMessages(FADE_OUT_INFO);
		mHandler.sendEmptyMessageDelayed(FADE_OUT_INFO, duration);
	}

	/**
	 * Handle resize of the surface and the overlay
	 */
	private final Handler mHandler = new VideoPlayerHandler(this);

	private static class VideoPlayerHandler extends WeakHandler<PlayActivity> {
		public VideoPlayerHandler(PlayActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			PlayActivity activity = getOwner();
			if (activity == null) // WeakReference could be GC'ed early
				return;
			switch (msg.what) {
			case FADE_OUT:
				activity.showOrHideController(false);
				break;
			case SHOW_PROGRESS:
				// activity.showProgress();
				break;
			case DISMISS_PROGRESS:
				// activity.dismissProgress();
				break;
			case SURFACE_SIZE:
				activity.changeSurfaceSize();
				break;
			case FADE_OUT_INFO:
				activity.fadeOutInfo();
				break;
			case LOADLIST:
				// activity.list_loading.setVisibility(View.GONE);
				// activity.adapter.notifyDataSetChanged();
				break;
			}
		}
	};

	private void fadeOutInfo() {
		if (mInfo.getVisibility() == View.VISIBLE)
			mInfo.startAnimation(AnimationUtils.loadAnimation(
					PlayActivity.this, android.R.anim.fade_out));
		mInfo.setVisibility(View.INVISIBLE);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - lastClick) < 2000) {
				stopAndFinish();
			} else {
				Toast.makeText(this, "再次按下返回键退出播放", Toast.LENGTH_SHORT).show();
				lastClick = System.currentTimeMillis();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void stopAndFinish() {
		progressDialog.dismiss();
		movieView.stopPlayback();
		finish();
	}

	private void showExitDialog(String msg) {
		Dialog alertDialog = new AlertDialog.Builder(this)
		.setTitle("提示").setMessage(msg)
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				stopAndFinish();
			}
		}).create();
		alertDialog.show();
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