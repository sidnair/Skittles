
package skittles.g2;

import java.util.Comparator;
import java.util.PriorityQueue;

import skittles.sim.Offer;
import skittles.sim.Player;
import skittles.sim.Skittles;

public class Ebenezer extends Player {
	
	public static final boolean DEBUG = true;
	
	Skittle[] rainbow;
	double dblHappiness;
	String strClassName;
	int intPlayerIndex;
	Mouth myMouth;
	
	SkittleComparator comparator = new SkittleComparator();
	PriorityQueue<Skittle> unknownSkittleQueue;
		
	@Override
	public void initialize(int intPlayerIndex, String strClassName, int[] aintInHand) {
		System.out.println(Ebenezer.class.toString() + " is index: " + intPlayerIndex);
		unknownSkittleQueue = new PriorityQueue<Skittle>(10, comparator);
		this.intPlayerIndex = intPlayerIndex;
		this.strClassName = strClassName;
		rainbow = new Skittle[aintInHand.length];
		for (int i = 0; i < rainbow.length; i++) {
			rainbow[i] = new Skittle(aintInHand[i], i);
			if (aintInHand[i] > 0) {
				unknownSkittleQueue.add(rainbow[i]);
			}
		}
		myMouth = new Mouth();
		dblHappiness = 0; 
	}

	@Override
	public void eat(int[] aintTempEat) {
		Skittle toEat;
		if (!unknownSkittleQueue.isEmpty()) {
			toEat = unknownSkittleQueue.remove();
			aintTempEat[toEat.getColor()] = 1;
			toEat.decCount(1);
			myMouth.skittleInMouth = toEat;
			myMouth.howMany = 1;
		}
		//No more unknowns to try
	}

	@Override
	public void offer(Offer offTemp) {
		// TODO Auto-generated method stub
	}

	@Override
	public void happier(double dblHappinessUp) {
		this.dblHappiness += dblHappinessUp;
		
	}

	@Override
	public Offer pickOffer(Offer[] aoffCurrentOffers) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void offerExecuted(Offer offPicked) {
		// TODO Auto-generated method stub

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
	
	//For debug mode apparently
	@Override
	public void syncInHand(int[] aintInHand) {
		// TODO Auto-generated method stub
	}
	
	/* Comparator for Skittles PriQueue */
	public class SkittleComparator implements Comparator<Skittle>
	{
	    @Override
	    public int compare(Skittle x, Skittle y)
	    {
	        if (x.getCount() > y.getCount())
	        {
	            return -1;
	        }
	        if (x.getCount() < y.getCount())
	        {
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
