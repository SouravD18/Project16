package player;

import java.util.Arrays;

public class Historian {
    public int numHands = 0;
    public int[] preFlopTypes = new int[4];
    public int numBigBets = 0;
    public int foldPreFlop = 0;
    public int foldFlop = 0;
    public int foldTurn = 0;
    public int foldRiver = 0;
    public int reachedFlop = 0;
    public int reachedTurn = 0;
    public int reachedRiver = 0;

    public double preFlopTypePercentile(int index){
        if (numHands == 0)
            return 0;

        int sum = 0;
        for (int i = preFlopTypes.length-1; i > index; i--){
            sum += preFlopTypes[i];
        }
        sum += preFlopTypes[index]/2;
        return 1 - ((double) sum )/ ((double)numHands/2);
    }

    public double bigBetFrequency(){
        if (numHands == 0)
            return 0;
        return ((double) numBigBets) / numHands;
    }

    public double foldPreFlopFrequency(){
        if (numHands == 0)
            return 0;
        return ((double) foldPreFlop) / numHands;
    }

    public double foldFlopFrequency(){
        if (reachedFlop == 0)
            return 0;
        return ((double) foldFlop) / reachedFlop;
    }

    public double foldTurnFrequency(){
        if (reachedTurn == 0)
            return 0;
        return ((double) foldTurn) / reachedTurn;
    }

    public double foldRiverFrequency(){
        if (reachedRiver == 0)
            return 0;
        return ((double) foldRiver) / reachedRiver;
    }

    public double foldFrequency(int turnNum){
        switch (turnNum){
        case 3:
            return foldFlopFrequency();
        case 4:
            return foldTurnFrequency();
        case 5:
            return foldRiverFrequency();
        default:
            return 0.2;
        }
    }
    
    public void halveEverything(){
        numHands /= 2;
        for (int i = 0; i < preFlopTypes.length; i++){
            preFlopTypes[i] /= 2;
        }
        numBigBets /= 2;
        foldPreFlop /= 2;
        foldFlop /= 2;
        foldTurn /= 2;
        foldRiver /= 2;
        reachedFlop /= 2;
        reachedTurn /= 2;
        reachedRiver /= 2;
    }

    public void printAll(){
        System.out.println("Number of hands is " + numHands);
        System.out.println("Preflop types looks like " + Arrays.toString(preFlopTypes));
        System.out.println("Number of big bets is " + numBigBets);
        System.out.println("Number of folds preFlop is " + foldPreFlop);
        System.out.println("Number of folds Flop is " + foldFlop);
        System.out.println("Number of folds Turn is " + foldTurn);
        System.out.println("Number of folds River is " + foldRiver);
        System.out.println("Number of hands that reached flop is " + reachedFlop);
        System.out.println("Number of hands that reached turn is " + reachedTurn);
        System.out.println("Number of hands that reached river is " + reachedRiver);

    }
}
