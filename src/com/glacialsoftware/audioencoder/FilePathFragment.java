package com.glacialsoftware.audioencoder;

import java.util.Locale;

import com.glacialsoftware.audioencoder.MainActivity.FileFormat;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class FilePathFragment extends Fragment {
	
	private static String[] extensions = {".aac",".flac",".mp3",".ogg",".wav",".wv"};
	private static boolean[] isLossless = {false,true,false,false,true,true};
	private static FileFormat[] formats = {FileFormat.AAC,FileFormat.FLAC,FileFormat.LAME_MP3,
		FileFormat.PCM_WAVE,FileFormat.WAVPACK};
	
	private String currentExtension;
	
	private EditText inputFilePath;
	private EditText outputFilePath;
	private TextView loss;
	private Spinner formatSpinner;
	
	public interface FilePathCallbacks{
		public void setCurrentFormat(FileFormat format);
	}
	
	private FilePathCallbacks filePathCallbacks;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		filePathCallbacks = (FilePathCallbacks) activity;
	}

	
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState){
		return inflater.inflate(R.layout.fragment_file_path, container,false);
    }
	
	@Override
	public void onStart (){
		super.onStart();
		
		inputFilePath=(EditText) getView().findViewById(R.id.inputFilePath);
		outputFilePath=(EditText) getView().findViewById(R.id.outputFilePath);
		loss=(TextView)getView().findViewById(R.id.loss);
		
		/*
		RadioGroup radioGroup = (RadioGroup) getView().findViewById(R.id.processingModeRadioGroup);
		radioGroup.check(R.id.singleFileRadio);
		*/
		
		formatSpinner = (Spinner) getView().findViewById(R.id.formatSpinner);
		ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getActivity(), 
				R.array.formats_array, android.R.layout.simple_spinner_item);
		
		arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		formatSpinner.setAdapter(arrayAdapter);
		formatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				currentExtension=extensions[position];
				setOutputPath(outputFilePath.getText().toString());
				
				if (isLossless[position]){
					loss.setText("lossless");
				} else {
					loss.setText("lossy");
				}
				
				filePathCallbacks.setCurrentFormat(formats[position]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				currentExtension=".wav";
				setOutputPath(outputFilePath.getText().toString());
				
				filePathCallbacks.setCurrentFormat(FileFormat.PCM_WAVE);
			}
		});
		
		restoreState(getArguments());
	}
	
	private void updateInputPath(String path){
		inputFilePath.setText("");
		inputFilePath.append(path);
	}
	
	private void updateOutputPath(String path){
		outputFilePath.setText("");
		outputFilePath.append(path);
	}
	
	public void setInputPath(String path){
		if (path==null){
			path="";
		}
		updateInputPath(path);

		setOutputPath(path);
	}
	
	public void setOutputPath(String path){
		if (path==null){
			path="";
		}
		if (!path.equals("")){
			path=changeExtensionToCurrent(path);
		} 
		
		updateOutputPath(path);
	}
	
	private String changeExtensionToCurrent(String path){
		return stripValidExtension(path)+currentExtension;
	}
	
	private String stripValidExtension(String path){
		String pathLower = path.toLowerCase(Locale.US);
		for (String ext : extensions){
			if (pathLower.endsWith(ext)){
				return path.substring(0,path.length()-ext.length());
			}
		}
		return path;
	}
	
	public String getInputPath(){
		return inputFilePath.getText().toString();
	}
	
	public String getOutputPath(){
		return outputFilePath.getText().toString();
	}
	
	public void saveState(Bundle state){
		state.putInt("formatSpinnerPosition", formatSpinner.getSelectedItemPosition());
		state.putString("inputFilePath", getInputPath());
		state.putBoolean("updateOutputFilePath", true);
		state.putString("outputFilePath", getOutputPath());
		state.putString("currentExtension", currentExtension);
	}
	
	public void restoreState(Bundle state){
		if (state!=null){
			currentExtension=state.getString("currentExtension");
			setInputPath(state.getString("inputFilePath"));
			if (state.getBoolean("updateOutputFilePath")){
				setOutputPath(state.getString("outputFilePath"));
			}
			MainActivity.imminentOptionsRestore=true;
			formatSpinner.setSelection(state.getInt("formatSpinnerPosition"));
		}
	}
	
	public static FilePathFragment newInstance(Bundle state){
		FilePathFragment filePathFragment = new FilePathFragment();
		filePathFragment.setArguments(state);
		return filePathFragment;
	}
	
}
