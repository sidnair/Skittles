package skittles.g2;

import java.util.ArrayList;

import skittles.sim.Offer;

/**
 * Keeps track of what people have guessed and tries to analyze what people
 * are interested in getting. 
 */
public class KnowledgeBase {
		
	private ArrayList<PreferenceHistory> playerHistories;
	private ArrayList<Offer> successfulOffers;
	private ArrayList<Offer> unsuccessfulOffers;
	private PreferenceHistory marketHistory;
		
	/**
	 * Index of ourselves in the playerTrades ArrayList.
	 */
	private int selfIndex;
	
	
	public KnowledgeBase(int playerCount, int selfIndex, int skittleCount) {
		successfulOffers = new ArrayList<Offer>();
		unsuccessfulOffers = new ArrayList<Offer>();
		this.selfIndex = selfIndex;
		playerHistories = new ArrayList<PreferenceHistory>();
		for (int i = 0; i < playerCount; i++) {
			playerHistories.add(new PreferenceHistory(skittleCount));
		}
		marketHistory = new PreferenceHistory(skittleCount);
	}
	
	public void storeUnselectedTrade(Offer offer) {
		int proposer = offer.getOfferedByIndex();
		playerHistories.get(proposer).addUnsuccessfulTrade(offer.getOffer(),
				offer.getDesire());
		if (proposer != selfIndex) {
			marketHistory.addUnsuccessfulTrade(offer.getOffer(),
					offer.getDesire());
		}
		unsuccessfulOffers.add(offer);
	}
	
	public void storeSelectedTrade(Offer offer) {
		int proposer = offer.getOfferedByIndex();
		int selector = offer.getPickedByIndex();
		
		playerHistories.get(proposer).addUnsuccessfulTrade(offer.getOffer(),
				offer.getDesire());
		playerHistories.get(selector).addUnsuccessfulTrade(offer.getDesire(),
				offer.getOffer());
		// If neither condition is true, the trades will cancel out. 
		if (proposer == selfIndex || selector == selfIndex) {
			if (proposer != selfIndex) {
				marketHistory.addSuccessfulTrade(offer.getOffer(),
						offer.getDesire());
			}
			if (selector != selfIndex) {
				marketHistory.addSuccessfulTrade(offer.getDesire(),
						offer.getOffer());
			}			
		}
		successfulOffers.add(offer);
	}
	
	public double[] getMarketPreferences() {
		return marketHistory.getPreferences();
	}
	
	// i is player id
	public double[] getPlayerPreferences(int i) {
		return playerHistories.get(i).getPreferences();
	}

	/**
	 * @param tastedSkittles
	 * @return
	 */
	public Skittle getHighestMarketValueColorFrom(
			int start,
			ArrayList<Skittle> tastedSkittles) {
		Skittle unwantedColor = null;
		double[] marketPrefs = this.getMarketPreferences();
		double currentMarketValue = Double.NEGATIVE_INFINITY;
		double newMarketValue = 0.0;
		
		for (int i = start; i < tastedSkittles.size(); i++) {
			newMarketValue = marketPrefs[tastedSkittles.get(i).getColor()];
			if (newMarketValue > currentMarketValue) {
				unwantedColor = tastedSkittles.get(i);
				currentMarketValue = newMarketValue;
			}
		}
		return unwantedColor;
	}
	
}
