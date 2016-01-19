package player;

public class ProcessActions {
    
    boolean checkPossible = false;
    boolean betPossible = true;
    // Upper range of bet
    int upperRange = 0;
    // Lower range of bet
    int lowerRange = 0;
    
    /**
     * Constructor
     */
    public void processActions(){
        
    }
    
    /**
     * process actions
     * @param legalAction: String with legal actions
     */
    public void process(String[] legalActions){
        for(String action: legalActions){
            // Checks if call or check
            if(action.split(":")[0].equals("CHECK")){
                this.checkPossible = true;
            }
            else if(action.split(":")[0].equals("CALL")){
                this.checkPossible = false;
            }
            else if(action.split(":")[0].equals("BET")){
                this.betPossible = true;
                this.lowerRange = Integer.parseInt(action.split(":")[1]);
                this.upperRange = Integer.parseInt(action.split(":")[2]);
            }
            else if(action.split(":")[0].equals("RAISE")){
                this.betPossible = false; 
                this.lowerRange = Integer.parseInt(action.split(":")[1]);
                this.upperRange = Integer.parseInt(action.split(":")[2]);
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
            betOrRaise = "BET";
        }
        else{
            betOrRaise = "RAISE";
        }
        
        if(amount < this.lowerRange){
            return betOrRaise.concat(Integer.toString(this.lowerRange));
        }
        else if(amount > this.upperRange){
            return betOrRaise.concat(Integer.toString(this.upperRange));
        }
        else{
            return betOrRaise.concat(Integer.toString(amount));
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
            // RN, I am assuming this will be the call size
            return this.lowerRange;
        }
    }

}
