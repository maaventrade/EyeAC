package com.alexmochalov.eyeac;

import android.content.*;
import android.content.SharedPreferences.*;
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

public class SurfaceViewScreenButtons extends SurfaceViewScreen {
	Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
	 
	private static final String PREFS_OFFSETX = "PREFS_OFFSETX";
	private static final String PREFS_OFFSETY = "PREFS_OFFSETY";
	private static final String PREFS_ZOOM = "PREFS_ZOOM";
	 
	private Context mContext;
	
	private DrawThreadMy drawThreadMy;
	  
	private String fileName = "";
	   

	private SeekBarSpeed seekBarSpeed = new SeekBarSpeed();
	
	private RectF[] rectsSaved = new RectF[2];
	
	private int groupsCount = 0;
	private int groupsCountRight = 0;
	  
//	private int period = 50;
	
    private float scale;
    
	private int rightCount = 0;
    
	private int signal;
	private Paint paint;
	
	//MyCallback callback = null;
	private Vibrator vibrator;
	
	private String message = "";
	
	int textTopShift;

	public void setTextTopShift(int shift)
	{
		textTopShift = shift;
	}

	public void setPrefs(SharedPreferences prefs)
	{
		mPrefs = prefs;
	}
	public static class MessageType{
		enum MType{info, ok, ups}
	}
	
	private static MType messageType;	

	private int faceNumber;
	private int mode;
	
	private float x0;
	private float y0;

	private double distance = 0; // Distance between fingers 

	private Point center = new Point();

	private double kZooming = 1;
	
	private SharedPreferences mPrefs;


	OnEventListener listener;
	public interface OnEventListener{
		public void onTouchDown(String VAC);
		public void onTouchUp();
	}
	
	public String getResultStr()
	{
		if (this.isRandom()){
			int count = getCount();
			if (count == 0)
				return mContext.getResources().getString(R.string.noresult);
			else return ( mContext.getResources().getString(R.string.resultstr)+" <b>"+
				(int)((float)rightCount/count*100f)+"</b>% ("+rightCount+"/"+count+")");
			
		} else
		if (this.isGroupAny()){
			if (groupsCount == 0)
				return mContext.getResources().getString(R.string.noresult);
			else return ( mContext.getResources().getString(R.string.resultstr)+" <b>"+
				(int)((float)groupsCountRight/groupsCount*100f)+"</b>% ("+groupsCountRight+"/"+groupsCount+")");
		} else 
			return "Select mode Continious or Sets of movements";
		
			
	}
	
	public SurfaceViewScreenButtons(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public SurfaceViewScreenButtons(Context context) {
        super(context);
    }
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public void setParams() {
		vibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
	}
		
	
	
	public void setSeekBarSpeedRect(int width, int height){
		seekBarSpeed.setSize(width, height);
	}
		
	public void createDrawThread1(){
    	if (drawThreadMy == null){
        	drawThreadMy = new DrawThreadMy(getHolder(), getElements(), this);
        	drawThreadMy.setRunning(true);
        	drawThreadMy.start();
        	
        	if (mode == 999)
        		drawThreadMy.offset(
        				mPrefs.getFloat(PREFS_OFFSETX, 0), 
        				mPrefs.getFloat(PREFS_OFFSETY, 0), 
        				1);
        	else
        		drawThreadMy.offset(
        				mPrefs.getFloat(PREFS_OFFSETX, 0), 
        				mPrefs.getFloat(PREFS_OFFSETY, 0), 
        				mPrefs.getFloat(PREFS_ZOOM, 1));
        	
    		//drawThreadMy.offset(0,0,1);
    		// <item>Get eyes coordinates (for designer)</item>
    		drawThreadMy.commitOffset();
        	
    	}
	}
	
	public void setOffset(){
		drawThreadMy.setOffset(isMovedResized);
	}
	
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
        // finish the thread wirking
    	if (drawThreadMy != null){
			// Save parameters the image
			Editor editor = mPrefs.edit();
			editor.putFloat(PREFS_OFFSETX, drawThreadMy.getOffsetX());
			editor.putFloat(PREFS_OFFSETY, drawThreadMy.getOffsetY());
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
		super.surfaceDestroyed(holder);
    }
	
    public void setFaceNumber(int param)
	{
		faceNumber = param;
	}
	
    public void setMode(int param)
	{ 
		mode = param;
		super.setMode(param);
	}
	
	@Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
							   int height) {
		Params.width = width;
		Params.height = height;
		  
		ButtonsList.setButtonsRects(width, height, allDirections());
		setSeekBarSpeedRect(width, height);
  
		setParams();
		/*
		if (width >= height)
			scale = 0.58f;
		else 
			scale = 0.65f;
		*/
		scale = 1;
		
		Log.d("","Face number "+faceNumber);
		if (faceNumber == 0)
			addFaceElements2(width, height, 65, R.drawable.face0, null, R.array.Dir0R, R.array.Dir0L);
		else if (faceNumber == 1)
			addFaceElements2(width, height, 38, R.drawable.face21, null, R.array.Dir2R, R.array.Dir2L);
		else if (faceNumber == 2)
			addFaceElements2(width, height, 82, R.drawable.face32, null, R.array.Dir2R, R.array.Dir2L);
		else if (faceNumber == 21)
			addFaceElements2(width, height, 82, R.drawable.face3, 
					BitmapFactory.decodeResource(mContext.getResources(), R.drawable.pupil31),
					R.array.Dir3R, R.array.Dir3L);
			
		
		float offsetX = mPrefs.getFloat(PREFS_OFFSETX, 0); 
		float offsetY = mPrefs.getFloat(PREFS_OFFSETY, 0); 
		float zoom = mPrefs.getFloat(PREFS_ZOOM, -1);

		if (zoom == -1){
			// First start: align image to the center of the screen
			Editor editor = mPrefs.edit();
			zoom = Math.min(width, height)/getFaceWidth();
			if (width < height){
				offsetY = (height - (getFaceWidth()*zoom))/2;
				offsetX = 0;
			}	
			else if (width > height){
				offsetX = (width - (getFaceWidth()*zoom))/2;
				offsetY = 0;
			}	
						
			editor.putFloat(PREFS_OFFSETX, offsetX);
			editor.putFloat(PREFS_OFFSETY, offsetY);
			editor.putFloat(PREFS_ZOOM, zoom);
			editor.apply();
		}
		
		if (offsetX + 1024 * zoom < 0 || offsetY + 1024 * zoom < 0 || offsetX > width-50 || offsetY > height - 50 ){
			Editor editor = mPrefs.edit();
			editor.putFloat(PREFS_OFFSETX, 0);
			editor.putFloat(PREFS_OFFSETY, 0);
			editor.putFloat(PREFS_ZOOM, 1);
			editor.apply();
		}
		
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
  
	public void draw(Canvas canvas, Paint paint) {
    	// Draw buttons
		ButtonsList.draw(canvas, paint, this, textTopShift);
		
		seekBarSpeed.draw(canvas, paint, this);
		
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
			
			//bounds.offset(i, getHeight()-(j*2));
			
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
		return rightCount;
	}
	 
	public void resetRightCount(){
		rightCount = 0;
	}
	
	
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
			//if (isMoveResize())
		//	return true;
			   
			// The mode of defining coords
			float zoom = drawThreadMy.getZoom();
//			setCoord(X - drawThreadMy.getOffsetX()*zoom, 
//				Y - drawThreadMy.getOffsetY()*zoom,  
//				X, drawThreadMy.getOffsetX()+ (1000*zoom)/2);
			
			setCoord(X, 
					Y,  
					drawThreadMy.getOffsetX()+ (1000*zoom)/2);
			
		 	
//			message = ""+(X - drawThreadMy.getOffsetX())+"  "+(Y - drawThreadMy.getOffsetY());
			message = ""+drawThreadMy.getElementOffsetXY(0)+" - "+drawThreadMy.getElementOffsetXY(1);
			return true;
		}
		
		if (seekBarSpeed.isVisible()){
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
						move(VAK, false);
						if (listener != null)
							listener.onTouchDown(VAK);
					}else if (choiceOfDirIsProper(VAK)){
	    				rightCount++;
	    				if (signal == 1){
	    					vibrator.vibrate(50);
	    				}
	    			}
				}
			};
        } else if (action == MotionEvent.ACTION_POINTER_DOWN){
			if (event.getPointerCount() == 2){
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
				
				Log.d("", "= "+coord0.x+"  "+coord0.y);
				Log.d("", "= "+coord1.x+"  "+coord1.y);
				Log.d("", "= "+X+"  "+Y);
				
				int pointerIndex = event.getActionIndex();
				
				//Toast.makeText(mContext,""+pointerIndex,);
 
				
					x0 = event.getX(pointerIndex);
					y0 = event.getY(pointerIndex);
				
				x0 = -999;
				//if (coord0.x != uX || coord0.y == Y)
				//	event.getPointerCoords(1, coord0);

	        	//drawThreadMy.setMessage("X "+X+" Y "+Y+" coord0.x "+coord0.x+" coord0.y "+coord0.y+" coord1.x "+coord1.x+" coord1.y "+coord1.y);
				
				//x0 = X;
				//y0 = Y;
				return true;
				
			}
        } else if (action == MotionEvent.ACTION_MOVE) {
        	//drawThreadMy.setMessage("X "+X+" Y "+Y);
        	if (! canPressBytton() && isMovedResized){ // 
				if (event.getPointerCount() == 1 || mode == 999){
					//Log.d("", "offsetY "+offsetY);
					//Log.d("", "y1 "+y1);
					//Log.d("", "y0 "+y0);
					//Log.d("", "* "+X+"  "+Y);
					if (drawThreadMy != null && x0 != -999)
						drawThreadMy.offset(x1 - x0, y1 - y0, -1);
					
					//Log.d("", "offsetY "+offsetY);
					
					x0 = x1;
					y0 = y1;
					 
					//mRect.set(0, 0, (int)(minWidthHeight * kZooming), (int)(minWidthHeight * kZooming));
					//mRect.offset(offsetX, offsetY);
					
					//moveElements(x1 - x0, y1 - y0);
				} else {
					PointerCoords coord0 = new PointerCoords(); // First finger coordinates
					PointerCoords coord1 = new PointerCoords(); // Second finger coordinates
					event.getPointerCoords(0, coord0);
					event.getPointerCoords(1, coord1);
					double distance1 = distance(coord0, coord1)/10;

					center.x = (int) ((coord0.x+coord1.x)/2);
					center.y = (int) ((coord0.y+coord1.y)/2);
					
					//double k1 = kZooming;
					
					if (distance != 0) // && k * distance1/distance > 0.3 && k * distance1/distance < 5
						kZooming = distance1/distance;
					else 
						kZooming = -1;
					
					Log.d("", "kZooming  "+kZooming);
					
					// Replace coordinates of the current touch to the coordinatece of the center 

					//offsetX = (int) (offsetX + (center1.x - x0));
					//offsetY = (int) (offsetY + (center1.y - y0));
					
					//Log.d("", "center1.y - y0 "+center1.y +"   "+ y0);
					//Log.d("", "k1 "+k1);
					//float minWidthHeight = Math.min(Params.width, Params.height);
					
					//Log.d("", "kZooming "+kZooming);
					//Log.d("", "offsetY "+offsetY);
					
					//offsetX = offsetX + (int)((-minWidthHeight*kZooming + minWidthHeight*k1)/2);
					//offsetY = offsetY + (int)((-minWidthHeight*kZooming + minWidthHeight*k1)/2);
					//Log.d("", "offsetY "+offsetY);
					//setElements((int)(minWidthHeight * kZooming), (int)(minWidthHeight * kZooming));
					//moveElements(offsetX, offsetY);

					if (drawThreadMy != null)
						drawThreadMy.offset(center.x - x0, center.y - y0, kZooming);
					
					
					//center0.x = center1.x;
					//center0.y = center1.y;
					
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
        		startMoving();
				Log.d("","2");
        		return true;
        	}   
			
        	if (ButtonsList.ACTION_UP()){
	        	invalidate();
        	} else {
				Log.d("","4");
				pause();
				return true;
			}
        	
        }
        	
		x0 = x1;
		y0 = y1;
        
		return true; // false?
	}
	/*
	
	public void startRandomMoving() {
		//setPeriod(period);
		super.startRandomMoving();
	}
	*/
	public void setMarkType(int markType) {
    	/*if (markType == 1){
    		rects[4].top = rectsSaved[0].top - (height - rectsSaved[0].top); 
    		rects[6].top = rectsSaved[1].top - (height - rectsSaved[1].top);
    	} else {
    		rects[4] = new RectF(rectsSaved[0]);
    		rects[6] = new RectF(rectsSaved[1]);
    	}
		*/
		this.invalidate();
	}
	
	//public int getCurrentPosition(){
	//	return drawThreadMy.getCurrentPosition();
	//}
	
	//public int getDuration(){
	//	return drawThreadMy.getCurrentPosition();
	//}
	

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
		this.signal = Integer.parseInt(signal);
	}
 
	public void resetRects() {
		ButtonsList.setButtonsRects(getWidth(), getHeight(), allDirections());
		setSeekBarSpeedRect(getWidth(), getHeight());
	}
	
	public void setMessage(String m, MType t) {
		message = m;
		messageType = t;
	}

	public void setGroupsCount(int p){
		groupsCount = p;
	}
	
	public void incGroupsCount(){
		groupsCount++;
	}

	public void setGroupCountRight(int p){
		groupsCountRight = p;
	}
	
	public void incGroupCountRight(){
		groupsCountRight++;
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
		return mode;
	}

	
	//public void setAllDirections(boolean boolean1) {
	//	super.setAllDirections(boolean1);
	//}

	
}
