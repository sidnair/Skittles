package skittles.g5.sim;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import javax.xml.parsers.*;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import skittles.manualplayer.ManualP;

public class Game 
{
	private Player[] aplyPlayers;
	private PlayerStatus[] aplsPlayerStatus;
	private ArrayList< Player > alPlayers ;
	private ArrayList< PlayerStatus > alPlayerStatus;
	private int intPlayerNum;
	private int intColorNum;
	private boolean doAllPermutations=false;
	
	private Double[] totalScores;
	private  Integer[][] factorialPermutations;
	private Integer[][] defaultPermutations;
	private Double[][] defaultTastes;
	private  Integer currentIndex;
	private ArrayList<Integer> playerIndexes;
	private String[] strPlayerClassArray;
	
	private Offer[] aoffCurrentOffers = null;
	private int[][] aintCurrentEats = null;

	public static Scanner scnInput = new Scanner( System.in );

	public Game( String strXMLPath )
	{
		DocumentBuilderFactory dbfGameConfig = DocumentBuilderFactory.newInstance();

		Document dcmGameConfig = null;
		try 
		{
			//Using factory get an instance of document builder
			DocumentBuilder dbdGameConfig = dbfGameConfig.newDocumentBuilder();
			//parse using builder to get DOM representation of the XML file
			dcmGameConfig = dbdGameConfig.parse( strXMLPath );
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
		//get the root element
		dcmGameConfig.getDocumentElement().normalize();
		//get a nodelist of elements
		NodeList ndlGame = dcmGameConfig.getElementsByTagName("Game");
		int intTotalNum = 0;
		if(ndlGame != null && ndlGame.getLength() > 0) 
		{
			for(int i = 0 ; i < ndlGame.getLength();i++) 
			{
				//get the employee element
				Element elmGame = (Element) ndlGame.item(i);
				//retrieve player information
				intColorNum = Integer.parseInt( getTagValue( elmGame, "ColorNum" ) );
				intTotalNum = Integer.parseInt( getTagValue( elmGame, "SkittleNum" ) );	
			}
		}
		// initialize players
			 playerIndexes=new ArrayList<Integer>();
		//get a nodelist of elements
		NodeList ndlPlayers = dcmGameConfig.getElementsByTagName("Player");
		defaultPermutations=new Integer[ndlPlayers.getLength()][intColorNum];
		defaultTastes=new Double[ndlPlayers.getLength()][intColorNum];
		strPlayerClassArray=new String[ndlPlayers.getLength()];
		totalScores=new Double[ndlPlayers.getLength()];
		if(ndlPlayers != null && ndlPlayers.getLength() > 0) 
		{
			intPlayerNum = ndlPlayers.getLength();
			for(int i = 0 ; i < ndlPlayers.getLength();i++) 
			{
				playerIndexes.add(i);
				totalScores[i]=0.0;
				//get the employee element
				Element elmPlayer = (Element) ndlPlayers.item(i);
				//retrieve player information
				String strPlayerClass = getTagValue( elmPlayer, "Class" );
				strPlayerClassArray[i]=strPlayerClass;
				String strTastes = getTagValue( elmPlayer, "Happiness" );
				String[] astrTastes = strTastes.split( "," );
				double[] adblTastes = new double[ intColorNum ];
				if ( !astrTastes[ 0 ].equals( "random" ) )
				{
					for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
					{
						adblTastes[ intColorIndex ] = Double.parseDouble( astrTastes[ intColorIndex ] );
					}
				}
				else
				{
					double dblMean = Double.parseDouble( astrTastes[ 1 ] );
					adblTastes = randomTastes( dblMean );
//					System.out.println( "Random color happiness:" );
					for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
					{
//						System.out.print( adblTastes[ intColorIndex ] );
					}
//					System.out.println();
				}
				String strInHand = getTagValue( elmPlayer, "InHand" );
				int[] aintInHand = new int[ intColorNum ];
				int intTempSkittleCount = 0;
				if ( strInHand.equals( "random" ) )
				{
					aintInHand = randomInHand( intTotalNum );
					for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
					{
						intTempSkittleCount += aintInHand[ intColorIndex ];
					}
				}
				else
				{
					String[] astrInHand = strInHand.split( "," );
					for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
					{
						aintInHand[ intColorIndex ] = Integer.parseInt( astrInHand[ intColorIndex ] );
						intTempSkittleCount += aintInHand[ intColorIndex ];
					}
					if ( intTempSkittleCount != intTotalNum )
					{
						System.out.println( "Skittle number in hand is not consistent." );
					}
				}
				
				for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
				{
					defaultPermutations[i][ intColorIndex ] = aintInHand[intColorIndex];
					defaultTastes[i][ intColorIndex ] = adblTastes[intColorIndex];
				}
				
			}
		}
		//	aplyPlayers = alPlayers.toArray( new Player[ 0 ] );
		//	aplsPlayerStatus = alPlayerStatus.toArray( new PlayerStatus[ 0 ] );	
	}
	
	
	public void runGame()
	{

	
		currentIndex=0;
		Integer numberOfPermutations=factorial(playerIndexes.size());
		factorialPermutations=new Integer[numberOfPermutations][playerIndexes.size()];
		setAllCombinations(new ArrayList<Integer>(),playerIndexes);
		for (int i=0;i<numberOfPermutations;i++) {
			if(!doAllPermutations && i>0)
				break;
//			System.out.println("\n\nGame  "+(i+1)+" starts");

			alPlayers = new ArrayList< Player >();			// players
			 alPlayerStatus = new ArrayList< PlayerStatus >();		// status of players for simulator's record
			 aplyPlayers = new Player[playerIndexes.size()];
			 aplsPlayerStatus = new PlayerStatus[playerIndexes.size()];
			for (int j=0;j<playerIndexes.size();j++) {
				
				Integer correspondingIndex=factorialPermutations[i][j];
				int[] inHand=new int[intColorNum];
				double[] taste=new double[intColorNum];
				for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
				{
					inHand[ intColorIndex ] = defaultPermutations[correspondingIndex][intColorIndex];
					taste[ intColorIndex ] = defaultTastes[ correspondingIndex ][intColorIndex];
				}
				Player plyNew = null;
				try {
					plyNew = ( Player ) Class.forName( strPlayerClassArray[j] ).newInstance();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				plyNew.initialize( intPlayerNum, j, strPlayerClassArray[j], inHand.clone() );
				alPlayers.add( plyNew );
				PlayerStatus plsTemp = new PlayerStatus( j, strPlayerClassArray[j], inHand.clone(), taste.clone() );
				alPlayerStatus.add( plsTemp );
			}
			
			
			aplyPlayers = alPlayers.toArray( new Player[ 0 ] );
			aplsPlayerStatus = alPlayerStatus.toArray( new PlayerStatus[ 0 ] );
			for (int j=0;j<playerIndexes.size();j++) {
				printArray("\nPlayer "+j+" in hand ",aplsPlayerStatus[j].getInHand());
				printArray("Player "+j+" tastes  ",aplsPlayerStatus[j].getTastes());
			}
		    //aplyPlayers = tempAlPlayers.toArray( new Player[ 0 ] );
			//aplsPlayerStatus = tempAlPlayerStatus.toArray( new PlayerStatus[ 0 ] );
			//showEveryInHand();
				 
			
			
			FileWriter[] afrtPortfolio = new FileWriter[ intPlayerNum ];
			BufferedWriter[] abfwPortfolio = new BufferedWriter[ intPlayerNum ];
			try {
				for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
				{
					afrtPortfolio[ intPlayerIndex ] = new FileWriter( "P" + intPlayerIndex + ".txt" );
					abfwPortfolio[ intPlayerIndex ] = new BufferedWriter( afrtPortfolio[ intPlayerIndex ] );

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// check whether there is still at least one player has more skittles to eat
			logGame( abfwPortfolio, "E" );
			logGame( abfwPortfolio, "O" );
			logGame( abfwPortfolio, "P" );
			logGame( abfwPortfolio, "H" );
			logGame( abfwPortfolio, "N" );
			while ( !checkFinish() )
			{
				showEveryInHand();		
				everyoneEatAndOffer();
				logGame( abfwPortfolio, "E" );
				int[] aintOrder = generateRandomOfferPickOrder();			// need code to log the order for repeated game
				pickOfferInOrder( aintOrder );
				broadcastOfferExcution();
				logGame( abfwPortfolio, "O" );
				logGame( abfwPortfolio, "P" );
				logGame( abfwPortfolio, "H" );
				logGame( abfwPortfolio, "N" );
			}
			double dblAver = 0;
			for ( PlayerStatus plsTemp : aplsPlayerStatus )
			{
				dblAver += plsTemp.getHappiness();
			}
			dblAver = dblAver / intPlayerNum;
			for ( PlayerStatus plsTemp : aplsPlayerStatus )
			{
				double dblTempHappy = plsTemp.getHappiness() + dblAver;
				System.out.println( "Player #" + plsTemp.getPlayerIndex() + "'s happiness is: " + dblTempHappy );
				totalScores[plsTemp.getPlayerIndex()]+=dblTempHappy;
			}

			try {
				for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
				{
					abfwPortfolio[ intPlayerIndex ].close();
					afrtPortfolio[ intPlayerIndex ].close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Game "+(i+1)+" ends");
			System.out.println("After Game "+(i+1)+" the total scores are");
			for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
			{
				System.out.println("Total Player #"+intPlayerIndex+"'s happiness ="+totalScores[intPlayerIndex]);
			}
			
			

		}//end of many game loops

	}

	private void logGame( BufferedWriter[] abfwPortfolio, String strLogWhat )
	{
		if ( strLogWhat.equals( "P" ) )
		{
			for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
			{
				PlayerStatus plsTemp = aplsPlayerStatus[ intPlayerIndex ];
				int[] aintInHand = plsTemp.getInHand();
				for ( int intInHand : aintInHand )
				{
					try {
						abfwPortfolio[ intPlayerIndex ].write( intInHand + "\t" );
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		else if ( strLogWhat.equals( "E" ) )
		{
			for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
			{
				int[] aintCurrentEat;
				if ( aintCurrentEats == null )
				{
					aintCurrentEat = new int[ intColorNum ];
				}
				else
				{
					aintCurrentEat = aintCurrentEats[ intPlayerIndex ];
				}
				for ( int intEat : aintCurrentEat )
				{
					try {
						abfwPortfolio[ intPlayerIndex ].write( intEat + "\t" );
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		else if ( strLogWhat.equals( "N" ) )
		{
			for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
			{
				try {
					abfwPortfolio[ intPlayerIndex ].write( "\n" );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else if ( strLogWhat.equals( "O" ) )
		{
			int[][] aintSumOffExe = new int[ intPlayerNum ][ intColorNum ];
			int[][] aintOffs = new int[ intPlayerNum ][ intColorNum ];
			if ( aoffCurrentOffers != null )
			{
				for ( Offer offTemp : aoffCurrentOffers )
				{
					int[] aintOff = offTemp.getOffer();
					int[] aintDesire = offTemp.getDesire();
					int intOfferedByIndex = offTemp.getOfferedByIndex();
					int intPickedByIndex = offTemp.getPickedByIndex();
					for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
					{
						aintOffs[ intOfferedByIndex ][ intColorIndex ] = aintOff[ intColorIndex ] - aintDesire[ intColorIndex ];
					}
					if ( !offTemp.getOfferLive() && offTemp.getPickedByIndex() != -1 )
					{
						for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
						{
							aintSumOffExe[ intOfferedByIndex ][ intColorIndex ] += aintDesire[ intColorIndex ] - aintOff[ intColorIndex ];
							aintSumOffExe[ intPickedByIndex ][ intColorIndex ] += aintOff[ intColorIndex ] - aintDesire[ intColorIndex ];
						}
					}
				}
			}
			for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
			{
				int[] aintSumOffE = aintSumOffExe[ intPlayerIndex ];
				int[] aintOff = aintOffs[ intPlayerIndex ];
				for ( int intOff : aintOff )
				{
					try {
						abfwPortfolio[ intPlayerIndex ].write( intOff + "\t" );
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				for ( int intSumOffE : aintSumOffE )
				{
					try {
						abfwPortfolio[ intPlayerIndex ].write( intSumOffE + "\t" );
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		else if ( strLogWhat.equals( "H" ) )
		{
			for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
			{
				PlayerStatus plsTemp = aplsPlayerStatus[ intPlayerIndex ];
				double dblHappiness = plsTemp.getHappiness();
				try {
					abfwPortfolio[ intPlayerIndex ].write( dblHappiness + "\t" );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private double[] randomTastes(double dblMean) 
	{
		double[] adblRandomTastes = new double[ intColorNum ];
		Random rdmTemp = new Random();
		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
		{
			double dblTemp = -5;		// out of range [ -1, 1 ]
			while ( dblTemp < -1 || dblTemp > 1 )
			{
				dblTemp = rdmTemp.nextGaussian() + dblMean;
			}
			adblRandomTastes[ intColorIndex ] = dblTemp;
		}
		return adblRandomTastes;
	}

	private int[] randomInHand(int intTotalNum) 
	{
		int[] aintRandomInHand = new int[ intColorNum ];
		//		Random rdmTemp = new Random();
		//		int[] aintTemp = new int[ intColorNum + 1 ];
		//		aintTemp[ intColorNum ] = intTotalNum;
		//		for ( int intColorIndex = 1; intColorIndex < intColorNum; intColorIndex ++ )
		//		{
		//			aintTemp[ intColorIndex ] = rdmTemp.nextInt( intTotalNum + 1 );
		//		}
		//		Arrays.sort( aintTemp );
		////		System.out.println( "RandomInHand: " );
		//		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
		//		{
		//			aintRandomInHand[ intColorIndex ] = aintTemp[ intColorIndex + 1 ] - aintTemp[ intColorIndex ];
		////			System.out.print( aintRandomInHand[ intColorIndex ] + " " );
		//		}
		//		System.out.println();
		Random rdmTemp = new Random();
		for ( int intSkittleIndex = 0; intSkittleIndex < intTotalNum; intSkittleIndex ++ )
		{
			int intTemp = rdmTemp.nextInt( intColorNum );
			aintRandomInHand[ intTemp ] ++;
		}
		return aintRandomInHand;
	}

	private String getTagValue( Element elmPlayer, String strTagName )
	{
		String strValue = null;
		NodeList ndlPlayer = elmPlayer.getElementsByTagName( strTagName );
		if( ndlPlayer != null && ndlPlayer.getLength() > 0) {
			Element elmTag = (Element) ndlPlayer.item(0);
			strValue = elmTag.getFirstChild().getNodeValue();
		}
		return strValue;
	}

	private void showEveryInHand() 
	{
		System.out.println( "<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n" );
		System.out.println( "******************************************" );
		System.out.println( "------------------------------------------");
		System.out.println( "Skittles portfolio:" );
		for ( PlayerStatus plsTemp : aplsPlayerStatus )
		{
			System.out.println( plsTemp.toString() );
		}
		System.out.println( "------------------------------------------");
		System.out.println( "******************************************\n" );
	}

	private boolean checkFinish()
	{
		boolean blnFinish = true;
		for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
		{
			if ( aplsPlayerStatus[ intPlayerIndex ].getTotalInHand() > 0 )
			{
				blnFinish = false;
				break;
			}
		}
		return blnFinish;
	}

	private void everyoneEatAndOffer()
	{
		ArrayList< Offer > alCurrentOffers = new ArrayList< Offer >();
		ArrayList< int[] > alEats = new ArrayList< int[] >();
		for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
		{
			// skip the player who has eaten all the skittles
			int[] aintTempEat = new int[ intColorNum ];
			Offer offTemp = new Offer( intPlayerIndex, intColorNum );
			if ( aplsPlayerStatus[ intPlayerIndex ].getTotalInHand() == 0 )
			{
				alEats.add( aintTempEat );
				alCurrentOffers.add( offTemp );
				continue;
			}
			aplyPlayers[ intPlayerIndex ].eat( aintTempEat );
			// process eat
			if ( aplsPlayerStatus[ intPlayerIndex ].checkCanEat( aintTempEat ) )
			{
				double dblHappinessUp = aplsPlayerStatus[ intPlayerIndex ].eat( aintTempEat );
				alEats.add( aintTempEat );
				aplyPlayers[ intPlayerIndex ].happier( dblHappinessUp );
			}
			else
			{
				double dblHappinessUp = aplsPlayerStatus[ intPlayerIndex ].randEat( aintTempEat );
				alEats.add( aintTempEat );
				aplyPlayers[ intPlayerIndex ].happier( dblHappinessUp );
				System.out.println( "Player #" + intPlayerIndex + ": You cannot eat these. Take them out of your mouth!" );
			}
			// process offer
			aplyPlayers[ intPlayerIndex ].offer( offTemp );
			if ( aplsPlayerStatus[ intPlayerIndex ].checkValidOffer( offTemp ) )
			{
				alCurrentOffers.add( offTemp );
			}
			else
			{
				System.out.println( "Player #" + intPlayerIndex + ": Invalid offer. Shame on you :)" );
				Offer offEmpty = new Offer( intPlayerIndex, intColorNum );
				alCurrentOffers.add( offEmpty );
			}
		}
		aintCurrentEats = alEats.toArray( new int[ 0 ][] );
		aoffCurrentOffers = alCurrentOffers.toArray( new Offer[ 0 ] );

		System.out.println( "******************************************" );
		System.out.println( "------------------------------------------");
		System.out.println( "Skittles consumption:" );
		for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
		{
			System.out.print( "Player #" + intPlayerIndex + ": [ " );
			String strInHand = "";
			int[] aintInHand = alEats.get( intPlayerIndex );
			for ( int intInHand : aintInHand )
			{
				strInHand += intInHand + ", ";
			}
			System.out.println( strInHand.substring( 0, strInHand.length() - 2 ) + " ]" );
		}
		System.out.println();
		System.out.println( "All offers:" );
		for ( Offer offTemp : aoffCurrentOffers )
		{
			System.out.println( offTemp.toString() );
		}
		System.out.println( "------------------------------------------");
		System.out.println( "******************************************\n" );
	}

	private int[] generateRandomOfferPickOrder()
	{
		ArrayList< Integer > alPlayerIndices = new ArrayList< Integer >();
		for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
		{
			alPlayerIndices.add( intPlayerIndex );
		}
		int[] aintOrder = new int[ intPlayerNum ];
		Random rdmGenerator = new Random();
//		System.out.println( "Random order is:" );
		for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
		{
			int intRandom = rdmGenerator.nextInt( intPlayerNum - intPlayerIndex );
			aintOrder[ intPlayerIndex ] = alPlayerIndices.get( intRandom );
			alPlayerIndices.remove( intRandom );
			System.out.print( aintOrder[ intPlayerIndex ] + " " );
		}
		System.out.println( "\n" );
		return aintOrder;
	}

	private void pickOfferInOrder( int[] aintOrder )
	{
		for ( int intOrderIndex = 0; intOrderIndex < intPlayerNum; intOrderIndex ++ )
		{
			int intPlayerIndex = aintOrder[ intOrderIndex ];
			//skip the player who has eaten all the skittles
			if ( aplsPlayerStatus[ intPlayerIndex ].getTotalInHand() == 0 )
			{
				continue;
			}
			Offer offPicked = aplyPlayers[ intPlayerIndex ].pickOffer( aoffCurrentOffers );
			if ( offPicked != null )
			{
				if ( offPicked.getOfferLive() == false )
				{
					System.out.println( "Offer has been picked, forget about it" );
				}
				else if ( !aplsPlayerStatus[ intPlayerIndex ].checkEnoughInHand( offPicked.getDesire() ) )
				{
					System.out.println( "Player #" + intPlayerIndex + ": you don't have enough skittles to accept this offer. Don't even think about it!" );
				}
				else if ( intPlayerIndex == offPicked.getOfferedByIndex() )
				{
					System.out.println( "Trade with yourself? Schizophrenia..." );
				}
				else
				{
					offPicked.setOfferLive( false );
					int intPickedByIndex = intPlayerIndex;
					int intOfferedByIndex = offPicked.getOfferedByIndex();
					offPicked.setPickedByIndex( intPickedByIndex );
					aplsPlayerStatus[ intOfferedByIndex ].offerExecuted( offPicked );
					aplyPlayers[ intOfferedByIndex ].offerExecuted( offPicked );
					aplsPlayerStatus[ intPickedByIndex ].pickedOffer( offPicked );
					// check after picking an offer, whether the offered offered by intPickedByIndex is still valid. if not, remove it
					Offer offOfferedByPicking = aoffCurrentOffers[ intPickedByIndex ];
					if ( offOfferedByPicking.getOfferLive() && !aplsPlayerStatus[ intPickedByIndex ].checkEnoughInHand( offOfferedByPicking.getOffer() ) )
					{
						offOfferedByPicking.setOfferLive( false );
					}
				}
			}
		}
	}

	private void broadcastOfferExcution()
	{
		for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
		{
			aplyPlayers[ intPlayerIndex ].updateOfferExe( aoffCurrentOffers );
		}

		System.out.println( "\n******************************************" );
		System.out.println( "------------------------------------------");
		System.out.println( "Offer execution: " );
		for ( Offer offTemp : aoffCurrentOffers )
		{
			if ( offTemp.getPickedByIndex() != -1 )
			{
				System.out.println( offTemp.toString() );
			}
		}
		System.out.println( "------------------------------------------");
		System.out.println( "******************************************\n" );

	}

	public static String arrayToString( int[] aintArray )
	{
		String strReturn = "[ ";
		for ( int intElement : aintArray )
		{
			strReturn += intElement + ", ";
		}
		strReturn = strReturn.substring( 0, strReturn.length() - 2 ) + " ]";
		return strReturn;
	}
	public  Integer factorial(Integer x) {
		Integer toReturn=1;
		for(int i=1; i<=x;i++) {
			toReturn*=i;
		}
		return toReturn;
		
	}
	public  void setAllCombinations(ArrayList<Integer> prefixList,ArrayList<Integer> stillToPlace) {
		// all combinations 0-max-1
		if(stillToPlace.isEmpty()) {
			//System.out.println(" permutation is "+currentIndex+"  value -->"+prefixList);
			Integer kk =0;
			for(Integer pp:prefixList) {
				factorialPermutations[currentIndex][kk]=pp;
				kk++;
			}
			//System.out.println("      is "+currentIndex+"  value -->"+factorialPermutations[currentIndex].toString());
					currentIndex++;
		}
		else
			for( int i =0 ;i<stillToPlace.size();i++) {
				
				ArrayList<Integer> tempListLeft= (ArrayList<Integer>)stillToPlace.clone();
				ArrayList<Integer> tempPrefixList= (ArrayList<Integer>)prefixList.clone();
				tempListLeft.remove(i);
				tempPrefixList.add(stillToPlace.get(i));
				setAllCombinations(tempPrefixList,tempListLeft);
			}
	}
	
	public void printArray(String message,double[] tempArray) {
		String printingString="";
		for (int i=0;i<tempArray.length;i++) {
			printingString=printingString+" , "+String.format("%+1.5f",tempArray[i]);
		}
		System.out.println(message+"  "+printingString);
	}
	public void printArray(String message,int[] tempArray) {
		String printingString="";
		for (int i=0;i<tempArray.length;i++) {
			printingString=printingString+" , "+String.format("%8d",tempArray[i]);
		}
//		System.out.println(message+"  "+printingString);
	}
	
	
}
