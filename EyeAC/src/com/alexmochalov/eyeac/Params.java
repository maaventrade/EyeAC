package com.alexmochalov.eyeac;

import android.graphics.Color;
import android.os.*;

/**
* Global variables
*/
public class Params {
	// Color of the buttons
	public static int colorSurfaceBg = Color.BLACK;
	public static int colorMessageText = Color.WHITE;
	
	public static int colorBtnBgDisable = Color.rgb(49, 83, 109);
	public static int colorBtnBg = Color.rgb(49, 83, 109);

	public static int colorBtnTextDisable = Color.rgb(150,159, 165);
	public static int colorBtnText = Color.rgb(254,254,254);
	public static int colorBtnPressedText = Color.rgb(255,255,255);
	public static int colorBtnBorder;
	
	
	public static int fontSize = 24; // Maximal font size
	public static int fontSize4= 12;

	public static int width = 0;
	public static int height = 0;
	
	// Thansparency of the buttons
	public static int transparency = 50;
	
	 // The initial directory for the files selection // storage          
	final static String PROGRAMM_FOLDER = "xolosoft";
	public final static String APP_FOLDER = Environment.getExternalStorageDirectory().getPath() + "/" + 
		PROGRAMM_FOLDER + "/EyeAC";
	public static boolean designMode = false;
	
	public static int timeWaiting;
	public  static int timeBetween;
	

}
