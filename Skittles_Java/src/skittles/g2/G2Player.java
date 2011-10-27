package skittles.g2;

import java.util.Arrays;

import skittles.sim.Offer;
import skittles.sim.Player;

public class G2Player extends Player {

	private int playerIndex;
	private String className;
	private int[] inHand;
	private int numColors;
	
	/**
	 * Wrap any printlns in this.
	 */
	public static final boolean DEBUG = true; 

	@Override
	public void initialize(int intPlayerIndex, String strClassName,
			int[] aintInHand) {
		this.playerIndex = intPlayerIndex;
		this.className = strClassName;
		this.inHand = aintInHand;
		this.numColors = aintInHand.length;
	}	
	
	@Override
	public void eatAndOffer(int[] aintTempEat, Offer offTemp) {
		System.out.println("G5 Hand: " + Arrays.toString(inHand));
		/* Pick one of the first color we can eat. */
		for (int i = 0; i < numColors; i++) {
			if (inHand[i] > 0) {
				aintTempEat[i] = 1;
				if (DEBUG) {
					System.out.println("G5 choice: " +
							Arrays.toString(aintTempEat));
				}
				inHand[i]--;
				break;
			}
		}
		
		int[] offer = new int[numColors];
		int[] desire = new int[numColors];
		offTemp.setOffer(offer, desire);
	}

	@Override
	public void happier(double deltaHappiness) {
		// TODO - store happiness, associate with color
	}

	@Override
	public Offer pickOffer(Offer[] currentOffers) {
		// TODO - make a smarter decision.
		// Always pick the first live offer.
		for (Offer o: currentOffers) {
			if (o.getOfferLive()) {
				int[] desiredSkittles = o.getDesire();
				int[] offeredSkittles = o.getOffer();
				for (int i = 0; i < numColors; i++) {
					inHand[i] += desiredSkittles[i] - offeredSkittles[i];
				}
				return o;
			}
		}
		return null;
	}

	@Override
	public void offerExecuted(Offer offPicked) {
		// TODO - this code should be uncommented once offPicked
		// has valid data...right now this work has been moved to
		// pickOffer.
		int[] desiredSkittles = offPicked.getDesire();
		int[] offeredSkittles = offPicked.getOffer();
		if (DEBUG) {
			System.out.println("In offer executed: ");
			System.out.println(offPicked);
		}
		for (int i = 0; i < numColors; i++) {
			inHand[i] += desiredSkittles[i] - offeredSkittles[i]; 
		}
	}

	@Override
	public void updateOfferExe(Offer[] aoffCurrentOffers) {
		// TODO Auto-generated method stub
	}

	

	
	/* METHODS BELOW HERE ARE NEVER ACTUALLY CALLED. */
	
	@Override
	public void syncInHand(int[] aintInHand) {
		// TODO - fill in body if this is ever called.
		System.err.println("Unexpected call to syncInHand");
		System.exit(1);
	}
	
	@Override
	public String getClassName() {
		// TODO - fill in body if this is ever called.
		System.err.println("Unexpected call to getClassName");
		System.exit(1);
		return className;
	}

	@Override
	public int getPlayerIndex() {
		// TODO - fill in body if this is ever called.
		System.err.println("Unexpected call to getPlayerIndex");
		System.exit(1);
		return playerIndex;
	}

}
