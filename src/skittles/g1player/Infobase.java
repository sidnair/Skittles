package skittles.g1player;

import java.util.ArrayList;
import java.util.LinkedList;

import skittles.sim.Offer;

/**
 * @author McWings
 *
 */
public class Infobase {
	
	/**
	 * made Infobase as a singleton class.
	 */
	
	int[][] playerPreferences = null;
	double[][] estimatedSkittles = null;
	public int[] roundsInactive = null;
	double[] marketValues = null;
	int numPlayers;
	// Round count, updated when eat
	int count=0;
	
	//private Priority priority;
	private Priority priority;
	private int desiredColorCount = 0; //'c' as per discussion terminology
	private double[] colorHappinessArray;//happiness matrix
	private int[] aintInHand;
	private int initialSkittlesPerPlayer;
	
	public int intColorNum;
	double dblHappiness;
	String strClassName;
	int intPlayerIndex;
	public boolean roundComplete = false;
	
	private double[] adblTastes;
	private int intLastEatIndex;
	private int intLastEatNum;
	protected boolean denied; // if last offer was denied
	protected int[] ourselves;

	
	public Infobase() {
		priority = new Priority();
	}
	
	public Priority getPriority() {
		return priority;
	}
	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public boolean tablesExist()
	{
		if (playerPreferences == null || estimatedSkittles == null)
			return false;
		else
			return true;
	}
		
	public void createTables(int numPlayers)
	{
		playerPreferences = new int[numPlayers][intColorNum];
		estimatedSkittles = new double[numPlayers][intColorNum];
		roundsInactive = new int[numPlayers];
		marketValues = new double[intColorNum];
		this.numPlayers = numPlayers;
		this.ourselves = new int[numPlayers];
		
		double estSkittlesPerColor = (double)initialSkittlesPerPlayer/(double)intColorNum;
		
		for (int i = 0; i < numPlayers; ++i)
		{
			for (int j = 0; j <intColorNum ; ++j)
			{
				estimatedSkittles[i][j] = estSkittlesPerColor;
			}
			// calculate desired number of colors for this round
			if (numPlayers >= intColorNum) {
				desiredColorCount = 1;
			} else {
				desiredColorCount = Math.round(intColorNum / numPlayers);
			}
		}
	}
	
/*	public void updatePlayerPreferences(int playerIndex, Offer playerOffer)
	{
		
	}*/
	
/*	public void updateEstSkittles(int playerIndex, Offer playerOffer)
	{
		
	}*/

	public void updateHappiness(double dblHappinessUp, int intLastEatIndex, int intLastEatNum) {
		if (intLastEatNum == 1)
		{
			this.colorHappinessArray[intLastEatIndex] = dblHappinessUp;
		}
	}
	
	public void updateSkittlesInHand(Offer off, boolean wePickedThis)
	{
		int[] skittlesWeHave = this.getAintInHand();
		int[] giving = null;
		int[] getting = null;
		if (off.getOfferedByIndex() == this.intPlayerIndex && off.getPickedByIndex() != this.intPlayerIndex)
		{
			giving = off.getOffer();
			getting = off.getDesire();
		}
		else if ((off.getPickedByIndex() == this.intPlayerIndex || wePickedThis) && off.getOfferedByIndex() != this.intPlayerIndex)
		{
			giving = off.getDesire();
			getting = off.getOffer();
		}
		
		if (giving != null && getting != null)
		{
			for (int j = 0; j < getting.length; ++j)
			{
				skittlesWeHave[j] -= giving[j];	
				skittlesWeHave[j] += getting[j];
			}
		}
		this.setAintInHand(skittlesWeHave);
	}

	private void verifySkittlesCountIsPositive(int player)
	{
		double[] ourEstimate = estimatedSkittles[player];
		LinkedList<Integer> adjusted = new LinkedList<Integer>();
		double totalWrong = 0;
		
		for (int i = 0; i < intColorNum; ++i)
		{
			if (ourEstimate[i] < 0)
			{
				adjusted.add(i);
				totalWrong -= ourEstimate[i];
				ourEstimate[i] = 0;
			}
		}
		
		double adjustment = totalWrong/(intColorNum - adjusted.size());
		
		for (int i = 0; i < intColorNum; ++i)
		{
			if (!adjusted.contains(i))
			{
				ourEstimate[i] -= adjustment;
			}
		}
	}

	public void updateOfferExecute(Offer offPicked) {
		//Check we are on left side or on right side
		updateSkittlesInHand(offPicked, false);
	}
	
	/**
	 * This method takes in a player index and returns whether or not that
	 * player is considered inactive.<br />
	 * Passing a negative number for the player index will signal the function
	 * to check if EVERY player is inactive, and it will return true if all 
	 * other players are considered inactive.<br />
	 * 
	 * Right now the check for whether a player is inactive is that they
	 * have offered a null offer for the last three consecutive rounds.
	 * @param playerIndex index of player, or negative number to indicate all players
	 * 						besides this player
	 * @return true if the player(s) is considered inactive
	 * 			false if the player(s) is considered active
	 */
	public boolean isPlayerInactive(int playerIndex)
	{
		if (playerIndex < 0)
		{
			for (int i = 0; i < numPlayers; ++i)
			{
				if (roundsInactive[i] < 3 && i != intPlayerIndex)
				{
					return false;
				}
			}
			return true;
		}
		else if (playerIndex < numPlayers)
		{
			if (roundsInactive[playerIndex] >= 3)
			{
				return true;
			}
		}
		return false;
	}

	public void updateOfferExe(Offer[] aoffCurrentOffers) {
		accountForEating();
		for (Offer o : aoffCurrentOffers)
		{
			if(this.count==1){
				/*
				 *  Check whehter player i is myself in the first round
				 */
				int sum=0;
				for(int i=0;i<this.intColorNum;i++){
					sum+=o.getOffer()[i];
					sum-=this.getAintInHand()[i];
				}
				if(sum==0 && o.getOfferedByIndex()!=this.getIntPlayerIndex()){
					this.ourselves[o.getOfferedByIndex()]=1;
				}
			}
			
			updateTables(o);
		}
		
		calculateMarketValues();
	}
	
	private void accountForEating()
	{
		for (int i = 0; i < numPlayers; ++i)
		{
			for(int j = 0; j < intColorNum; ++j)
			{
				estimatedSkittles[i][j] -= (1/(double)intColorNum);
			}
		}
	}
	
	private void updateTables(Offer off)
	{
		/* update inactive for null offers */
		checkForNullOffer(off);
		
		/* if the offer was made by us and not taken, set denied = true */
		checkOurDeniedOffer(off);
		
		/* update skittles and priority based on offer taken */
		int offeredBy = off.getOfferedByIndex();
		int tookOffer = off.getPickedByIndex();
		int[] desired = off.getDesire();
		int[] offered = off.getOffer();

		if (tookOffer == this.intPlayerIndex)
		{
			updateSkittlesInHand(off, false);
		}
		
		verifySkittlesCount(off);
		if (tookOffer != -1)
		{
			updatePlayerSkittles(off);
		}
		for (int i = 0; i < desired.length; ++i)
		{
			this.playerPreferences[offeredBy][i] += desired[i];
			this.playerPreferences[offeredBy][i] -= offered[i];
			if (tookOffer != -1)
			{
				this.playerPreferences[tookOffer][i] -= desired[i]; // why sometimes Exception ArrayIndexOutOfBoundsException: -1?
				this.playerPreferences[tookOffer][i] += offered[i]; // tookOffer == -1? Not possible....
			}
		}
	}
	
	private void verifySkittlesCount(Offer off)
	{
		int offeredBy = off.getOfferedByIndex();
		int[] skittlesOffered = off.getOffer();
		double[] ourEstimate = estimatedSkittles[offeredBy];
		LinkedList<Integer> adjusted = new LinkedList<Integer>();
		double totalWrong = 0;
		
		for (int i = 0; i < intColorNum; ++i)
		{
			if (ourEstimate[i] < skittlesOffered[i])
			{
				adjusted.add(i);
				totalWrong += skittlesOffered[i] - ourEstimate[i];
				ourEstimate[i] = skittlesOffered[i];
			}
		}
		
		double adjustment = totalWrong/(intColorNum - adjusted.size());
		
		for (int i = 0; i < intColorNum; ++i)
		{
			if (!adjusted.contains(i))
			{
				ourEstimate[i] -= adjustment;
			}
		}
	}

	private void checkOurDeniedOffer(Offer off) {
		if(off.getOfferedByIndex() == intPlayerIndex &&off.getPickedByIndex() == -1){
			this.denied = true;
			if (G1Player.DEBUG)
			{
				System.out.println("offer denied");
			}
		}
		else
			if((off.getOfferedByIndex() == intPlayerIndex && off.getPickedByIndex() != -1 ))
				this.denied = false; 
	}

	private void checkForNullOffer(Offer off) {
		if (isNullOffer(off))
		{
			roundsInactive[off.getOfferedByIndex()] += 1;
		}
		else
		{
			roundsInactive[off.getOfferedByIndex()] = 0;
		}
	}
	
	private boolean isNullOffer(Offer off)
	{
		int[] desired = off.getDesire();
		int[] offered = off.getOffer();
		for (int i = 0; i < desired.length; ++i)
		{
			if (desired[i] != 0 || offered[i] != 0)
				return false;
		}
		return true;
	}
	
	private void updatePlayerSkittles(Offer off)
	{
		int offeredBy = off.getOfferedByIndex();
		int pickedBy = off.getPickedByIndex();
		int[] offeredGivingPickedGetting = off.getOffer();
		int[] offeredGettingPickedGiving = off.getDesire();
		int length = offeredGettingPickedGiving.length;
		
		for (int i = 0; i < length; ++i)
		{
			estimatedSkittles[offeredBy][i] += offeredGettingPickedGiving[i];
			estimatedSkittles[offeredBy][i] -= offeredGivingPickedGetting[i];
			estimatedSkittles[pickedBy][i] -= offeredGettingPickedGiving[i];
			estimatedSkittles[pickedBy][i] += offeredGivingPickedGetting[i];
		}
		
		verifySkittlesCountIsPositive(offeredBy);
		verifySkittlesCountIsPositive(pickedBy);
	}
	
	public void calculateMarketValues()
	{
		ArrayList<Integer> desiredColorList = this.getPriority().getDesiredVector(this);
		int multiplier = 0;
		for (int i = 0; i < intColorNum; ++i)
		{
			marketValues[i] = 0;
			for (int j = 0; j < numPlayers; ++j)
			{
				multiplier = 1;
				if (j != this.intPlayerIndex)
				{
					for(Integer goodColor : desiredColorList)
					{
						if (goodColor != -1 && playerPreferences[j][goodColor] < 0)
						{
							multiplier += 1;
						}
					}
					if (playerPreferences[j][i] > 0)
					{
						marketValues[i] += playerPreferences[j][i] * multiplier;
					}
				}
			}
		}
	}
	
	/**
	 * @return an array of market values for each skittle.  The index of the array
	 * 			corresponds to the color index for that skittle
	 */
	public double[] getMarketValues()
	{
		return marketValues;
	}
	
	/**
	 * @param color the index of the color
	 * @return the corresponding market value
	 */
	public double getMarketValueForColor(int color)
	{
		return marketValues[color];
	}
		
	public void dumpSkittleCounts()
	{
		if (G1Player.DEBUG)
		{
			System.out.println("Estimated Skittles: ");
			for (int i = 0; i < numPlayers; ++i)
			{
				for(int j = 0; j < intColorNum; ++j)
				{
					System.out.print(estimatedSkittles[i][j] + "\t");
				}	
				System.out.println();
			}
			System.out.println();
		}
	}
	
	public int getDesiredColorCount() {
		return desiredColorCount;
	}

	public void setDesiredColorCount(int desiredColorCount) {
		this.desiredColorCount = desiredColorCount;
	}

	public double[] getColorHappinessArray() {
		return colorHappinessArray;
	}

	public void setColorHappinessArray(double[] colorHappiness) {
		this.colorHappinessArray = colorHappiness;
	}

	public double getColorHappiness(int index) {
		return colorHappinessArray[index];
	}

	public int[] getAintInHand() {
		return aintInHand;
	}

	public void setAintInHand(int[] aintInHand) {
		this.aintInHand = aintInHand;
	}

	public int getIntColorNum() {
		return intColorNum;
	}

	public void setIntColorNum(int intColorNum) {
		this.intColorNum = intColorNum;
	}

	public double getDblHappiness() {
		return dblHappiness;
	}

	public void setDblHappiness(double dblHappiness) {
		this.dblHappiness = dblHappiness;
	}

	public String getStrClassName() {
		return strClassName;
	}

	public void setStrClassName(String strClassName) {
		this.strClassName = strClassName;
	}

	public int getIntPlayerIndex() {
		return intPlayerIndex;
	}

	public void setIntPlayerIndex(int intPlayerIndex) {
		this.intPlayerIndex = intPlayerIndex;
	}

	public double[] getAdblTastes() {
		return adblTastes;
	}

	public void setAdblTastes(double[] adblTastes) {
		this.adblTastes = adblTastes;
	}
	
	public void setAdblTasteElement(int index , double  value){
		this.adblTastes[index] = value;
	}

	public int getIntLastEatIndex() {
		return intLastEatIndex;
	}

	public void setIntLastEatIndex(int intLastEatIndex) {
		this.intLastEatIndex = intLastEatIndex;
	}

	public int getIntLastEatNum() {
		return intLastEatNum;
	}

	public void setIntLastEatNum(int intLastEatNum) {
		this.intLastEatNum = intLastEatNum;
	}

	public int getInitialSkittlesPerPlayer() {
		return initialSkittlesPerPlayer;
	}

	public void setInitialSkittlesPerPlayer(int initialSkittlesPerPlayer) {
		this.initialSkittlesPerPlayer = initialSkittlesPerPlayer;
	}
	
	/*
	 * check if playerIndex is one of us
	 */
	public boolean isOurself(int playerIndex){
		return this.ourselves[playerIndex]==1;
	}


}
