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
    private final int min;

    /**
     * create holdem equity calculator for given game type
     */
    public HEPoker() {
        this(Value.hiValue);
    }
    
    /**
     * create holdem equity calculator for given game type
     */
    public HEPoker(Value hi) {
        super(hi);
        this.min = 2;
    }
    
    /** check hole has 4 cards  */
    private void validateHoleCards(String[] hole) {
        final int num = 4;
        if (hole.length != num) {
            throw new RuntimeException("invalid hole cards: " + Arrays.toString(hole));
        }
    }
    
    /**
     *Returns the predicted value of the hand after flop
     */
    public float equity(String[] board, String[] myHand, String[] blockers) {       

        validateHoleCards(myHand);
        String[][] holes = {myHand};
        // cards not used by hands or board
        final String[] deck = Poker.remdeck(holes, board, blockers);
        return equityImpl(new HEBoardEnum(deck, board), myHand);
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
     * Calc predicted value of the hand
     */
    private float equityImpl(final HEBoard heboard, final String[] myHand) {

        final String[] temp = new String[5];
        
        
        // get equity
        final int count = heboard.count();
        long totalValue = 0;
        
        for (int p = 0; p < count; p++) {
            // get board
            heboard.next();
            totalValue += heValue(value, heboard.board, myHand, temp);
         
        }
        
        return totalValue/(new Float(count));
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
