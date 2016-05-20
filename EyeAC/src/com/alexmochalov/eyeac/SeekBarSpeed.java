package com.alexmochalov.eyeac;
 
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;

import com.alexmochalov.eyeac.DialogInfo.MyCallbackBv;
import com.alexmochalov.animation.*;
/**
 * 
 * @author @Alexey Mochalov
 *	Class SeekBarSpeed is a visual control
 * for a setting moving speed
 *
 */
public class SeekBarSpeed{
	private boolean mVisible = false;
	
	private int mWidth;
	private int mHeight;
	private RectF rect;
	
	private int mMax = 0;
	private int mProgress = 0;

	private Handler handlerProgress; 
	
	MyCallback callback = null;
	interface MyCallback
	{
		void progressChanged(int progress); 
	} 
	
	public SeekBarSpeed(){
		handlerProgress = new Handler();
	}
	
	public boolean isVisible(){
		return mVisible;
	}

	final int LeftDelta = 50;
	final int DHeight = 20;
	final int DHeight2 = 40;
	final int Radius = 40;
	
	public void draw(Canvas canvas, Paint paint, SurfaceViewScreen surface) {
		if (!mVisible) return;
		if (surface == null) return;

		float y = (mHeight-DHeight2)/mMax * mProgress + DHeight2;
		
		// All line
		paint.setColor(Color.GRAY);
		paint.setAlpha(200);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setStrokeWidth(5);
		canvas.drawLine(mWidth-10, DHeight,mWidth-10,mHeight-DHeight, paint);

		// Bold part of line  
		paint.setColor(Color.rgb(100, 200, 255));
		paint.setAlpha(200);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setStrokeWidth(10);
		canvas.drawLine(mWidth-10, y - DHeight,mWidth-10, mHeight, paint);
		
		// Fill circle  
		paint.setAlpha(255);
		paint.setColor(Color.DKGRAY);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(5);
		paint.setColor(Color.rgb(100, 200, 255));
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		canvas.drawCircle(mWidth - 10, y - DHeight, Radius, paint);
		
		// Border of the circle  
		paint.setColor(Color.rgb(50, 100, 128));
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawCircle(mWidth - 10, y - DHeight, Radius, paint);
		
		// Text (progress)  
		paint.setStrokeWidth(1);
		paint.setTextSize(24);
		paint.setColor(Color.YELLOW);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		String str = ""+(mMax-mProgress+1);
		Rect bounds = new Rect();
		paint.getTextBounds(str, 0, str.length(), bounds);
		canvas.drawText(str, mWidth - 10 - (bounds.width()), y - DHeight, paint);
		
	}

	public void setMax(int i) {
		mMax = i;
	}

	public void setProgress(int i) {
		mProgress = Math.max(i, 3);
		Log.d("","mp  "+mProgress);
	}

	public void setSize(int width, int height) {
		mWidth = width;
		mHeight = height;
		
		rect = new RectF(mWidth - LeftDelta, 0, mWidth, mHeight);
	}

	public void onTouchEvent(MotionEvent event) {
		if (! rect.contains(event.getX(), event.getY())){
			mVisible = false;
			return;
		}
			
		float Y = event.getY();
		Y = Math.max(Y , 0 + DHeight);
		Y = Math.min(Y , mHeight - DHeight);
		
		switch (event.getAction()){
		case MotionEvent.ACTION_DOWN:
			mProgress = (int)(event.getY() / (mHeight-DHeight2) * mMax);
			if (callback != null)
				callback.progressChanged(mProgress);
			return;
		case MotionEvent.ACTION_MOVE:
			mProgress = (int)(event.getY() / (mHeight-DHeight2) * mMax);
			if (callback != null)
				callback.progressChanged(mProgress);
			return;
		case MotionEvent.ACTION_UP:
			handlerProgress.removeCallbacksAndMessages(null);
			handlerProgress.postDelayed(hideProgressTask, 1000); 
			return;
		}
		
	}

	public void show() {
		mVisible = true;
	}

	public void hide() {
		mVisible = false;
	}

	public void addProgress(int i) {
		mProgress = mProgress + i;
		mProgress = Math.max(
			Math.min(mProgress, mMax), 1);
			
		//Log.d("","mProgress "+mProgress);
		if (callback != null)
			callback.progressChanged(mProgress);
	}
	
    private Runnable hideProgressTask = new Runnable() { 
		   public void run() { 
			   handlerProgress.removeCallbacks(hideProgressTask);
			   mVisible = false;
		   } 
		};        
	
	
}


