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
 * Class DrawThreadMy provides a loop of animation and drawing of the elements
 *
 */
public class DrawThreadMy extends Thread{
	private boolean runFlag = false; // While runFlag the thread repeats   
	private SurfaceHolder surfaceHolder; // Where to draw
	private ArrayList <Element> elements; // What to draw

	protected Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	private boolean mMoving = false; // State is moving (when user moves picture on the surface)
	
	private SurfaceViewScreenButtons surfaceViewScreenButtons; // 
	
	private float shiftX = 0;
	private float shiftY = 0;
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

public float getShiftY()
{
return shiftY;
}

public float getShiftX()
{
return shiftX;
}

public String getElementshiftXY(int i)
{
	return ""+ (elements.get(i).getShiftX() - getShiftX())+" "+
			   (elements.get(i).getShiftY() - getShiftY());
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
	
	public void setshift(Boolean shift){
		if (mMoving && !shift)
    		for (Element b: elements)
    			b.commitShift(shiftX, shiftY, mZoom);
			
		mMoving = shift;
	}
	
	public void commitShift(){
    	for (Element b: elements)
    		b.commitShift(shiftX, shiftY, mZoom);
	}
	
	/**
	* Draw all visual elements
	**/
    private synchronized void draw(Canvas canvas)
    {
	    // Fill Canvas with background color
    	canvas.drawColor(Params.colorSurfaceBg);

		// Draw elements of the face
    	for (Element b: elements)
    		b.draw(canvas, mMoving, shiftX, shiftY, mZoom);
    	
		// Draw screen buttons, scroll bar and so.	
		surfaceViewScreenButtons.draw(canvas, paint);
		
		paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setTextSize(32);
		
		// Debugging
		/*canvas.drawLine(getshiftX()+
						(1000*
						getZoom())/2,
						0,
						getshiftX()+
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

	public void shift(float dx, float dy, double k) {
		//Log.d("", "shiftY "+shiftY+"  "+dx);
		shiftX = shiftX + dx;
		shiftY = shiftY + dy;
		if (k > 0 )
		{	
			mZoom = mZoom * k;
		}
	}


}
