package equity.poker;

public class Main{

    public static int threads = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        String[] board = {"6d", "9c", "4c"}; 
        String[] myCards = {"As", "4d", "2d", "Jc"};
        
        double equity = getEquity(board, myCards);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
        System.out.println("Equity is " + equity);
        System.out.println("Duration is " + (duration/1000000.0));

    }
    
    /**
     * Gets the equity of your own set of cards
     * @param board Known board cards (Must have either 0, 3, 4, or 5 cards)
     * @param myCards Your own set of cards (Must have 4 cards)
     * @return Equity
     */
    public static double getEquity(String[] board, String[] myCards){
        Enumerator[] enumerators = new Enumerator[threads];
        for (int i = 0; i < enumerators.length; i++) {
            enumerators[i] = new Enumerator(i, threads, myCards, board);
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
            wins += e.wins[0];
            splits += e.splits[0];
            losses += e.losses[0];
        }
        System.out.println("Number of trials is " + (wins + splits + losses));
        return (wins + splits/2.0) / (wins + splits + losses);
    }
}



