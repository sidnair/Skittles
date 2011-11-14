package skittles.g3_2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import skittles.sim.Offer;

public class Trader {
	public Info info;

	public Trader(Info info) {
		this.info = info;
	}

	public void setOffer(Offer off) {
		HashSet<Integer> hoarding = info.pile.hoarding;
		HashSet<Integer> trading = info.pile.trading;
		int maxSize = 0;
		int colorToTake = 0;
		int colorToGive = 0;
		info.previousTarget = -1;

		for (int id = 0; id != info.numPlayers; ++id) {
			if (id == info.id)
				continue;
			ArrayList<Integer> profile = info.profiles.get(id);
			for (int colorHoarding : hoarding)
				for (int colorTrading : trading) {
					if (profile.get(colorHoarding) > info.initialSkittlesPerColor)
						continue;
					if (profile.get(colorTrading) < info.initialSkittlesPerColor)
						continue;
					
					int canTake = profile.get(colorHoarding);
					int canGive = info.hand[colorTrading];
					int offerSize = canTake < canGive ? canTake : canGive;
					if (offerSize > maxSize) {
						maxSize = offerSize;
						colorToTake = colorHoarding;
						colorToGive = colorTrading;
						info.previousTarget = id;
					}
				}
		}
		
		int [] give = new int[info.hand.length];
		int [] take = new int[info.hand.length];
		
		for (int i = 0; i != info.hand.length; ++i)
			give[i] = take[i] = 0;
		
		give[colorToGive] = take[colorToTake] = maxSize;
		off.setOffer(give, take);
	}

	public Offer pickOffer() {
		Offer best = null;
		double maxValue = 0;
		for (Offer off : info.currentOffers) {
			if (off.getOfferLive() && off.getOfferedByIndex() != info.id) {
				double currentValue = info.evaluate(off.getOffer(), off.getDesire(), true);
				if (currentValue > maxValue) {
					best = off;
					maxValue = currentValue;
				}
			}
		}
		return best;
	}
}
