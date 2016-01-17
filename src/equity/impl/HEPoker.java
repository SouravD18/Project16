package equity.impl;

import java.util.Arrays;

import equity.*;

/**
 * Hold'em and Omaha hand analysis, using a combinatorial number system.
 */
public class HEPoker extends Poker {

    /** check board is either null or no more than 5 cards */
    private static void validateBoard(String[] board) {
        if (board != null && board.length > 5) {
            throw new RuntimeException("invalid board: " + Arrays.toString(board));
        }
    }
    
    //
    // instance stuff
    //
    
    /** must use 2 cards */
    private final boolean omaha;
    private final int min;
    private final boolean hilo;
    private final Value loValue;

    /**
     * create holdem equity calculator for given game type
     */
    public HEPoker(boolean omaha, boolean hilo) {
        this(omaha, hilo, Value.hiValue, Value.afLow8Value);
    }
    
    /**
     * create holdem equity calculator for given game type
     */
    public HEPoker(boolean omaha, boolean hilo, Value hi, Value lo) {
        super(hi);
        this.omaha = omaha;
        this.loValue = lo;
        this.min = 2;
        this.hilo = hilo;
    }
    
    /** check hole has at least 1 or 2 cards and at most 2 or 4 cards */
    private void validateHoleCards(String[] hole) {
        final int num = 4;
        if (hole.length != num) {
            throw new RuntimeException("invalid hole cards: " + Arrays.toString(hole));
        }
    }
    
    @Override
    public MEquity[] equity(String[] board, String[][] holeCards, String[] blockers) {
        
        //System.out.println("holdem/omaha equity: " + Arrays.deepToString(holeCards) + " board: " + Arrays.toString(board) + " blockers: " + Arrays.toString(blockers));
        
        validateBoard(board);
        for (String[] hole : holeCards) {
            validateHoleCards(hole);
        }
        
        // cards not used by hands or board
        final String[] deck = Poker.remdeck(holeCards, board, blockers);
        
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
        validateBoard(board);
        validateHoleCards(hole);
        
        if (board == null || board.length != 5) {
            // could use the draw poker getPair method...
            return 0;
            
        } else {
            return heValue(value, board, hole, new String[5]);
        }
    }
    
    /**
     * Calc exact tex/omaha hand equity for each hand for given board
     */
    private MEquity[] equityImpl(final HEBoard heboard, final String[][] holeCards) {
        
        
        // note: HL MEquity actually contains 3 equity types, so can be treated as high only
        final MEquity[] meqs = MEquityUtil.createMEquitiesHL(hilo, holeCards.length, heboard.deck.length, heboard.exact());
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
        long endTime = System.nanoTime();
        System.out.println((endTime-startTime)/1000000.0);
        
        // get equity
        final int count = heboard.count();
        final int pick = heboard.pick();
        int hiloCount = 0;
        
        for (int p = 0; p < count; p++) {
            // get board
            heboard.next();
            //System.out.println("board p: " + p + " current: " + Arrays.toString(heboard.current) + " next: " + Arrays.toString(heboard.board));
            
            // hi equity
            for (int i = 0; i < holeCards.length; i++) {
                hivals[i] = heValue(value, heboard.board, holeCards[i], temp);
            }


            
                // high winner
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
        int hv = 0;
        final int nh = MathsUtil.binomialCoefficientFast(hole.length, min);
        final int nb = MathsUtil.binomialCoefficientFast(board.length, 5 - min);
        for (int kh = 0; kh < nh; kh++) {
            MathsUtil.kCombination(min, kh, hole, temp, 0);
            for (int kb = 0; kb < nb; kb++) {
                MathsUtil.kCombination(5 - min, kb, board, temp, min);
                final int val = v.value(temp);
                //System.out.println(Arrays.asList(h5) + " - " + Poker.desc(v));
                if (val > hv) {
                    hv = val;
                }
            }
        }
        
        return hv;
    }
    
    @Override
    public int minHoleCards () {
        return min;
    }
}
