package player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * Simple example pokerbot, written in Java.
 * 
 * This is an example of a bare bones, pokerbot. It only sets up the socket
 * necessary to connect with the engine and then always returns the same action.
 * It is meant as an example of how a pokerbot should communicate with the
 * engine.
 * 
 */
public class Player {
    
    private final PrintWriter outStream;
    private final BufferedReader inStream;
    // Creating Brain
    private Brain myBrain;
    
    public Player(PrintWriter output, BufferedReader input) {
        this.outStream = output;
        this.inStream = input;
        // Initializing the brain
        this.myBrain = new Brain();
    }
    
    public void run() {
        String input;
        try {
            // Block until engine sends us a packet; read it into input.
            while ((input = inStream.readLine()) != null) {
        
                // We call this function to perform parsing
                parsePacket(input);
                
                // Right Now: It's only printing game stats in dump file
                System.out.println(input);
                
                String word = input.split(" ")[0];
                if ("GETACTION".compareToIgnoreCase(word) == 0) {
                    // When appropriate, reply to the engine with a legal
                    // action.
                    // The engine will ignore all spurious packets you send.
                    // The engine will also check/fold for you if you return an
                    // illegal action.
                    
                    // It checks the brain to get the action
                    
                    String action = this.myBrain.decision();
                    outStream.println(action);
                    
                } else if ("REQUESTKEYVALUES".compareToIgnoreCase(word) == 0) {
                    // At the end, engine will allow bot to send key/value pairs to store.
                    // FINISH indicates no more to store.
                    outStream.println("FINISH");
                }
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }

        System.out.println("Gameover, engine disconnected");
        
        // Once the server disconnects from us, close our streams and sockets.
        try {
            outStream.close();
            inStream.close();
        } catch (IOException e) {
            System.out.println("Encounterd problem shutting down connections");
            e.printStackTrace();
        }
    }
    
    public void parsePacket(String packet) throws IOException{
        /**
         * This function parse the necessary packets accordingly
         */
        
        String[] components = packet.split(" ");
        String word = components[0];
   
        if(word.compareToIgnoreCase("GETACTION") == 0)
            processGetAction(components);
        else if(word.compareToIgnoreCase("NEWHAND") == 0)
            processNewHand(components);
        else if(word.compareToIgnoreCase("HANDOVER") == 0)
            processHandOver(components);
        else if(word.compareToIgnoreCase("REQUESTKEYVALUES") == 0)
            processRequestKeyValues(components);
        else if(word.compareToIgnoreCase("KEYVALUE") == 0)
            processKeyValue(components);
        else /* word.compareToIgnoreCase(NEWGAME_STRING) == 0) */
            processNewGame(components);
    }
    
    private void processNewGame(String word[]){
        String myName = word[1];
        String opponentName = word[2];
        int handsNumber = Integer.parseInt(word[5]);
        double bankTime = Double.parseDouble(word[6]);
       
        // Call the brain saying that new game is starting.
        this.myBrain.newGame(myName, opponentName, handsNumber, bankTime);
    }
    
    private void processKeyValue(String word[]){
        // Need to implement something
    }
    
    private void processRequestKeyValues(String word[]){
        int bytesLeft = Integer.parseInt(word[1]);
        outStream.println("FINISH");
    }
    
    private void processNewHand(String word[]){
        int handId = Integer.parseInt(word[1]);
        boolean isButton = Boolean.valueOf(word[2]);
        String[] myHoleCards = new String[4];
        
        myHoleCards[0] = word[3+ 0];

        myHoleCards[1] = word[3+ 1];

        myHoleCards[2] = word[3+ 2];

        myHoleCards[3] = word[3+ 3];
        
        int myBank = Integer.parseInt(word[7]);
        int otherBank = Integer.parseInt(word[8]);
        double timeBank = Double.parseDouble(word[9]);
        
        this.myBrain.newHand(handId, isButton, myHoleCards, 
                myBank, otherBank, timeBank);
    }
    
    private void processGetAction(String word[]){
        int potSize = Integer.parseInt(word[1]);
        int numBoardCards = Integer.parseInt(word[2]);
        String[] boardCards = new String[numBoardCards];
        
        int index = 3;
        for(int i = 0; i < numBoardCards; i++){
            boardCards[i] = word[index];
            index++;
        }
        
        int numLastActions = Integer.parseInt(word[index]);
        index++;
        Action[] lastActions = new Action[numLastActions];
        for(int j = 0;j < numLastActions; j++){
            lastActions[j] = new Action(word[index]);
            index++;
        }
        
        int numLegalActions =Integer.parseInt(word[index]);
        index++;
        String[] legalActions = new String[numLegalActions];
        for(int k = 0;k < numLegalActions; k++){
            legalActions[k] = word[index];
            index++;
        }
        
        double timeBank = Double.parseDouble(word[index]);
        this.myBrain.getAction(potSize, numBoardCards, 
                boardCards, numLastActions, lastActions, 
                numLegalActions, legalActions, timeBank);
    }
    
    private void processHandOver(String word[]){
        int myBank = Integer.parseInt(word[1]);
        int otherBank = Integer.parseInt(word[2]);
        
        int numBoardCards = Integer.parseInt(word[3]);
        
        String[] boardCards = new String[numBoardCards];
        
        int index = 4;
        for(int i = 0; i < numBoardCards; i++){
            boardCards[i] = word[index];
            index++;
        }
        
        int numLastActions = Integer.parseInt(word[index]);
        index++;
        Action[] lastActions = new Action[numLastActions];
        for(int j = 0;j < numLastActions; j++){
            lastActions[j] = new Action(word[index]);
            index++;
        }
        
        double timeBank = Double.parseDouble(word[index]);
    }
}
