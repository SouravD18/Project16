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
    int winSD_post = 0;
    int wentSD_post = 0;
    
    // Game Stats:
    int numberOfHands = 0;
    int winCount = 0;
    int foldCount = 0;
    
    int my4bet = 0;
    
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
        
        System.out.println("\n");
        
        System.out.println("3Bet percent: " + threeBetPercent());
        System.out.println("fold4bet/3bet percent: " + foldToFourBetVsThreeBet());
        System.out.println("pfr percent: "+pfrPercent());
        System.out.println("cBet percent: "+continuationBetPercent());
        System.out.println("foldCBet percent: " + foldToContinuationBetPercent());
        System.out.println("checkRaising percent: " + checkRaisingPercent());
        System.out.println("aggessionFactor: "+aggressionFactor());
        System.out.println("Win ShowDown: "+sdWinPercent());
        System.out.println("Went ShowDown: "+sdWentPercent());
        
    }
    /**
     *  (number of three bet) / (number of BB)
     *   
     * @return (3-bet* 2) / (number of hands)
     */
    public double threeBetPercent(){
        double answer = (threeBet * 2.0)/ (numberOfHands);
        return answer;
    }
    /**
     * Ratio foldTo4Bet vs. ThreeBet
     * @return
     */
    public double foldToFourBetVsThreeBet(){
        double answer = 0.0;
        if(threeBet > 0){
            answer = ((double) foldToFourBet) / ((double) threeBet);
        }
        return answer;
    }
    /**
     * pfr / (number of hands)
     * 
     * @return pfr percentage
     */
    public double pfrPercent(){
        double answer = ((double) pfr) / numberOfHands;
        return answer;
    }
    /**
     * continuationBet / pfr 
     * @return
     */
    public double continuationBetPercent(){
        double answer = 0.0;
        if(pfr > 0){
            answer = ((double) continuationBet) / pfr;
        }
        return answer;
    }
    /**
     * foldToContinuationBet / vpip
     * @return
     */
    public double foldToContinuationBetPercent(){
        double answer = 0.0;
        if(vpip > 0){
            answer = ((double) foldToContinuation_flop) / vpip;
        }
        return answer;
    }
    /**
     * checkRaise / vpip
     * @return
     */
    public double checkRaisingPercent(){
        double answer = 0.0;
        if(vpip > 0){
            answer = ((double) checkRaise) / vpip;
        }
        return answer;
    }
    /**
     * Note that aggression factor calculation should be reviewed
     *      betOrRaiseCount / callCount
     * @return
     */
    public double aggressionFactor(){
        double answer = 0.0;
        if(callCount > 0){
            answer = ((double) betOrRaiseCount) /  callCount;
        }
        return answer;
    }
    /**
     * sdWin / sdWent
     * @return
     */
    public double sdWinPercent(){
        double answer = 0.0;
        if(wentSD_post > 0){
            answer = ((double) winSD_post) / (wentSD_post);
        }
        return answer;
    }
    /**
     * sdWent / VPIP
     * @return
     */
    public double sdWentPercent(){
        double answer = 0.0;
        if(vpip > 0){
            answer = ((double) wentSD_post) / vpip;
        }
        return answer;
    }
}
