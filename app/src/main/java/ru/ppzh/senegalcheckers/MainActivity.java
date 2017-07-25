package ru.ppzh.senegalcheckers;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
	private TextView mCodeVersionView;

	private Button mOnePlayerButton;
	private Button mTwoPlayersButton;
	private Button mRulesButton;
	private Button mTwoMachinesButton;
	private TextView titleView;
	
	private int count;
	private long t;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// remove title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		
		// To reveal mTwoMachinesButton you need to make 10 taps in 10 seconds over the label.
		t = count = 0;
		titleView = (TextView)findViewById(R.id.title_view);
		titleView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (count > -1) {
					if (count == 0) t = System.nanoTime()/1000000;	
					if (++count >= 10) {
						Toast.makeText(getApplicationContext(), R.string.SecretMode, Toast.LENGTH_SHORT).show();
						mTwoMachinesButton.setVisibility(View.VISIBLE);
						count = -1;
					}
					if ((System.nanoTime()/1000000 - t) >= 10000) count = 0;
				}				
			}
		});
		
		mOnePlayerButton = (Button) findViewById(R.id.one_player_button);
		mOnePlayerButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, ChooseColorActivity.class);
				startActivity(i);
			}
		});
		
		mTwoPlayersButton = (Button) findViewById(R.id.two_players_button);
		mTwoPlayersButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, PlayActivity.class);
				i.putExtra(PlayActivity.PLAY_MODE_EXTRA, GameLogic.MODE_TWO_HUMANS); 
				startActivity(i);
			}
		});	
		
		mRulesButton = (Button) findViewById(R.id.rules_button);
		mRulesButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, RulesActivity.class);
				startActivity(i);
			}
		});
		
		mTwoMachinesButton = (Button) findViewById(R.id.two_machines_button);
		mTwoMachinesButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, MachinesActivity.class);
				startActivity(i);
			}
		});	
		
		String mVersion;		 
		try {
			mVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;	
		} catch(NameNotFoundException e) {
			mVersion = "not found";
		}
		
		mCodeVersionView = (TextView) findViewById(R.id.code_version);
		mCodeVersionView.setText(("ver: " + mVersion));
		
	}
}
