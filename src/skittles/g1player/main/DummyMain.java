package skittles.g1player.main;

import skittles.g1player.G1Player;
import skittles.g1player.Infobase;


public class DummyMain {

	public static void main(String[] args) {
		int[] aintInHand = new int[]{6,5,50,5,6};
		int[] aintTempEat = new int[aintInHand.length];
		
		G1Player g1Player = new G1Player();
		g1Player.initialize(3, 0.0, 1, "g1player", aintInHand);
		
		Infobase info = new Infobase();
		info.setAintInHand(aintInHand);
		
		aintTempEat = new int[aintTempEat.length];
		g1Player.eat(aintTempEat);
		info.getPriority().updatePriority(info.getIntLastEatIndex(), 0.5, info);
		printArray(aintTempEat, "aintTempEat");
		
		aintTempEat = new int[aintTempEat.length];
		g1Player.eat(aintTempEat);
		info.getPriority().updatePriority(info.getIntLastEatIndex(), 1.0, info);
		printArray(aintTempEat, "aintTempEat");

		aintTempEat = new int[aintTempEat.length];
		g1Player.eat(aintTempEat);
		info.getPriority().updatePriority(info.getIntLastEatIndex(), 0.2, info);
		printArray(aintTempEat, "aintTempEat");
		
		aintTempEat = new int[aintTempEat.length];
		g1Player.eat(aintTempEat);
		info.getPriority().updatePriority(info.getIntLastEatIndex(), 0.0, info);
		printArray(aintTempEat, "aintTempEat");

		aintTempEat = new int[aintTempEat.length];
		g1Player.eat(aintTempEat);
		info.getPriority().updatePriority(info.getIntLastEatIndex(), 1.0, info);
		printArray(aintTempEat, "aintTempEat");		
	}
	
	public static void printArray(int[] array, String arrayName){
		int tLength;
		tLength= array.length;
		System.out.print("" + arrayName + ": ");
		for(int i=0; i<tLength; i++){
			System.out.print(array[i] + ", ");
		}
		System.out.println();
	}
	
	public static <T> void printArray(T[] array, String arrayName){
		int tLength = array.length;
//		System.out.print("" + arrayName + ": ");
//		for(int i=0; i<tLength; i++){
//			System.out.print(array[i] + ", ");
//		}
//		System.out.println();
	}
	
}
