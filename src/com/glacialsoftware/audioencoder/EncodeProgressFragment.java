package com.glacialsoftware.audioencoder;

import android.app.Fragment;
import android.os.Bundle;
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
		
	}
	
	public void progressBegin(){
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
