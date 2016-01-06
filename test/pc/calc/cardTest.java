package pc.calc;

import static org.junit.Assert.*;

import org.junit.Test;

import pb.calc.card;

public class cardTest {
    
    /**
     * Testing Strategy: All functions in this class are pretty Straight Forward to understand.
     *      So only adding one test case to test evverything 
     *      
     */
    
    @Test
    public void testEverything() {
        card spadesAce = new card("S", 14);
        card diamondsAce = new card("d", 14);
        card clubsSeven = new card("c", 7);
        card heartsTwo = new card("H", 2);
        card heartsJack = new card("h", 11);
        
        // Check isBigger
        assertTrue(spadesAce.isBigger(heartsTwo));
        
        // Check isSmaller
        assertTrue(heartsTwo.isSmaller(clubsSeven));
        
        // Check isEqualValue
        assertTrue(spadesAce.isEqualValue(diamondsAce));
        
        // Check isSameSuit
        assertTrue(heartsTwo.isSameSuit(heartsJack));
        
        // Check returnNumber
        assertTrue(clubsSeven.returnNumber() == 7);
        
        // Check returnSuit
        assertTrue(heartsJack.returnSuit().equals("h"));
    }

}
