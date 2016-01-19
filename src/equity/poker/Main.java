package equity.poker;

public class Main{

    public static int threads = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        
        String[] board = {"6d", "6c", "5h", "5d"}; //
        String[] myCards = {"Td","4c","7h","As"}; //"Td","4c","7h","As"
        String[] opponentCards = {};//"Kc","Qh", "3s", "5s"}; //"Kc","Qh", "3s", "5s"
        String[][] holeCards = {myCards, opponentCards};
        
        Enumerator[] enumerators = new Enumerator[threads];
        for (int i = 0; i < enumerators.length; i++) {
            enumerators[i] = new Enumerator(i, threads, holeCards, board);
            enumerators[i].start();
        }
        for (Enumerator enumerator : enumerators) {
            try {
                enumerator.join();
            } catch (InterruptedException never) {}
        }

        long[] wins = new long[2], splits = new long[2]; //sum up results from different threads from players
        double[] partialPots = new double[2];
        for (Enumerator e : enumerators)
            for (int i = 0; i < 2; i++) {
                wins[i] += e.getWins()[i];
                splits[i] += e.getSplits()[i];
                partialPots[i] += e.getPartialPots()[i];
            }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
        System.out.println(duration/1000000.0);
        System.out.println("number of wins is " + wins[0]);
        System.out.println("number of partial pots is " + partialPots[0]);
        System.out.println("number of hands total is " + (wins[0] + wins[1] + splits[0]));
        System.out.println("Equity is " + (wins[0] + partialPots[0]) / (wins[0] + wins[1] + splits[0]));
    }
}



