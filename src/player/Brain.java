package player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import equity.poker.Main;

public class Brain {
    ProcessActions action = new ProcessActions();
    Historian historian = new Historian();
    PostFlop postFlop = new PostFlop(3, historian);
    PostFlop turn = new PostFlop(4, historian);
    PostFlop river = new PostFlop(5, historian);

    String myName;
    String opponentName;
    String[] holeCards = new String[4];
    List<String> boardCards = new ArrayList<String>();

    public static final int maxStackSize = 400;
    double equity = 0.5;
    public int numSimulations = 7500;
    boolean isButton = false;

    public static int handsIn = 0;
    int handsRemaining = 0;
    double timePerHandLeft = 0.2;
    int currentPot = 0;

    //{My last action, opponent's last action, last action}
    Action[] actions = new Action[3];
    int numberOfLastActions = 0;

    int preFlopType = 0; //what the opponent does preflop (with it being the SB), 
    //0= check, 1 = raise 4, 2 = raise 5, 3 = raise 6
    boolean hadBigBet;

    // Turn Counter keep track of turns: 
    // Preflop = 0; Flop = 3; Turn = 4; River = 5;
    int turnCounter = 0;
    //betting turntrackers
    int preFlopBetTurn = 0;
    int flopBetTurn = 0;
    int turnBetTurn = 0;
    int riverBetTurn = 0;

    /**
     *  Whenever a new game starts, it resets everything.
     * @param myName
     * @param opponentName
     * @param numHands: that will be played in the game
     * @param time: remaining for me
     */
    public void newGame(String myName, String opponentName, 
            int numHands, double time){

        this.myName = myName;
        this.opponentName = opponentName;
        this.handsRemaining = numHands;

        // Reset some stuff:
        this.turnCounter = 0;
        this.isButton = false;
        this.currentPot = 0;
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
        this.isButton = button;

        this.holeCards[0] = cards[0];
        this.holeCards[1] = cards[1];
        this.holeCards[2] = cards[2];
        this.holeCards[3] = cards[3];

        this.handsRemaining--;
        handsIn++;

        timePerHandLeft = time / handsRemaining;
        if (timePerHandLeft < 0.19)
            numSimulations = 5000;
        else if (timePerHandLeft < 0.205)
            numSimulations = 7500;
        else
            numSimulations = 10000;

        // Reset some stuff:
        this.turnCounter = 0;
        this.boardCards.clear();
        this.currentPot = 0;
        this.preFlopBetTurn = 0;
        this.flopBetTurn = 0;
        this.turnBetTurn = 0;
        this.riverBetTurn = 0;
        this.turnCounter = 0;
        this.hadBigBet = false;
        this.preFlopType = 0;
    }

    public void getAction(int pot, int numBoardCards, String[] board, int numLastActions,
            Action[] lastActions,int numLegalActions, String[] legalActions, double time){

        this.currentPot = pot;
        this.turnCounter = numBoardCards;

        if(numBoardCards != 0){
            this.boardCards.clear();
            this.boardCards.addAll(Arrays.asList(board));
        }
        for(Action act: lastActions){
            if(act.actor().equals(myName)){
                this.actions[0] = act;
            }
            else if(act.actor().equals(opponentName)){
                this.actions[1] = act;
            }
        }
        this.actions[2] = lastActions[numLastActions - 1];
        this.action.process(legalActions, this.actions);
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
        Action opponentAction = actions[1];
        preFlopBetTurn++;
        if (preFlopBetTurn == 1){
            equity = Main.getEquity(new String[]{}, holeCards, numSimulations);
            if (!isButton){
                if (opponentAction.actionType().equals("RAISE")){
                    preFlopType = opponentAction.amount() - 3;
                } //else preFlopType = 0;
                historian.preFlopTypes[preFlopType]++;
            }
        }
        return PreFlop.takeAction(action, equity, currentPot, preFlopBetTurn, isButton, historian, preFlopType);
    }

    private String flop(){
        Action opponentAction = actions[1];
        flopBetTurn++;
        if (flopBetTurn == 1){
            String[] board = new String[3];
            board[0] = this.boardCards.get(0);
            board[1] = this.boardCards.get(1);
            board[2] = this.boardCards.get(2);
            equity = Main.getEquity(board, holeCards, numSimulations);
        }
        
        if (opponentAction.actionType().equals("BET") || opponentAction.actionType().equals("RAISE")){
            int bet = opponentAction.amount();
            if (bet >= 16 && bet*2.1 >= currentPot)
                hadBigBet = true;
        }
        
        return postFlop.takeAction(this.action, this.equity, this.currentPot, this.flopBetTurn, this.isButton, hadBigBet);
    }    

    private String turn(){
        Action opponentAction = actions[1];
        turnBetTurn++;
        if (turnBetTurn == 1){
            String[] board = new String[4];
            board[0] = this.boardCards.get(0);
            board[1] = this.boardCards.get(1);
            board[2] = this.boardCards.get(2);
            board[3] = this.boardCards.get(3);
            equity = Main.getEquity(board, holeCards, numSimulations);
        }
        
        if (opponentAction.actionType().equals("BET") || opponentAction.actionType().equals("RAISE")){
            int bet = opponentAction.amount();
            if (bet >= 16 && bet*2.1 >= currentPot)
                hadBigBet = true;
        }
        
        return turn.takeAction(this.action, this.equity, this.currentPot, this.turnBetTurn, this.isButton, hadBigBet);
    }

    private String river(){
        Action opponentAction = actions[1];
        riverBetTurn++;
        if (riverBetTurn == 1){
            String[] board = new String[5];
            board[0] = this.boardCards.get(0);
            board[1] = this.boardCards.get(1);
            board[2] = this.boardCards.get(2);
            board[3] = this.boardCards.get(3);
            board[4] = this.boardCards.get(4);
            equity = Main.getEquity(board, holeCards, numSimulations);
        }
        
        if (opponentAction.actionType().equals("BET") || opponentAction.actionType().equals("RAISE")){
            int bet = opponentAction.amount();
            if (bet >= 16 && bet*2.1 >= currentPot)
                hadBigBet = true;
        }
        
        return river.takeAction(this.action, this.equity, this.currentPot, this.turnBetTurn, this.isButton, hadBigBet);
    }

    public void updateAfterHandOver(Action[] givenActions){
        if (hadBigBet)
            historian.numBigBets++;
        historian.numHands++;

        switch(turnCounter){
        case 3:
            historian.reachedFlop++;
            break;
        case 4:
            historian.reachedFlop++;
            historian.reachedTurn++;
            break;
        case 5:
            historian.reachedFlop++;
            historian.reachedTurn++;
            historian.reachedRiver++;
            break;
        }

        for (Action a: givenActions){
            if (a.actionType().equals("FOLD") && a.actor().equals(opponentName)){
                switch (turnCounter){
                case 0:
                    historian.foldPreFlop++;
                    break;
                case 3:
                    historian.foldFlop++;
                    break;
                case 4:
                    historian.foldTurn++;
                    break;
                case 5:
                    historian.foldRiver++;
                    break;
                }
            }
        }
        
        if (historian.numHands >= 1500)
            historian.halveEverything();
        
        historian.printAll();
    }
}
