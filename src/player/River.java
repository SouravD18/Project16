package player;

import equity.poker.Main;

public class River {
    static double greatEquity = Constants.riverGreat;
    static double goodEquity = Constants.riverGood;
    static double badEquity = Constants.riverAverage;
 
    public static String takeAction(ProcessActions action, double equity, int potSize, int turn, boolean isButton, Historian historian, boolean hadBigBet){
        int callAmount = action.callAmount();
        double evForCall = (potSize)*equity - (callAmount)*(1-equity);
        double r = Math.random();

        if (Brain.handsIn < 100){ //not enough info yet, play default
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


                } else { //we're acting in response to something
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
            if (!isButton && turn == 1){ //we act first here

                if (equity >= greatEquity){
                    if (r < 0.8){
                        return action.bet(Brain.maxStackSize);
                    } else if (r < 0.9) {
                        return action.bet(action.upperRange/2); 
                    } else {
                        return action.call();
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

            } else if (isButton && turn == 1){ //we go 2nd
                if (hadBigBet){
                    double ourPercentile = Main.convertEquityToPercentile(equity, 5);
                    double avgPercentileOfOpponentPlayedHands = 1 - historian.bigBetFrequency()/2;
                    double difference = ourPercentile - avgPercentileOfOpponentPlayedHands;
                    System.out.println("My percentile is " + ourPercentile);
                    System.out.println("I think the opponent's percentile is " + avgPercentileOfOpponentPlayedHands);
                    if (difference > 0)
                        return action.bet(Brain.maxStackSize);
                    else if (difference > -0.10)
                        return action.call();
                    else return action.check();
                } else { //no big bet :(
                    if (equity >= greatEquity){
                        return action.bet(Brain.maxStackSize);
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
                }
                
            } else { //turn >= 2
                if (hadBigBet){
                    double ourPercentile = Main.convertEquityToPercentile(equity, 5);
                    double avgPercentileOfOpponentPlayedHands = 1 - historian.bigBetFrequency()/2;
                    double difference = ourPercentile - avgPercentileOfOpponentPlayedHands;
                    System.out.println("My percentile is " + ourPercentile);
                    System.out.println("I think the opponent's percentile is " + avgPercentileOfOpponentPlayedHands);
                    if (difference > 0)
                        return action.bet(Brain.maxStackSize);
                    else if (difference > -0.10)
                        return action.call();
                    else return action.check();
                } else { //no big bet :(
                    if (equity >= greatEquity){
                        return action.bet(Brain.maxStackSize);
                    } else if (equity >= goodEquity){
                        return action.call();
                    } else if (equity >= badEquity){
                        if(evForCall > 0){
                            return action.call();
                        } else {
                            return action.check();
                        }
                    } else { //terrible equity
                        return action.check();
                    }
                }
            }
        }
    }
}
