
package skittles.g2;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

import skittles.sim.Offer;
import skittles.sim.Player;

public class Ebenezer extends Player {

	public static final boolean DEBUG = true;

	Skittle[] rainbow;
	double dblHappiness;
	String strClassName;
	int intPlayerIndex;
	Mouth myMouth;
	Sense mySense;

	SkittleComparatorByCount comparatorByCount = new SkittleComparatorByCount();
	SkittleComparatorByValue comparatorByValue = new SkittleComparatorByValue();
	PriorityQueue<Skittle> skittlesToTry;
	PriorityQueue<Skittle> knownSkittleQueue;

	@Override
	public void initialize(int intPlayerIndex, String strClassName, int[] aintInHand) {
		System.out.println(Ebenezer.class.toString() + " is index: " + intPlayerIndex);
		skittlesToTry = new PriorityQueue<Skittle>(10, comparatorByCount);
		knownSkittleQueue = new PriorityQueue<Skittle>(10, comparatorByValue);
		this.intPlayerIndex = intPlayerIndex;
		this.strClassName = strClassName;
		rainbow = new Skittle[aintInHand.length];
		for (int i = 0; i < rainbow.length; i++) {
			rainbow[i] = new Skittle(aintInHand[i], i);
			if (aintInHand[i] > 0) {
				skittlesToTry.add(rainbow[i]);
			}
		}
		myMouth = new Mouth();
		mySense = new Sense();
		dblHappiness = 0;
	}

	@Override
	public void eat(int[] aintTempEat) {
		Skittle toEat = null;
		if (Skittle.getSkittlesLeft() <= 0) {
			return;
		}
		if (!skittlesToTry.isEmpty()) {
			toEat = skittlesToTry.remove();
			aintTempEat[toEat.getColor()] = 1;
			toEat.decCount(1);
			myMouth.skittleInMouth = toEat;
			myMouth.howMany = 1;
			toEat.setTasted();
			return;
		}
		// All unknowns were eaten
		while (toEat == null) {
			if (knownSkittleQueue.peek().getCount() > 0) {
				toEat = knownSkittleQueue.peek();
				rationOrPigOut(aintTempEat, toEat);
				if (toEat.getCount() == 0) {
					knownSkittleQueue.remove();
				}
				toEat.setTasted();
				return;
			}
			knownSkittleQueue.remove();
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

	@Override
	public void offer(Offer offTemp) {
		if (knownSkittleQueue.isEmpty()) {
			return;
		}

		Skittle leastFavorite = rainbow[0];
		Skittle mostFavorite = rainbow[0];

		for (Skittle s : rainbow) {
			if (s.getTasted() && s.getCount() > 0) {
				leastFavorite = s;
				mostFavorite = s;
				break;
			}
		}

		for (Skittle s : rainbow) {
			if (s.getTasted() && s.getCount() > 0) {
				if (s.getValue() < leastFavorite.getValue()) {
					leastFavorite = s;
				}
			}
			if (s.getTasted() && s.getCount() > 0) {
				if (s.getValue() > mostFavorite.getValue()) {
					mostFavorite = s;
				}
			}
		}

		int[] toOffer = new int[rainbow.length];
		int[] toRecieve = new int[rainbow.length];
		toOffer[leastFavorite.getColor()] = 1;
		toRecieve[mostFavorite.getColor()] = 1;

		offTemp.setOffer(toOffer, toRecieve);
	}

	@Override
	public void happier(double dblHappinessUp) {
		this.dblHappiness += dblHappinessUp;
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
	public Offer pickOffer(Offer[] aoffCurrentOffers) {
		// TODO Auto-generated method stub
		return null;
	}

	// Someone pick the offer
	@Override
	public void offerExecuted(Offer offPicked) {
		int[] aintOffer = offPicked.getOffer();
		int[] aintDesire = offPicked.getDesire();
		for (int intColorIndex = 0; intColorIndex < rainbow.length; intColorIndex++) {
			if (aintDesire[intColorIndex] > 0) {
				if (rainbow[intColorIndex].getCount() == 0) {
					knownSkittleQueue.add(rainbow[intColorIndex]);
				}
			}
			rainbow[intColorIndex].incCount(aintDesire[intColorIndex]);
			rainbow[intColorIndex].decCount(aintOffer[intColorIndex]);
		}
	}

	@Override
	public void updateOfferExe(Offer[] aoffCurrentOffers) {
		// TODO Auto-generated method stub

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
