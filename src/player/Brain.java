package player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import equity.poker.Main;

public class Brain {
    ProcessActions action = new ProcessActions();
    Historian h1 = new Historian();
    Historian h2 = new Historian();
    Historian historian = h1;
    PostFlop postFlop = new PostFlop(3);
    PostFlop turn = new PostFlop(4);
    PostFlop river = new PostFlop(5);

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
    boolean isSlowPlay;

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
        this.isSlowPlay = false;
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
                h1.preFlopTypes[preFlopType]++;
                h2.preFlopTypes[preFlopType]++;
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
            if ((bet >= 12 && bet*2.2 >= currentPot) || bet >= 50)
                hadBigBet = true;
        }
        
        return postFlop.takeAction(action, equity, currentPot, flopBetTurn, isButton, historian, hadBigBet);
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
            if ((bet >= 12 && bet*2.2 >= currentPot) || bet >= 50)
                hadBigBet = true;
        }
        
        return turn.takeAction(action, equity, currentPot, turnBetTurn, isButton, historian, hadBigBet);
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
            if ((bet >= 12 && bet*2.2 >= currentPot) || bet >= 50)
                hadBigBet = true;
        }
        
        return river.takeAction(action, equity, currentPot, turnBetTurn, isButton, historian, hadBigBet);
    }

    public void updateAfterHandOver(Action[] givenActions){
        if (hadBigBet){
            h1.numBigBets++;
            h2.numBigBets++;
        }
            
        h1.numHands++;
        h2.numHands++;

        switch(turnCounter){
        case 3:
            h1.reachedFlop++;
            h2.reachedFlop++;
            break;
        case 4:
            h1.reachedFlop++;
            h1.reachedTurn++;
            h2.reachedFlop++;
            h2.reachedTurn++;
            break;
        case 5:
            h1.reachedFlop++;
            h1.reachedTurn++;
            h1.reachedRiver++;
            h2.reachedFlop++;
            h2.reachedTurn++;
            h2.reachedRiver++;
            break;
        }

        for (Action a: givenActions){
            if (a.actionType().equals("FOLD") && a.actor().equals(opponentName)){
                switch (turnCounter){
                case 0:
                    h1.foldPreFlop++;
                    h2.foldPreFlop++;
                    break;
                case 3:
                    h1.foldFlop++;
                    h2.foldFlop++;
                    break;
                case 4:
                    h1.foldTurn++;
                    h2.foldTurn++;
                    break;
                case 5:
                    h1.foldRiver++;
                    h2.foldRiver++;
                    break;
                }
            }
            
            if (a.actionType().equals("WIN") && a.amount() == 800){
                h1.ultimateGames++;
                h2.ultimateGames++;
                if (a.actor().equals(opponentName)){
                    h1.ultimateWins++;
                    h2.ultimateWins++;
                }
            }
        }
        
        if (historian.ultimateOpponentWinFrequency() >= 0.7 && historian.ultimateGames >= 10){
            System.out.println("Reducing bigBets to 3/4 of its original value");
            h1.ultimateRaiseStandards();
            h2.ultimateRaiseStandards();
        }
        
        if (historian.numHands % 1000 == 0){
            historian.clear();
            if (h1.numHands != 0){
                historian = h1;
            } else {
                historian = h2;
            }
        }
        historian.printAll();
    }
}
