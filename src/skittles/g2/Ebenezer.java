
package skittles.g2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import skittles.sim.Offer;
import skittles.sim.Player;
import skittles.sim.Skittles;

public class Ebenezer extends Player {

	public static final boolean DEBUG = true;

	private double dblHappiness;
	private int intPlayerNum;
	private String strClassName;
	private int intPlayerIndex;
	private Mouth myMouth;
	private Sense mySense;
	private KnowledgeBase kb;
	private Rainbow rainbow;
	
	@Override
	public void initialize(int intPlayerNum, int intPlayerIndex, String strClassName, int[] aintInHand) {
		this.intPlayerIndex = intPlayerIndex;
		this.strClassName = strClassName;
		this.intPlayerNum = intPlayerNum;
		rainbow = new Rainbow(aintInHand);
		myMouth = new Mouth();
		mySense = new Sense();
		dblHappiness = 0;
		
	}

	@Override
	public void eat(int[] aintTempEat) {
		//Do Something
	}
	
	public void offer(Offer offTemp) {
		if (rainbow.tastedSkittlesByCount().isEmpty()) {
			return;
		}

		ArrayList<Skittle> skittles = new ArrayList<Skittle>();
		for (Skittle s : rainbow.getSkittles()) {
			if (s.isTasted()) {
				skittles.add(s);
			}
		}

		Collections.sort(skittles, new Comparator<Skittle>() {
			@Override
			public int compare(Skittle first, Skittle second) {
				double diff = first.getValue() - second.getValue();
				if (diff > 0) {
					return 1;
				} else if (diff == 0) {
					return 0;
				} else {
					return -1;
				}
			}
		});

		int[] toOffer = new int[rainbow.getSkittles().length];
		int[] toRecieve = new int[rainbow.getSkittles().length];

		if (skittles.size() < 3) {
			offTemp.setOffer(toOffer, toRecieve);
			return;
		}

		Skittle mostFavorite = skittles.size() > 2 ? skittles.get(0) : null;
		Skittle leastFavorite = null;
		double[] marketPrefs = kb.getMarketPreferences();
		double currentMarketValue = Double.NEGATIVE_INFINITY;
		for (int i = 2; i < skittles.size(); i++) {
			double newMarketValue = marketPrefs[skittles.get(i).getIndex()];
			if (newMarketValue > currentMarketValue) {
				leastFavorite = skittles.get(i);
				currentMarketValue = newMarketValue;
			}
		}

		if (leastFavorite != null && mostFavorite != null) {
			int count = (int) Math.min(Math.ceil(leastFavorite.getCount() / 5.0), Math.ceil(mostFavorite.getCount() / 5.0));
			toOffer[leastFavorite.getColor()] = count;
			toRecieve[mostFavorite.getColor()] = count;
			offTemp.setOffer(toOffer, toRecieve);
		}
	}

	@Override
	public void happier(double dblHappinessUp) {
		if (myMouth.skittleInMouth.getValue() == Skittle.UNDEFINED_VALUE) {
			double utility = mySense.getIndividualHappiness(dblHappinessUp, myMouth.howMany);
			myMouth.skittleInMouth.setValue(utility);
		}
	}

	@Override
	public Offer pickOffer(Offer[] currentOffers) {
		// We can't get the number of players another way...
		if (kb == null) {
			kb = new KnowledgeBase(currentOffers.length, intPlayerIndex, rainbow.getSkittles().length);
		}
		// Always pick the first live offer.
		for (Offer o : currentOffers) {
			if (o.getOfferLive() && canTake(o)) {
				int[] desiredSkittles = o.getDesire();
				int[] offeredSkittles = o.getOffer();
				for (int i = 0; i < rainbow.size(); i++) {
					rainbow.getSkittle(i).incCount(offeredSkittles[i] - desiredSkittles[i]);
				}
				return o;
			}
		}
		return null;
	}

	private boolean canTake(Offer o) {
		int[] desired = o.getDesire();
		for (int i = 0; i < desired.length; i++) {
			if (rainbow.getSkittle(i).getCount() < desired[i]) {
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
		for (int i = 0; i < rainbow.size(); i++) {
			rainbow.getSkittle(i).incCount(aintDesire[i]);
			rainbow.getSkittle(i).decCount(aintOffer[i]);
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
		return strClassName;
	}

	@Override
	public int getPlayerIndex() {
		return intPlayerIndex;
	}

	// For debug mode apparently
	@Override
	public void syncInHand(int[] aintInHand) {
		// TODO Auto-generated method stub
	}

	public class Mouth {
		public Skittle skittleInMouth;
		public int howMany;
	}
}
