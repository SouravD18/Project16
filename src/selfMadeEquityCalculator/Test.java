package selfMadeEquityCalculator;
import java.util.HashMap;

public class Test {
    public static void main(String[] args){
        Long startTime = System.nanoTime();
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        Long endTime = System.nanoTime();
        System.out.println((endTime - startTime) / 1000.0);
    }

}
