package skittles.g2;

/**
 * A group of a single color of Skittles
 */
public class Skittle {

	private boolean tasted;
	private int count;
	private double value;
	private int color;
	public static final double UNDEFINED_VALUE = -2.0;
		
	public Skittle(int count, int color) {
		this.count = count;
		this.tasted = false;
		this.color = color;
		this.value = UNDEFINED_VALUE;
	}

	public int getColor() {
		return this.color;
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
	public double getValue() {
		return this.value;
	}
	
	public int getCount() {
		return this.count;
	}
	
	public void updateCount(int count) {
		this.count += count;
	}
		
	public boolean isTasted() {
		return this.tasted;
	}
	
	public void setTasted(boolean tasted) {
		this.tasted = tasted;
	}
	
	/*
	 * Get the score from eating these Skittles. If the score is negative, we 
	 * assume that we're eating them one by one. Otherwise, assume that we
	 * eat them all as quickly as possible.
	 */
	public double getCurrentWorth() {
		if (this.value > 0) {
			return this.count*this.count*this.value;
		} else {
			return this.value * this.count;
		}
	}
		
	public String toString() {
		String ret = "[color: " + color + ", (count: " + count + "), ";
		if (this.value != UNDEFINED_VALUE) {
			ret += "(value: " + this.value + ")";
		} else {
			ret += "(value: unknown)";
		}
		ret += "]";
		return ret;
	}

}
