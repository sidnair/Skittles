package skittles.g2;

public class PreferenceHistory implements TradeHistory {
	
	private final double DECAY_FACTOR = 0.5;
	
	/**
	 * Keeps track of the net gains/losses.
	 */
	int[] trades;
	double[] preferences;
	
	public PreferenceHistory(int skittleCount) {
		trades = new int[skittleCount];
		preferences = new double[skittleCount];
	}
	
	public void addSuccessfulTrade(int[] given, int[] received) {
		// TODO - calculate color:color relations 
		for (int i = 0; i < trades.length; i++) {
			trades[i] += received[i];
			trades[i] -= given[i];
		}
		updatePreferences(given, received);
	}
	
	public void addUnsuccessfulTrade(int[] given, int[] received) {
		updatePreferences(given, received);
	}
	
	private void updatePreferences(int[] given, int[] received) {
		// TODO - calculate color:color relations
		for (int i = 0; i < trades.length; i++) {
			preferences[i] *= DECAY_FACTOR;
			preferences[i] += received[i];
			preferences[i] -= given[i];
		}
	}
	
	public double[] getPreferences() {
		return preferences;
	}

}
