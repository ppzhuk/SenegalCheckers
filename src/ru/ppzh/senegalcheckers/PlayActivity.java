package ru.ppzh.senegalcheckers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class PlayActivity extends Activity {
	public static final String PLAY_MODE_EXTRA = "ru.ppzh.senegalcheckers.play_mode";
	public static final String GAMES_AMOUNT_EXTRA = "ru.ppzh.senegalcheckers.games_amount";
	public static final String WHITE_AI_EXTRA = "ru.ppzh.senegalcheckers.white_ai";
	public static final String BLACK_AI_EXTRA = "ru.ppzh.senegalcheckers.black_ai";
	
	private PlayFieldView playField;
	private GameLogic gameLogic;
	
	private TextView player_one_color_text_view;
	private TextView player_two_color_text_view;
	private TextView player_one_who_text_view;
	private TextView player_two_who_text_view;
	private TextView whose_turn_view;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// remove title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_play);
		
		player_one_color_text_view = (TextView)this.findViewById(R.id.player1_color_textView);
		player_two_color_text_view = (TextView)this.findViewById(R.id.playerr2_color_textView);
		player_one_who_text_view = (TextView)this.findViewById(R.id.player1_who_textView);
		player_two_who_text_view = (TextView)this.findViewById(R.id.player2_who_textView);
		whose_turn_view = (TextView)this.findViewById(R.id.whose_turn);
		
		playField = (PlayFieldView)this.findViewById(R.id.play_field);
		int games_amount = getIntent().getIntExtra(GAMES_AMOUNT_EXTRA, -1);
		int whiteAI = getIntent().getIntExtra(WHITE_AI_EXTRA, -1);
		int blackAI = getIntent().getIntExtra(BLACK_AI_EXTRA, -1);
		int mode = getIntent().getIntExtra(PLAY_MODE_EXTRA, -1);
        
		if (mode < 4 && mode > -1) {
			if (mode == GameLogic.MODE_TWO_MACHINES) {
				gameLogic = GameLogic.newInstance(mode, whiteAI, blackAI);
			} else {
				gameLogic = GameLogic.newInstance(mode);
			}
			
			playField.setTwoMachinesInfo(games_amount, whiteAI, blackAI);
			playField.setGameLogic(gameLogic);		

			playField.setLabels(this.findViewById(R.id.player1_score_textView),
								this.findViewById(R.id.player2_score_textView),
								this.findViewById(R.id.whose_turn), 
								this.findViewById(R.id.player1_bonus_score_textView), 
								this.findViewById(R.id.player2_bonus_score_textView));
			
			// Define labels content in accordance with game mode.
			defineViews(mode);
			
			// Initiate first move.
			playField.Move();
			
		} else {
			Toast.makeText(getApplicationContext(), R.string.Error, Toast.LENGTH_SHORT).show();
			finish();
		}
	}
	
	private void defineViews(int mode){
		player_one_color_text_view.setText(R.string.white_color);
		player_two_color_text_view.setText(R.string.black_color);
		player_one_who_text_view.setText(R.string.man);
		player_two_who_text_view.setText(R.string.man);
		whose_turn_view.setText(R.string.player1);
		
		switch(mode){
		case 1:
			player_two_who_text_view.setText(R.string.machine);
			break;
		case 2:
			player_one_color_text_view.setText(R.string.black_color);
			player_two_color_text_view.setText(R.string.white_color);
			player_two_who_text_view.setText(R.string.machine);	
			whose_turn_view.setText(R.string.player2);
			break;
		case 3:
			player_one_who_text_view.setText(R.string.machine);
			player_two_who_text_view.setText(R.string.machine);
			break;
		}
	}

	public void startStatisticsActivity(Intent i) {
		startActivity(i);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		playField.setTerminated(true);
	}

	
	
	
}
