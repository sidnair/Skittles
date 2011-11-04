package skittles.g2;

import java.util.ArrayList;

import scala.actors.threadpool.Arrays;
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
	
	private ArrayList<double[][]> relativeWants;
	
	private int skittleCount;
	private int playerCount;
	
	private final static double WANTS_NEW_WEIGHT = 0.5;
		
	/**
	 * Index of ourselves in the playerTrades ArrayList.
	 */
	private int selfIndex;
	
	
	public KnowledgeBase(int playerCount, int selfIndex, int skittleCount) {
		successfulOffers = new ArrayList<Offer>();
		unsuccessfulOffers = new ArrayList<Offer>();
		this.selfIndex = selfIndex;
		this.playerCount = playerCount;
		this.skittleCount = skittleCount;
		playerHistories = new ArrayList<PreferenceHistory>();
		for (int i = 0; i < playerCount; i++) {
			playerHistories.add(new PreferenceHistory(skittleCount));
		}
		marketHistory = new PreferenceHistory(skittleCount);
		relativeWants = getRelativeWants();
	}
	
	public void storeUnselectedTrade(Offer offer) {
		unsuccessfulOffers.add(offer);
		
		int proposer = offer.getOfferedByIndex();
		playerHistories.get(proposer).addUnsuccessfulTrade(offer.getOffer(),
				offer.getDesire());
		if (proposer != selfIndex) {
			marketHistory.addUnsuccessfulTrade(offer.getOffer(),
					offer.getDesire());
		}
	}
	
	private ArrayList<double[][]> getRelativeWants() {
		ArrayList<double[][]> relativeWants =
				new ArrayList<double[][]>(playerCount); 
		for (int i = 0; i < playerCount; i++) {
			relativeWants.add(new double[skittleCount][skittleCount]);
		}
		return relativeWants;
	}
	
	public void updateRelativeWants(Offer[] offers) {
		ArrayList<double[][]> tempRelativeWants = getRelativeWants();
		for (Offer o : offers) {
			// Skip ignored trades for now.
			if (o.getPickedByIndex() == -1) {
				continue;
			}
			int proposer = o.getOfferedByIndex();
			int selector = o.getPickedByIndex();
			addRelativeWants(tempRelativeWants, proposer, o.getDesire(),
					o.getOffer());
			addRelativeWants(tempRelativeWants, selector, o.getOffer(),
					o.getDesire());
		}
		mergeWants(tempRelativeWants);
		
//		System.out.println(Arrays.deepToString(relativeWants.get(selfIndex)));
	}
	
	private void mergeWants(ArrayList<double[][]> tempRelativeWants) {
		for (int i = 0; i < tempRelativeWants.size(); i++) {
			double[][] tempArray = tempRelativeWants.get(i);
			double[][] oldArray = relativeWants.get(i);
			for (int j = 0; j < tempArray.length; j++) {
				for (int k = 0; k < tempArray[j].length; k++) {
					oldArray[j][k] = oldArray[j][k] * (1- WANTS_NEW_WEIGHT) +
							tempArray[j][k] * WANTS_NEW_WEIGHT;
				}
			}
		}
	}

	private double[] getRatios(int[] counts) {
		double[] givenRatios = new double[counts.length];
		double sum = 0;
		for (int i : counts) {
			sum += i;
		}
		if (sum == 0) {
			return null;
		}
		for (int i = 0; i < givenRatios.length; i++) {
			givenRatios[i] = counts[i] / sum;
		}
		return givenRatios;
	}
	private void addRelativeWants(ArrayList<double[][]> tempRelativeWants,
			int affectedPlayerIndex, int[] gained, int[] given) {
		double[][] playerWants = tempRelativeWants.get(affectedPlayerIndex);
		
		double[] givenRatios = getRatios(given);
		double[] gainedRatios = getRatios(gained);
		
		if (givenRatios == null || gainedRatios == null) {
			return;
		}
		
		for (int i = 0; i < givenRatios.length; i++) {
			for (int j = 0; j < gainedRatios.length; j++) {
				// TODO - tweak
				if (i == j || gainedRatios[i] == 0 || givenRatios[j] == 0) {
					playerWants[i][j] = 0;	
				} else if (i < j) {
					playerWants[i][j] = -gainedRatios[i] / givenRatios[j];
				} else {
					playerWants[i][j] = gainedRatios[i] / givenRatios[j];
				}
			}
		}
	}

	public void storeSelectedTrade(Offer offer) {
		successfulOffers.add(offer);
		
		int proposer = offer.getOfferedByIndex();
		int selector = offer.getPickedByIndex();
		playerHistories.get(proposer).addUnsuccessfulTrade(offer.getOffer(),
				offer.getDesire());
		playerHistories.get(selector).addUnsuccessfulTrade(offer.getDesire(),
				offer.getOffer());
		// If neither condition is true, the trades will cancel out, so don't
		// bother putting them in.
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
	}
	
	public double[] getMarketPreferences() {
		return marketHistory.getPreferences();
	}
	
	public double[] getPlayerPreferences(int playerId) {
		return playerHistories.get(playerId).getPreferences();
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
