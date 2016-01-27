package player;

import equity.poker.Main;

public class PostFlop {
    final double greatEquity;
    final double goodEquity;
    final double badEquity;
    final double greatCEquity;
    final double goodCEquity;
    final double badCEquity;
    final double equity96;
    final double equity86;
    final int turnCounter;
    final Historian historian;
    
    public PostFlop(int turnCounter, Historian h){
        this.turnCounter = turnCounter;
        historian = h;
        switch (turnCounter){
        case 3:
            greatEquity = Constants.flopGreat;
            goodEquity = Constants.flopGood;
            badEquity = Constants.flopAverage;
            greatCEquity = Constants.flopCGreat;
            goodCEquity = Constants.flopCGood;
            badCEquity = Constants.flopCAverage;
            equity96 = Constants.flop96;
            equity86 = Constants.flop86;
            break;
        case 4:
            greatEquity = Constants.turnGreat;
            goodEquity = Constants.turnGood;
            badEquity = Constants.turnAverage;
            greatCEquity = Constants.turnCGreat;
            goodCEquity = Constants.turnCGood;
            badCEquity = Constants.turnCAverage;
            equity96 = Constants.turn96;
            equity86 = Constants.turn86;
            break;
        case 5:
            greatEquity = Constants.riverGreat;
            goodEquity = Constants.riverGood;
            badEquity = Constants.riverAverage;
            greatCEquity = Constants.riverCGreat;
            goodCEquity = Constants.riverCGood;
            badCEquity = Constants.riverCAverage;
            equity96 = Constants.river96;
            equity86 = Constants.river86;
            break;
        default:
            throw new RuntimeException();
        }
    }
    
    public String takeAction(ProcessActions action, double equity, int potSize, int turn, boolean isButton, boolean hadBigBet){
        int callAmount = action.callAmount();
        double evForCall = (potSize)*equity - (callAmount)*(1-equity);
        double r = Math.random();
        double probFolding = historian.foldFrequency(turnCounter);
        System.out.println("Our equity is currently " + equity);
        

        if (Brain.handsIn < 100){ //not enough info yet, play default
            if (turn == 1){
                if (hadBigBet){
                    if (equity >= equity96){
                        return action.bet(Brain.maxStackSize);
                    } else if (equity >= equity86) {
                       return action.call();
                    } else return action.check();
                    
                } else {
                    if (!isButton){ //We're the BB and act first here
                        if(equity >= greatCEquity){
                            return action.bet(Brain.maxStackSize);
                        }
                        else if (equity >= goodCEquity){
                            return action.bet(callAmount + (int) evForCall);
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


                    } else { //we're acting in response to something
                        if (equity >= greatCEquity){
                            if (action.raisePossible){ //the opponent has raised
                                return action.bet(Brain.maxStackSize);
                            } else { //the opponent has just called
                                return action.bet(4); //we'll bet low here
                            }
                        } else if (equity >= goodCEquity){
                            if (action.raisePossible){ //opponent raised
                                return action.bet(callAmount + (int) evForCall);
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
                
            } else { //turn >= 2, the opponent must have raised here
                if (equity >= greatCEquity){
                    return action.bet(Brain.maxStackSize);
                } else if (equity >= goodCEquity || (equity >= badCEquity && evForCall > 0)){
                    return action.call();
                } else {
                    return action.check();
                }
            }



        } else { //play 4 real now
            if (!isButton && turn == 1){ //we act first here
                if (hadBigBet){
                    double ourPercentile = Main.convertEquityToPercentile(equity, turnCounter);
                    double avgPercentileOfOpponentPlayedHands = 1 - historian.bigBetFrequency()/2;
                    double difference = ourPercentile - avgPercentileOfOpponentPlayedHands;
                    System.out.println("My percentile is " + ourPercentile);
                    System.out.println("I think the opponent's percentile is " + avgPercentileOfOpponentPlayedHands);
                    if (difference > 0.01)
                        return action.bet(Brain.maxStackSize);
                    else if (difference > -0.10)
                        return action.call();
                    else return action.check();
                    
                } else {
                    if (equity >= greatCEquity){
                        if (r > probFolding){
                            return action.bet(Brain.maxStackSize);
                        } else if (r > probFolding/2) {
                            return action.bet(action.upperRange/2); 
                        } else {
                            return action.call();
                        }
                    } else if (equity >= goodCEquity){
                        return action.bet(callAmount + (int) evForCall);
                    } else if (equity >= badCEquity){
                        if(evForCall > 0){
                            return action.call();
                        } else {
                            return action.check();
                        }
                    } else { //terrible equity
                        return action.check();
                    }
                }
                
            } else { //we're acting in response to something
                if (hadBigBet){
                    double ourPercentile = Main.convertEquityToPercentile(equity, turnCounter);
                    double avgPercentileOfOpponentPlayedHands = 1 - historian.bigBetFrequency()/2;
                    double difference = ourPercentile - avgPercentileOfOpponentPlayedHands;
                    System.out.println("My percentile is " + ourPercentile);
                    System.out.println("I think the opponent's percentile is " + avgPercentileOfOpponentPlayedHands);
                    if (difference > 0.01)
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

            }
        }
    }
    
}
