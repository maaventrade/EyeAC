package com.alexmochalov.eyeac;

import java.util.ArrayList;

import com.alexmochalov.eyeac.R;
import com.alexmochalov.eyeac.R.drawable;
import com.alexmochalov.eyeac.R.id;
import com.alexmochalov.eyeac.R.layout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.app.Dialog;
import android.app.AlertDialog.Builder;

public class CustomListPreference extends ListPreference
{   
    CustomListPreferenceAdapter customListPreferenceAdapter = null;
    Context mContext;
    private LayoutInflater mInflater;
    CharSequence[] entries;
    CharSequence[] entryValues;
    ArrayList<RadioButton> rButtonList;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    public CustomListPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        rButtonList = new ArrayList<RadioButton>();
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = prefs.edit();
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder)
    {
    	//builder.setPositiveButton(null, null);
    	//builder.setNegativeButton(null, null);
    	
        entries = getEntries();
        entryValues = getEntryValues();

        if (entries == null || entryValues == null || entries.length != entryValues.length )
        {
            throw new IllegalStateException(
                    "ListPreference requires an entries array and an entryValues array which are both the same length");
        }

        customListPreferenceAdapter = new CustomListPreferenceAdapter(mContext);

        builder.setAdapter(customListPreferenceAdapter, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });
    }

    private class CustomListPreferenceAdapter extends BaseAdapter
    {        
        public CustomListPreferenceAdapter(Context context)
        {

        }

        public int getCount()
        {
            return entries.length;
        }

        public Object getItem(int position)
        {
            return position;
        }

        public long getItemId(int position)
        {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent)
        {  
            View row = convertView;
            CustomHolder holder = null;

            if(row == null)
            {                                                                   
                row = mInflater.inflate(R.layout.custom_list_preference_row, parent, false);
                holder = new CustomHolder(row, position);
                row.setTag(holder);
            }

            return row;
        }

        class CustomHolder
        {
            private RadioButton rButton = null;
            private ImageView image = null;

            CustomHolder(View row, int position)
            {    
                rButton = (RadioButton)row.findViewById(R.id.custom_list_view_row_radio_button);
                rButton.setId(position);

                
                image = (ImageView)row.findViewById(R.id.custom_list_view_row_image_view);
                Bitmap bmp = null;
                
                switch (position){
                case 0:
                    bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.face10);
                	break;
                case 1:
                    bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.face20);
                	break;
                default:
                    bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.face10);
                	break;
                }
                
                image.setImageBitmap(bmp);
                
                rButtonList.add(rButton);
                rButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                    {
                        if(isChecked)
                        {
                            for(RadioButton rb : rButtonList)
                            {
                                if(rb != buttonView)
                                    rb.setChecked(false);
                            }
                            
                            int index = buttonView.getId();
                            int value = Integer.valueOf((String) entryValues[index]);
                            editor.putInt("FACE_NUMBER", value);
                            editor.apply();

                        }
                    }
                });
            }
        }
    }
}