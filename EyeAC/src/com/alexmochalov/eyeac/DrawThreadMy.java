package com.alexmochalov.eyeac;


import java.util.ArrayList;

import com.alexmochalov.animation.Element;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.graphics.*;

/**
 * 
 * @author Alexey Mochalov
 * 
 * This class is Thread of animation elements and drawing on the surface
 *
 */
public class DrawThreadMy extends Thread{
	private boolean runFlag = false; // While runFlag the thread repeats   
	private SurfaceHolder surfaceHolder; // Where to draw
	private ArrayList <Element> elements; // What to draw

	protected Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	private boolean mMoving = false; // State is moving (when user moves picture on the surface)
	
	private SurfaceViewScreenButtons surfaceViewScreenButtons; // 
	
	private float offsetX = 0;
	private float offsetY = 0;
	private double mZoom = 1;

	private String message = "";
	
    public DrawThreadMy(SurfaceHolder surfaceHolder, ArrayList<Element> elements,
    		SurfaceViewScreenButtons surfaceViewScreenButtons){
    	 this.surfaceHolder = surfaceHolder;;
        this.elements = elements;
        this.surfaceViewScreenButtons = surfaceViewScreenButtons;
		
    }

public float getZoom()
{
	return (float)mZoom;
}

public float getOffsetY()
{
return offsetY;
}

public float getOffsetX()
{
return offsetX;
}

public String getElementOffsetXY(int i)
{
	return ""+ (elements.get(i).getOffsetX() - getOffsetX())+" "+
			   (elements.get(i).getOffsetY() - getOffsetY());
}

	public void setRunning(boolean run) {
        runFlag = run;
    }
 
    @Override
    public void run() {
        Canvas canvas;
         
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        while (runFlag) {
            canvas = null;
            try {
                // get the Canvas and start drawing
                for (Element e:elements)
                   	e.move();
            	
                canvas = surfaceHolder.lockCanvas(null);
                synchronized (surfaceHolder) {
                	if (canvas != null){
                		draw(canvas);
                	}
                }
            }
            
            finally {
                if (canvas != null) {
                    // drawing is finished
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
            /*
            try {
                Thread.sleep(10);
                // Do some stuff
              } catch (Exception e) {
                e.getLocalizedMessage();
            }   
           */ 
        }
    }
    
	public void setMessage(String pMessage){
		message = pMessage;
	}
	
	public void setOffset(Boolean offset){
		if (mMoving && !offset)
    		for (Element b: elements)
    			b.commitOffset(offsetX, offsetY, mZoom);
			
		mMoving = offset;
	}
	
	public void commitOffset(){
    	for (Element b: elements)
    		b.commitOffset(offsetX, offsetY, mZoom);
	}
	
	
    private synchronized void draw(Canvas canvas)
    {
	    // Fill Canvas with background color
    	canvas.drawColor(Params.colorSurfaceBg);

    	for (Element b: elements)
    		b.draw(canvas, mMoving, offsetX, offsetY, mZoom);
    	//for (int i = elements.size()-1; i>=0; i--)
    	//	elements.get(i).draw(canvas, mMoving, offsetX, offsetY, mZoom);
    
			
		surfaceViewScreenButtons.draw(canvas, paint);
		
    	// Draw message
		//surfaceViewScreenButtons.draw(canvas);
		
		paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setTextSize(32);
		
		/*canvas.drawLine(getOffsetX()+
						(1000*
						getZoom())/2,
						0,
						getOffsetX()+
						(1000*
						getZoom())/2,
						1000,
						paint
						
		);
		*/
		//canvas.drawText(message,10,300,paint);
		
		/*
		if (surfaceViewScreenButtons != null){
			paint.setColor(Color.GREEN);
			paint.setTextSize(60);
			canvas.drawText(surfaceViewScreenButtons.getCurrentDirStr(), 15, 50, paint);
			canvas.drawText(""+surfaceViewScreenButtons.getCount(), 15, 90, paint);
			canvas.drawText(""+surfaceViewScreenButtons.getRightCount(), 15, 130, paint);
		}*/
    }

	public void offset(float dx, float dy, double k) {
		Log.d("", "offsetY "+offsetY+"  "+dx);
		offsetX = offsetX + dx;
		offsetY = offsetY + dy;
		if (k > 0 )
		{	
			mZoom = mZoom * k;
		}
	}


}
