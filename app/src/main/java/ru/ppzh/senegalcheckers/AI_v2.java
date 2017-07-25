package ru.ppzh.senegalcheckers;

public class AI_v2 extends PlayerMachine {

	public AI_v2(int color, String who, boolean inverted,
			int analyzeDepth) {
		super(color, who, inverted, analyzeDepth);
	}

	@Override
	protected void getEstimator(GameState gs, int color) {
		int estimate = Estimate.BonusCheckersEstimateNewWithEnemyCheckers(gs, color, -50)+
			       	   Estimate.CheckersAmountEstimateNew(gs, color)+
			       	   Estimate.EndGameEstimateNew(gs, color);
	
		gs.setEstimate(estimate);
	}

	
	
}
