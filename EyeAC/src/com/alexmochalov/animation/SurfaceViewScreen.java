package com.alexmochalov.animation;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import com.alexmochalov.animation.ElementEye.*;


import java.util.*;

/**
 * @author Alexey Mochalov
 * SurfaceViewScreen provide creation of the visual elements, control of the Draw thread,
 * selection of movement directions and so.  
 *
 */
public class SurfaceViewScreen extends SurfaceView implements SurfaceHolder.Callback, ElementCallback{
	private Context mContext;
	
	// List of the elements of the face
	private ArrayList <Element> elements;
	// And references to every element
	private ElementEye leftEye;
	private ElementEye rightEye;
	private ElementFace2 face;
	
	// Handler for pause 
	private Handler handler = new Handler(); 

	// Speed of the movements
    public int period = 50;
    
    // If false - exclude Up, F, Dn (6 screen buttons, otherwise 9 buttons);
	private boolean allDirections = false; 
	
	// Store previous direction to avoid repeating
	private int previ = 0;
	private int prevj = 0;
	private boolean prevGoBack = false; // True if previous movement come back 
	
	private String prevDir = "";
	private String currentDir = "";
	private String currentDirStr = "";
	private int movingsCount = 0;
	// mMode (mode) of the application
	public static 
		enum Mode{coords, random, group, groupWait, groopBetween, toButton}
	private static Mode mMode;
	
	protected boolean mPause = true;
	protected boolean isMovedResized = false; // If image is moved and resised
	private boolean comeBack = false;
	private boolean dirSelected = false;
	
	private int mMaxCount; // The max count of the eyes movings in thr Group mode;
	private int count; // The counter of the count;
	
	private String groupMovings[];
	private String groupMovingsAnswer[];
	private int groupItemIndex; 

	public MyCallback callback = null;
	
	public interface MyCallback {
		void callbackGroupFinish(); 
		void callbackGroupResult(boolean result); 
		void onFinish(); 
	} 
	/**
	* Test if screen buttons can be pressed
	*/
	public boolean canPressBytton()
	{
		if (isMovedResized) return false;
		if (mMode == Mode.toButton) return true;
		if (mPause || mMode == Mode.groopBetween) return false;
		else if (mMode == Mode.random 
			|| mMode == Mode.groupWait ) return true;
		return false;
	}

	public void setMoveResize(){
		isMovedResized = !isMovedResized;
	}

	/**
	* Set mode and pause mMode
	**/
	public void setMode(int mode)
	{
		switch (mode){
			case 0: 
				mMode = Mode.random;
				break;
			case 1: 
				mMode = Mode.group;
				count = mMaxCount;
				groupMovings = new String[count];
				groupMovingsAnswer = new String[count];
				break;
			case 2: 
				mMode = Mode.toButton;
				break;
			case 300: 
				mMode = Mode.groupWait;
				mPause = false;
				break;
			case 301: 
				//mMode = Mode.resize;
				mPause = true;
				break;
			case 400: 
				mMode = Mode.groopBetween;
				break;
			case 999: 
				mMode = Mode.coords;
				mPause = true;
				break;
		}
	}
	
	//public boolean isPlaying()
	//{
		//return !pause;
	//}
	
	public boolean isWaiting()
	{
		return mMode == Mode.groupWait;
	}
	
	public boolean isGroup()
	{
		return mMode == Mode.group;
	}
	
	public boolean isGroupAny()
	{
		return mMode == Mode.group ||  mMode == Mode.groupWait || mMode == Mode.groopBetween;
	}
	
	public boolean isRandom()
	{
		return mMode == Mode.random;
	}
	
	public boolean modeIsToButton()
	{
		return mMode == Mode.toButton;
	}
	
	public boolean isCoords()
	{
		return mMode == Mode.coords && !isMovedResized;
	}
	
	/*
	* Set coordinates of the Eyes in the mode "Coords"
	*
	*@param x is the x coordinate of the touch
	*@param y is the y coordinate of the touch
	*@param centerOfTheFace is center of the face
	*/
	public void setCoord(float x, float y, float centerOfTheFace) {
		if (x < centerOfTheFace)
			rightEye.setCoord(x , y);
		else
			leftEye.setCoord(x , y);
	}
	
	public boolean allDirections()
	{
		return allDirections;
	}
	/*
	* Set the max count of the eyes movings in thr Group mode;
	*/
	public void setMaxCount(int maxCount)
	{
		mMaxCount = maxCount;
	}
	
	public void setAllDirections(boolean allDirections)
	{
		this.allDirections = allDirections;
	}
	
	
    public SurfaceViewScreen(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		getHolder().addCallback(this);
	}
	public SurfaceViewScreen(Context context) {
        super(context);
		mContext = context;
		getHolder().addCallback(this);
    }
    
	/*
	* Add element to the list of the elements
	*/
	public void add(Element e){
    	elements.add(e);
	}
	
	public ArrayList <Element> getElements(){
		return elements; 
	}
	
	/*
	* This method create the face 
	*/
	public void addFaceElements2(int width, int height, int radius, int faceID, Bitmap pupilBitmap, int rDirID, int lDirID){

		rightEye = new ElementEye(radius, "right", getResources().getStringArray(rDirID), 
				pupilBitmap);
		rightEye.event = this;

    	elements.add(rightEye);


    	leftEye = new ElementEye(radius, "left", getResources().getStringArray(lDirID), 
    			pupilBitmap);
    	leftEye.event = this;

    	elements.add(leftEye);
		
    	face = new ElementFace2(mContext, Color.BLACK, faceID);
    	
		rightEye.setFace(face);
		leftEye.setFace(face);

    	elements.add(face);
    	
	}
	
	//abstract void createDrawThread();
    	
	@Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
        int height) {
    	//createDrawThread();
	}

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	elements = new ArrayList<Element>();
    }

    private Runnable updateTimeTask = new Runnable() { 
		   public void run() { 
			   handler.removeCallbacks(updateTimeTask);
			   random();
		   } 
		};        

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    	elements = null;
    }

	public void pause() {
		for (Element e: elements )
			if (e != null)
				e.stop();
		
		mPause = true;
	}

	public void cont() {
		for (Element e: elements )
			if (e != null)
				e.cont();
		
		mPause = false;
	}

	public void pauseCont() {
		if (mPause) cont();
		else pause();
	}
	
	private String getCurrentDir(int i, int j){
		if (i == 0 && j == 1)
			return "Dn";
		else if (i == 1 && j == 1)
			return "Ad";
		else if (i == 1 && j == 0)
			return "Ar";
		else if (i == 1 && j == -1)
			return "Vr";
		else if (i == 0 && j == -1)
			return "Up";
		else if (i == -1 && j == -1)
			return "Vc";
		else if (i == -1 && j == 0)
			return "Ac";
		else if (i == -1 && j == 1)
			return "K";
		else 
			return "F";
	}
	
	public String getCurrentDir(){
		return currentDir;
	}
	
	public String getPrevDir(){
		return prevDir;
	}
	
	public String getCurrentDirStr()
	{
		// TODO: Implement 
		return currentDirStr;
	}
	
	
	public boolean chooseDir(String dir){
		//Log.d("", "dir "+dir);
		//Log.d("", "prevDir "+prevDir);
		//Log.d("", "currentDir "+currentDir);
		if (mMode == Mode.random){
			if (dirSelected)
				return false;
			else {
				dirSelected = true;
				return (dir.equals(prevDir)
					|| dir.equals(currentDir));
			}
		} else 
		if (mMode == Mode.groupWait){
			if (groupMovingsAnswer == null)
				return false;
			
			groupMovingsAnswer[groupItemIndex] = dir;
			groupItemIndex++;
			if (groupItemIndex == mMaxCount){
				//for (int i = 0; i < groupMovings.length; i++){
				//	Log.d("",groupMovings[i]);
				//	Log.d("",groupMovingsAnswer[i]);
				//}
				
				for (int i = 0; i < groupMovings.length; i++){
					if (groupMovingsAnswer[i] == null ||
							!groupMovingsAnswer[i].equals(groupMovings[i])){
						groupItemIndex = 0;
						callback.callbackGroupResult(false);
						return false;
					}
				}
				callback.callbackGroupResult(true);
			}
			return false;
		}
		return false;
	}
	
	public int getCount(){
		return movingsCount;
	}
	
	public void resetCount(){
		movingsCount = 0;
	}
	
	public void resetDirs(){
		currentDir = "";
		prevDir = "";
		comeBack = true;
	}
	
	public void move(String action, boolean goBack)
	{
		if (action == null)
			return;
		
		int i = 0, j = 0;
		
		if (action.equals("Up")){
			i = 0;
			j = -1;
		}
		else if (action.equals("Vr")){
			i = 1;
			j = -1;
		}
		else if (action.equals("Ar")){
			i = 1;
			j = 0;
		}
		else if (action.equals("Ad")){
			i = 1;
			j = 1;
		}
		else if (action.equals("Dn")){
			i = 0;
			j = 1;
		}
		else if (action.equals("K")){
			i = -1;
			j = 1;
		}
		else if (action.equals("Ac")){
			i = -1;
			j = 0;
		}
		else if (action.equals("Vc")){
			i = -1;
			j = -1;
		}
		else if (action.equals("F")){
			i = 0;
			j = 0;
		} else return;
		
		//goBack = false;
		
		if (i == 0 && j == 0){
			// Moving F
			rightEye.movingCoords(1, 100, period, goBack, true, true);
			leftEye.movingCoords(-1, 100, period, goBack, true, true);
		} else {
			// Moving Ar,Up,Ac and so on.
			rightEye.movingCoords(i, j, period, goBack, true, true);
			leftEye.movingCoords(i, j, period, goBack, true, true);
		}
	}
	

	public void returnToCenter()
	{
		rightEye.movingCoords(9, 9, period, false, false, true);
		leftEye.movingCoords(9, 9, period, false, false, true);
	}
	

	public void startMoving() {
		mPause = false;
		comeBack = false;
		if (mMode == Mode.group){
			count = mMaxCount;
			groupMovings = new String[count];
			groupMovingsAnswer = new String[count];
			
		}
		random();
	}

		
	public void random() {
		int i, j;
		boolean goBack;
		
		double r;

		r = Math.random(); 
		if (r <= 0.3f) j = 1; // down
		else if (r <= 0.6f) j = -1; // up
		else j = 0;
		
		if (allDirections){
			r = Math.random(); 
			if (r <= 0.3f) i = 1; // right
			else if (r <= 0.6f) i = -1; // left
			else i = 0;
		} else {
			r = Math.random(); 
			if (r <= 0.555555f) i = 1;
			else  i = -1;
		}
		
		r = Math.random(); 
		if (r <= 0.5f) goBack = true;
		else goBack = false;
		
		if (previ == i && prevj == j && prevGoBack == false){
			i = -i;
			j = -j;
		}
		
		previ = i;
		prevj = j;
		prevGoBack = goBack;
		
		prevDir = currentDir;
		currentDirStr = currentDirStr+" "+getCurrentDir(i, j);
		currentDir = getCurrentDir(i, j);
		//if (!currentDir.equals("")) 
			movingsCount++;
			
		dirSelected = false;
		
		float k = 1.5f;
		//i = 0;
		//j = 1;
		
		//Log.d("","currentDir "+currentDir);
		
		if (mMode == Mode.group){
			if (count == 1)
				goBack = true;
			groupMovings[mMaxCount-count] = currentDir;
		}	
		//i = -1;
		//j = -1;
		if (i == 0 && j == 0){
			rightEye.movingCoords(1, 100, period, goBack, true, false);
			leftEye.movingCoords(-1, 100, period, goBack,  true, false);
		} else {
			rightEye.movingCoords(i, j, period, goBack, true, false);
			leftEye.movingCoords(i, j, period, goBack, true, false);
			//rightEye.setMoving(i * eyeWidth/1.5f, j *  eyeHeight/1.5f, period, goBack, true);
			//leftEye.setMoving(i * eyeWidth/1.5f, j *  eyeHeight/1.5f, period, goBack, true);
			
		}
		

		//Log.d("", " i "+i+" j "+j+" goBack "+goBack);
		
		//isPlaying = true;
	}

	public void setPeriod(int period) {
		this.period =  period;
	} 
	
	public int getPeriod() {
		return period;
	}
	
	@Override  
    public void onDraw(Canvas canvas) {
	}

	@Override
	public void goFinish() {
		boolean f1 = rightEye.finish();
		boolean f2 = leftEye.finish();
		
		if ((f1 || f2) && !comeBack){
			if (mMode == Mode.group){
				count--;
				if (count == 0){
					pause();
					groupItemIndex = 0;
					callback.callbackGroupFinish();
					return;
				}
					
			}
			if (modeIsToButton()){
				callback.onFinish();
				pause();
			}else{
				double r = Math.random(); 
				if (r <= 0.3f){
					currentDir = "";
					handler.postDelayed(updateTimeTask, (int)(r * 2000)); // pause
				}
				else random();
			}
		} else {
			if (modeIsToButton())
				pause();
		}
		
	}


	protected float getFaceWidth() {
		return face.getWidth();
	}

	
}
