package pb.calc;

import pb.calc.card;

public class hand {
    
    private final card[] cards = new card[2];
    
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
    
    private void checkRep(){
        assert this.cards.length == 2;
    }
    
    public card returnHighCard(){
        return new card(cards[0]);
    }
    
    public card returnSecondCard(){
        return new card(cards[1]);
    }
    
    public double odds(){
        return 0; 
    }
}

