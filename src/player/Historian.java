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
    
    int foldInPost = 0;
    int wentToPost = 0;
    
    int my4bet = 0;
    
    // Went to stages:
    int wentToFlop = 0;
    int wentToTurn = 0;
    int wentToRiver = 0;
    
    // First Betting:
    int flopBet = 0;
    int turnBet = 0;
    int riverBet = 0;
    
    // Folding:
    int foldFlop = 0;
    int foldTurn = 0;
    int foldRiver = 0;
    
    // Check-Raise:
    int checkRaiseFlop = 0;
    int checkRaiseTurn = 0;
    int checkRaiseRiver = 0;
    
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

        System.out.println("Flop bet freq.: "+flopBettingFrequencies());
        System.out.println("Flop CheckRaiseFreq.: "+flopCheckRaiseFrequencies());
        System.out.println("Flop_Flod: "+flopFoldingFrequencies());
        
        System.out.println("Turn bet: "+turnBettingFrequencies());
        System.out.println("Turn check raise: "+turnCheckRaiseFrequencies());
        System.out.println("Turn fold: "+turnFoldingFrequencies());
        
        System.out.println("River bet: "+riverBettingFrequencies());
        System.out.println("River check raise: "+riverCheckRaiseFrequencies());
        System.out.println("River fold: "+riverFoldingFrequencies());
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
    
    public double foldPercentInPost(){
        double answer = 0.0;
        if(wentToPost > 0){
            answer = ((double) foldInPost) / wentToPost;
        }
        return answer;
    }
    public double flopBettingFrequencies(){
        double answer = 0.0;
        if(wentToFlop > 0){
            answer = ((double) flopBet) / wentToFlop;
        }
        return answer;
    }
    
    public double flopCheckRaiseFrequencies(){
        double answer = 0.0;
        if(wentToFlop > 0){
            answer = ((double) checkRaiseFlop) / wentToFlop;
        }
        return answer;
    }
    
    public double flopFoldingFrequencies(){
        double answer = 0.0;
        if(wentToFlop > 0){
            answer = ((double) foldFlop) / wentToFlop;
        }
        return answer;
    }
    
    public double turnBettingFrequencies(){
        double answer = 0.0;
        if(wentToTurn > 0){
            answer = ((double) turnBet) / wentToTurn;
        }
        return answer;
    }
    
    public double turnCheckRaiseFrequencies(){
        double answer = 0.0;
        if(wentToTurn > 0){
            answer = ((double) checkRaiseTurn) / wentToTurn;
        }
        return answer;
    }
    
    public double turnFoldingFrequencies(){
        double answer = 0.0;
        if(wentToTurn > 0){
            answer = ((double) foldTurn) / wentToTurn;
        }
        return answer;
    }
    
    public double riverBettingFrequencies(){
        double answer = 0.0;
        if(wentToRiver > 0){
            answer = ((double) riverBet) / wentToRiver;
        }
        return answer;
    }
    
    public double riverCheckRaiseFrequencies(){
        double answer = 0.0;
        if(wentToRiver > 0){
            answer = ((double) checkRaiseRiver) / wentToRiver;
        }
        return answer;
    }
    
    public double riverFoldingFrequencies(){
        double answer = 0.0;
        if(wentToRiver > 0){
            answer = ((double) foldRiver) / wentToRiver;
        }
        return answer;
    }
}
