package com.alexmochalov.animation;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.*;

/**
 * 
 * @author @Alexey Mochalov
 * Class Face provides drawing, moving and zooming of the image. 
 */
public class ElementFace2 extends Element{
	private boolean mEyesArrived = false; // To synchronize eyes moving
	
	public ElementFace2(Resources resources, int color, int faceID){
		super(new PointF(0, 0), color);
		
		Options opts = new Options();
		opts.inScaled = false;
		
		bitmapInitial = BitmapFactory.decodeResource(resources, faceID, opts);
		rectInitial = new Rect(0, 0, bitmapInitial.getWidth(), bitmapInitial.getHeight());
		
		bitmap = Bitmap.createScaledBitmap(bitmapInitial, 
				bitmapInitial.getWidth(), 
				bitmapInitial.getHeight(), false);
	}

	/**
	 * Sets coordinates and create zoomed bitmap when offset and zooming finished
	 */
	@Override
	public void commitShift(float newX, float newY, double zoom) {
		bitmap = Bitmap.createScaledBitmap(bitmapInitial, 
				(int)(bitmapInitial.getWidth() * zoom), 
				(int)(bitmapInitial.getHeight() * zoom), 
				false);
		
		x = newX;
		y = newY;
		
	}	
	
	public boolean isFace()
	{
		return true;
	}
	
	@Override
	public float getWidth()
	{
		return rectInitial.width();
	}
	
	/**
	 * Draw face
	 */
	@Override
	public void draw(Canvas canvas, boolean offsetZoom, float newX, float newY, double zoom) {
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		if (offsetZoom){
			// Drawing when user offset and zoom the image	
			RectF rect = new RectF(0, 0,  
					(int)(bitmapInitial.getWidth() * zoom), 
					(int)(bitmapInitial.getHeight() * zoom));
			rect.offsetTo(newX, newY);
			canvas.drawBitmap(bitmapInitial, rectInitial, rect, paint);
		} else {
			// Drawing in normal mode	
			canvas.drawBitmap(bitmap, x, y, paint);
		}
	}
	
	public boolean getEyesArrived(){
		return mEyesArrived;
	}
	
	public void setEyesArrived(boolean p){
		mEyesArrived = p;
	}

	@Override
	public void move() {
	}

}
