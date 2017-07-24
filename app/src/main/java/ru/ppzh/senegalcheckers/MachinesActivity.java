package ru.ppzh.senegalcheckers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class MachinesActivity extends Activity {

	private EditText games_amount_EditText;
	private Spinner whiteAISpinner;
	private Spinner blackAISpinner;
	private Button startButton;
	
	private int games_amount;
	private int whiteAI_ID;
	private int blackAI_ID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// remove title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_2_machines);
		
		games_amount = 1;
		whiteAI_ID = 0;
		blackAI_ID = 0;
		
		games_amount_EditText = (EditText) this.findViewById(R.id.games_amount_editText);
		games_amount_EditText.addTextChangedListener(new TextWatcher(){

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}

			@Override
			public void afterTextChanged(Editable s) {
			
				String ss = new String(s.toString()); 
				if (ss.equals("")) {
					games_amount = 1;	
				} else {
					games_amount = Integer.valueOf(ss);	
				}
				
				if (games_amount < 1) {
					games_amount = 1;
				}
				if (games_amount > 100) {
					games_amount = 100;
				}
			}
			
		});
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.AIs, R.layout.dropdown_item);
		whiteAISpinner = (Spinner) findViewById(R.id.white_AI_spinner);
		whiteAISpinner.setAdapter(adapter);
		whiteAISpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				
				whiteAI_ID = parent.getSelectedItemPosition();
				if (whiteAI_ID == AdapterView.INVALID_POSITION) whiteAI_ID = 0;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
			
		});
		
		blackAISpinner = (Spinner) findViewById(R.id.black_AI_spinner);
		blackAISpinner.setAdapter(adapter);
		blackAISpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				
				blackAI_ID = parent.getSelectedItemPosition();
				if (blackAI_ID == AdapterView.INVALID_POSITION) blackAI_ID = 0;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
			
		});
		
		startButton = (Button) this.findViewById(R.id.start_button);
		startButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MachinesActivity.this, PlayActivity.class);
				i.putExtra(PlayActivity.PLAY_MODE_EXTRA, GameLogic.MODE_TWO_MACHINES);
				i.putExtra(PlayActivity.GAMES_AMOUNT_EXTRA, games_amount);
				i.putExtra(PlayActivity.WHITE_AI_EXTRA, whiteAI_ID);
				i.putExtra(PlayActivity.BLACK_AI_EXTRA, blackAI_ID);
				startActivity(i);
				finish();
			}
		});
	}

}
