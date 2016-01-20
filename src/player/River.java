package player;

public class River {
    /**
     * Simple version:
     *      We'll take account the following stuff:
     *      --> Expected Value for calling
     *      --> Equity
     *   Please modify constants after simulations
     */
    
    double reallyGoodEquity = Constants.riverGreat;
    double goodEquity = Constants.riverGood;
    double averageEquity = Constants.riverAverage;
 
    public String takeAction(ProcessActions action, double equity, int potSize, int turn){
        
        int callAmount = action.callAmount();
        double evForCall = (potSize)*equity - (callAmount)*(1-equity);
        
        if(equity >= reallyGoodEquity){
            // Raise Maximum.
            return action.bet(200);
        }
        else if(equity >= goodEquity){
            // Bet according to EV
            // For safety: No 2nd bet
            if(turn < 2){
                return action.bet(callAmount + (int) evForCall);
            }
            else{
                return action.call();
            }
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
