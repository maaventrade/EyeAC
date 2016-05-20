package com.alexmochalov.animation;
 
import android.graphics.*;

/**
 * 
 * @author Alexey Mochalov
 * Class Element is base graphical class
 *
 */
public abstract class Element{
	// Screen coordinates
	protected float x; 
	protected float y;
	protected int color; // isn't used
	
	protected Bitmap bitmapInitial; // Initial picture
	protected Bitmap bitmap;     // Scaled picture

	protected Rect rectInitial; // rectangle of the initial picture

	boolean mMoved; // is eye moved
	
	public Element(PointF point, int color) {
		if (point != null){
			this.x = point.x;
			this.y = point.y;
		}
		this.color = color;
	}

	public float getShiftX()
	{
		return x;
	}
	
	public float getShiftY()
	{
		return y;
	}
	
	public abstract float getWidth();
	
	public abstract void move();
	
	/**
	 * Set coordinates and size after shifting and zooming.
	 * @param dx horizontal offset 
	 * @param dy vertical offset
	 * @param zoom 
	 */
	public synchronized void commitShift(float dx, float dy, double zoom) {
	}	
		
	/**
	 * 
	 * @param canvas is Canvas of the SurfaceView
	 * @param offsetZoom - is the element offseted and zoomed 
	 * @param newX - coordinate when image is offseted
	 * @param newY - coordinate when image is offseted
	 * @param zoom - zoom when image is offseted
	 */
	public abstract void draw(Canvas canvas, boolean offsetZoom, float newX, float newY, double zoom);

	public void stopMoving()
	{
		mMoved = false;
	}

	public void continueMoving()
	{
		mMoved = true;
	}
	
}
