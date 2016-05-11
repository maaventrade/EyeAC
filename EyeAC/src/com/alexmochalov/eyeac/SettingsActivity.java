package com.alexmochalov.eyeac;

import java.util.List;

import android.app.*;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.*;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.*;
import android.view.*;

import java.util.*;
import java.lang.reflect.*;

import com.alexmochalov.eyeac.R;
import com.alexmochalov.eyeac.R.drawable;
import com.alexmochalov.eyeac.R.xml;

public class SettingsActivity extends PreferenceActivity
{
	private SharedPreferences prefs;
	private Context context;
	
	public SettingsActivity(){
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
							 WindowManager.LayoutParams.FLAG_FULLSCREEN);					 
       
		getFragmentManager().beginTransaction()
        	.replace(android.R.id.content, new PreferencesFragment()).commit();

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		context = this;
	}

	private void setSummary(Preference pref, String val, int res, int resData){
		String[] alias = getResources().getStringArray(res); 
		String[] data = getResources().getStringArray(resData);
		
		ListPreference p  = (ListPreference)pref;
		
		for (int j=0 ; j < alias.length ; j++) { 
			if (data[j].equals(val)) { 
				pref.setSummary(alias[j]);
				p.setValueIndex(j);
				return; 
			} 
		} 
	}

	public class PreferencesFragment
	extends PreferenceFragment
	{ 
		@Override
		public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);

	        // Load the preferences from an XML resource
	        addPreferencesFromResource(R.xml.preferences);
			
	        // Set image path as the text of the summary
			//pref1 = findPreference("IMAGE_PATH");	
			//pref1.setSummary(prefs.getString("IMAGE_PATH", ""));
			
			Preference pref = findPreference("signal_for_correct_answer");
			String i = prefs.getString("signal_for_correct_answer", "0");
			if (i.equals("0"))
				pref.setSummary("None");
			else 
				pref.setSummary("Vibration");
			
			pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference,
						Object newValue) {
					if (newValue.equals("0"))
						preference.setSummary("None");
					else 
						preference.setSummary("Vibration");
					
					Editor editor = prefs.edit();
					editor.putString("signal_for_correct_answer", newValue.toString());
					editor.apply();
					
					return true;
				}
			});			
			
			pref = findPreference("bg_color");
			if (pref != null){
			i = prefs.getString("bg_color", "0");
			if (i.equals("0"))
				pref.setSummary("Black");
			else 
				pref.setSummary("White");
 
			pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference,
													  Object newValue) {
						if (newValue.equals("0"))
							preference.setSummary("Black");
						else 
							preference.setSummary("White");

						Editor editor = prefs.edit();
						editor.putString("bg_color", newValue.toString());
						editor.apply();

						return true;
					}
				});
			}
			pref = findPreference("extended_set");
			boolean f = prefs.getBoolean("extended_set", false);
			if (f)
				pref.setSummary("Vc,Up,Vr,Ar,Ad,Dn,K,Ac,F");
			else 
				pref.setSummary("Vc,Vr,Ar,Ad,K,Ac");

			pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference pref,
												  Object newValue) {
					boolean f = (boolean) newValue;
					if (f)
						pref.setSummary("Vc,Up,Vr,Ar,Ad,Dn,K,Ac,F");
					else 
						pref.setSummary("Vc,Vr,Ar,Ad,K,Ac");

					Editor editor = prefs.edit();
					editor.putBoolean("extended_set", f);
					editor.apply();

					return true;
				}
			});
			//FragmentTransaction ft = getFragmentManager().beginTransaction();

			/*
			pref = findPreference("mode");
			int mode = Integer.parseInt(prefs.getString("mode", "0"));
			if (!Params.designMode)
				mode = Math.min(mode, 1);
			
			if (!Params.designMode){
				ArrayList <String> entryes = new ArrayList <String>(
					Arrays.asList(getResources().getStringArray(R.array.mode)));
				ArrayList <String> entryValues = new ArrayList <String>(
					Arrays.asList(getResources().getStringArray(R.array.modeData)));
				
				for (int k = entryes.size()-1; k > 1; k--){
					entryes.remove(k);
					entryValues.remove(k);
				}
					
				String e[] = new String[entryes.size()];
				String ev[] = new String[entryValues.size()];
				
				e = entryes.toArray(e);
				ev = entryValues.toArray(ev);
				
				ListPreference l = (ListPreference)pref;
				l.setEntries(e);
				l.setEntryValues(ev);
			}
			setSummary(pref, ""+mode, R.array.mode, R.array.modeData);
			
			pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference pref,
													  Object newValue) {
						setSummary(pref, (String)newValue, R.array.mode, R.array.modeData);
						Editor editor = prefs.edit();
						editor.putString("mode", (String)newValue);
						editor.apply();
						
						//FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
						String s = (String)newValue;
						if (s.equals("1"))
						{
							DialogModeGroupPref dialog = new DialogModeGroupPref(prefs);
							dialog.show(getFragmentManager(), "dialog");
						}
						return true;
					}
				});
			
			*/
			
			pref = findPreference("face_number");
			int face_number = prefs.getInt("FACE_NUMBER", 0);
			setIcon(pref, face_number);
 
			pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference,
													  Object newValue) {

						int face_number = prefs.getInt("FACE_NUMBER", 0);
						setIcon(preference, face_number);

						return true;
					}
				});
			
		}
		
		private void setIcon(Preference pref, int face_number){
			Drawable d = null;
			
            switch (face_number){
            case 0:
                d = ContextCompat.getDrawable(getApplicationContext(), R.drawable.face10);
            	break;
            case 1:
                d = ContextCompat.getDrawable(getApplicationContext(), R.drawable.face20);
            	break;
            default:
                d = ContextCompat.getDrawable(getApplicationContext(), R.drawable.face10);
            	break;
            }
			
			pref.setIcon(d);
		}
	}
	
	
}
