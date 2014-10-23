package com.glacialsoftware.audioencoder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.glacialsoftware.audioencoder.EncodeService.CommCodes;
import com.glacialsoftware.audioencoder.LicenseDialogFragment.Licenses;
import com.ipaulpro.afilechooser.utils.FileUtils;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

public class MainActivity extends Activity implements FilePathFragment.FilePathCallbacks,
													  AudioEncoderPreferenceFragment.PreferenceCallbacks,
													  OverwriteFileDialogFragment.InvalidFileDialogCallbacks{
	
	private static final int SINGLE_FILE_INPUT_CHOOSER = 1001;
	private static final int SINGLE_FILE_OUTPUT_CHOOSER = 1010;
	//private static final int BATCH_INPUT_CHOOSER = 1101;
	//private static final int BATCH_OUTPUT_CHOOSER = 1110;
	
	//private static FfmpegController controller = null;
	private static Bundle state=null;
	
	private Messenger service=null;
	private Messenger messenger = new Messenger(new IncomingHandler());
	
	public static boolean shouldCancel=false;
	public static boolean imminentOptionsRestore=false;
	public static String currentTheme="Dark";
	
	private FilePathFragment filePathFragment;
	private FormatOptionsFragment formatOptionsFragment;
	private EncodeProgressFragment encodeProgressFragment;
	private AudioEncoderPreferenceFragment audioEncoderPreferenceFragment;
	private boolean bound=false;
	//private EncodeService encodeService=null;
	private static boolean encoding=false;
	private String tempInFile=null;
	
	public enum FileFormat{AAC,FLAC,LAME_MP3,VORBIS_OGG,PCM_WAVE,WAVPACK}
	
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	CommCodes code = CommCodes.values()[msg.what];

        	switch (code){
        	case CANCEL:
        		cancel();
        		break;
        	case ENCODING:
        		boolean encodingLocal = msg.getData().getBoolean("encoding");
        		if (!encodingLocal && encoding){
        			cancel();
        		} else if (encodingLocal && !encoding){
        			getFragmentManager().executePendingTransactions();
        			encodeProgressFragment.progressBegin();
        		}
        		updateIntermediate(PreferenceManager.getDefaultSharedPreferences(
        				MainActivity.this).getBoolean("delete_intermediate", true));
        		break;
        	case PROGRESS_UPDATE:
        		try{
        			encodeProgressFragment.updateProgressBar(msg.arg1);
        		} catch (Exception e){}
        		break;
        	case ERROR:
				EncodeErrorDialogFragment encodeErrorDialogFragment = new EncodeErrorDialogFragment();
				encodeErrorDialogFragment.show(getFragmentManager(), "encodeErrorDialogFragment");
        		break;
        	default:
        		super.handleMessage(msg);
        	}
        }
    }
    
    
	
    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
        	MainActivity.this.service=new Messenger(service);
        	bound=true;
        	Message message = Message.obtain(null, CommCodes.ATTACH.ordinal());
        	message.replyTo=messenger;
        	try{
        		MainActivity.this.service.send(message);
        	} catch (Exception e){}
        	
        	
        	/*
            LocalBinder binder = (LocalBinder) service;
            MainActivity.this.encodeService = (EncodeService) binder.getService();
            bound = true;
            encoding=encodeService.getEncodingLocal();
            Log.d("onServiceConnected_encoding",Boolean.toString(encoding));
    		if (encoding){
	    		try{
	    			getFragmentManager().executePendingTransactions();
	    			encodeProgressFragment.progressBegin();
	    			try{
	    				encodeService.updateEncodeServiceCallbacks(getCurrentContext());
	    			} catch (Exception e){}
	    			encodeService.setController(controller);
    			} catch (Exception e){}
    		}
    		*/
        }

        public void onServiceDisconnected(ComponentName className) {
        	service=null;
            bound = false;
        }
    };
 

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		String theme=PreferenceManager.getDefaultSharedPreferences(this).getString("theme_select", "Dark");
		if (theme.equals("Light")){
			setTheme(R.style.lightTheme);
			currentTheme=theme;
		} else {
			setTheme(R.style.darkTheme);
			currentTheme=theme;
		}
		
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
	    Intent intent = getIntent();
	    if (Intent.ACTION_VIEW.equals(intent.getAction())){
	    	Uri data = intent.getData();
	    	tempInFile=data.getPath();
	    }
	    
		/*
		if (controller==null){
			controller=FfmpegController.newInstance(this);
		}
		*/
		
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
			audioEncoderPreferenceFragment=new AudioEncoderPreferenceFragment();
			filePathFragment= FilePathFragment.newInstance(state);
			encodeProgressFragment=new EncodeProgressFragment();
			FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
			fragmentTransaction.add(R.id.file_frame, filePathFragment);
			fragmentTransaction.add(R.id.encode_progress_frame, encodeProgressFragment);
			fragmentTransaction.commit();
		}
		
		Intent intent = new Intent(this,EncodeService.class);
		bindService(intent, serviceConnection, BIND_AUTO_CREATE);
		
		if (shouldCancel){
			getFragmentManager().executePendingTransactions();
			cancel();
			shouldCancel=false;
		}

		if (encoding){
			getFragmentManager().executePendingTransactions();
			encodeProgressFragment.progressBegin();
		}
	}
	
	@Override
	protected void onResume (){
		super.onResume();
		
		if (tempInFile!=null){
			filePathFragment.setInputPath(tempInFile);
			tempInFile=null;
		}
		
		getFragmentManager().executePendingTransactions();
		RelativeLayout relativeLayout=(RelativeLayout)findViewById(R.id.filePathRelativeLayout);
		
		if (relativeLayout==null){
			audioEncoderPreferenceFragment=new AudioEncoderPreferenceFragment();
			filePathFragment= FilePathFragment.newInstance(state);
			encodeProgressFragment=new EncodeProgressFragment();
			FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
			fragmentTransaction.add(R.id.file_frame, filePathFragment);
			fragmentTransaction.add(R.id.encode_progress_frame, encodeProgressFragment);
			fragmentTransaction.commit();
		}
		
		if (shouldCancel){
			getFragmentManager().executePendingTransactions();
			cancel();
			shouldCancel=false;
		}
		if (encoding){
			getFragmentManager().executePendingTransactions();
			encodeProgressFragment.progressBegin();
		}
	}
	
    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            unbindService(serviceConnection);
            bound = false;
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
			if (!audioEncoderPreferenceFragment.isVisible()){
				getFragmentManager().executePendingTransactions();
				if (formatOptionsFragment==null){
					return true;
				}
				FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
				fragmentTransaction.add(R.id.main_activity_container, audioEncoderPreferenceFragment);
				fragmentTransaction.addToBackStack(null);
				fragmentTransaction.commit();
			}
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

	    saveState(new Bundle());
	    
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
		} else {
			formatOptionsFragment=FormatOptionsFragment.newInstance(format,null);
		}
		
		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.format_frame, formatOptionsFragment);	
		fragmentTransaction.commit();
		
		if (imminentOptionsRestore){
			boolean preferencesShowing=state.getBoolean("preferencesShowing");
			
			imminentOptionsRestore=false;
			state=null;
			
			if (preferencesShowing){
			    FragmentTransaction transaction0 = getFragmentManager().beginTransaction();
			    transaction0.add(R.id.main_activity_container,audioEncoderPreferenceFragment);
			    transaction0.addToBackStack(null);
			    transaction0.commit();
			}
		} 
	}
	
	@Override
	public void doEncode(){
		List<String> cmd = new ArrayList<String>();
		//cmd.add(controller.getBinaryPath());
		
		cmd.add("-y");
		
		cmd.add("-i");
		cmd.add(filePathFragment.getInputPath());
		
		formatOptionsFragment.getArgs(cmd);
		
		cmd.add(filePathFragment.getOutputPath());

		String total="";
		for (int i=0;i<cmd.size();++i){
			total+=cmd.get(i);
			if (i!=cmd.size()-1){
				total+=" ";
			}
		}
		 
		Log.d("doEncode",total);
		
		/*
		Bundle bundle = new Bundle();
		bundle.putStringArrayList("cmd", (ArrayList<String>)cmd);
		Message message = Message.obtain(null,,bundle);
		*/
		
		
		Intent intent = new Intent(this,EncodeService.class);
		//encodeService.setController(controller);
		
		intent.putExtra("cmd", (ArrayList<String>)cmd);
		
		startService(intent);
	}
	
	public void onEncodeClicked(View view){
		
		if (!encoding){
			encoding = true;
			encodeProgressFragment.progressBegin();
			
			String outPath = filePathFragment.getOutputPath();
			
			if (new File(outPath).exists()){
				OverwriteFileDialogFragment overwriteFileDialogFragment = OverwriteFileDialogFragment.newInstance(outPath);
				overwriteFileDialogFragment.show(getFragmentManager(), "overwriteFileDialogFragment");
			} else {
				doEncode();
			}	
		}
	}

	@Override
	public void updateIntermediate(boolean deleteIntermediate){
		if (bound){
			Message message = Message.obtain(null,CommCodes.UPDATE_INTERMEDIATE.ordinal());
    		Bundle bundle = new Bundle();
    		bundle.putBoolean("deleteIntermediate", deleteIntermediate);
    		message.setData(bundle);
			if (service!=null){
				try{
					service.send(message);
				} catch (Exception e){}
			}
		}
	}
	
	@Override
	public void cancel(){
		Log.d("MainActivity","Cancelled");
		
		/*
		if (encoding){
			encodeService.cancel();
		}
		*/
		
		if (bound){
			Message message = Message.obtain(null,CommCodes.CANCEL.ordinal());
			if (service!=null){
				try{
					service.send(message);
				} catch (Exception e){}
			}
		}
		
		encodeProgressFragment.progressFinish();
		encoding=false;
	}
	
	public void onCancelClicked(View view){
		cancel();
	}
	
	private void saveState(Bundle state){
		filePathFragment.saveState(state);
		formatOptionsFragment.saveState(state);
		
		/*
		if (encoding){
			try{
				encodeService.removeEncodeServiceCallbacks();
			} catch (Exception e){}
		}
		*/
		
		boolean preferencesShowing=audioEncoderPreferenceFragment.isVisible();
		
		if (preferencesShowing){
			getFragmentManager().popBackStackImmediate();
		}
		
		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		fragmentTransaction.remove(filePathFragment);
		fragmentTransaction.remove(encodeProgressFragment);
		fragmentTransaction.remove(formatOptionsFragment);
		fragmentTransaction.commit();
		
		state.putBoolean("preferencesShowing", preferencesShowing);
		MainActivity.state=state;
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		saveState(savedInstanceState);
		super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	public void showLicenseFragment(Licenses license) {
		LicenseDialogFragment licenseDialogFragment = LicenseDialogFragment.newInstance(license);
    	licenseDialogFragment.show(getFragmentManager(),"licenseDialog");
	}

	@Override
	public void updateOrientation(Boolean newValue) {
		if (newValue){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}	
	}

	@Override
	public void doRecreateActivity() {
		saveState(new Bundle());
		
		Intent intent = getIntent();
		
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();
		overridePendingTransition(0, 0);
		
		startActivity(intent);		
		overridePendingTransition(0, 0);
	}

	
}
