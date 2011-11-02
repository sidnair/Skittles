package skittles.g2;

public class Sense {
	
	public static boolean isHoardColor(int color) {
		return true;
	}
	
	public double getIndividualHappiness(double happiness, int count) {
		return (happiness / (Math.sqrt(count * 1.0)));
	}

}
