package player;

public class PreFlop {
    double greatEquity = Constants.preFlopGreat;
    double goodEquity = Constants.preFlopGood;
    double badEquity = Constants.preFlopAverage;
    
    double standardThreeBet = Constants.preFlop3BetStandard;
    double threeBetVariance = Constants.preFlop3betVariance;
    double pfr = Constants.pfrPercent;
    double pfrVariance = Constants.pfrVariance;
    
    public String takeAction(ProcessActions action, double equity, int potSize, int turn, boolean isButton){
        int callAmount = action.callAmount();
        double evForCall = (potSize)*equity - (callAmount)*(1-equity);
        
        if (equity >= greatEquity){
            // Raise Maximum.
            return action.bet(2000000);
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
    
    public String testAction(boolean isButton, int bettingTurn, 
            double equity, int potSize, ProcessActions process, Historian mister){
        
        int callAmount = process.callAmount();
        double evForCall = (potSize)*equity - (callAmount)*(1-equity);
        int betAmount  = callAmount + (int) evForCall;

        // First, divide in 2 cases: button and non-button:
        
        //  Small Blind
        if(isButton){
                  // Now, use bet turn counter:
            //  pfr Chance
            if(bettingTurn == 1){
                //  Only need to consider equity and hand ranges that we want to play:
                
                // Assuming excessThreeBet is in range (-.15,.15)
                double excessThreeBet = standardThreeBet - mister.threeBetPercent();
                if(excessThreeBet > threeBetVariance){
                    excessThreeBet = threeBetVariance;
                }
                else if(excessThreeBet < -threeBetVariance){
                    excessThreeBet = -threeBetVariance;
                }
                
                // If excess is positive, make ranges high:
                    // Great Hand
                if(equity >= greatEquity){
                    // raise randomly from 5-6 to disguise 
                    double prob = Math.random();
                    if(prob > .5){
                        return process.bet(6);
                    }
                    else{
                        return process.bet(5);
                    }
                }
                    // Shit Hand
                else if(equity < badEquity){
                    return process.check();
                }
                    // Good Hand
                else if(equity >= goodEquity){
                    double prob = Math.random();
                    double adjustedBarForCalling = pfr - threeBetVariance;
                    
                    if(prob > adjustedBarForCalling){
                        return process.call();
                    }
                    else{
                        if(prob > (adjustedBarForCalling)/2){
                            return process.bet(6);
                        }
                        else{
                            return process.bet(4);
                        }
                    }
                }
                    // Average Hand
                else if(equity >= badEquity){
                    double prob = Math.random();
                    double adjustedBarForCalling = pfr + 2 * threeBetVariance;
                    
                    if(prob > adjustedBarForCalling){
                        return process.call();
                    }
                    else{
                        if(prob > (adjustedBarForCalling)/2){
                            return process.bet(5);
                        }
                        else{
                            return process.bet(4);
                        }
                    }
                }
                else{
                    return "Should not reach here";
                }

            }
            //  4-Bet Chance 
            else if(bettingTurn == 2){
                // Probably need to modify later
                if(equity >= greatEquity){
                    return process.bet(200);
                }
                else{
                    return process.call();
                }
            }
            //  Everything else 
            else{
             // Probably need to modify later
                if(equity >= greatEquity){
                    return process.bet(200);
                }
                else{
                    return process.call();
                }
            }
        }
        //  Big Blind
        else {
                //  Now, use bet turn counter:
            //  3-bet chance
            if(bettingTurn == 1){
                double excessPFR = pfr - mister.pfrPercent();
                if(excessPFR > pfr){
                    excessPFR = pfr;
                }
                else if(excessPFR < -pfr){
                    excessPFR = -pfr;
                }
                 // If excess is positive, make ranges high:
                    // Great Hand
                if(equity >= greatEquity){
                    // If pfr is low then bet small:
                    if(excessPFR < -.10){
                        return process.bet(2); //bet minimum
                    }
                    else{
                       return process.bet(betAmount);
                    }
                }
                    // Shit Hand, Check
                else if(equity <= badEquity){
                    return process.check();
                }
                    // Good Hand
                else if(equity >= goodEquity){
                    double prob = Math.random();
                    double adjustedBarForCalling = standardThreeBet - pfrVariance;
                    
                    if(prob > adjustedBarForCalling){
                        return process.call();
                    }
                    else{
                        return process.bet(betAmount);
                    }
                }
                    //Average Hand
                else if(equity >= badEquity){
                    double prob = Math.random();
                    double adjustedBarForCalling = standardThreeBet - pfrVariance;
                    
                    if(prob > adjustedBarForCalling){
                        return process.bet(betAmount+1);
                    }
                    else{
                        if(betAmount > 0){
                            return process.call();
                        }
                        else{
                            return process.check();
                        }
                    }
                }
                else{
                    return "Should not reach here";
                }
            }
            //  Counter 4-bets
            else if(bettingTurn == 2){
             // Probably need to modify later
                if(equity >= greatEquity){
                    return process.bet(200);
                }
                else{
                    return process.call();
                }
            }
            //  Everything else
            else{
             // Probably need to modify later
                if(equity >= greatEquity){
                    return process.bet(200);
                }
                else{
                    return process.call();
                }
            }
        }
    }
}