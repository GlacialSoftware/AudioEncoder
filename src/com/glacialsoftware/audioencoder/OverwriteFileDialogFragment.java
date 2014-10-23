package com.glacialsoftware.audioencoder;

import java.io.File;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class OverwriteFileDialogFragment extends DialogFragment {
	
	public interface InvalidFileDialogCallbacks{
		public void doEncode();
		public void cancel();
	}
	
	public static OverwriteFileDialogFragment newInstance(String outPath){
		OverwriteFileDialogFragment overwriteFileDialogFragment = new OverwriteFileDialogFragment();
        Bundle args = new Bundle();
        args.putString("outPath", outPath);
        overwriteFileDialogFragment.setArguments(args);
        return overwriteFileDialogFragment;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	   	File out = new File(getArguments().getString("outPath"));
	   	String message="The file "+out.getName()+" already exists. Do you want to overwrite?";
	    AlertDialog.Builder builder = new Builder(getActivity());
	    builder.setIconAttribute(android.R.attr.alertDialogIcon);
	    builder.setTitle("File exists");
	    builder.setMessage(message);
	    
	    builder.setPositiveButton(R.string.overwrite_file_dialog_yes,
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                ((InvalidFileDialogCallbacks)getActivity()).doEncode();
            }
          }
	    );
    
	    builder.setNegativeButton(R.string.overwrite_file_dialog_no,
               new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                	((InvalidFileDialogCallbacks)getActivity()).cancel();
	                }
       });
   
	   return builder.create();
    }
	
}
