
package skittles.g2;

import java.util.ArrayList;

import skittles.sim.Game;
import skittles.sim.Offer;

/**
 * Keeps track of what people have guessed and tries to analyze what people are
 * interested in getting.
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


	private double[][] estimatedCount;
	private int turn;
	private enum STAGE {DISCOVERY, HOARD, END}
	private STAGE[] playerStage;

	private PreferenceHistory marketHistory;

	/**
	 * Each relative want: rows are the things that they would gain.
	 * Columns are the things they are giving up.
	 */
	private ArrayList<double[][]> relativeWants;
	
	private int playerCount;
	
	private final static double WANTS_NEW_WEIGHT = 0.5;
		
	/**
	 * Index of ourselves in the playerTrades ArrayList.
	 */
	private int selfIndex;

	public KnowledgeBase(Inventory inventory, int playerCount, int selfIndex) {
		this.playerCount = playerCount;
		playerStage = new STAGE[playerCount];
		for (STAGE s: playerStage) {
			s = STAGE.DISCOVERY;
		}
		this.inventory = inventory;
		this.successfulOffers = new ArrayList<Offer>();
		this.unsuccessfulOffers = new ArrayList<Offer>();
		this.selfIndex = selfIndex;
		this.playerCount = playerCount;
		playerHistories = new ArrayList<PreferenceHistory>();
		for (int i = 0; i < playerCount; i++) {
			playerHistories.add(new PreferenceHistory(inventory.getNumColors()));
		}

		relativeWants = getRelativeWants();
		marketHistory = new PreferenceHistory(inventory.getNumColors());

		// For counting the players
		int avgCount = inventory.getStartingSkittles() / inventory.size();
		estimatedCount = new double[playerCount][inventory.size()];
		for (int j = 0; j < playerCount; j++) {
			this.estimatedCount[j] = new double[inventory.size()];
			for (int i = 0; i < inventory.size(); i++) {
				estimatedCount[j][i] = avgCount;
			}
		}
		this.turn = 0;
	}

	public void storeUnselectedTrade(Offer offer) {
		unsuccessfulOffers.add(offer);
		
		int proposer = offer.getOfferedByIndex();
		playerHistories.get(proposer).addUnsuccessfulTrade(offer.getOffer(), offer.getDesire());
		if (proposer != selfIndex) {
			marketHistory.addUnsuccessfulTrade(offer.getOffer(), offer.getDesire());
		}
	}

	
	// giving is what they are giving
	// taking is what they are taking
	public double getOtherHappiness(int[] giving, int[] taking, 
			int playerIndex) {
		double[][] desires = relativeWants.get(playerIndex);
		double[] takingRatios = getRatios(taking);
		double value = 0;

		for (int i = 0; i < inventory.getNumColors(); i++) {
			if (giving[i] == 0) {
				continue;
			}
			for (int j = 0; j < inventory.getNumColors(); j++) {
				if (takingRatios[j] == 0) {
					continue;
				}
				value += giving[i] * takingRatios[j] * desires[i][j];
			}
		}
		return value;
	}
	
	private ArrayList<double[][]> getRelativeWants() {
		ArrayList<double[][]> relativeWants =
				new ArrayList<double[][]>(playerCount); 
		for (int i = 0; i < playerCount; i++) {
			int skittleCount = inventory.getNumColors();
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
		
		for (Offer o : offers) {
			if (o.getPickedByIndex() != -1) {
				for (int i : o.getDesire()) {
					if (i > 0) {
						getOtherHappiness(o.getDesire(), o.getOffer(),
								o.getPickedByIndex());
						break;
					}
				}
			}
		}
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
				if (gainedRatios[i] != 0 && givenRatios[j] != 0) {
					playerWants[j][i] += gainedRatios[i] / givenRatios[j];
					playerWants[i][j] -= gainedRatios[i] / givenRatios[j];
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
				marketHistory.addSuccessfulTrade(offer.getOffer(), offer.getDesire());
			}
			if (selector != selfIndex) {
				marketHistory.addSuccessfulTrade(offer.getDesire(), offer.getOffer());
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
	public Skittle getHighestMarketValueColorFrom(int start, ArrayList<Skittle> tastedSkittles) {
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

		for (int i = 0; i < in.length; i++) {
			valueIn += colorValues[i] * Math.pow(in[i], 2);
		}

		for (int j = 0; j < in.length; j++) {
			valueOut += colorValues[j] * Math.pow(out[j], 2);
		}

		return valueIn - valueOut;
	}

	// TODO: calculate the probability that a trade will be accepted
	public double tradeAcceptanceProbability(Offer o) {
		// Sid's model

		// ???, profit
		return 0.0;
	}

	// TODO
	public double countProbability(int count, int color, int player) {
		// p players, c colors, n skittles per player

		return 0.0;
	}

	public void updateCountByTurn() {
		if (turn == 0) {
			for (int j = 0; j < playerCount; j++) {
				for (int i = 0; i < inventory.size(); i++) {
					if (estimatedCount[j][i] > 0) {
						estimatedCount[j][i] -= 1;
					}
				}
			}
			return;
		}
		if (turn > inventory.size()) {
			for (STAGE s: playerStage) {
				if (s == STAGE.DISCOVERY) {
					s = STAGE.HOARD;
				}
			}
		}	
			for (int j = 0; j < playerCount; j++) {
				if (playerStage[j] == STAGE.HOARD) {
					int zeroCount = 0;
					for (int i = 0; i < inventory.size(); i++) {
						if (estimatedCount[j][i] <= 0) {
							zeroCount++;
						}
					}
					for (int i = 0; i < inventory.size(); i++) {
						if (estimatedCount[j][i] > 0) {
							estimatedCount[j][i] -= 1.0 / (inventory.size() - zeroCount);
						}
					}
				}
				if (playerStage[j] == STAGE.END) {
					;
					//The heuristic of not trading means eating doesn't work.. rethinking
				}
			}
		turn++;
	}

	public void updateCountByOffer(Offer o) {
		int proposer = o.getOfferedByIndex();
		int selector = o.getPickedByIndex();
		for (int i = 0; i < inventory.size(); i++) {
			estimatedCount[selector][i] += o.getOffer()[i];
			estimatedCount[proposer][i] -= o.getOffer()[i];
			
			estimatedCount[selector][i] -= o.getDesire()[i];
			estimatedCount[proposer][i] += o.getDesire()[i];

		}
	}
	
	public void printEstimateCount() {
		for (int i = 0; i < estimatedCount.length; i++) {
			System.out.println("Player " + i + " estimate: " + aToS(estimatedCount[i]));
		}
	}
	
	public String aToS(double[] a) {
		String ret = "[  ";
		for (double d: a) {
			ret+= d + "  ";
		}
		ret += "]";
		return ret;
	}
}
