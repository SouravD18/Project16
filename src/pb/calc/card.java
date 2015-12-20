package pb.calc;

/** 
 * 
 *  An immutable object representing one playing card. 
 *        
 */
public class card {
    
    private final String suit;
    private final int order;
    
//    Abstraction Function:
//        Card represents a playing card:
//            suit is a String representing the suit.
//            "s"=Spades; "h"=Hearts; "c"=Clubs; "d"=Diamonds
//            
//            order is a Integer representing the number on the card.
//            For numbers after 10, we will assign:
//            11 = Jack; 12 = Queen; 13 = King; 14 = Ace 
//    Representation Invariant:
//        suit belongs to set {"s", "h", "c", "d"}
//        1 < order < 15
//    Safety From Rep exposure:
//        All fields are immutable and final. 
    
    
    /**
     *  Constructor. 
     * @param givenSuit
     * @param givenOrder
     *      
     */
    public card(String givenSuit, int givenOrder){
        this.suit  = givenSuit.toLowerCase();
        this.order = givenOrder;
        
        checkRep();
    }
    
    /**
     * check the rep
     */
    private void checkRep(){
        assert this.suit.equals("s") 
            || this.suit.equals("h")
            || this.suit.equals("c")
            || this.suit.equals("d");
        
        assert this.order > 1 && this.order < 15;
    }
    
    /**
     * 
     * @return the number on the card
     */
    public int returnNumber(){
        return this.order;
    }
    
    /**
     * 
     * @return the suit string of the card
     */
    public String returnSuit(){
        return this.suit;
    }
    
    /**
     *  Decides Bigger number between two cards based on numbers
     *  
     * @param thatCard
     * @return True if the card number is bigger than thatCard's number
     */
    public boolean isBigger(card thatCard){
        return this.returnNumber() > thatCard.returnNumber();
    }
    
    /**
     *  Decides Smaller number between two cards based on numbers
     *  
     * @param thatCard
     * @return True is if the card number is smaller than thatCard's number
     */
    public boolean isSmaller(card thatCard){
        return this.returnNumber() < thatCard.returnNumber();
    }
    
    /**
     *  Decides whether two cards have equal value.
     * @param thatCard 
     * @return True if both card numbers are equal
     */
    public boolean isEqualValue(card thatCard){
        return this.returnNumber() == thatCard.returnNumber();
    }
    
    /**
     *  Decides whether two cards are from same suit or not
     * @param thatCard
     * @return True is both cards are from same suit
     */
    public boolean isSameSuit(card thatCard){
        return this.returnSuit().equals(thatCard.returnSuit());
    }
    
}
