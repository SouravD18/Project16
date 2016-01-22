package player;

/**
 * This class is takes account of history, current hand stats and equity of hand.
 * Then it gives "advice" to our brain
 * 
 * @author Sourav
 *
 */
public class Guru {
    /**
     * Preflop tactics (From lecture):
     *  ->  If high 3-bet% then tighten raising ranges
     *  ->  Fold-to-4-bet% high compare to 3-bet: 4-bet-bluff a lot
     *  ->  PFR high? Call and 3-bet often
     *  
     */
    
    /**
     * Flop tactics (From lecture):
     *  ->  If continuation bet% high, then increase check-raising
     *  ->  Fold to continuation bet% high, then Continuation bet a lot
     *  ->  Check-Rise% high, then Cbet with good hands, never slow play.
     */
    
    /**
     * PostFlop tactics (From Lecture):
     *  -> High aggression? Call more and bet high with good cards
     *  -> Win Show Down High? Don't value bet too thin + fold to bets + bluff a lot
     *  -> Went to Show Down high? Don't expect them to fold. Bet high when good hands
     */
}
