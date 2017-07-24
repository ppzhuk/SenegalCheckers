package ru.ppzh.senegalcheckers;

public class AI_v3 extends PlayerMachine {

	public AI_v3(int color, String who, boolean inverted, int analyzeDepth) {
		super(color, who, inverted, analyzeDepth);
	}

	@Override
	protected void getEstimator(GameState gs, int color) {
		gs.setEstimate(Estimate.BonusPointsEstimate(gs, color)+
					   Estimate.CheckersAmountEstimate(gs, color)+
					   Estimate.GameDurationEstimate(gs, color));
	}

}
