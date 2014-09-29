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

public class WavpackOptionsFragment extends FormatOptionsFragment {
	
	private TextView wavpackCompressionProgress;
	private SeekBar wavpackCompressionLevelSeekBar;
	private Spinner wavpackSampleRateSpinner;
	private Spinner wavpackChannelsSpinner;
	
	private static int[] sampleRates = {32000,44100,48000,88200,96000,192000};
	private static int[] channels = {1,2};
	private static String[] compressionLevels={"fast","normal","high","very high"};
	
	public static Bundle state=null;
	
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState){
		return inflater.inflate(R.layout.fragment_wavpack_options, container,false);
    }
	
	@Override
	public void onStart (){
		super.onStart();
		Bundle state=getArguments();
		if (state==null){
			state=WavpackOptionsFragment.state;
		}

		wavpackCompressionProgress = (TextView)getView().findViewById(R.id.wavpackCompressionProgress);
		
		wavpackCompressionLevelSeekBar = (SeekBar) getView().findViewById(R.id.wavpackCompressionLevelSeekBar);
		wavpackCompressionLevelSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (progress<4){
					wavpackCompressionProgress.setText(compressionLevels[progress]);
				} else {
					wavpackCompressionProgress.setText("extra processing "
							+Integer.toString(progress-2));
				}
			}
		});
		
		if (state!=null){
			wavpackCompressionLevelSeekBar.setProgress(state.getInt("compressionLevelProgress"));
		} else {
			wavpackCompressionLevelSeekBar.setProgress(2);
		}

		wavpackSampleRateSpinner = (Spinner) getView().findViewById(R.id.wavpackSampleRateSpinner);
		ArrayAdapter<CharSequence> sampleRateArrayAdapter = ArrayAdapter.createFromResource(getActivity(), 
				R.array.flac_sample_rates, android.R.layout.simple_spinner_item);
		
		sampleRateArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		wavpackSampleRateSpinner.setAdapter(sampleRateArrayAdapter);
		
		wavpackChannelsSpinner = (Spinner) getView().findViewById(R.id.wavpackChannelsSpinner);
		ArrayAdapter<CharSequence> channelsArrayAdapter = ArrayAdapter.createFromResource(getActivity(), 
				R.array.audio_channels, android.R.layout.simple_spinner_item);
		
		channelsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		wavpackChannelsSpinner.setAdapter(channelsArrayAdapter);
		
		if (state!=null){
			wavpackSampleRateSpinner.setSelection(state.getInt("sampleRateSpinnerPosition"));
			wavpackChannelsSpinner.setSelection(state.getInt("channelsSpinnerPosition"));
		}
	}

	@Override
	public void getArgs(List<String> cmd) {
		cmd.add("-c:a");
		cmd.add("wavpack");
		
		cmd.add("-compression_level");
		cmd.add(Integer.toString(wavpackCompressionLevelSeekBar.getProgress()));
		
		int sampleRateSpinnerPosition=wavpackSampleRateSpinner.getSelectedItemPosition();
		if (sampleRateSpinnerPosition!=0){
			cmd.add("-ar:a");
			cmd.add(Integer.toString(sampleRates[sampleRateSpinnerPosition-1]));
		}
		
		int channelsSpinnerPosition = wavpackChannelsSpinner.getSelectedItemPosition();
		if (channelsSpinnerPosition!=0){
			cmd.add("-ac:a");
			cmd.add(Integer.toString(channels[channelsSpinnerPosition-1]));
		}
	}

	@Override
	public void saveState(Bundle state) {
		state.putInt("channelsSpinnerPosition", wavpackChannelsSpinner.getSelectedItemPosition());
		state.putInt("sampleRateSpinnerPosition", wavpackSampleRateSpinner.getSelectedItemPosition());
		state.putInt("compressionLevelProgress", wavpackCompressionLevelSeekBar.getProgress());
	}

	@Override
	public void setSelfRestore() {
		state = new Bundle();
		saveState(state);
	}
}
