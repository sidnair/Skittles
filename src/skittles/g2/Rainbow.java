package skittles.g2;

import java.util.Comparator;
import java.util.PriorityQueue;

public class Rainbow {
	
	private Skittle[] skittles;
	private PriorityQueue<Skittle> skittlesToTry;
	
	public Rainbow(int[] aintInHand) {
		skittles = new Skittle[aintInHand.length];
		for (int i = 0; i < skittles.length; i++) {
			skittles[i] = new Skittle(aintInHand[i], i);
			if (aintInHand[i] > 0) {
				skittlesToTry.add(skittles[i]);
			}
		}
	}
	
/*	public int[] eatSkittles() {
		PriorityQueue<Skittle> untasted = untastedSkittlesByCount();
		
	}*/
		
	public Skittle getSkittleByColor(int color) {
		return skittles[color];
	}
	
	public Skittle[] getSkittles() {
		return skittles;
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
