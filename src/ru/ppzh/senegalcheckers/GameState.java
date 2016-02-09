package ru.ppzh.senegalcheckers;

import java.util.ArrayList;

import android.util.Log;

public class GameState {
	private int START;
	private int STOP;
    
	private int white_checkers;
	private int black_checkers;
    // Player color which is about to make a move. The color changes every level of the tree moves.
	private int curr_move;
	// Playfiled for the current gamestate.
	private int[][] field;
	private Moves[][] moves;    // Trees of allowed moves for every checker with color curr_move.
	private boolean cut;		// Ability to take enemy checker.
	private int maxDepth;		// Max amount of the enemy checkers that you can take for one move.
	
	private int movesAmount;  // Amount of allowed moves in current position.
	private int estimate; 	  
	private ArrayList<GameState> next;	// Childs nodes for building the moves tree.
	private GameState prev = null;		
	
    private boolean inverted;
        
    //--- for logging---
	private ArrayList<Moves> movesline;  
	private ArrayList<Integer> moveindex;
	//-----------------

	public GameState() {
		white_checkers = 14;
		black_checkers = 14;
		movesAmount = -1;   // -1 - moves weren't defined for this GameState.
		estimate = -1;
		inverted = false;
		curr_move = GameLogic.WHITE;
		next = new ArrayList<GameState>();
		movesline = new ArrayList<Moves>();
		moveindex = new ArrayList<Integer>();
		moves = new Moves[GameLogic.HORIZONTAL_CELL_AMOUNT][GameLogic.VERTICAL_CELL_AMOUNT];
		for (int i = 0; i < GameLogic.HORIZONTAL_CELL_AMOUNT; i++) {
			for (int j = 0; j < GameLogic.VERTICAL_CELL_AMOUNT; j++) {
				moves[i][j] = new Moves();
			}
		}
		maxDepth = 0;
		
		this.field = new int[GameLogic.HORIZONTAL_CELL_AMOUNT][GameLogic.VERTICAL_CELL_AMOUNT];
		for (int i = 0; i < this.field.length; i++) {
			for (int j = 0; j < this.field[i].length; j++) {
				if (inverted) {
					if (j < 2) this.field[i][j] = 1;
					if (j > 2) this.field[i][j] = 2;
					if (j == 2) {
						if (i < 2) this.field[i][j] = 1;
						if (i > 3) this.field[i][j] = 2;
						if (i >=2 && i <= 3) this.field[i][j] = 0;
					} 
				} else {
					if (j < 2) this.field[i][j] = 2;
					if (j > 2) this.field[i][j] = 1;
					if (j == 2) {
						if (i < 2) this.field[i][j] = 2;
						if (i > 3) this.field[i][j] = 1;
						if (i >=2 && i <= 3) this.field[i][j] = 0;
					}
				}
			}
		}
	}
	
	public GameState(boolean inverted) {
		this.inverted = inverted;
		white_checkers = 14;
		black_checkers = 14;
		movesAmount = -1;
		estimate = -1;
		curr_move = GameLogic.WHITE;
		next = new ArrayList<GameState>();
		movesline = new ArrayList<Moves>();
		moveindex = new ArrayList<Integer>();
		moves = new Moves[GameLogic.HORIZONTAL_CELL_AMOUNT][GameLogic.VERTICAL_CELL_AMOUNT];
		for (int i = 0; i < GameLogic.HORIZONTAL_CELL_AMOUNT; i++) {
			for (int j = 0; j < GameLogic.VERTICAL_CELL_AMOUNT; j++) {
				moves[i][j] = new Moves();
			}
		}
		maxDepth = 0;
		
		this.field = new int[GameLogic.HORIZONTAL_CELL_AMOUNT][GameLogic.VERTICAL_CELL_AMOUNT];
		for (int i = 0; i < this.field.length; i++) {
			for (int j = 0; j < this.field[i].length; j++) {
				if (inverted) {
					if (j < 2) this.field[i][j] = 1;
					if (j > 2) this.field[i][j] = 2;
					if (j == 2) {
						if (i < 2) this.field[i][j] = 1;
						if (i > 3) this.field[i][j] = 2;
						if (i >=2 && i <= 3) this.field[i][j] = 0;
					} 
				} else {
					if (j < 2) this.field[i][j] = 2;
					if (j > 2) this.field[i][j] = 1;
					if (j == 2) {
						if (i < 2) this.field[i][j] = 2;
						if (i > 3) this.field[i][j] = 1;
						if (i >=2 && i <= 3) this.field[i][j] = 0;
					}
				}
			}
		}
	}

	// for debugging
	public void showmoves() {
		for (int i = 0; i < GameLogic.HORIZONTAL_CELL_AMOUNT; i++) {
			for (int j = 0; j < GameLogic.VERTICAL_CELL_AMOUNT; j++) {
				if (field[i][j] == curr_move) {
					show(moves[i][j]);
				}
			}
		}		
	}
	private void show(Moves move) {
		if (move.getDepth() > -1) 
			Log.i("show", "cell with coord: "+move.getI()+" "+move.getJ()+ " have "+move.getN()+" moves. Depth: "+move.getDepth());
		int k = 0;
		while (k < move.getN()) {
			show(move.getNext().get(k)); 
			k++;
		}
				
	}
	// ----
	
    // Build the marix of possible moves for current gamestate.
	public void defineMoves(){
		maxDepth = 0;
		movesAmount = 0;
		cut = false;
		for (int i = 0; i < GameLogic.HORIZONTAL_CELL_AMOUNT; i++) {
			for (int j = 0; j < GameLogic.VERTICAL_CELL_AMOUNT; j++) {
				moves[i][j].reset();
			}
		}
		defineStartStop();
		defineCuts();		
		if (!cut) {			
			for (int i = 0; i < GameLogic.HORIZONTAL_CELL_AMOUNT; i++) {
				for (int j = START; j < STOP; j++) {
					if (field[i][j] == curr_move) {
						getMovesTree(i, j, moves[i][j]);
					}
				}
			}
		}
		
		// define the amount of allowed moves
		for (int i = 0; i < GameLogic.HORIZONTAL_CELL_AMOUNT; i++) {
			for (int j = 0; j < GameLogic.VERTICAL_CELL_AMOUNT; j++) {
				if (moves[i][j].getN() > 0) {
					movesAmount++;
				}
			}
		}		
		
	}
	
	// Define the amount of checkers that can be taken at once.
	private void defineCuts() {
		for (int i = 0; i < GameLogic.HORIZONTAL_CELL_AMOUNT; i++) {
			for (int j = START; j < STOP; j++) {
				if (field[i][j] == curr_move) {
					getCutsTree(i, j, moves[i][j], field, 0);
				}
			}
		}
		// If there are checkers to be taken, left the longest chains.
		if (cut) {
			for (int i = 0; i < GameLogic.HORIZONTAL_CELL_AMOUNT; i++) {
				for (int j = START; j < STOP; j++) {
					if (field[i][j] == curr_move) {
						if (removeForbittenCuts(moves[i][j])) {
							// do nothing
						}
					}
				}
			}			
		}
	}

	private boolean removeForbittenCuts(Moves move) {
		int k = 0;
		while (k < move.getN()) {
			if (removeForbittenCuts(move.getNext().get(k))) {
				move.getNext().remove(k);
				move.getPoint().remove(k);
				move.decreaseN();
			} else {
				k++;
			}
		}
		if ((move.getN() == 0) && (move.getDepth() < maxDepth)) {
			return true;
		} else {
			return false;
		}
	}
	
	
	// Build "cuts tree" for particular checker.
	private void getCutsTree(int i, int j, Moves move, int[][] field, int depth) {
		int[][] myfield;
		
		move.setI(i);
		move.setJ(j);
		move.setDepth(depth);
		maxDepth = (depth > maxDepth) ? depth : maxDepth;
		
		// check up
		if (canCut(field, i, j-1, 1)) {
			// Add changes and make recursive call.
			myfield = field.clone();
			for (int k = 0; k < field.length; k++) {
				myfield[k] = field[k].clone();
			}
			myfield[i][j] = 0;
			myfield[i][j-1] = 0;
			myfield[i][j-2] = curr_move;
			
			move.getPoint().add(new Point());
			move.getPoint().get(move.getN()).setX(i);
			move.getPoint().get(move.getN()).setY(j-2);
			move.getNext().add(new Moves());
			
			//Log.i("moves_tree", "checker wint coord: "+move.getI()+" "+move.getJ()+" - can CUT up. depth: "+move.getDepth());
			
			getCutsTree(i, j-2, move.getNext().get(move.getN()), myfield, depth+1);
			move.increaseN();
			cut = true;
		}
		// check down
		if (canCut(field, i, j+1, 2)) {
			myfield = field.clone();
			for (int k = 0; k < field.length; k++) {
				myfield[k] = field[k].clone();
			}
			myfield[i][j] = 0;
			myfield[i][j+1] = 0;
			myfield[i][j+2] = curr_move;
			
			move.getPoint().add(new Point());
			move.getPoint().get(move.getN()).setX(i);
			move.getPoint().get(move.getN()).setY(j+2);
			move.getNext().add(new Moves());
			
			//Log.i("moves_tree", "checker wint coord: "+move.getI()+" "+move.getJ()+" - can CUT down. depth: "+move.getDepth());
			
			getCutsTree(i, j+2, move.getNext().get(move.getN()), myfield, depth+1);
			move.increaseN();
			cut = true;	
		}
		// check left
		if (canCut(field, i-1, j, 3)) {
			myfield = field.clone();
			for (int k = 0; k < field.length; k++) {
				myfield[k] = field[k].clone();
			}
			myfield[i][j] = 0;
			myfield[i-1][j] = 0;
			myfield[i-2][j] = curr_move;
			
			move.getPoint().add(new Point());
			move.getPoint().get(move.getN()).setX(i-2);
			move.getPoint().get(move.getN()).setY(j);
			move.getNext().add(new Moves());
			
			//Log.i("moves_tree", "checker wint coord: "+move.getI()+" "+move.getJ()+" - can CUT left. depth: "+move.getDepth());
			
			getCutsTree(i-2, j, move.getNext().get(move.getN()), myfield, depth+1);
			move.increaseN();
			cut = true;
		}
		// check right
		if (canCut(field, i+1, j, 4)) {
			myfield = field.clone();
			for (int k = 0; k < field.length; k++) {
				myfield[k] = field[k].clone();
			}
			myfield[i][j] = 0;
			myfield[i+1][j] = 0;
			myfield[i+2][j] = curr_move;
			
			move.getPoint().add(new Point());
			move.getPoint().get(move.getN()).setX(i+2);
			move.getPoint().get(move.getN()).setY(j);
			move.getNext().add(new Moves());
			
			//Log.i("moves_tree", "checker wint coord: "+move.getI()+" "+move.getJ()+" - can CUT right. depth: "+move.getDepth());
			
			getCutsTree(i+2, j, move.getNext().get(move.getN()), myfield, depth+1);
			move.increaseN();
			cut = true;
		}
	}
	
	// Define witch direction checker can move.
	private void getMovesTree(int i, int j, Moves move){
		// check up
		if ((((curr_move == GameLogic.WHITE) && !inverted) ||
			 ((curr_move == GameLogic.BLACK) && inverted)) && canMove(i, j-1)) {
			move.getPoint().add(new Point());
			move.getPoint().get(move.getN()).setX(i);
			move.getPoint().get(move.getN()).setY(j-1);
			move.increaseN();
			move.getNext().add(new Moves());
			
			//Log.i("moves_tree", "checker wint coord: "+i+" "+j+" - can go up");
		}
		// check down
		if ((((curr_move == GameLogic.BLACK) && !inverted) ||
			 ((curr_move == GameLogic.WHITE)) && inverted) && canMove(i, j+1)) {
			move.getPoint().add(new Point());
			move.getPoint().get(move.getN()).setX(i);
			move.getPoint().get(move.getN()).setY(j+1);
			move.increaseN();
			move.getNext().add(new Moves());
			
			//Log.i("moves_tree", "checker wint coord: "+i+" "+j+" - can go down");
		}
		// check left
		if (canMove(i-1, j)) {
			move.getPoint().add(new Point());
			move.getPoint().get(move.getN()).setX(i-1);
			move.getPoint().get(move.getN()).setY(j);
			move.increaseN();
			move.getNext().add(new Moves());
			
			//Log.i("moves_tree", "checker wint coord: "+i+" "+j+" - can go left");
		}
		// check right
		if (canMove(i+1, j)) {
			move.getPoint().add(new Point());
			move.getPoint().get(move.getN()).setX(i+1);
			move.getPoint().get(move.getN()).setY(j);
			move.increaseN();
			move.getNext().add(new Moves());
			
			//Log.i("moves_tree", "checker wint coord: "+i+" "+j+" - can go right");
		}
	}
	
	// Define if checker can cut enemy check in particular direction (direction is given by k).
	private boolean canCut(int[][] myfield, int I, int J, int k) {
		if (I > -1 && I < GameLogic.HORIZONTAL_CELL_AMOUNT &&
			J > -1 && J < GameLogic.VERTICAL_CELL_AMOUNT) {
			if ((myfield[I][J] != 0) && (myfield[I][J] != curr_move)) {
				switch (k) {
				case 1:	if ((J > 0) && (myfield[I][J-1] == 0)) return true;
						break;
				case 2:	if ((J < GameLogic.VERTICAL_CELL_AMOUNT-1) && (myfield[I][J+1] == 0)) return true;
						break;
				case 3:	if ((I > 0) && (myfield[I-1][J] == 0)) return true;
						break;
				case 4:	if ((I < GameLogic.HORIZONTAL_CELL_AMOUNT-1) && (myfield[I+1][J] == 0)) return true;
						break;
				default: break;
				}
			}
		}
		return false;
	}
	
	// Define if a particular cell is free from any checkers.
	private boolean canMove(int i , int j){
		if (i > -1 && i < GameLogic.HORIZONTAL_CELL_AMOUNT &&
			j > -1 && j < GameLogic.VERTICAL_CELL_AMOUNT) {
			if (field[i][j] == 0) {
				return true;
			}
		}
		return false;
	}

	// Forbid bonus checkers to move on the last line.
	private void defineStartStop() {
		if (!inverted) {
			if (curr_move == GameLogic.WHITE) {
				START = 1;
				STOP = GameLogic.VERTICAL_CELL_AMOUNT;
			} else {
				START = 0;
				STOP = GameLogic.VERTICAL_CELL_AMOUNT-1;
			}	
		} else {
			if (curr_move == GameLogic.BLACK) {
				START = 1;
				STOP = GameLogic.VERTICAL_CELL_AMOUNT;
			} else {
				START = 0;
				STOP = GameLogic.VERTICAL_CELL_AMOUNT-1;
			}				
		}
		
	}
	
	// Make changes after move:
	// I J - origin coordinates
	// p - target coordinates
	public void changeField(int I, int J, Point p){
		field[I][J] = 0;
		field[p.getX()][p.getY()] = curr_move;
		
		// Define if it was taking or usual move.
		boolean isCut = false;		
		int diffX = I - p.getX();	
		int diffY = J - p.getY();
		if (diffX == 0) {
			// up
			if (diffY == 2) {
				isCut = true;
				field[I][J-1] = 0;
			}
			// down
			if (diffY == -2) {
				isCut = true;
				field[I][J+1] = 0;
			}
		}
		if (diffY == 0){
			// left
			if (diffX == 2) {
				isCut = true;
				field[I-1][J] = 0;
			}
			// right
			if (diffX == -2) {
				isCut = true;
				field[I+1][J] = 0;
			}
		}
		
		if(isCut){
			if (curr_move == GameLogic.WHITE) black_checkers--;
			if (curr_move == GameLogic.BLACK) white_checkers--;
		}
	}
	
	
	// Copy main parameters from gamestate.
	public void copy(GameState gs){
		white_checkers = gs.white_checkers;
		black_checkers = gs.black_checkers;
		inverted = gs.isInverted();
		field = gs.getField().clone();
		for (int k = 0; k < gs.getField().length; k++) {
			field[k] = gs.getField()[k].clone();
		}
		prev = null;
		next.clear();
	}

	// Check if players have passed one another.
	public boolean checkersExchanged(){
		int white_line;
		int black_line;
		if (!inverted) {
			white_line = GameLogic.VERTICAL_CELL_AMOUNT - 1;
			black_line = 0;
		} else {
			white_line = 0;
			black_line = GameLogic.VERTICAL_CELL_AMOUNT - 1;			
		}

		for (int j = 0; j < GameLogic.VERTICAL_CELL_AMOUNT; j++) {
			for (int i = 0; i < GameLogic.HORIZONTAL_CELL_AMOUNT; i++) {
				if (!inverted && (field[i][j] == 1)) white_line = j;
				if (inverted && (field[i][j] == 2)) black_line = j;
			}
		}
		for (int j = GameLogic.VERTICAL_CELL_AMOUNT-1; j > -1; j--) {
			for (int i = 0; i < GameLogic.HORIZONTAL_CELL_AMOUNT; i++) {
				if (!inverted && (field[i][j] == 2)) black_line = j;
				if (inverted && (field[i][j] == 1)) white_line = j;
			}
		}
		
		//Log.i(GameLogic.ENDGAME, "highest black line: "+black_line+". lowest white line: "+white_line);
		
		if (!inverted && (black_line > white_line+1)) {
			return true;
		}
		if (inverted && (white_line > black_line+1)) {
			return true;
		}
		
		return false;
	}
	
	public int getOnFinalLine(int color){
		int n = 0;
		int j;
		if (!inverted) {
			j = 0;
			if (color == GameLogic.BLACK) j = GameLogic.VERTICAL_CELL_AMOUNT-1;	
		} else {
			j = GameLogic.VERTICAL_CELL_AMOUNT-1;
			if (color == GameLogic.BLACK) j = 0;	
		}
		
		for (int i = 0; i < GameLogic.HORIZONTAL_CELL_AMOUNT; i++) {
			if (field[i][j] == color) n++;
		}
		return n;
	}

	public int getOnFirstLine(int color){
		int n = 0;
		int j;
		if (!inverted) {
			j = GameLogic.VERTICAL_CELL_AMOUNT-1;
			if (color == GameLogic.BLACK) j = 0;	
		} else {
			j = 0;
			if (color == GameLogic.BLACK) j = GameLogic.VERTICAL_CELL_AMOUNT-1;	
		}
		
		for (int i = 0; i < GameLogic.HORIZONTAL_CELL_AMOUNT; i++) {
			if (field[i][j] == color) n++;
		}
		return n;
	}


	public int getWhite_checkers() {
		return white_checkers;
	}

	public int getBlack_checkers() {
		return black_checkers;
	}

	public int getCurr_move() {
		return curr_move;
	}

	public void setCurr_move(int curr_move) {
		this.curr_move = curr_move;
	}

	public int[][] getField() {
		return field;
	}

	public Moves[][] getMoves() {
		return moves;
	}
		
	public int getMaxDepth() {
		return maxDepth;
	}

	public int getMovesAmount() {
		return movesAmount;
	}

	public boolean isCut() {
		return cut;
	}

	public boolean isInverted() {
		return inverted;
	}

	public int getEstimate() {
		return estimate;
	}

	public void setEstimate(int estimate) {
		this.estimate = estimate;
	}

	public ArrayList<GameState> getNext() {
		return next;
	}
	
	
	public ArrayList<Moves> getMovesline() {
		return movesline;
	}

	public ArrayList<Integer> getMoveindex() {
		return moveindex;
	}

	public void setMoveindex(ArrayList<Integer> moveindex) {
		this.moveindex = moveindex;
	}

	public void setMovesline(ArrayList<Moves> movesline) {
		this.movesline = movesline;
	}

	public GameState getPrev() {
		return prev;
	}

	public void setPrev(GameState prev) {
		this.prev = prev;
	}

	public void setWhite_checkers(int white) {
		white_checkers = white;
	}

	public void setBlack_checkers(int black) {
		black_checkers = black;
		
	}
	
	public int defineWinner() {
		int wpoints = 0;
		int bpoints = 0;
		for (int i = 0; i < 6; ++i) {
			for (int j = 1; j < 4; ++j) {
				switch (field[i][j]) {
					case 1: wpoints++;
							break;
					case 2: bpoints++;
							break;
				}
			}
		}
		for (int i = 0; i < 6; ++i) {
			if (!inverted) {
				if (field[i][0] == 1)
					wpoints += 2;
				if (field[i][4] == 2) {
					bpoints += 2;
				}
			} else {
				if (field[i][0] == 2)
					bpoints += 2;
				if (field[i][4] == 1) {
					wpoints += 2;
				}
			}
		}
		if (wpoints > bpoints) {
			return GameLogic.WHITE;
		} else {
			return bpoints > wpoints ? GameLogic.BLACK : GameLogic.DRAW;
		}
	}
	
}
