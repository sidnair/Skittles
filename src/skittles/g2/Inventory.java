package skittles.g2;

import java.util.ArrayList;
import java.util.Collections;
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
	
	public ArrayList<Skittle> getTastedSkittles() {
		ArrayList<Skittle> tastedSkittles = new ArrayList<Skittle>();
		for (Skittle s : skittles) {
			if (s.isTasted()) {
				tastedSkittles.add(s);
			}
		}
		return tastedSkittles;
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
		PriorityQueue<Skittle> ret =
			new PriorityQueue<Skittle>(10, new HighCountComparator());
		for (Skittle s: skittles) {
			if (!s.isTasted() && s.getCount() > 0) {
				ret.add(s);
			}
		}
		return ret;
	}
	
	public PriorityQueue<Skittle> tastedSkittlesByCount() {
		PriorityQueue<Skittle> ret = new PriorityQueue<Skittle>(10, new HighCountComparator());
		for (Skittle s: skittles) {
			if (s.isTasted() && s.getCount() > 0) {
				ret.add(s);
			}
		}
		return ret;
	}
		
	public PriorityQueue<Skittle> leastNegativeSkittles() {
		PriorityQueue<Skittle> ret = new PriorityQueue<Skittle>(10,
				new HighValueComparator());
		for (Skittle s: skittles) {
			if (s.getValue() <= 0 && s.getValue() != Skittle.UNDEFINED_VALUE && s.getCount() > 0) {
				ret.add(s);
			}
		}
		return ret;
	}
	
	public PriorityQueue<Skittle> skittlesByValuesLowest() {
		PriorityQueue<Skittle> ret =
			new PriorityQueue<Skittle>(10, new LowValueComparator());
		for (Skittle s: skittles) {
			if (s.getCount() > 0) {
				ret.add(s);
			}
		}
		return ret;
	}
	
	/*
	 * Higher counts come first.
	 */
	protected class HighCountComparator implements Comparator<Skittle> {
		@Override
		public int compare(Skittle x, Skittle y) {
			return y.getCount() - x.getCount();
		}
	}

	/*
	 * Lower values first.
	 */
	protected class LowValueComparator implements Comparator<Skittle> {
		@Override
		public int compare(Skittle x, Skittle y) {
			return (int) (x.getCurrentWorth() - y.getCurrentWorth());
		}
	}
	
	/*
	 * Higher values first.
	 */
	protected class HighValueComparator implements Comparator<Skittle> {
		@Override
		public int compare(Skittle x, Skittle y) {
			return (int) (y.getCurrentWorth() - x.getCurrentWorth());
		}
	}
	
	protected class HoardingScoreComparator implements Comparator<Skittle> {
		@Override
		public int compare(Skittle x, Skittle y) {
			/*
			 * TODO - incorporate tradability.
			 */
			if (x.isTasted() && y.isTasted()) {
				return (int) (x.getCurrentWorth() - y.getCurrentWorth());
			} else if (!x.isTasted() && !y.isTasted()) {
				return x.getCount() - y.getCount();
			} else if (!x.isTasted() && y.isTasted()) {
				return y.getValue() > 0 ? -1 : 1;
			} else {
				return x.getValue() > 0 ? 1 : -1;
			}
		}
		
	}
	
	public ArrayList<Skittle> getSortedSkittleArray() {
		ArrayList<Skittle> sortedSkittles = new ArrayList<Skittle>();
		Collections.addAll(sortedSkittles, this.getSkittles());
		Collections.sort(sortedSkittles, new HoardingScoreComparator());
		return sortedSkittles;
	}
}
