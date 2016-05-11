package com.alexmochalov.eyeac;

import android.content.*;

import java.io.*;
import java.util.*;

import com.alexmochalov.eyeac.SurfaceViewScreenButtons.OnEventListener;

import android.os.Handler;
import android.util.Log;
import android.widget.*;

public class Titles extends ArrayList{
	private boolean mRecordOn = false;
	private long timeStart;

	private int mIndex;
	
	private Handler handler = new Handler(); 
	
	OnEventListener listener;
	public interface OnEventListener{
		public void onStart(String VAC);
		public void onReturn(boolean stop);
	}
	
	public boolean save(Context context, String fileName)
	{
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			for (int i = 0; i < size(); i++){
				Title t = (Title)get(i);
				out.write(""+(i+1)+"\n");
		        out.write(t.getStringTime()+"\n");
				out.write(t.getText()+"\n");
				out.write("\n");
			} 
			out.close();			
	    } catch (FileNotFoundException e) {
	        Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
			return false;
	    } catch (IOException e) {
	        Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
			return false;
	    }	
		return true;
	}
	
	public boolean load(Context context, String fileName)
	{
		clear();
		try {
			FileInputStream fin = new FileInputStream(fileName);
			
			char chr = 65279; // byte-order mark (BOM).
			String utf8 = ""+chr; // Delete this symbol from the string
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(fin));

		    String line1 = null;
		    String line2 = null;
		    String line3 = null;
		    String line4 = null;
		    while (true) {
		    	if ((line1 = reader.readLine()) == null) break;
		    	if ((line2 = reader.readLine()) == null) break;
		    	if ((line3 = reader.readLine()) == null) break;
		    	if ((line4 = reader.readLine()) == null) break;

				line2 = line2.replace(utf8, "");
				line3 = line3.replace(utf8, "");
		    	
				Title title = new Title(line2, line3);
				add(title);
		    	
		    }			
			
			fin.close();			
	    } catch (FileNotFoundException e) {
	        Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
			return false;
	    } catch (IOException e) {
	        Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
			return false;
	    }	
		return true;
	}
	
	public CharSequence getString(int position)
	{
		Title t = (Title)(get(position));
		return t.getString();
	}
	
	//private ArrayList<Title> titles = new ArrayList<Title>();

	public void startRecord() {
		clear();
		mRecordOn = true;
		timeStart =  System.currentTimeMillis();
	}

	public void stopRecord() {
		mRecordOn = false;
	}

	public void addTitle(String VAC) {
		long time =  System.currentTimeMillis();
		Title title = new Title( (int)(time-timeStart) , 0, VAC);
		add(title);
	}

	public void setTitleTimeUp() {
		long time =  System.currentTimeMillis();
		if (size() > 0){
			Title t = (Title)(get(size() - 1));
			t.setTitleTimeUp((int)(time-timeStart));
		}
	}

    private Runnable startMovingTask = new Runnable() { 
		   public void run() {
			   String text = ((Title)get(mIndex)).getText(); 
			   listener.onStart(text);
			   
			   handler.removeCallbacks(startMovingTask);
			   handler.postDelayed(returnMovingTask, ((Title)get(mIndex)).getTimeOff()-((Title)get(mIndex)).getTimeOn());
			   Log.d("", "start "+(((Title)get(mIndex)).getTimeOff()-((Title)get(mIndex)).getTimeOn()));
		   } 
	};

	private Runnable returnMovingTask = new Runnable() { 
		public void run() { 
			handler.removeCallbacks(returnMovingTask);

			mIndex++;
			if (mIndex <= size()-1 ){
				handler.postDelayed(startMovingTask, ((Title)get(mIndex)).getTimeOn()-((Title)get(mIndex-1)).getTimeOff());
				listener.onReturn(false);
			} else
				listener.onReturn(true);
				
		} 
	};        
	
	public void play() {
		handler.removeCallbacks(returnMovingTask);
		handler.removeCallbacks(startMovingTask);

		mIndex = 0;
		
		handler.postDelayed(startMovingTask, ((Title)get(mIndex)).getTimeOn());
		
	}
}


class Title {
	private int mTimeOn = 0;
	private int mTimeOff = 0;
	private String mText = "";

	public Title(int timeOn, int timeOff, String text) {
		super();
		mTimeOn = timeOn;
		mTimeOff = timeOff;
		mText = text;
	}

	public static long stringToMs(String str){
		return Long.parseLong(str.substring(0,2))*1000*60*60+
			   Long.parseLong(str.substring(3,5))*1000*60+
			   Long.parseLong(str.substring(6,8))*1000+
			   Long.parseLong(str.substring(9,12));
	}
	
	public Title(String line, String text) {
		super();

		mTimeOn = (int)stringToMs(line.substring(0, 12));
		mTimeOff = (int)stringToMs(line.substring(17, 29));
		
		mText = text;
	}

	public String getText()
	{
		return mText;
	}
	
	public void setTitleTimeUp(int i) {
		if (mTimeOff == 0)
			mTimeOff = i;
	}

	public static String msToString(int mediaPosition){
        long second = (mediaPosition / 1000) % 60;
        long minute = (mediaPosition / (1000 * 60)) % 60;
        long hour = (mediaPosition / (1000 * 60 * 60)) % 24;

        return String.format("%02d:%02d:%02d,%03d", hour, minute, second, mediaPosition-
      		  second*1000-
      		  minute*1000*60-
      		  hour*1000 * 60 * 60);
	}

	public String getTimeOnStr() {
		return msToString(mTimeOn);
	}

	public int getTimeOn() {
		return mTimeOn;
	}

	public int getTimeOff() {
		return mTimeOff;
	}

	public String getTimeOffStr() {
		return msToString(mTimeOff);
	}
		
	public String getStringTime() {
	  return msToString(mTimeOn)+" --> "+msToString(mTimeOff);
	 }

	 public String getString() {
	  return msToString(mTimeOn)+" --> "+msToString(mTimeOff)+" "+mText;
	 }
	
}
