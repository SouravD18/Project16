package equity.impl;

import java.util.HashSet;
import java.util.Set;

import equity.*;
import stevebrecher.Card;
import stevebrecher.HandEval;

/**
 * Hold'em and Omaha hand analysis, using a combinatorial number system.
 */
public class HEPoker extends Poker {
    /**
     * create holdem equity calculator for given game type
     */
    public HEPoker() {
        this(Value.hiValue, Value.afLow8Value);
    }
    
    /**
     * create holdem equity calculator for given game type
     */
    public HEPoker(Value hi, Value lo) {
        super(hi);
    }
    
    @Override
    public MEquity[] equity(String[] board, String[][] holeCards) {
        // cards not used by hands or board
        Set<String> deckSet = new HashSet<>(Poker.deck);
        for (String[] array: holeCards){
            for (String s: array){
                deckSet.remove(s);
            }
        }
        for (String s: board)
            deckSet.remove(s);
        final String[] deck = deckSet.toArray(new String[deckSet.size()]);
        
        if (board.length <= 1) {
            // monte carlo (random sample boards)
            return equityImpl(new HEBoardSample(deck, board, 10000), holeCards);
            
        } else {
            // all possible boards
            return equityImpl(new HEBoardEnum(deck, board), holeCards);
        }
    }

    @Override
    public int value(String[] board, String[] hole) {
        return heValue(value, board, hole, new String[5]);
    }
    
    /**
     * Calc exact tex/omaha hand equity for each hand for given board
     */
    private MEquity[] equityImpl(final HEBoard heboard, final String[][] holeCards) {
        // note: HL MEquity actually contains 3 equity types, so can be treated as high only
        final MEquity[] meqs = MEquityUtil.createMEquitiesHL(holeCards.length, heboard.deck.length, heboard.exact());
        final int[] hivals = new int[holeCards.length];
        final String[] temp = new String[5];
        
        long startTime = System.nanoTime();
        // get current high hand values (not equity)
        if (heboard.current != null) {
            for (int n = 0; n < holeCards.length; n++) {
                if (heboard.current.length >= 3) {
                    hivals[n] = heValue(value, heboard.current, holeCards[n], temp);
                }
            }
            MEquityUtil.updateCurrent(meqs, Equity.Type.HI_ONLY, hivals);
        }
        
        // get equity
        final int count = heboard.count();
        final int pick = heboard.pick();
        int hiloCount = 0;
        long endTime = System.nanoTime();
        System.out.println("number of boards = " + count + " and runtime so far is " + (endTime-startTime)/1000000.0);
        for (int p = 0; p < count; p++) {
            
            heboard.next(); //first get board
            //System.out.println("board p: " + p + " current: " + Arrays.toString(heboard.current) + " next: " + Arrays.toString(heboard.board));
            //hi equity
            for (int i = 0; i < holeCards.length; i++) {
                hivals[i] = heValue(value, heboard.board, holeCards[i], temp);
            }
            //high winner
            MEquityUtil.updateMEquities(meqs, Equity.Type.HI_ONLY, hivals, null);

        }
        
        long endTime2 = System.nanoTime();
        System.out.println((endTime2-startTime)/1000000.0);

        MEquityUtil.summariseMEquities(meqs, count, hiloCount);
        // XXX shouldn't be here, just need to store pick and count on mequity
        MEquityUtil.summariseOuts(meqs, pick, count);
//      for(MEquity e: meqs){
//          System.out.println(e.getEquity(Equity.Type.HI_ONLY).total);
//      }
        return meqs;
    }
    

    /**
     * Calculate value of holdem/omaha hand (using at least min cards from hand). 
     * Board can be 3-5 cards.
     */
    private int heValue(final Value v, final String[] board, final String[] hole, final String[] temp) {
        if (board.length == 5 && hole.length == 4){
            Card[] boardCards = new Card[5];
            Card[] holeCards = new Card[4];
            for (int i = 0; i < 5; i++)
                boardCards[i] = new Card(board[i]);
            for (int i = 0; i < 4; i++)
                holeCards[i] = new Card(hole[i]);
            
            int hv = 0;
            Card[][] handCombos = { //essentially all the hand combinations are pre-memorized here
                    //makes for slightly faster (~3ms) code
                    {boardCards[0], boardCards[1], boardCards[2], holeCards[0], holeCards[1]},
                    {boardCards[0], boardCards[1], boardCards[2], holeCards[0], holeCards[2]},
                    {boardCards[0], boardCards[1], boardCards[2], holeCards[0], holeCards[3]},
                    {boardCards[0], boardCards[1], boardCards[2], holeCards[1], holeCards[2]},
                    {boardCards[0], boardCards[1], boardCards[2], holeCards[1], holeCards[3]},
                    {boardCards[0], boardCards[1], boardCards[2], holeCards[2], holeCards[3]},
                    
                    {boardCards[0], boardCards[1], boardCards[3], holeCards[0], holeCards[1]},
                    {boardCards[0], boardCards[1], boardCards[3], holeCards[0], holeCards[2]},
                    {boardCards[0], boardCards[1], boardCards[3], holeCards[0], holeCards[3]},
                    {boardCards[0], boardCards[1], boardCards[3], holeCards[1], holeCards[2]},
                    {boardCards[0], boardCards[1], boardCards[3], holeCards[1], holeCards[3]},
                    {boardCards[0], boardCards[1], boardCards[3], holeCards[2], holeCards[3]},
                    
                    {boardCards[0], boardCards[1], boardCards[4], holeCards[0], holeCards[1]},
                    {boardCards[0], boardCards[1], boardCards[4], holeCards[0], holeCards[2]},
                    {boardCards[0], boardCards[1], boardCards[4], holeCards[0], holeCards[3]},
                    {boardCards[0], boardCards[1], boardCards[4], holeCards[1], holeCards[2]},
                    {boardCards[0], boardCards[1], boardCards[4], holeCards[1], holeCards[3]},
                    {boardCards[0], boardCards[1], boardCards[4], holeCards[2], holeCards[3]},
                    
                    {boardCards[0], boardCards[2], boardCards[3], holeCards[0], holeCards[1]},
                    {boardCards[0], boardCards[2], boardCards[3], holeCards[0], holeCards[2]},
                    {boardCards[0], boardCards[2], boardCards[3], holeCards[0], holeCards[3]},
                    {boardCards[0], boardCards[2], boardCards[3], holeCards[1], holeCards[2]},
                    {boardCards[0], boardCards[2], boardCards[3], holeCards[1], holeCards[3]},
                    {boardCards[0], boardCards[2], boardCards[3], holeCards[2], holeCards[3]},
                    
                    {boardCards[0], boardCards[2], boardCards[4], holeCards[0], holeCards[1]},
                    {boardCards[0], boardCards[2], boardCards[4], holeCards[0], holeCards[2]},
                    {boardCards[0], boardCards[2], boardCards[4], holeCards[0], holeCards[3]},
                    {boardCards[0], boardCards[2], boardCards[4], holeCards[1], holeCards[2]},
                    {boardCards[0], boardCards[2], boardCards[4], holeCards[1], holeCards[3]},
                    {boardCards[0], boardCards[2], boardCards[4], holeCards[2], holeCards[3]},
                    
                    {boardCards[0], boardCards[3], boardCards[4], holeCards[0], holeCards[1]},
                    {boardCards[0], boardCards[3], boardCards[4], holeCards[0], holeCards[2]},
                    {boardCards[0], boardCards[3], boardCards[4], holeCards[0], holeCards[3]},
                    {boardCards[0], boardCards[3], boardCards[4], holeCards[1], holeCards[2]},
                    {boardCards[0], boardCards[3], boardCards[4], holeCards[1], holeCards[3]},
                    {boardCards[0], boardCards[3], boardCards[4], holeCards[2], holeCards[3]},
                    
                    {boardCards[1], boardCards[2], boardCards[3], holeCards[0], holeCards[1]},
                    {boardCards[1], boardCards[2], boardCards[3], holeCards[0], holeCards[2]},
                    {boardCards[1], boardCards[2], boardCards[3], holeCards[0], holeCards[3]},
                    {boardCards[1], boardCards[2], boardCards[3], holeCards[1], holeCards[2]},
                    {boardCards[1], boardCards[2], boardCards[3], holeCards[1], holeCards[3]},
                    {boardCards[1], boardCards[2], boardCards[3], holeCards[2], holeCards[3]},
                    
                    {boardCards[1], boardCards[2], boardCards[4], holeCards[0], holeCards[1]},
                    {boardCards[1], boardCards[2], boardCards[4], holeCards[0], holeCards[2]},
                    {boardCards[1], boardCards[2], boardCards[4], holeCards[0], holeCards[3]},
                    {boardCards[1], boardCards[2], boardCards[4], holeCards[1], holeCards[2]},
                    {boardCards[1], boardCards[2], boardCards[4], holeCards[1], holeCards[3]},
                    {boardCards[1], boardCards[2], boardCards[4], holeCards[2], holeCards[3]},
                    
                    {boardCards[1], boardCards[3], boardCards[4], holeCards[0], holeCards[1]},
                    {boardCards[1], boardCards[3], boardCards[4], holeCards[0], holeCards[2]},
                    {boardCards[1], boardCards[3], boardCards[4], holeCards[0], holeCards[3]},
                    {boardCards[1], boardCards[3], boardCards[4], holeCards[1], holeCards[2]},
                    {boardCards[1], boardCards[3], boardCards[4], holeCards[1], holeCards[3]},
                    {boardCards[1], boardCards[3], boardCards[4], holeCards[2], holeCards[3]},
                    
                    {boardCards[2], boardCards[3], boardCards[4], holeCards[0], holeCards[1]},
                    {boardCards[2], boardCards[3], boardCards[4], holeCards[0], holeCards[2]},
                    {boardCards[2], boardCards[3], boardCards[4], holeCards[0], holeCards[3]},
                    {boardCards[2], boardCards[3], boardCards[4], holeCards[1], holeCards[2]},
                    {boardCards[2], boardCards[3], boardCards[4], holeCards[1], holeCards[3]},
                    {boardCards[2], boardCards[3], boardCards[4], holeCards[2], holeCards[3]},
            };
            
            //long a = System.nanoTime();
            for (Card[] c: handCombos){
                final int val = HandEval.hand5Eval(HandEval.encode(c));
                if (val > hv) {
                    hv = val;
                }
            }
            //long b = System.nanoTime();
            //System.out.println(b-a);
            return hv;
            
        } else {
            int hv = 0;
            final int nh = MathsUtil.binomialCoefficientFast(hole.length, 2);
            final int nb = MathsUtil.binomialCoefficientFast(board.length, 5 - 2);
            for (int kh = 0; kh < nh; kh++) {
                MathsUtil.kCombination(2, kh, hole, temp, 0);
                for (int kb = 0; kb < nb; kb++) {
                    MathsUtil.kCombination(5 - 2, kb, board, temp, 2);
                    final int val = v.value(temp);
                    //System.out.println(Arrays.asList(h5) + " - " + Poker.desc(v));
                    if (val > hv) {
                        hv = val;
                    }
                }
            }
            return hv;
        }
        

    }
    
    @Override
    public int minHoleCards(){
        return 2;
    }
}
