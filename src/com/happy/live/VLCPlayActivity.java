package com.happy.live;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;
import org.videolan.libvlc.Util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.happy.live.entity.TURL;
import com.happy.live.service.AppService;
import com.happy.live.util.AppConstants;
import com.happy.live.util.AppUtils;

@SuppressLint("InlinedApi")
public class VLCPlayActivity extends Activity implements IVideoPlayer {
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

	private VLCPlayActivity instance;
	private LibVLC mLibVLC;
	private SurfaceView mSurface;
	private SurfaceHolder mSurfaceHolder;

	private int mVideoHeight;
	private int mVideoWidth;
	private int mSarNum;
	private int mSarDen;
	private int mCurrentSize = SURFACE_BEST_FIT;
	private int mSurfaceAlign;

	public List<TURL> urlList = new ArrayList<TURL>();
	private int mMaxVolume;
	private int mVolume = -1;
	private float windowWidth;
	private float windowHeight;
	private float mBrightness = -1f;
	private long lastClick = System.currentTimeMillis();
	private MotionEvent old = null;

	private View main_frame, view_control, view_title,
	mVolumeBrightnessLayout, view_advertise;
	private ImageView mOperationBg;
	private TextView mPercentText, txt_title, txt_time, mInfo;
	private ImageButton backbutton, playbutton, fullbutton, fillbutton, collectbutton;

	private AudioManager mAudioManager;

	private DateFormat sdf = new SimpleDateFormat("HH:mm", Locale.CHINA);
	private ProgressDialog progressDialog = null;
	private String title, url, icon, p2pUrl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE); // 隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN); // 隐藏状态栏
		setContentView(R.layout.vlcplayer);

		title = this.getIntent().getExtras().getString("title");
		url = this.getIntent().getExtras().getString("url");
		icon = this.getIntent().getExtras().getString("icon");

		instance = this;

		initControlView();
		initVLCVideo();
	}

	private void initControlView() {
		main_frame = this.findViewById(R.id.main_frame);
		view_control = this.findViewById(R.id.view_control);
		view_title = this.findViewById(R.id.view_title);
		txt_title = (TextView) this.findViewById(R.id.txt_title);
		txt_time = (TextView) this.findViewById(R.id.txt_time);
		txt_time.setText(sdf.format(new Date()));

		main_frame.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					if (view_control.getVisibility() == View.VISIBLE) {
						showOrHideController(false);
					} else {
						showOrHideController(true);
					}
					dismissPic();
					old.recycle();
					old = null;
					break;
				case MotionEvent.ACTION_DOWN:
					old = MotionEvent.obtain(event);
					AppUtils.vibrate(VLCPlayActivity.this, 10);
					break;
				case MotionEvent.ACTION_MOVE:
					onTouchMove(old, event);
					break;
				}
				return true;
			}
		});

		ButtonClickListener listener = new ButtonClickListener();
		backbutton = (ImageButton) this.findViewById(R.id.btn_back);
		playbutton = (ImageButton) this.findViewById(R.id.btn_play);
		fullbutton = (ImageButton) this.findViewById(R.id.btn_fullscreen);
		fillbutton = (ImageButton) this.findViewById(R.id.btn_filltype);
		collectbutton = (ImageButton) this.findViewById(R.id.btn_collect);
		playbutton.setOnClickListener(listener);
		backbutton.setOnClickListener(listener);
		fullbutton.setOnClickListener(listener);
		fillbutton.setOnClickListener(listener);
		collectbutton.setOnClickListener(listener);

		mOperationBg = (ImageView) findViewById(R.id.operation_bg);
		mVolumeBrightnessLayout = findViewById(R.id.operation_volume_brightness);
		mPercentText = (TextView) findViewById(R.id.operation_percent);

		windowWidth = getWindowManager().getDefaultDisplay().getWidth();
		windowHeight = getWindowManager().getDefaultDisplay().getHeight();
		mAudioManager = (AudioManager) instance
				.getSystemService(VLCPlayActivity.AUDIO_SERVICE);

		mInfo = (TextView) findViewById(R.id.player_overlay_info);
		view_advertise = findViewById(R.id.view_advertise);
		view_advertise.postDelayed(new Runnable() {
			@Override
			public void run() {
				eventHandler.sendEmptyMessage(EventHandler.MediaPlayerPlaying);
			}
		}, 5000);
		showOrHideController(true);
	}

	private final class ButtonClickListener implements View.OnClickListener {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_play:
				if (mLibVLC != null && mLibVLC.isPlaying()) {
					mLibVLC.pause();
					mSurface.setKeepScreenOn(false);
					v.setBackgroundResource(R.drawable.play_ctrl_played_bg);
				} else {
					mLibVLC.play();
					mSurface.setKeepScreenOn(true);
					v.setBackgroundResource(R.drawable.play_ctrl_pause_bg);
				}
				break;
			case R.id.btn_fullscreen:
				//				fullScreen();
				break;
			case R.id.btn_back:
				stopAndFinish();
				break;
			case R.id.btn_collect:
				AppService.addCollect(VLCPlayActivity.this, new TURL(url, title, icon));
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
			AppUtils.vibrate(VLCPlayActivity.this, 10);
		}
	}

	private void showOrHideController(boolean show) {
		if (!show) {
			view_control.setVisibility(View.GONE);
			view_title.setVisibility(View.GONE);
		} else {
			view_control.setVisibility(View.VISIBLE);
			view_title.setVisibility(View.VISIBLE);
			txt_time.setText(sdf.format(new Date()));
			mHandler.removeMessages(FADE_OUT);
			mHandler.sendEmptyMessageDelayed(FADE_OUT, 3000);
		}
	}

	private void play() {
		mSurface.setKeepScreenOn(true);
		playbutton.setBackgroundResource(R.drawable.play_ctrl_pause_bg);

		new Thread() {
			public void run() {
				p2pUrl = url;
				if (AppConstants.USE_P2P) {
					p2pUrl = AppUtils.toHexString(p2pUrl);
					p2pUrl = "http://127.0.0.1:17171/?m3u8=" + p2pUrl;
				}
				mLibVLC.readMedia(p2pUrl);
				Log.d(TAG, "play:" + p2pUrl);
				mHandler.sendEmptyMessageDelayed(DISMISS_PROGRESS, 15000);
			}
		}.start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public boolean onTouchMove(MotionEvent e1, MotionEvent e2) {
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

	public void dismissPic() {
		mVolume = -1;
		mBrightness = -1f;
		mVolumeBrightnessLayout.setVisibility(View.GONE);
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

	/**
	 * attach and disattach surface to the lib
	 */
	private final SurfaceHolder.Callback mSurfaceCallback = new Callback() {
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			if (format == PixelFormat.RGBX_8888)
				Log.d(TAG, "Pixel format is RGBX_8888");
			else if (format == PixelFormat.RGB_565)
				Log.d(TAG, "Pixel format is RGB_565");
			else if (format == ImageFormat.YV12)
				Log.d(TAG, "Pixel format is YV12");
			else
				Log.d(TAG, "Pixel format is other/unknown");
			mLibVLC.attachSurface(holder.getSurface(), VLCPlayActivity.this,
					width, height);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			mLibVLC.detachSurface();
		}
	};

	@Override
	public void setSurfaceSize(int width, int height, int sar_num, int sar_den) {
		if (width * height == 0)
			return;

		// store video size
		mVideoHeight = height;
		mVideoWidth = width;
		mSarNum = sar_num;
		mSarDen = sar_den;
		Message msg = mHandler.obtainMessage(SURFACE_SIZE);
		mHandler.sendMessage(msg);
	}

	private void initVLCVideo() {

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);

		mSurface = (SurfaceView) findViewById(R.id.surfaceView);
		mSurfaceHolder = mSurface.getHolder();

		int pitch;
		String chroma = pref.getString("chroma_format", "");
		if (Util.isGingerbreadOrLater() && chroma.equals("YV12")) {
			mSurfaceHolder.setFormat(ImageFormat.YV12);
			pitch = ImageFormat.getBitsPerPixel(ImageFormat.YV12) / 8;
		} else if (chroma.equals("RV16")) {
			mSurfaceHolder.setFormat(PixelFormat.RGB_565);
			PixelFormat info = new PixelFormat();
			PixelFormat.getPixelFormatInfo(PixelFormat.RGB_565, info);
			pitch = info.bytesPerPixel;
		} else {
			mSurfaceHolder.setFormat(PixelFormat.RGBX_8888);
			PixelFormat info = new PixelFormat();
			PixelFormat.getPixelFormatInfo(PixelFormat.RGBX_8888, info);
			pitch = info.bytesPerPixel;
		}

		mSurfaceAlign = 16 / pitch - 1;
		mSurfaceHolder.addCallback(mSurfaceCallback);
		try {
			mLibVLC = Util.getLibVlcInstance();
		} catch (LibVlcException e) {
			Log.d(TAG, "LibVLC initialisation failed");
			return;
		}

		EventHandler em = EventHandler.getInstance();
		em.addHandler(eventHandler);
		eventHandler.sendEmptyMessage(EventHandler.MediaPlayerPaused);

		txt_title.setText(title);
		AppUtils.vibrate(VLCPlayActivity.this, 5);
		mHandler.sendEmptyMessage(SHOW_PROGRESS);
		play();
	}

	private void changeSurfaceSize() {
		mSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);
		mSurface.invalidate();
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
		double density = (double) mSarNum / (double) mSarDen;
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
		LayoutParams lp = mSurface.getLayoutParams();
		lp.width = dw * alignedWidth / mVideoWidth;
		lp.height = dh;
		mSurface.setLayoutParams(lp);

		mSurface.invalidate();
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

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		setSurfaceSize(mVideoWidth, mVideoHeight, mSarNum, mSarDen);
		super.onConfigurationChanged(newConfig);
	}

	private void showProgress() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show(this, null, "加载中...");
			progressDialog.setCancelable(true);
		}
	}

	private void dismissProgress() {
		mHandler.removeMessages(DISMISS_PROGRESS);
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	/**
	 * Handle resize of the surface and the overlay
	 */
	private final Handler mHandler = new VideoPlayerHandler(this);

	private static class VideoPlayerHandler extends WeakHandler<VLCPlayActivity> {
		public VideoPlayerHandler(VLCPlayActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			VLCPlayActivity activity = getOwner();
			if (activity == null) // WeakReference could be GC'ed early
				return;
			switch (msg.what) {
			case FADE_OUT:
				activity.showOrHideController(false);
				break;
			case SHOW_PROGRESS:
				activity.showProgress();
				break;
			case DISMISS_PROGRESS:
				activity.dismissProgress();
				break;
			case SURFACE_SIZE:
				activity.changeSurfaceSize();
				break;
			case FADE_OUT_INFO:
				activity.fadeOutInfo();
				break;
			}
		}
	};

	/**
	 * Handle libvlc asynchronous events
	 */
	private final Handler eventHandler = new VideoPlayerEventHandler(this);

	private static class VideoPlayerEventHandler extends
	WeakHandler<VLCPlayActivity> {
		public VideoPlayerEventHandler(VLCPlayActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			final VLCPlayActivity activity = getOwner();
			if (activity == null) {
				return;
			}

			int event = msg.getData().getInt("event");

			switch (event) {
			case EventHandler.MediaPlayerPlaying:
				Log.i(TAG, "MediaPlayerPlaying");
				activity.setESTracks();
				break;
			case EventHandler.MediaPlayerPaused:
				Log.i(TAG, "MediaPlayerPaused");
				break;
			case EventHandler.MediaPlayerStopped:
				Log.i(TAG, "MediaPlayerStopped");
				break;
			case EventHandler.MediaPlayerEndReached:
				Log.i(TAG, "MediaPlayerEndReached");
				activity.play();
				break;
			case EventHandler.MediaPlayerVout:
				activity.handleVout(msg);
				break;
			case EventHandler.MediaPlayerPositionChanged:
				//				Log.i(TAG, "p:" + (msg.getWhen() - start) / 1000);
				break;
			case EventHandler.MediaPlayerEncounteredError:
				Log.i(TAG, "MediaPlayerEncounteredError");
				if (!AppUtils.checkNetworkState(activity)) {
					activity.showExitDialog("当前无网络连接，请检查网络设置！");
				} else {
					activity.showExitDialog("无法播放当前视频");
				}
				break;
			default:
				Log.e(TAG, String.format("Event not handled (0x%x)", event));
				break;
			}
		}
	};

	private void handleVout(Message msg) {
		if (msg.getData().getInt("data") == 0/* && !mEndReached */) {
			Log.i(TAG, "Video track lost, switching to audio");
		}
	}

	private void setESTracks() {
		mHandler.sendEmptyMessage(DISMISS_PROGRESS);
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

	private void fadeOutInfo() {
		if (mInfo.getVisibility() == View.VISIBLE)
			mInfo.startAnimation(AnimationUtils.loadAnimation(
					VLCPlayActivity.this, android.R.anim.fade_out));
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
		dismissProgress();

		if (mLibVLC != null && mLibVLC.isPlaying()) {
			mLibVLC.stop();
		}

		EventHandler em = EventHandler.getInstance();
		em.removeHandler(eventHandler);

		finish();
	}

	private void showExitDialog(String msg) {
		Dialog alertDialog = new AlertDialog.Builder(this)
		.setTitle("提示").setMessage(msg)
		.setPositiveButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				stopAndFinish();
			}
		}).create();
		alertDialog.show();
	}
}