package analyzer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Finds the net gain for each of the stages (pre-flop, flop, turn, river) for a dump file
 * @author unive
 *
 */
public class DumpAnalyzer {
    public static final String TEAM_NAME = "player1";
    public static final String DIRECTORY = "C:/Users/unive/Desktop/Hand Logs";
    
    public static void main(String[] args) throws IOException{
        List<File> files = allFilesInFolder(new File(DIRECTORY));
        for (File f: files)
            printMonies(f);
    }
    
    public static List<File> allFilesInFolder(File folder){
        List<File> files = new ArrayList<>();
        for (File fileEntry: folder.listFiles()){
            if (fileEntry.isFile()){
                files.add(fileEntry);
            }
        }
        return files;
    }
    
    public static void printInformation(File f) throws IOException{
        int currentLevel = 0; //0 = pre-flop, 1 = flop, 2 = turn, 3 = river
        int[] monies = {0, 0, 0, 0};
        BufferedReader br = new BufferedReader(new FileReader(f));
        String[] title = br.readLine().split(" ");
        System.out.println(title[4] + " " + title[5] + " " + title[6]);
        
        String currentLine;
        while ((currentLine = br.readLine()) != null){
            if (currentLine.contains("Hand #"))
                currentLevel = 0;
            else if (currentLine.contains("FLOP"))
                currentLevel = 1;
            else if (currentLine.contains("TURN"))
                currentLevel = 2;
            else if (currentLine.contains("RIVER"))
                currentLevel = 3;
            else if (currentLine.contains("wins the pot")){
                String[] strings = currentLine.split(" ");
                int value = Integer.parseInt(strings[4].substring(1, strings[4].length() - 1));
                if (strings[0].equals(TEAM_NAME))
                    monies[currentLevel] += value/2;
                else monies[currentLevel] -= value/2;
            }
        }

        System.out.println(Arrays.toString(monies));
        br.close();
    }
    
    public static void printMonies(File f) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(f));
        String currentLine;
        while ((currentLine = br.readLine()) != null){
            if (currentLine.contains("wins the pot")){
                String[] strings = currentLine.split(" ");
                int value = Integer.parseInt(strings[4].substring(1, strings[4].length() - 1));
                System.out.println(value);
            }
        }
        
        br.close();
    }
    
    
}
