package ru.ppzh.senegalcheckers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class PlayFieldView extends View {
	//��� ��� ������ � ��� ���������� � ���������(�� ����) �����.
	public static final String MOVES = "moves";

	private final float LINE_WIDTH = 5;
	private final int MACHINE_TURN_TIME = 650;
	
	private Paint paint;				// ����� ��� ��������� �����
	private Paint paintWhiteCheckers;	// ����� ��� ��������� ����� �����
	private Paint paintBlackCheckers;	// ~ ������ �����
	
	private float cellWidth;
	private float cellHeight;
	private float checkerRadius;
	
	private GameLogic gameLogic;
	private GameState gameState;  // GameState, ������� ������ gameLogic
	private Player currPlayer;    // �����, ������� �����
	
	// ����� ���������� ��� ��������� ������� �������� ����
	private boolean chosen = false;	// ������� �� �����, ������� ����� ������ ���
	private int chosenI;			// � ����������
	private int chosenJ;
	private boolean endMove = true;	// ���� ����� ����� >1 ����� ����� ������, ��� �������� � ��������� �������
	private Moves move;			// ��������� ��������� ����� � �� ����
	private boolean draw_moves = false;	//����, ���������� �� ��������� ����� ��� ��������� �����
	
	private TextView player_one_points;
	private TextView player_two_points;
	private TextView player_one_bonus_points;
	private TextView player_two_bonus_points;
	private TextView curr_move_View;
	
	private Handler handler = new Handler();
	
	private boolean touchable;
	private boolean terminated = false; // ���� ����������-���� ����������
	
	
	// ���� ��� AI x AI ������
	private int games_amount = -1;
	private int games_played = 0;
	private int whiteAI = -1;
	private int blackAI = -1;
	private int white_wins = 0;
	private int black_wins = 0;
	private int draws = 0;
	private int[] moves_per_game;
	//------------------------
	
	
	private Bitmap white_checker_used_img;
	private Bitmap white_checker_unused_img;
	private Bitmap black_checker_used_img;
	private Bitmap black_checker_unused_img;
	
	
	public PlayFieldView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.play_grid_color));
		paint.setStrokeWidth(LINE_WIDTH);
		
        paintWhiteCheckers = new Paint();
        paintWhiteCheckers.setColor(getResources().getColor(R.color.white_checkers_color));
        paintWhiteCheckers.setStrokeWidth(LINE_WIDTH-1);
        
        paintBlackCheckers = new Paint();
        paintBlackCheckers.setColor(getResources().getColor(R.color.dark_checkers_color));
        paintBlackCheckers.setStrokeWidth(LINE_WIDTH-1);
        

        white_checker_used_img = BitmapFactory.decodeResource(context.getResources(), R.drawable.white_checker_used);
        white_checker_unused_img = BitmapFactory.decodeResource(context.getResources(), R.drawable.white_checker_unused);
        black_checker_used_img = BitmapFactory.decodeResource(context.getResources(), R.drawable.black_checker_used);
        black_checker_unused_img = BitmapFactory.decodeResource(context.getResources(), R.drawable.black_checker_unused);
        
    }

	
	@Override
	protected void onDraw(Canvas canvas) {
		getCellDimentions(canvas);	// ���������� ������� ����� �������� ����
		if (draw_moves) {			// ���� ����������, ������������ ����������� ���� ��� ��������� �����
			drawMoves(canvas);		// ���� �������� ����� drawLines, ���������� ��������
		}
		canvas.drawLines(getPlayGridCoord(canvas), paint);	// ������ ����� ����
		drawCheckers(canvas);								// ������ �����
		if (draw_moves) {			// ���� ����������, ������� ��������� �����
			drawChosenChecker(canvas);
		}
		super.onDraw(canvas);
	}

	private float[] getPlayGridCoord(Canvas canvas) {
		int h = canvas.getHeight();
		int w = canvas.getWidth();
		
							// coordinates for vertical lines
		float[] gridCoords = {LINE_WIDTH/2, 0, 	   LINE_WIDTH/2, h, 
							  1*cellWidth, 0,      1*cellWidth, h,
							  2*cellWidth, 0,      2*cellWidth, h,
							  3*cellWidth, 0,      3*cellWidth, h,
							  4*cellWidth, 0,      4*cellWidth, h,
							  5*cellWidth, 0,      5*cellWidth, h,
							  w-(LINE_WIDTH/2), 0, w-(LINE_WIDTH/2), h,
							// coordinate for horizontal lines
							  0, LINE_WIDTH/2, 	   w, LINE_WIDTH/2,
							  0, 1*cellHeight,     w, 1*cellHeight,
							  0, 2*cellHeight, 	   w, 2*cellHeight,
							  0, 3*cellHeight,     w, 3*cellHeight,
							  0, 4*cellHeight,     w, 4*cellHeight,
							  0, h-(LINE_WIDTH/2), w, h-(LINE_WIDTH/2)};
		return gridCoords;
	}

	// ����������� �������� ������ ���� �� ������ �������� ������
	private void getCellDimentions(Canvas canvas) {
		cellWidth = (float)(canvas.getWidth()/GameLogic.HORIZONTAL_CELL_AMOUNT);
		cellHeight = (float)(canvas.getHeight()/GameLogic.VERTICAL_CELL_AMOUNT);
		
		float min = (cellWidth < cellHeight) ? cellWidth : cellHeight;
		checkerRadius = (float)(min/2)-5;
		
	}

	// ��������� ���� �����
	private void drawCheckers(Canvas c) {
		int[][] field;

		try {
			field = gameLogic.getGameState().getField();	

	
			for (int i = 0; i < field.length; i++) {
				for (int j = 0; j < field[i].length; j++ ) {
					if (field[i][j] == 1) {
						

						/* ��������� ��������� ����������� ������ �������� �����
					    Rect r = new Rect();
						r.set((int)(i*cellWidth+(float)(cellWidth/2)-checkerRadius), (int)(j*cellHeight+(float)(cellHeight/2)-checkerRadius),
							  (int)(i*cellWidth+(float)(cellWidth/2)+checkerRadius), (int)(j*cellHeight+(float)(cellHeight/2)+checkerRadius));
						c.drawBitmap(white_checker_unused_img, null, r, new Paint());						
						*/
						drawCircle(i, j, c, paintWhiteCheckers);
					}
					if (field[i][j] == 2) {
						
						/* ��������� ��������� ����������� ������ �������� �����
					    Rect r = new Rect();
						r.set((int)(i*cellWidth+(float)(cellWidth/2)-checkerRadius), (int)(j*cellHeight+(float)(cellHeight/2)-checkerRadius),
							  (int)(i*cellWidth+(float)(cellWidth/2)+checkerRadius), (int)(j*cellHeight+(float)(cellHeight/2)+checkerRadius));
						c.drawBitmap(black_checker_unused_img, null, r, new Paint());						
						*/
						drawCircle(i, j, c, paintBlackCheckers);
					}
				}
			}			
		} catch(NullPointerException e) {
			e.printStackTrace();
		}
	}

	private void drawCircle(int i, int j, Canvas c, Paint p) {
		c.drawCircle(i*cellWidth+(float)(cellWidth/2), j*cellHeight+(float)(cellHeight/2), checkerRadius, p);

	}
	
	// ������ ��������� �����
	private void drawChosenChecker(Canvas c){
		Paint p = new Paint();
		p.setStrokeWidth(LINE_WIDTH);
		p.setStyle(Paint.Style.STROKE);
		p.setColor(getResources().getColor(R.color.chosen_checker));
		drawCircle(move.getI(), move.getJ(), c, p);
	}
	
	// ������ ����������� ���� ��� ��������� �����
	private void drawMoves(Canvas c){
		Paint p = new Paint();
		p.setStrokeWidth(LINE_WIDTH);
		p.setColor(getResources().getColor(R.color.allowed_moves_color));
		for (int i = 0; i < move.getN(); i++) {
			c.drawRect(move.getPoint().get(i).getX()*cellWidth,
					   move.getPoint().get(i).getY()*cellHeight,
					  (move.getPoint().get(i).getX()+1)*cellWidth,
					  (move.getPoint().get(i).getY()+1)*cellHeight,
					   p);
		}
		
		
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (touchable) {	// ������������ �������, ������ ���� �� ���������
			int x = (int)(event.getX()/cellWidth);
			int y = (int)(event.getY()/cellHeight);
			
			// �������� ���������� ����� ������� ����� ������ ���,
			// � ����� ���������� � ��������� ����� ��� ������ ����� (���������� move)
			if ((endMove) && (gameLogic.getGameState().getField()[x][y] == gameLogic.getGameState().getCurr_move())) {
				chosen = true;
				chosenI = x;
				chosenJ = y;
				move = currPlayer.getGameState().getMoves()[chosenI][chosenJ];
				
				// ����� ��� ��������� ��������� ����� � �� �����
				draw_moves = true;
				invalidate();
				
				//debugging
				/*Log.i(MOVES, "checker with coord: "+x+" "+y+" is choosen");
				for (int i = 0; i < move.getN(); i++) {
					Log.i(MOVES, "it can go to: "+move.getPoint().get(i).getX()+" "+move.getPoint().get(i).getY());
				}*/
				//-------
				
			} else {
				// ���� ����� ���� ������� ���������� ��������,
				// ��������� ����� �� ���� �������� ��� �� ���������� �� ������� �����������.
				if(chosen) {
					boolean allow = false;
					int k = 0;
					int m = 0;
					while ((!allow) && (m < move.getN())) {
						if ((move.getPoint().get(m).getX() == x) && (move.getPoint().get(m).getY() == y)) {
							allow = true;
							k = m;
						}
						m++;
					}
					// ���� ��� ��������, �� ������ ��������������� ��������� � ������� ������ � �������������� ����.
					if (allow) {
						
						//Log.i(MOVES, "checker moved to "+x+" "+y);
						
						endMove = false;
						
						currPlayer.getGameState().changeField(move.getI(), move.getJ(), move.getPoint().get(k));
						gameLogic.getGameState().copy(currPlayer.getGameState());
						gameLogic.setPoints(currPlayer.getColor());
						gameLogic.setBonuses();
						updatePointsLabels();
						// ���������, ����� �� ���������� ���
						if (move.getNext().get(k).getN() == 0) { // ��� ���������� ������
							endMove = true;						 // ������ �����. ��������� � ������ ������ 
							chosen = false;
							
							//Log.i(MOVES, "Move ends. Change player");

							// ��� ��� ��� ��������, ������ ��� ������ ����� � �� �����.						
							draw_moves = false;
							invalidate();
							
							changePlayer();
						} else {								// ��� ����� ����������.
							move = move.getNext().get(k);
							
							// ����� ��� ��������� ��������� ������ � �� �����
							draw_moves = true;
							invalidate();
							
							/*for (int i = 0; i < move.getN(); i++) {
								Log.i(MOVES, "it can go to: "+move.getPoint().get(i).getX()+" "+move.getPoint().get(i).getY());
							}*/
							
						}
					} else {
						//Log.i(MOVES, "can't move to "+x+" "+y);
					}
				} else {
					//Log.i(MOVES, "you must choose "+gameLogic.getGameState().getCurr_move());
				}
			}
		}
		return false;
	}

	// �������� ������� ������
	public void setGameLogic(GameLogic gameLogic) {
		this.gameLogic = gameLogic;
		gameState = gameLogic.getGameState();
		currPlayer = gameLogic.getPlayer(GameLogic.WHITE);
		int mode = gameLogic.getMode();
		switch (mode) {
		case GameLogic.MODE_TWO_HUMANS:
		case GameLogic.MODE_HUMAN_WHITE:
			touchable = true;
			break;
		default:
			touchable = false;
		}
		 
		
		/*
		// ���������� ����������� ������� ������ ����
		for (int i = 0; i < GameLogic.HORIZONTAL_CELL_AMOUNT; i++) {
			for (int j = 0; j < GameLogic.VERTICAL_CELL_AMOUNT; j++) {
				gameLogic.getGameState().getField()[i][j] = 0;
			}
		}
		
		// ������ ���������, ������� ����� ���������
		//win
		gameLogic.getGameState().getField()[0][0] = 2;
		gameLogic.getGameState().getField()[5][0] = 2;
		gameLogic.getGameState().getField()[0][4] = 2;
		gameLogic.getGameState().getField()[5][4] = 1;
		gameLogic.getGameState().getField()[2][3] = 1;
		
		//lose
		gameLogic.getGameState().getField()[0][0] = 2;
		gameLogic.getGameState().getField()[5][0] = 1;
		gameLogic.getGameState().getField()[4][0] = 1;
		gameLogic.getGameState().getField()[0][4] = 2;
		gameLogic.getGameState().getField()[2][3] = 1;
		
		//-----
		int white = 0;
		int black = 0;
		for(int i = 0; i < GameLogic.HORIZONTAL_CELL_AMOUNT; i++) {
			for (int j = 0; j < GameLogic.VERTICAL_CELL_AMOUNT; j++) {
				if (gameLogic.getGameState().getField()[i][j] == 1) white++;
				if (gameLogic.getGameState().getField()[i][j] == 2) black++;
			}
		}		
		gameLogic.getGameState().setWhite_checkers(white);
		gameLogic.getGameState().setBlack_checkers(black);
		gameLogic.getPlayer(gameLogic.getGameState().getCurr_move()).getGameState().copy(gameLogic.getGameState());
		gameLogic.setBonuses();		
		//------
	*/

	}
	
	// ����������� ������������ �������� �����
	private void updatePointsLabels(){
		if (!gameLogic.isInverted()) {
			player_one_points.setText(""+gameLogic.getPlayer(GameLogic.WHITE).getPoints());
			player_two_points.setText(""+gameLogic.getPlayer(GameLogic.BLACK).getPoints());
			player_one_bonus_points.setText(""+gameLogic.getPlayer(GameLogic.WHITE).getBonus_points());
			player_two_bonus_points.setText(""+gameLogic.getPlayer(GameLogic.BLACK).getBonus_points());	
		} else {
			player_one_points.setText(""+gameLogic.getPlayer(GameLogic.BLACK).getPoints());
			player_two_points.setText(""+gameLogic.getPlayer(GameLogic.WHITE).getPoints());
			player_one_bonus_points.setText(""+gameLogic.getPlayer(GameLogic.BLACK).getBonus_points());
			player_two_bonus_points.setText(""+gameLogic.getPlayer(GameLogic.WHITE).getBonus_points());				
		}
		
	}
	
	private void updateCurrMoveLabel(){
		if (!gameLogic.isInverted()){
			if (currPlayer.getColor() == GameLogic.WHITE) {
				curr_move_View.setText(R.string.player1);	
			} else {
				curr_move_View.setText(R.string.player2);
			}	
		} else {
			if (currPlayer.getColor() == GameLogic.WHITE) {
				curr_move_View.setText(R.string.player2);	
			} else {
				curr_move_View.setText(R.string.player1);
			}
		}
	}
	
	public void setLabels(View points_one, View points_two, View move, View bonus_points_1, View bonus_points_2){
		player_one_points = (TextView)points_one;
		player_two_points = (TextView)points_two;
		curr_move_View = (TextView)move;
		player_one_bonus_points = (TextView)bonus_points_1;
		player_two_bonus_points = (TextView)bonus_points_2;
	}
	
	// ������ ������ �� ����������������, �������������� �������� �������� ���������� ����.
	private void changePlayer(){
		touchable = false;	// ������ �� ��������� �������
		// ���������, �������� �� ����� � �� ���������� �� ����� �������
		if (gameLogic.defineEndGame()) {
			gameOver();
		} else {
			
			if (currPlayer.getColor() == GameLogic.WHITE) {
				currPlayer = gameLogic.getPlayer(GameLogic.BLACK);
				gameState.setCurr_move(GameLogic.BLACK);
			} else {
				currPlayer = gameLogic.getPlayer(GameLogic.WHITE);
				gameState.setCurr_move(GameLogic.WHITE);
			}
			
			updateCurrMoveLabel();
			// � ������ ������ � ����� ������� � gameState ����� ������ ����.
			// ������� ����� ������� ���� �� ������ �������� ����� gs � gs ������
			// � ��������� ������� �������� ���� �����.
			if (gameLogic.getMovesAmount() < 2) {
				currPlayer.getGameState().copy(gameState);
			} else {
				currPlayer.changeStateEnemyMove(gameState);
			}
			
			Move();	
		}
	}

	// ��������� � ����� ����
	private void gameOver() {
		int winner = gameLogic.getWinner();	
		if (gameLogic.getMode() == GameLogic.MODE_TWO_MACHINES) {	//���� ������ ��� ������, ��
			switch (winner){
			case GameLogic.WHITE: white_wins++;
								  break;
			case GameLogic.BLACK: black_wins++;
								  break;
			default: draws++;
			}
			if (++games_played == games_amount) {		// �������� ����������
				float average_moves_per_game = 0;
				for(int i: moves_per_game) average_moves_per_game+=i;
				average_moves_per_game/=(games_amount*1.0);
				Intent i = new Intent((Activity)this.getContext(), MachinesStatisticsActivity.class);
				i.putExtra(MachinesStatisticsActivity.GAMES_AMOUNT_EXTRA, games_amount);
				i.putExtra(MachinesStatisticsActivity.WHITE_AI_EXTRA, whiteAI);
				i.putExtra(MachinesStatisticsActivity.BLACK_AI_EXTRA, blackAI);
				i.putExtra(MachinesStatisticsActivity.BLACK_WINS_EXTRA, black_wins);
				i.putExtra(MachinesStatisticsActivity.WHITE_WINS_EXTRA, white_wins);
				i.putExtra(MachinesStatisticsActivity.DRAWS_EXTRA, draws);
				i.putExtra(MachinesStatisticsActivity.AVERAGE_MOVES_EXTRA, average_moves_per_game);
				((PlayActivity)this.getContext()).startStatisticsActivity(i);

			} else {						//���������� �������� ����� ���
				GameLogic gl = GameLogic.newInstance(GameLogic.MODE_TWO_MACHINES, whiteAI, blackAI);
				setGameLogic(gl);
				System.gc();
				updateCurrMoveLabel();
				Move();
			}
		} else {
			int final_speech;
			switch (winner){
			case GameLogic.WHITE: final_speech = R.string.Player1won;
								  break;
			case GameLogic.BLACK: final_speech = R.string.Player2won;
								  break;
			default: final_speech = R.string.Draw;
			}	

			FinalDialog dialog = FinalDialog.newInstance();
			dialog.setFinalSpeech(final_speech);
			dialog.show(((Activity)this.getContext()).getFragmentManager(), "Final dialog");		
		}
	}
	
	// ����������� ��������� ����� ��� �������� � ���������� ���� ��� ������
	public void Move(){
		gameLogic.movesAmountInc();
		if (!isTerminated()) {
			new Thread(new Runnable() {  //����������� ����� ���������� ��������� �������, ����� UI-����� �� ��������

				@Override
				public void run() {
					
					long time_start = System.nanoTime()/1000000;
					currPlayer.defineMoves();							
					long time_finish = System.nanoTime()/1000000;
					long time_delta = time_finish - time_start;		// ���������� �����, ���������� �� defineMoves()
					
					Log.i("move_time", "����� "+currPlayer.getWho()+". �����, ���������� �� defineMoves(): "+time_delta+ " milliseconds.");
					
					if (gameLogic.currPlayerCantMove()) {
						handler.post(new Runnable() {					// ���� ���� �����������, ��������� ������� � UI-�����
																		// (handler ���������� � UI-����� => c ��� ������������)
							@Override
							public void run() {
								gameOver();					
							}
							
						});					
					} else {
						if (currPlayer.getWho() == Player.MACHINE) {
						
							if (time_delta < MACHINE_TURN_TIME){		// ���� ����� ����������� �� ����������� ���� ������ 
								try {									// MACHINE_TURN_TIME, �� �������� ����� �� ��������� �����
									Thread.sleep(MACHINE_TURN_TIME - time_delta);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							
							handler.post(new Runnable() {	// ��� ��������� ���������� ����� � UI-�����
								
								@Override
								public void run() {		
									
									// debug-info
									((PlayerMachine)currPlayer).showTree();
													
									currPlayer.makeMove();
									gameState.copy(currPlayer.getGameState());
									gameLogic.setPoints(currPlayer.getColor());
									gameLogic.setBonuses();
									updatePointsLabels();
									invalidate();				
									changePlayer();				
								
								}
							});	
						}	
					}
					touchable = true;	// ���������� ��������� �������
				}
			}).start();
		}
		if (gameLogic.getMode() == GameLogic.MODE_TWO_MACHINES) moves_per_game[games_played]++;
	}


	public void setTwoMachinesInfo(int games_amount, int wAI, int bAI) {
		this.games_amount = games_amount;
		this.blackAI = bAI;
		this.whiteAI = wAI;
		if (games_amount > 0) moves_per_game = new int[games_amount];
	}


	public boolean isTerminated() {
		return terminated;
	}


	public void setTerminated(boolean terminated) {
		this.terminated = terminated;
	}

	
}
