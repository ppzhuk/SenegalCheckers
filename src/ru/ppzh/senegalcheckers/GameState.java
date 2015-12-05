package ru.ppzh.senegalcheckers;

import java.util.ArrayList;

import android.util.Log;

public class GameState {
	private int START;
	private int STOP;
	
	// количество белых и черных шашек
	private int white_checkers;
	private int black_checkers;
	// цвет игрока, для которого расчитывается ход
	// При построении дерева для игрока-машины, цвет игрока меняется с каждым уровнем 
	private int curr_move;
	// игровое поле для текущей позиции
	private int[][] field;
	// массив, хранящий деревья резрешенных ходов для каждой шашки цвета curr_move
	private Moves[][] moves;
	private boolean cut;		// флаг, говорит о наличии возможности взять шашку противника
	private int maxDepth;		// максимальное количество вражеских шашек, которое можно взять за раз
	
	private int movesAmount;  // oбщее количество ходов, возможных в данном положении
	private int estimate; 	  // оценка узла
	private ArrayList<GameState> next;	// дочерние узлы для построения дерева анализа
	private GameState prev = null;		// ссылка на родителя в дереве анализа
	
	private ArrayList<Moves> movesline;  // используется при логгировании информации о построеном дереве
	private ArrayList<Integer> moveindex;
	
	private boolean inverted;

	public GameState() {
		white_checkers = 14;
		black_checkers = 14;
		movesAmount = -1;   // -1 - значит ходы еще не были определены для данного GameState
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
		movesAmount = -1;	// -1 - значит ходы еще не были определены для данного GameState
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
	
	// строит матрицу возможных ходов для текущего состояния
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
		defineCuts();		// строим дерево для взятия вражеских шашек		
		if (!cut) {			// если нет вражеских шашек для взятия, определяем обычные ходы
			for (int i = 0; i < GameLogic.HORIZONTAL_CELL_AMOUNT; i++) {
				for (int j = START; j < STOP; j++) {
					if (field[i][j] == curr_move) {
						getMovesTree(i, j, moves[i][j]);
					}
				}
			}
		}
		
		// посчитать общее количесво ходов 
		for (int i = 0; i < GameLogic.HORIZONTAL_CELL_AMOUNT; i++) {
			for (int j = 0; j < GameLogic.VERTICAL_CELL_AMOUNT; j++) {
				if (moves[i][j].getN() > 0) {
					movesAmount++;
				}
			}
		}		
		
	}
	
	// Для каждой шашки опеделяем сколько подряд вражеских шашек она может срубить 
	private void defineCuts() {
		for (int i = 0; i < GameLogic.HORIZONTAL_CELL_AMOUNT; i++) {
			for (int j = START; j < STOP; j++) {
				if (field[i][j] == curr_move) {
					getCutsTree(i, j, moves[i][j], field, 0);
				}
			}
		}
		// если есть шашки, подлежащие срубу, тога оставляем только самые длинные ходы
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

	// удаляем запрещенные ходы, оставляем только самую(ые) длинную(ые) цепочку(и) ходов.
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
	
	
	// для заданной шашки строит дерево взятия вражеских шашек
	private void getCutsTree(int i, int j, Moves move, int[][] field, int depth) {
		int[][] myfield;
		
		move.setI(i);
		move.setJ(j);
		move.setDepth(depth);
		maxDepth = (depth > maxDepth) ? depth : maxDepth;
		
		//проверяем сверху
		if (canCut(field, i, j-1, 1)) {
			// копируем поле field и вносим изменения
			myfield = field.clone();
			for (int k = 0; k < field.length; k++) {
				myfield[k] = field[k].clone();
			}
			myfield[i][j] = 0;
			myfield[i][j-1] = 0;
			myfield[i][j-2] = curr_move;
			
			// добавляем информацию о возможном ходе и вызываем рекурсию
			move.getPoint().add(new Point());
			move.getPoint().get(move.getN()).setX(i);
			move.getPoint().get(move.getN()).setY(j-2);
			move.getNext().add(new Moves());
			
			//Log.i("moves_tree", "checker wint coord: "+move.getI()+" "+move.getJ()+" - can CUT up. depth: "+move.getDepth());
			
			getCutsTree(i, j-2, move.getNext().get(move.getN()), myfield, depth+1);
			move.increaseN();
			cut = true;
		}
		//проверяем снизу
		if (canCut(field, i, j+1, 2)) {
			// копируем поле field и вносим изменения
			myfield = field.clone();
			for (int k = 0; k < field.length; k++) {
				myfield[k] = field[k].clone();
			}
			myfield[i][j] = 0;
			myfield[i][j+1] = 0;
			myfield[i][j+2] = curr_move;
			
			// добавляем информацию о возможном ходе и вызываем рекурсию
			move.getPoint().add(new Point());
			move.getPoint().get(move.getN()).setX(i);
			move.getPoint().get(move.getN()).setY(j+2);
			move.getNext().add(new Moves());
			
			//Log.i("moves_tree", "checker wint coord: "+move.getI()+" "+move.getJ()+" - can CUT down. depth: "+move.getDepth());
			
			getCutsTree(i, j+2, move.getNext().get(move.getN()), myfield, depth+1);
			move.increaseN();
			cut = true;	
		}
		//проверяем слева
		if (canCut(field, i-1, j, 3)) {
			// копируем поле field и вносим изменения
			myfield = field.clone();
			for (int k = 0; k < field.length; k++) {
				myfield[k] = field[k].clone();
			}
			myfield[i][j] = 0;
			myfield[i-1][j] = 0;
			myfield[i-2][j] = curr_move;
			
			// добавляем информацию о возможном ходе и вызываем рекурсию
			move.getPoint().add(new Point());
			move.getPoint().get(move.getN()).setX(i-2);
			move.getPoint().get(move.getN()).setY(j);
			move.getNext().add(new Moves());
			
			//Log.i("moves_tree", "checker wint coord: "+move.getI()+" "+move.getJ()+" - can CUT left. depth: "+move.getDepth());
			
			getCutsTree(i-2, j, move.getNext().get(move.getN()), myfield, depth+1);
			move.increaseN();
			cut = true;
		}
		//проверяем справа
		if (canCut(field, i+1, j, 4)) {
			// копируем поле field и вносим изменения
			myfield = field.clone();
			for (int k = 0; k < field.length; k++) {
				myfield[k] = field[k].clone();
			}
			myfield[i][j] = 0;
			myfield[i+1][j] = 0;
			myfield[i+2][j] = curr_move;
			
			// добавляем информацию о возможном ходе и вызываем рекурсию
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
	
	// определяем, в каком из 4ех направлений может двигаться шашка
	private void getMovesTree(int i, int j, Moves move){
		// шаг вверх (только для шешек того цвета, которые располагались внизу)
		if ((((curr_move == GameLogic.WHITE) && !inverted) ||
			 ((curr_move == GameLogic.BLACK) && inverted)) && canMove(i, j-1)) {
			move.getPoint().add(new Point());
			move.getPoint().get(move.getN()).setX(i);
			move.getPoint().get(move.getN()).setY(j-1);
			move.increaseN();
			move.getNext().add(new Moves());
			
			//Log.i("moves_tree", "checker wint coord: "+i+" "+j+" - can go up");
		}
		// шаг вниз (только для шешек того цвета, которые располагались вверху)
		if ((((curr_move == GameLogic.BLACK) && !inverted) ||
			 ((curr_move == GameLogic.WHITE)) && inverted) && canMove(i, j+1)) {
			move.getPoint().add(new Point());
			move.getPoint().get(move.getN()).setX(i);
			move.getPoint().get(move.getN()).setY(j+1);
			move.increaseN();
			move.getNext().add(new Moves());
			
			//Log.i("moves_tree", "checker wint coord: "+i+" "+j+" - can go down");
		}
		//шаг влево
		if (canMove(i-1, j)) {
			move.getPoint().add(new Point());
			move.getPoint().get(move.getN()).setX(i-1);
			move.getPoint().get(move.getN()).setY(j);
			move.increaseN();
			move.getNext().add(new Moves());
			
			//Log.i("moves_tree", "checker wint coord: "+i+" "+j+" - can go left");
		}
		//шаг вправо
		if (canMove(i+1, j)) {
			move.getPoint().add(new Point());
			move.getPoint().get(move.getN()).setX(i+1);
			move.getPoint().get(move.getN()).setY(j);
			move.increaseN();
			move.getNext().add(new Moves());
			
			//Log.i("moves_tree", "checker wint coord: "+i+" "+j+" - can go right");
		}
	}
	
	// проверяет: 1. попадаем ли точка I J на игровое поле. 
	// 2. Находится ли на этой точке вражеская шашка
	// 3. В зависимости от направления предполагаемого сруба (задается параметром k)
	//    определяет, может ли он быть совершен.
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
	
	// проверяет, попадает ли точка с координатами i j на поле, и является ли она свободной
	private boolean canMove(int i , int j){
		if (i > -1 && i < GameLogic.HORIZONTAL_CELL_AMOUNT &&
			j > -1 && j < GameLogic.VERTICAL_CELL_AMOUNT) {
			if (field[i][j] == 0) {
				return true;
			}
		}
		return false;
	}

	// в зависимости от цвета, запрещаем определение ходов для шашек на финальной линии.
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
	
	// вносит изменение в поле при совершении хода.
	// I J - начальные координаты шашки 
	// p - точка назначения
	public void changeField(int I, int J, Point p){
		field[I][J] = 0;
		field[p.getX()][p.getY()] = curr_move;
		
		//определяем был ли это сруб или обычный ход
		boolean isCut = false;		// флаг для сруба
		int diffX = I - p.getX();	// направление возможного сруба
		int diffY = J - p.getY();
		if (diffX == 0) {
			//up
			if (diffY == 2) {
				isCut = true;
				field[I][J-1] = 0;
			}
			//down
			if (diffY == -2) {
				isCut = true;
				field[I][J+1] = 0;
			}
		}
		if (diffY == 0){
			//left
			if (diffX == 2) {
				isCut = true;
				field[I-1][J] = 0;
			}
			//right
			if (diffX == -2) {
				isCut = true;
				field[I+1][J] = 0;
			}
		}
		
		// изменение количества шашек на доске, если был сруб
		if(isCut){
			if (curr_move == GameLogic.WHITE) black_checkers--;
			if (curr_move == GameLogic.BLACK) white_checkers--;
		}
	}
	
	
	// копируем из переданного GameState 4 основыне параметра
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

	// проверяет, не обменялись ли шашки местами друг с другом
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
	
	// получить шашки, находящиеся на последней (для цвета color) линии
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

	// получить шашки, находящиеся на первой (для цвета color) линии
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
