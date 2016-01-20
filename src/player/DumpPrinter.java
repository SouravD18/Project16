package player;

public class DumpPrinter {
    
    Integer[] checker = {
                        0,0,0,0,0,
                        0,0,0,0,0,
                        0,0,0,0,0,
                        0,0,0,0,0
                        };
    
    public String print(String stage, String[] holeCards, String[] boardCards, double equity){
        StringBuilder result = new StringBuilder();
        
        StringBuilder hole = new StringBuilder();
        for(String card: holeCards){
            hole.append(card);
            hole.append(" ");
        }
        
        StringBuilder board = new StringBuilder();
        for(String card: boardCards){
            board.append(card);
            board.append(" ");
        }
        
        result.append(stage);
        result.append("\n");
        result.append("HoleCards: ");
        result.append(hole.toString());
        result.append("\n");
        result.append("BoardCards: ");
        result.append(board.toString());
        result.append("\n");
        result.append("Equity: ");
        result.append(Double.toString(equity));
        
        return result.toString();
    }
    
    public void counter(double probability){
        long rounding = Math.round(probability*20);
        if(rounding < 20){
            checker[(int) rounding] += 1;
        }
        else{
            checker[19]+=1;
        }
    }
    
    public void printStats(){
        for(int i = 0; i < 20; i++){
            System.out.println(this.checker[i]);
        }
    }
}
