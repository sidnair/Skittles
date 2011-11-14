package skittles.g1player;

import java.util.Random;

public class OfferStrategy {
	private Infobase info;
	private int c = 2;
	// how many colors we are going to have last round.N/k or 1.

	protected int count = 0;
	private Random rand = new Random();
	private int colorNum;
	protected int[] offerTracker;
	private int lastGet;
	private int lastGivePrioty = 1; // used by first several rounds, points to the
								// priority list
	private int totSkittles = 0;
	private boolean validOffer = false;

	protected OfferStrategy(Infobase infoUpdate) {
		this.info = infoUpdate;
		this.colorNum = info.getIntColorNum();
		this.offerTracker = new int[colorNum];
	}

	public void getOffer(int[] aintOffer, int[] aintDesire, Infobase infoUpdate) {
		this.info = infoUpdate;
		this.c = info.getDesiredColorCount();
		count++;
		G1Player.printArray(info.getAintInHand(), "in hand");
		G1Player.printArray(this.offerTracker, "Offer Tracker");
		G1Player.printArray(info.ourselves, "Ourself");
		if (info.denied && this.validOffer) {
			offerTracker[this.lastGet]++;
			if (G1Player.DEBUG)
				System.out.println("update ++" + String.valueOf(this.lastGet));
		}

		int[] priorityArray = info.getPriority().getPriorityArray();
		G1Player.printArray(priorityArray, "OfferStrategy priorityArray:");
		colorNum = info.intColorNum; // Erica: changed this to intColorNum to be more direct than length of priority array

		int[] maxOffers = new int[c]; // # of skittles others able to give us
		int[] colorOffers = new int[c]; // what should we offer
		int colorOffer = 0;
		int colorGet = 0;

		/*
		 * STEP 1: at the beginning of the game, we have little info about what
		 * other's like of dislike so the beginning rounds will have special
		 * strategy
		 */
		if (count == 1) {
			this.validOffer = false;
			/*
			 * The following lines try to identify ourselves in the first round
			 * It will be more complex in the tournament
			 */
			for (int i = 0; i < colorNum; i++) {
				aintDesire[i] = aintOffer[i] = info.getAintInHand()[i];
				totSkittles += info.getAintInHand()[i];
			}

			return;
		}

		/*
		 * STEP 2: in the first several rounds, when we don't know most of the
		 * colors' value, our offer strategy is try to give away what's in the
		 * end of the priority list and ask for what's in {C}
		 */
		// Erica: this says 4 is magic number, why was it changed to 1?
		if (count < 4){//Math.min(colorNum / 2, 4)) {
			// here 4 is a magic number, pls try to run some test to find the
			// best one when eating bug fixed.

			this.validOffer = false;
			// start from last one we proposed to offer
			int tempLeast = this.lastGivePrioty;
			int leastLike = priorityArray[tempLeast - 1];
			// find least like, with positive inventory
			while (info.getAintInHand()[leastLike] == 0) {
				leastLike = priorityArray[--tempLeast];
				if (tempLeast < c) {
					if (G1Player.DEBUG)
						System.out
								.println("!!!!!!!!!!!!!!WTF!!!!!!!!!!!!!!!!!!!!!!!!!!!!~~~~~~~~~~~~~~~~~");
					break;
				}
			}
			this.lastGivePrioty = tempLeast;
			// 0.8 is a magic number.
			int quantity = (int) (totSkittles / colorNum * 0.8);
			// we've tasted count colors, randomly choose one
			int mostLike = rand.nextInt(Math.min(c, count));
			// check if someone happen to be willing make a deal
			quantity = Math.max(quantity,
					this.calculateOffer(mostLike, leastLike));
			// check inventory
			quantity = Math.min(info.getAintInHand()[leastLike], quantity);

			aintOffer[leastLike] = quantity;
			aintDesire[mostLike] = quantity;
			return;
		}

		/*
		 * STEP 3: We look for the most number of skittles we can get this term,
		 * in {C}, we don't care what kind of skittles we are going to give away
		 * as long as it is not in C.
		 */
		for (int i = 0; i < c; i++) {
			maxOffers[i] = 0;
			for (int j = c; j < priorityArray.length; j++) {
				int tempOffer = this.calculateOffer(priorityArray[i],
						priorityArray[j]);
				if (tempOffer > info.getAintInHand()[priorityArray[j]])
				// check if we have that many of skittles?
				{
					tempOffer = info.getAintInHand()[priorityArray[j]];
				}
				if (tempOffer > maxOffers[i]) {
					maxOffers[i] = tempOffer;
					colorOffers[i] = priorityArray[j];
				}
			}
		}
		int maxQuantity = 0;
		for (int i = 0; i < c; i++) {
			if (maxQuantity < maxOffers[i]) {
				maxQuantity = maxOffers[i];
				colorGet = priorityArray[i];
				colorOffer = colorOffers[i];
			}
		}
		aintOffer[colorOffer] = maxQuantity;
		aintDesire[colorGet] = maxQuantity;

		/*
		 * STEP 4: if we can't find perfect trade, propose some other trade,
		 * randomly.
		 */
		int quantity = 0;
		if (maxQuantity == 0) {
			int tempLeast = rand.nextInt(colorNum - c) + c;
			int leastLike = priorityArray[tempLeast];
			quantity = (int) (info.getAintInHand()[leastLike] / Math.pow(2,
					offerTracker[colorGet]));
			int randcount = 0;
			while (info.getAintInHand()[leastLike] == 0 || quantity == 0) {
				if (randcount++ > colorNum) {
					break;
				}
				tempLeast = rand.nextInt(colorNum - c) + c;
				leastLike = priorityArray[tempLeast];
				quantity = (int) (info.getAintInHand()[leastLike] / Math.pow(2,
						offerTracker[colorGet]));
			}
			// quantity = Math.min(quantity, 3);
			aintOffer[leastLike] = quantity;
			int mostLike = priorityArray[rand.nextInt(c)];
			aintDesire[mostLike] = quantity;
			this.lastGet = mostLike;
			this.validOffer = true;
		} else {
			this.lastGet = colorGet;
			this.validOffer = true;
		}

		/*
		 * STEP 5: tried everything for not_in_c -> c, then try c->c
		 */
		if (quantity == 0 && c > 1) {
			int c1 = rand.nextInt(c);
			int c2 = 0;
			int randcount = 0;
			// randomly pick some color to give away
			do {
				c2 = rand.nextInt(c);
				randcount++;
			} while ((c2 == c1 || info.getAintInHand()[c2] > 0)
					&& randcount < colorNum * 2);
			int q1 = info.getAintInHand()[c1];
			int q2 = info.getAintInHand()[c2];
			double u1 = info.getColorHappiness(c1);
			double u2 = info.getColorHappiness(c2);
			randcount = 0;
			if (q2 >= 1) {
				do {
					quantity = (int) (rand.nextInt(q2) / Math.pow(2,
							offerTracker[colorGet]));
					randcount++;
				} while ((u1 * q1 * q1 + u2 * q2 * q2) <= (u1 * (q1 + quantity)
						* (q1 + quantity) + u2 * (q2 - quantity)
						* (q2 - quantity))
						&& randcount < colorNum * 2);
			}

			this.lastGet = c1;
			this.validOffer = true;
			aintOffer[c2] = quantity;
			aintDesire[c1] = quantity;
		}

		G1Player.printArray(aintOffer, "Offer: ");
	}

	/*
	 * this functions calculates the max number of colorGet we can get from
	 * trading colorOffer
	 */
	private int calculateOffer(int colorGet, int colorOffer) {
		double max = 0;
		for (int i = 0; i < info.numPlayers; i++) {
			if (info.playerPreferences[i][colorOffer] > 0
					&& info.playerPreferences[i][colorGet] < 0) {
				if (max < info.estimatedSkittles[i][colorGet]) {
					if (info.isPlayerInactive(i)) {
						if (G1Player.DEBUG)
							System.out.println("Inactive Player  "
									+ String.valueOf(i));
					} else
						max = info.estimatedSkittles[i][colorGet];
				}
			}
		}
		return (int) (max / Math.pow(2, offerTracker[colorGet]));
	}

	public int getC() {
		return c;
	}

	public void setC(int c) {
		this.c = c;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
