package com.alexmochalov.eyeac;
 
import android.content.*;
import android.content.SharedPreferences.*;
import android.content.res.Resources;
import android.graphics.*;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.*;
import android.view.*;
import android.view.MotionEvent.*;
import android.widget.*;

import com.alexmochalov.buttons.ButtonsList;
import com.alexmochalov.eyeac.MainActivity.Action;
import com.alexmochalov.eyeac.SurfaceViewScreenButtons.MessageType.*;
import com.alexmochalov.animation.*;

import java.util.*;
/**
 * @author Alexey Mochalov
 * SurfaceViewScreenButtons provides 
 * 
 *
 */
public class SurfaceViewScreenButtons extends SurfaceViewScreen {
	Resources resources;
	Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	// For moving and zooming of the face picture
	private static final String PREFS_shiftX = "PREFS_shiftX";
	private static final String PREFS_shiftY = "PREFS_shiftY";
	private static final String PREFS_ZOOM = "PREFS_ZOOM";
	 
	//private Context mContext;
	
	private DrawThreadMy drawThreadMy;
	  
	// Visual speed control
	private SeekBarSpeed seekBarSpeed = new SeekBarSpeed();
	// In the mode "Groups of moving"
	private int mGroupsCount = 0; // Count of the movings in the group
	private int mGroupsCountRight = 0; // Count of the proper users touchings
    // In the mode "Continious moving"
	private int mRightCount = 0; // Count of the proper users touchings
    
	private int mSignal; // Kind of the signal on theb
	private Vibrator vibrator;
	// A message on the bottom of the screen
	private String message = "";
	private static MType messageType;
	
	private int textTopShift; // Shift text on the top buttons when the acrion bar is visible

	private int mFaceNumber;
	private int mMode;
	
	// Position of the previous touch (for zoom and shift mode)
	private float x0;
	private float y0;
	private double distance = 0; // Distance between fingers 
	private Point center = new Point();
	private double kZooming = 1;

	private SharedPreferences mPrefs;
	
	public void setTextTopShift(int shift)
	{
		textTopShift = shift;
	}
	/**
	* Store preferences
	*/
	public void setPrefs(SharedPreferences prefs)
	{
		mPrefs = prefs;
	}
	
	/**
	* Set type of the message on the buttom of the screen
	*/
	public static class MessageType{
		enum MType{info, ok, ups}
	}
	
	OnEventListener listener;
	public interface OnEventListener{
		public void onTouchDown(String VAC);
		public void onTouchUp();
	}
	
	/*
	* Creates a text string from the results data
	*/
	public String getResultStr()
	{
		if (this.isRandom()){
			int count = getCount();
			if (count == 0)
				return resources.getString(R.string.noresult);
			else return (resources.getString(R.string.resultstr)+" <b>"+
					(int)((float)mRightCount/count*100f)+"</b>% ("+mRightCount+"/"+count+")");
			
		} else
		if (this.isGroupAny()){
			if (mGroupsCount == 0)
				return resources.getString(R.string.noresult);
			else return ( resources.getString(R.string.resultstr)+" <b>"+
					(int)((float)mGroupsCountRight/mGroupsCount*100f)+"</b>% ("+mGroupsCountRight+"/"+mGroupsCount+")");
		} else 
			return "Select mode Continious or Sets of movements";
	}
	
	public SurfaceViewScreenButtons(Context context, AttributeSet attrs) {
		super(context, attrs);
		vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		resources = context.getResources();
	}

	public SurfaceViewScreenButtons(Context context) {
        super(context);
		vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		resources = context.getResources();
    }
	
	public void setSeekBarSpeedRect(int width, int height){
		seekBarSpeed.setSize(width, height);
	}
		
	/*
	* Create the draw thread
	*/
	public void createDrawThread1(){
    	if (drawThreadMy == null){
        	drawThreadMy = new DrawThreadMy(getHolder(), getElements(), this);
        	drawThreadMy.setRunning(true);
        	drawThreadMy.start();
        	
        	if (mMode == 999)
				// In the mode "Coordinates" no zooming
        		drawThreadMy.shift(
        				mPrefs.getFloat(PREFS_shiftX, 0), 
        				mPrefs.getFloat(PREFS_shiftY, 0), 
        				1);
        	else
        		drawThreadMy.shift(
        				mPrefs.getFloat(PREFS_shiftX, 0), 
        				mPrefs.getFloat(PREFS_shiftY, 0), 
        				mPrefs.getFloat(PREFS_ZOOM, 1));
        	
    		//drawThreadMy.shift(0,0,1);
    		// <item>Get eyes coordinates (for designer)</item>
    		drawThreadMy.commitShift();
        	
    	}
	}
	
	public void setShift(){
		drawThreadMy.setshift(isMovedResized);
	}
	
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
        // finish the thread wirking
    	if (drawThreadMy != null){
			// Save parameters the image
			Editor editor = mPrefs.edit();
			editor.putFloat(PREFS_shiftX, drawThreadMy.getShiftX());
			editor.putFloat(PREFS_shiftY, drawThreadMy.getShiftY());
			editor.putFloat(PREFS_ZOOM, drawThreadMy.getZoom());
			editor.apply();
		
    		drawThreadMy.setRunning(false);
            while (retry) {
                try {
                	drawThreadMy.join();
                    retry = false;
                } catch (InterruptedException e) {
                    // try again ang again
                }
            }
            drawThreadMy = null;
    	}
    	
    	listener = null;
    	ButtonsList.clearAll();
		super.surfaceDestroyed(holder);
    }
	
    public void setFaceNumber(int faceNumber)
	{
		mFaceNumber = faceNumber;
	}
	
    public void setFaceNumberAndReset(SharedPreferences prefs, int faceNumber, String FACE_NUMBER)
	{
		mFaceNumber = faceNumber;
		clearElements();
		setElements(Params.width, Params.height);
		
		Editor editor = prefs.edit();
		editor.putInt(FACE_NUMBER, faceNumber);
		editor.apply();
	}
	
    public void setMode(int mode)
	{ 
		mMode = mode;
		super.setMode(mMode);
	}
	
	@Override
	/*
	* Set visual elementsp: screen buttons, face image, seek bar
	* Create draw thread
	*/
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
							   int height) {
		Params.width = width;
		Params.height = height;
		  
		ButtonsList.setButtonsRects(width, height, allDirections());
		setSeekBarSpeedRect(width, height);
  
		setElements(width, height);
		
    	createDrawThread1();
    	
		ButtonsList.listener = new ButtonsList.OnEventListener() {
			@Override
			public void onTouchDown(String VAC) {
				if (listener != null)
					listener.onTouchDown(VAC);
			}

			@Override
			public void onTouchUp() {
				if (listener != null)
					listener.onTouchUp();
			}
		};
	} 
  	
	private void setElements(int width, int height) {
		//Log.d("","Face number "+mFaceNumber);
		if (mFaceNumber == 0)
			addFaceElements2(resources, width, height, 65, R.drawable.face01, null, R.array.Dir0R, R.array.Dir0L);
		else if (mFaceNumber == 1)
			addFaceElements2(resources, width, height, 38, R.drawable.face11, null, R.array.Dir2R, R.array.Dir2L);
		else if (mFaceNumber == 2)
			addFaceElements2(resources, width, height, 38, R.drawable.face21, null, R.array.Dir3R, R.array.Dir3L);
		//else if (mFaceNumber == 21)
		//	addFaceElements2(width, height, 82, R.drawable.face3, 
		//			BitmapFactory.decodeResource(mContext.getResources(), R.drawable.pupil31),
		//			R.array.Dir3R, R.array.Dir3L);
			
		// Get prefetences
		float shiftX = mPrefs.getFloat(PREFS_shiftX, 0); 
		float shiftY = mPrefs.getFloat(PREFS_shiftY, 0); 
		float zoom = mPrefs.getFloat(PREFS_ZOOM, -1);

		if (zoom == -1){
			// First start: align image to the center of the screen
			Editor editor = mPrefs.edit();
			zoom = Math.min(width, height)/getFaceWidth();
			if (width < height){
				shiftY = (height - (getFaceWidth()*zoom))/2;
				shiftX = 0;
			}	
			else if (width > height){
				shiftX = (width - (getFaceWidth()*zoom))/2;
				shiftY = 0;
			}	
						
			editor.putFloat(PREFS_shiftX, shiftX);
			editor.putFloat(PREFS_shiftY, shiftY);
			editor.putFloat(PREFS_ZOOM, zoom);
			editor.apply();
		}
		// Reset image coordinates if image is out of screen bounds
		if (shiftX + 1024 * zoom < 0 || shiftY + 1024 * zoom < 0 || shiftX > width-50 || shiftY > height - 50 ){
			Editor editor = mPrefs.edit();
			editor.putFloat(PREFS_shiftX, 0);
			editor.putFloat(PREFS_shiftY, 0);
			editor.putFloat(PREFS_ZOOM, 1);
			editor.apply();
		}
	}
	/*
	* This method is called from the fraw thread
	*/
	public void draw(Canvas canvas, Paint paint) {
    	// Draw buttons
		ButtonsList.draw(canvas, paint, this, textTopShift);
		// Draw seek bar
		seekBarSpeed.draw(canvas, paint, this);
		// Draw message
		if (message.length() > 0){
			paint.setColor(Params.colorMessageText);
			paint.setTextSize(30);
			
			Rect bounds = new Rect();
			paint.getTextBounds(message, 0, message.length(), bounds);
			
			if (messageType == MType.ok)
				paint.setColor(Color.GREEN);
			else if (messageType == MType.ups)
				paint.setColor(Color.RED);
			else
				paint.setColor(Color.WHITE);
			
			int i = (getWidth()-bounds.width())/2;
			int j = bounds.height();
			
			//bounds.shift(i, getHeight()-(j*2));
			
			bounds.left = bounds.left+i-10; 
			bounds.right = bounds.right+i+7; 
			bounds.top = getHeight()-(j*3); 
			bounds.bottom = getHeight()-3; 
			
			paint.setStyle(Paint.Style.FILL_AND_STROKE);
			canvas.drawRect(bounds, paint);

			paint.setStyle(Paint.Style.STROKE);
			paint.setColor(Color.DKGRAY);
			canvas.drawRect(bounds, paint);
			
			paint.setStyle(Paint.Style.FILL_AND_STROKE);
			if (messageType == MType.ok)
				paint.setColor(Color.BLUE);
			else if (messageType == MType.ups)
				paint.setColor(Color.WHITE);
			else
				paint.setColor(Color.BLACK);
			paint.setStrokeWidth(1);
			canvas.drawText(message, (getWidth()-paint.measureText(message))/2 , getHeight()-30, paint);
		}
		
	}
 
	public int getRightCount(){
		return mRightCount;
	}
	 
	public void resetRightCount(){
		mRightCount = 0;
	}
	
	/*
	* Calculates a distance between two fingers
	*/
	private double distance(PointerCoords center, PointerCoords coord){
		Float minX = Math.min(center.x, coord.x);
		Float maxX = Math.max(center.x, coord.x);
		Float x2 = (maxX-minX)*(maxX-minX);
		Float minY = Math.min(center.y, coord.y);
		Float maxY = Math.max(center.y, coord.y);
		Float y2 = (maxY-minY)*(maxY-minY);

		return  Math.sqrt(x2 + y2);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getActionMasked();
  
		float x1 = (int) event.getX();
		float y1 = (int) event.getY();
		
		int Y = (int)y1;
		int X = (int)x1;

		if (isCoords()) {
			// The mode of defining coordinates
			float zoom = drawThreadMy.getZoom();	
			setCoord(X, 
					Y,  
					drawThreadMy.getShiftX()+ (1000*zoom)/2);
			
			message = ""+drawThreadMy.getElementshiftXY(0)+" - "+drawThreadMy.getElementshiftXY(1);
			return true;
		}
		
		if (seekBarSpeed.isVisible()){
			// Seek bar processed this event
			seekBarSpeed.onTouchEvent(event);
			x0 = x1;
			y0 = y1;
			return true;
		}
		
        if (action == MotionEvent.ACTION_DOWN) {
			if (this.canPressBytton()){
				String VAK = ButtonsList.ACTION_DOWN(X, Y);
				if (VAK != null){
					if (modeIsToButton()){
						// Add VAC to the list
						move(VAK, false);
						if (listener != null)
							listener.onTouchDown(VAK);
					}else 
					// Check VAC
					if (choiceOfDirIsProper(VAK)){
	    				mRightCount++;
	    				if (mSignal == 1){
	    					vibrator.vibrate(50);
	    				}
	    			}
				}
			};
        } else if (action == MotionEvent.ACTION_POINTER_DOWN){
			if (event.getPointerCount() == 2){
				// Store pointers positions
				// for shift and zoom
				PointerCoords coord0 = new PointerCoords(); // First finger coordinates
				PointerCoords coord1 = new PointerCoords(); // Second finger coordinates
				event.getPointerCoords(0, coord0);
				event.getPointerCoords(1, coord1);
				
				center.x = (int)((coord0.x+coord1.x)/2);
				center.y = (int)((coord0.y+coord1.y)/2);
				
				distance = distance(coord0, coord1)/10;

				x0 = center.x;
				y0 = center.y;
				
				return true;
			}
        } else if (action == MotionEvent.ACTION_POINTER_UP){
			if (event.getPointerCount() == 2){
				PointerCoords coord0 = new PointerCoords();
				PointerCoords coord1 = new PointerCoords();
				
				event.getPointerCoords(0, coord0);
				event.getPointerCoords(1, coord1);
				
				// Log.d("", "= "+coord0.x+"  "+coord0.y);
				// Log.d("", "= "+coord1.x+"  "+coord1.y);
				// Log.d("", "= "+X+"  "+Y);
				
				int pointerIndex = event.getActionIndex();
				
				x0 = event.getX(pointerIndex);
				y0 = event.getY(pointerIndex);
				
				x0 = -999;
				return true;
				
			}
        } else if (action == MotionEvent.ACTION_MOVE) {
        	if (! canPressBytton() && isMovedResized){ // 
				if (event.getPointerCount() == 1 || mMode == 999){
					// shift and zoom finished
					if (drawThreadMy != null && x0 != -999)
						drawThreadMy.shift(x1 - x0, y1 - y0, -1);
		
					x0 = x1;
					y0 = y1;
				} else {
					PointerCoords coord0 = new PointerCoords(); // First finger coordinates
					PointerCoords coord1 = new PointerCoords(); // Second finger coordinates
					event.getPointerCoords(0, coord0);
					event.getPointerCoords(1, coord1);
					double distance1 = distance(coord0, coord1)/10;

					center.x = (int) ((coord0.x+coord1.x)/2);
					center.y = (int) ((coord0.y+coord1.y)/2);
					
					if (distance != 0) // && k * distance1/distance > 0.3 && k * distance1/distance < 5
						kZooming = distance1/distance;
					else 
						kZooming = -1;
					
					
					if (drawThreadMy != null)
						drawThreadMy.shift(center.x - x0, center.y - y0, kZooming);
						
					if (kZooming != -1)
						distance = distance1;		
						
					x1 = center.x;
					y1 = center.y;
					
					x0 = center.x;
					y0 = center.y;

					return true;
				} }
				else    
				if (modeIsToButton()){
					/*
					if (buttonSelected == null){
						if (selectButton(X, Y)){
							move(buttonSelected.getVAK(), false);
							if (listener != null)
								listener.onTouchDown(buttonSelected.getVAK());
						}

					} else{  
						ButtonS b = buttonSelected;
						if (selectButton(X, Y)){
							if (b != buttonSelected){
								returnToCenter();
								ButtonsList.buttonUp();
								
								move(buttonSelected.getVAK(), false);
								if (listener != null)
									listener.onTouchDown(buttonSelected.getVAK());
							}
						} else { 
							returnToCenter();
							ButtonsList.buttonUp();
						}
					}
*/
					return true;


				
			} 
        } else if (action == MotionEvent.ACTION_UP) {
    		x0 = (int) event.getX();
    		y0 = (int) event.getY();
			distance = 0;
        	
			if (isMovedResized) return true;
        	//resize = false;
        	
        	if (modeIsToButton()){
        		returnToCenter();
        		ButtonsList.ACTION_UP();
				if (listener != null)
					listener.onTouchUp();
                return true;
        	}
        	
        	if (isGroup() && !mPause){
            	if (!ButtonsList.ACTION_UP())
            		;//pauseCont();
        		return true;
        	}
        	
			if (isWaiting()){
        		ButtonsList.ACTION_UP();
        		return true;
        	}
        	
			if (mPause){
        		restartMoving();
        		return true;
        	}   
			
        	if (ButtonsList.ACTION_UP()){
	        	invalidate();
        	} else {
				pause();
				return true;
			}
        	
        }
        	
		x0 = x1;
		y0 = y1;
        
		return true; // false?
	}

	public void pause() {
		super.pause();
	}
  
	public void pauseCont() {
		super.pauseCont();
	}

	public void cont() {
		super.cont();
		if (drawThreadMy != null){
			;
		}	
	}

	public void incPeriod() {
		mPeriod += 10;
		setPeriod(mPeriod);
	}

	public void decPeriod() {
		mPeriod -= 10;
		setPeriod(Math.max(mPeriod, 10));
	}

	public void setSignal(String signal) {
		mSignal = Integer.parseInt(signal);
	}
 
	public void resetRects() {
		ButtonsList.setButtonsRects(getWidth(), getHeight(), allDirections());
		setSeekBarSpeedRect(getWidth(), getHeight());
	}
	
	public void setMessage(String m, MType t) {
		message = m;
		messageType = t;
	}

	public void setGroupsCount(int groupsCount){
		mGroupsCount = groupsCount;
	}
	
	public void incGroupsCount(){
		mGroupsCount++;
	}

	public void setGroupCountRight(int groupsCountRight){
		mGroupsCountRight = groupsCountRight;
	}
	
	public void incGroupCountRight(){
		mGroupsCountRight++;
	}
	public void setMaxSpeed(int i) {
		seekBarSpeed.setMax(i);
	}
	public void setProgressSpeed(int i) {
		seekBarSpeed.setProgress(i);
	}
	public void showSeekBarSpeed() {
		seekBarSpeed.show();
		seekBarSpeed.callback = new SeekBarSpeed.MyCallback(){
			@Override
			public void progressChanged(int progress) {
				setPeriod(progress);
			}};
	}
	public void addSeekBarProgressSpeed(int i) {
		seekBarSpeed.addProgress(i);
	}
	public int getMode() {
		return mMode;
	}

	
	//public void setAllDirections(boolean boolean1) {
	//	super.setAllDirections(boolean1);
	//}

	
}
