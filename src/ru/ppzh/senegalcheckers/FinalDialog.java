package ru.ppzh.senegalcheckers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;

public class FinalDialog extends DialogFragment {
	private int final_speech;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		AlertDialog.Builder message = new AlertDialog.Builder(getActivity())
					.setTitle(R.string.EndGame)
					.setMessage(final_speech)
					.setPositiveButton(R.string.BackToMainMenu, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {

							getActivity().finish();
						}
					});
		return message.create();
	}

	public static FinalDialog newInstance() {
		FinalDialog fragment = new FinalDialog();
		return fragment;
	}
	
	public void setFinalSpeech(int final_speech){
		this.final_speech = final_speech;
	}
}
