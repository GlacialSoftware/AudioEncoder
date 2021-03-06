package com.glacialsoftware.audioencoder;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;

public class EncodeProgressFragment extends Fragment{
	
	private Button encodeButton=null;
	private ProgressBar progressBar=null;
	private ImageButton encodeCancel=null;
	private boolean shouldBegin=false;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState){
		return inflater.inflate(R.layout.fragment_encode_progress, container,false);
    }
	
	@Override
	public void onStart(){
		super.onStart();
		
		encodeButton=(Button) getView().findViewById(R.id.encodeButton);
		progressBar=(ProgressBar) getView().findViewById(R.id.encodeProgressBar);
		encodeCancel=(ImageButton)getView().findViewById(R.id.encodeCancel);
		
		if (shouldBegin){
			shouldBegin=false;
			progressBegin();
		}
	}
	
	public void progressBegin(){
		if (progressBar==null){
			shouldBegin=true;
			return;
		}
		progressBar.setProgress(0);
		encodeButton.setVisibility(View.INVISIBLE);
		progressBar.setVisibility(View.VISIBLE);
		encodeCancel.setVisibility(View.VISIBLE);
	}
	
	public void progressFinish(){
		encodeCancel.setVisibility(View.INVISIBLE);
		progressBar.setVisibility(View.INVISIBLE);
		encodeButton.setVisibility(View.VISIBLE);
	}
	
	public void updateProgressBar(int progress) {
		if (progressBar!=null){
			try{
				progressBar.setProgress(progress);
			} catch (Exception e){}
		}
	}
	
}
