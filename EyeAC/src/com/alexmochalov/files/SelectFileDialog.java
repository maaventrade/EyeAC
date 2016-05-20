package com.alexmochalov.files;
 
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.alexmochalov.eyeac.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup; 
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 *    
 * @author Alexey Mochalov (maaventrade@gmail.com)
 * 
 * Activity for selection file from storage
 *
 */
public class SelectFileDialog extends Dialog {
	private Context context;
    private String addInfo;
    
    private String initPath; 
    private String fileExt[];
    private String message;

    private boolean editName;
    
    private Dialog dialog;
    private TextView textViewCurrentDir;
    
    boolean mEMail;
    String mFileName;
	
    public MyCallback callback = null;
    
    
    
	public interface MyCallback {
		void callbackACTION_SELECTED(String fileName); 
	} 
    
	public SelectFileDialog(Context context, String initPath, String fileName, String fileExt[], String message, 
			boolean editName, boolean eMail, String addInfo) {
		
			super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
			this.context = context;
			this.initPath = initPath;
			this.fileExt = fileExt;
			this.message = message;
			this.editName = editName;
			this.addInfo = addInfo;
			
			mEMail = eMail;
			mFileName = fileName;
			
			dialog = this;
		}

	//private void setCallBack(interface MyCallback1){
	//	
	//}
	
	
	ListView list_files; // 

	ArrayList<String> last_files;
	
	ArrayList<String> listItems=new ArrayList<String>(); 
	ArrayList<File> listFiles=new ArrayList<File>(); 
	MyArrayAdapter<String> adapter;
	File dir;
	
	class FileNameComparator implements Comparator<File> {   
		public int compare(File fileA, File fileB) {
			if ((fileA.isDirectory())&&(!fileB.isDirectory()))
				return -1;
			else if ((!fileA.isDirectory())&&(fileB.isDirectory()))
				return 1;
			else
				return fileA.getName().compareToIgnoreCase(fileB.getName());
			}
	}
	
	private class MyArrayAdapter<String> extends ArrayAdapter{      
		MyArrayAdapter() {      
			super(context, R.layout.raw,R.id.weekofday,listItems);  
		}    
		public View getView(int position, View convertView, ViewGroup parent) {      
			View row = super.getView(position, convertView, parent);      
			ImageView icon = (ImageView) row.findViewById(R.id.icon);
			if (listItems.get(position).equals(".."))
				icon.setImageResource(R.drawable.folder);
			else if (listFiles.get(position).isDirectory())
				icon.setImageResource(R.drawable.folder);
			else
				icon.setImageResource(R.drawable.voidf);
			return (row);   
			} 	
		}	
	
	void readDir(String path){
		listItems.clear();
		listFiles.clear();
        dir = new File(path); 
        File[] files = dir.listFiles();
        if (files != null )
            for (int i=0; i<files.length; i++){
            	boolean addFile = false;
            	if (files[i].isDirectory()) addFile = true;
            	else if (fileExt.length == 0) addFile = true;
            	else for (int j=0; j<fileExt.length; j++)
            		if (files[i].getName().endsWith(fileExt[j])){
            			addFile = true;
            			break;
            		}
            	if (addFile) listFiles.add(files[i]);
        	}	
        FileNameComparator fnc = new FileNameComparator();
        Collections.sort(listFiles, fnc);

        for (int i=0; i<listFiles.size(); i++)
    		listItems.add(listFiles.get(i).getName());
		
		listFiles.add(0,null);  
		listItems.add(0,"..");  

		
        this.setTitle(dir.toString());
        
        adapter = new MyArrayAdapter<String>(); 
        list_files.setAdapter(adapter);
        textViewCurrentDir.setText(dir.toString());
        
	}
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selectfile);
    
        list_files = (ListView)findViewById(R.id.FileList);
        textViewCurrentDir = (TextView)findViewById(R.id.textViewCurrentDir);
        textViewCurrentDir.setText(initPath);
        
        if (!editName){
        	LinearLayout linearLayoutNew = (LinearLayout)findViewById(R.id.LinearLayoutNew);
        	linearLayoutNew.setVisibility(View.INVISIBLE);
        } else {
        	EditText editTextName = (EditText)findViewById(R.id.editTextName);
        	editTextName.setText(mFileName);
        }
        
		File file = new File(initPath);                                    
		if (file.exists()) readDir(initPath);
		else readDir("/");
        
        list_files.setOnItemClickListener(new OnItemClickListener() { 
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
            	String selectedFile = listItems.get(position);
            	String currentdDir = dir.toString();
            	
            	if (selectedFile.equals("..")){
                	int slashposition= currentdDir.lastIndexOf("/");
                	if (slashposition == 0)
                		readDir("/");
                	else if (slashposition > 0)
                        readDir(currentdDir.substring(0,slashposition));
            	}
            	else if (listFiles.get(position).isDirectory()){
                    readDir(listFiles.get(position).getAbsolutePath());
            	}
            	else{
            		callback.callbackACTION_SELECTED(listFiles.get(position).getAbsolutePath());
                    dialog.dismiss();
            	}
            	
			} 
        }); 

        Button btnSave = (Button)findViewById(R.id.buttonSave);
        btnSave.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
            	EditText editTextName = (EditText)findViewById(R.id.editTextName); 
        		callback.callbackACTION_SELECTED(dir.toString()+"/"+editTextName.getText().toString());
                dialog.dismiss();
			}
        });

        Button btnMail = (Button)findViewById(R.id.buttonMail);
        if (mEMail){
            btnMail.setOnClickListener(new Button.OnClickListener(){
    			@Override
    			public void onClick(View v) {
            		callback.callbackACTION_SELECTED("send picture by email");
                    dialog.dismiss();
    			}
            });
        } else {
        	btnMail.setVisibility(View.INVISIBLE);
        }
        
        /*
        if (message != null
        && message.length() > 0){
        	AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder
            .setTitle(message)
            .setPositiveButton("Ok", new DialogInterface.OnClickListener() 
            {
                public void onClick(DialogInterface dialog, int which) 
                {       
                    dialog.dismiss();           
            }
            });             
        AlertDialog alert = builder.create();
                alert.show();        
        }
        */
	}

    public String getEditText(){
    	EditText editTextName = (EditText)findViewById(R.id.editTextName); 
    	return dir.toString()+"/"+editTextName.getText().toString();
    }
    
    
}
