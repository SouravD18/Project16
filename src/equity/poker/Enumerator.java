package equity.poker;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import equity.poker.*;

final class Enumerator extends Thread {

    private final int	nPlayers;
    private final int	nUnknown;
    private final int	startIx;	// where to start outer loop through deck
    private final int	increment;	// of outer loop through deck -- number of threads
    private long[]		wins, splits;
    private double[]	partialPots;
    private final int	nBoardCards;
    private long[]		deck;
    private boolean[]	dealt;
    private int[]		handValue;
    private final int	limitIx1, limitIx2, limitIx3, limitIx4, limitIx5;
    private long[]       boardCardsBits = new long[5];
    private long[][]     handCardsBits = new long[2][4];
    
    public static final Map<String, Long> cardMap = new HashMap<>();
    private static final String[] deckArr = { 
            "2h", "2s", "2c", "2d",
            "3h", "3s", "3c", "3d", "4h", "4s", "4c", "4d", "5h", "5s", "5c",
            "5d", "6h", "6s", "6c", "6d", "7h", "7s", "7c", "7d", "8h", "8s",
            "8c", "8d", "9h", "9s", "9c", "9d", "Th", "Ts", "Tc", "Td", "Jh",
            "Js", "Jc", "Jd", "Qh", "Qs", "Qc", "Qd", "Kh", "Ks", "Kc", "Kd",
            "Ah", "As", "Ac", "Ad" 
        };
    public static final Set<String> deckSet = new HashSet<String>(Arrays.asList(deckArr));
    
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
    }
    
    @Override public final void run() {
        if (nUnknown == 1)
            enumBoards();
        else if (nUnknown == 0)
            enumBoardsNoUnknown();
        else
            throw new RuntimeException("Invalid number of unknown players");
            
    }

    Enumerator(final int instance, final int instances, final String[][] holeCards, final String[] board) {
        super("Enumerator" + instance);
        startIx = instance;
        increment = instances;
        
        for (int j = 0; j < holeCards.length; j++){
            for (int k = 0; k < holeCards[j].length; k++){
                handCardsBits[j][k] = cardMap.get(holeCards[j][k]);
            }
        }
        for (int j = 0; j < board.length; j++){
            boardCardsBits[j] = cardMap.get(board[j]);
        }
        
        int nCardsInDeck = 52 - (board.length + holeCards[0].length + holeCards[1].length);
        this.deck = new long[nCardsInDeck];
        dealt = new boolean[nCardsInDeck];
        int i = 0;

        Set<String> tempDeck = new HashSet<>(deckSet);
        for (String[] array: holeCards)
            for (String s: array)
                tempDeck.remove(s);
        for (String s: board)
            tempDeck.remove(s);

        for (String s: tempDeck){
            this.deck[i++] = cardMap.get(s);
        }

        nPlayers = 2;
        this.nUnknown = holeCards[1].length == 0 ? 1 : 0;
        nBoardCards = board.length;
        wins = new long[nPlayers];
        splits = new long[nPlayers];
        partialPots = new double[nPlayers];
        handValue = new int[nPlayers];
        limitIx1 = nCardsInDeck - 5;
        limitIx2 = nCardsInDeck - 4;
        limitIx3 = nCardsInDeck - 3;
        limitIx4 = nCardsInDeck - 2;
        limitIx5 = nCardsInDeck - 1;
    }

    long[] getWins() {
        return wins;
    }

    long[] getSplits() {
        return splits;
    }

    double[] getPartialPots() {
        return partialPots;
    }

    private void enum2GuysNoFlop() { // special case for speed of EnumBoardsNoUnknown
        int handValue0, handValue1;
        int wins0 = 0, splits0 = 0, pots = 0;

        for (int deckIx1 = startIx; deckIx1 <= limitIx1; deckIx1 += increment) {
            boardCardsBits[0] = deck[deckIx1];
            for (int deckIx2 = deckIx1 + 1; deckIx2 <= limitIx2; ++deckIx2) {
                boardCardsBits[1] = deck[deckIx2];
                for (int deckIx3 = deckIx2 + 1; deckIx3 <= limitIx3; ++deckIx3) {
                    boardCardsBits[2] = deck[deckIx3];
                    for (int deckIx4 = deckIx3 + 1; deckIx4 <= limitIx4; ++deckIx4) {
                        boardCardsBits[3] = deck[deckIx4];
                        for (int deckIx5 = deckIx4 + 1; deckIx5 <= limitIx5; ++deckIx5) {
                            boardCardsBits[4] = deck[deckIx5];
                            handValue0 = HandEval.OmahaHighEval(boardCardsBits, handCardsBits[0]);
                            handValue1 = HandEval.OmahaHighEval(boardCardsBits, handCardsBits[1]);
                            /*
                             * wins[1], splits[1], and partialPots can be inferred
                             */
                            ++pots;
                            if (handValue0 > handValue1)
                                ++wins0;
                            else if (handValue0 == handValue1)
                                ++splits0;
                        }
                    }
                }
            }
        }
        wins[0] = wins0;
        wins[1] = pots - wins0 - splits0;
        splits[0] = splits[1] = splits0;
        partialPots[0] = partialPots[1] = splits0 / 2.0;
    }

    private void potResults() {
        int eval, bestEval = 0;
        int winningPlayer = 0, waysSplit = 0;
        double partialPot;

        for (int i = 0; i < nPlayers; ++i) {
            handValue[i] = eval = HandEval.OmahaHighEval(boardCardsBits, handCardsBits[i]);
            if (eval > bestEval) {
                bestEval = eval;
                waysSplit = 0;
                winningPlayer = i;
            } else if (eval == bestEval)
                ++waysSplit;
        }
        if (waysSplit == 0)
            ++wins[winningPlayer];
        else {
            partialPot = 1.0 / ++waysSplit;
            for (int i = 0; waysSplit > 0; ++i)
                if (handValue[i] == bestEval) {
                    partialPots[i] += partialPot;
                    ++splits[i];
                    --waysSplit;
                }
        }
    }

    private void enumBoardsNoUnknown() {
        /*
         * This is the same as EnumBoards except each case calls
         * potResults directly.  This one is called when there are
         * no players with unspecified hole cards (nUnknown == 0).
         */
        switch (nBoardCards) {
        case 0:
            if (nPlayers == 2) {
                enum2GuysNoFlop(); /* special case */
                break;
            }
            for (int deckIx1 = startIx; deckIx1 <= limitIx1; deckIx1 += increment) {
                boardCardsBits[0] = deck[deckIx1];
                for (int deckIx2 = deckIx1 + 1; deckIx2 <= limitIx2; ++deckIx2) {
                    boardCardsBits[1] = deck[deckIx2];
                    for (int deckIx3 = deckIx2 + 1; deckIx3 <= limitIx3; ++deckIx3) {
                        boardCardsBits[2] = deck[deckIx3];
                        for (int deckIx4 = deckIx3 + 1; deckIx4 <= limitIx4; ++deckIx4) {
                            boardCardsBits[3] = deck[deckIx4];
                            for (int deckIx5 = deckIx4 + 1; deckIx5 <= limitIx5; ++deckIx5) {
                                boardCardsBits[4] = deck[deckIx5];
                                potResults();
                            }
                        }
                    }
                }
            }
            break;
        case 1:
            for (int deckIx2 = startIx; deckIx2 <= limitIx2; deckIx2 += increment) {
                boardCardsBits[1] = deck[deckIx2];
                for (int deckIx3 = deckIx2 + 1; deckIx3 <= limitIx3; ++deckIx3) {
                    boardCardsBits[2] = deck[deckIx3];
                    for (int deckIx4 = deckIx3 + 1; deckIx4 <= limitIx4; ++deckIx4) {
                        boardCardsBits[3] = deck[deckIx4];
                        for (int deckIx5 = deckIx4 + 1; deckIx5 <= limitIx5; ++deckIx5) {
                            boardCardsBits[4] = deck[deckIx5];
                            potResults();
                        }
                    }
                }
            }
            break;
        case 2:
            for (int deckIx3 = startIx; deckIx3 <= limitIx3; deckIx3 += increment) {
                boardCardsBits[2] = deck[deckIx3];
                for (int deckIx4 = deckIx3 + 1; deckIx4 <= limitIx4; ++deckIx4) {
                    boardCardsBits[3] = deck[deckIx4];
                    for (int deckIx5 = deckIx4 + 1; deckIx5 <= limitIx5; ++deckIx5) {
                        boardCardsBits[4] = deck[deckIx5];
                        potResults();
                    }
                }
            }
            break;
        case 3:
            for (int deckIx4 = startIx; deckIx4 <= limitIx4; deckIx4 += increment) {
                boardCardsBits[3] = deck[deckIx4];
                for (int deckIx5 = deckIx4 + 1; deckIx5 <= limitIx5; ++deckIx5) {
                    boardCardsBits[4] = deck[deckIx5];
                    potResults();
                }
            }
            break;
        case 4:
            // enum 1 board card:
            for (int deckIx5 = startIx; deckIx5 <= limitIx5; deckIx5 += increment) {
                boardCardsBits[4] = deck[deckIx5];
                potResults();
            }
            break;
        case 5:
            if (startIx == 0)
                potResults();
            break;
        }
    }

    /**
     * Called when there is at least one unknown opponent card
     */
    private void enumBoards() {
        switch (nBoardCards) { 
        case 0: //all board cards are unknown, enumerating through all of them
            for (int deckIx1 = startIx; deckIx1 <= limitIx1; deckIx1 += increment) {
                dealt[deckIx1] = true;
                boardCardsBits[0] = deck[deckIx1];
                for (int deckIx2 = deckIx1 + 1; deckIx2 <= limitIx2; ++deckIx2) {
                    dealt[deckIx2] = true;
                    boardCardsBits[1] = deck[deckIx2];
                    for (int deckIx3 = deckIx2 + 1; deckIx3 <= limitIx3; ++deckIx3) {
                        dealt[deckIx3] = true;
                        boardCardsBits[2] = deck[deckIx3];
                        for (int deckIx4 = deckIx3 + 1; deckIx4 <= limitIx4; ++deckIx4) {
                            dealt[deckIx4] = true;
                            boardCardsBits[3] = deck[deckIx4];
                            for (int deckIx51 = deckIx4 + 1; deckIx51 <= limitIx5; ++deckIx51) {
                                dealt[deckIx51] = true;
                                boardCardsBits[4] = deck[deckIx51];
                                enumUnknowns();
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
        case 1:
            for (int deckIx2 = startIx; deckIx2 <= limitIx2; deckIx2 += increment) {
                dealt[deckIx2] = true;
                boardCardsBits[1] = deck[deckIx2];
                for (int deckIx3 = deckIx2 + 1; deckIx3 <= limitIx3; ++deckIx3) {
                    dealt[deckIx3] = true;
                    boardCardsBits[2] = deck[deckIx3];
                    for (int deckIx4 = deckIx3 + 1; deckIx4 <= limitIx4; ++deckIx4) {
                        dealt[deckIx4] = true;
                        boardCardsBits[3] = deck[deckIx4];
                        for (int deckIx51 = deckIx4 + 1; deckIx51 <= limitIx5; ++deckIx51) {
                            dealt[deckIx51] = true;
                            boardCardsBits[4] = deck[deckIx51];
                            enumUnknowns();
                            dealt[deckIx51] = false;
                        }
                        dealt[deckIx4] = false;
                    }
                    dealt[deckIx3] = false;
                }
                dealt[deckIx2] = false;
            }
            break;
        case 2:
            for (int deckIx3 = startIx; deckIx3 <= limitIx3; deckIx3 += increment) {
                dealt[deckIx3] = true;
                boardCardsBits[2] = deck[deckIx3];
                for (int deckIx4 = deckIx3 + 1; deckIx4 <= limitIx4; ++deckIx4) {
                    dealt[deckIx4] = true;
                    boardCardsBits[3] = deck[deckIx4];
                    for (int deckIx51 = deckIx4 + 1; deckIx51 <= limitIx5; ++deckIx51) {
                        dealt[deckIx51] = true;
                        boardCardsBits[4] = deck[deckIx51];
                        enumUnknowns();
                        dealt[deckIx51] = false;
                    }
                    dealt[deckIx4] = false;
                }
                dealt[deckIx3] = false;
            }
            break;
        case 3:
            for (int deckIx4 = startIx; deckIx4 <= limitIx4; deckIx4 += increment) {
                dealt[deckIx4] = true;
                boardCardsBits[3] = deck[deckIx4];
                for (int deckIx51 = deckIx4 + 1; deckIx51 <= limitIx5; ++deckIx51) {
                    dealt[deckIx51] = true;
                    boardCardsBits[4] = deck[deckIx51];
                    enumUnknowns();
                    dealt[deckIx51] = false;
                }
                dealt[deckIx4] = false;
            }
            break;
        case 4: //all but one board card is unknown
            for (int deckIx5 = startIx; deckIx5 <= limitIx5; deckIx5 += increment) {
                dealt[deckIx5] = true;
                boardCardsBits[4] = deck[deckIx5];
                enumUnknowns();
                dealt[deckIx5] = false;
            }
            
        case 5:
            if (startIx == 0)
                enumUnknowns();
        }
        

    }

    private void enumUnknowns() {
        if (nUnknown == 1){
            for (int deckIx1 = 0; deckIx1 <= limitIx2; ++deckIx1) {
                if (dealt[deckIx1])
                    continue;
                handCardsBits[1][0] = deck[deckIx1];
                for (int deckIx2 = deckIx1 + 1; deckIx2 <= limitIx3; ++deckIx2) {
                    if (dealt[deckIx2])
                        continue;
                    handCardsBits[1][1] = deck[deckIx2];
                    for (int deckIx3 = deckIx2 + 1; deckIx3 <= limitIx4; ++deckIx3) {
                        if (dealt[deckIx3])
                            continue;
                        handCardsBits[1][2] = deck[deckIx3];
                        for (int deckIx4 = deckIx3 + 1; deckIx4 <= limitIx5; ++deckIx4) {
                            if (dealt[deckIx4])
                                continue;
                            handCardsBits[1][3] = deck[deckIx4];
                            potResults();
                        }
                    }
                }
            }
        }
        else {
            throw new RuntimeException("More than one unknown, pls");
        }
    }
}
