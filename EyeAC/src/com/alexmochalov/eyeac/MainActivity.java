package com.alexmochalov.eyeac;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.preference.*;
import android.support.v4.content.ContextCompat;
import android.util.*;
import android.view.*;
import android.widget.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.content.SharedPreferences.*;

import com.alexmochalov.eyeac.SurfaceViewScreenButtons.MessageType.*;
import com.alexmochalov.files.SelectFileDialog;
import com.alexmochalov.animation.*;

/**
 * 
 * @author Mochalov Alexey
 * App intended to train the tracking of the eyes movement (the Eye Accessing Cues)
 *
 */
public class MainActivity extends Activity  { //implements OnSharedPreferenceChangeListener
	Context mContext;
	 
	String initPath = Params.APP_FOLDER; // The path to save files
	String mFileName = "new_subtitles"; // Name of the current marking file (*.srt)
	static final String FILE_EXT[] = {".srt"}; // Extension of the marking files

	String mInfo = ""; // Text is shown in the bottom of the screen 
	
	SurfaceViewScreenButtons surface; // Surface to pain all visual elements
	
    Titles titles = new Titles(); // The list of the marks. It is used in the mode "move to button" 
    
    static final String PREFS_PERIOD = "PREFS_PERIOD";
    static final String PREFS_MODE = "PREFS_MODE";
    static final String FACE_NUMBER = "FACE_NUMBER";
	 
	Handler handlerDlg; // To show results, wait and hide results
	Handler handlerGrp; // To wait users answer in the mode "Groups of the movements"
	// To hide and show the action bar
	ActionBar actionBar;
	// Menu items to set ebable/disable and change icon
	MenuItem mMenuItemRec; 
	MenuItem mMenuItemPlay; 
	MenuItem mMenuItemStop;
	MenuItem mMenuItemSubmenu;
	// To shift screen buttons when the action bar is visible
	int actionBarHeight;
	// Actions from action bar buttons. It is used  in the mode "move to button" 
	enum Action {play, stop, record};
	Action mAction;
	
	boolean mStop = false; // In the mode "move to button": replay finished
	Timer mTimer; // To show time of the stored marks
	MyTimerTask mMyTimerTask;
	long timeStart;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.activity_main);
		
		actionBar = getActionBar();
		actionBar.setTitle("");
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#aa000000")));

		//actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#bb111111")));		
		actionBar.hide();
		
		mContext = this;
		
		actionBarHeight = getActionBarHeight();
		// If there is not found APP_FOLDER, create it
		checkDirectory();
		
		//prefs.registerOnSharedPreferenceChangeListener(this);
		
		//surface.setRects(displaymetrics.widthPixels, displaymetrics.heightPixels);
		//surface.callback = new SurfaceViewScreenButtons.MyCallbackIv(){
		//	@Override
		//	public void callbackAction(String action) {
		//		//executeCommand(action);
		//	}};
		 
	}
	
	/**
	* If there is not found APP_FOLDER, create it
	**/
	private void checkDirectory() {
		File file = new File(Params.APP_FOLDER);
		if(!file.exists()){                          
			file.mkdirs();                  
		}
	}

	// Calculate ActionBar height
	private int getActionBarHeight(){
		TypedValue tv = new TypedValue();
		if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
			return TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
		}
		return 96;
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		//this.menu = menu;
		//MenuItem modeMenuItem = menu.findItem(R.id.action_mode);
		//modeMenuItem.setActionView(R.layout.custom_item_mode);
		//View view = modeMenuItem.getActionView();
		//TextView textView = (TextView)view.findViewById(R.id.title);
		//textView.setText("wertwer werwer");
		//modeMenuItem.setTitle(info+" ...");

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	    mMenuItemRec = (MenuItem) menu.findItem(R.id.action_record);
	    mMenuItemStop = (MenuItem) menu.findItem(R.id.action_stop);
	    mMenuItemSubmenu = (MenuItem) menu.findItem(R.id.action_submenu);
	    mMenuItemPlay = (MenuItem) menu.findItem(R.id.action_play);
	    
		if (surface.getMode() == 2){
			actionBar.show();
			setButtons(Action.stop);
		} else
			actionBar.hide();		
	    
	    return true;
	}	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final SelectFileDialog selectFileDialog;
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		  
		switch (id) {
		case R.id.action_mode:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setTitle(getResources().getString(R.string.mode));
			
			String[] modes = getResources().getStringArray(R.array.mode);
			
			builder.setItems( modes,
					
			    new DialogInterface.OnClickListener() 
			    {
			        public void onClick(DialogInterface dialog, int which) { /* which is an index */
			        	if (which == 3)
			        		which = 999;
			        	
						if (which == 1)
						{
							DialogModeGroup dialogMode = new DialogModeGroup(MainActivity.this, surface);
							dialogMode.show();
							
						}
						
						if (which == 2){
							surface.setTextTopShift(actionBarHeight);
							actionBar.show();
							setButtons(Action.stop);
						} else {
							surface.setTextTopShift(0);
							actionBar.hide();	
						}
						
			        	setMode(which);
			        	//MenuItem modeMenuItem = menu.findItem(R.id.action_mode);
						//modeMenuItem.setTitle(info+"...");
			        }
			    }); 
			builder.show();			
			return true; 
		case R.id.action_exit:
			//finish();
			//System.exit(0);
			dialogExit();
			return true;
		case R.id.action_about:
			DialogAbout dialog = new DialogAbout(this);
			dialog.show();
			return true;
		case R.id.action_help:
			DialogHelp dialogHelp = new DialogHelp(this);
			dialogHelp.show();
			return true;
		case R.id.action_speed:
			surface.showSeekBarSpeed();
			return true;
		case R.id.action_move:
			surface.setMoveResize();
			surface.setOffset();
			item.setChecked(!item.isChecked());
			return true;
		case R.id.action_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			// Parameters to set preference summaries 
			intent.putExtra("COUNT_DOWN_TIME",10);
			intent.putExtra("countDownTime",12);
			startActivityForResult(intent, 0);
			break;
		case R.id.action_results:
			DialogResult dialogresult = new DialogResult(this, surface);
			dialogresult.execute();
			return true;			
		case R.id.action_record:
			titles.startRecord();
			setButtons(Action.record);
			return true;			
		case R.id.action_stop:
			titles.stopRecord();
			setButtons(Action.stop);
			return true;		
		case R.id.action_list:
			DialogTitles dialogTitles = new DialogTitles(this, titles);
			dialogTitles.show();
	
			return true;		
		case R.id.action_play:
			if (titles.size() == 0)
				Toast.makeText(mContext, getResources().getString(R.string.warning_void_titles) , Toast.LENGTH_LONG).show();
			else {
				setButtons(Action.play);
				titles.play();
			}	
			return true;		
		case R.id.action_save_titles:
			selectFileDialog = new SelectFileDialog(this, 
					initPath, 
					mFileName, FILE_EXT, 
					"", 
					true,  // Edit name
					false, // Show EMail button
					"" );
			 	selectFileDialog.callback = new SelectFileDialog.MyCallback() {
					@Override
					public void callbackACTION_SELECTED(String fileName) {
						if (! fileName.endsWith(".srt"))
							fileName = fileName + ".srt"; 
						if (fileName.equals("send picture by email")){
							//viewCanvas.saveWithCanvas(fileName, true);
							return;
					    } else 
							if (titles.save(mContext, fileName)){
								mFileName = fileName;
								Toast.makeText(mContext, "File saved ", Toast.LENGTH_LONG).show();
							}	
					}
				};

			 	selectFileDialog.show();
			return true;
			
		case R.id.action_load_titles:
			selectFileDialog = new SelectFileDialog(this, 
					initPath, 
					mFileName, 
					FILE_EXT, "", false, false,  "" );
			selectFileDialog.callback = new SelectFileDialog.MyCallback() {
					@Override
					public void callbackACTION_SELECTED(String fileName) {
						if (titles.load(mContext, fileName)){
							mFileName = fileName;
							Toast.makeText(mContext, "File loaded ", Toast.LENGTH_LONG).show();
						}	
					}
				};

			 	selectFileDialog.show();
			return true;
				  	
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	
	private Runnable stopWaiting = new Runnable() { 
		public void run() { 
			handlerGrp.removeCallbacks(stopWaiting);
			dialogResult(false);
		} 
	};        
	
	@Override
	protected void onResume(){
		super.onResume();
		File file = new File(Params.APP_FOLDER+"/designer");
		             
	    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		float i = prefs.getFloat("offsetX", 0); 
		float j = prefs.getFloat("offsetY", 0); 
		float k = prefs.getFloat("zoom", 1);
		
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		if (i + 1024 * k < 0 || j + 1024 * k < 0 || i > metrics.widthPixels-50 || j > metrics.heightPixels - 50 ){
			Editor editor = prefs.edit();
			editor.putFloat("offsetX", 0);
			editor.putFloat("offsetY", 0);
			editor.putFloat("zoom", 1);
			editor.apply();
		}
		
		Params.designMode = file.exists();               
		//Params.designMode = true;               
		
		Log.d("","S 1");
		Button button123 =  ((Button)findViewById(R.id.button123));
		button123.setVisibility(View.INVISIBLE);
		
		handlerDlg = new Handler();
		handlerGrp = new Handler();
		
		surface = ((SurfaceViewScreenButtons)findViewById(R.id.surfaceViewScreen));
		surface.callback = new SurfaceViewScreen.MyCallback() {
			@Override
			public void callbackGroupFinish() {
				surface.setMode(300); 
				handlerGrp.postDelayed(stopWaiting, Params.timeWaiting*1000);
			}

			@Override
			public void callbackGroupOk() {
				dialogResult(true);
			}

			@Override
			public void callbackGroupError() {
				dialogResult(false);
			}

			@Override
			public void onFinish() {
				if (mStop){
					runOnUiThread(new Runnable() {
					     @Override
					     public void run() {
							setButtons(Action.stop);
					    }
					});					
				}
			}
		}; 

		surface.listener = new SurfaceViewScreenButtons.OnEventListener() {
			@Override
			public void onTouchDown(String VAC) {
				if (mAction == Action.record)
					titles.addTitle(VAC);
			}

			@Override
			public void onTouchUp() {
				if (mAction == Action.record)
					titles.setTitleTimeUp();
			}
		};
		
	    int period = Math.max(prefs.getInt(PREFS_PERIOD, 50), 10);
		surface.setPeriod(period);
		
		surface.setMaxSpeed(100);
		surface.setProgressSpeed(period);
		
		String signal_correct = prefs.getString("signal_for_correct_answer", "0");
		surface.setSignal(signal_correct);
		int bg_color = Integer.parseInt(prefs.getString("bg_color", "-1"));
		
		Params.colorSurfaceBg = bg_color;
		if (bg_color == -1){
			Params.transparency = 128;
			Params.colorBtnTextDisable = Color.DKGRAY;
			Params.colorBtnBorder = Color.RED;
			Params.colorMessageText = Color.BLACK;
		} else{
			Params.transparency = 255;
			Params.colorBtnTextDisable = Color.rgb(150,159, 165);
			Params.colorBtnBorder = Color.WHITE;
			Params.colorMessageText = Color.WHITE;
		}
		
		
		boolean bOld = surface.allDirections();
		boolean b = prefs.getBoolean("extended_set", false);
		surface.setAllDirections(prefs.getBoolean("extended_set", false));
		if (bOld != b)
			surface.resetRects();

		int mode = prefs.getInt(PREFS_MODE, 0);
		if (!Params.designMode)
			mode = Math.min(mode, 1);
		if (mode == 300) mode = 0;
		
		//Log.d("","actionBarHeight "+actionBarHeight);
		if (mode == 2)
			surface.setTextTopShift(actionBarHeight);
			
		setMode(mode);	

		int face_number = prefs.getInt(FACE_NUMBER, 0);
		if (!Params.designMode)
			face_number = Math.min(face_number, 1);
		surface.setFaceNumber( face_number);
		surface.setMode(mode);
		surface.setPrefs(prefs);
		
		titles.listener = new Titles.OnEventListener() {
			@Override
			public void onStart(String VAC) {
				surface.move(VAC, false);
			}
			
			@Override
			public void onReturn(boolean stop) {
				surface.returnToCenter();
				mStop = stop; 
				//setButtons(Action.stop);
			}
			
		};
		//handlerProgress.postDelayed(hideProgressTask, 1000); 
	}
	
	private void startTimer(){
		mTimer = new Timer();
		mMyTimerTask = new MyTimerTask();

		timeStart =  System.currentTimeMillis();
		mTimer.schedule(mMyTimerTask, 0, 10);
	}
	
	private void stopTimer(){
		if (mTimer != null)
			mTimer.cancel();
		mTimer = null;
		setInfo(surface.getMode());		
	}
	
	  private void setButtons(Action action) {
			 if (action == Action.stop){ // Stop
				 mMenuItemRec.setEnabled(true);
				 mMenuItemRec.setIcon( ContextCompat.getDrawable(getApplicationContext(),R.drawable.button_rec));
				 
				 mMenuItemPlay.setEnabled(true);
				 mMenuItemPlay.setIcon( ContextCompat.getDrawable(getApplicationContext(),R.drawable.button_play));
				 
				 mMenuItemStop.setEnabled(false);
				 mMenuItemStop.setIcon( ContextCompat.getDrawable(getApplicationContext(),R.drawable.button_stop_g));
				 
				 mMenuItemSubmenu.setEnabled(true);
				 mMenuItemSubmenu.setIcon( ContextCompat.getDrawable(getApplicationContext(),R.drawable.submenu));
				 
				 stopTimer();
			 } else if (action == Action.record){ 
				 mMenuItemRec.setEnabled(false);
				 mMenuItemRec.setIcon( ContextCompat.getDrawable(getApplicationContext(),R.drawable.button_rec_g));
				 
				 mMenuItemPlay.setEnabled(false);
				 mMenuItemPlay.setIcon( ContextCompat.getDrawable(getApplicationContext(),R.drawable.button_play_g));

				 mMenuItemStop.setEnabled(true);
				 mMenuItemStop.setIcon( ContextCompat.getDrawable(getApplicationContext(),R.drawable.button_stop));
				 
				 mMenuItemSubmenu.setEnabled(false);
				 mMenuItemSubmenu.setIcon( ContextCompat.getDrawable(getApplicationContext(),R.drawable.submenu_g));
				 startTimer();
			 } else if (action == Action.play){ 
				 mMenuItemRec.setEnabled(false);
				 mMenuItemRec.setIcon( ContextCompat.getDrawable(getApplicationContext(),R.drawable.button_rec_g));
			 
				 mMenuItemPlay.setEnabled(false);
				 mMenuItemPlay.setIcon( ContextCompat.getDrawable(getApplicationContext(),R.drawable.button_play_g));

				 mMenuItemStop.setEnabled(true);
				 mMenuItemStop.setIcon( ContextCompat.getDrawable(getApplicationContext(),R.drawable.button_stop));
			 
				 mMenuItemSubmenu.setEnabled(false);
				 mMenuItemSubmenu.setIcon( ContextCompat.getDrawable(getApplicationContext(),R.drawable.submenu_g));
				 startTimer();
			 }
			 mAction = action;
		
	}

	private void setInfo(int mode) {
		if (mode == 0)
			mInfo = getString(R.string.mode_info_random);
		else if (mode == 1)
			mInfo = getString(R.string.mode_info_groups);
		else if (mode == 2)
			mInfo = getString(R.string.mode_info_tobuttom);
		else if (mode == 999)
			mInfo = getString(R.string.mode_info_coordinates);
		surface.setMessage(mInfo, MType.info);
	}

	private void setMode(int mode) {
			surface.setMode(mode);
			
		    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			int i = prefs.getInt("count", 3);
			Params.timeWaiting = prefs.getInt("time_answer", 4);
			Params.timeBetween = prefs.getInt("time_between_groups", 2);
			
			surface.setMaxCount(i);
			
			setInfo(mode);
	}

	@Override
	  protected void onPause(){
		  Log.d("","ON PAUSE");
		  surface.pause();
		  
		  SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		  Editor editor = prefs.edit();
		  editor.putInt(PREFS_PERIOD, surface.getPeriod());
		  editor.putInt(PREFS_MODE, surface.getMode());
		  editor.apply();
		  
		  surface = null;
		  
		  handlerDlg.removeCallbacksAndMessages(null);
		  handlerDlg = null;
		  handlerGrp.removeCallbacksAndMessages(null);
		  handlerGrp = null;
		  
		  super.onPause();
	  }	  
	 
	private void dialogResult(boolean Ok){
	 	String str;
	 	surface.incGroupsCount();
		if (Ok){
		 	surface.incGroupCountRight();
			surface.setMessage("Ok !!!", MType.ok);
		} else
			surface.setMessage("Oops (:", MType.ups);
		
		handlerGrp.removeCallbacks(stopWaiting);
		handlerDlg.postDelayed(closeDialogResult, Params.timeBetween*1000);
		
		surface.setMode(400);
	}
	
	private Runnable closeDialogResult = new Runnable() { 
		public void run() { 
			handlerDlg.removeCallbacks(closeDialogResult);
			//dialogResult.cancel();
			surface.setMessage("", null);
			surface.setMode(1);
			surface.startMoving();
			
		} 
	};        
	
	private void dialogExit(){
	    new AlertDialog.Builder(this)
        .setMessage(getString(R.string.question_exit))
        .setCancelable(false)
        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                 MainActivity.this.finish();
            }
        })
        .setNegativeButton(getString(R.string.no), null)
        .show();
	}
	 
//		seekBarSpeed.setVisibility(View.INVISIBLE);
	
	@SuppressLint("NewApi")
	@Override
	public void onBackPressed() {
		surface.pause();
		if (Build.VERSION.SDK_INT <= 10 || Build.VERSION.SDK_INT >= 14 && 
				ViewConfiguration.get(this).hasPermanentMenuKey())
			dialogExit();
		else openOptionsMenu();
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
			surface.showSeekBarSpeed();
			surface.addSeekBarProgressSpeed(-1);
	        return true;
	    case KeyEvent.KEYCODE_VOLUME_DOWN:
			surface.showSeekBarSpeed();
			surface.addSeekBarProgressSpeed(1);
	        return true;
	    case KeyEvent.KEYCODE_MENU:
			surface.pause();
            return false;	        
	    //case KeyEvent.KEYCODE_BACK:
		//	if (surface.isPlaying())
		//	dialogExit();
		//	return true;
	    default:
	    	
	    return super.onKeyDown(keyCode, event);			
		}
    }
/*
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String prefName) {
		if (prefName.equals("signal_for_correct_answer")){
			signal_correct = sharedPreferences.getString("signal_for_correct_answer", "0");
		}
	}
*/
	
	public static String msToString(long mediaPosition){
        long second = (mediaPosition / 1000) % 60;
        long minute = (mediaPosition / (1000 * 60)) % 60;
        long hour = (mediaPosition / (1000 * 60 * 60)) % 24;

        return String.format("%02d:%02d:%02d,%03d", hour, minute, second, mediaPosition-
      		  second*1000-
      		  minute*1000*60-
      		  hour*1000 * 60 * 60);
	}
	
	
	class MyTimerTask extends TimerTask {

		@Override
		public void run() {
			long time =  System.currentTimeMillis();
			final String strDate = msToString(time-timeStart);
			
			surface.setMessage(strDate, MType.info);
		}
	}	
}


//SELECT * FROM instanceof android.app.Activity
