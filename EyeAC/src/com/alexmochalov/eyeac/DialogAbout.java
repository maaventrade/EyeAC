package com.alexmochalov.eyeac;

import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import android.text.*;
import android.text.method.LinkMovementMethod;

public class DialogAbout extends Dialog
{
	Context context;
	Dialog dialog;
	
	public DialogAbout(Context context) {
		super(context);
		this.context = context;
	}
  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTitle(context.getResources().getString(R.string.action_about));
		dialog = this;

		setContentView(R.layout.dialog_about);
		TextView text = (TextView)findViewById(R.id.dialogaboutTextView1); 
		text.setText(Html.fromHtml(context.getString(R.string.about)));
		text. setMovementMethod(LinkMovementMethod.getInstance());
		
		
	}
}
