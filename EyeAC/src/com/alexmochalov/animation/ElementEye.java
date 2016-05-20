package com.alexmochalov.animation;
 
import android.graphics.*;
import android.util.Log;

/**
 * 
 * @author @Alexey Mochalov
 *	Class ElementEye is image of an eye. 
 * It provides drawing, moving and zooming of the image. 
 *
 */
public class ElementEye extends Element
{
	private Bitmap bitmapFlash; //Scaled picture colored. When moving is finished image is flashing

	// This coordinates are used when image is zoomed
	private float X; 
	private float Y; 

	// Zoom coefficient
	private float mZoom = 1;
	
	// Coordinates of the point to come back
	private float xStart; 
	private float yStart; 
	// Coordinates of the target point
	private float xTarget; 
	private float yTarget; 

	// Distance from start to finish
	private float mDistanceX; 
	private float mDistanceY; 
	// Sizes of the step of moving
	private float mDx;
	private float mDy;
	
	public ElementCallback event;
	// Is picture flashing
	protected boolean mFlash;
	
	private float mRadius; // mRadius of the eye. Used also as width and height of the bitmap
	private PointF[][] mCoords; // Coordinates of moving (top left, left...)
	
	private Bitmap bitmapPupil = null;
	private Rect rectPupil;

	private boolean mMastGoBack; // True if the eye mast return to the center 
	private boolean mIsGoBack; // True if the eye are returning to the center

	private long mPeriod; // Speed of the moving (count of steps in one moving) 
	
	private ElementFace2 face; // Reference to the image of face

	//private float shiftX = 0;
	//private float shiftY = 0;
	
	interface ElementCallback { 
		void goFinish(); // Synchronize finish the eyes moving 
	}

	/**
	 * 
	 * @param mRadius
	 * @param name
	 * @param strings
	 * @param pupilBitmap
	 */
	public ElementEye(int mRadius, String name, String[] strings, Bitmap pupilBitmap){
		super(null, 0);

		mMoved = false;
		//mName = name;
		mCoords = new PointF[3][3];
		
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				mCoords[j][i] = new PointF( Float.parseFloat(strings[i*6+j*2]), 
						 				   Float.parseFloat(strings[i*6+j*2 + 1]));
		x = mCoords[1][1].x ;
		y = mCoords[1][1].y ;
		
		X = x;
		Y = y;
		
		xStart = x;
		yStart = y;
		
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				if (i != 1 || j != 1 ){
					mCoords[j][i].x = mCoords[j][i].x - x; 
					mCoords[j][i].y = mCoords[j][i].y - y;
				}
		if (pupilBitmap != null)
			this.bitmapPupil = Bitmap.createScaledBitmap(pupilBitmap, mRadius,  mRadius, false);
		else this.mRadius = mRadius;
		
		Bitmap.Config conf = Bitmap.Config.ARGB_8888; 
		bitmapInitial = Bitmap.createBitmap(mRadius * 2, (int)mRadius * 2, conf); // this creates a MUTABLE bitmap
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
	 * @param i - index of direction
	 * @param j - index of direction
	 * @param mPeriod - speed of moving
	 * @param goBack - if true eyes must return to the center
	 * @param flash = -1 no flash; 1 flash; 0 flash = mFlash
	 */
	public synchronized void startMoving(int i, int j, long period, boolean goBack, boolean flash, boolean fFlash) {
		mPeriod = period;
		
		if (j == 9){
			// Return to the center
			mDistanceX = 0;
			mDistanceY = 0;
		}
		else
		if (j == 100){
			// Move forward
			mDistanceX = mCoords[1][1].x*i*0.01f * mZoom;
			mDistanceY = 0;
		}
		else {
			mDistanceX = mCoords[i+1][j+1].x * mZoom;
			mDistanceY = mCoords[i+1][j+1].y * mZoom;
		}
		
		mIsGoBack = false;
		if (fFlash)
			mFlash = flash;
		else 
			mFlash = true;
		
		start(xStart + mDistanceX, yStart + mDistanceY, goBack);
	}
	
	/**
	* Calculates steps of moving and sets flag mMoved to true
	*
	* xTarget, yTarget are coordinates of the target point
	* mPeriod is the count of steps
	* goBack is the flag if Eye must return back to the center
	*
	**/
	public void start(float xTarget_, float yTarget_, boolean goBack) {
		mMastGoBack = goBack;
		
		xTarget = xTarget_;
		yTarget = yTarget_;

		mDistanceX = xTarget - x;
		mDistanceY = yTarget - y;

		mDx = mDistanceX/(mPeriod); 
		mDy = mDistanceY/(mPeriod);

		mMoved = true;

	}
	
	/**
	 * Makes one step of moving
	 */
	@Override
	public void move() {
		if (mMoved){ // If pause, then moving is false 
			if ( Math.signum(xTarget - (x + mDx)) != Math.signum(mDistanceX) || // Eye arrived to the finish  
				Math.signum(yTarget - (y + mDy)) != Math.signum(mDistanceY)){
				event.goFinish(); // Say about this to SurfaceView
				return;
			}	
			else if ( Math.abs(xTarget - (x + mDx)) <= 10 && // Eye is near to finish
					 Math.abs(yTarget - (y + mDy)) <= 10 && !mIsGoBack){
				face.setEyesArrived(true); // 
			}	
			else
				face.setEyesArrived(false);
			x = x + mDx;
			y = y + mDy;
		}
	}

	/**
	 * Stops moving
	 * @return False if Eye returned to the center (not necessary to move back) 
	 */
	public boolean finishAndGoBack() {
		mMoved = false;
		if (mMastGoBack){
			mMastGoBack = false;
			mIsGoBack = true;
			// Start moving back to the center
			start(xStart , yStart, false);
			return true;
		} else return false;
	}
	
	public PointF getCenter(){
		return new PointF(x,y);
	}
	
	public void setGoBack(boolean mastGoBack) {
		mMastGoBack = mastGoBack;
	}

	public void setCoord(float x_, float y_) {
		x = x_;
		y = y_;
	}  
	 
	/**
	 * Creates image of eye 
	 * @param canvas - Canvas of the bitmap
	 * @param flash - if eye is flashing
	 */
	private void drawToBitmap(Canvas canvas, boolean flash){
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		
		if (bitmapPupil == null){
			if (flash)
				paint.setColor(Color.GREEN);
			else 
				paint.setColor(Color.GRAY);
			
			// Draw the IRIS
			canvas.drawCircle(mRadius, mRadius, mRadius, paint);
			   
			// Draw the IRIS
			paint.setColor(Color.BLACK);
			canvas.drawCircle(mRadius, mRadius, mRadius/2, paint);
			
			// Draw the border of the IRIS
			paint.setStrokeWidth(mRadius/5);
			paint.setStyle(Paint.Style.STROKE);  
			canvas.drawCircle(mRadius, mRadius, mRadius-mRadius/10-1, paint);
			
		} else {
			/*
			canvas.drawBitmap(bitmapPupil, x-bitmapPupil.getWidth()/2, y-bitmapPupil.getHeight()/2, paint);
			if (face.getEyesArrived() && (!mIsGoBack) && flash){
				paint.setColor(Color.GREEN);
				paint.setStrokeWidth(mRadius/5);
				paint.setStyle(Paint.Style.STROKE);
				canvas.drawCircle(x, y, (int)(mRadius*0.8), paint);
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

	/**
	 * Sets coordinates and create zoomed bitmap when offset and zooming finished
	 */
	public void commitShift(float newX, float newY, double zoom) {
		bitmap = Bitmap.createScaledBitmap(bitmapInitial, 
				(int)(bitmapInitial.getWidth() * zoom), 
				(int)(bitmapInitial.getHeight() * zoom), 
				false);
		
		x = (int)(X * zoom) + newX; // + shiftX
		y = (int)(Y * zoom) + newY; // + shiftY
		
		xStart = x;
		yStart = y;
		
		mZoom = (float)zoom;
		fillBitmapFlash();

	}	
	
	/**
	 * Draw the eye
	 */
	@Override
	public void draw(Canvas canvas, boolean offsetZoom, float newX, float newY, double zoom) {
		Paint paint = new Paint();
		if (offsetZoom){
		// Drawing when user offset and zoom the image	
			RectF rect = new RectF(0, 0,  
					(int)(bitmapInitial.getWidth() * zoom), 
					(int)(bitmapInitial.getHeight() * zoom));
			rect.offsetTo((int)(X * zoom) + newX, //  + shiftX 
 					(int)(Y * zoom) + newY); //  + shiftY
			canvas.drawBitmap(bitmapInitial, rectInitial, rect, paint);
			
		} else {
		// Drawing in normal mode	
			if (face != null
				&& face.getEyesArrived() 
				&& (!mIsGoBack)
				&& mFlash)
				canvas.drawBitmap(bitmapFlash, x, y, paint);
			else
				canvas.drawBitmap(bitmap, x, y, paint);
		}
	}

	@Override
	public float getWidth() {
		return 0;
	}
	
}
