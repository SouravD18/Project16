package equity.poker;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;

import gnu.trove.map.TIntDoubleMap;

public class Main{

    public static int threads = Runtime.getRuntime().availableProcessors();
    static {
        if (threads <= 0)
            threads = 1;
    }
 
    public static void main(String[] args) throws IOException {
        Enumerator.init();
        String[] board = "Qd 5s Ac 9c 5h".split(" ");
        String[] myCards = "Tc As 6s 8h".split(" ");
        String[] opponentCards = "Kc 7c 5d Jh".split(" ");
       
        System.out.println("My equity is " + getEquity(board, myCards, 1000));
        System.out.println("My percentile is " + convertEquityToPercentile(getEquity(board, myCards, 1000), 5));
        System.out.println("Opponent equity is " + getEquity(board, opponentCards, 1000));
    }

    /**
     * Get the equity, same as below except default numSimulations per core of 500
     * @param board
     * @param myCards
     * @return
     */
    public static double getEquity(String[] board, String[] myCards){
        return getEquity(board, myCards, 5000);
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
    
    /**
     * Converts equity to percentile
     * @param equity 
     * @param turn 0 = preflop, 3 = flop, 4 = turn, 5 = river
     */
    public static double convertEquityToPercentile(double equity, int turn){
        TIntDoubleMap map;
        switch (turn){
        case 0:
            map = Enumerator.equityPreFlopPercentileMap;
            break;
        case 3:
            map = Enumerator.equityFlopPercentileMap;
            break;
        case 4:
            map = Enumerator.equityTurnPercentileMap;
            break;
        case 5:
            map = Enumerator.equityRiverPercentileMap;
            break;
        default:
            return 0.5;
        }
        
        if (equity <= 0 || equity >= 1)
            return equity;
        
        equity *= 100;
        int intEquity = (int) equity;
        double remaining = equity - intEquity;
        
        return (1-remaining)*map.get(intEquity) + (remaining)*map.get(intEquity+1);
    }
    
//  public static void printAverageTime(int numSimulations){
//  String[] boardCards = new String[5];
//  String[] handCards = new String[4];
//  long totalTime = 0;
//  for (int i = 0; i < 1; i++){
//      pickRandomCards(boardCards, handCards);
//      long startTime = System.nanoTime();
//      
//      getEquity(new String[] {}, handCards, numSimulations);
//      String[] turnHand = Arrays.copyOfRange(boardCards, 0, 3);
//      getEquity(turnHand, handCards, numSimulations);
//      getEquity(boardCards, handCards, numSimulations);
//      totalTime += System.nanoTime() - startTime;
//  }
//  System.out.println("On average simulation took " + (totalTime / 1)/1e6 + " milliseconds");
//}
//
//public static void pickRandomCards(String[] boardCards, String[] handCards){
//  int[] numbers = new int[52];
//  for (int i = 0; i < numbers.length; i++)
//      numbers[i] = i;
//  shuffleArray(numbers);
//  for (int i = 0; i < boardCards.length; i++)
//      boardCards[i] = Enumerator.deckArr[numbers[i]];
//  for (int i = 0; i < handCards.length; i++)
//      handCards[i] = Enumerator.deckArr[numbers[i + boardCards.length]];
//}
//
//public static void shuffleArray(int[] array){
//  Random r = new Random();
//  int index;
//  for (int i = array.length - 1; i > 0; i--)
//  {
//      index = r.nextInt(i + 1);
//      if (index != i)
//      {
//          array[index] ^= array[i];
//          array[i] ^= array[index];
//          array[index] ^= array[i];
//      }
//  }
//}
    
//  /**
//  * Returns the value of an Omaha 9-card hand
//  * @param board The 5 cards on the board
//  * @param hand The 4 cards on your hand
//  * @return Value, as specified in HandEval
//  */
// public static int handEval(String[] board, String[] hand){
//     long[] boardCards = new long[5];
//     long[] handCards = new long[4];
//     for (int i = 0; i < boardCards.length; i++){
//         boardCards[i] = Enumerator.cardMap.get(board[i]);
//     }
//     for (int i = 0; i < hand.length; i++){
//         handCards[i] = Enumerator.cardMap.get(hand[i]);
//     }
//     return HandEval.OmahaHighEval(boardCards, handCards);
// }
// 
// /**
//  * 
//  * @param board The 5 cards on the board 
//  * @param yourHand The 4 cards on your hand
//  * @param opponentHand The 4 cards on the opponent's hand
//  * @return Returns 1 if your hand wins, 0 if ties, and -1 if your hand loses
//  */
// public static int youWin(String[] board, String[] yourHand, String[] opponentHand){
//     int yourValue = handEval(board, yourHand);
//     int opponentValue = handEval(board, opponentHand);
//     if (yourValue > opponentValue)
//         return 1;
//     if (yourValue == opponentValue)
//         return 0;
//     else
//         return -1;
// }
    
//  public static void printStartingEquityDistros(int numSimulations){
//  PrintWriter pr = null;
//  try {
//      pr = new PrintWriter(new BufferedWriter(new FileWriter("startingEquity")));
//  } catch (IOException e) {
//      e.printStackTrace();
//  }
//  
//  long card1, card2, card3, card4;
//  String[] myCards = new String[4];
//  String[] board = {};
//  for (int a = 0; a < Enumerator.deckArr.length - 3; a++){
//      myCards[0] = Enumerator.deckArr[a];
//      card1 = Enumerator.cardMap.get(myCards[0]);
//      for (int b = a + 1; b < Enumerator.deckArr.length - 2; b++){
//          myCards[1] = Enumerator.deckArr[b];
//          System.out.println("Currently at " + myCards[1]);
//          card2 = card1 | Enumerator.cardMap.get(myCards[1]);
//          for (int c = b + 1; c < Enumerator.deckArr.length - 1; c++){
//              myCards[2] = Enumerator.deckArr[c];
//              card3 = card2 | Enumerator.cardMap.get(myCards[2]);
//              for (int d = c + 1; d < Enumerator.deckArr.length; d++){
//                  myCards[3] = Enumerator.deckArr[d];
//                  card4 = card3 | Enumerator.cardMap.get(myCards[3]);
//                  double equity = getEquity(board, myCards, numSimulations);
//                  pr.println(card4 + " " + equity);
//              }
//          }
//      }
//  }
//  pr.close();
//}
}



