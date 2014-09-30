package com.glacialsoftware.audioencoder;

import java.util.List;

import android.os.Bundle;
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

public class VorbisOggOptionsFragment extends FormatOptionsFragment{
	
	private TextView vorbisOggModeFull;
	private TextView vorbisOggBitrateProgress;
	private SeekBar vorbisOggBitrateSeekBar;
	private Spinner vorbisOggSampleRateSpinner;
	private Spinner vorbisOggChannelsSpinner;
	
	private static int[] bitrates = {48,64,80,96,112,128,160,192,224,256,350,410,500};
	private static double[] vbrQualities = {0,1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,8.5,9.0,9.5,10.0};
	private static int[] sampleRates = {8000,11025,12000,16000,22050,24000,32000,44100,48000,64000,88200,96000,192000};
	private static int[] channels = {1,2};
	private static int[] monoOffsetsRight = {13,12,12,10,10,10,6,4,4,13,13,13,13};
	private static int[] stereoOffsetsRight = {10,9,9,6,6,6,2,0,0,13,13,13,13};
	
	public static Bundle state=null;
	
	private int bitratesOffset=0;
	private int currentChannel=-1;
	private int currentSampleRate=-1;
	private int sampleRateOffset=0;
	private ArrayAdapter<CharSequence> vorbisOggSampleRatesFull;
	private ArrayAdapter<CharSequence> vorbisOggSampleRatesAbrMono;
	private ArrayAdapter<CharSequence> vorbisOggSampleRatesAbrStereo;
	
	public enum BitrateMode{VBR,ABR}
	
	private BitrateMode bitrateMode;

	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState){
		return inflater.inflate(R.layout.fragment_vorbis_ogg_options, container,false);
    }
	
	@Override
	public void onStart (){
		super.onStart();
		Bundle state=getArguments();
		if (state==null){
			state=VorbisOggOptionsFragment.state;
		}
		
		vorbisOggModeFull = (TextView)getView().findViewById(R.id.vorbisOggModeFull);
		vorbisOggBitrateProgress = (TextView)getView().findViewById(R.id.vorbisOggBitrateProgress);
		
		bitrateMode=BitrateMode.VBR;
		
		vorbisOggBitrateSeekBar = (SeekBar) getView().findViewById(R.id.vorbisOggBitrateSeekBar);
		vorbisOggBitrateSeekBar.setMax(bitrates.length-1);
		vorbisOggBitrateSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
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
		
		vorbisOggBitrateSeekBar.setProgress(8);
		
		RadioGroup vorbisOggBitrateMode = (RadioGroup) getView().findViewById(R.id.vorbisOggBitrateMode);
		vorbisOggBitrateMode.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				int maxSampleRate=0;
				int position = (vorbisOggSampleRateSpinner.getSelectedItemPosition()-1);
				if (position!=-1){
					position+=sampleRateOffset;
				}
				switch (checkedId){
				case R.id.vorbisOggAbr:
					bitrateMode=BitrateMode.ABR;
					if (currentChannel==-1 || currentChannel==1){
						vorbisOggSampleRateSpinner.setAdapter(vorbisOggSampleRatesAbrStereo);
						sampleRateOffset=0;
					} else if (currentChannel==0){
						vorbisOggSampleRateSpinner.setAdapter(vorbisOggSampleRatesAbrMono);
						sampleRateOffset=1;
					}
					maxSampleRate=8;
					break;
				case R.id.vorbisOggVbr:
					bitrateMode=BitrateMode.VBR;
					vorbisOggSampleRateSpinner.setAdapter(vorbisOggSampleRatesFull);
					sampleRateOffset=0;
					maxSampleRate=12;
					break;
				}
				if (position==-1){
					position=0;
				} else if (position<sampleRateOffset){
					position=1;
				} else if (position>maxSampleRate){
					position=maxSampleRate;
				} else {
					position-=sampleRateOffset;
					++position;
				}
				vorbisOggSampleRateSpinner.setSelection(position);
				updateBitrateMode();
			}
		});

		vorbisOggSampleRateSpinner = (Spinner) getView().findViewById(R.id.vorbisOggSampleRateSpinner);
		
		vorbisOggSampleRatesFull = ArrayAdapter.createFromResource(getActivity(), 
				R.array.vorbis_ogg_sample_rates_full, android.R.layout.simple_spinner_item);
		vorbisOggSampleRatesFull.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		vorbisOggSampleRatesAbrMono = ArrayAdapter.createFromResource(getActivity(), 
				R.array.vorbis_ogg_sample_rates_abr_mono, android.R.layout.simple_spinner_item);
		vorbisOggSampleRatesAbrMono.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		vorbisOggSampleRatesAbrStereo = ArrayAdapter.createFromResource(getActivity(), 
				R.array.vorbis_ogg_sample_rates_abr_stereo, android.R.layout.simple_spinner_item);
		vorbisOggSampleRatesAbrStereo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		vorbisOggSampleRateSpinner.setAdapter(vorbisOggSampleRatesFull);
		
		vorbisOggSampleRateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

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
		
		vorbisOggSampleRateSpinner.setSelection(0);
		
		vorbisOggChannelsSpinner = (Spinner) getView().findViewById(R.id.vorbisOggChannelsSpinner);
		ArrayAdapter<CharSequence> channelsArrayAdapter = ArrayAdapter.createFromResource(getActivity(), 
				R.array.audio_channels, android.R.layout.simple_spinner_item);
		
		channelsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		vorbisOggChannelsSpinner.setAdapter(channelsArrayAdapter);
		
		vorbisOggChannelsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				currentChannel=position-1;
				
				int maxSampleRate=0;
				position = (vorbisOggSampleRateSpinner.getSelectedItemPosition()-1);
				if (position!=-1){
					position+=sampleRateOffset;
				}
				
				switch (bitrateMode){
				case ABR:
					if (currentChannel==-1 || currentChannel==1){
						vorbisOggSampleRateSpinner.setAdapter(vorbisOggSampleRatesAbrStereo);
						sampleRateOffset=0;
					} else if (currentChannel==0){
						vorbisOggSampleRateSpinner.setAdapter(vorbisOggSampleRatesAbrMono);
						sampleRateOffset=1;
					}
					maxSampleRate=8;
					break;
				case VBR:
					vorbisOggSampleRateSpinner.setAdapter(vorbisOggSampleRatesFull);
					sampleRateOffset=0;
					maxSampleRate=12;
					break;
				}
				
				if (position==-1){
					position=0;
				} else if (position<sampleRateOffset){
					position=1;
				} else if (position>maxSampleRate){
					position=maxSampleRate;
				} else {
					position-=sampleRateOffset;
					++position;
				}
				
				vorbisOggSampleRateSpinner.setSelection(position);
				updateBitrateMode();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				currentChannel=-1;
				updateBitrateMode();
			}
		});
		
		vorbisOggChannelsSpinner.setSelection(0);
		
		if (state!=null){
			bitrateMode=BitrateMode.values()[state.getInt("bitrateMode")];
			switch (bitrateMode){
			case VBR:
				vorbisOggBitrateMode.check(R.id.vorbisOggVbr);
				break;
			case ABR:
				vorbisOggBitrateMode.check(R.id.vorbisOggAbr);
				break;
			}
			
			vorbisOggChannelsSpinner.setSelection(state.getInt("channelsSpinnerPosition"));
			vorbisOggSampleRateSpinner.setSelection(state.getInt("sampleRateSpinnerPosition"));
			vorbisOggBitrateSeekBar.setProgress(state.getInt("bitrateProgress"));
		} else {
			vorbisOggBitrateMode.check(R.id.vorbisOggVbr);
		}
		
	}
	
	private void updateBitrateMode(){
		switch (bitrateMode){
		case VBR:
			vorbisOggModeFull.setText("Variable bitrate:");
			int position = vorbisOggBitrateSeekBar.getProgress();
			if (position>12){
				position=12;
			}
			vorbisOggBitrateSeekBar.setProgress(0);
			vorbisOggBitrateSeekBar.setMax(12);
			vorbisOggBitrateSeekBar.setProgress(position);
			break;
		case ABR:
			vorbisOggModeFull.setText("Average bitrate:");
			updateBitrateSeekBarAbr();
			break;
		}
	}
	
	private int getNewBitrateProgress(int newOffset, int newMax){
		int position = vorbisOggBitrateSeekBar.getProgress()+bitratesOffset;
		if (position<newOffset){
			return 0;
		}
		if (position>(newOffset+newMax)){
			return newMax;
		}
		return position-newOffset;
	}
	
	private void updateBitrateSeekBarAbr(){
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
		vorbisOggBitrateSeekBar.setProgress(0);
		vorbisOggBitrateSeekBar.setMax(newMax);
		vorbisOggBitrateSeekBar.setProgress(newPosition);
	}
	
	private void updateBitrateProgress(int progress){
		switch (bitrateMode){
		case VBR:
			updateVbrProgress(progress);
			break;
		case ABR:
			updateAbrProgress(progress);
			break;
		}
	}
	
	private void updateVbrProgress(int progress){
		vorbisOggBitrateProgress.setText("Q "+Double.toString(vbrQualities[progress]));
	}
	
	private void updateAbrProgress(int progress){
		vorbisOggBitrateProgress.setText(Integer.toString(bitrates[progress+bitratesOffset])+" kbps");
	}
	
	
	@Override
	public void getArgs(List<String> cmd) {
		cmd.add("-vn");
		cmd.add("-c:a");
		cmd.add("libvorbis");
		
		switch (bitrateMode){
		case VBR:
			cmd.add("-q:a");
			cmd.add(Double.toString(vbrQualities[vorbisOggBitrateSeekBar.getProgress()]));
			break;
		case ABR:
			cmd.add("-b:a");
			cmd.add(Integer.toString(bitrates[vorbisOggBitrateSeekBar.getProgress()+bitratesOffset])+"k");
			break;
		}
		
		int sampleRateSpinnerPosition=vorbisOggSampleRateSpinner.getSelectedItemPosition()-1;
		if (sampleRateSpinnerPosition!=-1){
			cmd.add("-ar:a");
			cmd.add(Integer.toString(sampleRates[sampleRateSpinnerPosition+sampleRateOffset]));
		}
		
		int channelsSpinnerPosition = vorbisOggChannelsSpinner.getSelectedItemPosition()-1;
		if (channelsSpinnerPosition!=-1){
			cmd.add("-ac:a");
			cmd.add(Integer.toString(channels[channelsSpinnerPosition]));
		}
	}

	@Override
	public void saveState(Bundle state) {
		state.putInt("bitrateMode",bitrateMode.ordinal());
		state.putInt("channelsSpinnerPosition", vorbisOggChannelsSpinner.getSelectedItemPosition());
		state.putInt("sampleRateSpinnerPosition", vorbisOggSampleRateSpinner.getSelectedItemPosition());
		state.putInt("bitrateProgress", vorbisOggBitrateSeekBar.getProgress());	
	}

	@Override
	public void setSelfRestore() {
		state = new Bundle();
		saveState(state);
	}
}
