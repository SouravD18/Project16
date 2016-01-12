package player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Brain {
    
    // [myName, oppName]
    String[] Names = new String[2];
    int myBankRoll = 0;
    int otherBankRoll = 0;
    double timeBank = 0;
    int handId = 0;
    int handsRemaining = 0;
    
    String[] holeCards = new String[4];
    List<String> boardCards = new ArrayList<String>();
    
    // Turn Counter keep track of turns: 
    // Preflop = 0; Flop = 3; Turn = 4; River = 5;
    int turnCounter = 0;
    
    int currentPot = 0;
    int previousPot = 0;
    boolean isButton = false;
    
    // List of actions:
    // List[0] = my last action
    // List[1] = opponent's last action
    // List[2] = last action
    Action[] actions = new Action[3];
    int numberOfLastActions = 0;
    
    List<String> legalActions = new ArrayList<String>();
    int numberOflegalActions = 0;
    
    /**
     *  Betting turn trackers
     */
    int preFlopBetTurn = 0;
    int flopBetTurn = 0;
    int turnBetTurn = 0;
    int riverBetTurn = 0;
    
    public Brain(){    
    }
    /**
     *  Whenever a new game starts, it resets everything.
     * @param myName
     * @param opponentName
     * @param numHands: that will be played in the game
     * @param time: remaining for me
     */
    public void newGame(String myName, String opponentName, 
                int numHands, double time){
        
        this.Names[0] = myName;
        this.Names[1] = opponentName;
        this.handsRemaining = numHands;
        this.timeBank = time;
        
        // Reset some stuff:
        this.myBankRoll = 0;
        this.otherBankRoll = 0;
        this.handId = 0;
        this.turnCounter = 0;
        this.isButton = false;
        this.currentPot = 0;
        this.previousPot = 0;
    }
    /**
     *  Whenever a new hand starts, it resets some stuff.
     * @param id: current hand id
     * @param button: boolean indicating whether our bot is a button or not
     * @param cards: List of our hole Cards
     * @param myBank: my bank roll
     * @param otherBank: opponent bank roll
     * @param time: remaining time
     */
    public void newHand(int id, boolean button, String[] cards,
                    int myBank,int otherBank, double time){
        this.handId = id;
        this.isButton = button;
        
        this.holeCards[0] = cards[0];
        this.holeCards[1] = cards[1];
        this.holeCards[2] = cards[2];
        this.holeCards[3] = cards[3];
        
        this.myBankRoll = myBank;
        this.otherBankRoll = otherBank;
        this.timeBank = time;
        
        // Reset some stuff:
        this.turnCounter = 0;
        this.boardCards.clear();
        this.currentPot = 0;
        this.previousPot = 0;
        
        this.preFlopBetTurn = 0;
        this.flopBetTurn = 0;
        this.turnBetTurn = 0;
        this.riverBetTurn = 0;
    }
    
    public void getAction(int pot, int numBoardCards, String[] board, int numLastActions,
                    Action[] lastActions,int numLegalActions, String[] legalActions, double time){
        
        this.previousPot = this.currentPot + 0;
        this.currentPot = pot;
        this.turnCounter = numBoardCards;
        if(numBoardCards != 0){
            this.boardCards.clear();
            this.boardCards.addAll(Arrays.asList(board));
        }
        for(Action act: lastActions){
            
            // If the action is mine
            if(act.actor().equals(this.Names[0])){
                this.actions[0] = act;
            }
            // If Opponent's action:
            else if(act.actor().equals(this.Names[1])){
                this.actions[1] = act;
            }
        }
        // Assigning last Action
        this.actions[2] = lastActions[numLastActions - 1];
        
        // Assigning legal actions
        this.numberOflegalActions = numLegalActions;
        this.legalActions.clear();
        this.legalActions.addAll(Arrays.asList(legalActions));
        
        this.timeBank = time;
    }
    
    /**
     *  This function will make a decision
     * @return
     */
    public String decision(){
        // Check whether it's preflop/ flop/ turn/ river, and then call functions accordingly
        if(this.turnCounter == 0){
            return preFlop();
        }
        else if(this.turnCounter == 3){
            return flop();
        }
        else if(this.turnCounter == 4){
            return turn();
        }
        else if(this.turnCounter == 5){
            return river();
        }
        else{
           
            return "CHECK";
        }
    }
    
    /**
     * Simple Preflop Strategy:
     *  --> Always Raise Max from button position. And Also Call to 2nd-Bet
     *  --> Always Call/ Check from non-button position.
     */
    private String preFlop(){
        // If the bot is the button:
        if(this.isButton) {
            // If this is the first betting turn
            if(this.preFlopBetTurn == 0){
                // Raise Max == Raise 6
                
                this.preFlopBetTurn += 1;
                return "RAISE:6";
            }
            // If it's the second round of betting, then just call
            else{
                this.preFlopBetTurn += 1;
                return "CALL";
            }
        }
        
        // If the bot is not button, then just call/ check
        else{
            for(String act: this.legalActions){
                if(act.equals("CHECK") || act.equals("CALL")){
                    this.preFlopBetTurn++;
                    return act;
                }
            }
            // Code should not reach here.
            this.preFlopBetTurn++;
            return "CHECK";
        }
    }
    
    /**
     * Simple Flop Strategy:
     *  --> From Button Position (Act 2nd):
     *      --> A good hand
     *      --> A medium hand
     *      --> A bad hand
     *  --> From Non-Button Position (Act 1st):
     *      --> A good hand
     *      --> A medium hand
     *      --> A bad hand
     *      
     * Since we don't have an equity calculator, we'll randomly play our hand(No other option :( )
     * 
     * We'll play 90% of the flops from button. 
     * And 70% of the time we will bet/raise
     *      --> 70% chance of raise/Bet
     *      --> 20% chance of call
     *      --> 10% chance of fold
     *   There is 10% chance of folding to a 2-bet. But 90% chance to call a 2-bet
     *   
     * We'll play 80% of the flops from non-button position
     *      --> 50% chance of raise/bet
     *      --> 50% chance of Checking
     *  And 20% chance of folding to a 2-bet. But 80% chance to call a 2-bet
     */
    private String flop(){
        // If the bot is the button:
        if(this.isButton) {
            // If this is the first betting turn
            if(this.flopBetTurn == 0){
                double probability = (new Random()).nextDouble();
                
                // Probability of betting/ raising
                if(probability < .7){
                    // Need to return Bet/ Raise Max
                    
                    for(String act: this.legalActions){
                        String[] words = act.split(":");
                        if(words[0].equals("BET") || words[0].equals("RAISE")){
                            this.flopBetTurn ++;
                            return words[0].concat(":").concat(words[2]);
                        }
                    }
                    // Should not reach here
                    this.flopBetTurn ++;
                    return "CHECK";
                }
                
                // Probability of calling
                else if(probability < .9){
                    for(String act: this.legalActions){
                        if(act.equals("CHECK") || act.equals("CALL")){
                            this.flopBetTurn ++;
                            return act;
                        }
                    }
                    // Should not reach here
                    this.flopBetTurn ++;
                    return "CHECK";
                }
                
                // Probability of folding
                else{
                    this.flopBetTurn ++;
                    return "FOLD";
                }
                
            }
            // If it's the second round of betting, then Call/Check accordingly
            else{
                double againProbability = (new Random()).nextDouble();
                if(againProbability < .9){
                    for(String act: this.legalActions){
                        if(act.equals("CHECK") || act.equals("CALL")){
                            this.preFlopBetTurn++;
                            return act;
                        }
                    }
                    // Code should not reach here.
                    this.preFlopBetTurn++;
                    return "CHECK";
                }
                else{
                    this.preFlopBetTurn++;
                    return "FOLD";
                }
            }
        }
        
        // Now If I am the non-button:
        else{
            // If this is the first betting turn
            if(this.flopBetTurn == 0){
                double probability = (new Random()).nextDouble();
                
                // Probability of betting/ raising
                if(probability < .5){
                    // Need to return Bet/ Raise Max
                    
                    for(String act: this.legalActions){
                        String[] words = act.split(":");
                        if(words[0].equals("BET") || words[0].equals("RAISE")){
                            this.flopBetTurn ++;
                            return words[0].concat(":").concat(words[2]);
                        }
                    }
                    // Should not reach here
                    this.flopBetTurn ++;
                    return "CHECK";
                }
                
                // Probability of calling
                else {
                    for(String act: this.legalActions){
                        if(act.equals("CHECK") || act.equals("CALL")){
                            this.flopBetTurn ++;
                            return act;
                        }
                    }
                    // Should not reach here
                    this.flopBetTurn ++;
                    return "CHECK";
                }  
            }
            
            // If it's the second round of betting, then Call/Check accordingly
            else{
                double againProbability = (new Random()).nextDouble();
                if(againProbability < .8){
                    for(String act: this.legalActions){
                        if(act.equals("CHECK") || act.equals("CALL")){
                            this.preFlopBetTurn++;
                            return act;
                        }
                    }
                    // Code should not reach here.
                    this.preFlopBetTurn++;
                    return "CHECK";
                }
                else{
                    this.preFlopBetTurn++;
                    return "FOLD";
                }
            }
        }
    }    
    
    
    /**
     * Simple Turn Strategy:
     *  --> From Button Position (Act 2nd):
     *      --> A good hand
     *      --> A medium hand
     *      --> A bad hand
     *  --> From Non-Button Position (Act 1st):
     *      --> A good hand
     *      --> A medium hand
     *      --> A bad hand
     *      
     * But we don't have equity calculator yet(Also I need to sleep now). So we will randomly choose 
     * to call/bet/fold. We'll do: 40%, 35%, 25% (Need to fold often at this point)
     */
    private String turn(){
        if(turnBetTurn == 0){
            double probability = (new Random()).nextDouble();
            // Call
            if(probability < .4){
                for(String act: this.legalActions){
                    if(act.equals("CHECK") || act.equals("CALL")){
                        this.turnBetTurn++;
                        return act;
                    }
                }
                // Should not come here
                this.turnBetTurn++;
                return "CHECK";
            }
            // Bet
            else if(probability <.75){
             // Need to return Bet/ Raise Max
                
                for(String act: this.legalActions){
                    String[] words = act.split(":");
                    if(words[0].equals("BET") || words[0].equals("RAISE")){
                        this.flopBetTurn ++;
                        return words[0].concat(":").concat(words[2]);
                    }
                }
                // Should not reach here
                this.flopBetTurn ++;
                return "CHECK";

            }
            // Check FOLD
            else{
                for(String act: this.legalActions){
                    if(act.equals("CHECK")){
                        this.turnBetTurn++;
                        return act;
                    }
                }
                this.turnBetTurn++;
                return "FOLD";
            }
        }
        
        // If it's the second turn: Some mix of probabilities
        else{
            double probability = (new Random()).nextDouble();
            // Call
            if(probability < .75){
                for(String act: this.legalActions){
                    if(act.equals("CHECK") || act.equals("CALL")){
                        this.turnBetTurn++;
                        return act;
                    }
                }
                // Should not come here
                this.turnBetTurn++;
                return "CHECK";
            }
           
            // Check/FOLD
            else{
                for(String act: this.legalActions){
                    if(act.equals("CHECK")){
                        this.turnBetTurn++;
                        return act;
                    }
                }
                this.turnBetTurn++;
                return "FOLD";
            }
        }
    }
    
    
    /**
     * Simple River Strategy:
     *  --> From Button Position (Act 2nd):
     *      --> A good hand
     *      --> A medium hand
     *      --> A bad hand
     *  --> From Non-Button Position (Act 1st):
     *      --> A good hand
     *      --> A medium hand
     *      --> A bad hand
     *
     */
    
    // Again, some bull shit probabilities:
    private String river(){
        double probability = (new Random()).nextDouble();
        // Call
        if(probability < .75){
            for(String act: this.legalActions){
                if(act.equals("CHECK") || act.equals("CALL")){
                    this.riverBetTurn++;
                    return act;
                }
            }
            // Should not come here
            this.riverBetTurn++;
            return "CHECK";
        }
       
        // Check/FOLD
        else{
            for(String act: this.legalActions){
                if(act.equals("CHECK")){
                    this.riverBetTurn++;
                    return act;
                }
            }
            this.riverBetTurn++;
            return "FOLD";
        }
    }
    
}
