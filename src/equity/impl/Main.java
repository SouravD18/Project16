package equity.impl;

import equity.Equity;
import equity.MEquity;

/**
 * This is a class with only one method that can calculate equity. The method's name is equity.
 * Description is given below
 * @author Sourav
 *
 */
public class Main {
    public Main(){
    }
    /**
     * So cards are represented by 
     *      face number first(2,3,4...9 are numbers. and T,J,Q,K,A are letters) 
     *      with capital letters for (T,J,Q,K,A)
     *      and then the suit in small letters (s=Spades, h=Hearts, c=Clubs, d=Diamonds).
     *      Example: "2c" for two of clubs;"Ts" for Ten of spades; 
     *          "Jd" for Jack of Diamonds and etc.
     *      
     * @param board is a String[] where the Strings are the names of 
     *          the board cards. board.length < 6 
     * @param myCards is a String[] where the Strings are the names of 
     *          our bot's cards. myCards > 1
     * @param opponentCards is a String[] where the Strings are the names of 
     *          opponent bot's cards. myCards > 1
     * @return equity of our hand (a double)
     */
    public float equity(String[] board, String[] myCards){
        /// Not sure whether we need to assert or not
        //assert board.length < 6;
        //assert myCards.length > 1;
        //assert opponentCards.length > 1;

        
        return ( new HEPoker() )
                .equity(board.clone(), myCards, new String[]{});
    }
    
    public static void main(String[] args) {
        String[] board = {"6d", "6c", "5h"};
        String[] myCards = {"Td","4c","7h","As"};
        String[] opponentCards = {"Kc","Qh", "3s", "5s"};
        
        long startTime = System.nanoTime();
        
        double a = ((new Main()).equity(board, myCards) );
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
        System.out.println(a);
        System.out.println(duration/1000000.0);
        
    }
}
