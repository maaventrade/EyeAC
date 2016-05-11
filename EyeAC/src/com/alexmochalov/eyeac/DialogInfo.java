package com.alexmochalov.eyeac;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DialogInfo extends Dialog  implements ImageGetter{
	private Context context;
	private Dialog dialog;

	MyCallbackBv callback = null;
	interface MyCallbackBv
	{
		void okPressed(); 
	} 
	
	public DialogInfo(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dialog = this;

		setContentView(R.layout.info);

		TextView textViewInfo = (TextView)findViewById(R.id.textViewInfo);	
       
		String strInfo = "<H1>Results</H1>"; 
				
        Spanned spanned = Html.fromHtml(strInfo, this, null);		
        textViewInfo.setText(spanned);		
        textViewInfo. setMovementMethod(LinkMovementMethod.getInstance());
        textViewInfo. setMovementMethod(new ScrollingMovementMethod());
        
		Button button = (Button)findViewById(R.id.buttonOKInfo);
		button.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
        
		button = (Button)findViewById(R.id.buttonExit);
		button.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
		    	System.exit(0);	    	
				//dismiss();
			}
		});
	}

    @Override
    public Drawable getDrawable(String arg0) {
        // TODO Auto-generated method stub
        int id = 0;
/*
        if(arg0.equals("tu1.png")){
            id = R.drawable.list;
        } else if(arg0.equals("tu2.png")) {
            id = R.drawable.edit;
        } else if(arg0.equals("tu3.png")) {
            id = R.drawable.lock1;
        } else if(arg0.equals("tu4.png")) {
            id = R.drawable.menu_submenu;
        }
        
        LevelListDrawable d = new LevelListDrawable();
        Drawable empty = context.getResources().getDrawable(id);
        d.addLevel(0, 0, empty);
        d.setBounds(0, 0, empty.getIntrinsicWidth(), empty.getIntrinsicHeight());
*/
        //return d;
        return null;
    }        		

}

