package equity.poker;

public class Main{

    public static int threads = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        
        String[] board = {"6d", "6c", "5h", "5d", "Ad"}; 
        String[] myCards = {"Td", "4c", "7h", "As"};
        
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

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
        System.out.println(duration/1000000.0);
        System.out.println("number of wins is " + wins);
        System.out.println("number of partial pots is " + splits/2.0);
        System.out.println("number of hands total is " + (wins + splits + losses));
        System.out.println("Equity is " + (wins + splits/2.0) / (wins + splits + losses));
    }
}



