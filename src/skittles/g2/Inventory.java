package skittles.g2;

import java.util.Comparator;
import java.util.PriorityQueue;

public class Inventory {
	
	private Skittle[] skittles;
	private int startingSkittles;
	
	public Inventory(int[] aintInHand) {
		startingSkittles = 0;
		skittles = new Skittle[aintInHand.length];
		for (int i = 0; i < skittles.length; i++) {
			skittles[i] = new Skittle(aintInHand[i], i);
			this.startingSkittles += aintInHand[i];
		}
	}
	
	public int getStartingSkittles() {
		return startingSkittles;
	}
	
	public double getIndividualHappiness(double happiness, int count) {
		return (happiness / (Math.sqrt(count * 1.0)));
	}
	
	/* This would return true only for the skittle with the highest score currently */
	public boolean isWorthHoarding(int color) {
		for (int i = 0; i < skittles.length; i++) {
			if (i == color) {
				continue;
			}
			double count2ndSktl = skittles[i].getCount();
			double value = 1;
			if (skittles[i].getValue() != Skittle.UNDEFINED_VALUE) {
				value = skittles[i].getValue();
			}
			if (skittles[color].getCurrentWorth() < value*count2ndSktl*count2ndSktl) {
				return false;
			}
		}
		return true;
	}
	
	public int size() {
		return skittles.length;
	}
		
	public Skittle getSkittleByColor(int color) {
		return skittles[color];
	}
	
	public Skittle getSkittle(int color) {
		return skittles[color];
	}
	
	public Skittle[] getSkittles() {
		return skittles;
	}
	
	public int getNumColors() {
		return skittles.length;
	}
	
	public double[] getColorValues() {
		double[] values = new double[skittles.length];
		
		for(int i = 0; i < values.length; i++) {
			values[i] = skittles[i].getValue();
		}
		
		return values;
	}
	
	public PriorityQueue<Skittle> untastedSkittlesByCount() {
		PriorityQueue<Skittle> ret = new PriorityQueue<Skittle>(10, new SkittleComparatorByCount());
		for (Skittle s: skittles) {
			if (!s.isTasted() && s.getCount() > 0) {
				ret.add(s);
			}
		}
		return ret;
	}
	
	public PriorityQueue<Skittle> tastedSkittlesByCount() {
		PriorityQueue<Skittle> ret = new PriorityQueue<Skittle>(10, new SkittleComparatorByCount());
		for (Skittle s: skittles) {
			if (s.isTasted() && s.getCount() > 0) {
				ret.add(s);
			}
		}
		return ret;
	}
		
	public PriorityQueue<Skittle> leastNegativeSkittles() {
		PriorityQueue<Skittle> ret = new PriorityQueue<Skittle>(10, new SkittleComparatorByValueHigh());
		for (Skittle s: skittles) {
			if (s.getValue() <= 0 && s.getValue() != Skittle.UNDEFINED_VALUE && s.getCount() > 0) {
				ret.add(s);
			}
		}
		return ret;
	}
	
	public PriorityQueue<Skittle> skittlesByValuesLowest() {
		PriorityQueue<Skittle> ret = new PriorityQueue<Skittle>(10, new SkittleComparatorByValueLow());
		for (Skittle s: skittles) {
			if (s.getCount() > 0) {
				ret.add(s);
			}
		}
		return ret;
	}
	
	/* Comparator for Skittles By Count */
	private class SkittleComparatorByCount implements Comparator<Skittle> {
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
	private class SkittleComparatorByValueLow implements Comparator<Skittle> {
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
	
	/* Comparator for Skittles PriQueue By Value */
	private class SkittleComparatorByValueHigh implements Comparator<Skittle> {
		@Override
		public int compare(Skittle x, Skittle y) {
			if (x.getValue() > y.getValue()) {
				return -1;
			}
			if (x.getValue() < y.getValue()) {
				return 1;
			}
			return 0;
		}
	}
}
