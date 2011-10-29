
package skittles.g2;

import java.util.Comparator;
import java.util.PriorityQueue;

import skittles.sim.Offer;
import skittles.sim.Player;

public class Ebenezer extends Player {
	
	Skittle[] rainbow;
	double dblHappiness;
	String strClassName;
	int intPlayerIndex;
	Sense mySense; //Initialize this
	
	SkittleComparator comparator = new SkittleComparator();
	PriorityQueue<Skittle> unknownSkittleQueue = new PriorityQueue<Skittle>(10, comparator);
		
	@Override
	public void initialize(int intPlayerIndex, String strClassName, int[] aintInHand) {
		this.intPlayerIndex = intPlayerIndex;
		this.strClassName = strClassName;
		rainbow = new Skittle[aintInHand.length];
		for (int i = 0; i < rainbow.length; i++) {
			rainbow[i] = new Skittle(aintInHand[i]);
			Skittle.incSkittlesCount(aintInHand[i]);
			unknownSkittleQueue.add(rainbow[i]);
		}
		dblHappiness = 0;
	}

	@Override
	public void eat(int[] aintTempEat) {
		if (!unknownSkittleQueue.isEmpty()) {
			unknownSkittleQueue.remove();
		}
	}

	@Override
	public void offer(Offer offTemp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void happier(double dblHappinessUp) {
		// TODO Auto-generated method stub
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
	        if (x.getCount() < y.getCount())
	        {
	            return -1;
	        }
	        if (x.getCount() > y.getCount())
	        {
	            return 1;
	        }
	        return 0;
	    }
	}

}
