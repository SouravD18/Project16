package player;

public class PreFlop {
    double greatEquity = Constants.preFlopGreat;
    double goodEquity = Constants.preFlopGood;
    double badEquity = Constants.preFlopAverage;
 
    public String takeAction(ProcessActions action, double equity, int potSize, int turn){
        
        int callAmount = action.callAmount();
        double evForCall = (potSize)*equity - (callAmount)*(1-equity);
        
        if (equity >= greatEquity){
            // Raise Maximum.
            return action.bet(200);
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