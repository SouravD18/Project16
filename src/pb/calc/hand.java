package pb.calc;

import java.io.IOException;

import pb.calc.card;

/**
 * hand represents the two cards in our hand
 * 
 * @author Sourav
 *
 */
public class hand {
      
//  Abstraction Function:
//      Two cards are in an array. Card order is decreasing. So High card is first 
//      Low card is second. 
//  Representation Invariant:
//      The length of the array is 2
//  
//  Safety From Rep exposure:
//      All fields are immutable and final.    
    
    private final card[] cards = new card[2];
    
    /**
     * Constructor: need to be passed an array of two cards (card object)
     * 
     * @param givenCards
     */
    public hand(card[] givenCards){
        
        if(givenCards[0].isBigger(givenCards[1])){
            this.cards[0] = new card(givenCards[0]);
            this.cards[1] = new card(givenCards[1]);
        }
        else{
            this.cards[1] = new card(givenCards[0]);
            this.cards[0] = new card(givenCards[1]);
        }
        checkRep();
    }
    
    /**
     * Check the rep
     */
    private void checkRep(){
        assert this.cards.length == 2;
    }
    
    /**
     * Returns the higher card between two
     * @return the higher card
     */
    public card returnHighCard(){
        return new card(cards[0]);
    }
    
    /**
     * Returns the lower card between two
     * @return the lower card
     */
    public card returnLowCard(){
        return new card(cards[1]);
    }
    
    /**
     * Returns the starting odds of the two cards(Preflop odds)
     * @return Preflop odds of the two cards
     * @throws IOException 
     */
    public double odds() throws IOException{
        throw new IOException("Implement Me!"); 
    }
}

