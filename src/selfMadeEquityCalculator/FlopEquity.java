package selfMadeEquityCalculator;

import java.util.HashMap;

/**
 * The value of each hand is 
 * @author mikewang1670
 *
 */
public class FlopEquity {
    String[] myHand;
    String[] enemyHand;
    String[] flop;
    
    public int canMakePair(String[] hand, String[] flop){
        HashMap<String, Integer> cards = new HashMap<String, Integer>();
        for (String card : flop){
            if (cards.containsKey(card)){
                cards.put(card, cards.get(card)+1);
            } else {
                cards.put(card, 1);
            }
        }
        
    }

}
