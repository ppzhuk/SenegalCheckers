package ru.ppzh.senegalcheckers;

public class Player {
	public static final String MAN = "man";
	public static final String MACHINE = "machine";
	public static final int AI_v1 = 0;
	public static final int AI_v2 = 1;
	public static final int AI_v3 = 2;
	
	private String who;		// MAN or MACHINE
	private int color;
	private int points;
	private int bonus_points;
	private GameState gameState;
		
	public Player(int color, String who, boolean inverted) {
		points = 0;
		bonus_points = 0;
		this.who = who;
		this.color = color;
		this.gameState = new GameState(inverted);
		this.gameState.setCurr_move(color);
	}
	
	// Go down for one move.
	public void changeState(int i) {
		gameState = gameState.getNext().get(i);
	}
	
	public void defineMoves(){
		gameState.defineMoves();
	}
		
	// Get a gamestate after enemy move.
	public void changeStateEnemyMove(GameState gs){
		gameState.copy(gs);
	}
	
	public int getColor() {
		return color;
	}

	public int getPoints() {
		return points;
	}
	
	public GameState getGameState() {
		return gameState;
	}
	
	public int getBonus_points() {
		return bonus_points;
	}

	public void setBonus_points(int bonus) {
		this.bonus_points = bonus;
	}

	public int getAllPoints(){
		return points+bonus_points;
	}

	public String getWho() {
		return who;
	}

	public void setPoints(int points) {
		this.points = points;
	}
	
	public void makeMove(){
		
	}
}
