package com.alexmochalov.animation;

import android.graphics.*;

public abstract class Element{
	protected float x; 
	protected float y;
	protected int color;
	
	protected Bitmap bitmapInitial; // Initial picture
	protected Bitmap bitmap;     // Scaled picture

	protected Rect rectInitial;
	
	
	//private float[] steps;
	//private int stepNumber;
	
	public Element(PointF point, int color) {
		if (point != null){
			this.x = point.x;
			this.y = point.y;
		}
		this.color = color;
		
	}

	public float getOffsetX()
	{
		return x;
	}
	
	public float getOffsetY()
	{
		return y;
	}
	
	public float getWidth()
	{
		return 0;
	}
	
	public void move() {
	}	
	
	public synchronized void commitOffset(float dx, float dy, double zoom) {
	}	
		
	public void draw(Canvas canvas, boolean move, float newX, float newY, double zoom) {
	}

	public void stop() {
	}

	public void cont() {
		
	}

}
