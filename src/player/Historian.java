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
    public int ultimateGames = 0;
    public int ultimateWins = 0;

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
        double returnVal =  ((double) numBigBets) / numHands;
        if (returnVal < 0.05)
            return 0.05;
        return returnVal;
        
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
    
    public double ultimateOpponentWinFrequency(){
        if (ultimateGames == 0)
            return 0;
        return ((double) ultimateWins) / ultimateGames;
    }
    
    public void ultimateRaiseStandards(){
        numBigBets = (int) (0.75 * numBigBets);
        ultimateGames = 0;
        ultimateWins = 0;
    }
    
    public void clear(){
        numHands = 0;
        preFlopTypes = new int[4];
        numBigBets = 0;
        foldPreFlop = 0;
        foldFlop = 0;
        foldTurn = 0;
        foldRiver = 0;
        reachedFlop = 0;
        reachedTurn = 0;
        reachedRiver = 0;
        ultimateGames = 0;
        ultimateWins = 0;
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
        System.out.println("Number of ultimate games that the opponent won was " + ultimateWins);
        System.out.println("Number of ultimate games total was " + ultimateGames);
    }
}
