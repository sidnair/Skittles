package skittles.g2;

public class Skittle {

	private boolean tasted;
	private int count;
	private double value;
	private int color;
	public static final double UNDEFINED_VALUE = -2.0;
	int index;
		
	public Skittle(int count, int color) {
		this.count = count;
		this.tasted = false;
		this.color = color;
		this.value = UNDEFINED_VALUE;
		this.index = color;
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public int getColor() {
		return color;
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
	public double getValue() {
		return value;
	}
	
	public int getCount() {
		return count;
	}
	
	public void incCount(int incrementBy) {
		this.count += incrementBy;
	}
	
	public void decCount(int decrementBy) {
		this.count -= decrementBy;
	}
	
	public boolean isTasted() {
		return tasted;
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
