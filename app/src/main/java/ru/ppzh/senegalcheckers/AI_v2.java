package ru.ppzh.senegalcheckers;

public class AI_v2 extends PlayerMachine {

	public AI_v2(int color, String who, boolean inverted,
			int analizeDepth) {
		super(color, who, inverted, analizeDepth);
	}

	@Override
	protected void getEstimator(GameState gs, int color) {
		int estimate = Estimate.BonusCheckersEstimateNewWithEnemyCheckers(gs, color, 0)+
			       	   Estimate.ChackersAmountEstimateNew(gs, color)+
			       	   Estimate.EndGameEstimateNew(gs, color);
	
		gs.setEstimate(estimate);
	}

	
	
}
