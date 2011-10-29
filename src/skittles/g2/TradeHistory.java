package skittles.g2;

public interface TradeHistory {

	public void addSuccessfulTrade(int[] given, int[] received);
	
	public void addUnsuccessfulTrade(int[] given, int[] received);
}
