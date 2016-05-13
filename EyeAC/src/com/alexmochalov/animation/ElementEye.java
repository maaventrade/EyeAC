package com.alexmochalov.animation;

import android.graphics.*;
import android.util.Log;

public class ElementEye extends Element
{
	//private Bitmap bitmapInitial; // Initial picture
	//private Bitmap bitmap;     // Scaled picture
	private Bitmap bitmapFlash;     // Scaled picture colored
	
	//private Rect rectInitial;
	
	private float X; 
	private float Y; 

	private float mZoom = 1;
	
	
	private float x0; 
	private float y0; 
	private float x1; 
	private float y1; 

	private float deltax;
	private float deltay;

	public ElementCallback event;

	protected boolean mFlash;
	
	
	private float radius;
	private String name;
	private PointF[][] coords;
	
	private Bitmap bitmapPupil = null;
	private Rect rectPupil;


	interface ElementCallback { 
		void goFinish(); 
	}

	public void stop()
	{
		mMoved = false;
	}

	public void cont()
	{
		mMoved = true;
	}

//	private enum Dir {x, y};
//	private Dir dir = null;

	private float k;
//	private float vel;
	private float dx;
	private float dy;

	private boolean mMoved;

	boolean mastGoBack;
	boolean isGoBack;

	private long period;
	
	private ElementFace2 face;
	
	public ElementEye(int radius, String name, String[] strings, Bitmap pupilBitmap){
		super(null, 0);

		
		mMoved = false;
		
		this.name = name;

		coords = new PointF[3][3];
		
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				coords[j][i] = new PointF( Float.parseFloat(strings[i*6+j*2]), 
						 				   Float.parseFloat(strings[i*6+j*2 + 1]));
		x = coords[1][1].x ;
		y = coords[1][1].y ;
		
		X = x;
		Y = y;
		
		x0 = x;
		y0 = y;
		
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				if (i != 1 || j != 1 ){
					coords[j][i].x = coords[j][i].x - x; 
					coords[j][i].y = coords[j][i].y - y;
				}
		if (pupilBitmap != null)
			this.bitmapPupil = Bitmap.createScaledBitmap(pupilBitmap, radius,  radius, false);
		else this.radius = radius;
		//x0 = coords[1][1].x ;
		//y0 = coords[1][1].y ;
		
		Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
		bitmapInitial = Bitmap.createBitmap(radius * 2, (int)radius * 2, conf); // this creates a MUTABLE bitmap
		rectInitial = new Rect(0, 0, bitmapInitial.getWidth(), bitmapInitial.getHeight());
		Canvas canvas = new Canvas(bitmapInitial);

		drawToBitmap(canvas, false);
		
		bitmap = Bitmap.createScaledBitmap(bitmapInitial, bitmapInitial.getWidth(), bitmapInitial.getHeight(), false);
		fillBitmapFlash();
	}
	
	public void setFace(ElementFace2 p){
		face = p;
	}
	
	/**
	 * 
	 * @param i
	 * @param j
	 * @param period
	 * @param goBack
	 * @param flash = -1 no flash; 1 flash; 0 flash = mFlash
	 */
	public synchronized void movingCoords(int i, int j, long period, boolean goBack, boolean flash, boolean fFlash) {
		Log.d("", "J "+j);
		if (j == 9)
			setMoving(0, 0, period, goBack, flash, fFlash);
		else
		if (j == 100)
			setMoving(coords[1][1].x*i*0.01f * mZoom, 0, period, goBack, flash, fFlash);
		else setMoving(coords[i+1][j+1].x * mZoom, coords[i+1][j+1].y * mZoom, period, goBack, flash, fFlash);
	}
	
	public void setMoving(float deltax, float deltay, long period, boolean goBack, boolean flash, boolean fFlash) {
		startMoving(x0 + deltax, y0 + deltay, period, goBack);
		isGoBack = false;
		//Log.d("", " isGoBack "+isGoBack);
		//Log.d("", " flash "+flash);
		if (fFlash)
			mFlash = flash;
		else 
			mFlash = true;
		
	}
	
	
/*

	public synchronized void setMoving(int i, int j, long period, boolean goBack) {
		if (j == 100)
			setMoving(coords[1][1].x*i*0.01f * mZoom, 0, period, goBack, mFlash);
		else setMoving(coords[i+1][j+1].x * mZoom, coords[i+1][j+1].y * mZoom, period, goBack);
	}


	public void setMoving(float deltax, float deltay, long period, boolean goBack, boolean flash) {
		startMoving(x0 + deltax, y0 + deltay, period, goBack);
		isGoBack = false;
		//Log.d("", " isGoBack "+isGoBack);
		Log.d("", " flash "+flash);
		mFlash = flash;
	}


	public void setMoving(float deltax, float deltay, long period, boolean goBack) {
		startMoving(x0 + deltax, y0 + deltay, period, goBack);
		isGoBack = false;
		mFlash = true;
	}


	*/
	
	
	public PointF getCenter(){
		return new PointF(x,y);
	}
	
	private float shiftX = 0;
	private float shiftY = 0;
	
	/**
	 * Makes one step of moving
	 */
	@Override
	public void move() {
		if (mMoved){ // If pause, then moving is false 

			if ( Math.signum(x1 - (x + dx)) != Math.signum(deltax) || // Eye arrived to the finish  
				Math.signum(y1 - (y + dy)) != Math.signum(deltay)){
				event.goFinish(); // Tell about this to SurfaceView
				return;
			}	
			else if ( Math.abs(x1 - (x + dx)) <= 10 && // Eye is near to finish
					 Math.abs(y1 - (y + dy)) <= 10 && !isGoBack){
				face.setEyesArrived(true); // 
			}	
			else
				face.setEyesArrived(false);

			x = x + dx;
			y = y + dy;
		}
	}

	/**
	 * Stops moving
	 * @return False if Eye returned to the center (not obvious to move back) 
	 */
	public boolean finishAndGoBack() {
		mMoved = false;
		if (mastGoBack){
			mastGoBack = false;
			isGoBack = true;
			// Start moving back to the center
			startMoving(x0 , y0, period, false);
			
			return true;
		} else return false;
	}

	/**
	* Calculetes parameters of moving and sets flag mMoved to true
	*
	* x1, y1 are cordinates of the finish point
	* period is the count of steps
	* goBack is the flag if Eye must return back to the center
	*
	**/
	public void startMoving(float x1, float y1, long period, boolean goBack) {
		mastGoBack = goBack;

		//x1 = x - x1 * mZoom;
		//y1 = y + y1 * mZoom;
		
		this.x1 = x1;
		this.y1 = y1;

		deltax = x1 - x;
		deltay = y1 - y;

		this.period = period; 

		dx = deltax/(period); //* mZoom
		dy = deltay/(period);

		//steps = new float[Math.max(deltax, deltay)];

		//Log.d("", "deltax "+deltax+" period "+period+" dx "+dx);
		//Log.d("", "deltay "+deltay+" period "+period+" dy "+dy);

		mMoved = true;

	}

	public void setGoBack(boolean goBack) {
		mastGoBack = goBack;
	}

	public void setCoord(float x2, float y2) {
		x = x2;
		y = y2;
	}  
	 
	private void drawToBitmap(Canvas canvas, boolean flash){
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		
		if (bitmapPupil == null){
			//canvas.drawColor(Color.WHITE);
			if (flash)
				paint.setColor(Color.GREEN);
			else 
				paint.setColor(Color.GRAY);
			
			// Draw the IRIS
			canvas.drawCircle(radius, radius, radius, paint);
			   
			// Draw the IRIS
			paint.setColor(Color.BLACK);
			canvas.drawCircle(radius, radius, radius/2, paint);
			
			// Draw the border of the IRIS
			paint.setStrokeWidth(radius/5);
			paint.setStyle(Paint.Style.STROKE);  
			canvas.drawCircle(radius, radius, radius-radius/10-1, paint);
			
		} else {
			/*
			canvas.drawBitmap(bitmapPupil, x-bitmapPupil.getWidth()/2, y-bitmapPupil.getHeight()/2, paint);
			if (face.getEyesArrived() && (!isGoBack) && flash){
				paint.setColor(Color.GREEN);
				paint.setStrokeWidth(radius/5);
				paint.setStyle(Paint.Style.STROKE);
				canvas.drawCircle(x, y, (int)(radius*0.8), paint);
			}
			*/
		}
		
	}

	private void fillBitmapFlash(){
		Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
		Bitmap bmp = Bitmap.createBitmap(bitmapInitial.getWidth(), bitmapInitial.getHeight(), conf); // this creates a MUTABLE bitmap
		Canvas canvas = new Canvas(bmp);

		drawToBitmap(canvas, true);

		bitmapFlash = Bitmap.createScaledBitmap(bmp, 
			(int)(bmp.getWidth() * mZoom),
			(int)(bmp.getHeight() * mZoom), false);
		
	}
	
	public void commitOffset(float newX, float newY, double zoom) {
		bitmap = Bitmap.createScaledBitmap(bitmapInitial, 
				(int)(bitmapInitial.getWidth() * zoom), 
				(int)(bitmapInitial.getHeight() * zoom), 
				false);
		
		x = (int)(X * zoom) + newX + shiftX;
		y = (int)(Y * zoom) + newY + shiftY;
		
		x0 = x;
		y0 = y;
		
		mZoom = (float)zoom;
		fillBitmapFlash();

		}	
	
	/*
	 *  
	 * 
	 */
	public void draw(Canvas canvas, boolean mMoved, float newX, float newY, double zoom) {
		Paint paint = new Paint();
		if (mMoved){
			RectF rect = new RectF(0, 0,  
					(int)(bitmapInitial.getWidth() * zoom), 
					(int)(bitmapInitial.getHeight() * zoom));
			
			//x = x + newX;
			//y = y + newY;
			    
			//Log.d("", "Y "+y);
								   
			rect.offsetTo((int)(X * zoom) + newX + shiftX, (int)(Y * zoom) + newY + shiftY);
			canvas.drawBitmap(bitmapInitial, rectInitial, rect, paint);
			
		} else {
			if (face != null
				&& face.getEyesArrived() 
				&& (!isGoBack)
				&& mFlash)
				
				canvas.drawBitmap(bitmapFlash, x, y, paint);
			else
				canvas.drawBitmap(bitmap, x, y, paint);
		}
		
		
		
			
		
	}
	
}
