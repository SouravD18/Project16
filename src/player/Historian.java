package player;

import java.util.Arrays;

public class Historian {
    public int numHands = 0;
    public int[] preFlopTypes = new int[5];
    public int numBigBets;
    
    public double preFlopTypePercentile(int index){
        if (numHands == 0)
            return 0;
        
        int sum = 0;
        for (int i = 0; i < index; i++){
            sum += preFlopTypes[i];
        }
        sum += preFlopTypes[index]/2;
        return ((double) sum )/ ((double)numHands/2);
    }
    
    public double preFlopTypeExact(int index){
        if (numHands == 0)
            return 0;
        
        return ((double) preFlopTypes[index] )/ ((double)numHands/2);
    }
    
    public double bigBetFrequency(){
        if (numHands == 0)
            return 0;
        return ((double) numBigBets) / numHands;
    }
    
    public void printAll(){
        System.out.println("Number of hands is " + numHands);
        System.out.println("Preflop types looks like " + Arrays.toString(preFlopTypes));
        System.out.println("Number of big bets is " + numBigBets);
    }
}
