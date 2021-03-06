
package skittles.g2;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

import skittles.sim.Offer;
import skittles.sim.Player;

public class Ebenezer extends Player {

	public static final boolean DEBUG = false;

	private String className;
	private int playerIndex;
	private Mouth mouth;
	private KnowledgeBase kb;
	private Inventory inventory;
	private Offer ourOffer;
	private int numPlayers;

	private Offer[] lastOfferSet;

	@Override
	public void initialize(int numPlayers, double tasteDistMean, int playerIndex, String className, int[] inHand) {
		this.playerIndex = playerIndex;
		this.className = className;
		this.numPlayers = numPlayers;
		inventory = new Inventory(inHand);
		mouth = new Mouth();
		kb = new KnowledgeBase(inventory, numPlayers, playerIndex);
	}

	@Override
	public void eat(int[] toEat) {
		// Update everyone else's count
		kb.updateCountByTurn();
//		kb.printEstimateCount();

		// Prioritize skittles that you haven't tasted before.
		PriorityQueue<Skittle> untasted = inventory.untastedSkittlesByCount();
		if (!untasted.isEmpty()) {
			Skittle next = untasted.remove();
			next.setTasted(true);
			toEat[next.getColor()] = 1;
			mouth.put(next, 1);
			return;
		}
		
		/*
		 * Then, eat negative skittles, starting from the skittle with lowest
		 * absolute value. Doing this allows us to eat multiple low positives
		 * in the future in a bigger group, which should more than offest the
		 * small hit we take from eating these skittles. 
		 */
		PriorityQueue<Skittle> highestNegative = inventory.leastNegativeSkittles();
		if (!highestNegative.isEmpty()) {
			Skittle next = highestNegative.remove();
			next.setTasted(true);
			toEat[next.getColor()] = 1;
			mouth.put(next, 1);
			return;
		}

		PriorityQueue<Skittle> skittlesByValuesLowest =
			inventory.skittlesByValuesLowest();
		Skittle next = skittlesByValuesLowest.peek();
		// if there are no negatives and we've tasted all skittles and there are more than one pile left
		// and we can make a good trade, eat positives one at a time
		if(skittlesByValuesLowest.size() > 1) {
			Offer bestTrade = getOurBestTrade();
			
			if (kb.tradeUtility(bestTrade, true) * kb.tradeCountProbability(bestTrade) > next.getValue()) {
				next.setTasted(true);
				toEat[next.getColor()] = 1;
				mouth.put(next, 1);
				return;
			}
		}
		
		/*
		 * Eat the positive Skittles in groups if we can't make good trades
		 * ...even if we have more than one pile of positive skittles
		 */
		next.setTasted(true);
		toEat[next.getColor()] = next.getCount();
		mouth.put(next, next.getCount());
	}

	/*
	 * Wrapper for making an offer.
	 */
	public void offer(Offer offTemp) {
		// Update the relative wants with the set of offers 
		if (lastOfferSet != null) {
			kb.updateRelativeWants(SuperOffer.makeArr(lastOfferSet, numPlayers));
		}
		lastOfferSet = null;
		Offer bestOffer = getOurBestTrade();
		offTemp.setOffer(bestOffer.getOffer(), bestOffer.getDesire());
		ourOffer = offTemp;
	}

	/**
	 * returns the best trade we can make
	 */
	private Offer getOurBestTrade() {
		Offer offTemp = new Offer(getPlayerIndex(), inventory.getNumColors());
		
		ArrayList<Skittle> sortedSkittles = inventory.getSortedSkittleArray();
		
		ArrayList<Integer> willingToAdd = new ArrayList<Integer>();
		ArrayList<Integer> willingToGive = new ArrayList<Integer>();
		
		for (Skittle s : sortedSkittles) {
			if (s.getHoardingValue() >= 0 || !s.isTasted()) {
				willingToAdd.add(s.getColor());
			}

			// We're willing to give almost anything; the value calculation of 
			// offers will account for the fact that we don't want to give away
			// our best colors. Don't include our hoarding color for pruning.
			if (sortedSkittles.get(0) != s) {
				willingToGive.add(s.getColor());
			}
		}
		
		Offer best = getBestOffer(willingToAdd, willingToGive);
		if (best != null) {
			offTemp.setOffer(best.getOffer(), best.getDesire());
		}
		
		return offTemp;
	}
	
	private boolean isEmpty(int[] offer) {
		for (int i = 0; i < offer.length; i++) {
			if (offer[i] > 0) {
				return false;
			}
		}
		return true;
	}

	public Offer getBestOffer(ArrayList<Integer> willingToAdd,
			ArrayList<Integer> willingToGive) {
		Offer o = null;
		for (int i = 0; i < numPlayers; i++) {
			if (i == playerIndex || !kb.isActive(i)) {
				continue;
			}
			Offer iOffer = kb.getBestOfferPerPlayer(willingToAdd, willingToGive,
					i, playerIndex);
			
			if (o == null || kb.scoreOurOffer(o) < kb.scoreOurOffer(iOffer)) {
				o = iOffer;
			}
		}
		return o;
	}

	@Override
	public void happier(double deltaHappiness) {
		if (mouth.skittleInMouth.getValue() == Skittle.UNDEFINED_VALUE) {
			double utility = inventory.getIndividualHappiness(deltaHappiness, mouth.howMany);
			mouth.skittleInMouth.setValue(utility);
		}
		inventory.updateSkittleRankings();
	}

	@Override
	public Offer pickOffer(Offer[] currentOffers) {

		ArrayList<Offer> potentialTrades = new ArrayList<Offer>();
		for (Offer o : currentOffers) {
			if (o.getOfferLive() && canTake(o)) {
				potentialTrades.add(o);
			}
		}

		if (potentialTrades.size() == 0) {
			return null;
		}

		// sort trades by their utility (computed by tradeUtility())
		Collections.sort(potentialTrades, new Comparator<Offer>() {
			@Override
			public int compare(Offer first, Offer second) {
				// TODO - 
				double diff = kb.tradeUtility(first, true) - kb.tradeUtility(second, true);
				if (diff > 0) {
					return -1;
				} else if (diff == 0) {
					return 0;
				} else {
					return 1;
				}
			}
		});

		Offer bestTrade = potentialTrades.get(0);
		double bestTradeUtility = kb.tradeUtility(bestTrade, true);
		if (DEBUG) {
			for (Offer t : potentialTrades) {
				System.out.println(t.toString() + " = " + kb.tradeUtility(t, true));
			}
			System.out.println("bestTrade: " + bestTrade.toString() + " = " + bestTradeUtility);
		}
		if (bestTrade != null && bestTradeUtility > 0) {
			takeTrade(bestTrade);
			return bestTrade;
		}

		return null;
	}

	/**
	 * update counts based on the trade we're accepting
	 * 
	 * @param bestTrade
	 */
	private void takeTrade(Offer bestTrade) {
		int[] desiredSkittles = bestTrade.getDesire();
		int[] offeredSkittles = bestTrade.getOffer();

		for (int i = 0; i < inventory.size(); i++) {
			inventory.getSkittle(i).updateCount(offeredSkittles[i] - desiredSkittles[i]);
		}
	}

	private boolean canTake(Offer o) {
		if (ourOffer != null && o.equals(ourOffer)) {
			return false;
		}

		int[] offered = o.getOffer();
		int[] desired = o.getDesire();

		if (Arrays.equals(offered, desired)) {
			return false;
		}

		for (int i = 0; i < desired.length; i++) {
			if (inventory.getSkittle(i).getCount() < desired[i]) {
				return false;
			}
		}
		return true;
	}

	// Someone pick the offer
	@Override
	public void offerExecuted(Offer offPicked) {
		int[] aintOffer = offPicked.getOffer();
		int[] aintDesire = offPicked.getDesire();
		for (int i = 0; i < inventory.size(); i++) {
			inventory.getSkittle(i).updateCount(aintDesire[i] - aintOffer[i]);
		}
	}

	@Override
	public void updateOfferExe(Offer[] aoffCurrentOffers) {
		lastOfferSet = aoffCurrentOffers;
		for (Offer o : aoffCurrentOffers) {
			if (o.getPickedByIndex() > -1) {
				kb.storeSelectedTrade(o);
				kb.updateCountByOffer(o);
			} else {
				kb.storeUnselectedTrade(o);
			}
		}
	}

	public class Mouth {
		public void put(Skittle s, int h) {
			this.skittleInMouth = s;
			this.howMany = h;
			s.updateCount(-h);
		}

		public Skittle skittleInMouth;
		public int howMany;
	}
	

	// Unused methods.
	
	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public int getPlayerIndex() {
		return playerIndex;
	}

	@Override
	public void syncInHand(int[] aintInHand) {
		// TODO Auto-generated method stub
	}

}
