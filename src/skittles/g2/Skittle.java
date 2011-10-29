package skittles.g2;

public class Skittle {

	private boolean tasted;
	private int count;
	private double value;
	private static int skittlesLeft = 0;
		
	public Skittle(int count) {
		this.count = count;
		this.tasted = false;
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
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public boolean getTasted() {
		return tasted;
	}
	
	public void setTasted(boolean tasted) {
		this.tasted = tasted;
	}
	
	public static void incSkittlesCount(int incrementBy) {
		Skittle.skittlesLeft += incrementBy;
	}
	
	public static void decSkittlesCount(int decrementBy) {
		Skittle.skittlesLeft -= decrementBy;
	}
	
	public static int getSkittlesLeft() {
		return skittlesLeft;
	}
	
	
}
