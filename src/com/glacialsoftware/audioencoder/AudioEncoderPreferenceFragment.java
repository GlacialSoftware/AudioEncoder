package com.glacialsoftware.audioencoder;

import com.glacialsoftware.audioencoder.LicenseDialogFragment.Licenses;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AudioEncoderPreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener{
	
	public interface PreferenceCallbacks{
		public void updateOrientation(Boolean newValue);
		public void showLicenseFragment(Licenses license);
		public void doRecreateActivity();
		public void updateIntermediate(boolean deleteIntermediate);
	}
	
	private PreferenceCallbacks preferenceCallbacks;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        
        Preference ffmpegLicenseButton = (Preference)getPreferenceManager().findPreference("license_ffmpeg");      
        ffmpegLicenseButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        	
            @Override
            public boolean onPreferenceClick(Preference preference) {  
            	preferenceCallbacks.showLicenseFragment(Licenses.FFMPEG);
        		return true;
            }
        });     
        
        Preference lameLicenseButton = (Preference)getPreferenceManager().findPreference("license_lame");      
        lameLicenseButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        	
            @Override
            public boolean onPreferenceClick(Preference preference) {  
            	preferenceCallbacks.showLicenseFragment(Licenses.LAME);
        		return true;
            }
        });  
        
        Preference vorbisLicenseButton = (Preference)getPreferenceManager().findPreference("license_vorbis");      
        vorbisLicenseButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        	
            @Override
            public boolean onPreferenceClick(Preference preference) {  
            	preferenceCallbacks.showLicenseFragment(Licenses.VORBIS);
        		return true;
            }
        });  
        
        Preference oggLicenseButton = (Preference)getPreferenceManager().findPreference("license_ogg");      
        oggLicenseButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        	
            @Override
            public boolean onPreferenceClick(Preference preference) {  
            	preferenceCallbacks.showLicenseFragment(Licenses.OGG);
        		return true;
            }
        });  
        
        Preference fileChooserLicenseButton = (Preference)getPreferenceManager().findPreference("license_file_chooser");      
        fileChooserLicenseButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        	
            @Override
            public boolean onPreferenceClick(Preference preference) {  
            	preferenceCallbacks.showLicenseFragment(Licenses.FILE_CHOOSER);
        		return true;
            }
        }); 

    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        
        TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true);
        
        if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            int backgroundColor = typedValue.data;
            view.setBackgroundColor(backgroundColor);
            
        } else {
            Drawable drawable = getActivity().getResources().getDrawable(typedValue.resourceId);
            view.setBackgroundDrawable(drawable);
        }

        return view;
    }
    
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		preferenceCallbacks = (PreferenceCallbacks) activity;
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		preferenceCallbacks.updateOrientation(sharedPreferences.getBoolean("tilt_lock", false));
		preferenceCallbacks.updateIntermediate(sharedPreferences.getBoolean("delete_intermediate", true));
		
		String newTheme = sharedPreferences.getString("theme_select", "Light");
		if (!newTheme.equals(MainActivity.currentTheme)){
			preferenceCallbacks.doRecreateActivity();
		}
		
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

	}

	@Override
	public void onPause() {
	    getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	    super.onPause();
	}
}
