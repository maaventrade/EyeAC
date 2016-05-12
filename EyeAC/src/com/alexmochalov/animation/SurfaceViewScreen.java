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
import com.alexmochalov.animation.SurfaceViewScreen.states.*;

import java.util.*;

/**
 * 
 * @author Alexey Mochalov
 *
 */
public class SurfaceViewScreen extends SurfaceView implements SurfaceHolder.Callback, ElementCallback{
	private Context context;
	
	// List of the elements of the face
	private ArrayList <Element> elements;
	// And additional references to eyes
	private ElementEye leftEye;
	private ElementEye rightEye;
	private ElementFace2 face;
	
	// Handler for pause 
	private Handler handler = new Handler(); 

    public int period = 50;
    
    // If false - exclude Up, F, Dn;
	private boolean allDirections = false; 
	
	private int previ = 0;
	private int prevj = 0;
	private boolean prevGoBack = false;
	
	private String prevDir = "";
	private String currentDir = "";
	private String currentDirStr = "";
	private int movingsCount = 0;

	public static class states{
		enum State{coords, random, group, groupWait, groopBetween, toButton, resize}
	}
	
	private static State state;
	
	private boolean pause = true;
	private boolean moveResize = false;
	private boolean comeBack = false;
	private boolean dirSelected = false;
	
	private int maxCount; // The max count of the eyes movings in thr Group mode;
	private int count; // The counter of the count;
	
	private String groupMovings[];
	private String groupMovingsAnswer[];
	private int groupItemIndex; 

	public MyCallback callback = null;
	
	public interface MyCallback {
		void callbackGroupFinish(); 
		void callbackGroupOk(); 
		void callbackGroupError(); 
		void onFinish(); 
	} 
	
	public boolean canPressBytton()
	{
		if (moveResize) return false;
		
		if (state == State.toButton) return true;
		if (pause || state == State.groopBetween) return false;
		else if (state == State.random 
			|| state == State.groupWait ) return true;
		return false;
	}

	public void setMoveResize(){
		moveResize = !moveResize;
	}

	public boolean isMoveResize(){
		return moveResize;
	}
	
	
	public void setMode(int mode)
	{
		switch (mode){
			case 0: 
				state = State.random;
				break;
			case 1: 
				state = State.group;
				count = maxCount;
				groupMovings = new String[count];
				groupMovingsAnswer = new String[count];
				break;
			case 2: 
				state = State.toButton;
				break;
			case 300: 
				state = State.groupWait;
				pause = false;
				break;
			case 301: 
				//state = State.resize;
				pause = true;
				break;
			case 400: 
				state = State.groopBetween;
				break;
			case 999: 
				state = State.coords;
				pause = true;
				break;
		}
	}
	
	public boolean isPlaying()
	{
		return !pause;
	}
	
	public boolean isWaiting()
	{
		return state == State.groupWait;
	}
	
	public boolean isGroup()
	{
		return state == State.group;
	}
	
	public boolean isResized()
	{
		return state == State.resize;
	}
		
	
	public boolean isGroupAny()
	{
		return state == State.group ||  state == State.groupWait || state == State.groopBetween;
	}
	
	public boolean isRandom()
	{
		return state == State.random;
	}
	
	public boolean modeIsToButton()
	{
		return state == State.toButton;
	}
	
	public boolean isCoords()
	{
		return state == State.coords && !moveResize;
	}
	
	public void setCoord(float x, float y, float X, float halfWidth) {
		if (X < halfWidth)
			rightEye.setCoord(x , y);
		else
			leftEye.setCoord(x , y);
	}
	
	public boolean allDirections()
	{
		return allDirections;
	}
	
	public void setMaxCount(int maxCount)
	{
		this.maxCount = maxCount;
	}
	
	public void setAllDirections(boolean allDirections)
	{
		this.allDirections = allDirections;
	}
	
	private void init(){
        getHolder().addCallback(this);
	}
	
    public SurfaceViewScreen(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
	}
	public SurfaceViewScreen(Context context) {
        super(context);
		this.context = context;
		init();
    }
    
	interface TouchEventCallback { 
		//void callbackCall(); 
		//void callbackPhoto(); 
	}
	
	public void add(Element e){
    	elements.add(e);
	}
	
	public ArrayList <Element> getElements(){
		return elements; 
	}
	
	int GAP = 20; 
	int BORDER = 5; 
	
	public  void getCStr(Context context){
		/****************************/
		//Log.d("", ""+"<item>"+rightEye.getCenter().x+"</item>\n"+"<item>"+rightEye.getCenter().y+"</item>\n"+
		//		"<item>"+leftEye.getCenter().x+"</item>\n"+ "<item>"+leftEye.getCenter().y+"</item>\n"
		//		);
		
    	
	}
	
	public void addFaceElements2(int width, int height, float scale, int radius, int faceID, Bitmap pupilBitmap, int rDirID, int lDirID){
		float min;

		if (width <= height){
			min = width;
		} else {
			min = height;
		}
//this.scale = scale;
//		float centerX = width/2;
//		float centerY = height/2;

//		centerRight.x = centerX + radius*0.5582419f;
//		centerRight.y = centerY + radius*0.1846154f;

		//elements.add(new ElementCircle(centerX, (height)/2, Color.WHITE, radius));

		rightEye = new ElementEye(radius, "right", getResources().getStringArray(rDirID), 
				pupilBitmap);
		rightEye.event = this;

    	elements.add(rightEye);

//    	centerLeft.x = centerX + radius*-0.14065935f;
//    	centerLeft.y = centerY + radius*0.22417584f;

    	leftEye = new ElementEye(radius, "left", getResources().getStringArray(lDirID), 
    			pupilBitmap);
    	leftEye.event = this;

    	elements.add(leftEye);
		
    	face = new ElementFace2(context, Color.BLACK, faceID);
    	
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
		
		pause = true;
	}

	public void cont() {
		for (Element e: elements )
			if (e != null)
				e.cont();
		
		pause = false;
	}

	public void pauseCont() {
		if (pause) cont();
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
		if (state == State.random){
			if (dirSelected)
				return false;
			else {
				dirSelected = true;
				return (dir.equals(prevDir)
					|| dir.equals(currentDir));
			}
		} else 
		if (state == State.groupWait){
			if (groupMovingsAnswer == null)
				return false;
			
			groupMovingsAnswer[groupItemIndex] = dir;
			groupItemIndex++;
			if (groupItemIndex == maxCount){
				//for (int i = 0; i < groupMovings.length; i++){
				//	Log.d("",groupMovings[i]);
				//	Log.d("",groupMovingsAnswer[i]);
				//}
				
				for (int i = 0; i < groupMovings.length; i++){
					if (groupMovingsAnswer[i] == null ||
							!groupMovingsAnswer[i].equals(groupMovings[i])){
						groupItemIndex = 0;
						callback.callbackGroupError();
						return false;
					}
				}
				callback.callbackGroupOk();
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
		pause = false;
		comeBack = false;
		if (state == State.group){
			count = maxCount;
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
		
		if (state == State.group){
			if (count == 1)
				goBack = true;
			groupMovings[maxCount-count] = currentDir;
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
			if (state == State.group){
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
