package player;

public class Turn {
    /**
     * Simple version:
     *      We'll take account the following stuff:
     *      --> Expected Value for calling
     *      --> Equity
     *   Please modify constants after simulations
     */
    
    double reallyGoodEquity = .75;
    double goodEquity = .6;
    double averageEquity = .4;
 
    public String takeAction(ProcessActions action, double equity, int potSize){
        
        int callAmount = action.callAmount();
        double evForCall = (potSize)*equity - (callAmount)*(1-equity);
        
        if(equity >= reallyGoodEquity){
            // Raise Maximum.
            return action.bet(200);
        }
        else if(equity >= goodEquity){
            // Bet according to EV
            return action.bet(callAmount + (int) evForCall); 
        }
        else if(equity >= averageEquity){
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
