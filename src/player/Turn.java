package player;

import equity.poker.Main;

public class Turn {
    static double greatEquity = Constants.turnGreat;
    static double goodEquity = Constants.turnGood;
    static double badEquity = Constants.turnAverage;

    public static String takeAction(ProcessActions action, double equity, int potSize, int turn, boolean isButton, Historian mister, int betType){
        int callAmount = action.callAmount();
        double evForCall = (potSize)*equity - (callAmount)*(1-equity);
        
        double r = Math.random();

        if (Brain.handsIn < 175){ //not enough info yet, play default
            if (turn == 1){

                if (!isButton){ //We're the BB and act first here
                    if(equity >= greatEquity){
                        return action.bet(Brain.maxStackSize);
                    }
                    else if (equity >= goodEquity){
                        return action.bet(callAmount + (int) evForCall);
                    }
                    else if (equity >= badEquity){
                        if(evForCall > 0){
                            return action.call();
                        } else {
                            return action.check();
                        }
                    } else {
                        return action.check();
                    }


                } else { //we're the SB and act second
                    if (equity >= greatEquity){
                        if (action.raisePossible){ //the opponent has raised
                            return action.bet(Brain.maxStackSize);
                        } else { //the opponent has just called
                            return action.bet(4); //we'll bet low here
                        }
                    } else if (equity >= goodEquity){
                        if (action.raisePossible){ //opponent raised
                            return action.bet(callAmount + (int) evForCall);
                        } else { //opponent has called
                            return action.call();
                        }
                    } else if (equity >= badEquity && evForCall > 0){
                        return action.call();
                    } else { //terrible equity or negative EV
                        return action.check();
                    }
                }

            } else { //turn >= 2, the opponent must have raised here
                if (equity >= greatEquity){
                    return action.bet(Brain.maxStackSize); //we take on the raise!
                } else if (equity >= goodEquity || (equity >= badEquity && evForCall > 0)){
                    return action.call();
                } else {
                    return action.check();
                }
            }


        } else { //play 4 real now
            double avgPercentileOfOpponentPlayedHands;
            double foldFreq = mister.turnFoldingFrequencies();
            double betFreq = mister.turnBettingFrequencies();
            double highBetFreq = mister.highBetPercent();
            double veryHighBetFreq = mister.veryHighBetPercent();
            double ourPercentile = Main.convertEquityToPercentile(equity, 0);
            if (!isButton && turn == 1){ //we act first here

                if (equity >= greatEquity){
                    if (r < (1-foldFreq)){
                        return action.bet(Brain.maxStackSize); //1-foldFreq that we raise completely
                    } else if (r < (foldFreq)/2 + (1-foldFreq)) {
                        return action.bet(4); //foldFreq/2 we raise partially
                    } else {
                        return action.call(); //rest we just call
                    }
                } else if (equity >= goodEquity){
                    return action.bet(callAmount + (int) evForCall);
                } else if (equity >= badEquity){
                    if(evForCall > 0){
                        return action.call();
                    } else {
                        return action.check();
                    }
                } else { //terrible equity
                    return action.check();
                }

            } else { //we're acting in response to something
                if (action.raisePossible){
                    if (betType == 0){ //regular bet
                        avgPercentileOfOpponentPlayedHands = 1 - (betFreq + highBetFreq)/2;
                    } else if (betType == 1){ //mid high bet
                        avgPercentileOfOpponentPlayedHands = 1 - (highBetFreq + veryHighBetFreq)/2;
                    } else { //VERY HIGH BET
                        avgPercentileOfOpponentPlayedHands = 1 - veryHighBetFreq/2;
                    }
                } else if (action.betPossible){ //the opponent has just called, he's somewhere in the middle
                    avgPercentileOfOpponentPlayedHands = (foldFreq + (1 - betFreq)) / 2;
                } else {
                    return action.check();
                }
                //XXX: Important: we have to transform the percentiles upwards to account for folding in preFlop
                double wentToTurnFreq = mister.wentToTurnFrequency();
                avgPercentileOfOpponentPlayedHands = 1 - ((1 - avgPercentileOfOpponentPlayedHands) * wentToTurnFreq);

                System.out.println("Our percentile is " + ourPercentile);
                System.out.println("We think the opponent's percentile is around " + avgPercentileOfOpponentPlayedHands);
                
                
                if (ourPercentile - avgPercentileOfOpponentPlayedHands > 0.05) //our cards are probably better than theirs
                    return action.bet(Brain.maxStackSize); 
                if (ourPercentile - avgPercentileOfOpponentPlayedHands > -0.05) //our cards are around even
                    return action.call();
                else return action.check(); //our cards are significantly worse, should probably fold
            }
        }
    }
}
