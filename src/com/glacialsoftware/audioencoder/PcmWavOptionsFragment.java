package com.glacialsoftware.audioencoder;

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class PcmWavOptionsFragment extends FormatOptionsFragment{
	
	private Spinner pcmWavBitDepthSpinner;
	private Spinner pcmWavSampleRateSpinner;
	private Spinner pcmWavChannelsSpinner;
	
	private static int[] sampleRates = {8000,11025,12000,16000,22050,24000,32000,44100,48000,64000,88200,96000,192000};
	private static int[] channels = {1,2};
	private static String[] bitDepths = {"pcm_u8","pcm_alaw","pcm_mulaw","pcm_s16le","pcm_s24le","pcm_s32le","pcm_f32le","pcm_f64le"};
	
	public static Bundle state=null;
	
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState){
		return inflater.inflate(R.layout.fragment_pcm_wav_options, container,false);
    }
	
	@Override
	public void onStart (){
		super.onStart();
		Bundle state=getArguments();
		if (state==null){
			state=PcmWavOptionsFragment.state;
		}

		pcmWavBitDepthSpinner = (Spinner) getView().findViewById(R.id.pcmWavBitDepthSpinner);
		ArrayAdapter<CharSequence> bitDepthArrayAdapter = ArrayAdapter.createFromResource(getActivity(), 
				R.array.wav_pcm_bit_depth, android.R.layout.simple_spinner_item);
		
		bitDepthArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		pcmWavBitDepthSpinner.setAdapter(bitDepthArrayAdapter);
		pcmWavBitDepthSpinner.setSelection(3);

		pcmWavSampleRateSpinner = (Spinner) getView().findViewById(R.id.pcmWavSampleRateSpinner);
		ArrayAdapter<CharSequence> sampleRateArrayAdapter = ArrayAdapter.createFromResource(getActivity(), 
				R.array.pcm_wav_sample_rates, android.R.layout.simple_spinner_item);
		
		sampleRateArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		pcmWavSampleRateSpinner.setAdapter(sampleRateArrayAdapter);
		
		pcmWavChannelsSpinner = (Spinner) getView().findViewById(R.id.pcmWavChannelsSpinner);
		ArrayAdapter<CharSequence> channelsArrayAdapter = ArrayAdapter.createFromResource(getActivity(), 
				R.array.audio_channels, android.R.layout.simple_spinner_item);
		
		channelsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		pcmWavChannelsSpinner.setAdapter(channelsArrayAdapter);
		
		if (state!=null){
			pcmWavSampleRateSpinner.setSelection(state.getInt("sampleRateSpinnerPosition"));
			pcmWavChannelsSpinner.setSelection(state.getInt("channelsSpinnerPosition"));
			pcmWavBitDepthSpinner.setSelection(state.getInt("bitDepthSpinnerPosition"));
		}
	}

	@Override
	public void getArgs(List<String> cmd) {
		cmd.add("-c:a");
		cmd.add(bitDepths[pcmWavBitDepthSpinner.getSelectedItemPosition()]);

		int sampleRateSpinnerPosition=pcmWavSampleRateSpinner.getSelectedItemPosition();
		if (sampleRateSpinnerPosition!=0){
			cmd.add("-ar:a");
			cmd.add(Integer.toString(sampleRates[sampleRateSpinnerPosition-1]));
		}
		
		int channelsSpinnerPosition = pcmWavChannelsSpinner.getSelectedItemPosition();
		if (channelsSpinnerPosition!=0){
			cmd.add("-ac:a");
			cmd.add(Integer.toString(channels[channelsSpinnerPosition-1]));
		}
	}

	@Override
	public void saveState(Bundle state) {
		state.putInt("channelsSpinnerPosition", pcmWavChannelsSpinner.getSelectedItemPosition());
		state.putInt("sampleRateSpinnerPosition", pcmWavSampleRateSpinner.getSelectedItemPosition());
		state.putInt("bitDepthSpinnerPosition", pcmWavBitDepthSpinner.getSelectedItemPosition());		
	}

	@Override
	public void setSelfRestore() {
		state = new Bundle();
		saveState(state);
	}
}
