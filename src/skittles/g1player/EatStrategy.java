package skittles.g1player;

import java.util.ArrayList;

import skittles.g1player.main.DummyMain;

public class EatStrategy {

	/**
	 * finds the color we want to eat at every round.
	 * @param aintTempEat
	 * @param info
	 */
	public void update(int[] aintTempEat, Infobase info) {

//		aintTempEat = new int[aintTempEat.length];
		info.count++;
		int[] initialPriority = info.getPriority().getPriorityArray();
		int[] initialPriorityForEat = info.getPriority().getInitialPriorityForEat();
//		boolean isWeightedPriorityComplete = info.getPriority().isWeightedPriorityComplete();
		
		// decide if we do not require any more offers for all the colors - eat all 
		// u only have the skittles u desired  to have and u do not have any other skittle
		int[] aintInHand = info.getAintInHand();
		if(G1Player.DEBUG){
			DummyMain.printArray(aintInHand, " [--- aintinhand ---] ");
		}
		ArrayList<Integer>  desiredVector = info.getPriority().getDesiredVector(info);
		if(G1Player.DEBUG){
			System.out.println(" [--- desiredVecotr ---] " + desiredVector);
		}
		
//		for(int i=0 ; i<aintInHand.length; i++){
//			if(!desiredVector.contains(i) && aintInHand[i]!=-1){ // except for the desired color  check if ny other left
//				complete = false;
//			}
//		}

		//TODO decide if we dont require any  more offers for a particular color 
		boolean isInitial = false;	
		//if all desired colors gathered - eat one by one
		boolean isWeightedPriorityCompleteTemp = true;
		for(int i=0; i<initialPriorityForEat.length ; i++){
			if(initialPriorityForEat[i] != -1){
				isWeightedPriorityCompleteTemp = false;
				break;
			}
		}
		System.out.print("");
		
	    if(info.isPlayerInactive(-1) || info.isPlayerInactive(info.getIntPlayerIndex())){
			/*
			 * if either all players are inactive, or I am inactive, then
			 * eat all the skittles starting from the least priority color.
			 * If color has negative happiness, eat one by one.
			 */
	    	if(G1Player.DEBUG){
	    		System.out.println(" >>[EatStrategy] [update] complete");
	    	}
	    	
	    	for(int i=0; i<aintInHand.length; i++){
	    		int colorIndex = initialPriority[i];
	    		if(info.getColorHappiness(colorIndex) < 0 && aintInHand[colorIndex] > 0){
	    			aintTempEat[colorIndex] = 1;
	    			aintInHand[colorIndex]--;
	    			if(G1Player.DEBUG){
	    				System.out.println(" >>[EatStrategy] [update] complete - Negatives present, eating one by one - colorIndex=" + colorIndex);
	    	    	}
	    			checkIsRoundComplete(info);
	    			return;
	    		}
	    	}
	    	
	    	for(int i=aintInHand.length-1 ; i>=0; i--){
	    		int colorIndex = initialPriority[i];
				if(info.getColorHappiness(colorIndex) >= 0 && aintInHand[colorIndex] > 0){
					aintTempEat[colorIndex] = aintInHand[colorIndex];
					aintInHand[colorIndex] = 0;
					if(G1Player.DEBUG){
						System.out.println(" >>[EatStrategy] [update] complete - Negatives NOT present, eating all - colorIndex=" + colorIndex);
					}
					checkIsRoundComplete(info);
					return;
				}
			}
			
		} else if(!isWeightedPriorityCompleteTemp){ //for initial n/2  rounds check for colors not tasted according to priority queue
			
			if(G1Player.DEBUG){
				System.out.println(" >>[EatStrategy] [update] !complete and !isWeightedPriorityComplete");
			}
			for(int i=0; i<initialPriorityForEat.length ; i++){
				
				if(initialPriorityForEat[i] != -1){
					if(aintInHand[initialPriorityForEat[i]] > 0){
						aintTempEat[initialPriorityForEat[i]] = 1;
						aintInHand[initialPriorityForEat[i]]--;
						initialPriorityForEat[i] = -1;
						isInitial = true;
						break;
					}
					initialPriorityForEat[i] = -1;	
				}
			}
			
		} 
	    
	    if(!isInitial){
			
	    	if(G1Player.DEBUG){
	    		System.out.println(" >>[EatStrategy] [update] complete and isWeightedPriorityComplete");
	    	}
	    	
			int eatIndex = -1;
			double eatHappiness = 10000;
			for(int i=info.getDesiredColorCount(); i<initialPriority.length; i++){
				if(info.getColorHappiness(initialPriority[i]) >= 0 && info.getColorHappiness(initialPriority[i]) < eatHappiness){
					if(aintInHand[initialPriority[i]] > 0){
						eatIndex = initialPriority[i];
						eatHappiness = info.getColorHappiness(initialPriority[i]);
					}
						
				}
			}
			
			if(eatIndex != -1){
				aintTempEat[eatIndex] = 1;
				aintInHand[eatIndex]--;
				checkIsRoundComplete(info);
				return;
			} else {
				
				/*
				 * till now what we had did is we eat a color not in 'C'. 
				 * if we are not able to eat anything that is NOT IN 'C', then we move on the 'C'.
				 * Now, if we have negative colors in 'C', then we eat one by one, highest negative(closest to zero) first.
				 * Otherwise we eat the whole bunch, stating with the lowest proirity first.
				 */
				for(int i=0; i<aintInHand.length; i++){
		    		int colorIndex = initialPriority[i];
		    		if(info.getColorHappiness(colorIndex) < 0 && aintInHand[colorIndex] > 0){
		    			aintTempEat[colorIndex] = 1;
		    			aintInHand[colorIndex]--;
		    			if(G1Player.DEBUG){
		    				System.out.println(" >>[EatStrategy] [update] (toEat == -1) - Negatives present, eating one by one - colorIndex=" + colorIndex);
		    			}
		    			checkIsRoundComplete(info);
		    			return;
		    		}
		    	}
		    	
		    	for(int i=aintInHand.length-1 ; i>=0; i--){
		    		int colorIndex = initialPriority[i];
					if(info.getColorHappiness(colorIndex) >= 0 && aintInHand[colorIndex] > 0){
						aintTempEat[colorIndex] = aintInHand[colorIndex];
						aintInHand[colorIndex] = 0;
						if(G1Player.DEBUG){
							System.out.println(" >>[EatStrategy] [update] (toEat == -1) - Negatives NOT present, eating all - colorIndex=" + colorIndex);
						}
						checkIsRoundComplete(info);
						return;
					}
				}
				/*

				// changed by Erica
				// I added in the toEat and numToEat check because the loop was running
				// and for every skittle we had left it was adding it to the aintTempEat
				// array, meaning we were trying to eat multiple colors
				// so the toEat keeps track of which one we plan to eat, and the numToEat 
				// is how many of that skittle we have.  The loop updates those, and then
				// only assigns what we will eat at the end.  This will hopefully fix the 
				// trying to eat multiple colors bug.  
				
				// right now it's choosing to eat the skittle we have the least of first.
				// We might want to change this, but I thought it would be an ok way to decide
				// for now at least.
				int toEat = -1;
				int numToEat = Integer.MAX_VALUE;
				for(int  i=info.getDesiredColorCount()-1; i>=0; i--){
					DummyMain.printArray(initialPriority, " [EatStrategy] [] initialPriority");
					DummyMain.printArray(aintInHand, " [EatStrategy] [] aintInHand");
					System.out.print(" [EatStrategy] [] desiredColorCount=" + info.getDesiredColorCount() + ", i=" +i);
					System.out.println(", aintInHand[initialPriority[i]]=" + aintInHand[initialPriority[i]] + ", numToEat=" + numToEat);
					
					if(aintInHand[initialPriority[i]] > 0 && aintInHand[initialPriority[i]] <= numToEat){
						System.out.println(" [EatStrategy] [] insideif");
						numToEat = aintInHand[initialPriority[i]];
						toEat = initialPriority[i];
						System.out.print("");
					}
				}
				
				if(toEat == -1){
					for(int i=0; i<aintInHand.length; i++){
			    		int colorIndex = initialPriority[i];
			    		if(info.getColorHappiness(colorIndex) < 0 && aintInHand[colorIndex] > 0){
			    			aintTempEat[colorIndex] = 1;
			    			aintInHand[colorIndex]--;
			    			System.out.println(" >>[EatStrategy] [update] (toEat == -1) - Negatives present, eating one by one - colorIndex=" + colorIndex);
			    			return;
			    		}
			    	}
			    	
			    	for(int i=aintInHand.length-1 ; i>=0; i--){
			    		int colorIndex = initialPriority[i];
						if(info.getColorHappiness(colorIndex) >= 0 && aintInHand[colorIndex] > 0){
							aintTempEat[colorIndex] = aintInHand[colorIndex];
							aintInHand[i] = 0;
							System.out.println(" >>[EatStrategy] [update] (toEat == -1) - Negatives NOT present, eating all - colorIndex=" + colorIndex);
							return;
						}
					}
				}
				aintTempEat[toEat] = aintInHand[toEat];
				aintInHand[toEat] = 0;
				*/
			}
			
		}
	    
	    checkIsRoundComplete(info);
	}

	/**
	 * checks if the round is complete after eat and updates the value to InfoBase.  
	 * @param info
	 */
	private void checkIsRoundComplete(Infobase info) {
		info.roundComplete = true;
	    for (int i = 0; i<info.getAintInHand().length; i++){
	    	if(info.getAintInHand()[i] != 0){
	    		info.roundComplete = false;
	    	}
	    }
	}

}
