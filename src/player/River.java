package player;

public class River {
    /**
     * Simple version:
     *      We'll take account the following stuff:
     *      --> Expected Value for calling
     *      --> Equity
     *   Please modify constants after simulations
     */
    
    double reallyGoodEquity = .85;
    double goodEquity = .65;
    double averageEquity = .5;
 
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
