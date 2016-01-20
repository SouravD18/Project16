package player;

public class Flop {
    
    double reallyGoodEquity = Constants.flopGreat;
    double goodEquity = Constants.flopGood;
    double averageEquity = Constants.flopAverage;
 
    public String takeAction(ProcessActions action, double equity, int potSize, int turn){
        
        int callAmount = action.callAmount();
        double evForCall = (potSize)*equity - (callAmount)*(1-equity);
        
        if(equity >= reallyGoodEquity){
            // Raise Maximum.
            return action.bet(200);
        }
        else if (equity >= goodEquity){
            // Bet according to EV
            // For safety: No 2nd bet
            if(turn < 2){
                return action.bet(callAmount + (int) evForCall);
            }
            else{
                return action.call();
            }
        }
        else if (equity >= averageEquity){
            // Just call if ev > 0
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

