package equity.poker;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class Main{

    public static int threads = Runtime.getRuntime().availableProcessors();
    static {
        if (threads <= 0)
            threads = 1;
    }
 
    public static void main(String[] args) throws IOException {
        Enumerator.init();
        
        for (int i = 0; i < 3000; i++){
            String[] board = {};
            String[] myCards = {"5s", "7c", "9h", "Jc"};
            double result = Main.getEquity(board, myCards, 100);
            System.out.println(result);
        }
        
//        long startTime = System.nanoTime();
//        String[] board = {"4c", "Kh", "8s", "4h", "3d"};
//        String[] myCards = {"5d", "6c", "Ks", "9d"};
//        double equity = getEquity(board, myCards, 500);
//        long endTime = System.nanoTime();
//        double duration = ((endTime - startTime))/1000000.0;  //divide by 1000000 to get milliseconds.
//        System.out.println("Duration is " + duration + " milliseconds");
//        System.out.println(equity);
    }

    /**
     * Get the equity, same as below except default numSimulations per core of 500
     * @param board
     * @param myCards
     * @return
     */
    public static double getEquity(String[] board, String[] myCards){
        return getEquity(board, myCards, 400);
    }

    /**
     * Gets the equity of your own set of cards
     * @param board Known board cards (Must have either 0, 3, 4, or 5 cards)
     * @param myCards Your own set of cards (Must have 4 cards)
     * @param numSimulations If equal to 0, it enums every possibility, else sets # of simulations per core
     * @return Equity
     */
    public static double getEquity(String[] board, String[] myCards, int numSimulations){
        if (board.length == 0){
            long cardSerial = Enumerator.cardMap.get(myCards[0]);
            cardSerial = cardSerial | Enumerator.cardMap.get(myCards[1]);
            cardSerial = cardSerial | Enumerator.cardMap.get(myCards[2]);
            cardSerial = cardSerial | Enumerator.cardMap.get(myCards[3]);
            return Enumerator.startingHandEquityMap.get(cardSerial);
        }

        Enumerator[] enumerators = new Enumerator[threads];
        for (int i = 0; i < enumerators.length; i++) {
            enumerators[i] = new Enumerator(i, threads, myCards, board, numSimulations);
            enumerators[i].start();
        }
        for (Enumerator enumerator : enumerators) {
            try {
                enumerator.join();
            } catch (InterruptedException never) {}
        }
        long wins = 0; //sum up results from different threads from players
        long splits = 0;
        long losses = 0;
        for (Enumerator e : enumerators){
            wins += e.wins;
            splits += e.splits;
            losses += e.losses;
        }
        
        return (wins + splits/2.0) / (wins + splits + losses);
    }

        public static void printStartingEquityDistros(int numSimulations){
            PrintWriter pr = null;
            try {
                pr = new PrintWriter(new BufferedWriter(new FileWriter("startingEquity")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            long card1, card2, card3, card4;
            String[] myCards = new String[4];
            String[] board = {};
            for (int a = 0; a < Enumerator.deckArr.length - 3; a++){
                myCards[0] = Enumerator.deckArr[a];
                card1 = Enumerator.cardMap.get(myCards[0]);
                for (int b = a + 1; b < Enumerator.deckArr.length - 2; b++){
                    myCards[1] = Enumerator.deckArr[b];
                    System.out.println("Currently at " + myCards[1]);
                    card2 = card1 | Enumerator.cardMap.get(myCards[1]);
                    for (int c = b + 1; c < Enumerator.deckArr.length - 1; c++){
                        myCards[2] = Enumerator.deckArr[c];
                        card3 = card2 | Enumerator.cardMap.get(myCards[2]);
                        for (int d = c + 1; d < Enumerator.deckArr.length; d++){
                            myCards[3] = Enumerator.deckArr[d];
                            card4 = card3 | Enumerator.cardMap.get(myCards[3]);
                            double equity = getEquity(board, myCards, numSimulations);
                            pr.println(card4 + " " + equity);
                        }
                    }
                }
            }
            pr.close();
        }
}



