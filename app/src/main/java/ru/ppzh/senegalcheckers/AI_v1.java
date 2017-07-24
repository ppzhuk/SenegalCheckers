package ru.ppzh.senegalcheckers;

public class AI_v1 extends PlayerMachine {

	public AI_v1(int color, String who, boolean inverted,
			int analyzeDepth) {
		super(color, who, inverted, analyzeDepth);
	}

	@Override
	protected void getEstimator(GameState gs, int color) {
		int estimate = Estimate.EndGameEstimate(gs, color);
		estimate = estimate != -1 ? estimate :
				Estimate.BonusPointsEstimate(gs, color) +
			    Estimate.CheckersAmountEstimate(gs, color);
		gs.setEstimate(estimate); 

	}
	
}
