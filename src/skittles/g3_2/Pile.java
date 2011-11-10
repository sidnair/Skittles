package skittles.g3_2;

import java.util.HashSet;
import java.util.Iterator;

public class Pile {
	public HashSet<Integer> trading;
	public HashSet<Integer> hoarding;
	public Info info;
	
	public Pile(Info info){
		this.info = info;
		this.trading = new HashSet<Integer>();
		this.hoarding = new HashSet<Integer>();
	}
	
	public void add(int color){
		double preference = info.preference[color];
		if(preference > info.threshold)
			if(hoarding.size() != info.hoardingCount())
				hoarding.add(color);
			else{
				int[] colors = this.getHoardingColorsByPreference();
				if(info.preference[colors[colors.length-1]] < info.preference[color]){
					int colorTraded = colors[colors.length-1];
					hoarding.remove(colors[colors.length-1]);
					hoarding.add(color);
					trading.add(colorTraded);
				} else {
					trading.add(color);
				}
			}
		else
			if(trading.size() != info.hand.length - info.hoardingCount())
				trading.add(color);
			else{
				int[] colors = this.getTradingColorsByPreference();
				if(info.preference[colors[0]] > info.preference[color]){
					int colorTraded = colors[0];
					trading.remove(colors[0]);
					trading.add(color);
					hoarding.add(colorTraded);
				} else {
					trading.add(color);
				}				
			}
	}
	
	public String toString(){
		return "trade: "+ trading.toString() + ", hoard:" +hoarding.toString();
	}
	
	
	// get back a list of colors by preference
	// e.g. if we have colors 1,2,3 in list, and like them in rank 2,3,1, we get back 2,3,1.
	public int[] getColorsByPreference(HashSet<Integer> vals){
		int[] indices = new int[vals.size()];
		double[] prefs = new double[vals.size()];
		int i = 0;
		for(Iterator<Integer> itr = vals.iterator(); itr.hasNext(); )
			indices[i++] = itr.next();
		for(i = 0; i < indices.length; i++)
			prefs[i] = info.preference[indices[i]];
		int[] prefIndices = Util.index(prefs);
		int[] toReturn = new int[vals.size()];
		for(i = 0; i < indices.length; i++)
			toReturn[i] = indices[prefIndices[i]];
		return toReturn;
	}
	
	/**
	 * Most preferred is first.
	 * 
	 * Returns a list of indices for colors by preference
	 */
	public int[] getTradingColorsByPreference(){
		return this.getColorsByPreference(trading);
	}
	
	public int[] getHoardingColorsByPreference(){
		return this.getColorsByPreference(hoarding);
	}
}
