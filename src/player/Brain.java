package player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import equity.poker.Main;

public class Brain {
    // Declaring the necessary fields
    
    //Equity
    double equity = 0.5;
    
    // My name and Opponent Name
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
    

    
    /**
     *  Betting turn trackers
     */
    int preFlopBetTurn = 0;
    int flopBetTurn = 0;
    int turnBetTurn = 0;
    int riverBetTurn = 0;
    
    ProcessActions action = new ProcessActions();
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
        
        // Processing legalActions
        this.action.process(legalActions);
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
     * Edited: 19th Jan by sourav18
     *  Check the classes for descriptions
     */
    private String preFlop(){
        this.preFlopBetTurn += 1;
        return action.call();
    }

     
    private String flop(){
        this.flopBetTurn += 1;
        return action.call();
    }    
  
    private String turn(){
        this.turnBetTurn += 1;
        return action.call();
    }

    private String river(){
        this.riverBetTurn += 1;
        return action.call();
    }    
}
