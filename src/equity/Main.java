package equity;

public class Main{
    public static void main(String[] args) {
        String[] board = {"6d", "6c", "5h"};
        String[] myCards = {"Td","4c","7h","As"};
        String[] opponentCards = {"Kc","Qh", "3s", "5s"};
        
        long startTime = System.nanoTime();
        
        double a = ((new Main()).equity(board, myCards, opponentCards) );
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
        System.out.println(duration/1000000.0);
        System.out.println("equity is " + a);
    }
}



