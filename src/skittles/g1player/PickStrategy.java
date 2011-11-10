package skittles.g1player;

import java.util.ArrayList;

import skittles.sim.Offer;

public class PickStrategy {

	public Offer pick(Offer[] aoffCurrentOffers, Infobase info) {
		/* array to keep track of the 'score' of offers.  Scores indicate how much the trade would help/hurt us */
		double[] offerScores = new double[aoffCurrentOffers.length];
		/* keeps track of which offers are giving us goal skittles */
		ArrayList<Integer> offersGainingGoalSkittles = new ArrayList<Integer>();
		ArrayList<Integer> desiredColorList = info.getPriority().getDesiredVector(info);
	    int[] inHand = info.getAintInHand();
		
		
		/* for each offer, analyze how much would be my score increase after taking this offer
		 * calculation done considering only the desired elements
		 */
		for (int j = 0; j < aoffCurrentOffers.length; ++j)
		{
			Offer o = aoffCurrentOffers[j];
			if (o.getOfferLive() && o.getOfferedByIndex() != info.intPlayerIndex)
			{
				double score = 0; // initialize score
				int[] weReceive = o.getOffer();
				int[] weGiveUp = o.getDesire();
				/* for each color, add up how our score would change if we accepted the offer */
				for (int i = 0; i < weReceive.length; ++i)
				{
					if(desiredColorList.contains(i)){
						score += Math.pow((inHand[i]+weReceive[i]),2) * info.getColorHappiness(i);
						score -= Math.pow((inHand[i]+weGiveUp[i]),2) * info.getColorHappiness(i);
					}
//					//score -= weGiveUp[i] * info.getColorHappiness(i);
					/* if it's a skittle we'd be receiving and it's a desired skittle, keep track of it */
					if (weReceive[i] > 0 && desiredColorList.contains(i))
					{
						offersGainingGoalSkittles.add(j);
					}
				}
							
				offerScores[j] = score; // set score for this offer
			}
			else
			{
				offerScores[j] = Double.NEGATIVE_INFINITY;
			}
		}
		
		
		
		
		/* based on analysis, choose best offer (or no offer if none are good) */
		if (offersGainingGoalSkittles.size() == 0)
		{
			// if there are no trades giving us our goal skittle, don't take any trades
			return null;
		}
		else
		{
			int bestIndex = -1; // initialize best offer to non-existent index
			double bestScore = Double.NEGATIVE_INFINITY; // initialize best score to very low score
			for (int i : offersGainingGoalSkittles)
			{
				// if this offer has a better score than the best
				if (offerScores[i] > bestScore && canAffordTrade(aoffCurrentOffers[i].getDesire(), info))
				{
					bestIndex = i;
					bestScore = offerScores[i];
				}
				//if there is a conflict for desired colors i.e same score Pick the which gives u high trade value non desired  skittle
				// round the best score to have proper comparison  or put some range
				if (offerScores[i] == bestScore && canAffordTrade(aoffCurrentOffers[i].getDesire(), info))
				{
					//look for high  trade value
					//keep looking here
				}
				
			}
			
			if (bestIndex != -1)
			{
				/* update the skittles we have */
///				info.updateSkittlesInHand(aoffCurrentOffers[bestIndex], true);

				/* return chosen offer */
				return aoffCurrentOffers[bestIndex];
			}
			else
			{
				return null;
			}
		}		
	}
	
	private boolean canAffordTrade(int[] giving, Infobase info)
	{
		int[] skittlesWeHave = info.getAintInHand();
		for(int i = 0; i < giving.length; ++i)
		{
			if (!(skittlesWeHave[i] >= giving[i]))
				return false;
		}
		return true;
	}
}
