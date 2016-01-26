package player;

public class ProcessActions {
    
    public boolean checkPossible = false;
    public boolean betPossible = false;
    boolean raisePossible = false;
    // Upper range of bet
    public int upperRange = 0;
    // Lower range of bet
    public int lowerRange = 0;
    
    // Helper field to calculate call amount:
    public int moneyGiven = 0;
    public int opponentMoney = 0;
    
    /**
     * process actions
     * @param legalAction: String with legal actions
     */
    public void process(String[] legalActions, Action[] actions){
        checkPossible = false;
        betPossible = false;
        raisePossible = false;
        for(String action: legalActions){
            // Checks if call or check
            if(action.split(":")[0].equals("CHECK")){
                this.checkPossible = true;
                // Whenever check is possible, both of use have no money:
                this.moneyGiven = 0;
                this.opponentMoney = 0;                       
            }
            else if(action.split(":")[0].equals("CALL")){
                this.checkPossible = false;
            }
            else if(action.split(":")[0].equals("BET")){
                this.betPossible = true;
                this.raisePossible = false;
                this.lowerRange = Integer.parseInt(action.split(":")[1]);
                this.upperRange = Integer.parseInt(action.split(":")[2]);
            }
            else if(action.split(":")[0].equals("RAISE")){
                this.betPossible = false; 
                this.raisePossible = true;
                this.lowerRange = Integer.parseInt(action.split(":")[1]);
                this.upperRange = Integer.parseInt(action.split(":")[2]);
            }
        }
        
        if(actions[2].actionType().equals("POST")){
            this.moneyGiven = 1;
            this.opponentMoney = 2;
        }
        // If actor is opponent
        else if(!(actions[2].actor().equals("NONE"))){
            if(actions[0].actionType().equals("POST")){
                if(actions[2].actionType().equals("RAISE")){
                    this.opponentMoney = actions[2].amount() + 0;
                    this.moneyGiven = 2;
                }
            }
            else if(actions[2].actionType().equals("BET")){
                this.opponentMoney = actions[2].amount() + 0;
                this.moneyGiven = 0;
            }
            else if(actions[2].actionType().equals("RAISE")){
                this.opponentMoney = actions[2].amount() + 0;
            }
        }
    }
    // Now, some action classes to help out making actions
    /**
     * 
     * @return Will check if call is a valid action and return "CALL"/ "CHECK"
     */
    public String call(){
        // If check possible, then check. Otherwise call
        if(this.checkPossible){
            return "CHECK";
        }
        else{
            this.moneyGiven = this.opponentMoney + 0;
            return "CALL";
        }
    }
    /**
     * 
     * @return Will return CHECK or FOLD accordingly
     */
    public String check(){
        // If check possible, then check. Otherwise fold
        if(this.checkPossible){
            return "CHECK";
        }
        else{
            return "FOLD";
        }
    }
    /**
     * 
     * @return Will return CHECK or FOLD accordingly
     */
    public String fold(){
        // If check possible, then check. Otherwise fold
        if(this.checkPossible){
            return "CHECK";
        }
        else{
            return "FOLD";
        }
    }
    /**
     *  return bet/raise accordingly.
     * @param amount
     * @return amount if valid. Otherwise nearest min/max bet
     */
    public String bet(int amount){
        String betOrRaise;
        if(this.betPossible){
            betOrRaise = "BET:";
        }
        else if(this.raisePossible){
            betOrRaise = "RAISE:";
        }
        else{
            return call();
        }
        int actualAmount = amount + callAmount();
        if( actualAmount < this.lowerRange){
            this.moneyGiven = this.lowerRange + 0;
            return betOrRaise.concat(Integer.toString(this.lowerRange));
        }
        else if(actualAmount > this.upperRange){
            this.moneyGiven = this.upperRange + 0;
            return betOrRaise.concat(Integer.toString(this.upperRange));
        }
        else{
            this.moneyGiven = actualAmount + 0;
            return betOrRaise.concat(Integer.toString(actualAmount));
        }
    }
    /**
     * 
     * @return Amount used to call
     */
    public int callAmount(){
        if(this.betPossible){
            return 0;
        }
        else{
            return (this.opponentMoney - this.moneyGiven);
        }
    }
    
    public boolean isBet(){
        return this.betPossible;
    }
    
    public int minBet(){
        return this.lowerRange - this.opponentMoney;
    }
    
    public int maxBet(){
        return this.upperRange - this.opponentMoney;
    }
}
