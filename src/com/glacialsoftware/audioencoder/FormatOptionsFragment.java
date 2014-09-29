package com.glacialsoftware.audioencoder;

import java.util.List;

import com.glacialsoftware.audioencoder.MainActivity.FileFormat;

import android.app.Fragment;
import android.os.Bundle;

public abstract class FormatOptionsFragment extends Fragment{

	public static FormatOptionsFragment newInstance(FileFormat format,Bundle state){
		FormatOptionsFragment formatOptionsFragment=null;
		
		switch (format){
		case AAC:
			formatOptionsFragment = new AacOptionsFragment();
			break;
		case FLAC:
			formatOptionsFragment = new FlacOptionsFragment();
			break;
		case LAME_MP3:
			formatOptionsFragment = new LameMp3OptionsFragment();
			break;
		case VORBIS_OGG:
			break;
		case PCM_WAVE:
			formatOptionsFragment = new PcmWavOptionsFragment();
			break;
		case WAVPACK:
			formatOptionsFragment = new WavpackOptionsFragment();
		}
		
		formatOptionsFragment.setArguments(state);
		return formatOptionsFragment;
	}
	
	public abstract void getArgs(List<String> cmd);
	
	public abstract void saveState(Bundle state);
	
	public abstract void setSelfRestore();
	
}
