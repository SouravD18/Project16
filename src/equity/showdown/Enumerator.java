package equity.showdown;
import java.util.HashSet;
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
    //    private long[]		holeHand;
    private int[]		handValue;
    private final int	limitIx1, limitIx2, limitIx3, limitIx4, limitIx5;
    private long[]       boardCardsBits = new long[5];
    private long[][]     handCardsBits = new long[2][4];
    //  private long		board1, board2, board3, board4, board5;
    
    @Override public final void run() {
        if (nUnknown > 0)
            enumBoards();
        else
            enumBoardsNoUnknown();
    }

    Enumerator(final int instance, final int instances, final String[][] holeCards, final String[] board) {
        super("Enumerator" + instance);
        startIx = instance;
        increment = instances;

        for (int j = 0; j < holeCards.length; j++){
            for (int k = 0; k < holeCards[j].length; k++){
                handCardsBits[j][k] = HandEval.encode(new Card(holeCards[j][k]));
            }
        }
        for (int j = 0; j < board.length; j++){
            boardCardsBits[j] = HandEval.encode(new Card(board[j]));
        }

        int nCardsInDeck = 52 - (board.length + holeCards[0].length + holeCards[1].length);
        this.deck = new long[nCardsInDeck];
        dealt = new boolean[nCardsInDeck];
        int i = 0;

        Set<String> tempDeck = new HashSet<>(CardSet.deckSet);
        for (String[] array: holeCards)
            for (String s: array)
                tempDeck.remove(s);
        for (String s: board)
            tempDeck.remove(s);

        for (String s: tempDeck){
            this.deck[i++] = HandEval.encode(new Card(s));
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
