package skittles.g2;

import java.util.Comparator;
import java.util.PriorityQueue;

public class Rainbow {
	
	private Skittle[] skittles;
	
	
	public Rainbow(int[] aintInHand) {
		skittles = new Skittle[aintInHand.length];
		for (int i = 0; i < skittles.length; i++) {
			skittles[i] = new Skittle(aintInHand[i], i);
		}
	}
	
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
	
	public Skittle[] getSkittles() {
		return skittles;
	}
	
	public Skittle getSkittle(int color) {
		return skittles[color];
	}
	
	public PriorityQueue<Skittle> untastedSkittlesByCount() {
		PriorityQueue<Skittle> ret = new PriorityQueue<Skittle>(10, new SkittleComparatorByCount());
		for (Skittle s: skittles) {
			if (!s.isTasted()) {
				ret.add(s);
			}
		}
		return ret;
	}
	
	public PriorityQueue<Skittle> tastedSkittlesByCount() {
		PriorityQueue<Skittle> ret = new PriorityQueue<Skittle>(10, new SkittleComparatorByCount());
		for (Skittle s: skittles) {
			if (s.isTasted()) {
				ret.add(s);
			}
		}
		return ret;
	}
	
	public PriorityQueue<Skittle> skittlesByCount() {
		PriorityQueue<Skittle> ret = new PriorityQueue<Skittle>(10, new SkittleComparatorByCount());
		for (Skittle s: skittles) {
			ret.add(s);
		}
		return ret;
	}
	
	public PriorityQueue<Skittle> skittlesByValue() {
		PriorityQueue<Skittle> ret = new PriorityQueue<Skittle>(10, new SkittleComparatorByValue());
		for (Skittle s: skittles) {
			ret.add(s);
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
	private class SkittleComparatorByValue implements Comparator<Skittle> {
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
}
