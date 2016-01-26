package player;
import equity.poker.Main;

public class PreFlop {
    static double greatEquity = Constants.preFlopGreat;
    static double goodEquity = Constants.preFlopGood;
    static double badEquity = Constants.preFlopAverage;

    public static String takeAction(ProcessActions action, double equity, int potSize, int turn, boolean isButton, Historian mister){
        int callAmount = action.callAmount();
        double evForCall = (potSize)*equity - (callAmount)*(1-equity);
        double r = Math.random();

        if (Brain.handsIn < 100){ //not enough info yet, play default
            if (turn == 1){

                if (isButton){ //We act first here
                    if (equity >= greatEquity){
                        if (r < 0.6){
                            return action.bet(Brain.maxStackSize); //60% we raise full
                        } else if (r < 0.8) {
                            return action.bet(4); //20% we raise partially
                        } else {
                            return action.call(); //20% we just call
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


                } else { //we're the BB and act second
                    if (equity >= greatEquity){
                        if (action.raisePossible){ //the opponent has raised
                            return action.bet(Brain.maxStackSize);
                        } else { //the opponent has just called
                            return action.bet(4);
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


        } else { //it's been over 100 turns, we can start analyzing data
            double vpip = mister.vpipPercent();
            double pfr = mister.pfrPercent();
            double avgPercentileOfOpponentPlayedHands;// = 1 - mister.vpipPercent()/2;
            double ourPercentile = Main.convertEquityToPercentile(equity, 0);
            if (turn == 1){
                if (isButton){ //we act first here

                    if (equity >= greatEquity){
                        if (r < vpip){
                            return action.bet(Brain.maxStackSize); //vpip% that we raise full
                        } else if (r < vpip/2 + 0.5) {
                            return action.bet(4); //(1-vpip)/2% we raise partially
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

                } else { //we're the BB and act second
                    if (action.raisePossible){ //the opponent has raised
                        avgPercentileOfOpponentPlayedHands = 1 - pfr/2;
                    } else if (action.betPossible){ //the opponent has just called, he's somewhere in the middle
                        avgPercentileOfOpponentPlayedHands = 1 - (pfr + vpip)/2;
                    } else {
                        return action.check();
                    }

                    if (ourPercentile - avgPercentileOfOpponentPlayedHands > 0.05) //our cards are probably better than theirs
                        return action.bet(Brain.maxStackSize); 
                    if (ourPercentile - avgPercentileOfOpponentPlayedHands > -0.1) //our cards are around even
                        return action.call();
                    else return action.check(); //our cards are significantly worse, should probably fold
                }

            } else { //turn >=2
                avgPercentileOfOpponentPlayedHands = 1 - mister.threeBetPercent()/4; //cause threeBet was originally mult by 2
                if (ourPercentile - avgPercentileOfOpponentPlayedHands > 0.05) //our cards are probably better than theirs
                    return action.bet(Brain.maxStackSize); 
                if (ourPercentile - avgPercentileOfOpponentPlayedHands > -0.1) //our cards are around even
                    return action.call();
                else return action.check(); //our cards are significantly worse, should probably fold
            }
        }

    }
}