package com.alexmochalov.eyeac;

import android.app.*;
import android.content.*;
import android.content.DialogInterface.*;
import android.text.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;

import android.view.View.OnClickListener;

public class DialogResult
{
	private Context context;
	private SurfaceViewScreenButtons surfaceViewScreenButtons;
	
	public DialogResult(Context context, SurfaceViewScreenButtons surfaceViewScreenButtons){
		super();
		this.context = context;
		this.surfaceViewScreenButtons = surfaceViewScreenButtons;
	}	

	public void execute(){
		final Dialog dialog = new Dialog(context, 
			R.style.DialogSlideAnim);
			
	 	dialog.setContentView(R.layout.dialog_result);
		dialog.setTitle(context.getResources().getString(R.string.results));

		TextView dialogresultTextView1 = (TextView)dialog.findViewById(R.id.dialogresultTextView1);
        dialogresultTextView1.setText(
			Html.fromHtml(surfaceViewScreenButtons.getResultStr()));  

		android.widget.Button b = (android.widget.Button)dialog.findViewById(R.id.buttonContinue);
		b.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{	
					dialog.dismiss();
				}
			});
		b = (android.widget.Button)dialog.findViewById(R.id.buttonReset);
		b.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{	
					surfaceViewScreenButtons.resetRightCount();
					surfaceViewScreenButtons.resetCount();
					surfaceViewScreenButtons.resetDirs();
					surfaceViewScreenButtons.returnToCenter();
					dialog.dismiss();
				}
			});
			
		dialog.show();
	}
	
}
