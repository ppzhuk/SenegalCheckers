package ru.ppzh.senegalcheckers;

import java.util.ArrayList;
import java.util.Random;

import android.util.Log;

public abstract class PlayerMachine extends Player {
	public static final String TREE_INFO = "tree_info";
	
	private int analizeDepth;
	private boolean terminate_analize;
	
	public PlayerMachine(int color, String who, boolean inverted, int analizeDepth){
		super(color, who, inverted);
		this.analizeDepth = analizeDepth;
	}
	
	// ���������� ��� ��������� ����
	@Override
	public void defineMoves(){
		getGameState().defineMoves();   
		if (getGameState().getMovesAmount() > 0) {
			eraseEstimators(getGameState());
			getGameState().setPrev(null);  			// � ��������� ���� ������ ����������� ������.
			 
			// ������ �������� � ���� ������ ���. ��� ��������� ����� ��� ����������� ��� �� ������ ������� ����� ������� ����.
			// ���� � ������� ��������� �� �������.
			getGameState().getNext().clear();		
			makeTree(getGameState(), 0);	
		}
	}
	
	//---debugging---
	public void showTree(){
		show(getGameState(), 0);
	}	
	private void show(GameState gs, int depth) {
		Log.i(TREE_INFO, "���: "+gs.getCurr_move()+". �������: "+depth);
		Log.i(TREE_INFO, "Estimate: "+gs.getEstimate());	
		Log.i(TREE_INFO, "������� �����:");
		for (int i = 0; i < gs.getMovesline().size(); i++) {
			Log.i(TREE_INFO, ""+gs.getMovesline().get(i).getI()+" "+gs.getMovesline().get(i).getJ()+" --> "+
								gs.getMovesline().get(i).getPoint().get(gs.getMoveindex().get(i)).getX()+" "+
								gs.getMovesline().get(i).getPoint().get(gs.getMoveindex().get(i)).getY());
		}
		for (int i = 0; i < gs.getNext().size(); i++) {
			show(gs.getNext().get(i), depth+1);
		}
		
	}
	//------------------


	// ������ ������ ����� �� �������� �������
	public void makeTree(GameState gState, int depth){
		if (gState.getMovesAmount() == -1) gState.defineMoves(); 
		// ���� ����� ����������� ������, ��� ����� ���������� �������� �������.
		if (depth < analizeDepth) {  		
			if ((gState.getMovesAmount() > 0) && (!gState.checkersExchanged())) {	
				terminate_analize = false;
				Moves moves[] = getMovesArray(gState); 				// ����������� �������� ����
				for (int k = 0; k < gState.getMovesAmount(); k++) {
					if (!terminate_analize) {
						downToMovesTree(moves[k], gState, gState, depth, null, null);
					} else {
						break;
					}
				}
				terminate_analize = false;
			}
		} 
		defineEstimate(gState, depth);
	}
	
	// ��� ������ ��������� �� ����� ������ - �������� ��� ������ � ��������� �����-���� ��������� ��� �����������,
	// ����� �� ������ ������� ������ ��� ������ ����� �����. ��� ����� ���������� ������ ���� � ������� ���� ������������ �����.
	// ������ ����� ������������ �� �������� ���������.
	private void defineEstimate(GameState gState, int depth) {
		if (gState.getMovesAmount() == 0 || gState.checkersExchanged() || depth == analizeDepth) {
			getEstimator(gState, this.getColor());
		}
		
		if (depth % 2 == 0) {
			GameState previous = gState.getPrev();
			if (previous != null && (previous.getEstimate() == -1 || previous.getEstimate() > gState.getEstimate())) {
				previous.setEstimate(gState.getEstimate());
				GameState prePrevious = previous.getPrev();
				if (prePrevious != null && prePrevious.getEstimate() != -1 && prePrevious.getEstimate() >= gState.getEstimate()) {
					terminate_analize = true;
				}
			}
		}
		if (depth % 2 == 1) {
			GameState previous = gState.getPrev();
			if (previous != null && (previous.getEstimate() == -1 || previous.getEstimate() < gState.getEstimate())) {
				previous.setEstimate(gState.getEstimate());
				GameState prePrevious = previous.getPrev();
				if (prePrevious != null && prePrevious.getEstimate() != -1 && prePrevious.getEstimate() <= gState.getEstimate()) {
					terminate_analize = true;
				}
			}
		}
	}
	
	// ������ ���, � ���� ������� ����� ����� ������������ ������������� ������ � ������ ��������� ���������.
	private void downToMovesTree(Moves move, GameState gState, GameState globalState, int depth, 
								 ArrayList<Moves> moves, ArrayList<Integer> index){
		// ������ ����� ����� ����� �� 4 ����������� ����. ���, ������������, ����� ���� 4�� ����.
		for (int i = 0; i < move.getN(); i++) {
			GameState gs = new GameState();				// ��� ������� �� ��������� ����������� ������� ����� ���� GameState
			gs.copy(gState);							// � ��������� ���������� ����
			gs.setCurr_move(gState.getCurr_move());
			gs.changeField(move.getI(), move.getJ(), move.getPoint().get(i));
			
			
			ArrayList<Moves> movesline = new ArrayList<Moves>();		//-----------------
			ArrayList<Integer> movesindex = new ArrayList<Integer>();	// 
			if (moves != null) {										// ���������� ���������� � ������� ���� (move)
				copyMovesInfo(movesline, moves, movesindex, index);		// � � �����������, � ������� ��� ������ ��� (i)
			}															// ��� ������������ ������ ��� ������������ ���������� 
			movesline.add(move);										// � ���������� ������
			movesindex.add(i);											//-----------------
																								
			
																									// ���� ����� ����������
			downToMovesTree(move.getNext().get(i), gs, globalState, depth, movesline, movesindex);  // ��� - ��������
		}
		if (move.getN() == 0) {										// ���� ��� ���������� ������, �������� ����������� �������� ��� 	
			if (globalState.getCurr_move() == GameLogic.WHITE) {	// GameState � ��������� ��� � � ������
				gState.setCurr_move(GameLogic.BLACK);
			} else {
				gState.setCurr_move(GameLogic.WHITE);
			}								
			gState.setMovesline(moves);								
			gState.setMoveindex(index);								
			globalState.getNext().add(gState);
			gState.setPrev(globalState);
			makeTree(gState, depth+1);
		}
	}
	
	//����������� ������
	private void copyMovesInfo(ArrayList<Moves> movesTO, ArrayList<Moves> movesFROM,
							   ArrayList<Integer> indexTO, ArrayList<Integer> indexFROM){
		
		for (int i = 0; i < movesFROM.size(); i++) {
			movesTO.add(movesFROM.get(i));
			indexTO.add(indexFROM.get(i));
		}
	}
	
	// ��� �������� ���������� ���������� ��������� ������ ����� � ���������� 
	private Moves[] getMovesArray(GameState gState) {
		Moves moves[] = new Moves[gState.getMovesAmount()];
		int k = 0;
		for (int i = 0; i < GameLogic.HORIZONTAL_CELL_AMOUNT; i++ ) {
			for (int j = 0; j < GameLogic.VERTICAL_CELL_AMOUNT; j++) {
				if (gState.getMoves()[i][j].getN() > 0) moves[k++] = gState.getMoves()[i][j];
			}
		}
		return moves;
	}	
	
	private void eraseEstimators(GameState gs) {
		for (int i = 0; i < gs.getNext().size(); i++) {
			eraseEstimators(gs.getNext().get(i));
		}
		gs.setEstimate(-1);
	}
	
	// ����� ������ �� �������� ���� ��� ���������� ����
	@Override
	public void makeMove(){
		// ����� ���������� ���� ��� ���������� ������
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < getGameState().getNext().size(); i++) {
			if (getGameState().getNext().get(i).getEstimate() == getGameState().getEstimate()) {
				list.add(i);
			}
		}
		
		Random r = new Random(System.currentTimeMillis());
		int random = r.nextInt(list.size());
		changeState(list.get(random));
		
		/*
		// ����� ������� ���� � ������ �������
		int i = 0;
		while ((getGameState().getNext().get(i).getEstimate() != getGameState().getEstimate()) &&
			   ( i < getGameState().getNext().size())) {
			i++;
		}
		changeState(i);
		*/
	}
	
	// ��������� � ���� �������� ������ ����� ���� ����������
	@Override
	public void changeStateEnemyMove(GameState gs){
		// ���� ���, ��������� ����������� ��� ��������������� - ����� ������� ��� � ������ ������� � ���������� � ����.
		// ����� � ������ �������� ������ gs � ������� gameState � ������ ���� ������ ������������ ��������� ���.
		int size = getGameState().getNext().size();
		if (size != 0) {
			int i = 0;
			while (i < size && !fieldsEqual(getGameState().getNext().get(i).getField(), gs.getField())) {
				i++;
			}
			if (i < size) {
				changeState(i);	
				return;
			}
		}
		getGameState().copy(gs);
		if (this.getGameState().getCurr_move() == GameLogic.BLACK) {
			this.getGameState().setCurr_move(GameLogic.WHITE);
		} else {
			this.getGameState().setCurr_move(GameLogic.BLACK);
		}
	}
	
	// ��������� ���� �����
	private boolean fieldsEqual(int[][] a, int[][] b){
		for (int i = 0; i < GameLogic.HORIZONTAL_CELL_AMOUNT; i++) {
			for (int j = 0; j < GameLogic.VERTICAL_CELL_AMOUNT; j++) {
				if (a[i][j] != b[i][j]) return false;
			}
		}
		return true;
	}
	
	// ��������� ������� ��� ������������� ������. 
	// ��� �������� ������-������ ���������� 
	// ����������� ����� �� PlayerMachine � �������������� getEstimator.
	// ��� ������ ������� � ������ Estimate.
	abstract protected void getEstimator(GameState gs, int color);
}
