package player;

public class Flop {
    static double greatEquity = Constants.flopGreat;
    static double goodEquity = Constants.flopGood;
    static double averageEquity = Constants.flopAverage;
 
    public static String takeAction(ProcessActions action, double equity, int potSize, int turn, boolean isButton, Historian mister){
        int callAmount = action.callAmount();
        double evForCall = (potSize)*equity - (callAmount)*(1-equity);
        
        if(equity >= greatEquity){
            // Raise Maximum.
            return action.bet(Brain.maxStackSize);
        }
        else if (equity >= goodEquity){
            // Bet according to EV
            // For safety: No 2nd bet
            
            if (turn < 2){
                return action.bet(callAmount + (int) evForCall);
            }
            else {
                return action.call();
            }
        }
        else if (equity >= averageEquity){
            // Just call if ev > 0
            if(!isButton){
                double eq = equity;
                evForCall = (potSize)*eq - (callAmount)*(1-eq);
            }
            if(evForCall > 0){
                return action.call();
            }
            else{
                return action.check();
            }
        }
        else{
            // Check or fold
            return action.check();
        }
    }
}

