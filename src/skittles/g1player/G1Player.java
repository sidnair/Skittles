package skittles.g1player;

import skittles.sim.Offer;
import skittles.sim.Player;

public class G1Player extends Player {
	
	public static final boolean DEBUG = false;
	 
	protected EatStrategy eatStrategy;
	protected OfferStrategy offerStrategy;
	protected PickStrategy pickStrategy;
	protected Infobase info; 

	@Override
	public void eat(int[] aintTempEat) {
		G1Player.printArray(info.getAintInHand(), "[G1Player] [eat] info.getAintInHand()");
		eatStrategy.update(aintTempEat,info);
		G1Player.printArray(aintTempEat, "[G1Player] [eat] aintTempEat");
		for(int i = 0; i<aintTempEat.length;i++){
			if(aintTempEat[i]!=0)
			{
				//System.out.println("[G1Player] [eat] eating Color: " + i);
				info.setIntLastEatIndex(i);
				info.setIntLastEatNum(aintTempEat[i]);
				break;
			}
		}
	}
	
	@Override
	public void offer(Offer offTemp) {
		G1Player.printArray(info.getAintInHand(), "after eating - info.getAintInHand():");
		int[] aintOffer = new int[info.getIntColorNum()];
		int[] aintDesire = new int[info.getIntColorNum()];
		offerStrategy.getOffer(aintOffer,aintDesire,info);
		
		System.out.print("Player 2's offer: [");
		for (int o : aintOffer)
		{
			System.out.print(o + "  ");
		}
		System.out.print("] <--> [");
		for (int d : aintDesire)
		{
			System.out.print(d + "  ");
		}
		System.out.println("]");

		G1Player.printArray(aintOffer, "aintOffer");
		G1Player.printArray(aintDesire, "aintDesire");
		offTemp.setOffer( aintOffer, aintDesire );
	}

	@Override
	public void syncInHand(int[] aintInHand) {

	}

	@Override
	public void happier(double dblHappinessUp) {
		info.updateHappiness(dblHappinessUp,info.getIntLastEatIndex(),info.getIntLastEatNum());
		if(!info.getPriority().isWeightedPriorityComplete()){
			//System.out.println("[G1Player] [happier] " + dblHappinessUp + ", " + info.getIntLastEatIndex() + ", " + info.getIntLastEatNum());
			info.getPriority().updatePriority(info.getIntLastEatIndex(), dblHappinessUp/info.getIntLastEatNum(), info);
		}
	}

	@Override
	public Offer pickOffer(Offer[] aoffCurrentOffers) {
		return pickStrategy.pick(aoffCurrentOffers,info);
	}

	@Override
	public void offerExecuted(Offer offPicked) {
		//System.out.println("[G1Player] [offerExecuted]: " + offPicked);
		info.updateOfferExecute(offPicked);
	}

	@Override
	public void updateOfferExe(Offer[] aoffCurrentOffers) {
		//System.out.println("[G1Player] [updateOfferExe]");
//		for(Offer o : aoffCurrentOffers){
//			System.out.println(" -- " + o);
//		}
		info.updateOfferExe(aoffCurrentOffers);
	}

	@Override
	public String getClassName() {
		return "G1Player";
	}

	@Override
	public int getPlayerIndex() {
		return info.getIntPlayerIndex();
	}

	@Override
	public void initialize(int intPlayerNum, double tasteDistMean, int intPlayerIndex,
			String strClassName, int[] aintInHand) {
		
		info = new Infobase();
		info.setIntPlayerIndex(intPlayerIndex);
		info.setStrClassName(strClassName);
		info.setAintInHand(aintInHand);
		info.setIntColorNum(aintInHand.length);

		int totalSkittles = 0;
		for (int i : aintInHand)
		{
			totalSkittles += i;
		}
		info.setInitialSkittlesPerPlayer(totalSkittles);

		info.createTables(intPlayerNum);
		
		info.setDblHappiness(0);
		info.setAdblTastes( new double[info.getIntColorNum()]);
		
		info.setColorHappinessArray(new double[info.getIntColorNum()]);
		for ( int intColorIndex = 0; intColorIndex < info.getIntColorNum(); intColorIndex ++ )
		{
			info.setAdblTasteElement(intColorIndex, -1);
			//adblTastes[ intColorIndex ] = -1;
		}

		info.getPriority().initializePriority(aintInHand);
		
		
		eatStrategy = new EatStrategy();
		pickStrategy = new PickStrategy();
		offerStrategy = new OfferStrategy(info);
	}
	
	public static void printArray(int[] array, String arrayName){
		if(G1Player.DEBUG){
			int tLength;
			tLength= array.length;
			System.out.print("" + arrayName + ": ");
			for(int i=0; i<tLength; i++){
				System.out.print(array[i] + ", ");
			}
			System.out.println();
		}
	}

}
