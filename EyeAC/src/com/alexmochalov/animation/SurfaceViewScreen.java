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
 * SurfaceViewScreen provides creation of the visual elements, control of the Draw thread,
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
    public int mPeriod = 50;
    
    // If false - exclude Up, F, Dn (6 screen buttons, otherwise 9 buttons);
	private boolean allDirections = false; 
	
	// Store previous direction to avoid repeating
	private int previ = 0;
	private int prevj = 0;
	private boolean prevGoBack = false; // True if previous movement come back 
	
	private String prevDir = "";
	private String currentDir = ""; // Current direction as a string ("Ar","Vr"...)
	//private String currentDirStr = ""; Concatenated currentDir for debugging
	private int movingsCount = 0;
	// mMode (mode) of the application
	public static 
		enum Mode{coords, random, group, groupWait, groopBetween, toButton}
	private static Mode mMode;
	
	protected boolean mPause = true;
	protected boolean isMovedResized = false; // If image is moved and resised
	private boolean mEyesAreReturning = false;
	private boolean mDirSelected = false; // User select dir by tpuchng screen button
	
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
	
	public void clearElements()
	{
		elements.clear();
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
	* This method creates the face 
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
    	elements.add(face);
    	
		rightEye.setFace(face);
		leftEye.setFace(face);

	}
	
	@Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
        int height) {
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
		if (elements != null)
			for (Element e: elements )
				if (e != null)
					e.stopMoving();
		
		mPause = true;
	}

	public void cont() {
		for (Element e: elements )
			if (e != null)
				e.continueMoving();
		
		mPause = false;
	}

	public void pauseCont() {
		if (mPause) cont();
		else pause();
	}
	
	/**
	 * Get current direction as a string for the indexes
	 * @param i 1 is moving right, -1 is moving left, 0 no moving by horizontal
	 * @param j 1 is moving down, -1 is moving up, 0 no moving by vertical
	 * @return direction as a string
	 */
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
	
	/*
	public String getCurrentDirStr()
	{
		return currentDirStr;
	}
	*/
	
	/**
	 * Test if user selected proper direction by touching button 
	 * Is used in the modes "Continues movings" and "Groups of moving" 
	 * @param dir - is directory of moving as string
	 * @return
	 */
	public boolean choiceOfDirIsProper(String dir){
		if (mMode == Mode.random){
			if (mDirSelected)      // User already have selected direction   
				return false;
			else {
				mDirSelected = true;
				return (dir.equals(prevDir) // Compare
					|| dir.equals(currentDir));
			}
		} else 
		if (mMode == Mode.groupWait){
			if (groupMovingsAnswer == null) //
				return false;
			
			groupMovingsAnswer[groupItemIndex] = dir;
			groupItemIndex++;
			if (groupItemIndex == mMaxCount){
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
	
	/**
	 * Returning of eyes is started 
	 */
	public void resetDirs(){
		currentDir = "";
		prevDir = "";
		mEyesAreReturning = true;
	}
	
	/**
	 * Starts moving of the eyes to the defined direction
	 * @param dir - direction of moving as string
	 * @param goBack - true if the eyes must return to the center
	 */
	public void move(String dir, boolean goBack)
	{
		if (dir == null)
			return;
		
		int i = 0, j = 0;
		
		if (dir.equals("Up")){
			i = 0;
			j = -1;
		}
		else if (dir.equals("Vr")){
			i = 1;
			j = -1;
		}
		else if (dir.equals("Ar")){
			i = 1;
			j = 0;
		}
		else if (dir.equals("Ad")){
			i = 1;
			j = 1;
		}
		else if (dir.equals("Dn")){
			i = 0;
			j = 1;
		}
		else if (dir.equals("K")){
			i = -1;
			j = 1;
		}
		else if (dir.equals("Ac")){
			i = -1;
			j = 0;
		}
		else if (dir.equals("Vc")){
			i = -1;
			j = -1;
		}
		else if (dir.equals("F")){
			i = 0;
			j = 0;
		} else return;
		
		if (i == 0 && j == 0){
			// Moving forvard
			rightEye.startMoving(1, 100, mPeriod, goBack, true, true);
			leftEye.startMoving(-1, 100, mPeriod, goBack, true, true);
		} else {
			// Moving Ar,Up,Ac and so on.
			rightEye.startMoving(i, j, mPeriod, goBack, true, true);
			leftEye.startMoving(i, j, mPeriod, goBack, true, true);
		}
	}
	
	/**
	 * Starts returning of the eyes to the center
	*/
	public void returnToCenter()
	{
		rightEye.startMoving(9, 9, mPeriod, false, false, true);
		leftEye.startMoving(9, 9, mPeriod, false, false, true);
	}
	
	/**
	 * Restart moving after pause
	 */
	public void restartMoving() {
		mPause = false;
		mEyesAreReturning = false;
		if (mMode == Mode.group){
			count = mMaxCount;
			groupMovings = new String[count];
			groupMovingsAnswer = new String[count];
		}
		random();
	}

	/**
	* Starts random moving of the eyes
	*/
	public void random() {
		int i, j;
		boolean goBack;
		
		double r;
		r = Math.random(); 
		if (r <= 0.3f) j = 1; // down
		else if (r <= 0.6f) j = -1; // up
		else j = 0; // horizontal
		
		if (allDirections){
			r = Math.random(); 
			if (r <= 0.3f) i = 1; // right
			else if (r <= 0.6f) i = -1; // left
			else i = 0; // forward
		} else {
			r = Math.random(); 
			if (r <= 0.555555f) i = 1; // right
			else  i = -1; // left
		}
		
		// Define if eyes must come back to the center
		r = Math.random(); 
		if (r <= 0.5f) goBack = true;
		else goBack = false;
		
		// If eyes didn"t return to the center
		// avoid repeation
		if (previ == i && prevj == j && prevGoBack == false){
			i = -i;
			j = -j;
		}
		
		// save current values
		previ = i;
		prevj = j;
		prevGoBack = goBack;
		
		prevDir = currentDir;
		// currentDirStr = currentDirStr+" "+getCurrentDir(i, j); // for debugging
		currentDir = getCurrentDir(i, j);
		movingsCount++;
			
		mDirSelected = false;
		
		if (mMode == Mode.group){
			if (count == 1)
				goBack = true;
			// Save to the array for a comparing with
			// users answers
			groupMovings[mMaxCount-count] = currentDir;
		}	
		
		if (i == 0 && j == 0){
			// move forward
			rightEye.startMoving(1, 100, mPeriod, goBack, true, false);
			leftEye.startMoving(-1, 100, mPeriod, goBack,  true, false);
		} else {
			rightEye.startMoving(i, j, mPeriod, goBack, true, false);
			leftEye.startMoving(i, j, mPeriod, goBack, true, false);
		}
	}

	public void setPeriod(int period) {
		mPeriod =  period;
	} 
	
	public int getPeriod() {
		return mPeriod;
	}
	
	@Override
	/**
	 * When one of the eyes is near finish of the moving, this event arise
	 */
	public void goFinish() {
		boolean rMustGoBack = rightEye.finishAndGoBack();
		boolean lMustGoBack = leftEye.finishAndGoBack();
		
		if ( !rMustGoBack && !lMustGoBack && !mEyesAreReturning){
			// Eyes must not go back  
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
				// In this mode app wait while user touch up  
				pause();
		}
		
	}

	/**
	 * Returns face bitmap width in pixels
	 */
	protected float getFaceWidth() {
		return face.getWidth();
	}

}
