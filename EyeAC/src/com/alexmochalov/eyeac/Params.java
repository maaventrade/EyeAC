package com.alexmochalov.eyeac;

import android.graphics.Color;
import android.os.*;
//import com.alexmochalov.eyeac.Params.modes.Mode;

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
	
	private String initPath = "/sdcard"; // The initial directory for the files selection // storage          
	final static String PROGRAMM_FOLDER = "xolosoft";
	public final static String APP_FOLDER = Environment.getExternalStorageDirectory().getPath() + "/" + 
		PROGRAMM_FOLDER + "/EyeAC";
	public static boolean designMode = false;
	
	public static int timeWaiting;
	public  static int timeBetween;
	
/*
	static class states{
		enum State{stop, pause, random, group}
	}
	private static State state = State.stop;
	
	public static boolean isStop(){
		return state == State.stop;
	}
	
	public static boolean isPause(){
		return state == State.pause;
	}
	
	public static boolean isGroup(){
		return state == State.group;
	}
	
	
	public static void setStateRandom(){
		state = State.random;
	}
	
	public static void setStatePause(){
		state = State.pause;
	}


	public static void setStateGoup(){
		state = State.pause;
	}
	*/
	/*
	private Subtitles subtitles; // The marking

	private SurfaceViewScreenButtons imageViewButtons;
	private ImageViewMarkPanel imageViewMarkPanel; // View for the drawing of the marking 
	
    
	private int position = 0;
	
	
	public int fontSize = 24; // Maximal font size
	public int fontSize4= 12;
	*/
	////////////////////////////////////////////////
	/*
	static class modes{
		enum Mode{media, random}
	}
	private static Mode mode = Mode.media;
	private static boolean replay = false;
	
	public void setModeMedia(){
		mode = Mode.media;
		replay = false;
//		context.getActionBar().setTitle(context.getResources().getString(R.string.title_media));
	}
	
//	public static void setModeRandom(){
//		mode = Mode.random;
//		replay = false;
//		context.getActionBar().setTitle(context.getResources().getString(R.string.title_random));
	}
/*	
	public static boolean isModeRandom(){
		return mode == Mode.random;
	}
	
	public static boolean isModeMedia(){
		return mode == Mode.media;
	}
	
	public static boolean isReply(){
		return replay;
	}
*/	

/*
public void setButtons(String[] s){
	String fileName = subtitles.getFileName();
	
	if (!fileName.equals("") && s.length > 0){
		String str = s[0];
		for (int i=1; i<s.length; i++)
			str = str+","+s[i];
		 
		for (int i=0; i<libList.size(); i++)
			for (int j=0; j<libList.get(i).getChildren().size(); j++)
				if (libList.get(i).getChildren().get(j).getName().equals(fileName)){
					libList.get(i).getChildren().get(j).setButtons(str);
					libList.get(i).getChildren().get(j).setMarkType(markType);
				}	
	}
}
*/
/*
public void setMarkType(int markType){
	this.markType = markType;
	imageViewButtons.setMarkType(markType);
}
    
public int getMarkType(){
	return markType;
}
    
public void loadSubtitles(String fileName) {
	setInitPath(fileName);
	if (fileSourceIsResource())
		subtitles.loadSubtitlesFromResource(context, context.screenButtons.getVAK());
	else	
		subtitles.loadSubtitles(context, context.screenButtons.getVAK());
}

public void deleteSubtitles() {
	subtitles.deleteSubtitles();
}

public States getPlayerState() {
	return state;
}

public void setPlayerState(States state){
	this.state = state;
	//context.sct.invalidate();
}	

public void saveSubtitles() {
	subtitles.saveSubtitles(context);
}

public void clearSubtitles() {
	subtitles.clearSubtitles();
}

public void setSubtitleTimeOff(int mediaPosition) {
	subtitles.setSubtitleTimeOff(mediaPosition);
}

public void addSubtitleFinish(int currentPosition){
	subtitles.addSubtitleFinish(currentPosition);
}

public void addSubtitle(int currentPosition, String action){
	subtitles.addSubtitle(currentPosition, action);
}

public Subtitles getSubtitles() {
	return subtitles;
}

public String getInitPath() {
	return initPath;
}


public void setFileName(String fileName){
	subtitles.setFileName(fileName);
}
*/
/*
public void setFileName(String fileName, boolean addToLib) {
	this.fileName = fileName;
	setInitPath();
	
	if (addToLib)
		subtitles.addFileToLib(fileName, libList);
	
}
*/
/*
public String getFileName() {
	return subtitles.getFileName();
}

public String getFileNameOnly() {
	return subtitles.getFileNameOnly();
}

public String getSubsName() {
	return subtitles.getSubsName();
}

public ActivityMain getContext() {
	return context;
}

public ArrayList<Subtitle> getSubs(){
	return subtitles.getSubs();
}

void setInitPath(String fileName){
	int dotposition= fileName.lastIndexOf("/");
	initPath = fileName.substring(0,dotposition+1);
	initPath = initPath.replace("//", "/");
}

public void makeAppFolder(){
	File file = new File(APP_FOLDER);
	if(!file.exists()){                          
		file.mkdirs();                  
	}
}

public void fileSelected(String fileName, String subPath, String buttonsString, int markType) {
	subtitles.setSubPath(subPath);
	subtitles.setButtonsString(buttonsString);
	subtitles.setButtons(context.screenButtons.getVAK());
	this.markType = markType;
}

public void setButtonsString(String buttonsString) {
	subtitles.setButtonsString(buttonsString);
}

public void fileSelected(String fileName) {
	if (fileSourceIsInternetOrResource()){
		String f;
		int i = fileName.lastIndexOf("/");
		if ( i>=0 )
			f = fileName.substring(i+1);
		else
			f = fileName;
		subtitles.setSubPathFromMediaPath(APP_FOLDER+"/"+f);
	}
	else subtitles.setSubPathFromMediaPath(fileName);
	loadSubtitles(subtitles.getSubsName());
}

public boolean fileSourceIsInternetOrResource() {
	return fileSource == FileSources.internet || fileSource == FileSources.resourse;
}

public boolean fileSourceIsInternet() {
	return fileSource == FileSources.internet;
}

public boolean fileSourceIsResource() {
	return fileSource == FileSources.resourse;
}

public void setFileSource(String fileSourceStr) {
	if (fileSourceStr.equals("sdcard"))
		fileSource = FileSources.sdcard;
	else if (fileSourceStr.equals("internet"))
		fileSource = FileSources.internet;
	else if (fileSourceStr.equals("resourse"))
		fileSource = FileSources.resourse;
}

public void setFileSourceResource() {
	 fileSource = FileSources.resourse;
}

public void setFileSourceInternet() {
	 fileSource = FileSources.internet;
}

public void setFileSourceSdcard() {
	 fileSource = FileSources.sdcard;
}

public String getFileSourceStr() {
	return fileSource.toString();
}
public String getButtonString() {
	return subtitles.getButtonsString();
}
public void setScreen(SurfaceViewScreenButtons imageViewButtons, ImageViewMarkPanel imageViewMarkPanel) {
	this.imageViewButtons = imageViewButtons;
	this.imageViewMarkPanel = imageViewMarkPanel;
}
*/
}
