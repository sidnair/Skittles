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
	
	public void incCount(int incrementBy) {
		this.count += incrementBy;
	}
	
	public void decCount(int decrementBy) {
		this.count -= decrementBy;
	}
	
	public boolean isTasted() {
		return this.tasted;
	}
	
	public void setTasted() {
		this.tasted = true;
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
