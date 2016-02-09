package ru.ppzh.senegalcheckers;

import android.util.Log;

public class GameLogic {
	public static final String ENDGAME = "endgame";
	
	public static final int WHITE = 1;
	public static final int BLACK = 2;
	public static final int DRAW = 3;
	public static final int HORIZONTAL_CELL_AMOUNT = 6;
	public static final int VERTICAL_CELL_AMOUNT = 5;
	public static final int ANALIZE_DEPTH = 4;   
	public static final int MODE_TWO_HUMANS = 0;
	public static final int MODE_HUMAN_WHITE = 1;
	public static final int MODE_HUMAN_BLACK = 2;
	public static final int MODE_TWO_MACHINES = 3;
	
	// The checkers colors should be inverted if the white checkers have started the game in the top.
	private boolean inverted;	
    
	private int mode;
	private int winner;			
	private Player playerWhite;
	private Player playerBlack;
	private GameState gameState;
	private static GameLogic gl = null;
	private int moves_am = 0;   // The amount of moves during the match.
	
	private GameLogic(int mode){
		this.mode = mode;
		inverted = false;
		winner = 0;
		switch(mode){
		case MODE_TWO_HUMANS:
			playerWhite = new Player(WHITE, Player.MAN, inverted);
			playerBlack = new Player(BLACK, Player.MAN, inverted);
			break;
		case MODE_HUMAN_WHITE:
			playerWhite = new Player(WHITE, Player.MAN, inverted);
			playerBlack = new AI_v2(BLACK, Player.MACHINE, inverted, ANALIZE_DEPTH);
			//playerBlack = new PlayerMachine(BLACK, Player.MACHINE, inverted, ANALIZE_DEPTH);
			break;
		case MODE_HUMAN_BLACK:
			inverted = true;
			playerWhite = new AI_v2(WHITE, Player.MACHINE, inverted, ANALIZE_DEPTH);
			playerBlack = new Player(BLACK, Player.MAN, inverted);
			break;
		}
		gameState = new GameState(inverted);
	}
	
	private GameLogic(int mode, int whiteAI, int blackAI){
		this.mode = mode;
		inverted = false;
		winner = 0;
		switch(whiteAI){
		case Player.AI_v1:
			playerWhite = new AI_v1(WHITE, Player.MACHINE, inverted, ANALIZE_DEPTH);
			break;
		case Player.AI_v2:
			playerWhite = new AI_v2(WHITE, Player.MACHINE, inverted, ANALIZE_DEPTH);
			break;
		case Player.AI_v3:
			playerWhite = new AI_v3(WHITE, Player.MACHINE, inverted, ANALIZE_DEPTH);
			break;
			
		// Add new AIs here.
			
		default:
			playerWhite = new AI_v1(WHITE, Player.MACHINE, inverted, ANALIZE_DEPTH);
		}
		switch(blackAI){
		case Player.AI_v1:
			playerBlack = new AI_v1(BLACK, Player.MACHINE, inverted, ANALIZE_DEPTH);
			break;
		case Player.AI_v2:
			playerBlack = new AI_v2(BLACK, Player.MACHINE, inverted, ANALIZE_DEPTH);
			break;
		case Player.AI_v3:
			playerBlack = new AI_v3(BLACK, Player.MACHINE, inverted, ANALIZE_DEPTH);
			break;
			
		// Add new AIs here.
				
		default:
			playerBlack = new AI_v1(BLACK, Player.MACHINE, inverted, ANALIZE_DEPTH);
		}		
		gameState = new GameState(inverted);
	}
	
	public static GameLogic newInstance(int mode) {
		gl = null;
		gl = new GameLogic(mode);
		return gl;
		
	}
	
	public static GameLogic newInstance(int mode, int whiteAI, int blackAI) {
		gl = null;
		gl =  new GameLogic(mode, whiteAI, blackAI);
		return gl;
	}	
	
	public static GameLogic getGameLogic() {
		if (gl != null) {
			return gl;
		} else {
			return null;
		}
	}
	// Check 2 first conditions of the game's end.
	public boolean defineEndGame(){
		boolean endGame = false;
		
		//Log.i(ENDGAME, "white checkers - "+gameState.getWhite_checkers()+". black checkers - "+gameState.getBlack_checkers());
		
		// 1. If the last checker were taken OR
		// 2. If the players have passed for one another.
		if ((gameState.getWhite_checkers() == 0) ||
			(gameState.getBlack_checkers() == 0) ||
			 gameState.checkersExchanged()) {
			
			defineWinner();
			endGame = true;	
		}

		//Log.i(ENDGAME, "winner: "+winner+". endgame: "+endGame);
		//Log.i(ENDGAME, "--------------------------------------");
		return endGame;
	}
	
	// Check the third condition of the game's end.
	public boolean currPlayerCantMove(){
		// 3. If player can't to make a move.
		if (getPlayer(gameState.getCurr_move()).getGameState().getMovesAmount() == 0) {
			defineWinner();
			return true;
		}
		return false;
	}
	
	private void defineWinner(){
		if (playerWhite.getAllPoints() > playerBlack.getAllPoints()) {
			winner = WHITE;
		} else {
			if (playerWhite.getAllPoints() < playerBlack.getAllPoints()) {
				winner = BLACK;
			} else {
				winner = DRAW;
			}
		}
		if (inverted){
			switch (winner){
			case WHITE: winner = BLACK;
						break;
			case BLACK: winner = WHITE;		
						break;
			default:    winner = DRAW;
					    break;
			}
		}
	}
	
	public int getWinner() {
		return winner;
	}
	
	public void setPoints(int color){
		if (color == WHITE) {
			playerWhite.setPoints(14 - gameState.getBlack_checkers());
		} else {
			playerBlack.setPoints(14 - gameState.getWhite_checkers());
		}
	}
	
	public void setBonuses(){
		playerWhite.setBonus_points(gameState.getOnFinalLine(WHITE));
		playerBlack.setBonus_points(gameState.getOnFinalLine(BLACK));
	}

	public Player getPlayer(int color) {
		if (playerWhite.getColor() == color) {
			return playerWhite;
		} else {
			return playerBlack;
		}
	}
	
	public GameState getGameState() {
		return gameState;
	}
	
	public int getMode() {
		return mode;
	}

	public boolean isInverted() {
		return inverted;
	}
	
	public void movesAmountInc() {
		moves_am++;
	}

	public int getMovesAmount() {
		return moves_am;
	}
	
	
}
