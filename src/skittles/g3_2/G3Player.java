package skittles.g3_2;

import skittles.sim.Offer;

public class G3Player extends skittles.sim.Player {
	public Info info;
	public Eater eater;
	public Trader trader;

	public void eat(int[] aintTempEat) {
		// eater modifies aintTempEat, and lets info know what we decided to
		// eat.
		eater.decideToEat(aintTempEat);
		info.setEating(aintTempEat);

	}

	public void offer(Offer offTemp) {
		// trader modifies offTemp
		trader.setOffer(offTemp);
	}

	public void happier(double dblHappinessUp) {
		// happier gives us amount to boost score by (lets us update
		// preferences)
		info.update(dblHappinessUp);
	}

	public Offer pickOffer(Offer[] aoffCurrentOffers) {
		// we look through the current offers, picking one that looks best now.
		info.recordOffers(aoffCurrentOffers);
		return trader.pickOffer();
	}

	public void offerExecuted(Offer offPicked) {
		// we get offPicked, update info
		info.recordPicked(offPicked);
	}

	public void updateOfferExe(Offer[] aoffCurrentOffers) {
		// lets us figure out what others want
		info.recordExecuted(aoffCurrentOffers);
	}

	public void initialize(int players, double tasteDistMean, int intPlayerIndex,
			String strClassName, int[] aintInHand) {
		info = new Info(players, intPlayerIndex, strClassName, aintInHand);
		trader = new Trader(info);
		eater = new Eater(info);
	}

	public String getClassName() {
		return info.name;
	}

	public int getPlayerIndex() {
		return info.id;
	}

	public void syncInHand(int[] aintInHand) {
		info.hand = Util.copy(aintInHand);
	}
}
