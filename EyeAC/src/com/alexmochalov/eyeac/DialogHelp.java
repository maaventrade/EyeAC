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

public class DialogHelp extends Dialog  implements ImageGetter{
	private Context context;
	private Dialog dialog;

	MyCallbackBv callback = null;
	interface MyCallbackBv
	{
		void okPressed(); 
	} 
	
	public DialogHelp(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTitle(context.getResources().getString(R.string.action_help_ok));
		dialog = this;

		setContentView(R.layout.dialog_help);

		TextView textViewHelp = (TextView)findViewById(R.id.textViewHelp);	
       
		String strHelp = context.getResources().getString(R.string.info); 

        Spanned spanned = Html.fromHtml(strHelp, this, null);		
        textViewHelp.setText(spanned);		
        textViewHelp. setMovementMethod(LinkMovementMethod.getInstance());
        
		Button button = (Button)findViewById(R.id.buttonOKHelp);
		button.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
        
	}

    @Override
    public Drawable getDrawable(String arg0) {
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

