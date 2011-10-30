
package skittles.g2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import skittles.sim.Offer;
import skittles.sim.Player;

public class Ebenezer extends Player {

	public static final boolean DEBUG = true;

	private Skittle[] rainbow;
	private double dblHappiness;
	private String strClassName;
	private int intPlayerIndex;
	private Mouth myMouth;
	private Sense mySense;

	private SkittleComparatorByCount comparatorByCount = new SkittleComparatorByCount();
	private SkittleComparatorByValue comparatorByValue = new SkittleComparatorByValue();
	private PriorityQueue<Skittle> skittlesToTry;
	private PriorityQueue<Skittle> knownSkittleQueue;
	private KnowledgeBase kb;

	@Override
	public void initialize(int intPlayerIndex, String strClassName, int[] aintInHand) {
		this.intPlayerIndex = intPlayerIndex;
		this.strClassName = strClassName;
		skittlesToTry = new PriorityQueue<Skittle>(10, comparatorByCount);
		knownSkittleQueue = new PriorityQueue<Skittle>(10, comparatorByValue);

		rainbow = new Skittle[aintInHand.length];
		for (int i = 0; i < rainbow.length; i++) {
			rainbow[i] = new Skittle(aintInHand[i], i);
			if (aintInHand[i] > 0) {
				skittlesToTry.add(rainbow[i]);
				System.out.println("Added to try queue " + rainbow[i]);
			}
		}
		myMouth = new Mouth();
		mySense = new Sense();
		dblHappiness = 0;
	}

	@Override
	public void eat(int[] aintTempEat) {
		while (!skittlesToTry.isEmpty() && skittlesToTry.peek().getCount() == 0) {
			skittlesToTry.remove();
		}
		if (!skittlesToTry.isEmpty()) {
			System.out.println("Trying some new skittles");
			Skittle toEat = skittlesToTry.remove();
			aintTempEat[toEat.getColor()] = 1;
			toEat.decCount(1);
			myMouth.skittleInMouth = toEat;
			myMouth.howMany = 1;
			toEat.setTasted();
			return;
		}
		// All unknowns were eaten
		while (!knownSkittleQueue.isEmpty() && knownSkittleQueue.peek().getCount() == 0) {
			knownSkittleQueue.remove();
		}
		if (!knownSkittleQueue.isEmpty()) {
			Skittle toEat = knownSkittleQueue.peek();
			rationOrPigOut(aintTempEat, toEat);
			if (toEat.getCount() == 0) {
				knownSkittleQueue.remove();
			}
			toEat.setTasted();
			return;
		}
		for (Skittle s : rainbow) {
			if (s.getCount() > 0) {
				rationOrPigOut(aintTempEat, s);
			}
		}
	}

	private void rationOrPigOut(int[] aintTempEat, Skittle toEat) {
		if (toEat.getValue() > 0) {
			aintTempEat[toEat.getColor()] = toEat.getCount();
			toEat.decCount(toEat.getCount());
			myMouth.skittleInMouth = toEat;
			myMouth.howMany = toEat.getCount();
		} else {
			aintTempEat[toEat.getColor()] = 1;
			toEat.decCount(1);
			myMouth.skittleInMouth = toEat;
			myMouth.howMany = 1;
		}
	}

	public void offer(Offer offTemp) {
		if (knownSkittleQueue.isEmpty()) {
			return;
		}

		ArrayList<Skittle> skittles = new ArrayList<Skittle>();
		for (Skittle s : rainbow) {
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

		int[] toOffer = new int[rainbow.length];
		int[] toRecieve = new int[rainbow.length];

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
			double utility = mySense.findHappinessForSkittle(dblHappinessUp, myMouth.howMany);
			myMouth.skittleInMouth.setValue(utility);
			if (myMouth.skittleInMouth.getCount() > 0) {
				knownSkittleQueue.add(myMouth.skittleInMouth);
			}
			System.out.println("Found value for " + myMouth.skittleInMouth);
		}
	}

	@Override
	public Offer pickOffer(Offer[] currentOffers) {
		// We can't get the number of players another way...
		if (kb == null) {
			kb = new KnowledgeBase(currentOffers.length, intPlayerIndex, rainbow.length);
		}
		// Always pick the first live offer.
		for (Offer o : currentOffers) {
			if (o.getOfferLive() && canTake(o)) {
				int[] desiredSkittles = o.getDesire();
				int[] offeredSkittles = o.getOffer();
				for (int i = 0; i < rainbow.length; i++) {
					if (offeredSkittles[i] - desiredSkittles[i] > 0) {
						if (!knownSkittleQueue.contains(rainbow[i])) {
							knownSkittleQueue.add(rainbow[i]);
						}
					}
					rainbow[i].incCount(offeredSkittles[i] - desiredSkittles[i]);
				}
				return o;
			}
		}
		return null;
	}

	private boolean canTake(Offer o) {
		int[] desired = o.getDesire();
		for (int i = 0; i < desired.length; i++) {
			if (rainbow[i].getCount() < desired[i]) {
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
		for (int i = 0; i < rainbow.length; i++) {
			if (aintOffer[i] - aintDesire[i] > 0) {
				if (!knownSkittleQueue.contains(rainbow[i])) {
					knownSkittleQueue.add(rainbow[i]);
				}
			}
			rainbow[i].incCount(aintDesire[i]);
			rainbow[i].decCount(aintOffer[i]);
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

	/* Comparator for Skittles By Count */
	public class SkittleComparatorByCount implements Comparator<Skittle> {
		@Override
		public int compare(Skittle x, Skittle y) {
			if (x.getCount() > y.getCount()) {
				return -1;
			}
			if (x.getCount() < y.getCount()) {
				return 1;
			}
			return 0;
		}
	}

	/* Comparator for Skittles PriQueue By Value */
	public class SkittleComparatorByValue implements Comparator<Skittle> {
		@Override
		public int compare(Skittle x, Skittle y) {
			if (x.getValue() < y.getValue()) {
				return -1;
			}
			if (x.getValue() > y.getValue()) {
				return 1;
			}
			return 0;
		}
	}

	public class Mouth {
		public Skittle skittleInMouth;
		public int howMany;
	}
}
