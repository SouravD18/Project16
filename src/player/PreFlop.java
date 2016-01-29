package player;
import equity.poker.Main;

public class PreFlop {
    static double greatEquity = Constants.preFlopGreat;
    static double goodEquity = Constants.preFlopGood;
    static double badEquity = Constants.preFlopAverage;

    public static String takeAction(ProcessActions action, double equity, int potSize, int turn, boolean isButton, Historian historian, int preFlopType){
        int callAmount = action.callAmount();
        double evForCall = (potSize)*equity - (callAmount)*(1-equity);
        double r = Math.random();
        System.out.println("Our equity is currently " + equity);

        if (Brain.handsIn < 50){ //not enough info yet, play default
            if (turn == 1){

                if (isButton){ //We act first here
                    if (equity >= greatEquity){
                        if (r > 0.4){
                            return action.bet(Brain.maxStackSize); //60% we raise full
                        } else if (r > 0.2) {
                            return action.bet(4); //20% we raise partially
                        } else {
                            return action.call(); //20% we just call
                        }
                    } else if (equity >= goodEquity){
                        return action.bet(4);
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
                            return action.bet(action.lowerRange);
                        }
                    } else if (equity >= goodEquity){
                        return action.bet(action.lowerRange);
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

            if (turn == 1){ 
                if (isButton){ //we go first
                    if (equity >= greatEquity){
                        double probFolding = historian.foldPreFlopFrequency();
                        if (r > probFolding){
                            return action.bet(Brain.maxStackSize);
                        } else if (r > probFolding/2) {
                            return action.bet(4);
                        } else {
                            return action.call();
                        }
                    } else if (equity >= goodEquity){
                        return action.bet(4);
                    } else if (equity >= badEquity){
                        if(evForCall > 0){
                            return action.call();
                        } else {
                            return action.check();
                        }
                    } else { //terrible equity
                        return action.check();
                    }
                    
                } else { //first turn, we go second as BB
                    double avgPercentileOfOpponentPlayedHands = historian.preFlopTypePercentile(preFlopType);
                    double ourPercentile = Main.convertEquityToPercentile(equity, 0);
                    double difference = ourPercentile - avgPercentileOfOpponentPlayedHands;
                    if (difference > 0.1)
                        return action.bet(Brain.maxStackSize); 
                    else if (difference > -0.1)
                        return action.call();
                    else return action.check();
                }
                
            } else { //2nd turn
                if (equity >= greatEquity){
                    return action.bet(Brain.maxStackSize);
                } else if (equity >= goodEquity || equity >= badEquity && evForCall > 0){
                    return action.call();
                } else { //terrible equity
                    return action.check();
                }
            }
        }

    }
}