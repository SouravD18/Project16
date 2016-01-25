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
 
    public String takeAction(ProcessActions action, double equity, int potSize, int turn, boolean isButton){
        
        int callAmount = action.callAmount();
        double evForCall = (potSize)*equity - (callAmount)*(1-equity);
        
        if(equity >= reallyGoodEquity){
            // Raise Maximum.
            return action.bet(10000);
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
            if(!isButton){
                double eq = equity - Constants.reduceRiver;
                evForCall = (potSize)*eq - (callAmount)*(1-eq);
            }
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
    
    public String testAction(ProcessActions action, double equity, int potSize, int turn, Historian mister){
        double adjustedEquity = equity - Constants.adjustEquity; 
                
        double callExtraEquity = 0.0;
        double betExtraEquity =0.0;
        double foldExtraEquity = 0.0;
        
        double standardAggrFactor = Constants.standardAggrFactor;
        double standardSDWent = Constants.standardWentSD;
        double standardSDWin = Constants.standardWinSD;
        
        double differenceAggr = mister.aggressionFactor() - standardAggrFactor;
        double differenceSDWent = mister.sdWentPercent() - standardSDWent;
        double differenceSDWin = mister.sdWinPercent() - standardSDWin;
        
        // Fixing differenceAggr
        if(differenceAggr > Constants.aggrVariance){
            differenceAggr = Constants.aggrVariance;
        }
        else if(differenceAggr < - Constants.aggrVariance){
            differenceAggr = - Constants.aggrVariance;
        }
        
        // Fixing differenceSDWent
        if(differenceSDWent > Constants.wentSDVariance){
            differenceSDWent = Constants.wentSDVariance;
        }
        else if(differenceSDWent < -Constants.wentSDVariance){
            differenceSDWent = -Constants.wentSDVariance;
        }
        
        // Fixing differenceSDWin
        if(differenceSDWin > Constants.winSDVariance){
            differenceSDWin = Constants.winSDVariance;
        }
        else if(differenceSDWin < -Constants.winSDVariance){
            differenceSDWin = -Constants.winSDVariance;
        }
        // Determining Call Extra Equity
        callExtraEquity = (Constants.equityBarAggr) * (differenceAggr) / (Constants.aggrVariance)
                        -(Constants.equityBarWinSD) * (differenceSDWin) / (Constants.winSDVariance);
       
        // Determining Bet Extra Equity
        if(equity >= goodEquity){
            betExtraEquity = (Constants.equityBarWentSD) * (differenceSDWent) / (Constants.wentSDVariance);
        }
        // Extra opponent Fold Equity: 
        double opponentFoldEquity = mister.foldPercentInPost() * Constants.predictFold;
        betExtraEquity  = betExtraEquity + callExtraEquity +opponentFoldEquity;
        
        // Determining Fold Extra Equity
        foldExtraEquity = (Constants.equityBarWentSD) * (differenceSDWent) / (Constants.wentSDVariance)
                -(Constants.equityBarAggr) * (differenceAggr) / (Constants.aggrVariance)
                +(Constants.equityBarWinSD) * (differenceSDWin) / (Constants.winSDVariance);
        
        // Now, we are going to determine expected value for Call/ Bet/ Fold:
        double evForCall = potSize * (adjustedEquity+callExtraEquity) 
                        - action.callAmount() * (1 - adjustedEquity - callExtraEquity);
        
        double evForFold = potSize * (foldExtraEquity);
        
        // Now finding ev for bet is bit tricky. RN, I am averaging the bet.
        double averageBet = (action.minBet() + action.maxBet()) / 2.0;
        
        double evForBet = (potSize + averageBet) * (adjustedEquity + betExtraEquity)
                        - (averageBet + action.callAmount()) * (1 - adjustedEquity - betExtraEquity);
        System.out.println("eq: "+equity);
        System.out.println("After adjusting: "+adjustedEquity);
        System.out.println("evForBest: "+evForBet);
        System.out.println("evForCall: "+evForCall);
        System.out.println("evForFold: "+evForFold);
        
        // Now check ev and call/ bet / fold accordingly:
        if(evForBet > evForCall && evForBet > evForFold){
            // RN, simple bet function
            return action.bet((int) evForBet);            
        }
        
        else if( evForFold > evForCall){
            return action.fold();
        }
        else{
            return action.call();
        }
        
    }
}
