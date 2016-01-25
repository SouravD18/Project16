package equity.poker;

import static org.junit.Assert.*;
import org.junit.*;

public class EquityTest {
    public static final double delta = 0.01;
    public static final double strictDelta = 0.005;
    public static final int numSimulations = 5000;
    static {
        Enumerator.init();
    }
    
    @Test
    public void testPreFlopEquity(){
        String[] board = {};
        String[] myCards = {"Jc", "6s", "3s", "Ts"};
        double result = Main.getEquity(board, myCards, numSimulations);
        assertEquals(.4704, result, delta);
    }

    @Test
    public void testPreFlopEquity2(){
        String[] board = {};
        String[] myCards = {"Th", "5s", "Qd", "3c"};
        double result = Main.getEquity(board, myCards, numSimulations);
        assertEquals(.4525, result, delta);
    }
    
    @Test
    public void testPreFlopEquity3(){
        String[] board = {};
        String[] myCards = {"7d", "Kh", "5c", "3h"};
        double result = Main.getEquity(board, myCards, numSimulations);
        assertEquals(.4663, result, delta);
    }
    
    @Test
    public void testPreFlopEquity4(){
        String[] board = {};
        String[] myCards = {"5s", "7c", "9h", "Jc"};
        double result = Main.getEquity(board, myCards, numSimulations);
        assertEquals(.4973, result, delta);
    }
    
    @Test
    public void testPreFlopEquity5(){
        String[] board = {};
        String[] myCards = {"Ah", "9h", "9d", "Qc"};
        double result = Main.getEquity(board, myCards, numSimulations);
        assertEquals(.5908, result, delta);
    }
    
    @Test
    public void testPreFlopEquity6(){
        String[] board = {};
        String[] myCards = {"Kc", "8h", "Kd", "Ks"};
        double result = Main.getEquity(board, myCards, numSimulations);
        assertEquals(.5206, result, delta);
    }
    
    @Test
    public void testFlopEquity(){
        String[] board = {"7d", "Ks", "5s"};
        String[] myCards = {"7s", "Jc", "9h", "7h"};
        double result = Main.getEquity(board, myCards, numSimulations);
        assertEquals(.7985, result, delta);
    }
    
    @Test
    public void testFlopEquity2(){
        String[] board = {"Kh", "8d", "6s"};
        String[] myCards = {"3h", "5d", "Th", "7d"};
        double result = Main.getEquity(board, myCards, numSimulations);
        assertEquals(.4325, result, delta);
    }
    
    @Test
    public void testTurnEquity(){
        String[] board = {"7d", "Ks", "5s", "As"};
        String[] myCards = {"7s", "Jc", "9h", "7h"};
        double result = Main.getEquity(board, myCards, numSimulations);
        assertEquals(.7321, result, delta);
    }
    
    @Test
    public void testTurnEquity2(){
        String[] board = {"Ah", "5h", "3c", "9d"};
        String[] myCards = {"Ks", "5d", "9h", "3d"};
        double result = Main.getEquity(board, myCards, numSimulations);
        assertEquals(.6306, result, delta);
    }
    
    @Test
    public void testRiverEquity(){
        String[] board = {"4c", "Kh", "8s", "4h", "3d"};
        String[] myCards = {"5d", "6c", "Ks", "9d"};
        double result = Main.getEquity(board, myCards, numSimulations);
        assertEquals(.6195, result, delta);
    }
    
    @Test
    public void testRiverEquity2(){
        String[] board = {"Js", "7c", "Qc", "2s", "Ks"};
        String[] myCards = {"2c", "2d", "Kd", "Jc"};
        double result = Main.getEquity(board, myCards, numSimulations);
        assertEquals(.6106, result, delta);
    }
    
//    @Test
//    public void testTurnEquityExact(){
//        String[] board = {"Jd", "9h", "Ad", "2c"};
//        String[] myCards = {"Qs", "3d", "Th", "5h"};
//        double result = Main.getEquity(board, myCards, 0);
//        assertEquals(.3387, result, strictDelta);
//    }
    
    @Test
    public void testRiverEquityExact(){
        String[] board = {"4c", "Kh", "8s", "4h", "3d"};
        String[] myCards = {"5d", "6c", "Ks", "9d"};
        double result = Main.getEquity(board, myCards, 0);
        assertEquals(.6195, result, strictDelta);
    }
}
