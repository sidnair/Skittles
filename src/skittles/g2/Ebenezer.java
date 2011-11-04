
package skittles.g2;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

import skittles.sim.Offer;
import skittles.sim.Player;

public class Ebenezer extends Player {

	public static final boolean DEBUG = true;

	private int numPlayers;
	private String className;
	private int playerIndex;
	private Mouth mouth;
	private Sense sense;
	private KnowledgeBase kb;
	private Inventory inventory;
	private Offer ourOffer;
	
	@Override
	public void initialize(int intPlayerNum, int intPlayerIndex,
			String strClassName, int[] aintInHand) {
		this.playerIndex = intPlayerIndex;
		this.className = strClassName;
		this.numPlayers = intPlayerNum;
		inventory = new Inventory(aintInHand);
		mouth = new Mouth();
		sense = new Sense();
	}

	@Override
	public void eat(int[] aintTempEat) {
		PriorityQueue<Skittle> untasted = inventory.untastedSkittlesByCount();
		if (!untasted.isEmpty()) {
			Skittle next = untasted.remove();
			next.setTasted();
			aintTempEat[next.getColor()] = 1;
			mouth.put(next, 1);
			return;
		}
		PriorityQueue<Skittle> highestNegative =
				inventory.leastNegativeSkittles();
		if (!highestNegative.isEmpty()) {
			Skittle next = highestNegative.remove();
			next.setTasted();
			aintTempEat[next.getColor()] = 1;
			mouth.put(next, 1);
			return;
		}
		PriorityQueue<Skittle> skittlesByValuesLowest =
				inventory.skittlesByValuesLowest();
		Skittle next = skittlesByValuesLowest.remove();
		next.setTasted();
		aintTempEat[next.getColor()] = next.getCount();
		mouth.put(next, next.getCount());
	}
	
	public void offer(Offer offTemp) {
		makeOffer(offTemp);
	}

	/**
	 * Given an 0-for-0 Offer object, mutate it to the offer we want to put on the table.
	 * @param offTemp the offer reference from the simulator
	 */
	public void makeOffer(Offer offTemp) {
		if (inventory.tastedSkittlesByCount().isEmpty()) {
			return;
		}

		ArrayList<Skittle> tastedSkittles = new ArrayList<Skittle>();
		for (Skittle s : inventory.getSkittles()) {
			if (s.isTasted()) {
				tastedSkittles.add(s);
			}
		}

		// sort skittles by how much we like their color
		Collections.sort(tastedSkittles, new Comparator<Skittle>() {
			@Override
			public int compare(Skittle first, Skittle second) {
				double diff = first.getValue() - second.getValue();
				if (diff > 0) {
					return -1;
				} else if (diff == 0) {
					return 0;
				} else {
					return 1;
				}
			}
		});
		if(DEBUG) {
			System.out.println("\ntasted:");
			for(Skittle s: tastedSkittles) System.out.println(s.toString());
		}

		// the two sides of our new offer
		int[] toOffer = new int[inventory.getSkittles().length];
		int[] toReceive = new int[inventory.getSkittles().length];

		//if we haven't tasted more than 3 colors, make a null offer. (0-for-0)
		if (tastedSkittles.size() < 3) {
			offTemp.setOffer(toOffer, toReceive);
			return;
		}
		
		//if we've tasted more than two colors, get our favorite one
		Skittle wantedColor = tastedSkittles.size() > 2 ? tastedSkittles.get(0) : null;
		Skittle unwantedColor = null;
		
		double[] marketPrefs = kb.getMarketPreferences();
		double currentMarketValue = Double.NEGATIVE_INFINITY;
		
		// TODO: move to KnowledgeBase
		// starting with third-best color, find the color with the highest market value.
		// set it as our unwanted color.
		for (int i = 2; i < tastedSkittles.size(); i++) {
			double newMarketValue = marketPrefs[tastedSkittles.get(i).getColor()];
			if (newMarketValue > currentMarketValue) {
				unwantedColor = tastedSkittles.get(i);
				currentMarketValue = newMarketValue;
			}
		}

		// if we know what color we want AND what color we don't want,
		// set the offer to SEND unwantedColor and RECEIVE wantedColor		
		// at this point:
		// unwantedColor is the highest-market-value color that is not one of our top two
		// wantedColor is the color with the highest value
		if (unwantedColor != null && wantedColor != null) {
			// TODO: why are we calculating count like this?
			// TODO: make offers of mixed colors
			int count = (int) Math.min(
					Math.ceil(unwantedColor.getCount() / 5.0),
					Math.ceil(wantedColor.getCount() / 5.0));
			toOffer[unwantedColor.getColor()] = count;
			toReceive[wantedColor.getColor()] = count;
			offTemp.setOffer(toOffer, toReceive);
		}
		
		// This is a hack for the meantime because we cannot update if we pick
		// our own offer.
		ourOffer = offTemp;
	}

	@Override
	public void happier(double dblHappinessUp) {
		if (mouth.skittleInMouth.getValue() == Skittle.UNDEFINED_VALUE) {
			double utility = sense.getIndividualHappiness(dblHappinessUp,
					mouth.howMany);
			mouth.skittleInMouth.setValue(utility);
		}
	}

	@Override
	public Offer pickOffer(Offer[] currentOffers) {
		// We can't get the number of players another way...
		if (kb == null) {
			kb = new KnowledgeBase(currentOffers.length, playerIndex,
					inventory.getSkittles().length);
		}
		
		ArrayList<Offer> trades = new ArrayList<Offer>();
		for (Offer o : currentOffers) {
			if (o.getOfferLive() && canTake(o)) {
				trades.add(o);
			}
		}
		
		if(trades.size() == 0) {
			return null;
		}
		
		//sort trades by their utility (computed by tradeUtility())
		Collections.sort(trades, new Comparator<Offer>() {
			@Override
			public int compare(Offer first, Offer second) {
				double diff = tradeUtility(first) - tradeUtility(second);
				if (diff > 0) {
					return -1;
				} else if (diff == 0) {
					return 0;
				} else {
					return 1;
				}
			}
		});
		
		Offer bestTrade = trades.get(0);
		double bestTradeUtility = tradeUtility(bestTrade);
		if(DEBUG) {
			for(Offer t: trades) {
				System.out.println(t.toString()+" = "+tradeUtility(t));
			}
			System.out.println("bestTrade: " + bestTrade.toString() + " = " + 
						bestTradeUtility);
		}
		if(bestTrade != null && bestTradeUtility > 0) {
			int[] desiredSkittles = bestTrade.getDesire();
			int[] offeredSkittles = bestTrade.getOffer();
			
			// This is a hack for the meantime because we cannot update if we
			// pick our own offer.
			for (int i = 0; i < inventory.size(); i++) {
				if (ourOffer != null && !ourOffer.equals(bestTrade)){
					inventory.getSkittle(i).updateCount(
							offeredSkittles[i] - desiredSkittles[i]);
				}
			}
			
			return bestTrade;
		}
		
		return null;
	}
	
	private double tradeUtility(Offer o) {
		// TODO: compute the utility of a trade
		double valueIn = 0.0;
		double valueOut = 0.0;
		
		// what we receive is what they are offering
		int[] in = o.getOffer();
		// what we send is what they want
		int[] out = o.getDesire();
		
		for(int i = 0; i < in.length; i++) {
			valueIn += inventory.getSkittle(i).getValue() * Math.pow(in[i], 2);
		}
		
		for(int j = 0; j < in.length; j++) {
			valueOut += inventory.getSkittle(j).getValue() * Math.pow(out[j], 2);
		}
		
		return valueIn - valueOut;
	}

	private boolean canTake(Offer o) {
		int[] offered = o.getOffer();
		int[] desired = o.getDesire();
		
		if(Arrays.equals(offered, desired)) {
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
		for (Offer o : aoffCurrentOffers) {
			if (o.getPickedByIndex() > -1) {
				kb.storeSelectedTrade(o);
			} else {
				kb.storeUnselectedTrade(o);
			}
		}
	}

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public int getPlayerIndex() {
		return playerIndex;
	}

	// For debug mode apparently
	@Override
	public void syncInHand(int[] aintInHand) {
		// TODO Auto-generated method stub
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
}
