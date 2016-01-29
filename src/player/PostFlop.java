package player;

import equity.poker.Main;

public class PostFlop {
    double greatEquity;
    double goodEquity;
    double badEquity;
    double greatCEquity;
    double goodCEquity;
    double badCEquity;
    double equity96;
    double equity76;
    final int turnCounter;

    public PostFlop(int turnCounter){
        this.turnCounter = turnCounter;
        switch (turnCounter){
        case 3:
            greatEquity = Constants.flopGreat;
            goodEquity = Constants.flopGood;
            badEquity = Constants.flopAverage;
            greatCEquity = Constants.flopCGreat;
            goodCEquity = Constants.flopCGood;
            badCEquity = Constants.flopCAverage;
            equity96 = Constants.flop96;
            equity76 = Constants.flop76;
            break;
        case 4:
            greatEquity = Constants.turnGreat;
            goodEquity = Constants.turnGood;
            badEquity = Constants.turnAverage;
            greatCEquity = Constants.turnCGreat;
            goodCEquity = Constants.turnCGood;
            badCEquity = Constants.turnCAverage;
            equity96 = Constants.turn96;
            equity76 = Constants.turn76;
            break;
        case 5:
            greatEquity = Constants.riverGreat;
            goodEquity = Constants.riverGood;
            badEquity = Constants.riverAverage;
            greatCEquity = Constants.riverCGreat;
            goodCEquity = Constants.riverCGood;
            badCEquity = Constants.riverCAverage;
            equity96 = Constants.river96;
            equity76 = Constants.river76;
            break;
        default:
            throw new RuntimeException();
        }
    }

    public String takeAction(ProcessActions action, double equity, int potSize, int turn, boolean isButton, Historian historian, boolean hadBigBet){
        int callAmount = action.callAmount();
        double evForCall = (potSize)*equity - (callAmount)*(1-equity);
        double r = Math.random();
        double probFolding = historian.foldFrequency(turnCounter);
        System.out.println("Our equity is currently " + equity);


        if (Brain.handsIn < 100){ //not enough info yet, play default
            
            if (hadBigBet){
                if (equity >= equity96){
                    return action.bet(Brain.maxStackSize);
                } else if (equity >= equity76) {
                    return action.call();
                } else return action.check();
            }

            else if (turn == 1){ //turn 1
                if (!isButton){ //no big bet, we're acting first
                    if(equity >= greatCEquity){
                        return action.bet(Brain.maxStackSize);
                    }
                    else if (equity >= goodCEquity){
                        if (potSize < 50)
                            return action.bet(Math.min(25, callAmount + (int) evForCall));
                        else return action.call();
                    }
                    else if (equity >= badCEquity){
                        if(evForCall > 0){
                            return action.call();
                        } else {
                            return action.check();
                        }
                    } else {
                        return action.check();
                    }


                } else { //no big bet, we're acting second
                    if (equity >= greatCEquity){
                        if (action.raisePossible){ //the opponent has raised
                            return action.bet(Brain.maxStackSize);
                        } else { //the opponent has just called
                            return action.bet(4); //we'll bet low here
                        }
                    } else if (equity >= goodCEquity){
                        if (action.raisePossible){ //opponent raised
                            if (potSize < 50)
                                return action.bet(Math.min(25, callAmount + (int) evForCall));
                            else return action.call();
                        } else { //opponent has called
                            return action.call();
                        }
                    } else if (equity >= badCEquity && evForCall > 0){
                        return action.call();
                    } else { //terrible equity or negative EV
                        return action.check();
                    }
                }
            }

            else { //no big bet, turn 2
                if (equity >= greatCEquity){
                    return action.bet(Brain.maxStackSize);
                } else if (equity >= goodCEquity || (equity >= badCEquity && evForCall > 0)){
                    return action.call();
                } else {
                    return action.check();
                }
            }


        } else { //play 4 real now
            if (hadBigBet){ //has big bet
                double ourPercentile = Main.convertEquityToPercentile(equity, turnCounter);
                double avgPercentileOfOpponentPlayedHands = 1 - historian.bigBetFrequency()/2;
                double difference = ourPercentile - avgPercentileOfOpponentPlayedHands;
                System.out.println("My percentile is " + ourPercentile);
                System.out.println("I think the opponent's percentile is " + avgPercentileOfOpponentPlayedHands);
                if (difference > 0)
                    return action.bet(Brain.maxStackSize);
                else if (difference > -0.20)
                    return action.call();
                else return action.check();
            }

            else if (turn == 1){
                if (!isButton){ //no big bet, we're acting first
                    if (equity >= greatCEquity){
                        if (r > probFolding){
                            return action.bet(Brain.maxStackSize);
                        } else if (r > probFolding/2) {
                            return action.bet(action.upperRange/2); 
                        } else {
                            return action.call();
                        }
                    } else if (equity >= goodCEquity){
                        if (potSize < 50)
                            return action.bet(Math.min(25, callAmount + (int) evForCall));
                        else return action.call();
                    } else if (equity >= badCEquity){
                        if(evForCall > 0){
                            return action.call();
                        } else {
                            return action.check();
                        }
                    } else { //terrible equity
                        return action.check();
                    }

                } else { //no big bet, we're acting second
                    if (equity >= greatEquity){
                        return action.bet(Brain.maxStackSize);
                    } else if (equity >= goodEquity){
                        if (potSize < 50)
                            return action.bet(Math.min(25, callAmount + (int) evForCall));
                        else return action.call();
                    } else if (equity >= badEquity && evForCall > 0){
                        return action.call();
                    } else { //terrible equity
                        return action.check();
                    }
                }
                
            } else { //turn 2, no big bet :(
                if (equity >= greatEquity){
                    return action.bet(Brain.maxStackSize);
                } else if (equity >= goodEquity || (equity >= badEquity && evForCall > 0)){
                    return action.call();
                } else { //terrible equity
                    return action.check();
                }
            }

        }
    }
}
