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
	
	private Inventory inventory;
	private ArrayList<PreferenceHistory> playerHistories;
	private ArrayList<Offer> successfulOffers;
	private ArrayList<Offer> unsuccessfulOffers;
	
	private PreferenceHistory marketHistory;
		
	/**
	 * Index of ourselves in the playerTrades ArrayList.
	 */
	private int selfIndex;
	
	public KnowledgeBase(Inventory inventory, int playerCount, int selfIndex) {
		this.inventory = inventory;
		this.successfulOffers = new ArrayList<Offer>();
		this.unsuccessfulOffers = new ArrayList<Offer>();
		this.selfIndex = selfIndex;
		playerHistories = new ArrayList<PreferenceHistory>();
		for (int i = 0; i < playerCount; i++) {
			playerHistories.add(new PreferenceHistory(inventory.getNumColors()));
		}
		marketHistory = new PreferenceHistory(inventory.getNumColors());
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
	
	public double tradeUtility(Offer o) {
		double valueIn = 0.0;
		double valueOut = 0.0;
		
		// what we receive is what they are offering
		int[] in = o.getOffer();
		// what we send is what they want
		int[] out = o.getDesire();
		
		double[] colorValues = inventory.getColorValues();
		
		for(int i = 0; i < in.length; i++) {
			valueIn += colorValues[i] * Math.pow(in[i], 2);
		}
		
		for(int j = 0; j < in.length; j++) {
			valueOut += colorValues[j] * Math.pow(out[j], 2);
		}
		
		return valueIn - valueOut;
	}

	//TODO: calculate the probability that a trade will be accepted
	private double tradeAcceptanceProbability(Offer o) {
		//count how many times a similar trade was accepted
		
		//count how many times a similar trade was rejected
		
		//???, profit
		return 0.0;
	}
	
	//TODO
	private boolean areTradesSimilar(Offer first, Offer second) {
		//use tradeDistance, compare difference to threshold
		return false;
	}
	
	//TODO
	private double tradeDistance(Offer first, Offer second) {
		//normalize counts
		//fuzzy match
		return 0.0;
	}
	
}
