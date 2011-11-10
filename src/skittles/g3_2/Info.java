package skittles.g3_2;

import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import skittles.sim.Offer;

public class Info {
	
	public final static double DECREASING_FACTOR = 0.8;
	
	public int numPlayers;
	public int id;
	public String name;
	public int[] hand;
	public double[] preference;
	public boolean[] tasted;
	public Offer[] currentOffers;
	public Vector<Offer[]> pastOffers;
	public int[] eating;
	public Pile pile;
	public double threshold;
	public HashMap<Integer, ArrayList<Integer>> profiles;
	public int initialSkittlesPerColor; // the estimated number of skittles of
										// each color other player has
	public boolean endGame;
	public int currentTurn;
	public int previousTarget;

	public Info(int players, int intPlayerIndex, String strClassName,
			int[] aintInHand) {
		this.numPlayers = players;
		this.id = intPlayerIndex;
		this.name = strClassName;
		this.hand = Util.copy(aintInHand);
		this.pastOffers = new Vector<Offer[]>();
		this.preference = new double[hand.length];
		this.eating = new int[hand.length];
		this.tasted = new boolean[hand.length];
		this.threshold = computeThreshold();
		this.pile = new Pile(this);
		this.profiles = new HashMap<Integer, ArrayList<Integer>>();
		this.endGame = false;
		this.currentTurn = 0;
		this.previousTarget = -1;

		// initialSkittlesPerColor = #skittles / #colors
		initialSkittlesPerColor = Util.sum(hand) / hand.length;
		for (int id = 0; id != numPlayers; ++id) {
			if (id == this.id) // omit "me"
				continue;

			// let v = initialSkittlesPerColor
			// each profile is = <v, v, ..., v>, where the length is #colors
			ArrayList<Integer> profile = new ArrayList<Integer>(hand.length);
			for (int color = 0; color != hand.length; ++color)
				profile.add(initialSkittlesPerColor);

			profiles.put(id, profile);
		}
	}

	public void setEating(int[] eating) {
		this.currentTurn += 1;
		this.eating = Util.copy(eating);
		for (int i = 0; i < eating.length; i++)
			hand[i] -= eating[i];
	}

	public String toString() {
		return this.id + " " + this.name + "\nHand: "
				+ Util.toString(this.hand) + ",\nPreference:"
				+ Util.toString(this.preference);
	}

	public void update(double happiness) {
		for (int i = 0; i < eating.length; i++) {
			if (eating[i] != 0) {
				preference[i] = happiness / (eating[i] * eating[i]);
				if (!tasted[i])
					pile.add(i);
				tasted[i] = true;
			}
		}
	}

	public int hoardingCount() {
		int colors = hand.length;
		return Math.min(colors / 2,
				(int) Math.ceil(colors / (double) numPlayers));
	}

	public double computeThreshold() {
		int count = hoardingCount();
		int simulationSize = 100000;
		double[] points = new double[simulationSize];
		Random random = new Random();
		for (int i = 0; i != simulationSize; ++i) {
			points[i] = random.nextGaussian();
			if (points[i] < -1.0 || points[i] > 1.0)
				i--;
		}
		Arrays.sort(points);
		double perc = 1.0 - count / (double) hand.length;
		return points[(int) (perc * simulationSize)];
	}

	public void recordOffers(Offer[] offers) {
		currentOffers = offers;
	}

	public void recordExecuted(Offer[] offers) {
		for (Offer offer : offers) {
			if (offer.getPickedByIndex() == id)
				for (int i = 0; i < hand.length; i++)
					hand[i] += offer.getOffer()[i] - offer.getDesire()[i];
			pastOffers.add(offers);
		}
		updateProfiles(offers);
	}

	public void updateProfiles(Offer[] offers) {
		endGame = true;
		for (Offer offer : offers) {
			if (Util.sum(offer.getOffer()) != 0)
				endGame = false;
		}
		
		if (endGame == true)
			return;
		
		for (Offer offer : offers) {
			int giver = offer.getOfferedByIndex();
			int[] give = offer.getOffer();
			int taker = offer.getPickedByIndex();
			int[] take = offer.getDesire();
			
			if (taker == -1 && giver == this.id) {
				ArrayList<Integer> targetProfile = profiles.get(previousTarget);
				for (int color = 0; color != hand.length; ++color) {
					if (take[color] != 0)
						targetProfile.set(color, (int) (take[color] * DECREASING_FACTOR));
				}
			}

			if (taker != -1) {
				if (giver != this.id) {
					ArrayList<Integer> giverProfile = profiles.get(giver);
					for (int color = 0; color != hand.length; ++color) {
						int v = giverProfile.get(color) + take[color]
								- give[color];
						giverProfile.set(color, v);

					}
				}
				if (taker != this.id) {
					ArrayList<Integer> takerProfile = profiles.get(taker);
					for (int color = 0; color != hand.length; ++color) {
						int v = takerProfile.get(color) - take[color]
								+ give[color];
						takerProfile.set(color, v);
					}
				}
			}  else if (giver != this.id) {
				// taker == -1 i.e. proposed but not executed offer
				// and it's not my offer
				ArrayList<Integer> giverProfile = profiles.get(giver);
				for (int color = 0; color != hand.length; ++color) {
					 if (giverProfile.get(color) < give[color])	// he can still supply more than his profile
						 giverProfile.set(color, give[color]);	// update his profile
				}
			}
		}

		System.out.println("Profiles:");
		for (int id = 0; id != this.numPlayers; ++id) {
			if (id == this.id)
				continue;
			ArrayList<Integer> profile = profiles.get(id);
			System.out.println("Player #" + id + ": " + profile);
		}
	}

	public double evaluate(int[] offer, int[] desire, boolean usingProfile) {
		double value = 0;
		for (int i = 0; i < hand.length; i++) {
			double tmp = (usingProfile ? hand[i] : 0) + offer[i] - desire[i];
			if (tmp < 0) // invalid offer (gives negative something)
				return -1;
			else if (tasted[i])
				tmp *= tmp;
			value += tmp * preference[i];
		}
		return value;
	}

	public void recordPicked(Offer offer) {
		// we got an offer picked.
		int[] taking = offer.getDesire();
		int[] giving = offer.getOffer();
		for (int i = 0; i < hand.length; i++)
			hand[i] += taking[i] - giving[i];
	}
}
