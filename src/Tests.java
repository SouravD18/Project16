import static org.junit.Assert.*;

import org.junit.Test;

import equity.impl.*;

public class Tests {

    @Test
    public void test() {
        String[] board = {"6d", "6c", "5h"};
        String[] myCards = {"Td","4c","7h","As"};
        String[] opponentCards = {"Kc","Qh", "3s", "5s"};
        // I checked the answer in another online poker hand calculator.
        // Link:    http://omahacalculator.com/
        //
        // But still slow :( . Help me optimize
        //
        System.out.println(  (new Main()).equity(board, myCards, opponentCards) );
        
    }

}
