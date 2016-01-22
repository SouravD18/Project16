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
    double timePerHandLeft = 0.1;
    
    public int numSimulations = 2000;
    
    String[] holeCards = new String[4];
    List<String> boardCards = new ArrayList<String>();
    
    // Turn Counter keep track of turns: 
    // Preflop = 0; Flop = 3; Turn = 4; River = 5;
    int turnCounter = 0;
    
    int currentPot = 0;
    int previousPot = 0;
    // My stack size
    int myStack = 200;
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
    
    // Object to process actions taken by the bot
    ProcessActions action = new ProcessActions();
    
    // Printer for Dump Files
    DumpPrinter printer = new DumpPrinter();
    
    // Historian
    Historian opponentHistorian = new Historian();
    boolean raisedPreflop = false;
    boolean checkedPreflop = false;
    boolean myRaisePreflop = false;
    
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
        this.handsRemaining--;
        
        timePerHandLeft = timeBank / handsRemaining;
        if (timePerHandLeft < 0.18)
            numSimulations = 1000;
        else if (timePerHandLeft < 0.22)
            numSimulations = 2000;
        else
            numSimulations = 2400;
        
        // Reset some stuff:
        this.turnCounter = 0;
        this.boardCards.clear();
        this.currentPot = 0;
        this.previousPot = 0;
        
        this.preFlopBetTurn = 0;
        this.flopBetTurn = 0;
        this.turnBetTurn = 0;
        this.riverBetTurn = 0;
        
        this.myStack = 200;
        this.raisedPreflop = false;
        this.checkedPreflop = false;
        this.myRaisePreflop = false;
        this.opponentHistorian.numberOfHands += 1;
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
        this.action.process(legalActions, this.actions);
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
    
    private String preFlop(){
        String opponentActionType = this.actions[1].actionType();
        
        //check whether opponent raised in preflop
        if(opponentActionType.equals("RAISE")){
            raisedPreflop = true;
            if(!checkedPreflop){
                opponentHistorian.pfr += 1;
                checkedPreflop = true;
            }
        }
        
        String myActionType = this.actions[0].actionType();
        myRaisePreflop = myActionType.equals("RAISE");
        
        this.preFlopBetTurn += 1;
        if (this.preFlopBetTurn == 1){
            String[] board = {};
            equity = Main.getEquity(board, this.holeCards, numSimulations);
        }
        // Update Historian
            // For Opponent SB
        if(!(isButton)){
            opponentHistorian.vpip += 1;
            
            if(preFlopBetTurn == 1 && opponentActionType.equals("CALL")){
                opponentHistorian.limp += 1;
            }
            if(preFlopBetTurn == 2){
                opponentHistorian.fourBet += 1;
            }
        }
            // For Opponent BB
        else{
            if(preFlopBetTurn == 2){
                opponentHistorian.vpip += 1;
                
                opponentHistorian.threeBet += 1;
                
                if(myActionType.equals("CALL")){
                    opponentHistorian.limpRaise += 1;                    
                }
            }
        }
        return (new PreFlop()).testAction(this.isButton, this.preFlopBetTurn,
                equity, this.currentPot, this.action, this.opponentHistorian);
        
        //return (new PreFlop()).takeAction(this.action, this.equity, this.currentPot, this.preFlopBetTurn);
    }

    private String flop(){
        String opponentActionType = this.actions[1].actionType();
        String myActionType = this.actions[0].actionType();
        // Adjust vpip
        if(this.preFlopBetTurn == 1){
            if(this.actions[0].actionType().equals("RAISE") & this.isButton){
                this.opponentHistorian.vpip += 1;
            }
        }
        
        
        this.flopBetTurn += 1;
        if(this.flopBetTurn == 1){
            myRaisePreflop = myActionType.equals("RAISE");
            
            String[] board = new String[3];
            board[0] = this.boardCards.get(0);
            board[1] = this.boardCards.get(1);
            board[2] = this.boardCards.get(2);
            equity = Main.getEquity(board, this.holeCards, numSimulations);
        }
        // Update Historian
            // Opponent First to act
        if(isButton){
            if((flopBetTurn==1 && raisedPreflop) && opponentActionType.equals("BET")){
                opponentHistorian.continuationBet += 1;
            }
            else if(flopBetTurn==2 && myActionType.equals("BET")){
                opponentHistorian.checkRaise +=1; 
            }
            else if(flopBetTurn==2 && myActionType.equals("RAISE")){
                opponentHistorian.threeBet_flop += 1;
            }
        }
            // Opponent Second to act
        else{
            if(flopBetTurn==2 && raisedPreflop){
                opponentHistorian.continuationBet += 1;
            }
        }
        return (new Flop()).takeAction(this.action, this.equity, this.currentPot, this.flopBetTurn);
    }    
  
    private String turn(){
        String opponentAction =this.actions[1].actionType();
        
        this.turnBetTurn += 1;
        if(this.turnBetTurn == 1){
            String[] board = new String[4];
            board[0] = this.boardCards.get(0);
            board[1] = this.boardCards.get(1);
            board[2] = this.boardCards.get(2);
            board[3] = this.boardCards.get(3);
            equity = Main.getEquity(board, this.holeCards, numSimulations);
        }
        // Update historian
            // Opponent first to act
        if(this.isButton){
            if(opponentAction.equals("BET") || opponentAction.equals("RAISE")){
                opponentHistorian.betOrRaiseCount += 1;
            }
        }
            // Opponent second to act
        else{
            if(this.turnBetTurn > 1){
                opponentHistorian.betOrRaiseCount += 1;
            }
        }
        return (new Turn()).takeAction(this.action, this.equity, this.currentPot, this.turnBetTurn);
    }

    private String river(){
        String opponentAction = this.actions[1].actionType();
        this.riverBetTurn += 1;
        if(this.riverBetTurn == 1){
            String[] board = new String[5];
            board[0] = this.boardCards.get(0);
            board[1] = this.boardCards.get(1);
            board[2] = this.boardCards.get(2);
            board[3] = this.boardCards.get(3);
            board[4] = this.boardCards.get(4);
            equity = Main.getEquity(board, this.holeCards, numSimulations);
        }
         // Update historian
            // Opponent first to act
        if(this.isButton){
            if(riverBetTurn == 1){
                boolean called = this.actions[0].actionType().equals("BET") ||
                                this.actions[0].actionType().equals("RAISE");
                if(called){
                    opponentHistorian.callCount += 1;
                }
            }
            if(opponentAction.equals("BET") || opponentAction.equals("RAISE")){
                opponentHistorian.betOrRaiseCount += 1;
            }

        }
            // Opponent second to act
        else{
            if(this.riverBetTurn == 1){
                boolean called = this.actions[0].actionType().equals("BET") ||
                        this.actions[0].actionType().equals("RAISE");
                if(called){
                    opponentHistorian.callCount += 1;
                }
            }
            else{
                opponentHistorian.betOrRaiseCount += 1;
            }
        }
        return (new River()).takeAction(this.action, this.equity, this.currentPot, this.riverBetTurn);
    }
    
    public double equity(){
        return this.equity;
    }
    
    public void updateAfterHandOver(Action[] givenActions){
        int size = givenActions.length;
        
        if(givenActions[size - 1].actor().equals(this.Names[1])){
            opponentHistorian.winCount += 1;
            if(givenActions[size - 2].actionType().equals("SHOW")){
                opponentHistorian.winSD_post += 1;
            }
        }
        
        if(givenActions[size - 2].actionType().equals("SHOW")){
            opponentHistorian.wentSD_post += 1;
        }
        
        if(givenActions[1].actionType().equals("FOLD")){
            opponentHistorian.foldCount += 1;
        }
        
        if(givenActions[0].actionType().equals("BET") || 
                    givenActions[0].actionType().equals("RAISE")){
            if((this.turnCounter == 0 && this.preFlopBetTurn == 2) && this.isButton){
                opponentHistorian.foldToFourBet += 1;
            }
            if((this.turnCounter == 3 && this.flopBetTurn == 1 && myRaisePreflop)){
                if(givenActions[1].actionType().equals("FOLD")){
                    opponentHistorian.foldToContinuation_flop += 1;
                }
            }
            if(this.turnCounter > 3 && givenActions[1].actionType().equals("CALL")){
                opponentHistorian.callCount += 1;
            }
        }
        this.opponentHistorian.printAll();
    }
}
