package com.alexmochalov.settings;
import com.alexmochalov.eyeac.R;
import com.alexmochalov.eyeac.R.id;
import com.alexmochalov.eyeac.R.layout;

import android.content.*;
import android.content.SharedPreferences.Editor;
import android.app.*;
import android.os.*;
import android.widget.*;
import android.preference.*;
import android.app.*;
import android.preference.PreferenceFragment;
import android.view.*;
import android.view.View.*;

public class DialogModeGroupPref  extends DialogFragment
{
	//PreferenceFragment context;
	private SharedPreferences prefs;
	Dialog dialog;

	public DialogModeGroupPref(SharedPreferences prefs) {
		super();
		this.prefs = prefs;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		dialog = getDialog();
		dialog.setTitle("Title!");
		View v = inflater.inflate(R.layout.dialog_mode_group_pref, null);
		
		EditText e = (EditText)v.
			findViewById(R.id.editTextCount);
		int i = prefs.getInt("count", 3);
		e.setText(""+i);
		
		e = (EditText)v.
			findViewById(R.id.editTextTimeAnswer);
		i = prefs.getInt("time_answer", 4);
		e.setText(""+i);
		
		e = (EditText)v.
			findViewById(R.id.editTextTimeBetweenGroups);
		i = prefs.getInt("time_between_groups", 2);
		e.setText(""+i);
		
		Button b = (Button)v.findViewById(R.id.dialogmodegroupprefButtonOk);
		b.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View d)
				{
					int i;
					EditText e = (EditText)dialog.
						findViewById(R.id.editTextCount);
					
					i = toInt(e.getText().toString(), 3);
					Editor editor = prefs.edit();
					editor.putInt("count", Math.max(i, 2));
					
					e = (EditText)dialog.
							findViewById(R.id.editTextTimeAnswer);
					i = toInt(e.getText().toString(), 4);
					editor.putInt("time_answer", Math.max(i, 1));
						
					e = (EditText)dialog.
							findViewById(R.id.editTextTimeBetweenGroups);
					i = toInt(e.getText().toString(), 2);
					editor.putInt("time_between_groups", Math.max(i, 1));
					editor.apply();
					dialog.dismiss();
				}
			});
		b = (Button)v.findViewById(R.id.dialogmodegroupprefButtonCancel);
		b.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View d)
			{
				dialog.dismiss();
			}
		});
		
		return v;
	}

	protected int toInt(String string, int defaultValue) {
		try{
			return Integer.parseInt(string); 
		} catch(Exception e){
			return defaultValue;
		}
	}

	
	
}
