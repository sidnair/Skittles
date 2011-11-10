package skittles.g3_2;

public class Eater {
	public Info info;

	public Eater(Info info) {
		this.info = info;
	}

	private int whichColor = -1;
	private int howMany = -1;

	private void decideToEat() {
		whichColor = -1;
		howMany = 1;
		int colors = info.hand.length;
		
		// if endGame phase
		if (info.endGame && info.currentTurn > info.hand.length) {
			for (int i = 0; i != info.hand.length; ++i) {
				if (info.hand[i] > 0){
					whichColor = i;
					if (info.preference[i] > 0)
						howMany = info.hand[i];
					else
						howMany = 1;
					return;
				}
			}
		}
		
		
		// get all unknown colors
		int[] unknown = new int [colors];
		int unknownCount = 0;
		for (int i = 0 ; i != colors ; ++i)
			if (info.hand[i] > 0 && !info.tasted[i])
				unknown[unknownCount++] = i;
		// if there are unknown
		if (unknownCount != 0) {
			// return one of the biggest pile
			whichColor = unknown[0];
			for (int i = 1 ; i != unknownCount ; ++i)
				if (info.hand[unknown[i]] > info.hand[whichColor])
					whichColor = unknown[i];
			return;
		}
		
		int[] trading = info.pile.getTradingColorsByPreference();
		for (int i = 0 ; i != trading.length ; ++i) {
			int color = trading[i];
			if (info.hand[color] > 0)
				// pick the biggest pile
				if (whichColor < 0 || info.hand[color] > info.hand[whichColor])
					whichColor = color;
		}
		// if there was a skittle in a non-stacked pile
		if (whichColor >= 0){
			howMany = 1;
			return;
		}
		// pick from stacked piles
		int[] hoarding = info.pile.getHoardingColorsByPreference();
		for (int i = 0 ; i != hoarding.length ; ++i) {
			int color = hoarding[i];
			if (info.hand[color] > 0)
				// pick the smallest pile
				if (whichColor < 0 || info.hand[color] < info.hand[whichColor])
					whichColor = color;
		}
		howMany = info.hand[whichColor];
	}

	public void decideToEat(int[] eating) {
		decideToEat();
		int colors = info.hand.length;
		for (int i = 0 ; i != colors ; ++i)
			eating[i] = 0;
		eating[whichColor] = howMany;
		Util.print(info.preference);
		Util.print(eating);
		Util.print(info.hand);
		System.out.println(whichColor + " " + howMany);
	}
}
