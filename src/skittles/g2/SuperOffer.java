package skittles.g2;

import java.util.ArrayList;

import skittles.sim.Offer;

public class SuperOffer {
	
	private Offer offer;
	private ArrayList<Integer> skippers;

	public SuperOffer(Offer offer, ArrayList<Integer> skippers) {
		this.offer = offer;
		this.skippers = skippers;
	}
	
	public ArrayList<Integer> getSkippers() {
		return skippers;
	}
	
	public Offer getOffer() {
		return offer;
	}

	public static ArrayList<SuperOffer> makeArr(
			Offer[] lastOfferSet, int numPlayers) {
		ArrayList<SuperOffer> sos = new ArrayList<SuperOffer>();
		ArrayList<Integer> pickedOffer = new ArrayList<Integer>();
		for (int i = 0; i < lastOfferSet.length; i++) {
			pickedOffer.add(lastOfferSet[i].getPickedByIndex());
		}
		ArrayList<Integer> avoiders = new ArrayList<Integer>();
		for (int i = 0; i < numPlayers; i++) {
			if (!pickedOffer.contains(i)) {
				avoiders.add(i);
			}
		}
		for (Offer o: lastOfferSet) {
			if (o.getPickedByIndex() == -1) {
				sos.add(new SuperOffer(o, avoiders));
			} else {
				sos.add(new SuperOffer(o, new ArrayList<Integer>()));
			}
		}
		return sos;
	}

}
