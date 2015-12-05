package ru.ppzh.senegalcheckers;

public class AI_v1 extends PlayerMachine {

	public AI_v1(int color, String who, boolean inverted,
			int analizeDepth) {
		super(color, who, inverted, analizeDepth);
	}

	@Override
	protected void getEstimator(GameState gs, int color) {
		int estimate = Estimate.EndGameEstimate(gs, color);
		estimate = estimate != -1 ? estimate :
				Estimate.BonusPointsEstimate(gs, color) +
			    Estimate.ChackersAmountEstimate(gs, color);
		gs.setEstimate(estimate); 

	}
	
}
