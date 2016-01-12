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
	
	// Fields Created for our bot:
	
	private String[] myHoleCards;
	private String[] boardCards;
	
	// Boolean to indicate whether our bot is a button or not
	private boolean isButton;
	
	// Number of hands remaining
	private int numberOfHands;
	
	private int myBank; 
	private int otherBank;
	
	// Amount of time remaining
	private double timeBank;
	
	// current number of the hand
	private int handId;
	
	// List of names. First name is our bot's name. Second is for opponent
	private String[] names;
	
	// Brain
	private Brain myBrain;
	
	public Player(PrintWriter output, BufferedReader input) {
		this.outStream = output;
		this.inStream = input;
		
		this.myHoleCards = new String[4];
		this.isButton = false;
		this.numberOfHands = 0;
		this.myBank = 0;
		this.otherBank = 0;
		this.timeBank = .1;
		this.handId = 0;
		this.boardCards = new String[5];
		this.names = new String[2];
		this.myBrain = new Brain();
	}
	
	public void run() {
		String input;
		try {
			// Block until engine sends us a packet; read it into input.
			while ((input = inStream.readLine()) != null) {
			    // Changes made by: sourav18
			    // We call this function to perform parsing
			    parsePacket(input);
			    
				// Here is where you should implement code to parse the packets
				// from the engine and act on it.
				
			    System.out.println(input);
				
				String word = input.split(" ")[0];
				if ("GETACTION".compareToIgnoreCase(word) == 0) {
					// When appropriate, reply to the engine with a legal
					// action.
					// The engine will ignore all spurious packets you send.
					// The engine will also check/fold for you if you return an
					// illegal action.
				    
				    // FOLDERBOT works
				    // Checking Folderbot:
				    
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
        int stackSize = Integer.parseInt(word[3]);
        int bb = Integer.parseInt(word[4]);
        
        this.names[0] = myName;
        this.names[1] = opponentName;
        this.numberOfHands = Integer.parseInt(word[5]);
        this.timeBank = Double.parseDouble(word[6]);
        
        //// Reset Historian!!
        
        // Call the brain saying that new game is starting.
        
        this.myBrain.newGame(myName, opponentName, this.numberOfHands, this.timeBank);
    }
    
    private void processKeyValue(String word[]){
        // Need to implement something
    }
    
    private void processRequestKeyValues(String word[]){
        int bytesLeft = Integer.parseInt(word[1]);
        outStream.println("FINISH");
    }
	
    private void processNewHand(String word[]){
        this.handId = Integer.parseInt(word[1]);
        this.isButton = Boolean.valueOf(word[2]);
        
        this.myHoleCards[0] = word[3+ 0];

        this.myHoleCards[1] = word[3+ 1];

        this.myHoleCards[2] = word[3+ 2];

        this.myHoleCards[3] = word[3+ 3];
        
        this.myBank = Integer.parseInt(word[7]);
        this.otherBank = Integer.parseInt(word[8]);
        this.timeBank = Double.parseDouble(word[9]);
        
        this.myBrain.newHand(this.handId, this.isButton, this.myHoleCards, 
                this.myBank, this.otherBank, this.timeBank);
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
        
        this.timeBank = Double.parseDouble(word[index]);
        this.myBrain.getAction(potSize, numBoardCards, 
                boardCards, numLastActions, lastActions, 
                numLegalActions, legalActions, this.timeBank);
    }
    
    private void processHandOver(String word[]){
        this.myBank = Integer.parseInt(word[1]);
        this.otherBank = Integer.parseInt(word[2]);
        
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
        
        this.timeBank = Double.parseDouble(word[index]);
    }
}
