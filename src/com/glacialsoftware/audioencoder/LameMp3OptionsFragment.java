package com.glacialsoftware.audioencoder;

import java.util.List;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

public class LameMp3OptionsFragment extends FormatOptionsFragment{
	
	private TextView lameMp3ModeFull;
	private TextView lameMp3BitrateProgress;
	private TextView lameMp3AlgorithmProgress;
	private SeekBar lameMp3BitrateSeekBar;
	private SeekBar lameMp3AlgorithmQualitySeekBar;
	private Spinner lameMp3SampleRateSpinner;
	private Spinner lameMp3ChannelsSpinner;
	
	public enum BitrateMode{VBR,CBR,ABR}
	
	private BitrateMode bitrateMode;
	
	private static int[] bitrates = {8,16,24,32,40,48,56,64,80,96,112,128,160,192,224,256,320};
	private static int[] sampleRates = {8000,11025,12000,16000,22050,24000,32000,44100,48000};
	private static int[] channels = {1,2};
	private static int[] offsetsRight = {9,9,9,4,4,4,0,0,0};
	private static int[] offsetsLeft  = {0,0,0,0,0,0,3,3,3};
	
	public static Bundle state=null;
	
	private int bitratesOffset=0;
	private int currentSampleRate=-1;
	
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState){
		return inflater.inflate(R.layout.fragment_lame_mp3_options, container,false);
    }
	
	@Override
	public void onStart (){
		super.onStart();
		Bundle state=getArguments();
		if (state==null){
			state=LameMp3OptionsFragment.state;
		}
		
		lameMp3ModeFull = (TextView)getView().findViewById(R.id.lameMp3ModeFull);
		lameMp3BitrateProgress = (TextView)getView().findViewById(R.id.lameMp3BitrateProgress);
		lameMp3AlgorithmProgress= (TextView)getView().findViewById(R.id.lameMp3AlgorithmProgress);
		
		lameMp3BitrateSeekBar = (SeekBar) getView().findViewById(R.id.lameMp3BitrateSeekBar);
		lameMp3BitrateSeekBar.setMax(16);
		lameMp3BitrateSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				updateBitrateProgress(progress);
			}
		});
		
		
		RadioGroup lameMp3BitrateMode = (RadioGroup) getView().findViewById(R.id.lameMp3BitrateMode);
		lameMp3BitrateMode.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId){
				case R.id.lameMp3Vbr:
					bitrateMode=BitrateMode.VBR;
					break;
				case R.id.lameMp3Cbr:
					bitrateMode=BitrateMode.CBR;
					break;
				case R.id.lameMp3Abr:
					bitrateMode=BitrateMode.ABR;
					break;
				}
				updateBitrateMode();
			}
		});
		
		lameMp3AlgorithmQualitySeekBar = (SeekBar) getView().findViewById(R.id.lameMp3AlgorithmQualitySeekBar);
		lameMp3AlgorithmQualitySeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				lameMp3AlgorithmProgress.setText(Integer.toString(9-progress));	
			}
		});
		
		if (state!=null){
			lameMp3AlgorithmQualitySeekBar.setProgress(state.getInt("algorithmProgress"));
		} else {
			lameMp3AlgorithmQualitySeekBar.setProgress(6);
		}

		lameMp3SampleRateSpinner = (Spinner) getView().findViewById(R.id.lameMp3SampleRateSpinner);
		ArrayAdapter<CharSequence> sampleRateArrayAdapter = ArrayAdapter.createFromResource(getActivity(), 
				R.array.lame_mp3_sample_rates, android.R.layout.simple_spinner_item);
		
		sampleRateArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		lameMp3SampleRateSpinner.setAdapter(sampleRateArrayAdapter);
		
		lameMp3SampleRateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				currentSampleRate=position-1;
				updateBitrateMode();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				currentSampleRate=-1;
				updateBitrateMode();
			}
		});
		
		lameMp3ChannelsSpinner = (Spinner) getView().findViewById(R.id.lameMp3ChannelsSpinner);
		ArrayAdapter<CharSequence> channelsArrayAdapter = ArrayAdapter.createFromResource(getActivity(), 
				R.array.audio_channels, android.R.layout.simple_spinner_item);
		
		channelsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		lameMp3ChannelsSpinner.setAdapter(channelsArrayAdapter);
		
		bitrateMode=BitrateMode.CBR;
		lameMp3BitrateSeekBar.setProgress(16);
		
		if (state!=null){
			bitrateMode=BitrateMode.values()[state.getInt("bitrateMode")];
			switch (bitrateMode){
			case VBR:
				lameMp3BitrateMode.check(R.id.lameMp3Vbr);
				break;
			case CBR:
				lameMp3BitrateMode.check(R.id.lameMp3Cbr);
				break;
			case ABR:
				lameMp3BitrateMode.check(R.id.lameMp3Abr);
				break;
			}
			lameMp3ChannelsSpinner.setSelection(state.getInt("channelsSpinnerPosition"));
			lameMp3SampleRateSpinner.setSelection(state.getInt("sampleRateSpinnerPosition"));
			lameMp3BitrateSeekBar.setProgress(state.getInt("bitrateProgress"));
		} else {
			lameMp3BitrateMode.check(R.id.lameMp3Cbr);
		}
	}
	
	private void updateBitrateMode(){
		switch (bitrateMode){
		case VBR:
			lameMp3ModeFull.setText("Variable bitrate:");
			int position = lameMp3BitrateSeekBar.getProgress();
			if (position>9){
				position=9;
			}
			
			lameMp3BitrateSeekBar.setProgress(0);
			lameMp3BitrateSeekBar.setMax(9);
			lameMp3BitrateSeekBar.setProgress(position);
			break;
		case CBR:
			lameMp3ModeFull.setText("Constant bitrate:");
			updateBitrateSeekBarCbrAbr();
			break;
		case ABR:
			lameMp3ModeFull.setText("Average bitrate:");
			updateBitrateSeekBarCbrAbr();
			break;
		}
	}
	
	private int getNewBitrateProgress(int newOffset, int newMax){
		int position = lameMp3BitrateSeekBar.getProgress()+bitratesOffset;
		if (position<newOffset){
			return 0;
		}
		if (position>(newOffset+newMax)){
			return newMax;
		}
		return position-newOffset;
	}

	private void updateBitrateSeekBarCbrAbr(){
		int newOffsetLeft=0;
		int newOffsetRight=0;
		if (currentSampleRate!=-1){
			newOffsetLeft=offsetsLeft[currentSampleRate];
			newOffsetRight=offsetsRight[currentSampleRate];
		}
		
		int newMax=((bitrates.length-1)-newOffsetLeft)-newOffsetRight;
		int newPosition=getNewBitrateProgress(newOffsetLeft, newMax);
		bitratesOffset=newOffsetLeft;
		lameMp3BitrateSeekBar.setProgress(0);
		lameMp3BitrateSeekBar.setMax(newMax);
		lameMp3BitrateSeekBar.setProgress(newPosition);
	}
	
	private void updateBitrateProgress(int progress){
		switch (bitrateMode){
		case VBR:
			updateVbrProgress(progress);
			break;
		case CBR:
		case ABR:
			updateCbrAbrProgress(progress);
			break;
		}
	}
	
	private void updateVbrProgress(int progress){
		lameMp3BitrateProgress.setText("V"+Integer.toString(9-progress));
	}
	
	private void updateCbrAbrProgress(int progress){
		lameMp3BitrateProgress.setText(Integer.toString(bitrates[progress+bitratesOffset])+" kbps");
	}

	@Override
	public void getArgs(List<String> cmd) {
		cmd.add("-c:a");
		cmd.add("libmp3lame");
		
		switch (bitrateMode){
		case VBR:
			cmd.add("-q:a");
			cmd.add(Integer.toString(9-lameMp3BitrateSeekBar.getProgress()));
			break;
		case CBR:
			cmd.add("-b:a");
			cmd.add(Integer.toString(bitrates[lameMp3BitrateSeekBar.getProgress()+bitratesOffset])+"k");
			break;
		case ABR:
			cmd.add("-b:a");
			cmd.add(Integer.toString(bitrates[lameMp3BitrateSeekBar.getProgress()+bitratesOffset])+"k");
			cmd.add("-abr");
			cmd.add("1");
			break;
		}
		
		cmd.add("-compression_level");
		cmd.add(Integer.toString(9-lameMp3AlgorithmQualitySeekBar.getProgress()));
		
		int sampleRateSpinnerPosition=lameMp3SampleRateSpinner.getSelectedItemPosition();
		if (sampleRateSpinnerPosition!=0){
			cmd.add("-ar:a");
			cmd.add(Integer.toString(sampleRates[sampleRateSpinnerPosition-1]));
		}
		
		int channelsSpinnerPosition = lameMp3ChannelsSpinner.getSelectedItemPosition();
		if (channelsSpinnerPosition!=0){
			cmd.add("-ac:a");
			cmd.add(Integer.toString(channels[channelsSpinnerPosition-1]));
		}
	}

	@Override
	public void saveState(Bundle state) {
		state.putInt("bitrateMode",bitrateMode.ordinal());
		state.putInt("channelsSpinnerPosition", lameMp3ChannelsSpinner.getSelectedItemPosition());
		state.putInt("sampleRateSpinnerPosition", lameMp3SampleRateSpinner.getSelectedItemPosition());
		state.putInt("bitrateProgress", lameMp3BitrateSeekBar.getProgress());
		state.putInt("algorithmProgress", lameMp3AlgorithmQualitySeekBar.getProgress());	
	}

	@Override
	public void setSelfRestore() {
		state = new Bundle();
		saveState(state);
	}

}
