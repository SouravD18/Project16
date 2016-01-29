package player;

public class Action {
    /**
     * Number of Actions and their description:
     * (These actions are performed actions)
     *  
     *  BET:    "BET:amount[:actor]"
     *  CALL:   "CALL[:actor]"
     *  CHECK:  "CHECK[:actor]"
     *  DEAL:   "DEAL:STREET"  --> STREET can be "FLOP", "TURN", "RIVER"
     *  FOLD:   "FOLD[:actor]" 
     *  POST:   "POST:amount:actor"
     *  RAISE:  "RAISE:amount[:actor]"
     *  REFUND: "REFUND:amount:actor"
     *  SHOW:   "SHOW:card1:card2:card3:card4:actor"
     *  TIE:    "TIE:amount:actor"
     *  WIN:    "WIN:amount:actor"
     *  
     */
    
    private String actionType;
    private int amount;
    // actor = "NONE" when no played made the action
    private String actor;
    private String STREET;
    private String[] opponentCards = new String[4];
    
    public Action(String givenString){
        String[] words = givenString.split(":");
        this.actionType = words[0];
        if(this.actionType.equals("DEAL")){
            this.actor = "NONE";
            this.STREET = words[1];
            this.amount = 0;
        }
        else if(this.actionType.equals("SHOW")){
            this.actor = "NONE";
            
            this.opponentCards[0] = words[1];
            this.opponentCards[1] = words[2];
            this.opponentCards[2] = words[3];
            this.opponentCards[3] = words[4];
            
            this.amount = 0;
            this.STREET = "";
            
        }
        else if(words.length == 2){
            this.actor = words[1];
            this.amount = 0;
            this.STREET = "";
        }
        else{
            this.actor = words[2];
            this.amount = Integer.parseInt(words[1]);
            this.STREET = "";
        }
    }
    
    public String actionType(){
        return this.actionType;
    }
    
    public String actor(){
        return this.actor;
    }
    
    public int amount(){
        return this.amount;
    }
    
    public String STREET(){
        return this.STREET;
    }
    
    public String[] opponentCards(){
        return this.opponentCards;
    }
}
