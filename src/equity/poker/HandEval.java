package equity.poker;


/**
 * Non-instantiable class containing a variety of static poker hand evaluation and related utility methods.
 * <p>
 * All of the methods are thread-safe.
 * <p>
 * Each evaluation method takes a single parameter representing a hand of five to
 * seven cards represented within a long (64 bits).  The long is considered as
 * composed of four 16-bit fields, one for each suit.  The ordering of these
 * 16-bit fields within the long, i.e., the correspondence of each to a specific
 * suit, is immaterial.  Within each suit's 16-bit field, the least-significant
 * 13 bits (masked by 0x1FFF) are flags representing the presence of ranks in
 * that suit, where bit 0 set (0x0001) for a deuce, ..., bit 12 set (0x1000) for
 * an ace.  The values of the unused most-significant three bits within each
 * 16-bit suit field are immaterial.
 * <p>
 * A hand parameter can be built by encoding a {@link CardSet} or by bitwise
 * OR-ing, or adding, the encoded values of individual {@link Card}s.  These
 * encodings are returned by an {@link #encode encode} method.
 * <p>
 * Different methods are used for high and for lowball evaluation.
 * <p>
 * For high evaluation if results R1 > R2, hand 1 beats hand 2;
 * for lowball evaluation if results R1 > R2, hand 2 beats hand 1.
 * <p>
 * Evaluation result in 32 bits = 0x0V0RRRRR where V, R are
 * hex digits or "nybbles" (half-bytes).
 * <p>
 * V nybble = category code ranging from {@link HandCategory#NO_PAIR}<code>.ordinal()</code>
 *                                    to {@link HandCategory#STRAIGHT_FLUSH}<code>.ordinal()</code>
 * <p>
 * The R nybbles are the significant ranks (0..12), where 0 is the deuce
 * in a high result (Ace is 12, 0xC), and for lowball 0 is the Ace
 * (King is 0xC).  The Rs may be considered to consist of Ps for ranks
 * which determine the primary value of the hand, and Ks for kickers
 * where applicable.  Ordering is left-to-right:  first the Ps, then
 * any Ks, then padding with 0s.  Because 0 is a valid rank, to
 * interpret a result you must know how many ranks are significant,
 * which is a function of the hand category and whether high or lowball.
 * Examples: for a one-pair hand there are four significant ranks,
 * that of the pair and of the three kickers; for a straight, there is
 * one significant rank, that of the highest in the hand.
 * <p>
 * Common-card (board) games are assumed in determining the number of
 * significant ranks.  For example, a kicker value is returned for quads even
 * though it wouldn't be significant in a draw game.
 * <p><pre>
 * Examples of ...Eval method results (high where not indicated):
 *  Royal flush: 0x080C0000
 *  Four of a kind, Queens, with a 5 kicker:  0x070A3000
 *  Threes full of eights:  0x06016000
 *  Straight to the five (wheel): 0x04030000 (high)
 *  Straight to the five (wheel): 0x04040000 (lowball)
 *  One pair, deuces (0x0), with A65: 0x0100C430 (high)
 *  One pair, deuces (0x1), with 65A: 0x01015400 (lowball)
 *  No pair, KJT85: 0x000B9863
 *  Razz, wheel:  0x00043210</pre>
 * For the eight-or-better lowball ..._Eval functions, the result is
 * either as above or the constant {@link #NO_8_LOW}.  NO_8_LOW > any other
 * ..._Eval function result.
 * <p>
 * @version 2010Jun25.1
 * @author Steve Brecher
 *
 */
public final class HandEval {

    private HandEval() {}	// no instances
    
    /**
     * Returns a value which can be used in building a parameter to one of the HandEval evaluation methods.
     * @param card a {@link Card}
     * @return a value which may be bitwise OR'ed or added to other such
     * values to build a parameter to one of the HandEval evaluation methods.
     */
    public static long encode(final Card card) {
        return 0x1L << (card.suitOf().ordinal()*16 + card.rankOf().ordinal());
    }

    /**
     * Returns a value which can be used as a parameter to one of the HandEval evaluation methods.
     * @param cs a {@link CardSet}
     * @return a value which can be used as a parameter to one of the HandEval evaluation methods.
     * The value may also be bitwise OR'ed or added to other such
     * values to build an evaluation method parameter.
     */
    public static long encode(final CardSet cs) {
        long result = 0;
        for (Card c : cs)
            result |= encode(c);
        return result;
    }

    public static enum HandCategory { NO_PAIR, PAIR, TWO_PAIR, THREE_OF_A_KIND, STRAIGHT,
        FLUSH, FULL_HOUSE, FOUR_OF_A_KIND, STRAIGHT_FLUSH; }

    private static final int   RANK_SHIFT_1		= 4;
    private static final int   RANK_SHIFT_2		= RANK_SHIFT_1 + 4;
    private static final int   RANK_SHIFT_3		= RANK_SHIFT_2 + 4;
    private static final int   RANK_SHIFT_4		= RANK_SHIFT_3 + 4;
    private static final int   VALUE_SHIFT		= RANK_SHIFT_4 + 8;

    private static final int   NO_PAIR			= 0;
    private static final int   PAIR				= NO_PAIR			+ (1 << VALUE_SHIFT);
    private static final int   TWO_PAIR			= PAIR				+ (1 << VALUE_SHIFT);
    private static final int   THREE_OF_A_KIND	= TWO_PAIR			+ (1 << VALUE_SHIFT);
    private static final int   STRAIGHT			= THREE_OF_A_KIND	+ (1 << VALUE_SHIFT);
    private static final int   FLUSH			= STRAIGHT			+ (1 << VALUE_SHIFT);
    private static final int   FULL_HOUSE		= FLUSH				+ (1 << VALUE_SHIFT);
    private static final int   FOUR_OF_A_KIND	= FULL_HOUSE		+ (1 << VALUE_SHIFT);
    private static final int   STRAIGHT_FLUSH	= FOUR_OF_A_KIND	+ (1 << VALUE_SHIFT);

    /**
     *  Greater than any return value of the HandEval evaluation methods.
     */
    public static final int NO_8_LOW = STRAIGHT_FLUSH + (1 << VALUE_SHIFT);

    private static final int   ARRAY_SIZE		= 0x1FC0 + 1;			// all combos of up to 7 of LS 13 bits on
    /* Arrays for which index is bit mask of card ranks in hand: */
    private static final int[] straightValue	= new int[ARRAY_SIZE];	// Value(STRAIGHT) | (straight's high card rank-2 (3..12) << RANK_SHIFT_4); 0 if no straight
    private static final int[] nbrOfRanks		= new int[ARRAY_SIZE];	// count of bits set
    private static final int[] hiRank			= new int[ARRAY_SIZE];	// 4-bit card rank of highest bit set, right justified
    private static final int[] hiUpTo5Ranks		= new int[ARRAY_SIZE];	// 4-bit card ranks of highest (up to) 5 bits set, right-justified
    private static final int[] loMaskOrNo8Low	= new int[ARRAY_SIZE];	// low-order 5 of the low-order 8 bits set, or NO_8_LOW; Ace is LS bit.
    private static final int[] lo3_8OBRanksMask	= new int[ARRAY_SIZE];	// bits other than lowest 3 8-or-better reset; Ace is LS bit.

    
    /**
     * Returns the value of a 5-card poker hand.
     * @param hand bit mask with one bit set for each of 5 cards.
     * @return the value of the hand.
     */
    public static int hand5Eval(long hand) {

        final int c = (int)hand & 0x1FFF;
        final int d = ((int)hand >>> 16) & 0x1FFF;
        final int h = (int)(hand >>> 32) & 0x1FFF;
        final int s = (int)(hand >>> 48) & 0x1FFF;

        final int ranks = c | d | h | s;
        int i;

        switch (nbrOfRanks[ranks]) {

        case 2: /* quads or full house */
            i = c & d;				/* any two suits */
            if ((i & h & s) == 0) { /* no bit common to all suits */
                i = c ^ d ^ h ^ s;  /* trips bit */
                return FULL_HOUSE | (hiRank[i] << RANK_SHIFT_4) | (hiRank[i ^ ranks] << RANK_SHIFT_3); }
            else
                /* the quads bit must be present in each suit mask,
	                       but the kicker bit in no more than one; so we need
	                       only AND any two suit masks to get the quad bit: */
                return FOUR_OF_A_KIND | (hiRank[i] << RANK_SHIFT_4) | (hiRank[i ^ ranks] << RANK_SHIFT_3);

        case 3: /* trips and two kickers,
	                   or two pair and kicker */
            if ((i = c ^ d ^ h ^ s) == ranks) {
                /* trips and two kickers */
                if ((i = c & d) == 0)
                    if ((i = c & h) == 0)
                        i = d & h;
                return THREE_OF_A_KIND | (hiRank[i] << RANK_SHIFT_4) 
                        | (hiUpTo5Ranks[i ^ ranks] << RANK_SHIFT_2); }
            /* two pair and kicker; i has kicker bit */
            return TWO_PAIR | (hiUpTo5Ranks[i ^ ranks] << RANK_SHIFT_3) | (hiRank[i] << RANK_SHIFT_2);

        case 4: /* pair and three kickers */
            i = c ^ d ^ h ^ s; /* kicker bits */
            return PAIR | (hiRank[ranks ^ i] << RANK_SHIFT_4) | (hiUpTo5Ranks[i] << RANK_SHIFT_1);

        case 5: /* flush and/or straight, or no pair */
            if ((i = straightValue[ranks]) == 0)
                i = hiUpTo5Ranks[ranks];
            if (c != 0) {			/* if any clubs... */
                if (c != ranks)		/*   if no club flush... */
                    return i; }		/*      return straight or no pair value */
            else
                if (d != 0) {
                    if (d != ranks)
                        return i; }
                else
                    if (h != 0) {
                        if (h != ranks)
                            return i; }
            /*	else s == ranks: spade flush */
            /* There is a flush */
            if (i < STRAIGHT)
                /* no straight */
                return FLUSH | i;
            else
                return (STRAIGHT_FLUSH - STRAIGHT) + i;
        }

        return 0; /* never reached, but avoids compiler warning */
    }

    public static int OmahaHighEval(long[] boardCardPositions, long[] handCardPositions) {
        long hand1, hand2, hand3, hand4;
        int max = 0, current = 0;
        for (int a = 0; a < 3; a++){
            hand1 = boardCardPositions[a];
            for (int b = a + 1; b < 4; b++){
                hand2 = hand1 | boardCardPositions[b];
                for (int c = b + 1; c < 5; c++/*haha*/){
                    hand3 = hand2 | boardCardPositions[c];
                    for (int d = 0; d < 3; d++){
                        hand4 = hand3 | handCardPositions[d];
                        for (int e = d + 1; e < 4; e++){
                            current = hand5Eval(hand4 | handCardPositions[e]);
                            if (max < current){
                                max = current;
                            }     
                        }
                    }
                }
            }
        }
        return max;
    }


    /** ********** Initialization ********************** */

    private static final int ACE_RANK	= 14;

    private static final int A5432		= 0x0000100F; // A5432

    // initializer block
    static {
        int mask, bitCount, ranks;
        int shiftReg, i;
        int value;

        for (mask = 1; mask < ARRAY_SIZE; ++mask) {
            bitCount = ranks = 0;
            shiftReg = mask;
            for (i = ACE_RANK - 2; i >= 0; --i, shiftReg <<= 1)
                if ((shiftReg & 0x1000) != 0)
                    if (++bitCount <= 5) {
                        ranks <<= RANK_SHIFT_1;
                        ranks += i;
                        if (bitCount == 1)
                            hiRank[mask] = i;
                    }
            hiUpTo5Ranks[mask] = ranks;
            nbrOfRanks[mask] = bitCount;

            loMaskOrNo8Low[mask] = NO_8_LOW;
            bitCount = value = 0;
            shiftReg = mask;
            /* For the purpose of this loop, Ace is low; it's in the LS bit */
            for (i = 0; i < 8; ++i, shiftReg >>= 1)
                if ((shiftReg & 1) != 0) {
                    value |= (1 << i); /* undo previous shifts, copy bit */
                    if (++bitCount == 3)
                        lo3_8OBRanksMask[mask] = value;
                    if (bitCount == 5) {
                        loMaskOrNo8Low[mask] = value;
                        break; }
                }
        }
        for (mask = 0x1F00/* A..T */; mask >= 0x001F/* 6..2 */; mask >>= 1)
            setStraight(mask);
        setStraight(A5432); /* A,5..2 */
    }

    private static void setStraight(int ts) {
        /* must call with ts from A..T to 5..A in that order */
        int es, i, j;

        for (i = 0x1000; i > 0; i >>= 1)
            for (j = 0x1000; j > 0; j >>= 1) {
                es = ts | i | j; /* 5 straight bits plus up to two other bits */
                if (straightValue[es] == 0)
                    if (ts == A5432)
                        straightValue[es] = STRAIGHT | ((5-2) << RANK_SHIFT_4);
                    else
                        straightValue[es] = STRAIGHT | (hiRank[ts] << RANK_SHIFT_4);
            }
    }
}
