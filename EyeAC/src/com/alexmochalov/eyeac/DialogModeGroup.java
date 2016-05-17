package com.alexmochalov.eyeac;
import com.alexmochalov.eyeac.R;
import com.alexmochalov.eyeac.R.id;
import com.alexmochalov.eyeac.R.layout;
import com.alexmochalov.animation.SurfaceViewScreen;

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

/**
 * 
 * @author @Alexey Mochalov
 * Dialog for setting parameters when the mode "Groups of movings" is selected
 */
public class DialogModeGroup  extends Dialog
{
	//PreferenceFragment context;
	private SharedPreferences prefs;
	Dialog dialog;
	
	SurfaceViewScreenButtons mSurface;

	public DialogModeGroup(Context context, SurfaceViewScreenButtons surface) {
		super(context);
		mSurface = surface;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		dialog = this;
		dialog.setTitle("Parameters");
	 	dialog.setContentView(R.layout.dialog_mode_group_pref);
		
		EditText e = (EditText)dialog.
			findViewById(R.id.editTextCount);
		int i = prefs.getInt("count", 3);
		e.setText(""+i);
		
		e = (EditText)dialog.
			findViewById(R.id.editTextTimeAnswer);
		i = prefs.getInt("time_answer", 4);
		e.setText(""+i);
		
		e = (EditText)dialog.
			findViewById(R.id.editTextTimeBetweenGroups);
		i = prefs.getInt("time_between_groups", 2);
		e.setText(""+i);
		
		Button b = (Button)dialog.findViewById(R.id.dialogmodegroupprefButtonOk);
		b.setOnClickListener(new Button.OnClickListener(){
				@Override
				public void onClick(View v) 
				{
					int i;
					EditText e = (EditText)dialog.
						findViewById(R.id.editTextCount);
					
					int max_count = toInt(e.getText().toString(), 3);
					Editor editor = prefs.edit();
					editor.putInt("count", Math.max(max_count, 2));
					
					e = (EditText)dialog.
							findViewById(R.id.editTextTimeAnswer);
					int time_answer = toInt(e.getText().toString(), 4);
					editor.putInt("time_answer", Math.max(time_answer, 1));
						
					e = (EditText)dialog.
							findViewById(R.id.editTextTimeBetweenGroups);
					int time_between_groups = toInt(e.getText().toString(), 2);
					editor.putInt("time_between_groups", Math.max(time_between_groups, 1));
					editor.apply();
					
					Params.timeWaiting = time_answer;
					Params.timeBetween = time_between_groups;
					mSurface.setMaxCount(max_count);
					
					dialog.dismiss();
				}

		});
		
		b = (Button)dialog.findViewById(R.id.dialogmodegroupprefButtonCancel);
		b.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View d)
			{
				dialog.dismiss();
			}
		});
	}

	protected int toInt(String string, int defaultValue) {
		try{
			return Integer.parseInt(string); 
		} catch(Exception e){
			return defaultValue;
		}
	}

	
	
}
