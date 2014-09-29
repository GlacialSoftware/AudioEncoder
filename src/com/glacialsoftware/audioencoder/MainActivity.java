package com.glacialsoftware.audioencoder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ffmpeg.android.FfmpegController;

import com.ipaulpro.afilechooser.utils.FileUtils;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

public class MainActivity extends Activity implements FilePathFragment.FilePathCallbacks,
													  EncodeTask.EncodeTaskCallbacks{
	
	private static final int SINGLE_FILE_INPUT_CHOOSER = 1001;
	private static final int SINGLE_FILE_OUTPUT_CHOOSER = 1010;
	private static final int BATCH_INPUT_CHOOSER = 1101;
	private static final int BATCH_OUTPUT_CHOOSER = 1110;
	
	private static FfmpegController controller = null;
	private static EncodeTask encodeTask=null;
	private static boolean encoding=false;
	private static Bundle state=null;
	
	public static boolean shouldCancel=false;
	public static boolean imminentOptionsRestore=false;
	
	private FilePathFragment filePathFragment;
	private FormatOptionsFragment formatOptionsFragment;
	private EncodeProgressFragment encodeProgressFragment;
	
	public enum FileFormat{AAC,FLAC,LAME_MP3,VORBIS_OGG,PCM_WAVE,WAVPACK}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		if (controller==null){
			try {
				controller=new FfmpegController(this,getFilesDir());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (savedInstanceState!=null){
			state=savedInstanceState;
		}
		
		findViewById(R.id.frme_container_linear_layout).setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
				return false;
			}
		});
		
	}
	
	@Override
	protected void onStart (){
		super.onStart();
		
		
		getFragmentManager().executePendingTransactions();
		RelativeLayout relativeLayout=(RelativeLayout)findViewById(R.id.filePathRelativeLayout);
		
		if (relativeLayout==null){
			filePathFragment= FilePathFragment.newInstance(state);
			encodeProgressFragment=new EncodeProgressFragment();
			FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
			fragmentTransaction.add(R.id.file_frame, filePathFragment);
			fragmentTransaction.add(R.id.encode_progress_frame, encodeProgressFragment);
			fragmentTransaction.commit();
		}
		
		if (shouldCancel){
			cancel();
			shouldCancel=false;
		}
		if (encodeTask!=null){
			encodeProgressFragment.progressBegin();
			try{
				encodeTask.updateEncodeTaskCallbacks(this);
			} catch (Exception e){}
		}
	}
	
	@Override
	protected void onResume (){
		super.onResume();
		
		getFragmentManager().executePendingTransactions();
		RelativeLayout relativeLayout=(RelativeLayout)findViewById(R.id.filePathRelativeLayout);
		
		if (relativeLayout==null){
			filePathFragment= FilePathFragment.newInstance(state);
			encodeProgressFragment=new EncodeProgressFragment();
			FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
			fragmentTransaction.add(R.id.file_frame, filePathFragment);
			fragmentTransaction.add(R.id.encode_progress_frame, encodeProgressFragment);
			fragmentTransaction.commit();
		}
		
		if (shouldCancel){
			cancel();
			shouldCancel=false;
		}
		if (encodeTask!=null){
			encodeProgressFragment.progressBegin();
			try{
				encodeTask.updateEncodeTaskCallbacks(this);
			} catch (Exception e){}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	public void onSingleFileRadioClicked(View view){
		//TODO future: modify aFileChooser for multiple file selection
		// then make radio visible and add code to implement batch mode
	}
	
	public void onBatchRadioClicked(View view){
		//TODO future: modify aFileChooser for multiple file selection
		// then make radio visible and add code to implement batch mode
	}
	
	public void onInputBrowserClicked(View view){
	    Intent getContentIntent = FileUtils.createGetContentIntent();

	    state=new Bundle();
	    saveState(state);
	    
	    Intent intent = Intent.createChooser(getContentIntent, "Select a file");
	    startActivityForResult(intent, SINGLE_FILE_INPUT_CHOOSER);
	}
	
	public void onOutputBrowserClicked(View view){
		//TODO future: add 'filename' EditText to aFileChooser for new file in current directory
		// then make outputBrowser (ImageButton) visible and write this function
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
	    switch (requestCode) {
        case SINGLE_FILE_INPUT_CHOOSER:   
            if (resultCode == RESULT_OK) {
                final Uri uri = data.getData();

                String path = FileUtils.getPath(this, uri);
                state.putString("inputFilePath", path);
                state.putBoolean("updateOutputFilePath", false);
                //filePathFragment.setInputPath(path);
            }
            break;
        case SINGLE_FILE_OUTPUT_CHOOSER:
            if (resultCode == RESULT_OK) {
                final Uri uri = data.getData();

                String path = FileUtils.getPath(this, uri);
                state.putString("outputFilePath", path);
                //filePathFragment.setOutputPath(path);
            }
            break;
	    }
	}

	@Override
	public void setCurrentFormat(FileFormat format) {
		if (formatOptionsFragment!=null){
			formatOptionsFragment.setSelfRestore();
		}
		
		if (imminentOptionsRestore){
			formatOptionsFragment=FormatOptionsFragment.newInstance(format,state);
			state=null;
			imminentOptionsRestore=false;
		} else {
			formatOptionsFragment=FormatOptionsFragment.newInstance(format,null);
		}
		
		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.format_frame, formatOptionsFragment);
		fragmentTransaction.commit();
	}
	
	public void onEncodeClicked(View view){
		//TODO add '-y' option for overwrite after file exists dialog implemented
		
		if (!encoding){
			encoding=true;
	
			encodeProgressFragment.progressBegin();
		
			List<String> cmd = new ArrayList<String>();
			cmd.add(controller.getBinaryPath());
			
			//cmd.add("-encoders");
			
			
			
			cmd.add("-i");
			cmd.add(filePathFragment.getInputPath());
			
			formatOptionsFragment.getArgs(cmd);
			
			cmd.add(filePathFragment.getOutputPath());
			
	
			String total="";
			for (String s : cmd){
				total+=s+" ";
			}
			 
			Log.d("onEncodeClicked",total);
			
			encodeTask=new EncodeTask(cmd,controller,this);
			Thread thread = new Thread(encodeTask);
			thread.start();
		}
	}

	@Override
	public void updateProgressBar(int progress) {
		encodeProgressFragment.updateProgressBar(progress);
	}
	
	@Override
	public void cancel(){
		if (encodeTask !=null){
			encodeTask.cancel();
		}
		encodeTask=null;
		encodeProgressFragment.progressFinish();
		encoding=false;
	}
	
	public void onCancelClicked(View view){
		cancel();
	}
	
	private void saveState(Bundle state){
		filePathFragment.saveState(state);
		formatOptionsFragment.saveState(state);
		if (encodeTask!=null){
			try{
				encodeTask.removeEncodeTaskCallbacks();
			} catch (Exception e){}
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		saveState(savedInstanceState);
		
		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		fragmentTransaction.remove(filePathFragment);
		fragmentTransaction.remove(encodeProgressFragment);
		fragmentTransaction.remove(formatOptionsFragment);
		fragmentTransaction.commit();
		
		super.onSaveInstanceState(savedInstanceState);
	}
	
}
