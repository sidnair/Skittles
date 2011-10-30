package skittles.g2;

import java.util.ArrayList;

import skittles.sim.Offer;

/**
 * Keeps track of what people have guessed and tries to analyze what people
 * are interested in getting. 
 */
public class KnowledgeBase {
	
	// TODO - detect messages
	// TODO - see what trades people skipped
	// TODO - account for fact that people might not know color?
	
	// TODO - store stores trades in 'rounds'
	
	// TODO - keep track of trades that are 'bad' (being ignored) in a way that
	// allows us to make better trades
	// TODO - better decay for market preference?
	
	// NOTE - even distributions of skittles are per player
	
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
	
}
