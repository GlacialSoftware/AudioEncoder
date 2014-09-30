package com.glacialsoftware.audioencoder;

import java.util.List;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

public class AacOptionsFragment extends FormatOptionsFragment{
	
	private TextView aacModeFull;
	private TextView aacBitrateProgress;
	private TextView aacAlgorithmProgress;
	private SeekBar aacBitrateSeekBar;
	private SeekBar aacAlgorithmQualitySeekBar;
	private Spinner aacSampleRateSpinner;
	private Spinner aacChannelsSpinner;
	
	private static int[] bitrates = {32,64,80,96,112,128,160,192,224,256,288,320,384,425,529,576};
	private static double[] vbrQualities = {0.1,0.5,1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,10.0,13.0,16.0};
	private static int[] sampleRates = {8000,11025,12000,16000,22050,24000,32000,44100,48000,64000,88200,96000};
	private static int[] channels = {1,2};
	private static int[] monoOffsetsRight = {15,14,14,12,10,10,8,6,5,3,1,0};
	private static int[] stereoOffsetsRight = {12,10,10,8,6,5,3,1,0,0,0,0};
	private static String[] algorithmQualities = {"fast", "twoloop", "faac", "anmr"};
	
	public static Bundle state=null;
	
	private int bitratesOffset=0;
	private int currentChannel=-1;
	private int currentSampleRate=-1;
	private int sampleRateOffset=0;
	private ArrayAdapter<CharSequence> aacSampleRatesFull;
	private ArrayAdapter<CharSequence> aacSampleRatesVbrMono;
	private ArrayAdapter<CharSequence> aacSampleRatesVbrStereo;
	
	public enum BitrateMode{VBR,CBR}
	
	private BitrateMode bitrateMode;

	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState){
		return inflater.inflate(R.layout.fragment_aac_options, container,false);
    }
	
	@Override
	public void onStart (){
		super.onStart();
		Bundle state=getArguments();
		if (state==null){
			state=AacOptionsFragment.state;
		}
		
		aacModeFull = (TextView)getView().findViewById(R.id.aacModeFull);
		aacBitrateProgress = (TextView)getView().findViewById(R.id.aacBitrateProgress);
		aacAlgorithmProgress= (TextView)getView().findViewById(R.id.aacAlgorithmProgress);
		
		bitrateMode=BitrateMode.CBR;
		
		aacBitrateSeekBar = (SeekBar) getView().findViewById(R.id.aacBitrateSeekBar);
		aacBitrateSeekBar.setMax(bitrates.length-1);
		aacBitrateSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
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
		
		aacBitrateSeekBar.setProgress(11);
		
		RadioGroup aacBitrateMode = (RadioGroup) getView().findViewById(R.id.aacBitrateMode);
		aacBitrateMode.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				int position = (aacSampleRateSpinner.getSelectedItemPosition()-1);
				if (position!=-1){
					position+=sampleRateOffset;
				}
				switch (checkedId){
				case R.id.aacVbr:
					bitrateMode=BitrateMode.VBR;
					if (currentChannel==-1 || currentChannel==1){
						aacSampleRateSpinner.setAdapter(aacSampleRatesVbrStereo);
						sampleRateOffset=1;
					} else if (currentChannel==0){
						aacSampleRateSpinner.setAdapter(aacSampleRatesVbrMono);
						sampleRateOffset=4;
					}
					break;
				case R.id.aacCbr:
					bitrateMode=BitrateMode.CBR;
					aacSampleRateSpinner.setAdapter(aacSampleRatesFull);
					sampleRateOffset=0;
					break;
				}
				if (position==-1){
					position=0;
				} else if (position<sampleRateOffset){
					position=1;
				} else {
					position-=sampleRateOffset;
					++position;
				}
				aacSampleRateSpinner.setSelection(position);
				updateBitrateMode();
			}
		});
		
		aacAlgorithmQualitySeekBar = (SeekBar) getView().findViewById(R.id.aacAlgorithmQualitySeekBar);
		aacAlgorithmQualitySeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				aacAlgorithmProgress.setText(algorithmQualities[progress]);	
			}
		});
		
		if (state!=null){
			aacAlgorithmQualitySeekBar.setProgress(state.getInt("algorithmProgress"));
		} else {
			aacAlgorithmQualitySeekBar.setProgress(2);
		}

		aacSampleRateSpinner = (Spinner) getView().findViewById(R.id.aacSampleRateSpinner);
		
		aacSampleRatesFull = ArrayAdapter.createFromResource(getActivity(), 
				R.array.aac_sample_rates_full, android.R.layout.simple_spinner_item);
		aacSampleRatesFull.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		aacSampleRatesVbrMono = ArrayAdapter.createFromResource(getActivity(), 
				R.array.aac_sample_rates_vbr_mono, android.R.layout.simple_spinner_item);
		aacSampleRatesVbrMono.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		aacSampleRatesVbrStereo = ArrayAdapter.createFromResource(getActivity(), 
				R.array.aac_sample_rates_vbr_stereo, android.R.layout.simple_spinner_item);
		aacSampleRatesVbrStereo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		aacSampleRateSpinner.setAdapter(aacSampleRatesFull);
		
		aacSampleRateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

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
		
		aacSampleRateSpinner.setSelection(0);
		
		aacChannelsSpinner = (Spinner) getView().findViewById(R.id.aacChannelsSpinner);
		ArrayAdapter<CharSequence> channelsArrayAdapter = ArrayAdapter.createFromResource(getActivity(), 
				R.array.audio_channels, android.R.layout.simple_spinner_item);
		
		channelsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		aacChannelsSpinner.setAdapter(channelsArrayAdapter);
		
		aacChannelsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				currentChannel=position-1;
				
				if (bitrateMode==BitrateMode.VBR){
					position = (aacSampleRateSpinner.getSelectedItemPosition()-1);
					if (position!=-1){
						position+=sampleRateOffset;
					}
					if (currentChannel==-1 || currentChannel==1){
						aacSampleRateSpinner.setAdapter(aacSampleRatesVbrStereo);
						sampleRateOffset=1;
					} else if (currentChannel==0){
						aacSampleRateSpinner.setAdapter(aacSampleRatesVbrMono);
						sampleRateOffset=4;
					}
						
					if (position==-1){
						position=0;
					} else if (position<sampleRateOffset){
						position=1;
					} else {
						position-=sampleRateOffset;
						++position;
					}
					aacSampleRateSpinner.setSelection(position);
				}
				
				updateBitrateMode();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				currentChannel=-1;
				updateBitrateMode();
			}
		});
		
		aacChannelsSpinner.setSelection(0);
		
		if (state!=null){
			bitrateMode=BitrateMode.values()[state.getInt("bitrateMode")];
			switch (bitrateMode){
			case VBR:
				aacBitrateMode.check(R.id.aacVbr);
				break;
			case CBR:
				aacBitrateMode.check(R.id.aacCbr);
				break;
			}
			
			aacChannelsSpinner.setSelection(state.getInt("channelsSpinnerPosition"));
			aacSampleRateSpinner.setSelection(state.getInt("sampleRateSpinnerPosition"));
			aacBitrateSeekBar.setProgress(state.getInt("bitrateProgress"));
		} else {
			aacBitrateMode.check(R.id.aacCbr);
		}
		
	}
	
	private void updateBitrateMode(){
		switch (bitrateMode){
		case VBR:
			aacModeFull.setText("Variable bitrate:");
			int position = aacBitrateSeekBar.getProgress();
			if (position>13){
				position=13;
			}
			aacBitrateSeekBar.setProgress(0);
			aacBitrateSeekBar.setMax(13);
			aacBitrateSeekBar.setProgress(position);
			break;
		case CBR:
			aacModeFull.setText("Constant bitrate:");
			updateBitrateSeekBarCbr();
			break;
		}
	}
	
	private int getNewBitrateProgress(int newOffset, int newMax){
		int position = aacBitrateSeekBar.getProgress()+bitratesOffset;
		if (position<newOffset){
			return 0;
		}
		if (position>(newOffset+newMax)){
			return newMax;
		}
		return position-newOffset;
	}
	
	private void updateBitrateSeekBarCbr(){
		int newOffsetLeft=0;
		int newOffsetRight=0;
		if (currentSampleRate==-1){}
		else if (currentChannel==-1 || currentChannel==1){
			newOffsetRight=stereoOffsetsRight[currentSampleRate];
		} else if (currentChannel==0){
			newOffsetRight=monoOffsetsRight[currentSampleRate];
		}
		
		int newMax=((bitrates.length-1)-newOffsetLeft)-newOffsetRight;
		int newPosition=getNewBitrateProgress(newOffsetLeft, newMax);
		bitratesOffset=newOffsetLeft;
		aacBitrateSeekBar.setProgress(0);
		aacBitrateSeekBar.setMax(newMax);
		aacBitrateSeekBar.setProgress(newPosition);
	}
	
	private void updateBitrateProgress(int progress){
		switch (bitrateMode){
		case VBR:
			updateVbrProgress(progress);
			break;
		case CBR:
			updateCbrProgress(progress);
			break;
		}
	}
	
	private void updateVbrProgress(int progress){
		aacBitrateProgress.setText("Q "+Double.toString(vbrQualities[progress]));
	}
	
	private void updateCbrProgress(int progress){
		aacBitrateProgress.setText(Integer.toString(bitrates[progress+bitratesOffset])+" kbps");
	}
	
	
	@Override
	public void getArgs(List<String> cmd) {
		cmd.add("-strict");
		cmd.add("experimental");
		cmd.add("-c:a");
		cmd.add("aac");
		
		cmd.add("-aac_coder");
		cmd.add(algorithmQualities[aacAlgorithmQualitySeekBar.getProgress()]);
		
		switch (bitrateMode){
		case VBR:
			cmd.add("-q:a");
			cmd.add(Double.toString(vbrQualities[aacBitrateSeekBar.getProgress()]));
			break;
		case CBR:
			cmd.add("-b:a");
			cmd.add(Integer.toString(bitrates[aacBitrateSeekBar.getProgress()+bitratesOffset])+"k");
			break;
		}
		
		int sampleRateSpinnerPosition=aacSampleRateSpinner.getSelectedItemPosition()-1;
		if (sampleRateSpinnerPosition!=-1){
			cmd.add("-ar:a");
			cmd.add(Integer.toString(sampleRates[sampleRateSpinnerPosition+sampleRateOffset]));
		}
		
		int channelsSpinnerPosition = aacChannelsSpinner.getSelectedItemPosition()-1;
		if (channelsSpinnerPosition!=-1){
			cmd.add("-ac:a");
			cmd.add(Integer.toString(channels[channelsSpinnerPosition]));
		}
	}

	@Override
	public void saveState(Bundle state) {
		state.putInt("bitrateMode",bitrateMode.ordinal());
		state.putInt("channelsSpinnerPosition", aacChannelsSpinner.getSelectedItemPosition());
		state.putInt("sampleRateSpinnerPosition", aacSampleRateSpinner.getSelectedItemPosition());
		state.putInt("bitrateProgress", aacBitrateSeekBar.getProgress());
		state.putInt("algorithmProgress", aacAlgorithmQualitySeekBar.getProgress());	
	}

	@Override
	public void setSelfRestore() {
		state = new Bundle();
		saveState(state);
	}

}
