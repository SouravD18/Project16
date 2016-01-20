package equity.poker;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gnu.trove.map.TLongDoubleMap;
import gnu.trove.map.hash.TLongDoubleHashMap;

public final class Enumerator extends Thread {

    private final int	startIx;	// where to start outer loop through deck
    private final int	nBoardCards;
    private long[]		deck;
    private boolean[]	dealt;
    private final int	limitIx1, limitIx2, limitIx3, limitIx4, limitIx5;

    private long[]      board = new long[5];
    private long[]      myCards = new long[4];
    private long[]      opponentCards = new long[4];
    private int         myHandValue;
    private int         opponentHandValue;
    private final int   increment;  // of outer loop through deck -- number of threads
    public long         wins = 0L;
    public long         splits = 0L;
    public long         losses = 0L;

    public final boolean isSimulation;
    public final int numSimulations;
    public static final Map<String, Long> cardMap = new HashMap<>();
    public static final String[] deckArr = { 
            "2h", "2s", "2c", "2d",
            "3h", "3s", "3c", "3d", "4h", "4s", "4c", "4d", "5h", "5s", "5c",
            "5d", "6h", "6s", "6c", "6d", "7h", "7s", "7c", "7d", "8h", "8s",
            "8c", "8d", "9h", "9s", "9c", "9d", "Th", "Ts", "Tc", "Td", "Jh",
            "Js", "Jc", "Jd", "Qh", "Qs", "Qc", "Qd", "Kh", "Ks", "Kc", "Kd",
            "Ah", "As", "Ac", "Ad" 
    };
    public static final Set<String> deckSet = new HashSet<String>(Arrays.asList(deckArr));
    public static TLongDoubleMap startingHandEquityMap;
    static {
        cardMap.put("2c", 0x1L << 0);
        cardMap.put("2d", 0x1L << 16);
        cardMap.put("2h", 0x1L << 32);
        cardMap.put("2s", 0x1L << 48);
        cardMap.put("3c", 0x1L << 1);
        cardMap.put("3d", 0x1L << 17);
        cardMap.put("3h", 0x1L << 33);
        cardMap.put("3s", 0x1L << 49);
        cardMap.put("4c", 0x1L << 2);
        cardMap.put("4d", 0x1L << 18);
        cardMap.put("4h", 0x1L << 34);
        cardMap.put("4s", 0x1L << 50);
        cardMap.put("5c", 0x1L << 3);
        cardMap.put("5d", 0x1L << 19);
        cardMap.put("5h", 0x1L << 35);
        cardMap.put("5s", 0x1L << 51);
        cardMap.put("6c", 0x1L << 4);
        cardMap.put("6d", 0x1L << 20);
        cardMap.put("6h", 0x1L << 36);
        cardMap.put("6s", 0x1L << 52);
        cardMap.put("7c", 0x1L << 5);
        cardMap.put("7d", 0x1L << 21);
        cardMap.put("7h", 0x1L << 37);
        cardMap.put("7s", 0x1L << 53);
        cardMap.put("8c", 0x1L << 6);
        cardMap.put("8d", 0x1L << 22);
        cardMap.put("8h", 0x1L << 38);
        cardMap.put("8s", 0x1L << 54);
        cardMap.put("9c", 0x1L << 7);
        cardMap.put("9d", 0x1L << 23);
        cardMap.put("9h", 0x1L << 39);
        cardMap.put("9s", 0x1L << 55);
        cardMap.put("Tc", 0x1L << 8);
        cardMap.put("Td", 0x1L << 24);
        cardMap.put("Th", 0x1L << 40);
        cardMap.put("Ts", 0x1L << 56);
        cardMap.put("Jc", 0x1L << 9);
        cardMap.put("Jd", 0x1L << 25);
        cardMap.put("Jh", 0x1L << 41);
        cardMap.put("Js", 0x1L << 57);
        cardMap.put("Qc", 0x1L << 10);
        cardMap.put("Qd", 0x1L << 26);
        cardMap.put("Qh", 0x1L << 42);
        cardMap.put("Qs", 0x1L << 58);
        cardMap.put("Kc", 0x1L << 11);
        cardMap.put("Kd", 0x1L << 27);
        cardMap.put("Kh", 0x1L << 43);
        cardMap.put("Ks", 0x1L << 59);
        cardMap.put("Ac", 0x1L << 12);
        cardMap.put("Ad", 0x1L << 28);
        cardMap.put("Ah", 0x1L << 44);
        cardMap.put("As", 0x1L << 60);

        FileInputStream fis;
        try {
            fis = new FileInputStream(new File("startingMap"));
            ObjectInputStream ois = new ObjectInputStream(fis);
            startingHandEquityMap = (TLongDoubleHashMap) ois.readObject();
            ois.close();
            fis.close();
        } catch (ClassNotFoundException | IOException e1) {
            e1.printStackTrace();
        }
    }

    @Override public final void run() {
        if (isSimulation)
            simulateBoardsAndHands();
        else
            enumBoards();
    }

    /**
     * Counts all possibilities and records win/split/loss
     * @param instance Which thread this will run on (each Enumerator MUST have a different instance number)
     * @param instances Number of threads
     * @param myCards Your array of 4 cards - specified as e.g. "Ac" or "4s"
     * @param board Can contain either 0 cards, 3, 4, or 5 cards
     * @param numSimulations If equal to 0, it enums every possibility, else sets # of simulations per core
     */
    public Enumerator(final int instance, final int instances, final String[] myCards, final String[] board, int numSimulations) {
        super("Enumerator" + instance);
        startIx = instance;
        increment = instances;
        if (numSimulations == 0)
            isSimulation = false;
        else
            isSimulation = true;
        this.numSimulations = numSimulations;

        for (int j = 0; j < myCards.length; j++){
            this.myCards[j] = cardMap.get(myCards[j]);
        }
        for (int j = 0; j < board.length; j++){
            this.board[j] = cardMap.get(board[j]);
        }
        
        int nCardsInDeck = 52 - (board.length + myCards.length);
        this.deck = new long[nCardsInDeck];
        dealt = new boolean[nCardsInDeck];

        int i = 0;
        Set<String> tempDeck = new HashSet<>(deckSet);
        for (String s: myCards)
            tempDeck.remove(s);
        for (String s: board)
            tempDeck.remove(s);

        for (String s: tempDeck){
            this.deck[i++] = cardMap.get(s);
        }

        nBoardCards = board.length;
        limitIx1 = nCardsInDeck - 5;
        limitIx2 = nCardsInDeck - 4;
        limitIx3 = nCardsInDeck - 3;
        limitIx4 = nCardsInDeck - 2;
        limitIx5 = nCardsInDeck - 1;
    }
    
    /**
     * Constructor only to be used when starting up the bot
     * So that all objects can be loaded onto memory without
     * Counting against the time limit
     */
    public Enumerator(){
        startIx = 0;
        nBoardCards = 0;
        deck = null;
        dealt = null;
        limitIx1 = 0;
        limitIx2 = 0;
        limitIx3 = 0;
        limitIx4 = 0;
        limitIx5 = 0;
        board = null;
        myCards = null;
        opponentCards = null;
        myHandValue = 0;
        opponentHandValue = 0;
        increment = 0;
        isSimulation = false;
        numSimulations = 0;
    }

    /**
     * Called whenever both you and your opponent's hands are known
     * And you're iterating through all the possible boards
     */
    private void enumAllDecksWithKnownHands() {
        int handValue0, handValue1;
        for (int deckIx1 = startIx; deckIx1 <= limitIx1; deckIx1 += increment) {
            board[0] = deck[deckIx1];
            for (int deckIx2 = deckIx1 + 1; deckIx2 <= limitIx2; ++deckIx2) {
                board[1] = deck[deckIx2];
                for (int deckIx3 = deckIx2 + 1; deckIx3 <= limitIx3; ++deckIx3) {
                    board[2] = deck[deckIx3];
                    for (int deckIx4 = deckIx3 + 1; deckIx4 <= limitIx4; ++deckIx4) {
                        board[3] = deck[deckIx4];
                        for (int deckIx5 = deckIx4 + 1; deckIx5 <= limitIx5; ++deckIx5) {
                            board[4] = deck[deckIx5];
                            handValue0 = HandEval.OmahaHighEval(board, myCards);
                            handValue1 = HandEval.OmahaHighEval(board, opponentCards);
                            if (handValue0 > handValue1)
                                wins++;
                            else if (handValue0 == handValue1)
                                splits++;
                            else losses++;
                        }
                    }
                }
            }
        }
    }

    /**
     * Precondition: You must have called OmahaHighEval on your own cards by then
     * Compares your hand value against the opponent's (with the board cards) and 
     * records the stats
     */
    private void potResults() {
        opponentHandValue = HandEval.OmahaHighEval(board, opponentCards);
        if (myHandValue > opponentHandValue)
            wins++;
        else if (myHandValue == opponentHandValue)
            splits++;
        else losses++;
    }

    /**
     * Enumerates through all the unknown cards of a board
     */
    private void enumBoards() {
        switch (nBoardCards) { 
        case 0: //all board cards are unknown, enumerating through all of them
            for (int deckIx1 = startIx; deckIx1 <= limitIx1; deckIx1 += increment) {
                dealt[deckIx1] = true;
                board[0] = deck[deckIx1];
                for (int deckIx2 = deckIx1 + 1; deckIx2 <= limitIx2; ++deckIx2) {
                    dealt[deckIx2] = true;
                    board[1] = deck[deckIx2];
                    for (int deckIx3 = deckIx2 + 1; deckIx3 <= limitIx3; ++deckIx3) {
                        dealt[deckIx3] = true;
                        board[2] = deck[deckIx3];
                        for (int deckIx4 = deckIx3 + 1; deckIx4 <= limitIx4; ++deckIx4) {
                            dealt[deckIx4] = true;
                            board[3] = deck[deckIx4];
                            for (int deckIx51 = deckIx4 + 1; deckIx51 <= limitIx5; ++deckIx51) {
                                dealt[deckIx51] = true;
                                board[4] = deck[deckIx51];
                                enumOpponentHands();
                                dealt[deckIx51] = false;
                            }
                            dealt[deckIx4] = false;
                        }
                        dealt[deckIx3] = false;
                    }
                    dealt[deckIx2] = false;
                }
                dealt[deckIx1] = false;
            }
            break;
        case 3: //3 board cards known
            for (int deckIx4 = startIx; deckIx4 <= limitIx4; deckIx4 += increment) {
                dealt[deckIx4] = true;
                board[3] = deck[deckIx4];
                for (int deckIx51 = deckIx4 + 1; deckIx51 <= limitIx5; ++deckIx51) {
                    dealt[deckIx51] = true;
                    board[4] = deck[deckIx51];
                    enumOpponentHands();
                    dealt[deckIx51] = false;
                }
                dealt[deckIx4] = false;
            }
            break;
        case 4: //4 board cards known
            for (int deckIx5 = startIx; deckIx5 <= limitIx5; deckIx5 += increment) {
                dealt[deckIx5] = true;
                board[4] = deck[deckIx5];
                enumOpponentHands();
                dealt[deckIx5] = false;
            }
            break;
        case 5: //all 5 board cards known
            enumOpponentHandsMultiThreaded();
            break;
        default:
            throw new RuntimeException("We must know either 0, 3, 4, or 5 board cards");
        }
    }

    /**
     * Called when all board cards are determined, now iterating through all possible opponent hands
     * You MUST determine myHandValue here before calling potResults
     */
    private void enumOpponentHands() {
        myHandValue = HandEval.OmahaHighEval(board, myCards);
        for (int deckIx1 = 0; deckIx1 <= limitIx2; ++deckIx1) {
            if (dealt[deckIx1])
                continue;
            opponentCards[0] = deck[deckIx1];
            for (int deckIx2 = deckIx1 + 1; deckIx2 <= limitIx3; ++deckIx2) {
                if (dealt[deckIx2])
                    continue;
                opponentCards[1] = deck[deckIx2];
                for (int deckIx3 = deckIx2 + 1; deckIx3 <= limitIx4; ++deckIx3) {
                    if (dealt[deckIx3])
                        continue;
                    opponentCards[2] = deck[deckIx3];
                    for (int deckIx4 = deckIx3 + 1; deckIx4 <= limitIx5; ++deckIx4) {
                        if (dealt[deckIx4])
                            continue;
                        opponentCards[3] = deck[deckIx4];
                        potResults();
                    }
                }
            }
        }
    }

    /**
     * Call this only when all 5 board cards are known
     * This will make use of all cores in evaluating through 
     * You MUST determine myHandValue here before calling potResults
     */
    private void enumOpponentHandsMultiThreaded() {
        myHandValue = HandEval.OmahaHighEval(board, myCards);
        for (int deckIx1 = startIx; deckIx1 <= limitIx2; deckIx1 += increment) {
            if (dealt[deckIx1])
                continue;
            opponentCards[0] = deck[deckIx1];
            for (int deckIx2 = deckIx1 + 1; deckIx2 <= limitIx3; ++deckIx2) {
                if (dealt[deckIx2])
                    continue;
                opponentCards[1] = deck[deckIx2];
                for (int deckIx3 = deckIx2 + 1; deckIx3 <= limitIx4; ++deckIx3) {
                    if (dealt[deckIx3])
                        continue;
                    opponentCards[2] = deck[deckIx3];
                    for (int deckIx4 = deckIx3 + 1; deckIx4 <= limitIx5; ++deckIx4) {
                        if (dealt[deckIx4])
                            continue;
                        opponentCards[3] = deck[deckIx4];
                        potResults();
                    }
                }
            }
        }
    }

    /**
     * Use this method to run simulations
     */
    private void simulateBoardsAndHands(){
        int deckIx1, deckIx2, deckIx3, deckIx4, deckIx5, card1, card2, card3, card4;
        switch (nBoardCards){
        case 0:
            for (int i = 0; i < numSimulations; i++){
                deckIx1 = getRandomNumberPre();
                dealt[deckIx1] = true;
                deckIx2 = getRandomNumberPre();
                while (dealt[deckIx2])
                    deckIx2 = getRandomNumberPre();
                dealt[deckIx2] = true;
                deckIx3 = getRandomNumberPre();
                while (dealt[deckIx3])
                    deckIx3 = getRandomNumberPre();
                dealt[deckIx3] = true;
                deckIx4 = getRandomNumberPre();
                while (dealt[deckIx4])
                    deckIx4 = getRandomNumberPre();
                dealt[deckIx4] = true;
                deckIx5 = getRandomNumberPre();
                while (dealt[deckIx5])
                    deckIx5 = getRandomNumberPre();
                dealt[deckIx5] = true;
                card1 = getRandomNumberPre();
                while (dealt[card1])
                    card1 = getRandomNumberPre();
                dealt[card1] = true;
                card2 = getRandomNumberPre();
                while (dealt[card2])
                    card2 = getRandomNumberPre();
                dealt[card2] = true;
                card3 = getRandomNumberPre();
                while (dealt[card3])
                    card3 = getRandomNumberPre();
                dealt[card3] = true;
                card4 = getRandomNumberPre();
                while (dealt[card4])
                    card4 = getRandomNumberPre();
                board[0] = deck[deckIx1];
                board[1] = deck[deckIx2];
                board[2] = deck[deckIx3];
                board[3] = deck[deckIx4];
                board[4] = deck[deckIx5];
                opponentCards[0] = deck[card1];
                opponentCards[1] = deck[card2];
                opponentCards[2] = deck[card3];
                opponentCards[3] = deck[card4];
                myHandValue = HandEval.OmahaHighEval(board, myCards);
                potResults();

                dealt[deckIx1] = false;
                dealt[deckIx2] = false;
                dealt[deckIx3] = false;
                dealt[deckIx4] = false;
                dealt[deckIx5] = false;
                dealt[card1] = false;
                dealt[card2] = false;
                dealt[card3] = false;
                dealt[card4] = false;
            }
            break;
        case 3:
            for (int i = 0; i < numSimulations; i++){
                deckIx4 = getRandomNumberFlop();
                dealt[deckIx4] = true;
                deckIx5 = getRandomNumberFlop();
                while (dealt[deckIx5])
                    deckIx5 = getRandomNumberFlop();
                dealt[deckIx5] = true;
                card1 = getRandomNumberFlop();
                while (dealt[card1])
                    card1 = getRandomNumberFlop();
                dealt[card1] = true;
                card2 = getRandomNumberFlop();
                while (dealt[card2])
                    card2 = getRandomNumberFlop();
                dealt[card2] = true;
                card3 = getRandomNumberFlop();
                while (dealt[card3])
                    card3 = getRandomNumberFlop();
                dealt[card3] = true;
                card4 = getRandomNumberFlop();
                while (dealt[card4])
                    card4 = getRandomNumberFlop();
                board[3] = deck[deckIx4];
                board[4] = deck[deckIx5];
                opponentCards[0] = deck[card1];
                opponentCards[1] = deck[card2];
                opponentCards[2] = deck[card3];
                opponentCards[3] = deck[card4];
                myHandValue = HandEval.OmahaHighEval(board, myCards);
                potResults();

                dealt[deckIx4] = false;
                dealt[deckIx5] = false;
                dealt[card1] = false;
                dealt[card2] = false;
                dealt[card3] = false;
                dealt[card4] = false;
            }
            break;
        case 4:
            for (int i = 0; i < numSimulations; i++){
                deckIx5 = getRandomNumberTurn();
                dealt[deckIx5] = true;
                card1 = getRandomNumberTurn();
                while (dealt[card1])
                    card1 = getRandomNumberTurn();
                dealt[card1] = true;
                card2 = getRandomNumberTurn();
                while (dealt[card2])
                    card2 = getRandomNumberTurn();
                dealt[card2] = true;
                card3 = getRandomNumberTurn();
                while (dealt[card3])
                    card3 = getRandomNumberTurn();
                dealt[card3] = true;
                card4 = getRandomNumberTurn();
                while (dealt[card4])
                    card4 = getRandomNumberTurn();
                board[4] = deck[deckIx5];
                opponentCards[0] = deck[card1];
                opponentCards[1] = deck[card2];
                opponentCards[2] = deck[card3];
                opponentCards[3] = deck[card4];
                myHandValue = HandEval.OmahaHighEval(board, myCards);
                potResults();

                dealt[deckIx5] = false;
                dealt[card1] = false;
                dealt[card2] = false;
                dealt[card3] = false;
                dealt[card4] = false;
            }
            break;
        case 5:
            for (int i = 0; i < numSimulations; i++){
                card1 = getRandomNumberRiver();
                while (dealt[card1])
                    card1 = getRandomNumberRiver();
                dealt[card1] = true;
                card2 = getRandomNumberRiver();
                while (dealt[card2])
                    card2 = getRandomNumberRiver();
                dealt[card2] = true;
                card3 = getRandomNumberRiver();
                while (dealt[card3])
                    card3 = getRandomNumberRiver();
                dealt[card3] = true;
                card4 = getRandomNumberRiver();
                while (dealt[card4])
                    card4 = getRandomNumberRiver();
                opponentCards[0] = deck[card1];
                opponentCards[1] = deck[card2];
                opponentCards[2] = deck[card3];
                opponentCards[3] = deck[card4];
                myHandValue = HandEval.OmahaHighEval(board, myCards);
                potResults();

                dealt[card1] = false;
                dealt[card2] = false;
                dealt[card3] = false;
                dealt[card4] = false;
            }
            break;
        default:
            break;
        }
    }

    private int getRandomNumberPre(){
        long x = System.nanoTime();
        x ^= (x << 21);
        x ^= (x >>> 35);
        x ^= (x << 4);
        return (int) Math.floorMod(x, 48);
    }

    private int getRandomNumberFlop(){
        long x = System.nanoTime();
        x ^= (x << 21);
        x ^= (x >>> 35);
        x ^= (x << 4);
        return (int) Math.floorMod(x, 45);
    }

    private int getRandomNumberTurn(){
        long x = System.nanoTime();
        x ^= (x << 21);
        x ^= (x >>> 35);
        x ^= (x << 4);
        return (int) Math.floorMod(x, 44);
    }

    private int getRandomNumberRiver(){
        long x = System.nanoTime();
        x ^= (x << 21);
        x ^= (x >>> 35);
        x ^= (x << 4);
        return (int) Math.floorMod(x, 43);
    }
}
