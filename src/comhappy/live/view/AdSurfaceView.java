package comhappy.live.view;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class AdSurfaceView extends SurfaceView implements
		SurfaceHolder.Callback {

	Activity activity;
	SurfaceHolder holder;
	boolean isShowing = false;

	public AdSurfaceView(Context context) {
		super(context);
		init(context);
	}

	public AdSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
		init(context);
	}

	public AdSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		this.activity = (Activity) context;
		holder = this.getHolder();
		holder.addCallback(this);
	}

	protected void drawAD() {
		Canvas canvas = holder.lockCanvas();// 锁定画布
		Paint p = new Paint();
		p.setColor(0xffffffff);
		canvas.drawRect(canvas.getClipBounds(), p);
		p.setColor(0xFFFF0000);
		p.setTextSize(44);
		canvas.drawText("广告" + new Date().getSeconds(), 255, 255, p);
		holder.unlockCanvasAndPost(canvas);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		isShowing = true;
		new RefreshThread().start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		isShowing = false;
	}

	private class RefreshThread extends Thread {
		@Override
		public void run() {
			while (isShowing) {
				try {
					drawAD();
					Thread.sleep(100);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
