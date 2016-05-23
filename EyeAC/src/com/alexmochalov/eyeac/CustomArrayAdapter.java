package com.alexmochalov.eyeac;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class CustomArrayAdapter extends ArrayAdapter<String> {
	String[] facesList;
	Context mContext;

	public CustomArrayAdapter(Context context, int textViewResourceId,
			String[] objects) {
		super(context, textViewResourceId, objects);
		facesList = objects;
		mContext = context;
	}

	@Override
	public int getCount() {
		return super.getCount();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		v = inflater.inflate(R.layout.faces_item, null);
		
		ImageView imageView = (ImageView) v.findViewById(R.id.imageView);
		
		Resources resources = mContext.getResources();
		final int resourceId = resources.getIdentifier("face"+facesList[position]+"2", "drawable", 
				mContext.getPackageName());
		
		imageView.setImageResource(resourceId);
		
		return v;

	}
}
