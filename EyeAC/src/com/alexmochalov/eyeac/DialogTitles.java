package com.alexmochalov.eyeac;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.alexmochalov.eyeac.*;
import java.util.*;

public class DialogTitles extends Dialog
{
	private Context mContext;
	private Dialog dialog;
	
	Titles mTitles;
	
	MyCallback callback = null;
	interface MyCallback {
		void callbackACTION_DOWN(int bgID); 
	} 

	public DialogTitles(Context context, Titles titles) {
		super(context);
		mContext = context;
		dialog = this;
		mTitles = titles;
		
		setTitle(context.getResources().getString(R.string.action_list));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.dialog_titles);

        AdapterTitles adapter; 
        adapter = new AdapterTitles(mContext, mTitles);

		ListView listView = (ListView) findViewById(R.id.dialogtitlesListView);
        listView.setAdapter(adapter);
		listView.setOnItemClickListener( new ListView.OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> p1, View p2, int index, long p4)
				{
					//callback.callbackACTION_DOWN(canvas.get(index));
					dialog.dismiss();
				}}
		);
	}	
}

class AdapterTitles  extends BaseAdapter {
	Context mContext;
	LayoutInflater lInflater;
	Titles objects;

	AdapterTitles(Context context, Titles obj) {
		mContext = context;
	    objects = obj;
		
	    lInflater = (LayoutInflater)context
	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	@Override
	public int getCount() {
		return objects.size();
	}

	@Override
	public Object getItem(int position) {
		return objects.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
	    if (view == null) {
			view = lInflater.inflate(R.layout.title_item, parent, false);
	    }

		TextView text = (TextView) view.findViewById(R.id.titleitemTextViewText);
		text.setText(objects.getString(position));
		
	    return view;	
	}

}

