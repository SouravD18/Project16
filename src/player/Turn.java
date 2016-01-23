package player;

public class Turn {
    /**
     * Simple version:
     *      We'll take account the following stuff:
     *      --> Expected Value for calling
     *      --> Equity
     *   Please modify constants after simulations
     */
    
    double reallyGoodEquity = Constants.turnGreat;
    double goodEquity = Constants.turnGood;
    double averageEquity = Constants.turnAverage;
 
    public String takeAction(ProcessActions action, double equity, int potSize, int turn, 
            Historian mister, boolean isButton){
        
        int callAmount = action.callAmount();
        double evForCall = (potSize)*equity - (callAmount)*(1-equity);
        
        double excessFirstAggr = (mister.turnBettingFrequencies() - Constants.avgFirstAggr) *
                            Constants.aggrScale;
        double excessCheckRaising = (mister.turnCheckRaiseFrequencies() - Constants.avgCheckRaising) *
                            Constants.checkRaiseScale;
        double excessFolding = (mister.turnFoldingFrequencies() - Constants.avgFolding) *
                            Constants.foldingScale;
        
        if(equity >= reallyGoodEquity){
            
            if(excessFolding > 0 && excessCheckRaising < 0 && !isButton && turn == 1) {
                return action.call();
            }
            else if(excessFolding > 0.2 && turn == 1){
                if(action.betPossible){
                    return action.bet( action.minBet() + (int)excessFolding * 8);
                }
                else{
                    return action.bet((int) evForCall);
                }
            }
            if(turn == 1){
                return action.bet((int) evForCall);
            }
            return action.bet(10000000);
        }
        else if(equity >= goodEquity){
            if(excessFolding > 0 && excessCheckRaising < 0 && !isButton && turn == 1) {
                return action.call();
            }
            if(turn < 2){
                return action.bet(callAmount + (int) evForCall);
            }
            else{
                return action.call();
            }
        }
        else if(equity >= averageEquity){
         // Button turn 1 check
            if(turn == 1 && isButton && action.checkPossible){
                double foldEquity = excessFolding - excessCheckRaising;
                double polarity = Constants.polar - equity;
                double eq = polarity + foldEquity;
                double evForBet = (potSize)*eq - (callAmount)*(1-eq);
                
                if(foldEquity + polarity >= goodEquity){
                    return action.bet((int) evForBet + 1);
                }
            }
         // Non-Button raising
            if(turn == 1 && !isButton){
                double foldEquity = excessFolding - .5 * excessFirstAggr;
                double polarity = Constants.polar - equity;
                double eq = polarity + foldEquity;
                double evForBet = (potSize)*eq - (callAmount)*(1-eq);
                
                if(foldEquity + polarity >= goodEquity){
                    return action.bet((int) evForBet + 1);
                }
            }
            double eqForCall = excessFirstAggr;
            double eq = equity + eqForCall;
            double evCall = (potSize) * eq - (callAmount)*(1-eq);
            // Just call if ev > 0
            if(evCall > 0){
                return action.call();
            }
            else{
                return action.check();
            }
        }
        else{
            // Button turn 1 check
            if(turn == 1 && isButton && action.checkPossible){
                double foldEquity = excessFolding - excessCheckRaising;
                double polarity = Constants.polar - equity;
                double eq = polarity + foldEquity;
                double evForBet = (potSize)*eq - (callAmount)*(1-eq);
                
                if(foldEquity + polarity >= goodEquity){
                    return action.bet((int) evForBet + 1);
                }
            }
            // Non-Button raising
            if(turn == 1 && !isButton){
                double foldEquity = excessFolding - .5 * excessFirstAggr;
                double polarity = Constants.polar - equity;
                double eq = polarity + foldEquity;
                double evForBet = (potSize)*eq - (callAmount)*(1-eq);
                
                if(foldEquity + polarity >= goodEquity){
                    return action.bet((int) evForBet + 1);
                }
            }
            return action.check();
        }
    }
    
    public String testAction(ProcessActions action, double equity, int potSize, 
            int turn, Historian mister, boolean isButton){
        
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
                
        betExtraEquity  = betExtraEquity + callExtraEquity + opponentFoldEquity;
        
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
