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
	
	// Определяем все возможные ходы
	@Override
	public void defineMoves(){
		getGameState().defineMoves();   
		if (getGameState().getMovesAmount() > 0) {
			eraseEstimators(getGameState());
			getGameState().setPrev(null);  			// У корневого узла всегда отсутствует предок.
			 
			// Дерево строится с нуля каждый раз. Это оказалось проще чем достраивать его до нужной глубины после каждого хода.
			// Хотя и немного затратнее по времени.
			getGameState().getNext().clear();		
			makeTree(getGameState(), 0);	
		}
	}
	
	//---debugging---
	public void showTree(){
		show(getGameState(), 0);
	}	
	private void show(GameState gs, int depth) {
		Log.i(TREE_INFO, "ход: "+gs.getCurr_move()+". Глубина: "+depth);
		Log.i(TREE_INFO, "Estimate: "+gs.getEstimate());	
		Log.i(TREE_INFO, "Цепочка ходов:");
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


	// Строит дерево ходов на заданную глубину
	public void makeTree(GameState gState, int depth){
		if (gState.getMovesAmount() == -1) gState.defineMoves(); 
		// игра может закончиться раньше, чем будет достигнута заданная глубина.
		if (depth < analizeDepth) {  		
			if ((gState.getMovesAmount() > 0) && (!gState.checkersExchanged())) {	
				terminate_analize = false;
				Moves moves[] = getMovesArray(gState); 				// достраиваем дочерние узлы
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
	
	// Как только добрались до листа дерева - получаем его оценку и применяем альфа-бета отсечения для определения,
	// нужно ли дальше строить дерево для данной ветки ходов. Для этого сравниваем оценку узла с оценкой двух родительских узлов.
	// Оценка узлам выставляется по принципу минимакса.
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
	
	// Каждый ход, в свою очередь может также представлять разветвленное дерево и давать несколько вариантов.
	private void downToMovesTree(Moves move, GameState gState, GameState globalState, int depth, 
								 ArrayList<Moves> moves, ArrayList<Integer> index){
		// каждая шашка может иметь до 4 направлений хода. Что, потенциально, может дать 4ре узла.
		for (int i = 0; i < move.getN(); i++) {
			GameState gs = new GameState();				// для каждого из возможных направлений создаем новый узел GameState
			gs.copy(gState);							// и имитируем совершение хода
			gs.setCurr_move(gState.getCurr_move());
			gs.changeField(move.getI(), move.getJ(), move.getPoint().get(i));
			
			
			ArrayList<Moves> movesline = new ArrayList<Moves>();		//-----------------
			ArrayList<Integer> movesindex = new ArrayList<Integer>();	// 
			if (moves != null) {										// Запоминаем информацию о текущем ходе (move)
				copyMovesInfo(movesline, moves, movesindex, index);		// и о направлении, в котором был сделан ход (i)
			}															// Это используется только для логгирования информации 
			movesline.add(move);										// о построеном дереве
			movesindex.add(i);											//-----------------
																								
			
																									// пока можно продолжить
			downToMovesTree(move.getNext().get(i), gs, globalState, depth, movesline, movesindex);  // ход - рекурсия
		}
		if (move.getN() == 0) {										// если ход продложить нельзя, проводим необходимые действия над 	
			if (globalState.getCurr_move() == GameLogic.WHITE) {	// GameState и добавляем его в в дерево
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
	
	//копирование данных
	private void copyMovesInfo(ArrayList<Moves> movesTO, ArrayList<Moves> movesFROM,
							   ArrayList<Integer> indexTO, ArrayList<Integer> indexFROM){
		
		for (int i = 0; i < movesFROM.size(); i++) {
			movesTO.add(movesFROM.get(i));
			indexTO.add(indexFROM.get(i));
		}
	}
	
	// Для простоты индексации превращает двумерный массив ходов в одномерный 
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
	
	// выбор одного из дочерних улов для совершения хода
	@Override
	public void makeMove(){
		// выбор рандомного узла при одинаковой оценке
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
		// выбор первого узла с данной оценкой
		int i = 0;
		while ((getGameState().getNext().get(i).getEstimate() != getGameState().getEstimate()) &&
			   ( i < getGameState().getNext().size())) {
			i++;
		}
		changeState(i);
		*/
	}
	
	// Изменения в поле текущего игрока после хода портивника
	@Override
	public void changeStateEnemyMove(GameState gs){
		// Если ход, сделанный противником был проанализирован - тогда находим его в дереве анализа и спускаемся к ниму.
		// Иначе в ручную копируем данные gs в текущий gameState и меняем цвет игрока совершающего следующий ход.
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
	
	// сравнение двух полей
	private boolean fieldsEqual(int[][] a, int[][] b){
		for (int i = 0; i < GameLogic.HORIZONTAL_CELL_AMOUNT; i++) {
			for (int j = 0; j < GameLogic.VERTICAL_CELL_AMOUNT; j++) {
				if (a[i][j] != b[i][j]) return false;
			}
		}
		return true;
	}
	
	// Оценочная функция для компьютерного игрока. 
	// При создании игрока-машины необходимо 
	// наследовать класс от PlayerMachine и переопределить getEstimator.
	// Все оценки описаны в классе Estimate.
	abstract protected void getEstimator(GameState gs, int color);
}
