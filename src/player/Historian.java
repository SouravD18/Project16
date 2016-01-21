package player;

public class Historian {
    //
    //  Helper fields for Historian
    
    // PreFlop Stats: 
    int vpip = 0;
    int pfr = 0;
        //From Small Blind
    int limp = 0;
    int fourBet = 0;
        //From Big Blind
    int limpRaise = 0;
    int threeBet = 0;
    int foldToFourBet = 0;
    
    // Flop Stats:
    int continuationBet = 0;
    int foldToContinuation_flop = 0;
    int checkRaise = 0;
    int threeBet_flop = 0;
    
    // Post-Flop Stats:
    int betOrRaiseCount = 0;
    int callCount = 0;
    double aggressionFactor_post = 0.0;
    int winSD_post = 0;
    int wentSD_post = 0;
    
    // Game Stats:
    int numberOfHands = 0;
    int winCount = 0;
    int foldCount = 0;
    
    public void Historian(){
    }
    
    public void printAll(){
        System.out.println("vpip: "+vpip);
        System.out.println("pfr: "+pfr);
        System.out.println("limp: "+limp);
        System.out.println("fourBet: "+fourBet);
        System.out.println("limpRaise: "+limpRaise);
        System.out.println("threeBet: "+threeBet);
        System.out.println("foldToFourBet: "+foldToFourBet);
        System.out.println("Continuation Bet: "+continuationBet);
        System.out.println("foldToContinuation: "+foldToContinuation_flop);
        System.out.println("checkRaise: "+checkRaise);
        System.out.println("threeBet_flop: "+threeBet_flop);
        System.out.println("betOrRaiseCount: "+betOrRaiseCount);
        System.out.println("callCount: "+callCount);
        System.out.println("winSD_post: "+winSD_post);
        System.out.println("wentSD_post: "+wentSD_post);
        System.out.println("numberOfHands: "+numberOfHands);
        System.out.println("winCount: "+winCount);
        System.out.println("foldCount: "+foldCount);
    }
}
