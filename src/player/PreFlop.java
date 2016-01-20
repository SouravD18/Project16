package player;

public class PreFlop {
    double greatEquity = .7;
    double goodEquity = .6;
    double standardEquity = .5;
    double badEquity = .375;
 
    public String takeAction(ProcessActions action, double equity, int potSize){
        
        int callAmount = action.callAmount();
        double evForCall = (potSize)*equity - (callAmount)*(1-equity);
        
        if (equity >= greatEquity){
            // Raise Maximum.
            return action.bet(200);
        }
        else if (equity >= goodEquity){
            // Bet according to EV
            return action.bet(callAmount + (int) evForCall); 
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