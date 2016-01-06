package pb.calc;

import pb.calc.card;
import java.util.Arrays;

/**
 * This class will organize all 5 cards and find out 
 * what's the best combination: that means whether the cards make a flush/straight/pair etc.
 * 
 * @author Sourav
 *
 */
public class match {

//  Abstraction Function:
//      5 cards are in an array. Card order is increasing. The lowest card is at the beginning.
//      The highest card is at the end.
//  Representation Invariant:
//      The length of the array is 5
//
//  Safety From Rep exposure:
//      All fields are immutable and final.    

    private final card[] cards = new card[5];
    
    /**
     * Constructor: need to be passed an array of 5 cards (card object)
     * 
     * @param givenCards
     */
    public match(card[] givenCards){
        System.arraycopy(givenCards, 0, cards, 0, 5);
        Arrays.sort(cards);
        checkRep();
    }
    
    /**
     * Check Rep
     */
    private void checkRep(){
        assert this.cards.length == 5;
    }
    
    

}
