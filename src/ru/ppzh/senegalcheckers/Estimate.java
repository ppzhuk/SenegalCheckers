package ru.ppzh.senegalcheckers;

import android.util.Log;

public class Estimate {
	private static final String TAG = "Estimate";

	//----AI_v1-----------------------------------------
	// Return the amount of checkers of appropriate color.
	public static int ChackersAmountEstimate(GameState gameState, int color){
		return color == GameLogic.WHITE ? gameState.getWhite_checkers() : gameState.getBlack_checkers();
	}
	
	// Estimate based on bonus checkers.
	public static int BonusPointsEstimate(GameState gameState, int color){	
		return gameState.getOnFinalLine(color)*5;
	}


	public static int EndGameEstimate(GameState gs, int color){	
		if (gs.getMovesAmount() == 0 || gs.checkersExchanged()) {
			int winner = gs.defineWinner();
			if (winner == color) {
				return 100000;
			} else {
				return winner != GameLogic.DRAW ? -99999 : 50000;
			}
		}
		return -1;
	}
	
	//----AI_v2-----------------------------------------
	public static int ChackersAmountEstimateNew(GameState gameState, int color){
		int estimate = 0;
		switch (color) {
		case GameLogic.WHITE:
			estimate = gameState.getWhite_checkers()*1000 + 7000 - gameState.getBlack_checkers()*500;
			break;
		case GameLogic.BLACK:
			estimate = gameState.getBlack_checkers()*1000 + 7000 - gameState.getWhite_checkers()*500;
			break;
		}
		return estimate;
	}
	
	// Estimate based on bonus checkers (new variant).
	public static int BonusCheckersEstimateNew(GameState gameState, int color){	
		int estimate = 0;
		boolean inverted = gameState.isInverted();
		int[][] field = gameState.getField();
		int j;
		if (!inverted) {
			j = 0;
			if (color == GameLogic.BLACK) j = GameLogic.VERTICAL_CELL_AMOUNT-1;	
		} else {
			j = GameLogic.VERTICAL_CELL_AMOUNT-1;
			if (color == GameLogic.BLACK) j = 0;	
		}
		int enemyCheckersAmount = gameState.getOnFirstLine(color == GameLogic.BLACK ? GameLogic.WHITE : GameLogic.BLACK);
		boolean isEnemyNotAtFirstLine = enemyCheckersAmount == 0 ? true : false;
		for (int i = 0; i < GameLogic.HORIZONTAL_CELL_AMOUNT; i++) {
			if (field[i][j] == color) {
				if (isEnemyNotAtFirstLine || i == 0 || i == GameLogic.HORIZONTAL_CELL_AMOUNT-1 ||
					(i < GameLogic.HORIZONTAL_CELL_AMOUNT-1 && field[i+1][j] == color) || 
					(i > 0 && field[i-1][j] == color)) {
					estimate += 600;
				} else {
					estimate -= 900;
				}
			}
		}
		
		return estimate;
	}


	public static int EndGameEstimateNew(GameState gs, int color){	
		if (gs.getMovesAmount() == 0 || gs.checkersExchanged()) {
			int winner = gs.defineWinner();
			if (winner == color) {
				return 1000000;
			} else {
				return winner != GameLogic.DRAW ? -1000000 : 0;
				// These values give us AI which: 1 -  always avoid to lose.
                // 2 - always tries to win and choose draw only if alternative is lose.
			}
		}
		return 0;
	}
	
		// Estimate based on bonus checkers (with enemy checkers).
    public static int BonusCheckersEstimateNewWithEnemyCheckers(GameState gameState, int color, int tax){	
        int estimate = 0;
        boolean inverted = gameState.isInverted();
        int[][] field = gameState.getField();
        int ourLastLine;
        int enemyLastLine;
        
        if (!inverted) {
            ourLastLine = 0; 
            enemyLastLine = GameLogic.VERTICAL_CELL_AMOUNT-1;
            if (color == GameLogic.BLACK) {
                ourLastLine = GameLogic.VERTICAL_CELL_AMOUNT-1;
                enemyLastLine = 0;
            } 	
        } else {
            ourLastLine = GameLogic.VERTICAL_CELL_AMOUNT-1;
            enemyLastLine = 0;
            if (color == GameLogic.BLACK) {
                ourLastLine = 0;
                enemyLastLine = GameLogic.VERTICAL_CELL_AMOUNT-1;
            } 	
        }
        
        int enemyCheckersAmountAtOurLastLine = gameState.getOnFirstLine(color == GameLogic.BLACK ? GameLogic.WHITE : GameLogic.BLACK);
        boolean isEnemyAtOurLastLine = enemyCheckersAmountAtOurLastLine == 0 ? false : true;
        int ourCheckersAmountAtOurFirstLine = gameState.getOnFirstLine(color);
        boolean isOurNotAtOurFirstLine = ourCheckersAmountAtOurFirstLine == 0 ? true : false;
        
        int enemyColor = color == GameLogic.BLACK ? GameLogic.WHITE : GameLogic.BLACK;
        
        for (int i = 0; i < GameLogic.HORIZONTAL_CELL_AMOUNT; i++) {
            if (field[i][ourLastLine] == color) {
                if (!isEnemyAtOurLastLine || i == 0 || i == GameLogic.HORIZONTAL_CELL_AMOUNT-1 ||
                    (i < GameLogic.HORIZONTAL_CELL_AMOUNT-1 && field[i+1][ourLastLine] == color) || 
                    (i > 0 && field[i-1][ourLastLine] == color)) {
                    estimate += 600;
                } else {
                    estimate -= 900;
                }
            }
            if (field[i][enemyLastLine] == enemyColor) {
                if (isOurNotAtOurFirstLine || i == 0 || i == GameLogic.HORIZONTAL_CELL_AMOUNT-1 ||
                    (i < GameLogic.HORIZONTAL_CELL_AMOUNT-1 && field[i+1][enemyLastLine] == enemyColor) || 
                    (i > 0 && field[i-1][enemyLastLine] == enemyColor)) {
                    estimate -= 1100;
                } else {
                    // Reaction to undefended bonus checkers:
                    // 0 - often checkers are not allowed, except rare cases. Acceptable for the average difficulty.
                    // -50 -  all checkers are not allowed. Acceptable for the hard difficulty.
                    estimate -= tax;
                }
            }
        }
        
        return estimate;
    }
	//----AI_v3---------------------------------------------
	private static int[] defineArray(int moves_amount) {
		int[] arr = new int[] {0, 1, 2, 3, 4};
		if (moves_amount >=20 && moves_amount < 40) {
			arr[0] = 1;
			arr[1] = 2;
			arr[2] = 3;
			arr[3] = 4;
			arr[4] = 4;
		}
		if (moves_amount >=40 && moves_amount < 60) {
			arr[0] = 2;
			arr[1] = 3;
			arr[2] = 4;
			arr[3] = 4;
			arr[4] = 3;		
		}
		if (moves_amount >=60 && moves_amount < 80) {
			arr[0] = 3;
			arr[1] = 4;
			arr[2] = 4;
			arr[3] = 3;
			arr[4] = 2;		
		}
		if (moves_amount >=80 && moves_amount < 100) {
			arr[0] = 4;
			arr[1] = 4;
			arr[2] = 3;
			arr[3] = 2;
			arr[4] = 1;		
		}
		if (moves_amount >=100) {
			arr[0] = 4;
			arr[1] = 3;
			arr[2] = 2;
			arr[3] = 1;
			arr[4] = 0;		
		}
		return arr;
	}
	
	private static int getSum(int[] arr, int[][] field, int color) {
		int sum = 0;
		for (int j = 0; j < 5; ++j) {
			for (int i = 0; i < 6; ++i) {
				if (field[i][j] == color) {
					sum += (arr[j]);
				}
			}
		}
		return sum;
	}

	public static int GameDurationEstimate(GameState gameState, int color) {
		GameLogic gl = GameLogic.getGameLogic();
		try {
			if (gl == null)
				throw new Exception();
			int moves = gl.getMovesAmount();
			int[] arr = defineArray(moves);
			int[][] field = gameState.getField();
			boolean inverted = gameState.isInverted();
			if ((!inverted && color == GameLogic.BLACK) || (inverted && color == GameLogic.WHITE)) {
				return getSum(arr, field, color);
			} else {
				int[] inverted_array = new int[5];
				for (int i = 4; i >= 0; --i) {
					inverted_array[4-i] = arr[i];
				}
				return getSum(inverted_array, field, color);
			}		
		} catch (Exception e) {
			Log.e(TAG, "GameLogic is null.");
		} finally {
			return 0;
		}
		
	}
	
}
