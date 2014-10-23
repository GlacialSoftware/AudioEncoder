package com.glacialsoftware.audioencoder;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class EncodeErrorDialogFragment extends DialogFragment{
	
	   @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
		   	String message="There was an error with the current encode operation. If this error "
		   			+ "occured immediately after you pressed 'Encode' then there may be a conflict "
		   			+ "between the target bitrate and target channels/sample rate. If you have selected "
		   			+ "the 'Source' option for either channels or sample rate then try explicitly selecting "
		   			+ "values for both options instead.";
		    AlertDialog.Builder builder = new Builder(getActivity());
		    builder.setIconAttribute(android.R.attr.alertDialogIcon);
		    builder.setTitle("Error");
		    builder.setMessage(message);
		    builder.setNegativeButton(R.string.encode_error_dialog_dismiss,
                    new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int whichButton) {}
            });
        
		   return builder.create();
	    }
	   
}
