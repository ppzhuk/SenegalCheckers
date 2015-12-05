package ru.ppzh.senegalcheckers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;


public class ChooseColorActivity extends Activity {
	private Button mWhiteButton;
	private Button mBlackButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// remove title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_choose_color);
		
		//---Обработчик кнопки БЕЛЫЕ---
		mWhiteButton = (Button) findViewById(R.id.white_button);
		mWhiteButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(ChooseColorActivity.this, PlayActivity.class);
				i.putExtra(PlayActivity.PLAY_MODE_EXTRA, GameLogic.MODE_HUMAN_WHITE); 	// игрок решил играть белыми
				startActivity(i);
				finish();	
			}
		});
		
		//---Обработчик кнопки ЧЕРНЫЕ---
		mBlackButton = (Button) findViewById(R.id.black_button);
		mBlackButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(ChooseColorActivity .this, PlayActivity.class);
				i.putExtra(PlayActivity.PLAY_MODE_EXTRA, GameLogic.MODE_HUMAN_BLACK);	// игрок решил играть черными
				startActivity(i);
				finish();
			}
		});
	}
}
