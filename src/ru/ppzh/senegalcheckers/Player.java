package ru.ppzh.senegalcheckers;

// класс игрока человека.
// Класс для игрока- машины необходимо наследовать от этого класса.
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
	
	// спускается на i-тый узел дерева. Используется в подклассе PlayerMachine
	public void changeState(int i) {
		gameState = gameState.getNext().get(i);
	}
	
	// Действия, необходимые для определения возможных ходов. 
	public void defineMoves(){
		gameState.defineMoves();
	}
	
	// Определяется в PlayerMachine.
	public void makeMove(){
		// заглушка
	}
	
	// Внесение изменений в gameState текущего игрока после хода противника. 
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
	
	
}
