package player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import equity.poker.Main;

public class Brain {
    // Object to process actions taken by the bot
    ProcessActions action = new ProcessActions();
    // Printer for Dump Files
    DumpPrinter printer = new DumpPrinter();
    // Historian
    Historian opponentHistorian = new Historian();


    // My name and Opponent Name
    String[] Names = new String[2];

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
    int previousPot = 0;

    // List of actions:
    // List[0] = my last action
    // List[1] = opponent's last action
    // List[2] = last action
    Action[] actions = new Action[3];
    int numberOfLastActions = 0;

    boolean raisedPreflop = false;
    boolean checkedPreflop = false;
    boolean myRaisePreflop = false;
    boolean opponentHighBet = false;
    boolean opponentVeryHighBet = false;
    int betType = 0;

    // Turn Counter keep track of turns: 
    // Preflop = 0; Flop = 3; Turn = 4; River = 5;
    int turnCounter = 0;
    //betting turntrackers
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

        // Reset some stuff:
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
        this.previousPot = 0;

        this.preFlopBetTurn = 0;
        this.flopBetTurn = 0;
        this.turnBetTurn = 0;
        this.riverBetTurn = 0;

        this.raisedPreflop = false;
        this.checkedPreflop = false;
        this.myRaisePreflop = false;
        this.opponentHighBet = false;
        this.opponentVeryHighBet = false;
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

        return PreFlop.takeAction(this.action, this.equity, this.currentPot, this.preFlopBetTurn, this.isButton, this.opponentHistorian);
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
            opponentHistorian.wentToFlop += 1;

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
            if(flopBetTurn==1 && opponentActionType.equals("BET")){
                opponentHistorian.flopBet +=1;
            }
            if((flopBetTurn==1 && raisedPreflop) && opponentActionType.equals("BET")){
                opponentHistorian.continuationBet += 1;
            }
            else if(flopBetTurn==2 && myActionType.equals("BET")){
                opponentHistorian.checkRaise +=1; 
                opponentHistorian.checkRaiseFlop +=1;
            }
            else if(flopBetTurn==2 && myActionType.equals("RAISE")){
                opponentHistorian.threeBet_flop += 1;
            }
            if ((opponentActionType.equals("BET") || opponentActionType.equals("RAISE")) && this.actions[1].amount() >= 12 && this.actions[1].amount() * 3 >= currentPot){
                opponentHighBet = true;
                betType = 1;
                if (this.actions[1].amount() >= 24 && this.actions[1].amount() * 2 >= currentPot){
                    opponentVeryHighBet = true;
                    betType = 2;  
                }
            }
            // Opponent Second to act
            else{
                if(flopBetTurn==2 && opponentActionType.equals("BET")){
                    opponentHistorian.flopBet +=1;
                }
                if(flopBetTurn==2 && raisedPreflop){
                    opponentHistorian.continuationBet += 1;
                }
                if ((opponentActionType.equals("BET") || opponentActionType.equals("RAISE")) && this.actions[1].amount() >= 12 && this.actions[1].amount() * 3 >= currentPot){
                    opponentHighBet = true;
                    betType = 1;
                    if (this.actions[1].amount() >= 24 && this.actions[1].amount() * 2 >= currentPot){
                        opponentVeryHighBet = true;
                        betType = 2;  
                    }
                }

            }
        }
        return Flop.takeAction(this.action, this.equity, this.currentPot, this.flopBetTurn, this.isButton, this.opponentHistorian, betType);
    }    

    private String turn(){
        String opponentActionType =this.actions[1].actionType();
        String myActionType = this.actions[0].actionType();

        this.turnBetTurn += 1;
        if(this.turnBetTurn == 1){
            opponentHistorian.wentToTurn += 1;

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
            if(turnBetTurn==1 && opponentActionType.equals("BET")){
                opponentHistorian.turnBet +=1;
            }
            if(opponentActionType.equals("BET") || opponentActionType.equals("RAISE")){
                opponentHistorian.betOrRaiseCount += 1;
            }
            if(turnBetTurn==2 && myActionType.equals("BET")){
                opponentHistorian.checkRaiseTurn +=1;
            }
            if ((opponentActionType.equals("BET") || opponentActionType.equals("RAISE")) && this.actions[1].amount() >= 12 && this.actions[1].amount() * 3 >= currentPot){
                opponentHighBet = true;
                betType = 1;
                if (this.actions[1].amount() >= 24 && this.actions[1].amount() * 2 >= currentPot){
                    opponentVeryHighBet = true;
                    betType = 2;  
                }
            }
            // Opponent second to act
            else{
                if(this.turnBetTurn > 1){
                    opponentHistorian.betOrRaiseCount += 1;
                }
                if(turnBetTurn==2 && opponentActionType.equals("BET")){
                    opponentHistorian.turnBet +=1;
                }
                if ((opponentActionType.equals("BET") || opponentActionType.equals("RAISE")) && this.actions[1].amount() >= 12 && this.actions[1].amount() * 3 >= currentPot){
                    opponentHighBet = true;
                    betType = 1;
                    if (this.actions[1].amount() >= 24 && this.actions[1].amount() * 2 >= currentPot){
                        opponentVeryHighBet = true;
                        betType = 2;  
                    }
                }
            }
        }
        return Turn.takeAction(this.action, this.equity, this.currentPot, this.turnBetTurn, this.isButton, this.opponentHistorian, betType);
    }

    private String river(){
        String opponentActionType = this.actions[1].actionType();
        String myActionType = this.actions[0].actionType();
        this.riverBetTurn += 1;
        if(this.riverBetTurn == 1){
            opponentHistorian.wentToRiver += 1;

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
            if(riverBetTurn==1 && opponentActionType.equals("BET")){
                opponentHistorian.riverBet +=1;
            }

            if(riverBetTurn == 1){

                boolean called = this.actions[0].actionType().equals("BET") ||
                        this.actions[0].actionType().equals("RAISE");
                if(called){
                    opponentHistorian.callCount += 1;
                }
            }

            if(opponentActionType.equals("BET") || opponentActionType.equals("RAISE")){
                opponentHistorian.betOrRaiseCount += 1;
            }

            if(riverBetTurn==2 && myActionType.equals("BET")){
                opponentHistorian.checkRaiseRiver +=1;
            }
            if ((opponentActionType.equals("BET") || opponentActionType.equals("RAISE")) && this.actions[1].amount() >= 12 && this.actions[1].amount() * 3 >= currentPot){
                opponentHighBet = true;
                betType = 1;
                if (this.actions[1].amount() >= 24 && this.actions[1].amount() * 2 >= currentPot){
                    opponentVeryHighBet = true;
                    betType = 2;  
                }
            }
            // Opponent second to act
            else{
                if(turnBetTurn==2 && opponentActionType.equals("BET")){
                    opponentHistorian.turnBet +=1;
                }
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
                if ((opponentActionType.equals("BET") || opponentActionType.equals("RAISE")) && this.actions[1].amount() >= 12 && this.actions[1].amount() * 3 >= currentPot){
                    opponentHighBet = true;
                    betType = 1;
                    if (this.actions[1].amount() >= 24 && this.actions[1].amount() * 2 >= currentPot){
                        opponentVeryHighBet = true;
                        betType = 2;  
                    }

                }
            }
        }
        return River.takeAction(this.action, this.equity, this.currentPot, this.riverBetTurn, this.isButton, this.opponentHistorian, betType);
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
            if(this.turnCounter > 3){
                opponentHistorian.foldInPost += 1;
                opponentHistorian.wentToPost += 1;
            }
            if(this.turnCounter == 3){
                opponentHistorian.foldFlop +=1;
            }
            else if(this.turnCounter == 4){
                opponentHistorian.foldTurn += 1;
            }
            else if(this.turnCounter == 5){
                opponentHistorian.foldRiver += 1;
            }
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

        if (opponentHighBet)
            opponentHistorian.highBet++;;
            if (opponentVeryHighBet)
                opponentHistorian.veryHighBet++;

            this.opponentHistorian.printAll();

    }
}
