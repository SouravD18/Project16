package player;

public class PreFlop {
    static double greatEquity = Constants.preFlopGreat;
    static double goodEquity = Constants.preFlopGood;
    static double badEquity = Constants.preFlopAverage;
    
    public static String takeAction(ProcessActions action, double equity, int potSize, int turn, boolean isButton){
        int callAmount = action.callAmount();
        double evForCall = (potSize)*equity - (callAmount)*(1-equity);
        
        if (equity >= greatEquity){
            double r = Math.random();
            if (turn == 1){
                if (r > 0.8){
                    return action.bet(Brain.maxStackSize); //20% we raise full
                } else if (r > 0.4) {
                    return action.bet(4); //40% we raise partially
                } else {
                    return action.bet(3); //40% we raise only a little
                }
            } else { //turn >= 2
                return action.bet(Brain.maxStackSize); //if the opponent wants to raise, we up him!
            }
        }
        else if (equity >= goodEquity){
            // Bet according to EV
            // Will not 2nd-bet!! (Safety reasons)
            if(turn < 2){
                return action.bet(callAmount + (int) evForCall);
            }
            else{
                return action.call();
            }
        }
        else if (equity >= badEquity){
            // Just call if ev > 0
            if(!isButton){
                double eq = equity - Constants.reducePreflop;
                evForCall = (potSize)*eq - (callAmount)*(1-eq);
            }
            if(evForCall > 0){
                return action.call();
            }
            else {
                return action.check();
            }
        }
        else {
            // Check or fold
            return action.check();
        }
    }
}