package skittles.g2;

public class Sense {
	
	public static boolean hoardColor(int color) {
		return true;
	}
	
	public double findHappinessForSkittle(double happiness, int count) {
		return (happiness / (Math.sqrt(count * 1.0)));
	}

}
