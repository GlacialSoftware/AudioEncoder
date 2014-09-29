package com.glacialsoftware.audioencoder;

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

public class FlacOptionsFragment extends FormatOptionsFragment {
	
	private TextView flacCompressionProgress;
	private SeekBar flacCompressionLevelSeekBar;
	private Spinner flacSampleRateSpinner;
	private Spinner flacChannelsSpinner;
	
	private static int[] sampleRates = {32000,44100,48000,88200,96000,192000};
	private static int[] channels = {1,2};
	
	public static Bundle state=null;
	
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState){
		return inflater.inflate(R.layout.fragment_flac_options, container,false);
    }
	
	@Override
	public void onStart (){
		super.onStart();
		Bundle state=getArguments();
		if (state==null){
			state=FlacOptionsFragment.state;
		}

		flacCompressionProgress = (TextView)getView().findViewById(R.id.flacCompressionProgress);
		
		flacCompressionLevelSeekBar = (SeekBar) getView().findViewById(R.id.flacCompressionLevelSeekBar);
		flacCompressionLevelSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				flacCompressionProgress.setText(Integer.toString(progress));
			}
		});
		
		if (state!=null){
			flacCompressionLevelSeekBar.setProgress(state.getInt("compressionLevelProgress"));
		} else {
			flacCompressionLevelSeekBar.setProgress(8);
		}

		flacSampleRateSpinner = (Spinner) getView().findViewById(R.id.flacSampleRateSpinner);
		ArrayAdapter<CharSequence> sampleRateArrayAdapter = ArrayAdapter.createFromResource(getActivity(), 
				R.array.flac_sample_rates, android.R.layout.simple_spinner_item);
		
		sampleRateArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		flacSampleRateSpinner.setAdapter(sampleRateArrayAdapter);
		
		flacChannelsSpinner = (Spinner) getView().findViewById(R.id.flacChannelsSpinner);
		ArrayAdapter<CharSequence> channelsArrayAdapter = ArrayAdapter.createFromResource(getActivity(), 
				R.array.audio_channels, android.R.layout.simple_spinner_item);
		
		channelsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		flacChannelsSpinner.setAdapter(channelsArrayAdapter);
		
		if (state!=null){
			flacSampleRateSpinner.setSelection(state.getInt("sampleRateSpinnerPosition"));
			flacChannelsSpinner.setSelection(state.getInt("channelsSpinnerPosition"));
		}
	}

	@Override
	public void getArgs(List<String> cmd) {
		cmd.add("-c:a");
		cmd.add("flac");
		
		cmd.add("-compression_level");
		cmd.add(Integer.toString(flacCompressionLevelSeekBar.getProgress()));
		
		int sampleRateSpinnerPosition=flacSampleRateSpinner.getSelectedItemPosition();
		if (sampleRateSpinnerPosition!=0){
			cmd.add("-ar:a");
			cmd.add(Integer.toString(sampleRates[sampleRateSpinnerPosition-1]));
		}
		
		int channelsSpinnerPosition = flacChannelsSpinner.getSelectedItemPosition();
		if (channelsSpinnerPosition!=0){
			cmd.add("-ac:a");
			cmd.add(Integer.toString(channels[channelsSpinnerPosition-1]));
		}
	}

	@Override
	public void saveState(Bundle state) {
		state.putInt("channelsSpinnerPosition", flacChannelsSpinner.getSelectedItemPosition());
		state.putInt("sampleRateSpinnerPosition", flacSampleRateSpinner.getSelectedItemPosition());
		state.putInt("compressionLevelProgress", flacCompressionLevelSeekBar.getProgress());
	}

	@Override
	public void setSelfRestore() {
		state = new Bundle();
		saveState(state);
	}

}
