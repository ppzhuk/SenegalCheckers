package ru.ppzh.senegalcheckers;

public class AI_v3 extends PlayerMachine {

	public AI_v3(int color, String who, boolean inverted, int analizeDepth) {
		super(color, who, inverted, analizeDepth);
	}

	@Override
	protected void getEstimator(GameState gs, int color) {
		gs.setEstimate(Estimate.BonusPointsEstimate(gs, color)+
					   Estimate.ChackersAmountEstimate(gs, color)+
					   Estimate.GameDurationEstimate(gs, color));
	}

}
