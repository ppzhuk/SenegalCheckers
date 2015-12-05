package ru.ppzh.senegalcheckers;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

// Данная активити показывает стистику проведенных игр в случае режима GameLogic.MODE_TWO_MACHINES
public class MachinesStatisticsActivity extends Activity {
	public static final String GAMES_AMOUNT_EXTRA = "ru.ppzh.senegalcheckers.games_in_total";
	public static final String WHITE_AI_EXTRA = "ru.ppzh.senegalcheckers.white_ai_index";
	public static final String BLACK_AI_EXTRA = "ru.ppzh.senegalcheckers.black_ai_index";
	public static final String BLACK_WINS_EXTRA = "ru.ppzh.senegalcheckers.black_wins";
	public static final String WHITE_WINS_EXTRA = "ru.ppzh.senegalcheckers.white_wins";
	public static final String DRAWS_EXTRA = "ru.ppzh.senegalcheckers.draws";
	public static final String AVERAGE_MOVES_EXTRA = "ru.ppzh.senegalcheckers.average_moves_per_game";
	
	
	private TextView games_in_total;
	private TextView wAI_title;
	private TextView bAI_title;
	private TextView w_winnings;
	private TextView b_winnings;
	private TextView draws;
	private TextView average_moves;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// remove title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_2_machines_statistics);
		
		int games_amount = getIntent().getIntExtra(GAMES_AMOUNT_EXTRA, -1);
		int whiteAI_index = getIntent().getIntExtra(WHITE_AI_EXTRA, -1);
		int blackAI_index = getIntent().getIntExtra(BLACK_AI_EXTRA, -1);
		
		games_in_total = (TextView)this.findViewById(R.id.games_in_total);
		games_in_total.setText(""+games_amount);
		
		wAI_title = (TextView)this.findViewById(R.id.white_wins_title);
		wAI_title.setText(wAI_title.getText()+" - "+getAITitle(whiteAI_index));
		
		bAI_title = (TextView)this.findViewById(R.id.black_wins_title);
		bAI_title.setText(bAI_title.getText()+" - "+getAITitle(blackAI_index));	
		
		w_winnings = (TextView)this.findViewById(R.id.white_wins);
		w_winnings.setText(""+getIntent().getIntExtra(WHITE_WINS_EXTRA, -1));
		
		b_winnings = (TextView)this.findViewById(R.id.black_wins);
		b_winnings.setText(""+getIntent().getIntExtra(BLACK_WINS_EXTRA, -1));
		
		draws = (TextView)this.findViewById(R.id.draws);
		draws.setText(""+getIntent().getIntExtra(DRAWS_EXTRA, -1));
		
		average_moves = (TextView)this.findViewById(R.id.average_moves_per_game);
		average_moves.setText(""+getIntent().getFloatExtra(AVERAGE_MOVES_EXTRA, -1));
	
	}
	
	public String getAITitle(int i) {
		String[] array = getResources().getStringArray(R.array.AIs);
		return (i == -1) ? "error" : array[i];
	}
	
}
